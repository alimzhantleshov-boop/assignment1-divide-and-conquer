import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Classic divide-and-conquer Closest Pair of Points.
 *
 * 1. Sort points by x once (Theta(n log n)), and keep a copy sorted by y
 *    for the merge step.
 * 2. Split into left/right halves, recursively find the closest pair in
 *    each half -> delta.
 * 3. Build the "strip" of points within delta of the dividing line and,
 *    for each point (processed in y-order), only compare it against the
 *    next few (<=7) neighbours in y-order (a well-known geometric packing
 *    argument bounds this to a constant).
 *
 * Recurrence: T(n) = 2T(n/2) + Theta(n) (the strip step is linear because
 * each point is compared against O(1) neighbours) => Theta(n log n) by the
 * Master Theorem (case 2).
 */
public class ClosestPairSolver {

    public static class Result {
        public final Point p1, p2;
        public final double distance;
        Result(Point p1, Point p2, double distance) {
            this.p1 = p1; this.p2 = p2; this.distance = distance;
        }
    }

    public static Result solve(Point[] points, Metrics m) {
        if (points.length < 2) {
            throw new IllegalArgumentException("Need at least 2 points");
        }
        Point[] byX = points.clone();
        Arrays.sort(byX, Comparator.comparingDouble(p -> p.x));
        Point[] byY = byX.clone();
        Arrays.sort(byY, Comparator.comparingDouble(p -> p.y));
        return closest(byX, byY, 0, byX.length - 1, 0, m);
    }

    private static Result closest(Point[] byX, Point[] byY, int lo, int hi, int depth, Metrics m) {
        m.updateDepth(depth);
        int n = hi - lo + 1;

        if (n <= 3) {
            return bruteForce(byX, lo, hi, m);
        }

        m.recursiveCalls += 2;
        int mid = lo + (hi - lo) / 2;
        double midX = byX[mid].x;

        // Identity-based membership test (Point uses default identity equals/hashCode),
        // so duplicate coordinates never cause a point to be mis-assigned.
        Set<Point> leftSet = new HashSet<>();
        for (int i = lo; i <= mid; i++) leftSet.add(byX[i]);

        Point[] leftY = new Point[mid - lo + 1];
        Point[] rightY = new Point[hi - mid];
        int li = 0, ri = 0;
        for (Point p : byY) {
            if (leftSet.contains(p)) {
                if (li < leftY.length) leftY[li++] = p;
            } else {
                if (ri < rightY.length) rightY[ri++] = p;
            }
        }

        Result leftBest = closest(byX, leftY, lo, mid, depth + 1, m);
        Result rightBest = closest(byX, rightY, mid + 1, hi, depth + 1, m);
        Result best = (leftBest.distance <= rightBest.distance) ? leftBest : rightBest;

        // Build the strip: points within 'best.distance' of the dividing line, in y-order.
        Point[] strip = new Point[n];
        int stripSize = 0;
        for (Point p : byY) {
            m.comparisons++;
            if (Math.abs(p.x - midX) < best.distance) {
                strip[stripSize++] = p;
            }
        }

        for (int i = 0; i < stripSize; i++) {
            for (int j = i + 1; j < stripSize && (strip[j].y - strip[i].y) < best.distance; j++) {
                m.comparisons++;
                double d = strip[i].dist(strip[j]);
                if (d < best.distance) {
                    best = new Result(strip[i], strip[j], d);
                }
            }
        }
        return best;
    }

    private static Result bruteForce(Point[] byX, int lo, int hi, Metrics m) {
        Result best = null;
        for (int i = lo; i <= hi; i++) {
            for (int j = i + 1; j <= hi; j++) {
                m.comparisons++;
                double d = byX[i].dist(byX[j]);
                if (best == null || d < best.distance) {
                    best = new Result(byX[i], byX[j], d);
                }
            }
        }
        return best;
    }

    /** O(n^2) reference brute force over the whole point set, used only for testing. */
    public static Result bruteForceAll(Point[] points) {
        Result best = null;
        for (int i = 0; i < points.length; i++) {
            for (int j = i + 1; j < points.length; j++) {
                double d = points[i].dist(points[j]);
                if (best == null || d < best.distance) {
                    best = new Result(points[i], points[j], d);
                }
            }
        }
        return best;
    }
}
