# [Master's Thesis](thesis.pdf)

To build this thesis, a [TeX Live](https://www.latex-project.org/get/) distribution is required.
Run the following command from the parent directory:

```
xelatex -file-line-error -interaction=nonstopmode -synctex=1 --shell-escape -output-directory=$(pwd) thesis
```

## Teletype Font

A custom typeface, [JetBrains Mono](https://github.com/JetBrains/JetBrainsMono#installation) is needed to render source code listings and inline code tokens.

## Rail diagrams

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
