
.. image:: /_static/apiwatch-logo-small.png

====================
Context & Key issues
====================

Pratiques et contraintes du développement logiciel
==================================================

Practices and constraints in software development
=================================================

Software engineering respects the same staged design and delivery logic as most other engineering domains. Those stages are described in next figure.

Le génie logiciel respecte la même logique de conception et de livraison par étapes que la 
plupart des autres domaines d'ingénierie. Ces étapes sont décrites dans l'illustration suivante.

.. figure:: /diagrams/development-process.svg

   Étapes de la construction d'un logiciel
   
Dans un monde idéal, le logiciel livré au client ne contiendrait aucun bug et répondrait 
parfaitement au besoin exprimé. Il n'y aurait pas besoin de changer un comportement ou de 
corriger un problème : il n'y aurait pas de retour en arrière dans la succession des étapes 
décrites dans l'illustration précédente, et il n'y aurait qu'une seule livraison dudit logiciel.

Néanmoins, le produit livré n'est jamais exempt de défauts ; les besoins du client et les 
contraintes d'exploitations peuvent également amener à devoir modifier le logiciel. Il est 
donc inévitable de repasser par les étapes décrites ci-dessus (voir illustration suivate), 
et donc, de livrer plusieurs fois un même logiciel.

.. figure:: /diagrams/development-cycle.svg

   Cycles dans les étapes de la construction d'un logiciel
   
Dans tous les autres domaines d'ingénierie (mécanique, bâtiment, etc.), la livraison d'une 
révision du produit remplace généralement l'ancienne. Les anciennes révisions d'un logiciel, 
elles, ne sont pas rendues caduques par la livraison de nouvelles.

Comme il a été dit au chapitre 1.2, les composants communiquent entre eux. Le bon déroulement 
de ces communications repose sur une entente mutuelle au sujet d'une même API. Or, les composants 
évoluent, chaque évolution pouvant changer leur comportement mais surtout leur API. Au chapitre 
1.3, on a également vu que certains composants dépendent des services fournis par d'autres. 
On doit pouvoir exprimer cette dépendance en prenant en compte l'évolutivité des API. 
Malheureusement, les données d'API sont beaucoup trop complexes pour être exploitables pour 
spécifier une dépendance. Il faut trouver une notion plus facile à manipuler.

On commencera généralement par identifier clairement et de manière unique chaque livraison 
d'un même composant. Cet identifiant sera appelé « version », l'évolution de cette version 
permettant de rendre compte de l'évolution de l'API.

