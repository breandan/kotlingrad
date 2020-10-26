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
//
// 5/2014 Added Strings to ArithmeticExceptions.
// 5/2015 Added support for direct asin() implementation in CR.

package com.hp.creals;
// import android.util.Log;

import java.math.BigInteger;

/**
* Unary functions on constructive reals implemented as objects.
* The <TT>execute</tt> member computes the function result.
* Unary function objects on constructive reals inherit from
* <TT>UnaryCRFunction</tt>.
*/
// Naming vaguely follows ObjectSpace JGL convention.
public abstract class UnaryCRFunction {
    abstract public CR execute(CR x);

/**
* The function object corresponding to the identity function.
*/
    public static final UnaryCRFunction identityFunction =
        new identity_UnaryCRFunction();

/**
* The function object corresponding to the <TT>negate</tt> method of CR.
*/
    public static final UnaryCRFunction negateFunction =
        new negate_UnaryCRFunction();

/**
* The function object corresponding to the <TT>inverse</tt> method of CR.
*/
    public static final UnaryCRFunction inverseFunction =
        new inverse_UnaryCRFunction();

/**
* The function object corresponding to the <TT>abs</tt> method of CR.
*/
    public static final UnaryCRFunction absFunction =
        new abs_UnaryCRFunction();

/**
* The function object corresponding to the <TT>exp</tt> method of CR.
*/
    public static final UnaryCRFunction expFunction =
        new exp_UnaryCRFunction();

/**
* The function object corresponding to the <TT>cos</tt> method of CR.
*/
    public static final UnaryCRFunction cosFunction =
        new cos_UnaryCRFunction();

/**
* The function object corresponding to the <TT>sin</tt> method of CR.
*/
    public static final UnaryCRFunction sinFunction =
        new sin_UnaryCRFunction();

/**
* The function object corresponding to the tangent function.
*/
    public static final UnaryCRFunction tanFunction =
        new tan_UnaryCRFunction();

/**
* The function object corresponding to the inverse sine (arcsine) function.
* The argument must be between -1 and 1 inclusive.  The result is between
* -PI/2 and PI/2.
*/
    public static final UnaryCRFunction asinFunction =
        new asin_UnaryCRFunction();
        // The following also works, but is slower:
        // CR half_pi = CR.PI.divide(CR.valueOf(2));
        // UnaryCRFunction.sinFunction.inverseMonotone(half_pi.negate(),
        //                                             half_pi);

/**
* The function object corresponding to the inverse cosine (arccosine) function.
* The argument must be between -1 and 1 inclusive.  The result is between
* 0 and PI.
*/
    public static final UnaryCRFunction acosFunction =
        new acos_UnaryCRFunction();

/**
* The function object corresponding to the inverse cosine (arctangent) function.
* The result is between -PI/2 and PI/2.
*/
    public static final UnaryCRFunction atanFunction =
        new atan_UnaryCRFunction();

/**
* The function object corresponding to the <TT>ln</tt> method of CR.
*/
    public static final UnaryCRFunction lnFunction =
        new ln_UnaryCRFunction();

/**
* The function object corresponding to the <TT>sqrt</tt> method of CR.
*/
    public static final UnaryCRFunction sqrtFunction =
        new sqrt_UnaryCRFunction();

/**
* Compose this function with <TT>f2</tt>.
*/
    public UnaryCRFunction compose(UnaryCRFunction f2) {
        return new compose_UnaryCRFunction(this, f2);
    }

/**
* Compute the inverse of this function, which must be defined
* and strictly monotone on the interval [<TT>low</tt>, <TT>high</tt>].
* The resulting function is defined only on the image of
* [<TT>low</tt>, <TT>high</tt>].
* The original function may be either increasing or decreasing.
*/
    public UnaryCRFunction inverseMonotone(CR low, CR high) {
        return new inverseMonotone_UnaryCRFunction(this, low, high);
    }

/**
* Compute the derivative of a function.
* The function must be defined on the interval [<TT>low</tt>, <TT>high</tt>],
* and the derivative must exist, and must be continuous and
* monotone in the open interval [<TT>low</tt>, <TT>high</tt>].
* The result is defined only in the open interval.
*/
    public UnaryCRFunction monotoneDerivative(CR low, CR high) {
        return new monotoneDerivative_UnaryCRFunction(this, low, high);
    }

}

