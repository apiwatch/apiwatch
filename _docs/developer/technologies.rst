
.. image:: /_static/apiwatch-logo-small.png

================
Choix Techniques
================

Langage
-------

APIWatch sera développé en langage Java pour bénéficier des avantages suivants :

*  Langage mature créé en 1996 par Sun Microsystems. 
*  Contrôle fort du typage à la compilation (prévention d'erreurs).
*  Langage orienté-objet, adapté pour implémenter le modèle de données décrit au paragraphe 3.2.
*  Exécution dans une machine virtuelle (sécurité de fonctionnement).
*  La machine virtuelle Java présente des performances acceptables1 pour la plupart des usages 
   relatifs à APIWatch (lecture de fichiers, manipulation de chaînes de caractères, recherche 
   dans des tables de hachage, etc.).
*  Support de multiples plate-formes sans compilation spécifique (Linux et autres \*NIX, Windows, 
   etc.).
*  Large panel de bibliothèques de fonctions réutilisables.
*  Outillage étendu (environnements de développement, outils de compilation et assemblage, 
   automatisation de la gestion des dépendances externes et des tests unitaires, etc.).
*  Communauté open-source active.

Intégration
-----------

Maven__ a été retenu pour gérer l'intégration de APIWatch. Maven est un outil de production 
de projets logiciels Java comparable au système Make sous UNIX. Maven utilise un paradigme 
connu sous le nom de Project Object Model (POM) afin de décrire un projet logiciel, ses 
dépendances avec des modules externes et l'ordre à suivre pour sa production. Il est livré 
avec un grand nombre de tâches pré-définies, comme la compilation de code Java ou encore 
sa modularisation.

__ http://maven.apache.org/

Analyse de code source
----------------------

Pour l'analyse de code source, APIWatch s'appuiera sur ANTLR__. ANTLR est un successeur des 
outils Lex et YACC écrit en Java. Il est distribué sous la licence BSD. Il se base sur 
l'écriture de grammaires décrivant un langage de programmation. Il utilise ensuite ces 
grammaires pour générer le code source nécessaire à l'analyse de ce langage. Plus de 
détails seront donnés sur le mécanisme d'analyse de code source – avec ou sans ANTLR – 
dans le paragraphe 3.3.3.

__ http://www.antlr.org/

Web
---

L'interface web de APIWatch se basera sur le standard ``javax.servlet``. Ceci permettra un 
déploiement aisé dans tous les environnements qui supportent ce standard (`Apache Tomcat`__, 
`Jetty Server`__, JBoss__, `IBM Websphere`__, etc.).

Afin de respecter une architecture du type « Modèle-Vue-Contrôleur », la génération des pages 
HTML se fera grâce au moteur de templates `Apache Velocity`__.

__ http://tomcat.apache.org/
__ http://jetty.codehaus.org/jetty/
__ http://www.jboss.org/
__ http://www-01.ibm.com/software/websphere/
__ http://velocity.apache.org/

Interface avec les bases de données relationnelles
--------------------------------------------------

Il a été jugé préférable de découpler au maximum la communication entre APIWatch et le 
système de gestion de bases de données pour permettre notamment de changer de mécanisme 
de stockage si nécessaire. Aussi, cela nous affranchit d'écrire les requêtes dans le 
langage propriétaire dudit système. On a donc choisi d'utiliser un ORM qui permet d'avoir 
une couche d'abstraction entre le langage de programmation et la base de données sous-jacente. 
Pour rester cohérent avec les considérations mentionnées au début du chapitre 3.3, un ORM 
le plus simple et léger possible a été choisi : ORMLite__. Il est écrit en Java et supporte 
le standard JDBC ce qui le rend compatible avec la plupart des moteurs de base de données.

__ http://ormlite.com/
