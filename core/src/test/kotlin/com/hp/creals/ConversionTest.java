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


package com.hp.creals;


import org.junit.jupiter.api.Test;

import java.util.Random;

public class ConversionTest {
    private static void check(boolean x, String s) {
        if (!x) throw new Error(s);
    }

    final static int NRANDOM = 100;    // Number of random values to
    // test.  Bigger ==> slower
    final CR BIG = CR.valueOf(Double.MAX_VALUE);
    final CR HUGE = BIG.multiply(BIG);
    final CR TINY = CR.ONE.shiftRight(1078);

    public void checkDoubleConversion(double x) {
        if (!Double.isNaN(x) && !Double.isInfinite(x)) {
            CR crValue = CR.valueOf(x);
            check(x == crValue.doubleValue(), "double conversion: " + x);
        }
    }

    @Test
    public void checkFloatConversion(float f) {
        if (!Float.isNaN(f) && !Float.isInfinite(f)) {
            CR crValue = CR.valueOf(f);
            check(f == crValue.floatValue(), "float conversion: " + f);
        }
    }

    @Test
    public void checkNearbyConversions(double x) {
        checkDoubleConversion(x);
        checkDoubleConversion(Math.nextAfter(x, Double.NEGATIVE_INFINITY));
        checkDoubleConversion(Math.nextAfter(x, Double.POSITIVE_INFINITY));
        float f = (float) x;
        checkFloatConversion(f);
        checkFloatConversion(Math.nextAfter(f, Double.NEGATIVE_INFINITY));
        checkFloatConversion(Math.nextAfter(f, Double.POSITIVE_INFINITY));
    }

    @Test
    public void testConversions() {
        check(TINY.doubleValue() == 0.0d, "Tiny.doubleValue()");
        checkNearbyConversions(0.0d);
        checkNearbyConversions(Double.MAX_VALUE);
        checkNearbyConversions(Double.MIN_VALUE);
        check(HUGE.doubleValue() == Double.POSITIVE_INFINITY,
                "double +infinity");
        check(HUGE.negate().doubleValue() == Double.NEGATIVE_INFINITY,
                "double -infinity");
        check(HUGE.floatValue() == Float.POSITIVE_INFINITY,
                "float +infinity");
        check(HUGE.negate().floatValue() == Float.NEGATIVE_INFINITY,
                "float -infinity");
        for (double x = 1.0d; x != 0.0d; x /= 2.0d) {
            checkNearbyConversions(x);
        }
        for (double x = -1.0d; x != Double.NEGATIVE_INFINITY; x *= 2.0d) {
            checkNearbyConversions(x);
        }
        Random r = new Random();  // Random seed!
        for (int i = 0; i < NRANDOM; ++i) {
            double d = Math.exp(1000.0 * r.nextDouble());
            if (r.nextBoolean()) d = -d;
            checkNearbyConversions(d);
        }
    }
}
