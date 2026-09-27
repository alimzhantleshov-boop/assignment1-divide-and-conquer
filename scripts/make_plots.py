"""
Reads results/results.csv (produced by `java -cp target/classes Main`) and
generates:
  docs/plots/time_vs_n.png
  docs/plots/depth_vs_n.png
  docs/plots/comparisons_vs_n.png (bonus)
  results/summary.csv  (averaged over trials, per algorithm/inputType/n)

Run with: python3 scripts/make_plots.py
"""
import csv
import os
from collections import defaultdict
import statistics as stats

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CSV_PATH = os.path.join(ROOT, "results", "results.csv")
PLOTS_DIR = os.path.join(ROOT, "docs", "plots")
SUMMARY_PATH = os.path.join(ROOT, "results", "summary.csv")

rows = []
with open(CSV_PATH) as f:
    reader = csv.DictReader(f)
    for r in reader:
        r["n"] = int(r["n"])
        r["timeNs"] = int(r["timeNs"])
        r["maxDepth"] = int(r["maxDepth"])
        r["comparisons"] = int(r["comparisons"])
        rows.append(r)

# ---- aggregate: mean over trials, grouped by (algorithm, inputType, n) ----
groups = defaultdict(list)
for r in rows:
    groups[(r["algorithm"], r["inputType"], r["n"])].append(r)

summary = []
for (algo, itype, n), rs in groups.items():
    summary.append({
        "algorithm": algo,
        "inputType": itype,
        "n": n,
        "avgTimeMs": stats.mean(r["timeNs"] for r in rs) / 1e6,
        "avgMaxDepth": stats.mean(r["maxDepth"] for r in rs),
        "avgComparisons": stats.mean(r["comparisons"] for r in rs),
    })
summary.sort(key=lambda r: (r["algorithm"], r["inputType"], r["n"]))

with open(SUMMARY_PATH, "w", newline="") as f:
    w = csv.DictWriter(f, fieldnames=["algorithm", "inputType", "n", "avgTimeMs", "avgMaxDepth", "avgComparisons"])
    w.writeheader()
    for row in summary:
        w.writerow(row)
print(f"Wrote {SUMMARY_PATH} ({len(summary)} rows)")

os.makedirs(PLOTS_DIR, exist_ok=True)

ALGOS = ["MergeSort", "QuickSort", "DeterministicSelect", "ClosestPair"]
COLORS = {"MergeSort": "#1f77b4", "QuickSort": "#d62728",
          "DeterministicSelect": "#2ca02c", "ClosestPair": "#9467bd"}


def series_for(algo, itype="random"):
    pts = [(r["n"], r["avgTimeMs"], r["avgMaxDepth"], r["avgComparisons"])
           for r in summary if r["algorithm"] == algo and r["inputType"] == itype]
    pts.sort()
    return pts


# ---- Plot 1: Time vs n (random input) ----
plt.figure(figsize=(7, 5))
for algo in ALGOS:
    pts = series_for(algo, "random")
    if not pts:
        continue
    ns = [p[0] for p in pts]
    ts = [p[1] for p in pts]
    plt.plot(ns, ts, marker="o", label=algo, color=COLORS[algo])
plt.xlabel("n (input size)")
plt.ylabel("Average time (ms)")
plt.title("Execution time vs n (random input, mean of 5 trials)")
plt.xscale("log")
plt.yscale("log")
plt.legend()
plt.grid(True, which="both", alpha=0.3)
plt.tight_layout()
plt.savefig(os.path.join(PLOTS_DIR, "time_vs_n.png"), dpi=150)
plt.close()

# ---- Plot 2: Recursion depth vs n (random input) ----
plt.figure(figsize=(7, 5))
for algo in ALGOS:
    pts = series_for(algo, "random")
    if not pts:
        continue
    ns = [p[0] for p in pts]
    ds = [p[2] for p in pts]
    plt.plot(ns, ds, marker="o", label=algo, color=COLORS[algo])
plt.xlabel("n (input size)")
plt.ylabel("Average max recursion depth")
plt.title("Recursion depth vs n (random input, mean of 5 trials)")
plt.xscale("log")
plt.legend()
plt.grid(True, which="both", alpha=0.3)
plt.tight_layout()
plt.savefig(os.path.join(PLOTS_DIR, "depth_vs_n.png"), dpi=150)
plt.close()

# ---- Bonus plot: comparisons vs n ----
plt.figure(figsize=(7, 5))
for algo in ALGOS:
    pts = series_for(algo, "random")
    if not pts:
        continue
    ns = [p[0] for p in pts]
    cs = [p[3] for p in pts]
    plt.plot(ns, cs, marker="o", label=algo, color=COLORS[algo])
plt.xlabel("n (input size)")
plt.ylabel("Average comparisons")
plt.title("Comparisons vs n (random input, mean of 5 trials)")
plt.xscale("log")
plt.yscale("log")
plt.legend()
plt.grid(True, which="both", alpha=0.3)
plt.tight_layout()
plt.savefig(os.path.join(PLOTS_DIR, "comparisons_vs_n.png"), dpi=150)
plt.close()

# ---- Bonus plot: QuickSort time by input type (shows O(n^2) on sorted/duplicate w/o randomization would show, here randomized so should be flat) ----
plt.figure(figsize=(7, 5))
for itype in ["random", "sorted", "reverse", "duplicates"]:
    pts = series_for("QuickSort", itype)
    if not pts:
        continue
    ns = [p[0] for p in pts]
    ts = [p[1] for p in pts]
    plt.plot(ns, ts, marker="o", label=itype)
plt.xlabel("n (input size)")
plt.ylabel("Average time (ms)")
plt.title("QuickSort: time vs n by input type")
plt.xscale("log")
plt.yscale("log")
plt.legend()
plt.grid(True, which="both", alpha=0.3)
plt.tight_layout()
plt.savefig(os.path.join(PLOTS_DIR, "quicksort_by_type.png"), dpi=150)
plt.close()

print("Plots written to", PLOTS_DIR)
