---
description: Hand the current request to the orchestrator agent for decomposition + delegation
argument-hint: <task description>
---

Invoke the `orchestrator` agent with the following task. Brief it self-contained — it has not seen this conversation.

Task: $ARGUMENTS

Expectations:
- Decompose into 2–6 TaskCreate items.
- Delegate to specialist sub-agents in parallel where possible.
- Report a short summary back: what was done, what's left, what needs the user's input.
