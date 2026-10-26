$ErrorActionPreference = 'Stop'
Push-Location (Join-Path $PSScriptRoot '../..')
try {
    $json = & docker compose config --format json
    if ($LASTEXITCODE -ne 0) { throw 'Compose configuration could not be parsed.' }
    $config = $json | ConvertFrom-Json
    $app = $config.services.springboot
    $oracle = $config.services.oracle

    if (-not $app) { throw 'Spring Boot service is missing.' }
    if ($app.build.dockerfile -ne 'Dockerfile.springboot') { throw 'Unexpected application Dockerfile.' }
    if ($app.depends_on.oracle.condition -ne 'service_healthy') { throw 'Application must wait for healthy Oracle.' }
    if ($app.environment.ORACLE_URL -ne 'jdbc:oracle:thin:@//oracle:1521/FREEPDB1') {
        throw 'Container JDBC URL must use the Oracle service name.'
    }
    if ($app.environment.ORACLE_USERNAME -ne $oracle.environment.APP_USER -or
        $app.environment.ORACLE_PASSWORD -ne $oracle.environment.APP_USER_PASSWORD) {
        throw 'Application and database credentials must match.'
    }
    if ($app.environment.PORT -ne '8080' -or $app.ports[0].target -ne 8080 -or
        $app.ports[0].host_ip -ne '127.0.0.1') { throw 'Unexpected HTTP port mapping.' }

    # Adding the application must preserve the existing local database contract.
    if ($oracle.build.dockerfile -ne 'Dockerfile' -or
        $oracle.ports[0].target -ne 1521 -or $oracle.ports[0].host_ip -ne '127.0.0.1' -or
        $oracle.volumes[0].source -ne 'oracle-data' -or
        $oracle.volumes[0].target -ne '/opt/oracle/oradata') {
        throw 'Existing Oracle image, local port or persistent volume changed.'
    }
    Write-Output 'PASS: application wiring, healthy dependency, ports and Oracle persistence.'
} finally {
    Pop-Location
}
