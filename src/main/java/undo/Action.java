package undo;

public interface Action<T> {

    T act(T t);

    T undo(T t);

    @SuppressWarnings("unchecked")
    default Action invert() {
        Action that = this;
        return new Action<T>() {
            @Override
            public T act(T t) {
                return (T) that.undo(t);
            }

            @Override
            public T undo(T t) {
                return (T) that.act(t);
            }
        };
    }

}
