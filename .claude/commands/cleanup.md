# Cleanup Command

Analyze code cleanup for changes in current branch only (vs main).

## Phase 1: Get Changes & Analyze

```bash
git branch --show-current  # Check branch
git diff main...HEAD -- '*.kt'  # Get actual diff
```

If on `main` or no changes, ask user if they want full codebase analysis.

**Use Gemini CLI for analysis:**
```bash
gemini --prompt "Analyze this code diff for cleanup issues:

[INSERT GIT DIFF]

Identify in changed code only:
1. Unused: imports, variables, functions, dead code
2. Comment issues: redundant (just restates code), verbose (describes WHAT not WHY), candidates for better naming/extract method
3. Quality: magic numbers/strings, TODOs, commented-out code, non-idiomatic Kotlin

For each: file:line, severity (SAFE_TO_REMOVE/NEEDS_REVIEW/REFACTOR_SUGGESTION), brief explanation.
Keep comments explaining algorithms/business logic/non-obvious decisions."
```

## Phase 2: Lint & Report

```bash
./gradlew lint  # Check app/build/reports/lint-results.html, filter to changed lines only
grep -E "(ktlint|detekt)" build.gradle.kts app/build.gradle.kts  # Check for tools
```

**Organize findings:**
1. **Unused Code (Safe to Remove):** List with file:line
2. **Comment Issues:** Redundant to remove, refactoring suggestions
3. **Quality Issues:** Magic numbers, TODOs, commented code
4. **Lint Issues:** Summarize key findings
5. **Tool Recommendations:** Suggest ktlint/detekt if not configured

## Phase 3: Action Plan

"Found [X] issues. Fix: (1) All safe-to-remove, (2) Specific categories, (3) Review only?"

**After user confirms:**
- Use Edit tool (not Gemini) for changes
- Run `./gradlew test` after changes

**Notes:** Focus on actual diff only (no scope creep). Gemini CLI for analysis, Claude tools for operations. Non-destructive by default.
