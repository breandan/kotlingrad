# [Thèse de maîtrise](thesis.pdf)

## Résumé

Les outils de programmation sont des programmes informatiques qui aident les humains à programmer des ordinateurs. Les outils sont de toutes formes et tailles, par exemple les éditeurs, les compilateurs, les débogueurs et les profileurs. Chacun de ces outils facilite une tâche principale dans le flux de travail de programmation qui consomme des ressources cognitives lorsqu'il est effectué manuellement. Dans cette thèse, nous explorons plusieurs outils qui facilitent le processus de construction de systèmes intelligents et qui réduisent l'effort cognitif requis pour concevoir, développer, tester et déployer des systèmes logiciels intelligents. Tout d'abord, nous introduisons un environnement de développement intégré (EDI) pour la programmation d'applications Robot Operating System (ROS), appelé Hatchery. Deuxièmement, nous décrivons Kotlin$\nabla$, un système de langage et de type pour la programmation différentiable, un paradigme émergent dans l'apprentissage automatique. Troisièmement, nous proposons un nouvel algorithme pour tester automatiquement les programmes différentiables, en nous inspirant des techniques de tests contradictoires et métamorphiques, et démontrons son efficacité empirique dans le cadre de la régression. Quatrièmement, nous explorons une infrastructure de conteneurs basée sur Docker, qui permet un déploiement reproductible des applications ROS sur la plate-forme Duckietown. Enfin, nous réfléchissons à l'état actuel des outils de programmation pour ces applications et spéculons à quoi pourrait ressembler la programmation de systèmes intelligents à l'avenir.

## Construire

Pour construire cette thèse, une distribution [TeX Live](https://www.latex-project.org/get/) est nécessaire.
Exécutez la commande suivante à partir du répertoire parent:

```
xelatex -file-line-error -interaction=nonstopmode -synctex=1 --shell-escape -output-directory=$(pwd) thesis
```

### Police de télétype

Une police de caractères personnalisée, [JetBrains Mono](https://github.com/JetBrains/JetBrainsMono#installation) est nécessaire pour rendre les listes de code source et les jetons de code en ligne.

### Diagrammes de rail

Certaines parties de ce document ont été construites avec un package non standard appelé [rail](https://ctan.org/pkg/rail). Pour modifier les schémas de rails, installez le package comme ceci:

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

Une fois le rail installé, les étapes suivantes régénéreront le diagramme.

1. Lancez `xelatex ... thesis`, qui créera` thesis.rai`.
2. Exécutez `rail thesis` pour générer` thesis.rao` à partir de `thesis.rai`.
3. Exécutez `xelatex ... thesis` pour générer le document final.