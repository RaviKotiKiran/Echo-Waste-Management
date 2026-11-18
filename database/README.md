# Local MySQL database

The backend uses MySQL 8+ as its only database. Start the MySQL Server service before starting Spring Boot. The backend connects to `echo_waste` using `DB_URL`, `DB_USER`, and `DB_PASSWORD`; the local defaults match `create-database.sql`.

## Create the local database and account

1. Start MySQL Server 8+.
2. Open MySQL Workbench and connect with your MySQL administrator account.
3. Run `create-database.sql`. It creates `echo_waste`, user `echo_user`, and local password `echo_password`.
4. Start the backend from the `backend` folder with `.\mvnw.cmd spring-boot:run`. For a custom login, set `DB_URL`, `DB_USER`, and `DB_PASSWORD` in that same terminal first.

The local MySQL profile seeds demonstration accounts (with BCrypt-hashed passwords) and one pending pickup. Production should use a private password and reviewed schema migrations. `schema.sql` documents the MySQL table structure and does not insert demo accounts.

The former `backend/data/echo_waste.mv.db` H2 file is no longer used by the application and is left untouched as a data backup.
