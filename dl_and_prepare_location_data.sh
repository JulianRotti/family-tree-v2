#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET_DIR="$SCRIPT_DIR/infra/mysql"
TMP_DIR="$(mktemp -d)"

echo "→ Downloading GeoNames data..."
curl -sS --show-error -o "$TMP_DIR/cities500.zip" \
    "https://download.geonames.org/export/dump/cities500.zip"
curl -sS --show-error -o "$TMP_DIR/countryInfo.txt" \
    "https://download.geonames.org/export/dump/countryInfo.txt"

echo "→ Extracting cities..."
unzip -q "$TMP_DIR/cities500.zip" -d "$TMP_DIR"

echo "→ Preparing cities_slim.tsv..."
awk -F'\t' 'BEGIN{OFS="\t"} {print $1,$2,$3,$5,$6,$9,$15}' \
    "$TMP_DIR/cities500.txt" > "$TARGET_DIR/cities_slim.tsv"

echo "→ Preparing countries_slim.tsv..."
grep -v '^#' "$TMP_DIR/countryInfo.txt" | \
awk -F'\t' 'BEGIN{OFS="\t"} NF >= 5 {print $1,$5}' \
    > "$TARGET_DIR/countries_slim.tsv"

echo "→ Cleaning up..."
rm -rf "$TMP_DIR"

echo ""
echo "✓ Done. Files written to $TARGET_DIR:"
echo "  - countries_slim.tsv ($(wc -l < "$TARGET_DIR/countries_slim.tsv") rows)"
echo "  - cities_slim.tsv    ($(wc -l < "$TARGET_DIR/cities_slim.tsv") rows)"
