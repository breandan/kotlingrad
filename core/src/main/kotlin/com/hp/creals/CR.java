/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * The above license covers additions and changes by AOSP authors.
 * The original code is licensed as follows:
 */

//
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

// Copyright (c) 2001-2004, Hewlett-Packard Development Company, L.P.
//
// Permission is granted free of charge to copy, modify, use and distribute
// this software  provided you include the entirety of this notice in all
// copies made.
//
// THIS SOFTWARE IS PROVIDED ON AN AS IS BASIS, WITHOUT WARRANTY OF ANY
// KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION,
// WARRANTIES THAT THE SUBJECT SOFTWARE IS FREE OF DEFECTS, MERCHANTABLE, FIT
// FOR A PARTICULAR PURPOSE OR NON-INFRINGING.   HEWLETT-PACKARD ASSUMES
// NO RISK AS TO THE QUALITY AND PERFORMANCE OF THE SOFTWARE.
// SHOULD THE SOFTWARE PROVE DEFECTIVE IN ANY RESPECT,
// HEWLETT-PACKARD ASSUMES NO COST OR LIABILITY FOR ANY
// SERVICING, REPAIR OR CORRECTION.  THIS DISCLAIMER OF WARRANTY CONSTITUTES
// AN ESSENTIAL PART OF THIS LICENSE. NO USE OF ANY SUBJECT SOFTWARE IS
// AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
//
// UNDER NO CIRCUMSTANCES AND UNDER NO LEGAL THEORY, WHETHER TORT (INCLUDING,
// WITHOUT LIMITATION, NEGLIGENCE OR STRICT LIABILITY), CONTRACT, OR
// OTHERWISE, SHALL HEWLETT-PACKARD BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL,
// INCIDENTAL, OR CONSEQUENTIAL DAMAGES OF ANY CHARACTER WITH RESPECT TO THE
// SOFTWARE INCLUDING, WITHOUT LIMITATION, DAMAGES FOR LOSS OF GOODWILL, WORK
// STOPPAGE, LOSS OF DATA, COMPUTER FAILURE OR MALFUNCTION, OR ANY AND ALL
// OTHER COMMERCIAL DAMAGES OR LOSSES, EVEN IF HEWLETT-PACKARD SHALL
// HAVE BEEN INFORMED OF THE POSSIBILITY OF SUCH DAMAGES.
// THIS LIMITATION OF LIABILITY SHALL NOT APPLY TO LIABILITY RESULTING
// FROM HEWLETT-PACKARD's NEGLIGENCE TO THE EXTENT APPLICABLE
// LAW PROHIBITS SUCH LIMITATION.  SOME JURISDICTIONS DO NOT ALLOW THE
// EXCLUSION OR LIMITATION OF INCIDENTAL OR CONSEQUENTIAL DAMAGES, SO THAT
// EXCLUSION AND LIMITATION MAY NOT APPLY TO YOU.
//

// Added valueOf(string, radix), fixed some documentation comments.
//              Hans_Boehm@hp.com 1/12/2001
// Fixed a serious typo in inv_CR():  For negative arguments it produced
//              the wrong sign.  This affected the sign of divisions.
// Added byteValue and fixed some comments.  Hans.Boehm@hp.com 12/17/2002
// Added toStringFloatRep.      Hans.Boehm@hp.com 4/1/2004
// Added get_appr() synchronization to allow access from multiple threads
// hboehm@google.com 4/25/2014
// Changed cos() prescaling to avoid logarithmic depth tree.
// hboehm@google.com 6/30/2014
// Added explicit asin() implementation.  Remove one.  Add ZERO and ONE and
// make them public.  hboehm@google.com 5/21/2015
// Added Gauss-Legendre PI implementation.  Removed two.
// hboehm@google.com 4/12/2016
// Fix shift operation in doubleValue. That produced incorrect values for
// large negative exponents.
// Don't negate argument and compute inverse for exp(). That causes severe
// performance problems for (-huge).exp()
// hboehm@google.com 8/21/2017
// Have comparison check for interruption. hboehm@google.com 10/31/2017
// Fix precision overflow issue in most general compareTo function.
// Fix a couple of unused variable bugs. Notably selector_sign was
// accidentally locally redeclared. (This turns out to be safe but useless.)
// hboehm@google.com 11/20/2018.
// Fix an exception-safety issue in gl_pi_CR.approximate.
// hboehm@google.com 3/3/2019.
// Near-overflow floating point exponents were not handled correctly in
// doubleValue(). Fixed.
// hboehm@google.com 7/23/2019.

package com.hp.creals;

import java.math.BigInteger;
import java.util.ArrayList;

/**
* Constructive real numbers, also known as recursive, or computable reals.
* Each recursive real number is represented as an object that provides an
* approximation function for the real number.
* The approximation function guarantees that the generated approximation
* is accurate to the specified precision.
* Arithmetic operations on constructive reals produce new such objects;
* they typically do not perform any real computation.
* In this sense, arithmetic computations are exact: They produce
* a description which describes the exact answer, and can be used to
* later approximate it to arbitrary precision.
* <P>
* When approximations are generated, <I>e.g.</i> for output, they are
* accurate to the requested precision; no cumulative rounding errors
* are visible.
* In order to achieve this precision, the approximation function will often
* need to approximate subexpressions to greater precision than was originally
* demanded.  Thus the approximation of a constructive real number
* generated through a complex sequence of operations may eventually require
* evaluation to very high precision.  This usually makes such computations
* prohibitively expensive for large numerical problems.
* But it is perfectly appropriate for use in a desk calculator,
* for small numerical problems, for the evaluation of expressions
* computated by a symbolic algebra system, for testing of accuracy claims
* for floating point code on small inputs, or the like.
* <P>
* We expect that the vast majority of uses will ignore the particular
* implementation, and the member functons <TT>approximate</tt>
* and <TT>get_appr</tt>.  Such applications will treat <TT>CR</tt> as
* a conventional numerical type, with an interface modelled on
* <TT>java.math.BigInteger</tt>.  No subclasses of <TT>CR</tt>
* will be explicitly mentioned by such a program.
* <P>
* All standard arithmetic operations, as well as a few algebraic
* and transcendal functions are provided.  Constructive reals are
* immutable; thus all of these operations return a new constructive real.
* <P>
* A few uses will require explicit construction of approximation functions.
* The requires the construction of a subclass of <TT>CR</tt> with
* an overridden <TT>approximate</tt> function.  Note that <TT>approximate</tt>
* should only be defined, but never called.  <TT>get_appr</tt>
* provides the same functionality, but adds the caching necessary to obtain
* reasonable performance.
* <P>
* Any operation may throw <TT>com.hp.creals.AbortedException</tt> if the thread
* in which it is executing is interrupted.  (<TT>InterruptedException</tt>
* cannot be used for this purpose, since CR inherits from <TT>Number</tt>.)
* <P>
* Any operation may also throw <TT>com.hp.creals.PrecisionOverflowException</tt>
* If the precision request generated during any subcalculation overflows
* a 28-bit integer.  (This should be extremely unlikely, except as an
* outcome of a division by zero, or other erroneous computation.)
*
*/
public abstract class CR extends Number {
    // CR is the basic representation of a number.
    // Abstractly this is a function for computing an approximation
    // plus the current best approximation.
    // We could do without the latter, but that would
    // be atrociously slow.

/**
 * Indicates a constructive real operation was interrupted.
 * Most constructive real operations may throw such an exception.
 * This is unchecked, since Number methods may not raise checked
 * exceptions.
*/
public static class AbortedException extends RuntimeException {
    public AbortedException() { super(); }
    public AbortedException(String s) { super(s); }
}

/**
 * Indicates that the number of bits of precision requested by
 * a computation on constructive reals required more than 28 bits,
 * and was thus in danger of overflowing an int.
 * This is likely to be a symptom of a diverging computation,
 * <I>e.g.</i> division by zero.
*/
public static class PrecisionOverflowException extends RuntimeException {
    public PrecisionOverflowException() { super(); }
    public PrecisionOverflowException(String s) { super(s); }
}

