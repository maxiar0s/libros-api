FROM gvenzl/oracle-free:23.26.3-slim

# Reuse the same schema as Oracle Cloud without copying application secrets.
COPY database/oracle-schema.sql /opt/oracle/library/oracle-schema.sql
COPY --chmod=755 database/init-library.sh /container-entrypoint-initdb.d/001-init-library.sh
