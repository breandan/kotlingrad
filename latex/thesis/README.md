# [Master's Thesis](thesis.pdf)

To build this thesis, pdfLaTeX is required.
Run the following command from the parent directory:

```
pdflatex -file-line-error -interaction=nonstopmode -output-directory=$(pwd) thesis.tex
```

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

1. Run `latex mydoc`, which will create `mydoc.rai`.
2. Run `rail mydoc` to generate `mydoc.rao` from `mydoc.rai`.
3. Run `latex mydoc` for the final document.