    // First some frequently used constants, so we don't have to
    // recompute these all over the place.
      static final BigInteger big0 = BigInteger.ZERO;
      static final BigInteger big1 = BigInteger.ONE;
      static final BigInteger bigm1 = BigInteger.valueOf(-1);
      static final BigInteger big2 = BigInteger.valueOf(2);
      static final BigInteger bigm2 = BigInteger.valueOf(-2);
      static final BigInteger big3 = BigInteger.valueOf(3);
      static final BigInteger big6 = BigInteger.valueOf(6);
      static final BigInteger big8 = BigInteger.valueOf(8);
      static final BigInteger big10 = BigInteger.TEN;
      static final BigInteger big750 = BigInteger.valueOf(750);
      static final BigInteger bigm750 = BigInteger.valueOf(-750);

/**
* Setting this to true requests that  all computations be aborted by
* throwing AbortedException.  Must be rest to false before any further
* computation.  Ideally Thread.interrupt() should be used instead, but
* that doesn't appear to be consistently supported by browser VMs.
*/
public volatile static boolean please_stop = false;

/**
* Must be defined in subclasses of <TT>CR</tt>.
* Most users can ignore the existence of this method, and will
* not ever need to define a <TT>CR</tt> subclass.
* Returns value / 2 ** precision rounded to an integer.
* The error in the result is strictly < 1.
* Informally, approximate(n) gives a scaled approximation
* accurate to 2**n.
* Implementations may safely assume that precision is
* at least a factor of 8 away from overflow.
* Called only with the lock on the <TT>CR</tt> object
* already held.
*/
      protected abstract BigInteger approximate(int precision);
      transient int min_prec;
        // The smallest precision value with which the above
        // has been called.
      transient BigInteger max_appr;
        // The scaled approximation corresponding to min_prec.
      transient boolean appr_valid = false;
        // min_prec and max_val are valid.

    // Helper functions
      static int bound_log2(int n) {
        int abs_n = Math.abs(n);
        return (int)Math.ceil(Math.log((double)(abs_n + 1))/Math.log(2.0));
      }
      // Check that a precision is at least a factor of 8 away from
      // overflowng the integer used to hold a precision spec.
      // We generally perform this check early on, and then convince
      // ourselves that none of the operations performed on precisions
      // inside a function can generate an overflow.
      static void check_prec(int n) {
        int high = n >> 28;
        // if n is not in danger of overflowing, then the 4 high order
        // bits should be identical.  Thus high is either 0 or -1.
        // The rest of this is to test for either of those in a way
        // that should be as cheap as possible.
        int high_shifted = n >> 29;
        if (0 != (high ^ high_shifted)) {
            throw new PrecisionOverflowException();
        }
      }

/**
* The constructive real number corresponding to a
* <TT>BigInteger</tt>.
*/
      public static CR valueOf(BigInteger n) {
        return new int_CR(n);
      }

/**
* The constructive real number corresponding to a
* Java <TT>int</tt>.
*/
      public static CR valueOf(int n) {
        return valueOf(BigInteger.valueOf(n));
      }

/**
* The constructive real number corresponding to a
* Java <TT>long</tt>.
*/
      public static CR valueOf(long n) {
        return valueOf(BigInteger.valueOf(n));
      }

/**
* The constructive real number corresponding to a
* Java <TT>double</tt>.
* The result is undefined if argument is infinite or NaN.
*/
      public static CR valueOf(double n) {
        if (Double.isNaN(n)) throw new ArithmeticException("Nan argument");
        if (Double.isInfinite(n)) {
            throw new ArithmeticException("Infinite argument");
        }
        boolean negative = (n < 0.0);
        long bits = Double.doubleToLongBits(Math.abs(n));
        long mantissa = (bits & 0xfffffffffffffL);
        int biased_exp = (int)(bits >> 52);
        int exp = biased_exp - 1075;
        if (biased_exp != 0) {
            mantissa += (1L << 52);
        } else {
            mantissa <<= 1;
        }
        CR result = valueOf(mantissa).shiftLeft(exp);
        if (negative) result = result.negate();
        return result;
      }

/**
* The constructive real number corresponding to a
* Java <TT>float</tt>.
* The result is undefined if argument is infinite or NaN.
*/
      public static CR valueOf(float n) {
        return valueOf((double) n);
      }

      public static CR ZERO = valueOf(0);
      public static CR ONE = valueOf(1);

    // Multiply k by 2**n.
      static BigInteger shift(BigInteger k, int n) {
        if (n == 0) return k;
        if (n < 0) return k.shiftRight(-n);
        return k.shiftLeft(n);
      }

    // Multiply by 2**n, rounding result
      static BigInteger scale(BigInteger k, int n) {
        if (n >= 0) {
            return k.shiftLeft(n);
        } else {
            BigInteger adj_k = shift(k, n+1).add(big1);
            return adj_k.shiftRight(1);
        }
      }

    // Identical to approximate(), but maintain and update cache.
/**
* Returns value / 2 ** prec rounded to an integer.
* The error in the result is strictly < 1.
* Produces the same answer as <TT>approximate</tt>, but uses and
* maintains a cached approximation.
* Normally not overridden, and called only from <TT>approximate</tt>
* methods in subclasses.  Not needed if the provided operations
* on constructive reals suffice.
*/
      public synchronized BigInteger get_appr(int precision) {
        check_prec(precision);
        if (appr_valid && precision >= min_prec) {
            return scale(max_appr, min_prec - precision);
        } else {
            BigInteger result = approximate(precision);
            min_prec = precision;
            max_appr = result;
            appr_valid = true;
            return result;
        }
      }

    // Return the position of the msd.
    // If x.msd() == n then
    // 2**(n-1) < abs(x) < 2**(n+1)
    // This initial version assumes that max_appr is valid
    // and sufficiently removed from zero
    // that the msd is determined.
      int known_msd() {
        int first_digit;
        int length;
        if (max_appr.signum() >= 0) {
            length = max_appr.bitLength();
        } else {
            length = max_appr.negate().bitLength();
        }
        first_digit = min_prec + length - 1;
        return first_digit;
      }

    // This version may return Integer.MIN_VALUE if the correct
    // answer is < n.
      int msd(int n) {
        if (!appr_valid ||
                max_appr.compareTo(big1) <= 0
                && max_appr.compareTo(bigm1) >= 0) {
            get_appr(n - 1);
            if (max_appr.abs().compareTo(big1) <= 0) {
                // msd could still be arbitrarily far to the right.
                return Integer.MIN_VALUE;
            }
        }
        return known_msd();
      }


    // Functionally equivalent, but iteratively evaluates to higher
    // precision.
      int iter_msd(int n)
      {
        int prec = 0;

        for (; prec > n + 30; prec = (prec * 3)/2 - 16) {
            int msd = msd(prec);
            if (msd != Integer.MIN_VALUE) return msd;
            check_prec(prec);
            if (Thread.interrupted() || please_stop) {
                throw new AbortedException();
            }
        }
        return msd(n);
      }

