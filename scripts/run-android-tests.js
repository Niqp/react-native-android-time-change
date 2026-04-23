const { spawnSync } = require("node:child_process");
const fs = require("node:fs");
const os = require("node:os");
const path = require("node:path");

const rootDir = path.resolve(__dirname, "..");
const gradleTask = ":react-native-android-time-change:testDebugUnitTest";
const wrapperName = process.platform === "win32" ? "gradlew.bat" : "gradlew";
const wrapperPath = path.join(rootDir, wrapperName);
const usesWrapper = fs.existsSync(wrapperPath);
const gradleExecutableName = process.platform === "win32" ? "gradle.bat" : "gradle";

const hasSystemGradle = () => {
  const check = process.platform === "win32"
    ? spawnSync("where.exe", ["gradle"], { stdio: "ignore" })
    : spawnSync("sh", ["-c", "command -v gradle >/dev/null 2>&1"], {
        stdio: "ignore",
      });

  return check.status === 0;
};

const compareVersions = (left, right) => {
  const leftParts = left.split(".").map(Number);
  const rightParts = right.split(".").map(Number);
  const length = Math.max(leftParts.length, rightParts.length);

  for (let index = 0; index < length; index += 1) {
    const diff = (leftParts[index] || 0) - (rightParts[index] || 0);
    if (diff !== 0) {
      return diff;
    }
  }

  return 0;
};

const findCachedGradle = () => {
  const distsDir = path.join(os.homedir(), ".gradle", "wrapper", "dists");
  if (!fs.existsSync(distsDir)) {
    return undefined;
  }

  const matches = [];
  const visit = (directory, depth = 0) => {
    if (depth > 5) {
      return;
    }

    for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
      const entryPath = path.join(directory, entry.name);
      if (entry.isFile() && entry.name === gradleExecutableName) {
        const version = entryPath.match(/gradle-(\d+(?:\.\d+)*)/)?.[1] || "0";
        matches.push({ path: entryPath, version });
      } else if (entry.isDirectory()) {
        visit(entryPath, depth + 1);
      }
    }
  };

  visit(distsDir);
  matches.sort((left, right) => compareVersions(right.version, left.version));

  return matches[0]?.path;
};

const systemGradleAvailable = !usesWrapper && hasSystemGradle();
const cachedGradle = !usesWrapper && !systemGradleAvailable
  ? findCachedGradle()
  : undefined;
const command = usesWrapper
  ? wrapperPath
  : systemGradleAvailable
    ? "gradle"
    : cachedGradle;

if (!command) {
  console.error(
    [
      "Unable to run Android unit tests: no Gradle wrapper, system Gradle, or cached Gradle distribution was found.",
      "Install Gradle or add a Gradle wrapper, then run: npm run test:android",
      `Underlying Gradle task: ${gradleTask}`,
    ].join("\n"),
  );
  process.exit(1);
}

const result = spawnSync(command, [gradleTask], {
  cwd: rootDir,
  stdio: "inherit",
  shell: process.platform === "win32" && (systemGradleAvailable || command.endsWith(".bat")),
});

if (result.error) {
  console.error(result.error.message);
  process.exit(1);
}

process.exit(result.status === null ? 1 : result.status);
