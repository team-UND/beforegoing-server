#!/bin/bash
set -euo pipefail

export ACTUATOR_PORT=10090
export HEALTH_CHECK_URL="http://localhost:$ACTUATOR_PORT/actuator/health"

echo "Performing health check on $HEALTH_CHECK_URL..."

for i in $(seq 1 60); do
  echo "Attempt $i/60..."

  if ! nc -z localhost $ACTUATOR_PORT 2>/dev/null; then
    echo "Port $ACTUATOR_PORT is not open yet"
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
