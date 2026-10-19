#!/bin/sh
# Arranque del contenedor.
# Si llegan los secretos del wallet de Oracle Autonomous Database (en base64),
# los reconstruye en /tmp/wallet y se los pasa a la JVM para la conexión mTLS.
set -e

set -- java -XX:MaxRAMPercentage=70

if [ -n "$WALLET_KEYSTORE_B64" ] && [ -n "$WALLET_TRUSTSTORE_B64" ]; then
  mkdir -p /tmp/wallet
  printf '%s' "$WALLET_KEYSTORE_B64"   | base64 -d > /tmp/wallet/keystore.jks
  printf '%s' "$WALLET_TRUSTSTORE_B64" | base64 -d > /tmp/wallet/truststore.jks
  set -- "$@" \
    "-Djavax.net.ssl.keyStore=/tmp/wallet/keystore.jks" \
    "-Djavax.net.ssl.keyStoreType=JKS" \
    "-Djavax.net.ssl.keyStorePassword=$WALLET_PASSWORD" \
    "-Djavax.net.ssl.trustStore=/tmp/wallet/truststore.jks" \
    "-Djavax.net.ssl.trustStoreType=JKS" \
    "-Djavax.net.ssl.trustStorePassword=$WALLET_PASSWORD"
fi

exec "$@" -jar /app/app.jar
