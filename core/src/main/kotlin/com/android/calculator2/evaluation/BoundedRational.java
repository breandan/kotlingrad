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

package com.android.calculator2.evaluation;

import com.android.calculator2.StringUtils;
import com.hp.creals.CR;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Random;

/**
 * Rational numbers that may turn to null if they get too big. For many operations, if the length of
 * the nuumerator plus the length of the denominator exceeds a maximum size, we simply return null,
 * and rely on our caller do something else. We currently never return null for a pure integer or
 * for a BoundedRational that has just been constructed.
 *
 * <p>We also implement a number of irrational functions. These return a non-null result only when
 * the result is known to be rational.
 */
public class BoundedRational {
    // TODO: Consider returning null for integers.  With some care, large factorials might become
    // much faster.
    // TODO: Maybe eventually make this extend Number?

    private static final int MAX_SIZE = 10000; // total, in bits

    public static final BoundedRational ZERO = new BoundedRational(0);
    public static final BoundedRational HALF = new BoundedRational(1, 2);
    public static final BoundedRational MINUS_HALF = new BoundedRational(-1, 2);
    public static final BoundedRational THIRD = new BoundedRational(1, 3);
    public static final BoundedRational MINUS_THIRD = new BoundedRational(-1, 3);
    public static final BoundedRational QUARTER = new BoundedRational(1, 4);
    public static final BoundedRational MINUS_QUARTER = new BoundedRational(-1, 4);
    public static final BoundedRational SIXTH = new BoundedRational(1, 6);
    public static final BoundedRational MINUS_SIXTH = new BoundedRational(-1, 6);
    public static final BoundedRational ONE = new BoundedRational(1);
    public static final BoundedRational MINUS_ONE = new BoundedRational(-1);
    public static final BoundedRational TWO = new BoundedRational(2);
    public static final BoundedRational MINUS_TWO = new BoundedRational(-2);
    public static final BoundedRational THREE = new BoundedRational(3);
    public static final BoundedRational TEN = new BoundedRational(10);
    public static final BoundedRational TWELVE = new BoundedRational(12);
    public static final BoundedRational THIRTY = new BoundedRational(30);
    public static final BoundedRational MINUS_THIRTY = new BoundedRational(-30);
    public static final BoundedRational FORTY_FIVE = new BoundedRational(45);
    public static final BoundedRational MINUS_FORTY_FIVE = new BoundedRational(-45);
    public static final BoundedRational NINETY = new BoundedRational(90);
    public static final BoundedRational MINUS_NINETY = new BoundedRational(-90);

    // Max integer for which extractSquare is guaranteed to be optimal.
    // We currently fail to so for 44 = 11*4, but succeed for all perfect squares*n, with n <= 10
    // and numerator and denominator size < EXTRACT_SQUARE_MAX_LEN.
    public static final int EXTRACT_SQUARE_MAX_OPT = 43;

    private static final BigInteger BIG_MINUS_ONE = BigInteger.valueOf(-1);
    private static final BigInteger BIG_FIVE = BigInteger.valueOf(5);

    private static final Random REDUCE_RNG = new Random();

    private final BigInteger numerator;
    private final BigInteger denominator;

    public BoundedRational(BigInteger n, BigInteger d) {
        numerator = n;
        denominator = d;
    }

    public BoundedRational(BigInteger n) {
        numerator = n;
        denominator = BigInteger.ONE;
    }

    public BoundedRational(long n, long d) {
        numerator = BigInteger.valueOf(n);
        denominator = BigInteger.valueOf(d);
    }

    public BoundedRational(long n) {
        numerator = BigInteger.valueOf(n);
        denominator = BigInteger.ONE;
    }

