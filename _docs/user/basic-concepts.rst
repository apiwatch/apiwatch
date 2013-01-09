
.. image:: /_static/apiwatch-logo-small.png

==============
Basic Concepts
==============

Architecture logicielle à base de composants
============================================

L'ingénierie logicielle à base de composants met l'accent sur la séparation des rôles dans 
un système logiciel donné. C'est une approche fondée sur la réutilisation de modules indépendants 
à couplage lâche : les composants.

Un composant logiciel est une unité logique destinée à être réutilisée en tant que 
« pièce détachée » dans des applications. Il fournit le plus souvent un ensemble de services que 
l'on peut invoquer par le biais d'interfaces définies. Un des bénéfices fondamentaux de cette 
approche modulaire est que l'on peut substituer un composant par un autre du moment qu'il 
respecte les mêmes interfaces.

Dans l'Illustration 1, on peut voir un exemple d'application de gestion de commandes pour un 
site de vente en ligne. Une telle application doit rendre de nombreux services : stockage de 
l'historique des commandes, gestion de la comptabilité, envoi d'e-mails aux clients, etc. 
Il serait difficile de la concevoir de manière monolithique. Pour simplifier son développement 
et ses évolutions, on préférera une approche modulaire.

.. figure:: /diagrams/components-architecture.svg

   Exemple d'architecture à composants, application de gestion de commandes

Les différentes fonctionnalités de l'application sont gérées par des composants spécialisés 
comme montré dans la table suivante. 

======================= ===================================================================
Composant               Rôle/Service
======================= ===================================================================
``OrderManagement```    Création, édition et annulation de commandes client.
``Accounting``          Gestion comptable, édition de factures, etc.
``PersistanceLayer``    Stockage des données de l'application.
``Notifications``       Notifications au client et aux équipes techniques.
``UserInterface``       Interface avec l'utilisateur.
======================= ===================================================================

Comme dit plus haut, on peut substituer un composant par un autre respectant les mêmes interfaces. 
On pourra par exemple changer le composant ``PersistanceLayer`` pour qu'il stocke les données dans 
une base relationnelle ou directement dans un système de fichiers. Et ce, sans modifier le 
comportement global de l'application. L'autre avantage de cette approche est que – une fois les 
rôles correctement définis – chaque composant peut être développé par des équipes différentes.

Interface de programmation
==========================

Pour exploiter les services proposés par un composant, ses congénères doivent 
« entrer en communication » avec celui-ci. Il leur est donc nécessaire de définir au préalable 
un « protocole » et de le respecter. En ingénierie logicielle, ce « protocole » est appelé : 
*Application Programming Interface* ou *API*.

Le terme « API » peut représenter deux choses :

*  Par sa forme « instanciée » : une API est le « contrat » exposé par un composant logiciel 
   dans lequel est stipulé la façon d'invoquer les services de celui-ci. 
*  Par sa forme « conceptuelle » : des « informations d'API » portent des données relatives 
   à une API.
   
Une API inclut généralement des spécifications de routines ou de fonctions, de structures de 
données, de classes d'objets ou de variables que les composants doivent respecter, utiliser 
et/ou implémenter. Les formats des fichiers écrits et/ou lus, les protocoles de communication 
réseau, plus généralement tout échange de données entre composants à travers un support font 
également partie de l'API.

Reprenons l'exemple précédent ; le composant de gestion des commandes propose trois services 
comme indiqué dans la table suivante.

================= ============================
Service           Description
================= ============================
``createOrder``   Création d'une commande.
``modifyOrder``   Édition d'une commande.
``cancelOrder``   Annulation d'une commande.
================= ============================

Pour fonctionner, le composant ``OrderManagement`` exploite des services proposés par d'autres 
composants comme décrit dans la table suivante.

================= ======================= ============================
Service           Proposé par             Description
================= ======================= ============================
``getBilling``    ``Accounting``          Création d'une commande.
``persistOrder``  ``PersistanceLayer``    Édition d'une commande.
================= ======================= ============================

Sur la figure suivante, on peut voir un schéma au format UML représentant l'API du composant 
``OrderManagement``.

.. figure:: /diagrams/component-api.svg

   API d'un composant logiciel : ``OrderManagement``


Dépendances
===========

Dès qu'un composant A exploite un service fourni par un composant B, on dit que A possède une 
dépendance directe vers B. Quand un composant dépend d'un autre, il hérite des dépendances de 
celui-ci. On parle alors de dépendances transitives.

Sur l'illustration suivante, on peut voir les dépendances entre les composants ``OrderManagement``, 
``UserInterface`` et ``PersistanceLayer`` vus dans l'exemple précédent.

.. figure:: /diagrams/components-dependencies.svg

   Dépendances entre composants
   
   