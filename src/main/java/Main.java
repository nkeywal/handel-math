import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

@SuppressWarnings("WeakerAccess") public class Main {
  final int levelCt;

  final int nodeCt;
  final boolean[] liveNodes;
  final static Random rd = new Random(0);

  Main(int levelCt) {
    this.levelCt = levelCt;
    this.nodeCt = (int) Math.pow(2, levelCt);
    this.liveNodes = new boolean[nodeCt];
  }

  void chooseDeadNodes(double liveness) {
    for (int i = 0; i < nodeCt; i++) {
      liveNodes[i] = rd.nextDouble() < liveness;
    }
  }

  int level(int i, int j) {
    for (int l = 0; l <= levelCt; l++) {
      if (i == j) {
        return l;
      }

      i /= 2;
      j /= 2;
    }

    throw new IllegalStateException();
  }

  boolean[][] decideComs(double[] ps) {
    boolean[][] coms = new boolean[nodeCt][nodeCt];

    for (int i = 0; i < nodeCt; i++) {
      for (int j = 0; j < nodeCt; j++) {
        if (liveNodes[i] && liveNodes[j]) {
          int l = level(i, j);
          boolean com = (rd.nextDouble() < ps[l]);
          coms[i][j] = com;
        }
      }
    }
    return coms;
  }

  int[] bestSigs(boolean[][] coms) {
    int[] t = new int[nodeCt];

    for (int i = 0; i < nodeCt; i++) {
      t[i] = coms[i][i] ? 1 : 0;
    }

    for (int it = 1; it < levelCt + 1; it++) {
      int[] tc = new int[nodeCt];
      for (int i = 0; i < nodeCt; i++) {
        int curMax = 0;
        for (int j = 0; j < nodeCt; j++) {
          if (coms[j][i] && level(j, i) == it) {
            curMax = Math.max(curMax, t[j]);
          }
        }
        tc[i] = t[i] + curMax;
      }
      t = tc;
    }

    return t;
  }

  int max(int[] t) {
    int m = 0;
    for (int v : t) {
      m = Math.max(m, v);
    }
    return m;
  }

  int median(int[] t, int ln) {
    int[] tt = t.clone();
    Arrays.sort(tt);
    return tt[(tt.length - ln) + ln / 2];
  }

  /**
   * Creates the level-wise probabilities of establishing a communication.
   * p[l] = x => probability to contact a node x at level l
   */
  double[] probas(double connectionCount) {
    double[] ps = new double[levelCt + 1];

    ps[0] = 1; // every node has its signature for the level 0.
    // ps[1] == base probability of contacting

    final double base = 1;
    for (int i = 1; i < ps.length; i++) {
      ps[i] = base * Math.min(1, (connectionCount / Math.pow(2, i)));
    }

    return ps;
  }


  double run(double liveness, double connectionCount) {
    chooseDeadNodes(liveness);
    double[] probas = probas(connectionCount);
    boolean[][] coms = decideComs(probas);
    int[] sigs = bestSigs(coms);
    double best = max(sigs);

    int ln = 0;
    for (boolean b : liveNodes) {
      if (b) {
        ln++;
      }
    }

    int median = median(sigs, ln);

    System.out.print((int) best + "/" + ln + " (" + median + ") - ");
    for (int i = 0; i < Math.min(100, sigs.length); i++) {
      System.out.print(sigs[i]);
      if (i < Math.min(100, sigs.length) - 1) {
        System.out.print(",");
      }
    }
    System.out.println();

    return best / ln;
  }

  double mrun(double liveness, double connectionCount) {
    double res = 0;

    for (int i = 0; i < nodeCt; i++) {
      System.out.print("node_" + i);
      if (i < nodeCt - 1) {
        System.out.print(",");
      }
    }
    System.out.println();

    int runCt = 10;
    for (int i = 0; i < runCt; i++) {
      res += run(liveness, connectionCount);
    }

    double avg = res / runCt;

    System.out.println(
      "levels=" + levelCt + ", liveness=" + liveness + ", connectionCount=" + connectionCount
        + ", avr=" + (100 * avg));
    return avg;
  }

  public static int log2(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException("n=" + n);
    }
    return 31 - Integer.numberOfLeadingZeros(n);
  }

  static void genData(FileOutputStream out, int level) throws IOException {
    Main m = new Main(level);

    double cnx = level / 3.0;

    for (double liveness = 0.8; liveness <= .81; liveness += 0.05) {
      for (double connectionCount = cnx; connectionCount < cnx+0.01; connectionCount += 0.5) {
        double res = m.mrun(liveness, connectionCount);
        String toWrite = level + "," + liveness + "," + connectionCount + "," + res + "\n";
        out.write(toWrite.getBytes());
      }
      out.write("\n".getBytes());
    }
  }

  static void genDataLevels(int levelCtMin, int levelCtMax) throws IOException {
    File data = new File("handel-data-fixedliveness-" + levelCtMax + "-" + levelCtMin + ".csv");
    try (FileOutputStream out = new FileOutputStream(data)) {
      out.write("level,liveness,connectionCount,avgBest\n".getBytes());
      for (int i = levelCtMin; i <= levelCtMax; i++) {
        genData(out, i);
      }
    }
  }

  public static void main(String... args) throws IOException {
    genDataLevels(5, 14);
  }
}