    /** Produce BoundedRational equal to the given double. */
    public static BoundedRational valueOf(double x) {
        final long l = Math.round(x);
        if ((double) l == x && Math.abs(l) <= 1000) {
            return valueOf(l);
        }
        final long allBits = Double.doubleToRawLongBits(Math.abs(x));
        long mantissa = (allBits & ((1L << 52) - 1));
        final int biased_exp = (int) (allBits >>> 52);
        if ((biased_exp & 0x7ff) == 0x7ff) {
            throw new ArithmeticException("Infinity or NaN not convertible to BoundedRational");
        }
        final long sign = x < 0.0 ? -1 : 1;
        int exp = biased_exp - 1075; // 1023 + 52; we treat mantissa as integer.
        if (biased_exp == 0) {
            exp += 1; // Denormal exponent is 1 greater.
        } else {
            mantissa += (1L << 52); // Implied leading one.
        }
        BigInteger num = BigInteger.valueOf(sign * mantissa);
        BigInteger den = BigInteger.ONE;
        if (exp >= 0) {
            num = num.shiftLeft(exp);
        } else {
            den = den.shiftLeft(-exp);
        }
        return new BoundedRational(num, den);
    }

    /** Produce BoundedRational equal to the given long. */
    public static BoundedRational valueOf(long x) {
        if (x >= -2 && x <= 10) {
            switch ((int) x) {
                case -2:
                    return MINUS_TWO;
                case -1:
                    return MINUS_ONE;
                case 0:
                    return ZERO;
                case 1:
                    return ONE;
                case 2:
                    return TWO;
                case 10:
                    return TEN;
                default:
                    break;
            }
        }
        return new BoundedRational(x);
    }

    /** Convert to String reflecting raw representation. Debug or log messages only, not pretty. */
    @Override
    public String toString() {
        return numerator.toString() + "/" + denominator.toString();
    }

    /**
     * Convert to readable String. Intended for output to user. More expensive, less useful for
     * debugging than toString(). If mixed is true, convert improper fractions to mixed fractions. If
     * subsuperscript is true, use subscript and superscript characters to represent the fraction.
     * Subsuperscript relies on unicode characters intended for this purpose. Not internationalized.
     */
    public String toNiceString(boolean subsuperscript, boolean mixed) {
        final BoundedRational nicer = reduce().positiveDen();
        BigInteger num = nicer.numerator.abs();
        BigInteger den = nicer.denominator;
        final boolean negative = (nicer.numerator.signum() < 0);
        BigInteger whole = null;
        if (nicer.denominator.equals(BigInteger.ONE)) {
            whole = num;
            num = BigInteger.ZERO;
        } else if (mixed && num.compareTo(den) >= 0) {
            BigInteger[] divRem = num.divideAndRemainder(den);
            whole = divRem[0];
            num = divRem[1];
        }
        final String renderedMinus =
                (negative ? (whole == null && subsuperscript ? SUPERSCRIPT_MINUS : "-") : "");
        StringBuilder result = new StringBuilder(renderedMinus);
        if (whole != null) {
            result.append(whole);
        }
        // num == 0 ==> whole non-null.
        if (num.signum() == 0) {
            return result.toString();
        }
        String numString = num.toString();
        String denString = den.toString();
        if (whole != null) {
            // Need a separator.
            result.append(" ");
        }
        // FRACTION_SLASH was intended to cause preceding and following sequences of
        // digits to be rendered as a fraction. Apparently that often doesn't work,
        // but on Android it does. So the conversion to subscripts and superscripts
        // here is implicit. It does not appear to work for a leading minus, which
        // thus looks better as an explicit superscript.
        result.append(numString).append(subsuperscript ? FRACTION_SLASH : "/").append(denString);
        return result.toString();
    }

    /**
     * Convert to readable String, as a potentialy improper fraction. Use only basic characters
     * representable as a single UTF-8 byte. Intended for output to user. Not internationalized.
     */
    public String toNiceString() {
        return toNiceString(false, false);
    }

    private static final char FRACTION_SLASH = '\u2044';
    private static final String SUPERSCRIPT_MINUS = "\u207B";

    public BigInteger[] getNumDen() {
        final BoundedRational nicer = reduce().positiveDen();
        return new BigInteger[] {nicer.numerator, nicer.denominator};
    }

