#!/bin/bash
set -e

./mvnw package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t family-tree-backend:latest .
