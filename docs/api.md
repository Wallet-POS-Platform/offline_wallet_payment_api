# API catalog

This catalog describes the routes mapped by the current controllers. All routes use the `/api/v1` prefix.

An endpoint marked **stub** returns a fixed string and does not perform the named money movement. Do not use it as evidence that the operation is implemented.

## Client Operations

| Method | Path | Request | Response | Status | Purpose |
|---|---|---|---|---|---|
| `GET` | `/clients/{id}` | Path: client UUID | `ClientResponse` | 200 | Get a client and its wallets. |
| `GET` | `/clients?username={username}` | Query: username | `ClientResponse` | 200 | Get a client and its wallets by username. |
| `POST` | `/clients` | `ClientCreationRequest` | `ClientResponse` | 201 | Register a client and create its main wallet. |
| `POST` | `/clients/{id}/wallet` | Path: client UUID; `WalletCreationRequest` | `WalletResponse` | 201 | Create an additional wallet for a client. |
| `GET` | `/clients/{id}/wallets` | Path: client UUID | `List<WalletResponse>` | 200 | List a client's wallets. |
| `POST` | `/clients/{id}/topup` | Path: client UUID | Plain text | 200 | **Stub.** Returns `topup done!`. |
| `POST` | `/clients/{id}/withdraw` | Path: client UUID | Plain text | 200 | **Stub.** Returns `withdraw done!`. |
| `GET` | `/clients/details` | None | Plain text | 200 | **Stub.** Returns `Client created!`. |

## Merchant Operations

| Method | Path | Request | Response | Status | Purpose |
|---|---|---|---|---|---|
| `GET` | `/merchants/{id}` | Path: merchant UUID | `MerchantResponse` | 200 | Get a merchant, its business profile, and its main wallet. |
| `POST` | `/merchants` | `MerchantCreationRequest` | `MerchantResponse` | 201 | Register a merchant and create its main wallet. |
| `POST` | `/merchants/{merchantId}/terminals` | Path: merchant UUID; `TerminalCreationRequest` | `TerminalResponse` | 201 | Create a terminal for a merchant. |
| `POST` | `/merchants/settle` | None | Plain text | 200 | **Stub.** Returns `settle done!`. |

## Terminal Operations

| Method | Path | Request | Response | Status | Purpose |
|---|---|---|---|---|---|
| `POST` | `/terminals/{id}/pay` | Path: terminal UUID; `PaymentRequest` | Plain text | 200 | **Stub.** Returns `Pay done!`. |

## Admin Operations

There is no controller or endpoint currently mapped to the `Admin Operations` OpenAPI tag.
