%%
%% This is file `dms.cls',
%% generated with the docstrip utility.
%%
%% The original source files were:
%%
%% dms.dtx  (with options: `classe')
%%
%%% ====================================================================
%%%  @LaTeX-file{
%%%     filename        = "dms.dtx",
%%%     author    = "Nicolas Beauchemin, Damien Rioux-Lavoie, Victor Fardel, Jonathan Godin",
%%%     copyright = "Copyright (C) 2000 , DMS
%%%                  all rights reserved.  Copying of this file is
%%%                  authorized only if either:
%%%                  (1) you make absolutely no changes to your copy,
%%%                  including name; OR
%%%                  (2) if you do make changes, you first rename it
%%%                  to some other name.",
%%%     address   = "Département de Mathématiques et de Statistique",
%%%     telephone = "514-343-6705",
%%%     FAX       = "514-343-5700",
%%%     email     = "aide@dms.umontreal.ca (Internet)",
%%%     keywords  = "latex, amslatex, ams-latex, theorem",
%%%     abstract  = " Ce fichier est un package conçu pour être
%%%                  utilisé avec la version de LaTeX2e 1995/06/01. Il
%%%                  est prévue pour la classe ``amsbook''. Il en
%%%                  modifie le format des pages, l'entête des
%%%                  sections, etc, afin d'être  conforme au modèle de
%%%                  mémoire de maîtrise de l'Université de
%%%                  Montréal. Finalement ce fichier est grandement
%%%                  inspiré du fichier amsclass.dtx.",
%%%     docstring = "The checksum field contains: CRC-16 checksum,
%%%                  word count, line count, and character count, as
%%%                  produced by Robert Solovay's checksum utility."}
%%%  ====================================================================

\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{dms}[2019/07/18 2.2]
\newif\iftagsleft@
\newif\ifphd@
\newif\ifmaitrise@
\newif\ifrapport@
\newif\iftravail@
\newif\ifnobabel@
\newif\ifinitial@
\newif\ifrectoverso@
\newif\ifpagetitreart@

\DeclareOption{twoside}{\rectoverso@true}
\DeclareOption{nobabel}{\nobabel@true}
\DeclareOption{phd}{\phd@true}
\DeclareOption{maitrise}{\maitrise@true}
\DeclareOption{rapport}{\rapport@true}
\DeclareOption{travaildirige}{\travail@true}
\DeclareOption{initial}{\initial@true}
\DeclareOption{leqno}{\tagsleft@true}
\DeclareOption{reqno}{\tagsleft@false}
\DeclareOption{pagetitreart}{\pagetitreart@true}
\DeclareOption*{\PassOptionsToClass{\CurrentOption}{amsbook}}
\ProcessOptions\relax
\iftagsleft@ {\PassOptionsToClass{leqno}{amsbook}}
\else{\PassOptionsToClass{reqno}{amsbook}}\fi
\ifrectoverso@
\PassOptionsToClass{twoside}{amsbook}
\else
\PassOptionsToClass{oneside}{amsbook}
\fi
\def\@tempa#1#2\@nil{\edef\@classname{#1}}
\expandafter\@tempa\@currnamestack{}{}{}\@nil
\ifx\@classname\@empty \edef\@classname{\@currname}\fi
\LoadClass{amsbook}
\newcommand\HUGE{\@xsetfontsize\HUGE{12}}

\edef\partnamefr{Partie}
\edef\partnameen{Part}

\edef\dmschapternamefr{Chapitre}
\edef\dmschapternameen{Chapter}

\edef\appendixnamefr{Annexe}
\edef\appendixnameen{Appendix}

\edef\bibnamefr{Bibliographie}
\edef\bibnameen{Bibliography}

\edef\dms@etfr{ et}
\edef\dms@eten{, and}

\edef\dmslistfigurenamefr{Liste des figures}
\edef\dmslistfigurenameen{List of figures}

\edef\dmslisttablenamefr{Liste des tableaux}
\edef\dmslisttablenameen{List of tables}

\edef\lemmanamefr{Lemme}
\edef\lemmanameen{Lemma}

\edef\corollarynamefr{Corollaire}
\edef\corollarynameen{Corollary}

\edef\examplenamefr{Exemple}
\edef\examplenameen{Example}

\edef\remarknamefr{Remarque}
\edef\remarknameen{Remark}

\edef\tablenamefr{Tableau}
\edef\tablenameen{Table}

\edef\dmscontentsnamefr{Table des mati\noexpand\`eres} %\'e car on ne sait pas si inputenc sera chargé
\edef\dmscontentsnameen{Contents}

\edef\revuemsgfr{Cet article a \noexpand\'et\noexpand\'e soumis dans }
\edef\revuemsgen{This article was submitted in }

\edef\contribmsgfr{Les principales contributions de
\noexpand\authors\ \noexpand\`a cet article sont pr\noexpand\'esent\noexpand\'ees.}
\edef\contribmsgen{The main contributions of
\noexpand\authors\ for this articles are presented.}

\edef\dms@articlenamefr{article}
\edef\dms@articlenameen{Article}

\edef\dms@parnamefr{par}
\edef\dms@parnameen{by}

\AtBeginDocument{
\edef\refnamefr{R\'ef\'erences bibliographiques}
\edef\refnameen{References}

\edef\revuemsgfr{Cet article a \'et\'e soumis dans }
\edef\revuemsgen{This article was submitted in }

\edef\contribmsgfr{Les principales contributions de
\noexpand\authors\ \`a cet article sont pr\'esent\'ees.}
\edef\contribmsgen{The main contributions of
\noexpand\authors\ for this articles are presented.}

\edef\keywordsfr{Mots cl\'es}
\edef\keywordsen{Keywords}

\edef\dmscontentsnamefr{Table des mati\`eres} %\'e car on ne sait pas si inputenc sera chargé
\edef\dmscontentsnameen{Contents}

\edef\abstractnamefr{R\'esum\'e}
\edef\abstractnameen{Abstract}

\edef\theoremnamefr{Th\'eor\`eme}
\edef\theoremnameen{Theorem}

\edef\algonamefr{Algorithme}
\edef\algonameen{Algorithm}

\edef\definitionnamefr{D\'efinition}
\edef\definitionnameen{Definition}

\edef\problemnamefr{Probl\`eme}
\edef\problemnameen{Problem}

\edef\proofnamefr{D\'emonstration}
\edef\proofnameen{Proof}
}  %Fin \AtBeginDocument
\def\parttocentryfr{{\ifcase\value{part}\or Premi\`ere\or Deuxi\`eme\or
Troisi\`eme\or Quatri\`eme\or Cinqui\`eme\or Sixi\`eme\or
Septi\`eme\or Huiti\`eme\or Neuvi\`eme\or Dixi\`eme\or Onzi\`eme\or
Douzi\`eme\or Treizi\`eme\or Quatorzi\`eme\or Quinzi\`eme\or
Seizi\`eme\or Dix-septi\`eme\or Dix-huiti\`eme\or Dix-neuvi\`eme\or
Vingti\`eme\fi}\space partie.}
\def\parttocentryen{\partname\ \thepart.}

\def\articletocentryfr{\ifcase\value{part}\or Premier\or Deuxi\`eme\or
Troisi\`eme\or Quatri\`eme\or Cinqui\`eme\or Sixi\`eme\or
Septi\`eme\or Huiti\`eme\or Neuvi\`eme\or Dixi\`eme\or Onzi\`eme\or
Douzi\`eme\or Treizi\`eme\or Quatorzi\`eme\or Quinzi\`eme\or
Seizi\`eme\or Dix-septi\`eme\or Dix-huiti\`eme\or Dix-neuvi\`eme\or
Vingti\`eme\fi}

\def\articletocentryen{\ifcase\value{part}\or First\or Second\or
Third\or Fourth\or Fifth\or Sixth\or Seventh\or Eighth\or Ninth\or
Tenth\or Eleventh\or Twelfth\or Thirteenth\or Fourteenth\fi}

\newcount\escapecount
\def\dopolyglot{\do\partname \do\dmschaptername \do\appendixname \do\bibname \do\refname%
\do\dms@et \do\keywords \do\dmscontentsname \do\dmslistfigurename \do\dmslisttablename%
\do\theoremname \do\algoname \do\definitionname \do\lemmaname \do\abstractname%
\do\problemname \do\corollaryname \do\examplename \do\remarkname \do\proofname%
\do\revuemsg \do\contribmsg \do\dms@articlename \do\dms@parname \do\tablename}


% On vérifie que le francais est installé

\ifx\undefined\l@french %On vérifie LaTeX
\ifx\undefined\lang@french % On vérifie XeLaTeX
\errmsg{Le francais n'est pas installé!}\errhelp{Assurez-vous que les packages pour le francais soient installés.}
\def\dms@apostrophelccode=0
\def\l@language{0} % On utilise l'anglais vu que le français n'est pas installé...
\else
\let\l@french=\lang@french
\def\dms@apostrophelccode{39} % On prépare le "lower case code" de l'apostrophe
\fi
\else
\def\dms@apostrophelccode{39}
\fi

\def\@francais{%
\escapecount=\escapechar
\escapechar=-1
\def\do##1{\edef##1{\csname\string##1fr\endcsname}}
\dopolyglot
\def\bibname{R\'ef\'erences bibliographiques}
\escapechar=\escapecount
\language\l@french
\lccode`'=\dms@apostrophelccode
\lefthyphenmin=2
\righthyphenmin=3
\let\parttocentry\parttocentryfr%
\let\articletocentry=\articletocentryfr
}
\def\francais{
\@francais
\addtocontents{toc}{\protect\@francais}
}
\let\@fr\francais


% On vérifie que l'anglais est bien installé
\ifx\undefined\l@english %On vérifie LaTeX
\ifx\undefined\lang@english % On vérifie XeLaTeX
\errmsg{L'anglais n'est pas installé!}\errhelp{Assurez-vous que les packages pour l'anglais soient installés.}
\def\l@language{0} % On utilise l'anglais américain de base
\else
\let\l@english=\lang@english
\fi
\fi

\def\@anglais{%
\escapecount=\escapechar
\escapechar=-1
\def\do##1{\edef##1{\csname\string##1en\endcsname}}
\dopolyglot
\def\bibname{References}
\escapechar=\escapecount
\language\l@english
\lccode`'=0
\lefthyphenmin=2
\righthyphenmin=3
\let\parttocentry\parttocentryen%
\let\articletocentry=\articletocentryen
}
\def\anglais{
\@anglais
\addtocontents{toc}{\protect\@anglais}
}
\let\@en\anglais
\ifx\undefined\english \def\english{\anglais}\fi % Si \anglais, alors \english aussi


\newif\ifentetedynamique
\global\entetedynamiquefalse
\def\entetedynamique{
\entetedynamiquetrue
\polyglotlet\chaptername=\dmschaptername
\addtopolyglot\chaptername
\polyglotlet\contentsname=\dmscontentsname
\addtopolyglot\contentsname
\polyglotlet\listtablename=\dmslisttablename
\addtopolyglot\listtablename
\polyglotlet\listfigurename=\dmslistfigurename
\addtopolyglot\listfigurename
}
% On s'attend que #1 soit de la forme
% \mamacro et que \mamacrofr et \mamacroen
% soient définies («mamacro» peut être n'importe
% chaîne de caractères valide pour une macro)
\def\addtopolyglot#1{\expandafter\def\expandafter\dopolyglot\expandafter{\dopolyglot \do#1}}
\def\polyglotlet#1{\def\@polyarg{#1}\futurelet\next\@polylet}
\def\@polylet{\bgroup%
\ifx\next=
\aftergroup\@polyletegal
\else
\aftergroup\@polyletsansegal
\fi
\expandafter\egroup\@polyarg%
}
\def\@polyletegal#1=#2{\@polyglotlet{#1}{#2}}
\def\@polyglotlet#1#2{%
\escapecount=\escapechar
\escapechar=-1
\expandafter\let\csname\string#1fr\expandafter\endcsname\csname\string#2fr\endcsname
\expandafter\let\csname\string#1en\expandafter\endcsname\csname\string#2en\endcsname
\escapechar=\escapecount
\let#1=#2%
}
\let\@polyletsansegal=\@polyglotlet

\def\uselanguage#1{\csname #1\endcsname}
\def\envlangue#1{%
\newenvironment{#1}
{\begingroup\uselanguage{#1}}
{\endgroup}%
}
\envlangue{fr}
\envlangue{en}

\font\ecrm=ecrm1200
\def\guillemetouvrant{{\ecrm\char19}}
\def\guillemetfermant{{\ecrm\char20}}

\ifnobabel@
\def\chaptername{CHAPTER}
\def\thechapter{\arabic{chapter}}
\def\appendixname{APPENDIX}
\fi



%%%Voir nouvelle version ligne 100
%%%\DeclareFontShape{T1}{lmr}{bx}{sc} { <-> ssub * cmr/bx/sc }{} % permet l'ecriture en gras de tableau.. figure...

\voffset=0pt %1 inch=2.54 cm de marge en haut
\headheight=12pt
\headsep=16pt
\footskip=52pt%44pt%20pt
\textheight=656pt
\topskip=10pt
\topmargin=-29pt
\textwidth=470pt%451pt%413pt
\columnsep=10pt
\columnseprule=0pt
\oddsidemargin=0pt%56pt
\evensidemargin=0pt
\hoffset=0pt
\marginparwidth=0pt%60pt
\marginparsep=0pt%10pt
\marginparpush=5pt

\def\ps@headings{\ps@empty
\def\@evenhead{\hfil}%
\let\@oddhead\@evenhead%
\def\@oddfoot{\hss\normalfont\normalsize\rm
\thepage\hss}
\let\@evenfoot\@oddfoot
\let\@mkboth\markboth
\def\partmark{\@secmark\markboth\partrunhead\partname}%
\def\chaptermark{%
\@secmark\markboth\chapterrunhead{}}%
\def\sectionmark{%
\@secmark\markright\sectionrunhead\sectionname}%
}
\def\ps@firstheadings{\let\@mkboth\@gobbletwo
\def\@oddhead{\hfil}
\let\@evenhead\@oddhead
\def\@oddfoot{\hss\normalfont\normalsize\rm\thepage\hss}
\let\@evenfoot\@oddfoot
}
\def\ps@myheadings{\let\@mkboth\@gobbletwo
\let\@oddfoot\@empty\let\@evenfoot\@empty
\def\@oddhead{\null\\normalfont\normalsize\rm \hfil{\thepage}}
\def\@evenhead{\null\\normalfont\normalsize\rm
{\thepage}\hfil\null}
}
\def\sh@rtauthor{}
\def\sh@rttitle{}

\def\brouillon{
\def\ps@headings{\let\@mkboth\@gobbletwo
\let\partmark\@gobble\let\sectionmark\@gobble
\let\chaptermark\@gobble
\let\subsectionmark\@gobble
\def\@oddfoot {\null\normalfont
\footnotesize\hfil[Version\, \@version:
\todayfr---\hhmm\,]\hfil}
\def\@evenfoot{\null\normalfont
\footnotesize\hfil[Version\, \@version:
\todayfr---\hhmm\,]\hfil}
\def\@evenhead{\normalfont\normalsize\rm
\rlap{\thepage}\hfil{\sh@rtauthor}\hfil}%
\def\@oddhead{\normalfont\normalsize\rm \hfil
{\sh@rttitle}\hfil\llap{\thepage}}%
}
\def\ps@myheadings{\let\@mkboth\@gobbletwo
\def\@oddfoot {\null\normalfont
\footnotesize\hfil[Version\,\@version:
\todayfr---\hhmm\,]\hfil}
\def\@evenfoot{\null\normalfont
\footnotesize\hfil[Version\,\@version:
\todayfr---\hhmm\,]\hfil}
\def\@oddhead{\null\normalfont
\normalsize\rm
\hfil\thepage}%
\def\@evenhead{\normalfont
\normalsize\rm
\thepage \hfil\null}%
}
\def\ps@firstheadings{\let\@mkboth\@gobbletwo
\let\partmark\@gobble\let\sectionmark\@gobble
\let\chaptermark\@gobble
\let\subsectionmark\@gobble
\def\@oddfoot {\null\normalfont
\footnotesize\hfil[Version\,\@version:
\todayfr---\hhmm\,] \hfil}%\thepage\hfil
\def\@evenfoot{\null\normalfont
\footnotesize\hfil[Version\,\@version:
\todayfr---\hhmm\,] \hfil}%\thepage\hfil
\let\@evenhead\@empty \let\@oddhead\@empty
}
\ps@headings
}
\def\hhmm{%
\count1=\time                  % l'heure actuelle en minute 00:00
\count2=\count1                % count2 contient l'heure en minutes
\divide \count1 by 60          % count1 contient l'heure en heures
\count3=\count1                % count3 contient hh en heures
\multiply \count1 by 60        % count1 contient hh en minutes
\advance\count2 by -\count1    % count2 contient mm en minutes
% il ne doit pas y avoir d'espace
% après<\advance>
\number\count3:\number\count2  % affiche heures:minutes
}
\edef\@baselineskip@original{\the\baselineskip}
\def\restorebaselineskip{\baselineskip=\@baselineskip@original}

\renewcommand{\title}[2][]{\gdef\@title{#2}}
\let\titre=\title
\def\soustitre#1{\gdef\@soustitre{#1}}
\let\@soustitre\@empty
\let\subtitle=\soustitre


\def\copyrightname#1{\def\@copyrightname{{\sc#1}}}
\def\@copyrightname{{\tt\string\author}} %Rappel à l'utilisateur de définir \author

\renewcommand{\author}[1]{%
\gdef\authors{#1}%
\g@addto@macro\addresses{\author{}}%
\def\@copyrightname{#1}
}

\let\authors\@empty \let\addresses\@empty
\def\mdate#1{\def\@mdate{{#1}}}
\def\@mdate{}

\def\version#1{\def\@version{{#1}}}
\version{0}

\def\university#1{\def\@university{{\normalfont\bf\Large#1}}}
\university{{\bf\Large Universit\'e de Montr\'eal}}

\def\facultyto#1{\def\@facultyto{{\normalfont\rm#1}}}
\facultyto{{\rm Facult\'e des \'etudes sup\'erieures et postdoctorales}}

\def\department#1{\def\@department{{\normalfont\rm#1}}}
\def\@department{ D\'epartement de math\'ematiques et de statistique}

\def\faculty#1{\def\@faculty{{\normalfont\rm#1}}}
\def\@faculty{}

\def\date#1{\def\@date{{\normalfont\rm#1}}}
\def\@date{}

\def\copyrightyear#1{\def\@copyrightyear{{\normalfont\footnotesize#1}}}
\def\@copyrightyear{}

\def\copyrightline{%
\centerline{\footnotesize ${}^{\text{\copyright}}$
\ifx\@copyrightname\empty{..........},
\else {\@copyrightname},
\fi%
\ \@copyrightyear%
}%fin du centerline
}


\def\degree#1{\def\@degree{{\normalfont\rm\small#1}}}
\ifmaitrise@\def\@degree{Ma\^itre \`es sciences (M.Sc.)} \fi
\ifrapport@\def\@degree{Ma\^itre \`es sciences (M.Sc.)} \fi
\iftravail@\def\@degree{Ma\^itre \`es sciences (M.Sc.)} \fi
\ifphd@\def\@degree{Philosophi\ae\ Doctor (Ph.D.)} \fi

\def\orientation#1{\def\@orientation{{\normalfont\rm\small#1}}}
\def\@orientation{}

\def\sujet#1{\def\@sujet{{\normalfont\rm\small#1}}}
\def\@sujet{}

\def\president#1{\def\@president{{\normalfont\sl#1}}}
\def\@president{}

\def\directeur#1{\def\@directeur{{\normalfont\sl#1}}}
\def\@directeur{}

\def\codirecteur#1{\def\@codirecteur{{\normalfont\sl#1}}}
\def\@codirecteur{}

\def\codirecteurs#1{\def\@codirecteurs{{\normalfont\sl#1}}}
\def\@codirecteurs{}

\def\membrejury#1{\def\@membrejury{{\normalfont\sl#1}}}
\def\@membrejury{}

\def\membresjury#1{\def\@membresjury{{\normalfont\sl#1}}}
\def\@membresjury{}

\def\plusmembresjury#1{\def\@plusmembresjury{{\normalfont\sl#1}}}
\def\@plusmembresjury{}

\def\examinateur#1{\def\@examinateur{{\normalfont\sl#1}}}
\def\@examinateur{}

\def\repdoyen#1{\def\@repdoyen{{\normalfont\sl#1}}}
\def\@repdoyen{}

\def\dateacceptation#1{\def\@dateacceptation{{\normalfont\sl#1}}}
\def\@dateacceptation{}

\def\Year{\number\year}

\def\todayfr{\space\number\day\space\MoYrfr}

\def\MoYrfr{%
\ifcase\month\or
janvier\or f\'evrier\or mars\or avril\or mai\or juin\or
juillet\or ao\^ut\or septembre\or octobre\or novembre\or d\'ecembre
\fi%
\space\number\year}%
\def\maketitle{\par
\@topnum\z@
\ifx\@empty\sh@rtauthor \let\sh@rtauthor\sh@rttitle\fi
\begingroup
\@maketitle
\endgroup
\thispagestyle{empty}%
\c@footnote\z@
\def\do##1{\let##1\undefined}%
\do\maketitle \do\@maketitle

\let\maketitle\@maketitletwo
} %Fin de la def de \maketitle
\def\@maketitle{%
\clearpage \thispagestyle{empty}%
\begingroup
\normalfont\normalsize
\topskip=84\p@  %Espace au haut de la page
\advance\topskip by -\headsep
\advance\topskip by -\headheight
\vskip12\p@\@plus12\p@
\centerline{\bf\Large\@university}
\vskip42\p@
\vtop{\centering
\bgroup\bf\Large\expandafter\upshape\expandafter{\@title}\@@par\egroup%
\global\dimen@i=\prevdepth%
}%
\prevdepth=\dimen@i
\vskip-32pt
\vfill
\centerline{\rm par}\vskip20pt%
\ifx\@empty\authors
\dms@erreur\author %on rappelle de définir \author
\else %ici, \authors est non vide
\vtop{\centering
{\bf\Large\authors}\@@par
\global\dimen@i=\prevdepth%
}\relax%
\prevdepth=\dimen@i
\fi
\vskip18\p@\@plus12\p@
\vtop{\centering
{\rm\footnotesize\@department\\
\ifx\@empty\@faculty%
{Facult\'e des arts et des sciences}
\else%
\@faculty
\fi
\@@par}
}%Fin du \vtop
\vfill
\baselineskip9\p@
\vtop{\centering\bgroup\rm\small%
\ifmaitrise@  M\'emoire pr\'esent\'e      \fi
\ifrapport@ Rapport de stage pr\'esent\'e \fi
\iftravail@ Travail dirigé pr\'esent\'e   \fi
\ifphd@     Th\`ese pr\'esent\'ee         \fi
en vue de l'obtention du grade de\\
\@degree\\
en\strut\
            \ifx\@empty\@sujet
\dms@erreur\sujet
\else
\@sujet
\fi
\ifx\@empty\@orientation\else \\[\baselineskip] Orientation \@orientation\fi
\egroup%
}
\vfill
\centerline{\rm\ifx\@empty\@date {\MoYrfr}
\else\@date\fi}
\@@par
\vfill
\copyrightline
\endgroup
\cleardoublepage
} %fin \def\@maketitle

\def\@maketitletwo{
\cleardoublepage\thispagestyle{empty}
\begingroup
\topskip 5pc\relax
\vskip12\p@\@plus6\p@
\normalfont\normalsize
\centerline{\bf\Large\@university}
\centerline{\rm\@facultyto}
\vskip18\p@\@plus12\p@
\ifmaitrise@ \centerline{Ce m\'emoire intitul\'e}\fi
\ifrapport@  \centerline{Ce rapport de stage intitul\'e}\fi
\iftravail@  \centerline{Ce travail dirig\'e intitul\'e}\fi
\ifphd@ {\centerline{Cette th\`ese intitul\'ee}}\fi
\vskip12\p@\@plus6\p@
\vtop{
\raggedcenter\bf\large
\ifx\@empty\@title
\dms@erreur\title
\fi
\expandafter\upshape\expandafter{\centering\@title}\@@par
%
\ifx\@soustitre\@empty\else
\rm\large
\vskip1pc
%
\setbox0=\hbox{\@soustitre}
\ifdim\wd0>410pt\advance\leftskip by 30pt \let\rightskip=\leftskip\fi
\expandafter\upshape\expandafter{\unhbox0}\@@par
\dimen0=\textwidth\advance\dimen0 by -60pt\relax
\fi
\global\dimen@i\prevdepth%
}
\vskip18\p@\@plus12\p@\relax
\ifmaitrise@\centerline{pr\'esent\'e par}\fi
\ifrapport@\centerline{pr\'esent\'e par}\fi
\iftravail@\centerline{pr\'esent\'e par}\fi
\ifphd@\centerline{pr\'esent\'ee par}\fi
\vskip18\p@\@plus6\p@
\ifx\@empty\authors
\dms@erreur\author
\else
\vtop{
\centering
\bf\Large\authors\@@par
\global\dimen@i\prevdepth%
}
\prevdepth\dimen@i
\fi
\vskip18\p@\@plus6\p@
\ifmaitrise@\centerline{a \'et\'e \'evalu\'e par un jury compos\'e des personnes suivantes :}\fi %
\ifrapport@\centerline{a \'et\'e \'evalu\'e par un jury compos\'e des personnes suivantes :} \fi%
\iftravail@\centerline{a \'et\'e \'evalu\'e par un jury compos\'e des personnes suivantes :} \fi%
\ifphd@\centerline{a \'et\'e \'evalu\'ee par un jury compos\'e des personnes suivantes :}\fi %
\par\vskip24\p@ plus 12pt minus 12pt\relax
\baselineskip9\p@
\vtop to 11cm{\centering%
\def\lignemembre##1\titre##2{\vtop{\centering
\escapecount=\escapechar
\escapechar=-1
\expandafter\def\expandafter\dms@temp\expandafter{\string##1}
\escapechar=\escapecount
\setbox0=\hbox{\csname\dms@temp\endcsname}
\unhcopy0\strut\par\vskip-3pt
\noindent\vrule height 0.4pt depth 0pt width%
\ifdim\wd0>11pc \the\wd0\else 11pc\fi
\par\vskip1pt
{\footnotesize(##2)}}}%
% Une ligne est obligatoire si elle doit apparaître
% sur la page titre et non si l'utilateur doit définir
% le champ associé (puisqu'il peut vouloir inscrire la
% signature et donc nécessité une ligne vide).

%Obligatoire
\lignemembre\@president\titre{pr\'esident-rapporteur}
\vfil

%Obligatoire
\lignemembre\@directeur\titre{directeur de recherche}
\vfil

%Optionnel
\ifx\@codirecteur\empty\else
\lignemembre\@codirecteur\titre{codirecteur}
\vfil
\fi

%Optionnel
\ifx\@codirecteurs\empty\else
\lignemembre\@codirecteurs\titre{codirecteur}
\vfil
\fi

%Obligatoire
\lignemembre\@membrejury\titre{membre du jury}
\vfil

%Techniquement, obligatoire, mais il pourrait y avoir des cas
%où il ne faut pas plus d'une ligne pour les membres du jury...
\ifx\@membresjury\empty\else
\lignemembre\@membresjury\titre{membre du jury}
\vfil
\fi

%Même chose ici
\ifx\@plusmembresjury\empty\else
\lignemembre\@plusmembresjury\titre{membre du jury}
\vfil
\fi

%Nécessaire seulement pour les thèses
\ifphd@
\lignemembre\@examinateur\titre{examinateur externe}
\vfil
\fi

%Nécessaire seulement pour les thèses. C'est la personne
%garante du doyen qui assiste à la soutenance. (Il ne fait
%pas parti du jury.)
\ifphd@
\lignemembre\@repdoyen\titre{repr\'esentant du doyen de la FESP}
\vfil
\fi

%%            \ifmaitrise@\centerline{M\'emoire accept\'e le~:}\fi
%%            \ifrapport@\centerline{Rapport de stage accept\'e le~:}\fi
%%            \iftravail@\centerline{Travail dirig\'e accept\'e le~:}\fi
%%            \ifphd@\centerline{Th\`ese accept\'ee le~:}\fi
%%            \vskip6\p@
%%            %
%%            \vtop{\centering
%%                   \ifx\@dateacceptation\empty\vskip14\p@\else
%%                       \@dateacceptation\vskip3\p@\fi}
%%            \centerline{\vbox{\hrule width 10pc}}%
%
\ifx\@membresjury\empty
\par
\vskip0pt plus .33fil
\fi
\ifx\@plusmembresjury\empty
\par
\vskip0pt plus .33fil
\fi
\ifx\@codirecteur\empty
\par
\vskip0pt plus .33fil
\fi
}%fin du \vtop to 10cm
\endgroup
} % Fin de \@maketitle

\ifmaitrise@
\def\@settitle{\begin{center}
                   \baselineskip14\p@\relax
                   \bfseries
                   \uppercasenonmath\@title
                   \@title
\end{center}%
}
\fi
\ifrapport@
\def\@settitle{\begin{center}
                   \baselineskip14\p@\relax
                   \bfseries
                   \uppercasenonmath\@title
                   \@title
\end{center}%
}
\fi
\iftravail@
\def\@settitle{\begin{center}
                   \baselineskip14\p@\relax
                   \bfseries
                   \uppercasenonmath\@title
                   \@title
\end{center}%
}
\fi

\def\titlepage{%
\cleardoublepage
\thispagestyle{empty}\setcounter{page}{0}}
\def\endtitlepage{\newpage}
\renewenvironment{enumerate}{%
\ifnum \@enumdepth >3 \@toodeep\else
\advance\@enumdepth \@ne
\edef\@enumctr{enum\romannumeral\the\@enumdepth}\list
{\csname label\@enumctr\endcsname}{\usecounter
{\@enumctr}\def\makelabel##1{\hss\llap{\rmfamily##1}}}\fi
}{\endlist}
\renewenvironment{itemize}{%
\ifnum\@itemdepth>3 \@toodeep
\else \advance\@itemdepth\@ne
\edef\@itemitem{labelitem\romannumeral\the\@itemdepth}%
\list{\csname\@itemitem\endcsname}%
{\partopsep6\p@\def\makelabel##1{\hss\llap{\rmfamily##1}}}%
\fi}{\endlist}
\AtBeginDocument{%
\settowidth\leftmargini{\labelenumi\hskip\labelsep}%
\advance\leftmargini by \normalparindent
\settowidth\leftmarginii{\labelenumii\hskip\labelsep}%
\ifphd@\advance\leftmarginii by 15pt\fi
\ifmaitrise@\advance\leftmarginii by 6pt\fi
\ifrapport@\advance\leftmarginii by 6pt\fi
\iftravail@\advance\leftmarginii by 6pt\fi
\settowidth\leftmarginiii{\labelenumiii\hskip\labelsep}%
\ifphd@\advance\leftmarginiii by 12pt\fi
\ifmaitrise@\advance\leftmarginiii by 6pt\fi
\ifrapport@\advance\leftmarginiii by 6pt\fi
\iftravail@\advance\leftmarginiii by 6pt\fi
\settowidth\leftmarginiv{\labelenumiv\hskip\labelsep}%
\advance\leftmarginiv by 10pt
\leftmarginv=10pt
\leftmarginvi=10pt
\leftmargin=\leftmargini
\labelsep=5pt
\labelwidth=\leftmargini \advance\labelwidth-\labelsep
\@listi}
\newskip\listisep
\listisep\smallskipamount
\def\@listI{\leftmargin\leftmargini \parsep\z@skip
\topsep\z@skip \itemsep\z@skip
}
\let\@listi\@listI
\@listi
\def\Rel@x{\relax}
\renewenvironment{quote}[1][\relax]
{\bgroup
\def\ArgI{#1}                   %gardé en mémoire pour plus tard
\newdimen\temp \temp=\baselineskip
\renewcommand{\baselinestretch}{1}   %simple interligne
\normalsize
\advance\temp by -\baselineskip\par\vskip\temp
\leftskip3\parindent\rightskip2\parindent
\it\noindent\ignorespaces\guillemetouvrant}
{\penalty10000\guillemetfermant\par
\ifx\ArgI\Rel@x\else       %s'il est faux que #1=\relax
\noindent\null\hfill$\sim$ \ArgI\par\fi
\egroup}
\newenvironment{quote*}[1][\relax]
{\bgroup
\def\ArgI{#1}                   %gardé en mémoire pour plus tard
\newdimen\temp \temp=\baselineskip
\renewcommand{\baselinestretch}{1}   %simple interligne
\normalsize
\advance\temp by -\baselineskip\par\vskip\temp
\leftskip3\parindent\rightskip2\parindent
\rm\noindent\ignorespaces\guillemetouvrant}
{\penalty10000\guillemetfermant\par
\ifx\ArgI\Rel@x\else       %s'il est faux que #1=\relax
\noindent\null\hfill$\sim$ \ArgI\par\fi
\egroup}
\xdef\linestretch{2\baselineskip} % Paramètre pour la profondeur des boîtes
\def\@twocolumnlist#1#2{
\begingroup
\ifx\colun\undefined\let\colun\relax\fi
\ifx\coldeux\undefined\let\coldeux\relax\fi
\parindent0pt
\everycr{}
\def\\{\futurelet\next\action}
\def\action{\ifx\next[\expandafter\actionop\else\expandafter\actionsansop\fi}
\def\actionop[##1]{\cr\noalign{\vskip##1}}
\def\actionsansop{\cr}
\ifvmode\noindent\fi % Le tableau est en mode horizontale
\halign to \textwidth\bgroup %Tableau de la pleine largeur du texte
\hbox to #1{\hsize=#1\vtop{\colun ##}\hfil} % Première colonne
&\hbox to #2{\hsize=#2\vtop{\coldeux ##\hfil\vskip\linestretch\relax}\hfil}\cr %Deuxième colonne
} % fin de la définition de \@twocolumnlist
\def\@endtwocolumnlist{\egroup\endgroup} %On ferme le groupe du \halign et l'autre groupe
\newenvironment{twocolumnlist}[2]
{\@twocolumnlist{#1}{#2}}
{\@endtwocolumnlist}
\def\@threecolumnlist#1#2#3{
\begingroup
\ifx\colun\undefined\let\colun\relax\fi
\ifx\coldeux\undefined\let\coldeux\relax\fi
\ifx\coltrois\undefined\let\coltrois\relax\fi
\parindent0pt
\everycr{}
\def\\{\futurelet\next\action}
\def\action{\ifx\next[\expandafter\actionop\else\expandafter\actionsansop\fi}
\def\actionop[##1]{\cr\noalign{\vskip##1}}
\def\actionsansop{\cr}
\ifvmode\noindent\fi % Le tableau est en mode horizontale
\halign to \textwidth\bgroup %Tableau de la pleine largeur du texte
\hbox to #1{\hsize=#1\vtop{\colun ##}\hfil} % Première colonne
&\hbox to #2{\hsize=#2\vtop{\coldeux ##}\hfil} % Deuxième colonne
&\hbox to #3{\hsize=#3\vtop{\coltrois ##\hfil\vskip\linestretch\relax}\hfil}\cr %Troisième colonne
} % fin de la définition de \@twocolumnlist
\def\@endthreecolumnlist{\egroup\endgroup} %On ferme le groupe du \halign et l'autre groupe

\newenvironment{threecolumnlist}[3]
{\@threecolumnlist{#1}{#2}{#3}}
{\@endthreecolumnlist}

% Macros prisent de plain.tex pour définir
% les alignements (tableaux) utilisant
% la méthode \+ expliqué dans le TeXbook
% (chapitre 22).
\newif\ifus@ \newif\if@cr
\newbox\tabs \newbox\tabsyet \newbox\tabsdone

\def\cleartabs{\global\setbox\tabsyet\null \setbox\tabs\null}
\def\settabs{\setbox\tabs\null \futurelet\next\sett@b}
\let\+=\relax % in case this file is being read in twice
\def\sett@b{\ifx\next\+\def\nxt{\afterassignment\s@tt@b\let\nxt}%
\else\let\nxt\s@tcols\fi \let\next\relax \nxt}
\def\s@tt@b{\let\nxt\relax \us@false\m@ketabbox}
\def\tabalign{\us@true\m@ketabbox} % non-\outer version of \+
\def\+{\tabalign} % \outer\def\+{\tabalign} dans plain.tex (on enlène \outer)
\def\s@tcols#1\columns{\count@#1\dimen@\hsize
\loop\ifnum\count@>\z@ \@nother \repeat}
\def\@nother{\dimen@ii\dimen@ \divide\dimen@ii\count@
\setbox\tabs\hbox{\hbox to\dimen@ii{}\unhbox\tabs}%
\advance\dimen@-\dimen@ii \advance\count@\m@ne}

\def\m@ketabbox{\begingroup
\global\setbox\tabsyet\copy\tabs
\global\setbox\tabsdone\null
\def\cr{\@crtrue\crcr\egroup\egroup
\ifus@\unvbox\z@\lastbox\fi\endgroup
\setbox\tabs\hbox{\unhbox\tabsyet\unhbox\tabsdone}}%
\setbox\z@\vbox\bgroup\@crfalse
\ialign\bgroup&\t@bbox##\t@bb@x\crcr}

\def\t@bbox{\setbox\z@\hbox\bgroup}
\def\t@bb@x{\if@cr\egroup % now \box\z@ holds the column
\else\hss\egroup \global\setbox\tabsyet\hbox{\unhbox\tabsyet
\global\setbox\@ne\lastbox}% now \box\@ne holds its size
\ifvoid\@ne\global\setbox\@ne\hbox to\wd\z@{}%
\else\setbox\z@\hbox to\wd\@ne{\unhbox\z@}\fi
\global\setbox\tabsdone\hbox{\box\@ne\unhbox\tabsdone}\fi
\box\z@}
% Fin des macros prisent dans plain.tex
% pour définir \+
%
%%
%% Utiliser pour mettre des lignes horizontales avant et après le
%% pseudo-code
\long\def\Hline{\par\nobreak\vskip-.5\baselineskip\nobreak\vbox{\hrule width\textwidth height1pt}\nobreak\vskip6pt\nobreak}%

\let\c@part=\undefined
\let\c@chapter=\undefined
\let\c@section=\undefined
\let\c@subsection=\undefined
\let\c@subsubsection=\undefined
\let\c@paragraph=\undefined
\let\c@figure=\undefined
\let\c@table=\undefined
\newcounter{part}
\newcounter{chapter}
\newcounter{section}[chapter]
\newcounter{subsection}[section]
\newcounter{subsubsection}[subsection]
\newcounter{paragraph}[subsubsection]
\renewcommand\thepart          {\arabic{part}}
\renewcommand\thechapter{\arabic{chapter}}
\renewcommand\thesection       {\thechapter.\arabic{section}}
\renewcommand\thesubsection    {\thesection.\arabic{subsection}}
\renewcommand\thesubsubsection {\thesubsection .\arabic{subsubsection}}
\renewcommand\theparagraph     {\thesubsubsection.\arabic{paragraph}}
\setcounter{secnumdepth}{3}
\def\raggedcenter{\leftskip=0pt plus4em \rightskip=\leftskip
\parfillskip=0pt \spaceskip=.3333em \xspaceskip=.5em
\pretolerance=9999 \tolerance=9999 \parindent=0pt
\hyphenpenalty=9999 \exhyphenpenalty=9999 }
\def\part{\cleardoublepage \thispagestyle{empty}%
\null\vfil  \markboth{}{}\secdef\@part\@spart}
\def\@part[#1]#2{%
\ifnum \c@secnumdepth >-2\relax \refstepcounter{part}%
\addcontentsline{toc}{part}{\parttocentry
\protect\enspace\protect\noindent#1}%
\else
\addcontentsline{toc}{part}{#1}
\fi
\begingroup\centering
\ifnum \c@secnumdepth >-2\relax
{\fontsize{\@xviipt}{22}\bfseries
\partname\ \thepart} \vskip 20\p@ \fi
\fontsize{\@xxpt}{25}\bfseries
#1\vfil\vfil\endgroup \newpage\thispagestyle{empty}%
}

\def\@spart#1{\addcontentsline{toc}{part}{\protect\noindent#1}%
\begingroup\centering
\fontsize{\@xxpt}{25}\bfseries
#1\vfil\vfil\endgroup \newpage\thispagestyle{empty}%
}

\def\partrunhead#1#2#3{%
\@ifnotempty{#2}{\uppercase{#1 #2}\@ifnotempty{#3}{. }}%
\def\@tempa{#3}%
\ifx\@empty\@tempa\else\uppercasenonmath\@tempa\@tempa\fi}
\let\chapterrunhead\partrunhead
\let\sectionrunhead\partrunhead

\def\article{
\ifpagetitreart@
\expandafter\article@plus@pagetitre
\else
\expandafter\article@moins@pagetitre
\fi
}

\def\article@plus@pagetitre{
\dms@defmaketitle %Def de \maketitle
\def\dms@defmaketitle{ %La prochaine fois, on ne redéfinit pas \maketitle
\def\article@titre{}
\def\@dedicace{}
\def\@auteurs{}
\def\@adresses{\debut@adresses}
\gdef\dms@adressenum{1}
\def\auteur####1{\let\@et\dms@et\def\@auteurs{####1\dms@adressenote}%
\def\auteur########1{\expandafter\def\expandafter\@auteurs\expandafter{%
\expandafter\begingroup\expandafter\let\expandafter\@et\expandafter,\@auteurs\endgroup%
\@et\ ########1\dms@adressenote}}}
\let\author=\auteur
}
\cleardoublepage
\ifarticleenchapitre
\refstepcounter{chapter}
\advance\c@part by 1
\else
\preparticle
\refstepcounter{part}
\fi
%%%%
\thispagestyle{empty}
\pagestyle{headings}
\global\@topnum=\z@
\@afterindentfalse
\def\@toclevel{0}
\secdef\dms@article\dms@sarticle
} % FIN DE \def\article
%%%%
%%%

\let\finarticle\relax

\def\preparticle{
\def\finarticle{  %fin article est appelé au début de chaque chapitre
\def\thesection{\thechapter.\arabic{section}}
\let\finarticle\relax%
\c@chapter\dms@c@chapter
\let\dms@c@chapter\undefined
}
\ifx\dms@c@chapter\undefined
\edef\dms@c@chapter{\the\c@chapter}
\fi
\c@chapter=255
\advance\c@chapter by -\the\c@part
% Pour repartir les compteurs et enlever les chapitre de la numérotation
\c@section=0
\c@subsection=0
\c@subsubsection=0
\c@paragraph=0
\def\thesection{\arabic{section}}
} % fin de la def de \preparticle

\def\dms@article[#1]#2{
\titre{#2}
\def\temp{#1}
\ifx\temp\empty
\ifarticleenchapitre
\let\@secnumber\thechapter
\@tocwriteb\tocchapter{chapter}{#2} %Écrit chapitre dans la toc
\else
\addcontentsline{toc}{chapter}{\articletocentry\ \dms@articlename. #2} %Écrit article dans la toc
\fi
\else
\ifarticleenchapitre
\let\@secnumber\thechapter
\@tocwriteb\tocchapter{chapter}{#1} %Écrit chapitre dans la toc
\else
\addcontentsline{toc}{chapter}{\articletocentry\ \dms@articlename. #1}
\fi
\fi
\dms@makearticlehead{#2}
}

\def\dms@sarticle#1{
\titre{#1}
\addcontentsline{toc}{chapter}{\articletocentry\ \dms@articlename. #1}
% \@tocwriteb\tocpart{part}{#1}
\dms@makearticlehead{#1}
}


\let\@revue\empty
\let\@revueop\empty
\let\@contributions\empty
\let\@adress\empty
\def\revue{\futurelet\next\revue@switch}
\def\revue@switch{\ifx\next[\expandafter\revue@op\else
\expandafter\revue@sansop\fi}
\def\revue@op[#1]#2{\gdef\@revueop{#1}\gdef\@revue{#2}}
\def\revue@sansop#1{\global\let\@revueop\empty\gdef\@revue{#1}}
\let\journal=\revue
\def\contributions{\futurelet\next\contrib@switch}
\def\contrib@switch{\ifx\next[
\expandafter\contrib@op
\else
\expandafter\contrib@sansop
\fi}
\long\def\contrib@op[#1]#2{\long\gdef\contrib@preambule{{#1}}\long\gdef\@contributions{{#2}}}
\long\def\contrib@sansop#1{
\long\gdef\contrib@preambule{\contribmsg}
\long\gdef\@contributions{{#1}}
}
\let\contribution=\contributions %(Ce n'est pas la quantité, mais la qualité qui compte)

\def\dms@makearticlehead#1% le titre=#1
{
\global\topskip 5pc\relax
% Bloc pour écrire i-ème article et le titre
\bgroup
\bfseries
\ifarticleenchapitre
\vbox{\LARGE\noindent\chaptername\ \the\c@chapter\strut} %en tête en chapitre
\else
\do\dms@parname   \vbox{\LARGE\noindent\articletocentry\ \dms@articlename.\strut} %i-eme article
\fi
\par\vfil
\fontsize{30pt}{25pt}\selectfont
\vbox{\raggedcenter\strut #1\par} %titre en gras
\par\vfil
\fontsize{17.28pt}{19pt}\mdseries\rmfamily\upshape\selectfont
\vbox{\centering\authors\par} %auteur de la thèse
\par\vfil
\ifx\@empty\@revue\dms@erreur\revue\else
\vbox{\centering
\ifx\@revueop\empty \revuemsg \revuemsg
\else\@revueop\fi\@revue\par}
\fi
\par
\cleardoublepage
\egroup
}

\newif\ifarticleenchapitre
\articleenchapitrefalse
\def\articleenchapitre{\placerfigettab\articleenchapitretrue}
\def\placerfigettab{%
\@addtoreset{figure}{chapter}%
\@addtoreset{table}{chapter}%
}
\let\makearticleschapters\articleenchapitre
\let\articlesenchapitres\articleenchapitre
\let\makearticlechapter\articleenchapitre

\ifpagetitreart@\else
\def\dms@defmakesmalltitle{
\dms@prepmaketitle
\def\makesmalltitle{
\@topnum\z@ % this prevents figures from falling at the top of page 1
\global\topskip 5pc\relax
% Bloc pour écrire i-ème article et le titre
\bgroup
\bfseries
\ifarticleenchapitre
\vbox{\LARGE\noindent\chaptername\ \the\c@chapter\strut} %en tête en chapitre
\else
\vbox{\LARGE\noindent\articletocentry\ \dms@articlename.\strut} %i-eme article
\fi
\egroup
\hrule width0pt height32pt depth0pt
\vbox{          % Titre
\fontsize{30pt}{25pt}\bfseries\selectfont
\vbox{\raggedcenter\strut\article@titre\par} %titre en gras
}
\vskip1cm
\vbox{         % Liste des auteurs
\begingroup
\setbox0=\hbox{\@auteurs}
\ifdim\wd0>.7\textwidth
\aftergroup\raggedcenter
\else
\aftergroup\centering
\fi
\endgroup
\dms@parname\par\vskip9mm
\restorebaselineskip
\gdef\dms@adressenum{1}
\@auteurs
\gdef\dms@adressenum{1}
}
\vskip1.5em
\vbox{        % Tableau d'adresses
\restorebaselineskip
\@adresses
\egroup % <-- pour fermer le \halign commencé dans \@adresses
}
\ifx\@dedicace\@empty\else % Si la dédicace est vide, on ne fait rien
\vskip1em plus .5em minus .5em
\vbox{\smaller\itshape\centering\@dedicace\par}
\fi
\vskip2cm
\noindent
\ifx\@revueop\empty \revuemsg
\else\@revueop
\fi%
\ifx\@revue\empty%        % Si \@revue est vide,
\dms@erreur\revue%   % «\revue» sera inscrit
\else%                    % pour rappeler de le remplir
\@revue%
\fi.
\vskip2cm
\expandafter\ifx\csname contrib@preambule\endcsname\relax\else\noindent \bgroup\contrib@preambule\egroup\fi
\csname @contributions\endcsname
\vskip1.5em plus .5em minus .5em
%
} %Fin de la def de \makesmalltitle
\let\maketitle=\makesmalltitle
} %Fin de la def de \dms@defmakesmalltitle
\fi %fin du \ifpagetitreart@


\def\article@moins@pagetitre{
\dms@defmakesmalltitle %Def de \makesmalltitle
\def\dms@defmakesmalltitle{ %La prochaine fois, on ne redéfinit pas \makesmalltitle
\def\article@titre{}
\def\@dedicace{}
\def\@auteurs{}
\def\@adresses{\debut@adresses}
\gdef\dms@adressenum{1}
\def\auteur####1{\let\@et\dms@et\def\@auteurs{####1\dms@adressenote}%
\def\auteur########1{\expandafter\def\expandafter\@auteurs\expandafter{%
\expandafter\begingroup\expandafter\let\expandafter\@et\expandafter,\@auteurs\endgroup%
\@et\ ########1\dms@adressenote}}}
\let\author=\auteur
}
\cleardoublepage
\ifarticleenchapitre
\refstepcounter{chapter}
\advance\c@part by 1
\else
\preparticle
\refstepcounter{part}
\fi
%%%%
\thispagestyle{empty}
\pagestyle{headings}
\global\@topnum=\z@
\@afterindentfalse
\def\@toclevel{0}
\def\dms@makearticlehead##1{\relax}
\secdef\dms@article\dms@sarticle
} % FIN DE \def\article

\def\dms@prepmaketitle{
%Paramètres simples
\def\titre##1{\gdef\article@titre{##1}}
\let\title\titre
\def\@dedicace{}
\def\dedicace##1{\gdef\@dedicace{##1}}
\def\@auteurs{}
\gdef\dms@adressenum{1} % Utiliser pour le num de la note
\def\dms@adressenote{$^\dms@adressenum$} % Racourci pour mettre une note
\def\@adresses{\debut@adresses}
% Paramètres complexes
\def\auteur##1{\let\@et\dms@et\def\@auteurs{##1\dms@adressenote}%
\def\auteur####1{\expandafter\def\expandafter\@auteurs\expandafter{%
\expandafter\begingroup\expandafter\let\expandafter\@et\expandafter,\@auteurs\endgroup%
\@et\ ####1\dms@adressenote}}}
\let\author\auteur
%
\def\dms@adresseincrement{\count255=\dms@adressenum%
\advance\count255 by 1%
\global\edef\dms@adressenum{\the\count255}%
}
\def\debut@adresses{%
\halign to\textwidth\bgroup%
\hbox to 1cm{\hfil{####}\hfil} & \hbox{\vtop{\noindent ####}}\cr%
}
\def\adresse##1{\expandafter\def\expandafter\@auteurs\expandafter{%
\@auteurs\dms@adresseincrement}
\expandafter\def\expandafter\@adresses\expandafter{\@adresses%
(\dms@adressenote\dms@adresseincrement) & ##1\cr\noalign{\vskip5pt}}}
\let\address\adresse
%
% Environnements de résumés
\def\abstractskip{\vskip2em plus 1em minus 1em\relax\penalty-5000} %Racourci
\newenvironment{abstract*}
{\ifvmode\removelastskip\abstractskip\else\par\abstractskip\fi
\vbox\bgroup\smaller\smaller\narrower\narrower
\@anglais
\noindent{\expandafter\larger\expandafter\sc\abstractname.}\ \ignorespaces}
{\egroup}
\renewenvironment{abstract}[1]
{\def\@arg{##1}\begin{abstract*}}
{\par\nobreak\noindent{\bfseries\keywords:} \@arg\end{abstract*}}
\newenvironment{resume*}
{\ifvmode\removelastskip\abstractskip\else\par\abstractskip\fi
\vbox\bgroup\smaller\smaller\narrower\narrower
\@francais
\noindent{\expandafter\larger\expandafter\sc\abstractname.}\ \ignorespaces}
{\egroup}
\newenvironment{resume}[1]
{\def\@arg{##1}\begin{resume*}}
{\par\nobreak\noindent{\bfseries\keywords~:} \@arg\end{resume*}}
} %Fin de la def de \dms@prepmaketitle

\def\dms@defmaketitle{
%%
% Def de MAKETITLE
\dms@prepmaketitle
\def\maketitle{
\@topnum\z@ % this prevents figures from falling at the top of page 1
\hrule width0pt height32pt depth0pt
\vbox{          % Titre
\bfseries\Large
\raggedcenter
\article@titre\par
}
\vskip1cm
\vbox{         % Liste des auteurs
\begingroup
\setbox0=\hbox{\@auteurs}
\ifdim\wd0>.7\textwidth
\aftergroup\raggedcenter
\else
\aftergroup\centering
\fi
\endgroup
par\par\vskip9mm
\restorebaselineskip
\gdef\dms@adressenum{1}
\@auteurs
\gdef\dms@adressenum{1}
}
\vskip1.5em
\vbox{        % Tableau d'adresses
\restorebaselineskip
\@adresses
\egroup % <-- pour fermer le \halign commencé dans \@adresses
}
\ifx\@dedicace\@empty\else % Si la dédicace est vide, on ne fait rien
\vskip1em plus .5em minus .5em
\vbox{\smaller\itshape\centering\@dedicace\par}
\fi
\vskip2cm
\noindent
\ifx\@revueop\empty Cet article a \'et\'e soumis \`a la revue
\else\@revueop
\fi%
\ifx\@revue\empty%        % Si \@revue est vide,
\dms@erreur\revue   % «\revue» sera inscrit
\else%                    % pour rappeler de le remplir
\@revue%
\fi.
\vskip2cm
\expandafter\ifx\csname contrib@preambule\endcsname\relax\else\noindent \bgroup\contrib@preambule\egroup\fi
\csname @contributions\endcsname
\vskip1.5em plus .5em minus .5em
%
\def\abstractskip{\vskip2em plus 1em minus 1em\relax\penalty-5000} %Racourci
}
}
\def\section{%
\@startsection{section}
{1}
\z@
{.7\linespacing\@plus\linespacing}
{.5\linespacing}
{\large\bfseries}%
}

%%% À FAIRE Voir si on doit changer le paramètre d'indentation des
%%% sous et sous-sous sections
%%% et décider des espacements avant et après.
%%% Je ne suis pas fan des espacements en ce moment, mais
%%% j'imagine que ce n'est qu'une question de préférence...
\def\subsection{%
\@startsection{subsection}
{2}
\z@
{6\p@\@plus2\p@}
{1em}%
{\normalfont\bfseries}%
}

\def\subsubsection{%
\@startsection{subsubsection}
{3}
\normalparindent
{6\p@\@plus2\p@}
{1em}
{\normalfont\itshape}%
}
\def\paragraph{%
\@startsection{paragraph}
{4}
\z@
\z@
{-\fontdimen2\font}
{\normalfont\global\advance\c@paragraph by 1}%
}

\def\subparagraph{%
\@startsection{subparagraph}
{5}
\z@
\z@
{-\fontdimen2\font}
\normalfont%
}

\def\appendix{\par
\c@chapter\z@ \c@section\z@
\polyglotlet\dmschaptername\appendixname
\polyglotlet\chaptername\appendixname
\def\thechapter{\@Alph\c@chapter}
\AppendixPageNumber}
\def\AppendixPageNumber{
\def\chapter{%
\finarticle
\cleardoublepage
\thispagestyle{empty}
\def\thepage{\arabic{page}}
\global\@topnum\z@
\@afterindentfalse \secdef\@chapter\@schapter%
}%
}

\def\NoChapterPageNumber{
\def\chapter{%
\finarticle
\cleardoublepage
\thispagestyle{empty}\pagestyle{headings}%
\global\@topnum=\z@%
\@afterindentfalse%
\secdef\@chapter\@schapter%
}%
}

\def\chapter{%
\finarticle
\cleardoublepage%
\thispagestyle{firstheadings}\pagestyle{headings}%
\global\@topnum=\z@%
\@afterindentfalse%
\secdef\@chapter\@schapter%
}

\def\@chapter[#1]#2{\refstepcounter{chapter}%
\def\next{#1}
\ifnum\c@secnumdepth<\z@ \let\@secnumber\@empty
\else \let\@secnumber\thechapter \fi
\typeout{\chaptername\space\@secnumber}%
\def\@toclevel{0}%
\ifx\dmschaptername\appendixname
\@tocwriteb\tocappendix{chapter}{#2}%
\else
\ifx\next\empty
\@tocwriteb\tocchapter{chapter}{#2}
\else
\@tocwriteb\tocchapter{chapter}{#1}
\fi
\fi
\chaptermark{#1}%
\addtocontents{lof}{\protect\addvspace{10\p@}}%
\addtocontents{lot}{\protect\addvspace{10\p@}}%
\@makechapterhead{#2}\@afterheading}
\def\@schapter#1{\typeout{#1}%
\let\@secnumber\@empty
\def\@toclevel{0}%
\ifx\dmschaptername\appendixname \@tocwriteb\tocappendix{chapter}{#1}%
\else \@tocwriteb\tocchapter{chapter}{#1}\fi
\chaptermark{#1}%
\addtocontents{lof}{\protect\addvspace{10\p@}}%
\addtocontents{lot}{\protect\addvspace{10\p@}}%
\@makeschapterhead{#1}\@afterheading}
\def\@makechapterhead#1{\global \topskip 5pc\relax
\begingroup
\normalfont\Large\bfseries \centering
\ifnum\c@secnumdepth>\m@ne
\leavevmode \hskip-\leftskip
\rlap{\vbox to\z@{\vss
\leftline{\bfseries\LARGE\selectfont\@xp{\chaptername}\enspace\thechapter}
\vskip 1pt}}%  %fin du \rlap et \vbox
\hskip\leftskip
\fi                    %fin du \ifnum
\vskip 0pt%
\vbox{\hrule width \hsize height .1mm}%
\vskip 9pt
#1 \par %\uppercase{#1} \par  %Laisser ou non \uppercase? Choix à faire
\vskip 42pt \par
\endgroup
\skip@=36\p@%
\advance\skip@ by -\normalbaselineskip
\vskip\skip@%
}

\def\@makeschapterhead#1{%
\global\topskip 5pc\relax
\begingroup
\Large\bfseries
\leftline{#1} %\uppercase{#1}}%  % Laisser ou non \uppercase? Choix à faire
\vskip0pt
\vbox{\hrule width \hsize}
\vskip42pt
\par
\endgroup
\skip@=36pt
\advance\skip@-\normalbaselineskip
\vskip\skip@
}
\setcounter{tocdepth}{3}

\AtBeginDocument{
\ifx\ifFBunicode\undefined \else
\def\frenchtablename{\tablename}
\def\englishtablename{\tablename}
\def\francais{
\@francais
\addtocontents{toc}{\protect\@francais}
\selectlanguage{french}%
}
\fi
\ifx\captionsenglish\undefined \else
\def\frenchtablename{\tablename}
\def\englishtablename{\tablename}
\def\anglais{
\@anglais
\addtocontents{toc}{\protect\@anglais}
\selectlanguage{english}%
}
\fi
\ifx\dmschaptername\dmschapternameen % \anglais est utilisé
\@anglais
\else
\@francais%
\fi
\edef\chaptername{\ifx\dmschaptername\undefined Chapitre\else\dmschaptername\fi}
\edef\contentsname{\ifx\dmscontentsname\undefined Table des mati\noexpand\`ere\else\dmscontentsname\fi}
\edef\listfigurename{\ifx\dmslistfigurename\undefined Table des figures\else\dmslistfigurename\fi}
\edef\listtablename{\ifx\dmslisttablename\undefined Table des tableaux\else\dmslisttablename\fi}
\ifentetedynamique
\entetedynamique
\fi
}

\def\@dottedtocline#1#2#3#4#5#6#7{\relax
\ifnum #1>\c@tocdepth % then omit
\else
\par \addpenalty\@secpenalty\addvspace{#2}%
\begingroup \hyphenpenalty\@M
\@ifempty{#4}{%
\@tempdima\csname r@tocindent\number#1\endcsname\relax
}{%
\@tempdima#4\relax
}%
\parindent\z@ \leftskip#3\relax \advance\leftskip\@tempdima\relax
\rightskip\@pnumwidth plus1em \parfillskip-\@pnumwidth
#5\leavevmode\hskip-\@tempdima #6\relax
\dotfill\hbox to\@pnumwidth{\@tocpagenum{#7}}\par
\nobreak
\endgroup
\fi}

\def\@tocwriteb#1#2#3{%
\begingroup
\def\@dottedtocline##1##2##3##4##5##6{%
\ifnum##1>\c@tocdepth
\else \sbox\z@{##5\let\indentlabel\@tochangmeasure##6}\fi}%
\csname l@#2\endcsname{#1{\csname#2name\endcsname}{\@secnumber}{}}%
\endgroup
\addcontentsline{toc}{#2}%
{\protect#1{\csname#2name\endcsname}{\@secnumber}{#3}}}
\def\l@section{\@dottedtocline{1}{\medskipamount}{1.5em}{}{}}
\renewcommand{\tocsection}[3]{%
\indentlabel{\@ifnotempty{#2}{\ignorespaces#1 #2.\quad}}#3}
\def\l@subsection{\@dottedtocline{2}{0pt}{2pc}{5pc}{}}
\let\tocsubsection\tocsection
\def\l@subsubsection{\@dottedtocline{3}{0pt}{3pc}{7pc}{}}
\let\tocsubsubsection\tocsection
\def\l@chapter{\@dottedtocline{0}{8pt plus1pt}{0pt}{}{\bfseries}}
\let\tocchapter\tocsection
\let\tocappendix\tocsection
\def\l@part{\@dottedtocline{0}{8pt plus1pt}{0pt}{}{\bfseries}}
\let\tocpart\tocsection
\def\l@paragraph{\@dottedtocline{4}{8pt plus1pt}{0pt}{}{\bfseries}}
\let\tocparagraph\tocsection
\def\l@subparagraph{\@dottedtocline{5}{8pt plus1pt}{0pt}{}{\bfseries}}
\let\tocsubparagraph\tocsection
\def\l@figure{\@dottedtocline{0}{3pt plus2pt}{0pt}{2.8em}{}}
\let\l@table=\l@figure
%%
%%

\expandafter\def\expandafter\include\expandafter#\expandafter1\expandafter{\expandafter\edef\expandafter\dms@inputfilename%
\expandafter{\expandafter#\expandafter1\expandafter}%
\include{#1}}

%%
%%

% La biblio est dans une section
\def\sectionbibliography#1{
\begingroup
\let\chapter=\section %Pour que ça marche avec natbib aussi
\def\@bibtitlestyle{\section*{\protect\refname}}
\edef\jobname{\dms@inputfilename}
\bibliography{../biblio/kotlingrad}
\endgroup%
}

% La biblio est dans un chapitre
\def\chapterbibliography#1{
\begingroup
\let\section=\chapter  %Pour que ça marche avec natbib aussi
\def\@bibtitlestyle{\chapter*{\protect\refname}}
\edef\jobname{\dms@inputfilename}
\bibliography{#1}
\endgroup%
}

\@addtoreset{footnote}{chapter}
\setcounter{topnumber}{4}
\setcounter{bottomnumber}{4}
\setcounter{totalnumber}{4}
\setcounter{dbltopnumber}{4}
\renewcommand{\topfraction}{.97}
\renewcommand{\bottomfraction}{.97}
\renewcommand{\textfraction}{.03}
\renewcommand{\floatpagefraction}{.9}
\renewcommand{\dbltopfraction}{.97}
\renewcommand{\dblfloatpagefraction}{.9}
\setlength{\floatsep}{12pt plus 6pt minus 4pt}
\setlength{\textfloatsep}{15pt plus 8pt minus 5pt}
\setlength{\intextsep}{12pt plus 6pt minus 4pt}
\setlength{\dblfloatsep}{12pt plus 6pt minus 4pt}
\setlength{\dbltextfloatsep}{15pt plus 8pt minus 5pt}
\setlength{\@fptop}{0pt}% removed ``plus 1fil''
\setlength{\@fpsep}{8pt}% removed ``plus 2fil''
\setlength{\@fpbot}{0pt plus 1fil}
\setlength{\@dblfptop}{0pt}% removed ``plus 1fil''
\setlength{\@dblfpsep}{8pt}% removed ``plus 2fil''
\setlength{\@dblfpbot}{0pt plus 1fil}
\def\theindex{\@restonecoltrue\if@twocolumn\@restonecolfalse\fi
\columnseprule\z@ \columnsep 35\p@
\@xp\chapter\@xp*\@xp{\indexname}
\thispagestyle{plain}%
\let\item\@idxitem
\parindent\z@  \parskip\z@\@plus.3\p@\relax
\footnotesize}
\def\@starttoc#1#2{%
\begingroup
\let\secdef\@gobbletwo \chapter
\let\@secnumber\@empty % pour \@tocwrite et \chaptermark
\ifx\contentsname#2 \else%
\@tocwrite{chapter}{#2}
\fi
\typeout{#2}%
\@xp\chaptermark\@xp{#2}%
\@xp\@makeschapterhead\@xp{#2}%
\@afterheading
\makeatletter
\@input{\jobname.#1}%
\if@filesw
\@xp\newwrite\csname tf@#1\endcsname
\immediate\@xp\openout\csname tf@#1\endcsname \jobname.#1\relax
\fi
\global\@nobreakfalse \endgroup
\newpage
}
\ifphd@
\newcounter{figure}
\def\thefigure{\arabic{figure}}
\AtBeginDocument{\ifarticleenchapitre\else\def\thefigure{\arabic{figure}}\fi}
\else
\newcounter{figure}[chapter]
\fi
\def\captionheadfont@{\scshape}
\def\captionfont{\normalfont}
\def\captionseparator{}
\def\captionfont@{\normalfont}
\def\ftype@figure{1}
\def\ext@figure{lof}
\def\fnum@figure{\figurename~\thefigure}
\def\figurename{Fig.}
\def\figure{\@float{figure}}
\def\endfigure{\end@float}
\ifphd@
\newcounter{table}
\AtBeginDocument{\ifarticleenchapitre\else\def\thetable{\arabic{table}}\fi}
\def\fnum@table{\tablename\ \thetable}
\else
\newcounter{table}[chapter]
\fi
\def\ftype@table{2}
\def\ext@table{lot}
\def\thetable{\arabic{table}}
\def\tablename{Tab.}
\def\table{\@float{table}}
\def\endtable{\end@float}
\renewenvironment{proof}[1][\proofname]{\par \normalfont \noindent
\topsep6\p@\@plus6\p@ \trivlist \itemindent0pt
\item[\hskip\labelsep\scshape
#1\@addpunct{.}]\ignorespaces
}{%
\qed\endtrivlist
}
\newenvironment{demo}[1][\proofname]{\par \normalfont \noindent
\topsep6\p@\@plus6\p@ \trivlist \itemindent0pt
\item[\hskip\labelsep\bfseries
#1\@addpunct{.}]\ignorespaces
}{%
\qed\endtrivlist
}
\ifnobabel@
\def\proofname{Proof}
\def\parttocentry{\partname\ \thepart.}
\fi
\def\th@plain{%
\let\thm@indent\noindent % no indent
\thm@headfont{\bfseries}% heading font is bold
%%  \thm@notefont{}% same as heading font
\thm@headpunct{.}% add period after heading
%%  \let\thm@swap\@gobble
\thm@preskip=.5\lineskip
\thm@postskip=\thm@preskip
\itshape % body font
}
\def\th@definition{%
\let\thm@indent\noindent % no indent
\thm@headfont{\bfseries}% heading font is bold
\thm@preskip.1\lineskip
\thm@postskip\thm@preskip
\normalfont % body font
}
\def\th@remark{%
\thm@headfont{\itshape}%
\normalfont % body font
\thm@preskip\topsep \divide\thm@preskip\tw@
\thm@postskip\thm@preskip
}
\def\dms@erreur#1{\errhelp{Assurez de remplir tous les champs obligatoires d'une page titre,
dont \auteur, \titre, \sujet et, dans le cas d'un article, \revue. Si vous désirez en laisser un
vide, vous pouvez faire p.ex. \revue{\relax}.}
\errmessage{DMS : Vous devez remplir le champ \string#1 avant d'appeler \string\maketitle}}


\endinput
%%
%% End of file `dms.cls'.