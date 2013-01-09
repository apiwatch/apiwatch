
.. image:: /_static/apiwatch-logo-small.png

============
Installation
============

Il sera supposé que la machine virtuelle *Java* est déjà installée sur la machine cible et que 
l'exécutable :file:`java` est accessible depuis le :envvar:`PATH` système. 

Basique : outils en ligne de commande
=====================================

La distribution de l'application se présente sous la forme d'une archive au format ``.tar.gz``. 
Une fois l'archive copiée sur la machine cible, il faut la décompresser – de préférence dans 
un répertoire protégé du système afin d'éviter qu'il soit modifiable par les utilisateurs :

.. code-block:: console

   root:~ # cd /usr/lib
   root:lib # tar zxvf /tmp/apiwatch-1.0.tar.gz
   apiwatch-1.0/lib/apiwatch-core-1.0.jar
   apiwatch-1.0/lib/antlr-runtime-3.3.jar
   ...
   apiwatch-1.0/lib/apiwatch-rules-base-1.0.jar
   apiwatch-1.0/war/apiwatch-war-1.0.war
   apiwatch-1.0/conf/rules-config.ini
   ...
   apiwatch-1.0/bin/apiwatch
   apiwatch-1.0/bin/apidiff
   apiwatch-1.0/bin/apiscan

Il est recommandé d'ajouter le chemin :file:`/usr/lib/apiwatch-1.0/bin` au :envvar:`PATH` système 
pour que les exécutables :file:`apiwatch`, :file:`apidiff` et :file:`apiscan` soient accessibles 
facilement. La commande suivante permet de vérifier que les outils en ligne de commande ont été 
correctement installés :

.. code-block:: console

   root:~ # apiwatch --help
   usage: apiwatch [-h] [-e ENCODING] [-x PATTERN [PATTERN ...]]
                   [-i PATTERN [PATTERN ...]] [-u USERNAME] [-p PASSWORD]
                   [-v {TRACE,DEBUG,INFO,WARN,ERROR}] [-f {text,json}]
                   [-r RULES_CONFIG] [-s {INFO,MINOR,MAJOR,CRITICAL,BLOCKER}]
                   REFERENCE FILE_OR_DIR [FILE_OR_DIR ...]
   
   APIWATCH version 1.0
   
   ...


Optionnelle : Serveur de stockage
=================================

Pour installer l'application web dans le serveur d'applications, il faut décompresser le fichier 
/usr/lib/apiwatch-1.0/war/apiwatch-war-1.0.war dans un dossier webapps/apiwatch/ du serveur Tomcat. 
Puis relancer ce serveur pour qu'il prenne en compte la présence d'une nouvelle application web.


.. code-block:: console

   root:~ # cd /var/lib/tomcat-6/webapps
   root:webapps # unzip /usr/lib/apiwatch-1.0/war/*.war -d apiwatch
   Archive:  /usr/lib/apiwatch-1.0/war/apiwatch-war-1.0.war
    creating: apiwatch/META-INF/
    inflating: apiwatch/META-INF/MANIFEST.MF
   ...
   root:webapps # service tomcat-6 restart
    * Stopping tomcat-6 ...   [ ok ]
    * Starting tomcat-6 ...   [ ok ]

L'application web APIWatch devrait être accessible à l'adresse http://localhost:8080/apiwatch/. 
Sur l'illustration suivante on peut voir une capture d'écran de la page d'accueil (aucun composant 
logiciel n'a encore été analysé).

.. figure:: /images/webapp-installed.png

   Page d'accueil de l'application web après le premier démarrage.
   
