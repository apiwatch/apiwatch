
.. image:: /_static/apiwatch-logo-small.png

===========================
Directives de développement
===========================

D'une manière générale, APIWatch a été conçu et développé en prenant en compte les considérations 
suivantes (par ordre de priorité) :

#. **Modularité :** Le choix d'une technologie ne doit jamais être irréversible. Il faut éviter 
   au maximum de faire reposer l'application sur une bibliothèque logicielle précise et aménager 
   la possibilité de remplacer une dépendance par une autre en cas de besoin.
#. **Simplicité :** Il est primordial de ne considérer que les solutions les plus simples et 
   triviales. Steve Oualline souligne ce point dans son livre Practical C Programming [4] : 
   « You should take every opportunity to make sure that your program is clear and easy to 
   understand. Do not be clever. Clever kills. Clever makes for unreadable and 
   unmaintainable programs. » . 
#. **Faible empreinte :** On voit de plus en plus d'applications développées sur la base de 
   frameworks. Cette pratique a de nombreux avantages (gain de temps, standardisation, 
   réutilisation, etc.). Néanmoins, elle conduit souvent les développeurs à négliger 
   l'optimisation de leur code. Le framework utilisé répond généralement au besoin mais 
   propose également d'autres services parfois superflus. Cela implique une empreinte 
   de fonctionnement inutilement grande (espace disque et mémoire nécessaires). Dans le 
   contexte, d'un outil spécialisé comme celui-ci, où l'essentiel de la complexité réside 
   dans les algorithmes plutôt que dans l'environnement, le gain de temps apporté par 
   l'utilisation d'un framework complexe serait minime.

Règles de codage Java
=====================

TODO