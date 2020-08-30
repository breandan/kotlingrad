# Grammar

Below is the approximate BNF grammar for Kotlin∇. This is incomplete and subject to change without notice.

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

# Semantics

Below we provide a partial reduction semantics for Kotlin∇.

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
            
(e₁ ⊕ e₂)[e₃ → e₄] = e₁[e₃ → e₄] ⊕ e₂[e₃ → e₄]
(e₁ ⊙ e₂)[e₃ → e₄] = e₁[e₃ → e₄] ⊙ e₂[e₃ → e₄]
       e₁[e₁ → e₂] = e₂
       e₁[e₂ → e₃] = e₁
```

In the notation above, we use subscripts to denote conditional inequality.
If we have two nonterminals with matching subscripts within in the same
production, i.e. `eₘ`, `eₙ` where `m = n`, then `eₘ = eₙ` *must* be true.
If we have two nonterminals with different subscripts in one production,
i.e. `eₘ`, `eₙ` where `m ≠ n`, either `eₘ = eₙ` or `eₘ ≠ eₙ` may be true.
Subscripts have no meaning across multiple productions.