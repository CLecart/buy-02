FRONTEND: Top-level await build warning

## Summary

When building the Angular frontend with `src/main.ts` using top-level await, the build emits the following warning:

"The generated code contains 'async/await' because this module is using \"topLevelAwait\". However, your target environment does not appear to support 'async/await'."

## Why this matters

- Our CI enforces a strict "zero warnings" policy (fails on any build warning).
- This specific message is produced by the Angular toolchain during bundling and does not indicate a functional runtime error on supported modern browsers or Node 20.

## Temporary mitigation

- CI excludes this exact warning from causing job failure. This is a deliberate, documented temporary exception. See `.github/workflows/frontend-ci.yml` for the implementation.

## Next steps (owner: frontend)

1. Investigate which part of the toolchain emits that warning (Babel/terser/Angular builder) and whether it can be eliminated by configuration (preset-env targets, baseline-browser-mapping, or webpack/babel config).
2. If configuration cannot remove the message, open an upstream issue with the responsible tool and track resolution here.
3. When the toolchain is updated or the warning no longer appears, remove the CI exception and delete this file.

## Notes

- This is NOT a suppression of a Sonar rule. We keep Sonar rules active and the frontend code uses top-level await to satisfy Sonar S7785.
- All changes and decisions are recorded in this file and will be referenced in the PR description for transparency.
