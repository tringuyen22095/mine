# AGENTS.md

## 1. Purpose

This file defines the workflow for AI coding tools operating in this repository.

These instructions are intended to be compatible with Codex, Gemini CLI, and other AI command-line tools that support repository instruction files.

The repository root is the directory containing this file.

---

## 2. Instruction priority

Follow instructions in this order:

1. System and platform instructions
2. Developer instructions
3. This `AGENTS.md` file
4. More-specific `AGENTS.md` files in child directories
5. Existing project conventions and documentation

If another `AGENTS.md` exists in a child directory, apply its instructions to files under that directory in addition to this file.

Do not ignore more-specific instructions unless they conflict with a higher-priority instruction.

---

## 3. General rules

Before changing files:

- Inspect the repository structure.
- Inspect relevant source files, tests, build files, and configuration.
- Inspect existing documentation and conventions.
- Inspect the `spec/` directory.
- Inspect existing task specifications.
- Check the current version-control status when possible.
- Avoid overwriting unrelated local changes.
- Reuse existing patterns before introducing new patterns.
- Make the smallest change that fully satisfies the requirement.
- Do not perform unrelated refactoring.
- Do not add dependencies without documenting the reason.
- Do not claim that a command succeeded unless it was actually executed successfully.
- Do not claim that a file was created or modified unless it was actually created or modified.
- Do not silently invent important requirements.
- Record assumptions explicitly.
- Keep generated files out of source control unless the project requires them.

---

## 4. Requirement-to-task rule

For every actionable requirement, create a task specification file before implementation.

An actionable requirement includes requests for:

- New functionality
- Bug fixes
- Configuration changes
- Build changes
- Dependency changes
- Refactoring
- API changes
- Database changes
- Documentation changes
- Testing changes
- Analysis
- Design
- Planning
- Architecture decisions
- Tooling or CI/CD changes

A task specification is required even when the request asks only for analysis or a plan and no implementation is requested.

A task specification is not required for a simple informational question that does not concern this repository. When uncertain, create a task specification.

All task specifications must be stored in:

~~~text
spec/
~~~

Each task must use this filename format:

~~~text
spec/Task-[id].md
~~~

---

## 5. Task ID rules

Task IDs start at `1` and increase automatically.

Before creating a task:

1. Create `spec/` if it does not exist.
2. Find all files matching:

~~~text
spec/Task-*.md
~~~

3. Extract numeric IDs from filenames matching:

~~~text
Task-[number].md
~~~

4. Ignore files without a valid numeric ID.
5. Use `1` if no valid task files exist.
6. Otherwise, use the highest existing numeric ID plus `1`.
7. Never overwrite an existing task file.
8. If the calculated filename already exists, increase the ID until an unused filename is found.

Example:

~~~text
spec/Task-1.md
spec/Task-2.md
spec/Task-8.md
~~~

The next task must be:

~~~text
spec/Task-9.md
~~~

A follow-up request that changes an existing task must normally create a new task specification.

Create a new task unless the request explicitly says to update a particular task file.

---

## 6. Required workflow

### Step 1: Inspect the repository

Inspect, when available:

- Repository root
- Source directories
- Test directories
- Build files
- Dependency files
- Configuration files
- CI/CD files
- Documentation
- Existing `AGENTS.md` files
- Existing `GEMINI.md` files
- Existing task specifications
- Current version-control status

Do not assume that a file exists. Confirm its location first.

### Step 2: Allocate a task ID

Determine the next available task ID using the rules in section 5.

### Step 3: Create the task specification

Create the task file before implementation.

The task file must preserve the original requirement exactly in the `Requirement` section.

Do not rewrite, summarize, correct, or normalize the original requirement in that section.

### Step 4: Analyze the requirement

Document:

- Current project behavior
- Existing implementation
- Relevant files
- Existing project conventions
- Technical constraints
- Dependencies
- Risks
- Compatibility concerns
- Alternatives
- Recommended approach
- Open questions
- Assumptions

### Step 5: Ask blocking questions

If an open question prevents a reliable implementation:

1. Create the task specification.
2. Record the question in the `Open Questions` section.
3. Ask the question.
4. Wait for clarification before making an irreversible change.

