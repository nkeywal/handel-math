import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class HandelAnalysis {

  private static final BigDecimal ONE = new BigDecimal(1);
  private static final BigDecimal TWO = new BigDecimal(2);


  private static final MathContext mc = new MathContext(256, RoundingMode.HALF_EVEN);

  /**
   * @param A   - 1
   * @param n   - nombre de level eg. 1000 max
   * @param eta - eg. 0.01
   * @param r   - eg. 3
   * @param C   - précision, eg 2
   */
  private static void Wn(final int A, final int n, final double eta, final int r, final BigDecimal C) {
    // W = A * n**3
    long np3 = (long) Math.pow(n, 3);
    assert np3 < Integer.MAX_VALUE;
    BigDecimal NP3 = new BigDecimal(np3);
    BigDecimal twoPn = BigDecimalMath.pow(TWO, new BigDecimal(n), mc);
    BigDecimal W = new BigDecimal(np3).multiply(new BigDecimal(A));
    // Walt = W
    BigDecimal Walt = new BigDecimal(W.longValue());

    //  l = int(math.floor(3 * math.log(n,2))) + r
    int l = (int)Math.ceil(3 * (Math.log(n) / Math.log(2))) + r;


    if (n > l) {
      // epsilon = eta/n
      BigDecimal epsilon = new BigDecimal(eta).divide(new BigDecimal(n), mc);

      // 1 - epsilon
      BigDecimal eme = ONE.subtract(epsilon);
      BigDecimal emeP2m2 = eme.pow(2).multiply(TWO);
      BigDecimal emem2 = eme.multiply(TWO);

      //for k in range(n - l):
      BigDecimal cnp3 = C.multiply(NP3);
      for (int k = 0; k < n - l; k++) {
        // p = 1 - C * n**3 / math.pow(2, k+l)
        BigDecimal pr = TWO.pow(k + l);
        final BigDecimal pd = cnp3.divide(pr, mc);
        final BigDecimal p = ONE.subtract(pd);

        // q = math.pow(p, W)
        BigDecimal q = BigDecimalMath.pow(p, W, mc);

        // W *= 2 (1 - epsilon) * (1 - q)
        W = W.multiply(emem2.multiply(ONE.subtract(q)));

        // Walt *= 2 (1 - epsilon)**2
        Walt = Walt.multiply(emeP2m2);

        if ( k == n - l - 1) {
          BigDecimal WmWalt = W.subtract(Walt);
          if (W.compareTo(Walt) < 0) {
            System.out.println("< diff=" + WmWalt.doubleValue());
            System.out.println("< W   =" + W.divide(twoPn, mc).doubleValue());
            System.out.println("< Walt=" + Walt.divide(twoPn, mc).doubleValue());
          } else {
            System.out.println("> " + WmWalt.doubleValue());
            System.out.println("> W   =" + W.divide(twoPn, mc).doubleValue());
            System.out.println("> Walt=" + Walt.divide(twoPn, mc).doubleValue());
          }
        }
      }
    }
  }


  public static void main(String... args) {
    for (int i = 80; i <= 100; i++) {
      System.out.println("\n\n******************* " + i);
      Wn(1, i, 0.001, 1, ONE);
    }
  }
}
