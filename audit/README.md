# Audit Checklist & Evidence

## 1. Code Source

- Repo GitHub: https://github.com/CLecart/buy-02
- Code in English, JavaDoc style maintained for backend classes
- Microservices structure validated (`user-service`, `product-service`, `media-service`, `order-service`, `shared-lib`)

## 2. CI/CD

- GitHub Actions workflow: `.github/workflows/frontend-ci.yml`
- GitHub Actions workflow: `.github/workflows/backend-strict-gate.yml`
- Jenkins pipeline: `Jenkinsfile`
- Latest merged PR used for compliance uplift: https://github.com/CLecart/buy-02/pull/39

## 3. Tests

- Backend tests present across services (`src/test/java` in each Java module)
- Shared-lib coverage gate enforced at verify (`shared-lib/pom.xml`, JaCoCo LINE >= 80%)
- Full Maven verify passes on main branch

## 4. Documentation

- AUDIT_QUICKSTART.md
- CI-CD-GUIDE.md
- GITHUB-CONFIGURATION.md
- DATABASE-SCHEMA.md
- WISHLIST-SCHEMA.md

## 5. Process

- PR/merge/review process validated on GitHub (see PR #39)
- Process explanation in `docs/GITHUB-CONFIGURATION.md`
- Gitea (`origin`) used as mirror; GitHub used for PR and CI checks

## 6. Code Quality

- Config files present: `sonar-project.properties`, `config/checkstyle/checkstyle.xml`, `config/pmd/pmd-ruleset.xml`
- Maven `verify` passes with static analysis configuration in parent `pom.xml`
- JaCoCo threshold enforced for shared-lib to prevent coverage regression

## 7. Bonus: Multi-payment & Wishlist

- Wishlist: backend service/repository/controller + frontend service/page + tests
- Multi-payment: payment enums and order payment flow implemented/documented

---

## Evidence Pack (recommended attachments)

- CI run links (GitHub Actions + Jenkins)
- PR link(s) with review comments and merge status
- Maven verify output summary
- JaCoCo report snapshots (`shared-lib/target/site/jacoco`)
- Optional API smoke-test artifacts (`out/smoke`)