If the question does not block implementation:

1. Record it in `Open Questions`.
2. Document the assumption.
3. Continue with the safest reasonable implementation.

### Step 6: Implement

Use the task specification as the implementation source of truth.

Before editing:

- Confirm the affected files.
- Confirm the implementation approach.
- Confirm compatibility with existing code.
- Confirm that the change does not overwrite unrelated work.

### Step 7: Validate

Run appropriate validation commands.

Validation can include:

- Compilation
- Unit tests
- Integration tests
- Static analysis
- Formatting
- Linting
- Packaging
- Dependency checks
- Manual verification
- Documentation checks

Record executed commands and results in the task specification and final response.

### Step 8: Update task status

Use one of these statuses:

~~~text
Proposed
In Progress
Blocked
Complete
Cancelled
~~~

Only use `Complete` when the Definition of Done is satisfied.

---

## 7. Task specification template

Every task file must use the following structure:

~~~markdown
# Task-[id]: [Short descriptive title]

- **Status:** Proposed
- **Created:** [YYYY-MM-DD]
- **Related tasks:** [Task IDs or None]

## Requirement

> [Copy the original requirement exactly as provided.]

## Acceptance Criteria

### Requested criteria

- [ ] [Acceptance criterion explicitly requested in the original requirement.]

### Recommended criteria

- [?] [Additional acceptance criterion recommended by the AI.]

Only use the `- [?]` prefix for criteria recommended by the AI.

Do not mark a recommended criterion as required unless the requirement is updated to include it.

Acceptance criteria must be observable and testable.

Avoid vague criteria such as:

- Works correctly
- Good performance
- Clean implementation
- User-friendly

Prefer specific criteria such as:

- [ ] `mvn clean verify` completes successfully.
- [ ] The generated class contains the expected annotation.
- [ ] Invalid input produces the documented error.
- [ ] Existing tests continue to pass.

## Goal

[Describe what the requirement is intended to achieve.]

Include:

- Expected outcome
- Affected application, module, or component
- Expected behavior
- Relevant assumptions
- Boundaries of the requested change

## Open Questions

List every question that could affect:

- Design
- Implementation
- Behavior
- Compatibility
- Security
- Performance
- Testing
- Deployment
- Data migration
- Backward compatibility

- [Question]

If there are no open questions, write:

None.

## Assumptions

List assumptions used for analysis or implementation.

- [Assumption]

If there are no assumptions, write:

None.

## Analysis

Describe:

- Current project structure
- Existing relevant files
- Current behavior
- Existing patterns and conventions
- Technical constraints
- Dependencies
- Risks
- Security considerations
- Performance considerations
- Compatibility considerations
- Backward-compatibility concerns
- Alternatives considered
- Recommended approach
- Reason the recommended approach is preferred

Reference concrete files whenever possible.

## Proposed Pattern

Describe the design or implementation pattern to use, if applicable.

Include:

- Pattern name
- Where it will be applied
- Why it fits the requirement
- Example usage
- Rules future changes must follow

If no specific pattern is required, write:

No specific design pattern is required.

## Integration Plan

Provide a detailed, file-by-file plan.

Use this table:

| Action | File | Location | Details | Reason |
|---|---|---|---|---|
| Add/Modify/Delete | `path/to/file` | Class, method, XML element, or section | Exact change | Why the change is required |

The plan must identify, where applicable:

- Parent build files
- Child module build files
- Dependency management
- Dependencies
- Plugins
- Annotation processors
- Java or language version
- Profiles
- Module ordering
- Source files
- Test files
- Configuration files
- Environment variables
- Database migration files
- API contracts
- Documentation
- Build files
- CI/CD files
- Generated files
- Deployment files

Do not list only directories.

Use concrete file paths whenever possible.

If the exact path is unknown, identify the expected location and explain how it will be determined.

## Implementation Steps

List implementation steps in execution order.

1. [Step with affected file or component]
2. [Step with affected file or component]
3. [Step with affected file or component]

Every step should reference the relevant files or components.

## Testing Strategy

Describe the tests required for the change.

Include, where applicable:

- Unit tests
- Integration tests
- End-to-end tests
- Regression tests
- Negative tests
- Boundary tests
- Configuration tests
- Build tests
- Manual tests

