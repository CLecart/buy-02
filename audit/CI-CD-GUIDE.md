# CI/CD Guide

- CI validation is managed by GitHub Actions on `push` and `pull_request` to `main`.
- Active workflows:
  - `Frontend CI` (`.github/workflows/frontend-ci.yml`)
  - `Backend Strict Gate` (`.github/workflows/backend-strict-gate.yml`)
- Backend strict command used in CI and locally:

```bash
mvn -Pintegration -Panalysis -DskipITs=false verify
```

- Local smoke verification script:

```bash
bash scripts/ci-smoke.sh
```

- Jenkins pipeline (`Jenkinsfile`) exists for environment-specific automation but is not the required PR gate.
- Code quality checks include Checkstyle, PMD, SpotBugs, and JaCoCo (with `shared-lib` coverage threshold enforced).

See README.md for evidence and logs.
