# Deployment

Target: each module as its own **Cloud Run** service, images built by **Cloud Build** and stored in **Artifact Registry**, with Gemini served by **Vertex AI**.

> The repository does not yet contain a `Dockerfile` or `cloudbuild.yaml`. The ones below are templates that follow the project layout; they have not been run against a real GCP project, so treat the first deployment as a verification step.

## Before you deploy: production caveats

| Topic | Current state | What to do |
|---|---|---|
| Database | H2 in memory, one database per service instance. Data is lost on restart and is not shared between instances. | Fine for demos. For real use, point `*_DB_URL` at Cloud SQL (this needs the matching JDBC driver added to the module) and keep `DDL_AUTO` as a deliberate choice. |
| Scaling | In-memory state means more than one instance gives inconsistent data. | Deploy stateful services with `--max-instances 1`, and `--min-instances 1` to avoid losing data on scale-to-zero. |
| Authentication | No service validates callers, and the agent sends no credentials. | Demo: allow unauthenticated invocation (insecure; anyone can call `PUT /delivery/weather`). Production: require Cloud Run IAM and add ID-token support to the agent's HTTP client, which is a code change not yet made. |
| Cache | Caffeine per instance. | Acceptable; entries expire after `CACHE_TTL`. |
| H2 console | Disabled (`H2_CONSOLE_ENABLED=false`). | Keep it disabled. |

## Cloud Build

### One-time setup

```bash
export PROJECT_ID=<your-project>
export REGION=europe-west2

gcloud config set project $PROJECT_ID
gcloud services enable run.googleapis.com cloudbuild.googleapis.com \
  artifactregistry.googleapis.com aiplatform.googleapis.com

gcloud artifacts repositories create commerce \
  --repository-format=docker --location=$REGION
```

### Dockerfile

One parameterised multi-stage file at the repository root, built once per module:

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
ARG MODULE
WORKDIR /src
COPY . .
RUN mvn -B -q -pl ${MODULE} -am package -DskipTests

FROM eclipse-temurin:17-jre
ARG MODULE
WORKDIR /app
COPY --from=build /src/${MODULE}/target/${MODULE}-*.jar app.jar
RUN useradd --system app
USER app
# Cloud Run supplies PORT; the services default to 8081-8085 otherwise.
ENTRYPOINT ["sh", "-c", "exec java -jar app.jar --server.port=${PORT:-8080}"]
```

Add a `.dockerignore` containing `**/target` and `.git` to keep the build context small.

### cloudbuild.yaml

Builds and pushes all six images, tagged with the build id:

```yaml
substitutions:
  _REGION: europe-west2
  _REPO: commerce

steps:
  - id: build-and-push
    name: gcr.io/cloud-builders/docker
    entrypoint: bash
    env:
      - SERVICES=order-service inventory-service payment-service crm-service delivery-service rerouting-agent
    args:
      - -c
      - |
        set -e
        for s in $$SERVICES; do
          image=${_REGION}-docker.pkg.dev/$PROJECT_ID/${_REPO}/$$s:$BUILD_ID
          docker build --build-arg MODULE=$$s -t $$image .
          docker push $$image
        done

options:
  logging: CLOUD_LOGGING_ONLY
```

Run it:

```bash
gcloud builds submit --config cloudbuild.yaml .
```

Unit tests are skipped in the image build. Run `mvn test` earlier in the pipeline (for example, a first step using `maven:3.9-eclipse-temurin-17` with `mvn -B test`) so a failing test stops the build.

## Cloud Run

### Service account for the agent

```bash
gcloud iam service-accounts create rerouting-agent
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member=serviceAccount:rerouting-agent@$PROJECT_ID.iam.gserviceaccount.com \
  --role=roles/aiplatform.user
```

Gemini is called through Application Default Credentials, so no key file is needed. Without this role (or with `GEMINI_ENABLED=false`) the agent uses the rule-based advisor.

### Deploy the backend services first

```bash
export IMAGE_BASE=$REGION-docker.pkg.dev/$PROJECT_ID/commerce
export TAG=<build id from the Cloud Build run>

for s in order-service inventory-service payment-service crm-service delivery-service; do
  gcloud run deploy $s \
    --image $IMAGE_BASE/$s:$TAG \
    --region $REGION \
    --port 8080 \
    --max-instances 1 --min-instances 1 \
    --memory 512Mi \
    --allow-unauthenticated   # demo only, see caveats above
done
```

Per-service settings go in `--set-env-vars`, for example weather for the delivery service:

```bash
gcloud run services update delivery-service --region $REGION \
  --set-env-vars WEATHER_SEVERE=true,WEATHER_AFFECTED_REGIONS=SOUTH_ENGLAND
```

### Deploy the agent with the service URLs

```bash
url() { gcloud run services describe $1 --region $REGION --format='value(status.url)'; }

gcloud run deploy rerouting-agent \
  --image $IMAGE_BASE/rerouting-agent:$TAG \
  --region $REGION \
  --port 8080 \
  --service-account rerouting-agent@$PROJECT_ID.iam.gserviceaccount.com \
  --memory 1Gi \
  --allow-unauthenticated \
  --set-env-vars "ORDER_SERVICE_URL=$(url order-service),INVENTORY_SERVICE_URL=$(url inventory-service),PAYMENT_SERVICE_URL=$(url payment-service),CRM_SERVICE_URL=$(url crm-service),DELIVERY_SERVICE_URL=$(url delivery-service),GEMINI_ENABLED=true,GCP_PROJECT_ID=$PROJECT_ID,GCP_LOCATION=us-central1"
