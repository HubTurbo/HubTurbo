# Filtering Issues

An essential part of navigating GitHub with HubTurbo is having multiple panels, all filled with exactly the issues you're interested in. Controlling what shows up in panels is done by writing a filter which precisely expresses what it should contain.

<img src="images/gettingStarted/panelExplanation.png" width="600">

Don't worry, there isn't much syntax to learn!

## Contents

- [Basics](#basics)
- [Examples](#examples)
- [Key Differences](#key-differences)
- [Operators](#operators)
- [Application](#application)
- [Qualifiers](#qualifiers)
- [Incompatibilities](#incompatibilities)

## Basics

Filters are compatible with a subset of GitHub's search syntax. Their documentation is a great reference for the details:

- [Searching Issues](https://help.github.com/articles/searching-issues/)
- [Search Syntax](https://help.github.com/articles/search-syntax/)
- [Examples](http://zachholman.com/posts/searching-github-issues/)

To very quickly summarize the key points in our own words:

- Filters contain **keywords**, **qualifiers**, and **meta-qualifiers**.
    + A **keyword** is a search term which matches issue text. The filter `cats dogs` will pick issues containing BOTH the words `cats` and `dogs` in any order.
    + A **qualifier** is a search term which matches metadata instead of text. `label:red` will match issues with the label containing `red`.
    + A **meta-qualifier** changes the semantics of keywords or qualifiers. `in:title meow` will pick issues with the text `meow` in their titles only. `repo:hubturbo/hubturbo` will search in HubTurbo's repository, loading it if it's not already loaded.
- All of the above can be freely intermixed and combined with operators.

## Examples

The filter "all issues assigned to John that aren't closed and are due in milestones v0.1 or v0.2" may be expressed as:

```
assignee:john -state:closed (milestone:v0.1 OR milestone:v0.2)
```

"Open issues updated in the last day" may be expressed as:

```
is:open updated:<24
```

Viewing recently-updated, closed issues across multiple repositories is also easy:

```
(repo:hubturbo/hubturbo | repo:teammates/repo) updated:2 ~is:open is:issue
```

The repository qualifiers need to be parenthesised because leaving out an operator implicitly inserts an AND there, and AND has a higher precedence than OR.

All this syntax is explained below!

## Key Differences

HubTurbo extends GitHub's search syntax in a number of ways.

- Boolean operators are supported for all qualifiers. `assignee:alice | assignee:bob` will show issues which match either qualifier in a panel.
- Parentheses can be used for grouping. `(assignee:alice || assignee:bob) && state:open` will show open issues assigned to either Alice or Bob.
- Quotation marks can additionally be used to specify search keywords containing spaces. For example, `"test case"` will match issues containing the string `test case`, space included.
- Additional qualifiers are available, for example `has` and `updated`.

A number of GitHub's qualifiers are not yet supported or [inapplicable](#incompatibilities).

## Operators

Logical operators (AND, OR, NOT) may be used to combine qualifiers. This can be used to compose complex expressions. If an operator is left out between two qualifiers, it's taken to be AND.

Operators may be written in any of the following forms:

- AND: `AND` `&&` `&`
- OR: `OR` `||` `|`
- NOT: `NOT` `!` `~` `-`

As in C-like languages, NOT is prefix, AND and OR are infix and left-associative, and precedence goes: NOT > AND > OR.
Note that operators are case sensitive: `AND` is a valid operator, but `and` is not.

<!-- To be enabled later

## Application

Predicates are useful for specifying the exact subset of labels to show in a panel. This admits a useful secondary function -- dragging an issue onto a panel will cause HubTurbo to automatically apply the attributes required to make it show up in that panel! In other words, the issue will be modified such that it will be matched by the filter of the target panel.

This will not work for ambiguous expressions (containing OR or NOT operators) and expressions containing predicates for which this does not make sense (`title`, `id`, `in`, `has`, etc.).
-->

## Qualifiers

- [`id`](#id)
- [`keyword`](#keyword)
- [`title`](#title)
- [`body`](#body)
- [`milestone`](#milestone)
- [`label`](#label)
- [`assignee`](#assignee)
- [`author`](#author)
- [`involves`](#involves)
- [`state`](#state)
- [`has`](#has)
- [`no`](#no)
- [`in`](#in)
- [`type`](#type)
- [`is`](#is)
- [`created`](#created)
- [`updated`](#updated)
- [`repo`](#repo)
- [`sort`](#sort)

#### Formats

- Dates are written as `YYYY-MM-DD`.
- Date ranges are written using a relational operator (e.g. `>=2014-1-1`) or as a range (e.g. `2014-1-1 .. 2014-2-1`).
- Numbers are assumed to be integers.
- Number ranges are written using a relational operator (.e.g `>5`, `<=10`).
- Repo ids are written as `owner/name`
- Sorting keys are written as a comma-separated list of possibly-negated keys. For example, `repo, ~updated, -comments`. See `sort` for more information.

### id

*Expects a number or number range*

Matches the issue with the given id number, or issues with ids in the given range.

### keyword

*Expects a string*

Matches all issues with text containing the given string. Same as not specifying a qualifier.

### title

*Expects a string*

Matches all issues with a title containing the given string.

### body

*Expects a string*

Matches all issues with a body (or description) containing the given string.

### milestone

*Expects a string*

Matches all issues associated with any milestones whose names contain the given string.

### label

*Expects a string in the form `name`, `group.name`, or `group.`*

Matches all issues with the given label. A group name may be used to qualify the label, in case labels of the same name appear in different groups. If only a group name is provided, matches issues containing any label in the given group.

### assignee

*Expects a string*

Matches all issues assigned to the given person, identified by the given alias, GitHub username, or real name, in that priority.

### author

*Expects a string*

Matches all issues created by the given person, identified by the given alias, GitHub username, or real name, in that priority.

### involves

*Expects a string*

Matches all issues involving (assigned to or created by) the given person, identified by the given alias, GitHub username, or real name, in that priority.

### state

*Expects one of `open` or `closed`*

Matches all issues of the given state.

### has

*Expects one of `label`, `milestone`, `assignee`

Matches all issues possessing the given attribute.

### no

*Expects one of `label`, `milestone`, `assignee`

The negation of `has`. Matches all issues without the given attribute.

### in

*Expects one of `title` or `body`*

Meta-qualifier. Changes the semantics of `keyword` so it only searches in either the title or body.

### type

*Expects one of `issue` or `pr`*

Matches issues of a given issue type. Pull requests are loosely considered issues in that the same operations work on both; this predicate allows users to distinguish them.

### is

*Expects one of `open`, `closed`, `pr`, `issue`, `merged`, `unmerged`, `read`, `unread`*

Matches issues which are either open or closed, a pull request or an issue, depending on their merged status if they are pull requests, or read or unread. Is partially an alias for `state` and `type`.

### created

*Expects a date or date range*

Matches issues which were created on a given date, or within a given date range.

### updated

*Expects a number or  number range*

Matches issues which were updated in the given number of hours. For example, `updated:<24` would match issues updated in the last day. If a number `n` is given, it is implicitly translated to `<n`.

### repo

*Expects a repo id*

Matches issues of the given repository. If omitted, will match issues of the default repository instead (not all repositories).

### sort

*Expects a comma-separated list of sorting keys. For example, `repo, ~updated, -comments`. Any negation operator can be used to invert them. Keys can be any of `comments`, `repo`, `updated`, `date` (an alias for `updated`), `id`, or a label group. Label groups can be disambiguated by appending a `.`.*

Sorts a repository by the list of keys, going from left to right. Negated keys will reverse the ordering that they describe.

## Incompatibilities

HubTurbo's filter system is incompatible with GitHub's in a number of ways.

- GitHub's filter system is site-wide and so contains qualifiers for matching repositories, language, etc. HubTurbo's filters are for local use only; while `repo` applies, most other GitHub qualifiers do not:
    + `language`
    + `is:public|private` (repository)
    + `team`
- A number of qualifiers are not implemented because they would require as-yet unimplemented features in HubTurbo.
    + `in:comment`
    + `closed:[date range]`
    + `mentions:[string]`
    + `commenter:[string]`