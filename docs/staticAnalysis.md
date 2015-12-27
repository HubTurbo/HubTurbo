
# Static Analysis Tools

We use a number of static analysis tools to ensure code quality.

## CheckStyle

CheckStyle analyses source code for style guide violations.

It is configured by enabling rules. Each rule comes with its own configuration options. Inline source code annotations are not needed.

Plugins are available for viewing CheckStyle violations in [Eclipse](http://eclipse-cs.sourceforge.net/#!/) and [IntelliJ](https://plugins.jetbrains.com/plugin/1065).

## FindBugs

FindBugs analyses bytecode for potential bugs.

Exceptions can be made for packages, classes, methods, and fields, using an expressive DSL to express conditions in which particular rules should and should not apply. There is no need for inline annotations in source code.

## PMD

PMD analyses source code for a large variety of problems, including potential bugs and syntactic issues.

We use the more [widely-applicable rulesets](../config/pmd/mainRuleset.xml) (full set [here](https://pmd.github.io/pmd-5.4.1/pmd-java/rules/index.html)). A [slightly-different set](../config/pmd/testRuleset.xml) is used for tests. In general, applicable rules and exceptions for tests and source code may differ: for example, empty catch blocks are acceptable in tests, but are generally a bad sign in source code.

The above rulesets list the enabled rules. False positives can be suppressed in a number of ways. They are all necessary as they have different levels of granularity.

- `// NOPMD` line comments. These disable PMD for specific lines.
- `@SuppressWarnings("PMD")` annotations. These disable rules at the class or method level.
- Exceptions in rulesets. This is used to turn off entire rules.

Suppressed warnings should generally come with an explanatory comment if the reasoning isn't obvious (to reviewers).
