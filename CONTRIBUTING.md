# Contributing to buy-02

Thank you for your interest in contributing to the buy-02 E-Commerce Platform! This document provides guidelines and instructions for contributing.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Development Setup](#development-setup)
3. [Making Changes](#making-changes)
4. [Testing](#testing)
5. [Code Quality](#code-quality)
6. [Submitting Changes](#submitting-changes)
7. [Code Review Process](#code-review-process)

## Getting Started

- Fork the repository
- Clone your fork: `git clone git@github.com:your-username/buy-02.git`
- Add upstream: `git remote add upstream git@github.com:CLecart/buy-02.git`
- Create feature branch: `git checkout -b feature/your-feature`

## Development Setup

### Prerequisites

- Java 21
- Maven 3.9+
- Docker & Docker Compose
- Node.js 18+ (for frontend)

### Setup Steps

```bash
# Clone repository
git clone git@github.com:CLecart/buy-02.git
cd buy-02

# Setup development environment
bash scripts/setup-dev-env.sh

# Update .env with your configuration
cp .env.example .env
# Edit .env with your values

# Build project
mvn clean verify

# Start services
docker compose -f docker-compose.dev.yml up -d
```

## Making Changes

### Branch Naming

```
feature/<name>     # New feature
fix/<name>         # Bug fix
refactor/<name>    # Code refactor
docs/<name>        # Documentation
test/<name>        # Tests
chore/<name>       # Dependencies, config
```

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Example:**

```
feat(orders): add order creation endpoint

- Implement POST /api/orders
- Add request validation
- Add unit and integration tests

Closes #42
```

### Code Style

- **Java**: Follow Google Java Style Guide
- **Checkstyle**: Configured in `config/checkstyle/checkstyle.xml`
- **Spacing**: 2 spaces for Java, 2 spaces for YAML
- **Line Length**: 120 characters max
- **Naming**: camelCase for methods/variables, PascalCase for classes

### Documentation

- Add JavaDoc for public methods and classes
- Update README if adding new features
- Update API documentation (OpenAPI/Swagger)
- Add comments for complex logic

## Testing

### Unit Tests

- File naming: `*Test.java`
- Location: `src/test/java`
- Framework: JUnit 5
- Minimum coverage: 70%

```java
@SpringBootTest
class UserServiceTest {

    @Test
    void shouldReturnUserWhenFound() {
        // Arrange
        User user = new User("test");

        // Act
        User result = userService.findById(1L);

        // Assert
        assertNotNull(result);
    }
}
```

### Integration Tests

- File naming: `*IntegrationTest.java` or `*IT.java`
- Location: `src/test/java`
- Database: TestContainers
- Run: `mvn verify -Pintegration`

### Running Tests

```bash
# Unit tests only
mvn test

# Integration tests
mvn verify -Pintegration

# All tests with coverage
mvn clean verify -Pintegration jacoco:report

# Specific test class
mvn test -Dtest=UserServiceTest

# Specific test method
mvn test -Dtest=UserServiceTest#shouldReturnUserWhenFound
```

## Code Quality

### Checkstyle

```bash
mvn checkstyle:check
```

### SpotBugs

```bash
mvn spotbugs:check
```

### PMD

```bash
mvn pmd:check
```

### All Checks

```bash
mvn -Panalysis clean verify
```

### SonarQube Locally

```bash
# First, start SonarQube
docker run -d -p 9000:9000 sonarqube:latest

# Login (admin/admin) and create token

mvn sonar:sonar \
  -Dsonar.projectKey=buy-02 \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<your-token>
```

## Submitting Changes

### Before Submitting

1. Update your branch: `git fetch upstream && git rebase upstream/develop`
2. Run tests: `mvn clean verify -Pintegration`
3. Run analysis: `mvn -Panalysis clean verify`
4. Commit with meaningful messages
5. Push to your fork: `git push origin feature/your-feature`

### Create Pull Request

1. Go to GitHub and create PR
2. Fill out PR template completely
3. Link related issues: `Closes #42`
4. Request reviewers (2 for main, 1 for develop)
5. Ensure CI pipeline passes

### PR Checklist

- [ ] Branch is up-to-date with develop
- [ ] All tests pass locally
- [ ] Code coverage maintained/improved
- [ ] Code quality checks pass
- [ ] Documentation updated
- [ ] Commit messages follow conventions
- [ ] No hardcoded secrets or credentials
- [ ] PR description is clear and complete

## Code Review Process

### For Authors

1. Request review from team members
2. Respond to feedback promptly
3. Address all comments
4. Push new commits without force-pushing
5. Wait for approval before merging

### For Reviewers

1. Review code thoroughly
2. Check tests and coverage
3. Verify documentation
4. Suggest improvements
5. Approve or request changes

### Approval Criteria

- ✅ Code follows style guidelines
- ✅ Tests are included and pass
- ✅ Documentation is updated
- ✅ No security issues
- ✅ No hardcoded secrets
- ✅ Code quality improved or maintained
- ✅ No merge conflicts

## Common Issues

### Build Failures

```bash
# Clean and rebuild
mvn clean install

# Skip tests for troubleshooting (not recommended)
mvn clean install -DskipTests
```

### Test Failures

```bash
# Run failing test in isolation
mvn test -Dtest=YourTest

# Run with debugging
mvn test -Dtest=YourTest -X

# Check if Docker is running (for integration tests)
docker ps
```

### Code Quality Issues

```bash
# Run locally first
mvn -Panalysis clean verify

# Fix common issues
mvn spotbugs:gui  # GUI to review and fix bugs
```

## Getting Help

- Check existing documentation
- Review closed issues and PRs
- Ask in PR comments
- Contact maintainers

## Code of Conduct

Please note that this project is released with a [Code of Conduct](CODE_OF_CONDUCT.md). By participating in this project you agree to abide by its terms.

## License

By contributing to buy-02, you agree that your contributions will be licensed under the project's license.

---

**Thank you for contributing to buy-02! 🙌**
