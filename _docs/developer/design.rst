
.. image:: /_static/apiwatch-logo-small.png

==========================
Conception et modélisation
==========================

Interface logicielle
====================

Le travail de Terrence Parr dans son livre `Language Implementation Patterns`__ a servi de 
fondations pour modéliser ce qu'est une API de manière formelle. Il était important que le 
modèle soit aussi « générique » que possible. Ceci afin qu'il s'adapte à tous les paradigmes 
de programmation.

__ http://pragprog.com/book/tpdsl/language-implementation-patterns

Le modèle est constitué d'un ensemble de classes simples :

Visibility
----------

C'est un type énuméré permettant de représenter de manière générique la portée ou visibilité 
d'un élément d'API. On retrouvera les différentes valeurs du type énuméré dans la Table 4.

=============== ===============================================================
Valeur          Description
=============== ===============================================================
``PRIVATE``     Visibilité restreinte à la classe dans laquelle l'élément 
                est défini (uniquement applicable pour les langages objet).
``SCOPE``       Visibilité restreinte au contexte courant (fichier, classe, 
                module, foncteur, fonction, procédure, etc.).
``PROTECTED``   Visibilité restreinte aux sous-classes de celle où l'élément 
                est défini (uniquement applicable pour les langages objet).
``PUBLIC``      Visibilité maximale.
=============== ===============================================================

La plupart des langages de programmation n'utiliseront pas toutes les valeurs du type énuméré. 
Par exemple, le langage C n'a que deux niveaux de visibilité possibles : ``SCOPE`` pour une 
variable ou fonction déclarée dans un fichier ``.c``, et ``PUBLIC`` pour une variable ou fonction 
déclarée dans un fichier ``.h``.

La visibilité d'un élément sera cruciale pour déterminer l'importance d'une modification d'API. 
Plus l'élément affecté par la modification est visible, plus le risque engendré par celle-ci 
est élevé.

APIElement
----------

C'est l'élément de base du modèle. C'est une classe abstraite dont toutes les classes du modèle 
vont hériter. On retrouvera une description de ses attributs et méthodes dans les tables 5 et 6 :

=============== =============== ===============================================================
Attribut        Type            Description
=============== =============== ===============================================================
``name``        ``String``      Nom de l'élément.
``language``    ``String``      Langage dans lequel l'élément a été défini. Un champ libre 
                                est utilisé ici plutôt qu'un type énuméré pour permettre une 
                                extensibilité des langages supportés par l'application.
``sourceFile``  ``String``      Chemin du fichier dans lequel l'élément est défini. Il pourra 
                                éventuellement contenir une indication de numéro de ligne dans 
                                le fichier en respectant la syntaxe : 
                                ``<chemin>:<n° de ligne>``
``visibility``  ``Visibility``  Visibilité de l'élément. Voir paragraphe précédent.
``parent``      ``APIElement``  Référence vers l'élément parent de celui-ci.
=============== =============== ===============================================================

=========== =============== ==============================================================
Méthode     Type de retour  Description
=========== =============== ==============================================================
``ident()`` ``String``      Renvoie un identifiant unique pour cet élément. 
                            L’implémentation par défaut renvoie le type effectif 
                            et le nom de l'élément : ``<type>:<nom>``
``path()``  ``String``      Renvoie un « chemin » permettant de retrouver l'élément 
                            dans la structure d'une API. Cette méthode sera utilisée 
                            par l'algorithme de comparaison. Elle utilise l'attribut 
                            ``parent`` pour calculer le chemin.
=========== =============== ==============================================================

Symbol
------

Un symbole est la brique principale d'une API. Cela peut être une variable, une fonction ou 
même une définition de type (simple ou complexe). La classe Symbol est abstraite et hérite 
de la classe APIElement. Ses attributs propres sont décrits dans la Table 7.

============== ================ ==================================================================
Attribut       Type             Description
============== ================ ==================================================================
``modifiers``  ``Set<String>``  Ensemble de mots clés qui permettent de qualifier ou modifier 
                                le comportement d'un symbole. Ils sont dépendants du langage 
                                source. Par exemple en Java, le mot clé static permet de définir 
                                une variable ou méthode attachée directement à une classe et non 
                                pas aux instances de cette classe.