Il existe de nombreuses manières [#]_ de noter la version des logiciels. Une des plus courantes 
consiste à utiliser une suite de nombres ordonnés du plus significatif au moins significatif. 
À chaque nouvelle version, on incrémente un ou plusieurs des nombres en fonction de l'importance 
des modifications apportées au logiciel. L'Illustration 6 montre un exemple d'une version à 
trois nombres.

.. figure:: /diagrams/versioning.svg

   Signification du numéro de version d'un logiciel

Grâce à cette notion de version, on pourra identifier les composants logiciels en prenant en 
compte la dimension temporelle et son influence sur l'état de leur API. Ceci permettra également 
d'exprimer les dépendances de manière plus précise.

Modification d'une API : risques et conséquences
================================================

Un changement d'API peut survenir à plusieurs niveaux :

*  Changement de format d'un fichier (entrée ou sortie)
*  Changement du type d'une variable
*  Modification de la spécification d'une structure de données
*  Changement du nombre ou des types de paramètres nécessaires à l'invocation d'une fonction.
*  etc.

Si lors de l'exécution, un composant fait appel à un service d'un autre sans prendre en compte 
une modification de l'API, il est très difficile de prévoir le comportement du programme. 
Celui-ci va dans la plupart des cas s'arrêter de fonctionner ou pire, continuer à fonctionner 
de manière erratique.

On a vu précédemment que la version d'un composant logiciel est supposée refléter les évolutions 
de son API. Or, la modification de la version est une opération manuelle. Il est donc de la 
responsabilité des équipes de développement de faire évoluer la version en corrélation avec 
la nature des changements apportés au composant. Comme toute opération manuelle, celle-ci est 
sujette aux erreurs et aux oublis. Il n'existe pas de moyen simple d'assurer que l'état de 
l'API d'un composant logiciel soit reflété par sa version.

Solutions existantes
====================

La plupart du temps, on a recours à des tests d'intégration pour vérifier le bon fonctionnement 
de tous les composants d'un même système avant de le livrer. 

Néanmoins, l'écriture et la maintenance de ces tests est une tâche très coûteuse en temps et 
ressources humaines, à faible valeur ajoutée et qui permet difficilement d'assurer une 
couverture totale des API. Aussi, l'exécution de ces tests doit souvent être supervisée par 
un être humain, l'interprétation des résultats pouvant être très complexe. En cas de développement 
de composants réutilisables comme des bibliothèques de fonctions ou des systèmes d'exploitation, 
il est impossible de faire des tests d'intégration car les autres modules susceptibles d'exploiter 
les API fournies ne peuvent être connus par avance.

Il existe cependant des solutions qui permettent de contrôler la stabilité et la compatibilité 
ascendante de composants logiciels sans utiliser de tests d'intégration. Ces solutions se 
présentent souvent comme des outils en ligne de commande facilement automatisables :

ABI Compliance Checker
----------------------

ABI Compliance Checker (ACC) is a tool for checking backward binary and source-level compatibility 
of a C/C++ library. The tool checks header files and shared libraries of old and new versions and 
analyzes changes in API and ABI (ABI=API+compiler ABI) that may break binary and/or source 
compatibility: changes in calling stack, v-table changes, removed symbols, renamed fields, etc. 
Binary incompatibility may result in crashing or incorrect behavior of applications built with 
an old version of a library if they run on a new one. Source incompatibility may result in 
recompilation errors with a new library version. The tool is intended for developers of software 
libraries and maintainers of operating systems who are interested in ensuring backward 
compatibility, i.e. allow old applications to run or to be recompiled with newer library 
versions.

`Official WebSite`__ 

__ http://ispras.linuxbase.org/index.php/ABI_compliance_checker


PkgDiff
-------

Package Changes Analyzer (pkgdiff) is a tool for analyzing changes in Linux software packages 
(RPM, DEB, TAR.GZ, etc). The tool is intended for Linux maintainers who are interested in 
ensuring compatibility of old and new versions of packages.

`Official WebSite`__ 

__ http://pkgdiff.github.com/pkgdiff/

Why APIWatch?
=============

Ces solutions ont été conçues pour des technologies ou langages de programmation bien spécifiques 
(C/C++, Linux, etc.). De plus, chacun d'entre eux est développé sur la base d'une technologie 
qui lui est propre (langage, framework, plate-forme, etc.). De ce fait, il est techniquement 
difficile de mutualiser les fonctionnalités de chacun. Étendre le fonctionnement de ces outils 
à d'autres langages serait très complexe car ceux-ci n'ont pas été conçus dans l'optique d'être 
évolutifs.

A ce jour, il n'existe pas de solution qui permette une gestion unifiée des contrôles de cohérence 
d'API qui soit indépendante du langage de programmation analysé. 

Pour pallier ce besoin, il faudrait disposer d'un outil facilement extensible à de nouveaux 
langages. Dans cette optique, cet outil devrait fournir une abstraction totale du langage de 
programmation pour ne conserver que les informations d'API. Cette abstraction permettrait 
notamment de mutualiser les processus de traitement des ces informations. 

Dans le chapitre suivant, on décrira la conception et la mise en œuvre d'une solution technique 
à ce problème.


.. rubric:: Footnotes

.. [#] Le procédé de « versionnage » (de l'anglais versioning) consiste à assigner un identifiant 
   à chaque état d'une pièce logicielle http://en.wikipedia.org/wiki/Software_versioning
