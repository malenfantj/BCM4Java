# BCM4Java
Basic component model for Java/RMI

BCM4Java implements a simple component model for Java. It is based on the
standard concepts for a component model: components themselves, offered
and required interfaces, outbount and inbound ports through which components
call each others and expose interfaces, connectors that are used to
connect components through their outbound and inbound ports and that
can mediate between required and offered interfaces that need not be
the same. In BCM4Java, components can have their own threads and they
can reside in different JVM on different hosts. Ports and connectors
use RMI to connect components that are in different JVM.

BCM4Java can be packaged as a jar and deployed as simply as putting
its jar in the classpath of applications. It has few dependencies.
The Javadoc provides help for new users to grasp the model. The
source folder 'examples' gives a few examples among which the
basic client/server (basic_cs) has a complete documentation showing
the steps in programming such an exmaple and then running it on
a single JVM and then on two, one for the client and one for the
server.

BCM has been created for research purposes (experimenting with
autonomic cyber-physical systems, where it was used to implement an
applicaiotn running 10.000 components over 50 JVM running on five
computers). It was then used for teaching; at this time nearly
100 students at master level have programmed their term projects
using it.

BCM4Java is distributed under the CeCILL-C license.

Contributing to the project is not yet open, but should be in the
near future. Contact me if you are interested.