```

The agent is the public entry point. Verify:

```bash
AGENT=$(url rerouting-agent)
curl $AGENT/actuator/health
curl $AGENT/swagger-ui.html
```

Then run the scenario from [README.md](README.md#try-the-weather-scenario) against `$AGENT` and the delivery service URL.

Use `--set-secrets` (Secret Manager) rather than `--set-env-vars` for `DB_PASSWORD` or any other secret.

## Environment Variables

Every variable is optional; the default is used when it is unset.

### All services

| Variable | Default | Purpose |
|---|---|---|
| `DB_USERNAME` | `sa` | Database user |
| `DB_PASSWORD` | empty | Database password (use Secret Manager) |
| `DDL_AUTO` | `update` | Hibernate schema mode |
| `H2_CONSOLE_ENABLED` | `false` | H2 web console |
| `PAGE_DEFAULT_SIZE` | `20` | Default page size |
| `PAGE_MAX_SIZE` | `100` | Maximum page size |
| `CACHE_TTL` | `5m` | Cache entry lifetime |
| `CACHE_MAX_SIZE` | `1000` | Maximum entries per cache |

On Cloud Run the container is started with `--server.port=$PORT`, so the port variables below only matter for local runs.

### Per service

| Service | Variable | Default | Purpose |
|---|---|---|---|
| order | `ORDER_SERVICE_PORT` | `8081` | HTTP port |
| order | `ORDER_DB_URL` | `jdbc:h2:mem:orders;DB_CLOSE_DELAY=-1` | JDBC URL |
| inventory | `INVENTORY_SERVICE_PORT` | `8082` | HTTP port |
| inventory | `INVENTORY_DB_URL` | `jdbc:h2:mem:inventory;DB_CLOSE_DELAY=-1` | JDBC URL |
| inventory | `INVENTORY_SEED_ENABLED` | `true` | Load the LON / MAN / BHX demo warehouses |
| payment | `PAYMENT_SERVICE_PORT` | `8083` | HTTP port |
| payment | `PAYMENT_DB_URL` | `jdbc:h2:mem:payments;DB_CLOSE_DELAY=-1` | JDBC URL |
| payment | `PAYMENT_MAX_AMOUNT` | `10000` | Amounts above this are declined |
| crm | `CRM_SERVICE_PORT` | `8084` | HTTP port |
| crm | `CRM_DB_URL` | `jdbc:h2:mem:crm;DB_CLOSE_DELAY=-1` | JDBC URL |
| delivery | `DELIVERY_SERVICE_PORT` | `8085` | HTTP port |
| delivery | `DELIVERY_DB_URL` | `jdbc:h2:mem:delivery;DB_CLOSE_DELAY=-1` | JDBC URL |
| delivery | `WEATHER_SEVERE` | `false` | Start with severe weather on |
| delivery | `WEATHER_AFFECTED_REGIONS` | `SOUTH_ENGLAND` | Comma-separated regions blocked while severe |

### Re-routing agent

| Variable | Default | Purpose |
|---|---|---|
| `AGENT_PORT` | `8080` | HTTP port |
| `ORDER_SERVICE_URL` | `http://localhost:8081` | Order service base URL |
| `INVENTORY_SERVICE_URL` | `http://localhost:8082` | Inventory service base URL |
| `PAYMENT_SERVICE_URL` | `http://localhost:8083` | Payment service base URL |
| `CRM_SERVICE_URL` | `http://localhost:8084` | CRM service base URL |
| `DELIVERY_SERVICE_URL` | `http://localhost:8085` | Delivery service base URL |
| `HTTP_CONNECT_TIMEOUT` | `2s` | Downstream connect timeout |
| `HTTP_READ_TIMEOUT` | `5s` | Downstream read timeout |
| `AGENT_MAX_REROUTE_ATTEMPTS` | `3` | Warehouses to try before giving up |
| `GEMINI_ENABLED` | `false` | Use Gemini to choose the warehouse |
| `GCP_PROJECT_ID` | empty | Project used for Vertex AI (required when Gemini is enabled) |
| `GCP_LOCATION` | `us-central1` | Vertex AI region |
| `GEMINI_MODEL` | `gemini-2.0-flash` | Model name; confirm it is available in your project and region |
| `GEMINI_TEMPERATURE` | `0` | Sampling temperature |
| `CB_WINDOW_SIZE` | `10` | Default breaker: calls in the sliding window |
| `CB_MIN_CALLS` | `5` | Default breaker: calls before the failure rate is evaluated |
| `CB_FAILURE_RATE` | `50` | Default breaker: failure percentage that opens it |
| `CB_OPEN_WAIT` | `30s` | Default breaker: time open before probing |
| `CB_DELIVERY_WINDOW_SIZE` | `6` | Delivery breaker: window |
| `CB_DELIVERY_MIN_CALLS` | `3` | Delivery breaker: minimum calls |
| `CB_DELIVERY_OPEN_WAIT` | `60s` | Delivery breaker: time open |

### Local Gemini authentication

```powershell
gcloud auth application-default login
$env:GEMINI_ENABLED = "true"
$env:GCP_PROJECT_ID = "<your-project>"
mvn -pl rerouting-agent spring-boot:run
```
