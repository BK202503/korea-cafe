---
description: Run reviewer + tester in parallel on the current branch, then report verdict
---

Run the following two agents **in parallel** (single message, two Agent tool calls):

1. `reviewer` — review all pending changes on the current branch. Output the verdict + blocking issues.
2. `tester` — run the relevant test suite for the changed files. Output pass/fail summary.

After both return, synthesize one verdict for the user:
- **Ship**: reviewer clean, tests pass
- **Fix needed**: list the specific issues with `file:line`

Do not commit or push. The user decides.
