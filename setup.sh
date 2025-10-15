#!/bin/sh

echo "Setting up git hooks..."

# Create symbolic links to the git hooks
ln -s -f ../../scripts/git-hooks/pre-commit .git/hooks/pre-commit
ln -s -f ../../scripts/git-hooks/pre-push .git/hooks/pre-push

echo "Git hooks set up successfully."

echo "Project setup complete."
