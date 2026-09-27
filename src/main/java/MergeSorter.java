/**
 * Top-down MergeSort with:
 *  - a single reusable auxiliary buffer (allocated once, not per call),
 *  - a small-input cutoff that switches to Insertion Sort,
 *  - a linear-time merge step.
 *
 * Complexity: Theta(n log n) time, Theta(n) extra space.
 * Recurrence: T(n) = 2T(n/2) + Theta(n)  =>  Theta(n log n) by the Master Theorem
 * (case 2: a=2, b=2, f(n)=Theta(n) = Theta(n^{log_b a})).
 */
public class MergeSorter {

    private static final int CUTOFF = 16;

    public static Metrics sort(int[] a) {
        Metrics metrics = new Metrics();
        if (a.length < 2) return metrics;
        int[] aux = new int[a.length]; // reusable buffer, allocated once
        sort(a, aux, 0, a.length - 1, 0, metrics);
        return metrics;
    }

    private static void sort(int[] a, int[] aux, int lo, int hi, int depth, Metrics m) {
        m.updateDepth(depth);
        if (hi - lo + 1 <= CUTOFF) {
            insertionSort(a, lo, hi, m);
            return;
        }
        m.recursiveCalls += 2;
        int mid = lo + (hi - lo) / 2;
        sort(a, aux, lo, mid, depth + 1, m);
        sort(a, aux, mid + 1, hi, depth + 1, m);
        merge(a, aux, lo, mid, hi, m);
    }

    private static void merge(int[] a, int[] aux, int lo, int mid, int hi, Metrics m) {
        System.arraycopy(a, lo, aux, lo, hi - lo + 1);
        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                a[k] = aux[j++];
            } else if (j > hi) {
                a[k] = aux[i++];
            } else {
                m.comparisons++;
                if (aux[j] < aux[i]) {
                    a[k] = aux[j++];
                } else {
                    a[k] = aux[i++];
                }
            }
        }
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
}