    // This version returns a correct answer eventually, except
    // that it loops forever (or throws an exception when the
    // requested precision overflows) if this constructive real is zero.
      int msd() {
          return iter_msd(Integer.MIN_VALUE);
      }

    // A helper function for toString.
    // Generate a String containing n zeroes.
      private static String zeroes(int n) {
        char[] a = new char[n];
        for (int i = 0; i < n; ++i) {
            a[i] = '0';
        }
        return new String(a);
      }

    // Natural log of 2.  Needed for some prescaling below.
    // ln(2) = 7ln(10/9) - 2ln(25/24) + 3ln(81/80)
        CR simple_ln() {
            return new prescaled_ln_CR(this.subtract(ONE));
        }
        static CR ten_ninths = valueOf(10).divide(valueOf(9));
        static CR twentyfive_twentyfourths = valueOf(25).divide(valueOf(24));
        static CR eightyone_eightyeths = valueOf(81).divide(valueOf(80));
        static CR ln2_1 = valueOf(7).multiply(ten_ninths.simple_ln());
        static CR ln2_2 =
                valueOf(2).multiply(twentyfive_twentyfourths.simple_ln());
        static CR ln2_3 = valueOf(3).multiply(eightyone_eightyeths.simple_ln());
        static CR ln2 = ln2_1.subtract(ln2_2).add(ln2_3);

    // Atan of integer reciprocal.  Used for atan_PI.  Could perhaps be made
    // public.
        static CR atan_reciprocal(int n) {
            return new integral_atan_CR(n);
        }
    // Other constants used for PI computation.
        static CR four = valueOf(4);

  // Public operations.
/**
* Return 0 if x = y to within the indicated tolerance,
* -1 if x < y, and +1 if x > y.  If x and y are indeed
* equal, it is guaranteed that 0 will be returned.  If
* they differ by less than the tolerance, anything
* may happen.  The tolerance allowed is
* the maximum of (abs(this)+abs(x))*(2**r) and 2**a
*       @param x        The other constructive real
*       @param r        Relative tolerance in bits
*       @param a        Absolute tolerance in bits
*/
      public int compareTo(CR x, int r, int a) {
        int this_msd = iter_msd(a);
        int x_msd = x.iter_msd(this_msd > a? this_msd : a);
        int max_msd = (x_msd > this_msd? x_msd : this_msd);
        if (max_msd == Integer.MIN_VALUE) {
          return 0;
        }
        check_prec(r);
        int rel = max_msd + r;
        int abs_prec = (rel > a? rel : a);
        return compareTo(x, abs_prec);
      }

/**
* Approximate comparison with only an absolute tolerance.
* Identical to the three argument version, but without a relative
* tolerance.
* Result is 0 if both constructive reals are equal, indeterminate
* if they differ by less than 2**a.
*
*       @param x        The other constructive real
*       @param a        Absolute tolerance in bits
*/
      public int compareTo(CR x, int a) {
        int needed_prec = a - 1;
        BigInteger this_appr = get_appr(needed_prec);
        BigInteger x_appr = x.get_appr(needed_prec);
        int comp1 = this_appr.compareTo(x_appr.add(big1));
        if (comp1 > 0) return 1;
        int comp2 = this_appr.compareTo(x_appr.subtract(big1));
        if (comp2 < 0) return -1;
        return 0;
      }

/**
* Return -1 if <TT>this &lt; x</tt>, or +1 if <TT>this &gt; x</tt>.
* Should be called only if <TT>this != x</tt>.
* If <TT>this == x</tt>, this will not terminate correctly; typically it
* will run until it exhausts memory.
* If the two constructive reals may be equal, the two or 3 argument
* version of compareTo should be used.
*/
      public int compareTo(CR x) {
        for (int a = -20; ; a *= 2) {
            check_prec(a);
            int result = compareTo(x, a);
            if (0 != result) return result;
            if (Thread.interrupted() || please_stop) {
                throw new AbortedException();
            }
        }
      }

/**
* Equivalent to <TT>compareTo(CR.valueOf(0), a)</tt>
*/
      public int signum(int a) {
        if (appr_valid) {
            int quick_try = max_appr.signum();
            if (0 != quick_try) return quick_try;
        }
        int needed_prec = a - 1;
        BigInteger this_appr = get_appr(needed_prec);
        return this_appr.signum();
      }

/**
* Return -1 if negative, +1 if positive.
* Should be called only if <TT>this != 0</tt>.
* In the 0 case, this will not terminate correctly; typically it
* will run until it exhausts memory.
* If the two constructive reals may be equal, the one or two argument
* version of signum should be used.
*/
      public int signum() {
        for (int a = -20; ; a *= 2) {
            check_prec(a);
            int result = signum(a);
            if (0 != result) return result;
            if (Thread.interrupted() || please_stop) {
                throw new AbortedException();
            }
        }
      }

/**
* Return the constructive real number corresponding to the given
* textual representation and radix.
*
*       @param s        [-] digit* [. digit*]
*       @param radix
*/

      public static CR valueOf(String s, int radix)
             throws NumberFormatException {
          int len = s.length();
          int start_pos = 0, point_pos;
          String fraction;
          while (s.charAt(start_pos) == ' ') ++start_pos;
          while (s.charAt(len - 1) == ' ') --len;
          point_pos = s.indexOf('.', start_pos);
          if (point_pos == -1) {
              point_pos = len;
              fraction = "0";
          } else {
              fraction = s.substring(point_pos + 1, len);
          }
          String whole = s.substring(start_pos, point_pos);
          BigInteger scaled_result = new BigInteger(whole + fraction, radix);
          BigInteger divisor = BigInteger.valueOf(radix).pow(fraction.length());
          return CR.valueOf(scaled_result).divide(CR.valueOf(divisor));
      }

/**
* Return a textual representation accurate to <TT>n</tt> places
* to the right of the decimal point.  <TT>n</tt> must be nonnegative.
*
*       @param  n       Number of digits (>= 0) included to the right of decimal point
*       @param  radix   Base ( >= 2, <= 16) for the resulting representation.
*/
      public String toString(int n, int radix) {
          CR scaled_CR;
          if (16 == radix) {
            scaled_CR = shiftLeft(4*n);
          } else {
            BigInteger scale_factor = BigInteger.valueOf(radix).pow(n);
            scaled_CR = multiply(new int_CR(scale_factor));
          }
          BigInteger scaled_int = scaled_CR.get_appr(0);
          String scaled_string = scaled_int.abs().toString(radix);
          String result;
          if (0 == n) {
              result = scaled_string;
          } else {
              int len = scaled_string.length();
              if (len <= n) {
                // Add sufficient leading zeroes
                  String z = zeroes(n + 1 - len);
                  scaled_string = z + scaled_string;
                  len = n + 1;
              }
              String whole = scaled_string.substring(0, len - n);
              String fraction = scaled_string.substring(len - n);
              result = whole + "." + fraction;
          }
          if (scaled_int.signum() < 0) {
              result = "-" + result;
          }
          return result;
      }


/**
* Equivalent to <TT>toString(n,10)</tt>
*
*       @param  n       Number of digits included to the right of decimal point
*/
    public String toString(int n) {
        return toString(n, 10);
    }

/**
* Equivalent to <TT>toString(10, 10)</tt>
*/
    public String toString() {
        return toString(10);
    }

