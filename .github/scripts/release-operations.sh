#!/usr/bin/env bash

export BUILD_VERSION=$(./gradlew :pi4j-plugin-grpc:properties -q | grep 'version:' | awk '{print $2}')
export BUILD_VERSION=$(./gradlew :pi4j-plugin-grpc:properties -q | grep 'version:' | awk '{print $2}')
export DIST_ZIP=$(find . -name "pi4j-plugin-grpc-server-${BUILD_VERSION}.zip")
export SHADOW_JAR=$(find . -name "pi4j-plugin-grpc-server-${BUILD_VERSION}-all.jar")

# `true` if BUILD_VERSION ends with `SNAPSHOT`
is_snapshot() {
  [[ "${BUILD_VERSION}" == *SNAPSHOT ]]
}

# `true` if gh release list contains "v${BUILD_VERSION}"
release_exists() {
  if gh release list | grep -qF "v${BUILD_VERSION}"; then
    return 0
  else
    return 1
  fi
}

# `true` if either this is a non-snapshot which doesn't already exist,
# or this is a SNAPSHOT build which can be overridden
can_release() {
  if [[ ! is_snapshot && release_exists ]]; then
    echo "ERROR: Release v${BUILD_VERSION} already exists and this is not a snapshot" >&2
    return 1
  else
    return 0
  fi
}

# If the release doesn't exist, create it
create_github_release() {
  if ! release_exists; then
    echo "Creating release: v${BUILD_VERSION}"

    if is_snapshot; then
      echo "Marking release as prerelease (snapshot)"
      gh release create "v${BUILD_VERSION}" --title "v${BUILD_VERSION}" --notes "Automated snapshot release for version ${BUILD_VERSION}" --prerelease
      RC=$?
    else
      gh release create "v${BUILD_VERSION}" --title "v${BUILD_VERSION}" --notes "Release for version ${BUILD_VERSION}"
      RC=$?
    fi

    if [ ${RC} -ne 0 ]; then
      echo "ERROR: Failed to create release v${BUILD_VERSION}" >&2
      return 1
    fi
  fi
  return 0
}

# If the packages can be released, upload them
upload_github_packages() {
  if can_release; then
    echo "Uploading ${DIST_ZIP} to v${BUILD_VERSION}"
    gh release upload "v${BUILD_VERSION}" "${DIST_ZIP}" "${SHADOW_JAR}" --clobber
    RC=$?
    if [ ${RC} -ne 0 ]; then
      echo "ERROR: Failed to upload asset to existing release v${BUILD_VERSION}" >&2
      return 1
    fi
    echo "Asset uploaded to existing snapshot release v${BUILD_VERSION}."
    return 0
  else
    return 1
  fi
}

do_release() {
  if can_release; then
    create_github_release && \
      ./gradlew :pi4j-plugin-grpc:publishToMavenCentral && \
      upload_github_packages
  fi
}

release_summary() {
  if [ ${GITHUB_STEP_SUMMARY} ]; then
    echo '# Release summary' >> $GITHUB_STEP_SUMMARY
    echo '```' >> $GITHUB_STEP_SUMMARY
    gh release view "v${BUILD_VERSION}" >> $GITHUB_STEP_SUMMARY
    echo '```' >> $GITHUB_STEP_SUMMARY
  else
    gh release view "v${BUILD_VERSION}"
  fi
}