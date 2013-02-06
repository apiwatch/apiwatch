
.. image:: /_static/apiwatch-logo-small.png

==============
Basic Concepts
==============

Component-based software architecture
=====================================

Component-based software architecture focuses on segregating roles in a given software system. It is an approach based on reusing loosely coupled, independent modules called components.

A software component is a logical unit supposed to be used as "spare part" in potentially multiple applications. It most often provides a set of services that can be called through well defined interfaces. One of the benefits of this modular approach is that a component can be replaced with another, as long as interfaces are the same.

In figure 1, we can see a web shop command management example application. Such an application must propose a number of services: command history storage, accounting management, emailing to clients, etc. It would be hard to design such an application in a monolithic way. To make its development easier, a modular approach will be preferred.

.. figure:: /diagrams/components-architecture.svg

   An exemple component-based architecture, command management application

The applications features are provided by specilised components, as shown in next table:

======================= ===================================================================
Component               Role/Service
======================= ===================================================================
``OrderManagement```    Crating, editing, canceling client commands
``Accounting``          accounting management, invoice editing, etc.
``PersistanceLayer``    Storing application data.
``Notifications``       Notifying clients and technical teams.
``UserInterface``       User interface.
======================= ===================================================================

As explained previously, a component can be replaced by another one exposing the same interfaces. For example, we can change the component ``PersistanceLayer`` so that it stores data in a relational database, or directly in a filesystem. And that can be done without changing the application's global behaviour. The other advantage of this approach is that - once roles are defined - each component can be developed by a different team.

Programming interface
=====================

To make usage of the services provided by a component, the other components must "open a channel" with it. Because of this, it is required that they beforehand define a "protocol", and respect it. In software engineering, this protocol is called: *Application Programming Interface*, or *API*.

The term *API* covers two different things:

 *  In its "instantiated" form: an API is the contract exposed by a software component, in which is defined the way to call the services it defines.
 *  In its "conceptual" form: "API informations" carry informations relative to an API.

An API most often includes specifications for routines or functions, data structures, object classes or variables that components must respect, use and/or implement. Formats of read or written files, network communication protocols, an more generally any data interchange between components through a support are part of the API.

Back to previous example; command management component provides three services, as shown in the next table:

================= ============================
Service           Description
================= ============================
``createOrder``   Create an order
``modifyOrder``   Modify an order
``cancelOrder``   Cancel an order
================= ============================

To work, the ``OrderManagement`` component uses services provided by other components, as shown in the next table:

================= ======================= ============================
Service           Provided by             Description
================= ======================= ============================
``getBilling``    ``Accounting``          Creatin a command
``persistOrder``  ``PersistanceLayer``    Editing a command
================= ======================= ============================

On the next figure, we can see an UML diagram representing the API of the ``OrderManagement`` component.

.. figure:: /diagrams/component-api.svg

   API of a software component : ``OrderManagement``


Dependencies
============

As soon as a component A uses a service provided by a component B, A has a direct dependency to B. When a component depends on another, it inherits its dependencies. We then talk about trasitive dependencies.

On next figure we can see the dependencies between the ``OrderManagement``, ``UserInterface`` and ``PersistanceLayer`` components, seen in previous example.

.. figure:: /diagrams/components-dependencies.svg

   Dependencies between components
   
   
