
.. image:: /_static/apiwatch-logo-small.png

===============================
Analyse formelle de code source
===============================

Il a été privilégié de laisser une grande liberté quant aux détails d'implémentation des 
analyseurs spécialisés pour chaque langage de programmation. Dans le paragraphe suivant, 
on donnera cependant un aperçu d'une manière courante d'analyser du code source et d'en 
extraire les données d'API.

Pour analyser un langage formel, on procède en général en plusieurs étapes qui transforment 
une donnée en une représentation intermédiaire de celle-ci (en anglais, Intermediate 
Representation ou IR). On considérera que le code source est représenté par un flux de 
caractères (la plupart du temps provenant de la lecture du contenu d'un fichier).

Analyse lexicale
================

Cette étape consiste à découper le flux de caractères en unités lexicales ou lexèmes 
(en anglais, tokens) sans se préoccuper de leur signification. En anglais un analyseur 
lexical est appelé un lexer. 

Certains tokens sont constants, comme les opérateurs ou les mots-clés du langage, d'autres sont 
variables, comme les nombres ou les chaînes de caractères. On utilise souvent des expressions 
régulières pour décrire les tokens valides d'un langage. 

Prenons comme exemple un langage mathématique simplifié dans lequel on peut écrire des 
multiplications de nombres entiers. Voici les définitions de tokens nécessaires à l'analyse 
lexicale d'un tel langage :

.. code-block:: antlr

   /**
    * Opérateur de multiplication.
    * 
    * Ce token est constant.
    */
   STAR : '*'  ;
   
   /**
    * Nombre entier.
    * 
    * Ce token est variable, il peut être composé de un ou 
    * plusieurs chiffres
    */
   NUMBER : ('0'..'9')+ ;
   
   /**
    * Espaces, tabulations et retours à la ligne.
    * 
    * Ces tokens sont ignorés par le lexer car ils n'ont pas 
    * de signification particulière.
    */
   WS : (' '|'\t'|'\r'|'\n')+ {$channel = HIDDEN;}  ;

Dans l'Illustration 16 on peut voir le procédé de découpage en tokens d'une expression 
multiplicative simple. Les tokens de type WS sont placés dans un flux séparé afin qu'ils 
soient ignorés par l'analyseur syntaxique.

.. figure:: /diagrams/lexing.svg

   Découpage d'un flux de caractère en unités lexicales
   
Analyse syntaxique
==================

En anglais un analyseur syntaxique est appelé un parser. L'étape de parsing consiste à 
reconnaître des « phrases » valides dans le flux de tokens produit par le lexer. Pour cela, 
on se base généralement sur une grammaire non-contextuelle [#]_. Celle-ci définit récursivement 
l'ensemble des tokens valides du langage et l'ordre dans lequel ils doivent apparaître 
pour former des phrases correctes. Au fur et à mesure de la reconnaissance de structures 
valides du langage, le parser produit une représentation arborescente du code source analysé.

Reprenons le langage mathématique simplifié abordé au paragraphe précédent. Dans celui-ci, la 
règle de grammaire décrivant une expression multiplicative s'écrit de la manière suivante [#]_ :

.. code-block:: antlr
   
   /**
    * Définition récursive d'une expression multiplicative.
    */
   multiplicative_expression 
     : NUMBER (STAR multiplicative_expression)* 
     ; 

Si on évalue le résultat produit par l'exemple précédent en organisant les tokens de manière 
arborescente, voici un résultat possible :

.. figure:: /diagrams/parsing.svg

   Résultat du parsing d'un flux de tokens
   
Dans l'Illustration 17, on constate que les tokens de type STAR ont été placés comme nœuds 
de l'arbre et ceux de type NUMBER comme feuilles. Cela permet de représenter l'expression 
sous une forme voisine de la notation polonaise [#]_.

Le résultat d'une analyse syntaxique produit toujours un « arbre de syntaxe abstraite » 
(appelé généralement par son nom anglais : Abstract Syntax Tree ou AST). Cet arbre porte 
la représentation en deux dimensions du code analysé (là où les flux de caractères et de 
tokens sont des représentations à une seule dimension).

Analyse sémantique
==================

Une fois l'AST construit par le parser, il faut maintenant lui donner du « sens ». C'est 
l'étape la plus délicate d'un processus d'analyse de langages. Ici on ne produit plus de 
représentation intermédiaire mais bien une conversion finale vers le format désiré. Ce format 
désiré peut avoir plusieurs formes :

*  Un résultat dans le cas de l'évaluation d'une expression mathématique.
*  Une traduction vers un autre langage (par exemple, C vers Java).
*  Des instructions en code machine dans le cas d'un compilateur.

Ces formes sont rarement obtenues directement depuis l'AST tel que produit par le parser. 
Il est alors nécessaire de réécrire des parties de l'AST1 afin d'arriver à une forme assez 
triviale qui pourra être directement traduite dans le format désiré. Parfois, certaines 
parties du code analysé seront volontairement exclues, puisque non exploitées par les 
analyseurs sémantiques pour des raisons de performance. Par exemple, dans les analyseurs 
de APIWatch, on aura tendance à exclure les sous-arbres des corps de fonctions car ils ne 
portent que rarement – voire jamais – des informations d'API.

Comme l'AST est une structure arborescente, on utilisera généralement un algorithme du type 
« visiteur » pour le parcourir. Cette méthode est appelée tree walking en anglais. Elle 
consiste à analyser chaque nœud de l'arbre récursivement. Il y a deux façons de « visiter » 
un arbre : avec un parcours en profondeur ou un parcours en largeur. Selon les besoins, 
l'une ou l'autre de ces méthodes sera utilisée.

Pour en finir avec l'exemple précédent, partons du principe que le parser produit un AST utilisant 
des objets de ce type (code d'une classe Java) :

.. code-block:: java

   class MultiExprAST {
       public String token;
       public MultiExprAST left;
       public MultiExprAST right;
   /* classe volontairement incomplète pour des raisons de lisibilité */
   }

Voici le code nécessaire pour évaluer le résultat de l'expression mathématique analysée (également en Java) :

.. code-block:: java

   int evaluate(MultiExprAST ast) {
       if (ast.token.equals("*"))
           return evaluate(ast.left) * evaluate(ast.right);
       else
           return Integer.valueOf(ast.token);
   }
   
On constate que la fonction evaluate est récursive et qu'elle utilise un parcours en profondeur 
pour évaluer le résultat de l'expression multiplicative portée par l'AST.

Analyse de langages avec ANTLR
==============================

La programmation d'analyseurs est une tâche extrêmement fastidieuse,  répétitive et sujette 
aux erreurs. ANTLR est un outil qui permet de définir un langage formel par une grammaire 
écrite dans une syntaxe dérivée de la Backus-Naur Form [#]_. Depuis cette grammaire, ANTLR est 
capable de générer des analyseurs lexicaux, syntaxiques et même sémantiques, et ce dans de 
nombreux langages de programmation : Java, C, C++, Python, etc. Dans son livre 
`The Definitive ANTLR Reference`__, Terrence Parr explique comment écrire et exploiter 
les grammaires.

La communauté ANTLR met à disposition des grammaires pour de nombreux langages de 
programmation. Le support des langages Java et C a été implémenté dans la distribution 
standard de APIWatch à l'aide de ces grammaires.

__ http://pragprog.com/book/tpantlr/the-definitive-antlr-reference

Exemple 1 : Java
----------------

La grammaire disponible sur le site officiel ANTLR était déjà très complète. Elle permettait 
de construire des AST depuis n'importe quel code Java (édition 5 ou 6). Il a uniquement été 
nécessaire d'écrire une fonction récursive walk qui, au fur et à mesure du parcours de l'AST, 
construit une structure de données utilisant le modèle d'interface logicielle décrit au 
paragraphe 3.2.1.

Exemple 2 : C
-------------

L'unique grammaire C disponible a été écrite par l'auteur de ANTLR lui-même : Terrence Parr. 
Malheureusement, elle prenait en charge uniquement le C ANSI 89 et elle n'était pas en mesure 
de produire des AST. Celle-ci a dû être modifiée pour qu'elle supporte les nouveaux éléments 
introduits dans les standards C ANSI de 1999 et 2011. On a également ajouté le support des 
built-ins et de l'opérateur ``__attribute__`` du compilateur GCC__.

__ http://gcc.gnu.org/

Les programmes écrits en langage C utilisent généralement des instructions destinées à un 
préprocesseur de texte. Ces instructions permettent l'inclusion de segments de code contenu 
dans d'autres fichiers (avec l'instruction ``#include``), la substitution de chaînes de caractères 
(définies avec ``#define``) ou encore de la compilation conditionnelle (avec ``#ifdef`` et 
``#ifndef``). Or, la syntaxe de ces instructions n'a aucun rapport avec celle du langage C. 
Il est donc obligatoire que chaque fichier que l'on souhaite analyser avec APIWatch soit traité 
auparavant par le préprocesseur. 

Cela entraîne un problème majeur : après le passage du préprocesseur, le code écrit par le 
développeur va être noyé au milieu de toutes les inclusions dont il a eu besoin. Une majorité 
de ces inclusions provenant des bibliothèques du système, elles apportent des définitions 
qui n'ont rien à voir avec le code original. Si on les analyse, elles vont produire du 
« bruit » et l'API donnée en résultat sera fausse. Pour clarifier les choses, voici un 
exemple. Prenons le code C suivant contenu dans un fichier ``hello.c`` :

.. code-block:: c

   #include <stdio.h>
   int say_hello(char *name, FILE *stream) {
      return fprintf(file, "Hello %s!\n", name);
   }

On constate que le code définit une unique fonction ``say_hello`` et tient dans 4 lignes. Si on 
fait passer ce code par le préprocesseur de GCC avec la commande suivante :

.. code-block:: console

   gcc -E hello.c > hello.i

On obtient un fichier ``hello.i`` de 851 lignes qui ressemble à ceci (la quasi-totalité du 
fichier a été masquée pour des raisons de lisibilité) :

.. code-block:: c

   [...]
   # 45 "/usr/include/stdio.h" 3 4
   struct _IO_FILE;
   [...]
   # 65 "/usr/include/stdio.h" 3 4
   typedef struct _IO_FILE FILE;
   [...]
   # 322 "/usr/include/stdio.h" 3 4
   extern int fprintf (FILE *__restrict __stream,
                       __const char *__restrict __format, ...);
   [...]
   # 2 "hello.c" 2
   int say_hello(char *name, FILE *stream) {
      return fprintf(file, "Hello %s!\n", name);
   }

Seules les 3 dernières lignes définissent l'API du code de l'utilisateur. Tout le reste fait 
partie du « bruit » évoqué plus haut. Néanmoins, afin que l'analyseur reconnaisse la fonction 
``fprintf`` et le type ``FILE`` utilisés dans la fonction ``say_hello``, il doit avoir 
rencontré leur définition avant – ceux-ci ne faisant pas partie des mots-clés du langage C. 
Tout ce « bruit » est donc nécessaire mais il faut trouver un moyen de l'exclure des données d'API. 

Il se trouve que le préprocesseur insère des directives spécifiques qui permettent au 
compilateur de savoir d'où provient chaque ligne de code (nom de fichier et numéro de 
ligne) [#]_. Notamment pour lui permettre de localiser précisément les erreurs de syntaxe 
s'il en trouve. Ces directives respectent une syntaxe bien définie :

.. code-block:: c

   # <n° de ligne> "chemin du fichier source" <indicateurs divers>

Il est important de noter que l'ensemble des fichiers du système et/ou des bibliothèques 
susceptibles d'être inclus par le code de l'utilisateur sont localisés à des endroits 
précis et généralement connus.

On procédera donc de la manière suivante :

*  On traite le fichier source original avec le préprocesseur et on stocke le résultat dans 
   un fichier temporaire qui sera analysé.
*  On répertorie l'ensemble des chemins (dossiers) qui contiennent des en-têtes du système 
   et/ou provenant de bibliothèque externes. Exemple :
   
      ``/usr/include``
      ``/usr/lib64/gcc/x86_64-pc-linux-gnu/4.5.3/include``

*  Durant l'analyse lexicale, le lexer va maintenir une table contenant les informations de 
   provenance des inclusions. Il va mémoriser la ligne à laquelle l'inclusion a été rencontrée, 
   le fichier et la ligne de ce fichier depuis lequel elle a été faite. Comme il connaît 
   l'ensemble des chemins qui ne sont pas en rapport avec le code utilisateur, le lexer peut 
   identifier si ce qui suit l'inclusion fait partie ou non d'un fichier « système ».

   +-----------------+----------------------------+------------------+-------------+    
   | Ligne dans le   | Chemin du fichier source   |  Ligne dans le   | Fichier     | 
   | fichier analysé |                            |  fichier source  | « système » |
   +=================+============================+==================+=============+
   |                                       ...                                     |
   +-----------------+----------------------------+------------------+-------------+
   | 133             | ``/usr/include/stdio.h``   | 45               | ``true``    |
   +-----------------+----------------------------+------------------+-------------+
   |                                       ...                                     |
   +-----------------+----------------------------+------------------+-------------+
   | 484             | ``/usr/include/stdio.h``   | 322              | ``true``    |
   +-----------------+----------------------------+------------------+-------------+
   |                                       ...                                     |
   +-----------------+----------------------------+------------------+-------------+
   | 847             | ``hello.c``                | 2                | ``false``   |
   +-----------------+----------------------------+------------------+-------------+

*  Il est important de noter que chaque token produit par le lexer contient la ligne à laquelle 
   il a été détecté. Durant l'analyse syntaxique, avant d'insérer un token dans l'AST, le parser 
   va inspecter la table produite par le lexer (en plus du flux de tokens) et va donc pouvoir 
   décider si le token fait partie du code écrit par l'utilisateur en comparant les numéros 
   de ligne.
   
Grâce à cette méthode, l'AST produit par le parser ne contiendra que les symboles définis 
par l'utilisateur. On procédera alors de la même façon que pour le langage Java pour 
générer une structure de données à base des modèles définis au paragraphe 3.2.1.

.. rubric:: Footnotes

.. [#] Appelée également grammaire algébrique, elle permet de décrire de manière formelle la 
   syntaxe d'un langage.
.. [#] Les identifiants en majuscules sont des références aux tokens produits par le lexer.
.. [#] La notation polonaise représente les formules algébriques sans utiliser de parenthèses en 
   préfixant ou suffixant les opérandes par l'opérateur.
.. [#] La forme de Backus-Naur (souvent abrégée en BNF, de l'anglais Backus-Naur Form) est une 
   notation permettant de décrire les règles syntaxiques des langages de programmation.
.. [#] http://gcc.gnu.org/onlinedocs/cpp/Preprocessor-Output.html
   
   
   