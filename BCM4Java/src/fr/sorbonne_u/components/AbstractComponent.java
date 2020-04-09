package fr.sorbonne_u.components;

// Copyright Jacques Malenfant, Sorbonne Universite.
// 
// Jacques.Malenfant@lip6.fr
// 
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// distributed applications in the Java programming language.
// 
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
// 
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
// 
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
// 
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import fr.sorbonne_u.components.annotations.AddPlugin;
import fr.sorbonne_u.components.annotations.AddPlugins;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.connectors.ConnectorI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.exceptions.PostconditionException;
import fr.sorbonne_u.components.exceptions.PreconditionException;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.helpers.ComponentExecutorServiceManager;
import fr.sorbonne_u.components.helpers.ComponentSchedulableExecutorServiceManager;
import fr.sorbonne_u.components.helpers.Logger;
import fr.sorbonne_u.components.helpers.TracerOnConsole;
import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;
import fr.sorbonne_u.components.ports.InboundPortI;
import fr.sorbonne_u.components.ports.OutboundPortI;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionI;
import fr.sorbonne_u.components.reflection.utils.ConstructorSignature;
import fr.sorbonne_u.components.reflection.utils.ServiceSignature;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.util.HotSwapAgent;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractComponent</code> represents the basic information
 * and methods for components in the component model, completing the component
 * virtual machine with operations dealing with individual components.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * In BCM, a component is an instance of class that extends
 * <code>AbstractComponent</code> and which implements component services
 * with traditional Java methods. <code>AbstractComponent</code> provides
 * methods to query components to know its implemented interfaces, the URIs
 * of the ports through which one can connect to it and other information.
 * All calls to component services pass through inbound ports.  Inbound ports
 * as objects implements the methods from an offered component service
 * interface but each inbound port method implementation must relay the call
 * to a method actually implementing the corresponding service in the component
 * object. To relay the call, inbound ports create a component task
 * implementing <code>ComponentService</code> and then call the method
 * <code>handleRequest</code> passing it this task as parameter.
 * </p>
 * <p>
 * Components can be passive or active. Passive components do not have their
 * own thread, so any call they serve will use the thread of the caller. 
 * <code>handleRequest</code> simply calls the component service in the
 * thread of the caller component. Active components use their own threads
 * to perform the tasks.which are managed through the Java Executor framework
 * that implements the concurrent servicing of requests.  At creation time,
 * components may be given 0, 1 or more threads as well as 0, 1 or more
 * schedulable threads. Schedulable threads are useful when some service
 * or task must be executed at some specific (real) time or after some
 * specific (real) duration.
 * </p>
 * <p>
 * Note that methods that implement the component services need not have the
 * same signature as the ones exposed in offered interfaces.  Being able to
 * distinguish between exposed and implementation signatures can be interesting
 * when a component offers the same interface through several ports.  It can
 * then have different implementations of the service depending on the port
 * through which it is called.
 * </p>
 * <p>
 * Active, or concurrent, components have their own threads, hence concurrent
 * execution can be used to service requests coming from client components or
 * to execute some task required by the component itself or by some other
 * component.The Java executor service uses Java futures making all calls
 * asynchronous call with futures. As BCM aims to provide distributed processing
 * capabilities on more than one JVM, Java futures cannot be returned to the
 * caller component. Currently, three variations of the method
 * <code>handleRequest</code> are proposed to programmers of inbound ports.
 * The current <code>handleRequest</code> implementation returns the Java
 * future and leave to the inbound port programmer to decide how to use it.
 * <code>handleRequestSync</code> forces a synchronous call by getting the
 * value of the future right after passing the call to the Java executor.
 * <code>handleRequestAsync</code> assumes that no result is expected by the
 * caller (typically the method result is <code>void</code>) and makes the
 * call asynchronous by returning to the caller immediately after submitting
 * the component service task to the Java executor, hence allowing the caller
 * resume its execution in parallel with the execution of the component service.
 * A fully distributed BCM future variable implementation is planned to be
 * added soon, and then the return type of <code>handleRequest</code> will
 * be changed to some <code>BCMFuture</code> like class.
 * </p>
 * <p>
 * Active components can also execute pure tasks, implementing
 * <code>ComponentTask</code>, in a fire-and-forget mode by calling the
 * methods <code>runTask</code> or <code>scheduleTask</code>. Finally,
 * in the component life-cycle, the method <code>execute</code> is
 * called after the method <code>start</code>. Programmers of components
 * can use this method to implement a background processing in the
 * component, pretty much as the main method is used in Java classes.
 * </p>
 * <p>
 * To get reliable behaviours, components should execute all code within
 * component services and tasks run through the executor service.
 * </p>
 * <p>
 * As it relies on the Executor framework, the concurrent component implements
 * part of the <code>ExecutorService</code> interface regarding the life
 * cycle management that is simply forwarded to the executor.  Subclasses
 * should redefine these methods especially when they implement composite
 * components with concurrent subcomponents.
 * </p>
 * <pre>
 * TODO: Still needs more work and thinking about the life cycle implementation
 *       and in particular the shutting down of components and the interaction
 *       with reflective features.
 * </pre>
 * 
 * <p><i>Usage</i></p>
 * 
 * <p>
 * This class is meant to be extended by any class implementing a kind of
 * components in the application.  Constructors and methods should be used only
 * in the code of the component so to hide technicalities of the implementation
 * from the component users.  The proper vision of the component model is to
 * consider the code in this package, and therefore in this class, as a virtual
 * machine to implement components rather that application code.
 * </p>
 * <p>
 * Components are indeed implemented as objects but calling from the outside of
 * these objects methods they define directly is something that should be done
 * only in virtual machine code and in this component code but never in other
 * components code (i.e., essentially in classes derived from AbstractCVM or in
 * the class defining the component or one of its subclasses). The call should
 * also use only methods defined within this abstract class and not methods
 * defined as services in user components that must be called through the
 * Executor framework.
 * </p>
 * 
 * <p><i>Executor services management</i></p>
 * 
 * <p>
 * Components can have their own threads, which are managed through Java
 * executor services. By default, <code>AbstractComponent</code> can
 * create two executor services: one for non schedulable threads and
 * another for schedulable ones. The constructor takes three arguments,
 * the two last ones controlling the number of threads in the non
 * schedulable and the schedulable executor services respectively.
 * When an inbound port wants to make a service execute on its owner
 * component, it constructs a request or a task from
 * <code>AbstractService</code> (for requests) or from
 * <code>AbstractTask</code> and summit it to the appropriate
 * executor service using the methods <code>runTask</code> or
 * <code>handleRequest</code> for non scheduled ones and
 * <code>scheduleTask</code> or <code>scheduleRequest</code>
 * for scheduled ones.
 * </p>
 * <p>
 * Besides the possibility to submit task and requests to these
 * two standard executor services, components can also create more
 * non schedulable or schedulable executor services giving them
 * unique identifiers (URI) using the method
 * <code>createNewExecutorService</code>. A complementary set of
 * methods for running tasks and executing requests take as first
 * argument either the URI of the executor service that must
 * execute them. To make these calls more efficient, other
 * similar methods take the index of the executor service in
 * a vector of executor services defined by the component
 * (one can get the index corresponding to a given URI with the
 * method <code>getExecutorServiceIndex(java.lang.String)</code>).
 * This capability is particularly interesting for components
 * that want to separate completely the threads used to execute
 * non overlapping subsets of their services or requests, to
 * impose a finer concurrency control mechanism or different
 * priorities for clients, for example.
 * </p>
 * 
 * <p><i>Plug-in facility</i></p>
 * 
 * <p>
 * To ease the reuse of component behaviours, BCM implements a plug-in
 * facility for components. A plug-in is an object which class inherit from
 * <code>AbstractPlugin</code> (see its documentation) and which is meant to
 * implement a coherent reusable behaviour consisting of service
 * implementations with their required or offered interfaces declarations,
 * port creations, as well as a proper plug-in life-cycle with its installation
 * on the component, initialisation, finalisation and uninstallation. Plug-ins
 * can be added or removed dynamically to and from components; they are
 * identified by URI. When an inbound port want to call a service implemented
 * by a plug-in, it can retrieve the plug-in from its URI with the method
 * <code>getPlugin</code> and then call the service method on the retrieved
 * object. To ease this process, a specific set of inbound ports for plug-ins
 * can be used; they are created by passing them the URI of their corresponding
 * plug-in to abstract the retrieving of the plug-in object away from the user
 * code.
 * </p>
 * 
 * <p><i>Logging and tracing facility</i></p>
 * 
 * <p>
 * Debugging threaded Java code is notoriously difficult as debuggers rarely
 * handle thread interruptions correctly. It is even more difficult for
 * programs distributed among several JVM. Hence, most of the debugging rely
 * on trace or log messages allowing to understand the order of events among
 * the different threads and JVM. However, using standard output and standard
 * error stream for that is inappropriate when the code is executed on remote
 * computers that do not have access to a proper standard output and standard
 * error.
 * </p>
 * <p>
 * In place, BCM proposes a logging and tracing facility that can be used even
 * in a distributed environment. The basic method to be used to produce trace
 * and log messages is <code>logMessage</code>. Each message is tagged with
 * the system time at their production. Log and trace can be activated
 * independently using the corresponding toggle methods. If none are activated,
 * <code>logMessage</code> does nothing. When tracing is activated, each message
 * will appear in a trace window. When logging is activated, messages are kept
 * until the end of the execution and can be written to a file. The produced
 * file is in CSV format, so they can be merged into one file, read as a
 * spreadsheet file with time stamps in the first column. Hence, sorting by the
 * first column put the messages in their order of execution (modulo the clock
 * drifts for distributed programs).
 * </p>
 * 
 * <p><i>BCM internal traces</i></p>
 * 
 * <p>
 * The component virtual machine defined by <code>AbstractCVM</code> uses the
 * logging and tracing facility and complete it with a way to activate,
 * deactivate and extend debugging modes that can be tested in if statements
 * to activate and deactivate debugging traces. This capability is used in the
 * BCM kernel to help the debugging. See the documentation of
 * <code>AbstractCVM</code>, <code>CVMDebugModesI</code> and
 * <code>CVMDebugModes</code> for more information.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	requiredInterfaces != null
 * invariant	offeredInterfaces != null
 * invariant	interfaces2ports != null
 * invariant	forall(Class inter : interfaces2ports.keys()) { requiredInterfaces.contains(inter) || offeredInterfaces.contains(inter) }
 * </pre>
 * 
 * <p>Created on : 2012-11-06</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractComponent
implements	ComponentI
{
	// ------------------------------------------------------------------------
	// Internal information about inner components and component lifecycle
	// management.
	// ------------------------------------------------------------------------

	/** current state in the component life-cycle.						*/
	protected ComponentState			state ;

	// ------------------------------------------------------------------------
	// Inner components management
	// ------------------------------------------------------------------------

	/** inner components owned by this component.						*/
	private final Vector<ComponentI>	innerComponents ;
	/** reference to the immediate composite component containing this
	 *  component as subcomponent.										*/
	private AbstractComponent			composite ;

	/**
	 * sets the reference to the composite component containing immediately
	 * this component ; the composite component must have this component as
	 * subcomponent for this method to succeed otherwise an assertion exception
	 * is raised.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	composite != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param composite		the reference to the composite component containing immediately this component.
	 */
	private void		setCompositeComponentReference(
		AbstractComponent composite
		)
	{
		assert	this.composite == null ;
		this.composite = composite ;
	}

	/**
	 * return true if this component is a subcomponent of a composite.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if this component is a subcomponent of a composite.
	 */
	protected boolean	isSubcomponent()
	{
		return this.composite != null ;
	}

	/**
	 * get the reference to the immediate composite component containing
	 * this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isSubcomponent()
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the reference to the immediate composite component containing this component.
	 */
	protected AbstractComponent	getCompositeComponentReference()
	{
		assert	this.isSubcomponent() ;

		return composite ;
	}

	/**
	 * find the inbound port with the given URI of a subcomponent that has
	 * the given reflection inbound port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param subcomponentURI	the URI of the reflection inbound port of the subcomponent.
	 * @param portURI			the URI of the port that is sought.
	 * @return					the reference on the inbound port of the subcomponent.
	 */
	protected InboundPortI	findSubcomponentInboundPortFromURI(
		String subcomponentURI,
		String portURI
		)
	{
		ComponentI subcomponent = null ;
		synchronized (this.innerComponents) {
			for (ComponentI c : this.innerComponents) {
				try {
					if (c.findPortURIsFromInterface(ReflectionI.class)[0].
													equals(subcomponentURI)) {
						subcomponent = c ;
						break ;
					}
				} catch (Exception e) {
					throw new RuntimeException(e) ;
				}
			}
		}

		return ((AbstractComponent)subcomponent).
										findInboundPortFromURI(this, portURI) ;
	}

	/**
	 * finds an inbound port of this component from its URI if it is a
	 * subcomponent of the given composite.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	composite != null
	 * pre	portURI != null
	 * post	return == null || return.getPortURI().equals(portURI)
	 * </pre>
	 *
	 * @param composite	the reference to a component that must be the composite that has this component as subcomponent.
	 * @param portURI	the URI a the sought port.
	 * @return			the port with the given URI or null if not found.
	 */
	private InboundPortI	findInboundPortFromURI(
		ComponentI composite,
		String portURI
		)
	{
		assert	composite != null :
					new PreconditionException("composite must not be null!") ;
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	portURI != null :
					new PreconditionException("Port URI is null!") ;

		InboundPortI p = null ;
		if (this.composite == composite) {
			synchronized (this.portURIs2ports) {
				p = (InboundPortI) this.portURIs2ports.get(portURI) ;
			}
		}

		return p ;
	}

	// ------------------------------------------------------------------------
	// Internal concurrency management
	// ------------------------------------------------------------------------

	/**
	 * The interface <code>ExecutorServiceFactory</code> proposes a mean to
	 * provide a factory creating executor service instances to be added to the
	 * executor services of the component.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * This interface is useful when programmers need to extend standard Java
	 * thread pools to provide additional services, like gathering execution
	 * statistics.
	 * </p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2020-03-18</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static interface		ExecutorServiceFactory
	{
		/**
		 * create a new executor service (thread pool) with the given number of
		 * threads.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code nbThreads > 0}
		 * post	ret != null
		 * </pre>
		 *
		 * @param nbThreads	number of threads to put in the executor service.
		 * @return			the new executor service with the given number of threads.
		 */
		public ExecutorService	createExecutorService(int nbThreads) ;
	}

	/**
	 * The class <code>StandardExecutorServiceFactory</code> implements an
	 * executor service factory creating standard Java thread pools.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2020-03-18</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	protected static class		StandardExecutorServiceFactory
	implements	ExecutorServiceFactory
	{
		/**
		 * @see fr.sorbonne_u.components.AbstractComponent.ExecutorServiceFactory#createExecutorService(int)
		 */
		@Override
		public ExecutorService	createExecutorService(int nbThreads)
		{
			if (nbThreads == 1) {
				return Executors.newSingleThreadExecutor() ;
			} else {
				return Executors.newFixedThreadPool(nbThreads) ;
			}
		}
	}

	/**
	 * The class <code>StandardSheduledExecutorServiceFactory</code> implements
	 * an executor service factory creating standard Java scheduled thread
	 * pools.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2020-03-18</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	protected static class		StandardSheduledExecutorServiceFactory
	implements	ExecutorServiceFactory
	{
		/**
		 * @see fr.sorbonne_u.components.AbstractComponent.ExecutorServiceFactory#createExecutorService(int)
		 */
		@Override
		public ExecutorService	createExecutorService(int nbThreads)
		{
			if (nbThreads == 1) {
				return Executors.newSingleThreadScheduledExecutor() ;
			} else {
				return Executors.newScheduledThreadPool(nbThreads) ;
			}
		}
	}

	/** true if the component executes concurrently.						*/
	protected boolean					isConcurrent ;
	/** true if the component can schedule tasks.							*/
	protected boolean					canScheduleTasks ;

	/** the executor service in charge of handling component requests.	*/
