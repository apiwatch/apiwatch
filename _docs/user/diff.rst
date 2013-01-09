
.. image:: /_static/apiwatch-logo-small.png

=======================
Diff between 2 versions
=======================

Une fois les deux versions analysées et stockées dans la base de données, on peut évaluer les 
différences d'API et les violations qu'elles entraînent. Ceci peut être fait de deux façons 
différentes :

Via l'interface web
===================

Sur la page d'un composant, il est possible de sélectionner deux versions que l'on souhaite 
comparer. En cliquant sur le bouton View differences between selected A and B versions, 
le serveur APIWatch va effectuer une comparaison de l'API des deux versions sélectionnées. 

.. figure:: /images/webapp-diff.png

   Sélection des versions à comparer

Il va ensuite évaluer toutes les différences trouvées à l'aide de règles de stabilité d'API, 
chaque différence pouvant donc donner lieu à une ou plusieurs violations. L'Illustration suivante 
montre le résultat de la comparaison entre les versions 1.447 et 1.466 du composant jenkins-core. 

.. figure:: /images/webapp-violations.png

   Violations de stabilité d'API entre deux versions d'un même composant

Pour certaines violations, il est possible d'avoir plus de détailsen cliquant sur le bouton 
avec un symbole de loupe.

.. figure:: /images/webapp-violation-details.png

   Détail d'une violation de stabilité d'API

Par ligne de commande
=====================

Grâce à l'outil apidiff, on peut évaluer les violations à la stabilité d'une API entre deux 
versions d'un même composant logiciel.

La syntaxe de base de la commande est : apidiff <version A> <version B> Les versions peuvent 
être un chemin de fichier ou une URL du serveur. Voici un exemple de résultat obtenu si on 
compare les deux versions enregistrées précédemment :

.. code-block:: console

   user:~ $ apidiff http://localhost:8080/apiwatch/jenkins-core/1.447/ \             
                        http://localhost:8080/apiwatch/jenkins-core/1.466/
   [CRITICAL] <REM001> Removed PROTECTED Function:configure() @ 'hudson/ExtensionFinder.java:416'
   [BLOCKER] <REM001> Removed PUBLIC Variable:LOG_STARTUP_PERFORMANCE @ 'jenkins/model/Jenkins.java:3610'
   ...
   [BLOCKER] <TYP001> Changed type of PUBLIC variable 'CONFIG_DELEGATE_TO' (Class -> Class<Plugin>) @ 'hudson/os/windows/ManagedWindowsServiceConnector.java:42'

On peut modifier le format de sortie des violations de stabilité d'API détectées avec l'option 
-f/--format. 

Ici, APIWatch a évalué les violations à l'aide de la configuration par défaut des règles de 
stabilité d'API (cf. Annexe A). On peut changer cette configuration pour l'adapter à un projet 
en utilisant l'option -r/--rules-config avec un fichier personnalisé. La version par défaut 
d'un tel fichier se trouve dans le répertoire conf de l'installation d'APIWatch.

