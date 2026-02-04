# CI/CD & Code Quality Guide for buy-02

## Overview

The buy-02 project follows industry best practices for Continuous Integration/Continuous Deployment and code quality assurance. This guide explains the setup and workflows.

## Architecture

```
┌─────────────────┐
│   Git (GitHub)  │
│   Push → PR     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Jenkins CI   │
│ • Build         │
│ • Test          │
│ • Analyze       │
│ • Deploy        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    SonarQube    │
│ Code Quality    │
│ Security        │
└─────────────────┘
```

## Development Workflow

### 1. Local Setup

```bash
# Clone the repository
git clone git@github.com:CLecart/buy-02.git
cd buy-02

# Setup development environment
bash scripts/setup-dev-env.sh

# Create a feature branch
git checkout -b feature/your-feature-name
```

### 2. Git Hooks

**Pre-commit hook** runs:

- Checkstyle validation
- Code formatting checks

**Pre-push hook** runs:

- Unit tests
- Code analysis (PMD, SpotBugs, Checkstyle)

To bypass hooks (not recommended):

```bash
git commit --no-verify
git push --no-verify
```

### 3. Development & Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify -Pintegration

# Run all tests with code coverage
mvn clean verify -Pintegration jacoco:report

# Run static analysis
mvn -Panalysis clean verify

# Build for deployment
mvn clean package
```

### 4. Create Pull Request

- Push your branch to GitHub
- Open a PR against `develop` branch
- PR must follow template (.github/pull_request_template.md)
- Ensure CI/CD pipeline passes
- Request code reviews from team members

### 5. Code Review Checklist

Reviewers must verify:

- [ ] Code follows project style guidelines
- [ ] No hardcoded credentials or secrets
- [ ] Unit tests are included and pass
- [ ] Integration tests pass if applicable
- [ ] SonarQube quality gate passes
- [ ] Documentation is updated
- [ ] Database migrations (if any) are included

### 6. Merge & Deploy

- Once approved and CI passes, squash and merge to `develop`
- Jenkins automatically deploys to dev environment
- Monitor logs in Jenkins for any issues

## Jenkins Pipeline

The Jenkinsfile defines the automated pipeline:

### Stages

1. **Checkout** - Clone repository
2. **Build** - Compile Maven projects
3. **Unit Tests** - Run unit tests with JUnit 5
4. **Integration Tests** - Run integration tests with Testcontainers
5. **Code Analysis** - Run PMD, SpotBugs, Checkstyle
6. **SonarQube Analysis** - Upload to SonarQube
7. **Build Docker Images** - Create Docker images
8. **Push Docker Images** - Push to Docker registry
9. **Deploy to Dev** - Deploy using Docker Compose
10. **Smoke Tests** - Run basic health checks

### Triggers

- **Main branch**: Manual trigger only
- **Develop branch**: Automatic on push
- **Feature branches**: Automatic on PR creation

## SonarQube Integration

### Running SonarQube Analysis Locally

```bash
mvn sonar:sonar \
  -Dsonar.projectKey=buy-02 \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<token>
```

### Configuration

- **Project Key**: buy-02
- **Project Name**: buy-02 E-Commerce Platform
- **Quality Gates**: Default (see sonar-project.properties)
- **Coverage Threshold**: 70% for new code

### Key Metrics

- **Maintainability Rating**: A-E scale
- **Security Rating**: A-E scale
- **Reliability Rating**: A-E scale
- **Code Coverage**: % of code covered by tests
- **Duplication**: % of duplicated lines
- **Issues**: Critical, Major, Minor, Info

## Code Quality Rules

### Checkstyle (config/checkstyle/checkstyle.xml)

Enforces:

- Naming conventions
- Code structure
- Documentation requirements
- Import organization

### PMD (config/pmd/pmd-ruleset.xml)

Detects:

- Dead code
- Duplicate code
- Inefficient code
- Security vulnerabilities

### SpotBugs

Detects:

- Potential bugs
- Performance issues
- Thread safety issues
- Null pointer exceptions

## Testing Standards

### Unit Tests

- File naming: `*Test.java`
- Location: `src/test/java`
- Framework: JUnit 5
- Mocking: Mockito
- Coverage: Minimum 70%

### Integration Tests

- File naming: `*IntegrationTest.java` or `*IT.java`
- Location: `src/test/java`
- Database: TestContainers (MongoDB)
- Run with: `mvn verify -Pintegration`

### Test Example

```java
@SpringBootTest
@ExtendWith(SpringExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testFindUserById() {
        // Arrange
        User user = new User("test", "test@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        User found = userService.findById(1L);

        // Assert
        assertNotNull(found);
        assertEquals("test", found.getName());
    }
}
```

## Deployment

### Development Environment

```bash
# Deployed automatically on push to develop
# Monitor at Jenkins dashboard

# Manual deployment
docker compose -f docker-compose.dev.yml up -d --build
```

### Production Environment

Not yet configured. Set up when needed.

## Monitoring & Logs

### Jenkins

- URL: `http://jenkins.example.com`
- Logs: Available in Jenkins UI
- Build artifacts: Stored on Jenkins server

### SonarQube

- URL: `http://sonarqube.example.com`
- Reports: Generated after each analysis
- Trends: Tracked over time

### Application Logs

```bash
# View logs
docker compose -f docker-compose.dev.yml logs -f

# Specific service
docker compose -f docker-compose.dev.yml logs -f user-service
```

## Troubleshooting

### Build Failures

1. Check Jenkins console output
2. Run locally: `mvn clean verify -Pintegration`
3. Check for missing dependencies: `mvn dependency:resolve`
4. Clear Maven cache: `mvn clean`

### SonarQube Issues

1. Verify SonarQube token
2. Check sonar-project.properties
3. Run analysis with debug: `mvn sonar:sonar -X`

### Test Failures

1. Run locally: `mvn test`
2. Check Docker is running (for integration tests)
3. Review test logs: `mvn test -X`

### Code Quality Issues

1. Review SonarQube dashboard
2. Run locally: `mvn -Panalysis clean verify`
3. Fix issues and commit
4. Push to trigger new analysis

## Best Practices

1. **Commit Frequently** - Small, focused commits are easier to review
2. **Write Tests First** - Follow TDD principles
3. **Code Review** - Always get peer review before merging
4. **Document** - Add comments for complex logic
5. **Update Dependencies** - Keep frameworks and libraries current
6. **Monitor Quality** - Check SonarQube trends regularly
7. **Follow Conventions** - Stick to naming and style conventions
8. **Security First** - No hardcoded credentials, validate inputs

## References

- [Maven Documentation](https://maven.apache.org)
- [JUnit 5 Guide](https://junit.org/junit5/docs/current/user-guide/)
- [SonarQube Documentation](https://docs.sonarqube.org)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)

## Support

For questions or issues:

1. Check this guide first
2. Review project documentation
3. Contact the development team
