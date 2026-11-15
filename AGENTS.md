# AGENTS.md

Instructions for AI coding agents in this repository. Read this file first.

## Project

Wallet and POS Platform is the backend of a closed-loop payment network. Clients hold wallets in a mobile app. They pay merchants with a signed QR code. Merchant terminals scan the code and call the backend. The backend moves money between wallets on an internal double-entry ledger. An external provider is involved only for top-up, withdrawal, and merchant settlement.

The platform is general purpose. It is not tied to one organization, city, or country.

## Working method: spec-driven development

The spec comes first. The code follows the spec.

1. Read `docs/constitution.md`.
2. Read the spec for your task in `specs/`. Read the `docs/` files that the spec links.
3. If no spec exists for the task, write one from `specs/_template.md`. Wait for the human to approve it.
4. Implement only what the spec says. Do not add endpoints, columns, or features that the spec does not list.
5. Write tests from the acceptance criteria. Tick a criterion only when a passing test proves it.
6. If code and spec disagree, the spec wins. If the spec is unclear or wrong, stop and ask. Do not guess.
7. If behavior changes, change the spec in the same commit.

## Where things are

| Path | Content |
|---|---|
| `docs/constitution.md` | Mission, tech stack, principles, roadmap, out of scope |
| `docs/architecture.md` | Services, databases, message flow, deployment |
| `docs/domain/` | Rules the code must follow: ledger, wallets, POS payment, provider boundary |
| `docs/data-model.md` | Tables and constraints (the migrations are the source) |
| `docs/api.md` | Endpoint catalog: path, caller, purpose |
| `docs/decisions/` | One short file per technical decision |
| `specs/` | One spec per feature, improvement, or issue |

Each fact has one home. Link to it. Do not copy it.

## Tech stack

- Java and Spring Boot. Build tool is Maven.
- PostgreSQL 18 with Flyway migrations. Redis for nonces. RabbitMQ for messages between services.
- Docker for local development and deployment. Prometheus and Grafana for metrics.
- Tests use JUnit 5 and Testcontainers.

## Commands

```
./mvnw clean verify       # build and run all tests
./mvnw spring-boot:run    # run the service locally
docker compose up -d      # start PostgreSQL, Redis, RabbitMQ for local work
```

## Rules

### Money and VAT

- Use `BigDecimal` for every amount. Never use `double` or `float`.
- The database stores amounts as `NUMERIC(10,2)` and VAT rates as `NUMERIC(4,2)` (0.19 means 19%).
- An amount is gross. It includes VAT. Net equals gross divided by (1 + rate).
- A partial refund rounds its VAT portion. The remainder comes from subtraction.

### Ledger

What a ledger row should hold
The money fact: wallet_id, amount, vat, currency, created_at.
The link to its transaction: trans_id. Everything else about the event, such as type, status, counterparty, terminal, and reason, is found there.
Values that must stay frozen: vat_rate is the example. Tax rates change over time, and the row must show the rate that applied at that moment. Its transaction could one day hold a different value.

Ledger rows stay minimal

- The ledger is append-only. Never update or delete a row. Database triggers enforce this.
- Fix a mistake with a new entry that reverses it.
- The entries of one transaction sum to zero, for amount and for VAT, per currency.
- VAT has the same sign as its amount. Every row stores its `vat_rate`.
- Only successful money movements write ledger rows. Failed attempts live only in `transactions`.
- `wallets.balance` is a cache. Update it in the same database transaction as the ledger rows. The ledger is the source of truth.
- Every movement goes from one wallet to another:
  - payment: client to merchant
  - refund: merchant to client
  - transfer: client to client
  - top-up: `external_clearing` to client
  - withdrawal: client to `pending_payout`
  - settlement: merchant to `pending_payout`
- A client wallet never goes below zero. Merchant and system wallets can.

### POS payment

- The terminal is identified by its mTLS certificate. Never read the terminal identity from the request body.
- The backend runs these steps in order: decrypt, check expiry, verify signature, check `orderAmount <= maxAmount`, check the nonce, check idempotency, write, respond.
- The write is one atomic database transaction. It holds the ledger rows, the balance update, the transaction row, and the consumed nonce.
- The response goes out only after the commit.
- Duplicates are stopped by two keys. The `randToken` is unique per payment. The composite key is terminal, receipt number, paying wallet, business date, and amount. A retry returns the stored result.
- Declined attempts are stored in `transactions` with a failure reason.
- Requests that start in an app (refund, transfer, top-up, withdrawal) carry a client-generated `idempotencyKey`.