Identify the test file for each test whenever possible.

## Validation Plan

List the commands and checks used to validate the implementation.

Include:

- Build commands
- Test commands
- Static analysis
- Formatting or linting
- Packaging
- Manual verification
- Expected results
- Failure cases

Example:

~~~bash
mvn clean verify
~~~

Do not list a command as executed unless it was actually executed.

## Definition of Done

The task is complete only when:

- [ ] All requested acceptance criteria pass.
- [ ] Recommended acceptance criteria are either completed or explicitly declined.
- [ ] Relevant tests pass.
- [ ] The project builds successfully.
- [ ] Validation commands have been recorded.
- [ ] Documentation is updated where required.
- [ ] No unintended files are changed.
- [ ] Dependencies and plugins are documented.
- [ ] The implementation matches this task specification.
- [ ] The task status is updated to `Complete`.

## Notes

[Additional decisions, constraints, references, or implementation notes.]
~~~

---

## 8. Acceptance criteria rules

Separate acceptance criteria into two groups:

### Requested criteria

These come directly from the original requirement.

Use:

~~~markdown
- [ ] Criterion
~~~

### Recommended criteria

These are additional criteria suggested by the AI.

Use:

~~~markdown
- [?] Criterion
~~~

Recommended criteria must not be presented as original requirements.

Each acceptance criterion must be:

- Specific
- Observable
- Testable
- Relevant to the task

When an acceptance criterion cannot be verified automatically, document the manual verification procedure.

---

## 9. Integration plan requirements

The Integration Plan must be detailed enough for another AI tool to implement the task without repeating the analysis.

For every affected file, explain:

- Add, modify, move, or delete action
- Exact file path
- Relevant class, method, package, XML element, configuration key, or section
- Expected change
- Reason for the change
- Dependencies on other changes
- Test or validation method

For Maven projects, explicitly identify:

- Parent `pom.xml` changes
- Child `pom.xml` changes
- `<dependencies>` changes
- `<dependencyManagement>` changes
- Maven plugin changes
- Annotation processors
- Java release level
- Maven profiles
- Module ordering
- Build lifecycle phase
- Build commands
- Test commands

For Java projects, explicitly identify:

- Package names
- New classes
- Modified classes
- Interfaces
- Public methods
- Constructors
- Configuration properties
- Exception behavior
- Test classes

For API changes, explicitly identify:

- Endpoint or operation
- HTTP method
- Request format
- Response format
- Status codes
- Validation rules
- Error format
- Backward-compatibility impact
- API documentation changes

For database changes, explicitly identify:

- Migration file
- Table or collection changes
- Index changes
- Data migration
- Rollback strategy
- Compatibility with existing data

---

## 10. Implementation rules

- Follow existing naming conventions.
- Follow existing formatting conventions.
- Follow existing package and module structure.
- Prefer existing libraries and utilities.
- Avoid introducing duplicate abstractions.
- Avoid broad refactoring when a focused change is sufficient.
- Preserve backward compatibility unless the requirement explicitly allows breaking changes.
- Add or update tests for changed behavior.
- Keep public APIs documented.
- Keep configuration changes documented.
- Use explicit dependency versions through the project’s existing dependency-management strategy.
- Do not commit secrets, credentials, tokens, private keys, or environment-specific sensitive data.
- Do not modify generated files unless the project explicitly stores generated files.
- Do not remove tests merely to make a build pass.
- Do not disable validation without documenting the reason.
- Do not suppress warnings without documenting the cause and impact.

---

## 11. Maven project rules

When working on a Maven project:

1. Identify the root `pom.xml`.
2. Identify the parent POM and aggregator POM.
3. Identify all modules in `<modules>`.
4. Check whether child POMs inherit from the intended parent.
5. Distinguish between:
   - `<dependencies>`
   - `<dependencyManagement>`
   - `<build><plugins>`
   - `<build><pluginManagement>`
6. Document dependency scopes.
7. Document annotation processors.
8. Document Maven lifecycle phases.
9. Use the project’s configured Java version.
10. Do not introduce a nonstandard lifecycle phase without documenting how it is provided.
11. Validate with the project’s appropriate Maven commands.

