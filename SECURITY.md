# Security Policy

## Reporting

Report security issues privately to `hello@arifaqyl.me`.

Do not disclose publicly:
- GitHub personal access tokens
- repository metadata write scopes or active token details
- private repository names if they were processed locally

## Local Safety Rules

- use env vars for tokens: `GITHUB_TOKEN`, `GITHUB_PAT`, or `GH_TOKEN`
- do not hardcode credentials
- rotate any token immediately if it was exposed
