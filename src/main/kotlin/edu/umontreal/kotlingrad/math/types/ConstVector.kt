package edu.umontreal.kotlingrad.math.types

import edu.umontreal.kotlingrad.math.algebra.Field
import edu.umontreal.kotlingrad.math.functions.VectorFunction

//typealias ConstVector<X> = VectorFunction<Const<X>>
class ConstVector<X: Field<X>>(vararg contents: Const<X>): VectorFunction<X>(*contents)