#!/bin/bash
docker build --tag safe-docker:latest --tag safe-docker:`date +%Y%m%d` --pull=true --no-cache=true .
docker image ls --quiet --no-trunc --filter before=safe-docker:latest safe-docker | tail -n +2 | xargs -r docker rmi
