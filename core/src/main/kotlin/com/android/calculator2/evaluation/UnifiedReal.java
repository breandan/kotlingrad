/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.calculator2.evaluation;

import com.android.calculator2.StringUtils;
import com.hp.creals.CR;
import com.hp.creals.UnaryCRFunction;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Computable real numbers, represented so that we can get exact decidable comparisons for a number
 * of interesting special cases, including rational computations.
 *
 * <p>A real number is represented as the product of two numbers with different representations:
 * A) A BoundedRational that can only represent a subset of the rationals, but supports exact
 * computable comparisons. B) A lazily evaluated "constructive real number" that provides
 * operations to evaluate itself to any requested number of digits. Whenever possible, we choose
 * (B) to be such that we can describe its exact symbolic value using a CRProperty. For example,
 * whenever we can, we represent rationals such that (B) is 1. This scheme allows us to do some
 * very limited symbolic computation on numbers when both have the same (B) value, as well as in
 * some other situations. We try to maximize that possibility.
 *
 * UnifiedReals, as well as their three components (the rational part, the constructive real part,
 * and the property describing the constructive real part) are logically immutable. (The CR
 * component physically contains an evaluation cache, which is transparently mutated.)
 *
 * <p>Arithmetic operations and operations that produce finite approximations may throw unchecked
 * exceptions produced by the underlying CR and BoundedRational packages, including
 * CR.PrecisionOverflowException and CR.AbortedException.
 *
 * Note on approach: This API assumes that we cannot effectively decide whether a "real number" is
 * exactly zero. That assumption is easily proven correct if we allow real numbers to be defined by
 * an arbitrary approximating function, as we do here by allowing an arbitrary CR to be converted to
 * a UnifiedReal. However Richardson and Fitch show in their ISAAC '94 paper that this problem is
 * decidable (unless the decision procedure disproves Shanuel's conjecture) for numbers that are
 * defined by a system of polynomial and exponential equations over the complexes. This effectively
 * covers all values that can be computed by composing the arithmetic and transcendental functions
 * provided here. However we know of no attempts to turn that decision procedure into
 * a practical algorithm. Since that algorithm currently tries to prove inequality essentially by
 * evaluating a constructive real to increasing precision, it is currently unsuited
 * to e.g. determining that pi^e + e^-e^e^e^100 is not equal to pi^e within a
 * practical amount of time or memory. But a more practical algorithm may exist.
 */
public class UnifiedReal {
    // Default comparison tolerances, in bits
    private static final int DEFAULT_INITIAL_TOLERANCE = -100;  // For rough comparison.
    private static final int DEFAULT_RELATIVE_TOLERANCE = -1000;  // Used only in isComparable.
    private static final int DEFAULT_COMPARISON_TOLERANCE = -3500; // Absolute tolerance.
    // Roughly the number of leading zeroes we're willing to accept in comparisons.
    private static final int ZERO_COMPARISON_TOLERANCE = -5000; // Absolute tolerance.

    // Other helpful constants
    private static final BigInteger BIG_TWO = BigInteger.valueOf(2);
    private static final BigInteger BIG_THREE = BigInteger.valueOf(3);
    private static final BigInteger BIG_FIVE = BigInteger.valueOf(5);
    private static final BigInteger BIG_24 = BigInteger.valueOf(24);
    private static final BigInteger BIG_180 = BigInteger.valueOf(180);
    private static final BigInteger BIG_MINUS_ONE = BigInteger.valueOf(-1);
    private static final CR CR_TWO = CR.valueOf(2);
    private static final CR CR_THREE = CR.valueOf(3);
    private static final CR CR_TEN = CR.valueOf(10);
    private static final CR CR_LN10 = CR_TEN.ln();
    private static final BoundedRational BR3_2 = new BoundedRational(3, 2);
    private static final BoundedRational BR180 = new BoundedRational(BIG_180);

    // Well-known CR constants we try to use in the crFactor position:
    private static final CR CR_ONE = CR.ONE;
    private static final CR CR_PI = CR.PI;
    private static final CR CR_E = CR.ONE.exp();
    private static final CR CR_SQRT2 = CR_TWO.sqrt();
    private static final CR CR_SQRT3 = CR_THREE.sqrt();

    // Some convenient UnifiedReal constants for our clients.
    public static final UnifiedReal PI = new UnifiedReal(CR_PI);
    public static final UnifiedReal E = new UnifiedReal(CR_E);
    public static final UnifiedReal ZERO = new UnifiedReal(BoundedRational.ZERO);
    public static final UnifiedReal ONE = new UnifiedReal(BoundedRational.ONE);
    public static final UnifiedReal MINUS_ONE = new UnifiedReal(BoundedRational.MINUS_ONE);
    public static final UnifiedReal TWO = new UnifiedReal(BoundedRational.TWO);
    public static final UnifiedReal HALF = new UnifiedReal(BoundedRational.HALF);
    public static final UnifiedReal MINUS_HALF = new UnifiedReal(BoundedRational.MINUS_HALF);
    public static final UnifiedReal TEN = new UnifiedReal(BoundedRational.TEN);
    public static final UnifiedReal RADIANS_PER_DEGREE = new UnifiedReal(
            BoundedRational.inverse(BR180), CR_PI);
    public static final UnifiedReal LN10 = new UnifiedReal(CR_LN10);

    // Some more that we use internally.
    private static final UnifiedReal HALF_SQRT2 = new UnifiedReal(BoundedRational.HALF, CR_SQRT2);
    private static final UnifiedReal SQRT3 = new UnifiedReal(CR_SQRT3);
    private static final UnifiedReal HALF_SQRT3 = new UnifiedReal(BoundedRational.HALF, CR_SQRT3);
    private static final UnifiedReal THIRD_SQRT3 = new UnifiedReal(BoundedRational.THIRD, CR_SQRT3);
    private static final UnifiedReal PI_OVER_2 = new UnifiedReal(BoundedRational.HALF, CR_PI);
    private static final UnifiedReal PI_OVER_3 = new UnifiedReal(BoundedRational.THIRD, CR_PI);
    private static final UnifiedReal PI_OVER_4 = new UnifiedReal(BoundedRational.QUARTER, CR_PI);
    private static final UnifiedReal PI_OVER_6 = new UnifiedReal(BoundedRational.SIXTH, CR_PI);

    // Kinds of properties we associated with constructive reals.
    // Revisit definitelyIndependent() below if anything is added here.
    // We simplify/normalize where easily possible. E.g. we always represent exp(0) as 1,
    // and ln(1/2) as -ln(2). That is reflected in constraints on the arguments below.
    // For everything other than IS_ONE, we disallow arguments such that the property
    // would describe a rational value.

    @Retention(RetentionPolicy.SOURCE)
    // @IntDef({IS_ONE, IS_PI, IS_SQRT, IS_EXP, IS_LN, IS_LOG, IS_SIN_PI, IS_TAN_PI, IS_ASIN, IS_ATAN,
    //         IS_IRRATIONAL})
    public @interface PropertyKind {}

    // CR is 1.
    private static final byte IS_ONE = 1;

    // CR is pi.
    private static final byte IS_PI = 2;

    // CR is sqrt(<rational>). Arg is > 0 and not 1.
    // The argument is minimal (doesn't contain square factors > 1)
    // if num and den < EXTRACT_SQUARE_MAX_OPT.
    // It is not an exact square.
    private static final byte IS_SQRT = 3;

    // CR is exp(<rational>). Arg is not 0.
    private static final byte IS_EXP = 4;

    // CR is ln(<rational>). Arg is > 1.
    private static final byte IS_LN = 5;

    // CR is log10(<rational>). Arg is > 1, and not a power of 10.
    // Note that if log(a/b) equals a positive rational c/d then a/b = 10^(c/d),
    // or a^d / b^d = 10^c. Assuming that a/b is in lowest terms, b must be 1, and
    // a most be the nth root or nth power of 10. There are no integral roots of 10,
    // so we must be in the latter case. Thus if arg is not a power of 10 log10(arg)
    // is irrational.
    private static final byte IS_LOG = 6;

    // We explicitly represent sin and tan where the argument is a rational multiple of pi.
    // This is the most common case, and allows arguments to be easily reeduced, enabling
    // further symbolic reductions. We currently also use symbolic representations for the
    // radian (i.e. result not divided by pi) versions of arcsin and arctan.
    // This combination allows asin(sin(pi x) to be symbolically simplified, but not a
    // sin(asin(...) composition in degree mode. It's not clear this matters in practice.

    // CR is sin(pi*<rational>)
    // The rational argument is strictly between 0 and 0.5.
    // The argument is not equal to 1/6, 1/4, or 1/3. By Niven's theorem
    // the result is always irrational.
    private static final byte IS_SIN_PI = 7;

    // CR is tan(pi*<rational>)
    // The rational argument is strictly between 0 and 0.5
    // The argument is not equal to zero or 1/6, 1/4, or 1/3.
    // The result is always irrational.
    private static final byte IS_TAN_PI = 8;

    // CR is asin(<rational>)
    // The rational argument is strictly between 0 and 1, and not equal to 0.5.
    // Always irrational.
    private static final byte IS_ASIN = 9;

    // CR is atan(<rational>)
    // The rational argument is > 0 and not equal to 1.
    // Always irrational. (Laczkovich's proof's proof that pi is irrational.)
    private static final byte IS_ATAN = 10;

    // CR is irrational, but we otherwise know nothing about it.
    private static final byte IS_IRRATIONAL = 11;

    /**
     * Properties associated with certain constructive reals.
     */
    private static class CRProperty {
        @PropertyKind
        final byte kind;  // One of the above property kinds.
        final BoundedRational arg;  // Reduced rational argument, if any.

        private static void checkArg(byte kind, BoundedRational arg) {
            switch(kind) {
                case IS_ONE:
                case IS_PI:
                case IS_IRRATIONAL:
                    check(arg == null);
                    break;
                case IS_SQRT:
                    check(arg.signum() > 0 && arg.compareToOne() != 0);
                    break;
                case IS_EXP:
                    check(arg.signum() != 0);
                    break;
                case IS_LOG:
                    check(arg.compareToOne() > 0 && intLog10(arg) == 0 /* Not power of 10 */);
                    break;
                case IS_LN:
                    check(arg.compareToOne() > 0);
                    break;
                case IS_SIN_PI:
                case IS_TAN_PI:
                    check(!canTrigBeReduced(arg));
                    break;
                case IS_ASIN:
                    check(arg.compareTo(BoundedRational.MINUS_ONE) > 0);
                    check(arg.compareTo(BoundedRational.ONE) < 0);
                    check(arg.compareTo(BoundedRational.MINUS_HALF) != 0);
                    check(arg.compareTo(BoundedRational.HALF) != 0);
                    check(arg.signum() != 0);
                    break;
                case IS_ATAN:
                    check(arg.compareTo(BoundedRational.MINUS_ONE) != 0);
                    check(arg.compareTo(BoundedRational.ONE) != 0);
                    check(arg.signum() != 0);
                    break;
                default:
                    throw new AssertionError("bad kind");
            }
        }

        private CRProperty(byte kind, BoundedRational arg) {
            checkArg(kind, arg);
            this.kind = kind;
            this.arg = arg;
        };

        // The only instances of IS_ONE, IS_PI, and IS_IRRATIONAL properties.
        static final CRProperty ONE = new CRProperty(IS_ONE, null);
        static final CRProperty PI = new CRProperty(IS_PI, null);
        static final CRProperty IRRATIONAL = new CRProperty(IS_IRRATIONAL, null);

        static final CRProperty SQRT2 = new CRProperty(IS_SQRT, BoundedRational.TWO);
        static final CRProperty SQRT3 = new CRProperty(IS_SQRT, BoundedRational.THREE);
        static final CRProperty E = new CRProperty(IS_EXP, BoundedRational.ONE);
        static final CRProperty LN10 = new CRProperty(IS_LN, BoundedRational.TEN);

        // Does this property uniquely determine the number?
        boolean determinesCr() {
            return kind != IS_IRRATIONAL;  // No other such properties yet.
        }

        @Override
        public int hashCode() {
            throw new AssertionError("CRProperty hashCode used");
        }

        @Override
        public boolean equals(Object r) {
            if (r == this) {
                return true;
            } else if (kind == IS_ONE /* optimization, there is only one such object */
                    || r == null || !(r instanceof CRProperty)) {
                return false;
            }
            final CRProperty other = (CRProperty) r;
            return kind == other.kind && arg.equals(other.arg);
        }
    }

    /**
     * Is the argument such that trig_func(pi*arg) can be simplified, or which should have
     * been reduced to the (0, 1/2) interval.
     */
    private static boolean canTrigBeReduced(BoundedRational arg) {

        return arg.signum() <= 0
                || arg.compareTo(BoundedRational.HALF) >= 0
                || arg.compareTo(BoundedRational.THIRD) == 0
                || arg.compareTo(BoundedRational.QUARTER) == 0
                || arg.compareTo(BoundedRational.SIXTH) == 0;
    }

    /**
     * Reduce a SIN_PI or TAN_PI argument to the interval [-1/2, 1.5).
     * Preserves null-ness. May produce null if BoundedRational arithmetic overflows.
     */
    @Nullable
    private static BoundedRational reducedArg(@Nullable BoundedRational arg) {
        if (arg == null) {
            return null;
        }
        // Avoid real computations if we can. Only needed for performance.
        if (arg.compareTo(BoundedRational.MINUS_HALF) >= 0 && arg.compareTo(BR3_2) < 0) {
            return arg;
        }
        final BoundedRational argPlusHalf = BoundedRational.add(arg, BoundedRational.HALF);
        if (argPlusHalf == null) {
            return null;
        }
        final BigInteger argPHFloor = argPlusHalf.floor();
        final BigInteger resultOffset = argPHFloor.andNot(BigInteger.ONE);
        return BoundedRational.subtract(arg, new BoundedRational(resultOffset));
    }

    private static CRProperty makeProperty(@PropertyKind byte kind, BoundedRational arg) {
        // This enforces requirements on arg.
        if (kind == IS_ONE || (kind == IS_SQRT && arg.equals(BoundedRational.ONE))
                || (kind == IS_EXP && arg.signum() == 0)) {
            return CRProperty.ONE;
        } else if (kind == IS_PI) {
            return CRProperty.PI;
        } else if (kind == IS_IRRATIONAL) {
            return CRProperty.IRRATIONAL;
        } else {
            return new CRProperty(kind, arg.reduce());
        }
    }

    /*
     * Pair returned by trig normalization routines.
     */
    private static class SignedProperty {
        SignedProperty(CRProperty p, boolean sign) {
            property = p;
            negative = sign;
        }
        final CRProperty property;
        final boolean negative;
    }

    /**
     * Return a property corresponding to sin(pi*arg), normalizing arg to the correct range. Caller is
     * responsible for ensuring that the argument is not one that leads to a rational result.
     * The negate field of the result is set if the property corresponds to the negated argument,
     * rather than the argument itself.
     * Returns null if we can't normalize the argument.
     */
    @Nullable
    private static SignedProperty makeSinPiProperty(BoundedRational arg) {
        BoundedRational nArg = reducedArg(arg);
        if (nArg != null) {
            boolean neg = false;
            if (nArg.compareTo(BoundedRational.HALF) >= 0) {
                // sin(x) = sin(pi - x)
                nArg = BoundedRational.subtract(BoundedRational.ONE, nArg);
            }
            if (nArg != null && nArg.signum() < 0) {
                nArg = BoundedRational.negate(nArg);
                neg = true;
            }
            if (nArg != null) {
                return new SignedProperty(makeProperty(IS_SIN_PI, nArg), neg);
            }
        }
        return null;
    }

    /**
     * Return a property corresponding to tan(pi*arg), normalizing arg to the correct range. Caller is
     * responsible for ensuring that the argument is not one that leads to a rational result.
     * The negate field of the result is set if the property corresponds to the negated argument,
     * rather than the argument itself.
     * Returns null if we can't normalize the argument.
     */
    @Nullable
    private static SignedProperty makeTanPiProperty(BoundedRational arg) {
        BoundedRational nArg = reducedArg(arg);
        if (nArg != null) {
            boolean neg = false;
            if (nArg.compareTo(BoundedRational.HALF) >= 0) {
                // tan(x) = tan(x - pi)
                nArg = BoundedRational.subtract(nArg, BoundedRational.ONE);
            }
            if (nArg != null && nArg.signum() < 0) {
                nArg = BoundedRational.negate(nArg);
                neg = true;
            }
            if (nArg != null) {
                return new SignedProperty(makeProperty(IS_TAN_PI, nArg), neg);
            }
        }
        return null;
    }

    private static boolean isOne(CRProperty p) {
        return p != null && p.kind == IS_ONE;
    }
    private static boolean isPi(CRProperty p) {
        return p != null && p.kind == IS_PI;
    }
    private static boolean isUnknownIrrational(CRProperty p) {
        return p != null && p.kind == IS_IRRATIONAL;
    }

    // Does p guarantee that the described constructive real is nonzero?
    private static boolean isNonzero(CRProperty p) {
        if (p == null) {
            return false;
        }
        switch (p.kind) {
            case IS_ONE:
            case IS_PI:
            case IS_IRRATIONAL:
                return true;
            case IS_EXP:
                // It would be correct to always answer true. But we intentionally fail to provide the
                // guarantee for large negative arguments, since it would be expensive to actually
                // distinguish the value from zero, and answering true often results in such an attempt.
                return p.arg.compareTo(new BoundedRational(-10000)) >= 0;
            case IS_LN:
            case IS_LOG:
                // arg > 1
                return true;
            case IS_SQRT:
                // arg > 0
                return true;
            case IS_SIN_PI:
            case IS_TAN_PI:
            case IS_ASIN:
            case IS_ATAN:
                // arg != 0
                return true;
            default:
                throw new AssertionError("isNonzero");
        }
    }

    // Return the constructive real described by the property.  We could instead omit storing the CR
    // value when we have a sufficiently descriptive property.  But that might hurt performance
    // slightly, since we would sometimes lose the benefit of prior argument evaluations.
    private static CR crFromProperty(CRProperty p) {
        if (p == null) {
            return null;
        }
        switch (p.kind) {
            case IS_IRRATIONAL:
                return null;
            case IS_ONE:
                return CR_ONE;
            case IS_PI:
                return CR_PI;
            case IS_EXP:
                return p.arg.crValue().exp();
            case IS_LN:
                return p.arg.crValue().ln();
            case IS_LOG:
                return p.arg.crValue().ln().divide(CR_LN10);
            case IS_SQRT:
                return p.arg.crValue().sqrt();
            case IS_SIN_PI:
                return p.arg.crValue().multiply(CR_PI).sin();
            case IS_TAN_PI:
                return UnaryCRFunction.tanFunction.execute(p.arg.crValue().multiply(CR_PI));
            case IS_ASIN:
                return p.arg.crValue().asin();
            case IS_ATAN:
                return UnaryCRFunction.atanFunction.execute(p.arg.crValue());
            default:
                throw new AssertionError("crFromProperty");
        }
    }

    // Try to compute a property describing the constructive resl.
    // Recognizes only a few specific constants.
    @SuppressWarnings("ReferenceEquality")
    @Nullable
    private static CRProperty propertyFor(@NonNull CR cr) {
        if (cr == CR_ONE) {
            return CRProperty.ONE;
        } else if (cr == CR_PI) {
            return CRProperty.PI;
        } else if (cr == CR_SQRT2) {
            return CRProperty.SQRT2;
        } else if (cr == CR_SQRT3) {
            return CRProperty.SQRT3;
        } else if (cr == CR_E) {
            return CRProperty.E;
        } else if (cr == CR_LN10) {
            return CRProperty.LN10;
        } else {
            return null;
        }
    }

    /**
     * Check that if crProperty uniquely defines a constructive real, then crProperty
     * and crFactor both describe approximately the same number.
     */
    public boolean propertyCorrect(int prec) {
        final CR propertyCr = crFromProperty(crProperty);
        if (propertyCr == null) {
            return true;
        } else {
            int bound = msbBound(crProperty);
            if (bound != Integer.MIN_VALUE
                    && propertyCr.abs().compareTo(CR.ONE.shiftLeft(bound), prec) < 0) {
                // msbBound produced incorrect result.
                return false;
            }
            return crFactor.compareTo(propertyCr, prec) == 0;
        }
    }

    // Return the argument if the property p has the given kind.
    @Nullable
    private static BoundedRational getArgForKind(@Nullable CRProperty p, @PropertyKind byte kind) {
        if (p == null || p.kind != kind) {
            return null;
        }
        return p.arg;
    }

    // The following return the rational argument if the property describes an application of
    // the matching function, null if not.
    @Nullable
    private static BoundedRational getSqrtArg(@Nullable CRProperty p) {
        return getArgForKind(p, IS_SQRT);
    }

    @Nullable
    private static BoundedRational getExpArg(@Nullable CRProperty p) {
        return getArgForKind(p, IS_EXP);
    }

    @Nullable
    private static BoundedRational getLnArg(@Nullable CRProperty p) {
        return getArgForKind(p, IS_LN);
    }

    @Nullable
    private static BoundedRational getLogArg(@Nullable CRProperty p) {
        return getArgForKind(p, IS_LOG);
    }

    @Nullable
    private static BoundedRational getSinPiArg(@Nullable CRProperty p) {
        return getArgForKind(p, IS_SIN_PI);
    }

    @Nullable
    private static BoundedRational getTanPiArg(@Nullable CRProperty p) {
        return getArgForKind(p, IS_TAN_PI);
    }

    @Nullable
    private static BoundedRational getASinArg(@Nullable CRProperty p) {
        return getArgForKind(p, IS_ASIN);
    }

    @Nullable
    private static BoundedRational getATanArg(@Nullable CRProperty p) {
        return getArgForKind(p, IS_ATAN);
    }

    // The (in abs value) integral exponent for which we attempt to use a recursive
    // algorithm for evaluating pow(). The recursive algorithm works independent of the sign of the
    // base, and can produce rational results. But it can become slow for very large exponents.
    private static final BigInteger RECURSIVE_POW_LIMIT = BigInteger.valueOf(1000);

    // The corresponding limit when we're using rational arithmetic. This should fail fast
    // anyway, but we avoid ridiculously deep recursion.
    private static final BigInteger HARD_RECURSIVE_POW_LIMIT = BigInteger.ONE.shiftLeft(1000);

    // In some cases we cowardly refuse to compute answers longer than BIT_LIMIT, normally because
    // doing so is likely to cause us to run out of space in unpleasant ways.
    private static final int BIT_LIMIT = 2_000_000;
    private static final UnifiedReal BIT_LIMIT_AS_UR = new UnifiedReal(BIT_LIMIT);

    // Number of extra bits used in {@link toStringTruncated} evaluation to prefer truncation to
    // rounding. Must be <= 30.
    private static final int EXTRA_PREC = 10;

    private final BoundedRational ratFactor;
    private final CR crFactor;
    private final CRProperty crProperty;

    private static void check(boolean b) {
        if (!b) {
            throw new AssertionError();
        }
    }

    private UnifiedReal(@NonNull BoundedRational rat, @NonNull CR cr, @Nullable CRProperty p) {
        check(rat != null);
        crFactor = cr;
        ratFactor = rat;
        crProperty = p;
    }

    // Shorthand constructor; computes non-null property only for a few special cases.
    private UnifiedReal(@NonNull BoundedRational rat, @NonNull CR cr) {
        this(rat, cr, propertyFor(cr));
    }

    public UnifiedReal(@NonNull CR cr) {
        this(BoundedRational.ONE, cr);
    }

    private UnifiedReal(@NonNull CR cr, @Nullable CRProperty p) {
        this(BoundedRational.ONE, cr, p);
    }

    private UnifiedReal(@NonNull BoundedRational rat, @NonNull CRProperty p) {
        this(rat, crFromProperty(p), p);
        check(p.determinesCr());
    }

    private UnifiedReal(@NonNull CRProperty p) {
        this(BoundedRational.ONE, p);
    }

    public UnifiedReal(@NonNull BoundedRational rat) {
        this(rat, CR.ONE, CRProperty.ONE);
    }

    public UnifiedReal(BigInteger n) {
        this(new BoundedRational(n));
    }

    public UnifiedReal(long n) {
        this(new BoundedRational(n));
    }

    public static UnifiedReal valueOf(double x) {
        if (x == 0.0 || x == 1.0) {
            return valueOf((long) x);
        }
        return new UnifiedReal(BoundedRational.valueOf(x));
    }

    public static UnifiedReal valueOf(long n) {
        if (n == 0) {
            return UnifiedReal.ZERO;
        } else if (n == 1) {
            return UnifiedReal.ONE;
        } else {
            return new UnifiedReal(n);
        }
    }

    /**
     * Return a string describing r*pi radians. If degrees is true describe the
     * given number of radians in units of degrees.
     */
    @NonNull
    private static String symbolicPiMultiple(BoundedRational r, boolean degrees,
                                             boolean subsuperscript) {
        if (degrees) {
            BoundedRational rInDegrees = BoundedRational.multiply(r, BR180);
            if (rInDegrees != null) {
                return rInDegrees.toNiceString(subsuperscript, false /* mixed fractions */);
            } else {
                // Extremely unlikely, and the result isn't very useful. The result will be huge,
                // may not be fully reduced, and may be ugly, but it is correct.
                BigInteger[] numDen = r.getNumDen();
                return numDen[0].multiply(BIG_180) + "/" + numDen[1];
            }
        } else {
            BigInteger[] numDen = r.getNumDen();
            if (numDen[1].equals(BigInteger.ONE)) {
                return numDen[0] + PI_STRING;
            } else if (subsuperscript && !numDen[0].equals(BigInteger.ONE)) {
                return r.toNiceString(subsuperscript, false /* mixed fractions */) + PI_STRING;
            } else {
                return ((numDen[0].equals(BigInteger.ONE) ? "" :  numDen[0]) + PI_STRING + "/" + numDen[1]);
            }
        }
    }

    /**
     * If the property indicates the constructive real has a simple symbolic representation, return
     * it. The name is intended to be appended to the rational multiplier, so the name of one is the
     * empty string. If subsuperscript is true, use subscripts and superscripts to render the
     * embedded rational.
     */
    @Nullable
    private static String crSymbolic(@Nullable CRProperty p, boolean degrees,
                                     boolean subsuperscript) {
        // TODO(hboehm): This currently ignores translation issues. Revisit if/when Calculator
        // exposes this to the user again.
        if (p == null || isUnknownIrrational(p)) {
            // Not enough information for a symbolic representation.
            return null;
        }
        if (isOne(p)) {
            return "";
        }
        if (isPi(p)) {
            return PI_STRING;
        }
        {
            final BoundedRational expArg = getExpArg(p);
            if (expArg != null) {
                if (expArg.equals(BoundedRational.ONE)) {
                    return "e";
                } else {
                    return "exp(" + expArg.toNiceString(subsuperscript, false /* mixed */) + ")";
                }
            }
        }
        {
            final BoundedRational sqrtArg = getSqrtArg(p);
            if (sqrtArg != null) {
                final BigInteger intSqrtArg = BoundedRational.asBigInteger(sqrtArg);
                if (intSqrtArg != null) {
                    return SQRT_STRING + intSqrtArg;
                } else {
                    return SQRT_STRING + "(" + sqrtArg.toNiceString(subsuperscript, false) + ")";
                }
            }
        }
        {
            final BoundedRational lnArg = getLnArg(p);
            if (lnArg != null) {
                return "ln(" + lnArg.toNiceString(subsuperscript, false) + ")";
            }
        }
        {
            final BoundedRational logArg = getLogArg(p);
            if (logArg != null) {
                return "log(" + logArg.toNiceString(subsuperscript, false) + ")";
            }
        }
        {
            final BoundedRational sinPiArg = getSinPiArg(p);
            if (sinPiArg != null) {
                return "sin(" + symbolicPiMultiple(sinPiArg, degrees, subsuperscript) + ")";
            }
        }
        {
            final BoundedRational tanPiArg = getTanPiArg(p);
            if (tanPiArg != null) {
                return "tan(" + symbolicPiMultiple(tanPiArg, degrees, subsuperscript) + ")";
            }
        }
        {
            final BoundedRational aSinArg = getASinArg(p);
            if (aSinArg != null) {
                // sin superscript -1
                return "sin" + INVERSE_STRING + "(" + aSinArg.toNiceString(subsuperscript, false) + ")"
                        + (degrees ? DEGREE_CONVERSION : "");
            }
        }
        {
            final BoundedRational aTanArg = getATanArg(p);
            if (aTanArg != null) {
                return "tan" + INVERSE_STRING + "(" + aTanArg.toNiceString(subsuperscript, false) + ")"
                        + (degrees ? DEGREE_CONVERSION : "");
            }
        }
        return null;
    }

    private static final String PI_STRING = "\u03C0";  // GREEK SMALL LETTER PI
    private static final String SQRT_STRING = "\u221A";
    private static final String MULT_STRING = "\u00D7";
    private static final String INVERSE_STRING = "\u207B\u00B9";  // -1 superscript
    private static final String DEGREE_CONVERSION = MULT_STRING + "180/" + PI_STRING;

    /** Is this number known to be algebraic? */
    public boolean definitelyAlgebraic() {
        return definitelyAlgebraic(crProperty) || ratFactor.signum() == 0;
    }

    /**
     * Is cr known to be algebraic (as opposed to transcendental)? Currently only produces meaningful
     * results for the above known special constructive reals.
     */
    private static boolean definitelyAlgebraic(@Nullable CRProperty p) {
        return p != null
                && (p.kind == IS_ONE || p.kind == IS_SQRT || p.kind == IS_SIN_PI || p.kind == IS_TAN_PI);
    }

    /** Is this number known to be rational? */
    public boolean definitelyRational() {
        return isOne(crProperty) || ratFactor.signum() == 0;
    }

    /**
     * Is this number known to be irrational?
     */
    public boolean definitelyIrrational() {
        return crProperty != null && crProperty.kind != IS_ONE;
        // Clearly correct for IS_IRRATIONAL.
        // The other kinds carefully exclude arguments describing a rational value.
    }

    /** Is this number known to be transcendental? */
    public boolean definitelyTranscendental() {
        if (definitelyRational() || crProperty == null) {
            return false;
        }
        switch (crProperty.kind) {
            case IS_PI:
                return true;
            case IS_SQRT:
                return false;
            case IS_LN:
                // arg > 1
                // Follows from Lindemann-Weierstrass theorem. If ln(r) = a, where r is rational, and a
                // algebraic, then r = e^a. But if a is nonzero algebraic, then e^a is transcendental.
                return true;
            case IS_LOG:
                // If this is rational, then n ln(arg) = m ln(10), n and m integers.
                // TODO: Can we do better?
                return false;
            case IS_EXP:
                // arg != 0
                // Simple application of Lindemann-Weierstrass theorem.
                return true;
            case IS_SIN_PI:
            case IS_TAN_PI:
                // Always algebraic for rational multiples of pi.
                return false;
            case IS_ASIN:
            case IS_ATAN:
                // If asin(r) = a, r rational, a algebraic, then r = sin(a). It follows from
                // Lindemann-Weierstrass that this can happen only if a is zero, i.e. if r is zero.
                // We don't use this representation for asin(0). The atan argument is similar.
                return true;
            case IS_IRRATIONAL:
                // Not enough information to tell.
                return false;
            default:
                throw new AssertionError(); // Can't get here.
        }
    }

    /** Is the supplied IS_SQRT property argument known to be minimal? */
    private static boolean irreducibleSqrt(BoundedRational sqrtArg) {
        BigInteger[] nd = sqrtArg.getNumDen();
        // Check absolute values without allocation.
        return nd[0].bitLength() <= 30
                && Math.abs(nd[0].intValue()) <= BoundedRational.EXTRACT_SQUARE_MAX_OPT
                && nd[1].bitLength() <= 30
                && Math.abs(nd[1].intValue()) <= BoundedRational.EXTRACT_SQUARE_MAX_OPT;
    }

    /**
     * Return a rational r != 0, such that a = b^r, or null if we didn't find one. Effectively this
     * tests whether a and b have a common integral power and returns the two exponents as a rational.
     * A and b are presumed to be positive. Note that if a = b = 1, we return 1, but any rational
     * would do. We do not try very hard if any of the numbers involved are large.
     */
    private static BoundedRational commonPower(BigInteger a, BigInteger b) {
        int compareResult = a.compareTo(b);
        if (compareResult == 0) {
            return BoundedRational.ONE;
        } else if (compareResult < 0) {
            // BoundedRational already propagates null as desired.
            return BoundedRational.inverse(commonPower(b, a));
        }
        if (a.compareTo(BigInteger.ONE) == 0 || b.compareTo(BigInteger.ONE) == 0) {
            return null;
        }
        if (a.bitLength() > COMMON_POWER_LENGTH_LIMIT) {
            // punt
            return null;
        }
        // We use a modified version of the Euclidean GCD algorithm, repeatedly dividing the larger
        // number by the smaller. If a = b^r, then (a/b) = b^(r-1).
        BigInteger[] qAndR = a.divideAndRemainder(b);
        if (qAndR[1].signum() != 0) {
            // If they're not divisible, there must be two primes, such that a is divisible by a larger
            // power of one than b and vice-versa. That makes it impossible that a^n = b^m, m and n
            // integers. thus we know r doesn't exist.
            return null;
        }
        return BoundedRational.add(commonPower(qAndR[0], b), BoundedRational.ONE);
    }

    /**
     * Do a and b have a common power? Considers negative exponents. Both a and b must be positive.
     */
    public static boolean commonPower(@NonNull BoundedRational a, @NonNull BoundedRational b) {
        BigInteger[] nda = a.getNumDen();
        BigInteger[] ndb = b.getNumDen();
        // First consider the case in which one numerator and/or denominator pair consists
        // entirely of ones. This is special because commonPower() is not uniquely determined.
        if (nda[1].compareTo(BigInteger.ONE) == 0) {
            if (ndb[1].compareTo(BigInteger.ONE) == 0) {
                return commonPower(nda[0], ndb[0]) != null;
            } else if (ndb[0].compareTo(BigInteger.ONE) == 0) {
                return commonPower(nda[0], ndb[1]) != null;
            }
        } else if (nda[0].compareTo(BigInteger.ONE) == 0) {
            if (ndb[0].compareTo(BigInteger.ONE) == 0) {
                return commonPower(nda[1], ndb[1]) != null;
            } else if (ndb[1].compareTo(BigInteger.ONE) == 0) {
                return commonPower(nda[1], ndb[0]) != null;
            }
        }
        // In the general case, two commonPower computations must produce the same result.
        final BoundedRational commonPowerNaNb = commonPower(nda[0], ndb[0]);
        final BoundedRational commonPowerNaDb = commonPower(nda[0], ndb[1]);
        if (commonPowerNaNb != null && commonPowerNaNb.equals(commonPower(nda[1], ndb[1]))) {
            // They have a common power, and both exponents have the same sign.
            return true;
        }
        if (commonPowerNaDb != null && commonPowerNaDb.equals(commonPower(nda[1], ndb[0]))) {
            // They have a common power, and exponents have different sign.
            return true;
        }
        return false;
    }

    private static final int COMMON_POWER_LENGTH_LIMIT = 200;

    /**
     * Do we know that this.crFactor is an irrational nonzero multiple of u.crFactor? If this returns
     * true, then a comparison of the two UnifiedReals cannot diverge, though we don't know of a good
     * runtime bound. Note that if both values are really tiny, it still may be completely impractical
     * to compare them.
     */
    public boolean definitelyIndependent(UnifiedReal u) {
        // We always return false if either crFactor might be zero.
        CRProperty p1 = crProperty;
        CRProperty p2 = u.crProperty;
        if (p1 == null || p2 == null || p1.equals(p2)) {
            return false;
        }
        // Halve the number of cases. ONE < PI < SQRT < EXP < LN.
        if (p1.kind > p2.kind) {
            return u.definitelyIndependent(this);
        }
        switch(p1.kind) {
            case IS_ONE:
                return u.definitelyIrrational();
            case IS_PI:
                // It appears to be unknown whether pi is a rational multiple of an exponential or log.
                // If we were brave, we could say true, and hope for an infinite loop, which would
                // probably prove an interesting theorem. But we are not ...
                // IS_ONE case is already handled, since p1 <= p2.
                return p2.kind == IS_SQRT;
            case IS_SQRT:
                if (u.definitelyTranscendental()) {
                    return true;
                }
                if (p2.kind == IS_SQRT) {
                    // The argument is not necessarily minimal.
                    return irreducibleSqrt(p1.arg) && irreducibleSqrt(p2.arg) && !p1.equals(p2);
                }
                return false;
            case IS_EXP:
                if (p2.kind == IS_EXP) {
                    // Lindemann-Weierstrass theorem gives us algebraic independence.
                    return !p1.arg.equals(p2.arg);
                } else if (p2.kind == IS_LN) {
                    // If e^a = cln(b), then e^e^a = b^c. The r.h.s is an algebraic multiple of e^0.
                    // By Lindemann-Weierstrass, this can only happen if e^a = 0, which is impossible.
                    return true;
                } else {
                    return u.definitelyAlgebraic();
                }
            case IS_LN:
                if (p2.kind == IS_IRRATIONAL) {
                    return false; // Not enough information.
                }
                if (p2.kind == IS_LN) {
                    // If ln(a) = cln(b), then a = b^c, a, b, and c rational, or equivalently a^c1 = b^c2,
                    // with c1 and c2 integers. C must be nonzero, since a > 1.  A necessary condition for
                    // this is that the numerator and denominator separately have to have a common integral
                    // power.
                    return !commonPower(p1.arg, p2.arg);
                } else {
                    // Assume ln(r) = a is algebraic. Then e^a is rational. By Lindemann-Weierstrass, this
                    // implies a = 0 and r = 1. We know that the argument is not one, so any algebraic
                    // number must be linearly independent over the rationals.
                    return u.definitelyAlgebraic();
                    // TODO: Can we do better for IS_LOG?
                }
            case IS_LOG:
                // In the irrational case, with u rational, we would have checked in the other order.
                if (p2.kind == IS_LOG) {
                    // We're asking if ln(a)/ln(10) = r ln(b)/ln(10), which is true iff ln(a) = r ln(b).
                    // Use the same algorithm as for IS_LN.
                    return !commonPower(p1.arg, p2.arg);
                }
                return false;
            case IS_SIN_PI:
            case IS_TAN_PI:
                // Always algebraic. We already handled the u rational case above.
                return u.definitelyTranscendental();
            case IS_ASIN:
                // As we argued above, this is transcendental.
                return u.definitelyAlgebraic();
            case IS_ATAN:
                // The case of u rational is handled above. Can we do better?
                return false;
            case IS_IRRATIONAL:
                return false;
            default:
                throw new AssertionError();
        }
    }

    /** Convert to String reflecting raw representation. Debug or log messages only, not pretty. */
    @Override
    public String toString() {
        return ratFactor + "*" + crFactor;
    }

    /**
     * Convert to readable String. Intended for user output. Produces exact expression when possible.
     * If degrees is true, then any trig functions in the output will refer to the degree versions.
     * Otherwise the radian versions are used.
     */
    @NonNull
    public String toNiceString(boolean degrees, boolean subsuperscript, boolean mixed) {
        if (isOne(crProperty) || ratFactor.signum() == 0) {
            return ratFactor.toNiceString(subsuperscript, mixed);
        }
        final String symbolic = crSymbolic(crProperty, degrees, subsuperscript);
        if (symbolic != null) {
            final BigInteger bi = BoundedRational.asBigInteger(ratFactor);
            if (bi != null) {
                if (bi.equals(BigInteger.ONE)) {
                    return symbolic;
                } else if (bi.equals(BIG_MINUS_ONE)) {
                    return "-" + symbolic;
                }
                return bi + symbolic;
            }
            final BigInteger biInverse = BoundedRational.asBigInteger(BoundedRational.inverse(ratFactor));
            if (biInverse != null) {
                // Use spaces to reduce ambiguity with square roots.
                return (biInverse.signum() < 0 ? "-" : "") + symbolic + " / " + biInverse.abs();
            }
            if (subsuperscript) {
                return ratFactor.toNiceString(subsuperscript, false /* mixed */) + symbolic;
            } else {
                return "(" + ratFactor.toNiceString() + ")" + symbolic;
            }
        }
        if (ratFactor.equals(BoundedRational.ONE)) {
            return crFactor.toString();
        }
        return crValue().toString();
    }

    /**
     * Convert to a readable string using radian version of trig functions.
     * Use improper fractions, and no sub-and super-scripts.
     */
    @NonNull
    public String toNiceString() {
        return toNiceString(false, false, false);
    }

    /** Will toNiceString() produce an exact representation? */
    public boolean exactlyDisplayable() {
        return crProperty != null && crProperty.determinesCr();
    }

    /**
     * Return summary statistics for the expression returned by toNiceString().
     * Assumes exactlyDisplayable() is true.
     */
    @NonNull
    public ExprStats getStats(boolean degrees) {
        final ExprStats result = new ExprStats();
        BoundedRational normalized = ratFactor.reduce();
        BigInteger bi = BoundedRational.asBigInteger(normalized);
        result.totalConstBitLength = (bi != null ? bi.bitLength() : normalized.bitLength());
        result.nOps = (bi != null ? 0 : 1);
        switch(crProperty.kind) {
            case IS_ONE:
                break;
            case IS_PI:
                ++result.nOps;
                ++result.nCommonOps;
                break;
            case IS_EXP:
                if (crProperty.arg.equals(BoundedRational.ONE)) {
                    // Displayed as "e".
                    ++result.nOps;
                    ++result.nCommonOps;
                    break;
                } // else fall through:
            case IS_LN:
            case IS_LOG:
            case IS_SIN_PI:
            case IS_TAN_PI:
            case IS_ASIN:
            case IS_ATAN:
                result.transcendentalFns++;
                // And fall through:
            case IS_SQRT:
                BoundedRational arg = crProperty.arg;
                if (degrees && (crProperty.kind == IS_SIN_PI || crProperty.kind == IS_TAN_PI)) {
                    // Arg will be converted to degrees on output.
                    arg = BoundedRational.multiply(arg, BR180).reduce();
                }
                BigInteger argAsBI = BoundedRational.asBigInteger(arg);
                int argBitLength = (argAsBI != null ? argAsBI.bitLength() : arg.bitLength());
                result.totalConstBitLength += argBitLength;
                result.interestingConstBitLength = argBitLength;
                result.nOps += (argAsBI != null ? 1 : 2);  // 1 for main function, maybe 1 for quotient.
                if (degrees) {
                    if (crProperty.kind == IS_ASIN || crProperty.kind == IS_ATAN) {
                        // Need to add ugly conversion on output.
                        result.totalConstBitLength += BIG_180.bitLength();
                        result.nOps += 3;  // Multiplication, division, pi.
                    }
                } else {
                    // The result expression needs a pi behind the argument.
                    result.nOps += 1;
                }
                break;
            default:
                throw new AssertionError("Unexpected kind " + crProperty.kind);
        }
        return result;
    }

    /*
     * Returns a truncated representation of the result.
     * If exactlyTruncatable(), we round correctly towards zero. Otherwise the resulting digit
     * string may occasionally be rounded up instead.
     * Always includes a decimal point in the result.
     * The result includes n digits to the right of the decimal point.
     * @param n result precision, >= 0
     */
    public String toStringTruncated(int n) {
        if (isOne(crProperty) || ratFactor.signum() == 0) {
            return ratFactor.toStringTruncated(n);
        }
        final CR scaled = CR.valueOf(BigInteger.TEN.pow(n)).multiply(crValue());
        boolean negative = false;
        BigInteger intScaled;
        if (exactlyTruncatable()) {
            intScaled = scaled.get_appr(0);
            if (intScaled.signum() < 0) {
                negative = true;
                intScaled = intScaled.negate();
            }
            if (CR.valueOf(intScaled).compareTo(scaled.abs()) > 0) {
                intScaled = intScaled.subtract(BigInteger.ONE);
            }
            check(CR.valueOf(intScaled).compareTo(scaled.abs()) < 0);
        } else {
            // Approximate case.  Exact comparisons are impossible.
            intScaled = scaled.get_appr(-EXTRA_PREC);
            if (intScaled.signum() < 0) {
                negative = true;
                intScaled = intScaled.negate();
            }
            intScaled = intScaled.shiftRight(EXTRA_PREC);
        }
        String digits = intScaled.toString();
        int len = digits.length();
        if (len < n + 1) {
            digits = StringUtils.repeat('0', n + 1 - len) + digits;
            len = n + 1;
        }
        return (negative ? "-" : "") + digits.substring(0, len - n) + "." + digits.substring(len - n);
    }

    /*
     * Can we compute correctly truncated approximations of this number?
     */
    public boolean exactlyTruncatable() {
        // If the value is known rational, we can do exact comparisons.
        // If the value is known irrational, then we can safely compare to rational approximations;
        // equality is impossible; hence the comparison must converge.
        // The only problem cases are the ones in which we don't know.
        return isOne(crProperty) || ratFactor.signum() == 0 || definitelyIrrational();
    }

    /**
     * Return a double approximation. Rational arguments are currently rounded to nearest, with ties
     * away from zero. TODO: Improve rounding.
     */
    public double doubleValue() {
        if (isOne(crProperty)) {
            return ratFactor.doubleValue(); // Hopefully correctly rounded
        } else {
            return crValue().doubleValue(); // Approximately correctly rounded
        }
    }

    public CR crValue() {
        return ratFactor.compareToOne() == 0 ? crFactor : ratFactor.crValue().multiply(crFactor);
    }

    @SuppressWarnings("ReferenceEquality")
    private boolean sameCrFactor(UnifiedReal u) {
        return crFactor == u.crFactor
                || (crProperty != null && crProperty.determinesCr() && crProperty.equals(u.crProperty));
    }

    /**
     * Do both numbers have properties of the same kind describing either a constant or
     * a strictly monotonic function?
     */
    private boolean sameMonotonicCrKind(UnifiedReal u) {
        if (crProperty != null && u.crProperty != null
                && crProperty.kind == u.crProperty.kind && crProperty.determinesCr()) {
            // All of our kinds other than IS_IRRATIONAL currently qualify.
            return true;
        }
        return false;
    }

    /** Are this and r exactly comparable? */
    public boolean isComparable(UnifiedReal u) {
        // We check for ONE only to speed up the common case.
        // The use of a tolerance here means we can spuriously return false, not true.
        return (sameCrFactor(u) && isNonzero(crProperty))
                || ratFactor.signum() == 0 && u.ratFactor.signum() == 0
                || (definitelyIndependent(u)
                // One of the operands also needs to be non-tiny for the comparison to be practical.
                && (leadingBinaryZeroes() < -ZERO_COMPARISON_TOLERANCE
                || u.leadingBinaryZeroes() < -ZERO_COMPARISON_TOLERANCE
                || crValue().signum(DEFAULT_INITIAL_TOLERANCE) != 0  // Try cheaper test first.
                || u.crValue().signum(DEFAULT_INITIAL_TOLERANCE) != 0
                || crValue().signum(ZERO_COMPARISON_TOLERANCE) != 0
                || u.crValue().signum(ZERO_COMPARISON_TOLERANCE) != 0))
                || (sameMonotonicCrKind(u) &&
                (ratFactor.equals(u.ratFactor) || crProperty.kind == IS_SQRT))
                || crValue().compareTo(u.crValue(),
                DEFAULT_RELATIVE_TOLERANCE, DEFAULT_COMPARISON_TOLERANCE) != 0;
    }

    /**
     * Return +1 if this is greater than r, -1 if this is less than r, or 0 of the two are known to be
     * equal. May diverge if the two are equal and !isComparable(r).
     */
    public int compareTo(UnifiedReal u) {
        if (definitelyZero() && u.definitelyZero()) {
            return 0;
        }
        if (sameCrFactor(u)) {
            final int signum = crFactor.signum(); // Can diverge if crFactor == 0.
            return signum * ratFactor.compareTo(u.ratFactor);
        }
        if (sameMonotonicCrKind(u)) {
            if (ratFactor.equals(u.ratFactor)) {
                // kind cannot be IS_PI or IS_ONE, since sameCrFactor() would have been true.
                // sameMonotonicCrKind() precludes IS_IRRATIONAL.
                // All other kinds represent monotonically increasing functions over the range we allow.
                // Just compare the arguments.
                return ratFactor.signum() * crProperty.arg.compareTo(u.crProperty.arg);
            }
            if (crProperty.kind == IS_SQRT) {
                // Compare the squares. We promise to compare these accrurately, so we force
                // the multiplications to succeed by letting the result exceed BoundedRational
                // size bounds.
                final int signum = ratFactor.signum();
                final int uSignum = u.ratFactor.signum();
                if (signum < uSignum) {
                    return -1;
                } else if (signum > uSignum) {
                    return 1;
                }
                final BoundedRational squared = BoundedRational.rawMultiply(
                        BoundedRational.rawMultiply(ratFactor, ratFactor), getSqrtArg(crProperty));
                final BoundedRational uSquared = BoundedRational.rawMultiply(
                        BoundedRational.rawMultiply(u.ratFactor, u.ratFactor), getSqrtArg(u.crProperty));
                // Both squared and uSquared are non-null.
                return signum * squared.compareTo(uSquared);
            }
        }
        return crValue().compareTo(u.crValue()); // Can also diverge.
    }

    /**
     * Return +1 if this is greater than r, -1 if this is less than r, 0 if the two are equal, or
     * possibly 0 if the two are within 2^a of each other, and not comparable.
     */
    public int compareTo(UnifiedReal u, int a) {
        if (isComparable(u)) {
            return compareTo(u);
        } else {
            // See if we can resolve comparison with lower precision first.
            for (int prec = DEFAULT_INITIAL_TOLERANCE; prec * 2 > a; prec *= 2) {
                int result = crValue().compareTo(u.crValue(), prec);
                if (result != 0) return result;
            }
            return crValue().compareTo(u.crValue(), a);
        }
    }

    /** Return compareTo(ZERO, a). */
    public int signum(int a) {
        return compareTo(ZERO, a);
    }

    /** Return compareTo(ZERO). May diverge for ZERO argument if !isComparable(ZERO). */
    public int signum() {
        return compareTo(ZERO);
    }

    /**
     * Equality comparison. May erroneously return true if values differ by less than 2^a, and
     * !isComparable(u).
     */
    public boolean approxEquals(UnifiedReal u, int a) {
        if (isComparable(u)) {
            if (definitelyIndependent(u) && (ratFactor.signum() != 0 || u.ratFactor.signum() != 0)) {
                // No need to actually evaluate, though we don't know which is larger.
                return false;
            } else {
                return compareTo(u) == 0;
            }
        }
        return crValue().compareTo(u.crValue(), a) == 0;
    }

    /**
     * Returns true if values are definitely known to be equal, false in all other cases. This does
     * not satisfy the contract for Object.equals().
     */
    public boolean definitelyEquals(UnifiedReal u) {
        return isComparable(u) && compareTo(u) == 0;
    }

    @Override
    public int hashCode() {
        throw new AssertionError("UnifiedReals don't have equality or hashcodes");
    }

    @Override
    public boolean equals(Object r) {
        if (r == null || !(r instanceof UnifiedReal)) {
            return false;
        }
        // This is almost certainly a programming error. Don't even try.
        throw new AssertionError("Can't compare UnifiedReals for exact equality");
    }

    /**
     * Returns true if values are definitely known not to be equal based on internal symbolic
     * information, false in all other cases. Performs no approximate evaluation.
     */
    public boolean definitelyNotEquals(UnifiedReal u) {
        if (ratFactor.signum() == 0) {
            return isNonzero(u.crProperty) && u.ratFactor.signum() != 0;
        }
        if (u.ratFactor.signum() == 0) {
            return isNonzero(crProperty) && ratFactor.signum() != 0;
        }
        if (crProperty == null || u.crProperty == null) {
            return false;
        }
        if (definitelyIndependent(u)) {
            return ratFactor.signum() != 0 || u.ratFactor.signum() != 0;
        } else if (sameCrFactor(u) && isNonzero(crProperty)) {
            return !ratFactor.equals(u.ratFactor);
        }
        return false;
    }

    // And some slightly faster convenience functions for special cases:

    public boolean definitelyZero() {
        // If crFactor were known to be zero, we would have used a different representation.
        return ratFactor.signum() == 0;
    }

    /**
     * Can this number be determined to be definitely nonzero without performing approximate
     * evaluation?
     */
    public boolean definitelyNonzero() {
        return isNonzero(crProperty) && ratFactor.signum() != 0;
    }

    public boolean definitelyOne() {
        return isOne(crProperty) && ratFactor.equals(BoundedRational.ONE);
    }

    /** Return equivalent BoundedRational, if known to exist, null otherwise */
    public BoundedRational boundedRationalValue() {
        if (isOne(crProperty) || ratFactor.signum() == 0) {
            return ratFactor;
        }
        return null;
    }

    /** Returns equivalent BigInteger result if it exists, null if not. */
    public BigInteger bigIntegerValue() {
        final BoundedRational r = boundedRationalValue();
        return BoundedRational.asBigInteger(r);
    }

    /**
     * Returns a suitable representation of ln(arg) or log(arg). arg is positive and not one. kind is
     * IS_LN or IS_LOG.
     */
    private static UnifiedReal logRep(byte kind, @NonNull BoundedRational arg) {
        if (arg.compareToOne() < 0) {
            // Convert to an argument larger than one. Normalizing arguments in this way increases the
            // chance of repeat occurrences of the same argument, and makes them cleaner to display.
            return logRep(kind, BoundedRational.inverse(arg)).negate();
        }
        BigInteger intArg = BoundedRational.asBigInteger(arg);
        if (intArg != null) {
            UnifiedReal smallPowerLog = lgSmallPower(kind, intArg);
            if (smallPowerLog != null) {
                return  smallPowerLog;
            }
        }
        if (arg.bitLength() > LOG_ARG_BITS) {
            if (kind == IS_LN) {
                return new UnifiedReal(arg.crValue().ln());
            } else {
                return new UnifiedReal(arg.crValue().ln().divide(CR_TEN.ln()));
            }
        } else {
            return new UnifiedReal(BoundedRational.ONE, makeProperty(kind, arg));
        }
    }

    public UnifiedReal add(UnifiedReal u) {
        if (sameCrFactor(u)) {
            final BoundedRational nRatFactor = BoundedRational.add(ratFactor, u.ratFactor);
            if (nRatFactor != null) {
                return new UnifiedReal(nRatFactor, crFactor, crProperty);
            }
        }
        if (definitelyZero()) {
            // Avoid creating new crFactor, even if they don't currently match.
            return u;
        }
        if (u.definitelyZero()) {
            return this;
        }
        // Consider "simplifying" sums of logs.
        if (crProperty != null && u.crProperty != null
                && crProperty.kind == u.crProperty.kind
                && (crProperty.kind == IS_LN || crProperty.kind == IS_LOG)) {
            // a ln(b) + c ln(d) = ln(b^a * d^c)
            // a log(b) + c log(d) = log(b^a * d^c)
            // If the resulting ln argument is reasonably compact, compute the sum as the right side
            // instead, since that preserves the symbolic representation.
            BigInteger ratAsInt = BoundedRational.asBigInteger(ratFactor);
            BigInteger uRatAsInt = BoundedRational.asBigInteger(u.ratFactor);
            if (ratAsInt != null && uRatAsInt != null) {
                Double ratAsDouble = ratAsInt.doubleValue();
                Double uRatAsDouble = uRatAsInt.doubleValue();
                // Estimate size of resulting argument.
                double estimatedSize = Math.abs(ratAsDouble) * (double) crProperty.arg.bitLength()
                        + Math.abs(uRatAsDouble) * (double) u.crProperty.arg.bitLength();
                if (estimatedSize <= (float)LOG_ARG_CANDIDATE_BITS) {
                    BoundedRational term1 = BoundedRational.pow(crProperty.arg, ratFactor);
                    BoundedRational term2 = BoundedRational.pow(u.crProperty.arg, u.ratFactor);
                    BoundedRational newArg = BoundedRational.multiply(term1, term2);
                    if (newArg != null) {
                        return logRep(crProperty.kind, newArg);
                    } // The else case here is probably impossible, since we already checked the size.
                }
            }
        }
        // Since we got here, neither ratFactor is zero.
        // We can still conclude that the result is irrational, so long as the two arguments
        // are independent. But it can be counter-productive to track this if the arguments
        // are of greatly differing magnitude. We know that 1 + e^(-e^10000) is irrational,
        // but we still don't want to evaluate it sufficiently to distinguish it from 1.
        // Thus we want to treat 1 + e^(-e^10000) as not comparable to rationals.
        // We in fact don't track this if either argument might be ridiculously small, where
        // ridiculously small is < 10^-1000, and thus also way outside of IEEE exponent range.
        CRProperty resultProp = null;
        if (definitelyIndependent(u)
                && leadingBinaryZeroes() < -DEFAULT_COMPARISON_TOLERANCE
                && u.leadingBinaryZeroes() < -DEFAULT_COMPARISON_TOLERANCE) {
            resultProp = makeProperty(IS_IRRATIONAL, null);
        }
        return new UnifiedReal(crValue().add(u.crValue()), resultProp);
    }

    // Don't track ln() or log() arguments whose representation is larger than this.
    private static final int LOG_ARG_BITS = 100;

    // Don't even attempt to simplify ln() or log() arguments larger than this.
    private static final int LOG_ARG_CANDIDATE_BITS = 2000;

    public UnifiedReal negate() {
        return new UnifiedReal(BoundedRational.negate(ratFactor), crFactor, crProperty);
    }

    public UnifiedReal subtract(UnifiedReal u) {
        return add(u.negate());
    }

    /** Return sqrt(x*y) as a UnifiedReal. */
    private static UnifiedReal multiplySqrts(BoundedRational x, BoundedRational y) {
        if (x.equals(y)) {
            return new UnifiedReal(x);
        } else {
            final BoundedRational product = BoundedRational.multiply(x, y).reduce();
            if (product.signum() == 0) {
                return ZERO;
            }
            if (product != null) {
                final BoundedRational[] decomposedProduct = product.extractSquareReduced();
                return new UnifiedReal(decomposedProduct[0], decomposedProduct[1].crValue().sqrt(),
                        makeProperty(IS_SQRT, decomposedProduct[1]));
            } else {
                return new UnifiedReal(x.crValue().multiply(y.crValue()).sqrt());
            }
        }
    }

    public UnifiedReal multiply(UnifiedReal u) {
        // Preserve a preexisting crFactor when we can.
        if (isOne(crProperty)) {
            final BoundedRational nRatFactor = BoundedRational.multiply(ratFactor, u.ratFactor);
            if (nRatFactor != null) {
                return new UnifiedReal(nRatFactor, u.crFactor, u.crProperty);
            }
        }
        if (isOne(u.crProperty)) {
            final BoundedRational nRatFactor = BoundedRational.multiply(ratFactor, u.ratFactor);
            if (nRatFactor != null) {
                return new UnifiedReal(nRatFactor, crFactor, crProperty);
            }
        }
        if (definitelyZero() || u.definitelyZero()) {
            return ZERO;
        }
        CRProperty resultProp = null;  // Property for product of crFactors.
        final @Nullable BoundedRational nRatFactor = BoundedRational.multiply(ratFactor, u.ratFactor);
        if (crProperty != null && u.crProperty != null) {
            if (crProperty.kind == IS_SQRT && u.crProperty.kind == IS_SQRT) {
                UnifiedReal crPart = multiplySqrts(getSqrtArg(crProperty), getSqrtArg(u.crProperty));
                final BoundedRational ratResult = BoundedRational.multiply(nRatFactor, crPart.ratFactor);
                if (ratResult != null) {
                    return new UnifiedReal(ratResult, crPart.crFactor, crPart.crProperty);
                }
            }
            if (crProperty.kind == IS_EXP && u.crProperty.kind == IS_EXP) {
                // exp(a) * exp(b) is exp(a + b) .
                final BoundedRational sum = BoundedRational.add(crProperty.arg, u.crProperty.arg);
                if (sum != null) {
                    // we use this only for the property, since crFactors may already have been evaluated.
                    resultProp = makeProperty(IS_EXP, sum);
                }
            }
        }
        // Probably a bit cheaper to multiply component-wise.
        // TODO: We should often be able to determine that the result is irrational.
        // But definitelyIndependent is not the right criterion. Consider e and e^-1.
        if (nRatFactor != null) {
            return new UnifiedReal(nRatFactor, crFactor.multiply(u.crFactor), resultProp);
        }
        // resultProp invalid for this computation; discard. We know that the ratFactors are nonzero.
        return new UnifiedReal(crValue().multiply(u.crValue()));
    }

    /** Return the reciprocal. */
    public UnifiedReal inverse() {
        if (definitelyZero()) {
            throw new ZeroDivisionException("Inverse of zero");
        }
        if (isOne(crProperty)) {
            return new UnifiedReal(BoundedRational.inverse(ratFactor));
        }
        final BoundedRational square = getSqrtArg(crProperty);
        if (BoundedRational.asBigInteger(square) != null) {
            // Prefer square roots of integers. 1/sqrt(n) = sqrt(n)/n
            final BoundedRational nRatFactor =
                    BoundedRational.inverse(BoundedRational.multiply(ratFactor, square));
            if (nRatFactor != null) {
                return new UnifiedReal(nRatFactor, crFactor, crProperty);
            }
        }
        CRProperty newProperty = null;
        if (crProperty != null && crProperty.kind == IS_EXP) {
            newProperty = makeProperty(IS_EXP, BoundedRational.negate(crProperty.arg));
        } else if (definitelyIrrational()) {
            newProperty = makeProperty(IS_IRRATIONAL, null);
        }
        return new UnifiedReal(BoundedRational.inverse(ratFactor), crFactor.inverse(), newProperty);
    }

    public UnifiedReal divide(UnifiedReal u) {
        if (sameCrFactor(u)) {
            if (u.definitelyZero()) {
                throw new ZeroDivisionException("Division by zero");
            }
            final BoundedRational nRatFactor = BoundedRational.divide(ratFactor, u.ratFactor);
            if (nRatFactor != null) {
                return new UnifiedReal(nRatFactor);
            }
        }
        // Try to reduce ln(x)/ln(10) to log(x) to keep symbolic representation.
        final BoundedRational lnArg = getLnArg(crProperty);
        if (lnArg != null) {
            final BoundedRational uLnArg = getLnArg(u.crProperty);
            if (uLnArg != null && uLnArg.equals(BoundedRational.TEN)) {
                final BoundedRational ratQuotient = BoundedRational.divide(ratFactor, u.ratFactor);
                if (ratQuotient != null) {
                    return new UnifiedReal(ratQuotient, makeProperty(IS_LOG, lnArg));
                }
            }
        }
        return multiply(u.inverse());
    }

    /**
     * Return the square root. This may return a value with a null property, rather than a known
     * rational, even when the result is rational.
     */
    public UnifiedReal sqrt() {
        if (signum(DEFAULT_COMPARISON_TOLERANCE) < 0) {
            throw new ArithmeticException("sqrt(negative)");
        }
        if (definitelyZero()) {
            return ZERO;
        }
        CRProperty newCrProperty = null;
        if (isOne(crProperty)) {
            final BoundedRational r = ratFactor.reduce();
            if (r.extractSquareWillSucceed()) {
                // Avoid generating IS_SQRT property for rational values.
                final BoundedRational[] decomposedProduct = r.extractSquareReduced();
                if (decomposedProduct[1].compareToOne() == 0) {
                    newCrProperty = makeProperty(IS_ONE, null);
                } else {
                    newCrProperty = makeProperty(IS_SQRT, decomposedProduct[1]);
                }
                return new UnifiedReal(decomposedProduct[0],
                        decomposedProduct[1].crValue().sqrt(), newCrProperty);
            } // else don't track; we don't know if it's rational.
        }

        // If this is exp(a), result is exp(a/2). Track that.
        final BoundedRational expArg = getExpArg(crProperty);
        if (expArg != null) {
            final BoundedRational newArg = BoundedRational.divide(expArg, BoundedRational.TWO);
            if (newArg != null) {
                newCrProperty = makeProperty(IS_EXP, newArg);
            }
        }
        return new UnifiedReal(crValue().sqrt(), newCrProperty);
    }

    /** Return (this mod 2pi)/(pi/6) as a BigInteger, or null if that isn't easily possible. */
    private BigInteger getPiTwelfths() {
        if (definitelyZero()) return BigInteger.ZERO;
        if (isPi(crProperty)) {
            final BigInteger quotient =
                    BoundedRational.asBigInteger(BoundedRational.multiply(ratFactor, BoundedRational.TWELVE));
            if (quotient == null) {
                return null;
            }
            return quotient.mod(BIG_24);
        }
        return null;
    }

    /**
     * Compute the sin of an integer multiple n of pi/12, if easily representable.
     *
     * @param n value between 0 and 23 inclusive.
     */
    private static UnifiedReal sinPiTwelfths(int n) {
        if (n >= 12) {
            final UnifiedReal negResult = sinPiTwelfths(n - 12);
            return negResult == null ? null : negResult.negate();
        }
        switch (n) {
            case 0:
                return ZERO;
            case 2: // 30 degrees
                return HALF;
            case 3: // 45 degrees
                return HALF_SQRT2;
            case 4: // 60 degrees
                return HALF_SQRT3;
            case 6:
                return ONE;
            case 8:
                return HALF_SQRT3;
            case 9:
                return HALF_SQRT2;
            case 10:
                return HALF;
            default:
                return null;
        }
    }

    private static UnifiedReal cosPiTwelfths(int n) {
        int sinArg = n + 6;
        if (sinArg >= 24) {
            sinArg -= 24;
        }
        return sinPiTwelfths(sinArg);
    }

    public UnifiedReal sin() {
        final BigInteger piTwelfths = getPiTwelfths();
        if (piTwelfths != null) {
            final UnifiedReal result = sinPiTwelfths(piTwelfths.intValue());
            if (result != null) {
                return result;
            }
        }
        if (isPi(crProperty)) {
            final SignedProperty newCrProperty = makeSinPiProperty(ratFactor);
            if (newCrProperty != null) {
                return new UnifiedReal(
                        (newCrProperty.negative ? BoundedRational.MINUS_ONE : BoundedRational.ONE),
                        newCrProperty.property);
            }
        }
        BoundedRational aSinArg = getASinArg(crProperty);
        if (aSinArg != null && ratFactor.compareToOne() == 0) {
            return new UnifiedReal(aSinArg);
        }
        final CRProperty newCrProperty =
                (definitelyAlgebraic() && definitelyNonzero() ? makeProperty(IS_IRRATIONAL, null) : null);
        return new UnifiedReal(crValue().sin(), newCrProperty);
    }

    /*
     * Return a copy of the argument that is at least marked is irrational.
     */
    private static UnifiedReal tagIrrational(UnifiedReal arg) {
        if (arg.crProperty == null) {
            return new UnifiedReal(arg.ratFactor, arg.crFactor, makeProperty(IS_IRRATIONAL, null));
        } else {
            return arg;
        }
    }

    public UnifiedReal cos() {
        if (definitelyAlgebraic() && definitelyNonzero()) {
            // We know from Lindemann-Weierstrass that the result is transendental, and therefore
            // irrational.
            return tagIrrational(add(PI_OVER_2).sin());
        }
        return add(PI_OVER_2).sin();
    }

    public UnifiedReal tan() {
        final BigInteger piTwelfths = getPiTwelfths();
        if (piTwelfths != null) {
            final int i = piTwelfths.intValue();
            if (i == 6 || i == 18) {
                throw new DomainException("Tangent undefined");
            }
            final UnifiedReal top = sinPiTwelfths(i);
            final UnifiedReal bottom = cosPiTwelfths(i);
            if (top != null && bottom != null) {
                return top.divide(bottom);
            }
        }
        if (isPi(crProperty)) {
            final SignedProperty newCrProperty = makeTanPiProperty(ratFactor);
            if (newCrProperty != null) {
                return new UnifiedReal(
                        (newCrProperty.negative ? BoundedRational.MINUS_ONE : BoundedRational.ONE),
                        newCrProperty.property);
            }
        }
        BoundedRational aTanArg = getATanArg(crProperty);
        if (aTanArg != null && ratFactor.compareToOne() == 0) {
            return new UnifiedReal(aTanArg);
        }
        final CRProperty newCrProperty =
                (definitelyAlgebraic() && definitelyNonzero() ? makeProperty(IS_IRRATIONAL, null) : null);
        return new UnifiedReal(UnaryCRFunction.tanFunction.execute(crValue()), newCrProperty);
    }

    // Throw an exception if the argument is definitely out of bounds for asin or acos.
    private void checkAsinDomain() {
        if (isComparable(ONE) && (compareTo(ONE) > 0 || compareTo(MINUS_ONE) < 0)) {
            throw new DomainException("inverse trig argument out of range");
        }
    }

    /** Return asin(n/2). n is between -2 and 2. */
    public static UnifiedReal asinHalves(int n) {
        if (n < 0) {
            return (asinHalves(-n).negate());
        }
        switch (n) {
            case 0:
                return ZERO;
            case 1:
                return new UnifiedReal(BoundedRational.SIXTH, CR.PI);
            case 2:
                return new UnifiedReal(BoundedRational.HALF, CR.PI);
            default:
                break;
        }
        throw new AssertionError("asinHalves: Bad argument");
    }

    public UnifiedReal asin() {
        checkAsinDomain();
        final BigInteger halves = multiply(TWO).bigIntegerValue();
        if (halves != null) {
            return asinHalves(halves.intValue());
        }
        if (compareTo(ZERO, -10) < 0) {
            return negate().asin().negate();
        }
        if (definitelyEquals(HALF_SQRT2)) {
            return new UnifiedReal(BoundedRational.QUARTER, CR_PI);
        }
        if (definitelyEquals(HALF_SQRT3)) {
            return new UnifiedReal(BoundedRational.THIRD, CR_PI);
        }
        final BoundedRational sinPiArg = getSinPiArg(crProperty);
        if (sinPiArg != null) {
            if (ratFactor.compareToOne() == 0) {
                return new UnifiedReal(sinPiArg, CR_PI);
            }
            if (ratFactor.compareTo(BoundedRational.MINUS_ONE) == 0) {
                return new UnifiedReal(BoundedRational.negate(sinPiArg), CR_PI, CRProperty.PI);
            }
        }
        if (isOne(crProperty)) {
            check(ratFactor.signum() > 0);
            return new UnifiedReal(makeProperty(IS_ASIN, ratFactor));
        }
        return new UnifiedReal(crValue().asin());
    }

    public UnifiedReal acos() {
        return PI_OVER_2.subtract(asin());
    }

    public UnifiedReal atan() {
        if (compareTo(ZERO, -10) < 0) {
            return negate().atan().negate();
        }
        final BigInteger asBI = bigIntegerValue();
        if (asBI != null && asBI.compareTo(BigInteger.ONE) <= 0) {
            final int asInt = asBI.intValue();
            // These seem to be all rational cases:
            switch (asInt) {
                case 0:
                    return ZERO;
                case 1:
                    return PI_OVER_4;
                default:
                    throw new AssertionError("Impossible r_int");
            }
        }
        if (definitelyEquals(THIRD_SQRT3)) {
            return PI_OVER_6;
        }
        if (definitelyEquals(SQRT3)) {
            return PI_OVER_3;
        }
        final BoundedRational tanPiArg = getTanPiArg(crProperty);
        if (tanPiArg != null) {
            if(ratFactor.compareToOne() == 0) {
                return new UnifiedReal(tanPiArg, CR_PI);
            }
            if (ratFactor.compareTo(BoundedRational.MINUS_ONE) == 0) {
                return new UnifiedReal(BoundedRational.negate(tanPiArg), CR_PI);
            }
        }
        if (isOne(crProperty)) {
            check(ratFactor.signum() > 0);
            return new UnifiedReal(makeProperty(IS_ATAN, ratFactor));
        }
        return new UnifiedReal(UnaryCRFunction.atanFunction.execute(crValue()));
    }

    /**
     * Compute an integral power of a constructive real, using the standard recursive algorithm. exp
     * is known to be positive.
     */
    private static CR recursivePow(CR base, BigInteger exp) {
        if (exp.equals(BigInteger.ONE)) {
            return base;
        }
        if (exp.testBit(0)) {
            return base.multiply(recursivePow(base, exp.subtract(BigInteger.ONE)));
        }
        final CR tmp = recursivePow(base, exp.shiftRight(1));
        if (Thread.interrupted()) {
            throw new CR.AbortedException();
        }
        return tmp.multiply(tmp);
    }

    /**
     * Compute an integral power of a constructive real, using the exp function when we safely can.
     * Use recursivePow when we can't. exp is known to be nozero.
     */
    private UnifiedReal expLnPow(BigInteger exp) {
        final int sign = signum(DEFAULT_COMPARISON_TOLERANCE);
        if (sign > 0) {
            // Safe to take the log. This avoids deep recursion for huge exponents, which
            // may actually make sense here.
            return new UnifiedReal(crValue().ln().multiply(CR.valueOf(exp)).exp());
        } else if (sign < 0) {
            CR result = crValue().negate().ln().multiply(CR.valueOf(exp)).exp();
            if (exp.testBit(0) /* odd exponent */) {
                result = result.negate();
            }
            return new UnifiedReal(result);
        } else {
            // Base of unknown sign with integer exponent. Use a recursive computation.
            // (Another possible option would be to use the absolute value of the base, and then
            // adjust the sign at the end.  But that would have to be done in the CR
            // implementation.)
            if (exp.signum() < 0) {
                // This may be very expensive if exp.negate() is large.
                return new UnifiedReal(recursivePow(crValue(), exp.negate()).inverse());
            } else {
                return new UnifiedReal(recursivePow(crValue(), exp));
            }
        }
    }

    /**
     * Compute an integral power of this. This recurses roughly as deeply as the number of bits in the
     * exponent, and can, in ridiculous cases, result in a stack overflow.
     */
    private UnifiedReal pow(BigInteger exp) {
        if (exp.equals(BigInteger.ONE)) {
            return this;
        }
        final int exp_sign = exp.signum();
        if (exp_sign == 0) {
            // The following check may diverge, causing us to time out. This only happens
            // if we try to raise something that is zero, but not obviously so, to the
            // zeroth power.
            if (signum() != 0) {
                return ONE;
            }
            // Base is known to be exactly zero.
            throw new ZeroToTheZerothException("0^0");
        }
        if (definitelyZero() && exp_sign < 0) {
            throw new ArithmeticException("zero to negative power");
        }
        final BigInteger absExp = exp.abs();
        if (isOne(crProperty)) {
            final double resultLen = exp.doubleValue() * ratFactor.apprLog2Abs();
            // Both multiplicands may be negative. That still implies a huge answer.
            if (resultLen > (double)BIT_LIMIT /* +INFINITY qualifies */ ) {
                throw new TooBigException("Power result is too big");
            }
            if (absExp.compareTo(HARD_RECURSIVE_POW_LIMIT) <= 0) {
                final BoundedRational ratPow = ratFactor.pow(exp);
                // We count on this to fail, e.g. for very large exponents, when it would
                // otherwise be too expensive.
                if (ratPow != null) {
                    return new UnifiedReal(ratPow);
                }
            }
        }
        if (absExp.compareTo(RECURSIVE_POW_LIMIT) > 0) {
            return expLnPow(exp);
        }
        final BoundedRational square = getSqrtArg(crProperty);
        if (square != null) {
            // Compute powers as UnifiedReals, so we get the limit checking above.
            final UnifiedReal resultFactor1 = new UnifiedReal(ratFactor).pow(exp);
            final UnifiedReal squareAsUR = new UnifiedReal(square);
            final UnifiedReal resultFactor2 = squareAsUR.pow(exp.shiftRight(1));
            final UnifiedReal product = resultFactor1.multiply(resultFactor2);
            if (exp.and(BigInteger.ONE).intValue() == 1) {
                // Odd power: Multiply by remaining square root.
                return product.multiply(squareAsUR.sqrt());
            } else {
                return product;
            }
        }
        return expLnPow(exp);
    }

    /**
     * Return this ^ expon. This is really only well-defined for a positive base, particularly since
     * 0^x is not continuous at zero. (0^0 = 1 (as is epsilon^0), but 0^epsilon is 0. We nonetheless
     * try to do reasonable things at zero, when we recognize that case.
     */
    public UnifiedReal pow(UnifiedReal expon) {
        if (crProperty != null) {
            if (crProperty.kind == IS_EXP && crProperty.arg.compareToOne() == 0) {
                if (ratFactor.equals(BoundedRational.ONE)) {
                    return expon.exp();
                } else {
                    // (<ratFactor>e)^<expon> = <ratFactor>^<expon> * e^<expon>
                    final UnifiedReal ratPart = new UnifiedReal(ratFactor).pow(expon);
                    return expon.exp().multiply(ratPart);
                }
            }
            if (crProperty.kind == IS_ONE && ratFactor.compareTo(BoundedRational.TEN) == 0) {
                final BoundedRational exponLogArg = getLogArg(expon.crProperty);
                if (exponLogArg != null) {
                    // 10^(r * log(exponLogArg)) = exponLogArg^r
                    return new UnifiedReal(exponLogArg).pow(new UnifiedReal(expon.ratFactor));
                }
            }
        }
        final int sign = signum(DEFAULT_COMPARISON_TOLERANCE);
        final BoundedRational expAsBR = expon.boundedRationalValue();
        boolean knownIrrational = false;
        if (expAsBR != null) {
            final BigInteger[] expNumDen = expAsBR.getNumDen();
            if (expNumDen[1].equals(BigInteger.ONE)) {
                return pow(expNumDen[0]);
            }
            // Check for the case in which both arguments are rational, and there is an
            // exact rational answer.
            // We explicitly avoid returning a result for a negative base here,
            // even when that would make sense, as in (-8)^(1/3).
            // This is probably wrong if we're computing cube roots.
            // But note that we could never return a meaningful result for
            // (-8)^<1/3 computed so we can't recognize it as such>.
            if (sign >= 0 && isOne(crProperty) && expNumDen[1].bitLength() <= 30) {
                final int expDen = expNumDen[1].intValue(); // Doesn't lose information.
                // Don't just use BoundedRational.pow(), since that would bypass above checks.
                final BoundedRational rt = BoundedRational.nthRoot(ratFactor, expDen);
                if (rt != null) {
                    return new UnifiedReal(rt).pow(expNumDen[0]);
                } else {
                    // We know that the root is irrational. Raising it to a power relatively prime to expDen
                    // is not going to change that.
                    knownIrrational = true;
                }
            }
            // Explicitly check for the square root case, in case the result is representable
            // as an integer multiple of a small square root.
            if (expNumDen[1].equals(BIG_TWO)) {
                return pow(expNumDen[0]).sqrt();
            }
        }
        // If the exponent were known zero, we would have handled it above.
        if (sign == 0 && definitelyZero()) {
            // Compute the exponent sign, at the risk of divergence. The result depends on it.
            final int expon_sign = expon.signum();
            if (expon_sign > 0) {
                return ZERO;
            } else if (expon_sign < 0) {
                throw new ArithmeticException("zero to negative power");
            } else {
                // Unclear we can get here.
                throw new ZeroToTheZerothException("0^0");
            }
        }
        if (sign < 0) {
            throw new ArithmeticException("Negative base for pow() with non-integer exponent");
        }
        if (knownIrrational) {
            return new UnifiedReal(crValue().ln().multiply(expon.crValue()).exp(),
                    makeProperty(IS_IRRATIONAL, null));
        } else {
            return new UnifiedReal(crValue().ln().multiply(expon.crValue()).exp());
        }
    }

    /**
     * Return the integral log with respect to the given base if it exists, 0 otherwise. n is presumed
     * positive. base is presumed to be at least 2.
     */
    private static long getIntLog(BigInteger n, int base) {
        final double nAsDouble = n.doubleValue();
        final double approx = Math.log(nAsDouble) / Math.log(base);
        // A relatively quick test first.
        // Try something else for values to big for a double.
        if (Double.isInfinite(nAsDouble)) {
            // Floating point test doesn't help. Try another quick test.
            if (base % 2 != 0 && !n.testBit(0)) {
                // Has a divisor of 2. Can't be a power of an odd number.
                return 0;
            }
            if (base % 3 != 0 && n.remainder(BIG_THREE).signum() == 0) {
                return 0;
            }
            if (base % 5 != 0 && n.remainder(BIG_FIVE).signum() == 0) {
                return 0;
            }
        } else if (Math.abs(approx - Math.rint(approx)) > 1.0e-6) {
            return 0;
        }
        // It's important to avoid allocating large numbers of large BigIntegers here.
        // In particular, we need to avoid e.g. repeatedly dividing by base.
        // Otherwise computations like log(100,000!) can behave very badly.
        // This algorithm performs worst case O(log(log n)) BigInteger operations.
        // We build a set of powers of base by repeated squaring, with powers[i] == base^(2^i).
        long result = 0;
        final ArrayList<BigInteger> powers = new ArrayList<>();
        powers.add(BigInteger.valueOf(base));
        BigInteger nReduced = n; // always equal to n/base^result .
        for (int i = 1; ; ++i) {
            final BigInteger last = powers.get(i - 1);
            final BigInteger next = last.multiply(last); // base^(2^i)
            if (next.bitLength() > nReduced.bitLength()) {
                break;
            }
            if (Thread.interrupted()) {
                throw new CR.AbortedException();
            }
            final BigInteger[] qAndR = nReduced.divideAndRemainder(next);
            if (qAndR[1].signum() != 0) {
                // A power of base smaller than 2 * nReduced didn't divide nReduced.
                // nReduced, and thus n, are clearly not a power of base.
                return 0;
            }
            powers.add(next);
            // Since we have the quotient, opportunistically reduce n.
            result += (1 << i);
            nReduced = qAndR[0];
        }
        // We've computed all repeated squaring powers <= nReduced.
        // Use those to divide repeatedly until we get to one, or determine it's not
        // a power of base.
        for (int i = powers.size() - 1; nReduced.compareTo(BigInteger.ONE) != 0; --i) {
            final BigInteger power = powers.get(i);
            if (power.bitLength() <= nReduced.bitLength()) {
                if (Thread.interrupted()) {
                    throw new CR.AbortedException();
                }
                final BigInteger[] qAndR = nReduced.divideAndRemainder(power);
                if (qAndR[1].signum() != 0) {
                    return 0;
                }
                result += (1 << i);
                nReduced = qAndR[0];
                // Now power.bitLength() > nReduced.bitLength() .
                // Otherwise we would have divided by the next bigger power, which is power^2.
            }
        }
        return result;
    }

    /**
     * If the argument is a positive power of 10, return its base 10 logarithm. Otherwise
     * return 0. We assume r strictly greater than 1.
     */
    private static long intLog10(@NonNull BoundedRational r) {
        final BigInteger rAsInt = BoundedRational.asBigInteger(r);
        if (rAsInt == null || rAsInt.signum() <= 0) {
            return 0;
        }
        return getIntLog(rAsInt, 10);
    }

    /**
     * Return lg(n) as k lg(m) if there is a small integer m such that n = m^k. Lg is either
     * log or ln, depending on the kind argument. Return null if there are no such m and k.
     */
    private static UnifiedReal lgSmallPower(byte kind, BigInteger n) {
        for (int m : SMALL_NON_POWERS) {
            final long intLog = getIntLog(n, m);
            final CR newCrValue;
            if (intLog != 0) {
                if (kind == IS_LOG) {
                    if (m == 10) {
                        return new UnifiedReal(new BoundedRational(intLog));
                    }
                    newCrValue = CR.valueOf(m).ln().divide(CR_LN10);
                } else {
                    newCrValue = CR.valueOf(m).ln();
                }
                return new UnifiedReal(new BoundedRational(intLog), newCrValue,
                        makeProperty(kind, BoundedRational.valueOf(m)));
            }
        }
        return null;
    }

    // Small integers for which we try to recognize ln(small_int^n), so we can simplify it to
    // n*ln(small_int).
    private static final int[] SMALL_NON_POWERS = {2, 3, 5, 6, 7, 10};

    public UnifiedReal ln() {
        BoundedRational expArg = getExpArg(crProperty);
        CRProperty newCrProperty = null;
        if (expArg != null) {
            return new UnifiedReal(ratFactor).ln().add(new UnifiedReal(expArg));
        }
        int signum = signum(DEFAULT_COMPARISON_TOLERANCE);
        if (signum < 0) {
            throw new DomainException("log(negative)");
        }
        if (isComparable(ZERO)) {
            if (signum == 0) {
                throw new DomainException("log(zero)");
            }
            final int compare1 = compareTo(ONE, DEFAULT_COMPARISON_TOLERANCE);
            if (compare1 == 0) {
                if (definitelyEquals(ONE)) {
                    return ZERO;
                }
            } else if (compare1 < 0) {
                return inverse().ln().negate();
            }
            final BigInteger bi = BoundedRational.asBigInteger(ratFactor);
            if (bi != null) {
                if (isOne(crProperty)) {
                    UnifiedReal smallPowerLn = lgSmallPower(IS_LN, bi);
                    if (smallPowerLn != null) {
                        return  smallPowerLn;
                    }
                } else {
                    // Check for n^k * sqrt(n), for which we can also return a more useful answer.
                    final BigInteger square = BoundedRational.asBigInteger(getSqrtArg(crProperty));
                    if (square != null && square.bitLength() < 30) {
                        final int intSquare = square.intValue();
                        final long intLog = getIntLog(bi, intSquare);
                        if (intLog != 0) {
                            final BoundedRational nRatFactor =
                                    BoundedRational.add(BoundedRational.valueOf(intLog), BoundedRational.HALF);
                            if (nRatFactor != null) {
                                return new UnifiedReal(nRatFactor, CR.valueOf(square).ln(),
                                        makeProperty(IS_LN, new BoundedRational(square)));
                            }
                        }
                    }
                }
            }
            if (isOne(crProperty)) {
                // Normalize to argument > 1, and remember symbolic representation.
                return logRep(IS_LN, ratFactor);
            }
        }
        return new UnifiedReal(crValue().ln(), newCrProperty);
    }

    /**
     * Base 10 logarithm
     */
    public UnifiedReal log() {
        return ln().divide(UnifiedReal.LN10);
    }

    public UnifiedReal exp() {
        if (definitelyEquals(ZERO)) {
            return ONE;
        }
        if (definitelyEquals(ONE)) {
            // Avoid redundant computations, and ensure we recognize all instances as equal.
            return E;
        }
        final BoundedRational lnArg = getLnArg(crProperty);
        if (lnArg != null) {
            boolean needSqrt = false;
            BoundedRational ratExponent = ratFactor;
            final BigInteger asBI = BoundedRational.asBigInteger(ratExponent);
            if (asBI == null) {
                // check for multiple of one half.
                needSqrt = true;
                ratExponent = BoundedRational.multiply(ratExponent, BoundedRational.TWO);
            }
            final BoundedRational nRatFactor = BoundedRational.pow(lnArg, ratExponent);
            if (nRatFactor != null) {
                UnifiedReal result = new UnifiedReal(nRatFactor);
                if (needSqrt) {
                    result = result.sqrt();
                }
                return result;
            }
        }
        if (compareTo(BIT_LIMIT_AS_UR, 0) > 0) {
            throw new TooBigException("exp argument is too big");
        }
        CRProperty newCrProperty = null;
        if (isOne(crProperty)) {
            newCrProperty = makeProperty(IS_EXP, ratFactor);
        }
        return new UnifiedReal(crValue().exp(), newCrProperty);
    }

    /**
     * Absolute value.
     */
    UnifiedReal abs() {
        if (isComparable(ZERO)) {
            return signum() < 0 ? negate() : this;
        } else {
            return new UnifiedReal(crValue().abs(),
                    (isUnknownIrrational(crProperty) ? makeProperty(IS_IRRATIONAL, null) : null));
        }
    }

    /**
     * Generalized factorial. Compute n * (n - step) * (n - 2 * step) * etc. This can be used to
     * compute factorial a bit faster, especially if BigInteger uses sub-quadratic multiplication.
     */
    private static BigInteger genFactorial(long n, long step) {
        if (n > 4 * step) {
            final BigInteger prod1 = genFactorial(n, 2 * step);
            if (Thread.interrupted()) {
                throw new CR.AbortedException();
            }
            final BigInteger prod2 = genFactorial(n - step, 2 * step);
            if (Thread.interrupted()) {
                throw new CR.AbortedException();
            }
            return prod1.multiply(prod2);
        } else {
            if (n == 0) {
                return BigInteger.ONE;
            }
            BigInteger res = BigInteger.valueOf(n);
            for (long i = n - step; i > 1; i -= step) {
                res = res.multiply(BigInteger.valueOf(i));
            }
            return res;
        }
    }

    /**
     * Factorial function. Fails if argument is clearly not an integer. May round to nearest integer
     * if value is close.
     */
    public UnifiedReal fact() {
        BigInteger asBI = bigIntegerValue();
        if (asBI == null) {
            asBI = crValue().get_appr(0); // Correct if it was an integer.
            if (!approxEquals(new UnifiedReal(asBI), DEFAULT_COMPARISON_TOLERANCE)) {
                throw new DomainException("Non-integral factorial argument");
            }
        }
        if (asBI.signum() < 0) {
            throw new DomainException("Negative factorial argument");
        }
        if (asBI.bitLength() > 18) {
            // Several million digits. Will fail.  LongValue() may not work. Punt now.
            throw new TooBigException("Factorial argument too big");
        }
        final BigInteger biResult = genFactorial(asBI.longValue(), 1);
        final BoundedRational nRatFactor = new BoundedRational(biResult);
        return new UnifiedReal(nRatFactor);
    }

    /**
     * Returns an n such that the absolute value of the associated constructive real is no less
     * than than 2^n. n may be negative. Integer.MIN_VALUE indicates we didn't find a bound.
     */
    private static int msbBound(CRProperty p) {
        if (p == null) {
            return Integer.MIN_VALUE;
        }
        switch(p.kind) {
            case IS_ONE:
                return 0;
            case IS_PI:
                return 1;
            case IS_SQRT:
                int wnb = p.arg.wholeNumberBits();
                if (wnb == Integer.MIN_VALUE) {
                    return wnb;
                } else {
                    return (p.arg.wholeNumberBits() >> 1) - 2;
                }
            case IS_LN:
            case IS_LOG:
                if (p.arg.compareTo(BoundedRational.TWO) >= 0) {
                    // ln(2) > log(2) > 1/4
                    return -2;
                } else {
                    // Argument is in the vicinity of 1, result may be near zero.
                    return Integer.MIN_VALUE;
                }
            case IS_EXP:
                BigInteger result = p.arg.floor();
                int signum = result.signum();
                if (result.bitLength() <= 30) {
                    if (signum >= 0) {
                        // multiply by a bit less than 1/ln(2).
                        return (result.intValue() / 5) * 7;
                    } else {
                        // multiply by a bit more than 1/ln(2), making sure we err on correct side.
                        return (result.intValue() / 2 - 1) * 3;
                    }
                } else if (signum > 0) {
                    // Positive and takes > 30 bits to represent.
                    return 100_000_000;
                } else {
                    return Integer.MIN_VALUE;
                }
            case IS_SIN_PI:
            case IS_TAN_PI:
            case IS_ASIN:
            case IS_ATAN:
                // These all behave like x or <pi>x near zero. Thus the following very rough estimate holds.
                if (p.arg.compareTo(BR1_1024) >= 1) {
                    return -11;
                }
                return Integer.MIN_VALUE;
            case IS_IRRATIONAL:
                return Integer.MIN_VALUE;
            default:
                throw new AssertionError(); // Can't get here.
        }
    }

    private static final BoundedRational BR1_1024 = new BoundedRational(1, 1024);

    /**
     * Return the number of decimal digits to the right of the decimal point required to represent
     * the argument exactly. Return Integer.MAX_VALUE if that's not possible. Never returns a value
     * less than zero, even if r is a power of ten.
     */
    public int digitsRequired() {
        if (isOne(crProperty) || ratFactor.signum() == 0) {
            return BoundedRational.digitsRequired(ratFactor);
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Return an upper bound on the number of leading zero bits. These are the number of 0 bits to the
     * right of the binary point and to the left of the most significant digit. Return
     * Integer.MAX_VALUE if we cannot bound it based only on the rational factor and property.
     */
    public int leadingBinaryZeroes() {
        int crBound = msbBound(crProperty); // lower bound on binary log.
        if (crBound != Integer.MIN_VALUE) {
            final int wholeBits = ratFactor.wholeNumberBits();
            if (wholeBits == Integer.MIN_VALUE) {
                return Integer.MAX_VALUE;
            }
            if (wholeBits + crBound >= 3) {
                return 0;
            } else {
                return -(wholeBits + crBound) + 3;
            }
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Is the number of bits to the left of the decimal point greater than bound? The result is
     * inexact: We roughly approximate the whole number bits. bound is non-negative.
     */
    public boolean approxWholeNumberBitsGreaterThan(int bound) {
        check(bound >= 0);
        final int crBound = msbBound(crProperty);
        final int ratBits = ratFactor.wholeNumberBits();
        if (crBound != Integer.MIN_VALUE && ratBits != Integer.MIN_VALUE) {
            return ratBits + crBound > bound;
        } else {
            return crValue().get_appr(bound - 2).bitLength() > 2;
        }
    }

    public static class ZeroDivisionException extends ArithmeticException {
        public ZeroDivisionException(String s) {
            super(s);
        }
    }

    public static class TooBigException extends ArithmeticException {
        public TooBigException(String s) {
            super(s);
        }
    }

    public static class DomainException extends ArithmeticException {
        public DomainException(String s) {
            super(s);
        }
    }

    public static class ZeroToTheZerothException extends ArithmeticException {
        public ZeroToTheZerothException(String s) {
            super(s);
        }
    }
}