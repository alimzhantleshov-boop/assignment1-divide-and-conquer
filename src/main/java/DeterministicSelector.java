/**
 * Deterministic linear-time selection (Median-of-Medians, Blum-Floyd-Pratt-
 * Rivest-Tarjan 1973).
 *
 * Steps for select(a, k):
 *  1. Split a[lo..hi] into groups of 5, sort each group with insertion sort,
 *     and collect the Theta(n/5) group medians in place at the front.
 *  2. Recursively find the median-of-medians of those Theta(n/5) values -
 *     this is the pivot.
 *  3. Partition a[lo..hi] around the pivot (in place).
 *  4. Recurse only into the side that contains the k-th order statistic.
 *
 * Recurrence: T(n) <= T(n/5) + T(7n/10) + Theta(n).
 * Since 1/5 + 7/10 = 9/10 < 1, the Akra-Bazzi / substitution argument gives
 * T(n) = Theta(n): the two recursive calls together shrink by a constant
 * factor < 1 every level, so the Theta(n) partition cost dominates and the
 * total work is a geometric series that sums to Theta(n).
 */
public class DeterministicSelector {

    /** Returns the k-th smallest element (0-indexed) of a, using and mutating a copy internally is NOT done: a is reordered in place. */
    public static int select(int[] a, int k, Metrics m) {
        if (k < 0 || k >= a.length) {
            throw new IllegalArgumentException("k out of range");
        }
        return select(a, 0, a.length - 1, k, 0, m);
    }

    private static int select(int[] a, int lo, int hi, int k, int depth, Metrics m) {
        m.updateDepth(depth);
        if (lo == hi) return a[lo];

        m.recursiveCalls++;
        int pivotValue = medianOfMedians(a, lo, hi, depth, m);
        int pivotIndex = findIndex(a, lo, hi, pivotValue);
        int p = partition(a, lo, hi, pivotIndex, m);

        if (k == p) {
            return a[p];
        } else if (k < p) {
            return select(a, lo, p - 1, k, depth + 1, m);
        } else {
            return select(a, p + 1, hi, k, depth + 1, m);
        }
    }

    /** Groups a[lo..hi] into blocks of 5, sorts each block, moves medians to
     *  the front of the range, then recursively selects the median of those
     *  medians. Returns the median-of-medians VALUE. */
    private static int medianOfMedians(int[] a, int lo, int hi, int depth, Metrics m) {
        int n = hi - lo + 1;
        if (n <= 5) {
            insertionSort(a, lo, hi, m);
            return a[lo + (n - 1) / 2];
        }

        int numGroups = (n + 4) / 5;
        for (int i = 0; i < numGroups; i++) {
            int groupLo = lo + i * 5;
            int groupHi = Math.min(groupLo + 4, hi);
            insertionSort(a, groupLo, groupHi, m);
            int medianIdx = groupLo + (groupHi - groupLo) / 2;
            swap(a, lo + i, medianIdx, m);
        }

        m.recursiveCalls++;
        return select(a, lo, lo + numGroups - 1, lo + (numGroups - 1) / 2, depth + 1, m);
    }

    private static int findIndex(int[] a, int lo, int hi, int value) {
        for (int i = lo; i <= hi; i++) {
            if (a[i] == value) return i;
        }
        return lo; // unreachable for well-formed input
    }

    private static int partition(int[] a, int lo, int hi, int pivotIndex, Metrics m) {
        swap(a, pivotIndex, hi, m);
        int pivot = a[hi];
        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            m.comparisons++;
            if (a[j] < pivot) {
                i++;
                swap(a, i, j, m);
            }
        }
        swap(a, i + 1, hi, m);
        return i + 1;
    }

    private static void insertionSort(int[] a, int lo, int hi, Metrics m) {
        for (int i = lo + 1; i <= hi; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= lo) {
                m.comparisons++;
                if (a[j] > key) {
                    a[j + 1] = a[j];
                    m.swaps++;
                    j--;
                } else {
                    break;
                }
            }
            a[j + 1] = key;
        }
    }

    private static void swap(int[] a, int i, int j, Metrics m) {
        if (i == j) return;
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
        m.swaps++;
    }
}
