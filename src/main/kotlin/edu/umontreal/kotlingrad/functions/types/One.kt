package edu.umontreal.kotlingrad.functions.types

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.numerical.FieldPrototype

class One<X: Field<X>>(fieldPrototype: FieldPrototype<X>): Const<X>(fieldPrototype.one)