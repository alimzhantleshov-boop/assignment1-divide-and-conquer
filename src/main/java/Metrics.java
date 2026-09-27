/**
 * Simple mutable container for the performance counters collected while an
 * algorithm runs. One Metrics instance is created per algorithm invocation.
 */
public class Metrics {
    public long comparisons = 0;
    public long swaps = 0;
    public long recursiveCalls = 0;
    public int maxDepth = 0;

    /** Record that the recursion has reached the given depth. */
    public void updateDepth(int depth) {
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }

    @Override
    public String toString() {
        return "comparisons=" + comparisons +
                ", swaps=" + swaps +
                ", recursiveCalls=" + recursiveCalls +
                ", maxDepth=" + maxDepth;
    }
}
