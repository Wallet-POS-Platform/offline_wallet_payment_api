import base64
import os
import secrets
import string
import sys
import uuid
from typing import Any

import requests
from requests.auth import HTTPBasicAuth
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import rsa


API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080").rstrip("/")
API_USERNAME = os.getenv("API_USERNAME", "user")
API_PASSWORD = os.getenv("API_PASSWORD", "apple")

TIMEOUT_SECONDS = 20
CREATE_CLIENT_2_SUB_WALLET = os.getenv(
    "CREATE_CLIENT_2_SUB_WALLET", "false"
).strip().lower() in {"1", "true", "yes", "y"}


# ---------------------------------------------------------------------------
# Test data
#
# All fields exposed by the Swagger request schemas are populated, including
# optional fields. These are fictional development identities and addresses.
# Add more objects to these lists to expand the dataset.
# ---------------------------------------------------------------------------

CLIENTS = [
    {
        "label": "Client 1",
        "username_prefix": "amelia",
        "firstName": "Amelia",
        "lastName": "Schneider",
        "address": "Leopoldstraße 58, 80802 München",
        "phoneNumber": "+4915112345601",
        "countryCode": "DE",
        "currency": "EUR",
        "subWallets": [
            {
                "name": "Travel Wallet",
                "currency": "EUR",
            }
        ],
    },
    {
        "label": "Client 2",
        "username_prefix": "leon",
        "firstName": "Leon",
        "lastName": "Fischer",
        "address": "Fraunhoferstraße 17, 80469 München",
        "phoneNumber": "+4915112345602",
        "countryCode": "DE",
        "currency": "EUR",
        "subWallets": [
            {
                "name": "Personal Savings",
                "currency": "EUR",
            }
        ],
    },
]


MERCHANTS = [
    {
        "label": "Isar Corner Market",
        "username_prefix": "isar_market",
        "businessName": "Isar Corner Market GmbH",
        "category": "Retail",
        "address": "Sendlinger Straße 24, 80331 München",
        "countryCode": "DE",
        "payoutIBAN": "DE89370400440532013000",
        "settleType": "MONTHLY",
        "currency": "EUR",
        "terminals": [
            {
                "name": "Main Entrance POS",
                "certificateSerialNumber": "ISAR-POS-0001",
            },
            {
                "name": "Customer Service POS",
                "certificateSerialNumber": "ISAR-POS-0002",
            },
        ],
    }
]


def generate_public_key() -> str:
    """
    Generate an RSA public key in Base64-encoded X.509 SubjectPublicKeyInfo DER.

    The API receives the public key only. The private key is not sent or saved.
    """
    private_key = rsa.generate_private_key(
        public_exponent=65537,
        key_size=2048,
    )
    public_key_der = private_key.public_key().public_bytes(
        encoding=serialization.Encoding.DER,
        format=serialization.PublicFormat.SubjectPublicKeyInfo,
    )
    return base64.b64encode(public_key_der).decode("ascii")


def unique_username(prefix: str) -> str:
    """Generate a username within the API's 5-20 character limit."""
    normalized = "".join(
        char.lower() for char in prefix if char.isalnum() or char in "._-"
    )
    suffix = uuid.uuid4().hex[:6]
    username = f"{normalized[:13]}_{suffix}"
    return username[:20]


def strong_password() -> str:
    """Generate a password matching the Swagger password requirements."""
    alphabet = string.ascii_letters + string.digits + "!@#$%^&*"
    characters = [
        secrets.choice(string.ascii_lowercase),
        secrets.choice(string.ascii_uppercase),
        secrets.choice(string.digits),
        secrets.choice("!@#$%^&*"),
    ]
    characters.extend(secrets.choice(alphabet) for _ in range(12))
    secrets.SystemRandom().shuffle(characters)
    return "".join(characters)


class WalletPlatformApi:
    def __init__(self, base_url: str) -> None:
        self.base_url = base_url
        self.session = requests.Session()
        self.session.auth = HTTPBasicAuth(API_USERNAME, API_PASSWORD)
        self.session.headers.update(
            {
                "Accept": "application/json",
                "Content-Type": "application/json",
            }
        )

    def post(self, path: str, payload: dict[str, Any]) -> dict[str, Any]:
        url = f"{self.base_url}{path}"
        response = self.session.post(
            url,
            json=payload,
            timeout=TIMEOUT_SECONDS,
        )

        if not response.ok:
            safe_payload = {
                key: ("<redacted>" if key in {"password", "publicKey"} else value)
                for key, value in payload.items()
            }
            raise RuntimeError(
                f"POST {path} failed with HTTP {response.status_code}.\n"
                f"Response: {response.text}\n"
                f"Request payload (sensitive fields redacted): {safe_payload}"
            )

        if not response.content:
            return {}

        try:
            body = response.json()
        except requests.exceptions.JSONDecodeError as exc:
            raise RuntimeError(
                f"POST {path} returned a non-JSON response: {response.text}"
            ) from exc

        if not isinstance(body, dict):
            raise RuntimeError(
                f"POST {path} returned an unexpected response: {body!r}"
            )

        return body


def require_id(response: dict[str, Any], resource_name: str) -> str:
    resource_id = response.get("id")
    if not resource_id:
        raise RuntimeError(
            f"{resource_name} creation response does not contain 'id': {response}"
        )
    return str(resource_id)


