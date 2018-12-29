# Kotlin𝛁: A type-safe AD implementation for Kotlin

Inspired by [Stalin∇](https://github.com/Functional-AutoDiff/STALINGRAD), [Autograd](https://github.com/hips/autograd), [DiffSharp](https://github.com/DiffSharp/DiffSharp), [Tangent](https://github.com/google/tangent), et al.

AD is useful for [gradient descent](https://en.wikipedia.org/wiki/Gradient_descent) and has a variety of applications in numerical optimization and machine learning.

We aim to provide an algebraically sound implementation of AD for type safe tensor manipulations.

# Usage

```kotlin
import co.ndan.kotlingrad.math.calculus.Differential.Companion.d
import co.ndan.kotlingrad.math.calculus.DoubleFunctor

with(DoubleFunctor) {
    val x = variable("x", 0)
    val y = variable("y", 1)

    val z = x * (-sin(x * y) + y)      // Operator overloads
    val `∂z_∂x` = d(z) / d(x)          // Leibniz notation
    val `∂z_∂y` = d(z) / d(y)          // Multiple variables
    val `∂²z_∂x²` = d(`∂z_∂x`) / d(x)  // Higher order and
    val `∂²z_∂x∂y` = d(`∂z_∂x`) / d(y) // partial derivatives

    val p = "${x.value}, ${y.value}"
    print("z(x, y) \t\t\t= $z\n" +
      "∂z($p)/∂x \t= $`∂z_∂x` \n\t\t\t\t\t= " + `∂z_∂x`.value + "\n" +
      "∂z($p)/∂y \t= $`∂z_∂y` \n\t\t\t\t\t= " + `∂z_∂y`.value + "\n" +
      "∂²z($p)/∂x² \t= $`∂z_∂y` \n\t\t\t\t\t= " + `∂²z_∂x²`.value + "\n" +
      "∂²z($p)/∂x∂y \t= $`∂²z_∂x∂y` \n\t\t\t\t\t= " + `∂²z_∂x∂y`.value)
}
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

```kotlin
val x = Variable(1.0)              // x: Variable<Double> inferred type
val y = Variable(1.0)              // x: Variable<Double> "
val f = x * y + sin(2 * x + 3 * y) // f: BinaryFunction<Double> "
val g = f(x to -1.0)               // g: UnaryFunction<Double> == -y + sin(-2 + 3 * y)
val h = f(x to 0.0, y to 0.0)      // h: Const<Double> == 0 + sin(0 + 0) == 0
```

### Vector functions

```kotlin
val x = VVariable(0.0, 0.0, 0.0)   // x: VVariable<Double, `3`>
val y = VVariable(0.0, `3`)        // x: VVariable<Double, `3`>
val f = 2 * x + x / 2              // f: UnaryVFunction<Double>
val g = f(-2.0, 0.0, 2.0)          // g: ConstVector<`3`> == [-3. 0. 5.]
```

### Matrix functions

```kotlin
val x = MVariable(0.0, 0.0, 0.0)   // x: MVariable<Double, `1`, `3`>
val y = MVariable(0.0, `3`, `3`)   // y: MVariable<Double, `3`, `3`>
val f = sin(2 * x) + log(x / 2)    // f: UnaryMFunction<Double>
val g = f(x) / d(x)                // g: UnaryMFunction<Double>
```

## References

### Computer Algebra

* [A Design Proposal for an Object Oriented Algebraic Library](https://pdfs.semanticscholar.org/6fd2/88960ef83469c898a3d8ed8f0950e7839625.pdf)
* [On Using Generics for Implementing Algebraic Structures](http://www.cs.ubbcluj.ro/~studia-i/contents/2011-4/02-Niculescu.pdf)
* [How to turn a scripting language into a domain specific language for computer algebra](https://arxiv.org/pdf/0811.1061.pdf)
* [Evaluation of a Java Computer Algebra System](https://pdfs.semanticscholar.org/ce81/39a9008bdc7d23be0ff05ef5a16d512b352c.pdf)
* [jalgebra](https://github.com/mdgeorge4153/jalgebra): An abstract algebra library for Java

### Automatic Differentiation

* [Efficient Differentiable Programming in a Functional Array-Processing Language](https://arxiv.org/pdf/1806.02136.pdf)
* [First-Class Automatic Differentiation in Swift: A Manifesto](https://gist.github.com/rxwei/30ba75ce092ab3b0dce4bde1fc2c9f1d)
* [The Simple Essence of Automatic Differentiation](http://conal.net/papers/essence-of-ad/essence-of-ad-icfp.pdf)
* [Reverse-Mode AD in a Functional Framework: Lambda the Ultimate Backpropagator](http://www-bcl.cs.may.ie/~barak/papers/toplas-reverse.pdf)
* [Backpropagation with Continuation Callbacks: Foundations for Efficient and Expressive Differentiable Programming](http://papers.nips.cc/paper/8221-backpropagation-with-callbacks-foundations-for-efficient-and-expressive-differentiable-programming.pdf)
* [Demystifying Differentiable Programming: Shift/Reset the Penultimate Backpropagator](https://www.cs.purdue.edu/homes/rompf/papers/wang-preprint201811.pdf)