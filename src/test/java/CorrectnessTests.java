import java.util.Arrays;
import java.util.Random;

/**
 * Plain-Java correctness test runner (no JUnit dependency needed, so it can
 * be compiled and run with only a JDK: see README for the exact commands).
 *
 * Covers section 3 of the assignment brief:
 *  - MergeSort / QuickSort vs Arrays.sort() on random, sorted, reverse-sorted,
 *    duplicate-heavy, empty and single-element arrays.
 *  - DeterministicSelector vs Arrays.sort(a)[k] over >=100 random trials.
 *  - ClosestPairSolver (fast) vs O(n^2) brute force for n <= 2000.
 */
public class CorrectnessTests {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testSortingAgainstReference("MergeSort");
        testSortingAgainstReference("QuickSort");
        testEdgeCases("MergeSort");
        testEdgeCases("QuickSort");
        testSelect();
        testClosestPair();

        System.out.println("\n===================================");
        System.out.println("PASSED: " + passed + "   FAILED: " + failed);
        System.out.println("===================================");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testSortingAgainstReference(String algo) {
        Random rnd = new Random(7);
        String[] types = {"random", "sorted", "reverse", "duplicates"};
        for (String type : types) {
            for (int trial = 0; trial < 20; trial++) {
                int n = 1 + rnd.nextInt(500);
                int[] a = Experiment.generateArray(n, type, trial + 1);
                int[] expected = a.clone();
                Arrays.sort(expected);
                int[] actual = a.clone();
                if (algo.equals("MergeSort")) {
                    MergeSorter.sort(actual);
                } else {
                    QuickSorter.sort(actual);
                }
                check(algo + " [" + type + ", n=" + n + ", trial=" + trial + "]",
                        Arrays.equals(expected, actual));
            }
        }
    }

    private static void testEdgeCases(String algo) {
        int[][] edgeCases = { {}, {1}, {2, 1}, {5, 5, 5, 5}, {1, 2, 3, 4, 5} };
        for (int[] original : edgeCases) {
            int[] expected = original.clone();
            Arrays.sort(expected);
            int[] actual = original.clone();
            if (algo.equals("MergeSort")) {
                MergeSorter.sort(actual);
            } else {
                QuickSorter.sort(actual);
            }
            check(algo + " edge case " + Arrays.toString(original), Arrays.equals(expected, actual));
        }
    }

    private static void testSelect() {
        Random rnd = new Random(11);
        int okCount = 0;
        int total = 200; // >= 100 required by the brief
        for (int trial = 0; trial < total; trial++) {
            int n = 1 + rnd.nextInt(300);
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = rnd.nextInt(1000) - 500;
            int k = rnd.nextInt(n);

            int[] referenceSorted = a.clone();
            Arrays.sort(referenceSorted);
            int expected = referenceSorted[k];

            Metrics m = new Metrics();
            int actual = DeterministicSelector.select(a.clone(), k, m);
            if (actual == expected) okCount++;
        }
        check("DeterministicSelect vs Arrays.sort(a)[k] over " + total + " random trials",
                okCount == total);
    }

    private static void testClosestPair() {
        Random rnd = new Random(13);
        for (int trial = 0; trial < 15; trial++) {
            int n = 2 + rnd.nextInt(400); // keep <= 2000 as required for brute-force comparison
            Point[] pts = new Point[n];
            for (int i = 0; i < n; i++) {
                pts[i] = new Point(rnd.nextDouble() * 1000, rnd.nextDouble() * 1000);
            }
            Metrics m = new Metrics();
            ClosestPairSolver.Result fast = ClosestPairSolver.solve(pts, m);
            ClosestPairSolver.Result brute = ClosestPairSolver.bruteForceAll(pts);
            boolean ok = Math.abs(fast.distance - brute.distance) < 1e-9;
            check("ClosestPair fast vs brute-force [n=" + n + ", trial=" + trial + "]", ok);
        }
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL: " + description);
        }
    }
}
