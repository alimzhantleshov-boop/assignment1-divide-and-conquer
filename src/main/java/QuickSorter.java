import java.util.concurrent.ThreadLocalRandom;

/**
 * Randomized, in-place QuickSort.
 *  - The pivot index is chosen uniformly at random to avoid worst-case
 *    behaviour on already-sorted or adversarial inputs.
 *  - After partitioning, the smaller partition is handled by a recursive
 *    call and the larger partition is handled by looping (tail-iteration),
 *    which bounds the recursion (call-stack) depth to O(log n) even though
 *    the total work can still be Theta(n^2) in the worst case.
 *
 * Recurrence (expected case): T(n) = 2T(n/2) + Theta(n) => Theta(n log n)
 * (Master Theorem, case 2). Worst case (always the most unbalanced split
 * chosen): T(n) = T(n-1) + Theta(n) => Theta(n^2).
 */
public class QuickSorter {

    private static final int CUTOFF = 10;

    public static Metrics sort(int[] a) {
        Metrics m = new Metrics();
        quicksort(a, 0, a.length - 1, 0, m);
        return m;
    }

    private static void quicksort(int[] a, int lo, int hi, int depth, Metrics m) {
        while (lo < hi) {
            if (hi - lo + 1 <= CUTOFF) {
                insertionSort(a, lo, hi, m);
                return;
            }
            m.updateDepth(depth);
            m.recursiveCalls++;
            int p = partition(a, lo, hi, m);

            // Recurse into the smaller side, iterate over the larger side.
            if (p - lo < hi - p) {
                quicksort(a, lo, p - 1, depth + 1, m);
                lo = p + 1; // continue the loop on the (larger) right side
            } else {
                quicksort(a, p + 1, hi, depth + 1, m);
                hi = p - 1; // continue the loop on the (larger) left side
            }
        }
    }

    private static int partition(int[] a, int lo, int hi, Metrics m) {
        int pivotIndex = lo + ThreadLocalRandom.current().nextInt(hi - lo + 1);
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