    static double doubleLog2 = Math.log(2.0);
/**
* Return a textual scientific notation representation accurate
* to <TT>n</tt> places to the right of the decimal point.
* <TT>n</tt> must be nonnegative.  A value smaller than
* <TT>radix</tt>**-<TT>m</tt> may be displayed as 0.
* The <TT>mantissa</tt> component of the result is either "0"
* or exactly <TT>n</tt> digits long.  The <TT>sign</tt>
* component is zero exactly when the mantissa is "0".
*
*       @param  n       Number of digits (&gt; 0) included to the right of decimal point.
*       @param  radix   Base ( &ge; 2, &le; 16) for the resulting representation.
*       @param  m       Precision used to distinguish number from zero.
*                       Expressed as a power of m.
*/
    public StringFloatRep toStringFloatRep(int n, int radix, int m) {
        if (n <= 0) throw new ArithmeticException("Bad precision argument");
        double log2_radix = Math.log((double)radix)/doubleLog2;
        BigInteger big_radix = BigInteger.valueOf(radix);
        long long_msd_prec = (long)(log2_radix * (double)m);
        if (long_msd_prec > (long)Integer.MAX_VALUE
            || long_msd_prec < (long)Integer.MIN_VALUE)
            throw new PrecisionOverflowException();
        int msd_prec = (int)long_msd_prec;
        check_prec(msd_prec);
        int msd = iter_msd(msd_prec - 2);
        if (msd == Integer.MIN_VALUE)
            return new StringFloatRep(0, "0", radix, 0);
        int exponent = (int)Math.ceil((double)msd / log2_radix);
                // Guess for the exponent.  Try to get it usually right.
        int scale_exp = exponent - n;
        CR scale;
        if (scale_exp > 0) {
            scale = CR.valueOf(big_radix.pow(scale_exp)).inverse();
        } else {
            scale = CR.valueOf(big_radix.pow(-scale_exp));
        }
        CR scaled_res = multiply(scale);
        BigInteger scaled_int = scaled_res.get_appr(0);
        int sign = scaled_int.signum();
        String scaled_string = scaled_int.abs().toString(radix);
        while (scaled_string.length() < n) {
            // exponent was too large.  Adjust.
            scaled_res = scaled_res.multiply(CR.valueOf(big_radix));
            exponent -= 1;
            scaled_int = scaled_res.get_appr(0);
            sign = scaled_int.signum();
            scaled_string = scaled_int.abs().toString(radix);
        }
        if (scaled_string.length() > n) {
            // exponent was too small.  Adjust by truncating.
            exponent += (scaled_string.length() - n);
            scaled_string = scaled_string.substring(0, n);
        }
        return new StringFloatRep(sign, scaled_string, radix, exponent);
    }

/**
* Return a BigInteger which differs by less than one from the
* constructive real.
*/
    public BigInteger BigIntegerValue() {
        return get_appr(0);
    }

/**
* Return an int which differs by less than one from the
* constructive real.  Behavior on overflow is undefined.
*/
    public int intValue() {
        return BigIntegerValue().intValue();
    }

/**
* Return an int which differs by less than one from the
* constructive real.  Behavior on overflow is undefined.
*/
    public byte byteValue() {
        return BigIntegerValue().byteValue();
    }

/**
* Return a long which differs by less than one from the
* constructive real.  Behavior on overflow is undefined.
*/
    public long longValue() {
        return BigIntegerValue().longValue();
    }

/**
* Return a double which differs by less than one in the least
* represented bit from the constructive real.
* (We're in fact closer to round-to-nearest than that, but we can't and
* don't promise correct rounding.)
*/
    public double doubleValue() {
        int my_msd = iter_msd(-1080 /* slightly > exp. range */);
        if (Integer.MIN_VALUE == my_msd) return 0.0;
        int needed_prec = my_msd - 60;
        double scaled_int = get_appr(needed_prec).doubleValue();
        boolean may_underflow = (needed_prec < -1000);
        long scaled_int_rep = Double.doubleToLongBits(scaled_int);
        long exp_adj = may_underflow? needed_prec + 96 : needed_prec;
        long orig_exp = (scaled_int_rep >> 52) & 0x7ff;
        // Original unbiased exponent is > 50. Exp_adj > -1050.
        // Thus the sum must be > the smallest representable exponent
        // of -1023.
        if (orig_exp + exp_adj >= 0x7ff) {
            // Exponent overflowed.
            if (scaled_int < 0.0) {
                return Double.NEGATIVE_INFINITY;
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }
        scaled_int_rep += exp_adj << 52;
        double result = Double.longBitsToDouble(scaled_int_rep);
        if (may_underflow) {
            // Exponent is too large by 96. Compensate, relying on fp arithmetic
            // to handle gradual underflow correctly.
            double two48 = (double)(1L << 48);
            return result/two48/two48;
        } else {
            return result;
        }
    }

/**
* Return a float which differs by less than one in the least
* represented bit from the constructive real.
*/
    public float floatValue() {
        return (float)doubleValue();
        // Note that double-rounding is not a problem here, since we
        // cannot, and do not, guarantee correct rounding.
    }

/**
* Add two constructive reals.
*/
    public CR add(CR x) {
        return new add_CR(this, x);
    }

/**
* Multiply a constructive real by 2**n.
* @param n      shift count, may be negative
*/
    public CR shiftLeft(int n) {
        check_prec(n);
        return new shifted_CR(this, n);
    }

/**
* Multiply a constructive real by 2**(-n).
* @param n      shift count, may be negative
*/
    public CR shiftRight(int n) {
        check_prec(n);
        return new shifted_CR(this, -n);
    }

/**
* Produce a constructive real equivalent to the original, assuming
* the original was an integer.  Undefined results if the original
* was not an integer.  Prevents evaluation of digits to the right
* of the decimal point, and may thus improve performance.
*/
    public CR assumeInt() {
        return new assumed_int_CR(this);
    }

/**
* The additive inverse of a constructive real
*/
    public CR negate() {
        return new neg_CR(this);
    }

/**
* The difference between two constructive reals
*/
    public CR subtract(CR x) {
        return new add_CR(this, x.negate());
    }

/**
* The product of two constructive reals
*/
    public CR multiply(CR x) {
        return new mult_CR(this, x);
    }

/**
* The multiplicative inverse of a constructive real.
* <TT>x.inverse()</tt> is equivalent to <TT>CR.valueOf(1).divide(x)</tt>.
*/
    public CR inverse() {
        return new inv_CR(this);
    }

/**
* The quotient of two constructive reals.
*/
    public CR divide(CR x) {
        return new mult_CR(this, x.inverse());
    }

/**
* The real number <TT>x</tt> if <TT>this</tt> < 0, or <TT>y</tt> otherwise.
* Requires <TT>x</tt> = <TT>y</tt> if <TT>this</tt> = 0.
* Since comparisons may diverge, this is often
* a useful alternative to conditionals.
*/
    public CR select(CR x, CR y) {
        return new select_CR(this, x, y);
    }

/**
* The maximum of two constructive reals.
*/
    public CR max(CR x) {
        return subtract(x).select(x, this);
    }

/**
* The minimum of two constructive reals.
*/
    public CR min(CR x) {
        return subtract(x).select(this, x);
    }

/**
* The absolute value of a constructive reals.
* Note that this cannot be written as a conditional.
*/
    public CR abs() {
        return select(negate(), this);
    }

/**
* The exponential function, that is e**<TT>this</tt>.
*/
    public CR exp() {
        final int low_prec = -10;
        BigInteger rough_appr = get_appr(low_prec);
        // Handle negative arguments directly; negating and computing inverse
        // can be very expensive.
        if (rough_appr.compareTo(big2) > 0 || rough_appr.compareTo(bigm2) < 0) {
            CR square_root = shiftRight(1).exp();
            return square_root.multiply(square_root);
        } else {
            return new prescaled_exp_CR(this);
        }
    }

/**
* The ratio of a circle's circumference to its diameter.
*/
    public static CR PI = new gl_pi_CR();

    // Our old PI implementation. Keep this around for now to allow checking.
    // This implementation may also be faster for BigInteger implementations
    // that support only quadratic multiplication, but exhibit high performance
    // for small computations.  (The standard Android 6 implementation supports
    // subquadratic multiplication, but has high constant overhead.) Many other
    // atan-based formulas are possible, but based on superficial
    // experimentation, this is roughly as good as the more complex formulas.
    public static CR atan_PI = four.multiply(four.multiply(atan_reciprocal(5))
                                            .subtract(atan_reciprocal(239)));
        // pi/4 = 4*atan(1/5) - atan(1/239)
    static CR half_pi = PI.shiftRight(1);

/**
* The trigonometric cosine function.
*/
    public CR cos() {
        BigInteger halfpi_multiples = divide(PI).get_appr(-1);
        BigInteger abs_halfpi_multiples = halfpi_multiples.abs();
        if (abs_halfpi_multiples.compareTo(big2) >= 0) {
            // Subtract multiples of PI
            BigInteger pi_multiples = scale(halfpi_multiples, -1);
            CR adjustment = PI.multiply(CR.valueOf(pi_multiples));
            if (pi_multiples.and(big1).signum() != 0) {
                return subtract(adjustment).cos().negate();
            } else {
                return subtract(adjustment).cos();
            }
        } else if (get_appr(-1).abs().compareTo(big2) >= 0) {
            // Scale further with double angle formula
            CR cos_half = shiftRight(1).cos();
            return cos_half.multiply(cos_half).shiftLeft(1).subtract(ONE);
        } else {
            return new prescaled_cos_CR(this);
        }
    }

/**
* The trigonometric sine function.
*/
    public CR sin() {
        return half_pi.subtract(this).cos();
    }

/**
* The trignonometric arc (inverse) sine function.
*/
    public CR asin() {
        BigInteger rough_appr = get_appr(-10);
        if (rough_appr.compareTo(big750) /* 1/sqrt(2) + a bit */ > 0){
            CR new_arg = ONE.subtract(multiply(this)).sqrt();
            return new_arg.acos();
        } else if (rough_appr.compareTo(bigm750) < 0) {
            return negate().asin().negate();
        } else {
            return new prescaled_asin_CR(this);
        }
    }

/**
* The trignonometric arc (inverse) cosine function.
*/
    public CR acos() {
        return half_pi.subtract(asin());
    }

    static final BigInteger low_ln_limit = big8; /* sixteenths, i.e. 1/2 */
    static final BigInteger high_ln_limit =
                        BigInteger.valueOf(16 + 8 /* 1.5 */);
    static final BigInteger scaled_4 =
                        BigInteger.valueOf(4*16);

/**
* The natural (base e) logarithm.
*/
    public CR ln() {
        final int low_prec = -4;
        BigInteger rough_appr = get_appr(low_prec); /* In sixteenths */
        if (rough_appr.compareTo(big0) < 0) {
            throw new ArithmeticException("ln(negative)");
        }
        if (rough_appr.compareTo(low_ln_limit) <= 0) {
            return inverse().ln().negate();
        }
        if (rough_appr.compareTo(high_ln_limit) >= 0) {
            if (rough_appr.compareTo(scaled_4) <= 0) {
                CR quarter = sqrt().sqrt().ln();
                return quarter.shiftLeft(2);
            } else {
                int extra_bits = rough_appr.bitLength() - 3;
                CR scaled_result = shiftRight(extra_bits).ln();
                return scaled_result.add(CR.valueOf(extra_bits).multiply(ln2));
            }
        }
        return simple_ln();
    }

/**
* The square root of a constructive real.
*/
    public CR sqrt() {
        return new sqrt_CR(this);
    }

}  // end of CR


//
// A specialization of CR for cases in which approximate() calls
// to increase evaluation precision are somewhat expensive.
// If we need to (re)evaluate, we speculatively evaluate to slightly
// higher precision, miminimizing reevaluations.
// Note that this requires any arguments to be evaluated to higher
// precision than absolutely necessary.  It can thus potentially
// result in lots of wasted effort, and should be used judiciously.
// This assumes that the order of magnitude of the number is roughly one.
//
abstract class slow_CR extends CR {
    static int max_prec = -64;
    static int prec_incr = 32;
    public synchronized BigInteger get_appr(int precision) {
        check_prec(precision);
        if (appr_valid && precision >= min_prec) {
            return scale(max_appr, min_prec - precision);
        } else {
            int eval_prec = (precision >= max_prec? max_prec :
                             (precision - prec_incr + 1) & ~(prec_incr - 1));
            BigInteger result = approximate(eval_prec);
            min_prec = eval_prec;
            max_appr = result;
            appr_valid = true;
            return scale(result, eval_prec - precision);
        }
    }
}


// Representation of an integer constant.  Private.
class int_CR extends CR {
    BigInteger value;
    int_CR(BigInteger n) {
        value = n;
    }
    protected BigInteger approximate(int p) {
        return scale(value, -p) ;
    }
}

// Representation of a number that may not have been completely
// evaluated, but is assumed to be an integer.  Hence we never
// evaluate beyond the decimal point.
class assumed_int_CR extends CR {
    CR value;
    assumed_int_CR(CR x) {
        value = x;
    }
    protected BigInteger approximate(int p) {
        if (p >= 0) {
            return value.get_appr(p);
        } else {
            return scale(value.get_appr(0), -p) ;
        }
    }
}

// Representation of the sum of 2 constructive reals.  Private.
class add_CR extends CR {
    CR op1;
    CR op2;
    add_CR(CR x, CR y) {
        op1 = x;
        op2 = y;
    }
    protected BigInteger approximate(int p) {
        // Args need to be evaluated so that each error is < 1/4 ulp.
        // Rounding error from the cale call is <= 1/2 ulp, so that
        // final error is < 1 ulp.
        return scale(op1.get_appr(p-2).add(op2.get_appr(p-2)), -2);
    }
}

// Representation of a CR multiplied by 2**n
class shifted_CR extends CR {
    CR op;
    int count;
    shifted_CR(CR x, int n) {
        op = x;
        count = n;
    }
    protected BigInteger approximate(int p) {
        return op.get_appr(p - count);
    }
}

// Representation of the negation of a constructive real.  Private.
class neg_CR extends CR {
    CR op;
    neg_CR(CR x) {
        op = x;
    }
    protected BigInteger approximate(int p) {
        return op.get_appr(p).negate();
    }
}

// Representation of:
//      op1     if selector < 0
//      op2     if selector >= 0
// Assumes x = y if s = 0
class select_CR extends CR {
    CR selector;
    int selector_sign;
    CR op1;
    CR op2;
    select_CR(CR s, CR x, CR y) {
        selector = s;
        selector_sign = selector.get_appr(-20).signum();
        op1 = x;
        op2 = y;
    }
    protected BigInteger approximate(int p) {
        if (selector_sign < 0) return op1.get_appr(p);
        if (selector_sign > 0) return op2.get_appr(p);
        BigInteger op1_appr = op1.get_appr(p-1);
        BigInteger op2_appr = op2.get_appr(p-1);
        BigInteger diff = op1_appr.subtract(op2_appr).abs();
        if (diff.compareTo(big1) <= 0) {
            // close enough; use either
            return scale(op1_appr, -1);
        }
        // op1 and op2 are different; selector != 0;
        // safe to get sign of selector.
        if (selector.signum() < 0) {
            selector_sign = -1;
            return scale(op1_appr, -1);
        } else {
            selector_sign = 1;
            return scale(op2_appr, -1);
        }
    }
}

// Representation of the product of 2 constructive reals. Private.
class mult_CR extends CR {
    CR op1;
    CR op2;
    mult_CR(CR x, CR y) {
        op1 = x;
        op2 = y;
    }
    protected BigInteger approximate(int p) {
        int half_prec = (p >> 1) - 1;
        int msd_op1 = op1.msd(half_prec);
        int msd_op2;

        if (msd_op1 == Integer.MIN_VALUE) {
            msd_op2 = op2.msd(half_prec);
            if (msd_op2 == Integer.MIN_VALUE) {
                // Product is small enough that zero will do as an
                // approximation.
                return big0;
            } else {
                // Swap them, so the larger operand (in absolute value)
                // is first.
                CR tmp;
                tmp = op1;
                op1 = op2;
                op2 = tmp;
                msd_op1 = msd_op2;
            }
        }
        // msd_op1 is valid at this point.
        int prec2 = p - msd_op1 - 3;    // Precision needed for op2.
                // The appr. error is multiplied by at most
                // 2 ** (msd_op1 + 1)
                // Thus each approximation contributes 1/4 ulp
                // to the rounding error, and the final rounding adds
                // another 1/2 ulp.
        BigInteger appr2 = op2.get_appr(prec2);
        if (appr2.signum() == 0) return big0;
        msd_op2 = op2.known_msd();
        int prec1 = p - msd_op2 - 3;    // Precision needed for op1.
        BigInteger appr1 = op1.get_appr(prec1);
        int scale_digits =  prec1 + prec2 - p;
        return scale(appr1.multiply(appr2), scale_digits);
    }
}

// Representation of the multiplicative inverse of a constructive
// real.  Private.  Should use Newton iteration to refine estimates.
class inv_CR extends CR {
    CR op;
    inv_CR(CR x) { op = x; }
    protected BigInteger approximate(int p) {
        int msd = op.msd();
        int inv_msd = 1 - msd;
        int digits_needed = inv_msd - p + 3;
                                // Number of SIGNIFICANT digits needed for
                                // argument, excl. msd position, which may
                                // be fictitious, since msd routine can be
                                // off by 1.  Roughly 1 extra digit is
                                // needed since the relative error is the
                                // same in the argument and result, but
                                // this isn't quite the same as the number
                                // of significant digits.  Another digit
                                // is needed to compensate for slop in the
                                // calculation.
                                // One further bit is required, since the
                                // final rounding introduces a 0.5 ulp
                                // error.
        int prec_needed = msd - digits_needed;
        int log_scale_factor = -p - prec_needed;
        if (log_scale_factor < 0) return big0;
        BigInteger dividend = big1.shiftLeft(log_scale_factor);
        BigInteger scaled_divisor = op.get_appr(prec_needed);
        BigInteger abs_scaled_divisor = scaled_divisor.abs();
        BigInteger adj_dividend = dividend.add(
                                        abs_scaled_divisor.shiftRight(1));
                // Adjustment so that final result is rounded.
        BigInteger result = adj_dividend.divide(abs_scaled_divisor);
        if (scaled_divisor.signum() < 0) {
          return result.negate();
        } else {
          return result;
        }
    }
}


// Representation of the exponential of a constructive real.  Private.
// Uses a Taylor series expansion.  Assumes |x| < 1/2.
// Note: this is known to be a bad algorithm for
// floating point.  Unfortunately, other alternatives
// appear to require precomputed information.
class prescaled_exp_CR extends CR {
    CR op;
    prescaled_exp_CR(CR x) { op = x; }
    protected BigInteger approximate(int p) {
        if (p >= 1) return big0;
        int iterations_needed = -p/2 + 2;  // conservative estimate > 0.
          //  Claim: each intermediate term is accurate
          //  to 2*2^calc_precision.
          //  Total rounding error in series computation is
          //  2*iterations_needed*2^calc_precision,
          //  exclusive of error in op.
        int calc_precision = p - bound_log2(2*iterations_needed)
                               - 4; // for error in op, truncation.
        int op_prec = p - 3;
        BigInteger op_appr = op.get_appr(op_prec);
          // Error in argument results in error of < 3/8 ulp.
          // Sum of term eval. rounding error is < 1/16 ulp.
          // Series truncation error < 1/16 ulp.
          // Final rounding error is <= 1/2 ulp.
          // Thus final error is < 1 ulp.
        BigInteger scaled_1 = big1.shiftLeft(-calc_precision);
        BigInteger current_term = scaled_1;
        BigInteger current_sum = scaled_1;
        int n = 0;
        BigInteger max_trunc_error =
                big1.shiftLeft(p - 4 - calc_precision);
        while (current_term.abs().compareTo(max_trunc_error) >= 0) {
          if (Thread.interrupted() || please_stop) throw new AbortedException();
          n += 1;
          /* current_term = current_term * op / n */
          current_term = scale(current_term.multiply(op_appr), op_prec);
          current_term = current_term.divide(BigInteger.valueOf(n));
          current_sum = current_sum.add(current_term);
        }
        return scale(current_sum, calc_precision - p);
    }
}

// Representation of the cosine of a constructive real.  Private.
// Uses a Taylor series expansion.  Assumes |x| < 1.
class prescaled_cos_CR extends slow_CR {
    CR op;
    prescaled_cos_CR(CR x) {
        op = x;
    }
    protected BigInteger approximate(int p) {
        if (p >= 1) return big0;
        int iterations_needed = -p/2 + 4;  // conservative estimate > 0.
          //  Claim: each intermediate term is accurate
          //  to 2*2^calc_precision.
          //  Total rounding error in series computation is
          //  2*iterations_needed*2^calc_precision,
          //  exclusive of error in op.
        int calc_precision = p - bound_log2(2*iterations_needed)
                               - 4; // for error in op, truncation.
        int op_prec = p - 2;
        BigInteger op_appr = op.get_appr(op_prec);
          // Error in argument results in error of < 1/4 ulp.
          // Cumulative arithmetic rounding error is < 1/16 ulp.
          // Series truncation error < 1/16 ulp.
          // Final rounding error is <= 1/2 ulp.
          // Thus final error is < 1 ulp.
        BigInteger current_term;
        int n;
        BigInteger max_trunc_error =
                big1.shiftLeft(p - 4 - calc_precision);
        n = 0;
        current_term = big1.shiftLeft(-calc_precision);
        BigInteger current_sum = current_term;
        while (current_term.abs().compareTo(max_trunc_error) >= 0) {
          if (Thread.interrupted() || please_stop) throw new AbortedException();
          n += 2;
          /* current_term = - current_term * op * op / n * (n - 1)   */
          current_term = scale(current_term.multiply(op_appr), op_prec);
          current_term = scale(current_term.multiply(op_appr), op_prec);
          BigInteger divisor = BigInteger.valueOf(-n)
                                  .multiply(BigInteger.valueOf(n-1));
          current_term = current_term.divide(divisor);
          current_sum = current_sum.add(current_term);
        }
        return scale(current_sum, calc_precision - p);
    }
}

// The constructive real atan(1/n), where n is a small integer
// > base.
// This gives a simple and moderately fast way to compute PI.
class integral_atan_CR extends slow_CR {
    int op;
    integral_atan_CR(int x) { op = x; }
    protected BigInteger approximate(int p) {
        if (p >= 1) return big0;
        int iterations_needed = -p/2 + 2;  // conservative estimate > 0.
          //  Claim: each intermediate term is accurate
          //  to 2*base^calc_precision.
          //  Total rounding error in series computation is
          //  2*iterations_needed*base^calc_precision,
          //  exclusive of error in op.
        int calc_precision = p - bound_log2(2*iterations_needed)
                               - 2; // for error in op, truncation.
          // Error in argument results in error of < 3/8 ulp.
          // Cumulative arithmetic rounding error is < 1/4 ulp.
          // Series truncation error < 1/4 ulp.
          // Final rounding error is <= 1/2 ulp.
          // Thus final error is < 1 ulp.
        BigInteger scaled_1 = big1.shiftLeft(-calc_precision);
        BigInteger big_op = BigInteger.valueOf(op);
        BigInteger big_op_squared = BigInteger.valueOf(op*op);
        BigInteger op_inverse = scaled_1.divide(big_op);
        BigInteger current_power = op_inverse;
        BigInteger current_term = op_inverse;
        BigInteger current_sum = op_inverse;
        int current_sign = 1;
        int n = 1;
        BigInteger max_trunc_error =
                big1.shiftLeft(p - 2 - calc_precision);
        while (current_term.abs().compareTo(max_trunc_error) >= 0) {
          if (Thread.interrupted() || please_stop) throw new AbortedException();
          n += 2;
          current_power = current_power.divide(big_op_squared);
          current_sign = -current_sign;
          current_term =
            current_power.divide(BigInteger.valueOf(current_sign*n));
          current_sum = current_sum.add(current_term);
        }
        return scale(current_sum, calc_precision - p);
    }
}

// Representation for ln(1 + op)
class prescaled_ln_CR extends slow_CR {
    CR op;
    prescaled_ln_CR(CR x) { op = x; }
    // Compute an approximation of ln(1+x) to precision
    // prec. This assumes |x| < 1/2.
    // It uses a Taylor series expansion.
    // Unfortunately there appears to be no way to take
    // advantage of old information.
    // Note: this is known to be a bad algorithm for
    // floating point.  Unfortunately, other alternatives
    // appear to require precomputed tabular information.
    protected BigInteger approximate(int p) {
        if (p >= 0) return big0;
        int iterations_needed = -p;  // conservative estimate > 0.
          //  Claim: each intermediate term is accurate
          //  to 2*2^calc_precision.  Total error is
          //  2*iterations_needed*2^calc_precision
          //  exclusive of error in op.
        int calc_precision = p - bound_log2(2*iterations_needed)
                               - 4; // for error in op, truncation.
        int op_prec = p - 3;
        BigInteger op_appr = op.get_appr(op_prec);
          // Error analysis as for exponential.
        BigInteger x_nth = scale(op_appr, op_prec - calc_precision);
        BigInteger current_term = x_nth;  // x**n
        BigInteger current_sum = current_term;
        int n = 1;
        int current_sign = 1;   // (-1)^(n-1)
        BigInteger max_trunc_error =
                big1.shiftLeft(p - 4 - calc_precision);
        while (current_term.abs().compareTo(max_trunc_error) >= 0) {
          if (Thread.interrupted() || please_stop) throw new AbortedException();
          n += 1;
          current_sign = -current_sign;
          x_nth = scale(x_nth.multiply(op_appr), op_prec);
          current_term = x_nth.divide(BigInteger.valueOf(n * current_sign));
                                // x**n / (n * (-1)**(n-1))
          current_sum = current_sum.add(current_term);
        }
        return scale(current_sum, calc_precision - p);
    }
}

// Representation of the arcsine of a constructive real.  Private.
// Uses a Taylor series expansion.  Assumes |x| < (1/2)^(1/3).
class prescaled_asin_CR extends slow_CR {
    CR op;
    prescaled_asin_CR(CR x) {
        op = x;
    }
    protected BigInteger approximate(int p) {
        // The Taylor series is the sum of x^(2n+1) * (2n)!/(4^n n!^2 (2n+1))
        // Note that (2n)!/(4^n n!^2) is always less than one.
        // (The denominator is effectively 2n*2n*(2n-2)*(2n-2)*...*2*2
        // which is clearly > (2n)!)
        // Thus all terms are bounded by x^(2n+1).
        // Unfortunately, there's no easy way to prescale the argument
        // to less than 1/sqrt(2), and we can only approximate that.
        // Thus the worst case iteration count is fairly high.
        // But it doesn't make much difference.
        if (p >= 2) return big0;  // Never bigger than 4.
        int iterations_needed = -3 * p / 2 + 4;
                                // conservative estimate > 0.
                                // Follows from assumed bound on x and
                                // the fact that only every other Taylor
                                // Series term is present.
          //  Claim: each intermediate term is accurate
          //  to 2*2^calc_precision.
          //  Total rounding error in series computation is
          //  2*iterations_needed*2^calc_precision,
          //  exclusive of error in op.
        int calc_precision = p - bound_log2(2*iterations_needed)
                               - 4; // for error in op, truncation.
        int op_prec = p - 3;  // always <= -2
        BigInteger op_appr = op.get_appr(op_prec);
          // Error in argument results in error of < 1/4 ulp.
          // (Derivative is bounded by 2 in the specified range and we use
          // 3 extra digits.)
          // Ignoring the argument error, each term has an error of
          // < 3ulps relative to calc_precision, which is more precise than p.
          // Cumulative arithmetic rounding error is < 3/16 ulp (relative to p).
          // Series truncation error < 2/16 ulp.  (Each computed term
          // is at most 2/3 of last one, so some of remaining series <
          // 3/2 * current term.)
          // Final rounding error is <= 1/2 ulp.
          // Thus final error is < 1 ulp (relative to p).
        BigInteger max_last_term =
                big1.shiftLeft(p - 4 - calc_precision);
        int exp = 1; // Current exponent, = 2n+1 in above expression
        BigInteger current_term = op_appr.shiftLeft(op_prec - calc_precision);
        BigInteger current_sum = current_term;
        BigInteger current_factor = current_term;
                                    // Current scaled Taylor series term
                                    // before division by the exponent.
                                    // Accurate to 3 ulp at calc_precision.
        while (current_term.abs().compareTo(max_last_term) >= 0) {
          if (Thread.interrupted() || please_stop) throw new AbortedException();
          exp += 2;
          // current_factor = current_factor * op * op * (exp-1) * (exp-2) /
          // (exp-1) * (exp-1), with the two exp-1 factors cancelling,
          // giving
          // current_factor = current_factor * op * op * (exp-2) / (exp-1)
          // Thus the error any in the previous term is multiplied by
          // op^2, adding an error of < (1/2)^(2/3) < 2/3 the original
          // error.
          current_factor = current_factor.multiply(BigInteger.valueOf(exp - 2));
          current_factor = scale(current_factor.multiply(op_appr), op_prec + 2);
                // Carry 2 extra bits of precision forward; thus
                // this effectively introduces 1/8 ulp error.
          current_factor = current_factor.multiply(op_appr);
          BigInteger divisor = BigInteger.valueOf(exp - 1);
          current_factor = current_factor.divide(divisor);
                // Another 1/4 ulp error here.
          current_factor = scale(current_factor, op_prec - 2);
                // Remove extra 2 bits.  1/2 ulp rounding error.
          // Current_factor has original 3 ulp rounding error, which we
          // reduced by 1, plus < 1 ulp new rounding error.
          current_term = current_factor.divide(BigInteger.valueOf(exp));
                // Contributes 1 ulp error to sum plus at most 3 ulp
                // from current_factor.
          current_sum = current_sum.add(current_term);
        }
        return scale(current_sum, calc_precision - p);
      }
  }


class sqrt_CR extends CR {
    CR op;
    sqrt_CR(CR x) { op = x; }
    // Explicitly provide an initial approximation.
    // Useful for arithmetic geometric mean algorithms, where we've previously
    // computed a very similar square root.
    sqrt_CR(CR x, int min_p, BigInteger max_a) {
        op = x;
        min_prec = min_p;
        max_appr = max_a;
        appr_valid = true;
    }
    final int fp_prec = 50;     // Conservative estimate of number of
                                // significant bits in double precision
                                // computation.
    final int fp_op_prec = 60;
    protected BigInteger approximate(int p) {
        int max_op_prec_needed = 2*p - 1;
        int msd = op.iter_msd(max_op_prec_needed);
        if (msd <= max_op_prec_needed) return big0;
        int result_msd = msd/2;                 // +- 1
        int result_digits = result_msd - p;     // +- 2
        if (result_digits > fp_prec) {
          // Compute less precise approximation and use a Newton iter.
            int appr_digits = result_digits/2 + 6;
                // This should be conservative.  Is fewer enough?
            int appr_prec = result_msd - appr_digits;
            int prod_prec = 2*appr_prec;
            // First compute the argument to maximal precision, so we don't end up
            // reevaluating it incrementally.
            BigInteger op_appr = op.get_appr(prod_prec);
            BigInteger last_appr = get_appr(appr_prec);
            // Compute (last_appr * last_appr + op_appr) / last_appr / 2
            // while adjusting the scaling to make everything work
            BigInteger prod_prec_scaled_numerator =
                last_appr.multiply(last_appr).add(op_appr);
            BigInteger scaled_numerator =
                scale(prod_prec_scaled_numerator, appr_prec - p);
            BigInteger shifted_result = scaled_numerator.divide(last_appr);
            return shifted_result.add(big1).shiftRight(1);
        } else {
          // Use a double precision floating point approximation.
            // Make sure all precisions are even
            int op_prec = (msd - fp_op_prec) & ~1;
            int working_prec = op_prec - fp_op_prec;
            BigInteger scaled_bi_appr = op.get_appr(op_prec)
                                        .shiftLeft(fp_op_prec);
            double scaled_appr = scaled_bi_appr.doubleValue();
            if (scaled_appr < 0.0)
                throw new ArithmeticException("sqrt(negative)");
            double scaled_fp_sqrt = Math.sqrt(scaled_appr);
            BigInteger scaled_sqrt = BigInteger.valueOf((long)scaled_fp_sqrt);
            int shift_count = working_prec/2 - p;
            return shift(scaled_sqrt, shift_count);
        }
    }
}

// The constant PI, computed using the Gauss-Legendre alternating
// arithmetic-geometric mean algorithm:
//      a[0] = 1
//      b[0] = 1/sqrt(2)
//      t[0] = 1/4
//      p[0] = 1
//
//      a[n+1] = (a[n] + b[n])/2        (arithmetic mean, between 0.8 and 1)
//      b[n+1] = sqrt(a[n] * b[n])      (geometric mean, between 0.7 and 1)
//      t[n+1] = t[n] - (2^n)(a[n]-a[n+1])^2,  (always between 0.2 and 0.25)
//
//      pi is then approximated as (a[n+1]+b[n+1])^2 / 4*t[n+1].
//
class gl_pi_CR extends slow_CR {
    // In addition to the best approximation kept by the CR base class, we keep
    // the entire sequence b[n], to the extent we've needed it so far.  Each
    // reevaluation leads to slightly different sqrt arguments, but the
    // previous result can be used to avoid repeating low precision Newton
    // iterations for the sqrt approximation.
    ArrayList<Integer> b_prec = new ArrayList<Integer>();
    ArrayList<BigInteger> b_val = new ArrayList<BigInteger>();
    gl_pi_CR() {
        b_prec.add(null);  // Zeroth entry unused.
        b_val.add(null);
    }
    private static BigInteger TOLERANCE = BigInteger.valueOf(4);
    // sqrt(1/2)
    private static CR SQRT_HALF = new sqrt_CR(ONE.shiftRight(1));

