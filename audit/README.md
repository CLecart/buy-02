# Audit Checklist & Evidence

## 1. Code Source

- Repo GitHub: [link]
- Code in English, JavaDoc only, microservices structure
- Example: Wishlist.java, README.md in each service

## 2. CI/CD

- GitHub Actions: .github/workflows/frontend-ci.yml
- CI/CD logs (screenshots or links)
- No warnings/errors (wishlist.service.spec.ts: no error)

## 3. Tests

- Test files: /media-service/src/test/java/, /product-service/src/test/java/
- Passing test results (CI/CD logs or reports)

## 4. Documentation

- AUDIT_QUICKSTART.md
- CI-CD-GUIDE.md
- GITHUB-CONFIGURATION.md
- DATABASE-SCHEMA.md
- WISHLIST-SCHEMA.md

## 5. Process

- PR/merge/review on GitHub (screenshots or links)
- GITHUB-CONFIGURATION.md (process explanation)
- Gitea: mirror push only, no CI/CD, no PR

## 6. Code Quality

- sonar-project.properties
- checkstyle.xml
- pmd-ruleset.xml
- SonarQube, ESLint, Checkstyle, PMD reports (logs/screenshots)

## 7. Bonus: Multi-payment & Wishlist

- Code and tests in relevant services
- Documentation in README.md and WISHLIST-SCHEMA.md

---

Add screenshots, links, and reports as needed for the audit.