// Subclasses of UnaryCRFunction for various built-in functions.
class sin_UnaryCRFunction extends UnaryCRFunction {
    public CR execute(CR x) {
        return x.sin();
    }
}

class cos_UnaryCRFunction extends UnaryCRFunction {
    public CR execute(CR x) {
        return x.cos();
    }
}

class tan_UnaryCRFunction extends UnaryCRFunction {
    public CR execute(CR x) {
        return x.sin().divide(x.cos());
    }
}

class asin_UnaryCRFunction extends UnaryCRFunction {
    public CR execute(CR x) {
        return x.asin();
    }
}

class acos_UnaryCRFunction extends UnaryCRFunction {
    public CR execute(CR x) {
        return x.acos();
    }
}

// This uses the identity (sin x)^2 = (tan x)^2/(1 + (tan x)^2)
// Since we know the tangent of the result, we can get its sine,
// and then use the asin function.  Note that we don't always
// want the positive square root when computing the sine.
class atan_UnaryCRFunction extends UnaryCRFunction {
    CR one = CR.valueOf(1);
    public CR execute(CR x) {
        CR x2 = x.multiply(x);
        CR abs_sin_atan = x2.divide(one.add(x2)).sqrt();
        CR sin_atan = x.select(abs_sin_atan.negate(), abs_sin_atan);
        return sin_atan.asin();
    }
}

class exp_UnaryCRFunction extends UnaryCRFunction {
    public CR execute(CR x) {
        return x.exp();
    }
}

class ln_UnaryCRFunction extends UnaryCRFunction {
    public CR execute(CR x) {
        return x.ln();
    }
}

class identity_UnaryCRFunction extends UnaryCRFunction {
    public CR execute(CR x) {
        return x;
    }
}

class negate_UnaryCRFunction extends UnaryCRFunction {
    public CR execute(CR x) {
        return x.negate();
    }
}

class inverse_UnaryCRFunction extends UnaryCRFunction {
    public CR execute(CR x) {
        return x.inverse();
    }
}

class abs_UnaryCRFunction extends UnaryCRFunction {
    public CR execute(CR x) {
        return x.abs();
    }
}

class sqrt_UnaryCRFunction extends UnaryCRFunction {
    public CR execute(CR x) {
        return x.sqrt();
    }
}

class compose_UnaryCRFunction extends UnaryCRFunction {
    UnaryCRFunction f1;
    UnaryCRFunction f2;
    compose_UnaryCRFunction(UnaryCRFunction func1,
                            UnaryCRFunction func2) {
        f1 = func1; f2 = func2;
    }
    public CR execute(CR x) {
        return f1.execute(f2.execute(x));
    }
}

