package edu.umontreal.kotlingrad.functions

import edu.umontreal.kotlingrad.algebra.Field
import edu.umontreal.kotlingrad.algebra.Ring
import edu.umontreal.kotlingrad.calculus.Differentiable

//class MatrixFun<X: Field<X>>(override val variables: Set<Var<X>>): Ring<MatrixFun<X>>, Differentiable<X>, Function<X> {
//  override fun unaryMinus(): MatrixFun<X> {
//    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//  }
//
//  override fun plus(addend: MatrixFun<X>): MatrixFun<X> {
//    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//  }
//
//  override fun times(multiplicand: MatrixFun<X>): MatrixFun<X> {
//    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//  }
//
//  override val one: MatrixFun<X>
//    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
//  override val zero: MatrixFun<X>
//    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
//
//  override fun diff(ind: X): X {
//    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//  }
//
//  override fun grad(): Map<X, X> {
//    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//  }
//}