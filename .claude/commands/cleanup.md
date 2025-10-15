# Cleanup Command

Perform a comprehensive code cleanup analysis focusing on changes in the current branch. Follow this workflow:

## Phase 0: Identify Changed Files

First, get the list of files changed in the current branch:

```bash
# Check current branch
git branch --show-current

# Get files changed compared to main branch
git diff --name-only main...HEAD | grep "\.kt$"
```

If the current branch is `main` or no changes are found, ask the user if they want to analyze the entire codebase instead.

Store the list of changed files to use in subsequent phases.

## Phase 1: Analysis (Use Gemini CLI)

Use the Gemini CLI extensively for analysis to minimize token usage. Pass the list of changed files from Phase 0:

```bash
gemini --prompt "Analyze only these Kotlin files that were changed in the current branch:
[INSERT LIST OF CHANGED FILES HERE]

For each file, identify:

1. **Unused Code:**
   - Unused variables (private properties, local variables, parameters)
   - Unused functions (private/internal functions not called anywhere)
   - Unused imports
   - Dead code and unreachable branches

2. **Comment Quality Issues:**
   - Redundant comments that just restate code (e.g., '// Set name' above 'name = value')
   - Verbose comments that describe WHAT code does instead of WHY
   - Comments that could be replaced by better variable/function names
   - Identify opportunities for self-documenting code through:
     - Better naming (rename vague names like 'data', 'temp', 'value')
     - Extract method refactoring (complex logic with explanatory names)
     - Extract constant/enum (magic numbers/strings)
   - KEEP: Comments explaining complex algorithms, business logic, non-obvious decisions

3. **Code Quality Issues:**
   - Magic numbers and strings (should be named constants)
   - TODOs and FIXMEs
   - Commented-out code blocks
   - Non-idiomatic Kotlin (e.g., using Java patterns instead of Kotlin idioms)

Provide findings with:
- File path and line numbers
- Severity: SAFE_TO_REMOVE, NEEDS_REVIEW, or REFACTOR_SUGGESTION
- Brief explanation
- For comment issues, suggest specific refactoring to make code self-documenting

Format as structured markdown."
```

## Phase 2: Android Lint

Run Android's built-in linting on the changed files. You can run a full lint to catch cross-file issues:

```bash
./gradlew lint
```

Check the lint report (usually at `app/build/reports/lint-results.html`) and filter for issues in the changed files from Phase 0.

## Phase 3: Report Findings

Organize findings into categories:

### 1. Unused Code (Safe to Remove)
- List each unused import, variable, function with file:line

### 2. Comment Quality Issues
**Redundant/Verbose Comments to Remove:**
- List with file:line

**Refactoring Opportunities (Self-Documenting Code):**
- Suggest better names, extract methods, etc.

### 3. Other Code Quality Issues
- Magic numbers, TODOs, commented code, etc.

### 4. Android Lint Issues
- Summarize key findings from lint report

## Phase 4: Tool Recommendations

Check if ktlint or detekt are configured:

```bash
grep -E "(ktlint|detekt)" build.gradle.kts app/build.gradle.kts
```

If not found, recommend:
- **ktlint**: Automatic code formatting
- **detekt**: Static analysis with UnusedPrivateMember rule for automated unused code detection

## Phase 5: Action Plan

Present findings and ask user:

"I found [X] issues across these categories. Would you like me to:
1. Fix all safe-to-remove issues (unused imports, redundant comments)
2. Address specific categories only
3. Review recommendations but make no changes yet"

**IMPORTANT:**
- Do NOT make any changes until user confirms
- Use Edit tool for making changes (not Gemini CLI)
- After making changes, run tests: `./gradlew test`

## Execution Notes

- **Scope:** Only analyze files changed in current branch (vs main)
- **Use Gemini CLI for analysis** (heavy lifting, per CLAUDE.md)
- **Use Claude's built-in tools for operations** (Grep, Edit, Bash)
- **Non-destructive by default** - report first, act with permission
- Focus on making code more readable and maintainable
- Preserve meaningful comments that explain WHY, not WHAT
- If on `main` branch or no changes found, ask user if they want full codebase analysis