class inverseMonotone_UnaryCRFunction extends UnaryCRFunction {
  // The following variables are final, so that they
  // can be referenced from the inner class inverseIncreasingCR.
  // I couldn't find a way to initialize these such that the
  // compiler accepted them as final without turning them into arrays.
    final UnaryCRFunction f[] = new UnaryCRFunction[1];
                                // Monotone increasing.
                                // If it was monotone decreasing, we
                                // negate it.
    final boolean f_negated[] = new boolean[1];
    final CR low[] = new CR[1];
    final CR high[] = new CR[1];
    final CR f_low[] = new CR[1];
    final CR f_high[] = new CR[1];
    final int max_msd[] = new int[1];
                        // Bound on msd of both f(high) and f(low)
    final int max_arg_prec[] = new int[1];
                                // base**max_arg_prec is a small fraction
                                // of low - high.
    final int deriv_msd[] = new int[1];
                                // Rough approx. of msd of first
                                // derivative.
    final static BigInteger BIG1023 = BigInteger.valueOf(1023);
    static final boolean ENABLE_TRACE = false;  // Change to generate trace
    static void trace(String s) {
        if (ENABLE_TRACE) {
            System.out.println(s);
            // Change to Log.v("UnaryCRFunction", s); for Android use.
        }
    }
    inverseMonotone_UnaryCRFunction(UnaryCRFunction func, CR l, CR h) {
        low[0] = l; high[0] = h;
        CR tmp_f_low = func.execute(l);
        CR tmp_f_high = func.execute(h);
        // Since func is monotone and low < high, the following test
        // converges.
        if (tmp_f_low.compareTo(tmp_f_high) > 0) {
            f[0] = UnaryCRFunction.negateFunction.compose(func);
            f_negated[0] = true;
            f_low[0] = tmp_f_low.negate();
            f_high[0] = tmp_f_high.negate();
        } else {
            f[0] = func;
            f_negated[0] = false;
            f_low[0] = tmp_f_low;
            f_high[0] = tmp_f_high;
        }
        max_msd[0] = low[0].abs().max(high[0].abs()).msd();
        max_arg_prec[0] = high[0].subtract(low[0]).msd() - 4;
        deriv_msd[0] = f_high[0].subtract(f_low[0])
                    .divide(high[0].subtract(low[0])).msd();
    }
    class inverseIncreasingCR extends CR {
        final CR arg;
        inverseIncreasingCR(CR x) {
            arg = f_negated[0]? x.negate() : x;
        }
        // Comparison with a difference of one treated as equality.
        int sloppy_compare(BigInteger x, BigInteger y) {
            BigInteger difference = x.subtract(y);
            if (difference.compareTo(big1) > 0) {
                return 1;
            }
            if (difference.compareTo(bigm1) < 0) {
                return -1;
            }
            return 0;
        }
        protected BigInteger approximate(int p) {
            final int extra_arg_prec = 4;
            final UnaryCRFunction fn = f[0];
            int small_step_deficit = 0; // Number of ineffective steps not
                                        // yet compensated for by a binary
                                        // search step.
            int digits_needed = max_msd[0] - p;
            if (digits_needed < 0) return big0;
            int working_arg_prec = p - extra_arg_prec;
            if (working_arg_prec > max_arg_prec[0]) {
                working_arg_prec = max_arg_prec[0];
            }
            int working_eval_prec = working_arg_prec + deriv_msd[0] - 20;
                        // initial guess
            // We use a combination of binary search and something like
            // the secant method.  This always converges linearly,
            // and should converge quadratically under favorable assumptions.
            // F_l and f_h are always the approximate images of l and h.
            // At any point, arg is between f_l and f_h, or no more than
            // one outside [f_l, f_h].
            // L and h are implicitly scaled by working_arg_prec.
            // The scaled values of l and h are strictly between low and high.
            // If at_left is true, then l is logically at the left
            // end of the interval.  We approximate this by setting l to
            // a point slightly inside the interval, and letting f_l
            // approximate the function value at the endpoint.
            // If at_right is true, r and f_r are set correspondingly.
            // At the endpoints of the interval, f_l and f_h may correspond
            // to the endpoints, even if l and h are slightly inside.
            // F_l and f_u are scaled by working_eval_prec.
            // Working_eval_prec may need to be adjusted depending
            // on the derivative of f.
            boolean at_left, at_right;
            BigInteger l, f_l;
            BigInteger h, f_h;
            BigInteger low_appr = low[0].get_appr(working_arg_prec)
                                        .add(big1);
            BigInteger high_appr = high[0].get_appr(working_arg_prec)
                                          .subtract(big1);
            BigInteger arg_appr = arg.get_appr(working_eval_prec);
            boolean have_good_appr = (appr_valid && min_prec < max_msd[0]);
            if (digits_needed < 30 && !have_good_appr) {
                trace("Setting interval to entire domain");
                h = high_appr;
                f_h = f_high[0].get_appr(working_eval_prec);
                l = low_appr;
                f_l = f_low[0].get_appr(working_eval_prec);
                // Check for clear out-of-bounds case.
                // Close cases may fail in other ways.
                  if (f_h.compareTo(arg_appr.subtract(big1)) < 0
                    || f_l.compareTo(arg_appr.add(big1)) > 0) {
                    throw new ArithmeticException("inverse(out-of-bounds)");
                  }
                at_left = true;
                at_right = true;
                small_step_deficit = 2;        // Start with bin search steps.
            } else {
                int rough_prec = p + digits_needed/2;

                if (have_good_appr &&
                    (digits_needed < 30 || min_prec < p + 3*digits_needed/4)) {
                    rough_prec = min_prec;
                }
                BigInteger rough_appr = get_appr(rough_prec);
                trace("Setting interval based on prev. appr");
                trace("prev. prec = " + rough_prec + " appr = " + rough_appr);
                h = rough_appr.add(big1)
                              .shiftLeft(rough_prec - working_arg_prec);
                l = rough_appr.subtract(big1)
                              .shiftLeft(rough_prec - working_arg_prec);
                if (h.compareTo(high_appr) > 0)  {
                    h = high_appr;
                    f_h = f_high[0].get_appr(working_eval_prec);
                    at_right = true;
                } else {
                    CR h_cr = valueOf(h).shiftLeft(working_arg_prec);
                    f_h = fn.execute(h_cr).get_appr(working_eval_prec);
                    at_right = false;
                }
                if (l.compareTo(low_appr) < 0) {
                    l = low_appr;
                    f_l = f_low[0].get_appr(working_eval_prec);
                    at_left = true;
                } else {
                    CR l_cr = valueOf(l).shiftLeft(working_arg_prec);
                    f_l = fn.execute(l_cr).get_appr(working_eval_prec);
                    at_left = false;
                }
            }
            BigInteger difference = h.subtract(l);
            for(int i = 0;; ++i) {
                if (Thread.interrupted() || please_stop)
                    throw new AbortedException();
                trace("***Iteration: " + i);
                trace("Arg prec = " + working_arg_prec
                      + " eval prec = " + working_eval_prec
                      + " arg appr. = " + arg_appr);
                trace("l = " + l); trace("h = " + h);
                trace("f(l) = " + f_l); trace("f(h) = " + f_h);
                if (difference.compareTo(big6) < 0) {
                    // Answer is less than 1/2 ulp away from h.
                    return scale(h, -extra_arg_prec);
                }
                BigInteger f_difference = f_h.subtract(f_l);
                // Narrow the interval by dividing at a cleverly
                // chosen point (guess) in the middle.
                {
                    BigInteger guess;
                    boolean binary_step =
                        (small_step_deficit > 0 || f_difference.signum() == 0);
                    if (binary_step) {
                        // Do a binary search step to guarantee linear
                        // convergence.
                        trace("binary step");
                        guess = l.add(h).shiftRight(1);
                        --small_step_deficit;
                    } else {
                      // interpolate.
                      // f_difference is nonzero here.
                      trace("interpolating");
                      BigInteger arg_difference = arg_appr.subtract(f_l);
                      BigInteger t = arg_difference.multiply(difference);
                      BigInteger adj = t.divide(f_difference);
                          // tentative adjustment to l to compute guess
                      // If we are within 1/1024 of either end, back off.
                      // This greatly improves the odds of bounding
                      // the answer within the smaller interval.
                      // Note that interpolation will often get us
                      // MUCH closer than this.
                      if (adj.compareTo(difference.shiftRight(10)) < 0) {
                        adj = adj.shiftLeft(8);
                        trace("adjusting left");
                      } else if (adj.compareTo(difference.multiply(BIG1023)
                                                       .shiftRight(10)) > 0){
                        adj = difference.subtract(difference.subtract(adj)
                                                  .shiftLeft(8));
                        trace("adjusting right");
                      }
                      if (adj.signum() <= 0)
                          adj = big2;
                      if (adj.compareTo(difference) >= 0)
                          adj = difference.subtract(big2);
                      guess = (adj.signum() <= 0? l.add(big2) : l.add(adj));
                    }
                    int outcome;
                    BigInteger tweak = big2;
                    BigInteger f_guess;
                    for(boolean adj_prec = false;; adj_prec = !adj_prec) {
                        CR guess_cr = valueOf(guess)
                                        .shiftLeft(working_arg_prec);
                        trace("Evaluating at " + guess_cr
                              + " with precision " + working_eval_prec);
                        CR f_guess_cr = fn.execute(guess_cr);
                        trace("fn value = " + f_guess_cr);
                        f_guess = f_guess_cr.get_appr(working_eval_prec);
                        outcome = sloppy_compare(f_guess, arg_appr);
                        if (outcome != 0) break;
                        // Alternately increase evaluation precision
                        // and adjust guess slightly.
                        // This should be an unlikely case.
                        if (adj_prec) {
                            // adjust working_eval_prec to get enough
                            // resolution.
                            int adjustment = -f_guess.bitLength()/4;
                            if (adjustment > -20) adjustment = - 20;
                            CR l_cr = valueOf(l)
                                        .shiftLeft(working_arg_prec);
                            CR h_cr = valueOf(h)
                                        .shiftLeft(working_arg_prec);
                            working_eval_prec += adjustment;
                            trace("New eval prec = " + working_eval_prec
                                  + (at_left? "(at left)" : "")
                                  + (at_right? "(at right)" : ""));
                            if (at_left) {
                                f_l = f_low[0].get_appr(working_eval_prec);
                            } else {
                                f_l = fn.execute(l_cr)
                                        .get_appr(working_eval_prec);
                            }
                            if (at_right) {
                                f_h = f_high[0].get_appr(working_eval_prec);
                            } else {
                                f_h = fn.execute(h_cr)
                                        .get_appr(working_eval_prec);
                            }
                            arg_appr = arg.get_appr(working_eval_prec);
                        } else {
                            // guess might be exactly right; tweak it
                            // slightly.
                            trace("tweaking guess");
                            BigInteger new_guess = guess.add(tweak);
                            if (new_guess.compareTo(h) >= 0) {
                                guess = guess.subtract(tweak);
                            } else {
                                guess = new_guess;
                            }
                            // If we keep hitting the right answer, it's
                            // important to alternate which side we move it
                            // to, so that the interval shrinks rapidly.
                            tweak = tweak.negate();
                        }
                    }
                    if (outcome > 0) {
                        h = guess;
                        f_h = f_guess;
                        at_right = false;
                    } else {
                        l = guess;
                        f_l = f_guess;
                        at_left = false;
                    }
                    BigInteger new_difference = h.subtract(l);
                    if (!binary_step) {
                        if (new_difference.compareTo(difference
                                                     .shiftRight(1)) >= 0) {
                            ++small_step_deficit;
                        } else {
                            --small_step_deficit;
                        }
                    }
                    difference = new_difference;
                }
            }
        }
    }
    public CR execute(CR x) {
        return new inverseIncreasingCR(x);
    }
}

