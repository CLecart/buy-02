# GitHub Repository Configuration for buy-02

## Branch Protection Rules

### Main Branch (`main`)

**Status checks required:**

- Jenkins CI (all checks must pass)
- SonarQube Quality Gate
- Code Coverage (minimum 70%)

**Settings:**

- ✅ Require pull request reviews before merging (2 approvals)
- ✅ Dismiss stale pull request approvals when new commits are pushed
- ✅ Require status checks to pass before merging
- ✅ Require branches to be up to date before merging
- ✅ Require conversation resolution before merging
- ✅ Require signed commits
- ✅ Include administrators in restrictions

### Develop Branch (`develop`)

**Status checks required:**

- Jenkins CI (all checks must pass)
- SonarQube Quality Gate
- Code Coverage (minimum 70%)

**Settings:**

- ✅ Require pull request reviews before merging (1 approval)
- ✅ Dismiss stale pull request approvals when new commits are pushed
- ✅ Require status checks to pass before merging
- ✅ Require branches to be up to date before merging

## Pull Request Rules

### Naming Convention (Conventional Commits)

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**

- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation
- `style` - Style/formatting
- `refactor` - Code refactor
- `perf` - Performance improvement
- `test` - Test-related
- `chore` - Build/dependencies
- `ci` - CI/CD configuration

**Example:**

```
feat(orders): implement order creation endpoint

- Add POST /api/orders endpoint
- Implement order validation
- Add integration tests

Closes #123
```

## Collaboration Rules

### Code Review Process

1. **Create Feature Branch**

   ```bash
   git checkout -b feature/your-feature
   ```

2. **Commit & Push**

   ```bash
   git commit -m "feat(scope): description"
   git push origin feature/your-feature
   ```

3. **Create Pull Request**
   - Fill out PR template completely
   - Request 2 reviewers for main, 1 for develop
   - Ensure CI passes

4. **Code Review**
   - Reviewers check code quality, tests, documentation
   - Request changes or approve
   - Address feedback with new commits

5. **Merge**
   - Use "Squash and merge" to keep history clean
   - Delete feature branch after merge

### Reviewer Responsibilities

- [ ] Code quality and best practices
- [ ] Test coverage and test quality
- [ ] Security (no hardcoded secrets, input validation)
- [ ] Performance (no obvious bottlenecks)
- [ ] Documentation updates
- [ ] Database migrations (if applicable)

## CI/CD Integration

### Jenkins Integration

- Automatically triggered on PR creation
- Runs build, tests, and code analysis
- Reports results back to GitHub
- Blocks merge if checks fail

### SonarQube Integration

- Code quality analysis on every PR
- Quality Gate determines pass/fail
- Reports metrics to GitHub

## Secrets Management

### No Hardcoded Secrets

- ❌ Passwords, API keys, tokens in code
- ❌ Database credentials in configuration
- ❌ AWS keys, certificates in repository

**Instead:**

- Use environment variables
- Use GitHub Secrets for CI/CD
- Use .env files (not committed)
- Use vault/secret management systems

### GitHub Secrets (for CI/CD)

Set in Repository Settings → Secrets and Variables:

```
SONAR_HOST_URL          # SonarQube server URL
SONAR_LOGIN_TOKEN       # SonarQube auth token
DOCKER_REGISTRY_URL     # Docker registry URL
DOCKER_USERNAME         # Docker registry username
DOCKER_PASSWORD         # Docker registry password
```

## Issue Tracking

### Issue Labels

- `bug` - Bug reports
- `enhancement` - Feature requests
- `documentation` - Doc updates
- `good first issue` - For new contributors
- `help wanted` - Community contributions welcome
- `high priority` - Urgent
- `low priority` - Can wait
- `blocked` - Waiting on something else

### Issue Workflow

1. Create issue with label
2. Link to PR when created
3. Auto-close when PR merges
4. Track progress with milestone

## Contributing Guidelines

See [CONTRIBUTING.md](../CONTRIBUTING.md)

## Code of Conduct

See [CODE_OF_CONDUCT.md](../CODE_OF_CONDUCT.md)
