package undo.actions;

import javafx.util.Pair;

public interface Action<T> {

    String getDescription();

    T act(T t);

    T undo(T t);

    Pair<Action, Action> reconcile(Action a, Action b);

    boolean isNoOp();

    // Returns an inverse of the Action.
    @SuppressWarnings("unchecked")
    default Action invert() {
        Action that = this;
        return new Action<T>() {
            @Override
            public String getDescription() {
                return that.getDescription();
            }

            @Override
            public T act(T t) {
                return (T) that.undo(t);
            }

            @Override
            public T undo(T t) {
                return (T) that.act(t);
            }

            @Override
            public Pair<Action, Action> reconcile(Action a, Action b) {
                return that.reconcile(a, b);
            }

            @Override
            public boolean isNoOp() {
                return that.isNoOp();
            }
        };
    }

}
