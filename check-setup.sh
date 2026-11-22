#!/usr/bin/env bash
# Read-only environment check. Does not install, start, or modify services.
set -u

if [[ ! -f pom.xml ]]; then
  echo "ERROR: Run this script from the project root (the folder containing pom.xml)."
  exit 1
fi

missing=0
printf '\nElevate Fitness - setup check\n============================\n'

if command -v java >/dev/null 2>&1; then
  version_line="$(java -version 2>&1 | head -n 1)"
  echo "OK: Java -> $version_line"
  java_major="$(printf '%s' "$version_line" | sed -nE 's/.*version "([0-9]+).*/\1/p')"
  if [[ -n "$java_major" && "$java_major" =~ ^[0-9]+$ && "$java_major" -lt 17 ]]; then
    echo "ERROR: Java 17 or newer is required."
    missing=1
  fi
else
  echo "MISSING: Java JDK 17+"
  missing=1
fi

if command -v mvn >/dev/null 2>&1; then
  echo "OK: Maven -> $(mvn -version 2>&1 | head -n 1)"
else
  echo "MISSING: Maven 3.6.3+"
  missing=1
fi

if command -v mysql >/dev/null 2>&1; then
  echo "OK: MySQL client -> $(mysql --version 2>&1)"
elif command -v brew >/dev/null 2>&1 && [[ -x "$(brew --prefix mysql@8.4 2>/dev/null)/bin/mysql" ]]; then
  echo "OK: MySQL client -> $("$(brew --prefix mysql@8.4)/bin/mysql" --version 2>&1)"
else
  echo "MISSING: MySQL client (recommended: MySQL 8.4)"
  missing=1
fi

if [[ -f sql/setup.local.sql ]]; then
  echo "OK: Local SQL setup file exists."
  if grep -q 'CHANGE_THIS_PASSWORD' sql/setup.local.sql; then
    echo "WARNING: sql/setup.local.sql still contains CHANGE_THIS_PASSWORD."
  fi
else
  echo "INFO: Local SQL setup not found. Copy sql/setup.sql to sql/setup.local.sql and set a private password."
fi

if [[ -n "${DB_PASSWORD:-}" ]]; then
  echo "OK: DB_PASSWORD environment variable is set (value hidden)."
else
  echo "INFO: DB_PASSWORD is not set in this shell. Set it before starting the application."
fi

printf '\n'
if [[ "$missing" -eq 0 ]]; then
  echo "Prerequisite commands found. This script does not verify DB authentication or run the application."
else
  echo "One or more required tools are missing. See README.md for installation instructions."
fi
exit "$missing"
