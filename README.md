# Kotlin𝛁: A type-safe AD implementation for Kotlin

Inspired by [Stalin∇](https://github.com/Functional-AutoDiff/STALINGRAD), [Autograd](https://github.com/hips/autograd), [DiffSharp](https://github.com/DiffSharp/DiffSharp), [Tangent](https://github.com/google/tangent), et al.

AD is useful for [gradient descent](https://en.wikipedia.org/wiki/Gradient_descent) and has a variety of applications in numerical optimization and machine learning.

We aim to provide an algebraically sound implementation of AD for type safe tensor manipulations.

# Usage

```kotlin
import co.ndan.kotlingrad.math.algebra.DoublePrototype
import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.DoubleFunctor.sin
import co.ndan.kotlingrad.math.types.Double
import co.ndan.kotlingrad.math.types.Var

val x = Var("x", Double(0), DoublePrototype)
val y = Var("y", Double(1), DoublePrototype)

val z = x * (-sin(x * y) + y)      // Operator overloads
val `∂z_∂x`    = d(   z   ) / d(x) // Leibniz notation
val `∂z_∂y`    = d(   z   ) / d(y) // Multiple variables
val `∂²z_∂x²`  = d(`∂z_∂x`) / d(x) // Higher order and
val `∂²z_∂x∂y` = d(`∂z_∂x`) / d(y) // partial derivatives

val p = "${x.value}, ${y.value}"
print("z(x, y) \t\t\t= $z\n" +
  "∂z($p)_∂x \t= " + `∂z_∂x` + "\n\t\t\t\t\t= " + `∂z_∂x`.value + "\n" +
  "∂z($p)_∂y \t= " + `∂z_∂y` + "\n\t\t\t\t\t= " + `∂z_∂y`.value + "\n" +
  "∂²z($p)_∂x² \t= " + `∂z_∂y` + "\n\t\t\t\t\t= " + `∂²z_∂x²`.value + "\n" +
  "∂²z($p)_∂x∂y \t= " + `∂²z_∂x∂y` + "\n\t\t\t\t\t= " + `∂²z_∂x∂y`.value)
```

Running this (`./gradlew run`) should print:

```
z(x, y)             = ((y::1.0 + -sin((y::1.0 * x::0.0))) * x::0.0)
∂z(0.0, 1.0)_∂x     = ((1.0 * (y::1.0 + -sin((y::1.0 * x::0.0)))) + (x::0.0 * -((1.0 * y::1.0) * cos((y::1.0 * x::0.0)))))
                    = 1.0
∂z(0.0, 1.0)_∂y     = ((0.0 * (y::1.0 + -sin((y::1.0 * x::0.0)))) + (x::0.0 * (-(((0.0 * y::1.0) + x::0.0) * cos((y::1.0 * x::0.0))) + 1.0)))
                    = 0.0
∂²z(0.0, 1.0)_∂x²   = ((0.0 * (y::1.0 + -sin((y::1.0 * x::0.0)))) + (x::0.0 * (-(((0.0 * y::1.0) + x::0.0) * cos((y::1.0 * x::0.0))) + 1.0)))
                    = -2.0
∂²z(0.0, 1.0)_∂x∂y  = ((-((-(((0.0 * y::1.0) + x::0.0) * sin((y::1.0 * x::0.0))) * (1.0 * y::1.0)) + cos((y::1.0 * x::0.0))) * x::0.0) + (-(((0.0 * y::1.0) + x::0.0) * cos((y::1.0 * x::0.0))) + 1.0))
                    = 1.0
```

To run the tests: `./gradlew test`

## Ideal API (WIP)

### Scalar functions

```
val x = Variable(1.0)              // x: Variable<Double> inferred type
val y = Variable(1.0)              // x: Variable<Double> "
val f = x * y + sin(2 * x + 3 * y) // f: BinaryFunction<Double> "
val g = f(x to -1.0)               // g: UnaryFunction<Double> == -y + sin(-2 + 3 * y)
val h = f(x to 0.0, y to 0.0)      // h: Const<Double> == 0 + sin(0 + 0) == 0
```

### Vector functions

```
val x = VVariable(0.0, 0.0, 0.0)   // x: VVariable<Double, `3`>
val y = VVariable(0.0, 3)          // x: VVariable<Double, `3`>
val f = 2 * x + x / 2              // f: UnaryVFunction<Double>
val g = f(-2.0, 0.0, 2.0)          // g: ConstVector<`3`> == [-3. 0. 5.]
```

### Matrix functions

```
val x = MVariable(0.0, 0.0, 0.0)   // x: MVariable<Double, `1`, `3`>
val y = MVariable(0.0, 3, 3)       // y: MVariable<Double, `3`, `3`>
val f = sin(2 * x) + log(x / 2)    // f: UnaryMFunction<Double>
val g = f(x) / d(x)                // g: UnaryMFunction<Double>
```

## References

* [A Design Proposal for an Object Oriented Algebraic Library](https://pdfs.semanticscholar.org/6fd2/88960ef83469c898a3d8ed8f0950e7839625.pdf)
* [Efficient Differentiable Programming in a Functional Array-Processing Language](https://arxiv.org/pdf/1806.02136.pdf)
* [First-Class Automatic Differentiation in Swift: A Manifesto](https://gist.github.com/rxwei/30ba75ce092ab3b0dce4bde1fc2c9f1d)
* [jalgebra](https://github.com/mdgeorge4153/jalgebra): An abstract algebra library for Java
* [The Simple Essence of Automatic Differentiation](http://conal.net/papers/essence-of-ad/essence-of-ad-icfp.pdf)
* [Reverse-Mode AD in a Functional Framework: Lambda the Ultimate Backpropagator](http://www-bcl.cs.may.ie/~barak/papers/toplas-reverse.pdf)
* [A Size-Aware Type System with Algebraic Data Types](https://pdfs.semanticscholar.org/3a13/cf1599e212c089ccd6a2e05d944ec57c2f87.pdf)
