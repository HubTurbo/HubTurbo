package util.events;

public interface EventHandler {
}

// @FunctionalInterface
// public interface EventHandler<T extends Event> {
// 	   @Subscribe public void handle(T eventData);
// }

// The previous version. Much more succinct and supported the use of lambda
// expressions, but does not play well with EventBus -- apparently a generics
// constraint doesn't constitute a super-type relationship, so with this
// implementation EventBus calls *every* EventHandler registered when one fires,
// treating them all as the same class.

// This would fail (presumably due to a ClassCastException, which would then be
// swallowed by EventBus...) and cause lots of errors to be logged. No damage done,
// however it's probably better to be on the safe side, repeat ourselves everywhere,
// but make the super-type relationship explicit by extending and not using generics.

// Now every *Event must have a corresponding *EventHandler and lambda expressions can't be used.
