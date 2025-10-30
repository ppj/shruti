# PR Description Command

Create or update PR description by analyzing commits in current branch.

## Phase 1: Gather Info & Determine Strategy

```bash
git branch --show-current  # Current branch
gh pr view --json number,title,body,baseRefName 2>/dev/null || echo "No PR"  # Check if PR exists
git log main..HEAD --format="%H %s"  # All commits (SHA + message)
git diff main...HEAD --stat  # Stats
git diff main...HEAD  # Full diff
```

**Strategy:**
- **No PR:** Create new from overall diff (commits are implementation details)
- **PR exists:** Compare SHAs in "## Commits" section vs current branch:
  - SHAs changed (rebase/amend) → Regenerate from scratch
  - Only new commits → Update with new changes only (`git diff <last-SHA>..HEAD`)
  - No new commits → Exit (up to date)

## Phase 2: Analyze with Gemini

**For NEW PR:**
```bash
gemini --prompt "Create PR description from git changes:
Branch: [NAME], Base: main
Files: [git diff --stat]
Diff: [git diff]
Commits: [git log] (reference only, focus on overall diff)

Structure:
## Summary: 1-2 sentences
## Changes: 5-7 bullets (one line each, group by area)
## Technical Details: Only if noteworthy architectural decisions (otherwise OMIT)
## Testing: One line (e.g., 'Unit tests added', 'Tested on Pixel 6')
## Related Issues: 'Fixes #123' or 'None'
## Commits: List all as '- <full-SHA>: <message>'
## Checklist: Code conventions, tests, docs, device testing

Be concise, scannable, focus on WHAT/WHY not HOW."
```

**For UPDATE (new commits):**
```bash
gemini --prompt "Update PR with new commits:
Existing: [PR BODY]
New commits: [LIST with SHA]
New diff: [git diff <last-SHA>..HEAD]

Keep Summary (update if fundamental change). APPEND to Changes (one-line bullets). Update Technical Details/Testing if needed. UPDATE Commits section (all commits). Keep Issues/Checklist. Mark new: '**Update:** <brief>'. Stay concise."
```

## Phase 3: Create/Update PR

```bash
# Create new:
gh pr create --title "[TITLE]" --body "$(cat <<'EOF'
[DESCRIPTION]
🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)" --base main

# Update existing:
gh pr edit [NUM] --body "$(cat <<'EOF'
[UPDATED DESCRIPTION]
🤖 Updated with [Claude Code](https://claude.com/claude-code)
EOF
)"

# Display:
gh pr view --web
```

**Error handling:**
- No commits → "Make commits first"
- On `main` → Ask which branch
- Not authenticated → `gh auth login`
- PR missing Commits section → Regenerate from scratch
- No new commits → Exit (no change needed)

**Notes:** Always include "## Commits" section (full SHA + message) for tracking rebases. Focus overall diff for new PRs. Detect rebases by SHA comparison. Keep descriptions scannable (short bullets, omit fluff).