    /**
     * Returns a truncated (rounded towards 0) representation of the result. Includes n digits to the
     * right of the decimal point.
     *
     * @param n result precision, >= 0
     */
    public String toStringTruncated(int n) {
        String digits =
                numerator.abs().multiply(BigInteger.TEN.pow(n)).divide(denominator.abs()).toString();
        int len = digits.length();
        if (len < n + 1) {
            digits = StringUtils.repeat('0', n + 1 - len) + digits;
            len = n + 1;
        }
        return (signum() < 0 ? "-" : "")
                + digits.substring(0, len - n)
                + "."
                + digits.substring(len - n);
    }

    /**
     * Return a double approximation. The result is correctly rounded to nearest, with ties rounded
     * away from zero. TODO: Should round ties to even.
     */
    public double doubleValue() {
        final int sign = signum();
        if (sign < 0) {
            return -BoundedRational.negate(this).doubleValue();
        }

        // We get the mantissa by dividing the numerator by denominator, after
        // suitably prescaling them so that the integral part of the result contains
        // enough bits. We do the prescaling to avoid any precision loss, so the division result
        // is correctly truncated towards zero.
        final int apprExp = numerator.bitLength() - denominator.bitLength();
        if (apprExp < -1100 || sign == 0) {
            // Bail fast for clearly zero result.
            return 0.0;
        }

        final int neededPrec = apprExp - 80;
        final BigInteger dividend = neededPrec < 0 ? numerator.shiftLeft(-neededPrec) : numerator;
        final BigInteger divisor = neededPrec > 0 ? denominator.shiftLeft(neededPrec) : denominator;
        final BigInteger quotient = dividend.divide(divisor);
        final int qLength = quotient.bitLength();
        int extraBits = qLength - 53;
        int exponent = neededPrec + qLength; // Exponent assuming leading binary point.
        if (exponent >= -1021) {
            // Binary point is actually to right of leading bit.
            --exponent;
        } else {
            // We're in the gradual underflow range. Drop more bits.
            extraBits += (-1022 - exponent) + 1;
            exponent = -1023;
        }
        final BigInteger bigMantissa =
                quotient.add(BigInteger.ONE.shiftLeft(extraBits - 1)).shiftRight(extraBits);
        if (exponent > 1024) {
            return Double.POSITIVE_INFINITY;
        }
        if (exponent > -1023 && bigMantissa.bitLength() != 53
                || exponent <= -1023 && bigMantissa.bitLength() >= 53) {
            throw new AssertionError("doubleValue internal error");
        }
        final long mantissa = bigMantissa.longValue();
        final long bits = (mantissa & ((1L << 52) - 1)) | (((long) exponent + 1023) << 52);
        return Double.longBitsToDouble(bits);
    }

    public CR crValue() {
        return CR.valueOf(numerator).divide(CR.valueOf(denominator));
    }

    public int intValue() {
        BoundedRational reduced = reduce();
        if (!reduced.denominator.equals(BigInteger.ONE)) {
            throw new ArithmeticException("intValue of non-int");
        }
        return reduced.numerator.intValue();
    }

    /**
     * Approximate number of bits to left of binary point.
     * Negative indicates leading zeroes to the right of binary point.
     */
    public int wholeNumberBits() {
        if (numerator.signum() == 0) {
            return Integer.MIN_VALUE;
        } else {
            return numerator.bitLength() - denominator.bitLength();
        }
    }

    /**
     * Number of bits in the representation. Makes the most sense for the result of reduce(),
     * since it does not implicitly reduce.
     */
    public int bitLength() {
        return numerator.bitLength() + denominator.bitLength();
    }

    /*
     * Return an approximation of the base 2 log of the absolute value.
     * We assume this is nonzero.
     * We try to be reasonably accurate around 1.  When in doubt we return 0.
     * The result is either 0 or within 20% of the truth.
     */
    public double apprLog2Abs() {
        final int wholeBits = wholeNumberBits();
        if (wholeBits > 10 || wholeBits < -10) {
            // Bit lengths suffice for our purposes.
            return (double) (wholeBits);
        } else {
            // Argument is in the vicinity of one. numerator and denominator are nonzero, but may be
            // individually huge.
            final double quotient = Math.abs(numerator.doubleValue() / denominator.doubleValue());
            if (Double.isInfinite(quotient) || Double.isNaN(quotient) || quotient == 0.0) {
                // Zero quotient means denominator overflowed and is meaningless. Ignore.
                return 0.0;
            }
            return Math.log(quotient) / Math.log(2.0);
        }
    }

