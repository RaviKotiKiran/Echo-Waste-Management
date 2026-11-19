# Local databases

By default, local development uses an embedded H2 database stored under `backend/data/`; MySQL is optional locally and required for production. Demo accounts are seeded only outside the `prod` profile.

## Optional: create a local MySQL database and account

1. Start MySQL Server 8+.
2. Open MySQL Workbench and connect with your MySQL administrator account.
3. Run `create-database.sql`. It creates `echo_waste`, user `echo_user`, and local password `echo_password`.
4. Start Echo with `.\start-echo.cmd`; it detects MySQL on `localhost:3306` and uses the `mysql` profile. For custom credentials, set `DB_URL`, `DB_USER`, and `DB_PASSWORD` before starting it.

`schema.sql` documents the MySQL table structure and does not insert demo accounts. Production requires configured MySQL credentials, a private JWT secret, and a specific frontend origin; it does not use H2 or seed demo accounts.
