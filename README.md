<!--- @file:Suppress("ClassName") --->
<!--- @file:Suppress("PropertyName") --->

# Kotlin∇: Type-safe Symbolic Differentiation for the JVM

[![Kotlin 1.6.20](https://img.shields.io/badge/Kotlin-1.6.20-blue.svg?style=flat&logo=kotlin)](http://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/ai.hypergraph/kotlingrad.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22ai.hypergraph%22)
[![CI](https://github.com/breandan/kotlingrad/workflows/CI/badge.svg)](https://github.com/breandan/kotlingrad/actions)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.3549076.svg)](https://doi.org/10.5281/zenodo.3549076)

Kotlin∇ is a type-safe [automatic differentiation](http://breandan.net/public/masters_thesis.pdf#1a) framework written in [Kotlin](https://kotl.in). It allows users to express [differentiable programs](http://breandan.net/public/masters_thesis.pdf#1b) with higher-dimensional data structures and operators. We attempt to restrict syntactically valid constructions to those which are algebraically valid and can be checked at compile-time. By enforcing these constraints in the type system, it eliminates certain classes of runtime errors that may occur during the execution of a differentiable program. Due to type-inference, most type declarations may be safely omitted by the end-user. Kotlin∇ strives to be expressive, safe, and notationally similar to mathematics.

## Table of contents

* [Introduction](#introduction)
* [Supported features](#features)
* [Usage](#usage)
  * [Installation](#installation)
  * [Notation](#notation)
  * [Shape safety](#shape-safety)
  * [Higher-rank](#higher-rank-derivatives)
  * [Higher-order](#higher-order-derivatives)
  * [Example](#example)
  * [Variable capture](#variable-capture)
* [Visualization](#visualization-tools)
  * [Dataflow graphs](#dataflow-graphs)
  * [Plotting functions](#plotting)
  * [Loss curves](#loss-curves)
* [Testing and gradient checking](#testing)
* [How does it work?](#how)
  * [Operator overloading](#operator-overloading)
  * [First-class functions](#first-class-functions)
  * [Multi-stage programming](#multi-stage-programming)
  * [Extension functions](#extension-functions)
  * [Algebraic data types](#algebraic-data-types)
  * [Multiple dispatch](#multiple-dispatch)
  * [Shape-safe tensor operations](#shape-safe-tensor-operations)
  * [Intermediate representation](#intermediate-representation)
  * [Property delegation](#property-delegation)
* [Experimental ideas](#experimental-ideas)
  * [Church encoding](#church-encoding)
  * [Type classes](#type-classes)
  * [Type arithmetic](#type-arithmetic)
* [Formal grammar](#grammar)
* [UML diagram](#uml-diagram)
* [Comparison to other frameworks](#comparison)
* [References](#references)
* [Acknowledgements](#special-thanks)

## Introduction

Inspired by [Stalin∇](https://github.com/Functional-AutoDiff/STALINGRAD), [Autograd](https://github.com/hips/autograd), [DiffSharp](https://github.com/DiffSharp/DiffSharp), [Myia](https://github.com/mila-udem/myia), [Nexus](https://github.com/ctongfei/nexus), [Tangent](https://github.com/google/tangent), [Lantern](https://github.com/feiwang3311/Lantern) et al., Kotlin∇ attempts to port recent advancements in automatic differentiation (AD) to the Kotlin language. AD is useful for [gradient descent](https://en.wikipedia.org/wiki/Gradient_descent) and has a variety of applications in [numerical optimization](https://uhra.herts.ac.uk/bitstream/handle/2299/4342/903843.pdf) and [machine learning](http://www.jmlr.org/papers/volume18/17-468/17-468.pdf). Our implementation adds a number of experimental ideas, including compile-time [shape-safety](#shape-safety), [algebraic simplification](#multiple-dispatch) and numerical [stability checking](#testing) with property-based testing. We aim to provide an [algebraically-grounded](#operator-overloading) implementation of AD for shape-safe tensor operations. Tensors in Kotlin∇ are represented as [multidimensional arrays](https://en.wikipedia.org/wiki/Tensor#As_multidimensional_arrays).

## Features

Kotlin∇ currently supports the following features:

* Arithmetical operations on scalars, vectors and matrices
* Shape-safe vector and matrix algebra
* Partial and higher-order differentiation on scalars
* Property-based testing for numerical gradient checking
* Recovery of symbolic derivatives from AD

Additionally, it aims to support:

* PyTorch-style [define-by-run](https://openreview.net/pdf?id=BJJsrmfCZ#section.1) semantics
* N-dimensional tensors and [higher-order tensor operators](https://en.wikipedia.org/wiki/Tensor_contraction)
* Fully-general AD over control flow, variable reassignment
(via [delegation](https://kotlinlang.org/docs/reference/delegated-properties.html)), and array programming, possibly using a typed IR such as [Myia](https://github.com/mila-udem/myia)

All of these features are implemented without access to bytecode or special compiler tricks - just using [higher-order functions and lambdas](https://kotlinlang.org/docs/reference/lambdas.html) as shown in [Lambda the Ultimate Backpropogator](http://www-bcl.cs.may.ie/~barak/papers/toplas-reverse.pdf), embedded DSLs a la [Lightweight Modular Staging](https://infoscience.epfl.ch/record/150347/files/gpce63-rompf.pdf), and [ordinary generics](https://kotlinlang.org/docs/reference/generics.html). Please see below for a more detailed [feature comparison](#comparison).

## Usage

### Installation

Kotlin∇ is hosted on [Maven Central](https://s01.oss.sonatype.org/index.html#nexus-search;quick~kotlingrad). An example project is provided [here](https://github.com/breandan/kotlingrad-consumer).

#### Gradle

```kotlin
dependencies {
  implementation("ai.hypergraph:kotlingrad:0.4.7")
}
```

#### Maven

```xml
<dependency>
  <groupId>ai.hypergraph</groupId>
  <artifactId>kotlingrad</artifactId>
  <version>0.4.7</version>
</dependency>
```

#### Jupyter Notebook

To access Kotlin∇'s notebook support, use the following line magic:

```kotlin
@file:DependsOn("ai.hypergraph:kotlingrad:0.4.7")
```

For more information, explore the [tutorial](samples/notebooks/hello_kotlingrad.ipynb).

### Notation

Kotlin∇ operators are [higher-order functions](https://en.wikipedia.org/wiki/Higher-order_function), which take at most two inputs and return a single output, all of which are functions with the same numerical type, and whose shape is denoted using superscript in the rightmost column below. 

|                                Math                                |      Infix <sup>&dagger;</sup>  |              Prefix              |     Postfix<sup>&Dagger;</sup>      |                                            Operator Type Signature                                               |
|:------------------------------------------------------------------:|:-------------------------------:|:--------------------------------:|:-----------------------------------:|:----------------------------------------------------------------------------------------------------------------:|
|    $$\mathbf{A}(\mathbf{B})$$<br>$$\mathbf{A}\circ\mathbf{B}$$     |       `a(b)`<br>`a of b`        |                                  |                                     |       (`a`:  ℝ<sup>τ</sup>→ℝ<sup>π</sup>, `b`: ℝ<sup>λ</sup> → ℝ<sup>τ</sup>) → (ℝ<sup>λ</sup>→ℝ<sup>π</sup>)    |
|                    $$\mathbf{A}\pm\mathbf{B}$$                     |       `a + b`<br>`a - b`        | `plus(a, b)`<br>`minus(a, b)`    |                                     |       (`a`:  ℝ<sup>τ</sup>→ℝ<sup>π</sup>, `b`: ℝ<sup>λ</sup> → ℝ<sup>π</sup>) → (ℝ<sup>?</sup>→ℝ<sup>π</sup>)    |
|                      $$\mathbf{A}\mathbf{B}$$                      |     `a * b`<br>`a.times(b)`     |          `times(a, b)`           |                                     |      (`a`: ℝ<sup>τ</sup>→ℝ<sup>m×n</sup>, `b`: ℝ<sup>λ</sup>→ℝ<sup>n×p</sup>) → (ℝ<sup>?</sup>→ℝ<sup>m×p</sup>)  |
| $$\frac{\mathbf{A}}{\mathbf{B}}$$<br>$$\mathbf{A}\mathbf{B}^{-1}$$ |      `a / b`<br>`a.div(b)`      |           `div(a, b)`            |                                     |      (`a`: ℝ<sup>τ</sup>→ℝ<sup>m×n</sup>, `b`: ℝ<sup>λ</sup>→ℝ<sup>p×n</sup>) → (ℝ<sup>?</sup>→ℝ<sup>m×p</sup>)  |
|                         $$\pm\mathbf{A}$$                          |                                 |           `-a`<br>`+a`           |       `a.neg()`<br>`a.pos()`        |                           (`a`: ℝ<sup>τ</sup>→ℝ<sup>π</sup>) → (ℝ<sup>τ</sup>→ℝ<sup>π</sup>)                     |
|             $$\sin{a}$$<br>$$\cos{a}$$<br>$$\tan{a}$$              |                                 | `sin(a)`<br>`cos(a)`<br>`tan(a)` | `a.sin()`<br>`a.cos()`<br>`a.tan()` |                                               (`a`: ℝ→ℝ) → (ℝ→ℝ)                                                 |
|                             $$\ln{a}$$                             |                                 |       `ln(a)`<br>`log(a)`        |        `a.ln()`<br>`a.log()`        |                         (`a`: ℝ<sup>τ</sup>→ℝ<sup>m×m</sup>) → (ℝ<sup>τ</sup>→ℝ<sup>m×m</sup>)                   |
|                           $$\log_{b}a$$                            |           `a.log(b)`            |           `log(a, b)`            |                                     |              (`a`: ℝ<sup>τ</sup>→ℝ<sup>m×m</sup>, `b`: ℝ<sup>λ</sup>→ℝ<sup>m×m</sup>) → (ℝ<sup>?</sup>→ℝ)        |
|                          $$\mathbf{A}^b$$                          |           `a.pow(b)`            |           `pow(a, b)`            |                                     |              (`a`: ℝ<sup>τ</sup>→ℝ<sup>m×m</sup>, `b`: ℝ<sup>λ</sup>→ℝ) → (ℝ<sup>?</sup>→ℝ<sup>m×m</sup>)        |
|                  $$\sqrt{A}$$<br>$$\sqrt[3]{A}$$                   |  `a.pow(1.0/2)`<br>`a.root(3)`  |      `sqrt(a)`<br>`cbrt(a)`      |      `a.sqrt()`<br>`a.cbrt()`       |                               (`a`: ℝ<sup>τ</sup>→ℝ<sup>m×m</sup>) → (ℝ<sup>τ</sup>→ℝ<sup>m×m</sup>)             |
| $$\frac{da}{db},\frac{\partial{a}}{\partial{b}}$$ <br> $$D_b{a}$$  |  `a.d(b)`<br>`d(a) / d(b)`      |            `grad(a)[b]`          |                                     |                           (`a`: C(ℝ<sup>τ</sup>→ℝ)<sup>*</sup>, `b`: C(ℝ<sup>λ</sup>→ℝ)) → (ℝ<sup>?</sup>→ℝ)     |
|                           $$\nabla{a}$$                            |                                 |            `grad(a)`             |             `a.grad()`              |                           (`a`: C(ℝ<sup>τ</sup>→ℝ)) → (ℝ<sup>τ</sup>→ℝ<sup>τ</sup>)                              |
|                      $$\nabla_{\mathbf{B}}a$$                      |     `a.d(b)`<br>`a.grad(b)`     |   `grad(a, b)`<br>`grad(a)[b]`   |                                     |   (`a`: C(ℝ<sup>τ</sup>→ℝ<sup>π</sup>), `b`: C(ℝ<sup>λ</sup>→ℝ<sup>ω</sup>)) → (ℝ<sup>?</sup>→ℝ<sup>π×ω</sup>)   |
|                    $$\nabla\cdot{\mathbf{A}}$$                     |                                 |            `divg(a)`             |             `a.divg()`              |                     (`a`: C(ℝ<sup>τ</sup>→ℝ<sup>m</sup>)) → (ℝ<sup>τ</sup>→ℝ)                                    |
|                    $$\nabla\times{\mathbf{A}}$$                    |                                 |            `curl(a)`             |             `a.curl()`              |                     (`a`: C(ℝ<sup>3</sup>→ℝ<sup>3</sup>)) → (ℝ<sup>3</sup>→ℝ<sup>3</sup>)                        |
|                    $$\mathcal{J}(\mathbf{A})$$                     |                                 |            `grad(a)`             |             `a.grad()`              |               (`a`: C(ℝ<sup>τ</sup>→ℝ<sup>m</sup>)) → (ℝ<sup>τ</sup>→ℝ<sup>m×τ</sup>)                            |
|                         $$\mathbf{H}(a)$$                          |                                 |            `hess(a)`             |             `a.hess()`              |                           (`a`: C(ℝ<sup>τ</sup>→ℝ)) → (ℝ<sup>τ</sup>→ℝ<sup>τ×τ</sup>)                            |
|                     $$\Delta{a},\nabla^{2}a$$                      |                                 |            `lapl(a)`             |             `a.lapl()`              |                           (`a`: C(ℝ<sup>τ</sup>→ℝ)) → (ℝ<sup>τ</sup>→ℝ<sup>τ</sup>)                              |

ℝ can be a `Double`, `Float` or `BigDecimal`. Specialized operators are defined for subsets of ℝ, e.g., `Int`, `Short` or `BigInteger` for subsets of ℤ, however differentiation is [only defined](https://en.wikipedia.org/wiki/Differentiable_function) for continuously differentiable functions on ℝ.

<sup>&dagger;</sup> `a` and `b` are higher-order functions. These may be constants (e.g., `0`, `1.0`), variables (e.g., `Var()`) or expressions (e.g., `x + 1`, `2 * x + y`).

<sup>&Dagger;</sup> For infix notation, `.` is optional. Parentheses are also optional depending on [precedence](https://kotlinlang.org/docs/reference/functions.html#infix-notation).

<sup>&sect;</sup> Matrix division is defined iff **B** is invertible, although it could be possible to redefine this operator using the [Moore-Penrose inverse](https://en.wikipedia.org/wiki/Moore%E2%80%93Penrose_inverse).

<sup>&lowast;</sup> Where C(ℝ<sup>m</sup>) is the space of all continuous functions over ℝ. If the function is not over ℝ, it will fail at compile-time. If the function is over ℝ but not continuous differentiable at the point under consideration, it will fail at runtime.

<sup>?</sup> The input shape is tracked at runtime, but not at the type level. While it would be nice to infer a union type bound over the inputs of binary functions, it is likely impossible using the Kotlin type system [without great effort](core/src/commonMain/gen/ai/hypergraph/kotlingrad/typelevel/arity/Variables.kt). If the user desires type checking when invoking higher order functions with literal values, they will need to specify the combined input type explicitly or do so at runtime.

<sup>τ, λ, π, ω</sup> Arbitrary products.

### Higher-Rank Derivatives

Kotlin∇ supports derivatives between tensors of up to rank 2. The shape of a tensor derivative depends on (1) the shape of the function under differentiation and (2) the shape of the variable with respect to which we are differentiating.

|              I/O Shape               |        ℝ<sup>?</sup>→ℝ        |  ℝ<sup>?</sup>→ℝ<sup>m</sup>  | ℝ<sup>?</sup>→ℝ<sup>j×k</sup> |
|:------------------------------------:|:-----------------------------:|:-----------------------------:|:-----------------------------:|
|        <b>ℝ<sup>?</sup>→ℝ</b>        |        ℝ<sup>?</sup>→ℝ        |  ℝ<sup>?</sup>→ℝ<sup>m</sup>  | ℝ<sup>?</sup>→ℝ<sup>j×k</sup> |
|  <b>ℝ<sup>?</sup>→ℝ<sup>n</sup></b>  |  ℝ<sup>?</sup>→ℝ<sup>n</sup>  | ℝ<sup>?</sup>→ℝ<sup>m×n</sup> |              :x:              |
| <b>ℝ<sup>?</sup>→ℝ<sup>h×i</sup></b> | ℝ<sup>?</sup>→ℝ<sup>h×i</sup> |              :x:              |              :x:              |

Matrix-by-vector, vector-by-matrix, and matrix-by-matrix derivatives require rank 3+ tensors and are currently unsupported.

### Higher-order derivatives

Kotlin∇ supports arbitrary order derivatives on scalar functions, and up to 2nd order derivatives on vector functions. Higher-order derivatives on matrix functions are unsupported.

### Shape safety

Shape safety is an important concept in Kotlin∇. There are three broad strategies for handling shape errors:

* Hide the error somehow by implicitly reshaping or [broadcasting](https://docs.scipy.org/doc/numpy-1.10.4/user/basics.broadcasting.html) arrays
* Announce the error at runtime, with a relevant message, e.g., [`InvalidArgumentError`](https://www.tensorflow.org/api_docs/python/tf/errors/InvalidArgumentError)
* Do not allow programs which can result in a shape error to compile

In Kotlin∇, we use the last strategy to check the shape of tensor operations. Consider the following program:

```kotlin
// Inferred type: Vec<Double, D2>
val a = Vec(1.0, 2.0)
// Inferred type: Vec<Double, D3>
val b = Vec(1.0, 2.0, 3.0)

val c = b + b

// Does not compile, shape mismatch
// a + b
```

Attempting to sum two vectors whose shapes do not match will fail to compile, and they must be explicitly resized.

```kotlin
// Inferred type: Mat<Double, D1, D4>
val a = Mat1x4(1.0, 2.0, 3.0, 4.0)
// Inferred type: Mat<Double, D4, D1>
val b = Mat4x1(1.0, 2.0, 3.0, 4.0)

val c = a * b

// Does not compile, inner dimension mismatch
// a * a
// b * b
```

Similarly, attempting to multiply two matrices whose inner dimensions do not match will fail to compile.

```kotlin
val a = Mat2x4( 
  1.0, 2.0, 3.0, 4.0,
  5.0, 6.0, 7.0, 8.0
)

val b = Mat4x2( 
  1.0, 2.0,
  3.0, 4.0,
  5.0, 6.0,
  7.0, 8.0
)

// Types are optional, but encouraged
val c: Mat<Double, D2, D2> = a * b 

val d = Mat2x1(1.0, 2.0)

val e = c * d

val f = Mat3x1(1.0, 2.0, 3.0)

// Does not compile, inner dimension mismatch
// e * f
```

Explicit types are optional but encouraged. [Type inference](https://www.youtube.com/watch?v=MyljSWm0Y_k) helps preserve shape information over long programs.

```kotlin
fun someMatFun(m: Mat<Double, D3, D1>): Mat<Double, D3, D3> = ...
fun someMatFun(m: Mat<Double, D2, D2>) = ...
```

When writing a function, it is mandatory to declare the input type(s), but the return type [may be omitted](https://kotlinlang.org/docs/reference/functions.html#explicit-return-types). Shape-safety is currently supported up to rank-2 tensors, i.e. matrices.

### Example

The following example shows how to derive higher-order partials of a function `z` of type ℝ²→ℝ:

```kotlin
val z = x * (-sin(x * y) + y) * 4  // Infix notation
val `∂z∕∂x` = d(z) / d(x)          // Leibniz notation [Christianson, 2012]
val `∂z∕∂y` = d(z) / d(y)          // Partial derivatives
val `∂²z∕∂x²` = d(`∂z∕∂x`) / d(x)  // Higher-order derivatives
val `∂²z∕∂x∂y` = d(`∂z∕∂x`) / d(y) // Higher-order partials
val `∇z` = z.grad()                // Gradient operator

val values = arrayOf(x to 0, y to 1)

println("z(x, y) \t= $z\n" +
  "z(${values.map { it.second }.joinToString()}) \t\t= ${z(*values)}\n" +
  "∂z/∂x \t\t= $`∂z∕∂x` \n\t\t= " + `∂z∕∂x`(*values) + "\n" +
  "∂z/∂y \t\t= $`∂z∕∂y` \n\t\t= " + `∂z∕∂y`(*values) + "\n" +
  "∂²z/∂x² \t= $`∂z∕∂y` \n\t\t= " + `∂²z∕∂x²`(*values) + "\n" +
  "∂²z/∂x∂y \t= $`∂²z∕∂x∂y` \n\t\t= " + `∂²z∕∂x∂y`(*values) + "\n" +
  "∇z \t\t= $`∇z` \n\t\t= [${`∇z`[x]!!(*values)}, ${`∇z`[y]!!(*values)}]ᵀ")
```

Any backticks and unicode characters above are simply for readability and have no effect on the behavior. Running [this program](samples/src/main/kotlin/ai/hypergraph/kotlingrad/samples/HelloKotlingrad.kt) via `./gradlew HelloKotlingrad` should produce the following output:

```
z(x, y)         = ((x) * ((- (sin((x) * (y)))) + (y))) * (4.0)
z(0, 1)         = 0.0
∂z/∂x           = d(((x) * ((- (sin((x) * (y)))) + (y))) * (4.0)) / d(x) 
                = 4.0
∂z/∂y           = d(((x) * ((- (sin((x) * (y)))) + (y))) * (4.0)) / d(y) 
                = 0.0
∂²z/∂x²         = d(((x) * ((- (sin((x) * (y)))) + (y))) * (4.0)) / d(y) 
                = 4.0
∂²z/∂x∂y        = d(d(((x) * ((- (sin((x) * (y)))) + (y))) * (4.0)) / d(x)) / d(y) 
                = 4.0
∇z              = {y=d(((x) * ((- (sin((x) * (y)))) + (y))) * (4.0)) / d(y), x=d(((x) * ((- (sin((x) * (y)))) + (y))) * (4.0)) / d(x)} 
                = [4.0, 0.0]ᵀ
```

### Variable capture

Not only does Kotlin∇'s type system encode [output shape](#shape-safety), it is also capable of tracking free and bound variables, for order-independent name binding and partial application. Expressions inhabited by free variables are typed as functions until fully bound, at which time they return a concrete value. Consider the following example:

```kotlin
val q = X + Y * Z + Y + 0.0
val p0 = q(X to 1.0, Y to 2.0, Z to 3.0) // Name binding
val p1 = q(X to 1.0, Y to 1.0)(Z to 1.0) // Variadic currying
val p3 = q(Z to 1.0)(X to 1.0, Y to 1.0) // Any order is possible
val p4 = q(Z to 1.0)(X to 1.0)(Y to 1.0) // Proper currying
val p5 = q(Z to 1.0)(X to 1.0) // Returns a partially applied function
val p6 = (X + Z + 0)(Y to 1.0) // Does not compile
```

This feature is made possible by encoding a type-level [Hasse diagram](https://en.wikipedia.org/wiki/Hasse_diagram) over a small set of predefined variable names, with skip-connections for variadic combination and partial application. Curious readers may glean further details by referring to [the implementation](core/src/commonMain/gen/ai/hypergraph/kotlingrad/typelevel/arity/Variables.kt) and [usage example](samples/src/main/kotlin/ai/hypergraph/kotlingrad/samples/VariableCapture.kt).

## Visualization tools

Kotlin∇ provides various graphical tools that can be used for visual debugging.

### Dataflow graphs

Kotlin∇ functions are a type of [directed acyclic graph](https://en.wikipedia.org/wiki/Directed_acyclic_graph), called dataflow graphs (DFGs). For example, running the expression `((1 + x * 2 - 3 + y + z / y).d(y).d(x) + z / y * 3 - 2).render()` will display the following DFG:

![](samples/src/main/resources/dataflow.svg)

Red and blue edges indicate the right and left inputs to a binary operator, respectively. Consider the DFG for a batch of stochastic gradients on [linear regression](samples/src/main/kotlin/ai/hypergraph/kotlingrad/samples/LinearRegression.kt), which can be written in matrix form as <img src="https://render.githubusercontent.com/render/math?math=\nabla_{\Theta}||\mathbf{Y} - \mathbf{X}\Theta||^2">:

![](samples/src/main/resources/lr_batch_loss_graph.svg)

Thetas represent the hidden parameters under differentiation and the constants are the batch inputs (**X**) and targets (**Y**). When all the free variables are bound to numerical values, the graph collapses into a single node, which can be unwrapped into a Kotlin `Number`.

### Plotting

To generate the [sample 2D plots](samples/src/main/kotlin/ai/hypergraph/kotlingrad/samples/Plot2D.kt) below, run `./gradlew Plot2D`.

<p align="center"><img src="samples/src/main/resources/plot.svg"></p>
<p align="center"><img src="samples/src/main/resources/hermite.svg"></p>

Plotting is also possible in higher dimensions, [for example](samples/src/main/kotlin/ai/hypergraph/kotlingrad/samples/Plot3D.kt) in 3D via `./gradlew Plot3D`:

![](samples/src/main/resources/ripple.png)
![](samples/src/main/resources/pulsar.png)
![](samples/src/main/resources/starquake.png)
![](samples/src/main/resources/novaflux.png)

### Loss curves

Gradient descent is one application for Kotlin∇. Below, is a typical loss curve of SGD on [a multilayer perceptron](samples/src/main/kotlin/ai/hypergraph/kotlingrad/samples/MLP.kt):

![](samples/src/main/resources/mlp_loss.svg)

To train the model, execute `./gradlew MLP` from within the parent directory.

## Testing

To run [the tests](core/src/jvmTest/kotlin/ai/hypergraph/kotlingrad), execute `../gradlew allTests` from the `core` directory.

Kotlin∇ claims to eliminate certain runtime errors, but how do we know the proposed implementation is not incorrect? One method, borrowed from the Haskell community, is called [property-based testing](http://breandan.net/public/masters_thesis.pdf#33) (PBT), closely related to [metamorphic testing](http://breandan.net/public/masters_thesis.pdf#34). Notable implementations include [QuickCheck](https://github.com/nick8325/quickcheck), [Hypothesis](https://github.com/HypothesisWorks/hypothesis) and [ScalaTest](http://www.scalatest.org/user_guide/property_based_testing) (ported to Kotlin in [Kotest](https://github.com/kotest/kotest)). PBT uses algebraic properties to verify the result of an operation by constructing semantically equivalent but syntactically distinct expressions, which should produce the same answer. Kotlin∇ uses two such equivalences to validate its AD implementation:

* [Analytic differentiation](https://en.wikipedia.org/wiki/Differentiation_rules): manually differentiate and compare the values returned on a subset of the domain with AD.
* [Finite difference approximation](http://breandan.net/public/masters_thesis.pdf#5a): sample space of symbolic (differentiable) functions, comparing results of AD to FD.

For example, consider the following test, which checks whether the analytical derivative and the automatic derivative, when evaluated at a given point, are equal to each other within the limits of numerical precision:

```kotlin
val x by Var()
val y by Var()

val z = y * (sin(x * y) - x)            // Function under test
val `∂z∕∂x` = d(z) / d(x)               // Automatic derivative
val manualDx = y * (cos(x * y) * y - 1) // Analytical derivative 

"∂z/∂x should be y * (cos(x * y) * y - 1)" {
  NumericalGenerator.assertAll { ẋ, ẏ ->
    // Evaluate the results at a given seed
    val autoEval = `∂z∕∂x`(x to ẋ, y to ẏ) 
    val manualEval = manualDx(x to ẋ, y to ẏ)
    // Should pass iff Δ(adEval, manualEval) < Ɛ
    autoEval shouldBeApproximately manualEval
  }
}
```

PBT will search the input space for two numerical values `ẋ` and `ẏ`, which violate the specification, then ["shrink"](https://hackage.haskell.org/package/QuickCheck-2.12.6.1/docs/Test-QuickCheck-Arbitrary.html#v:shrink) them to discover pass-fail boundary values. We can construct a similar test using finite differences:

```kotlin
"d(sin x)/dx should be equal to (sin(x + dx) - sin(x)) / dx" {
  NumericalGenerator.assertAll { ẋ ->
    val f = sin(x)
    
    val `df∕dx` = d(f) / d(x)
    val adEval = `df∕dx`(ẋ) 
    
    val dx = 1E-8
    // Since ẋ is a raw numeric type, sin => kotlin.math.sin
    val fdEval = (sin(ẋ + dx) - sin(ẋ)) / dx
    adEval shouldBeApproximately fdEval
  }
}
```

![](samples/src/main/resources/comparison.svg)

Above, we [compare numerical errors](samples/src/main/kotlin/ai/hypergraph/kotlingrad/samples/ADSDComparison.kt) for three types of computational differentiation against infinite precision symbolic differentiation (IP):

1. Finite precision automatic differentiation (AD)
2. Finite precision symbolic differentiation (SD)
3. Finite precision finite differences (FD)

AD and SD both exhibit relative errors (i.e. with respect to each other) several orders of magnitude lower than their absolute errors (i.e. with respect to IP), which roughly agree to within numerical precision. As expected, FD exhibits numerical error significantly higher than AD and SD due to the inaccuracy of floating-point division.

There are many other ways to independently verify the numerical gradient, such as [dual numbers](https://en.wikipedia.org/wiki/Dual_number#Differentiation) or the [complex step derivative](https://timvieira.github.io/blog/post/2014/08/07/complex-step-derivative/). Another method is to compare the numerical output against a well-known implementation, such as [TensorFlow](https://github.com/JetBrains/kotlin-native/tree/master/samples/tensorflow). We plan to conduct a more thorough comparison of numerical accuracy and performance.

## How?

To understand the core of Kotlin∇'s AD implementation, please refer to the [scalar example](core/src/commonMain/kotlin/ai/hypergraph/kotlingrad/api/Scalar.kt).

This project relies on a few Kotlin-specific language features, which together enable a concise, flexible and type-safe user interface. The following features have proven beneficial to the development of Kotlin∇:

#### Operator overloading
 
[Operator overloading](https://kotlinlang.org/docs/reference/operator-overloading.html) enables concise notation for arithmetic on abstract types, where the types encode [algebraic structures](http://breandan.net/public/masters_thesis.pdf#page=58), e.g., `Group`, `Ring`, and `Field`. These abstractions are extensible to other kinds of mathematical structures, such as complex numbers and quaternions.

For example, suppose we have an interface `Group`, which overloads the operators `+` and `*`, and is defined like so:

```kotlin
interface Group<T: Group<T>> {
  operator fun plus(addend: T): T

  operator fun times(multiplicand: T): T
}
```

Here, we specify a recursive type bound using a method known as [F-bounded quantification](http://staff.ustc.edu.cn/~xyfeng/teaching/FOPL/lectureNotes/CookFBound89.pdf) to ensure that operations return the concrete type variable `T`, rather than something more abstract like `Group`. Imagine a class `Fun` that has implemented `Group`. It can be used as follows:

```kotlin
fun <T: Group<T>> cubed(t: T): T = t * t * t

fun <T: Group<T>> twiceCubed(t: T): T = cubed(t) + cubed(t)
```

Like [Python](https://docs.python.org/3.4/library/operator.html), Kotlin supports overloading a [limited set of operators](https://kotlinlang.org/docs/reference/operator-overloading.html), which are evaluated using a [fixed precedence](https://kotlinlang.org/docs/reference/grammar.html#precedence). In the current version of Kotlin∇, operators do not perform any computation, they simply construct a directed acyclic graph representing the symbolic expression. Expressions are only evaluated when invoked as a function.

#### First-class functions

With [higher-order functions and lambdas](https://kotlinlang.org/docs/reference/lambdas.html), Kotlin treats [functions as first-class citizens](https://en.wikipedia.org/wiki/First-class_function). This allows us to represent mathematical functions and programming functions with the same underlying abstractions (typed FP). Several [recent](http://www-bcl.cs.may.ie/~barak/papers/toplas-reverse.pdf) [papers](https://papers.nips.cc/paper/8221-backpropagation-with-callbacks-foundations-for-efficient-and-expressive-differentiable-programming.pdf) have demonstrated the expressiveness of this paradigm for automatic differentiation.

In Kotlin∇, all expressions can be treated as functions. For example:

```kotlin
fun <T: Group<T>> makePoly(x: Var<T>, y: Var<T>) = x * y + y * y + x * x
val x by Var()
val y by Var()
val f = makePoly(x, y)
val z = f(1.0, 2.0) // Returns a value
println(z) // Prints: 7
```

Additionally, it is possible to build functions consisting of varying dimensional inputs:

```kotlin
fun <T: Fun<T>> mlp(p1: VFun<T, D3>, p2: MFun<T, D3, D3>, p3: T) =
  ((p1 * p2 + p1 * p2 * p2 dot p1 + p1) - p3) pow p3
```

#### Multi-stage programming

Kotlin∇ uses [operator overloading](#operator-overloading) in the host language to first construct a [dataflow graph](#dataflow-graphs), but evaluates the graph lazily. Called "multi-stage programming", or *staging*, this is a metaprogramming technique from the [ML community](http://ocamllabs.io/iocamljs/staging.html) which enables type-safe runtime code translation and compilation. More recently, staging has been put to effective use for [compiling embedded DSLs](https://static.csg.ci.i.u-tokyo.ac.jp/papers/14/scherr-ecoop2014.pdf) similar to Kotlin∇.

In its current form, Kotlin∇ takes a "shallow embedding" approach. Similar to an [interpreter](https://en.wikipedia.org/wiki/Interpreter_pattern), it adheres closely to the user-defined program and does not perform much code specialization or rewriting for optimization purposes. Unlike an interpreter, it postpones evaluation until all free variables in an expression have been bound. Consider the following snippet, which decides when to evaluate an expression:

```kotlin
var EAGER = false
operator fun invoke(newBindings: Bindings<X>): Fun<X> =
    Composition(this, newBindings).run { if (bindings.complete || EAGER) evaluate() else this }
```

If `bindings` are `complete`, this means there are no unbound variables remaining (implementation omitted for brevity), and we can evaluate the expression to obtain a numerical result. Suppose we have the following user code:

```kotlin
val x = Var()
val y = Var()
val z = Var()
val f0 = x + y * z
var f1 = f0(x to 1).also { println(it) } // Prints: (x + y * z)(x=1)
var f2 = f1(y to 2).also { println(it) } // Prints: (x + y * z)(x=1)(y=2)
var f3 = f2(z to 3).also { println(it) } // Prints: 7
```

Once the last line is reached, all variables are bound, and instead of returning a `Composition`, Kotlin∇ evaluates the function, returning a constant. Alternatively, if `EAGER` mode is enabled, each invocation is applied as early as possible:

```kotlin
EAGER = true
f1 = f0(x to 1).also { println(it) } // Prints: 1 + y * z
f2 = f1(y to 2).also { println(it) } // Prints: 1 + 2 * z
f3 = f2(z to 3).also { println(it) } // Prints: 7
```

In the following section, we describe how evaluation works.

#### Algebraic data types

[Algebraic data types](https://en.wikipedia.org/wiki/Algebraic_data_type) (ADTs) in the form of [sealed classes](https://kotlinlang.org/docs/reference/sealed-classes.html) (a.k.a. sum types) facilitate a limited form of pattern matching over a closed set of subclasses. By using these, the compiler forces us to provide an exhaustive control flow when type checking a sealed class. Consider the following classes:

```kotlin
class Const<T: Fun<T>>(val number: Number) : Fun<T>()
class Sum<T: Fun<T>>(val left: Fun<T>, val right: Fun<T>) : Fun<T>()
class Prod<T: Fun<T>>(val left: Fun<T>, val right: Fun<T>) : Fun<T>()
class Var<T: Fun<T>>: Fun<T>() { override val variables: Set<Var<X>> = setOf(this) }
class Zero<T: Fun<T>>: Const<T>(0.0)
class One<T: Fun<T>>: Const<T>(1.0)
```

When checking the type of a sealed class, consumers must explicitly handle every case, as incomplete control flow will produce a compiler error rather than fail at runtime. Consider a simplified definition of the superclass `Fun`, which defines invocation and differentiation using a restricted form of pattern matching:

```kotlin
sealed class Fun<X: Fun<X>>(open val variables: Set<Var<X>> = emptySet()): Group<Fun<X>> {
    constructor(vararg fns: Fun<X>): this(fns.flatMap { it.variables }.toSet())

    // Since the subclasses of Fun are a closed set, no `else  ...` is required.
    operator fun invoke(map: Bindings<X>): Fun<X> = when (this) {
        is Const -> this
        is Var -> map.getOrElse(this) { this } // Partial application is permitted
        is Prod -> left(map) * right(map) // Smart casting implicitly casts after checking
        is Sum -> left(map) + right(map)
    }

    fun d(variable: Var<X>): Fun<X> = when(this) {
       is Const -> Zero
       is Var -> if (variable == this) One else Zero
       // Product rule: d(u*v)/dx = du/dx * v + u * dv/dx
       is Prod -> left.d(variable) * right + left * right.d(variable)
       is Sum -> left.d(variable) + right.d(variable)
    }

    operator fun plus(addend: Fun<T>) = Sum(this, addend)

    operator fun times(multiplicand: Fun<T>) = Prod(this, multiplicand)
}
```

Symbolic differentiation as implemented by Kotlin∇ has two distinct passes, one for differentiation and one for evaluation. Differentiation constitutes a top-down substitution process on the computation graph and evaluation propagates the values from the bottom, up. This reduction semantics for this procedure are described more precisely in [the specification](https://github.com/breandan/kotlingrad/blob/master/specification.md#reduction-semantics).

[![](latex/figures/kotlingrad_diagram.png)](http://breandan.net/public/masters_thesis.pdf#page=58)

Kotlin∇ functions are not only data structures, but Kotlin functions which can be invoked by passing a [`Bindings`](/core/src/commonMain/kotlin/ai/hypergraph/kotlingrad/api/Bindings.kt) instance (effectively, a `Map<Fun<X>, Fun<X>>`). To enable this functionality, we overload the [`invoke` operator](https://kotlinlang.org/docs/reference/operator-overloading.html#invoke), then recurse over the graph, using `Bindings` as a lookup table. If a matching subexpression is found, we propagate the bound value instead of the matching function. This is known as the [interpreter pattern](https://en.wikipedia.org/wiki/Interpreter_pattern).

Kotlin's [smart casting](https://kotlinlang.org/docs/reference/typecasts.html#smart-casts) is an example of [flow-sensitive type analysis](https://en.wikipedia.org/wiki/Flow-sensitive_typing) where the abstract type `Fun` can be treated as `Sum` after performing an `is Sum` check. Without smart casting, we would need to write `(this as Sum).left` to access the member, `left`, causing a potential `ClassCastException` if the cast were mistaken.

#### Extension functions

By using [extension functions](https://kotlinlang.org/docs/reference/extensions.html), users can convert between numerical types in the host language and our eDSL, by augmenting classes with additional operators. [Context-oriented programming](https://proandroiddev.com/an-introduction-context-oriented-programming-in-kotlin-2e79d316b0a2), allows users to define custom extensions without requiring subclasses or inheritance.

```kotlin
data class Const<T: Group<T>>(val number: Double) : Fun()
data class Sum<T: Group<T>>(val e1: Fun, val e2: Fun) : Fun()
data class Prod<T: Group<T>>(val e1: Fun, val e2: Fun) : Fun()

class Fun<T: Group<T>>: Group<Fun<T>> {
  operator fun plus(addend: Fun<T>) = Sum(this, addend)
  
  operator fun times(multiplicand: Fun<T>) = Prod(this, multiplicand)
}

object DoubleContext {
  operator fun Number.times(expr: Fun<Double>) = Const(toDouble()) * expr
}
```

Now, we can use the context to define another extension, `Fun.multiplyByTwo`, which computes the product inside a `DoubleContext`, using the operator overload we defined above:

```kotlin
fun Fun<Double>.multiplyByTwo() = with(DoubleContext) { 2 * this } // Uses `*` operator in DoubleContext
```

Extensions can also be defined in another file or context and imported on demand. For example, Kotlin∇ also uses extensions to define [shape-safe](#shape-safe-tensor-operations) constructors and operators for vector and matrix arithmetic.

#### Multiple dispatch

In conjunction with ADTs, Kotlin∇ also uses [multiple dispatch](https://en.wikipedia.org/wiki/Multiple_dispatch) to instantiate the most specific result type of [applying an operator](https://github.com/breandan/kotlingrad/blob/09f4aaf789238820fb5285706e0f1e22ade59b7c/src/main/kotlin/ai/hypergraph/kotlingrad/functions/Function.kt#L24-L38) based on the type of its operands. While multiple dispatch is not an explicit language feature, it can be emulated using inheritance.

Building on the previous example, a common task in AD is to [simplify a graph](http://deeplearning.net/software/theano/extending/optimization.html). This is useful in order to minimize the total number of calculations required, improving numerical stability. We can eagerly simplify expressions based on algebraic [rules of replacement](https://en.wikipedia.org/wiki/Rule_of_replacement). Smart casting allows us to access members of a class after checking its type, without explicitly casting it:

[//]: # (Note: numerical stability is sensitive to the order of rewriting, cf. https://en.wikipedia.org/wiki/Kahan_summation_algorithm)

```kotlin
override fun times(multiplicand: Function<X>): Function<X> = when {
  this == zero -> this
  this == one -> multiplicand
  multiplicand == one -> this
  multiplicand == zero -> multiplicand
  this == multiplicand -> pow(two)
  this is Const && multiplicand is Const -> const(value * multiplicand.value)
  // Further simplification is possible using rules of replacement
  else -> Prod(this, multiplicand)
}

val result = Const(2.0) * Sum(Var(2.0), Const(3.0)) // Sum(Prod(Const(2.0), Var(2.0)), Const(6.0))
```

This allows us to put all related control flow on a single abstract class which is inherited by subclasses, simplifying readability, debugging and refactoring.


#### Shape-safe tensor operations

While first-class [dependent types](https://wiki.haskell.org/Dependent_type) are useful for ensuring arbitrary shape safety (e.g., when concatenating and reshaping matrices), they are unnecessary for simple equality checking (such as when multiplying two matrices). When the shape of a tensor is known at compile-time, it is possible to encode this information using a less powerful type system*, as long as it supports subtyping and parametric polymorphism (a.k.a. generics). In practice, we can implement a shape-checked tensor arithmetic in languages like Java, Kotlin, C++, C# or Typescript, which accept generic type parameters. In Kotlin, whose type system is [less expressive](https://kotlinlang.org/docs/reference/generics.html#variance) than Java, we use the following strategy.

Shape safety is currently supported up to rank-2 tensors, i.e. matrices. To perform dimension checking in our type system, we first enumerate a list of integer type literals as a chain of subtypes, `C <: C - 1 <: C - 2 <: ... <: 1 <: 0`, where `C` is the largest fixed-length dimension we wish to represent, which can be specified by the user prior to compilation. This guarantees linear space and time complexity for subtype checking, with a constant upper bound.

```kotlin
@file:Suppress("ClassName")
interface Nat<T: D0> { val i: Int } // Used for certain type bounds
sealed class D0(open val i: Int = 0) { companion object: D0(), Nat<D0> }
sealed class D1(override val i: Int = 1): D0(i) { companion object: D1(), Nat<D1> }
sealed class D2(override val i: Int = 2): D1(i) { companion object: D2(), Nat<D2> }
sealed class D3(override val i: Int = 3): D2(i) { companion object: D3(), Nat<D3> }
//... † Automatically generated
```

Next, we overload the call operator to emulate instantiating a collection literal, using arity to infer its dimensionality. Consider the rank-1 case for length inference on vector literals:

```kotlin
open class Vec<E, Len: D1>(val contents: List<E>)
fun <T> Vec(t1: T): Vec<T, D1> = Vec(listOf(t1))
fun <T> Vec(t1: T, t2: T): Vec<T, D2> = Vec(listOf(t1, t2))
fun <T> Vec(t1: T, t2: T, t3: T): Vec<T, D3> = Vec(listOf(t1, t2, t3))
//... † Automatically generated
```

Finally, we encode length as a parameter of the operand type. Since integer literals are a chain of subtypes, we need only define one operator using the highest literal, and can rely on [Liskov substitution](https://en.wikipedia.org/wiki/Liskov_substitution_principle) to preserve shape safety for all subtypes.

```kotlin
infix operator fun <C: D1, V: Vec<Int, C>> V.plus(v: V): Vec<Int, C> =
  Vec(contents.zip(v.contents).map { it.first + it.second })
```

The operator `+` can now be used like so. Incompatible operands will cause a type error:

```kotlin
val one = Vec(1, 2, 3) + Vec(1, 2, 3)          // Always runs safely
val add = Vec(1, 2, 3) + Vec(listOf(/*...*/))  // May fail at runtime
val sum = Vec(1, 2) + add                      // Does not compile
```

A similar syntax is available for [matrices](core/src/commonMain/kotlin/ai/hypergraph/kotlingrad/api/Matrix.kt) and higher-rank [tensors](core/src/commonMain/kotlin/ai/hypergraph/kotlingrad/api/Tensor.kt). For example, Kotlin∇ can infer the shape of multiplying two matrices, and will not compile if their inner dimensions do not match:

```kotlin
open class Mat<X, R: D1, C: D1>(vararg val rows: Vec<X, C>)
fun <X> Mat1x2(d0: X, d1: X): Mat<X, D1, D2> = Mat(Vec(d0, d1))
fun <X> Mat2x1(d0: X, d1: X): Mat<X, D2, D1> = Mat(Vec(d0), Vec(d1))
//... † Automatically generated
operator fun <Q: D1, R: D1, S: D1> Mat<Int, Q, R>.times(m: Mat<Int, R, S>): Mat<Int, Q, S> = TODO()

// Inferred type: Mat<Int, D4, D4>
val l = Mat4x4(
  1, 2, 3, 4,
  5, 6, 7, 8,
  9, 0, 0, 0,
  9, 0, 0, 0
)

// Inferred type: Mat<Int, D4, D3>
val m = Mat4x3(
  1, 1, 1,
  2, 2, 2,
  3, 3, 3,
  4, 4, 4
)

// Inferred type: Mat<Int, D4, D3>
val lm = l * m
// m * m // Compile error: Expected Mat<3, *>, found Mat<4, 3>
```

[Further examples](samples/src/main/kotlin/ai/hypergraph/kotlingrad/samples/MatrixDemo.kt) are provided for shape-safe matrix operations such as addition, subtraction and transposition.

A similar technique is possible in Haskell, which is capable of a more powerful form of type-level computation, [type arithmetic](https://wiki.haskell.org/Type_arithmetic). Type arithmetic makes it easy to express [convolutional arithmetic](https://arxiv.org/pdf/1603.07285.pdf) and other arithmetic operations on shape variables (say, splitting a vector in half), which is currently not possible, or would require enumerating every possible combination of type literals.

<sup>&lowast;</sup> Many type systems are still capable of performing arbitrary computation in the type checker. As specified, Java's type system is [known to be Turing Complete](https://arxiv.org/pdf/1605.05274.pdf). It may be possible to emulate a limited form of dependent types in Java by exploiting this property, although this may not be computationally tractable due to the practical limitations noted by Grigore.

<sup>&dagger;</sup> Statically generated code, shipped within the library. To regenerate these methods (e.g., using larger dimensions), a code generator is [provided](shipshape/src/main/kotlin/ai/hypergraph/shipshape).

#### Intermediate representation

Kotlin∇ programs are [staged](#multi-stage-programming) into [Kaliningraph](https://github.com/breandan/kaliningraph), an experimental IR for graph computation. As written by the user, many graphs are computationally suboptimal due to expression swell and parameter sharing. To accelerate forward- and backpropagation, it is often advantageous to simplify the graph by applying the [reduction semantics](https://github.com/breandan/kotlingrad/blob/master/specification.md#operational-semantics) in a process known as [graph canonicalization](https://en.wikipedia.org/wiki/Graph_canonization). Kaliningraph enables compiler-like optimizations over the graph such as expression simplification and analytic root-finding, and supports features for visualization and debugging, e.g., in [computational notebooks](https://github.com/breandan/kotlingrad/blob/master/samples/notebooks/hello_kotlingrad.ipynb).

#### Property delegation

[Property delegation](https://kotlinlang.org/docs/reference/delegated-properties.html) is a reflection feature in the Kotlin language which lets us access properties to which an instance is bound. For example, we can read the property name like so:

```kotlin
class Var(val name: String?) {
  operator fun getValue(thisRef: Any?, property: KProperty<*>) = Var(name ?: property.name)
}
```

This feature allows consumers to instantiate variables e.g., in an embedded DSL without redeclaring their names:

```kotlin
val x by Var()   // With property delegation
val x = Var("x") // Without property delegation
```

Without property delegation, users would need to repeat the property name in the constructor.

## Experimental ideas

The current API is stable but can be [improved](https://github.com/breandan/kotlingrad/issues) in many ways. Currently, Kotlin∇ does not infer a function's input dimensionality (i.e. free variables and their corresponding shape). While it is possible to perform [variable capture](#variable-capture) over a small alphabet using [type safe currying](samples/src/main/kotlin/ai/hypergraph/kotlingrad/samples/VariableCapture.kt), this technique incurs a large source code [overhead](core/src/commonMain/kotlin/ai/hypergraph/kotlingrad/typelevel/VariableCapture.kt). It may be possible to reduce the footprint using [phantom types](https://gist.github.com/breandan/d0d7c21bb7f78ef54c21ce6a6ac49b68) or some form of union type bound (cf. [Kotlin](https://kotlinlang.org/docs/reference/generics.html#upper-bounds), [Java](https://docs.oracle.com/javase/tutorial/java/generics/bounded.html)).

When the shape of an N-dimensional array is known at compile-time, we can use [type-level integers](shipshape/src/main/kotlin/ai/hypergraph/shipshape/DimGen.kt) to ensure shape conforming tensor operations (inspired by [Nexus](https://github.com/ctongfei/nexus) and others).

Allowing users to specify a matrix's structure in its type signature, (e.g., `Singular`, `Symmetric`, `Orthogonal`, `Unitary`, `Hermitian`, `Toeplitz`) would allow us to specialize derivation over such matrices (cf. [section 2.8](https://www.math.uwaterloo.ca/~hwolkowi/matrixcookbook.pdf#page=14) of The Matrix Cookbook).

### Church encoding

Computers appear to be very complicated machines. Beneath this complexity lies a remarkably simple idea: many apparently complex routines can be rewritten in terms of function composition. Consider the binary operator `^`, which can be lowered as follows:

```
a ^ b :=  a * ... * a 
          \_________/
            b times
a * b :=  a + ... + a 
          \_________/
            b times
a + b :=  a + 1 + ... + 1
              \_________/
                b times
a := next*(next(...next(1)...))
     \________________/
          a times
```
&lowast; `next` is also called `S` in [Peano arithmetic](https://en.wikipedia.org/wiki/Successor_function).

By using the λ-calculus, Church [tells us](https://compcalc.github.io/public/church/church_calculi_1941.pdf#page=9), we can lower a large portion of mathematics onto a single operator: function application. Curry, by way of [Schönfinkel](https://writings.stephenwolfram.com/data/uploads/2020/12/Schonfinkel-OnTheBuildingBlocksOfMathematicalLogic.pdf), gives us combinatory logic, a kind of Rosetta stone for deciphering and translating between a host of cryptic languages. These two ideas, λ-calculus and combinators, are keys to unlocking many puzzles in computer science and mathematics.

Though mathematically elegant, Church numerals are not particularly efficient or pleasant to read. One discovers that trying to encode Church arithmetic in a language without dependent types grows quickly impractical. By selecting a higher radix, however, it is possible to reduce spatial complexity and improve readability, albeit at the cost of increased temporal complexity on certain operations (e.g., `+` and `-`). Kotlin∇ uses a [binary encoding](#type-arithmetic) by default, however generators for other bases are also provided for convenience.

### Type classes

The trouble with numerical towers is that they assume all inheritors are aware of the tower. In practice, many types we would like to reuse are entirely oblivious to our DSL. How do we allow users to bring in existing types without needing to modify their source code? This kind of [ad hoc polymorphism](https://en.wikipedia.org/wiki/Ad_hoc_polymorphism) can be achieved using a pattern called the [type class](https://en.wikipedia.org/wiki/Type_class). While the JVM does not allow multiple inheritance on classes, it does support multiple inheritance and [default methods](https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html) on interfaces, allowing users to implement an interface via delegation rather than inheritance.

Suppose we have a base type, `Nat` defined as an interface with a unitary member, `nil`, and its successor function, `next`, representing the [Church encoding](https://en.wikipedia.org/wiki/Church_axioms) for natural numbers. To emulate instantiation, we can provide a [nested class](https://kotlinlang.org/docs/nested-classes.html) equipped with a constructor overriding `nil` and `next` as follows:

```kotlin
interface Nat<T> {
  val nil: T
  val one: T get() = nil.next()
  fun T.next(): T

  class of<T>(
    override val nil: T,
    val vnext: T.() -> T
  ): Nat<T> {
    override fun T.next(): T = vnext()
  }
}
```

Now, if we wanted to wrap an external type, such as `Double`, inside our tower, we could do so as follows:

```kotlin
val doubleNat = Nat.of(nil = 0.0) { this + 1.0 }
```

Although the `Nat` interface is very expressive, evaluating arithmetic expressions on `Nat`s can be computationally expensive. For instance, we could define the first three [hyperoperations](https://en.wikipedia.org/wiki/Hyperoperation) naïvely as follows:

```kotlin
tailrec fun <T> Nat<T>.plus(l: T, r: T, acc: T = l, i: T = nil): T =
  if (i == r) acc else plus(l, r, acc.next(), i.next())

tailrec fun <T> Nat<T>.times(l: T, r: T, acc: T = nil, i: T = nil): T =
  if (i == r) acc else times(l, r, acc + l, i.next())

tailrec fun <T> Nat<T>.pow(base: T, exp: T, acc: T = one, i: T = one): T =
  if (i == exp) acc else pow(base, exp, acc * base, i.next())
```

However, we note that computing `pow(a, b)` using this representation requires 𝓞(a↑b) operations using [Knuth notation](https://en.wikipedia.org/wiki/Knuth%27s_up-arrow_notation). Clearly, we must do better if this encoding is to be usable. We can make `Nat` more efficient by introducing a subtype, `Group`, which forces implementors to define a native addition operator:

```kotlin
interface Group<T>: Nat<T> {
  override fun T.next(): T = this + one
  override fun T.plus(t: T): T

  class of<T>(
    override val nil: T, override val one: T,
    val plus: (T, T) -> T
  ): Group<T> {
    override fun T.plus(t: T) = plus(this, t)
  }
}
```

Given a `Group`, we can now define a more efficient implementation of Fibonacci. This will use the group-specific addition operator:

```kotlin
tailrec fun <T> Nat<T>.fibonacci(
  n: T,
  seed: Pair<T, T> = nil to one,
  fib: (Pair<T, T>) -> Pair<T, T> = { (a, b) -> b to a + b },
  i: T = nil,
): T =
  if (i == n) fib(seed).first
  else fibonacci(n = n, seed = fib(seed), i = i.next())

val doubleGroup = Group.of(one = 1.0, plus = { a, b -> a + b })
println(doubleGroup.fibonacci(10.0)) // Prints: 233.0
```

We could further extend this chain by introducing a subtype called `Ring`, which overrides `+` and requires implementors to define a native `*` operator. `Ring`s and their relatives are known to have many useful applications in [graph theory](https://github.com/breandan/kaliningraph#algebra) and [statistics](https://github.com/breandan/markovian#algebraic-methods):

```kotlin
interface Ring<T>: Group<T> {
  override fun T.plus(t: T): T
  override fun T.times(t: T): T

  class of<T>(
    override val nil: T, override val one: T,
    val plus: (T, T) -> T,
    val times: (T, T) -> T
  ): Ring<T> {
    override fun T.plus(t: T) = plus(this, t)
    override fun T.times(t: T) = times(this, t)
  }
}

val doubleRing = Ring.of(one = 1.0, plus = { a, b -> a + b }, times = { a, b -> a * b })
```

Since differentiation is a [linear map](https://en.wikipedia.org/wiki/Linear_map) between function spaces, we now have the primitives necessary to build a fully-generic AD system, and could easily implement the [sum and product rules](https://compcalc.github.io/public/pytorch/ad_pytorch.pdf#page=6). To view the above example in full, see [`Types.kt`](https://github.com/breandan/kaliningraph/blob/master/src/commonMain/kotlin/ai/hypergraph/kaliningraph/types/Types.kt).

What benefit does this abstraction provide to the end user? By parameterizing over primitive operators, Kotlin∇ consumers can easily swap out a tensor backend without needing to alter or recompile any upstream dependencies. This feature makes multiplatform development a breeze: wherever a type class operator (e.g., `+` or `*`) with matching signature is encountered across a project, it will be dispatched to the user-supplied lambda delegate for specialized execution on custom hardware. Runtime indirection can be elided with proper compiler inlining for zero-cost abstraction.

### Type arithmetic

By default, Kotlin∇ supports compile time type arithmetic in the following domain:

* Fully symmetric arithmetic: `{ a ⍟ b ϵ [0..16){+,-,*}[0..16) | 0 ≤ a ⍟ b }`
* Asymmetric arithmetic: `{ a ⍟ b ϵ [0..512){+,-}[0..16) | 0 ≤ a ⍟ b < 512 }`
* Semi-symmetric arithmetic: `{ a / b = c, a = b * c | a, b, c ϵ [0..128) & a % b = 0 }`

Arithmetic outside this domain is checked at runtime, prior to evaluation.

Compile time type arithmetic is achieved by generating a type-level representation of the [Church encoding](#church-encoding). A usage example is shown in [`ChurchArithmeticTest.kt`](/core/src/commonTest/kotlin/ai/hypergraph/kotlingrad/typelevel/church/ChurchArithmeticTest.kt), which may be run with the following command:

```sh
./gradlew :kotlingrad:cleanJvmTest :kotlingrad:jvmTest --tests "ai.hypergraph.kotlingrad.typelevel.church.ChurchArithmeticTest"
```

Extensions to other bases, including [binary](/core/src/commonTest/kotlin/ai/hypergraph/kotlingrad/typelevel/binary/BinaryArithmeticTest.kt) and [decimal](/core/src/commonTest/kotlin/ai/hypergraph/kotlingrad/typelevel/chinese/AbacusTest.kt) are also provided, which may be used as follows:

```kotlin
// Boolean arithmetic
val b32 = T.F
  .let { it + T.F }   // B_4<Ø>
  .let { it + T.F.F } // B_8<Ø>
  .let { it + T.T }   // T<T<F<T<Ø>>>>
  .let { it + T.F }   // T<F<T<T<Ø>>>>
  .let { it - T.F }   // T<T<F<T<Ø>>>>
  .let { it + T.F }   // T<F<T<T<Ø>>>>
  .let { it + T.F }   // T<T<T<T<Ø>>>>
  .let { it + T }     // T<F<F<F<Ø>>>>

assertEquals(T.F.F.F.F, b32)

// Chinese arithmetic
val 四十二 = (十七 减 九)
  .let { it 加 it }        // 六<一<无>>
  .let { (it 加 八) 加 六 } // 零<三<无>>
  .let { (it 减 三) 加 九 } // 六<三<无>>
  .let { (it 加 六) 除 六 } // 七<无>
  .let { (it 乘 六) 加 五 } // 七<四<无>>
  .let { (it 减 三) 减 九 } // 五<三<无>>
  .let { (it 加 五) 加 二 } // 二<四<无>>
  .also { assertEquals(六 乘 七, it) }

assertEquals(42, 四十二.toInt())
```

To alter the arithmetic domain, edit the file [`BinGen.kt`](/shipshape/src/main/kotlin/ai/hypergraph/shipshape/BinGen.kt)/[`算盘厂.kt`](/shipshape/src/main/kotlin/ai/hypergraph/shipshape/算盘厂.kt), then use the following command to regenerate [`Arithmetic.kt`](/core/src/commonMain/gen/ai/hypergraph/kotlingrad/typelevel/binary/Arithmetic.kt)/[`算盘.kt`](/core/src/commonMain/gen/ai/hypergraph/kotlingrad/typelevel/chinese/算盘.kt):

```sh
./gradlew genShapes
```

In practice, compile time type arithmetic may struggle to compute numbers in excess of `4095`. The Kotlin team has been informed of these issues:

* [KT-30040](https://youtrack.jetbrains.com/issue/KT-30040)
* [~~KT-50466~~](https://youtrack.jetbrains.com/issue/KT-50466)
* [KT-50533](https://youtrack.jetbrains.com/issue/KT-50533)
* [KT-50553](https://youtrack.jetbrains.com/issue/KT-50553)
* [~~KT-50617~~](https://youtrack.jetbrains.com/issue/KT-50617)

This API is experimental and subject to change without notice. In the future, it will be used to statically type check tensor functions whose output shape is an arithmetic function of the input shapes, e.g., concatenation, splitting and [convolution](https://arxiv.org/pdf/1603.07285.pdf).

## Grammar

For a detailed grammar and semantics, please refer to [the Kotlin∇ specification](specification.md).

## UML Diagram

The following graph depicts the subtyping relation between classes and interfaces in Kotlin∇.

[![](samples/src/main/resources/uml_diagram.svg)](https://raw.githubusercontent.com/breandan/kotlingrad/master/samples/src/main/resources/uml_diagram.svg)

## Comparison

Unlike certain frameworks which simply wrap an existing AD library in a type-safe DSL, Kotlin∇ contains a fully shape-safe implementation of algorithmic differentiation, written in pure Kotlin. By doing so, it can leverage Kotlin language features such as typed functional programming, as well as interoperability with other languages on the JVM platform. Furthermore, it implements [symbolic differentiation](http://breandan.net/public/masters_thesis.pdf#2a), which unlike Wengert tape or dual-number based ADs, allows it to calculate derivatives of arbitrarily high order with zero extra engineering required. Further details can be found below.

|                                    Framework                                     | Language |        SD¹         |        AD²         |        HD³         |        DP⁴         |        FP⁵         |        TS⁶         |        SS⁷         |        DT⁸         |        MP⁹         |
|:--------------------------------------------------------------------------------:|:--------:|:------------------:|:------------------:|:------------------:|:------------------:|:------------------:|:------------------:|:------------------:|:------------------:|:------------------:|
|                                     Kotlin∇                                      |  Kotlin  | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |   :construction:   | :heavy_check_mark: |
|               [DiffSharp](https://diffsharp.github.io/DiffSharp/)                |    F#    |        :x:         | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |        :x:         |        :x:         |        :x:         |
|       [TensorFlow.FSharp](https://github.com/fsprojects/TensorFlow.FSharp)       |    F#    |        :x:         |        :x:         |        :x:         | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |        :x:         |        :x:         |
|               [shapesafe](https://github.com/tribbloid/shapesafe)                |  Scala   |   :construction:   |   :construction:   |   :construction:   |   :construction:   | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |   :construction:   |        :x:         |
|                        [Nexus](https://tongfei.me/nexus/)                        |  Scala   |        :x:         | :heavy_check_mark: |        :x:         | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |        :x:         |        :x:         |
|                [Lantern](https://feiwang3311.github.io/Lantern/)                 |  Scala   |        :x:         | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |        :x:         |        :x:         |        :x:         |
|           [Hipparchus](https://github.com/Hipparchus-Math/hipparchus)            |   Java   |        :x:         | :heavy_check_mark: |        :x:         |        :x:         |        :x:         | :heavy_check_mark: |        :x:         |        :x:         |        :x:         |
|                [JAutoDiff](https://github.com/uniker9/JAutoDiff/)                |   Java   | :heavy_check_mark: | :heavy_check_mark: |        :x:         |        :x:         |        :x:         | :heavy_check_mark: |        :x:         |        :x:         |        :x:         |
|                   [Eclipse DL4J](https://deeplearning4j.org/)                    |   Java   |        :x:         |   :construction:   |        :x:         |        :x:         |        :x:         | :heavy_check_mark: |        :x:         |        :x:         |        :x:         |
|               [SICMUtils](https://github.com/sicmutils/sicmutils)                | Clojure  | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |        :x:         |        :x:         |        :x:         |        :x:         |
|                        [Halide](https://halide-lang.org/)                        |   C++    |        :x:         | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |        :x:         | :heavy_check_mark: |        :x:         |        :x:         |        :x:         |
|              [Tensor Safe](https://github.com/leopiney/tensor-safe)              | Haskell  |        :x:         |        :x:         |        :x:         |        :x:         | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |        :x:         |
|               [HaskTorch](https://github.com/hasktorch/hasktorch)                | Haskell  |        :x:         |        :x:         |        :x:         |        :x:         | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |        :x:         |        :x:         |
|                [Dex](https://github.com/google-research/dex-lang)                | Haskell  |        :x:         | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |   :construction:   |        :x:         |
|                [Grenade](https://github.com/HuwCampbell/grenade)                 | Haskell  |        :x:         |        :x:         |        :x:         |        :x:         | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |        :x:         |        :x:         |
|           [Stalin∇](https://github.com/Functional-AutoDiff/STALINGRAD)           |  Scheme  |        :x:         | :heavy_check_mark: |        :x:         |        :x:         | :heavy_check_mark: |        :x:         |        :x:         |        :x:         |        :x:         |
|                    [Myia](https://github.com/mila-udem/myia)                     |  Python  | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |        :x:         |        :x:         |        :x:         |   :construction:   |
|                  [Autograd](https://github.com/HIPS/autograd/)                   |  Python  |        :x:         | :heavy_check_mark: |        :x:         |        :x:         |        :x:         |        :x:         |        :x:         |        :x:         |        :x:         |
|                       [JAX](https://github.com/google/jax)                       |  Python  |        :x:         | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: |        :x:         |        :x:         |        :x:         |   :construction:   |
|                   [Tangent](https://github.com/google/tangent)                   |  Python  |        :x:         | :heavy_check_mark: |        :x:         |        :x:         |        :x:         |        :x:         |        :x:         |        :x:         |        :x:         |
| [Analitik](https://link.springer.com/content/pdf/10.1007/BF01070461.pdf#page=39) | Analitik | :heavy_check_mark: |        :x:         |        :x:         |        :x:         | :heavy_check_mark: |        :x:         |        :x:         |        :x:         |        :x:         |

¹ Symbolic differentiation*, ² Automatic differentiation*, ³ Higher-order/rank differentiation, ⁴ Differentiable programming*, ⁵ Functional programming, ⁶ Compile-time type safety, ⁷ Compile-time shape safety, ⁸ Dependently Typed, ⁹ Multiplatform

<sup>&lowast;</sup> Although we do not distinguish between AD and SD, here we adopt the authors' preferred nomenclature. We do make a distinction between differentiable programming libraries and those which simply construct neural networks. The :construction: symbol indicates work in progress.

## References

To the author's knowledge, Kotlin∇ is the first AD implementation in native Kotlin. While the particular synthesis of these ideas (i.e. shape-safe, functional AD, using generic types) is unique, it has been influenced by a long list of prior work in AD. Below is a list of projects and publications that helped inspire this work.

### Automatic differentiation

* [The Simple Essence of Automatic Differentiation](http://conal.net/papers/essence-of-ad/essence-of-ad-icfp.pdf)
* [Reverse-Mode AD in a Functional Framework: Lambda the Ultimate Backpropagator](http://www-bcl.cs.may.ie/~barak/papers/toplas-reverse.pdf)
* [Automatic differentiation in ML: Where we are and where we should be going](https://papers.nips.cc/paper/8092-automatic-differentiation-in-ml-where-we-are-and-where-we-should-be-going.pdf)
* [A Leibniz Notation for Automatic Differentiation](https://uhra.herts.ac.uk/bitstream/handle/2299/8933/904722.pdf)
* [First-Class Automatic Differentiation in Swift: A Manifesto](https://gist.github.com/rxwei/30ba75ce092ab3b0dce4bde1fc2c9f1d)
* [The (JAX) Autodiff Cookbook](https://colab.research.google.com/github/google/jax/blob/master/notebooks/autodiff_cookbook.ipynb)
* [Automatic Differentiation in PyTorch](https://openreview.net/pdf?id=BJJsrmfCZ)
* [Automatic Differentiation in Machine Learning: a Survey](http://jmlr.org/papers/volume18/17-468/17-468.pdf)
* [Complexity of Derivatives Generated by Symbolic Differentiation](https://doi.org/10.1007/978-3-642-57201-2_12)
* [Eigen-AD: Algorithmic Differentiation of the Eigen Library](https://arxiv.org/pdf/1911.12604.pdf)

### Complexity

* [Fast parallel computation of polynomials using few processors](http://www.cs.tau.ac.il/~amnon/Classes/2015-PRG/Papers/VSBR83.pdf), Valiant and Skyum (1983)
* [The complexity of partial derivatives](https://core.ac.uk/download/pdf/82480031.pdf), Baur and Strassen (1983)
* [Lower Bounds on Arithmetic Circuits via Partial Derivatives](https://www.math.ias.edu/~avi/PUBLICATIONS/MYPAPERS/NW96/final.pdf)
* [Learning Restricted Models of Arithmetic Circuits](https://www.cs.tau.ac.il/~shpilka/publications/KlivansShpilka_Learning_via_partial_derivatives.pdf)

### Differentiable programming

* [Neural Networks, Types, and Functional Programming](https://colah.github.io/posts/2015-09-NN-Types-FP/)
* [Backpropagation with Continuation Callbacks: Foundations for Efficient and Expressive Differentiable Programming](https://papers.nips.cc/paper/8221-backpropagation-with-callbacks-foundations-for-efficient-and-expressive-differentiable-programming.pdf)
* [Backprop as Functor: A compositional perspective on supervised learning](https://arxiv.org/pdf/1711.10455.pdf)
* [Demystifying Differentiable Programming: Shift/Reset the Penultimate Backpropagator](https://www.cs.purdue.edu/homes/rompf/papers/wang-preprint201811.pdf)
* [Efficient Differentiable Programming in a Functional Array-Processing Language](https://arxiv.org/pdf/1806.02136.pdf)
* [Operational Calculus for Differentiable Programming](https://arxiv.org/pdf/1610.07690.pdf)
* [Differentiable Functional Programming](https://www.robots.ox.ac.uk/~gunes/assets/pdf/baydin-2016-slides-functionallondoners.pdf)
* [Differentiable Programming for Image Processing and Deep Learning in Halide](https://people.csail.mit.edu/tzumao/gradient_halide/gradient_halide.pdf)
* [Software 2.0](https://medium.com/@karpathy/software-2-0-a64152b37c35)

### Calculus

* [The Matrix Calculus You Need For Deep Learning](https://explained.ai/matrix-calculus/index.html), Parr and Howard (2018)
* [Backpropagation in matrix notation](https://arxiv.org/pdf/1707.02746.pdf), Mishachev (2017)
* [Matrix derivatives](https://www.math.uwaterloo.ca/~hwolkowi/matrixcookbook.pdf#derivatives), from the Matrix Cookbook
* [Div, Grad, Curl and All That](https://archive.org/details/H.M.ScheyDivGradCurlAndAllThat), Petersen and Pedersen (2012)
* [Matrix Differentiation (and some other stuff)](https://atmos.washington.edu/~dennis/MatrixCalculus.pdf), Barnes (2006)
* [Symbolic Matrix Derivatives](https://www.jstor.org/stable/2236019), Dwyer and Macphail (1948)

### Computer algebra

* [Towards an API for the real numbers](https://doi.org/10.1145/3395658), Boehm (2020)
* [miniKanren as a Tool for Symbolic Computation in Python](https://arxiv.org/pdf/2005.11644.pdf), Willard (2020)
* [A Design Proposal for an Object Oriented Algebraic Library](https://pdfs.semanticscholar.org/6fd2/88960ef83469c898a3d8ed8f0950e7839625.pdf), Niculescu (2003)
* [On Using Generics for Implementing Algebraic Structures](https://www.cs.ubbcluj.ro/~studia-i/contents/2011-4/02-Niculescu.pdf), Niculescu (2011)
* [How to turn a scripting language into a domain-specific language for computer algebra](https://arxiv.org/pdf/0811.1061.pdf), Jolly and Kredel (2008)
* [Evaluation of a Java Computer Algebra System](https://pdfs.semanticscholar.org/ce81/39a9008bdc7d23be0ff05ef5a16d512b352c.pdf), Kredel (2007)
* [Typesafe Abstractions for Tensor Operations](https://arxiv.org/pdf/1710.06892.pdf), Chen (2017)
* [Einstein Summation in Numpy](https://obilaniu6266h16.wordpress.com/2016/02/04/einstein-summation-in-numpy/), Bilaniuk (2016)
* [Issues in Computer Algebra](https://www.cs.rit.edu/~anh/comp_alg.html), Nunes-Harwitt
* [Term Rewriting and All That](https://www21.in.tum.de/~nipkow/TRaAT/), Baader and Nipkow (1998)
* [Describing the syntax of programming languages using conjunctive and Boolean grammars](http://users.utu.fi/aleokh/papers/conj_bool_programming.pdf), Okhotin (2016)
* [Formal languages over GF(2)](https://users.math-cs.spbu.ru/~okhotin/papers/formal_languages_gf2.pdf), Okhotin (2019)

### Symbolic mathematics

* [KMath](https://github.com/altavir/kmath) - Kotlin mathematics extensions library
* [SymJa](https://github.com/axkr/symja_android_library/) - Computer algebra language & symbolic math library for Android
* [tensor](https://github.com/amodeus-science/tensor) - Linear algebra for tensors with symbolic and numeric scalars
* [Hipparchus](https://github.com/Hipparchus-Math/hipparchus) - An efficient, general-purpose mathematics components library in the Java programming language
* [miniKanren](http://minikanren.org/) - A tool for symbolic computation and logic programming
* [SymJava](https://github.com/yuemingl/SymJava) - A Java library for fast symbolic-numeric computation
* [JAS](https://github.com/kredel/java-algebra-system) - Java Algebra System
* [jalgebra](https://github.com/mdgeorge4153/jalgebra) - An abstract algebra library for Java
* [COJAC](https://github.com/Cojac/Cojac) - Numerical sniffing tool and Enriching number wrapper for Java
* [chebfun](https://www.chebfun.org) - Allows representing functions as [Chebyshev polynomials](https://en.wikipedia.org/wiki/Chebyshev_polynomials), for easy symbolic differentiation (or integration)
* [horeilly1101/deriv](https://github.com/horeilly1101/deriv) - Open source derivative calculator REST API (and Java library)

### Neural networks

* [Hacker's Guide to Neural Networks](https://karpathy.github.io/neuralnets/), Karpathy (2014)
* [Tricks from Deep Learning](https://arxiv.org/pdf/1611.03777.pdf), Baydin et al. (2016)
* [Practical Dependent Types in Haskell: Type-Safe Neural Networks](https://blog.jle.im/entry/practical-dependent-types-in-haskell-1.html), Le (2016)
* [A guide to convolutional arithmetic for deep learning](https://arxiv.org/pdf/1603.07285.pdf), Dumoulin and Visin (2018)

### Type systems

* [Generalized Algebraic Data Types and Object-Oriented Programming](https://www.microsoft.com/en-us/research/wp-content/uploads/2016/02/gadtoop.pdf), Kennedy and Russo (2005)
* [Java Generics are Turing Complete](https://arxiv.org/pdf/1605.05274.pdf), Grigore (2016)
* [Dimension Types](https://link.springer.com/content/pdf/10.1007%2F3-540-57880-3_23.pdf), Kennedy (2004)
* [An algebraic view of dimension types](https://www.cl.cam.ac.uk/techreports/UCAM-CL-TR-391.pdf#page=145), Kennedy (1996)
* [Type Inference and Unification](https://www.cs.cornell.edu/courses/cs3110/2011sp/Lectures/lec26-type-inference/type-inference.htm)
* [Constructive mathematics and computer programming](https://royalsocietypublishing.org/doi/pdf/10.1098/rsta.1984.0073), Martin-Lof (1984)
* [Programming in Martin-Löf's Type Theory](http://www.cse.chalmers.se/research/group/logic/book/book.pdf#page=23), Nordstrom et al. (1990)

### Domain-specific languages

* [Compiling Embedded Languages](http://conal.net/papers/jfp-saig/compile-dsel.pdf), Elliott et al. (2003)
* [Implicit Staging of EDSL Expressions: A Bridge between Shallow and Deep Embedding](https://static.csg.ci.i.u-tokyo.ac.jp/papers/14/scherr-ecoop2014.pdf), Scherr and Chiba (2014)
* [DSL Implementation Using Staging and Monads](https://dl.acm.org/doi/pdf/10.1145/331963.331975) Sheard et al. (1999)
* [Deeply Reifying Running Code for Constructing a Domain-Specific Language](https://dl.acm.org/doi/pdf/10.1145/2972206.2972219), Chiba et al. (2016)
* [Staged Abstract Interpreters](https://www.cs.purdue.edu/homes/rompf/papers/wei-oopsla19.pdf), Wei et al. (2019)
* [Generating Fluent Embedded Domain-Specific Languages with Subchaining](https://static.csg.ci.i.u-tokyo.ac.jp/papers/19/nakamaru-jcl50.pdf), Nakamaru et al. (2019)
* [Generating a Generic Fluent API in Java](https://arxiv.org/pdf/2002.06179.pdf), Nakamarua and Chiba (2020)
* [Fling – A Fluent API Generator](https://drops.dagstuhl.de/opus/volltexte/2019/10805/pdf/LIPIcs-ECOOP-2019-13.pdf), Gil and Roth (2019)
* [Scripting an IDE for EDSL awareness](https://ilyasergey.net/papers/groovy-dsl.pdf), Sergey et al. (2011)

### Automated testing

* [DeepTest: Automated Testing of Deep-Neural-Network-driven Autonomous Cars](https://arxiv.org/pdf/1708.08559.pdf), Tian et al. (2018)
* [QuickCheck: A Lightweight Tool for Random Testing of Haskell Programs](https://www.eecs.northwestern.edu/~robby/courses/395-495-2009-fall/quick.pdf), Claessen and Hughes (2000)
* [Learning to Discover Efficient Mathematical Identities](https://papers.nips.cc/paper/5350-learning-to-discover-efficient-mathematical-identities.pdf), Zaremba et al. (2014)

### AD libraries

* [TensorFlow.FSharp](https://github.com/fsprojects/TensorFlow.FSharp): An eDSL for writing numerical models in F# with support for interactive tensor shape-checking
* [Stalin∇](https://github.com/Functional-AutoDiff/STALINGRAD), a brutally optimizing compiler for the VLAD language, a pure dialect of Scheme with first-class automatic differentiation operators
* [Autograd](https://github.com/hips/autograd) - Efficiently computes derivatives of NumPy code
* [Myia](https://github.com/mila-udem/myia) - SCT based AD, adapted from Pearlmutter & Siskind's "Reverse Mode AD in a functional framework"
* [JAX](https://github.com/google/jax) - Composable transformations of Python+NumPy programs: differentiate, vectorize, JIT to GPU/TPU, and more
* [Dex](https://github.com/google-research/dex-lang) -  Research language for array processing in the Haskell/ML family
* [Nexus](https://github.com/ctongfei/nexus) - Type-safe tensors, deep learning and probabilistic programming in Scala
* [Tangent](https://github.com/google/tangent) - "Source-to-Source Debuggable Derivatives in Pure Python"
* [Grenade](https://github.com/HuwCampbell/grenade) - composable, dependently typed, practical, and fast RNNs in Haskell
* [Lantern](https://feiwang3311.github.io/Lantern/) - a framework in Scala, based on delimited continuations and multi-stage programming
* [JAutoDiff](https://github.com/uniker9/JAutoDiff) - An Automatic Differentiation Library
* [DiffSharp](https://github.com/DiffSharp/DiffSharp), a functional AD library implemented in the F# language
* [Analitik](https://link.springer.com/content/pdf/10.1007/BF01070461.pdf) - Algebraic language for the description of computing processes using analytical transformations

## Special thanks

The following individuals have helped shape this project through their enthusiasm and thoughtful feedback. Please check out their work.

* [Liam Paull](http://liampaull.ca)
* [Michalis Famelis](https://michalis.famelis.info/)
* [Marc Feeley](http://www.iro.umontreal.ca/~feeley/)
* [Eugene Syriani](http://www-ens.iro.umontreal.ca/~syriani/)
* [Hanneli Tavante](http://hannelita.com/)
* [Stefan Monnier](https://www.iro.umontreal.ca/~monnier/)
* [Alexander Nozik](https://scholar.google.com/citations?user=B-WJi4kAAAAJ)
* [Erik Meijer](https://twitter.com/headinthebox/)
* [Krishna Murthy](https://krrish94.github.io/)
* [Maxime Chevalier-Boisvert](https://pointersgonewild.com/)
* [Kiran Gopinathan](https://scholar.google.com/citations?user=IcuGXgcAAAAJ&hl=en)
* [Jacob Miller](https://scholar.google.ca/citations?user=xG3VWpEAAAAJ)
* [Adam Pocock](http://www.adampocock.com/)
* [Torsten Scholak](https://tscholak.github.io/)
