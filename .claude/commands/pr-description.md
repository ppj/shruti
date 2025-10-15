# PR Description Command

Create or update a Pull Request description by analyzing commits and changes in the current branch. Follow this workflow:

## Phase 1: Gather Branch Information

Collect information about the current branch and its base:

```bash
# Get current branch name
git branch --show-current

# Check if PR already exists
gh pr view --json number,title,body,baseRefName || echo "No PR found"

# If no PR exists, determine base branch (usually 'main')
# Get list of commits in current branch vs base
git log main..HEAD --oneline

# Get all changed files with diff stats
git diff main...HEAD --stat

# Get detailed diff of changes
git diff main...HEAD
```

Store this information:
- Current branch name
- Base branch name (default: main)
- Whether PR exists
- List of commits with messages
- Changed files
- Diff content

## Phase 2: Analyze Changes (Use Gemini CLI)

Use Gemini CLI to analyze the branch changes and generate a comprehensive PR description:

```bash
gemini --prompt "Analyze the following git changes and create a comprehensive Pull Request description:

**Current Branch:** [INSERT BRANCH NAME]
**Base Branch:** [INSERT BASE BRANCH NAME]

**Commits:**
[INSERT git log OUTPUT]

**Changed Files:**
[INSERT git diff --stat OUTPUT]

**Detailed Changes:**
[INSERT git diff OUTPUT - include key sections]

Generate a PR description with this structure:

## Summary
[2-3 sentences explaining what this PR does and why]

## Changes
[Bulleted list of key changes, organized by area/component]

## Technical Details
[Any important implementation details, architectural decisions, or technical notes]

## Testing
[What testing was done or should be done]

## Related Issues
[Look for issue references in commit messages like #123, or note 'None']

## Checklist
- [ ] Code follows project conventions
- [ ] Tests added/updated
- [ ] Documentation updated if needed
- [ ] Tested on physical device (if applicable)

Use clear, concise language. Focus on WHAT changed and WHY, not HOW (the code shows that).
Make it easy for reviewers to understand the purpose and impact of these changes."
```

## Phase 3: Create or Update PR

Based on whether a PR exists:

### If NO PR exists:

```bash
gh pr create \
  --title "[GENERATE CONCISE TITLE FROM CHANGES]" \
  --body "$(cat <<'EOF'
[INSERT GENERATED PR DESCRIPTION]

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)" \
  --base main
```

### If PR exists:

```bash
gh pr edit [PR_NUMBER] --body "$(cat <<'EOF'
[INSERT GENERATED PR DESCRIPTION]

🤖 Updated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

## Phase 4: Confirm and Display

After creating/updating:

```bash
# Display the PR
gh pr view --web
```

Show the user:
- PR URL
- PR title
- Brief summary of what was included in the description

## Error Handling

Handle common scenarios:

1. **No commits in branch:**
   - Message: "No commits found compared to main. Make some commits first."

2. **Not on a feature branch:**
   - If on `main`, ask user which branch to analyze

3. **GitHub CLI not authenticated:**
   - Message: "Please authenticate with GitHub: `gh auth login`"

4. **No base branch found:**
   - Default to `main`, but ask user to confirm

## Execution Notes

- **Use Gemini CLI for analysis** - Generate PR description from commits and diffs
- **Use `gh` CLI for PR operations** - Create or update via GitHub CLI
- **Smart title generation** - Extract intent from commits to create clear PR title
- **Include context** - Reference commits, issues, and technical decisions
- **Reviewer-friendly** - Make it easy to understand what changed and why
- **Test checklist** - Include relevant testing checkboxes
- **Claude Code attribution** - Add generation note at bottom

## Best Practices

- Keep summary concise (2-3 sentences max)
- Group changes by component/area for clarity
- Highlight breaking changes prominently
- Reference related issues/PRs
- Note testing approach (unit tests, manual testing, etc.)
- If PR updates existing description, preserve any manual notes/context added by humans