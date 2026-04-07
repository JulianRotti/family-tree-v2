# Family Tree App

## Project Structure

```
my-app/
├── backend/          # Quarkus REST API
├── frontend/         # React app
├── infra/
│   ├── keycloak/     # Keycloak realm export (backup/restore)
│   └── mysql/        # Schema, init scripts, and test data
├── docker-compose.yml
└── README.md
```

---

## Quick Start

```bash
docker compose up
```

Starts MySQL, Keycloak, and MinIO. MySQL automatically runs the init scripts
(`01_schema.sql`, `02_fill_test_data.sql`) on first startup.

```bash
docker compose up -d   # run in background
docker compose down    # stop all containers
```

---

## Services Overview

| Service   | URL                                       | Credentials               |
|-----------|-------------------------------------------|---------------------------|
| Keycloak  | [http://localhost:8080](http://localhost:8080) | `admin` / `admin`     |
| MinIO API | `http://localhost:9000`                   | `minioadmin` / `minioadmin` |
| MinIO UI  | [http://localhost:9001](http://localhost:9001) | `minioadmin` / `minioadmin` |

---

## MinIO (Image Storage)

Member profile images are stored in [MinIO](https://min.io/), an S3-compatible object store running as a local container.

The bucket `member-images` is created automatically on first backend startup. Image data persists across container restarts via the named Docker volume `minio_data`.

To wipe all stored images:

```bash
docker volume rm $(docker volume ls -q | grep minio)
```

> This only removes MinIO data. MySQL and Keycloak volumes are unaffected.

---

## Resetting the Database

MySQL only runs the init scripts on an **empty data volume**. To force a full reload:

```bash
docker rm -f mysql
docker volume rm $(docker volume ls -q | grep mysql)
docker compose up
```

> ⚠️ This wipes all database content. Keycloak users and realm config are unaffected.

---

## Keycloak Setup (First Time Only)

Keycloak config is **persisted in a Docker volume** (`keycloak_data`), so this setup only needs to be done once. It survives container restarts as long as you don't run `docker compose down -v`.

### 1. Start the Containers

```bash
docker compose up
```

### 2. Log in to the Admin Console

Open [http://localhost:8080](http://localhost:8080) and log in:

- **Username:** `admin`
- **Password:** `admin`

### 3. Create the Realm

1. Click the dropdown in the top-left (shows `master` by default)
2. Click **Create Realm**
3. Set the name to `family-tree` and click **Create**

### 4. Create the Clients

Navigate to **Clients → Create client** and add the following three clients:

#### `family-tree-frontend`
Used by the React frontend.

| Setting | Value |
|---|---|
| Client ID | `family-tree-frontend` |
| Client Protocol | `openid-connect` |
| Client Authentication | Off (public client) |

#### `family-tree-swagger`
Used for local/dev testing of API endpoints via Swagger UI.

| Setting | Value |
|---|---|
| Client ID | `family-tree-swagger` |
| Client Protocol | `openid-connect` |
| Client Authentication | Off (public client) |

#### `family-tree-backend`
Used by the Quarkus backend to validate incoming tokens. No client secret is needed — the backend only checks JWTs using Keycloak's public JWKS endpoint.

| Setting | Value |
|---|---|
| Client ID | `family-tree-backend` |
| Client Protocol | `openid-connect` |
| Client Authentication | Off (public client) |

### 5. Add an Audience Mapper

Quarkus validates that incoming tokens contain `family-tree-backend` in their `aud` (audience) claim. Tokens issued to the frontend don't include this by default, so you need to add a mapper.

1. Go to **Clients → `family-tree-frontend` → Client scopes**
2. Click the dedicated scope (named `family-tree-frontend-dedicated`)
3. Click **Add mapper → By configuration → Audience**
4. Set **Included Client Audience** to `family-tree-backend` and save

Tokens issued to the frontend will now include `family-tree-backend` in their audience, and the backend will accept them.

### 5. Create Roles

Go to **Realm roles → Create role** and add the following roles:

| Role | Description |
|---|---|
| `view` | Read-only access to family tree data |
| `edit` | Can modify existing records |
| `create` | Can add new members or records |
| `delete` | Can remove members or records |

### 6. Create Users

1. Go to **Users → Add user**, fill in a username, and save
2. Under the **Credentials** tab, set a password — disable **Temporary** to skip forced reset on first login
3. Under the **Role mapping** tab, assign one or more roles (`view`, `edit`, `create`, `delete`)

---

## Backing Up Keycloak Config

Export the realm after setup so it can be restored without repeating the manual steps above:

```bash
docker exec keycloak \
  /opt/keycloak/bin/kc.sh export \
  --dir /tmp/export \
  --realm family-tree \
  --users realm_file

docker cp keycloak:/tmp/export/family-tree-realm.json \
  infra/keycloak/realm-export.json
```

Commit `infra/keycloak/realm-export.json` to version control. This is your source of truth if the Keycloak volume is ever lost.