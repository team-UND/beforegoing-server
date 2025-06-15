#!/bin/bash
set -euo pipefail

export AWS_DEFAULT_REGION="ap-northeast-2"
export ECR_REGISTRY="116541189059.dkr.ecr.ap-northeast-2.amazonaws.com"
export REPOSITORY_NAME="beforegoing-build"
export IMAGE_TAG="latest"
export CONTAINER_NAME="server"
export APP_PORT=8080

SPRING_PROFILE="preprod"

SPRING_DATASOURCE_PASSWORD=$(aws secretsmanager get-secret-value \
  --secret-id 'your-rds-master-user-secret-name-or-arn' \
  --query SecretString --output text --region ${AWS_REGION} \
  | jq -r .password)

ENV_FILE="/opt/server/.env"
if [ ! -f "$ENV_FILE" ]; then
    echo "Error: .env file not found at $ENV_FILE"
    echo "Please ensure .env file is deployed with the application"
    exit 1
fi

echo "Starting Spring Boot application container..."

echo "Pulling latest image: $ECR_REGISTRY/$REPOSITORY_NAME:$IMAGE_TAG"
docker pull $ECR_REGISTRY/$REPOSITORY_NAME:$IMAGE_TAG

echo "Starting new container: $CONTAINER_NAME"
docker run -d \
  --name $CONTAINER_NAME \
  --restart unless-stopped \
  --env-file $ENV_FILE \
  -p $APP_PORT:$APP_PORT \
  -v /var/log/$REPOSITORY_NAME:/app/logs \
  -e SPRING_PROFILES_ACTIVE=$SPRING_PROFILE \
  -e SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD}" \
  -e TZ=Asia/Seoul \
  $ECR_REGISTRY/$REPOSITORY_NAME:$IMAGE_TAG

echo "Checking container status..."
for i in {1..30}; do
    if docker ps --filter name=$CONTAINER_NAME --filter status=running -q | grep -q .; then
        echo "Container started successfully after $i attempts"
        break
    elif [ $i -eq 30 ]; then
        echo "Container failed to start after 30 attempts"
        docker logs $CONTAINER_NAME
        exit 1
    else
        echo "Attempt $i/30: Container not ready yet, waiting..."
        sleep 3
    fi
done

echo "Container startup completed successfully!"
