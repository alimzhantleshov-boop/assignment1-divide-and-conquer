import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * Generates test inputs of different sizes/types, times each algorithm with
 * System.nanoTime(), collects the Metrics produced by each algorithm, and
 * writes everything to results/results.csv.
 */
public class Experiment {

    private static final int TRIALS = 5;
    private static final int[] SORT_SIZES = {100, 500, 1000, 2000, 5000, 10000, 20000, 50000};
    private static final int[] CLOSEST_PAIR_SIZES = {100, 500, 1000, 2000, 5000, 10000, 20000};
    private static final String[] INPUT_TYPES = {"random", "sorted", "reverse", "duplicates"};

    public static void run(String csvPath) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(csvPath))) {
            out.println("algorithm,inputType,n,trial,timeNs,maxDepth,comparisons,swaps,recursiveCalls");

            for (String type : INPUT_TYPES) {
                for (int n : SORT_SIZES) {
                    for (int trial = 1; trial <= TRIALS; trial++) {
                        runMergeSort(out, type, n, trial);
                        runQuickSort(out, type, n, trial);
                        runSelect(out, type, n, trial);
                    }
                }
            }

            for (String type : new String[]{"random", "duplicates"}) {
                for (int n : CLOSEST_PAIR_SIZES) {
                    for (int trial = 1; trial <= TRIALS; trial++) {
                        runClosestPair(out, type, n, trial);
                    }
                }
            }
        }
        System.out.println("Results written to " + csvPath);
    }

    private static void runMergeSort(PrintWriter out, String type, int n, int trial) {
        int[] a = generateArray(n, type, trial);
        long start = System.nanoTime();
        Metrics m = MergeSorter.sort(a);
        long elapsed = System.nanoTime() - start;
        writeRow(out, "MergeSort", type, n, trial, elapsed, m);
    }

    private static void runQuickSort(PrintWriter out, String type, int n, int trial) {
        int[] a = generateArray(n, type, trial);
        long start = System.nanoTime();
        Metrics m = QuickSorter.sort(a);
        long elapsed = System.nanoTime() - start;
        writeRow(out, "QuickSort", type, n, trial, elapsed, m);
    }

    private static void runSelect(PrintWriter out, String type, int n, int trial) {
        int[] a = generateArray(n, type, trial);
        int k = a.length / 2; // median
        Metrics m = new Metrics();
        long start = System.nanoTime();
        DeterministicSelector.select(a, k, m);
        long elapsed = System.nanoTime() - start;
        writeRow(out, "DeterministicSelect", type, n, trial, elapsed, m);
    }

    private static void runClosestPair(PrintWriter out, String type, int n, int trial) {
        Point[] pts = generatePoints(n, type, trial);
        Metrics m = new Metrics();
        long start = System.nanoTime();
        ClosestPairSolver.solve(pts, m);
        long elapsed = System.nanoTime() - start;
        writeRow(out, "ClosestPair", type, n, trial, elapsed, m);
    }

    private static void writeRow(PrintWriter out, String algo, String type, int n, int trial, long elapsedNs, Metrics m) {
        out.printf("%s,%s,%d,%d,%d,%d,%d,%d,%d%n",
                algo, type, n, trial, elapsedNs, m.maxDepth, m.comparisons, m.swaps, m.recursiveCalls);
    }

    static int[] generateArray(int n, String type, int seed) {
        Random rnd = new Random(1000L * seed + n);
        int[] a = new int[n];
        switch (type) {
            case "random":
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt(1_000_000);
                break;
            case "sorted":
                for (int i = 0; i < n; i++) a[i] = i;
                break;
            case "reverse":
                for (int i = 0; i < n; i++) a[i] = n - i;
                break;
            case "duplicates":
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt(Math.max(1, n / 20)); // heavy repetition
                break;
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
        return a;
    }

    static Point[] generatePoints(int n, String type, int seed) {
        Random rnd = new Random(2000L * seed + n);
        Point[] pts = new Point[n];
        if (type.equals("duplicates")) {
            int distinct = Math.max(1, n / 20);
            Point[] pool = new Point[distinct];
            for (int i = 0; i < distinct; i++) {
                pool[i] = new Point(rnd.nextDouble() * 100000, rnd.nextDouble() * 100000);
            }
            // Copy coordinates into a *new* Point object per index: ClosestPairSolver
            // partitions left/right by object identity, so distinct positions in
            // the array must never share the same underlying Point reference.
            for (int i = 0; i < n; i++) {
                Point base = pool[rnd.nextInt(distinct)];
                pts[i] = new Point(base.x, base.y);
            }
        } else {
            for (int i = 0; i < n; i++) {
                pts[i] = new Point(rnd.nextDouble() * 100000, rnd.nextDouble() * 100000);
            }
        }
        return pts;
    }
}
