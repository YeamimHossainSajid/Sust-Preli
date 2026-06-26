# Fintech Investigator API

Production-ready Spring Boot 3 API for AI-powered fintech complaint investigation. Built for hackathon demos with clean architecture, rule-based fallback, and optional OpenAI/Gemini extraction.

## Stack

- Java 21
- Spring Boot 3.3.5
- Maven
- Springdoc OpenAPI (Swagger UI)
- Jakarta Bean Validation
- Lombok
- MapStruct
- Docker

## Project Structure

```
src/main/java/com/hackathon/investigator/
├── controller/          # REST endpoints
├── dto/                 # Request/response records
├── entity/              # Domain models
├── enums/               # Allowed enum values
├── service/             # Service interfaces
├── service/impl/        # Service implementations
├── validator/           # Business rule validation
├── exception/           # Global exception handling
├── config/              # OpenAPI and app config
├── client/              # Pluggable AI extraction clients
├── mapper/              # MapStruct mappers
└── util/                # Shared utilities
```

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+

### Run locally

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

### Run with Docker

```bash
docker compose up --build
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8080` | HTTP port (Render/Railway set this automatically) |
| `AI_ENABLED` | `false` | Enable external AI providers |
| `AI_PROVIDER` | `rule-based` | `ollama`, `openai`, `gemini`, or `rule-based` |
| `OLLAMA_API_KEY` | empty | Ollama Cloud API key ([create here](https://ollama.com/settings/keys)) |
| `OLLAMA_BASE_URL` | `https://ollama.com` | Ollama Cloud host |
| `OLLAMA_MODEL` | `gemini-3-flash-preview:cloud` | Cloud model id |
| `OPENAI_API_KEY` | empty | OpenAI API key |
| `GEMINI_API_KEY` | empty | Google Gemini API key (direct Google API) |

Example with Ollama Cloud (Gemini 3 Flash Preview):

```bash
export AI_ENABLED=true
export AI_PROVIDER=ollama
export OLLAMA_API_KEY=your_key_from_ollama_com_settings
export OLLAMA_MODEL=gemini-3-flash-preview:cloud
mvn spring-boot:run
```

The model page at [ollama.com/library/gemini-3-flash-preview](https://ollama.com/library/gemini-3-flash-preview) maps to the cloud id `gemini-3-flash-preview:cloud`.

Example with OpenAI:

```bash
export AI_ENABLED=true
export AI_PROVIDER=openai
export OPENAI_API_KEY=sk-...
mvn spring-boot:run
```

Example with Gemini:

```bash
export AI_ENABLED=true
export AI_PROVIDER=gemini
export GEMINI_API_KEY=...
mvn spring-boot:run
```

If AI is disabled or fails, the service falls back to rule-based extraction supporting English, Bangla, and Banglish.

## API Endpoints

### Health Check

`GET /health`

Response:

```json
{
  "status": "ok"
}
```

### Analyze Ticket

`POST /analyze-ticket`

Request:

```json
{
  "ticket_id": "TKT-001",
  "complaint": "I sent 5000 taka to the wrong number.",
  "language": "en",
  "channel": "in_app_chat",
  "user_type": "customer",
  "transaction_history": [
    {
      "transaction_id": "TXN-9101",
      "timestamp": "2026-04-14T14:08:22Z",
      "type": "transfer",
      "amount": 5000,
      "counterparty": "+8801719876543",
      "status": "completed"
    }
  ]
}
```

Response:

```json
{
  "ticket_id": "TKT-001",
  "relevant_transaction_id": "TXN-9101",
  "evidence_verdict": "consistent",
  "case_type": "wrong_transfer",
  "severity": "high",
  "department": "dispute_resolution",
  "agent_summary": "Ticket TKT-001 classified as wrong_transfer...",
  "recommended_next_action": "Verify beneficiary details and initiate dispute workflow without promising reversal.",
  "customer_reply": "We understand you may have sent funds to the wrong recipient...",
  "human_review_required": true,
  "confidence": 0.90,
  "reason_codes": ["CASE_WRONG_TRANSFER", "EVIDENCE_CONSISTENT", "PROVIDER_RULE-BASED"]
}
```

## Swagger / OpenAPI

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Investigation Pipeline

1. **ValidationService** validates request payload and business rules
2. **AiExtractionService** extracts intent, amount, type, risk, keywords
3. **TransactionMatcherService** scores and selects the best transaction
4. **EvidenceService** checks complaint vs transaction consistency
5. **CaseClassificationService** assigns case type
6. **RoutingService** maps case to department
7. **EscalationService** decides human review requirement
8. **ResponseGenerationService** creates agent/customer responses
9. **SafetyService** blocks unsafe customer replies

## Transaction Matching Scores

| Signal | Score |
|--------|-------|
| Amount match | +5 |
| Type match | +4 |
| Recent time match | +3 |
| Counterparty match | +2 |
| Status match | +2 |

## Deploy

### Render

1. Push this repo to GitHub
2. Create a new Web Service on Render
3. Select Docker runtime
4. Set health check path to `/health`
5. Add environment variables as needed

A `render.yaml` blueprint is included.

### Railway

1. Create a new project from this repo
2. Railway detects the Dockerfile automatically
3. Set `AI_ENABLED`, `AI_PROVIDER`, and API keys in Variables
4. Expose port `8080`

## Testing

```bash
mvn test
```

Included tests cover transaction matching, evidence evaluation, safety filtering, and REST integration.

## Allowed Enum Values

- `evidence_verdict`: `consistent`, `inconsistent`, `insufficient_data`
- `case_type`: `wrong_transfer`, `payment_failed`, `refund_request`, `duplicate_payment`, `merchant_settlement_delay`, `agent_cash_in_issue`, `phishing_or_social_engineering`, `other`
- `severity`: `low`, `medium`, `high`, `critical`
- `department`: `customer_support`, `dispute_resolution`, `payments_ops`, `merchant_operations`, `agent_operations`, `fraud_risk`
- `transaction_type`: `transfer`, `payment`, `cash_in`, `cash_out`, `settlement`, `refund`
- `transaction_status`: `completed`, `failed`, `pending`, `reversed`

## License

MIT
# Sust-Preli
