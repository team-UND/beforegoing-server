#!/bin/bash
set -euo pipefail

export APP_PORT=8080
export HEALTH_CHECK_URL="http://localhost:$APP_PORT/actuator/health"

echo "Performing health check on $HEALTH_CHECK_URL..."

for i in $(seq 1 120); do
  echo "Attempt $i/120..."

  if ! nc -z localhost $APP_PORT 2>/dev/null; then
    echo "Port $APP_PORT is not open yet"
    echo "Retrying in 5 second..."
    sleep 5
    continue
  fi

  HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 $HEALTH_CHECK_URL || echo "000")

  if [ "$HEALTH_STATUS" -eq 200 ]; then
    echo "Application is healthy"
    exit 0
  fi

  echo "Health check failed with status $HEALTH_STATUS"
  echo "Retrying in 5 second..."
  sleep 5
done

echo "Health check failed after multiple retries"
exit 1
