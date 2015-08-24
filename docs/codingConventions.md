
# Coding Conventions

We use a coding standard derived from [TEAMMATES'](https://docs.google.com/document/pub?id=1iAESIXM0zSxEa5OY7dFURam_SgLiSMhPQtU0drQagrs&embedded=true), which is a customised variant of Google's.

We've tweaked a few of the rules for our needs:

- Conventions we don't follow: Google copyright notice, named TODOs
- Import order matches Eclipse's default (no priority given to Google-specific package names)
- Such `__METHOD_NAMES__` are allowed
- Line length is increased to 120
- `if` and `else` blocks that are multi-line require braces. Single line `if` and `else` blocks **should** not have braces.

These changes are documented in our [CheckStyle configuration](../config/checkstyle/checkstyle.xml).
