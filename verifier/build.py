import os
import re
import shutil
import subprocess
import sys

here = os.path.dirname(os.path.abspath(__file__))
root = os.environ.get("GAME_ROOT") or os.path.dirname(here)
build = os.path.join(here, "build")
marker = "=== FILE: "


def find(name):
    override = os.environ.get("GAME_MAIN" if name == "MainActivity.java" else "GAME_SETTINGS")
    if override:
        return override
    found = []
    for base, dirs, files in os.walk(root):
        dirs[:] = [d for d in dirs if d != ".git" and os.path.join(base, d) != here]
        if name in files:
            path = os.path.join(base, name)
            found.append((0 if "src/main/java" in path.replace(os.sep, "/") else 1, len(path), path))
    if not found:
        sys.exit("Could not find %s in the repository." % name)
    return sorted(found)[0][2]


def unpack(sources):
    text = open(os.path.join(here, "checker.txt"), encoding="utf-8").read()
    count = 0
    for chunk in text.split(marker)[1:]:
        path, body = chunk.split("\n", 1)
        target = os.path.join(sources, path.strip())
        os.makedirs(os.path.dirname(target), exist_ok=True)
        open(target, "w", encoding="utf-8").write(body)
        count += 1
    return count


def make_r(target, games):
    found = {}
    for path in games:
        text = open(path, encoding="utf-8").read()
        for kind, name in re.findall(r"(?<![\w.])R\.(\w+)\.(\w+)", text):
            found.setdefault(kind, set()).add(name)
    found.setdefault("drawable", set()).update(["settingsbutton"] + ["number_%d" % i for i in range(10)])
    found.setdefault("id", set()).add("game_container")
    lines = ["package com.flappybird.recreation;", "", "public final class R {"]
    for group, kind in enumerate(sorted(found)):
        lines.append("    public static final class %s {" % kind)
        for index, name in enumerate(sorted(found[kind])):
            lines.append("        public static final int %s = 0x7f%02x%04x;" % (name, group + 1, index + 1))
        lines.append("    }")
    lines.append("}")
    os.makedirs(os.path.dirname(target), exist_ok=True)
    open(target, "w", encoding="utf-8").write("\n".join(lines) + "\n")


def main():
    main_file = find("MainActivity.java")
    settings_file = find("SettingsActivity.java")
    print("Game code: " + main_file)
    print("Settings code: " + settings_file)
    shutil.rmtree(build, ignore_errors=True)
    sources = os.path.join(build, "src")
    classes = os.path.join(build, "classes")
    os.makedirs(classes)
    print("Checker files: %d" % unpack(sources))
    make_r(os.path.join(sources, "com", "flappybird", "recreation", "R.java"), [main_file, settings_file])
    files = [os.path.join(base, f) for base, _, names in os.walk(sources) for f in names if f.endswith(".java")]
    result = subprocess.run(["javac", "-nowarn", "-encoding", "UTF-8", "-d", classes] + files + [main_file, settings_file])
    if result.returncode != 0 or not os.path.exists(os.path.join(classes, "com", "flappybird", "recreation", "Verifier.class")):
        sys.exit("The checker did not build.")
    print("Checker built.")


main()
