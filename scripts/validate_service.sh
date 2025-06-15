#!/bin/bash
set -euo pipefail

export APP_PORT=8080
export HEALTH_CHECK_URL="http://localhost:$APP_PORT/actuator/health"

echo "Performing health check on $HEALTH_CHECK_URL..."
for i in $(seq 1 60); do
  HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_CHECK_URL)
  if [ "$HEALTH_STATUS" -eq 200 ]; then
    echo "Application is healthy"
    exit 0
  fi
  echo "Health check failed with status $HEALTH_STATUS"
  echo "Retrying in 1 second..."
  sleep 1
done

echo "Health check failed after multiple retries"
exit 1