    /**
     * Is this number too big for us to continue with rational arithmetic? We return false for
     * integers on the assumption that we have no better fallback.
     */
    private boolean tooBig() {
        return !denominator.equals(BigInteger.ONE)
                && (numerator.bitLength() + denominator.bitLength() > MAX_SIZE);
    }

    /** Return an equivalent fraction with a positive denominator. */
    private BoundedRational positiveDen() {
        if (denominator.signum() > 0) {
            return this;
        }
        return new BoundedRational(numerator.negate(), denominator.negate());
    }

    /** Return an equivalent fraction in lowest terms. Denominator sign may remain negative. */
    public BoundedRational reduce() {
        if (denominator.equals(BigInteger.ONE)) {
            return this; // Optimization only
        }
        final BigInteger divisor = numerator.gcd(denominator);
        return new BoundedRational(numerator.divide(divisor), denominator.divide(divisor));
    }

    /** Return a possibly reduced version of r that's not tooBig(). Return null if none exists. */
    private static BoundedRational maybeReduce(BoundedRational r) {
        if (r == null) return null;
        // Reduce randomly, with 1/16 probability, or if the result is too big.
        if (!r.tooBig() && (REDUCE_RNG.nextInt() & 0xf) != 0) {
            return r;
        }
        BoundedRational result = r.positiveDen();
        result = result.reduce();
        if (!result.tooBig()) {
            return result;
        }
        return null;
    }

    public int compareTo(BoundedRational r) {
        // If the signs differ, don't bother with the multiplications.
        final int sign1 = signum();
        final int sign2 = r.signum();
        if (sign1 > sign2) {
            return 1;
        } else if (sign1 < sign2) {
            return -1;
        }
        // Compare by multiplying both sides by denominators, invert result if denominator product
        // was negative.
        return numerator.multiply(r.denominator).compareTo(r.numerator.multiply(denominator))
                * denominator.signum()
                * r.denominator.signum();
    }

    /**
     * Equivalent to CompareTo(BoundedRational.ONE). Allows a faster implementation.
     */
    public int compareToOne() {
        return numerator.compareTo(denominator) * denominator.signum();
    }

    public int signum() {
        return numerator.signum() * denominator.signum();
    }

    @Override
    public int hashCode() {
        // Note that this may be too expensive to be useful.
        final BoundedRational reduced = reduce().positiveDen();
        return Objects.hash(reduced.numerator, reduced.denominator);
    }

    @Override
    public boolean equals(Object r) {
        return r != null && r instanceof BoundedRational && compareTo((BoundedRational) r) == 0;
    }

    // We use static methods for arithmetic, so that we can easily handle the null case.  We try
    // to catch domain errors whenever possible, sometimes even when one of the arguments is null,
    // but not relevant.

    /** Returns equivalent BigInteger result if it exists, null if not. */
    public static BigInteger asBigInteger(BoundedRational r) {
        if (r == null) {
            return null;
        }
        final BigInteger[] quotAndRem = r.numerator.divideAndRemainder(r.denominator);
        if (quotAndRem[1].signum() == 0) {
            return quotAndRem[0];
        } else {
            return null;
        }
    }

    /** Returns closest integer no larger than this. */
    public BigInteger floor() {
        final BigInteger[] quotAndRem = numerator.divideAndRemainder(denominator);
        BigInteger result = quotAndRem[0];
        if (quotAndRem[1].signum() < 0) {
            result = result.subtract(BigInteger.ONE);
        }
        return result;
    }

    public static BoundedRational add(BoundedRational r1, BoundedRational r2) {
        if (r1 == null || r2 == null) {
            return null;
        }
        final BigInteger den = r1.denominator.multiply(r2.denominator);
        final BigInteger num =
                r1.numerator.multiply(r2.denominator).add(r2.numerator.multiply(r1.denominator));
        return maybeReduce(new BoundedRational(num, den));
    }

