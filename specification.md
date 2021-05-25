# Grammar

Below is the approximate BNF grammar for Kotlin∇. This is incomplete and subject to change without notice.

## Numerical types:

```ebnf
  𝔹 = "True" | "False"
  𝔻 = "1" | ...  | "9"
  ℕ =  𝔻  | 𝔻"0" | 𝔻ℕ
  ℤ = "0" | ℕ    | -ℕ
  ℚ = ℕ | ℤ"/"ℕ
  ℝ = ℕ | ℕ"."ℕ | "-"ℝ
  ℂ = ℝ + ℝi
  ℍ = ℝ + ℝi + ℝj + ℝk
  T = 𝔹 | ℕ | ℤ | ℚ | ℝ | ℂ | ℍ
  n = ℕ < 100*
vec = [Tⁿ]
mat = [[Tⁿ]ⁿ]
```

<sup>&lowast;</sup> To increase `n`, see [`DimGen.kt`](shipshape/src/main/kotlin/edu/mcgill/shipshape/DimGen.kt).

## DSL

```ebnf
       type = "Double" | "Float" | "Int" | "BigInteger" | "BigDouble";
        nat = "1" | ... | "99";
     output = "Fun<" type "Real>" | "VFun<" type "Real," nat ">" | "MFun<" type "Real," nat "," nat ">";
        int = "0" | nat int;
      float = int "." int;
        num = type "(" int ")" | type "(" float ")";
        var = "x" | "y" | "z" | "ONE" | "ZERO" | "E" | "Var()";
     signOp = "+" | "-";
      binOp = signOp | "*" | "/" | "pow";
     trigOp = "sin" | "cos" | "tan" | "asin" | "acos" | "atan" | "asinh" | "acosh" | "atanh";
    unaryOp = signOp | trigOp | "sqrt" | "log" | "ln" | "exp";
        exp = var | num | unaryOp exp | var binOp exp | "(" exp ")";
    expList = exp | exp "," expList;
      linOp = signOp | "*" | " dot ";
        vec = "Vec(" expList ")" | "Vec" nat "(" expList ")";
     vecExp = vec | signOp vecExp | exp "*" vecExp | vec linOp vecExp | vecExp ".norm(" int ")";
        mat = "Mat" nat "x" nat "(" expList ")";
     matExp = mat | signOp matExp | exp linOp matExp | vecExp linOp matExp | mat linOp matExp;
     anyExp = exp | vecExp | matExp | derivative | invocation;
   bindings = exp " to " exp | exp " to " exp "," bindings;
 invocation = anyExp "(" bindings ")";
 derivative = "d(" anyExp ") / d(" exp ")" | anyExp ".d(" exp ")" | anyExp ".d(" expList ")";
   gradient = exp ".grad()";
```

# Operational Semantics

Below we provide an operational semantics for Kotlin∇.

```ebnf
                 v = a | ... | z | vv
                 c = 1 | ... | 9 | cc | c.c
                 e = v | c | e ⊕ e | e ⊙ e | (e) | (e).d(v) | e(e = e)
                 
       d(e) / d(v) = e.d(v)
      Plus(e₁, e₂) = e₁ ⊕ e₂
     Times(e₁, e₂) = e₁ ⊙ e₂
           c₁ ⊕ c₂ = c₁ + c₂
           c₁ ⊙ c₂ = c₁ * c₂
       e₁(e₂ = e₃) = e₁[e₂ → e₃]
           
    (e₁ ⊕ e₂).d(v) =    e₁.d(v)   ⊕   e₂.d(v)
    (e₁ ⊙ e₂).d(v) = e₁.d(v) ⊙ e₂ ⊕ e₁ ⊙ e₂.d(v)
          v₁.d(v₁) = 1
          v₁.d(v₂) = 0
            c.d(v) = 0

     e.d(v₁).d(v₂) = e.d(v₂).d(v₁) †

(e₁ ⊕ e₂)[e₃ → e₄] = e₁[e₃ → e₄] ⊕ e₂[e₃ → e₄]
(e₁ ⊙ e₂)[e₃ → e₄] = e₁[e₃ → e₄] ⊙ e₂[e₃ → e₄]
       e₁[e₁ → e₂] = e₂
       e₁[e₂ → e₃] = e₁
```

In the notation above, we use subscripts to denote conditional inequality. If we have two nonterminals with matching subscripts in the same production, i.e. `eₘ`, `eₙ` where `m = n`, then `eₘ = eₙ` *must* be true. If we have two nonterminals with different subscripts in the same production, i.e. `eₘ`, `eₙ` where `m ≠ n`, either `eₘ = eₙ` or `eₘ ≠ eₙ` may be true. Subscripts have no meaning across multiple productions.

<sup>&dagger;</sup> See [Clairaut-Schwarz](https://en.wikipedia.org/wiki/Symmetry_of_second_derivatives). Commutativity holds for twice-differentiable functions, however it is possible to have a continuously differentiable function whose partials are defined everywhere, but whose permuted partials are unequal ∂F²/∂x∂y ≠ ∂F²/∂y∂x. Some examples of continuously differentiable functions whose partials do not commute may be found in Tolstov ([1949](http://www.mathnet.ru/links/c5537a9da2ecaa5e1eafbafda3f15a1e/sm5966.pdf), [1949](http://www.mathnet.ru/links/a8dfdc976cf22fdce373cdcba2eeda2b/im3207.pdf)).

# Reduction semantics

Our reduction semantics can be described as follows:

![](https://user-images.githubusercontent.com/175716/119237696-21713100-bb0c-11eb-9740-8753362249d0.png)

`Sub` loosely corresponds to η-reduction in the untyped λ-calculus, however f ∉ Ω is disallowed, and we assume all variables are bound by `Inv`. Let us consider what happens under f∶ {a, b, c} ↦ abc, g∶ {a, b, c} ↦ ac under Ω ∶= {(a, 1), (b, 2), (c, 2)}:

![](https://user-images.githubusercontent.com/175716/119237755-7d3bba00-bb0c-11eb-8bea-cd9933d3b716.png)

We can view the above process as acting on a dataflow graph, where `Inv` backpropagates Ω, and `Sub` returns concrete values Y, here depicted on g:

![](https://user-images.githubusercontent.com/175716/119237786-af4d1c00-bb0c-11eb-85e4-7699e05d6c0b.png)