============== ================ ==================================================================

Variable
--------

Une variable est un espace de stockage pour une valeur auquel est attaché un nom. La plupart du 
temps cette variable permet de stocker des valeurs d'un certain type. Dans certains langages 
(comme C) on peut définir des variables avec des contraintes. Par exemple, on peut définir une 
structure de données avec des champs suffixés par une taille en bits [#]_ :

.. code-block:: c

   typedef struct DISK_REGISTER {
        unsigned track         :9; /* valeurs (0, 511) */
        unsigned sector        :5; /* valeurs (0, 31)  */
        unsigned write_protect :1; /* valeurs (0, 1)   */
   } disk_register_t;

Ici, les champs sont déclarés comme des entiers non-signés mais leur valeur est limitée 
à leur taille en bits. 

On utilisera des objets de type Variable pour représenter les variables, les attributs de classe 
et les arguments de fonction. La classe Variable hérite de Symbol. Ses attributs propres sont 
décrits dans la Table 8.

================ =========== ==========================================================
Attribut         Type        Description
================ =========== ==========================================================
``type``         ``String``  Nom du type de la variable tel qu'il est défini dans le 
                             code source.
``constraints``  ``String``  Optionnel. Cet attribut est un champ libre car sa valeur 
                             est trop dépendante du langage de programmation et donc 
                             trop incertaine.
================ =========== ==========================================================

Function
--------

On utilisera la classe Function pour représenter à la fois les procédures, les fonctions et 
les méthodes de classe. Une fonction possède un type de retour, et une liste d'arguments. 
Function hérite également de Symbol. On retrouvera une description de ses attributs et méthodes 
dans les tables 9 et 10, respectivement.

=============== =================== ===========================================================
Attribut        Type                Description
=============== =================== ===========================================================
``returnType``  ``String``          Nom du type de retour de la fonction tel qu'il est 
                                    défini dans le code source.
``arguments``   ``List<Variable>``  Arguments de la fonction. Peut être vide.
``hasVarArgs``  ``boolean``         Vrai si la fonction accepte un nombre variable 
                                    d'arguments. Dans la plupart des langages, on symbolise 
                                    cela par une ellipse « ... » à la fin des arguments.
``exceptions``  ``Set<String>``     Dans certains langages, en plus du type de retour, 
                                    on peut déclarer des types d'exceptions qui peuvent 
                                    être émises par une fonction. Celles-ci ne changent 
                                    pas toujours l'API de manière significative mais peuvent 
                                    avoir un impact tout de même.
=============== =================== ===========================================================

================ ================= ======================================================
Méthode          Type de retour    Description
================ ================= ======================================================
``signature()``  ``String``        Dans certains langages (comme C++ et Java), il peut 
                                   y avoir plusieurs méthodes d'une classe qui ont le 
                                   même nom. Ce procédé est appelé « surcharge ». Pour 
                                   les différencier, on procédera comme la plupart des 
                                   compilateurs [#]_ en suffixant les noms de fonction 
                                   avec les types de leurs arguments.
``ident()``      ``String``        Cette méthode est redéfinie ici en utilisant la 
                                   valeur de la méthode ``signature()`` à la place de 
                                   l'attribut ``name``.
================ ================= ======================================================

Type
----

Dans la plupart des langages de programmation il est possible d'attribuer arbitrairement 
un type aux variables pour – entre autres – déterminer la nature des données qu'elle 
peuvent contenir et la manière dont elle sont enregistrées et traitées par le système. 
Concrètement le type d'un élément influe sur la taille que le compilateur ou l'interpréteur 
lui allouera en mémoire.

La classe Type hérite de Symbol et est également abstraite. Elle n'a pas d'attributs 
ni méthodes propres.

SimpleType
----------

Un type simple sera en règle générale un type scalaire du langage de programmation (booléen, 
nombre entier, caractère, nombre à virgule flottante, etc.) ou un alias vers un autre type 
pré-existant [#]_. 

La classe SimpleType hérite de Type et n'en est qu'une version concrète. Elle n'a pas 
d'attributs ni méthodes propres.

ComplexType
-----------

Les types de données complexes sont des types composés de plusieurs types plus élémentaires 
et qui possèdent une architecture spécifique autorisant des traitements dédiés à leur type.
 
On utilisera des objets de type ComplexType pour représenter des structures de données ou 
des classes (pour les langages orientés objet). Un ComplexType peut contenir un ensemble 
de symboles : des variables, des fonctions ou même d'autres types internes.

ComplexType hérite de la classe Type et possède deux attributs supplémentaires décrits 
dans la Table 12.

=============== ================ ==============================================================
Attribut        Type             Description
=============== ================ ==============================================================
``symbols``     ``Set<Symbol>``  Ensemble de symboles définis à l'intérieur du type complexe. 
``superTypes``  ``Set<String>``  Ensemble de noms de types dont le type complexe hérite.
=============== ================ ==============================================================

APIScope
--------

En règle générale, pour faciliter la maintenance et l'utilisation d'un composant, on place 
les éléments d'API dans des espaces de noms « nommés ». Ceci permet notamment d'éviter les 
conflits de noms. En Java et Ada, on utilise des packages, en C# et C++ : des namespaces, etc.

La classe APIScope reflète ce concept. Elle hérite directement de APIElement car ce n'est 
pas un symbole à proprement parler. En revanche, elle contient des symboles et éventuellement 
un ensemble d'APIScope. Ses attributs et méthodes sont détaillés dans les tables 13 et 14, 
respectivement.

================= =================== ==================================================
Attribut          Type                Description
================= =================== ==================================================
``dependencies``  ``Set<String>``     Ensemble de symboles dont l'APIScope dépend. 
                                      Cet attribut représente les instructions du 
                                      type import ou uses dans le code source.   
``symbols``       ``Set<Symbol>``     Symboles définis dans cet APIScope.
``subScopes``     ``Set<APIScope>``   APIScopes contenus à l'intérieur de celui-ci.
================= =================== ==================================================

======================= =============== =========================================
Méthode                 Type de retour  Description
======================= =============== =========================================
``update(APIScope)``    `-`             Met à jour l'APIScope avec le contenu 
                                        d'un autre passé en argument.
======================= =============== =========================================

N'importe quelle API sera toujours représentée par un APIScope « racine » qui n'a pas de 
nom. Pour les langages qui ne supportent pas les espaces de noms « nommés » (comme C) une 
API sera modélisée uniquement par cet APIScope « racine ». 

L'Illustration 8 montre une vue d'ensemble des modèles précédents organisés les uns par 
rapport aux autres.

.. figure:: /diagrams/model-api.svg
   
   Diagramme de classes, modélisation d'une API


Modifications d'interface
=========================

Maintenant qu'une API peut être modélisée, il faut définir comment modéliser des modifications 
de celle-ci. On partira du principe qu'une modification est détectable entre deux versions 
(A et B) des informations d'API d'un même composant. 

ChangeType
----------

Le type énuméré ChangeType sert à qualifier le type d'une modification d'API afin de pouvoir 
associer un niveau de risque à celle-ci. Les différentes valeurs du type énuméré sont détaillées 
dans la Table 15.

=============== ==================================================
Valeur          Description
=============== ==================================================
``REMOVED``     L'élément a été supprimé de l'API.
``ADDED``       L'élément a été ajouté à l'API.
``CHANGED``     Une des propriétés de l'élément a été modifiée.
=============== ==================================================

APIDifference
-------------

Les modifications seront considérées de manière unitaire. Si plusieurs changements sont 
détectés sur un même élément d'API, ils donneront lieu à plusieurs « modifications ».

La classe qui représente une modification d'API est APIDifference. Elle est caractérisée 
par un type de changement (ChangeType), une référence vers le ou les APIElements concernés 
et dans le cas d'une modification de type CHANGED, l'attribut concerné par cette modification. 
Pour des raisons pratiques et de performance, les valeurs de l'attribut modifié seront 
stockées dans les objets APIDifference.

=============== =============== ============================================================
Attribut        Type            Description
=============== =============== ============================================================
``changeType``  ``ChangeType``  Type de modification. Voir paragraphe précédent.
``elementA``    ``APIElement``  Élément de la version A concerné par la modification. 
                                Non-défini si il s'agit d'un ajout.
``elementB``    ``APIElement``  Élément de la version B concerné par la modification. 
                                Non-défini si il s'agit d'une suppression.
``attribute``   ``String``      Nom de l'attribut concerné par cette modification. 
                                Uniquement défini pour les modifications de type CHANGED.
``valueA``      ``Object``      Valeur de l'attribut pour la version A. Uniquement défini 
                                pour les modifications de type CHANGED.
``valueB``      ``Object``      Valeur de l'attribut pour la version B. Uniquement défini 
                                pour les modifications de type CHANGED.
=============== =============== ============================================================

Sur l'Illustration 9, on trouvera un diagramme de classes de ChangeType et APIDifference.

.. figure:: /diagrams/model-diffs.svg

   Diagramme de classes, modélisation d'une modification d'API
   
Stabilité d'API – Gestion du risque
===================================

La notion d'APIDifference est trop technique pour pouvoir en tirer des conclusions et 
évaluer le risque impliqué par celle-ci. Il va falloir trouver une notion dérivée plus 
exploitable dans la gestion du risque : la « stabilité » d'une API. Le principe de base 
est que « quand une API ne change pas, le risque est nul ». Le but va ensuite être 
d'évaluer le niveau de risque attaché à chaque modification détectée. 

Le problème sera traité de la même manière que les outils de contrôle de qualité de code 
source1 tels que lint__, CheckStyle__, PMD__ ou Klocwork__. Ces outils proposent un mode 
d'opération assez similaire : ils définissent un ensemble de règles de codage (configurables 
ou non) qui sont évaluées sur une représentation du code source analysé. Chaque violation 
de ces règles est associée à un niveau de risque.

__ http://en.wikipedia.org/wiki/Lint_(software)
__ http://checkstyle.sourceforge.net/
__ http://pmd.sourceforge.net/
__ http://www.klocwork.com/

Pour clarifier l'intérêt et le fonctionnement de ces outils d'analyse de code, voici un 
exemple pratique : L'outil CheckStyle possède un ensemble de règles de style de code 
(configurables pour la plupart). L'une d'entre elles vérifie si le développeur n'utilise 
pas de nombres magiques. Un nombre magique est une constante numérique qui diminue souvent 
la lisibilité du code. 

Si on considère le code Java suivant :

.. code-block:: java
   
   if (response.getCode() > 400) {
       throw new HttpError(response);
   }

Ici, le nombre 400 a une signification assez floue. Il serait préférable de le remplacer 
par une constante nommée. En évaluant ce code, CheckStyle va produire une violation de ce type :

.. code-block:: xml

   <violation line="87" column="41" severity="error" 
              message="400 should be defined as a constant."
              source="com.checkstyle.checks.coding.MagicNumberCheck" />

Dans APIWatch, les classes mises en œuvre pour la gestion de stabilité d'API sont les suivantes :

Severity
--------

Le type énuméré Severity sert à qualifier le niveau de risque engendré par une modification d'API. 
Les différentes valeurs du type énuméré sont détaillées dans la Table 17.

============== =======================================================================
Valeur         Description
============== =======================================================================
``INFO``       Niveau de risque nul. La modification n'entraîne aucune rupture 
               de compatibilité.
``MINOR``      Niveau de risque mineur. La modification porte sur des parties 
               réduites et ne risque pas d'entraîner de rupture de compatibilité. 
               Elle mérite néanmoins d'être contrôlée.
``MAJOR``      Niveau de risque majeur. La modification porte sur des parties 
               sensibles et a des chances non négligeables d'entraîner une rupture 
               de compatibilité. Elle doit impérativement être contrôlée.
``CRITICAL``   Niveau de risque critique. La modification a de fortes chances 
               d'entraîner une rupture de compatibilité ascendante.
``BLOCKER``    Niveau de risque bloquant. La modification entraîne une rupture 
               complète de compatibilité ascendante.
============== =======================================================================

APIStabilityViolation
---------------------

Quand une modification est évaluée par une règle de stabilité d'API (voir plus loin), une 
violation peut être émise. Les attributs de la classe APIStabilityViolation sont détaillés 
dans la Table 18.

=============== ======================= ============================================================
Attribut        Type                    Description
=============== ======================= ============================================================
``difference``  ``APIDifference``       Modification d'API qui a donné lieu à cette violation. 
                                        Voir 3.2.2.
``rule``        ``APIStabilityRule``    Règle qui a donné lieu à cette violation. Voir paragraphe 
                                        suivant : APIStabilityRule.
``severity``    ``Severity``            Niveau de risque de cette violation.
``message``     ``String``              Message portant plus de détails sur la violation et son 
                                        contexte.
=============== ======================= ============================================================

Sur l'Illustration 10, on trouvera un diagramme de classes de Severity et APIStabilityViolation.

.. figure:: /diagrams/model-violations.svg

   Diagramme de classes, violation de règle de stabilité d'API
   
APIStabilityRule
----------------

Cette classe abstraite (ou interface) sera utilisée pour représenter la notion de règle de 
stabilité d'API. Les attributs de la classe APIStabilityRule sont détaillés dans la Table 19.

=================================== =========================== ============================================
Méthode                             Type de retour              Description
=================================== =========================== ============================================
``id()``                            ``String``                  Identifiant unique de la règle.
``name()``                          ``String``                  Nom de la règle
``description()``                   ``String``                  Description de la règle
``configure(Map<String, String>)``  `–`                         Configuration du comportement de la règle. 
                                                                Par exemple, quel est le niveau de risque 
                                                                des violations émises dans certains cas. 
                                                                Les détails sont laissés à chaque 
                                                                implémentation.
``isApplicable(APIDifference)``     ``boolean``                 Renvoie Vrai si la règle est applicable 
                                                                à une modification d'API donnée. Cette 
                                                                méthode permet de décider si il est 
                                                                nécessaire d'évaluer les violations 
                                                                provoquées par une modification d'API.
``evaluate(APIDifference)``         ``APIStabilityViolation``   Évalue une modification d'API et renvoie 
                                                                (ou non) une violation de stabilité d'API. 
                                                                Le risque associé à cette violation peut 
                                                                être configuré selon les implémentations 
                                                                des règles.
=================================== =========================== ============================================

On retrouvera un diagramme de la classe de l'interface APIStabilityRule sur l'Illustration 11.

.. figure:: /diagrams/model-rules.svg

   Diagramme de classes, règle de stabilité d'API

Plusieurs implémentations par défaut de l'interface APIStabilityRule seront proposées dans 
APIWatch :


=================== ==============================================================
Classe              Description
=================== ==============================================================
ElementRemoval      Suppression d'un élément d'API.
ElementAddition     Nouvel élément d'API.
ReducedVisibility   Réduction du niveau de visibilité d'un élément d'API.
DependenciesChange  Modification des dépendances d'un APIScope. Uniquement 
                    applicable aux éléments de type APIScope.
FunctionTypeChange  Modification du type de retour d'une fonction. Uniquement 
                    applicable aux éléments de type Function.
ModifiersChange     Changement des « modifers » d'un symbole. Uniquement 
                    applicable aux éléments de type Symbol.
SuperTypesChange    Changement des « super-classes » d'une classe ou interface. 
                    Uniquement applicable aux éléments de type ComplexType. 
VariableTypeChange  Modification du type d'une variable. Uniquement applicable 
                    aux éléments de type Variable. 
=================== ==============================================================

Toutes ces règles sont configurables par l'utilisateur. Un détail des propriétés 
ajustables est donné en annexe A.

Il doit être possible de fournir des implémentations supplémentaires de l'interface 
APIStabilityRule (voir paragraphe 3.2.4).

Extensions
==========

Voir :doc:`extensions`

Principaux services
===================

Analyse
-------

Le cœur du système APIWatch se base sur l'analyse de code source pour en extraire des données 
d'API. Les composants de plus haut niveau (internes ou non à APIWatch) feront appel au service 
Analyser. Ce service contient des références vers toutes les implémentations disponibles du 
service LanguageAnalyser indexées par leurs extensions de fichier supportées.

Le service Analyser expose une unique fonction analyse() qui prend un ensemble de fichiers 
en paramètre. Pour chaque fichier, il va rechercher un analyseur supportant son extension 
et lui déléguer l'analyse de celui-ci. Le résultat (APIScope) de cette analyse va être 
stocké temporairement. Enfin, tous ces résultats intermédiaires vont être fusionnés en un 
seul, représentant l'API du code source analysé.

On trouvera sur l'Illustration 12 un diagramme de flux représentant le mécanisme d'analyse utilisé.

.. figure:: /diagrams/flow-analysis.svg

   Diagramme de flux, analyse de code source

Comparaison de données d'API
----------------------------

Pour pouvoir détecter des violations aux règles de stabilité d'API, il faut calculer 
les différences entre deux versions d'un même composant.
 
Le service DifferenceCalculator proposera une fonction getDiffs() prenant en paramètre 
deux objets du type APIScope et renvoyant une liste d'objets APIDifference. Les 
différences seront recherchées récursivement dans l'ensemble des objets contenus 
par les deux APIScopes (voir :doc:`diff-algorithm`). 

Un diagramme de flux décrivant le mécanisme de calcul des différences d'API est 
donné sur l'Illustration 13,

.. figure:: /diagrams/flow-diff.svg

   Diagramme de flux, calcul des différences entre deux versions d'API d'un même composant

Détection de violations de stabilité d'API
------------------------------------------

Une fois les différences entre deux versions d'une même API calculées, il faut évaluer 
ces différences à l'aide de règles de stabilité d'API. 

Le service ViolationsCalculator doit en premier lieu être configuré avec un ensemble 
de ces règles. Puis, on peut appeler la méthode getViolations() avec pour paramètre, 
les différences calculées. Chaque différence va être évaluée par chaque règle qui va 
émettre ou non une violation de stabilité d'API. Enfin, l'ensemble des violations 
détectées seront retournées par le service.

L'Illustration 14 montre un diagramme de flux représentant le mécanisme de détection 
de violations de stabilité d'API.

.. figure:: /diagrams/flow-violations.svg

   Diagramme de flux, détection des violations de stabilité d'API

Interface avec l'utilisateur
============================

APIWatch se présentera sous la forme d'un outil en ligne de commande et d'une interface web. 

Ligne de commande
-----------------

L'application APIWatch est destinée en premier lieu à être utilisée dans le cadre 
d'une gestion de projet logiciel en intégration continue1. Pour rendre APIWatch 
facilement automatisable, le plus simple est de fournir une interface en ligne de 
commande.

L'interface en ligne de commande doit permettre les opérations suivantes :

*  Extraction des données d'API d'un ensemble de fichiers et/ou répertoires 
   (de manière récursive). Sérialisation de ces données dans un format choisi 
   par l'utilisateur. Envoi des données dans un fichier ou dans l'interface web 
   de APIWatch en renseignant un nom et une version de composant.
*  Calcul des violations de stabilité d'API depuis deux versions analysées auparavant. 
   Les données utilisées en entrée doivent pouvoir provenir de fichiers et/ou de 
   l'interface web de APIWatch. Les règles de stabilité utilisées pour cette opération 
   doivent être configurables par l'utilisateur. Les violations peuvent ensuite être 
   exportées dans un format choisi par l'utilisateur (texte brut, XML, JSON, etc.).

Application « web »
-------------------

Afin de pouvoir garder une trace des données d'API des différentes versions des composants 
logiciels, il faut un support de stockage. Le moyen le plus adapté est d'utiliser une base 
de données relationnelle (un système de fichiers pourrait convenir mais serait plus 
difficile à mettre en œuvre). Les moyens d'accès à une base de données sont multiples 
et manquent de standardisation. De plus, pour l'utilisateur final, l'accès direct à une 
base de données n'est pas convivial. La technologie HTTP a été choisie pour masquer cette 
complexité et réaliser une interface utilisateur simple et compatible avec tous les 
navigateurs web.

L'interface web de APIWatch sera donc couplée à une base de données. Elle doit fournir 
les fonctionnalités suivantes :

*  Stockage des données d'API dans la base de données. Les données doivent être organisées 
   par composant et version de composant. Le résultat d'analyse produit depuis l'interface 
   en ligne de commande doit pouvoir être « poussé » dans la base de données via des 
   requêtes HTTP.
*  Accès aux données d'API stockées dans la base de données. Dans une page au format HTML 
   pour consultation dans un navigateur web et également au format brut (i.e. XML, JSON, etc.) 
   pour utilisation depuis l'interface en ligne de commande.

Découpage logique
=================

Pour des raisons de modularité et pour faciliter la réutilisation, le découpage en 
composants suivant a été choisi :

Core
----

Comme son nom l'indique, ce composant est le « cœur » de APIWatch. Il contient tout le 
modèle de données décrit aux paragraphes 3.2.1, 3.2.2 et 3.2.3. Il expose les points 
d'extension définis au paragraphe 3.2.4. Enfin, il contient toute la logique nécessaire 
aux services décrits au paragraphe 3.2.5.

XXX-Analyser
------------

Ce composant est la première brique modulaire de l'application. Il dépend de Core. 
Son rôle est d'apporter le support d'un nouveau langage de programmation à APIWatch 
en implémentant le point d'extension LanguageAnalyser. Plusieurs composants de ce 
« type » (un par langage) pourront donc exister simultanément dans l'application.

XXX-Serializer
--------------

Ce composant est lui aussi un module interchangeable. Il dépend de Core. Son rôle est 
d'apporter le support d'un nouveau format de sérialisation à APIWatch en implémentant 
les points d'extension APIScopeSerializer et APIStabilityRuleSerializer. Plusieurs 
composants de ce « type » (un par format de sérialisation) pourront donc exister 
simultanément dans l'application.

API Stability Rules
-------------------

Ce composant est lui aussi un module interchangeable. Il dépend de Core. Son rôle est 
d'apporter le support de nouvelles règles de stabilité d'API à APIWatch en implémentant 
le point d'extension APIStabilityRule. Plusieurs composants de ce « type » pourront 
donc exister simultanément dans l'application. Un composant implémentant les règles 
de base de stabilité d'API décrites au paragraphe 3.2.3 sera embarqué par défaut dans 
APIWatch.

Command-Line Interface
----------------------

Ce composant permet l'invocation des services de APIWatch via une interface en ligne 
de commande comme décrit en 3.2.6. Il a une dépendance forte vers Core, et un couplage 
lâche vers les composants XXX-Analyser, XXX-Serializer et API Stability Rules à 
travers le mécanisme de points d'extension. 

Web Interface
-------------

Ce composant permet la persistance et l'accès aux données analysées par APIWatch par 
le biais d'une interface web comme décrit en 3.2.6. Il a une dépendance forte vers 
Core, et un couplage lâche vers les composants XXX-Serializer à travers le 
mécanisme de points d'extension. 

On trouvera un schéma représentant tous ces composants et leurs interdépendances 
dans l'Illustration 15.

.. figure:: /diagrams/apiwatch-components.svg

   Structure et dépendances des composants de l'application APIWatch



.. rubric:: Footnotes

.. [#] http://publications.gbdirect.co.uk/c_book/chapter6/bitfields.html
.. [#] http://en.wikipedia.org/wiki/Name_mangling
.. [#] Dans certains langages on peut définir des alias vers d'autres types pour rendre le 
   programme plus lisible. Par exemple en C, typedef long ADDRESS; permet de déclarer des 
   variables de type ADDRESS qui seront interprétées comme long par le compilateur.