    /** Return the argument, but with the opposite sign. Returns null only for a null argument. */
    public static BoundedRational negate(BoundedRational r) {
        if (r == null) {
            return null;
        }
        return new BoundedRational(r.numerator.negate(), r.denominator);
    }

    public static BoundedRational subtract(BoundedRational r1, BoundedRational r2) {
        return add(r1, negate(r2));
    }

    /** Return product of r1 and r2 without reducing the result. */
    @SuppressWarnings("ReferenceEquality")
    public static BoundedRational rawMultiply(BoundedRational r1, BoundedRational r2) {
        // It's tempting but marginally unsound to reduce 0 * null to 0.  The null could represent
        // an infinite value, for which we failed to throw an exception because it was too big.
        if (r1 == null || r2 == null) {
            return null;
        }
        // Optimize the case of our special ONE constant, since that's cheap and somewhat frequent.
        if (r1 == ONE) {
            return r2;
        }
        if (r2 == ONE) {
            return r1;
        }
        final BigInteger num = r1.numerator.multiply(r2.numerator);
        final BigInteger den = r1.denominator.multiply(r2.denominator);
        return new BoundedRational(num, den);
    }

    public static BoundedRational multiply(BoundedRational r1, BoundedRational r2) {
        return maybeReduce(rawMultiply(r1, r2));
    }

    /** Return the reciprocal of r (or null if the argument was null). */
    public static BoundedRational inverse(BoundedRational r) {
        if (r == null) {
            return null;
        }
        if (r.numerator.signum() == 0) {
            throw new ZeroDivisionException();
        }
        return new BoundedRational(r.denominator, r.numerator);
    }

    public static BoundedRational divide(BoundedRational r1, BoundedRational r2) {
        return multiply(r1, inverse(r2));
    }

    /** Return nth root of x if it's an integer, null otherwise */
    private static BigInteger nthRoot(BigInteger x, int n) {
        // This usually performs and throws away a constructive real computation that might be useful.
        // TODO: Adjust the API so we can reuse it?
        final int sign = x.signum();
        if (sign < 0) {
            if ((n & 1) == 0) {
                throw new ArithmeticException("even root(negative)");
            } else {
                return nthRoot(x.negate(), n).negate();
            }
        }
        if (sign == 0) {
            return BigInteger.ZERO;
        }
        final CR xAsCr = CR.valueOf(x);
        CR rootAsCr;
        if (n == 2) {
            rootAsCr = xAsCr.sqrt();
        } else if (n == 4) {
            rootAsCr = xAsCr.sqrt().sqrt();
        } else {
            rootAsCr = xAsCr.ln().divide(CR.valueOf(n)).exp();
        }
        final int scale = -10;
        final BigInteger scaledRoot = rootAsCr.get_appr(scale); // Includes -scale bits to right of b.p.
        final int fracMask = (1 << -scale) - 1;
        final int fracBits = scaledRoot.and(BigInteger.valueOf(fracMask)).intValue();
        if (fracBits != 0 && fracBits != fracMask) {
            // Cannot be within 1ulp of an integer; not a perfect root.
            return null;
        }
        final BigInteger rootAsBI;
        if (fracBits == 0) {
            rootAsBI = scaledRoot.shiftLeft(scale); // Actually right shift.
        } else {
            rootAsBI = scaledRoot.add(BigInteger.ONE).shiftLeft(scale);
        }
        if (rootAsBI.pow(n).equals(x)) {
            return rootAsBI;
        } else {
            return null;
        }
    }

    /**
     * Compute r^(1/n) exactly. Return null if it's irrational. n != 0. Note that this is arguably
     * well-defined when r is negative and n is odd. We produce a meaningful answer in such
     * cases. TODO: Try harder to produce a non-null result.
     */
    public static BoundedRational nthRoot(BoundedRational r, int n) {
        // Return non-null if numerator and denominator are small perfect squares.
        if (r == null) {
            return null;
        }
        if (n < 0) {
            return inverse(nthRoot(r, -n));
        }
        r = r.positiveDen().reduce();
        final BigInteger numRt = nthRoot(r.numerator, n);
        final BigInteger denRt = nthRoot(r.denominator, n);
        return (numRt == null || denRt == null) ? null : new BoundedRational(numRt, denRt);
    }

