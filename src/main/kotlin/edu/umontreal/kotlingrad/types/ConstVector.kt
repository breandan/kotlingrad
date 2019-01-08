package edu.umontreal.kotlingrad.types

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.VectorFunction

//typealias ConstVector<X> = VectorFunction<Const<X>>
class ConstVector<X: Field<X>>(vararg contents: Const<X>): VectorFunction<X>(*contents)