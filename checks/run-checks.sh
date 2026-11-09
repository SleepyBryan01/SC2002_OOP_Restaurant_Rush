#!/usr/bin/env bash
# Regression evidence for the stage report.
# For every stage tag: compile that tag's source, replay its two recorded runs, and run the rule
# checks of that stage AND every earlier stage (Stage1Checks also runs on stage-2 and stage-3).
# Usage (Git Bash / macOS / Linux, from the repository root):  bash checks/run-checks.sh
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

for n in 1 2 3; do
  tag="stage-$n"
  dir="$work/$tag"
  mkdir -p "$dir/out"
  git -C "$root" archive "$tag" src | tar -x -C "$dir"
  checks=()
  for k in $(seq 1 "$n"); do checks+=("$root/checks/src/restaurantrush/checks/Stage${k}Checks.java"); done
  javac --release 17 -d "$dir/out" $(find "$dir/src" -name "*.java") \
        "$root/checks/src/restaurantrush/checks/Check.java" "${checks[@]}"

  echo "=================== $tag ==================="
  for run in victory defeat; do
    result=$(java -cp "$dir/out" restaurantrush.Main "$root/runs/stage$n-$run.txt" \
             | grep -E '\[(x| )\]  (Paid|Revenue|Avg)' | sed -E 's/^\| \[.\]  //; s/ +\|$//; s/ {2,}/ /g' | paste -sd ';' -)
    echo "replay stage$n-$run: $result"
  done
  for k in $(seq 1 "$n"); do
    java -cp "$dir/out" "restaurantrush.checks.Stage${k}Checks" | tail -1 | sed "s/^/on $tag: /"
  done
done
