#!/bin/bash
set -e

VERSION=1.0.1
IMAGE=ghcr.io/julianrotti/family-tree-backend

./mvnw package -DskipTests

docker build -f src/main/docker/Dockerfile.jvm -t $IMAGE:$VERSION .
docker tag $IMAGE:$VERSION $IMAGE:latest

# dann (später durch github actions oder andere pipeline ersetzen):
# echo <personal access token ghcr> | docker login ghcr.io -u JulianRotti --password-stdi
# docker push ghcr.io/julianrotti/family-tree-backend:$VERSION
# docker push ghcr.io/julianrotti/family-tree-backend:latest