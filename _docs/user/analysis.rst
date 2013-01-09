
.. image:: /_static/apiwatch-logo-small.png

====================
Source Code Analysis
====================


L'analyse simple de code source se fait avec l'outil apiscan. Celui-ci permet de stocker les 
résultats d'une analyse dans un fichier ou dans un serveur *APIWatch*.

Ici, on souhaite analyser l'API exposée par le code source d'un composant logiciel appelé 
``jenkins-core`` pour deux versions de celui-ci : ``1.447`` et ``1.466``. On considérera que l'appel de 
l'outil apiscan se fait depuis un dossier contenant tous les fichiers de code source.

Voici la commande nécessaire pour analyser récursivement le contenu du répertoire courant. 
L'option ``-o/--output`` permet de spécifier l'emplacement où l'on souhaite stocker le résultat 
de l'analyse. Ici on a fourni l'URL du serveur *APIWatch* mais on peut également donner un 
chemin vers un répertoire et le résultat sera stocké dans un fichier ``<répertoire>/api.json``. 
Si l'option ``-o/--output`` est omise, le résultat est envoyé vers la sortie standard :

.. code-block:: console
   
   user:src $ apiscan * -o http://localhost:8080/apiwatch/jenkins-core/1.447/
   [INFO] Sent results to URL: http://localhost:8080/apiwatch/jenkins-core/1.447/

Quand on choisit d'envoyer le résultat vers le serveur APIWatch, il faut spécifier une URL 
respectant la forme suivante [#]_ :

   ``<URL de base de l'application><nom du composant>/<version>/``

Sur l'Illustration suivante on peut voir la page d'accueil après avoir analysé deux versions du 
composant ``jenkins-core``.

.. figure:: /images/webapp-home.png

   Page d'accueil après analyse de deux versions du composant ``jenkins-core``

En cliquant sur le nom du composant, on accède au détail des versions de celui-ci :

.. figure:: /images/webapp-component.png

   Versions d'un composant logiciel

En cliquant sur le nom d'une des versions on peut visualiser les données d'API analysées :

.. figure:: /images/webapp-version.png

   Données d'API pour une version

Les icônes utilisées sur la page représentée dans l'illustration précédente correspondent aux 
objets du modèle d'API décrit au chapitre 3.2.1. Certaines icônes sont décorées en fonction de 
la visibilité de l'élément qu'elles représentent. La couleur de l'icône ou de la décoration 
donne la visibilité (rouge pour PRIVATE, jaune pour PROTECTED et bleu pour SCOPE). Si l'icône 
est verte ou n'a pas de décoration, la visibilité de l'élément est PUBLIC :

.. |AR| image:: /_static/icons/apiscope-private.gif
.. |AO| image:: /_static/icons/apiscope-protected.gif
.. |AU| image:: /_static/icons/apiscope-public.gif
.. |AS| image:: /_static/icons/apiscope-scope.gif

.. |TR| image:: /_static/icons/complextype-private.gif
.. |TO| image:: /_static/icons/complextype-protected.gif
.. |TU| image:: /_static/icons/complextype-public.gif
.. |TS| image:: /_static/icons/complextype-scope.gif

.. |FR| image:: /_static/icons/function-private.gif
.. |FO| image:: /_static/icons/function-protected.gif
.. |FU| image:: /_static/icons/function-public.gif
.. |FS| image:: /_static/icons/function-scope.gif

.. |VR| image:: /_static/icons/variable-private.gif
.. |VO| image:: /_static/icons/variable-protected.gif
.. |VU| image:: /_static/icons/variable-public.gif
.. |VS| image:: /_static/icons/variable-scope.gif

.. |SR| image:: /_static/icons/simpletype-private.gif
.. |SO| image:: /_static/icons/simpletype-protected.gif
.. |SU| image:: /_static/icons/simpletype-public.gif
.. |SS| image:: /_static/icons/simpletype-scope.gif

.. |D| image:: /_static/icons/dependency.gif

==================== ==================
Icône(s)             Objet
==================== ==================
|AR| |AO| |AU| |AS|  ``APIScope``
|TR| |TO| |TU| |TS|  ``ComplexType``
|FR| |FO| |FU| |FS|  ``Function``
|VR| |VO| |VU| |VS|  ``Variable``
|SR| |SO| |SU| |SS|  ``SimpleType``
|D|                  Dépendance
==================== ==================

.. note:: 

   Sur cette page, on peut filtrer les éléments que l'on souhaite afficher par plusieurs 
   critères :

   *  **Leur nom** grâce au champ de recherche.
   *  **Leur visibilité** grâce à la liste déroulante rouge en haut à droite.

.. rubric:: Footnotes

.. [#] Si une version a déjà été stockée dans le serveur, il ne sera pas possible de l'écraser.