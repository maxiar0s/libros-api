#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/../.."

# Run with TMPDIR pointing to a temporary directory outside the project.
: "${TMPDIR:?Set TMPDIR to an external temporary directory}"
probe=$(mktemp -d)
trap 'rm -rf "$probe"' EXIT
export PROBE="$probe"

# Exercise bootstrap control flow without connecting to Oracle or writing its volume.
sqlplus() {
    cat > "$PROBE/sql"
    return "$SQL_EXIT"
}
touch() {
    printf '%s\n' "$@" > "$PROBE/marker"
}
export -f sqlplus touch

before=$(sha256sum database/oracle-schema.sql)
for scenario in success sqlfail empty malformed overlong; do
    rm -f "$probe/sql" "$probe/marker"
    export APP_USER=LIBRARY_APP SQL_EXIT=0
    case "$scenario" in
        sqlfail) SQL_EXIT=17 ;;
        empty) APP_USER='' ;;
        malformed) APP_USER='APP;DROP' ;;
        overlong) APP_USER=ABCDEFGHIJKLMNOPQRSTUVWXYZABCDE ;;
    esac

    result=0
    bash --noprofile --norc database/init-library.sh >"$probe/out" 2>"$probe/err" || result=$?
    test ! -s "$probe/out"
    case "$scenario" in
        success)
            test "$result" = 0
            test ! -s "$probe/err"
            test "$(cat "$probe/marker")" = /opt/oracle/oradata/.library-schema-ready
            printf '%s\n' \
                'WHENEVER OSERROR EXIT FAILURE' \
                'WHENEVER SQLERROR EXIT FAILURE ROLLBACK' \
                'ALTER SESSION SET CONTAINER = FREEPDB1;' \
                'ALTER SESSION SET CURRENT_SCHEMA = LIBRARY_APP;' \
                '@/opt/oracle/library/oracle-schema.sql' \
                'EXIT SUCCESS' >"$probe/expected"
            cmp "$probe/sql" "$probe/expected"
            ;;
        sqlfail)
            test "$result" = 17
            test ! -e "$probe/marker"
            test ! -s "$probe/err"
            ;;
        *)
            test "$result" = 1
            test ! -e "$probe/marker"
            test ! -e "$probe/sql"
            printf '%s\n' \
                'APP_USER must be a simple Oracle identifier of at most 30 characters.' \
                >"$probe/expected"
            cmp "$probe/err" "$probe/expected"
            ;;
    esac
    test "$before" = "$(sha256sum database/oracle-schema.sql)"
    printf '%s: PASS\n' "$scenario"
done
