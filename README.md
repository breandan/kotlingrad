# Kotlin∇: A type-safe AD implementation for Kotlin

Inspired by [Stalin∇](https://github.com/Functional-AutoDiff/STALINGRAD), [Autograd](https://github.com/hips/autograd), [DiffSharp](https://github.com/DiffSharp/DiffSharp), [Tangent](https://github.com/google/tangent).

AD is particularly useful for [gradient descent](https://en.wikipedia.org/wiki/Gradient_descent) and has a variety of applications in numerical optimization and machine learning.

We aim to provide an algebraically sound implementation of AD for type safe tensor manipulations.

# Usage

```kotlin
val rft = RealFunctor(DoublePrototype)
val x = rft.variable("x", Double(cx))
val y = rft.variable("y", Double(cy))

val h = 2 * x * (sin(x * y) + y)
val `∂z_∂x` = d(h) / d(x)
val `∂z_∂y` = d(h) / d(y)
val `∂²z_∂x²` = d(`∂z_∂x`) / d(x)
val `∂²_∂x∂y` = d(`∂z_∂x`) / d(y)
```

## References

* [A Design Proposal for an Object Oriented Algebraic Library](https://pdfs.semanticscholar.org/6fd2/88960ef83469c898a3d8ed8f0950e7839625.pdf)
* [Efficient Differentiable Programming in a Functional Array-Processing Language](https://arxiv.org/pdf/1806.02136.pdf)
* [jalgebra](https://github.com/mdgeorge4153/jalgebra)
* [The Simple Essence of Automatic Differentiation](http://conal.net/papers/essence-of-ad/essence-of-ad-icfp.pdf)
