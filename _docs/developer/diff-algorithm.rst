
.. image:: /_static/apiwatch-logo-small.png

=========================
Algorithme de comparaison
=========================

Pour détecter des modifications entre deux versions d'une même API, on doit comparer deux 
structures de données construites à base des modèles définis au paragraphe 3.2.1. Rappelons 
que les données d'API produites par APIWatch sont en structures arborescentes. 

Il existe de nombreux algorithmes de comparaison d'arbres. Kuo-Chung Tai en propose un dans 
son article *The Tree-To-Tree Correction Problem* qui permet de calculer la « distance » entre 
deux arbres :math:`A` et :math:`A'`, c'est-à-dire le nombre d'opérations élémentaires [#]_ 
nécessaires pour transformer :math:`A` en :math:`A'` ou vice-versa. L'algorithme présenté 
résout ce problème avec une complexité de :math:`O(n * n' * p^2 * p'^2)`, où n et n' sont 
les nombres de nœuds dans :math:`A` et :math:`A'`, respectivement, et :math:`p` et :math:`p'` 
sont les niveaux de profondeur de :math:`A` et :math:`A'`, respectivement. 
Ce niveau de complexité est bien trop élevé pour être viable dans un logiciel comme APIWatch. 
En effet, les données d'API manipulées pourront atteindre des tailles considérables [#]_ et il 
est impératif que leur comparaison reste une opération rapide – sinon triviale.

On proposera ici un algorithme plus simple qui part du principe essentiel que chaque élément 
de l'arbre est identifiable de manière unique. Cet algorithme se base sur une propriété des 
tables de hachage : la recherche d'un élément par sa clé de hachage se fait en :math:`O(1)` quel 
que soit le nombre d'éléments dans la table si chaque clé de hachage est unique.

La première étape consiste à « aplatir » les deux arbres. On va parcourir la structure 
arborescente et insérer chaque ``APIElement`` rencontré dans une table de hachage. La clé 
utilisée sera le résultat de la méthode ``path()`` de la classe ``APIElement``. Comme la 
structure est un arbre, il ne peut pas y avoir deux éléments qui ont la même valeur de 
``path()``. Prenons l'exemple d'un arbre d'API simple comme décrit sur l'Illustration 18 :

.. figure:: /diagrams/api-tree.svg

   Arbre d'API

Si on calcule les valeurs ``path()`` de chaque élément et qu'on les insère dans une table de 
hachage, on obtient le résultat montré dans l'Illustration 19 :

.. figure:: /diagrams/api-tree-flattened.svg

   Arbre d'API « aplati » dans une table de hachage
   
Voici la fonction Java qui permet « d'aplatir » un APIElement et toute sa sous-arborescence 
dans une table de hachage :

.. code-block:: java
   
   Map<String, APIElement> flatten(APIElement e, Map<String, APIElement> map) {
      map.put(e.path(), e);
      if (e instanceof APIScope)
         for (APIScope scope : ((APIScope) e).subScopes)
            flatten(scope, map);
   
      if (e instanceof SymbolContainer)
         for (Symbol symbol : ((SymbolContainer) e).symbols())
            flatten(symbol, map);
   
      if (e instanceof Function)
         for (Variable arg : ((Function) e).arguments)
            map.put(arg.path(), arg);
   
      return map;
   }

La fonction ``flatten`` sera appelée sur les éléments racines des deux versions *A* et *B* du 
composant logiciel concerné.

.. code-block:: java

   Map<String, APIElement> elementsA = flatten(versionA, new HashMap<String, APIElement>());
   Map<String, APIElement> elementsB = flatten(versionB, new HashMap<String, APIElement>());

Une fois les arbres « aplatis » dans deux tables de hachage, il suffit de parcourir celles-ci 
pour détecter les ajouts et suppressions d'éléments, ainsi que les éléments communs. Voici le 
code Java nécessaire pour effectuer ces comparaisons :

.. code-block:: java
   
   /* Elements de la version A également présents dans la version B */
   Map<String, APIElement> commonA = new HashMap<String, APIElement>();
   
   /* Elements de la version B également présents dans la version A */
   Map<String, APIElement> commonB = new HashMap<String, APIElement>();
   
   List<APIElement> added = new ArrayList<APIElement>();
   List<APIElement> removed = new ArrayList<APIElement>();
   
   for (Map.Entry<String, APIElement> e : elementsA.entrySet()) {
      if (elementsB.containsKey(e.getKey())) {
         commonA.put(e.getKey(), e.getValue());
      } else {
         removed.add(e.getValue());
      }
   }
   for (Map.Entry<String, APIElement> e : elementsB.entrySet()) {
      if (elementsA.containsKey(e.getKey())) {
         commonB.put(e.getKey(), e.getValue());
      } else {
         added.add(e.getValue());   
      }
   }

On remarquera que les ensembles d’éléments communs aux deux versions sont également placés 
dans des tables de hachage. Pour calculer les réelles différences entre les éléments communs, 
il va falloir comparer en détail chaque élément de ``commonA`` avec son homologue dans ``commonB``. 
L'utilisation de tables de hachage permet d'accéder à l'élément correspondant dans l'ensemble 
``commonB`` avec une complexité de :math:`O(1)`. On utilisera ensuite une méthode ``getDiffs`` de 
la classe ``APIElement`` qui renvoie une liste de ``APIDifference``, en comparant en détail chaque 
attribut des deux objets. Les éléments qui ont été ajoutés ou supprimés, seront utilisés pour 
créer des ``APIDifference`` de type ``ADDED`` et ``REMOVED``.

Voici le code Java permettant de créer la liste des différences :

.. code-block:: java
   
   List<APIDifference> diffs = new ArrayList<APIDifference>();
   for (Map.Entry<String, APIElement> e : commonA.entrySet()) {
      APIElement eltA = e.getValue();
      /* L'accès à eltB se fait en O(1) */
      APIElement eltB = commonB.get(e.getKey());
      diffs.addAll(eltA.getDiffs(eltB));
   }
   for (APIElement e : removed) {
      diffs.add(new APIDifference(ChangeType.REMOVED, e, null));
   }
   for (APIElement e : added) {
      diffs.add(new APIDifference(ChangeType.ADDED, null, e));
   }

Déterminons à présent la complexité de l'algorithme proposé, appliqué sur deux structures de 
données d'API contenant respectivement :math:`n` et :math:`n'` éléments et de profondeur 
:math:`p` et :math:`p'`, ces structures ayant :math:`C` éléments en commun. Pour la suite, 
on posera les équivalences suivantes : 

.. math::
   
   N = max(n, n')
   
   P = max(p, p')
   
   C \leq N

La Table 25 montre que l'algorithme proposé effectue la comparaison entre deux arbres en 
:math:`O(N * P)`. Ceci le rend exploitable même sur des jeux de données de taille considérable. 

+-----------------------------------+----------------------------+------------------------+
| Opération                         | Complexité réelle          | Complexité équivalente |
+===================================+============================+========================+
| Aplatissement des données d'API.  | :math:`O(n * p + n' * p')` | :math:`O(N * P)`       |
| On considérera que le calcul de   |                            |                        |
| la clé de hachage d'un élément –  |                            |                        |
| avec la méthode ``path()`` – se   |                            |                        |
| fait en :math:`O(d)` au maximum   |                            |                        |
| pour un arbre de profondeur       |                            |                        |
| :math:`d`.                        |                            |                        |
+-----------------------------------+----------------------------+------------------------+
| Détection des éléments ajoutés et | :math:`O(n + n')`          | :math:`O(N)`           |
| supprimés.                        |                            |                        |
+-----------------------------------+----------------------------+------------------------+
| Calcul des différences sur les    | :math:`O(C)`               | :math:`O(N)`           |
| éléments communs (:math:`C` est   |                            |                        |
| borné par :math:`N`).             |                            |                        |
+-----------------------------------+----------------------------+------------------------+
| Total                                                          | :math:`O(N * P)`       |
+-----------------------------------+----------------------------+------------------------+

.. rubric:: Footnotes

.. [#] Une opération élémentaire consiste à changer, supprimer ou ajouter un nœud d'un arbre.
.. [#] Il est impossible – et inutile – de donner des estimations de taille ici. Un algorithme 
   qui donne une solution en :math:`O(n * n' * p^2 * p'^2)` n'est pas exploitable dans notre cas.