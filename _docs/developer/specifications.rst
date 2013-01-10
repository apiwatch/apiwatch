
.. image:: /_static/apiwatch-logo-small.png

==============
Spécifications
==============

APIWatch est une application dont le but est de surveiller les évolutions des interfaces 
d'un composant logiciel. Elle doit permettre une détection automatisée des modifications 
d'API. Ceci afin de faciliter les prises de décision que ces dernières impliquent : 

*  Ajustement du numéro de version
*  Modification du changelog
*  Annulation des modifications
*  etc.

Analyse
-------

APIWatch devra pouvoir extraire les informations essentielles d'API depuis un code source donné.

*  L'application devra être capable de supporter la plupart des paradigmes de programmation [#]_.
*  Les informations d'API extraites devront être modélisées de manière formelle.

   +  Ce modèle devra être indépendant du langage et du paradigme de programmation.
   +  Ce modèle unifié pourra contenir des informations provenant d'un ou plusieurs fichiers 
      de code source.
   +  Ce modèle unifié devra être sérialisable dans les formats textuels les plus communs : 
      XML, JSON, etc.

Persistance
-----------

APIWatch devra conserver une trace de ces informations d'API.

*  Les informations stockées devront être organisées et classées par composant et version 
   de composant.
*  L'utilisateur devra être en mesure de visualiser de manière informelle l'état de l'API d'une 
   version d'un composant.

Surveillance
------------

APIWatch devra être en mesure de détecter des modifications de l'API d'un composant logiciel.

*  Les modifications devront être classées en fonction de leur importance.
*  L'utilisateur devra être en mesure de spécifier en détail l'importance de chaque 
   type de modification pour un projet/composant donné.




.. rubric:: Footnotes

.. [#] Il existe quatre principaux paradigmes de programmation informatique. La programmation 
   *impérative*, *fonctionnelle*, *orientée-objet*, et *logique*. La plupart des langages 
   utilisent une combinaison de ces paradigmes. http://en.wikipedia.org/wiki/Programming_paradigm
