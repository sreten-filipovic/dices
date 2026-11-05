#!/usr/bin/env bash
# Turns the template into a new project.
#
# Usage:
#   ./rename.sh <package> <AppName> [android_app_id] [ios_bundle_id]
# Example:
#   ./rename.sh com.cubes.myapp "My App"
#   ./rename.sh rs.cubes.news "News App" rs.cubes.news.android rs.cubes.news.ios
#
# After running: delete this script, review README.md and commit.
set -euo pipefail

if [ $# -lt 2 ]; then
  echo "Usage: $0 <package> <AppName> [android_app_id] [ios_bundle_id]"
  exit 1
fi

NEW_PKG="$1"
NEW_NAME="$2"
NEW_APP_ID="${3:-$NEW_PKG}"
NEW_BUNDLE_ID="${4:-$NEW_PKG}"

OLD_PKG="com.cubes.template"
OLD_NAME="App Template"
OLD_ROOT_NAME="AppTemplate"

if ! [[ "$NEW_PKG" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]]; then
  echo "Package must look like com.company.app (lowercase, dot separated)."
  exit 1
fi

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

OLD_PATH="$(echo "$OLD_PKG" | tr . /)"
NEW_PATH="$(echo "$NEW_PKG" | tr . /)"
NEW_ROOT_NAME="$(echo "$NEW_NAME" | tr -cd '[:alnum:]')"

echo "Package:        $OLD_PKG -> $NEW_PKG"
echo "App name:       $OLD_NAME -> $NEW_NAME"
echo "Android app id: $NEW_APP_ID"
echo "iOS bundle id:  $NEW_BUNDLE_ID"
echo "Root project:   $OLD_ROOT_NAME -> $NEW_ROOT_NAME"
echo

# 1. Move source directories.
move_pkg_dir() {
  local base="$1"
  if [ -d "$base/$OLD_PATH" ]; then
    mkdir -p "$base/$NEW_PATH"
    # move contents, then remove the old empty tree
    (shopt -s dotglob; mv "$base/$OLD_PATH"/* "$base/$NEW_PATH"/)
    local dir="$base/$OLD_PATH"
    while [ "$dir" != "$base" ] && [ -d "$dir" ] && [ -z "$(ls -A "$dir")" ]; do
      rmdir "$dir"
      dir="$(dirname "$dir")"
    done
  fi
}
for base in \
  androidApp/src/main/java \
  shared/src/commonMain/kotlin \
  shared/src/androidMain/kotlin \
  shared/src/iosMain/kotlin \
  shared/src/commonTest/kotlin \
  shared/src/androidHostTest/kotlin \
  shared/src/androidDeviceTest/kotlin; do
  move_pkg_dir "$base"
done

# 2. Replace identifiers in text files.
is_macos() { [ "$(uname)" = "Darwin" ]; }
sed_i() { if is_macos; then sed -i '' "$@"; else sed -i "$@"; fi; }

FILES=$(grep -rl --exclude-dir=.git --exclude-dir=build --exclude-dir=.gradle --exclude-dir=.kotlin \
  --exclude=rename.sh \
  --include='*.kt' --include='*.kts' --include='*.xml' --include='*.swift' --include='*.plist' \
  --include='*.xcconfig' --include='*.pbxproj' --include='*.md' --include='*.toml' --include='*.properties' \
  -e "$OLD_PKG" -e "$OLD_NAME" -e "$OLD_ROOT_NAME" . || true)

for f in $FILES; do
  sed_i \
    -e "s/applicationId = \"$OLD_PKG\"/applicationId = \"$NEW_APP_ID\"/" \
    -e "s/BundleId = $OLD_PKG\.debug/BundleId = $NEW_BUNDLE_ID.debug/" \
    -e "s/BundleId = $OLD_PKG$/BundleId = $NEW_BUNDLE_ID/" \
    -e "s/$OLD_PKG/$NEW_PKG/g" \
    -e "s/$OLD_NAME/$NEW_NAME/g" \
    -e "s/$OLD_ROOT_NAME/$NEW_ROOT_NAME/g" \
    "$f"
done

echo "Done. Next steps:"
echo "  - Replace launcher icons (androidApp/src/main/res, iosApp/iosApp/Assets.xcassets)."
echo "  - Set BaseUrl in androidApp/build.gradle.kts and iosApp/iosApp/*.xcconfig."
echo "  - Set DEVELOPMENT_TEAM in iosApp/iosApp.xcodeproj/project.pbxproj."
echo "  - Run: ./gradlew :androidApp:assembleDebug"
echo "  - Delete rename.sh"
