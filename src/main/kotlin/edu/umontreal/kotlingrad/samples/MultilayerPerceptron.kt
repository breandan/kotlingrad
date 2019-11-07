package edu.umontreal.kotlingrad.samples

fun <X: Fun<X>> sigmoid(x: Fun<X>) = One<X>() / (One<X>() + E<X>().pow(-x))

fun <X: Fun<X>> layer1(x: VFun<X, D4>) =

fun main() {

}