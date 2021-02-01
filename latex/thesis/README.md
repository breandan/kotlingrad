# [Master's Thesis](thesis.pdf)

Archival version: http://hdl.handle.net/1866/24310

## Abstract

Programming tools are computer programs which help humans program computers. Tools come in all shapes and forms, from editors and compilers to debuggers and profilers. Each of these tools facilitates a core task in the programming workflow which consumes cognitive resources when performed manually. In this thesis, we explore several tools that facilitate the process of building intelligent systems, and which reduce the cognitive effort required to design, develop, test and deploy intelligent software systems. First, we introduce an integrated development environment (IDE) for programming Robot Operating System (ROS) applications, called Hatchery. Second, we describe Kotlin∇, a language and type system for differentiable programming, an emerging paradigm in machine learning. Third, we propose a new algorithm for automatically testing differentiable programs, drawing inspiration from techniques in adversarial and metamorphic testing, and demonstrate its empirical efficiency in the regression setting. Fourth, we explore a container infrastructure based on Docker, which enables reproducible deployment of ROS applications on the Duckietown platform. Finally, we reflect on the current state of programming tools for these applications and speculate what intelligent systems programming might look like in the future.

## Building

To build this thesis, a [TeX Live](https://www.latex-project.org/get/) distribution is required.
Run the following command from the parent directory:

```
xelatex -file-line-error -interaction=nonstopmode -synctex=1 --shell-escape -output-directory=$(pwd) thesis
```

### Teletype Font

A custom typeface, [JetBrains Mono](https://github.com/JetBrains/JetBrainsMono#installation) is needed to render source code listings and inline code tokens.

### Rail diagrams

Parts of this document were built with a non-standard package called [rail](https://ctan.org/pkg/rail). To modify the rail diagrams, install the package like so:

```
$ curl -L https://github.com/Holzhaus/latex-rail/archive/v1.2.1.tar.gz | tar xzvf -

$ cd latex-rail-1.2.1

$ make
bison -y  -dv gram.y
gram.y: warning: 2 reduce/reduce conflicts [-Wconflicts-rr]
cmp -s gram.c y.tab.c || cp y.tab.c gram.c
cmp -s gram.h y.tab.h || cp y.tab.h gram.h
gcc -DYYDEBUG -O   -c -o rail.o rail.c
gcc -DYYDEBUG -O   -c -o gram.o gram.c
flex  -t lex.l > lex.c
gcc -DYYDEBUG -O   -c -o lex.o lex.c
gcc -DYYDEBUG -O rail.o gram.o lex.o -o rail

$ sudo make PREFIX=/usr install
$ sudo mktexlsr
```

When the rail has been installed, the following steps will regenerate the diagram.

1. Run `xelatex ... thesis`, which will create `thesis.rai`.
2. Run `rail thesis` to generate `thesis.rao` from `thesis.rai`.
3. Run `xelatex ... thesis` to generate the final document.
