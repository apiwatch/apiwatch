
.. image:: /_static/apiwatch-logo-small.png

==========
Extensions
==========


Afin de rendre APIWatch modulaire et facilement extensible, on mettra en place un 
mécanisme de plug-ins pour les rôles suivants :

Analyse de langages spécifiques
===============================

Chaque langage de programmation a une syntaxe particulière. Comme il n'est pas possible 
d'écrire un analyseur « générique », une interface a été créée : LanguageAnalyser qui 
permet de laisser les détails d'implémentation séparés du cœur de l'application.

Les méthodes de cette interface sont détaillées dans la Table 21.

Méthode
Type de retour
Description
language()
String
Nom du langage supporté
fileExtensions()
String[]
Liste des extensions de fichiers utilisés par le langage. Exemple, pour un analyseur C++, cette méthode doit renvoyer ["cpp", "hpp"].  La valeur retournée sera utilisée pour choisir quel analyseur convient à un fichier donné.
analyse(File)
APIScope
Principale méthode à implémenter. Chaque analyseur a une liberté complète quant à la façon d'analyser l'API d'un fichier.

Formats de sérialisation
========================

Les éléments du modèle de données doivent pouvoir être sérialisés dans le but de les afficher à l'utilisateur et/ou les stocker. Afin de ne pas limiter les formats de sérialisation et pour faciliter l'interopérabilité de APIWatch avec des systèmes existants, deux interfaces sont disponibles :

APIScopeSerializer
------------------

On trouvera dans la Table 22 un détail des méthodes de l'interface.

Méthode
Type de retour
Description
format()
String
Permet de récupérer l'identifiant – supposé unique – du format de sérialisation.
dump(APIScope, Writer)
-
Sérialise une instance de APIScope dans un flux de texte passé en paramètre.
load(Reader)
APIScope
Reconstitue une instance de APIScope depuis un flux de texte passé en paramètre.

APIStabilityViolationSerializer
-------------------------------

On trouvera dans la Table 23 un détail des méthodes de l'interface.

Méthode
Type de retour
Description
format()
String
Identifiant du format de sérialisation.
dump(
List<APIStability
Violation>,Writer)
-
Sérialise une liste de APIStabilityViolations dans un flux de texte passé en paramètre.
load(Reader)
List<APIStability
Violation>
Reconstitue une liste de APIStabilityViolations depuis un flux de texte passé en paramètre.

Règles de stabilité d'API
-------------------------

L'interface APIStabilityRule vue au paragraphe 3.2.3 est également un point d'extension.


Découverte automatique des extensions en Java
=============================================

Pour mettre en place le système de plug-ins mentionné dans le chapitre 3.2.4, on utilisera 
un mécanisme introduit dans la version 6 de Java : JavaServiceLoader__. Il permet de rechercher 
les implémentations d'une interface ou classe abstraite visibles depuis un ClassLoader. 

__ http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html

Dans le composant **Core** de APIWatch, le service Analyser va découvrir les implémentations de 
l'interface LanguageAnalyser grâce au code Java suivant :

.. code-block:: java
   
   ServiceLoader<LanguageAnalyser> loader = ServiceLoader.load(LanguageAnalyser.class);
   Map<String, LanguageAnalyser> analysers = new HashMap<String, LanguageAnalyser>();
   
   for (LanguageAnalyser impl : loader) {
       for (String fileExt : impl.fileExtensions()) {
           analysers.put(fileExt, impl);
       }
   }

L'application APIWatch est composée de plusieurs modules au format JAR. Pour contribuer à 
un ou plusieurs des points d'extension définis au paragraphe 3.2.4, il suffit de fournir 
un fichier ``.jar`` contenant :

*  Les classes implémentant les interfaces concernées par le ou les points d'extension.
*  Un ou plusieurs fichiers qui portent le nom qualifié des interfaces concernées, placés 
   dans le dossier ``META-INF/services``. Ces fichiers doivent contenir le nom qualifié des 
   classes implémentant les interfaces concernées.

Afin de clarifier les choses, voici un exemple pratique. On souhaite rajouter le support du 
langage C à APIWatch :
 
*  On écrit donc une classe ``CAnalyser`` qui implémente l'interface LanguageAnalyser.
*  On crée également un fichier ``org.apiwatch.analyser.LanguageAnalyser``. Dans ce fichier, 
   on renseigne le nom qualifié de l'implémentation ``org.apiwatch.analyser.CAnalyser``.

Il suffit ensuite de générer un fichier .jar contenant ces fichiers :

*  ``org/apiwatch/analyser/CAnalyser.class``
*  ``META-INF/services/org.apiwatch.analyser.LanguageAnalyser``

Dans la distribution de APIWatch il y aura un dossier lib contenant tous les fichiers ``.jar``. 
Au démarrage des outils en ligne de commande, tous ces fichiers seront ajoutés au class path 
de l'application. Il suffit donc de placer notre fichier ``.jar`` créé dans ce dossier ``lib``.
