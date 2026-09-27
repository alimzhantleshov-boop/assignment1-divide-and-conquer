import java.util.Arrays;
import java.util.Random;

/**
 * Entry point: runs a short correctness/demo pass to stdout, then runs the
 * full experiment suite and writes results/results.csv.
 *
 * Usage:
 *   java -cp target/classes Main            (runs demo + experiments)
 *   java -cp target/classes Main --demo     (demo only, no CSV)
 */
public class Main {
    public static void main(String[] args) throws Exception {
        demo();

        if (args.length > 0 && args[0].equals("--demo")) {
            return;
        }

        System.out.println("\nRunning full experiment suite (this can take a while for n=50000)...");
        Experiment.run("results/results.csv");
    }

    private static void demo() {
        System.out.println("=== Divide & Conquer Algorithms: Demo ===\n");
        Random rnd = new Random(42);

        // MergeSort demo
        int[] a1 = randomArray(20, rnd);
        int[] a1Copy = a1.clone();
        System.out.println("MergeSort input:  " + Arrays.toString(a1));
        Metrics m1 = MergeSorter.sort(a1);
        System.out.println("MergeSort output: " + Arrays.toString(a1));
        System.out.println("Correct: " + isSorted(a1) + " | matches Arrays.sort: " + matches(a1, sortedCopy(a1Copy)));
        System.out.println("Metrics: " + m1 + "\n");

        // QuickSort demo
        int[] a2 = randomArray(20, rnd);
        int[] a2Copy = a2.clone();
        System.out.println("QuickSort input:  " + Arrays.toString(a2));
        Metrics m2 = QuickSorter.sort(a2);
        System.out.println("QuickSort output: " + Arrays.toString(a2));
        System.out.println("Correct: " + isSorted(a2) + " | matches Arrays.sort: " + matches(a2, sortedCopy(a2Copy)));
        System.out.println("Metrics: " + m2 + "\n");

        // Deterministic Select demo
        int[] a3 = randomArray(21, rnd);
        int[] a3Sorted = sortedCopy(a3);
        int k = a3.length / 2;
        Metrics m3 = new Metrics();
        int selected = DeterministicSelector.select(a3.clone(), k, m3);
        System.out.println("Select k=" + k + " -> " + selected + " | Arrays.sort(a)[k] = " + a3Sorted[k]
                + " | match: " + (selected == a3Sorted[k]));
        System.out.println("Metrics: " + m3 + "\n");

        // Closest Pair demo
        Point[] pts = randomPoints(200, rnd);
        Metrics m4 = new Metrics();
        ClosestPairSolver.Result fast = ClosestPairSolver.solve(pts, m4);
        ClosestPairSolver.Result brute = ClosestPairSolver.bruteForceAll(pts);
        System.out.println("ClosestPair (fast):  " + fast.p1 + " - " + fast.p2 + " dist=" + fast.distance);
        System.out.println("ClosestPair (brute): " + brute.p1 + " - " + brute.p2 + " dist=" + brute.distance);
        System.out.println("Match: " + (Math.abs(fast.distance - brute.distance) < 1e-9));
        System.out.println("Metrics: " + m4 + "\n");
    }

    private static int[] randomArray(int n, Random rnd) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = rnd.nextInt(200) - 100;
        return a;
    }

    private static Point[] randomPoints(int n, Random rnd) {
        Point[] pts = new Point[n];
        for (int i = 0; i < n; i++) pts[i] = new Point(rnd.nextDouble() * 1000, rnd.nextDouble() * 1000);
        return pts;
    }

    private static int[] sortedCopy(int[] a) {
        int[] b = a.clone();
        Arrays.sort(b);
        return b;
    }

    private static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) if (a[i - 1] > a[i]) return false;
        return true;
    }

    private static boolean matches(int[] a, int[] b) {
        return Arrays.equals(a, b);
    }
}