class monotoneDerivative_UnaryCRFunction extends UnaryCRFunction {
  // The following variables are final, so that they
  // can be referenced from the inner class inverseIncreasingCR.
    final UnaryCRFunction f[] = new UnaryCRFunction[1];
                                // Monotone increasing.
                                // If it was monotone decreasing, we
                                // negate it.
    final CR low[] = new CR[1]; // endpoints and mispoint of interval
    final CR mid[] = new CR[1];
    final CR high[] = new CR[1];
    final CR f_low[] = new CR[1]; // Corresponding function values.
    final CR f_mid[] = new CR[1];
    final CR f_high[] = new CR[1];
    final int difference_msd[] = new int[1];  // msd of interval len.
    final int deriv2_msd[] = new int[1];
                                // Rough approx. of msd of second
                                // derivative.
                                // This is increased to be an appr. bound
                                // on the msd of |(f'(y)-f'(x))/(x-y)|
                                // for any pair of points x and y
                                // we have considered.
                                // It may be better to keep a copy per
                                // derivative value.

    monotoneDerivative_UnaryCRFunction(UnaryCRFunction func, CR l, CR h) {
        f[0] = func;
        low[0] = l; high[0] = h;
        mid[0] = l.add(h).shiftRight(1);
        f_low[0] = func.execute(l);
        f_mid[0] = func.execute(mid[0]);
        f_high[0] = func.execute(h);
        CR difference = h.subtract(l);
        // compute approximate msd of
        // ((f_high - f_mid) - (f_mid - f_low))/(high - low)
        // This should be a very rough appr to the second derivative.
        // We add a little slop to err on the high side, since
        // a low estimate will cause extra iterations.
        CR appr_diff2 = f_high[0].subtract(f_mid[0].shiftLeft(1)).add(f_low[0]);
        difference_msd[0] = difference.msd();
        deriv2_msd[0] = appr_diff2.msd() - difference_msd[0] + 4;
    }
    class monotoneDerivativeCR extends CR {
        CR arg;
        CR f_arg;
        int max_delta_msd;
        monotoneDerivativeCR(CR x) {
            arg = x;
            f_arg = f[0].execute(x);
            // The following must converge, since arg must be in the
            // open interval.
            CR left_diff = arg.subtract(low[0]);
            int max_delta_left_msd = left_diff.msd();
            CR right_diff = high[0].subtract(arg);
            int max_delta_right_msd = right_diff.msd();
            if (left_diff.signum() < 0 || right_diff.signum() < 0) {
                throw new ArithmeticException("fn not monotone");
            }
            max_delta_msd = (max_delta_left_msd < max_delta_right_msd?
                                max_delta_left_msd
                                : max_delta_right_msd);
        }
        protected BigInteger approximate(int p) {
            final int extra_prec = 4;
            int log_delta = p - deriv2_msd[0];
            // Ensure that we stay within the interval.
              if (log_delta > max_delta_msd) log_delta = max_delta_msd;
            log_delta -= extra_prec;
            CR delta = ONE.shiftLeft(log_delta);

            CR left = arg.subtract(delta);
            CR right = arg.add(delta);
            CR f_left = f[0].execute(left);
            CR f_right = f[0].execute(right);
            CR left_deriv = f_arg.subtract(f_left).shiftRight(log_delta);
            CR right_deriv = f_right.subtract(f_arg).shiftRight(log_delta);
            int eval_prec = p - extra_prec;
            BigInteger appr_left_deriv = left_deriv.get_appr(eval_prec);
            BigInteger appr_right_deriv = right_deriv.get_appr(eval_prec);
            BigInteger deriv_difference =
                appr_right_deriv.subtract(appr_left_deriv).abs();
            if (deriv_difference.compareTo(big8) < 0) {
                return scale(appr_left_deriv, -extra_prec);
            } else {
                if (Thread.interrupted() || please_stop)
                    throw new AbortedException();
                deriv2_msd[0] =
                        eval_prec + deriv_difference.bitLength() + 4/*slop*/;
                deriv2_msd[0] -= log_delta;
                return approximate(p);
            }
        }
    }
    public CR execute(CR x) {
        return new monotoneDerivativeCR(x);
    }
}
