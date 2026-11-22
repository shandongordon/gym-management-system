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

The app automatically seeds 12 fictional members from *Jujutsu Kaisen* and *Fairy Tail* when the members table is empty, plus member `1013` (active, payment pending) for check-in testing. Try searching `Gojo`, `Natsu`, or `1001`.

## Implemented features

- Employee login/logout and authenticated sessions
- Dashboard and member search by ID or name
- Empty/invalid input handling and no-results messages
- Member selection handoff for membership verification
- Membership verification: status, payment status, last check-in, and verification warnings
- Approve or deny check-in, with a required reason for a denial
- Attendance recording, attendance history per member, and check-out

### Membership verification and check-in

Search for a member, choose **Select Member**, then **Verify Membership & Check-In**. The screen is at `/members/{memberId}/check-in`.

| Member | Membership | Payment | Result |
| --- | --- | --- | --- |
| `1004` Satoru Gojo | Active | Paid | No warnings; check-in can be approved |
| `1013` Gajeel Redfox | Active | Pending | Warning: payment pending; check-in can still be approved |
| `1002` Megumi Fushiguro | Expired | Pending | Warnings: membership expired, payment pending; approve unavailable |
| `1003` Nobara Kugisaki | Suspended | Paid | Warning: membership suspended; approve unavailable |

Rules:

- A membership also counts as expired once its expiration date has passed.
- Check-in can be approved only for an active membership whose member is not already checked in.
- A pending payment is a warning only; the employee decides.
- A denial requires a reason, and the reason "Other" also requires notes.
- Every decision is saved in the `attendance_records` table, which JPA creates on first start.

## Tests

```bash
mvn clean test
```

Tests cover login, member search, access protection, member selection, membership verification, approve/deny check-in, and attendance recording. They use an in-memory test database, so MySQL is not required for running tests. The running application uses MySQL.
