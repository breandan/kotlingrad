package edu.umontreal.kotlingrad.numerical

import edu.umontreal.kotlingrad.calculus.RealFunctor
import java.math.BigDecimal

object BigDecimalPrecision: RealFunctor<BigDecimalReal, BigDecimal>(ProtoBigDecimal)

object DoublePrecision: RealFunctor<DoubleReal, Double>(ProtoDouble)
