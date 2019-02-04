package edu.umontreal.kotlingrad.functions.types

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.functions.Function
import edu.umontreal.kotlingrad.functions.Function.Const
import edu.umontreal.kotlingrad.functions.VectorFunction

//typealias ConstVector<X> = VectorFunction<Const<X>>
class ConstVector<X: Field<X>>(vararg contents: Const<X>): VectorFunction<X>(*contents) {
  override val one: Vector<Function<X>>
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
  override val zero: Vector<Function<X>>
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}