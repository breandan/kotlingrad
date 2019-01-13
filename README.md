# Kotlin𝛁: A type-safe AD for Kotlin

Kotlin𝛁 is a framework for type-safe [automatic differentiation](https://en.wikipedia.org/wiki/Automatic_differentiation) in [Kotlin](https://kotl.in). It allows users to express differentiable programs on higher-dimensional data structures and operands. We attempt to restrict syntactically valid constructions to those which are algebraically valid and can be checked at compile-time. By enforcing these constraints in the type system, it eliminates certain classes of runtime errors that may occur during the execution of a correctly-typed program. Due to type-inference in the language, most types may be safely omitted by the end user. Kotlin𝛁 strives to be expressive, safe, and notationally similar to mathematics. It is currently pre-release and offers no stability guarantees at this time.

## Introduction

Inspired by [Stalin∇](https://github.com/Functional-AutoDiff/STALINGRAD), [Autograd](https://github.com/hips/autograd), [DiffSharp](https://github.com/DiffSharp/DiffSharp), [Tangent](https://github.com/google/tangent), [Myia](https://github.com/mila-udem/myia) et al.

AD is useful for [gradient descent](https://en.wikipedia.org/wiki/Gradient_descent) and has a variety of applications in numerical optimization and machine learning.

We aim to provide an algebraically sound implementation of AD for type safe tensor operations.

## How?

Kotlin𝛁 relies on a few language features, which together enable a concise, flexible and type-safe user interface:

* [Operator overloading](https://kotlinlang.org/docs/reference/operator-overloading.html) enables concise notation for arithmetic on abstract types, e.g. group, ring, and field.
* [Higher-order functions and lambdas](https://kotlinlang.org/docs/reference/lambdas.html) treats [functions as first-class citizens](https://en.wikipedia.org/wiki/First-class_function) for representing mathematical functions and programming functions with the same underlying abstractions (typed FP)
* [Coroutines](https://kotlinlang.org/docs/reference/coroutines/basics.html) and shift-reset continuations for implementing reverse mode AD with operator overloading alone, inspired by [Wang et al.](https://arxiv.org/pdf/1803.10228.pdf). Also enables independent branches of an expression to be evaluated asynchronously. (WIP)
* [Extension functions](https://kotlinlang.org/docs/reference/extensions.html) allows augmenting classes with new fields and methods. Via [context oriented programming](https://proandroiddev.com/an-introduction-context-oriented-programming-in-kotlin-2e79d316b0a2), Kotlin𝛁 can expose its custom extensions (e.g. in [DoubleFunctor](src/main/kotlin/edu/umontreal/kotlingrad/calculus/DoubleFunctor.kt)) to [consumers](src/main/kotlin/edu/umontreal/kotlingrad/samples/HelloKotlinGrad.kt) without requiring subclasses or inheritance.
* [Algebraic data types](https://en.wikipedia.org/wiki/Algebraic_data_type) in the form of [sealed classes](https://kotlinlang.org/docs/reference/sealed-classes.html) (a.k.a. sum types) allows creating a closed set of internal subclasses to guarantee an exhaustive control flow over the concrete types of an abstract class.

Kotlin𝛁 also uses [multiple dispatch](https://en.wikipedia.org/wiki/Multiple_dispatch) to instantiate the most specific result type of [applying an operator](https://github.com/breandan/kotlingrad/blob/09f4aaf789238820fb5285706e0f1e22ade59b7c/src/main/kotlin/edu/umontreal/kotlingrad/functions/Function.kt#L24:L38) to operand(s). While multiple dispatch is not an explicit language feature, it can be emulated using inheritance and [smart-casting](https://kotlinlang.org/docs/reference/typecasts.html#smart-casts).

## Features

Kotlin𝛁 currently supports the following features:

* Arithmetical operations on scalars, vectors and matrices
* Partial and higher order differentiation on scalars
* Property-based testing for numerical gradient checking
* Recovery of symbolic derivatives from AD 

Additionally, it aims to support:

* PyTorch-style define-by-run semantics
* N-dimensional tensors and tensor-algebraic operators
* Compiler plugin to instrument existing programs for AD
* Differentiation through general-purpose operators like loops, recursion, get- and set- assignment
(via [delgation](https://kotlinlang.org/docs/reference/delegated-properties.html)) and other common operators

# Usage

The following example shows how to derive higher-order partials of a function `z` with type ℝ²→ℝ:

```kotlin
import edu.umontreal.kotlingrad.calculus.DoubleFunctor

@Suppress("NonAsciiCharacters", "LocalVariableName")
fun main(args: Array<String>) {
  with(DoubleFunctor) {
    val x = variable("x")
    val y = variable("y")

    val z = x * (-sin(x * y) + y) * 4  // Infix notation
    val `∂z∕∂x` = d(z) / d(x)          // Leibniz notation
    val `∂z∕∂y` = d(z) / d(y)          // Partial derivatives
    val `∂²z∕∂x²` = d(`∂z∕∂x`) / d(x)  // Higher order derivatives
    val `∂²z∕∂x∂y` = d(`∂z∕∂x`) / d(y) // Higher order partials
    val `∇z` = z.grad()                // Gradient operator

    val values = mapOf(x to 0, y to 1)
    val indVar = z.variables.joinToString(", ")

    print("z($indVar) \t\t\t= $z\n" +
        "z($values) \t\t\t= ${z(values)}\n" +
        "∂z($values)/∂x \t\t= $`∂z∕∂x` \n\t\t\t\t= " + `∂z∕∂x`(values) + "\n" +
        "∂z($values)/∂y \t\t= $`∂z∕∂y` \n\t\t\t\t= " + `∂z∕∂y`(values) + "\n" +
        "∂²z($values)/∂x² \t\t= $`∂z∕∂y` \n\t\t\t\t= " + `∂²z∕∂x²`(values) + "\n" +
        "∂²z($values)/∂x∂y \t\t= $`∂²z∕∂x∂y` \n\t\t\t\t= " + `∂²z∕∂x∂y`(values) + "\n" +
        "∇z($values) \t\t\t= $`∇z` \n\t\t\t\t= [${`∇z`[x]!!(values)}, ${`∇z`[y]!!(values)}]ᵀ")
  }
}
```

Running [this program](src/main/kotlin/edu/umontreal/math/samples/HelloKotlinGrad.kt) via `./gradlew demo` should print:

```
z(x, y) 			= ((x * (-sin((x * y)) + y)) * 4)
z({x=0, y=1}) 			= 0.0
∂z({x=0, y=1})/∂x 		= (((-sin((x * y)) + y) + (x * -(cos((x * y)) * y))) * 4) 
				= 4.0
∂z({x=0, y=1})/∂y 		= ((x * (-(cos((x * y)) * x) + 1)) * 4) 
				= 0.0
∂²z({x=0, y=1})/∂x² 		= ((x * (-(cos((x * y)) * x) + 1)) * 4) 
				= -8.0
∂²z({x=0, y=1})/∂x∂y 		= (((-(cos((x * y)) * x) + 1) + (x * -((-(sin((x * y)) * x) * y) + cos((x * y))))) * 4) 
				= 4.0
∇z({x=0, y=1}) 			= {x=(((-sin((x * y)) + y) + (x * -(cos((x * y)) * y))) * 4), y=((x * (-(cos((x * y)) * x) + 1)) * 4)} 
				= [4.0, 0.0]ᵀ
```

## Testing

Kotlin𝛁 claims to eliminate certain runtime errors, but how can we be sure the proposed implementation is error-free? One way is to use a property-based testing methodology in the style of [QuickCheck](https://github.com/nick8325/quickcheck) and [Hypothesis](https://github.com/HypothesisWorks/hypothesis). It uses two primary mechanisms to check the functional correctness of automatic derivatives:

* Symbolic differentiation: manually find the derivative and compare the values returned on a subset of the domain with AD.
* [Finite difference approximation](https://en.wikipedia.org/wiki/Finite_difference_method): sample space of symbolic (differentiable) functions, and compare results of AD with FD.

However, there are many other ways to independently verify the numerical gradient. Another way is to compare the output with a well-known implementation, such as [TensorFlow](https://github.com/JetBrains/kotlin-native/tree/master/samples/tensorflow). We plan to implement this capability in a future release.

To run [the tests](src/test/kotlin/edu/umontreal/kotlingrad), execute: `./gradlew test`

### Plotting

![](src/main/resources/plot.png)

This plot was generated with the following code:

```kotlin
import edu.umontreal.kotlingrad.calculus.DoubleFunctor
import edu.umontreal.kotlingrad.utils.step
import krangl.dataFrameOf
import kravis.geomLine
import kravis.plot
import java.io.File

@Suppress("NonAsciiCharacters", "LocalVariableName", "RemoveRedundantBackticks")
fun main(args: Array<String>) {
  with(DoubleFunctor) {
    val x = variable("x")

    val y = sin(sin(sin(x))) / x + sin(x) * x + cos(x) + x
    val `dy∕dx` = d(y) / d(x)
    val `d²y∕dx²` = d(`dy∕dx`) / d(x)
    val `d³y∕dx³` = d(`d²y∕dx²`) / d(x)
    val `d⁴y∕dx⁴` = d(`d³y∕dx³`) / d(x)
    val `d⁵y∕dx⁵` = d(`d⁴y∕dx⁴`) / d(x)

    val xs = -10.0..10.0 step 0.09
    val ys = (xs.map { listOf(it, y(it), "y") }
            + xs.map { listOf(it, `dy∕dx`(it), "dy/dx") }
            + xs.map { listOf(it, `d²y∕dx²`(it), "d²y/x²") }
            + xs.map { listOf(it, `d³y∕dx³`(it), "d³y/dx³") }
            + xs.map { listOf(it, `d⁴y∕dx⁴`(it), "d⁴y/dx⁴") }
            + xs.map { listOf(it, `d⁵y∕dx⁵`(it), "d⁵y/dx⁵") }).flatten()

    dataFrameOf("x", "y", "Function")(ys)
      .plot(x = "x", y = "y", color = "Function")
      .geomLine(size = 1.0)
      .title("Derivatives of y=$y")
      .save(File("src/main/resources/plot.png"))
  }
}
```

To generate the above plot, you will need to install R and some packages. Ubuntu 18.04 instructions follow:

```
sudo apt-get install r-base && \
sudo ln -s /usr/bin/R /usr/local/bin/R && \
R -e "install.packages(c('ggplot2','dplyr','readr','forcats'))"
```

Then run `./gradlew plot`.

## Ideal API (WIP)

The current API is experimental, but can be improved in many ways. Currently it uses default values for variables, so when a function is invoked with missing variable(s) (i.e. `z = x * y; z(x to 1) // y = ?`) the default value will be applied. This is similar to [broadcasting in NumPy](https://docs.scipy.org/doc/numpy-1.15.0/user/basics.broadcasting.html), to ensure shape compatibility. However we could encode the dimensionality of the function into the type. Instead of allowing default values, this would enforce passing mandatory values when invoking a function (similar to the [builder pattern](https://gist.github.com/breandan/d0d7c21bb7f78ef54c21ce6a6ac49b68)). When the shape is known at compile-time, we can use a restricted form of [dependent types](src/main/kotlin/edu/umontreal/kotlingrad/functions/types/dependent) to ensure type-safe matrix- and tensor- operations (inspired by [Nexus](https://github.com/ctongfei/nexus)).

Another such optimization is to encode some useful properties of matrices into a variable's type, (e.g. `Symmetric`, `Orthogonal`, `Unitary`, `Hermitian`). Although it would be difficult to infer such properties using the JVM type system, if the user explicitly specified them explicitly, we could perform a number of optimizations on specialized matrices.

### Scalar functions

A function's type should encode arity, based on the number of variables:

```kotlin
val x = variable(1.0)              // x: Variable<Double> inferred type
val y = variable(1.0)              // x: Variable<Double> "
val f = x * y + sin(2 * x + 3 * y) // f: BinaryFunction<Double> "
val g = f(x to -1.0)               // g: UnaryFunction<Double> == -y + sin(-2 + 3 * y)
val h = f(x to 0.0, y to 0.0)      // h: Const<Double> == 0 + sin(0 + 0) == 0
```

### Vector functions

Vector functions should have a size type, to ensure all values are set:

```kotlin
val x = vvariable(0.0, 0.0, 0.0)   // x: VVariable<Double, `3`>
val y = vvariable(0.0, `3`)        // x: VVariable<Double, `3`>
val f = 2 * x + x / 2              // f: UnaryVFunction<Double>
val g = f(-2.0, 0.0, 2.0)          // g: ConstVector<`3`> == [-3. 0. 5.]
```

### Matrix functions

Multiplying matrices `x = N x M` and `y = M x P` should yield matrix `z` of type `N x P`:

```kotlin
val x = vvariable(0.0, `3`, `1`)   // y: MVariable<Double, `3`, `1`>
val y = vvariable(0.0, 0.0)        // x: MVariable<Double, `1`, `2`>
val z = x * y                      // z: MVariable<Double, `3`, `2`>
```

## References

The following are some excellent projects and publications that have inspired this work.

### Computer Algebra

* [A Design Proposal for an Object Oriented Algebraic Library](https://pdfs.semanticscholar.org/6fd2/88960ef83469c898a3d8ed8f0950e7839625.pdf)
* [On Using Generics for Implementing Algebraic Structures](http://www.cs.ubbcluj.ro/~studia-i/contents/2011-4/02-Niculescu.pdf)
* [How to turn a scripting language into a domain specific language for computer algebra](https://arxiv.org/pdf/0811.1061.pdf)
* [Evaluation of a Java Computer Algebra System](https://pdfs.semanticscholar.org/ce81/39a9008bdc7d23be0ff05ef5a16d512b352c.pdf)
* [jalgebra](https://github.com/mdgeorge4153/jalgebra): An abstract algebra library for Java
* [Typesafe Abstractions for Tensor Operations](https://arxiv.org/pdf/1710.06892.pdf)
* [Generalized Algebraic Data Types and Object-Oriented Programming](https://www.microsoft.com/en-us/research/wp-content/uploads/2016/02/gadtoop.pdf)

### Automatic Differentiation

* [The Simple Essence of Automatic Differentiation](http://conal.net/papers/essence-of-ad/essence-of-ad-icfp.pdf)
* [Reverse-Mode AD in a Functional Framework: Lambda the Ultimate Backpropagator](http://www-bcl.cs.may.ie/~barak/papers/toplas-reverse.pdf)
* [Stalin∇](https://github.com/Functional-AutoDiff/STALINGRAD), a brutally optimizing compiler for the VLAD language, a pure dialect of Scheme with first-class automatic differentiation operators
* [Autograd](https://github.com/hips/autograd) - Efficiently computes derivatives of NumPy code
* [DiffSharp](https://github.com/DiffSharp/DiffSharp), a functional AD library implemented in the F# language
* [Myia](https://github.com/mila-udem/myia) - SCT based AD, adapted from Pearlmutter & Siskind's "Reverse Mode AD in a functional framework"
* [Nexus](https://github.com/ctongfei/nexus) - Type-safe tensors, deep learning and probabilistic programming in Scala
* [Tangent](https://github.com/google/tangent) - "Source-to-Source Debuggable Derivatives in Pure Python"
* [First-Class Automatic Differentiation in Swift: A Manifesto](https://gist.github.com/rxwei/30ba75ce092ab3b0dce4bde1fc2c9f1d)
* [AD and the danger of confusing infinitesimals](http://conway.rutgers.edu/~ccshan/wiki/blog/posts/Differentiation/)
* [Automatic differentiation in PyTorch](https://openreview.net/pdf?id=BJJsrmfCZ)

### Differentiable Programming

* [Neural Networks, Types, and Functional Programming](http://colah.github.io/posts/2015-09-NN-Types-FP/)
* [Backpropagation with Continuation Callbacks: Foundations for Efficient and Expressive Differentiable Programming](http://papers.nips.cc/paper/8221-backpropagation-with-callbacks-foundations-for-efficient-and-expressive-differentiable-programming.pdf)
* [Demystifying Differentiable Programming: Shift/Reset the Penultimate Backpropagator](https://www.cs.purdue.edu/homes/rompf/papers/wang-preprint201811.pdf)
* [Operational Calculus for Differentiable Programming](https://arxiv.org/pdf/1610.07690.pdf)
* [Efficient Differentiable Programming in a Functional Array-Processing Language](https://arxiv.org/pdf/1806.02136.pdf)
* [Software 2.0](https://medium.com/@karpathy/software-2-0-a64152b37c35)

### Computational Mathematics

* [KMath](https://github.com/altavir/kmath) - Kotlin mathematics extensions library
* [An introduction to context-oriented programming in Kotlin](https://proandroiddev.com/an-introduction-context-oriented-programming-in-kotlin-2e79d316b0a2)
* [COJAC](https://github.com/Cojac/Cojac) - Numerical sniffing tool and Enriching number wrapper for Java
* [chebfun](http://www.chebfun.org/) - Allows representing functions as [Chebyshev polynomials](https://en.wikipedia.org/wiki/Chebyshev_polynomials), for easy symbolic differentiation (or integration)

### Calculus

* [The Matrix Calculus You Need For Deep Learning](https://explained.ai/matrix-calculus/index.html)

### Neural Networks

* [Hacker's Guide to Neural Networks](http://karpathy.github.io/neuralnets/)
* [Tricks from Deep Learning](https://arxiv.org/pdf/1611.03777.pdf)

### Automated Testing

* [DeepTest: Automated Testing of Deep-Neural-Network-driven Autonomous Cars](https://arxiv.org/pdf/1708.08559.pdf)
* [QuickCheck: A Lightweight Tool for Random Testing of Haskell Programs](https://www.eecs.northwestern.edu/~robby/courses/395-495-2009-fall/quick.pdf)