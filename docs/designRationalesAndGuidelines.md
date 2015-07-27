# Design Decisions and Guidelines

This document contains explanations for some of the design decisions made, as well as tips on how to safely extend the codebase.

It makes reference to many points first covered in [Architecture](architecture.md).

## Communication between `UI` and `Logic`

`UI` has a reference to an instance of `Logic`, which is passed deep into the `UI` hierarchy, and communicates by calling its methods. This is intuitive because the interface of `Logic` has a direct mapping to application-level actions.

When `Logic` has to notify the `UI` of some change that has occurred, however, it communicates via a Google Guava `EventBus`, sending an `Event` subclass. This way `Logic` can communicate with arbitary parts of the `UI` via event handlers registered there.

An alternative way of communication from `Logic` to `UI`, or `UI` to another part of `UI`, is via the UI singleton instance. This is primarily for triggering events, though, and should not be used for other purposes as it increases coupling.

## `Optional`

`Optional` is preferred to `null` for indicating the absence of a value.

A few benefits of using it are:

- Absence of `NullPointerException`s. `Optional` leverages Java's type system to maintain invariants on whether values are present. This is in contrast to `null`, which the type system cannot provide guarantees about.
- Much like assertions, `Optional` makes causes of failure explicit.
- It results in interfaces that are much more illustrative of what they do and how they should be used.
- `Optional` has a rich API for composition. Multiple functions returning `Optional` can be chained together, propagating the 'nullness' of a value in a type-safe way.

An article which summarises the plus points of `Optional` can be found [here](https://www.voxxed.com/blog/2015/05/why-even-use-java-8-optional/).

## Immutability and Functional Style

Much of the back end is written in functional style, favouring immutability. There are many benefits to this:

- Immutable objects are thread-safe by default. This greatly simplifies things in the presence of concurrency.
- Immutability makes the flow of data through code explicit. Pure functions are easier to reason about in isolation.
- As explicit return values are preferred to updating internal state, immutable interfaces are easier to test.
- Well-designed interfaces allow maximum composition of functionality. A good example of this is in the use of `CompletableFuture` in `Logic` and `RepoIO`, where asynchronous actions are strung together by chaining method calls.

General tips:

- Make as many fields immutable as possible. Mark fields as final by default. This rule should be followed unless it makes dealing with programs much more inconvenient, or if it increases code complexity more than it helps.
    + For example, updating a deeply-nested field immutably can be inconvenient if all containing classes up to that point are immutable.
- Prefer `forEach` over loops with explicit indices; off-by-one errors are impossible with the former.
- Many abstractions for reduction, grouping, mapping, and filtering are available with the new `Stream` class, and reusing those over potentially-buggy reimplementations with explicit loops is also preferred.

Sometimes immutability is inapplicable, for example if the UI is inherently stateful, or if performance is required. For those cases...

## Thread-safety

In general, immutable interfaces are trivially thread-safe. In the presence of state, however, thread-safety can be very tricky to reason about, due to the implicitness of the thread that a particular block of code executes in.

- Try to ensure by design that multiple threads *cannot* interfere. Keep the effects of threads as encapsulated as possible. If two threads only work on their own queues and do not modify the same state, there cannot be race conditions between them.
- Stopping threads in Java is *cooperative*. This means that it is not in general possible to stop a concurrent task *now* with any guarantee. That should be kept in mind when designing interactions between threads.
- When the effects of multiple threads *may* interfere, some kind of synchronisation policy *must* be considered.
    + Thread confinement is a simple option: where possible, confine state to a single thread, even if the statefulness might originate from multiple threads. The JavaFX UI thread is a great candidate for this with `Platform.runLater`.
    + In all other cases, ensure that explicit synchronisation is used.
    + Never assume that a class is thread-safe. The documentation on the class should be consulted to find out its synchronisation policy.
