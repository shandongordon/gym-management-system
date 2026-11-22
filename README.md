# Elevate Fitness Gym Management System

CEN4021 Software Engineering II — Deliverable 5 prototype.

A Spring Boot web app for gym front desk employees to log in, find members, membership verification and check-in.

## Requirements

- Java 17+
- Maven 3.6.3+
- MySQL 8.4

Java dependencies are managed by `pom.xml` and installed automatically by Maven.

On macOS with Homebrew, install prerequisites using `brew bundle` (or install Java, Maven, and MySQL individually). Start MySQL with:

```bash
brew services start mysql@8.4
```

## Setup

Run these commands from the project folder containing `pom.xml`.

1. Create a local database setup file:

   ```bash
   cp sql/setup.sql sql/setup.local.sql
   ```

2. In `sql/setup.local.sql`, replace both instances of `CHANGE_THIS_PASSWORD` with the same password. **Do not commit this file.**

3. Set up the database:

   ```bash
   "$(brew --prefix mysql@8.4)/bin/mysql" -u root < sql/setup.local.sql
   ```

   If your MySQL root account requires a password, use `-u root -p` instead.

4. Provide the app's database password:

   ```bash
   read -s "DB_PASSWORD?Database password: "
   echo
   export DB_PASSWORD
   ```

## Run

```bash
mvn clean test
mvn spring-boot:run
```

Open **http://localhost:8080/login**. Press `Ctrl+C` to stop the app.

### Demo logins

| Username | Password |
| --- | --- |
| `frontdesk` | `Elevate123!` |
| `manager` | `Manager123!` |

The app automatically seeds 12 fictional members from *Jujutsu Kaisen* and *Fairy Tail* when the members table is empty. Try searching `Gojo`, `Natsu`, or `1001`.

## Implemented features

- Employee login/logout and authenticated sessions
- Dashboard and member search by ID or name
- Empty/invalid input handling and no-results messages
- Member selection handoff for membership verification

**Still in development:** membership verification, approve/deny check-in, and attendance recording (another team member's portion).

## Tests

```bash
mvn clean test
```

Tests cover login, member search, access protection, and member selection. They use an in-memory test database, so MySQL is not required for running tests. The running application uses MySQL.