### Provider boundary

- Only Provider Service talks to a provider. Payment Service never does.
- Provider Service owns its database. There are no foreign keys between the two databases.
- Provider statuses map to `PENDING`, `SUCCESS`, `FAILED` through `status_map`.
- A webhook is verified, deduplicated by event id, stored, and then published to RabbitMQ.
- Payment Service accepts only forward state changes: `pending` to `success` or `failed`. An old `PENDING` event never overwrites `SUCCESS`.
- A top-up credits the wallet only after `SUCCESS`. A withdrawal debits first and adds a compensating entry if it fails.
- Reconciliation never edits the ledger. It flags mismatches for the back office.

### Data and identity

- IDs are UUIDv7 (`uuidv7()` in PostgreSQL 18).
- Timestamps are `timestamptz`, stored in UTC. The API uses RFC 3339.
- Schema changes go in a new Flyway migration. Never edit a migration that has run.
- `users` holds login data. `clients` and `merchants` share the id of their `users` row. Wallets point to `users`. Terminals belong to a merchant.
- Wallet creation checks the owner type in code. The database does not.

### Security

- Never log QR tokens, private keys, passwords, secrets, or full IBANs.
- Secrets come from the environment or a secret store. Never commit them.
- Store passwords as Argon2id or bcrypt hashes.
- Do not add a library or service that sends customer data outside the EU.

### API

- Prefix `/api/v1`. Use plural nouns and ids in the path.
- Nest only one level to create or list children: `POST /merchants/{id}/terminals`. After that, use the flat path `/terminals/{id}`.
- A state change is a POST action: `POST /terminals/{id}/suspend`.
- Each kind of money movement has its own resource: `/payments`, `/refunds`, `/transfers`, `/topups`, `/withdrawals`.
- The caller's role decides access. The URL does not.
- The full catalog is in `docs/api.md`.

## Code conventions

- Package by feature (`client`, `merchant`, `terminal`, `wallet`, `ledger`, `payment`), not by layer.
- Flow is controller, then service, then repository. Controllers hold no business logic.
- A service uses only its own repository. To reach another domain, call that domain's service. `ClientService` calls `WalletService`, never `WalletRepository`.
- Services do not depend on each other in a circle.
- Put `@Transactional` on service methods that write. One business operation is one database transaction.
- Use constructor injection with `@RequiredArgsConstructor`.
- DTOs are records. Controllers never return entities. Validate input with Jakarta annotations.
- Entities use `@Getter` and `@Setter`, not `@Data`. Map `@ManyToOne` on the many side. Add a collection mapping only when a spec needs it. Store enums with `EnumType.STRING`.
- Write less code. Reuse existing methods. Do not add abstractions for needs that do not exist yet.

## Testing

- Unit-test the ledger math with the worked example: a €11.90 payment at 19% VAT, then a €5.00 refund. The expected rows are in `docs/domain/ledger.md`.
- Integration tests use Testcontainers with PostgreSQL. Do not use H2.
- Test `POST /pos/payments` under concurrency. The same QR sent twice at once gives exactly one success.
- Test the failure paths too: expired QR, bad signature, amount above the limit, insufficient balance, replay.

## Definition of done

- The build passes: `./mvnw clean verify`.
- Every acceptance criterion in the spec is ticked and has a test.
- The spec status is `implemented`.
- A migration exists for every schema change.
- The docs match the behavior.

## Do not

- Do not edit a migration that has run.
- Do not update or delete ledger rows.
- Do not call a provider from Payment Service.
- Do not put business logic in controllers.
- Do not use `float` or `double` for money.
- Do not commit secrets.
- Do not add a new library, service, or database without a file in `docs/decisions/`.

## Writing docs

- Use short sentences with a clear subject, verb, and object.
- Use concrete words. Leave out filler and marketing language.
- Keep each fact in one place and link to it.
