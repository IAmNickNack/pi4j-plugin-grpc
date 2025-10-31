#!/usr/bin/env bash

BUILD_VERSION=$(./gradlew :pi4j-plugin-grpc:properties -q | grep 'version:' | awk '{print $2}')
DIST_ZIP=$(find . -name "pi4j-plugin-grpc-server-${BUILD_VERSION}.zip")
SHADOW_JAR=$(find . -name "pi4j-plugin-grpc-server-${BUILD_VERSION}-all.jar")

# Stop if DIST_ZIP is empty
if [ -z "${DIST_ZIP}" ]; then
  echo "ERROR: No distribution zip not found for version ${BUILD_VERSION}"
  exit 1
fi

# Set IS_SNAPSHOT to true if BUILD_VERSION ends with `SNAPSHOT`
if [[ "${BUILD_VERSION}" == *SNAPSHOT ]]; then
  IS_SNAPSHOT=true
else
  IS_SNAPSHOT=false
fi

# Set RELEASE_EXISTS to true if gh release list contains "v${BUILD_VERSION}"
if gh release list | grep -q "v${BUILD_VERSION}"; then
  RELEASE_EXISTS=true
else
  RELEASE_EXISTS=false
fi

# If this is a non-snapshot release and the release already exists, abort to avoid overwriting it.
if [ "${IS_SNAPSHOT}" = "false" ] && [ "${RELEASE_EXISTS}" = "true" ]; then
  echo "ERROR: Release v${BUILD_VERSION} already exists and this is not a snapshot. Aborting to avoid overwrite."
  exit 1
fi

# If the release doesn't exist, create it
if [ "${RELEASE_EXISTS}" = "false" ]; then
  echo "Creating release: v${BUILD_VERSION}"

  if [ "${IS_SNAPSHOT}" = "true" ]; then
    echo "Marking release as prerelease (snapshot)"
    gh release create "v${BUILD_VERSION}" --title "v${BUILD_VERSION}" --notes "Automated snapshot release for version ${BUILD_VERSION}" --prerelease
    RC=$?
  else
    gh release create "v${BUILD_VERSION}" --title "v${BUILD_VERSION}" --notes "Release for version ${BUILD_VERSION}"
    RC=$?
  fi

  if [ ${RC} -ne 0 ]; then
    echo "ERROR: Failed to create release v${BUILD_VERSION}"
    exit 1
  fi
fi

echo "Uploading ${DIST_ZIP} to v${BUILD_VERSION}"
gh release upload "v${BUILD_VERSION}" "${DIST_ZIP}" "${SHADOW_JAR}" --clobber
RC=$?
if [ ${RC} -ne 0 ]; then
  echo "ERROR: Failed to upload asset to existing release v${BUILD_VERSION}"
  exit 1
fi
echo "Asset uploaded to existing snapshot release v${BUILD_VERSION}."
