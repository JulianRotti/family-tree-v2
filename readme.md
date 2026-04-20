# Family Tree App

## Project Structure

```
my-app/
├── backend/          # Quarkus REST API
├── frontend/         # React app (separate Git repo, included as submodule)
├── infra/
│   ├── keycloak/     # Keycloak realm export (backup/restore)
│   └── mysql/        # Schema, init scripts, test data, and GeoNames data
├── docker-compose.yml
├── dl_and_prepare_location_data.sh
├── Makefile
└── README.md
```

### Notes

* The `frontend/` directory is managed as a **Git submodule**.
* It is a standalone repository (used for deployment, e.g. on Vercel), but linked into this project.
* After cloning this repository, make sure to initialize submodules:

```bash
git submodule update --init --recursive
```

---

## Git Workflow

### Main repository

```bash
git add .
git commit -m "Your message"
git push
```

### Frontend (submodule)

```bash
cd frontend
git add .
git commit -m "Your message"
git push
cd ..
```

### After updating frontend

```bash
git add frontend
git commit -m "Update frontend submodule"
git push
```

## Prerequisites

- Docker
- `make`
- `curl`
- `unzip`

---

## Quick Start

```bash
make up
```

On first run this will:
1. Download and prepare GeoNames city/country data (if not already present)
2. Start MySQL, Keycloak, and MinIO

MySQL automatically runs the init scripts on first startup (empty volume only):
`01_schema.sql`, `02_fill_test_data.sql`, `03_geonames_schema.sql`, `04_geonames_data.sql`

```bash
make up              # download geo data if missing, then start stack
docker compose up -d # start stack only (assumes geo data already present)
docker compose down  # stop all containers
```

---

## Services Overview

| Service   | URL                                           | Credentials                 |
|-----------|-----------------------------------------------|-----------------------------|
| Keycloak  | [http://localhost:8080](http://localhost:8080) | `admin` / `admin`           |
| MinIO API | `http://localhost:9000`                       | `minioadmin` / `minioadmin` |
| MinIO UI  | [http://localhost:9001](http://localhost:9001) | `minioadmin` / `minioadmin` |

---

## GeoNames Location Data

City and country autocomplete is powered by an offline
[GeoNames](https://download.geonames.org/export/dump/) dataset loaded directly
into MySQL. No external geocoding service is used at runtime.

The prepared TSV files (`cities_slim.tsv`, `countries_slim.tsv`) are gitignored
since they are auto-generated. `make up` downloads and prepares them
automatically if they are missing.

```bash
make geodata          # download/prepare only, don't start stack
make geodata-refresh  # force re-download even if files already exist
```

> The dataset adds ~30MB to MySQL. This is negligible and has no meaningful
> impact on container startup time or memory usage.

---

## Data Management

### Resetting the Database

MySQL only runs the init scripts on an **empty data volume**. To force a full
reload (e.g. after schema changes or to re-import GeoNames data):

```bash
make reset-db
```

Or manually:

```bash
docker rm -f mysql
docker volume rm $(docker volume ls -q | grep mysql)
make up
```

> ⚠️ This wipes all database content including GeoNames data (which will be
> re-imported automatically). Keycloak users and realm config are unaffected.

### Wiping Stored Images (MinIO)

Member profile images are stored in [MinIO](https://min.io/), an S3-compatible
object store. The bucket `member-images` is created automatically on first
backend startup. Image data persists across restarts via the `minio_data` volume.

To wipe all stored images:

```bash
docker volume rm $(docker volume ls -q | grep minio)
```

> This only removes MinIO data. MySQL and Keycloak volumes are unaffected.

### Wiping Everything

To wipe all volumes at once (database, images, and Keycloak config):

```bash
docker compose down -v
```

> ⚠️ This includes Keycloak — you will need to redo the one-time Keycloak setup
> described below.

---

## Keycloak Setup (First Time Only)

Keycloak config is **persisted in a Docker volume** (`keycloak_data`), so this
setup only needs to be done once. It survives container restarts as long as you
don't wipe the volume.

### 1. Start the Containers

```bash
make up
```

### 2. Log in to the Admin Console

Open [http://localhost:8080](http://localhost:8080) and log in:

- **Username:** `admin`
- **Password:** `admin`

### 3. Create the Realm

1. Click the dropdown in the top-left (shows `master` by default)
2. Click **Create Realm**
3. Set the name to `family-tree` and click **Create**
4. In Realm Settings set Unmanaged Attributes to "Only administrators can write"

### 4. Create the Clients

Navigate to **Clients → Create client** and add the following three clients:

#### `family-tree-frontend`
Used by the React frontend.

| Setting | Value |
|---|---|
| Client ID | `family-tree-frontend` |
| Valid redirect URIs | <frontend url>/* |
| Valid post logout redirect URIs | <frontend url>/* |
| Web origins | <frontend url> |
| Client Protocol | `openid-connect` |
| Client Authentication | Off (public client) |
| Authentication flow | Standard flow, direct Access grants |

#### `family-tree-swagger`
Used for local/dev testing of API endpoints via Swagger UI.

| Setting | Value |
|---|---|
| Client ID | `family-tree-swagger` |
| Valid redirect URIs | <backend url>/q/swagger-ui/oauth2-redirect.html |
| Valid post logout redirect URIs | <backend url>/* |
| Web origins | <backend url>/* |
| Client Protocol | `openid-connect` |
| Client Authentication | Off (public client) |
| Authentication flow | Standard flow |

#### `family-tree-backend`
Used by the Quarkus backend to validate incoming tokens.

| Setting | Value |
|---|---|
| Client ID | `family-tree-backend` |
| Client Protocol | `openid-connect` |
| Client Authentication | Off (public client) |

### 5. Add an Audience Mapper

Quarkus validates that incoming tokens contain `family-tree-backend` in their
`aud` claim. Tokens issued to the frontend don't include this by default.

1. Go to **Clients → `family-tree-frontend` → Client scopes**
2. Click the dedicated scope (`family-tree-frontend-dedicated`)
3. Click **Add mapper → By configuration → Audience**
4. Set **Included Client Audience** to `family-tree-backend` and save

### 6. Create Roles

Go to **Realm roles → Create role** and add:

| Role | Description |
|---|---|
| `view` | Read-only access to family tree data |
| `edit` | Can modify existing records |
| `create` | Can add new members or records |
| `delete` | Can remove members or records |

### 7. Create Users

1. Go to **Users → Add user**, fill in a username, and save
2. Under **Credentials**, set a password — disable **Temporary** to skip forced reset
3. Under **Role mapping**, assign one or more roles
4. Under **Attributes**, add the tenant_id (UUID) of the family the user should have access to

---

## Backing Up Keycloak Config

Export the realm after setup so it can be restored without repeating the manual
steps above:

```bash
docker exec keycloak \
  /opt/keycloak/bin/kc.sh export \
  --dir /tmp/export \
  --realm family-tree \
  --users realm_file

docker cp keycloak:/tmp/export/family-tree-realm.json \
  infra/keycloak/realm-export.json
```

Commit `infra/keycloak/realm-export.json` to version control. This is your
source of truth if the Keycloak volume is ever lost.