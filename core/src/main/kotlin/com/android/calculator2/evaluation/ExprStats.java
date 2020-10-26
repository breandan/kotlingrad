/*
 * Copyright (C) 2019 The Android Open Source Project
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
// Used by both UnifiedReal and CalculatorExpr; doesn't really fit in either place.
/**
 * Simple helper class to hold summary statistics for the contents of an expression.
 * Sometimes useful for comparing the complexity of equivalent expressions.
 */
class ExprStats {
    // The bit length of a constant is the bit length of its normalized
    // rational or integer representation.
    // Operator counts include pi and e constants.
    // PreEvals count as constants of length 100 appearing as arguments to "interesting"
    // functions.
    public int nCommonOps;  // Total # of +, -, * operators, pi and e constants.
    public int nOps;  // Total # of operators.
    public int transcendentalFns; // Number of trig, exponential, log function invocations.
    public int totalConstBitLength;  // Total length in bits of all constants.
    public int interestingConstBitLength;  // Total len of constants appearing as sqrt etc. args.
    public int nDecimals;  // Total number of decimal points or exponents in constants.
    public int nUncommonOps() {
        return nOps - nCommonOps;
    }
    public void add(ExprStats other) {
        nCommonOps += other.nCommonOps;
        nOps += other.nOps;
        transcendentalFns += other.transcendentalFns;
        totalConstBitLength += other.totalConstBitLength;
        interestingConstBitLength += other.interestingConstBitLength;
        nDecimals += other.nDecimals;
    }
}