    public static BoundedRational sqrt(BoundedRational r) {
        return nthRoot(r, 2);
    }

    /**
     * Return a pair p, such that p[0]^2 * p[1] = x.
     * x is assumed positive. We try to maximize p[0], but not very hard.
     */
    private static BigInteger[] extractSquare(BigInteger x) {
        BigInteger square = BigInteger.ONE;
        BigInteger rest = x;
        if (rest.bitLength() > EXTRACT_SQUARE_MAX_LEN) {
            return new BigInteger[] { square, rest };
        }
        for (int i = 0; i < SOME_PRIMES.length; ++i) {
            if (rest.equals(BigInteger.ONE)) {
                break;
            }
            for (;;) {
                final BigInteger[] qr = rest.divideAndRemainder(PRIME_SQUARES[i]);
                if (qr[1].signum() == 0) {
                    rest = qr[0];  // Remaining quotient.
                    square = square.multiply(SOME_PRIMES[i]);
                } else {
                    break;
                }
            }
        }
        // Check whether rest/<small int> is a perfect square
        for (int i = 1; i <= 10; ++i) {
            final BigInteger[] qr = rest.divideAndRemainder(BigInteger.valueOf(i));
            if (qr[1].signum() == 0) {
                final BigInteger root = nthRoot(qr[0], 2);
                if (root != null) {
                    rest = BigInteger.valueOf(i);
                    square = square.multiply(root);
                    break;
                }
            }
        }
        return new BigInteger[] { square, rest };
    }

    // Max bit length for attempting to extract square, so as to not take too much time.
    // Large enough so that computations on floating point numbers cannot easily overflow this.
    private static final int EXTRACT_SQUARE_MAX_LEN = 5000;

    private static final BigInteger[] SOME_PRIMES = {BigInteger.valueOf(2), BigInteger.valueOf(3),
            BigInteger.valueOf(5), BigInteger.valueOf(7), BigInteger.valueOf(11),
            BigInteger.valueOf(13)};

    private static final BigInteger[] PRIME_SQUARES = {BigInteger.valueOf(4), BigInteger.valueOf(9),
            BigInteger.valueOf(25), BigInteger.valueOf(49), BigInteger.valueOf(121),
            BigInteger.valueOf(169)};

    /**
     * Return a pair p, such that p[0]^2 * p[1] = this.
     * We try to maximize p[0]s numerator and denominator, but not very hard.
     * This rational is assumed to be in reduced form.
     */
    public BoundedRational[] extractSquareReduced() {
        if (signum() == 0) {
            return new BoundedRational[] { BoundedRational.ZERO, BoundedRational.ONE };
        }
        BigInteger[] numResult = extractSquare(numerator.abs());
        final BigInteger[] denResult = extractSquare(denominator.abs());
        if (signum() < 0) {
            numResult[1] = numResult[1].negate();
        }
        return new BoundedRational[] { new BoundedRational(numResult[0], denResult[0]),
                new BoundedRational(numResult[1], denResult[1]) };
    }

    /**
     * Will extractSquareReduced guarantee that p[1] is not a perfect square?
     * This rational is assumed to be in reduced form.
     */
    public Boolean extractSquareWillSucceed() {
        // We take the absolute value before extracting the square. That may increase the length by 1.
        // Hence <, not <= .
        return numerator.bitLength() < EXTRACT_SQUARE_MAX_LEN
                && denominator.bitLength() < EXTRACT_SQUARE_MAX_LEN;
    }

