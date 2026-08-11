#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BOOST_PATH="$ROOT_DIR/3party/boost"

cd "$ROOT_DIR"
git submodule sync --recursive
git submodule update --init --recursive

# Organic Maps expects a generated umbrella include tree for modular Boost.
if [ -d "$BOOST_PATH/libs/spirit/include/boost/spirit" ] && [ ! -f "$BOOST_PATH/libs/spirit/include/boost/spirit/include/qi.hpp" ]; then
  mkdir -p "$BOOST_PATH/libs/spirit/include/boost/spirit/include"
  printf '%s\n' '#include <boost/spirit/home/qi.hpp>' > "$BOOST_PATH/libs/spirit/include/boost/spirit/include/qi.hpp"
fi

rm -rf "$BOOST_PATH/boost"
mkdir -p "$BOOST_PATH/boost"
for include_boost_dir in "$BOOST_PATH"/libs/*/include/boost "$BOOST_PATH"/libs/numeric/*/include/boost; do
  [ -d "$include_boost_dir" ] || continue
  find "$include_boost_dir" -mindepth 1 -maxdepth 1 -exec sh -c '
    boost_path="$1"
    shift
    for entry do
      target="$0/boost/$(basename "$entry")"
      if [ -d "$entry" ]; then
        mkdir -p "$target"
        find "$entry" -mindepth 1 -maxdepth 1 -exec sh -c '\''
          boost_path="$1"
          target_dir="$2"
          shift 2
          for child do
            child_target="$target_dir/$(basename "$child")"
            [ -e "$child_target" ] || ln -s "../../${child#"$boost_path"/}" "$child_target"
          done
        '\'' sh "$boost_path" "$target" {} +
      elif [ ! -e "$target" ]; then
        ln -s "../${entry#"$0"/}" "$target"
      fi
    done
  ' "$BOOST_PATH" "$BOOST_PATH" {} +
done
