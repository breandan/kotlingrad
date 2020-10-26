// Copyright (c) 1999, Silicon Graphics, Inc. -- ALL RIGHTS RESERVED
//
// Permission is granted free of charge to copy, modify, use and distribute
// this software  provided you include the entirety of this notice in all
// copies made.
//
// THIS SOFTWARE IS PROVIDED ON AN AS IS BASIS, WITHOUT WARRANTY OF ANY
// KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION,
// WARRANTIES THAT THE SUBJECT SOFTWARE IS FREE OF DEFECTS, MERCHANTABLE, FIT
// FOR A PARTICULAR PURPOSE OR NON-INFRINGING.   SGI ASSUMES NO RISK AS TO THE
// QUALITY AND PERFORMANCE OF THE SOFTWARE.   SHOULD THE SOFTWARE PROVE
// DEFECTIVE IN ANY RESPECT, SGI ASSUMES NO COST OR LIABILITY FOR ANY
// SERVICING, REPAIR OR CORRECTION.  THIS DISCLAIMER OF WARRANTY CONSTITUTES
// AN ESSENTIAL PART OF THIS LICENSE. NO USE OF ANY SUBJECT SOFTWARE IS
// AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
//
// UNDER NO CIRCUMSTANCES AND UNDER NO LEGAL THEORY, WHETHER TORT (INCLUDING,
// WITHOUT LIMITATION, NEGLIGENCE OR STRICT LIABILITY), CONTRACT, OR
// OTHERWISE, SHALL SGI BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL,
// INCIDENTAL, OR CONSEQUENTIAL DAMAGES OF ANY CHARACTER WITH RESPECT TO THE
// SOFTWARE INCLUDING, WITHOUT LIMITATION, DAMAGES FOR LOSS OF GOODWILL, WORK
// STOPPAGE, LOSS OF DATA, COMPUTER FAILURE OR MALFUNCTION, OR ANY AND ALL
// OTHER COMMERCIAL DAMAGES OR LOSSES, EVEN IF SGI SHALL HAVE BEEN INFORMED OF
// THE POSSIBILITY OF SUCH DAMAGES.  THIS LIMITATION OF LIABILITY SHALL NOT
// APPLY TO LIABILITY RESULTING FROM SGI's NEGLIGENCE TO THE EXTENT APPLICABLE
// LAW PROHIBITS SUCH LIMITATION.  SOME JURISDICTIONS DO NOT ALLOW THE
// EXCLUSION OR LIMITATION OF INCIDENTAL OR CONSEQUENTIAL DAMAGES, SO THAT
// EXCLUSION AND LIMITATION MAY NOT APPLY TO YOU.
//
// These license terms shall be governed by and construed in accordance with
// the laws of the United States and the State of California as applied to
// agreements entered into and to be performed entirely within California
// between California residents.  Any litigation relating to these license
// terms shall be subject to the exclusive jurisdiction of the Federal Courts
// of the Northern District of California (or, absent subject matter
// jurisdiction in such courts, the courts of the State of California), with
// venue lying exclusively in Santa Clara County, California.

// Superficial sanity test for the constructive reals package.

// Added test for division by negative number.  Hans_Boehm@hp.com, 8/13/01
// Modified to use AssertionFailedError. hboehm@google.com, 6/6/14
// Modified further for Android/JUnit testing framework, 12/15/14
// Added basic asin and acos tests, improved messages,
//        hboehm@google.com, 5/22/15

