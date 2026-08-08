# CampusWorks Deployment — Neon + MongoDB Atlas + Render + Vercel

| Layer | Platform |
|-------|----------|
| Frontend | **Vercel** |
| Java services + Chat (Node) | **Render** (7 free Web Services) |
| Relational DB | **Neon** (PostgreSQL) |
| Chat DB | **MongoDB Atlas** |

> Free Render services **sleep when idle**. First request after sleep can take 1–2 minutes. Before demos, open each `*.onrender.com` URL once to wake them.

---

## Your connection strings (filled)

**Neon host:** `ep-weathered-cloud-axhpd4hu-pooler.c-4.us-east-2.aws.neon.tech`  
**Neon user:** `neondb_owner`  
**Neon password:** `npg_pC6tqO7mkPwb`

**MongoDB Atlas (chat):**

```text
mongodb+srv://n210419_db_user:reddi2273@cluster0.xrjmxy9.mongodb.net/campusworks_chat?retryWrites=true&w=majority&appName=Cluster0
```

**Still replace after Render/Vercel go live:**
- `XXXX` → your real Render service hostnames (from Render dashboard)
- `YOUR_VERCEL_APP` → your Vercel URL

---

## Architecture

```
Browser → Vercel (frontend)
            ├─ REST → https://cw-api-gateway-xxxx.onrender.com/api/**
            └─ WS   → https://cw-chat-service-xxxx.onrender.com
                        │
              Render Web Services
              eureka / gateway / auth / task / bidding / profile / chat
                        │
              Neon (Postgres)          MongoDB Atlas
              auth/tasks/bids/profile     campusworks_chat
```

---

## STEP 1 — Neon (PostgreSQL) — REQUIRED before auth/task/bidding deploy

> If `cw-auth-service`, `cw-task-service`, or `cw-bidding-service` failed on Render,  
> almost always the Neon databases below are missing. Create them first, then **Manual Deploy**.

In Neon SQL Editor, run (skip any that already exist):

```sql
CREATE DATABASE campusworks_auth;
CREATE DATABASE campusworks_tasks;
CREATE DATABASE campusworks_bids;
CREATE DATABASE campusworks_profile;
```

**JDBC URLs (ready):**

```text
jdbc:postgresql://ep-weathered-cloud-axhpd4hu-pooler.c-4.us-east-2.aws.neon.tech/campusworks_auth?sslmode=require
jdbc:postgresql://ep-weathered-cloud-axhpd4hu-pooler.c-4.us-east-2.aws.neon.tech/campusworks_tasks?sslmode=require
jdbc:postgresql://ep-weathered-cloud-axhpd4hu-pooler.c-4.us-east-2.aws.neon.tech/campusworks_bids?sslmode=require
jdbc:postgresql://ep-weathered-cloud-axhpd4hu-pooler.c-4.us-east-2.aws.neon.tech/campusworks_profile?sslmode=require
```

---

## STEP 2 — MongoDB Atlas (Chat)

Your `campusworks_chat` DB on Cluster0 is ready.  
Also confirm **Network Access** allows `0.0.0.0/0`.

---

## STEP 3 — GitHub

Repo: `https://github.com/ManikantaReddi2273/CampusWorks-Microservices.git`  
(Deploy config already pushed on `main`.)

---

## STEP 4 — Render Blueprint