def create_client(
    api: WalletPlatformApi,
    client_data: dict[str, Any],
) -> dict[str, Any]:
    username = unique_username(client_data["username_prefix"])

    # Every property in ClientCreationRequest is supplied.
    request_body = {
        "username": username,
        "password": strong_password(),
        "firstName": client_data["firstName"],
        "lastName": client_data["lastName"],
        "address": client_data["address"],
        "phoneNumber": client_data["phoneNumber"],
        "countryCode": client_data["countryCode"],
        "publicKey": generate_public_key(),
        "currency": client_data["currency"],
    }

    response = api.post("/api/v1/clients", request_body)
    client_id = require_id(response, client_data["label"])

    print(f"[CREATED] {client_data['label']}: "
          f"{client_data['firstName']} {client_data['lastName']}")
    print(f"          Username: {username}")
    print(f"          Address:  {client_data['address']}")
    print(f"          Client ID: {client_id}")
    print("           MAIN wallet: created by the API")

    return {
        "id": client_id,
        "username": username,
        "response": response,
    }


def create_sub_wallet(
    api: WalletPlatformApi,
    client_id: str,
    wallet_data: dict[str, str],
    client_label: str,
) -> dict[str, Any]:
    # Every property in WalletCreationRequest is supplied.
    request_body = {
        "name": wallet_data["name"],
        "currency": wallet_data["currency"],
    }

    response = api.post(
        f"/api/v1/clients/{client_id}/wallet",
        request_body,
    )
    wallet_id = require_id(response, f"{client_label} SUB wallet")

    print(f"[CREATED] {client_label} SUB wallet: {wallet_data['name']}")
    print(f"          Currency: {wallet_data['currency']}")
    print(f"          Wallet ID: {wallet_id}")

    return response


def create_merchant(
    api: WalletPlatformApi,
    merchant_data: dict[str, Any],
) -> dict[str, Any]:
    username = unique_username(merchant_data["username_prefix"])

    # Every property in MerchantCreationRequest is supplied.
    request_body = {
        "username": username,
        "password": strong_password(),
        "businessName": merchant_data["businessName"],
        "category": merchant_data["category"],
        "address": merchant_data["address"],
        "countryCode": merchant_data["countryCode"],
        "payoutIBAN": merchant_data["payoutIBAN"],
        "settleType": merchant_data["settleType"],
        "currency": merchant_data["currency"],
    }

    response = api.post("/api/v1/merchants", request_body)
    merchant_id = require_id(response, merchant_data["label"])

    print(f"[CREATED] Merchant: {merchant_data['businessName']}")
    print(f"          Username: {username}")
    print(f"          Category: {merchant_data['category']}")
    print(f"          Address:  {merchant_data['address']}")
    print(f"          Merchant ID: {merchant_id}")
    print("          MAIN wallet: created by the API")

    return {
        "id": merchant_id,
        "username": username,
        "response": response,
    }


def create_terminal(
    api: WalletPlatformApi,
    merchant_id: str,
    merchant_name: str,
    terminal_data: dict[str, str],
) -> dict[str, Any]:
    # Every property in TerminalCreationRequest is supplied.
    request_body = {
        "name": terminal_data["name"],
        "certificateSerialNumber": terminal_data["certificateSerialNumber"],
    }

    response = api.post(
        f"/api/v1/merchants/{merchant_id}/terminals",
        request_body,
    )
    terminal_id = require_id(
        response,
        f"{merchant_name} terminal {terminal_data['name']}",
    )

    print(f"[CREATED] Terminal: {terminal_data['name']}")
    print(f"          Merchant: {merchant_name}")
    print(f"          Certificate serial: "
          f"{terminal_data['certificateSerialNumber']}")
    print(f"          Terminal ID: {terminal_id}")

    return response


def main() -> int:
    api = WalletPlatformApi(API_BASE_URL)

    print("=" * 72)
    print("WALLET PLATFORM - DEVELOPMENT TEST DATA")
    print(f"API: {API_BASE_URL}")
    print("=" * 72)

    created_clients: list[dict[str, Any]] = []
    created_merchants: list[dict[str, Any]] = []

    try:
        # 1. Create all configured clients.
        for index, client_data in enumerate(CLIENTS, start=1):
            print(f"\n--- Creating client {index}/{len(CLIENTS)} ---")
            client = create_client(api, client_data)
            created_clients.append(client)

            # The API creates each client's MAIN wallet automatically.
            # Create configured SUB wallets after the client exists.
            configured_wallets = client_data.get("subWallets", [])
            if index == 2 and not CREATE_CLIENT_2_SUB_WALLET:
                configured_wallets = []

            for wallet_data in configured_wallets:
                create_sub_wallet(
                    api,
                    client["id"],
                    wallet_data,
                    client_data["label"],
                )

        # 2. Create every configured merchant and its terminals.
        for index, merchant_data in enumerate(MERCHANTS, start=1):
            print(f"\n--- Creating merchant {index}/{len(MERCHANTS)} ---")
            merchant = create_merchant(api, merchant_data)
            created_merchants.append(merchant)

            for terminal_data in merchant_data.get("terminals", []):
                create_terminal(
                    api,
                    merchant["id"],
                    merchant_data["businessName"],
                    terminal_data,
                )

    except (requests.RequestException, RuntimeError) as exc:
        print(f"\n[ERROR] Setup stopped: {exc}", file=sys.stderr)
        print(
            "Previously created resources remain in the database; "
            "the script does not automatically delete them.",
            file=sys.stderr,
        )
        return 1

    print("\n" + "=" * 72)
    print("TEST DATA CREATION COMPLETED")
    print(f"Clients created:   {len(created_clients)}")
    print(f"Merchants created: {len(created_merchants)}")
    print("Money-related operations performed: 0")
    print("=" * 72)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