//	protected ExecutorService			requestHandler ;
	/** number of threads in the <code>ExecutorService</code>.			*/
	protected int						nbThreads ;
	/** the executor service in charge of handling scheduled tasks.		*/
//	protected ScheduledExecutorService	scheduledTasksHandler ;
	/** number of threads in the <code>ScheduledExecutorService</code>.	*/
	protected int						nbSchedulableThreads ;

	protected int						executorServicesNextIndex ;
	/** map from URI of executor services to their index.				 	*/
	protected Map<String,Integer>		executorServicesIndexes ;
	/** URI of the standard request handler pool of threads.				*/
	public static final String			STANDARD_REQUEST_HANDLER_URI =
											"STANDARD_REQUEST_H_URI" ;
	/** index of the standard request handler pool of threads.			*/
	protected final int					standardRequestHandlerIndex ;
	/** URI of the standard schedulable tasks handler pool of threads.	*/
	public static final String			STANDARD_SCHEDULABLE_HANDLER_URI =
											"STANDARD_SCHEDULABLE_H_URI" ;
	/** index of the standard schedulable tasks handler pool of threads.	*/
	protected final int					standardSchedulableHandlerIndex ;
	/** vector of executor service managers.							 	*/
	protected ArrayList<ComponentExecutorServiceManager>	executorServices ;

	/**
	 * create a new user-defined executor service under the given URI and
	 * with the given number of threads.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null
	 * pre	!this.validExecutorServiceURI(uri)
	 * pre	nbThreads &gt; 0
	 * post	this.validExecutorServiceURI(uri)
	 * </pre>
	 *
	 * @param uri			URI of the new executor service.
	 * @param nbThreads		number of threads of the new executor service.
	 * @param schedulable	if true, the new executor service is schedulable otherwise it is not.
	 * @return				the index associated with the new executor service.
	 */
	protected int			createNewExecutorService(
		String uri,
		int nbThreads,
		boolean schedulable
		)
	{
		return this.createNewExecutorService(
							uri, nbThreads, schedulable,
							schedulable ?
								new StandardSheduledExecutorServiceFactory()
							:	new StandardExecutorServiceFactory()) ;
	}

	/**
	 * create a new user-defined executor service under the given URI and
	 * with the given number of threads.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null
	 * pre	!this.validExecutorServiceURI(uri)
	 * pre	{@code nbThreads > 0}
	 * pre	factory != null
	 * pre	schedulable ?
	 * 			factory.createExecutorService(nbThreads) instanceof
	 * 												ScheduledExecutorService
	 * 		:	factory.createExecutorService(nbThreads) instanceof
	 * 												ExecutorService
	 * post	this.validExecutorServiceURI(uri)
	 * </pre>
	 *
	 * @param uri			URI of the new executor service.
	 * @param nbThreads		number of threads of the new executor service.
	 * @param schedulable	if true, the new executor service is schedulable otherwise it is not.
	 * @param factory		an executor service factory used to create the new thread pool.
	 * @return				the index associated with the new executor service.
	 */
	protected int			createNewExecutorService(
		String uri,
		int nbThreads,
		boolean schedulable,
		ExecutorServiceFactory factory
		)
	{
		assert	uri != null : new PreconditionException("uri != null") ;
		assert	!this.validExecutorServiceURI(uri) :
					new PreconditionException(
								"!this.validExecutorServiceURI(uri)") ;
		assert	nbThreads > 0 : new PreconditionException("nbThreads > 0") ;
		assert	factory != null : new PreconditionException("factory != null") ;
		int size_pre = this.executorServices.size() ;

		int index = this.executorServicesNextIndex++ ;
		assert	index == this.executorServices.size() ;
		this.executorServicesIndexes.put(uri, index) ;

		ComponentExecutorServiceManager cesm = null ;
		ExecutorService es = factory.createExecutorService(nbThreads) ;

		assert	schedulable ?
					es instanceof ScheduledExecutorService
				:	es instanceof ExecutorService ;

		if (!schedulable) {
			cesm = new ComponentExecutorServiceManager(uri, nbThreads, es) ;
		} else {
			cesm = new ComponentSchedulableExecutorServiceManager(
													   uri, nbThreads, es) ;
		}
		this.executorServices.add(cesm) ;
		this.isConcurrent = true ;
		this.canScheduleTasks = schedulable ;

		assert	this.executorServicesIndexes.get(uri) == index ;
		assert	this.executorServices.get(index) != null ;
		assert	uri.equals(this.executorServices.get(index).getURI()) ;
		assert	this.executorServices.size() == size_pre + 1 ;

		assert	this.validExecutorServiceURI(uri) :
					new PostconditionException(
								"this.validExecutorServiceURI(uri)") ;

		return index ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#validExecutorServiceURI(java.lang.String)
	 */
	@Override
	public boolean		validExecutorServiceURI(String uri)
	{
		return uri != null && this.executorServicesIndexes.containsKey(uri) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#validExecutorServiceIndex(int)
	 */
	@Override
	public boolean		validExecutorServiceIndex(int index)
	{
		return index >= 0 && index < this.executorServices.size() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isSchedulable(java.lang.String)
	 */
	@Override
	public boolean		isSchedulable(String uri)
	{
		assert	this.validExecutorServiceURI(uri) :
					new PreconditionException(
							"this.validExecutorServiceURI(uri) " + uri) ;

		return this.executorServices.
				get(this.executorServicesIndexes.get(uri)).isSchedulable() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isSchedulable(int)
	 */
	@Override
	public boolean		isSchedulable(int index)
	{
		assert	this.validExecutorServiceIndex(index) :
					new PreconditionException(
							"this.validExecutorServiceIndex(index) " + index) ;

		return this.executorServices.get(index).isSchedulable() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getExecutorServiceIndex(java.lang.String)
	 */
	@Override
	public int			getExecutorServiceIndex(String uri)
	{
		assert	this.validExecutorServiceURI(uri) :
					new PreconditionException(
							"this.validExecutorServiceURI(uri) " + uri) ;

		int ret = this.executorServicesIndexes.get(uri) ;

		assert	this.validExecutorServiceIndex(ret) :
					new PostconditionException(
							"this.validExecutorServiceIndex(return) " + ret) ;

		return ret ;
	}

	/**
	 * get the executor service at the given index.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.validExecutorServiceIndex(index)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param index	index of the sought executor service.
	 * @return		the executor service at the given index.
	 */
	protected ExecutorService	getExecutorService(int index)
	{
		assert	this.validExecutorServiceIndex(index) :
					new PreconditionException(
							"this.validExecutorServiceIndex(index) " + index) ;

		return this.executorServices.get(index).getExecutorService() ;
	}

	/**
	 * get the executor service at the given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.validExecutorServiceURI(uri)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri	URI of the sought executor service.
	 * @return		the executor service at the given URI.
	 */
	protected ExecutorService	getExecutorService(String uri)
	{
		assert	this.validExecutorServiceURI(uri) :
					new PreconditionException(
							"this.validExecutorServiceURI(uri) " + uri) ;

		return this.executorServices.get(this.getExecutorServiceIndex(uri)).
													getExecutorService() ;
	}

	/**
	 * get the executor service at the given index.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.validExecutorServiceIndex(index)
	 * pre	this.isSchedulable(index)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param index	index of the sought executor service.
	 * @return		the executor service at the given index.
	 */
	protected ScheduledExecutorService	getSchedulableExecutorService(
		int index
		)
	{
		assert	this.validExecutorServiceIndex(index) :
					new PreconditionException(
							"this.validExecutorServiceIndex(index) " + index) ;
		assert	this.isSchedulable(index) :
					new PreconditionException(
							"this.isSchedulable(index) " + index) ;

		return ((ComponentSchedulableExecutorServiceManager) 
									this.executorServices.get(index)).
											getScheduledExecutorService() ;
	}

	/**
	 * get the executor service at the given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.validExecutorServiceURI(uri)
	 * pre	this.isSchedulable(uri)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri	URI of the sought executor service.
	 * @return		the executor service at the given URI.
	 */
	protected ScheduledExecutorService	getSchedulableExecutorService(
		String uri
		)
	{
		assert	this.validExecutorServiceURI(uri) :
					new PreconditionException(
								"this.validExecutorServiceURI(uri) " + uri) ;

		ComponentExecutorServiceManager csem =
				this.executorServices.get(this.getExecutorServiceIndex(uri)) ;

		assert	csem.isSchedulable() :
					new PreconditionException("csem.isSchedulable() " + csem) ;

		return ((ComponentSchedulableExecutorServiceManager) csem).
											getScheduledExecutorService() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#hasUserDefinedSchedulableThreads()
	 */
	@Override
	public boolean		hasUserDefinedSchedulableThreads()
	{
		boolean ret = false ;
		for (int i = 0 ; !ret && i < this.executorServices.size() ; i++) {
			ret = this.executorServices.get(i).isSchedulable() ;
		}
		return ret ;
	}

	// ------------------------------------------------------------------------
	// Plug-ins facilities
	// ------------------------------------------------------------------------

	/** Map of plug-in URI to installed plug-ins on this component.		*/
	protected Map<String,PluginI>		installedPlugins ;

	/**
	 * configure the plug-in facilities for this component, adding the offered
	 * interface, the inbound port and publish it.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!this.isPluginFacilitiesConfigured()
	 * post	this.isPluginFacilitiesConfigured()
	 * </pre>
	 *
	 * @throws Exception	<i>todo.</i>
	 */
	protected void		configurePluginFacilities() throws Exception
	{
		assert	!this.isPluginFacilitiesConfigured() :
					new RuntimeException("Can't configure plug-in "
										+ "facilities, already done!") ;

		this.installedPlugins = new HashMap<String,PluginI>() ;

		assert	this.isPluginFacilitiesConfigured() :
					new RuntimeException("Plug-in facilities "
							+ "configuration not achieved correctly!") ;
	}

	/**
	 * return true if the plug-in facilities have been configured.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the plug-in facilities have been configured.
	 */
	protected boolean	isPluginFacilitiesConfigured()
	{
		return this.installedPlugins != null ;
	}

	/**
	 * unconfigure the plug-in facilities for this component, removing the
	 * offered interface, the inbound port and unpublish it.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isPluginFacilitiesConfigured()
	 * post	!this.isPluginFacilitiesConfigured()
	 * </pre>
	 *
	 * @throws Exception	<i>todo.</i>
	 */
	protected void		unConfigurePluginFacilitites() throws Exception
	{
		assert	this.isPluginFacilitiesConfigured() :
					new RuntimeException("Can't unconfigure plug-in "
								+ "facilities, they are not configured!") ;

		for (Entry<String,PluginI> e : this.installedPlugins.entrySet()) {
			((PluginI)e.getValue()).uninstall() ;
		}
		this.installedPlugins = null ;

		assert	!this.isPluginFacilitiesConfigured() :
					new RuntimeException("Plug-in facilities "
							+ "unconfiguration not achieved correctly!") ;
	}

	/**
	 * install a plug-in into this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!this.isInstalled(plugin.getPluginURI())
	 * post	this.isIntalled(plugin.getPluginURI())
	 * </pre>
	 *
	 * @param plugin		plug-in implementation object.
	 * @throws Exception	<i>todo.</i>
	 */
	protected void			installPlugin(
		PluginI plugin
		) throws Exception
	{
		assert	this.isPluginFacilitiesConfigured() :
					new RuntimeException("Can't install plug-in, "
							+ "plug-in facilities are not configured!") ;
		assert	!this.isInstalled(plugin.getPluginURI()) :
					new PreconditionException("Can't install plug-in, "
						+ plugin.getPluginURI() + " already installed!") ;

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PLUGIN)) {
			AbstractCVM.getCVM().logDebug(
						CVMDebugModes.PLUGIN,
						"Installing plug-in " + plugin.getPluginURI()) ;
		}

		((AbstractPlugin)plugin).installOn(this) ;
		this.installedPlugins.put(plugin.getPluginURI(), plugin) ;
		((AbstractPlugin)plugin).initialise() ;

		assert	this.isInstalled(plugin.getPluginURI()) :
					new PostconditionException("Plug-in "
						+ plugin.getPluginURI() + " not installed correctly!") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#hasInstalledPlugins()
	 */
	@Override
	public boolean		hasInstalledPlugins()
	{
		assert	this.isPluginFacilitiesConfigured() :
					new RuntimeException("Can't test, "
						+ "plug-in facilities are not configured!") ;

		return this.isPluginFacilitiesConfigured() &&
											!this.installedPlugins.isEmpty() ;
	}

	/**
	 * finalise the plug-in, at least when finalising the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginURI != null and this.isIntalled(pluginURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pluginURI		unique plug-in identifier.
	 * @throws Exception	<i>todo.</i>
	 */
	protected void		finalisePlugin(String pluginURI) throws Exception
	{
		assert	this.isPluginFacilitiesConfigured()  :
					new RuntimeException("Can't uninstall plug-in, "
							+ "plug-in facilities are not configured!") ;
		assert	pluginURI != null :
					new PreconditionException("Plug-in URI is null!") ;
		assert	this.isInstalled(pluginURI) :
					new PreconditionException("Can't uninstall plug-in, "
							+ pluginURI + " not installed!") ;

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PLUGIN)) {
			AbstractCVM.getCVM().logDebug(
									CVMDebugModes.PLUGIN,
									"Finalising plug-in " + pluginURI) ;
		}

		PluginI temp = this.installedPlugins.get(pluginURI) ;
		temp.finalise() ;
	}

	/**
	 * uninstall a plug-in from this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginURI != null and this.isIntalled(pluginURI)
	 * post	!this.isIntalled(pluginURI)
	 * </pre>
	 *
	 * @param pluginURI		unique plug-in identifier.
	 * @throws Exception	<i>todo.</i>
	 */
	protected void		uninstallPlugin(String pluginURI) throws Exception
	{
		assert	this.isPluginFacilitiesConfigured()  :
					new RuntimeException("Can't uninstall plug-in, "
								+ "plug-in facilities are not configured!") ;
		assert	pluginURI != null :
					new PreconditionException("Plug-in URI is null!") ;
		assert	this.isInstalled(pluginURI) :
					new PreconditionException("Can't uninstall plug-in, "
								+ pluginURI + " not installed!") ;

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PLUGIN)) {
			AbstractCVM.getCVM().logDebug(
									CVMDebugModes.PLUGIN,
									"Uninstalling plug-in " + pluginURI) ;
		}

		this.finalisePlugin(pluginURI) ;
		PluginI temp = this.installedPlugins.get(pluginURI) ;
		temp.uninstall() ;
		this.installedPlugins.remove(pluginURI) ;

		assert	!this.isInstalled(pluginURI) :
					new PostconditionException("Plug-in " + pluginURI
						+ " still installed after uninstalling!") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isInstalled(java.lang.String)
	 */
	@Override
	public boolean		isInstalled(String pluginURI)
	{
		assert	this.isPluginFacilitiesConfigured() :
					new RuntimeException("Can't test, "
						+ "plug-in facilities are not configured!") ;
		assert	pluginURI != null :
					new PreconditionException("Plug-in URI is null!") ;

		return this.installedPlugins != null &&
								this.installedPlugins.containsKey(pluginURI) ;
	}

	/**
	 * access a named plug-in into this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginURI != null
	 * pre	
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pluginURI	unique plug-in identifier.
	 * @return			the corresponding installed plug-in or null if none.
	 */
	protected PluginI	getPlugin(String pluginURI)
	{
		assert	this.isPluginFacilitiesConfigured() :
					new RuntimeException("Can't access plug-in, "
							+ "plug-in facilities are not configured!") ;
		assert	pluginURI != null :
					new PreconditionException("Plug-in URI is null!") ;

		return this.installedPlugins.get(pluginURI) ;
	}

	/**
	 * initialise the identified plug-in by adding to the owner component every
	 * specific information, ports, etc. required to run the plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginURI != null and !this.isInitialised(pluginURI)
	 * post	this.isInitialised(pluginURI)
	 * </pre>
	 *
	 * @param pluginURI		unique plug-in identifier.
	 * @throws Exception	<i>todo.</i>
	 */
	protected void		initialisePlugin(String pluginURI)
	throws Exception
	{
		assert	this.isPluginFacilitiesConfigured() :
					new RuntimeException("Can't access plug-in, "
							+ "plug-in facilities are not configured!") ;
		assert	pluginURI != null :
					new PreconditionException("Plug-in URI is null!") ;
		assert	!this.isInitialised(pluginURI) :
					new PreconditionException("Can't initialise plug-in "
						+ pluginURI + ", already initialised!")  ;

		this.installedPlugins.get(pluginURI).initialise() ;

		assert	this.isInitialised(pluginURI) :
					new PostconditionException("Plug-in " + pluginURI +
													" not initialised!") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isInitialised(java.lang.String)
	 */
	@Override
	public boolean		isInitialised(String pluginURI)
	throws Exception
	{
		assert	this.isPluginFacilitiesConfigured() :
					new RuntimeException("Can't test, "
							+ "plug-in facilities are not configured!") ;
		assert	pluginURI != null :
					new PreconditionException("Plug-in URI is null!") ;

		return this.installedPlugins.get(pluginURI).isInitialised() ;
	}

	// ------------------------------------------------------------------------
	// Logging and tracing facilities
	// ------------------------------------------------------------------------

	/**	The logger for this component.									*/
	protected Logger			executionLog ;
	/** The tracer for this component.									*/
	protected TracerOnConsole	tracer ;

	/**
	 * @see fr.sorbonne_u.components.ComponentI#setLogger(fr.sorbonne_u.components.helpers.Logger)
	 */
	@Override
	public void			setLogger(Logger logger)
	{
		this.executionLog = logger ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#logMessage(java.lang.String)
	 */
	@Override
	public void			logMessage(String message)
	{
		if (this.executionLog.isLogging()) {
			this.executionLog.logMessage(message) ;
		}
		if (this.tracer.isTracing()) {
			this.tracer.traceMessage(System.currentTimeMillis() + "|" +
									message + "\n") ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isLogging()
	 */
	@Override
	public boolean		isLogging()
	{
		if (this.executionLog == null) {
			return false ;
		} else {
			return this.executionLog.isLogging() ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#toggleLogging()
	 */
	@Override
	public void			toggleLogging()
	{
		this.executionLog.toggleLogging() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#printExecutionLog()
	 */
	@Override
	public void			printExecutionLog()
	{
		try {
			this.executionLog.printExecutionLog() ;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#printExecutionLogOnFile(java.lang.String)
	 */
	@Override
	public void			printExecutionLogOnFile(String fileName)
	{
		assert	fileName != null ;

		try {
			this.executionLog.printExecutionLogOnFile(fileName) ;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#toggleTracing()
	 */
	@Override
	public void			toggleTracing()
	{
		this.tracer.toggleTracing() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#setTracer(fr.sorbonne_u.components.helpers.TracerOnConsole)
	 */
	@Override
	public void			setTracer(TracerOnConsole tracer)
	{
		this.tracer = tracer ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#traceMessage(java.lang.String)
	 */
	@Override
	public void			traceMessage(String message)
	{
		if (this.tracer != null) {
			this.tracer.traceMessage(
							System.currentTimeMillis() + "|" + message) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isTracing()
	 */
	@Override
	public boolean		isTracing()
	{
		if (this.tracer == null) {
			return false ;
		} else {
			return this.tracer.isTracing() ;
		}
	}

	// ------------------------------------------------------------------------
	// Interfaces and ports information
	// ------------------------------------------------------------------------

	/** class objects representing all the required interfaces implemented
	 *  by this component.												*/
	protected Vector<Class<?>>					requiredInterfaces ;
	/** class objects representing all the offered interfaces implemented
	 * by this component.												*/
	protected Vector<Class<?>>					offeredInterfaces ;
	/** a hashtable mapping interfaces implemented by this component to
	 *  vectors of ports to which one can connect using these interfaces.	*/
	protected Hashtable<Class<?>,Vector<PortI>>	interfaces2ports ;
	/** a hashtable mapping URIs of ports owned by this component to
	 *  ports to which one can connect.									*/
	protected Hashtable<String,PortI>			portURIs2ports ;

	/**
	 * automatically declare the required and offered interface using the
	 * information given in the corresponding annotations.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	protected void		addInterfacesFromAnnotations()
	{
		RequiredInterfaces requiredAnnotation =
					this.getClass().getAnnotation(RequiredInterfaces.class) ;
		if (requiredAnnotation != null) {
			Class<? extends RequiredI>[] required =
											requiredAnnotation.required() ;
			if (required != null) {
				for (int i = 0 ; i < required.length ; i++) {
					this.addRequiredInterface(required[i]) ;
				}
			}
		}
		OfferedInterfaces offeredAnnotation =
					this.getClass().getAnnotation(OfferedInterfaces.class) ;
		if (offeredAnnotation != null) {
			Class<? extends OfferedI>[] offered = offeredAnnotation.offered() ;
			if (offered != null) {
				for (int i = 0 ; i < offered.length ; i++) {
					this.addOfferedInterface(offered[i]) ;
				}
			}
		}
	}

	/**
	 * automatically install and initialise plug-ins using the information
	 * given in the corresponding annotations.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 */
	protected void		addPluginsFromAnnotations()
	{
		assert	this.isPluginFacilitiesConfigured() :
					new RuntimeException("Can't install plug-ins, "
							+ "plug-in facilities are not configured!") ;

		try {
			AddPlugins pluginsAnnotation =
					this.getClass().getAnnotation(AddPlugins.class) ;
			if (pluginsAnnotation != null) {
				AddPlugin[] pluginAnnotations = pluginsAnnotation.pluginList() ;
				if (pluginAnnotations != null) {
					for (int i = 0 ; i < pluginAnnotations.length ; i++) {
						String uri = pluginAnnotations[i].pluginURI() ;
						Class<? extends PluginI> pluginClass =
								pluginAnnotations[i].pluginClass() ;
						PluginI p = pluginClass.newInstance();
						p.setPluginURI(uri) ;
						this.installPlugin(p) ;
						p.initialise() ;
					}
				}
			}
			AddPlugin pluginAnnotation =
								this.getClass().getAnnotation(AddPlugin.class) ;
			if (pluginAnnotation != null) {
				String uri = pluginAnnotation.pluginURI() ;
				Class<? extends PluginI> pluginClass =
											pluginAnnotation.pluginClass() ;
				PluginI p = pluginClass.newInstance() ;
				p.setPluginURI(uri) ;
				this.installPlugin(p) ;
				p.initialise() ;
			}
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	// ------------------------------------------------------------------------
	// Creation, constructors, invariant
	// ------------------------------------------------------------------------

	/** URI of the (unique) reflection inbound port of the component.		*/
	protected final String			reflectionInboundPortURI ;

	/**
	 * create a passive component if both <code>nbThreads</code> and
	 * <code>nbSchedulableThreads</code> are both zero, and an active one with
	 * <code>nbThreads</code> non schedulable thread and
	 * <code>nbSchedulableThreads</code> schedulable threads otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	nbThreads &gt;= 0 and nbSchedulableThreads &gt;= 0
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param nbThreads				number of threads to be created in the component pool.
	 * @param nbSchedulableThreads	number of threads to be created in the component schedulable pool.
	 */
	protected			AbstractComponent(
		int nbThreads,
		int nbSchedulableThreads
		)
	{
		this(AbstractPort.generatePortURI(ReflectionI.class),
											nbThreads, nbSchedulableThreads) ;
	}

	/**
	 * create a passive component if both <code>nbThreads</code> and
	 * <code>nbSchedulableThreads</code> are both zero, and an active one with
	 * <code>nbThreads</code> non schedulable thread and
	 * <code>nbSchedulableThreads</code> schedulable threads otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	reflectionInboundPortURI != null
	 * pre	nbThreads &gt;= 0
	 * pre	nbSchedulableThreads &gt;= 0
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the inbound port offering the <code>ReflectionI</code> interface.
	 * @param nbThreads					number of threads to be created in the component pool.
	 * @param nbSchedulableThreads		number of threads to be created in the component schedulable pool.
	 */
	protected			AbstractComponent(
		String reflectionInboundPortURI,
		int nbThreads,
		int nbSchedulableThreads
		)
	{
		assert	reflectionInboundPortURI != null :
					new PreconditionException("Reflection inbound port URI is"
																+ " null!") ;
		assert	nbThreads >= 0 :
					new PreconditionException("Number of threads is negative!") ;
		assert	nbSchedulableThreads >= 0 :
					new PreconditionException("Number of schedulable threads"
														+ " is negative!") ;

		this.reflectionInboundPortURI = reflectionInboundPortURI ;
		this.innerComponents = new Vector<ComponentI>() ;
		this.isConcurrent = false ;
		this.canScheduleTasks = false ;
		this.nbThreads = 0 ;
		this.nbSchedulableThreads = 0 ;
		this.executorServicesNextIndex = 0 ;
		this.executorServicesIndexes = new HashMap<String,Integer>() ;
		this.executorServices = new ArrayList<ComponentExecutorServiceManager>() ;
		this.requiredInterfaces = new Vector<Class<?>>() ;
		this.offeredInterfaces = new Vector<Class<?>>() ;
		this.interfaces2ports = new Hashtable<Class<?>,Vector<PortI>>() ;
		this.portURIs2ports = new Hashtable<String, PortI>() ;
		this.executionLog = new Logger(reflectionInboundPortURI) ;
		this.tracer = new TracerOnConsole(reflectionInboundPortURI, 0, 0) ;

		this.state = ComponentState.INITIALISED ;

		try {
			this.configurePluginFacilities() ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}

		this.nbThreads = nbThreads ;
		if (nbThreads > 0) {
			this.standardRequestHandlerIndex =
				this.createNewExecutorService(
										STANDARD_REQUEST_HANDLER_URI,
										nbThreads,
										false) ;
		} else {
			this.standardRequestHandlerIndex = -1 ;
		}
		
		this.nbSchedulableThreads = nbSchedulableThreads ;
		if (nbSchedulableThreads > 0) {
			this.standardSchedulableHandlerIndex =
				this.createNewExecutorService(
										STANDARD_SCHEDULABLE_HANDLER_URI,
										nbSchedulableThreads,
										true) ;
		} else {
			this.standardSchedulableHandlerIndex = -1 ;
		}

		try {
			this.configureReflection(reflectionInboundPortURI) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}

		this.addInterfacesFromAnnotations() ;
		this.addPluginsFromAnnotations() ;

		assert	AbstractComponent.checkInvariant(this) ;
	}

	/**
	 * configure the reflection offered interface and its inbound port;
	 * extensions of <code>AbstractComponent</code> that need to offer
	 * an extended reflection interface must redefine this method to
	 * offer the right reflection interface and create the right
	 * reflection inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	reflectionInboundPortURI != null
	 * post	this.isOfferedInterface(ReflectionI.class)
	 * post	this.findInboundPortURIsFromInterface(ReflectionI.class) != null
	 * post	this.findInboundPortURIsFromInterface(ReflectionI.class).length == 1
	 * post	this.findInboundPortURIsFromInterface(ReflectionI.class)[0].equals(reflectionInboundPortURI)
	 * </pre>
	 * 
	 * @param reflectionInboundPortURI	URI of the reflection inbound port to be created.
	 * @throws Exception				<i>TODO</i>.
	 */
	protected void		configureReflection(String reflectionInboundPortURI)
	throws Exception
	{
		assert	reflectionInboundPortURI != null ;

		this.addOfferedInterface(ReflectionI.class) ;
		try {
			ReflectionInboundPort rip =
					new ReflectionInboundPort(reflectionInboundPortURI, this) ;
			rip.publishPort() ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}

		assert	this.isOfferedInterface(ReflectionI.class) ;
		assert	this.findInboundPortURIsFromInterface(ReflectionI.class) != null ;
		assert	this.findInboundPortURIsFromInterface(ReflectionI.class).length == 1 ;
		assert	this.findInboundPortURIsFromInterface(ReflectionI.class)[0].equals(reflectionInboundPortURI) ; 
	}

	/**
	 * check the invariant of component objects.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	ac != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param ac	component object on which the invariant is checked.
	 * @return		true if the invariant holds, false otherwise.
	 */
	protected static boolean		checkInvariant(AbstractComponent ac)
	{
		assert	ac != null ;

		boolean ret = AbstractComponentHelper.isComponentClass(ac.getClass()) ;

		ret &= ac.innerComponents != null ;
		ret &= ac.isConcurrent == (ac.executorServices.size() > 0) ;
		ret &= ac.isConcurrent == (ac.nbThreads > 0 ||
								   ac.nbSchedulableThreads > 0 ||
								   ac.executorServices.size() > 0) ;
		ret &= ac.canScheduleTasks == (ac.hasUserDefinedSchedulableThreads()) ;
		ret &= ac.canScheduleTasks == (ac.nbSchedulableThreads > 0 ||
									  ac.hasUserDefinedSchedulableThreads()) ;
		ret &= ac.installedPlugins != null ;
		ret &= ac.executionLog != null || !ac.isLogging() ;
		ret &= ac.executionLog == null ||
							(ac.isLogging() == ac.executionLog.isLogging()) ;
		ret &= ac.requiredInterfaces != null ;
		ret &= ac.offeredInterfaces != null ;
		ret &= ac.interfaces2ports != null ;
		ret &= ac.portURIs2ports != null ;

		if (ret) {
			for (Class<?> inter : ac.interfaces2ports.keySet()) {
				ret &= ac.isInterface(inter) ;
			}
		}
		if (ret) {
			for (PortI p : ac.portURIs2ports.values()) {
				try {
					ret &= ac.isInterface(p.getImplementedInterface()) ;
					ret &= ac.interfaces2ports.get(
								p.getImplementedInterface()).contains(p) ;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return ret ;
	}

	/**
	 * create a component instantiated from the class of the given class name
	 * and initialised by the constructor which parameters are given.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * Due to the use of reflection to find the appropriate constructor in the
	 * component class, BCM does not currently apply the constructor selection
	 * rules that the Java compiler would apply. The actual parameters types
	 * must in fact match exactly the formal parameters ones. This is a common
	 * problem that Java software using reflection face when looking up
	 * constructors and methods. This forces to avoid sophisticated overriding
	 * of constructors in component classes and in their call sequences. When
	 * several constructors can apply, the first to be found is used rather
	 * than the most specific in compiled Java.
	 * </p>
	 * <p>
	 * If the <code>NoSuchMethodException</code> is thrown, it is likely that
	 * the match between the actual parameters types and the formal parameters
	 * ones has not been found by the current algorithm. Programmers must the
	 * try to change the types of the formal parameters to simplify the
	 * constructor selection.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	reflInboundPortURI != null and classname != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param classname			name of the class from which the component is created.
	 * @param constructorParams	parameters to be passed to the constructor.
	 * @return					the URI of the reflection inbound port of the new component.
	 * @throws Exception		if the creation did not succeed.
	 */
	public static String	createComponent(
		String classname,
		Object[] constructorParams
		) throws Exception
	{
		assert	classname != null && constructorParams != null ;

		ComponentI component =
				instantiateComponent(classname, constructorParams) ;
		String[] ret =
				component.findInboundPortURIsFromInterface(ReflectionI.class) ;
		assert	ret != null && ret.length == 1 && ret[0] != null ;

		AbstractCVM.getCVM().addDeployedComponent(ret[0], component) ;

		return ret[0] ;
	}

	/**
	 * instantiate a component instantiated from the class of the given class
	 * name and initialised by the constructor which parameters are given.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param classname			name of the class from which the component is created.
	 * @param constructorParams	parameters to be passed to the constructor.
	 * @return					the Java reference on the object representing the new component.
	 * @throws Exception		if the creation did not succeed.
	 */
	protected static ComponentI	instantiateComponent(
		String classname,
		Object[] constructorParams
		) throws Exception
	{
		Class<?> cl = Class.forName(classname) ;
		assert	cl != null && AbstractComponentHelper.isComponentClass(cl) ;
		Constructor<?> cons =
			AbstractComponentHelper.getConstructor(cl, constructorParams) ;
		assert	cons != null ;
		cons.setAccessible(true) ;
		AbstractComponent component =
			(AbstractComponent)cons.newInstance(constructorParams) ;
		return component ;
	}

	/**
	 * create a subcomponent instantiated from the class of the given class
	 * name and initialised by the constructor which parameters are given.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * Due to the use of reflection to find the appropriate constructor in the
	 * component class, BCM does not currently apply the constructor selection
	 * rules that the Java compiler would apply. The actual parameters types
	 * must in fact match exactly the formal parameters ones. This is a common
	 * problem that Java software using reflection face when looking up
	 * constructors and methods. This forces to avoid sophisticated overriding
	 * of constructors in component classes and in their call sequences. When
	 * several constructors can apply, the first to be found is used rather
	 * than the most specific in compiled Java.
	 * </p>
	 * <p>
	 * If the <code>NoSuchMethodException</code> is thrown, it is likely that
	 * the match between the actual parameters types and the formal parameters
	 * ones has not been found by the current algorithm. Programmers must the
	 * try to change the types of the formal parameters to simplify the
	 * constructor selection.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param classname			name of the class from which the component is created.
	 * @param constructorParams	parameters to be passed to the constructor.
	 * @return					the URI of the reflection inbound port of the new component.
	 * @throws Exception		if the creation did not succeed.
	 */
	protected String	createSubcomponent(
		String classname,
		Object[] constructorParams
		) throws Exception
	{
		assert	classname != null && constructorParams != null ;

		ComponentI component =
				instantiateComponent(classname, constructorParams) ;
		String[] ret =
				component.findInboundPortURIsFromInterface(ReflectionI.class) ;
		assert	ret != null && ret.length == 1 && ret[0] != null ;

		this.innerComponents.add(component) ;
		((AbstractComponent)component).setCompositeComponentReference(this) ;

		return ret[0] ;
	}

	// ------------------------------------------------------------------------
	// Internal behaviour requests
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isInStateAmong(fr.sorbonne_u.components.ComponentStateI[])
	 */
	@Override
	public boolean		isInStateAmong(ComponentStateI[] states)
	{
		assert	states != null :
					new PreconditionException("State array can't be null!") ;

		boolean ret = false ;
		for (int i = 0 ; !ret && i < states.length ; i++) {
			ret = (this.state == states[i]) ;
		}
		return ret ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#notInStateAmong(fr.sorbonne_u.components.ComponentStateI[])
	 */
	@Override
	public boolean		notInStateAmong(ComponentStateI[] states)
	{
		assert	states != null :
					new PreconditionException("State array can't be null!") ;

		boolean ret = true ;
		for (int i = 0 ; ret && i < states.length ; i++) {
			ret = (this.state != states[i]) ;
		}
		return ret ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#hasItsOwnThreads()
	 */
	@Override
	public boolean		hasItsOwnThreads()
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
										+ " in Terminated state!") ;

		return this.isConcurrent ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getTotalNumberOfThreads()
	 */
	@Override
	public int			getTotalNumberOfThreads()
	{
		int nbUserDefinedThreads = 0 ;
		for (int i = 0 ; i < this.executorServices.size() ; i++) {
			nbUserDefinedThreads +=
					this.executorServices.get(i).getNumberOfThreads() ;
		}
		return this.nbThreads + this.nbSchedulableThreads +
											nbUserDefinedThreads ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#hasSerialisedExecution()
	 */
	@Override
	public boolean		hasSerialisedExecution()
	{
		return this.hasItsOwnThreads() &&
							this.getTotalNumberOfThreads() == 1 ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#canScheduleTasks()
	 */
	@Override
	public boolean		canScheduleTasks()
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
										+ " in Terminated state!") ;

		return this.canScheduleTasks ;
	}

	// ------------------------------------------------------------------------
	// Implemented interfaces
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getInterfaces()
	 */
	@Override
	public Class<?>[]	getInterfaces()
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
										+ " in Terminated state!") ;

		ArrayList<Class<?>> temp = new ArrayList<Class<?>>() ;
		synchronized (this.requiredInterfaces) {
			temp.addAll(this.requiredInterfaces) ;
		}
		synchronized (this.offeredInterfaces) {
			temp.addAll(this.offeredInterfaces) ;
		}
		return temp.toArray(new Class<?>[]{}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
										+ " in Terminated state!") ;

		Class<?> ret = this.getRequiredInterface(inter) ;
		if (ret == null) {
			ret = this.getOfferedInterface(inter) ;
		}
		return ret ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getRequiredInterfaces()
	 */
	@Override
	public Class<?>[]	getRequiredInterfaces()
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
										+ " in Terminated state!") ;

		Class<?>[] ret ;
		synchronized (this.requiredInterfaces) {
			ret = this.requiredInterfaces.toArray(new Class<?>[]{}) ;
		}
		return ret ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getRequiredInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getRequiredInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
										+ " in Terminated state!") ;

		Class<?> ret = null ;
		boolean found = false ;
		for(int i = 0 ; !found && i < this.requiredInterfaces.size() ; i++) {
			if (inter.isAssignableFrom(this.requiredInterfaces.get(i))) {
				found = true ;
				ret = this.requiredInterfaces.get(i) ;
			}
		}
		return ret ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getOfferedInterfaces()
	 */
	@Override
	public Class<?>[]	getOfferedInterfaces()
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
										+ " in Terminated state!") ;

		Class<?>[] ret ;
		synchronized (this.offeredInterfaces) {
			ret = this.offeredInterfaces.toArray(new Class<?>[]{}) ;
		}
		return ret ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getOfferedInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getOfferedInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
										+ " in Terminated state!") ;

		Class<?> ret = null ;
		boolean found = false ;
		for(int i = 0 ; !found && i < this.offeredInterfaces.size() ; i++) {
			if (inter.isAssignableFrom(this.offeredInterfaces.get(i))) {
				found = true ;
				ret = this.offeredInterfaces.get(i) ;
			}
		}
		return ret ;
	}

	/**
	 * add a required interface to the required interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	RequiredI.class.isAssignableFrom(inter)
	 * pre	!this.isRequiredInterface(inter)
	 * post	this.isRequiredInterface(inter)
	 * </pre>
	 *
	 * @param inter	required interface to be added.
	 */
	protected void			addRequiredInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	RequiredI.class.isAssignableFrom(inter) :
					new PreconditionException(inter +
								" is not defined as a required interface!") ;
		assert	!this.isRequiredInterface(inter) :
					new PreconditionException(inter + " is already a"
												+ " required interface!") ;

		synchronized (this.requiredInterfaces) {
			this.requiredInterfaces.add(inter) ;
		}

		assert	this.isRequiredInterface(inter) :
					new PostconditionException(inter + " has not been "
							+ "correctly added as a required interface!") ;
	}

	/**
	 * remove a required interface from the required interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	this.isRequiredInterface(inter)
	 * pre	this.findPortsFromInterface(inter) == null || this.findPortsFromInterface(inter).isEmpty()
	 * post	!this.isRequiredInterface(inter)
	 * </pre>
	 *
	 * @param inter required interface to be removed.
	 */
	protected void			removeRequiredInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	RequiredI.class.isAssignableFrom(inter) :
					new PreconditionException(inter +
								" is not defined as a required interface!") ;
		assert	this.isRequiredInterface(inter) :
					new PreconditionException(inter + " is not a"
										+ " declared required interface!") ;

		synchronized (this.requiredInterfaces) {
			this.requiredInterfaces.remove(inter) ;
		}

		assert	!this.isRequiredInterface(inter) :
					new PostconditionException(inter + " has not been "
							+ "correctly removed as a required interface!") ;
	}

	/**
	 * add an offered interface to the offered interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	OfferedI.class.isAssignableFrom(inter)
	 * pre	!this.isOfferedInterface(inter)
	 * post	this.isOfferedInterface(inter)
	 * </pre>
	 *
	 * @param inter offered interface to be added.
	 */
	protected void		addOfferedInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	OfferedI.class.isAssignableFrom(inter) :
					new PreconditionException(inter +
								" is not defined as an offered interface!") ;
		assert	!this.isOfferedInterface(inter) :
					new PreconditionException(inter + " must not be a"
										+ " declared offered interface!") ;

		synchronized (this.offeredInterfaces) {
			this.offeredInterfaces.add(inter) ;
		}

		assert	this.isOfferedInterface(inter) :
					new PostconditionException(inter + " has not been "
								+ "correctly added as an offered interface!") ;
	}

	/**
	 * remove an offered interface from the offered interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	this.isOfferedInterface(inter)
	 * pre	this.findPortsFromInterface(inter) == null || this.findPortsFromInterface(inter).isEmpty()
	 * post	!this.isOfferedInterface(inter)
	 * </pre>
	 *
	 * @param inter	offered interface to be removed
	 */
	protected void		removeOfferedInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	OfferedI.class.isAssignableFrom(inter) :
					new PreconditionException(inter +
								" is not defined as an offered interface!") ;
		assert	this.isOfferedInterface(inter) :
					new PreconditionException(inter + " is not a"
										+ " declared offered interface!") ;

		synchronized (this.offeredInterfaces) {
			this.offeredInterfaces.remove(inter) ;
		}

		assert	!this.isOfferedInterface(inter) :
					new PostconditionException(inter + " has not been "
							+ "correctly removed as an offered interface!") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isInterface(java.lang.Class)
	 */
	@Override
	public boolean		isInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;

		return this.isRequiredInterface(inter) ||
											this.isOfferedInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isRequiredInterface(java.lang.Class)
	 */
	@Override
	public boolean		isRequiredInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;

		synchronized (this.requiredInterfaces) {
			boolean ret = false ;
			for(int i = 0 ; !ret && i < this.requiredInterfaces.size() ; i++) {
				if (inter.isAssignableFrom(this.requiredInterfaces.get(i))) {
					ret = true ;
				}
			}
			return ret ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isOfferedInterface(java.lang.Class)
	 */
	@Override
	public boolean		isOfferedInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;

		synchronized (this.offeredInterfaces) {
			boolean ret = false ;
			for(int i = 0 ; !ret && i < this.offeredInterfaces.size() ; i++) {
				if (inter.isAssignableFrom(this.offeredInterfaces.get(i))) {
					ret = true ;
				}
			}
			return ret ;
		}
	}

	// ------------------------------------------------------------------------
	// Port management
	//
	//   Port objects are implementation artifacts for components and must
	//   not be manipulated (referenced) outside their owner component.
	//   Port URIs are used to designate ports most of the time. The only
	//   exceptions in the model are plug-ins, which are meant to extend the
	//   internal behaviour of components and as such can manipulate ports.
	//   Hence, methods that directly manipulate port objects are protected
	//   while the ones manipulating port URIs are public.
	//
	// ------------------------------------------------------------------------

	/**
	 * find the ports of this component that expose the interface inter.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	inter != null
	 * post	return == null || forall(PortI p : return) { inter.equals(p.getImplementedInterface()) }
	 * </pre>
	 *
	 * @param inter	interface for which ports are sought.
	 * @return		array of ports exposing inter.
	 */
	protected PortI[]	findPortsFromInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	inter != null :
					new PreconditionException("Interface is null!") ;

		PortI[] ret = null ;
		Vector<PortI> temp ;

		synchronized (this.interfaces2ports) {
			temp = this.interfaces2ports.get(inter) ;
		}
		if (temp != null) {
			synchronized (temp) {
				ret = temp.toArray(new PortI[]{}) ;
			}
		}
		return ret ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getPortImplementedInterface(java.lang.String)
	 */
	@Override
	public Class<?>		getPortImplementedInterface(String portURI)
	throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	portURI != null :
					new PreconditionException("Port URI is null!") ;
		assert	this.isPortExisting(portURI) :
					new PreconditionException(portURI + " is not a port!") ;

		return this.findPortFromURI(portURI).getImplementedInterface() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#findPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findPortURIsFromInterface(Class<?> inter)
	throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	inter != null :
					new PreconditionException("Interface is null!") ;

		String[] ret = null ;
		PortI[] ports = this.findPortsFromInterface(inter) ;
		if (ports != null && ports.length > 0) {
			ret = new String[ports.length] ;
			for (int i = 0 ; i < ports.length ; i++) {
				ret[i] = ports[i].getPortURI() ;
			}
		}
		return ret ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#findInboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findInboundPortURIsFromInterface(Class<?> inter)
	throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	inter != null :
					new PreconditionException("Interface is null!") ;

		String[] ret = null ;

		PortI[] ports = this.findPortsFromInterface(inter) ;
		if (ports != null && ports.length > 0) {
			ArrayList<String> al = new ArrayList<String>() ;
			for (int i = 0 ; i < ports.length ; i++) {
				if (ports[i] instanceof InboundPortI) {
					al.add(ports[i].getPortURI()) ;
				}
			}
			ret = al.toArray(new String[0]) ;
		}
		return ret ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#findOutboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findOutboundPortURIsFromInterface(Class<?> inter)
	throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	inter != null :
					new PreconditionException("Interface is null!") ;

		String[] ret = null ;

		PortI[] ports = this.findPortsFromInterface(inter) ;
		if (ports != null && ports.length > 0) {
			ArrayList<String> al = new ArrayList<String>() ;
			for (int i = 0 ; i < ports.length ; i++) {
				if (ports[i] instanceof OutboundPortI) {
					al.add(ports[i].getPortURI()) ;
				}
			}
			ret = al.toArray(new String[0]) ;
		}
		return ret ;
	}

	/**
	 * finds a port of this component from its URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	portURI != null
	 * post	return == null || return.getPortURI().equals(portURI)
	 * </pre>
	 *
	 * @param portURI	the URI a the sought port.
	 * @return			the port with the given URI or null if not found.
	 */
	protected PortI		findPortFromURI(String portURI)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	portURI != null :
					new PreconditionException("Port URI is null!") ;

		synchronized (this.portURIs2ports) {
			return this.portURIs2ports.get(portURI) ;
		}
	}

	/**
	 * add a port to the set of ports of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	p != null
	 * pre	this.equals(p.getOwner())
	 * pre	this.isInterface(p.getImplementedInterface())
	 * pre	this.findPortFromURI(p.getPortURI()) == null
	 * post p.equals(this.findPortFromURI(p.getPortURI()))
	 * </pre>
	 *
	 * @param p				port to be added.
	 * @throws Exception	<i>todo.</i>
	 */
	protected void		addPort(PortI p) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	p != null : new PreconditionException("p is null!") ;
		assert	this.equals(p.getOwner()) :
					new PreconditionException("This component is not the"
												+ " owner of this port!") ;
		assert	this.isInterface(p.getImplementedInterface()) :
					new PreconditionException("The port doesn't implement"
							+ " an inteface declared by this component!");
		assert	this.findPortFromURI(p.getPortURI()) == null :
					new RuntimeException("A port with the same URI is"
								+ " already registered in this component!") ;

		Vector<PortI> vps = null ;
		synchronized (this.interfaces2ports) {
			vps = this.interfaces2ports.get(p.getImplementedInterface()) ;
			if (vps == null) {
				vps = new Vector<PortI>() ;
				vps.add(p) ;
				this.interfaces2ports.put(p.getImplementedInterface(), vps) ;
			} else {
				synchronized (vps) {
					vps.add(p) ;
				}
			}
		}
		synchronized (this.portURIs2ports) {
			this.portURIs2ports.put(p.getPortURI(), p) ;
		}

		assert	this.interfaces2ports.containsKey(p.getImplementedInterface()) :
					new PostconditionException("Port not correctly registered!") ;
		assert	this.portURIs2ports.containsKey(p.getPortURI()) :
					new PostconditionException("Port not correctly registered!") ;
		assert	p.equals(this.findPortFromURI(p.getPortURI())) :
					new PostconditionException("Port not correctly registered!") ;
	}

	/**
	 * remove a port from the set of ports of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	p != null
	 * pre	this.equals(p.getOwner())
	 * pre	exist(PortI p1 : this.findPortsFromInterface(p.getImplementedInterface())) { p1.equals(p)) ; }
	 * post	!exist(PortI p1 : this.findPortsFromInterface(p.getImplementedInterface())) { p1.equals(p)) ; }
	 * </pre>
	 *
	 * @param p				port to be removed.
	 * @throws Exception	<i>todo.</i>
	 */
	protected void		removePort(PortI p) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
											+ " in Terminated state!") ;
		assert	p != null : new PreconditionException("p is null!") ;
		assert	this.equals(p.getOwner()) :
					new PreconditionException("This component is not the"
													+ " owner of this port!") ;
		assert	this.interfaces2ports.containsKey(p.getImplementedInterface()) :
					new PreconditionException("Port is not registered "
													+ "in this component!") ;
		assert	this.portURIs2ports.containsKey(p.getPortURI()) :
					new PreconditionException("Port is not registered "
													+ "in this component!") ;

		synchronized (this.interfaces2ports) {
			Vector<PortI> vps =
				this.interfaces2ports.get(p.getImplementedInterface()) ;
			synchronized (vps) {
				vps.remove(p) ;
				if (vps.isEmpty()) {
					this.interfaces2ports.remove(p.getImplementedInterface()) ;
				}
			}
		}
		synchronized (this.portURIs2ports) {
			this.portURIs2ports.remove(p.getPortURI()) ;
		}

		assert	!this.portURIs2ports.containsKey(p.getPortURI()) :
					new PostconditionException("Port not correctly removed "
														+ "from component!") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isPortExisting(java.lang.String)
	 */
	@Override
	public boolean		isPortExisting(String portURI) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	portURI != null : new PreconditionException("p is null!") ;

		PortI p = this.findPortFromURI(portURI) ;
		return p != null ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isPortConnected(java.lang.String)
	 */
	@Override
	public boolean		isPortConnected(String portURI) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	portURI != null :
					new PreconditionException("Port URI is null!") ;
		assert	this.isPortExisting(portURI) :
					new PreconditionException(portURI + " is not a port!") ;

		PortI p = this.findPortFromURI(portURI) ;
		return p.connected() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#doPortConnection(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void			doPortConnection(
		String portURI,
		String otherPortURI,
		String ccname
		) throws Exception 
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	portURI != null :
					new PreconditionException("Port URI is null!") ;
		assert	otherPortURI != null :
					new PreconditionException("Other port URI is null!") ;
		assert	ccname != null :
					new PreconditionException("Connector class name is null!") ;
		assert	this.isPortExisting(portURI) :
					new PreconditionException(portURI + " is not a port!") ;
		assert	!this.isPortConnected(portURI) :
					new PreconditionException(portURI + " is already "
															+ "connected!") ;

		PortI p = this.findPortFromURI(portURI) ;
		p.doConnection(otherPortURI, ccname) ;

		assert	this.isPortConnected(portURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#doPortConnection(java.lang.String, java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			doPortConnection(
		String portURI,
		String otherPortURI,
		ConnectorI connector
		) throws Exception 
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	portURI != null :
					new PreconditionException("Port URI is null!") ;
		assert	otherPortURI != null :
					new PreconditionException("Other port URI is null!") ;
		assert	connector != null :
					new PreconditionException("Connector is null!") ;
		assert	this.isPortExisting(portURI) :
					new PreconditionException(portURI + " is not a port!") ;
		assert	!this.isPortConnected(portURI) :
					new PreconditionException(portURI + " is already "
															+ "connected!") ;

		PortI p = this.findPortFromURI(portURI) ;
		p.doConnection(otherPortURI, connector) ;

		assert	this.isPortConnected(portURI) ;
	}

	/**
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.ComponentI#doPortDisconnection(java.lang.String)
	 */
	@Override
	public void			doPortDisconnection(String portURI) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	portURI != null :
					new PreconditionException(
										"Can't disconnect null port URI!") ;
		assert	this.isPortExisting(portURI) :
					new PreconditionException(
							"Can't disconnect non existing port : " + portURI) ;
		assert	this.isPortConnected(portURI) :
					new PreconditionException(
						"Can't disconnect not connected port : " + portURI) ;
	
		PortI p = this.findPortFromURI(portURI) ;
		p.doDisconnection() ;

		assert	!this.isPortConnected(portURI) :
					new PostconditionException("Port has not been "
											+ "correctly disconnected!") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#removePort(java.lang.String)
	 */
	@Override
	public void			removePort(String portURI) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!") ;
		assert	portURI != null :
			new PreconditionException("Can't remove undefined port URI : "
																+ portURI) ;
		assert	this.isPortExisting(portURI) :
					new PreconditionException("Can't remove non existing port : "
																+ portURI) ;

		PortI p = this.findPortFromURI(portURI) ;
		this.removePort(p) ;

		assert	!this.isPortExisting(portURI) :
					new PostconditionException("Pourt has not been "
												+ "correctly removed!") ;
	}

	// ------------------------------------------------------------------------
	// Component life cycle
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ComponentI#start()
	 */
	@Override
	public void			start() throws ComponentStartException
	{
		assert	this.isInitialised() ;

		// Start inner components
		// assumes that the creation and publication are done
		// assumes that composite components always reside in one JVM
		for(ComponentI c : this.innerComponents) {
			c.start() ;
		}

		this.state = ComponentState.STARTED ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		assert	this.isStarted() ;	

		for(ComponentI c : this.innerComponents) {
			if (c.hasItsOwnThreads()) {
				c.runTask(
					new AbstractTask() {
						@Override
						public void run() {
							try {
								this.getTaskOwner().execute() ;
							} catch (Exception e) {
								throw new RuntimeException(e) ;
							}
						}
					}) ;
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		assert	this.isStarted() :
					new PreconditionException("AbstractComponent#finalise: "
												+ this + " not started!") ;

		for(ComponentI c : this.innerComponents) {
			c.finalise() ;
		}

		for (Map.Entry<String,PluginI> e : this.installedPlugins.entrySet()) {			
			this.finalisePlugin(e.getKey()) ;
		}
		String[] reflPortURI =
				this.findInboundPortURIsFromInterface(ReflectionI.class) ;
		PortI reflPort = this.findPortFromURI(reflPortURI[0]) ;
		reflPort.unpublishPort() ;

		this.state = ComponentState.FINALISED ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		assert	this.isFinalised() ;

		// Shutdown inner components
		// assumes that all inner components are disconnected.
		for(ComponentI c : this.innerComponents) {
			c.shutdown() ;
		}

		try {
			if (this.isPluginFacilitiesConfigured()) {
				this.unConfigurePluginFacilitites() ;
			}
			ArrayList<PortI> toBeDestroyed =
					new ArrayList<PortI>(this.portURIs2ports.values()) ;
			for (PortI p : toBeDestroyed) {
				p.destroyPort() ;
			}
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}

		boolean isSubcomponent = this.isSubcomponent() ;
		Thread t = new Thread() {
						/**
						 * @see java.lang.Thread#run()
						 */
						@Override
						public void run() {
							for (int i = 0 ; i < executorServices.size() ; i++) {
								executorServices.get(i).shutdown() ;
							}
							state = ComponentState.SHUTTINGDOWN ;
							// TODO: make sure the pools are really shut down.
							if (!isConcurrent && !canScheduleTasks) {
								state = ComponentState.SHUTDOWN ;
							}
							if (!isSubcomponent) {
								AbstractCVM.getCVM().
									removeDeployedComponent(
													reflectionInboundPortURI) ;
							}
						}
					} ;
		t.start() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#shutdownNow()
	 */
	@Override
	public void			shutdownNow() throws ComponentShutdownException
	{
		assert	this.isFinalised() ;

		// Shutdown inner components
		// assumes that all inner components are disconnected.
		for(ComponentI c : this.innerComponents) {
			c.shutdownNow() ;
		}

		try {
			if (this.isPluginFacilitiesConfigured()) {
				this.unConfigurePluginFacilitites() ;
			}
			for (PortI p : this.portURIs2ports.values()) {
				p.destroyPort() ;
			}
		} catch (Exception e1) {
			throw new ComponentShutdownException(e1) ;
		}

		boolean isSubcomponent = this.isSubcomponent() ;
		Thread t = new Thread() {
						/**
						 * @see java.lang.Thread#run()
						 */
						@Override
						public void run() {
							for (int i = 0 ; i < executorServices.size() ; i++) {
								executorServices.get(i).shutdown() ;
							}
							state = ComponentState.SHUTDOWN ;
							if (!isSubcomponent) {
								AbstractCVM.getCVM().
										removeDeployedComponent(
													reflectionInboundPortURI) ;
							}
						}
					} ;
		t.start() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isInitialised()
	 */
	@Override
	public boolean		isInitialised()
	{
		return this.state == ComponentState.INITIALISED ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isStarted()
	 */
	@Override
	public boolean		isStarted()
	{
		return this.state == ComponentState.STARTED ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isFinalised()
	 */
	@Override
	public boolean		isFinalised()
	{
		return this.state == ComponentState.FINALISED ;
	}

	public boolean		isShuttingDown()
	{
		return this.state == ComponentState.SHUTTINGDOWN ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isShutdown()
	 */
	@Override
	public boolean		isShutdown()
	{
		boolean isShutdown = true ;

		if (this.state == ComponentState.SHUTDOWN) {
			return true ;
		}

		if (this.executorServices.size() > 0) {
			for (int i = 0 ; i < this.executorServices.size() ; i++) {
				isShutdown = isShutdown &&
								this.executorServices.get(i).isShutdown() ;
			}
		}
		if (isShutdown) {
			this.state = ComponentState.SHUTDOWN ;
		}
		return isShutdown ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isTerminated()
	 */
	@Override
	public boolean		isTerminated()
	{
		boolean isTerminated = true ;

		if (this.state == ComponentState.TERMINATED) {
			return true ;
		}

		if (this.isConcurrent) {
			for (int i = 0 ; i < this.executorServices.size() ; i++) {
				isTerminated = isTerminated &&
								this.executorServices.get(i).isTerminated() ;
			}
		} else {
			isTerminated = this.isShutdown() ;
		}
		if (isTerminated) {
			this.state = ComponentState.TERMINATED ;
		}
		return isTerminated ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#awaitTermination(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public boolean		awaitTermination(long timeout, TimeUnit unit)
	throws	InterruptedException
	{
		if (this.state == ComponentState.TERMINATED) {
			return true ;
		}

		boolean status = true ;
		if (this.executorServices.size() > 0) {
			for (int i = 0 ; i < this.executorServices.size() ; i++) {
				status = status &&
							this.executorServices.get(i).awaitTermination(
															timeout, unit) ;
			}
		}
		if (status) {
			this.state = ComponentState.TERMINATED ;
		}
		return status ;
	}

	// ------------------------------------------------------------------------
	// Task execution
	// ------------------------------------------------------------------------

	/**
	 * The abstract class <code>AbstractTask</code> provides the basic
	 * method implementations for component tasks.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2018-09-18</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static abstract class	AbstractTask
	implements	ComponentI.ComponentTask
	{
		protected AbstractComponent	taskOwner ;
		protected final String		taskPluginURI ;
		protected Object			taskPlugin ;

		/**
		 * create a task which uses the owner component method only.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 */
		public				AbstractTask()
		{
			super() ;
			this.taskPluginURI = null ;
			this.taskPlugin = null ;
		}

		/**
		 * create a task which uses both the owner component method and
		 * methods of its designated plug-in.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	pluginURI != null
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param pluginURI	URI of a plug-in installed on the owner.
		 */
		public				AbstractTask(String pluginURI)
		{
			super();
			assert	pluginURI != null ;
			this.taskPluginURI = pluginURI;
			this.taskPlugin = null ;
		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentTask#setOwnerReference(fr.sorbonne_u.components.ComponentI)
		 */
		@Override
		public void			setOwnerReference(ComponentI owner)
		{
			assert	owner != null ;
			assert	owner instanceof AbstractComponent ;

			try {
				assert this.taskPluginURI == null ||
										owner.isInstalled(taskPluginURI) ;
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}

			this.taskOwner = (AbstractComponent) owner ;
			if (this.taskPluginURI != null) {
				this.taskPlugin =
						this.taskOwner.getPlugin(this.taskPluginURI) ;
			}

		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentTask#getTaskOwner()
		 */
		@Override
		public AbstractComponent	getTaskOwner()
		{
			return this.taskOwner ;
		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentTask#getTaskProviderReference()
		 */
		@Override
		public Object		getTaskProviderReference()
		{
			if (this.taskPluginURI == null) {
				return this.taskOwner ;
			} else {
				return this.taskPlugin ;
			}
		}

		/**
		 * run a lambda expression as a task, providing it the owner as
		 * parameter.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	t != null
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param t			lambda defining the task to be executed.
		 */
		protected void		runTaskLambda(FComponentTask t)
		{
			assert	t != null ;

			t.run(this.getTaskOwner()) ;
		}
	}

	/**
	 * run the <code>ComponentTask</code> on the given executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	t != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceIndex			index of the executor service that will run the task.
	 * @param t								component task to be executed as main task.
	 * @return								a future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected Future<?>		runTaskOnComponent(
		int executorServiceIndex,
		ComponentTask t
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	t != null ;

		t.setOwnerReference(this) ;
		if (this.hasItsOwnThreads() &&
						this.validExecutorServiceIndex(executorServiceIndex)) {
			return this.executorServices.get(executorServiceIndex).
												getExecutorService().submit(t) ;
		} else {
			t.run() ;
			Future<?> f =
				new Future<Object>() {
						@Override
						public boolean	cancel(boolean maybeInterrupted)
						{ return false ; }

						@Override
						public Object	get()
						throws	InterruptedException, ExecutionException
						{ return null ; }

						@Override
						public Object get(long timeout, TimeUnit unit)
						throws 	InterruptedException,
								ExecutionException,
							   	TimeoutException
						{ return null ; }

						@Override
						public boolean	isCancelled()
						{ return false ; }

						@Override
						public boolean	isDone()
						{ return true ; }
					} ;
					return f ;
		}
	}

	/**
	 * run the <code>ComponentTask</code> on the given executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.validExecutorServiceURI(executorServiceURI)
	 * pre	t != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceURI			URI of the executor service that will run the task.
	 * @param t								component task to be executed as main task.
	 * @return								a future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected Future<?>		runTaskOnComponent(
		String executorServiceURI,
		ComponentTask t
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceURI(executorServiceURI) ;
		assert	t != null ;

		int executorServiceIndex =
						this.getExecutorServiceIndex(executorServiceURI) ;
		return this.runTaskOnComponent(executorServiceIndex, t) ;
	}

	/**
	 * run the <code>ComponentTask</code> on the standard executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	t != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param t								component task to be executed as main task.
	 * @return								a future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected Future<?>		runTaskOnComponent(ComponentTask t)
	throws	AssertionError,
			RejectedExecutionException
	{
		if (this.hasItsOwnThreads()) {
			if (this.validExecutorServiceIndex(
											this.standardRequestHandlerIndex)) {
				return this.runTaskOnComponent(
									this.standardRequestHandlerIndex, t) ;
			} else {
				assert	this.validExecutorServiceIndex(
										this.standardSchedulableHandlerIndex) ;
				return this.runTaskOnComponent(
									this.standardSchedulableHandlerIndex, t) ;
			}
		} else {
			return this.runTaskOnComponent(-1, t) ;
		}			
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#runTask(fr.sorbonne_u.components.ComponentI.ComponentTask)
	 */
	@Override
	public void 		runTask(ComponentTask t)
	throws	AssertionError,
			RejectedExecutionException
	{
		this.runTaskOnComponent(t) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#runTask(fr.sorbonne_u.components.ComponentI.FComponentTask)
	 */
	@Override
	public void 		runTask(FComponentTask t)
	throws	AssertionError,
			RejectedExecutionException
	{
		this.runTask(new AbstractTask() {
						@Override
						public void run() { this.runTaskLambda(t); }
		 			 }) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#runTask(int, fr.sorbonne_u.components.ComponentI.ComponentTask)
	 */
	@Override
	public void			runTask(int executorServiceIndex, ComponentTask t)
	throws	AssertionError,
			RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceIndex(executorServiceIndex) ;
		assert	t != null ;

		this.runTaskOnComponent(executorServiceIndex, t) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#runTask(java.lang.String, fr.sorbonne_u.components.ComponentI.ComponentTask)
	 */
	@Override
	public void			runTask(
		String executorServiceURI,
		ComponentTask t
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceURI(executorServiceURI) ;
		assert	t != null ;

		int executorServiceIndex =
						this.getExecutorServiceIndex(executorServiceURI) ;
		this.runTask(executorServiceIndex, t) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#runTask(java.lang.String, fr.sorbonne_u.components.ComponentI.FComponentTask)
	 */
	@Override
	public void			runTask(
		String executorServiceURI,
		FComponentTask t
		) throws	AssertionError,
					RejectedExecutionException
	{
		this.runTask(executorServiceURI, 
					 new AbstractTask() {
						@Override
						public void run() { this.runTaskLambda(t); }
					 }) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#runTask(int, fr.sorbonne_u.components.ComponentI.FComponentTask)
	 */
	@Override
	public void			runTask(int executorServiceIndex, FComponentTask t)
	throws	AssertionError,
			RejectedExecutionException
	{
		this.runTask(executorServiceIndex,
					 new AbstractTask() {
						@Override
						public void run() { this.runTaskLambda(t); }
					 });
	}

	/**
	 * schedule a <code>ComponentTask</code> to be run after a given delay
	 * on the given executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.validExecutorServiceIndex(executorServiceIndex)
	 * pre	this.isSchedulable(executorServiceIndex)
	 * pre	{@code t != null and delay > 0 and u != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceIndex			index of the executor service that will run the task.
	 * @param t								task to be scheduled.
	 * @param delay							delay after which the task must be run.
	 * @param u								time unit in which the delay is expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskOnComponent(
		int executorServiceIndex,
		ComponentTask t,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceIndex(executorServiceIndex) ;
		assert	this.isSchedulable(executorServiceIndex) ;
		assert	t != null && delay > 0 && u != null ;

		t.setOwnerReference(this) ;
		return ((ComponentSchedulableExecutorServiceManager)
					this.executorServices.get(executorServiceIndex)).
						getScheduledExecutorService().schedule(t, delay, u) ;
	}

	/**
	 * schedule a <code>ComponentTask</code> to be run after a given delay
	 * on the given executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.validExecutorServiceURI(executorServiceURI)
	 * pre	this.isSchedulable(executorServiceIndex)
	 * pre	{@code t != null and delay > 0 and u != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceURI			URI of the executor service that will run the task.
	 * @param t								task to be scheduled.
	 * @param delay							delay after which the task must be run.
	 * @param u								time unit in which the delay is expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskOnComponent(
		String executorServiceURI,
		ComponentTask t,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceURI(executorServiceURI) ;
		assert	t != null ;

		int executorServiceIndex =
							this.getExecutorServiceIndex(executorServiceURI) ;
		return this.scheduleTaskOnComponent(executorServiceIndex, t, delay, u) ;
	}

	/**
	 * schedule a <code>ComponentTask</code> to be run after a given delay
	 * on the given executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.canScheduleTasks()
	 * pre	this.validExecutorServiceIndex(this.standardSchedulableHandlerIndex)
	 * pre	{@code t != null and delay > 0 and u != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param t								task to be scheduled.
	 * @param delay							delay after which the task must be run.
	 * @param u								time unit in which the delay is expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskOnComponent(
		ComponentTask t,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.canScheduleTasks() ;
		assert	this.validExecutorServiceIndex(
										this.standardSchedulableHandlerIndex) ;

		return this.scheduleTaskOnComponent(
							this.standardSchedulableHandlerIndex, t, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTask(fr.sorbonne_u.components.ComponentI.ComponentTask, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTask(
		ComponentTask t,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.canScheduleTasks() ;
		assert	this.validExecutorServiceIndex(
									this.standardSchedulableHandlerIndex) ;
		assert	t != null && delay >= 0 && u != null ;

		this.scheduleTask(this.standardSchedulableHandlerIndex, t, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTask(fr.sorbonne_u.components.ComponentI.FComponentTask, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTask(
		FComponentTask t,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		this.scheduleTask(new AbstractTask() {
							@Override
							public void run() { this.runTaskLambda(t); }
						  }, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTask(java.lang.String, fr.sorbonne_u.components.ComponentI.ComponentTask, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTask(
		String executorServiceURI,
		ComponentTask t,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceURI(executorServiceURI) ;
		assert	this.isSchedulable(executorServiceURI) ;
		assert	t != null && delay >= 0 && u != null ;

		int executorServiceIndex =
							this.getExecutorServiceIndex(executorServiceURI) ;
		this.scheduleTask(executorServiceIndex, t, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTask(java.lang.String, fr.sorbonne_u.components.ComponentI.FComponentTask, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTask(
		String executorServiceURI,
		FComponentTask t,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		this.scheduleTask(executorServiceURI, 
						  new AbstractTask() {
							@Override
							public void run() { this.runTaskLambda(t); }
						  }, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTask(int, fr.sorbonne_u.components.ComponentI.ComponentTask, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTask(
		int executorServiceIndex,
		ComponentTask t,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceIndex(executorServiceIndex) ;
		assert	this.isSchedulable(executorServiceIndex) ;
		assert	t != null && delay >= 0 && u != null ;

		this.scheduleTaskOnComponent(executorServiceIndex, t, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTask(int, fr.sorbonne_u.components.ComponentI.FComponentTask, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTask(
		int executorServiceIndex,
		FComponentTask t,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		this.scheduleTask(executorServiceIndex,
						  new AbstractTask() {
							@Override
							public void run() { this.runTaskLambda(t); }
						  }, delay, u) ;
	}

	/**
	 * schedule a <code>ComponentTask</code> that becomes enabled first after
	 * the given initial delay, and subsequently with the given period; that
	 * is executions will commence after <code>initialDelay</code> then
	 * <code>initialDelay+period</code>, the
	 * <code>initialDelay + 2 * period</code>, and so on. If any execution
	 * of the task encounters an exception, subsequent executions are suppressed.
	 * Otherwise, the task will only terminate via cancellation or termination
	 * of the executor. If any execution of this task takes longer than its
	 * period, then subsequent executions may start late, but will not
	 * concurrently execute.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.canScheduleTasks()
	 * pre	t != null and initialDelay &gt;= 0 and period &gt; 0 and u != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceIndex			index of the executor service that will run the task.
	 * @param t								task to be scheduled.
	 * @param initialDelay					delay after which the task begins to run.
	 * @param period						period between successive executions.
	 * @param u								time unit in which the initial delay and the period are expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskAtFixedRateOnComponent(
		int executorServiceIndex,
		ComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceIndex(executorServiceIndex) ;
		assert	this.isSchedulable(executorServiceIndex) ;
		assert	t != null && initialDelay >= 0  && period > 0 && u != null ;

		t.setOwnerReference(this) ;
		return ((ComponentSchedulableExecutorServiceManager)
					this.executorServices.get(executorServiceIndex)).
						getScheduledExecutorService().
							scheduleAtFixedRate(t, initialDelay, period, u) ;
		}

	/**
	 * schedule a <code>ComponentTask</code> that becomes enabled first after
	 * the given initial delay, and subsequently with the given period; that
	 * is executions will commence after <code>initialDelay</code> then
	 * <code>initialDelay+period</code>, the
	 * <code>initialDelay + 2 * period</code>, and so on. If any execution
	 * of the task encounters an exception, subsequent executions are suppressed.
	 * Otherwise, the task will only terminate via cancellation or termination
	 * of the executor. If any execution of this task takes longer than its
	 * period, then subsequent executions may start late, but will not
	 * concurrently execute.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.canScheduleTasks()
	 * pre	t != null and initialDelay &gt;= 0 and period &gt; 0 and u != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceURI			URI of the executor service that will run the task.
	 * @param t								task to be scheduled.
	 * @param initialDelay					delay after which the task begins to run.
	 * @param period						period between successive executions.
	 * @param u								time unit in which the initial delay and the period are expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskAtFixedRateOnComponent(
		String executorServiceURI,
		ComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceURI(executorServiceURI) ;
		assert	t != null ;

		int executorServiceIndex =
							this.getExecutorServiceIndex(executorServiceURI) ;
		return this.scheduleTaskAtFixedRateOnComponent(
							executorServiceIndex, t, initialDelay, period, u) ;
	}

	/**
	 * schedule a <code>ComponentTask</code> that becomes enabled first after
	 * the given initial delay, and subsequently with the given period; that
	 * is executions will commence after <code>initialDelay</code> then
	 * <code>initialDelay+period</code>, the
	 * <code>initialDelay + 2 * period</code>, and so on. If any execution
	 * of the task encounters an exception, subsequent executions are suppressed.
	 * Otherwise, the task will only terminate via cancellation or termination
	 * of the executor. If any execution of this task takes longer than its
	 * period, then subsequent executions may start late, but will not
	 * concurrently execute.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.canScheduleTasks()
	 * pre	t != null and initialDelay &gt;= 0 and period &gt; 0 and u != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param t								task to be scheduled.
	 * @param initialDelay					delay after which the task begins to run.
	 * @param period						period between successive executions.
	 * @param u								time unit in which the initial delay and the period are expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskAtFixedRateOnComponent(
		ComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.validExecutorServiceIndex(
											this.standardRequestHandlerIndex) ;

		return this.scheduleTaskAtFixedRateOnComponent(
										standardSchedulableHandlerIndex,
										t, initialDelay, period, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTaskAtFixedRate(fr.sorbonne_u.components.ComponentI.ComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTaskAtFixedRate(
		ComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.canScheduleTasks() ;
		assert	this.validExecutorServiceIndex(
										this.standardSchedulableHandlerIndex) ;
		assert	t != null && initialDelay >= 0  && period > 0 && u != null ;

		this.scheduleTaskAtFixedRate(this.standardSchedulableHandlerIndex,
									 t, initialDelay, period, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTaskAtFixedRate(fr.sorbonne_u.components.ComponentI.FComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTaskAtFixedRate(
		FComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		this.scheduleTaskAtFixedRate(
						new AbstractTask() {
							@Override
							public void run() { this.runTaskLambda(t); }
						}, initialDelay, period, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTaskAtFixedRate(java.lang.String, fr.sorbonne_u.components.ComponentI.ComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTaskAtFixedRate(
		String executorServiceURI,
		ComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.canScheduleTasks() ;
		assert	this.validExecutorServiceURI(executorServiceURI) ;
		assert	this.isSchedulable(executorServiceURI) ;
		assert	t != null && initialDelay >= 0  && period > 0 && u != null ;

		int executorServiceIndex =
						this.getExecutorServiceIndex(executorServiceURI) ;
		this.scheduleTaskAtFixedRate(executorServiceIndex,
									 t, initialDelay, period, u);
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTaskAtFixedRate(java.lang.String, fr.sorbonne_u.components.ComponentI.FComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTaskAtFixedRate(
		String executorServiceURI,
		FComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		this.scheduleTaskAtFixedRate(
						executorServiceURI,
						new AbstractTask() {
							@Override
							public void run() { this.runTaskLambda(t); }
						}, initialDelay, period, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTaskAtFixedRate(int, fr.sorbonne_u.components.ComponentI.ComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTaskAtFixedRate(
		int executorServiceIndex,
		ComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.canScheduleTasks() ;
		assert	this.validExecutorServiceIndex(executorServiceIndex) ;
		assert	this.isSchedulable(executorServiceIndex) ;
		assert	t != null && initialDelay >= 0  && period > 0 && u != null ;

		this.scheduleTaskAtFixedRateOnComponent(executorServiceIndex,
												t, initialDelay, period, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTaskAtFixedRate(int, fr.sorbonne_u.components.ComponentI.FComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTaskAtFixedRate(
		int executorServiceIndex,
		FComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		this.scheduleTaskAtFixedRate(
					executorServiceIndex,
					new AbstractTask() {
						@Override
						public void run() { this.runTaskLambda(t); }
					}, initialDelay, period, u) ;
	}

	/**
	 * schedule a <code>ComponentTask</code> that becomes enabled first after
	 * the given initial delay, and subsequently with the given delay between
	 * the termination of one execution and the beginning of the next. If any
	 * execution of the task encounters an exception, subsequent executions
	 * are suppressed. Otherwise, the task will only terminate via cancellation
	 * or termination of the executor.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.canScheduleTasks()
	 * pre	t != null and initialDelay &gt;= 0 and delay &gt;= 0 and u != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceIndex			index of the executor service that will run the task.
	 * @param t								task to be scheduled.
	 * @param initialDelay					delay after which the task begins to run.
	 * @param delay							delay between the termination of one execution and the beginning of the next.
	 * @param u								time unit in which the initial delay and the delay are expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskWithFixedDelayOnComponent(
		int executorServiceIndex,
		ComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceIndex(executorServiceIndex) ;
		assert	this.isSchedulable(executorServiceIndex) ;
		assert	t != null && initialDelay >= 0 && delay >= 0 && u != null ;

		t.setOwnerReference(this) ;
		return ((ComponentSchedulableExecutorServiceManager)
				this.executorServices.get(executorServiceIndex)).
					getScheduledExecutorService().scheduleWithFixedDelay(
												t, initialDelay, delay, u) ;
	}

	/**
	 * schedule a <code>ComponentTask</code> that becomes enabled first after
	 * the given initial delay, and subsequently with the given delay between
	 * the termination of one execution and the beginning of the next. If any
	 * execution of the task encounters an exception, subsequent executions
	 * are suppressed. Otherwise, the task will only terminate via cancellation
	 * or termination of the executor.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.canScheduleTasks()
	 * pre	t != null and initialDelay &gt;= 0 and delay &gt;= 0 and u != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceURI			URI of the executor service that will run the task.
	 * @param t								task to be scheduled.
	 * @param initialDelay					delay after which the task begins to run.
	 * @param delay							delay between the termination of one execution and the beginning of the next.
	 * @param u								time unit in which the initial delay and the delay are expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskWithFixedDelayOnComponent(
		String executorServiceURI,
		ComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceURI(executorServiceURI) ;
		assert	t != null ;

		int executorServiceIndex =
							this.getExecutorServiceIndex(executorServiceURI) ;
		return this.scheduleTaskWithFixedDelayOnComponent(
							executorServiceIndex, t, initialDelay, delay, u) ;
	}

	/**
	 * schedule a <code>ComponentTask</code> that becomes enabled first after
	 * the given initial delay, and subsequently with the given delay between
	 * the termination of one execution and the beginning of the next. If any
	 * execution of the task encounters an exception, subsequent executions
	 * are suppressed. Otherwise, the task will only terminate via cancellation
	 * or termination of the executor.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.canScheduleTasks()
	 * pre	t != null and initialDelay &gt;= 0 and delay &gt;= 0 and u != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param t								task to be scheduled.
	 * @param initialDelay					delay after which the task begins to run.
	 * @param delay							delay between the termination of one execution and the beginning of the next.
	 * @param u								time unit in which the initial delay and the delay are expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskWithFixedDelayOnComponent(
		ComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.validExecutorServiceIndex(
										this.standardSchedulableHandlerIndex) ;

		return this.scheduleTaskWithFixedDelayOnComponent(
										this.standardSchedulableHandlerIndex,
										t, initialDelay, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTaskWithFixedDelay(fr.sorbonne_u.components.ComponentI.ComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTaskWithFixedDelay(
		ComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.canScheduleTasks() ;
		assert	this.validExecutorServiceIndex(
									this.standardSchedulableHandlerIndex) ;
		assert	t != null && initialDelay >= 0 && delay >= 0 && u != null ;

		this.scheduleTaskWithFixedDelay(this.standardSchedulableHandlerIndex,
										t, initialDelay, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTaskWithFixedDelay(fr.sorbonne_u.components.ComponentI.FComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTaskWithFixedDelay(
		FComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		this.scheduleTaskWithFixedDelay(
					new AbstractTask() {
						@Override
						public void run() { this.runTaskLambda(t); }
					}, initialDelay, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTaskWithFixedDelay(java.lang.String, fr.sorbonne_u.components.ComponentI.ComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTaskWithFixedDelay(
		String executorServiceURI,
		ComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.canScheduleTasks() ;
		assert	this.validExecutorServiceURI(executorServiceURI) ;
		assert	this.isSchedulable(executorServiceURI) ;
		assert	t != null && initialDelay >= 0  && delay > 0 && u != null ;

		int executorServiceIndex =
							this.getExecutorServiceIndex(executorServiceURI) ;
		this.scheduleTaskWithFixedDelay(executorServiceIndex,
										t, initialDelay, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTaskWithFixedDelay(java.lang.String, fr.sorbonne_u.components.ComponentI.FComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTaskWithFixedDelay(
		String executorServiceURI,
		FComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		this.scheduleTaskWithFixedDelay(
						executorServiceURI,
						new AbstractTask() {
							@Override
							public void run() { this.runTaskLambda(t); }
						}, initialDelay, delay, u);
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTaskWithFixedDelay(int, fr.sorbonne_u.components.ComponentI.ComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTaskWithFixedDelay(
		int executorServiceIndex,
		ComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.canScheduleTasks() ;
		assert	this.validExecutorServiceIndex(executorServiceIndex) ;
		assert	this.isSchedulable(executorServiceIndex) ;
		assert	t != null && initialDelay >= 0  && delay > 0 && u != null ;

		this.scheduleTaskWithFixedDelayOnComponent(executorServiceIndex,
												   t, initialDelay, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTaskWithFixedDelay(int, fr.sorbonne_u.components.ComponentI.FComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleTaskWithFixedDelay(
		int executorServiceIndex,
		FComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		this.scheduleTaskWithFixedDelay(
						executorServiceIndex,
						new AbstractTask() {
							@Override
							public void run() { this.runTaskLambda(t); }
						}, initialDelay, delay, u);
	}

	// ------------------------------------------------------------------------
	// Request handling
	// ------------------------------------------------------------------------

	/**
	 * The abstract class <code>AbstractService</code> provides the basic
	 * method implementations for component service calls.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2018-09-18</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static abstract class	AbstractService<V>
	implements	ComponentI.ComponentService<V>
	{
		protected AbstractComponent	serviceOwner ;
		protected final String		servicePluginURI ;
		protected PluginI			servicePlugin ;

		/**
		 * create a service callable which calls a service directly
		 * implemented by the object representing the component.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 */
		public				AbstractService()
		{
			this.servicePluginURI = null ;
		}

		/**
		 * create a service callable which calls a service 
		 * implemented by the designated plugin of the component.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	pluginURI != null
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param pluginURI	URI of a plug-in installed on the component.
		 */
		public				AbstractService(String pluginURI)
		{
			assert	pluginURI != null ;

			this.servicePluginURI = pluginURI ;
		}

		@Override
		public void			setOwnerReference(ComponentI owner)
		{
			assert	owner != null ;
			assert	owner instanceof AbstractComponent ;

			try {
				assert	this.servicePluginURI == null ||
										owner.isInstalled(servicePluginURI) ;
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}

			this.serviceOwner = (AbstractComponent)owner ;
			if (this.servicePluginURI != null) {
				this.servicePlugin =
						this.serviceOwner.getPlugin(this.servicePluginURI) ;
			}
		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentService#getServiceOwner()
		 */
		@Override
		public AbstractComponent	getServiceOwner()
		{
			return this.serviceOwner ;
		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentService#getServiceProviderReference()
		 */
		@Override
		public Object		getServiceProviderReference()
		{
			if (this.servicePluginURI == null) {
				return this.serviceOwner ;
			} else {
				return this.servicePlugin ;
			}
		}

		/**
		 * call a service lambda on the owner component passing the correct
		 * parameters.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	sl != null
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param sl			lambda expression representing a service execution.
		 * @return				the result of the lambda expression.
		 * @throws Exception	<i>to do.</i>
		 */
		protected V			callServiceLambda(FComponentService<V> sl)
		throws Exception
		{
			assert	sl != null ;

			return sl.apply(this.getServiceOwner()) ;
		}
	}

	/**
	 * execute a request represented by a <code>ComponentService</code> on the
	 * component.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * Uniform API entry to execute a call on the component.  The call, that
	 * represents a method call on the object representing the component, is
	 * embedded in a <code>ComponentService</code> object.  In concurrent
	 * components, the Java executor framework is used to handle such requests.
	 * Sequential components may simply use this method to handle requests, or
	 * they may bypass it by directly calling the method on the object
	 * representing the component for the sought of efficiency.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	task != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param <T>							the type of the value returned by the request.
	 * @param executorServiceIndex			index of the executor service that will run the task.
	 * @param request						service request to be executed on the component.
	 * @return								a future value embedding the result of the task.
	 * @throws AssertionError				if the component is not started, the index is not valid or the request is null.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected <T> Future<T>		handleRequest(
		int executorServiceIndex,
		ComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	request != null ;

		request.setOwnerReference(this) ;
		if (this.hasItsOwnThreads() &&
						this.validExecutorServiceIndex(executorServiceIndex)) {
			return this.executorServices.get(executorServiceIndex).
										getExecutorService().submit(request) ;
		} else {
			final ComponentService<T> t = request ;
			return new Future<T>() {
						@Override
						public boolean	cancel(boolean mayInterruptIfRunning)
						{ return false ; }

						@Override
						public T		get()
						throws	InterruptedException,
								ExecutionException
						{
							try {
								return t.call() ;
							} catch (Exception e) {
								throw new ExecutionException(e) ;
							}
						}

						@Override
						public T		get(long timeout, TimeUnit unit)
						throws	InterruptedException,
								ExecutionException,
								TimeoutException
						{ 	try {
								return t.call() ;
							} catch (Exception e) {
								throw new ExecutionException(e) ;
							}
						}

						@Override
						public boolean	isCancelled()
						{ return false ; }

						@Override
						public boolean	isDone()
						{ return true ; }
					} ;
		}
	}

	/**
	 * execute a request represented by a <code>ComponentService</code> on the
	 * component.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * This method is meant to be used when programmers need to manage within
	 * a component requests with futures. It can be requests executed as
	 * services of the component or calls to other components which are
	 * synchronous but that the calling component wants to manage as
	 * asynchronous tasks.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.validExecutorServiceURI(executorServiceURI)
	 * pre	request != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param <T>					the type of the value returned by the request.
	 * @param executorServiceURI	URI of the executor service that will run the task.
	 * @param request				service request to be executed on the component.
	 * @return						a future value embedding the result of the task.
	 * @throws AssertionError				if the component is not started, the URI is not valid or the request is null.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected <T> Future<T>		handleRequest(
		String executorServiceURI,
		ComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceURI(executorServiceURI) ;
		assert	request != null ;

		int executorServiceIndex =
				this.getExecutorServiceIndex(executorServiceURI) ;
		return this.handleRequest(executorServiceIndex, request) ;
	}

	/**
	 * execute a request represented by a <code>ComponentService</code> on the
	 * component.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * This method is meant to be used when programmers need to manage within
	 * a component requests with futures. It can be requests executed as
	 * services of the component or calls to other components which are
	 * synchronous but that the calling component wants to manage as
	 * asynchronous tasks.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.validExecutorServiceIndex(this.standardRequestHandlerIndex) ||
	 *      this.validExecutorServiceIndex(this.standardSchedulableHandlerIndex)
	 * pre	request != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param <T>							the type of the value returned by the request.
	 * @param request						service request to be executed on the component.
	 * @return								a future value embedding the result of the task.
	 * @throws AssertionError				if the component is not started or the request is null.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected <T> Future<T>		handleRequest(
		ComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException
	{
		if (this.validExecutorServiceIndex(this.standardRequestHandlerIndex)) {
			return this.handleRequest(this.standardRequestHandlerIndex,
									  request) ;
		} else if (this.validExecutorServiceIndex(
										this.standardSchedulableHandlerIndex)) {
			return this.handleRequest(this.standardSchedulableHandlerIndex,
									  request) ;
		} else {
			return this.handleRequest(-1, request) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#handleRequestSync(int, fr.sorbonne_u.components.ComponentI.ComponentService)
	 */
	@Override
	public <T> T		handleRequestSync(
		int executorServiceIndex,
		ComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceIndex(executorServiceIndex) ;
		assert	request != null ;

		return this.handleRequest(executorServiceIndex, request).get() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#handleRequestSync(fr.sorbonne_u.components.ComponentI.ComponentService)
	 */
	@Override
	public <T> T			handleRequestSync(ComponentService<T> request)
	throws	AssertionError,
			RejectedExecutionException,
			InterruptedException,
			ExecutionException
	{
		assert	this.isStarted() ;
		assert	request != null ;

		if (this.hasItsOwnThreads()) {
			if (this.validExecutorServiceIndex(
										this.standardRequestHandlerIndex)) {
				return this.handleRequest(this.standardRequestHandlerIndex,
															request).get() ;
			} else {
				assert this.validExecutorServiceIndex(
									this.standardSchedulableHandlerIndex) ;
				return this.handleRequest(
								this.standardSchedulableHandlerIndex,
								request).get() ;
			}
		} else {
			return this.handleRequest(-1, request).get() ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#handleRequestSync(fr.sorbonne_u.components.ComponentI.FComponentService)
	 */
	@Override
	public <T> T		handleRequestSync(
		FComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		return this.handleRequestSync(
							new AbstractService<T>() {
								@Override
								public T call() throws Exception {
									return this.callServiceLambda(request) ;
								}								
							}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#handleRequestSync(java.lang.String, fr.sorbonne_u.components.ComponentI.ComponentService)
	 */
	@Override
	public <T> T		handleRequestSync(
		String executorServiceURI,
		ComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceURI(executorServiceURI) ;
		assert	request != null ;

		int executorServiceIndex =
				this.getExecutorServiceIndex(executorServiceURI) ;
		return this.handleRequest(executorServiceIndex, request).get() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#handleRequestSync(java.lang.String, fr.sorbonne_u.components.ComponentI.FComponentService)
	 */
	@Override
	public <T> T		handleRequestSync(
		String executorServiceURI,
		FComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		return this.handleRequestSync(
							executorServiceURI,
							new AbstractService<T>() {
								@Override
								public T call() throws Exception {
									return this.callServiceLambda(request) ;
								}
							}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#handleRequestSync(int, fr.sorbonne_u.components.ComponentI.FComponentService)
	 */
	@Override
	public <T> T		handleRequestSync(
		int executorServiceIndex,
		FComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		return this.handleRequestSync(
							executorServiceIndex,
							new AbstractService<T>() {
								@Override
								public T call() throws Exception {
									return this.callServiceLambda(request) ;
								}
							}) ;
	}

	/**
	 * schedule a service for execution after a given delay.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.canScheduleTasks()
	 * pre	s != null and delay &gt; 0 and u != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param <T>							the type of the value returned by the request.
	 * @param executorServiceIndex			index of the executor service that will run the task.
	 * @param request						service request to be scheduled.
	 * @param delay							delay after which the task must be run.
	 * @param u								time unit in which the delay is expressed.
	 * @return								a scheduled future to synchronise with the task.
	 * @throws AssertionError				if the component is not started, this index is not valid, the executor is not schedulable or the request in null.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected <T> ScheduledFuture<T>	scheduleRequest(
		int executorServiceIndex,
		ComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceIndex(executorServiceIndex) ;
		assert	this.isSchedulable(executorServiceIndex) ;
		assert	request != null && delay >= 0 && u != null ;

		request.setOwnerReference(this) ;
		return ((ComponentSchedulableExecutorServiceManager)
					this.executorServices.get(executorServiceIndex)).
							getScheduledExecutorService().
											schedule(request, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleRequestSync(int, fr.sorbonne_u.components.ComponentI.ComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T 			scheduleRequestSync(
		int executorServiceIndex,
		ComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceIndex(executorServiceIndex) ;
		assert	this.isSchedulable(executorServiceIndex) ;
		assert	request != null && delay >= 0 && u != null ;

		return this.scheduleRequest(
						executorServiceIndex, request, delay, u).get() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleRequestSync(fr.sorbonne_u.components.ComponentI.ComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T			scheduleRequestSync(
		ComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() ;
		assert	this.canScheduleTasks() ;
		assert	this.validExecutorServiceIndex(
									this.standardSchedulableHandlerIndex) ;
		assert	request != null && delay >= 0 && u != null ;

		return this.scheduleRequestSync(this.standardSchedulableHandlerIndex,
														request, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleRequestSync(fr.sorbonne_u.components.ComponentI.FComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T			scheduleRequestSync(
		FComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		return this.scheduleRequestSync(
								new AbstractService<T>() {
									@Override
									public T call() throws Exception {
										return this.callServiceLambda(request) ;
									}
								}, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleRequestSync(java.lang.String, fr.sorbonne_u.components.ComponentI.ComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T			scheduleRequestSync(
		String executorServiceURI,
		ComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() ;
		assert	this.validExecutorServiceURI(executorServiceURI) ;
		assert	this.isSchedulable(executorServiceURI) ;
		assert	request != null && delay >= 0 && u != null ;

		int executorServiceIndex =
							this.getExecutorServiceIndex(executorServiceURI) ;
		return this.scheduleRequestSync(executorServiceIndex,
										request, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleRequestSync(java.lang.String, fr.sorbonne_u.components.ComponentI.FComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T			scheduleRequestSync(
		String executorServiceURI,
		FComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		return this.scheduleRequestSync(
								executorServiceURI,
								new AbstractService<T>() {
									@Override
									public T call() throws Exception {
										return this.callServiceLambda(request) ;
									}
								}, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleRequestSync(int, fr.sorbonne_u.components.ComponentI.FComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T			scheduleRequestSync(
		int executorServiceIndex,
		FComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		return this.scheduleRequestSync(
								executorServiceIndex,
								new AbstractService<T>() {
									@Override
									public T call() throws Exception {
										return this.callServiceLambda(request) ;
									}
								}, delay, u) ;
	}

	// ------------------------------------------------------------------------
	// Reflection facility
	// FIXME: experimental...
	// To use the reflection facility:
	//    - the jar tools.jar from the Java distribution must be in the
	//      classpath of the compiler and of the JVM
	//    - the JVM must be passed the argument "-javaagent:hotswap.jar"
	//      with the jar "hotswap.jar" accessible from the base directory
	//      (or the appropriate path given in the argument
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getComponentDefinitionClassName()
	 */
	@Override
	public String			getComponentDefinitionClassName()
	throws Exception
	{
		return this.getClass().getCanonicalName() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getComponentAnnotations()
	 */
	@Override
	public Annotation[]		getComponentAnnotations() throws Exception
	{
		return this.getClass().getAnnotations() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getComponentLoader()
	 */
	@Override
	public ClassLoader		getComponentLoader() throws Exception
	{
		return this.getClass().getClassLoader() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getComponentServiceSignatures()
	 */
	@Override
	public ServiceSignature[]	getComponentServiceSignatures()
	throws Exception
	{
		Vector<ServiceSignature> ret = new Vector<ServiceSignature>() ;
		Class<?> clazz = this.getClass() ;
		while (clazz != AbstractComponent.class) {
			Method[] ms = clazz.getDeclaredMethods() ;
			for (int i = 0 ; i < ms.length ; i++) {
				if (Modifier.isPublic(ms[i].getModifiers())) {
					ret.add(new ServiceSignature(
									ms[i].getReturnType(),
									ms[i].getParameterTypes())) ;
				}
			}
			clazz = clazz.getSuperclass() ;
		}
		return ret.toArray(new ServiceSignature[0]) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getComponentConstructorSignatures()
	 */
	@Override
	public ConstructorSignature[]	getComponentConstructorSignatures()
	throws Exception
	{
		Constructor<?>[] cons = this.getClass().getConstructors() ;
		ConstructorSignature[] ret = new ConstructorSignature[cons.length] ;
		for (int i = 0 ; i < cons.length ; i++) {
			ret[i] = new ConstructorSignature(cons[i].getParameterTypes()) ;
		}
		return ret ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#newInstance(java.lang.Object[])
	 */
	@Override
	public ComponentI	newInstance(Object[] parameters) throws Exception
	{
		Class<?>[] pTypes = new Class<?>[parameters.length] ;
		for (int i = 0 ; i < parameters.length ; i++) {
			pTypes[i] = parameters[i].getClass() ;
		}
		Constructor<?> cons = this.getClass().getConstructor(pTypes) ;
		return (ComponentI) cons.newInstance(parameters) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#invokeService(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object		invokeService(String name, Object[] params)
	throws Exception
	{
		assert	this.isStarted() ;
		assert	name != null && params != null ;

		Class<?>[] pTypes = new Class<?>[params.length] ;
		for (int i = 0 ; i < params.length ; i++) {
			pTypes[i] = params[i].getClass() ;
		}
		Method m = this.getClass().getMethod(name, pTypes) ;
		int index ;
		if (this.validExecutorServiceIndex(this.standardRequestHandlerIndex)) {
			index = this.standardRequestHandlerIndex ;
		} else {
			index = this.standardSchedulableHandlerIndex ;
		}
		return this.handleRequest(
						index,
						new AbstractService<Object>() {
							@Override
							public Object call() throws Exception {
								return m.invoke(this.getServiceOwner(), params) ;
							}
						}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#invokeServiceSync(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object		invokeServiceSync(String name, Object[] params)
	throws Exception
	{
		Class<?>[] pTypes = new Class<?>[params.length] ;
		for (int i = 0 ; i < params.length ; i++) {
			pTypes[i] = params[i].getClass() ;
		}
		Method m = this.getClass().getMethod(name, pTypes) ;
		return this.handleRequestSync(
						new AbstractService<Object>() {
							@Override
							public Object call() throws Exception {
								return m.invoke(this.getServiceOwner(), params) ;
							}
						}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#invokeServiceAsync(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void			invokeServiceAsync(String name, Object[] params)
	throws Exception
	{
		Class<?>[] pTypes = new Class<?>[params.length] ;
		for (int i = 0 ; i < params.length ; i++) {
			pTypes[i] = params[i].getClass() ;
		}
		Method m = this.getClass().getMethod(name, pTypes) ;
		this.runTask(new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								m.invoke(this.getTaskOwner(), params) ;
							} catch (IllegalAccessException |
									 IllegalArgumentException |
									 InvocationTargetException e) {
								e.printStackTrace() ;
							}
						}
					 });
	}

	/** Javassist classpool containing the component classes.				*/
	protected static ClassPool	javassistClassPool ;
	/** The Javassist CtClass representation of the compoennt's class.	*/
	protected CtClass			javassistClassForComponent ;

	/**
	 * ensure that the Javassist representation of the component's class
	 * is loaded and can be accessed by the reflective code.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws NotFoundException		<i>todo.</i>
	 */
	protected void		ensureLoaded() throws NotFoundException
	{
		if (AbstractComponent.javassistClassPool == null) {
			AbstractComponent.javassistClassPool = javassist.ClassPool.getDefault() ;
			String libFullName =
				ClassLoader.getSystemClassLoader().getParent().
					getResource("java/lang/String.class").toString() ;
			libFullName =
				libFullName.replaceAll("rt.jar!/java/lang/String.class", "") ;
			libFullName = libFullName.replaceAll("jar:file:", "") ;
			AbstractComponent.javassistClassPool.appendClassPath(libFullName) ;
		}
		if (this.javassistClassForComponent == null) {
			this.javassistClassForComponent =
					AbstractComponent.javassistClassPool.
								get(this.getClass().getCanonicalName()) ;
		}
	}

	/**
	 * get a declared method from the Javassist representation of the
	 * component's class.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	methodName != null
	 * pre	parametersCanonicalClassNames != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param methodName						name of the method to be retrieved.
	 * @param parametersCanonicalClassNames	names of the classes typing the parameters of the method.
	 * @return								the corresponding method.
	 * @throws NotFoundException				if no method is found.
	 */
	protected CtMethod	getDeclaredMethod(
		String methodName,
		String[] parametersCanonicalClassNames
		) throws NotFoundException
	{
		assert	methodName != null :
					new PreconditionException("Method name is null!") ;
		assert	parametersCanonicalClassNames != null :
					new PreconditionException("Parameter type names array"
													+ " can't be null!") ;

		CtClass[] paramCtClass =
						new CtClass[parametersCanonicalClassNames.length] ;
		for (int i = 0 ; i < parametersCanonicalClassNames.length ; i++) {
			paramCtClass[i] =
				AbstractComponent.javassistClassPool.
									get(parametersCanonicalClassNames[i]) ;
		}
		CtMethod m = this.javassistClassForComponent.
							getDeclaredMethod(methodName, paramCtClass) ;
		return m ;
	}
	
	/**
	 * @see fr.sorbonne_u.components.ComponentI#insertBeforeService(java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public void			insertBeforeService(
		String methodName,
		String[] parametersCanonicalClassNames,
		String code
		) throws Exception
	{
		assert	methodName != null :
					new PreconditionException("Service name is null!") ;
		assert	parametersCanonicalClassNames != null :
					new PreconditionException("Parameter type names array"
														+ " is null!") ;
		assert	code != null :
					new PreconditionException("Code to be added is null!") ;

		this.ensureLoaded() ;
		CtMethod m = this.getDeclaredMethod(methodName,
										   parametersCanonicalClassNames) ;
		m.insertBefore(code) ;
		HotSwapAgent.redefine(this.getClass(),
							  this.javassistClassForComponent) ;
		this.javassistClassForComponent.defrost() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#insertAfterService(java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public void			insertAfterService(
		String methodName,
		String[] parametersCanonicalClassNames,
		String code
		) throws Exception
	{
		assert	methodName != null :
					new PreconditionException("Service name is null!") ;
		assert	parametersCanonicalClassNames != null :
					new PreconditionException("Parameter type names array"
														+ " is null!") ;
		assert	code != null :
					new PreconditionException("Code to be added is null!") ;

		this.ensureLoaded() ;
		CtMethod m = this.getDeclaredMethod(methodName,
										   parametersCanonicalClassNames) ;
		m.insertAfter(code) ;
		HotSwapAgent.redefine(this.getClass(),
							  this.javassistClassForComponent) ;
		this.javassistClassForComponent.defrost() ;
	}
}
// -----------------------------------------------------------------------------