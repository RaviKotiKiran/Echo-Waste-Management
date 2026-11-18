# Echo Waste Management

Echo is a full-stack waste collection MVP connecting citizens, collection teams, and administrators. Citizens book and track pickups, look up common disposal guidance, and report service problems; administrators approve requests, assign collectors, and triage resident issues; collectors update collection progress; residents can leave feedback after a completed pickup.

## Architecture

```text
React + Vite (Bootstrap layout, React Router, Axios)
                    │ JWT over REST/JSON
                    ▼
Spring Boot: Controller → Service → Repository → JPA Entity
                    │ JDBC
                    ▼
          MySQL 8+
```

## Technology

- Frontend: React 19, Vite, React Router, Axios, Bootstrap 5, React Icons, and project-owned responsive CSS.
- Backend: Java 17, Spring Boot, Spring Web, Spring Security, JWT, Spring Data JPA, Bean Validation, Maven.
- Database: MySQL 8+ for local development and deployment, with Flyway schema migrations in production. H2 is not an application dependency or runtime option.

## Folder guide

- `frontend/src/pages`: public pages and role-specific portal screens.
- `frontend/src/context`: current login state and role profile.
- `frontend/src/services/api.js`: Axios base URL and JWT header handling.
- `backend/src/main/java/com/echo/waste/controller`: REST request/response handling.
- `backend/src/main/java/com/echo/waste/service`: request, account, dashboard, and feedback business rules.
- `backend/src/main/java/com/echo/waste/repository`: Spring Data database operations.
- `backend/src/main/java/com/echo/waste/entity`: JPA persistence entities stored in MySQL.
- `backend/src/main/java/com/echo/waste/dto`: validated API input and safe output records.
- `backend/src/main/java/com/echo/waste/security` and `config`: JWT filter, stateless access rules, CORS, and local demo seeding.
- `database`: MySQL database creation and reference schema.

## Resident experience additions

- **Waste guide** (`/guide`, also available inside every signed-in workspace): search and filter common materials by recycling, organics, special handling, general waste, or reuse. Guidance is intentionally general; collection rules vary by municipality.
- **Service issue reports** (resident workspace): submit missed-pickup, damaged-bin, illegal-dumping, or other reports, optionally linking a pickup request. Residents can follow Open, In review, and Resolved status.
- **Issue queue** (admin workspace): filter incoming reports and move open issues into review or resolution. Resolved reports are locked to preserve a clear service record.
- **In-app alerts**: residents hear about pickup and report updates, collectors receive assignment alerts, and admins get notified about new pickups and issues. The alert bell shows unread counts and links to the relevant screen.
- **Collection calendar**: residents can view monthly pickup dates, request statuses, upcoming collections, and planned waste quantity from their saved requests.

## Local Windows setup (MySQL required)

The default `mysql` profile connects to MySQL at `localhost:3306`, using the `echo_waste` database and the `echo_user` local account. Hibernate updates the local schema and seeds demo users and one example pickup. Start the MySQL Server service before launching the backend.

> **Important:** npm commands are for the React frontend only. The `backend` is a Java/Maven project and does not contain `package.json`. Running `npm install` or `npm run` from `backend` will produce an `ENOENT package.json` error. You can launch each part using `start-backend.cmd` and `start-frontend.cmd` in the project root, or follow the commands below in separate terminals.

### Prepare the MySQL database

Start the MySQL Server service and confirm it is listening on `localhost:3306`. If Windows reports an access or permissions error, start the service from Windows Services with administrator access. In MySQL Workbench, connect to your local server as a MySQL administrator, open `database/create-database.sql`, and execute it. It creates `echo_waste` and configures the local app account `echo_user` with password `echo_password`; it does not drop existing tables.

For custom MySQL credentials, set `DB_URL`, `DB_USER`, and `DB_PASSWORD` in the same terminal. If you see `Access denied`, reconnect to MySQL Workbench as the administrator and rerun the SQL script. Do not put a real password in source files or chat.

### Start Echo locally

1. Start the MySQL Server service and run `database/create-database.sql` once in Workbench if you have not already.
2. From the project root in VS Code, run this single command:

   ```powershell
   .\start-echo.cmd
   ```

3. Keep the backend and frontend terminal windows open. Open the Vite URL shown in its terminal, normally `http://localhost:5173`.

The launcher checks MySQL, reuses Echo if its API is already healthy, and selects a free backend port from `8081` to `8090` if needed. It passes the chosen port to the frontend proxy. Do not start a second backend manually. The launcher prints the API health URL. The project uses Java 17 or newer and MySQL 8+; the Maven wrapper manages Maven. For deployment, set `VITE_API_URL` to the public API base URL.

