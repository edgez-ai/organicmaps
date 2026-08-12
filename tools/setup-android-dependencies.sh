#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BOOST_PATH="$ROOT_DIR/3party/boost"

# Keep this list aligned with 3party/CMakeLists.txt for PLATFORM_ANDROID. Desktop,
# generator, test, documentation, and iOS-only submodules intentionally stay uninitialized.
ANDROID_SUBMODULES=(
  3party/BLAKE3
  3party/Vulkan-Headers
  3party/boost
  3party/expat
  3party/fast_float
  3party/fast_obj
  3party/freetype/freetype
  3party/gflags
  3party/glaze
  3party/glm
  3party/harfbuzz/harfbuzz
  3party/icu/icu
  3party/just_gtfs
  3party/pugixml/pugixml
  3party/utfcpp
)

# These are the Boost libraries included directly by the Android native source.
# boostdep expands their transitive header dependencies, avoiding Boost's full
# recursive checkout (currently more than 170 modules and tools).
BOOST_ROOT_MODULES=(
  algorithm
  circular_buffer
  container
  container_hash
  date_time
  functional
  geometry
  integer
  iterator
  math
  polygon
  range
  serialization
  unordered
  utility
)

cd "$ROOT_DIR"
git submodule sync -- "${ANDROID_SUBMODULES[@]}"
git submodule update --init --depth 1 -- "${ANDROID_SUBMODULES[@]}"

cd "$BOOST_PATH"
git submodule sync -- tools/boostdep
git submodule update --init --depth 1 -- tools/boostdep
for module in "${BOOST_ROOT_MODULES[@]}"; do
  # Initialize the requested root before scanning its transitive headers.
  python3 tools/boostdep/depinst/depinst.py -u -g "--depth 1" "$module"
done

printf 'Android dependencies are ready. Initialized Boost modules: '
git submodule status | awk '$1 !~ /^-/ {count++} END {print count + 0}'
