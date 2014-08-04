# HubTurbo (working title)

## Getting Started

**Development environment**

- JDK 1.8
- Eclipse Kepler/Luna

## Filter Expressions

### Predicates

Filters are written as a series of predicates, in the form `name(content)`. Issues for which the entire filter expression returns true will be displayed.

The query "all issues assigned to John that aren't closed and are due in milestones v0.1 and v0.2" may be expressed as follows:

```
assignee(john) ~state(closed) (milestone(v0.1) or milestone(v0.2))
```

As shown, parentheses are also used for grouping.

Predicates may also be written in the form `name:content` if `content` contains no spaces. This allows a syntax resembling that of Google Code's filter language:

```
assignee:john -state:closed (milestone:v0.1 or milestone:v0.2)
```

### Operators

Logical operators (AND, OR, NOT) may be applied to any filter expression. This can be used to compose complex queries. If an operator is left out, AND is implicit.

Operators may be written in any of the following forms:

- AND: `and`, `&&`, `&`
- OR: `or`, `||`, `|`
- NOT: `~`, `!`, `-`

As in C-like languages, NOT is a prefix operator, AND and OR are infix and left-associative, and precedence goes: NOT > AND > OR.

### Valid Predicates

- `id` (`number`)
- `title`
- `milestone`
- `parent` (`number`)
- `label` (`name`, `group.name`, or `group.`)
- `assignee`
- `state/status` (`open`, `closed`)
- `has` (`label`, `milestone`, `assignee`, `parent`)

`number`s may be written with a leading `#`.