package com.hp.creals;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class CRTest {
    private static void check(boolean x, String s) {
        if (!x) throw new Error(s);
    }
    private static void check_eq(CR x, CR y, String s) {
        if (x.compareTo(y, -50) != 0) {
            throw new Error(s + "(" + x + " vs. " + y + ")");
        }
    }
    private static void check_appr_eq(double x, double y, String s) {
        if (Math.abs(x - y) > 0.000001) {
            throw new Error(s + "(" + x + " vs. " + y + ")");
        }
    }
    // TODO: Break this up into meaningful smaller tests.
    @Test
    public void testCR() {
        CR zero = CR.valueOf(0);
        CR one = CR.valueOf(1);
        CR two = CR.valueOf(2);
        check(one.signum() == 1, "signum(1) failed");
        check(one.negate().signum() == -1, "signum(-1) failed");
        check(zero.signum(-100) == 0, "signum(0) failed");
        check(one.compareTo(two, -10) == -1, "comparison failed");
        check(two.toString(4).compareTo("2.0000") == 0, "toString failed");
        check_eq(one.shiftLeft(1),two, "shiftLeft failed");
        check_eq(two.shiftRight(1),one, "shiftRight failed");
        check_eq(one.add(one),two, "add failed 1");
        check_eq(one.max(two),two, "max failed");
        check_eq(one.min(two),one, "min failed");
        check_eq(one.abs(),one, "abs failed 1");
        check_eq(one.negate().abs(),one, "abs failed 2");
        CR three = two.add(one);
        CR four = two.add(two);
        check_eq(CR.valueOf(4), four, "2 + 2 failed");
        check_eq(CR.valueOf(3), three, "2 + 1 failed");
        check_eq(one.negate().add(two), one, "negate failed");
        check(one.negate().signum() == -1, "signum(-1) failed");
        check_eq(two.multiply(two), four, "multiply failed");
        check_eq(one.divide(four).shiftLeft(4), four, "divide failed 1");
        check_eq(two.divide(one.negate()), two.negate(), "divide(neg) failed");
        CR thirteen = CR.valueOf(13);
        check_eq(one.divide(thirteen).multiply(thirteen), one,
                 "divide failed 2");
        check(thirteen.floatValue() == 13.0, "floatValue failed");
        check(thirteen.intValue() == 13, "intValue failed");
        check(thirteen.longValue() == 13, "longValue failed");
        check(thirteen.doubleValue() == 13.0, "doubleValue failed");
        check_eq(zero.exp(), one, "exp(0) failed");
        CR e = one.exp();
        check(e.toString(20).substring(0,17)
                            .compareTo("2.718281828459045") == 0,
              "exp(1) failed");
        check_eq(e.ln(), one, "ln(e) failed");
        CR half_pi = CR.PI.divide(two);
        CR half = one.divide(two);
        BigInteger million = BigInteger.valueOf(1000*1000);
        BigInteger thousand = BigInteger.valueOf(1000);
        CR huge = CR.valueOf(million.multiply(million).multiply(thousand));
        UnaryCRFunction asin = UnaryCRFunction.asinFunction;
        UnaryCRFunction acos = UnaryCRFunction.acosFunction;
        UnaryCRFunction atan = UnaryCRFunction.atanFunction;
        UnaryCRFunction tan = UnaryCRFunction.tanFunction;
        check_eq(half_pi.sin(), one, "sin(pi/2) failed");
        check_eq(asin.execute(one),half_pi, "asin(1) failed");
        check_eq(asin.execute(one.negate()),
                              half_pi.negate(), "asin(-1) failed");
        check_eq(asin.execute(zero), zero, "asin(0) failed");
        check_eq(asin.execute(half.sin()), half, "asin(sin(0.5)) failed");
        UnaryCRFunction cosine = UnaryCRFunction.sinFunction
                                        .monotoneDerivative(zero, CR.PI);
        check_eq(cosine.execute(one), one.cos(), "monotoneDerivative failed");
        check_eq(cosine.execute(three), three.cos(),
                 "monotoneDerivative failed 2");
        check_eq(asin.execute(one.sin()), one, "asin(sin(1) failed");
        check_eq(acos.execute(one.cos()), one, "acos(cos(1) failed");
        check_eq(atan.execute(tan.execute(one)), one, "atan(tan(1) failed");
        check_eq(atan.execute(tan.execute(one.negate())), one.negate(),
                 "atan(tan(-1) failed");
        check_eq(tan.execute(atan.execute(huge)), huge,
                 "tan(atan(10**15)) failed");
        CR sqrt13 = thirteen.sqrt();
        check_eq(sqrt13.multiply(sqrt13), thirteen, "sqrt(13)*sqrt(13) failed");
        CR tmp = CR.PI.add(CR.valueOf(-123).exp());
        CR tmp2 = tmp.subtract(CR.PI);
        check(tmp2.ln().intValue() == -123, "intValue(...) failed");
        check(tmp2.ln().longValue() == -123, "longValue(...) failed");
        check(tmp2.ln().floatValue() == -123.0, "floatValue(...) failed");
        check(tmp2.ln().doubleValue() == -123.0, "doubleValue(...) failed");
        for (double n = -10.0; n < 10.0; n += 2.0) {
            check_appr_eq(Math.sin(n), CR.valueOf(n).sin().doubleValue(),
                          "sin failed at " + n);
            check_appr_eq(Math.cos(n), CR.valueOf(n).cos().doubleValue(),
                          "cos failed at " + n);
            check_appr_eq(Math.exp(n), CR.valueOf(n).exp().doubleValue(),
                          "exp failed at " + n);
            check_appr_eq(Math.asin(0.1*n),
                          CR.valueOf(0.1*n).asin().doubleValue(),
                          "asin failed at " + 0.1*n);
            check_appr_eq(Math.acos(0.1*n),
                          CR.valueOf(0.1*n).acos().doubleValue(),
                          "acos failed at " + 0.1*n);
            if (n > 0.0) {
              check_appr_eq(Math.log(n), CR.valueOf(n).ln().doubleValue(),
                          "ln failed at " + n);
            }
        }
        check_appr_eq(Math.cos(12345678.0),
                      CR.valueOf(12345678).cos().doubleValue(),
                      "cos failed at " + 12345678);
    }
}