    /** Compute integral power of this, assuming this has been reduced and exp is &ge; 0. */
    private BoundedRational rawPow(BigInteger exp) {
        if (exp.equals(BigInteger.ONE)) {
            return this;
        }
        if (exp.and(BigInteger.ONE).intValue() == 1) {
            return rawMultiply(rawPow(exp.subtract(BigInteger.ONE)), this);
        }
        if (exp.signum() == 0) {
            return ONE;
        }
        BoundedRational tmp = rawPow(exp.shiftRight(1));
        if (Thread.interrupted()) {
            throw new CR.AbortedException();
        }
        BoundedRational result = rawMultiply(tmp, tmp);
        if (result == null || result.tooBig()) {
            return null;
        }
        return result;
    }

    /** Compute an integral power of this. */
    public BoundedRational pow(BigInteger exp) {
        int expSign = exp.signum();
        if (expSign == 0) {
            // Questionable if base has undefined or zero value.
            // java.lang.Math.pow() returns 1 anyway, so we do the same.
            return BoundedRational.ONE;
        }
        if (exp.equals(BigInteger.ONE)) {
            return this;
        }
        // Reducing once at the beginning means there's no point in reducing later.
        BoundedRational reduced = reduce().positiveDen();
        // First handle cases in which huge exponents could give compact results.
        if (reduced.denominator.equals(BigInteger.ONE)) {
            if (reduced.numerator.equals(BigInteger.ZERO)) {
                return ZERO;
            }
            if (reduced.numerator.equals(BigInteger.ONE)) {
                return ONE;
            }
            if (reduced.numerator.equals(BIG_MINUS_ONE)) {
                if (exp.testBit(0)) {
                    return MINUS_ONE;
                } else {
                    return ONE;
                }
            }
        }
        if (exp.bitLength() > 1000) {
            // Stack overflow is likely; a useful rational result is not.
            return null;
        }
        if (expSign < 0) {
            return inverse(reduced).rawPow(exp.negate());
        } else {
            return reduced.rawPow(exp);
        }
    }

    public static BoundedRational pow(BoundedRational base, BoundedRational exp) {
        if (exp == null) {
            return null;
        }
        if (base == null) {
            return null;
        }
        exp = exp.reduce().positiveDen();
        if (exp.denominator.bitLength() > 30) {
            return null;
        }
        int expDen = exp.denominator.intValue(); // Doesn't lose information.
        if (expDen == 1) {
            return base.pow(exp.numerator);
        }
        BoundedRational rt = BoundedRational.nthRoot(base, expDen);
        if (rt == null) {
            return null;
        }
        return rt.pow(exp.numerator);
    }

    /**
     * Return the number of decimal digits to the right of the decimal point required to represent the
     * argument exactly. Return Integer.MAX_VALUE if that's not possible. Never returns a value less
     * than zero, even if r is a power of ten.
     */
    public static int digitsRequired(BoundedRational r) {
        if (r == null) {
            return Integer.MAX_VALUE;
        }
        int powersOfTwo = 0; // Max power of 2 that divides denominator
        int powersOfFive = 0; // Max power of 5 that divides denominator
        // Try the easy case first to speed things up.
        if (r.denominator.equals(BigInteger.ONE)) {
            return 0;
        }
        r = r.reduce();
        BigInteger den = r.denominator;
        if (den.bitLength() > MAX_SIZE) {
            return Integer.MAX_VALUE;
        }
        while (!den.testBit(0)) {
            ++powersOfTwo;
            den = den.shiftRight(1);
        }
        while (den.mod(BIG_FIVE).signum() == 0) {
            ++powersOfFive;
            den = den.divide(BIG_FIVE);
        }
        // If the denominator has a factor of other than 2 or 5 (the divisors of 10), the decimal
        // expansion does not terminate.  Multiplying the fraction by any number of powers of 10
        // will not cancel the demoniator.  (Recall the fraction was in lowest terms to start
        // with.) Otherwise the powers of 10 we need to cancel the denominator is the larger of
        // powersOfTwo and powersOfFive.
        if (!den.equals(BigInteger.ONE) && !den.equals(BIG_MINUS_ONE)) {
            return Integer.MAX_VALUE;
        }
        return Math.max(powersOfTwo, powersOfFive);
    }

    public static class ZeroDivisionException extends ArithmeticException {
        public ZeroDivisionException() {
            super("Division by zero");
        }
    }
}