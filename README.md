# Kotlin∇: A type-safe AD implementation for Kotlin

Inspired by [Stalin∇](https://github.com/Functional-AutoDiff/STALINGRAD), [Autograd](https://github.com/hips/autograd), [DiffSharp](https://github.com/DiffSharp/DiffSharp), [Tangent](https://github.com/google/tangent), et al.

AD is useful for [gradient descent](https://en.wikipedia.org/wiki/Gradient_descent) and has a variety of applications in numerical optimization and machine learning.

We aim to provide an algebraically sound implementation of AD for type safe tensor manipulations.

# Usage

```kotlin
val rft = RealFunctor(DoublePrototype)
val x = rft.variable("x", Double(0))
val y = rft.variable("y", Double(1))

val z = 2 * x * (-sin(x * y) + y) // Operator overloading
val `∂z_∂x` = d(z) / d(x)         // Leibniz notation
val `∂z_∂y` = d(z) / d(y)         // Multiple variables
val `∂²z_∂x²` = d(`∂z_∂x`) / d(x) // Higher order and
val `∂²_∂x∂y` = d(`∂z_∂x`) / d(y) // partial derivatives
```

## References

* [A Design Proposal for an Object Oriented Algebraic Library](https://pdfs.semanticscholar.org/6fd2/88960ef83469c898a3d8ed8f0950e7839625.pdf)
* [Efficient Differentiable Programming in a Functional Array-Processing Language](https://arxiv.org/pdf/1806.02136.pdf)
* [First-Class Automatic Differentiation in Swift: A Manifesto](https://gist.github.com/rxwei/30ba75ce092ab3b0dce4bde1fc2c9f1d)
* [jalgebra](https://github.com/mdgeorge4153/jalgebra): An abstract algebra library for Java
* [The Simple Essence of Automatic Differentiation](http://conal.net/papers/essence-of-ad/essence-of-ad-icfp.pdf)
* [Reverse-Mode AD in a Functional Framework: Lambda the Ultimate Backpropagator](http://www-bcl.cs.may.ie/~barak/papers/toplas-reverse.pdf)
* [A Size-Aware Type System with Algebraic Data Types](https://pdfs.semanticscholar.org/3a13/cf1599e212c089ccd6a2e05d944ec57c2f87.pdf)
