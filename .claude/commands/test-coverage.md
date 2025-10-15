# Test Coverage Command

Analyze test coverage for changes in the current branch and identify untested code that needs tests. Follow this workflow:

## Phase 0: Setup Java Runtime

Ensure using the Java runtime from Android Studio:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

This is required for all Gradle commands (build, test, etc.) to work correctly.

## Phase 1: Identify Changed Files

Get the list of files changed in the current branch:

```bash
# Get current branch
git branch --show-current

# Get changed Kotlin files (exclude test files)
git diff --name-only main...HEAD | grep "\.kt$" | grep -v "Test\.kt$" | grep -v "/test/" | grep -v "/androidTest/"

# Store this as "changed production files"
```

If no production files changed, report: "No production code changes found in this branch."

## Phase 2: Map to Test Files

For each changed production file, identify corresponding test files:

```bash
# For each changed file, check if test file exists
# Example: app/src/main/java/com/.../Foo.kt
#       -> app/src/test/java/com/.../FooTest.kt
#       -> app/src/androidTest/java/com/.../FooTest.kt

# Check existence:
# ls app/src/test/java/path/to/FooTest.kt 2>/dev/null
# ls app/src/androidTest/java/path/to/FooTest.kt 2>/dev/null
```

Categorize files:
- **Files with tests**: Both production file and test file exist
- **Files without tests**: Production file exists, no test file found

## Phase 3: Analyze Test Coverage (Use Gemini CLI)

Use Gemini CLI to analyze test adequacy for changed files:

```bash
gemini --prompt "Analyze test coverage for these changed Kotlin files:

**Changed Production Files:**
[LIST OF CHANGED PRODUCTION FILES]

**Existing Test Files:**
[LIST OF CORRESPONDING TEST FILES FOUND]

**Missing Test Files:**
[LIST OF FILES WITH NO TESTS]

For files WITH tests, read both the production code and test code:
[INSERT CONTENT OF CHANGED PRODUCTION FILE]
[INSERT CONTENT OF CORRESPONDING TEST FILE]

Analyze and report:

1. **Untested Changes:**
   - Which new/modified functions/methods lack tests?
   - Which code paths are not covered?
   - Which edge cases are missing test coverage?

2. **Test Quality:**
   - Are existing tests comprehensive?
   - Do tests follow naming convention: \`methodName_stateUnderTest_expectedBehavior\`?
   - Any obvious missing test scenarios?

3. **Files Without Tests:**
   - Which files have NO tests at all?
   - What's the risk level (critical business logic vs simple data classes)?
   - Should these have unit tests, UI tests, or both?

For each gap, provide:
- File path and function/method name
- Risk level: HIGH (business logic), MEDIUM (utilities), LOW (simple data/UI)
- Suggested test scenarios
- Whether it needs unit test (app/src/test) or UI test (app/src/androidTest)

Format as structured markdown with actionable recommendations."
```

## Phase 4: Check Test Execution

Run existing tests to ensure they still pass (using Android Studio's Java runtime):

```bash
# Set Java home for Android Studio
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Run unit tests
./gradlew test

# Check if there are any UI test changes
git diff --name-only main...HEAD | grep -q "/androidTest/" && echo "UI tests modified" || echo "No UI test changes"
```

If tests fail, report failures prominently.

## Phase 5: Report Findings

Organize findings into clear sections:

### Files Without Any Tests (High Priority)
```
- path/to/File.kt (Risk: HIGH/MEDIUM/LOW)
  Reason: [Why tests are important for this file]
  Suggested tests:
    - Test scenario 1
    - Test scenario 2
```

### Files With Insufficient Test Coverage
```
- path/to/File.kt (has tests, but gaps exist)
  Untested functions:
    - functionName() at line X
      Suggested: Test with valid input, edge cases, error conditions
  Missing coverage:
    - Edge case: [describe]
    - Error handling: [describe]
```

### Test Quality Issues
```
- path/to/FileTest.kt
  Issues:
    - Test naming doesn't follow convention
    - Missing assertions
    - Not testing edge cases
```

### Summary Statistics
```
- Changed files: X
- Files with tests: Y (Z%)
- Files without tests: N
- High-risk untested code: M files
- Test execution: PASSED/FAILED
```

## Phase 6: Action Prompts

Based on findings, ask the user:

"I found [X] files with missing or insufficient tests. Would you like me to:

1. **Generate test scaffolding** for files without tests
   - Creates FooTest.kt with basic test structure
   - Includes test methods for each public function
   - Follows project conventions (MockK, Truth, JUnit4)

2. **Add specific missing tests** to existing test files
   - Add tests for untested functions
   - Add edge case tests

3. **Review recommendations only** - You'll add tests manually

4. **Skip for now** - Acknowledge technical debt"

## Phase 7: Generate Test Scaffolding (If Requested)

If user chooses option 1 or 2, generate tests following project conventions:

**Unit Test Template (app/src/test/java):**
```kotlin
package [package]

import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class FooTest {

    private lateinit var subject: Foo

    @Before
    fun setUp() {
        // Initialize test subject
    }

    @After
    fun tearDown() {
        // Cleanup
    }

    @Test
    fun `methodName_normalCase_returnsExpectedValue`() {
        // Arrange

        // Act

        // Assert
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `methodName_edgeCase_handlesCorrectly`() {
        // Test edge cases
    }

    @Test
    fun `methodName_errorCondition_throwsException`() {
        // Test error handling
    }
}
```

After generating, run tests using Android Studio's Java runtime:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew test
```

## Error Handling

Handle common scenarios:

1. **No changes in branch:**
   - "No changes found. Make some code changes first."

2. **All files already tested:**
   - "Great! All changed files have test coverage. Running tests to verify..."

3. **Tests failing:**
   - Prominently report failures
   - Suggest fixing existing tests before adding new ones

4. **Only UI changes:**
   - Note that UI testing requires device/emulator
   - Suggest manual testing approach if UI tests aren't feasible

## Execution Notes

- **CRITICAL: Always set JAVA_HOME** to Android Studio's runtime before running Gradle commands
- **Scope:** Only analyze files changed in current branch (vs main)
- **Use Gemini CLI for analysis** - Analyze code and identify gaps
- **Use Claude's tools for test generation** - Write/Edit for creating tests
- **Follow project conventions:**
  - MockK for mocking
  - Google Truth for assertions
  - JUnit 4
  - Test naming: `` `methodName_stateUnderTest_expectedBehavior` ``
- **Risk-based prioritization** - Highlight high-risk untested code
- **Run tests after generation** - Verify generated tests compile and pass
- **Reference project docs** - CLAUDE.md has testing guidelines

## Best Practices

- Prioritize testing business logic over simple data classes
- UI components may need UI tests (androidTest) not unit tests
- ViewModels should have comprehensive unit tests
- Audio processing and music theory logic is critical - needs thorough tests
- Edge cases matter: null inputs, boundary values, error conditions
- Integration tests for multi-component interactions
- Don't over-test simple getters/setters