Typical validation commands include:

~~~bash
mvn validate
mvn compile
mvn test
mvn verify
mvn clean verify
mvn clean install
~~~

Run only commands appropriate for the project and requirement.

When adding a plugin or dependency, document:

- Group ID
- Artifact ID
- Version
- Scope
- Purpose
- Whether it is inherited
- Whether it is optional
- Whether it affects all modules
- Compatibility requirements

---

## 12. Testing rules

Every implementation must include an appropriate validation strategy.

When behavior changes:

- Add a test for the expected behavior.
- Add tests for invalid input when applicable.
- Add regression coverage for the original issue.
- Preserve existing tests.
- Run the narrowest relevant test first.
- Run the broader project validation afterward.

If tests cannot be added, document:

- Why tests cannot be added
- What alternative validation was performed
- What risk remains

---

## 13. Documentation rules

Update documentation when the change affects:

- Public behavior
- Build commands
- Configuration
- Environment variables
- API usage
- Installation
- Deployment
- Database setup
- Development workflow
- Generated code
- Module structure

Documentation changes must be listed in the Integration Plan.

---

## 14. Version-control rules

Before implementation, inspect the current version-control status when possible.

Do not:

- Revert unrelated changes
- Reset the repository
- Delete untracked files unrelated to the task
- Overwrite existing work
- Commit changes unless explicitly requested
- Change branches unless explicitly requested

At the end, report changed files accurately.

If unrelated changes already exist, preserve them and mention them when relevant.

---

## 15. Error handling and uncertainty

When an error occurs:

1. Preserve the original error message.
2. Identify the command or file that caused it.
3. Investigate the root cause.
4. Apply the smallest appropriate fix.
5. Re-run validation.
6. Record the result in the task specification.

When uncertain:

- State the uncertainty.
- Record it in `Open Questions`.
- Document assumptions.
- Ask for clarification when the decision is blocking or risky.
- Do not claim certainty without evidence.

---

## 16. Final response format

After processing a requirement, use this format:

~~~markdown
## Task

`spec/Task-[id].md`

## Status

[Proposed, In Progress, Blocked, Complete, or Cancelled]

## Summary

[Short description of the analysis or implementation.]

## Files Added

- `path/to/file`
- None

## Files Modified

- `path/to/file`
- None

## Files Deleted

- `path/to/file`
- None

## Validation

~~~text
[Commands executed and their results]
~~~

## Open Items

- [Remaining question, limitation, or None]
~~~

If only a specification was created, state clearly:

~~~text
The task specification was created. No implementation changes were made.
~~~

Do not claim that implementation was completed when only analysis or planning was performed.

---

## 17. Task status update rules

Update the task specification during implementation.

Use:

~~~markdown
- **Status:** Proposed
~~~

when analysis or planning exists but implementation has not started.

Use:

~~~markdown
- **Status:** In Progress
~~~

when implementation has started.

Use:

~~~markdown
- **Status:** Blocked
~~~

when implementation cannot continue because of a blocking issue or unanswered question.

Use:

~~~markdown
- **Status:** Complete
~~~

only when all applicable Definition of Done items pass.

Use:

~~~markdown
- **Status:** Cancelled
~~~

when the task is intentionally abandoned.

Add implementation notes, validation results, and final decisions to the task file before marking it complete.

---

## 18. Completion checklist

Before declaring a task complete, verify:

- [ ] A task file exists under `spec/`.
- [ ] The task ID is unique and sequential.
- [ ] The original requirement is preserved exactly.
- [ ] Requested acceptance criteria are documented.
- [ ] Recommended criteria use the `- [?]` prefix.
- [ ] The goal is documented.
- [ ] Open questions are documented.
- [ ] Assumptions are documented.
- [ ] Analysis is complete.
- [ ] The proposed pattern is documented, if applicable.
- [ ] The Integration Plan identifies affected files.
- [ ] Implementation steps are documented.
- [ ] Tests or validation are documented.
- [ ] Relevant commands were executed.
- [ ] Results are recorded accurately.
- [ ] No unrelated files were changed.
- [ ] The task status is correct.
- [ ] The final response lists changed files accurately.