1. [https://dashboard.render.com](https://dashboard.render.com)  
2. **New +** → **Blueprint**  
3. Connect GitHub → **CampusWorks-Microservices** → apply `render.yaml`  
4. Services created:

| Service | Purpose |
|---------|---------|
| `cw-eureka-server` | Discovery |
| `cw-auth-service` | Auth + JWT |
| `cw-task-service` | Tasks |
| `cw-bidding-service` | Bids / UPI |
| `cw-profile-service` | Profiles |
| `cw-api-gateway` | Public API entry |
| `cw-chat-service` | Socket.io chat |

5. Wait until Eureka is **Live**, copy its public URL  
6. Paste env blocks below into each service → Save → redeploy  

Use the **same** `JWT_SECRET` on gateway, auth, and chat.

---

### Env templates (copy-paste into Render)

> After Blueprint finishes, replace every `XXXX` with the real suffix from your Render URLs  
> (example: if Eureka is `https://cw-eureka-server-a1b2.onrender.com`, use that full host).

#### cw-eureka-server
```env
JAVA_OPTS=-Xms64m -Xmx200m
EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
EUREKA_NON_SECURE_PORT_ENABLED=false
EUREKA_SECURE_PORT_ENABLED=true
EUREKA_INSTANCE_SECURE_PORT=443
```

#### cw-auth-service
```env
JAVA_OPTS=-Xms64m -Xmx200m
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-weathered-cloud-axhpd4hu-pooler.c-4.us-east-2.aws.neon.tech/campusworks_auth?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=npg_pC6tqO7mkPwb
JWT_SECRET=mysupersecuresecretkeythatismorethan32chars
SPRING_MAIL_USERNAME=campusworks2273@gmail.com
SPRING_MAIL_PASSWORD=nlbdkysxhffrjwnt
APP_FRONTEND_URL=https://YOUR_VERCEL_APP.vercel.app
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=https://cw-eureka-server-XXXX.onrender.com/eureka/
EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
EUREKA_NON_SECURE_PORT_ENABLED=false
EUREKA_SECURE_PORT_ENABLED=true
EUREKA_INSTANCE_SECURE_PORT=443
```

#### cw-task-service
```env
JAVA_OPTS=-Xms64m -Xmx200m
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-weathered-cloud-axhpd4hu-pooler.c-4.us-east-2.aws.neon.tech/campusworks_tasks?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=npg_pC6tqO7mkPwb
SPRING_MAIL_USERNAME=campusworks2273@gmail.com
SPRING_MAIL_PASSWORD=nlbdkysxhffrjwnt
BIDDING_SERVICE_URL=https://cw-bidding-service-XXXX.onrender.com
PROFILE_SERVICE_URL=https://cw-profile-service-XXXX.onrender.com
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=https://cw-eureka-server-XXXX.onrender.com/eureka/
EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
EUREKA_NON_SECURE_PORT_ENABLED=false
EUREKA_SECURE_PORT_ENABLED=true
EUREKA_INSTANCE_SECURE_PORT=443
```

#### cw-bidding-service
```env
JAVA_OPTS=-Xms64m -Xmx200m
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-weathered-cloud-axhpd4hu-pooler.c-4.us-east-2.aws.neon.tech/campusworks_bids?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=npg_pC6tqO7mkPwb
SPRING_MAIL_USERNAME=campusworks2273@gmail.com
SPRING_MAIL_PASSWORD=nlbdkysxhffrjwnt
TASK_SERVICE_URL=https://cw-task-service-XXXX.onrender.com
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=https://cw-eureka-server-XXXX.onrender.com/eureka/
EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
EUREKA_NON_SECURE_PORT_ENABLED=false
EUREKA_SECURE_PORT_ENABLED=true
EUREKA_INSTANCE_SECURE_PORT=443
```

#### cw-profile-service
```env
JAVA_OPTS=-Xms64m -Xmx200m
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-weathered-cloud-axhpd4hu-pooler.c-4.us-east-2.aws.neon.tech/campusworks_profile?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=npg_pC6tqO7mkPwb
TASK_SERVICE_URL=https://cw-task-service-XXXX.onrender.com
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=https://cw-eureka-server-XXXX.onrender.com/eureka/
EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
EUREKA_NON_SECURE_PORT_ENABLED=false
EUREKA_SECURE_PORT_ENABLED=true
EUREKA_INSTANCE_SECURE_PORT=443
```

#### cw-api-gateway
```env
JAVA_OPTS=-Xms64m -Xmx200m
JWT_SECRET=mysupersecuresecretkeythatismorethan32chars
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://YOUR_VERCEL_APP.vercel.app
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=https://cw-eureka-server-XXXX.onrender.com/eureka/
GATEWAY_AUTH_SERVICE_URI=https://cw-auth-service-XXXX.onrender.com
GATEWAY_TASK_SERVICE_URI=https://cw-task-service-XXXX.onrender.com
GATEWAY_BIDDING_SERVICE_URI=https://cw-bidding-service-XXXX.onrender.com
GATEWAY_PROFILE_SERVICE_URI=https://cw-profile-service-XXXX.onrender.com
EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
EUREKA_NON_SECURE_PORT_ENABLED=false
EUREKA_SECURE_PORT_ENABLED=true
EUREKA_INSTANCE_SECURE_PORT=443
```

#### cw-chat-service
```env
NODE_ENV=production
MONGODB_URI=mongodb+srv://n210419_db_user:reddi2273@cluster0.xrjmxy9.mongodb.net/campusworks_chat?retryWrites=true&w=majority&appName=Cluster0
JWT_SECRET=mysupersecuresecretkeythatismorethan32chars
SPRING_BOOT_BASE_URL=https://cw-api-gateway-XXXX.onrender.com
AUTH_SERVICE_URL=https://cw-auth-service-XXXX.onrender.com
TASK_SERVICE_URL=https://cw-task-service-XXXX.onrender.com
BIDDING_SERVICE_URL=https://cw-bidding-service-XXXX.onrender.com
SOCKET_CORS_ORIGIN=http://localhost:3000,https://YOUR_VERCEL_APP.vercel.app
```

---

## STEP 5 — Vercel (Frontend)

1. [https://vercel.com](https://vercel.com) → **Add New Project** → import `CampusWorks-Microservices`  
2. **Root Directory**: `campus-works-frontend`  
3. Framework: Vite (auto)  
4. Environment variables:

```env
VITE_API_BASE_URL=https://cw-api-gateway-XXXX.onrender.com
VITE_CHAT_SERVICE_URL=https://cw-chat-service-XXXX.onrender.com
VITE_APP_NAME=CampusWorks
VITE_APP_VERSION=1.0.0
VITE_ENABLE_REDUX_DEVTOOLS=false
```

5. Deploy → copy the Vercel URL  
6. Update on Render:
   - Gateway `CORS_ALLOWED_ORIGINS` (add Vercel URL)
   - Chat `SOCKET_CORS_ORIGIN` (add Vercel URL)
   - Auth `APP_FRONTEND_URL` (Vercel URL)

---

## STEP 6 — Smoke test

1. Wake each Render service once in the browser  
2. Gateway: `https://cw-api-gateway-XXXX.onrender.com/actuator/health`  
3. Chat: `https://cw-chat-service-XXXX.onrender.com/health`  
4. Open Vercel app → Register / Login → create task → bid  

---

## Notes

- Neon + Atlas credentials above are filled for copy-paste.  
- Only `XXXX` (Render hosts) and `YOUR_VERCEL_APP` still need your live URLs.  
- Create the 4 Neon databases if they are not created yet.  
- Chat uses Atlas only (`campusworks_chat`), not Neon.  
- Do not commit this file publicly if the repo is open — it contains DB passwords.  
