
.. image:: /_static/apiwatch-logo-small.png

=============================
Diff with a reference version
=============================

L'outil apiwatch permet de combiner les fonctionnalités de apiscan et apidiff. Il va analyser 
un ensemble de fichiers et dossiers, puis en comparer l'API avec celle d'une version de 
référence passée en paramètre. apiscan sera le plus souvent utilisé en intégration continue 
pour contrôler la stabilité des API au fur et à mesure du développement.

La syntaxe de la commande est la suivante : apiwatch <reference> <fichiers à analyser>. Comme 
pour apidiff, la référence peut être un fichier ou une URL du serveur. 

Voici un exemple :

.. code-block:: console

   user:src $ apiwatch http://localhost:8080/apiwatch/jenkins-core/1.466/ *
   [INFO] <REM001> Removed PRIVATE Function:jnlpConnect(SlaveComputer) @ 'hudson\TcpSlaveAgentListener.java:314'
   [INFO] <REM001> Removed PRIVATE Function:runJnlpConnect(DataInputStream, PrintWriter) @ 'hudson\TcpSlaveAgentListener.java:227'
   [INFO] <REM001> Removed PRIVATE Function:getSecretKey() @ 'hudson\TcpSlaveAgentListener.java:118'
   ...
   [INFO] 20 violations.

La plupart des options de apiscan et apidiff s'applique également à apiwatch.