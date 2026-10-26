#!/bin/bash
set -euo pipefail

# APP_USER is created by the base image before initialization scripts run.
# Restrict the identifier before interpolating it into SQL.
if [[ ! "${APP_USER:-}" =~ ^[A-Za-z][A-Za-z0-9_]{0,29}$ ]]; then
    echo "APP_USER must be a simple Oracle identifier of at most 30 characters." >&2
    exit 1
fi

sqlplus -s / as sysdba <<SQL
WHENEVER OSERROR EXIT FAILURE
WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = ${APP_USER};
@/opt/oracle/library/oracle-schema.sql
EXIT SUCCESS
SQL

# The health check must wait for application schema initialization as well.
touch /opt/oracle/oradata/.library-schema-ready
