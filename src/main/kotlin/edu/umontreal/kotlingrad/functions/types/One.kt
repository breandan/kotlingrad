package edu.umontreal.kotlingrad.functions.types

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.algebra.RealPrototype

class One<X: Field<X>>(realPrototype: RealPrototype<X>): Const<X>(realPrototype.one, realPrototype)