    protected BigInteger approximate(int p) {
        // Get us back into a consistent state if the last computation
        // was interrupted after pushing onto b_prec.
        if (b_prec.size() > b_val.size()) {
            b_prec.remove(b_prec.size() - 1);
        }
        // Rough approximations are easy.
        if (p >= 0) return scale(BigInteger.valueOf(3), -p);
        // We need roughly log2(p) iterations.  Each iteration should
        // contribute no more than 2 ulps to the error in the corresponding
        // term (a[n], b[n], or t[n]).  Thus 2log2(n) bits plus a few for the
        // final calulation and rounding suffice.
        final int extra_eval_prec =
                (int)Math.ceil(Math.log(-p) / Math.log(2)) + 10;
        // All our terms are implicitly scaled by eval_prec.
        final int eval_prec = p - extra_eval_prec;
        BigInteger a = BigInteger.ONE.shiftLeft(-eval_prec);
        BigInteger b = SQRT_HALF.get_appr(eval_prec);
        BigInteger t = BigInteger.ONE.shiftLeft(-eval_prec - 2);
        int n = 0;
        while (a.subtract(b).subtract(TOLERANCE).signum() > 0) {
            // Current values correspond to n, next_ values to n + 1
            // b_prec.size() == b_val.size() >= n + 1
            final BigInteger next_a = a.add(b).shiftRight(1);
            final BigInteger next_b;
            final BigInteger a_diff = a.subtract(next_a);
            final BigInteger b_prod = a.multiply(b).shiftRight(-eval_prec);
            // We compute square root approximations using a nested
            // temporary CR computation, to avoid implementing BigInteger
            // square roots separately.
            final CR b_prod_as_CR = CR.valueOf(b_prod).shiftRight(-eval_prec);
            if (b_prec.size() == n + 1) {
                // Add an n+1st slot.
                // Take care to make this exception-safe; b_prec and b_val
                // must remain consistent, even if we are interrupted, or run
                // out of memory. It's OK to just push on b_prec in that case.
                final CR next_b_as_CR = b_prod_as_CR.sqrt();
                next_b = next_b_as_CR.get_appr(eval_prec);
                final BigInteger scaled_next_b = scale(next_b, -extra_eval_prec);
                b_prec.add(p);
                b_val.add(scaled_next_b);
            } else {
                // Reuse previous approximation to reduce sqrt iterations,
                // hopefully to one.
                final CR next_b_as_CR =
                        new sqrt_CR(b_prod_as_CR,
                                    b_prec.get(n + 1), b_val.get(n + 1));
                next_b = next_b_as_CR.get_appr(eval_prec);
                // We assume that set() doesn't throw for any reason.
                b_prec.set(n + 1, p);
                b_val.set(n + 1, scale(next_b, -extra_eval_prec));
            }
            // b_prec.size() == b_val.size() >= n + 2
            final BigInteger next_t =
                    t.subtract(a_diff.multiply(a_diff)
                     .shiftLeft(n + eval_prec));  // shift dist. usually neg.
            a = next_a;
            b = next_b;
            t = next_t;
            ++n;
        }
        final BigInteger sum = a.add(b);
        final BigInteger result = sum.multiply(sum).divide(t).shiftRight(2);
        return scale(result, -extra_eval_prec);
    }
}
