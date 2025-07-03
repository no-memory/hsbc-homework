#!/usr/bin/env bash


CWD=$(readlink -f "$(dirname "$0")")
cd "$CWD"

docker run --rm -p 8080:8080 hsbc-homework
