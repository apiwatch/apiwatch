
.. image:: /_static/apiwatch-logo-small.png

=============================
Diff with a reference version
=============================
The apiwatch tool can be used to combine the features of apiscan and apidiff. It can analyse a set of files and directories, then compare their API with the one of a version passed as a parameter. apiscan is designed to be used in a continuous integration environment, to control API stability during development.

The command syntax is as follows: apiwatch <reference> <files to analyse>. As with apidiff, the reference can be a file or a server URI

Here is an example:

.. code-block:: console

   user:src $ apiwatch http://localhost:8080/apiwatch/jenkins-core/1.466/ *
   [INFO] <REM001> Removed PRIVATE Function:jnlpConnect(SlaveComputer) @ 'hudson\TcpSlaveAgentListener.java:314'
   [INFO] <REM001> Removed PRIVATE Function:runJnlpConnect(DataInputStream, PrintWriter) @ 'hudson\TcpSlaveAgentListener.java:227'
   [INFO] <REM001> Removed PRIVATE Function:getSecretKey() @ 'hudson\TcpSlaveAgentListener.java:118'
   ...
   [INFO] 20 violations.

Most apiscan and apidiff options apply to apiwatch as well.
