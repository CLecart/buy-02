#!/usr/bin/env bash
set -euo pipefail

# Collect surefire XML reports from modules, create an archive and SHA256 checksums
# Usage: ./scripts/package-audit.sh [output-dir]

OUT_DIR=${1:-out}
mkdir -p "$OUT_DIR"
TS=$(date -u +%Y%m%dT%H%M%SZ)
ARCHIVE_NAME="audit-artifacts-${TS}.tar.gz"
TMPDIR=$(mktemp -d)

echo "Packaging audit artifacts into $OUT_DIR/$ARCHIVE_NAME"

# gather surefire xmls safely into a reports/ directory inside the temp dir
while IFS= read -r -d '' file; do
	# remove leading ./ if present
	rel=${file#./}
	dest="$TMPDIR/reports/$rel"
	mkdir -p "$(dirname "$dest")"
	cp "$file" "$dest" || true
done < <(find . -type f -path "*/target/surefire-reports/*.xml" -print0)

# Also include build metadata
echo "commit=$(git rev-parse --verify HEAD 2>/dev/null || echo unknown)" > "$TMPDIR/build-info.txt"
echo "date=$TS" >> "$TMPDIR/build-info.txt"
echo "user=$(whoami)" >> "$TMPDIR/build-info.txt"

tar -C "$TMPDIR" -czf "$OUT_DIR/$ARCHIVE_NAME" .

pushd "$OUT_DIR" >/dev/null
sha256sum "$ARCHIVE_NAME" > "${ARCHIVE_NAME}.sha256"
popd >/dev/null

echo "Created: $OUT_DIR/$ARCHIVE_NAME"
echo "Checksum: $OUT_DIR/${ARCHIVE_NAME}.sha256"

rm -rf "$TMPDIR"

exit 0
#!/usr/bin/env bash
set -eu
# Simple helper to collect surefire XMLs, package them and compute checksums for audit.

OUT_DIR=out/audit-$(date -u +%Y%m%dT%H%M%SZ)
mkdir -p "$OUT_DIR"

echo "Collecting surefire XMLs..."
find . -path "*/target/surefire-reports/*.xml" -print0 | xargs -0 -I{} bash -c 'mkdir -p "$OUT_DIR/$(dirname {})"; cp {} "$OUT_DIR/{}"' || true

echo "Copying important artifacts (jars, frontend) into bundle..."
mkdir -p "$OUT_DIR/artifacts"
find . -type f -name "*.jar" -print0 | xargs -0 -I{} cp {} "$OUT_DIR/artifacts/" || true
if [ -d frontend-angular/dist ]; then cp -r frontend-angular/dist "$OUT_DIR/artifacts/frontend" || true; fi

echo "Compute SHA256 checksums for artifacts..."
pushd "$OUT_DIR/artifacts" >/dev/null || true
find . -type f -print0 | xargs -0 sha256sum > ../sha256.txt || true
popd >/dev/null || true

echo "Zipping audit bundle..."
pushd out >/dev/null
ZIP_NAME="audit-bundle-$(date -u +%Y%m%dT%H%M%SZ).zip"
zip -r "$ZIP_NAME" "$(basename $OUT_DIR)" >/dev/null || true
echo "Created $ZIP_NAME"
popd >/dev/null

echo "Audit bundle created in out/ with checksums at $OUT_DIR/sha256.txt"
