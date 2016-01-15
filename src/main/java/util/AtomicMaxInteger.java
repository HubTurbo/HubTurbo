package util;

/**
 * An atomic integer that records the maximum value it has seen
 */
public class AtomicMaxInteger {

    private int value;
    private int max = 0;

    public AtomicMaxInteger(int start) {
        value = start;
    }

    public synchronized void increment() {
        max = Math.max(max, ++value);
    }

    public synchronized void decrement() {
        --value;
    }

    public synchronized int getMax() {
        return max;
    }

    public synchronized int get() {
        return value;
    }
}
