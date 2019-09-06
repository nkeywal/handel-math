public class RoutingProblem {

  int depth;

  int[] p;
  int[] q;

  RoutingProblem(int depth, int[] p, int[] q) {

    this.depth = depth;

    this.p = p;
    this.q = q;
  }

  boolean valuesAreDistinct (int[] p) {

    boolean areDistinct = true;

    for (int i=0; i < p.length; i++) {
      for (int j = i+1; j < p.length; j++) {
        areDistinct = areDistinct && (i != j);
      }
    }

    return areDistinct;
  }

  boolean valuesAreInRange (int[] p) {

    boolean inRange = true;

    for (int i=0; i<p.length; i++) {
      inRange = inRange && (0 <= p[i]) && (p[i] < p.length);
    }

    return inRange;
  }

  boolean isPermutation (int[] p) {

    boolean isPermutationOrNot = valuesAreDistinct(p) && valuesAreInRange(p);

    return isPermutationOrNot;
  }

  boolean validRP (RoutingProblem rp) {

    int card = (int) Math.pow(2, rp.depth);

    if ((rp.p.length == card) && (rp.q.length == card) && isPermutation(rp.p) && isPermutation(rp.q)) {

      boolean isValid = true;

      for (int i=0; i<card; i++) {

        isValid = isValid && (p[q[i]]==i);
      }

      return isValid;
    }

    else {

      return false;
    }
  }

  int jumpBy (int x, int jump) {

    int width = 2 * jump;

    int quot = x / width;

    return width*quot + ((x+jump) % width);
  }

  boolean isAbove (int x, int jump) {

    int width = 2*jump;

    return ((x%width) >= jump);
  }

  int conditionalJumpBy (int x, int jump, boolean doIt) {

    if (!doIt) {

      return x;
    }

    else {

      return jumpBy(x, jump);
    }
  }


  // for a routing problem of depth d, it produces
  // a router that is a (2^d) x (2 * d - 1)
  // boolean table.

  boolean[][] benesRouting (RoutingProblem rp) {

    int card = (int) Math.pow(2, rp.depth);

    boolean[][] RoutingTable = new boolean[card][2*rp.depth-1];     // résultat final

    for (int round = 1; round < depth; round++) {   // this loop populates all the columns EXCEPT the central one

      boolean[] visited = new boolean[card];      // every round has a new visited boolean table

      int currentJump = (int) Math.pow(2, depth - round);

      for (int i = 0; i < card; i++) {

        if (!visited[i]) {

          int x = i;

          boolean xVert = false;      // this could be started randomly; we choose a horizontal start.

          while (!visited[x]) {


            int y = p[x];

            int xJump = jumpBy(x, currentJump);
            int yJump = jumpBy(y, currentJump);

            boolean xIsAbove = isAbove(x, currentJump);
            boolean yIsAbove = isAbove(y, currentJump);

            boolean yVert = xVert ^ xIsAbove ^ yIsAbove;

            int z = q[yJump];
            boolean zIsAbove = isAbove(z, currentJump);

            boolean zVert = zIsAbove ^ (!yIsAbove) ^ yVert;

            RoutingTable[x][round - 1] = xVert;
            RoutingTable[xJump][round - 1] = xVert;
            RoutingTable[y][2 * depth - round - 1] = yVert;
            RoutingTable[yJump][2 * depth - round - 1] = yVert;

            // updating p at x and z
            p[conditionalJumpBy(x, currentJump, xVert)] = conditionalJumpBy(y, currentJump, yVert);
            p[conditionalJumpBy(z, currentJump, zVert)] = conditionalJumpBy(yJump, currentJump, yVert);

            // updating q at y and yJump
            q[conditionalJumpBy(y, currentJump, yVert)] = conditionalJumpBy(x, currentJump, xVert);
            q[conditionalJumpBy(yJump, currentJump, yVert)] = conditionalJumpBy(z, currentJump, zVert);

            x = jumpBy(z, currentJump);
            xVert = zVert;
          }
        }
      }
    }

    // the central row : switch or not
    for (int i=0; i<card; i++) {

      RoutingTable[i][depth-1] = (p[i]==q[i]);
    }

    return RoutingTable;
  }

  public static void main(String[] args) {
    // write your code here

    int[] p = {3,2,0,1};

    int[] q = {2,3,0,1};

    int depth = 2;

    RoutingProblem rp = new RoutingProblem(depth, p, q);

    boolean[][] solution = rp.benesRouting(rp);

    // System.out.println(solution);
  }
}
