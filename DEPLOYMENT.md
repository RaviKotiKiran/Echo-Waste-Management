# Deploy Echo

This repository is prepared for a first production deployment with a Vercel static frontend, a Railway Spring Boot API, and Railway MySQL. No local Docker installation is needed. Railway builds the backend from its `backend/` directory; the included Dockerfile is used by the host's build service.

## 1. Push the project to GitHub

Create a private GitHub repository, then from the project root:

```powershell
git init
git add .
git commit -m "Prepare Echo for deployment"
git branch -M main
git remote add origin https://github.com/YOUR_ACCOUNT/echo-waste-management.git
git push -u origin main
```

Replace the remote URL with the repository you created. Do not commit `.env` files, database passwords, or signing keys.

## 2. Create the production MySQL database

In Railway, create a project and add a MySQL database service. Keep it private to the project. Enable database backups before using it for real resident data. Use the generated database variables from Railway; do not use the local `echo_user` credentials from `database/create-database.sql`.

## 3. Deploy the backend

Create a Railway service from the GitHub repository and set its **Root Directory** to `/backend`. Railway will use `backend/Dockerfile` and the service's injected `PORT`. Generate a public domain for the API service.

Set these variables on the backend service (replace `MySQL` with the exact database service name if it differs):

| Variable | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?serverTimezone=UTC` |
| `DB_USER` | `${{MySQL.MYSQLUSER}}` |
| `DB_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
| `JWT_SECRET` | Generate a private random value of at least 32 characters in the hosting dashboard |
| `FRONTEND_URL` | `https://YOUR-VERCEL-PROJECT.vercel.app` |
| `BOOTSTRAP_ADMIN_EMAIL` | Your admin email for the first deployment |
| `BOOTSTRAP_ADMIN_PASSWORD` | A unique password of at least 12 characters for the first deployment |
| `BOOTSTRAP_ADMIN_NAME` | Your preferred admin display name |

The first production startup runs Flyway migration `V1__initial_schema.sql`, then creates the initial admin only if that email does not already exist. After the first successful deployment, remove the three `BOOTSTRAP_ADMIN_*` variables. The admin account remains in MySQL. Never use the local demo accounts in production.

Set the Railway health check path to `/api/health`. Confirm the backend deploy logs show successful Flyway migration and application startup, then open `https://YOUR-RAILWAY-DOMAIN/api/health`; it should return `{"status":"ok","application":"echo-waste","database":"UP"}`. If the database probe fails, the endpoint returns HTTP 503 with `"status":"degraded"` and `"database":"DOWN"`.

## 4. Deploy the frontend

Import the same GitHub repository into Vercel. Set **Root Directory** to `frontend`; use Vite, build command `npm run build`, and output directory `dist`. Add this environment variable before deploying:

| Variable | Value |
|---|---|
| `VITE_API_URL` | `https://YOUR-RAILWAY-DOMAIN/api` |

After Vercel gives you the production URL, set that exact origin in Railway as `FRONTEND_URL` and redeploy the backend. The API CORS policy accepts the configured frontend origin.

## 5. Verify the live deployment

1. Open the frontend URL and register a citizen account.
2. Sign in and submit a test pickup request.
3. Sign in with the bootstrap admin and check the request queue.
4. Confirm the request and account appear in the production MySQL database.
5. Remove bootstrap admin environment variables and confirm a subsequent backend redeploy retains the admin account.
6. Set up a custom domain and HTTPS only after these checks pass.

## Production settings and limitations

- Production requires `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, and `FRONTEND_URL`; the app fails fast when they are absent.
- Production schema changes must be added as new Flyway migrations. Do not edit an already-applied migration.
- Production does not seed demo users or demo pickups. Admin access is provisioned with the one-time bootstrap variables above.
- The backend includes automated unit tests; CI runs them via `mvn verify`.
- Before accepting real personal data, add an appropriate privacy notice and retention process, review account recovery and abuse controls, and rehearse database backup restoration.
