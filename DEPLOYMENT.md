# CampusWorks Deployment — Neon + MongoDB Atlas + Render + Vercel

| Layer | Platform |
|-------|----------|
| Frontend | **Vercel** |
| Java services + Chat (Node) | **Render** (7 free Web Services) |
| Relational DB | **Neon** (PostgreSQL) |
| Chat DB | **MongoDB Atlas** |

> I prepared Dockerfiles, `render.yaml`, cloud-ready env properties, and this guide.  
> **You must complete the dashboard steps below** (accounts, secrets, URLs). There is no CLI login for Neon/Render/Vercel/Atlas on this machine.

> Free Render services **sleep when idle**. First request after sleep can take 1–2 minutes. Before demos, open each `*.onrender.com` URL once to wake them.

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

## STEP 1 — Neon (PostgreSQL) — YOU

1. Go to [https://console.neon.tech](https://console.neon.tech) → create/open a project  
2. SQL Editor — run:

```sql
CREATE DATABASE campusworks_auth;
CREATE DATABASE campusworks_tasks;
CREATE DATABASE campusworks_bids;
CREATE DATABASE campusworks_profile;
```

3. **Connect** panel → copy Host, User, Password  

**JDBC URL pattern** (replace `HOST`):

```text
jdbc:postgresql://HOST/campusworks_auth?sslmode=require
jdbc:postgresql://HOST/campusworks_tasks?sslmode=require
jdbc:postgresql://HOST/campusworks_bids?sslmode=require
jdbc:postgresql://HOST/campusworks_profile?sslmode=require
```

---

## STEP 2 — MongoDB Atlas (Chat) — YOU

1. [https://cloud.mongodb.com](https://cloud.mongodb.com) → create free cluster  
2. Database Access → create user + password  
3. Network Access → allow `0.0.0.0/0` (Render outbound)  
4. Connect → Drivers → copy URI, e.g.

```text
mongodb+srv://USER:PASSWORD@cluster0.xxxxx.mongodb.net/campusworks_chat?retryWrites=true&w=majority
```

---

## STEP 3 — Push code to GitHub — YOU (if not already)

Repo: `https://github.com/ManikantaReddi2273/CampusWorks-Microservices.git`

```bash
git add .
git commit -m "Add Render/Vercel/Neon/Atlas deployment config"
git push origin main
```

---

## STEP 4 — Render Blueprint — YOU

1. [https://dashboard.render.com](https://dashboard.render.com)  
2. **New +** → **Blueprint**  
3. Connect GitHub → select **CampusWorks-Microservices** → apply `render.yaml`  
4. Render creates:

| Service | Purpose |
|---------|---------|
| `cw-eureka-server` | Discovery |
| `cw-auth-service` | Auth + JWT |
| `cw-task-service` | Tasks |
| `cw-bidding-service` | Bids / UPI |
| `cw-profile-service` | Profiles |
| `cw-api-gateway` | Public API entry |
| `cw-chat-service` | Socket.io chat |

5. Wait until **eureka** is Live, copy its URL (e.g. `https://cw-eureka-server-xxxx.onrender.com`)  
6. Paste env vars below into each service → **Save** → redeploy if needed  

Use the **same** `JWT_SECRET` everywhere (gateway, auth, chat).

---

### Env templates (paste & replace)

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
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_NEON_HOST/campusworks_auth?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=YOUR_NEON_PASSWORD
JWT_SECRET=mysupersecuresecretkeythatismorethan32chars
SPRING_MAIL_USERNAME=your_gmail@gmail.com
SPRING_MAIL_PASSWORD=your_gmail_app_password
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
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_NEON_HOST/campusworks_tasks?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=YOUR_NEON_PASSWORD
SPRING_MAIL_USERNAME=your_gmail@gmail.com
SPRING_MAIL_PASSWORD=your_gmail_app_password
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
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_NEON_HOST/campusworks_bids?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=YOUR_NEON_PASSWORD
SPRING_MAIL_USERNAME=your_gmail@gmail.com
SPRING_MAIL_PASSWORD=your_gmail_app_password
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
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_NEON_HOST/campusworks_profile?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=YOUR_NEON_PASSWORD
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
MONGODB_URI=mongodb+srv://USER:PASSWORD@cluster0.xxxxx.mongodb.net/campusworks_chat?retryWrites=true&w=majority
JWT_SECRET=mysupersecuresecretkeythatismorethan32chars
SPRING_BOOT_BASE_URL=https://cw-api-gateway-XXXX.onrender.com
AUTH_SERVICE_URL=https://cw-auth-service-XXXX.onrender.com
TASK_SERVICE_URL=https://cw-task-service-XXXX.onrender.com
BIDDING_SERVICE_URL=https://cw-bidding-service-XXXX.onrender.com
SOCKET_CORS_ORIGIN=http://localhost:3000,https://YOUR_VERCEL_APP.vercel.app
```

---

## STEP 5 — Vercel (Frontend) — YOU

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
6. Go back to Render and update:
   - Gateway `CORS_ALLOWED_ORIGINS` (include Vercel URL)
   - Chat `SOCKET_CORS_ORIGIN` (include Vercel URL)
   - Auth `APP_FRONTEND_URL` (Vercel URL — for email verification links)

---

## STEP 6 — Smoke test — YOU

1. Wake services: open Eureka, Auth, Task, Bidding, Profile, Gateway, Chat once each  
2. Gateway health: `https://cw-api-gateway-XXXX.onrender.com/actuator/health`  
3. Chat health: `https://cw-chat-service-XXXX.onrender.com/health`  
4. Open Vercel app → Register / Login → create task → bid  

---

## What was prepared in the repo (already done)

- PostgreSQL migration (driver + dialect + Neon-ready env vars)  
- Dockerfiles for all 6 Java services + chat  
- Root `render.yaml` Blueprint  
- Cloud `PORT` / Eureka / CORS / Feign direct URL support  
- Chat Atlas-ready `MONGODB_URI` + CORS list support  
- Frontend `vercel.json` SPA rewrites  

## What only YOU can do

1. Create Neon DBs + copy JDBC URLs/password  
2. Create MongoDB Atlas cluster + URI  
3. Push to GitHub (if needed)  
4. Apply Render Blueprint + paste secrets/URLs  
5. Deploy frontend on Vercel with `VITE_*` vars  
6. Wire final Vercel URL into CORS / email frontend URL  
7. Provide Gmail app password for verification emails  

---

## Notes / limits

- Render **free** account may limit how many services run at once — if Blueprint fails, create services one-by-one or upgrade plan.  
- Cold starts make Eureka flaky; **always set `GATEWAY_*_URI` and `TASK_SERVICE_URL` / `BIDDING_SERVICE_URL` direct HTTPS URLs**.  
- Tables are created by Hibernate (`ddl-auto=update`) on first start.  
- Chat does **not** use Neon — only MongoDB Atlas.  