### Demonstration accounts (local MySQL profile only)

| Role | Email | Password |
|---|---|---|
| Administrator | `admin@echo.local` | `Admin@123` |
| Collector | `collector@echo.local` | `Collector@123` |
| Citizen | `citizen@echo.local` | `Citizen@123` |

For an end-to-end demo, sign in as admin and approve the seeded pending request, assign it to the demo collector, sign in as the collector to start and complete the pickup, then sign in as the citizen to see its status and submit feedback.

## REST API

All private endpoints require `Authorization: Bearer <JWT>`. Registration always creates a Citizen; only an administrator can create Collector accounts.

| Method | Endpoint | Access / purpose |
|---|---|---|
| POST | `/api/auth/register` | Public; register citizen |
| POST | `/api/auth/login` | Public; login and receive JWT |
| GET | `/api/health` | Public health check |
| GET / POST | `/api/requests` | List role-appropriate requests / citizen creates a pickup |
| GET | `/api/requests/{id}` | Own citizen request, assigned collector request, or admin request |
| GET | `/api/requests/my-requests` | Citizen's requests |
| PUT | `/api/requests/{id}/cancel` | Citizen cancels own pending request |
| GET | `/api/citizen/dashboard` | Citizen dashboard counts |
| GET | `/api/citizen/calendar?month=YYYY-MM` | Signed-in citizen's pickup calendar for one month |
| GET | `/api/collector/dashboard` | Collector dashboard counts |
| GET | `/api/collector/assignments` | Collector's assigned pickups |
| PUT | `/api/collector/assignments/{assignmentId}/status` | Collector updates to `IN_PROGRESS` or `COLLECTED` |
| GET | `/api/admin/dashboard` | Admin statistics from MySQL |
| GET | `/api/admin/requests` | Admin request queue |
| PUT | `/api/admin/requests/{id}/approve` | Approve pending request |
| PUT | `/api/admin/requests/{id}/reject` | Reject pending request |
| PUT | `/api/admin/requests/{id}/assign` | Assign approved request with `{ "collectorId": 2 }` |
| GET | `/api/users` | Admin user list; passwords are never returned |
| GET / PUT / DELETE | `/api/users/{id}` | Read/update own profile; admin may manage users (delete is blocked when history exists) |
| GET / POST | `/api/admin/collectors` | Admin list/create collectors |
| POST | `/api/feedback` | Citizen rates a completed own pickup |
| GET | `/api/feedback` | Admin feedback list |
| GET / POST | `/api/issues` | Citizen lists own service reports / creates a report |
| GET | `/api/admin/issues` | Admin reviews all service reports |
| PATCH | `/api/admin/issues/{id}/status` | Admin moves an issue to `IN_REVIEW` or `RESOLVED` |
| GET | `/api/notifications` | Signed-in user gets their latest alerts and unread count |
| PATCH | `/api/notifications/{id}/read` | Mark one of the current user's alerts as read |
| POST | `/api/notifications/read-all` | Mark all of the current user's alerts as read |

Request status flow: `PENDING → APPROVED → ASSIGNED → IN_PROGRESS → COLLECTED`. Citizens may cancel only `PENDING` requests; admins may also reject a pending request.

## Environment variables

`backend/.env.example` lists backend settings; Spring Boot does not automatically load `.env`, so configure variables in the terminal or hosting dashboard. `frontend/.env.example` is for Vite and can be copied to `.env` locally.

Important production settings:

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL=jdbc:mysql://<mysql-host>:3306/echo_waste`
- `DB_USER` and `DB_PASSWORD` from the database provider
- `JWT_SECRET` set to a private random value of at least 32 characters
- `FRONTEND_URL=https://<your-vercel-domain>`
- `VITE_API_URL=https://<your-render-api-domain>/api`

Never commit real environment values. The local `dev` signing key and demo passwords are for development only.

## Deployment

For a prepared deployment path using Vercel, Railway, and MySQL, follow [DEPLOYMENT.md](DEPLOYMENT.md). Production schema changes run through Flyway migrations; demo account seeding is limited to the local MySQL profile. No service has been deployed or verified yet.

## Security and MVP boundaries

Passwords are BCrypt-hashed. JWT-protected APIs enforce roles and ownership on the server. The browser stores the short-lived access token in `sessionStorage` for this learning/demo MVP. For a public production system, move authentication to secure HttpOnly cookies, add email verification/password recovery, rate limiting, audit logging, and managed database migrations.
