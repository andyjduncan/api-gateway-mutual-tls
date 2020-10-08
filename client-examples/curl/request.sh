#!/usr/bin/env bash

PATH_TO_KEY="$1"
PATH_TO_CERT="$2"
URL="$3"

curl -v --key "$PATH_TO_KEY" --cert "$PATH_TO_CERT" "$URL"