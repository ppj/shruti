# Test Coverage Command

Analyze test coverage for changes in current branch (vs main). **CRITICAL: Set JAVA_HOME for all Gradle commands:**
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

## Phase 1: Identify Changes & Map Tests

```bash
git branch --show-current
git diff main...HEAD -- '*.kt' | grep -v "Test\.kt$" | grep -v "/test/" | grep -v "/androidTest/"  # Production diff
git diff --name-only main...HEAD | grep "\.kt$" | grep -v "Test\.kt$" | grep -v "/test/" | grep -v "/androidTest/"  # File names

# For each file, check test existence:
# ls app/src/test/java/.../FooTest.kt 2>/dev/null
# ls app/src/androidTest/java/.../FooTest.kt 2>/dev/null
```

Categorize: Files with tests vs without tests. If no production changes, report and exit.

## Phase 2: Analyze with Gemini

```bash
gemini --prompt "Analyze test coverage for code changes:

Changed code (diff): [INSERT DIFF]
Changed files: [LIST]
Existing tests: [LIST]
Missing tests: [LIST]

Focus only on added/modified code (not unchanged). Report:
1. Untested changes: functions/methods, code paths, edge cases
2. Test quality: comprehensive? naming (backtick methodName_state_behavior)? missing scenarios?
3. Files without tests: risk (HIGH=business logic, MEDIUM=utilities, LOW=data/UI), unit vs UI test needed?

For each gap: file:function, risk level, suggested scenarios, test type (unit/UI)."
```

## Phase 3: Run Tests & Report

```bash
./gradlew test  # Run unit tests
git diff --name-only main...HEAD | grep -q "/androidTest/" && echo "UI tests modified"
```

**Report structure:**
1. **Files Without Tests (High Priority):** path, risk, reason, suggested scenarios
2. **Insufficient Coverage:** path, untested functions (line #), missing edge cases/error handling
3. **Quality Issues:** naming, assertions, edge cases
4. **Summary:** Changed files, % with tests, high-risk untested count, test execution status

## Phase 4: Action & Generate (If Requested)

"Found [X] gaps. Options: (1) Generate test scaffolding, (2) Add specific tests, (3) Review only, (4) Skip?"

**Test template:** See CLAUDE.md for conventions (MockK, Truth, JUnit4, backtick naming: `methodName_state_behavior`). Generate unit tests in `app/src/test/java/`, UI tests in `app/src/androidTest/`.

After generation: `./gradlew test` to verify.

**Error handling:**
- No changes → "Make code changes first"
- All tested → "All files covered. Running tests..."
- Tests fail → Report prominently, fix before adding new
- Only UI changes → Note device/emulator needed

**Notes:** Focus on diff only (no scope creep). Gemini for analysis, Claude tools for generation. Prioritize business logic (ViewModels, audio, music theory). UI may need androidTest. Edge cases critical (null, boundaries, errors). Reference CLAUDE.md for guidelines.