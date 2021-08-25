package fr.sorbonne_u.components;

// Copyright Jacques Malenfant, Sorbonne Universite.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import fr.sorbonne_u.components.annotations.AddPlugin;
import fr.sorbonne_u.components.annotations.AddPlugins;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.connectors.ConnectorI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.exceptions.ComponentTaskExecutionException;
import fr.sorbonne_u.components.exceptions.ExecutorServicesManagementException;
import fr.sorbonne_u.components.exceptions.PluginException;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.helpers.ComponentExecutorServiceManager;
import fr.sorbonne_u.components.helpers.ComponentSchedulableExecutorServiceManager;
import fr.sorbonne_u.components.helpers.Logger;
import fr.sorbonne_u.components.helpers.TracerWindow;
import fr.sorbonne_u.components.interfaces.ComponentInterface;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.ports.InboundPortI;
import fr.sorbonne_u.components.ports.OutboundPortI;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;
import fr.sorbonne_u.components.reflection.utils.ConstructorSignature;
import fr.sorbonne_u.components.reflection.utils.ServiceSignature;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.Pair;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.util.HotSwapAgent;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
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
 * invariant	
 * </pre>
 * 
 * <p>Created on : 2012-11-06</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractComponent
implements	ComponentI
{
	// -------------------------------------------------------------------------
	// Internal information about inner components and component life-cycle
	// management.
	// -------------------------------------------------------------------------

	/** current state in the component life-cycle.							*/
	protected final AtomicReference<ComponentState>		state;

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isInStateAmong(fr.sorbonne_u.components.ComponentStateI[])
	 */
	@Override
	public boolean		isInStateAmong(ComponentStateI[] states)
	{
		assert	states != null :
					new PreconditionException(
							"State array parameter can't be null!");

		synchronized (this.state) {
			boolean ret = false;
			for (int i = 0; !ret && i < states.length; i++) {
				ret = (this.state.get() == states[i]);
			}
			return ret;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#notInStateAmong(fr.sorbonne_u.components.ComponentStateI[])
	 */
	@Override
	public boolean		notInStateAmong(ComponentStateI[] states)
	{
		assert	states != null :
					new PreconditionException(
							"State array parameter can't be null!");

		synchronized (this.state) {
			boolean ret = true;
			for (int i = 0; ret && i < states.length; i++) {
				ret = (this.state.get() != states[i]);
			}
			return ret;
		}
	}

	// -------------------------------------------------------------------------
	// Inner components management
	// -------------------------------------------------------------------------

	// Implementation invariant
	//
	//     (composite.get() == null) || isSubcomponentOf(composite.get())
	//
	//     forall (String uri) {
	//         !hasSubcomponent(uri) || innerComponents.get(uri) != null
	//     }

	/** inner components owned by this component.							*/
	private final ConcurrentHashMap<String,AbstractComponent>	innerComponents;
	/** reference to the immediate composite component containing this
	 *  component as subcomponent.											*/
	private final AtomicReference<AbstractComponent>			composite;

	/**
	 * sets the reference to the composite component containing immediately
	 * this component; the composite component must have this component as
	 * subcomponent for this method to succeed otherwise an assertion exception
	 * is raised.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isSubcomponent()}
	 * pre	{@code composite != null}
	 * post	{@code isSubcomponent()}
	 * post	{@code getCompositeComponentReference() == composite}
	 * </pre>
	 *
	 * @param composite	the reference to the composite component containing immediately this component.
	 */
	private void		setCompositeComponentReference(
		AbstractComponent composite
		)
	{
		assert	composite != null :
						new PreconditionException("composite != null");

		synchronized (this.composite) {
			boolean isFirstAssignment =
							this.composite.compareAndSet(null, composite);

			assert	isFirstAssignment :
						new PreconditionException(
								"The composite component of a subcomponent can "
										+ "only be set once.");

			assert	this.isSubcomponentOf(composite) :
						new PostconditionException("isSubcomponent()");
			assert	this.getCompositeComponentReference() == composite :
						new PostconditionException(
								"getCompositeComponentReference() == composite");
		}
		
		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.INNER_COMPONENTS)) {
			try {
				AbstractCVM.getCVM().logDebug(
					CVMDebugModes.INNER_COMPONENTS,
					"setting the composite component " +
					composite.reflectionInboundPortURI +
					" on subcomponent " + this.reflectionInboundPortURI
					+ " ...done.");
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}
		}

		AbstractComponent.checkImplementationInvariant(this);
	}

	/**
	 * return true if this component is a subcomponent of some composite.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	true if this component is a subcomponent of some composite.
	 */
	protected boolean	isSubcomponent()
	{
		return this.composite.get() != null;
	}

	/**
	 * return true if this component is a subcomponent of the given composite.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code composite != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param composite	the reference to the composite component containing immediately this component.
	 * @return			true if this component is a subcomponent of the given composite.
	 */
	protected boolean	isSubcomponentOf(ComponentI composite)
	{
		assert	composite != null :
					new PreconditionException("composite != null");

		AbstractComponent c = this.composite.get();
		return c != null && c == composite;
	}

	/**
	 * return true if this component has a subcomponent with the given
	 * reflection inbound port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection inbound port of the sought subcomponent.
	 * @return							true if this component has a subcomponent with the given reflection inbound port URI.
	 */
	protected boolean	hasSubcomponent(String reflectionInboundPortURI)
	{
		if (reflectionInboundPortURI == null) return false;
		return this.innerComponents.containsKey(reflectionInboundPortURI);
	}

	/**
	 * get the reference to the immediate composite component containing
	 * this component, if any.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code this.isSubcomponent()}
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return	the reference to the immediate composite component containing this component.
	 */
	protected AbstractComponent	getCompositeComponentReference()
	{
		AbstractComponent ret = this.composite.get();

		assert	ret != null : new PreconditionException("isSubcomponent()");

		return ret;
	}

	/**
	 * find the inbound port with the given URI of a subcomponent that has
	 * the given reflection inbound port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code subcomponentURI != null && portURI != null}
	 * pre	{@code hasSubcomponent(subcomponentURI)}
	 * post	true		// no postcondition.
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
		assert	subcomponentURI != null && portURI != null :
					new PreconditionException(
							"subcomponentURI != null && portURI != null");

		ComponentI subcomponent = this.innerComponents.get(subcomponentURI);

		assert	subcomponent != null :
						new PreconditionException(
								"hasSubcomponent(subcomponentURI)");

		// WARNING: this implementation assumes that inner components are
		// located in the same JVM as their composite component.
		return ((AbstractComponent)subcomponent).
										findInboundPortFromURI(this, portURI);
	}

	/**
	 * finds an inbound port of this component from its URI if it is a
	 * subcomponent of the given composite.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code composite != null}
	 * pre	{@code portURI != null}
	 * pre	{@code isSubcomponentOf(composite)}
	 * post	{@code ret == null || ret.getPortURI().equals(portURI)}
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
					new PreconditionException("composite must not be null!");
		assert	portURI != null :
					new PreconditionException("Port URI can't be null!");
		assert	this.isSubcomponentOf(composite) :
					new PreconditionException("isSubcomponentOf(composite)");

		// WARNING: this implementation assumes that inner components are
		// located in the same JVM as their composite component.
		this.portManagementLock.readLock().lock();
		try {
			InboundPortI ret = (InboundPortI) this.portURIs2ports.get(portURI);
			
			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CONNECTING)) {
				try {
					AbstractCVM.getCVM().logDebug(
						CVMDebugModes.CONNECTING,
						"finding the inbound port with URI " + portURI +
						" on subcomponent " + this.reflectionInboundPortURI +
						(ret == null ?
							" found none,"
						:	" found one with URI " + ret.getPortURI() + ",")
						+ " ...done.");
				} catch (Exception e) {
					throw new RuntimeException(e) ;
				}
			}

			return ret;
		} finally {
			this.portManagementLock.readLock().unlock();
		}
	}

	// -------------------------------------------------------------------------
	// Internal executor services and parallelism management
	// -------------------------------------------------------------------------

	/**
	 * The functional interface <code>ExecutorServiceFactory</code> proposes a
	 * mean to provide a factory creating executor service instances to be
	 * added to the executor services of the component.
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
	 * invariant	true
	 * </pre>
	 * 
	 * <p>Created on : 2020-03-18</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	@FunctionalInterface
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
		 * post	{@code ret != null}
		 * </pre>
		 *
		 * @param nbThreads	number of threads to put in the executor service.
		 * @return			the new executor service with the given number of threads.
		 */
		public ExecutorService	createExecutorService(int nbThreads);
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
	 * invariant	true
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
				return Executors.newSingleThreadExecutor();
			} else {
				return Executors.newFixedThreadPool(nbThreads);
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
	 * invariant	true
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
				return Executors.newSingleThreadScheduledExecutor();
			} else {
				return Executors.newScheduledThreadPool(nbThreads);
			}
		}
	}

	// The following implementation of executor services management for
	// components aims at providing fast access to executor services as well
	// as the capability to create and shutdown executor services dynamically.
	// To achieve this, executor services designated by URIs (string) also have
	// an index and are stored in an array at this index. When a new executor
	// service is created from an URI, its attributed index is returned. Also,
	// at any time, the index corresponding to an URI can be obtained. Hence,
	// methods submitting tasks or requests to executor services can use the
	// indexes rather than URIs. The index is guaranteed not to change over
	// time, which is complicating a bit the management of the executor services
	// array as shutting down executor services creates "holes" (null values
	// at these indexes). The implementation is able to reuse indexes and their
	// corresponding placeholder when creating new executor services.

	// Implementation invariant
	//
	//     assert hasItsOwnThreads() == (getTotalNumberOfThreads() > 0)
	//
	//     boolean uri2index = true;
	//     for (Entry<String,Integer> entry :
	//                                     executorServicesIndexes.entrySet()) {
	//         String uri = entry.getKey();
	//         int index = entry.getValue();
	//         uri2index = uri2index && 
	//                       executorServices.get()[index].getURI().equals(uri);
	//     }
	//     assert uri2index;

	/** URI of the standard request handler pool of threads.				*/
	public static final String			STANDARD_REQUEST_HANDLER_URI =
											"STANDARD_REQUEST_H_URI";
	/** URI of the standard schedulable tasks handler pool of threads.		*/
	public static final String			STANDARD_SCHEDULABLE_HANDLER_URI =
											"STANDARD_SCHEDULABLE_H_URI";

	/** lock protecting the concurrent accesses to executorServices and
	 *  executorServicesIndexes.											*/
	protected final ReentrantReadWriteLock	executorServicesLock =
												new ReentrantReadWriteLock();
	/** standard initial size of the executor services pool.				*/
	protected static int				INITIAL_EXECUTOR_SERVICES_POOL_SIZE = 4;
	/** initial size of the executor services pool for this component.		*/
	protected int						initialExecutorServicesPoolSize =
											INITIAL_EXECUTOR_SERVICES_POOL_SIZE;
	/** array of executor service managers.								 	*/
	protected final AtomicReference<ComponentExecutorServiceManager[]>
											executorServices;
	/** map from URI of executor services to their index in the array list.	*/
	protected final Map<String,Integer>		executorServicesIndexes;

	/**
	 * @see fr.sorbonne_u.components.ComponentI#hasItsOwnThreads()
	 */
	@Override
	public boolean		hasItsOwnThreads()
	{
		this.executorServicesLock.readLock().lock();
		try {
			if (this.executorServices.get() == null) {
				return false;
			}
			boolean ret = false;
			ComponentExecutorServiceManager[] temp =
											this.executorServices.get();
			for (int i = 0 ; !ret && i < temp.length ; i++) {
				ret = (temp[i] != null);
			}
			return ret;
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getTotalNumberOfThreads()
	 */
	@Override
	public int			getTotalNumberOfThreads()
	{
		this.executorServicesLock.readLock().lock();
		try {
			if (this.executorServices.get() == null) {
				return 0;
			}
			int nbThreads = 0;
			ComponentExecutorServiceManager[] temp =
											this.executorServices.get();
			for (int i = 0 ; i < temp.length ; i++) {
				if (temp[i] != null) {
					nbThreads += temp[i].getNumberOfThreads();
				}
			}
			return nbThreads;
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#hasSerialisedExecution()
	 */
	@Override
	public boolean		hasSerialisedExecution()
	{
		this.executorServicesLock.readLock().lock();
		try {
			return this.hasItsOwnThreads() &&
							this.getTotalNumberOfThreads() == 1;
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#canScheduleTasks()
	 */
	@Override
	public boolean		canScheduleTasks()
	{
		this.executorServicesLock.readLock().lock();
		try {
			if (this.executorServices.get() == null) {
				return false;
			}
			boolean ret = false;
			ComponentExecutorServiceManager[] temp =
												this.executorServices.get();
			for (int i = 0; !ret && i < temp.length ; i++) {
				if (temp[i] != null) {
					ret = temp[i].isSchedulable();
				}
			}
			return ret;
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * create a new user-defined executor service under the given URI and
	 * with the given number of threads.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null}
	 * pre	{@code !validExecutorServiceURI(uri)}
	 * pre	{@code nbThreads > 0}
	 * post	{@code validExecutorServiceURI(uri)}
	 * </pre>
	 *
	 * @param uri			URI of the new executor service.
	 * @param nbThreads		number of threads of the new executor service.
	 * @param schedulable	if true, the new executor service is schedulable otherwise it is not.
	 * @return				the index associated with the new executor service.
	 */
	protected int		createNewExecutorService(
		String uri,
		int nbThreads,
		boolean schedulable
		)
	{
		return this.createNewExecutorService(
							uri, nbThreads,
							schedulable ?
								new StandardSheduledExecutorServiceFactory()
							:	new StandardExecutorServiceFactory());
	}

	/**
	 * find the first placeholder for an executor service in the array of
	 * executor services and return its index.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return the first index at which no executor service reference exists in the array of executor services.
	 */
	private int			nextExecutorServicesIndex()
	{
		int ret = 0;
		if (this.executorServices.get() == null) {
			this.executorServices.set(
				new ComponentExecutorServiceManager[
				                  this.initialExecutorServicesPoolSize]);
		}
		ComponentExecutorServiceManager[] temp = this.executorServices.get();
		boolean found = false;
		for ( ; !found && ret < temp.length ; ret++) {
			found = (temp[ret] == null);
		}
		if (!found) {
			ComponentExecutorServiceManager[] reallocated =
							new ComponentExecutorServiceManager[2*temp.length];
			for (int i = 0 ; i < temp.length ; i++) {
				reallocated[i] = temp[i];
			}
			this.executorServices.set(reallocated);
			return temp.length;
		} else {
			return ret - 1;
		}
	}

	/**
	 * return the number of executor services in use in this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	the number of executor services in use in this component.
	 */
	private int			countExecutorServices()
	{
		int count = 0;
		if (this.executorServices.get() != null) {
			for (int i = 0 ; i < this.executorServices.get().length ; i++) {
				if (this.executorServices.get()[i] != null) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * create a new user-defined executor service under the given URI and
	 * with the given number of threads.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null}
	 * pre	{@code !validExecutorServiceURI(uri)}
	 * pre	{@code nbThreads > 0}
	 * pre	{@code factory != null}
	 * post	{@code validExecutorServiceURI(uri)}
	 * </pre>
	 *
	 * @param uri			URI of the new executor service.
	 * @param nbThreads		number of threads of the new executor service.
	 * @param factory		an executor service factory used to create the new thread pool.
	 * @return				the index associated with the new executor service.
	 */
	protected int		createNewExecutorService(
		String uri,
		int nbThreads,
		ExecutorServiceFactory factory
		)
	{
		assert	uri != null : new PreconditionException("uri != null");
		assert	nbThreads > 0 : new PreconditionException("nbThreads > 0");
		assert	factory != null : new PreconditionException("factory != null");

		int index = -1;
		this.executorServicesLock.writeLock().lock();
		try {
			assert	!this.validExecutorServiceURI(uri) :
						new PreconditionException(
								"!this.validExecutorServiceURI(uri)");
			int numberOfES_pre = this.countExecutorServices();

			index = this.nextExecutorServicesIndex();
			this.executorServicesIndexes.put(uri, index);

			ComponentExecutorServiceManager cesm = null;
			ExecutorService es = factory.createExecutorService(nbThreads);

			if (!(es instanceof ScheduledExecutorService)) {
				cesm = new ComponentExecutorServiceManager(uri, nbThreads, es);
			} else {
				cesm = new ComponentSchedulableExecutorServiceManager(
													   uri, nbThreads, es);
			}
			this.executorServices.get()[index] = cesm;

			assert	this.executorServicesIndexes.get(uri) == index :
						new ExecutorServicesManagementException(
								"executor service with URI " + uri +
								" does not have index " + index);
			assert	this.executorServices.get()[index] != null :
						new ExecutorServicesManagementException(
								"no executor service at index " + index);
			assert	uri.equals(this.executorServices.get()[index].getURI()) :
						new ExecutorServicesManagementException(
								"the executor serviec at index " + index +
								" does not have URI " + uri + " but rather " +
								this.executorServices.get()[index].getURI());
			assert	this.countExecutorServices() == numberOfES_pre + 1 :
						new ExecutorServicesManagementException(
								"the executor services pool did not increase "
								+ "in size after creating the new one with URI "
								+ uri);

			assert	this.validExecutorServiceURI(uri) :
						new PostconditionException(
										"validExecutorServiceURI(uri)");
		} finally {
			this.executorServicesLock.writeLock().unlock();
		}


		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.EXECUTOR_SERVICES)) {
			AbstractCVM.getCVM().logDebug(
					CVMDebugModes.EXECUTOR_SERVICES,
					"creating the executor service " + uri + " with index " +
					index + " and " + nbThreads + " threads on component " +
					this.reflectionInboundPortURI + " ...done.");
		}

		AbstractComponent.checkImplementationInvariant(this);

		return index;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#validExecutorServiceURI(java.lang.String)
	 */
	@Override
	public boolean		validExecutorServiceURI(String uri)
	{
		this.executorServicesLock.readLock().lock();
		try {
			return uri != null && this.executorServicesIndexes.containsKey(uri);
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#validExecutorServiceIndex(int)
	 */
	@Override
	public boolean		validExecutorServiceIndex(int index)
	{
		this.executorServicesLock.readLock().lock();
		try {
			return index >= 0 &&
						this.executorServicesIndexes.containsValue(index);
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isSchedulable(java.lang.String)
	 */
	@Override
	public boolean		isSchedulable(String uri)
	{
		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceURI(uri) :
						new PreconditionException(
								"validExecutorServiceURI(uri) " + uri);

			return this.isSchedulable(this.getExecutorServiceIndex(uri));
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isSchedulable(int)
	 */
	@Override
	public boolean		isSchedulable(int index)
	{
		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceIndex(index) :
						new PreconditionException(
								"validExecutorServiceIndex(index) " + index);

			return this.executorServices.get()[index].isSchedulable();
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * get the index of the executor service with the given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code validExecutorServiceURI(uri)}
	 * post	{@code validExecutorServiceIndex(ret)}
	 * </pre>
	 *
	 * @param uri	URI of the sought executor service.
	 * @return		the index of the executor service with the given URI.
	 */
	protected int		getExecutorServiceIndex(String uri)
	{
		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceURI(uri) :
						new PreconditionException(
								"validExecutorServiceURI(uri) " + uri);

			int ret = this.executorServicesIndexes.get(uri);

			assert	this.validExecutorServiceIndex(ret) :
						new PostconditionException(
								"validExecutorServiceIndex(ret) " + ret);

			return ret;
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * get the executor service at the given index.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code validExecutorServiceIndex(index)}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param index	index of the sought executor service.
	 * @return		the executor service at the given index.
	 */
	protected ExecutorService	getExecutorService(int index)
	{
		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceIndex(index) :
						new PreconditionException(
								"validExecutorServiceIndex(index) " + index);

			return this.executorServices.get()[index].getExecutorService();
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * get the executor service at the given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code validExecutorServiceURI(uri)}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param uri	URI of the sought executor service.
	 * @return		the executor service at the given URI.
	 */
	protected ExecutorService	getExecutorService(String uri)
	{
		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceURI(uri) :
						new PreconditionException(
								"validExecutorServiceURI(uri) " + uri);

			return this.getExecutorService(this.getExecutorServiceIndex(uri));
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * get the executor service at the given index.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code validExecutorServiceIndex(index)}
	 * pre	{@code isSchedulable(index)}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param index	index of the sought executor service.
	 * @return		the executor service at the given index.
	 */
	protected ScheduledExecutorService	getSchedulableExecutorService(
		int index
		)
	{
		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceIndex(index) :
						new PreconditionException(
								"validExecutorServiceIndex(index) " + index);
			assert	this.isSchedulable(index) :
						new PreconditionException(
								"isSchedulable(index) " + index);

			return ((ComponentSchedulableExecutorServiceManager) 
									this.executorServices.get()[index]).
											getScheduledExecutorService();
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * get the executor service at the given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code validExecutorServiceURI(uri)}
	 * pre	{@code isSchedulable(uri)}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param uri	URI of the sought executor service.
	 * @return		the executor service at the given URI.
	 */
	protected ScheduledExecutorService	getSchedulableExecutorService(
		String uri
		)
	{
		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceURI(uri) :
						new PreconditionException(
								"validExecutorServiceURI(" + uri + ")");

			return this.getSchedulableExecutorService(
											this.getExecutorServiceIndex(uri));
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * shutdown the given executor service and remove it from the executor
	 * services of the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code validExecutorServiceURI(uri)}
	 * post	{@code ret.isShutdown()}
	 * post	{@code !validExecutorServiceURI(uri)}
	 * </pre>
	 *
	 * @param uri	URI of a valid executor service on this component.
	 * @return		the Java executor service after being shutdown.
	 */
	protected ExecutorService	shutdownExecutorService(String uri)
	{
		ExecutorService es = null;

		this.executorServicesLock.writeLock().lock();
		try {
			assert	this.validExecutorServiceURI(uri) :
						new PreconditionException(
								"validExecutorServiceURI(" + uri + ")");

			int index = this.getExecutorServiceIndex(uri);
			es = this.executorServices.get()[index].getExecutorService();
			es.shutdown();
			this.executorServices.get()[index] = null;
			this.executorServicesIndexes.remove(uri);
		} finally {
			this.executorServicesLock.writeLock().unlock();
		}

		AbstractComponent.checkImplementationInvariant(this);

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.EXECUTOR_SERVICES)) {
			AbstractCVM.getCVM().logDebug(
					CVMDebugModes.EXECUTOR_SERVICES,
					"shutting down the executor service " + uri +
					" on component " + this.reflectionInboundPortURI +
					" ...done.");
		}

		return es;
	}

	/**
	 * shutdown immediately the given executor service and remove it from the
	 * executor services of the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code validExecutorServiceURI(uri)}
	 * post	{@code ret.isShutdown()}
	 * post	{@code !validExecutorServiceURI(uri)}
	 * </pre>
	 *
	 * @param uri	URI of a valid executor service on this component.
	 * @return		a pair containing the Java executor service after being shutdown and the list of tasks that were waiting to be executed when shutting down.
	 */
	protected Pair<ExecutorService,List<Runnable>> shutdownNowExecutorService(
		String uri
		)
	{
		ExecutorService es = null;
		List<Runnable> waitingTasks =  null;

		this.executorServicesLock.writeLock().lock();
		try {
			assert	this.validExecutorServiceURI(uri) :
						new PreconditionException(
								"validExecutorServiceURI(" + uri + ")");

			int index = this.getExecutorServiceIndex(uri);
			es = this.executorServices.get()[index].getExecutorService();
			waitingTasks = es.shutdownNow();
			this.executorServices.get()[index] = null;
			this.executorServicesIndexes.remove(uri);
		} finally {
			this.executorServicesLock.writeLock().unlock();
		}

		AbstractComponent.checkImplementationInvariant(this);

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.EXECUTOR_SERVICES)) {
			AbstractCVM.getCVM().logDebug(
					CVMDebugModes.EXECUTOR_SERVICES,
					"shutting down now the executor service " + uri +
					" on component " + this.reflectionInboundPortURI +
					" ...done.");
		}

		return new Pair<ExecutorService,List<Runnable>>(es,waitingTasks);
	}

	// -------------------------------------------------------------------------
	// Plug-ins facilities
	// -------------------------------------------------------------------------

	/** Map of plug-in URI to installed plug-ins on this component.			*/
	protected final AtomicReference<ConcurrentHashMap<String,PluginI>>
															installedPlugins;

	/**
	 * configure the plug-in facilities for this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isPluginFacilitiesConfigured()}
	 * post	{@code isPluginFacilitiesConfigured()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		configurePluginFacilities() throws Exception
	{
		synchronized (this.installedPlugins) {
			assert	!this.isPluginFacilitiesConfigured() :
						new PreconditionException(
								"Can't configure plug-in "
										+ "facilities, already done!");

			this.installedPlugins.set(new ConcurrentHashMap<String,PluginI>());

			assert	this.isPluginFacilitiesConfigured() :
						new PostconditionException(
								"Plug-in facilities configuration "
										+ "not achieved correctly!");

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PLUGIN)) {
				AbstractCVM.getCVM().logDebug(
						CVMDebugModes.PLUGIN,
						"plug-in facilities configured on component "
						+ this.reflectionInboundPortURI + " ...done.");
			}
		}
	}

	/**
	 * return true if the plug-in facilities have been configured.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	true if the plug-in facilities have been configured.
	 */
	protected boolean	isPluginFacilitiesConfigured()
	{
		return this.installedPlugins.get() != null;
	}

	/**
	 * unconfigure the plug-in facilities for this component, removing all
	 * plug-ins after finalising and uninstalling them.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isPluginFacilitiesConfigured()}
	 * post	{@code !isPluginFacilitiesConfigured()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		unConfigurePluginFacilitites() throws Exception
	{
		synchronized (this.installedPlugins) {
			assert	this.isPluginFacilitiesConfigured() :
						new PreconditionException(
								"Can't unconfigure plug-in "
									+ "facilities, they are not configured!");

			for (Entry<String,PluginI> e :
									this.installedPlugins.get().entrySet()) {
				e.getValue().finalise();
				e.getValue().uninstall();
			}
			this.installedPlugins.set(null);

			assert	!this.isPluginFacilitiesConfigured() :
						new PostconditionException(
								"Plug-in facilities unconfiguration "
										+ "not achieved correctly!");

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PLUGIN)) {
				AbstractCVM.getCVM().logDebug(
						CVMDebugModes.PLUGIN,
						"plug-in facilities unconfigured on component "
						+ this.reflectionInboundPortURI + " ...done.");
			}
		}
	}

	/**
	 * install a plug-in into this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isPluginFacilitiesConfigured()}
	 * pre	{@code plugin != null}
	 * pre	{@code plugin.get}
	 * pre	{@code !plugin.isInitialised()}
	 * pre	{@code !isInstalled(plugin.getPluginURI())}
	 * post	{@code plugin.isInitialised()}
	 * post	{@code isIntalled(plugin.getPluginURI())}
	 * </pre>
	 *
	 * @param plugin		plug-in implementation object.
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		installPlugin(
		PluginI plugin
		) throws Exception
	{
		assert	plugin != null : new PreconditionException("plugin != null");
		assert	plugin.getPluginURI() != null :
					new PreconditionException("plugin.getPluginURI() != null");
		assert	!this.isInstalled(plugin.getPluginURI());
		assert	!plugin.isInitialised() :
					new PreconditionException("plugin.isInitialised()");

		synchronized (this.installedPlugins) {
			assert	this.isPluginFacilitiesConfigured() :
						new PluginException(
								"Can't install plug-in, plug-in facilities "
										+ "are not configured!");
			assert	!this.isInstalled(plugin.getPluginURI()) :
						new PreconditionException(
								"Can't install plug-in, "
								+ plugin.getPluginURI()
								+ " already installed!");

			((AbstractPlugin)plugin).installOn(this);
			this.installedPlugins.get().put(plugin.getPluginURI(), plugin);
			((AbstractPlugin)plugin).initialise();

			assert	this.isInstalled(plugin.getPluginURI()) :
						new PostconditionException(
								"Plug-in "
								+ plugin.getPluginURI()
								+ " not installed correctly!");

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PLUGIN)) {
				AbstractCVM.getCVM().logDebug(
					CVMDebugModes.PLUGIN,
					"installing plug-in " + plugin.getPluginURI() + 
					" on component " + this.reflectionInboundPortURI +
					"...done.");
			}
		}

		assert	plugin.isInitialised() :
					new PostconditionException("plugin.isInitialised()");
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#hasInstalledPlugins()
	 */
	@Override
	public boolean		hasInstalledPlugins()
	{
		synchronized (this.installedPlugins) {
			return this.isPluginFacilitiesConfigured() &&
										!this.installedPlugins.get().isEmpty();
		}
	}

	/**
	 * finalise the plug-in, at least when finalising the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isPluginFacilitiesConfigured()}
	 * pre	{@code pluginURI != null && isIntalled(pluginURI)}
	 * post	{@code !isIntalled(pluginURI)}
	 * </pre>
	 *
	 * @param pluginURI		unique plug-in identifier (within the component).
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		finalisePlugin(String pluginURI) throws Exception
	{
		assert	pluginURI != null :
					new PreconditionException("pluginURI != null");

		synchronized (this.installedPlugins) {

			assert	this.isPluginFacilitiesConfigured()  :
						new PluginException("Can't uninstall plug-in, "
								+ "plug-in facilities are not configured!");
			assert	this.isInstalled(pluginURI) :
						new PreconditionException("Can't uninstall plug-in, "
								+ pluginURI + " not installed!");

			PluginI plugin = this.installedPlugins.get().get(pluginURI);
			plugin.finalise();

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PLUGIN)) {
				AbstractCVM.getCVM().logDebug(
						CVMDebugModes.PLUGIN,
						"finalising plug-in " + pluginURI + " on component " +
						this.reflectionInboundPortURI + " ...done.");
			}
		}
	}

	/**
	 * uninstall a plug-in from this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isPluginFacilitiesConfigured()}
	 * pre	{@code pluginURI != null && this.isIntalled(pluginURI)}
	 * post	{@code !isIntalled(pluginURI)}
	 * </pre>
	 *
	 * @param pluginURI		unique plug-in identifier.
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		uninstallPlugin(String pluginURI) throws Exception
	{
		assert	pluginURI != null :
					new PreconditionException("pluginURI != null");

		synchronized (this.installedPlugins) {
			assert	this.isPluginFacilitiesConfigured()  :
						new PluginException("Can't uninstall plug-in, "
								+ "plug-in facilities are not configured!");
			assert	this.isInstalled(pluginURI) :
						new PreconditionException("Can't uninstall plug-in, "
									+ pluginURI + " not installed!");

			PluginI plugin = this.installedPlugins.get().get(pluginURI);
			plugin.uninstall();
			this.installedPlugins.get().remove(pluginURI);

			assert	!this.isInstalled(pluginURI) :
						new PostconditionException("Plug-in " + pluginURI
								+ " still installed after uninstalling!");

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PLUGIN)) {
				AbstractCVM.getCVM().logDebug(
						CVMDebugModes.PLUGIN,
						"uninstalling plug-in " + pluginURI + "on component " +
						this.reflectionInboundPortURI  + " ...done.");
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isInstalled(java.lang.String)
	 */
	@Override
	public boolean		isInstalled(String pluginURI)
	{
		assert	pluginURI != null :
					new PreconditionException("pluginURI != null");

		synchronized (this.installedPlugins) {
			return this.hasInstalledPlugins() &&
							this.installedPlugins.get().containsKey(pluginURI);
		}
	}

	/**
	 * access a named plug-in into this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isPluginFacilitiesConfigured()}
	 * pre	{@code pluginURI != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param pluginURI	unique plug-in identifier.
	 * @return			the corresponding installed plug-in or null if none.
	 */
	protected PluginI	getPlugin(String pluginURI)
	{
		assert	pluginURI != null :
					new PreconditionException("pluginURI != null");

		synchronized (this.installedPlugins) {
			assert	this.isPluginFacilitiesConfigured() :
						new PluginException("Can't access plug-in, "
								+ "plug-in facilities are not configured!");

			return this.installedPlugins.get().get(pluginURI);
		}
	}

	/**
	 * initialise the identified plug-in by adding to the owner component every
	 * specific information, ports, etc. required to run the plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isPluginFacilitiesConfigured()}
	 * pre	{@code pluginURI != null && !isInitialised(pluginURI)}
	 * post	{@code isInitialised(pluginURI)}
	 * </pre>
	 *
	 * @param pluginURI		unique plug-in identifier.
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		initialisePlugin(String pluginURI)
	throws Exception
	{
		assert	pluginURI != null :
					new PreconditionException("pluginURI != null");

		synchronized (this.installedPlugins) {
			assert	this.isPluginFacilitiesConfigured() :
						new PluginException("Can't access plug-in, "
								+ "plug-in facilities are not configured!");
			assert	!this.isInitialised(pluginURI) :
						new PreconditionException("Can't initialise plug-in "
								+ pluginURI + ", already initialised!");

			this.installedPlugins.get().get(pluginURI).initialise();

			assert	this.isInitialised(pluginURI) :
						new PostconditionException("Plug-in " + pluginURI +
													" not initialised!");

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PLUGIN)) {
				AbstractCVM.getCVM().logDebug(
						CVMDebugModes.PLUGIN,
						"initialising plug-in " + pluginURI + " on component " +
						this.reflectionInboundPortURI + " ...done.");
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isInitialised(java.lang.String)
	 */
	@Override
	public boolean		isInitialised(String pluginURI)
	throws Exception
	{
		assert	pluginURI != null :
					new PreconditionException("pluginURI != null");

		synchronized (this.installedPlugins) {
			assert	this.isPluginFacilitiesConfigured() :
						new PluginException("Can't test, "
								+ "plug-in facilities are not configured!");

			return this.installedPlugins.get().get(pluginURI).isInitialised();
		}
	}

	// -------------------------------------------------------------------------
	// Logging and tracing facilities
	// -------------------------------------------------------------------------

	/**	The logger for this component.										*/
	protected final AtomicReference<Logger>			executionLog;
	/** The tracer for this component.										*/
	protected final AtomicReference<TracerWindow>	tracer;

	/**
	 * return the current logger.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	the current logger.
	 */
	protected Logger	getLogger()
	{
		return this.executionLog.get();
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#setLogger(fr.sorbonne_u.components.helpers.Logger)
	 */
	@Override
	public void			setLogger(Logger logger)
	{
		this.executionLog.set(logger);
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isLoggerSet()
	 */
	@Override
	public boolean		isLoggerSet()
	{
		return this.getLogger() != null;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isLogging()
	 */
	@Override
	public boolean		isLogging()
	{
		synchronized (this.executionLog) {
			if (this.getLogger() == null) {
				return false;
			} else {
				return this.getLogger().isLogging();
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#toggleLogging()
	 */
	@Override
	public void			toggleLogging()
	{
		synchronized (this.executionLog) {
			assert	this.isLoggerSet() : new PreconditionException("isLoggerSet()");
			boolean	logging_at_pre = this.isLogging();

			this.getLogger().toggleLogging();

			assert	this.isLogging() == !logging_at_pre :
						new PostconditionException(
								"isLogging() == !isLogging()@pre");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#logMessage(java.lang.String)
	 */
	@Override
	public void			logMessage(String message)
	{
		synchronized (this.executionLog) {
			if (this.isLogging()) {
				this.getLogger().logMessage(message);
			}
			if (this.isTracing()) {
				this.getTracer().traceMessage(System.currentTimeMillis() + "|" +
										  message + "\n");
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#printExecutionLog()
	 */
	@Override
	public void			printExecutionLog()
	{
		assert	this.isLoggerSet() : new PreconditionException("isLoggerSet()");
		try {
			this.getLogger().printExecutionLog();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#printExecutionLogOnFile(java.lang.String)
	 */
	@Override
	public void			printExecutionLogOnFile(String fileName)
	throws FileNotFoundException
	{
		assert	this.isLoggerSet() : new PreconditionException("isLoggerSet()");
		assert	fileName != null :
					new PreconditionException("fileName != null");

		this.getLogger().printExecutionLogOnFile(fileName);
	}

	/**
	 * return the current tracer.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	the current tracer.
	 */
	protected TracerWindow	getTracer()
	{
		return this.tracer.get();
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#setTracer(fr.sorbonne_u.components.helpers.TracerWindow)
	 */
	@Override
	public void			setTracer(TracerWindow tracer)
	{
		this.tracer.set(tracer);
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isTracerSet()
	 */
	@Override
	public boolean		isTracerSet()
	{
		return this.getTracer() != null;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#toggleTracing()
	 */
	@Override
	public void			toggleTracing()
	{
		synchronized (this.tracer) {
			assert	this.isTracerSet() :
						new PreconditionException("isTracerSet()");

			boolean tracing_at_pre = this.isTracing();

			this.getTracer().toggleTracing();

			assert	this.isTracing() == !tracing_at_pre :
						new PostconditionException(
								"isTracing() == ! isTracing()@pre");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#traceMessage(java.lang.String)
	 */
	@Override
	public void			traceMessage(String message)
	{
		synchronized (this.tracer) {
			if (this.isTracing()) {
				this.tracer.get().traceMessage(
							System.currentTimeMillis() + "|" + message);
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isTracing()
	 */
	@Override
	public boolean		isTracing()
	{
		synchronized (this.tracer) {
			if (this.getTracer() == null) {
				return false;
			} else {
				return this.getTracer().isTracing();
			}
		}
	}

	// -------------------------------------------------------------------------
	// Creation, constructors, invariant
	// -------------------------------------------------------------------------

	/** URI of the (unique) reflection inbound port of the component.		*/
	protected final String		reflectionInboundPortURI;

	/**
	 * create a passive component if both <code>nbThreads</code> and
	 * <code>nbSchedulableThreads</code> are both zero, and an active one with
	 * <code>nbThreads</code> non schedulable thread and
	 * <code>nbSchedulableThreads</code> schedulable threads otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <p>
	 * The first precondition forbids the component to implement (in the Java
	 * sense) an interface that is a component interface. In BCM, this is
	 * forbidden as it would make possible to use a component object as a port
	 * object, hence leading to confusions in the model concepts. In BCM, only
	 * ports can implement (in the Java sense) a component interface and
	 * components offer or require component interfaces through inbound or
	 * outbound ports.
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code nbThreads >= 0 && nbSchedulableThreads >= 0}
	 * post	true		// no postcondition.
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
		this(AbstractPort.generatePortURI(ReflectionCI.class),
											nbThreads, nbSchedulableThreads);
	}

	/**
	 * create a passive component if both <code>nbThreads</code> and
	 * <code>nbSchedulableThreads</code> are both zero, and an active one with
	 * <code>nbThreads</code> non schedulable thread and
	 * <code>nbSchedulableThreads</code> schedulable threads otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <p>
	 * The first precondition forbids the component to implement (in the Java
	 * sense) an interface that is a component interface. In BCM, this is
	 * forbidden as it would make possible to use a component object as a port
	 * object, hence leading to confusions in the model concepts. In BCM, only
	 * ports can implement (in the Java sense) a component interface and
	 * components offer or require component interfaces through inbound or
	 * outbound ports.
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null}
	 * pre	{@code nbThreads >= 0}
	 * pre	{@code nbSchedulableThreads >= 0}
	 * post	true		// no postcondition.
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
		super();

		assert	!(this instanceof ComponentInterface) :
					new PreconditionException(
							"The new component implements (in the Java sense) a"
							+ " component interface; this is forbidden by BCM!");
		assert	reflectionInboundPortURI != null :
					new PreconditionException("Reflection inbound port URI is"
																+ " null!");
		assert	nbThreads >= 0 :
					new PreconditionException("Number of threads is negative!");
		assert	nbSchedulableThreads >= 0 :
					new PreconditionException("Number of schedulable threads"
														+ " is negative!");

		this.reflectionInboundPortURI = reflectionInboundPortURI;
		this.innerComponents =
						new ConcurrentHashMap<String,AbstractComponent>();

		this.executorServicesIndexes = new HashMap<String,Integer>();
		this.executorServices =
			new AtomicReference<ComponentExecutorServiceManager[]>(null);
		this.requiredInterfaces = new Vector<Class<? extends RequiredCI>>();
		this.offeredInterfaces = new Vector<Class<? extends OfferedCI>>();
		this.interfaces2ports =
			new Hashtable<Class<? extends ComponentInterface>,Vector<PortI>>();
		this.portURIs2ports = new Hashtable<String, PortI>();
		this.executionLog =
				new AtomicReference<>(new Logger(reflectionInboundPortURI));
		this.tracer =
				new AtomicReference<>(
						new TracerWindow(reflectionInboundPortURI, 0, 0));

		this.state = new AtomicReference<>(ComponentState.INITIALISED);
		this.composite = new AtomicReference<>(null);

		try {
			this.installedPlugins = new AtomicReference<>(null);
			this.configurePluginFacilities();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (nbThreads > 0) {
			this.createNewExecutorService(STANDARD_REQUEST_HANDLER_URI,
										  nbThreads, false);
		}
		
		if (nbSchedulableThreads > 0) {
			this.createNewExecutorService(STANDARD_SCHEDULABLE_HANDLER_URI,
										  nbSchedulableThreads, true);
		}

		try {
			this.configureReflection(reflectionInboundPortURI);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		this.addInterfacesFromAnnotations();
		this.addPluginsFromAnnotations();

		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
	}

	/**
	 * check the implementation invariant of the given component object.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ac != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param ac	component to be checked.
	 */
	protected static void	checkImplementationInvariant(AbstractComponent ac)
	{
		assert	ac != null : new PreconditionException("ac != null");

		// From subcomponent management
		synchronized (ac.composite) {
			assert	(ac.composite.get() == null) ||
									ac.isSubcomponentOf(ac.composite.get()) :
						new ImplementationInvariantException(
								"(composite.get() == null) || " + 
								"isSubcomponentOf(composite.get())");
		}

		assert	ac.innerComponents != null :
					new ImplementationInvariantException(
							"innerComponents != null");

		synchronized (ac.innerComponents) {
			for (String uri : ac.innerComponents.keySet()) {
				assert	ac.hasSubcomponent(uri) &&
										ac.innerComponents.get(uri) != null :
							new ImplementationInvariantException(
									"hasSubcomponent(uri) && " + 
									"innerComponents.get(uri) != null");
			}
		}

		// From executor services management
		ac.executorServicesLock.readLock().lock();
		try {
			assert	ac.hasItsOwnThreads() == (ac.getTotalNumberOfThreads() > 0) :
						new ImplementationInvariantException(
								"hasItsOwnThreads() == "
								+ "(getTotalNumberOfThreads() > 0)");

			boolean uri2index = true;
			for (Entry<String,Integer> entry :
									ac.executorServicesIndexes.entrySet()) {
				String uri = entry.getKey();
				int index = entry.getValue();
				uri2index = uri2index &&
						ac.executorServices.get()[index].getURI().equals(uri);
			}
			assert	uri2index :
						new ImplementationInvariantException(
								"executorServices and executorServicesIndexes"
								+ " not in synchrony!");

			if (ac.executorServices.get() != null) {
				for (int i = 0 ; i < ac.executorServices.get().length ; i++) {
					if (ac.executorServices.get()[i] != null) {
						assert	ac.executorServicesIndexes.containsValue(i) :
									new ImplementationInvariantException(
										i + " is a valid executor service "
										+ "index but is not in "
										+ "executorServicesIndexes");
					} else {
						assert	!ac.executorServicesIndexes.containsValue(i) :
									new ImplementationInvariantException(
										i + " is not  a valid executor service "
										+ "index but is in "
										+ "executorServicesIndexes");
					}
				}
			}
		} finally {
			ac.executorServicesLock.readLock().unlock();
		}

		// For plug-in management
		assert	ac.installedPlugins.get() != null :
					new ImplementationInvariantException(
							"installedPlugins.get() != null");

		// For component interfaces and ports management
		ac.interfaceManagementLock.readLock().lock();
		ac.portManagementLock.readLock().lock();
		try {
			assert	ac.requiredInterfaces != null :
						new ImplementationInvariantException(
								"requiredInterfaces != null");
			assert	ac.offeredInterfaces != null :
						new ImplementationInvariantException(
								"offeredInterfaces != null");
			assert	ac.interfaces2ports != null :
						new ImplementationInvariantException(
								"interfaces2ports != null");
			assert	ac.portURIs2ports != null :
						new ImplementationInvariantException(
								"portURIs2ports != null");

			for (Class<? extends ComponentInterface> inter :
												ac.interfaces2ports.keySet()) {
				assert	ac.isInterface(inter) :
							new ImplementationInvariantException(
									"component interface " + inter +
									" is implemented by a port but not "
									+ "declared by the component!");
			}

			for (PortI p : ac.portURIs2ports.values()) {
				try {
					assert	ac.isInterface(p.getImplementedInterface()) :
								new ImplementationInvariantException(
										"port " + p.getPortURI() +
										" is in the component but its " +
										"interface is not declared by the " +
										"latter!");
					assert	ac.interfaces2ports.get(
									p.getImplementedInterface()).contains(p) :
								new ImplementationInvariantException(
										"port " + p.getPortURI() +
										" is in the component but not " +
										"associated to its implemented " +
										"interface " +
										p.getImplementedInterface() + "!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			for (Entry<Class<? extends ComponentInterface>,Vector<PortI>> entry :
											ac.interfaces2ports.entrySet()) {
				Class<? extends ComponentInterface> ci = entry.getKey();
				Vector<PortI> ps = entry.getValue();
				for (PortI p : ps) {
					try {
						assert	p.getImplementedInterface() == ci :
									new ImplementationInvariantException(
											"port " + p.getPortURI() +
											" is not registered under its "
											+ "implemented interface " +
											p.getImplementedInterface()
											+ " but under " + ci + "!");
						assert	ac.portURIs2ports.containsKey(p.getPortURI()) :
									new ImplementationInvariantException(
											"port " +p.getPortURI() +
											" is registered under its implement"
											+ " interface " + ci + " but its" +
											" URI is unknown to the component!");
						assert	ac.portURIs2ports.get(p.getPortURI()) == p :
									new ImplementationInvariantException(
											"port " +p.getPortURI() +
											" is registered under its implement"
											+ " interface " + ci + " but not" +
											" under its URI!");
					} catch (Exception e) {
						throw new RuntimeException(e) ;
					}
				}
			}

			for (Entry<String,PortI> entry : ac.portURIs2ports.entrySet()) {
				String uri = entry.getKey();
				PortI p = entry.getValue();
				try {
					assert	uri.equals(p.getPortURI()) :
								new ImplementationInvariantException(
										"port " + p.getPortURI() + " is not " +
										"registered under its URI but under "
										+ uri + "!");
					assert	ac.interfaces2ports.get(
									p.getImplementedInterface()).contains(p) :
								new ImplementationInvariantException(
										"port " + p.getPortURI() +
										" is registered under its URI but not "
										+ "under its implemented interface " +
										p.getImplementedInterface() + "!");
				} catch (Exception e) {
					throw new RuntimeException(e) ;
				}
			}
		} finally {
			ac.portManagementLock.readLock().unlock();
			ac.interfaceManagementLock.readLock().unlock();
		}
	}


	/**
	 * check the invariant of component objects.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ac != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param ac	component object on which the invariant is checked.
	 */
	protected static void	checkInvariant(AbstractComponent ac)
	{
		assert	ac != null;

		// Component class properties
		assert	AbstractComponentHelper.isComponentClass(ac.getClass()) :
					new InvariantException(
							ac.getClass() + " does not conform to a class "
									+ "defining a BCM component!");

		// Internal parallelism management
		ac.executorServicesLock.readLock().lock();
		try {
			assert	ac.hasItsOwnThreads() == ac.getTotalNumberOfThreads() > 0 :
					new InvariantException(
							"hasItsOwnThreads() == "
									+ "getTotalNumberOfThreads() > 0");
		} finally {
			ac.executorServicesLock.readLock().unlock();
		}

		// Logger and tracer management
		assert	ac.getLogger() != null || !ac.isLogging() :
					new InvariantException(
							"getLogger() != null || !isLogging()");
		assert	ac.getLogger() == null ||
								(ac.isLogging() == ac.getLogger().isLogging()) :
					new InvariantException(
							"getLogger() == null || " + 
							"(ac.isLogging() == ac.getLogger().isLogging())");

		// For component interfaces management
		ac.interfaceManagementLock.readLock().lock();
		try {
			Class<? extends ComponentInterface>[] dis = ac.getInterfaces();
			Class<? extends RequiredCI>[] rcis = ac.getRequiredInterfaces();
			Class<? extends OfferedCI>[] ocis = ac.getOfferedInterfaces();
			for (Class<? extends RequiredCI> rci : rcis) {
				assert	appearsIn(rci, dis, dis.length) :
							new InvariantException(
									rci + " is a required component interface "
									+ "but not an interface of the component!");
			for (Class<? extends OfferedCI> oci : ocis) {
				assert	appearsIn(oci, dis, dis.length) :
							new InvariantException(
									oci + " is an offered component interface "
									+ "but not an interface of the component!");
				}
			}
			for (Class<? extends ComponentInterface> di : dis) {
				assert	appearsIn(di, rcis, rcis.length) ||
											appearsIn(di, ocis, ocis.length) :
							new InvariantException(
									di + " is a component interface but neither"
									+ " a required nor an offered one!");
			}
		} finally {
			ac.interfaceManagementLock.readLock().unlock();
		}
	}

	/**
	 * return true if the value appears in the array.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code array != null}
	 * pre	{@code length >= 0}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param value		value to be checked.
	 * @param array		array in which value is looked for.
	 * @param length	length of the array.
	 * @return			true if the value appears in the array.
	 */
	private static boolean	appearsIn(Object value, Object array, int length)
	{
		assert	array != null : new PreconditionException("array != null");
		assert	length >= 0 : new PreconditionException("length >= 0");

		boolean ret = false;
		for (int i = 0 ; !ret && i < length ; i++) {
			ret = Array.get(array, i) == value;
		}
		return ret;
	}

	/**
	 * automatically declare the required and offered interface using the
	 * information given in the corresponding annotations.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 */
	protected void		addInterfacesFromAnnotations()
	{
		RequiredInterfaces requiredAnnotation =
					this.getClass().getAnnotation(RequiredInterfaces.class);
		if (requiredAnnotation != null) {
			Class<? extends RequiredCI>[] required =
											requiredAnnotation.required();
			if (required != null) {
				for (int i = 0; i < required.length; i++) {
					this.addRequiredInterface(required[i]);
				}
			}
		}
		OfferedInterfaces offeredAnnotation =
					this.getClass().getAnnotation(OfferedInterfaces.class);
		if (offeredAnnotation != null) {
			Class<? extends OfferedCI>[] offered = offeredAnnotation.offered();
			if (offered != null) {
				for (int i = 0; i < offered.length; i++) {
					this.addOfferedInterface(offered[i]);
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
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 */
	protected void		addPluginsFromAnnotations()
	{
		assert	this.isPluginFacilitiesConfigured() :
					new PreconditionException("Can't install plug-ins, "
							+ "plug-in facilities are not configured!");

		try {
			AddPlugins pluginsAnnotation =
					this.getClass().getAnnotation(AddPlugins.class);
			if (pluginsAnnotation != null) {
				AddPlugin[] pluginAnnotations = pluginsAnnotation.pluginList();
				if (pluginAnnotations != null) {
					for (int i = 0; i < pluginAnnotations.length; i++) {
						String uri = pluginAnnotations[i].pluginURI();
						Class<? extends PluginI> pluginClass =
								pluginAnnotations[i].pluginClass();
						PluginI p = pluginClass.newInstance();
						p.setPluginURI(uri);
						this.installPlugin(p);
					}
				}
			}
			AddPlugin pluginAnnotation =
								this.getClass().getAnnotation(AddPlugin.class);
			if (pluginAnnotation != null) {
				String uri = pluginAnnotation.pluginURI();
				Class<? extends PluginI> pluginClass =
											pluginAnnotation.pluginClass();
				PluginI p = pluginClass.newInstance();
				p.setPluginURI(uri);
				this.installPlugin(p);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
	 * pre	{@code reflectionInboundPortURI != null}
	 * post	{@code isOfferedInterface(ReflectionI.class)}
	 * post	{@code findInboundPortURIsFromInterface(ReflectionI.class) != null}
	 * post	{@code findInboundPortURIsFromInterface(ReflectionI.class).length == 1}
	 * post	{@code findInboundPortURIsFromInterface(ReflectionI.class)[0].equals(reflectionInboundPortURI)}
	 * </pre>
	 * 
	 * @param reflectionInboundPortURI	URI of the reflection inbound port to be created.
	 * @throws Exception				<i>to do</i>.
	 */
	protected void		configureReflection(String reflectionInboundPortURI)
	throws Exception
	{
		assert	reflectionInboundPortURI != null :
					new PreconditionException(
							"reflectionInboundPortURI != null");

		this.addOfferedInterface(ReflectionCI.class);
		
		try {
			ReflectionInboundPort rip =
					new ReflectionInboundPort(reflectionInboundPortURI, this);
			rip.publishPort();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		assert	this.isOfferedInterface(ReflectionCI.class) :
					new PostconditionException(
							"isOfferedInterface(ReflectionI.class)");
		assert	this.findInboundPortURIsFromInterface(ReflectionCI.class)
																	!= null :
					new PostconditionException(
							"findInboundPortURIsFromInterface("
									+ "ReflectionI.class) != null");
		assert	this.findInboundPortURIsFromInterface(ReflectionCI.class).length
																		== 1 :
					new PostconditionException(
							"findInboundPortURIsFromInterface("
									+ "ReflectionI.class).length == 1");
		assert	this.findInboundPortURIsFromInterface(
						ReflectionCI.class)[0].equals(reflectionInboundPortURI) :
					new PostconditionException(
							"findInboundPortURIsFromInterface("
									+ "ReflectionI.class)[0].equals("
									+ "reflectionInboundPortURI)");
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
	 * pre	{@code classname != null && constructorParams != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param classname						name of the class from which the component is created.
	 * @param constructorParams				parameters to be passed to the constructor.
	 * @return								the URI of the reflection inbound port of the new component.
	 * @throws InvocationTargetException	<i>to do</i>.
	 * @throws IllegalArgumentException		<i>to do</i>.
	 * @throws IllegalAccessException		<i>to do</i>.
	 * @throws InstantiationException		<i>to do</i>.
	 * @throws SecurityException			<i>to do</i>.
	 * @throws NoSuchMethodException		<i>to do</i>.
	 * @throws ClassNotFoundException		<i>to do</i>.
	 * @throws Exception					if the creation did not succeed.
	 */
	public static String	createComponent(
		String classname,
		Object[] constructorParams
		) throws	ClassNotFoundException,
					NoSuchMethodException,
					SecurityException,
					InstantiationException,
					IllegalAccessException,
					IllegalArgumentException,
					InvocationTargetException,
					Exception
	{
		assert	classname != null && constructorParams != null :
					new PreconditionException(
							"classname != null && constructorParams != null");

		ComponentI component =
				instantiateComponent(classname, constructorParams);
		String[] ret =
				component.findInboundPortURIsFromInterface(ReflectionCI.class);
		assert	ret != null && ret.length == 1 && ret[0] != null;

		AbstractCVM.getCVM().addDeployedComponent(ret[0], component);
		
		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			AbstractCVM.getCVM().logDebug(
					CVMDebugModes.LIFE_CYCLE,
					"creating component " + ret[0] + " ...done.");
		}

		return ret[0];
	}

	/**
	 * instantiate a component instantiated from the class of the given class
	 * name and initialised by the constructor which parameters are given.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code classname != null && constructorParams != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param classname						name of the class from which the component is created.
	 * @param constructorParams				parameters to be passed to the constructor.
	 * @return								the Java reference on the object representing the new component.
	 * @throws ClassNotFoundException		<i>to do</i>.
	 * @throws SecurityException			<i>to do</i>.
	 * @throws NoSuchMethodException		<i>to do</i>.
	 * @throws InvocationTargetException	<i>to do</i>.
	 * @throws IllegalArgumentException		<i>to do</i>.
	 * @throws IllegalAccessException		<i>to do</i>.
	 * @throws InstantiationException		<i>to do</i>.
	 */
	protected static ComponentI	instantiateComponent(
		String classname,
		Object[] constructorParams
		) throws	ClassNotFoundException,
					NoSuchMethodException,
					SecurityException,
					InstantiationException,
					IllegalAccessException,
					IllegalArgumentException,
					InvocationTargetException
	{
		assert	classname != null && constructorParams != null :
			new PreconditionException(
					"classname != null && constructorParams != null");

		Class<?> cl = Class.forName(classname);
		assert	cl != null && AbstractComponentHelper.isComponentClass(cl);
		Constructor<?> cons =
			AbstractComponentHelper.getConstructor(cl, constructorParams);
		assert	cons != null;
		cons.setAccessible(true);
		AbstractComponent component =
			(AbstractComponent)cons.newInstance(constructorParams);

		return component;
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
	 * pre	{@code classname != null && constructorParams != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param classname						name of the class from which the component is created.
	 * @param constructorParams				parameters to be passed to the constructor.
	 * @return								the URI of the reflection inbound port of the new component.
	 * @throws InvocationTargetException	<i>to do</i>.
	 * @throws IllegalArgumentException		<i>to do</i>.
	 * @throws IllegalAccessException		<i>to do</i>.
	 * @throws InstantiationException		<i>to do</i>.
	 * @throws SecurityException			<i>to do</i>.
	 * @throws NoSuchMethodException		<i>to do</i>.
	 * @throws ClassNotFoundException		<i>to do</i>.
	 * @throws Exception					if the creation did not succeed.
	 */
	protected String	createSubcomponent(
		String classname,
		Object[] constructorParams
		) throws	ClassNotFoundException,
					NoSuchMethodException,
					SecurityException,
					InstantiationException,
					IllegalAccessException,
					IllegalArgumentException,
					InvocationTargetException,
					Exception
	{
		assert	classname != null && constructorParams != null :
					new PreconditionException(
							"classname != null && constructorParams != null");

		ComponentI component =
				instantiateComponent(classname, constructorParams);
		String[] ret =
				component.findInboundPortURIsFromInterface(ReflectionCI.class);
		assert	ret != null && ret.length == 1 && ret[0] != null;

		this.innerComponents.put(ret[0], (AbstractComponent)component);
		((AbstractComponent)component).setCompositeComponentReference(this);
		
		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			AbstractCVM.getCVM().logDebug(
					CVMDebugModes.LIFE_CYCLE,
					"creating subcomponent " + ret[0] + " ...done.");
		}

		return ret[0];
	}

	// -------------------------------------------------------------------------
	// Component interfaces management
	// -------------------------------------------------------------------------

	// Invariants
	//     forall ci in getRequiredInterfaces() : ci appearsIn getInterfaces()
	//     forall ci in getOfferedInterfaces() : ci appearsIn getInterfaces()
	//     forall ci in getInterfaces()
	//         ci appearsIn getRequiredInterfaces() ||
	//                         ci appearsIn getOfferedInterfaces()

	/** lock protecting the access to the component interfaces management
	 *  data structures.													*/
	protected final ReentrantReadWriteLock	interfaceManagementLock =
												new ReentrantReadWriteLock();
	/** class objects representing all the required interfaces implemented
	 *  by this component.													*/
	protected final Vector<Class<? extends RequiredCI>>	requiredInterfaces;
	/** class objects representing all the offered interfaces implemented
	 * by this component.													*/
	protected final Vector<Class<? extends OfferedCI>>	offeredInterfaces;

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getInterfaces()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends ComponentInterface>[]	getInterfaces()
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");

		this.interfaceManagementLock.readLock().lock();
		try {
			ArrayList<Class<?>> temp = new ArrayList<Class<?>>();
			temp.addAll(this.requiredInterfaces);
			temp.addAll(this.offeredInterfaces);
			return (Class<? extends ComponentInterface>[])
											temp.toArray(new Class<?>[]{});
		} finally {
			this.interfaceManagementLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getInterface(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends ComponentInterface>	getInterface(
		Class<? extends ComponentInterface> inter
		)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	inter != null : new PreconditionException("inter != null");

		this.interfaceManagementLock.readLock().lock();
		try {
			Class<? extends ComponentInterface> ret =
				this.getRequiredInterface((Class<? extends RequiredCI>) inter);
			if (ret == null) {
				ret = this.getOfferedInterface(
										  (Class<? extends OfferedCI>) inter);
			}
			return ret;
		} finally {
			this.interfaceManagementLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getRequiredInterfaces()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends RequiredCI>[]	getRequiredInterfaces()
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");

		this.interfaceManagementLock.readLock().lock();
		try {
			Class<? extends RequiredCI>[] ret;
			ret = (Class<? extends RequiredCI>[])
							this.requiredInterfaces.toArray(new Class<?>[]{});
			return ret;
		} finally {
			this.interfaceManagementLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getRequiredInterface(java.lang.Class)
	 */
	@Override
	public Class<? extends RequiredCI>	getRequiredInterface(
		Class<? extends RequiredCI> inter
		)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	inter != null : new PreconditionException("inter != null");

		this.interfaceManagementLock.readLock().lock();
		try {
			Class<? extends RequiredCI> ret = null;
			boolean found = false;
			for(int i = 0; !found && i < this.requiredInterfaces.size(); i++) {
				if (inter.isAssignableFrom(this.requiredInterfaces.get(i))) {
					found = true;
					ret = this.requiredInterfaces.get(i);
				}
			}
			return ret;
		} finally {
			this.interfaceManagementLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getOfferedInterfaces()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends OfferedCI>[]	getOfferedInterfaces()
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");

		this.interfaceManagementLock.readLock().lock();
		try {
			Class<? extends OfferedCI>[] ret;
			ret = (Class<? extends OfferedCI>[])
							this.offeredInterfaces.toArray(new Class<?>[]{});
			return ret;
		} finally {
			this.interfaceManagementLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getOfferedInterface(java.lang.Class)
	 */
	@Override
	public Class<? extends OfferedCI>	getOfferedInterface(
		Class<? extends OfferedCI> inter
		)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	inter != null : new PreconditionException("inter != null");

		this.interfaceManagementLock.readLock().lock();
		try {
			Class<? extends OfferedCI> ret = null;
			boolean found = false;
			for(int i = 0; !found && i < this.offeredInterfaces.size(); i++) {
				if (inter.isAssignableFrom(this.offeredInterfaces.get(i))) {
					found = true;
					ret = this.offeredInterfaces.get(i);
				}
			}
			return ret;
		} finally {
			this.interfaceManagementLock.readLock().unlock();
		}
	}

	/**
	 * add a required interface to the required interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	!isRequiredInterface(inter)
	 * post	isRequiredInterface(inter)
	 * </pre>
	 *
	 * @param inter	required interface to be added.
	 */
	protected void		addRequiredInterface(Class<? extends RequiredCI> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	inter != null : new PreconditionException("inter != null");

		this.interfaceManagementLock.writeLock().lock();
		try {
			assert	!this.isRequiredInterface(inter) :
						new PreconditionException(
								inter + " is already a required interface!");

			this.requiredInterfaces.add(inter);

			assert	this.isRequiredInterface(inter) :
						new PostconditionException(
								inter + " has not been correctly added "
										+ "as a required interface!");

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.INTERFACES)) {
				AbstractCVM.getCVM().logDebug(
						CVMDebugModes.INTERFACES,
						"adding required interface " + inter +
						" to the component " + this.reflectionInboundPortURI +
						" ...done.");
			}
		} finally {
			this.interfaceManagementLock.writeLock().unlock();
		}
	}

	/**
	 * remove a required interface from the required interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	isRequiredInterface(inter)
	 * pre	findPortsFromInterface(inter) == null || findPortsFromInterface(inter).isEmpty()
	 * post	!isRequiredInterface(inter)
	 * </pre>
	 *
	 * @param inter required interface to be removed.
	 */
	protected void		removeRequiredInterface(
		Class<? extends RequiredCI> inter
		)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	inter != null : new PreconditionException("inter != null");

		this.interfaceManagementLock.writeLock().lock();
		try {
			assert	this.isRequiredInterface(inter) :
						new PreconditionException(
								inter + " is not a required interface!");

			this.requiredInterfaces.remove(inter);

			assert	!this.isRequiredInterface(inter) :
						new PostconditionException(inter + " has not been "
								+ "correctly removed as a required interface!");

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.INTERFACES)) {
				AbstractCVM.getCVM().logDebug(
						CVMDebugModes.INTERFACES,
						"removing required interface " + inter +
						" from the component " + this.reflectionInboundPortURI +
						" ...done.");
			}
		} finally {
			this.interfaceManagementLock.writeLock().unlock();
		}
	}

	/**
	 * add an offered interface to the offered interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	!this.isOfferedInterface(inter)
	 * post	this.isOfferedInterface(inter)
	 * </pre>
	 *
	 * @param inter offered interface to be added.
	 */
	protected void		addOfferedInterface(Class<? extends OfferedCI> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	inter != null : new PreconditionException("inter != null");

		this.interfaceManagementLock.writeLock().lock();
		try {
			assert	!this.isOfferedInterface(inter) :
						new PreconditionException(
								inter + " must not be an offered interface!");

			this.offeredInterfaces.add(inter);

			assert	this.isOfferedInterface(inter) :
						new PostconditionException(
								inter + " has not been correctly added "
										+ "as an offered interface!");

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.INTERFACES)) {
				AbstractCVM.getCVM().logDebug(
						CVMDebugModes.INTERFACES,
						"adding offered interface " + inter +
						" to the component " + this.reflectionInboundPortURI +
						" ...done.");
			}
		} finally {
			this.interfaceManagementLock.writeLock().unlock();
		}
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
	protected void		removeOfferedInterface(
		Class<? extends OfferedCI> inter
		)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	inter != null : new PreconditionException("inter != null");

		this.interfaceManagementLock.writeLock().lock();
		try {
			assert	this.isOfferedInterface(inter) :
						new PreconditionException(
								inter + " is not an offered interface!");

			this.offeredInterfaces.remove(inter);

			assert	!this.isOfferedInterface(inter) :
						new PostconditionException(
								inter + " has not been correctly removed "
										+ "as an offered interface!");

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.INTERFACES)) {
				AbstractCVM.getCVM().logDebug(
						CVMDebugModes.INTERFACES,
						"removing offered interface " + inter +
						" from the component " + this.reflectionInboundPortURI +
						" ...done.");
			}
		} finally {
			this.interfaceManagementLock.writeLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isInterface(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean		isInterface(Class<? extends ComponentInterface> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	inter != null : new PreconditionException("inter != null");

		this.interfaceManagementLock.readLock().lock();
		try {
			boolean ret = false;
			if (RequiredCI.class.isAssignableFrom(inter)) {
				ret = this.isRequiredInterface(
										(Class<? extends RequiredCI>)inter);
			}
			if (!ret && OfferedCI.class.isAssignableFrom(inter)) {
				ret = this.isOfferedInterface(
										(Class<? extends OfferedCI>)inter);
			}
			return ret;
		} finally {
			this.interfaceManagementLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isRequiredInterface(java.lang.Class)
	 */
	@Override
	public boolean		isRequiredInterface(Class<? extends RequiredCI> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	inter != null : new PreconditionException("inter != null");

		this.interfaceManagementLock.readLock().lock();
		try {
			boolean ret = false;
			for(int i = 0; !ret && i < this.requiredInterfaces.size(); i++) {
				if (inter.isAssignableFrom(this.requiredInterfaces.get(i))) {
					ret = true;
				}
			}
			return ret;
		} finally {
			this.interfaceManagementLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isOfferedInterface(java.lang.Class)
	 */
	@Override
	public boolean		isOfferedInterface(Class<? extends OfferedCI> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	inter != null : new PreconditionException("inter != null");

		this.interfaceManagementLock.readLock().lock();
		try {
			boolean ret = false;
			for(int i = 0; !ret && i < this.offeredInterfaces.size(); i++) {
				if (inter.isAssignableFrom(this.offeredInterfaces.get(i))) {
					ret = true;
				}
			}
			return ret;
		} finally {
			this.interfaceManagementLock.readLock().unlock();
		}
	}

	// -------------------------------------------------------------------------
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
	// -------------------------------------------------------------------------

	// Implementation invariant
	//     for (Entry<Class<? extends ComponentInterface>,Vector<PortI>> entry :
	//                                       this.interfaces2ports.entrySet()) {
	//         Class<? extends ComponentInterface> ci = entry.getKey();
	//         Vector<PortI> ps = entry.getValue();
	//         for (PortI p : ps) {
	//             try {
	//                 assert p.getImplementedInterface() == ci;
	//                 assert portURIs2ports.containsKey(p.getPortURI());
	//                 assert portURIs2ports.get(p.getPortURI()) == p;
	//             } catch (Exception e) {
	//                 throw new RuntimeException(e) ;
	//             }
	//         }
	//     }
	//     for (Entry<String,PortI> entry : this.portURIs2ports.entrySet()) {
	//         String uri = entry.getKey();
	//         PortI p = entry.getValue();
	//         try {
	//             assert uri.equals(p.getPortURI());
	//             assert interfaces2ports.get(p.getImplementedInterface()).contains(p);
	//         } catch (Exception e) {
	//             throw new RuntimeException(e) ;
	//         }
	//     }
	//
	// Invariant
	//

	/** lock protected the access to the port management data structures.	*/
	protected final ReentrantReadWriteLock	portManagementLock =
												new ReentrantReadWriteLock();
	/** a hashtable mapping interfaces implemented by this component to
	 *  vectors of ports to which one can connect using these interfaces.	*/
	protected Hashtable<Class<? extends ComponentInterface>,Vector<PortI>>
													interfaces2ports;
	/** a hashtable mapping URIs of ports owned by this component to
	 *  ports to which one can connect.										*/
	protected Hashtable<String,PortI>				portURIs2ports;

	/**
	 * find the ports of this component that expose the interface inter.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code inter != null}
	 * post	{@code ret == null || forall(PortI p : ret) { inter.equals(p.getImplementedInterface()) }}
	 * </pre>
	 *
	 * @param inter	interface for which ports are sought.
	 * @return		array of ports exposing inter.
	 */
	protected PortI[]	findPortsFromInterface(
		Class<? extends ComponentInterface> inter
		)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!");
		assert	inter != null :
					new PreconditionException("Interface is null!");		

		Vector<PortI> temp;

		this.portManagementLock.readLock().lock();
		try {
			temp = this.interfaces2ports.get(inter);
		} finally {
			this.portManagementLock.readLock().unlock();
		}

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.PORTS,
				new StringBuffer(
						"looking for ports implementing the interface ").
						append(inter).append(" on the component ").
						append(this.reflectionInboundPortURI).
						append(
							(temp == null ?
								" found none,"
							: 	new StringBuffer(" found [").
									append(temp.stream().map(t -> {
										try {
											return t.getPortURI();
										} catch (Exception e1) {
											throw new RuntimeException(e1) ;
										}
									}).collect(Collectors.joining(", "))).
																	toString()
							)).append("] ...done.").toString());
		}

		return temp != null ? temp.toArray(new PortI[]{}) : null;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getPortImplementedInterface(java.lang.String)
	 */
	@Override
	public Class<? extends ComponentInterface>	getPortImplementedInterface(
		String portURI
		) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException("Component must not be"
												+ " in Terminated state!");
		assert	portURI != null : new PreconditionException("portURI != null");
		
		this.portManagementLock.readLock().lock();
		try {
			assert	this.isPortExisting(portURI) :
						new PreconditionException(
								"isPortExisting(" + portURI + ")");

			return this.findPortFromURI(portURI).getImplementedInterface();
		} finally {
			this.portManagementLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#findPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findPortURIsFromInterface(
		Class<? extends ComponentInterface> inter
		) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	inter != null : new PreconditionException("inter != null");

		String[] ret = null;
		PortI[] ports = null;

		this.portManagementLock.readLock().lock();
		try {
			ports = this.findPortsFromInterface(inter);
		} finally {
			this.portManagementLock.readLock().unlock();
		}

		if (ports != null && ports.length > 0) {
			ret = new String[ports.length];
			for (int i = 0; i < ports.length; i++) {
				ret[i] = ports[i].getPortURI();
			}
		}

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.PORTS,
				new StringBuffer(
					"looking for URIs of ports implementing the interface ").
					append(inter).append(" on the component ").
					append(this.reflectionInboundPortURI).
					append(
						(ports == null ? " found none,"
						: 	new StringBuffer(" found [").
									append(Arrays.stream(ret).map(t -> t).
											collect(Collectors.joining(", "))).
																	toString()
							)).append("] ...done.").toString());
		}

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#findInboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findInboundPortURIsFromInterface(
		Class<? extends OfferedCI> inter
		) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	inter != null : new PreconditionException("inter != null");

		String[] ret = null;
		PortI[] ports = null;

		this.portManagementLock.readLock().lock();
		try {
			ports = this.findPortsFromInterface(inter);
		} finally {
			this.portManagementLock.readLock().unlock();
		}

		if (ports != null && ports.length > 0) {
			ArrayList<String> al = new ArrayList<String>();
			for (int i = 0; i < ports.length; i++) {
				if (ports[i] instanceof InboundPortI) {
					al.add(ports[i].getPortURI());
				}
			}
			ret = al.toArray(new String[0]);
		}

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.PORTS,
				new StringBuffer(
					"looking for URIs of inbound ports implementing").
					append(" the interface ").append(inter).
					append(" on the component ").
					append(this.reflectionInboundPortURI).
					append(
						(ports == null ? " found none,"
						: 	new StringBuffer(" found [").
									append(Arrays.stream(ret).map(t -> t).
											collect(Collectors.joining(", "))).
																	toString()
							)).append("] ...done.").toString());
		}

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#findOutboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findOutboundPortURIsFromInterface(
		Class<? extends RequiredCI> inter
		) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	inter != null : new PreconditionException("inter != null");

		String[] ret = null;
		PortI[] ports = null;

		this.portManagementLock.readLock().lock();
		try {
			ports = this.findPortsFromInterface(inter);
		} finally {
			this.portManagementLock.readLock().unlock();
		}

		if (ports != null && ports.length > 0) {
			ArrayList<String> al = new ArrayList<String>();
			for (int i = 0; i < ports.length; i++) {
				if (ports[i] instanceof OutboundPortI) {
					al.add(ports[i].getPortURI());
				}
			}
			ret = al.toArray(new String[0]);
		}

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.PORTS,
				new StringBuffer(
					"looking for URIs of outbound ports implementing").
					append(" the interface ").append(inter).
					append(" on the component ").
					append(this.reflectionInboundPortURI).
					append(
						(ports == null ? " found none,"
						: 	new StringBuffer(" found [").
									append(Arrays.stream(ret).map(t -> t).
											collect(Collectors.joining(", "))).
																	toString()
							)).append("] ...done.").toString());
		}

		return ret;
	}

	/**
	 * finds a port of this component from its URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code portURI != null}
	 * post	{@code ret == null || ret.getPortURI().equals(portURI)}
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
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	portURI != null :
					new PreconditionException("Port URI is null!");

		this.portManagementLock.readLock().lock();
		try {
			PortI ret = this.portURIs2ports.get(portURI);

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
				AbstractCVM.getCVM().logDebug(
					CVMDebugModes.PORTS,
					new StringBuffer("finding port with URI ").
						append(portURI).
						append(" on the component ").
						append(this.reflectionInboundPortURI).
						append(ret != null ? " found," : " not found,").
						append(" ...done.").toString());
			}

			return ret;
		} finally {
			this.portManagementLock.readLock().unlock();
		}
	}

	/**
	 * add a port to the set of ports of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code p != null}
	 * pre	{@code this.equals(p.getOwner())}
	 * pre	{@code isInterface(p.getImplementedInterface())}
	 * pre	{@code findPortFromURI(p.getPortURI()) == null}
	 * post {@code p.equals(findPortFromURI(p.getPortURI()))}
	 * </pre>
	 *
	 * @param p				port to be added.
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		addPort(PortI p) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	p != null : new PreconditionException("p != null");
		assert	this.equals(p.getOwner()) :
					new PreconditionException(
							"This component is not the owner of this port!");

		this.portManagementLock.writeLock().lock();
		try {
			assert	this.isInterface(p.getImplementedInterface()) :
						new PreconditionException(
								"The port doesn't implement an interface"
										+ " declared by this component!");
			assert	this.findPortFromURI(p.getPortURI()) == null :
						new PreconditionException(
								"A port with the same URI is already registered"
										+ " in this component!");

			Vector<PortI> vps =
					this.interfaces2ports.get(p.getImplementedInterface());
			if (vps == null) {
				vps = new Vector<PortI>();
				vps.add(p);
				this.interfaces2ports.put(p.getImplementedInterface(), vps);
			} else {
				synchronized (vps) {
					vps.add(p);
				}
			}

			this.portURIs2ports.put(p.getPortURI(), p);

			assert	this.interfaces2ports.containsKey(
											p.getImplementedInterface()) :
						new PostconditionException(
								"Port not correctly registered!");
			assert	this.portURIs2ports.containsKey(p.getPortURI()) :
						new PostconditionException(
								"Port not correctly registered!");
			assert	p.equals(this.findPortFromURI(p.getPortURI())) :
						new PostconditionException(
								"Port not correctly registered!");

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
				AbstractCVM.getCVM().logDebug(
					CVMDebugModes.PORTS,
					new StringBuffer("adding port with URI ").
						append(p.getPortURI()).
						append(" on the component ").
						append(this.reflectionInboundPortURI).
						append(" ...done.").toString());
			}
		} finally {
			this.portManagementLock.writeLock().unlock();
		}
	}

	/**
	 * remove a port from the set of ports of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code p != null}
	 * pre	{@code this.equals(p.getOwner())}
	 * pre	{@code exist(PortI p1 : this.findPortsFromInterface(p.getImplementedInterface())) { p1.equals(p)); }}
	 * post	{@code !exist(PortI p1 : this.findPortsFromInterface(p.getImplementedInterface())) { p1.equals(p)); }}
	 * </pre>
	 *
	 * @param p				port to be removed.
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		removePort(PortI p) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	p != null : new PreconditionException("p != null");
		assert	this.equals(p.getOwner()) :
					new PreconditionException(
							"This component is not the owner of this port!");

		this.portManagementLock.writeLock().lock();
		try {
			assert	this.interfaces2ports.containsKey(
												p.getImplementedInterface()) :
						new PreconditionException(
								"Port is not registered in this component!");
			assert	this.portURIs2ports.containsKey(p.getPortURI()) :
						new PreconditionException(
								"Port is not registered in this component!");

			Vector<PortI> vps =
						this.interfaces2ports.get(p.getImplementedInterface());
			synchronized (vps) {
				vps.remove(p);
				if (vps.isEmpty()) {
					this.interfaces2ports.remove(p.getImplementedInterface());
				}
			}
			this.portURIs2ports.remove(p.getPortURI());

			assert	!this.portURIs2ports.containsKey(p.getPortURI()) :
						new PostconditionException(
								"Port not correctly removed from component!");

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
				AbstractCVM.getCVM().logDebug(
					CVMDebugModes.PORTS,
					new StringBuffer("removing port with URI ").
						append(p.getPortURI()).
						append(" on the component ").
						append(this.reflectionInboundPortURI).
						append(" ...done.").toString());
			}
		} finally {
			this.portManagementLock.writeLock().unlock();
		}
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
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	portURI != null : new PreconditionException("portURI != null");

		this.portManagementLock.writeLock().lock();
		try {
			assert	this.isPortExisting(portURI) :
						new PreconditionException(
								"Can't remove non existing port : " + portURI);

			PortI p = this.findPortFromURI(portURI);
			this.removePort(p);

			assert	!this.isPortExisting(portURI) :
						new PostconditionException(
								"Port has not been correctly removed!");

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
				AbstractCVM.getCVM().logDebug(
					CVMDebugModes.PORTS,
					new StringBuffer("removing port with URI ").
						append(p.getPortURI()).
						append(" on the component ").
						append(this.reflectionInboundPortURI).
						append(" ...done.").toString());
			}
		} finally {
			this.portManagementLock.writeLock().unlock();
		}
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
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	portURI != null : new PreconditionException("portURI != null");

		this.portManagementLock.readLock().lock();
		try {
			PortI p = this.findPortFromURI(portURI);
			return p != null;
		} finally {
			this.portManagementLock.readLock().unlock();
		}
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
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	portURI != null : new PreconditionException("portURI != null");

		this.portManagementLock.readLock().lock();
		try {
			assert	this.isPortExisting(portURI) :
						new PreconditionException(portURI + " is not a port!");

			PortI p = this.findPortFromURI(portURI);
			return p.connected();
		} finally {
			this.portManagementLock.readLock().unlock();
		}
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
		assert	ccname != null : new PreconditionException("ccname != null");

		Class<?> cc = Class.forName(ccname);
		Constructor<?> c = cc.getConstructor(new Class<?>[]{});
		ConnectorI connector = (ConnectorI) c.newInstance();
		this.doPortConnection(portURI, otherPortURI, connector);
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
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	portURI != null : new PreconditionException("portURI != null");
		assert	otherPortURI != null :
					new PreconditionException("otherPortURI != null");
		assert	connector != null :
					new PreconditionException("connector != null");

		this.portManagementLock.readLock().lock();
		try {
			assert	this.isPortExisting(portURI) :
						new PreconditionException(portURI + " is not a port!");
			assert	!this.isPortConnected(portURI) :
						new PreconditionException(
								portURI + " is already connected!");

			PortI p = this.findPortFromURI(portURI);
			p.doConnection(otherPortURI, connector);

			assert	this.isPortConnected(portURI);

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CONNECTING)) {
				AbstractCVM.getCVM().logDebug(
					CVMDebugModes.CONNECTING,
					new StringBuffer("connecting port with URI ").
						append(portURI).
						append(" from the component ").
						append(this.reflectionInboundPortURI).
						append(" ...done.").toString());
			}
		} finally {
			this.portManagementLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#doPortDisconnection(java.lang.String)
	 */
	@Override
	public void			doPortDisconnection(String portURI) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) :
					new PreconditionException(
							"Component must not be in Terminated state!");
		assert	portURI != null : new PreconditionException("portURI != null");

		this.portManagementLock.readLock().lock();
		try {
			assert	this.isPortExisting(portURI) :
						new PreconditionException(
								"Can't disconnect non existing port : "
										+ portURI);
			assert	this.isPortConnected(portURI) :
						new PreconditionException(
								"Can't disconnect not connected port : "
										+ portURI);
	
			PortI p = this.findPortFromURI(portURI);
			p.doDisconnection();

			assert	!this.isPortConnected(portURI) :
						new PostconditionException(
								"Port has not been correctly disconnected!");

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CONNECTING)) {
				AbstractCVM.getCVM().logDebug(
					CVMDebugModes.CONNECTING,
					new StringBuffer("disconnecting port with URI ").
						append(p.getPortURI()).
						append(" from the component ").
						append(this.reflectionInboundPortURI).
						append(" ...done.").toString());
			}
		} finally {
			this.portManagementLock.readLock().unlock();
		}
	}

	// -------------------------------------------------------------------------
	// Component life cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ComponentI#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		assert	this.isInitialised() :
					new PreconditionException("isInitialised()");

		// Start inner components
		// assumes that the creation and publication are done
		// assumes that composite components always reside in one JVM
		for(ComponentI c : this.innerComponents.values()) {
			c.start();
		}

		this.state.set(ComponentState.STARTED);

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.LIFE_CYCLE,
				new StringBuffer("starting the component ").
					append(this.reflectionInboundPortURI).
					append(" ...done.").toString());
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#execute()
	 */
	@Override
	public synchronized void	execute() throws Exception
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");	

		for(ComponentI c : this.innerComponents.values()) {
			if (c.hasItsOwnThreads()) {
				c.runTask(
					new AbstractTask() {
						@Override
						public void run() {
							try {
								this.getTaskOwner().execute();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
			}
		}

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.LIFE_CYCLE,
				new StringBuffer("executing the component ").
					append(this.reflectionInboundPortURI).
					append(" ...done.").toString());
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");

		for(ComponentI c : this.innerComponents.values()) {
			c.finalise();
		}

		if (this.hasInstalledPlugins()) {
			for (Map.Entry<String,PluginI> e :
									this.installedPlugins.get().entrySet()) {			
				this.finalisePlugin(e.getKey());
			}
		}
		String[] reflPortURI =
				this.findInboundPortURIsFromInterface(ReflectionCI.class);
		PortI reflPort = this.findPortFromURI(reflPortURI[0]);
		reflPort.unpublishPort();

		this.state.set(ComponentState.FINALISED);

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.LIFE_CYCLE,
				new StringBuffer("finalising the component ").
					append(this.reflectionInboundPortURI).
					append(" ...done.").toString());
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		assert	this.isFinalised() : new PreconditionException("isFinalised()");

		// Shutdown inner components
		// assumes that all inner components are disconnected.
		for(ComponentI c : this.innerComponents.values()) {
			c.shutdown();
		}

		try {
			if (this.hasInstalledPlugins()) {
				for (Map.Entry<String,PluginI> e :
									this.installedPlugins.get().entrySet()) {
					this.uninstallPlugin(e.getKey());
				}
				this.installedPlugins.set(null);
			}
			ArrayList<PortI> toBeDestroyed =
					new ArrayList<PortI>(this.portURIs2ports.values());
			for (PortI p : toBeDestroyed) {
				p.destroyPort();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}

		final boolean isSubcomponent = this.composite.get() != null;
		final boolean isConcurrent = this.hasItsOwnThreads();
		Thread t = new Thread() {
				/**
				 * @see java.lang.Thread#run()
				 */
				@Override
				public void run() {
					if (executorServices.get() != null) {
						for (int i = 0; i < executorServices.get().length; i++)
						{
							if (executorServices.get()[i] != null) {
								executorServices.get()[i].shutdown();
							}
						}
					}
					state.set(ComponentState.SHUTTINGDOWN);
					// TODO: make sure the pools are really shut down.
					if (!isConcurrent) {
						state.set(ComponentState.SHUTDOWN);
					}
					if (!isSubcomponent) {
						AbstractCVM.getCVM().
							removeDeployedComponent(reflectionInboundPortURI);
					}
				}
			};
		t.start();

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.LIFE_CYCLE,
				new StringBuffer("shutting down the component ").
					append(this.reflectionInboundPortURI).
					append(" ...done.").toString());
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#shutdownNow()
	 */
	@Override
	public synchronized void	shutdownNow() throws ComponentShutdownException
	{
		assert	this.isFinalised() : new PreconditionException("isFinalised()");

		// Shutdown inner components
		// assumes that all inner components are disconnected.
		for(ComponentI c : this.innerComponents.values()) {
			c.shutdownNow();
		}

		try {
			if (this.hasInstalledPlugins()) {
				for (Map.Entry<String,PluginI> e :
									this.installedPlugins.get().entrySet()) {
					this.uninstallPlugin(e.getKey());
				}
				this.installedPlugins.set(null);
			}
			for (PortI p : this.portURIs2ports.values()) {
				p.destroyPort();
			}
		} catch (Exception e1) {
			throw new ComponentShutdownException(e1);
		}

		boolean isSubcomponent = this.composite.get() != null;
		Thread t = new Thread() {
				/**
				 * @see java.lang.Thread#run()
				 */
				@Override
				public void run() {
					for (int i = 0; i < executorServices.get().length; i++) {
						if (executorServices.get()[i] != null) {
							executorServices.get()[i].shutdown();
						}
					}
					state.set(ComponentState.SHUTDOWN);
					if (!isSubcomponent) {
						AbstractCVM.getCVM().
										removeDeployedComponent(
													reflectionInboundPortURI);
					}
				}
			};
		t.start();

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.LIFE_CYCLE,
				new StringBuffer("shutting down now the component ").
					append(this.reflectionInboundPortURI).
					append(" ...done.").toString());
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isInitialised()
	 */
	@Override
	public boolean		isInitialised()
	{
		return this.state.get() == ComponentState.INITIALISED;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isStarted()
	 */
	@Override
	public boolean		isStarted()
	{
		return this.state.get() == ComponentState.STARTED;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isFinalised()
	 */
	@Override
	public boolean		isFinalised()
	{
		return this.state.get() == ComponentState.FINALISED;
	}

	public boolean		isShuttingDown()
	{
		return this.state.get() == ComponentState.SHUTTINGDOWN;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isShutdown()
	 */
	@Override
	public boolean		isShutdown()
	{
		boolean isShutdown = true;

		if (this.state.get() == ComponentState.SHUTDOWN) {
			return true;
		}
		if (this.executorServices.get() == null) {
			return true;
		}

		this.executorServicesLock.readLock().lock();
		try {
			for (int i = 0 ; i < this.executorServices.get().length ; i++) {
				if (this.executorServices.get()[i] != null) {
					isShutdown = isShutdown &&
								this.executorServices.get()[i].isShutdown();
				}
			}
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
		if (isShutdown) {
			this.state.set(ComponentState.SHUTDOWN);
		}
		return isShutdown;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#isTerminated()
	 */
	@Override
	public boolean		isTerminated()
	{
		boolean isTerminated = true;

		if (this.state.get() == ComponentState.TERMINATED) {
			return true;
		}
		if (this.executorServices.get() == null) {
			return this.isShutdown();
		}

		this.executorServicesLock.readLock().lock();
		try {
			for (int i = 0 ; i < this.executorServices.get().length ; i++) {
				if (this.executorServices.get()[i] != null) {
					isTerminated = isTerminated &&
								this.executorServices.get()[i].isTerminated();
				}
			}
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
		if (isTerminated) {
			this.state.set(ComponentState.TERMINATED);
		}
		return isTerminated;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#awaitTermination(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public boolean		awaitTermination(long timeout, TimeUnit unit)
	throws	InterruptedException
	{
		if (this.state.get() == ComponentState.TERMINATED) {
			return true;
		}
		if (this.executorServices.get() == null) {
			return true;
		}

		boolean status = true;
		this.executorServicesLock.readLock().lock();
		try {
			for (int i = 0 ; i < this.executorServices.get().length ; i++) {
				if (this.executorServices.get()[i] != null) {
					status = status &&
							this.executorServices.get()[i].awaitTermination(
															timeout, unit);
				}
			}
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
		if (status) {
			this.state.set(ComponentState.TERMINATED);
		}
		return status;
	}

	// -------------------------------------------------------------------------
	// Task execution
	// -------------------------------------------------------------------------

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
		protected AbstractComponent	taskOwner;
		protected final String		taskPluginURI;
		protected Object			taskPlugin;

		/**
		 * create a task which uses the owner component method only.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true	// no precondition.
		 * post	true	// no postcondition.
		 * </pre>
		 *
		 */
		public			AbstractTask()
		{
			super();
			this.taskPluginURI = null;
			this.taskPlugin = null;
		}

		/**
		 * create a task which uses both the owner component method and
		 * methods of its designated plug-in.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code pluginURI != null}
		 * post	true	// no postcondition.
		 * </pre>
		 *
		 * @param pluginURI	URI of a plug-in installed on the owner.
		 */
		public			AbstractTask(String pluginURI)
		{
			super();
			assert	pluginURI != null :
						new PreconditionException("pluginURI != null");

			this.taskPluginURI = pluginURI;
			this.taskPlugin = null;
		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentTask#setOwnerReference(fr.sorbonne_u.components.ComponentI)
		 */
		@Override
		public void		setOwnerReference(ComponentI owner)
		{
			assert	owner != null : new PreconditionException("owner != null");
			assert	owner instanceof AbstractComponent;

			try {
				assert this.taskPluginURI == null ||
										owner.isInstalled(this.taskPluginURI) 
							;
			} catch (Exception e) {
				throw new RuntimeException(
							new ComponentTaskExecutionException(
									"owner does not have a plug-in with URI "
									+ this.taskPluginURI));
			}

			this.taskOwner = (AbstractComponent) owner;
			if (this.taskPluginURI != null) {
				this.taskPlugin =
						this.taskOwner.getPlugin(this.taskPluginURI);
			}

		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentTask#getTaskOwner()
		 */
		@Override
		public AbstractComponent	getTaskOwner()
		{
			return this.taskOwner;
		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentTask#getTaskProviderReference()
		 */
		@Override
		public Object	getTaskProviderReference()
		{
			if (this.taskPluginURI == null) {
				return this.taskOwner;
			} else {
				return this.taskPlugin;
			}
		}

		/**
		 * run a lambda expression as a task, providing it the owner as
		 * parameter.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code t != null}
		 * post	true	// no postcondition.
		 * </pre>
		 *
		 * @param t		lambda defining the task to be executed.
		 */
		protected void	runTaskLambda(FComponentTask t)
		{
			assert	t != null :
						new PreconditionException("trying to run a null task!");

			t.run(this.getTaskOwner());
		}
	}

	/**
	 * run the <code>ComponentTask</code> on the given executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isStarted()}
	 * pre	{@code t != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceIndex			index of the executor service that will run the task.
	 * @param t								component task to be executed as main task.
	 * @return								a future allowing to cancel and synchronise on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected Future<?>	runTaskOnComponent(
		int executorServiceIndex,
		ComponentTask t
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null :
					new PreconditionException("trying to run a null task!");

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.CALLING,
				new StringBuffer(
						"running a task on the executor service of index ").
						append(executorServiceIndex).
						append(executorServiceIndex >= 0 ?
									new StringBuffer(" (URI ").
										append(this.executorServices.get()[
						                       executorServiceIndex].getURI()).
										append(")").toString()
								: "").
						append(" of the component ").
						append(this.reflectionInboundPortURI).
						append(".").toString());
		}

		this.executorServicesLock.readLock().lock();
		try {
			t.setOwnerReference(this);
			if (this.hasItsOwnThreads() &&
						this.validExecutorServiceIndex(executorServiceIndex)) {
				return this.getExecutorService(executorServiceIndex).submit(t);
			} else {
				t.run();
				Future<?> f =
						new Future<Object>() {
							@Override
							public boolean	cancel(boolean maybeInterrupted)
							{ return false; }

							@Override
							public Object	get()
									throws	InterruptedException, ExecutionException
							{ return null; }

							@Override
							public Object get(long timeout, TimeUnit unit)
							throws 	InterruptedException,
									ExecutionException,
									TimeoutException
							{ return null; }

							@Override
							public boolean	isCancelled()
							{ return false; }

							@Override
							public boolean	isDone()
							{ return true; }
						};
				return f;
			}
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * run the <code>ComponentTask</code> on the given executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isStarted()}
	 * pre	{@code validExecutorServiceURI(executorServiceURI)}
	 * pre	{@code t != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceURI			URI of the executor service that will run the task.
	 * @param t								component task to be executed as main task.
	 * @return								a future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected Future<?>	runTaskOnComponent(
		String executorServiceURI,
		ComponentTask t
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null :
					new PreconditionException("trying to run a null task!");

		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceURI(executorServiceURI) :
						new PreconditionException(
								"validExecutorServiceURI(executorServiceURI)");

			int executorServiceIndex =
						this.getExecutorServiceIndex(executorServiceURI);
			return this.runTaskOnComponent(executorServiceIndex, t);
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * run the <code>ComponentTask</code> on the standard executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isStarted()}
	 * pre	{@code t != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param t								component task to be executed as main task.
	 * @return								a future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected Future<?>	runTaskOnComponent(ComponentTask t)
	throws	AssertionError,
			RejectedExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null :
					new PreconditionException("trying to run a null task!");

		this.executorServicesLock.readLock().lock();
		try {
			if (this.hasItsOwnThreads()) {
				if (this.validExecutorServiceURI(STANDARD_REQUEST_HANDLER_URI)) {
					return this.runTaskOnComponent(
									this.getExecutorServiceIndex(
											STANDARD_REQUEST_HANDLER_URI),
									t);
				} else {
					assert	this.validExecutorServiceURI(
											STANDARD_SCHEDULABLE_HANDLER_URI) :
								new ComponentTaskExecutionException(
										"incoherent status for component "
										+ "threads!");
					return this.runTaskOnComponent(
									this.getExecutorServiceIndex(
											STANDARD_SCHEDULABLE_HANDLER_URI),
									t);
				}
			} else {
				return this.runTaskOnComponent(-1, t);
			}			
		} finally {
			this.executorServicesLock.readLock().unlock();
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null :
					new PreconditionException("trying to run a null task!");

		this.runTaskOnComponent(t);
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#runTask(fr.sorbonne_u.components.ComponentI.FComponentTask)
	 */
	@Override
	public void 		runTask(FComponentTask t)
	throws	AssertionError,
			RejectedExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null :
					new PreconditionException("trying to run a null task!");

		this.runTask(new AbstractTask() {
						@Override
						public void run() { this.runTaskLambda(t); }
		 			 });
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#runTask(int, fr.sorbonne_u.components.ComponentI.ComponentTask)
	 */
	@Override
	public void			runTask(int executorServiceIndex, ComponentTask t)
	throws	AssertionError,
			RejectedExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null :
					new PreconditionException("trying to run a null task!");

		this.runTaskOnComponent(executorServiceIndex, t);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null :
					new PreconditionException("trying to run a null task!");

		this.runTaskOnComponent(executorServiceURI, t);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null :
					new PreconditionException("trying to run a null task!");

		this.runTask(executorServiceURI, 
					 new AbstractTask() {
						@Override
						public void run() { this.runTaskLambda(t); }
					 });
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#runTask(int, fr.sorbonne_u.components.ComponentI.FComponentTask)
	 */
	@Override
	public void			runTask(int executorServiceIndex, FComponentTask t)
	throws	AssertionError,
			RejectedExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null :
					new PreconditionException("trying to run a null task!");

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
	 * pre	{@code isStarted()}
	 * pre	{@code validExecutorServiceIndex(executorServiceIndex)}
	 * pre	{@code isSchedulable(executorServiceIndex)}
	 * pre	{@code t != null && delay > 0 && u != null}
	 * post	true		// no postcondition.
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && delay > 0 && u != null :
					new PreconditionException(
							"t != null && delay > 0 && u != null");

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.CALLING,
				new StringBuffer(
						"scheduling a task on the executor service of index ").
						append(executorServiceIndex).
						append(executorServiceIndex >= 0 ?
									new StringBuffer(" (URI ").
										append(this.executorServices.get()[
						                       executorServiceIndex].getURI()).
										append(")").toString()
								: "").
						append(" of the component ").
						append(this.reflectionInboundPortURI).
						append(".").toString());
		}

		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceIndex(executorServiceIndex) :
						new PreconditionException(
								"validExecutorServiceIndex("
								+ "executorServiceIndex)");
			assert	this.isSchedulable(executorServiceIndex) :
						new PreconditionException(
								"isSchedulable(executorServiceIndex)");

			t.setOwnerReference(this);
			return this.getSchedulableExecutorService(executorServiceIndex).
														schedule(t, delay, u);
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * schedule a <code>ComponentTask</code> to be run after a given delay
	 * on the given executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isStarted()}
	 * pre	{@code validExecutorServiceURI(executorServiceURI)}
	 * pre	{@code isSchedulable(executorServiceURI)}
	 * pre	{@code t != null && delay > 0 && u != null}
	 * post	true		// no postcondition.
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && delay > 0 && u != null :
					new PreconditionException(
							"t != null && delay > 0 && u != null");

		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceURI(executorServiceURI) :
						new PreconditionException(
								"validExecutorServiceURI(executorServiceURI)");
			assert	this.isSchedulable(executorServiceURI) :
						new PreconditionException(
								"isSchedulable(executorServiceIndex)");

			int executorServiceIndex =
							this.getExecutorServiceIndex(executorServiceURI);
			return this.scheduleTaskOnComponent(executorServiceIndex, t, delay, u);
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * schedule a <code>ComponentTask</code> to be run after a given delay
	 * on the given executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isStarted()}
	 * pre	{@code canScheduleTasks()}
	 * pre	{@code validExecutorServiceIndex(this.standardSchedulableHandlerIndex)}
	 * pre	{@code t != null && delay > 0 && u != null}
	 * post	true		// no postcondition.
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && delay > 0 && u != null :
					new PreconditionException(
							"t != null && delay > 0 && u != null");

		this.executorServicesLock.readLock().lock();
		try {
			assert	this.canScheduleTasks() :
						new PreconditionException("canScheduleTasks()");
			assert	this.validExecutorServiceURI(
											STANDARD_SCHEDULABLE_HANDLER_URI) :
						new ComponentTaskExecutionException(
								"validExecutorServiceIndex(" + 
								"standardSchedulableHandlerIndex)");

			return this.scheduleTaskOnComponent(
							this.getExecutorServiceIndex(
									STANDARD_SCHEDULABLE_HANDLER_URI),
							t, delay, u);
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && delay > 0 && u != null :
					new PreconditionException(
							"t != null && delay > 0 && u != null");

		this.scheduleTask(this.getExecutorServiceIndex(
										STANDARD_SCHEDULABLE_HANDLER_URI),
						  t, delay, u);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && delay > 0 && u != null :
					new PreconditionException(
							"t != null && delay > 0 && u != null");

		this.scheduleTask(new AbstractTask() {
							@Override
							public void run() { this.runTaskLambda(t); }
						  }, delay, u);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && delay > 0 && u != null :
					new PreconditionException(
							"t != null && delay > 0 && u != null");

		this.scheduleTaskOnComponent(executorServiceURI, t, delay, u);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && delay > 0 && u != null :
					new PreconditionException(
							"t != null && delay > 0 && u != null");

		this.scheduleTask(executorServiceURI, 
						  new AbstractTask() {
							@Override
							public void run() { this.runTaskLambda(t); }
						  }, delay, u);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && delay > 0 && u != null :
					new PreconditionException(
							"t != null && delay > 0 && u != null");

		this.scheduleTaskOnComponent(executorServiceIndex, t, delay, u);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && delay > 0 && u != null :
					new PreconditionException(
							"t != null && delay > 0 && u != null");

		this.scheduleTask(executorServiceIndex,
						  new AbstractTask() {
							@Override
							public void run() { this.runTaskLambda(t); }
						  }, delay, u);
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
	 * pre	{@code isStarted()}
	 * pre	{@code validExecutorServiceIndex(executorServiceIndex)}
	 * pre	{@code isSchedulable(executorServiceIndex)}
	 * pre	{@code t != null && initialDelay >= 0 && period > 0 && u != null}
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0  && period > 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0  && period > 0 "
									+ "&& u != null");

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.CALLING,
				new StringBuffer(
						"scheduling a task at fixed rate on the executor").
						append(" service of index ").
						append(executorServiceIndex).
						append(executorServiceIndex >= 0 ?
									new StringBuffer(" (URI ").
										append(this.executorServices.get()[
						                       executorServiceIndex].getURI()).
										append(")").toString()
								: "").
						append(" of the component ").
						append(this.reflectionInboundPortURI).
						append(".").toString());
		}

		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceIndex(executorServiceIndex) :
						new PreconditionException(
								"validExecutorServiceIndex("
								+ "executorServiceIndex)");
			assert	this.isSchedulable(executorServiceIndex) :
						new PreconditionException(
								"isSchedulable(executorServiceIndex)");

			t.setOwnerReference(this);
			return this.getSchedulableExecutorService(executorServiceIndex).
								scheduleAtFixedRate(t, initialDelay, period, u);
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
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
	 * pre	{@code validExecutorServiceURI(executorServiceURI)}
	 * pre	{@code isSchedulable(executorServiceURI)}
	 * pre	t != null and initialDelay &gt;= 0 and period &gt; 0 and u != null
	 * post	true		// no postcondition.
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	this.validExecutorServiceURI(executorServiceURI) :
					new PreconditionException(
							"validExecutorServiceURI(executorServiceURI)");
		assert	this.isSchedulable(executorServiceURI) :
					new PreconditionException(
							"isSchedulable(executorServiceURI)");
		assert	t != null && initialDelay >= 0  && period > 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0  && period > 0 "
									+ "&& u != null");

		this.executorServicesLock.readLock().lock();
		try {
			int executorServiceIndex =
							this.getExecutorServiceIndex(executorServiceURI);
			return this.scheduleTaskAtFixedRateOnComponent(
							executorServiceIndex, t, initialDelay, period, u);
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
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
	 * pre	{@code isStarted()}
	 * pre	{@code canScheduleTasks()}
	 * pre	{@code t != null && initialDelay >= 0 && period > 0 && u != null}
	 * post	true		// no postcondition.
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0  && period > 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0  && period > 0 "
									+ "&& u != null");

		this.executorServicesLock.readLock().lock();
		try {
			assert	this.canScheduleTasks() :
						new PreconditionException("canScheduleTasks()");
			assert	this.validExecutorServiceURI(STANDARD_SCHEDULABLE_HANDLER_URI) :
						new ComponentTaskExecutionException(
								"validExecutorServiceIndex(" + 
								"this.standardRequestHandlerIndex)");

			return this.scheduleTaskAtFixedRateOnComponent(
								this.getExecutorServiceIndex(
											STANDARD_SCHEDULABLE_HANDLER_URI),
								t, initialDelay, period, u);
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0  && period > 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0  && period > 0 "
									+ "&& u != null");

		this.scheduleTaskAtFixedRate(this.getExecutorServiceIndex(
											STANDARD_SCHEDULABLE_HANDLER_URI),
									 t, initialDelay, period, u);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0  && period > 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0  && period > 0 "
									+ "&& u != null");

		this.scheduleTaskAtFixedRate(
						new AbstractTask() {
							@Override
							public void run() { this.runTaskLambda(t); }
						}, initialDelay, period, u);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0  && period > 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0  && period > 0 "
									+ "&& u != null");

		this.scheduleTaskAtFixedRateOnComponent(
										executorServiceURI,
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0  && period > 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0  && period > 0 "
									+ "&& u != null");

		this.scheduleTaskAtFixedRate(
						executorServiceURI,
						new AbstractTask() {
							@Override
							public void run() { this.runTaskLambda(t); }
						}, initialDelay, period, u);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0  && period > 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0  && period > 0 "
									+ "&& u != null");

		this.scheduleTaskAtFixedRateOnComponent(executorServiceIndex,
												t, initialDelay, period, u);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0  && period > 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0  && period > 0 "
									+ "&& u != null");

		this.scheduleTaskAtFixedRate(
					executorServiceIndex,
					new AbstractTask() {
						@Override
						public void run() { this.runTaskLambda(t); }
					}, initialDelay, period, u);
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
	 * pre	{@code isStarted()}
	 * pre	{@code validExecutorServiceIndex(executorServiceIndex)}
	 * pre	{@code isSchedulable(executorServiceIndex)}
	 * pre	{@code t != null && initialDelay >= 0 && delay >= 0 && u != null}
	 * post	true		// no postcondition.
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0 && delay > 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0  && period > 0 "
									+ "&& u != null");

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.CALLING,
				new StringBuffer(
						"scheduling a task at fixed delay on the executor").
						append(" service of index ").
						append(executorServiceIndex).
						append(executorServiceIndex >= 0 ?
									new StringBuffer(" (URI ").
										append(this.executorServices.get()[
						                       executorServiceIndex].getURI()).
										append(")").toString()
								: "").
						append(" of the component ").
						append(this.reflectionInboundPortURI).
						append(".").toString());
		}

		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceIndex(executorServiceIndex) :
						new PreconditionException(
								"validExecutorServiceIndex("
								+ "executorServiceIndex)");
			assert	this.isSchedulable(executorServiceIndex) :
						new PreconditionException(
								"isSchedulable(executorServiceIndex)");

			t.setOwnerReference(this);
			return this.getSchedulableExecutorService(executorServiceIndex).
							scheduleWithFixedDelay(t, initialDelay, delay, u);
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
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
	 * pre	{@code isStarted()}
	 * pre	{@code validExecutorServiceURI(executorServiceURI)}
	 * pre	{@code isSchedulable(executorServiceURI)}
	 * pre	{@code t != null && initialDelay >= 0 && delay >= 0 && u != null}
	 * post	true		// no postcondition.
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0 && delay >= 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0 && delay >= 0 "
									+ "&& u != null");

		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceURI(executorServiceURI) :
						new PreconditionException(
								"validExecutorServiceURI(executorServiceURI)");
			assert	this.isSchedulable(executorServiceURI) :
						new PreconditionException(
								"isSchedulable(executorServiceURI)");

			int executorServiceIndex =
							this.getExecutorServiceIndex(executorServiceURI);
			return this.scheduleTaskWithFixedDelayOnComponent(
							executorServiceIndex, t, initialDelay, delay, u);
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
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
	 * pre	{@code isStarted()}
	 * pre	{@code canScheduleTasks()}
	 * pre	{@code t != null && initialDelay >= 0 && delay >= 0 && u != null}
	 * post	true		// no postcondition.
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	this.canScheduleTasks() :
					new PreconditionException("canScheduleTasks()");
		assert	t != null && initialDelay >= 0 && delay >= 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0 && delay >= 0 "
									+ "&& u != null");

		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceURI(
											STANDARD_SCHEDULABLE_HANDLER_URI) :
						new ComponentTaskExecutionException(
								"validExecutorServiceURI("
										+ "STANDARD_SCHEDULABLE_HANDLER_URI)");

			return this.scheduleTaskWithFixedDelayOnComponent(
								this.getExecutorServiceIndex(
											STANDARD_SCHEDULABLE_HANDLER_URI),
								t, initialDelay, delay, u);
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0 && delay >= 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0 && delay >= 0 "
									+ "&& u != null");

		this.scheduleTaskWithFixedDelay(this.getExecutorServiceIndex(
											STANDARD_SCHEDULABLE_HANDLER_URI),
										t, initialDelay, delay, u);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0 && delay >= 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0 && delay >= 0 "
									+ "&& u != null");

		this.scheduleTaskWithFixedDelay(
					new AbstractTask() {
						@Override
						public void run() { this.runTaskLambda(t); }
					}, initialDelay, delay, u);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0 && delay >= 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0 && delay >= 0 "
									+ "&& u != null");

		this.scheduleTaskWithFixedDelayOnComponent(
										executorServiceURI,
										t, initialDelay, delay, u);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0 && delay >= 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0 && delay >= 0 "
									+ "&& u != null");

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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0 && delay > 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0  && period > 0 "
									+ "&& u != null");

		this.scheduleTaskWithFixedDelayOnComponent(executorServiceIndex,
												   t, initialDelay, delay, u);
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	t != null && initialDelay >= 0 && delay > 0 && u != null :
					new PreconditionException(
							"t != null && initialDelay >= 0  && period > 0 "
									+ "&& u != null");

		this.scheduleTaskWithFixedDelay(
						executorServiceIndex,
						new AbstractTask() {
							@Override
							public void run() { this.runTaskLambda(t); }
						}, initialDelay, delay, u);
	}

	// -------------------------------------------------------------------------
	// Request handling
	// -------------------------------------------------------------------------

	/**
	 * The abstract class <code>AbstractService</code> provides the basic
	 * method implementations for component service calls.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	true
	 * </pre>
	 * 
	 * <p>Created on : 2018-09-18</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static abstract class	AbstractService<V>
	implements	ComponentI.ComponentService<V>
	{
		protected AbstractComponent	serviceOwner;
		protected final String		servicePluginURI;
		protected PluginI			servicePlugin;

		/**
		 * create a service callable which calls a service directly
		 * implemented by the object representing the component.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true	// no precondition.
		 * post	true	// no postcondition.
		 * </pre>
		 *
		 */
		public			AbstractService()
		{
			this.servicePluginURI = null;
		}

		/**
		 * create a service callable which calls a service 
		 * implemented by the designated plugin of the component.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code pluginURI != null}
		 * post	true	// no postcondition.
		 * </pre>
		 *
		 * @param pluginURI	URI of a plug-in installed on the component.
		 */
		public			AbstractService(String pluginURI)
		{
			assert	pluginURI != null :
						new PreconditionException("pluginURI != null");

			this.servicePluginURI = pluginURI;
		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentService#setOwnerReference(fr.sorbonne_u.components.ComponentI)
		 */
		@Override
		public void		setOwnerReference(ComponentI owner)
		{
			assert	owner != null : new PreconditionException("owner != null");
			assert	owner instanceof AbstractComponent :
						new PreconditionException(
								"owner instanceof AbstractComponent");

			try {
				assert	this.servicePluginURI == null ||
										owner.isInstalled(servicePluginURI);
			} catch (Exception e) {
				throw new RuntimeException(
							new ComponentTaskExecutionException(
									"owner does not have a plug-in with URI "
									+ this.servicePluginURI));
			}

			this.serviceOwner = (AbstractComponent)owner;
			if (this.servicePluginURI != null) {
				this.servicePlugin =
						this.serviceOwner.getPlugin(this.servicePluginURI);
			}
		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentService#getServiceOwner()
		 */
		@Override
		public AbstractComponent	getServiceOwner()
		{
			return this.serviceOwner;
		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentService#getServiceProviderReference()
		 */
		@Override
		public Object	getServiceProviderReference()
		{
			if (this.servicePluginURI == null) {
				return this.serviceOwner;
			} else {
				return this.servicePlugin;
			}
		}

		/**
		 * call a service lambda on the owner component passing the correct
		 * parameters.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code sl != null}
		 * post	true	// no postcondition.
		 * </pre>
		 *
		 * @param sl			lambda expression representing a service execution.
		 * @return				the result of the lambda expression.
		 * @throws Exception	<i>to do.</i>
		 */
		protected V		callServiceLambda(FComponentService<V> sl)
		throws Exception
		{
			assert	sl != null :
						new PreconditionException(
								"trying to run a null request!");

			return sl.apply(this.getServiceOwner());
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
	 * pre	{@code isStarted()}
	 * pre	{@code request != null}
	 * pre	{@code validExecutorServiceIndex(executorServiceIndex)}
	 * pre	{@code isSchedulable(executorServiceIndex)}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param <T>							the type of the value returned by the request.
	 * @param executorServiceIndex			index of the executor service that will run the task.
	 * @param request						service request to be executed on the component.
	 * @return								a future value embedding the result of the task.
	 * @throws AssertionError				if the component is not started, the index is not valid or the request is null.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected <T> Future<T>		baselineHandleRequest(
		int executorServiceIndex,
		ComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null : new PreconditionException("request != null");

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.CALLING,
				new StringBuffer(
						"handling a request on the executor service of index ").
						append(executorServiceIndex).
						append(executorServiceIndex >= 0 ?
									new StringBuffer(" (URI ").
										append(this.executorServices.get()[
						                       executorServiceIndex].getURI()).
										append(")").toString()
								: "").
						append(" of the component ").
						append(this.reflectionInboundPortURI).
						append(".").toString());
		}

		this.executorServicesLock.readLock().lock();
		try {
			request.setOwnerReference(this);
			if (this.hasItsOwnThreads() &&
						this.validExecutorServiceIndex(executorServiceIndex)) {
				return this.getExecutorService(executorServiceIndex).
																submit(request);
			} else {
				final ComponentService<T> t = request;
				return new Future<T>() {
							@Override
							public boolean	cancel(boolean mayInterruptIfRunning)
							{ return false; }

							@Override
							public T		get()
							throws	InterruptedException,
									ExecutionException
							{
								try {
									return t.call();
								} catch (Exception e) {
									throw new ExecutionException(e);
								}
							}

							@Override
							public T		get(long timeout, TimeUnit unit)
							throws	InterruptedException,
									ExecutionException,
									TimeoutException
							{ 	try {
									return t.call();
								} catch (Exception e) {
									throw new ExecutionException(e);
								}
							}

							@Override
							public boolean	isCancelled()
							{ return false; }

							@Override
							public boolean	isDone()
							{ return true; }
						};
			}
		} finally {
			this.executorServicesLock.readLock().unlock();
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
	 * pre	{@code isStarted()}
	 * pre	{@code validExecutorServiceURI(executorServiceURI)}
	 * pre	{@code request != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param <T>					the type of the value returned by the request.
	 * @param executorServiceURI	URI of the executor service that will run the task.
	 * @param request				service request to be executed on the component.
	 * @return						a future value embedding the result of the task.
	 * @throws AssertionError				if the component is not started, the URI is not valid or the request is null.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected <T> Future<T>		baselineHandleRequest(
		String executorServiceURI,
		ComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null : new PreconditionException("request != null");

		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceURI(executorServiceURI) :
						new PreconditionException(
								"validExecutorServiceURI(executorServiceURI)");

			int executorServiceIndex =
					this.getExecutorServiceIndex(executorServiceURI);
			return this.baselineHandleRequest(executorServiceIndex, request);
		} finally {
			this.executorServicesLock.readLock().unlock();
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
	 * pre	{@code isStarted()}
	 * pre	{@code validExecutorServiceIndex(standardRequestHandlerIndex) ||
	 *             validExecutorServiceIndex(standardSchedulableHandlerIndex)}
	 * pre	{@code request != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param <T>							the type of the value returned by the request.
	 * @param request						service request to be executed on the component.
	 * @return								a future value embedding the result of the task.
	 * @throws AssertionError				if the component is not started or the request is null.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected <T> Future<T>		baselineHandleRequest(
		ComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null : new PreconditionException("request != null");

		this.executorServicesLock.readLock().lock();
		try {
			if (this.validExecutorServiceURI(STANDARD_REQUEST_HANDLER_URI)) {
				return this.baselineHandleRequest(this.getExecutorServiceIndex(
												STANDARD_REQUEST_HANDLER_URI),
										  request);
			} else if (this.validExecutorServiceURI(
											STANDARD_SCHEDULABLE_HANDLER_URI)) {
				return this.baselineHandleRequest(
								this.getExecutorServiceIndex(
											STANDARD_SCHEDULABLE_HANDLER_URI),
								request);
			} else {
				return this.baselineHandleRequest(-1, request);
			}
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#handleRequest(int, fr.sorbonne_u.components.ComponentI.ComponentService)
	 */
	@Override
	public <T> T		handleRequest(
		int executorServiceIndex,
		ComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null : new PreconditionException("request != null");

		return this.baselineHandleRequest(executorServiceIndex, request).get();
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#handleRequest(fr.sorbonne_u.components.ComponentI.ComponentService)
	 */
	@Override
	public <T> T		handleRequest(ComponentService<T> request)
	throws	AssertionError,
			RejectedExecutionException,
			InterruptedException,
			ExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null : new PreconditionException("request != null");

		this.executorServicesLock.readLock().lock();
		try {
			if (this.hasItsOwnThreads()) {
				if (this.validExecutorServiceURI(STANDARD_REQUEST_HANDLER_URI)) {
					return this.baselineHandleRequest(
								this.getExecutorServiceIndex(
												STANDARD_REQUEST_HANDLER_URI),
								request).get();
				} else {
					assert this.validExecutorServiceURI(STANDARD_SCHEDULABLE_HANDLER_URI);

					return this.baselineHandleRequest(
								this.getExecutorServiceIndex(
											STANDARD_SCHEDULABLE_HANDLER_URI),
								request).get();
				}
			} else {
				return this.baselineHandleRequest(-1, request).get();
			}
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#handleRequest(fr.sorbonne_u.components.ComponentI.FComponentService)
	 */
	@Override
	public <T> T		handleRequest(
		FComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null : new PreconditionException("request != null");

		return this.handleRequest(
							new AbstractService<T>() {
								@Override
								public T call() throws Exception {
									return this.callServiceLambda(request);
								}								
							});
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#handleRequest(java.lang.String, fr.sorbonne_u.components.ComponentI.ComponentService)
	 */
	@Override
	public <T> T		handleRequest(
		String executorServiceURI,
		ComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null : new PreconditionException("request != null");

		return this.baselineHandleRequest(executorServiceURI, request).get();
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#handleRequest(java.lang.String, fr.sorbonne_u.components.ComponentI.FComponentService)
	 */
	@Override
	public <T> T		handleRequest(
		String executorServiceURI,
		FComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null : new PreconditionException("request != null");

		return this.handleRequest(
							executorServiceURI,
							new AbstractService<T>() {
								@Override
								public T call() throws Exception {
									return this.callServiceLambda(request);
								}
							});
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#handleRequest(int, fr.sorbonne_u.components.ComponentI.FComponentService)
	 */
	@Override
	public <T> T		handleRequest(
		int executorServiceIndex,
		FComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null : new PreconditionException("request != null");

		return this.handleRequest(
							executorServiceIndex,
							new AbstractService<T>() {
								@Override
								public T call() throws Exception {
									return this.callServiceLambda(request);
								}
							});
	}

	/**
	 * schedule a service for execution after a given delay.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isStarted()}
	 * pre	{@code canScheduleTasks()}
	 * pre	{@code s != null && delay > 0 && u != null}
	 * post	true		// no postcondition.
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
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null : new PreconditionException("request != null");
		assert	delay >= 0 && u != null;

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.CALLING,
				new StringBuffer(
						"scheduling a request on the executor service of index ").
						append(executorServiceIndex).
						append(executorServiceIndex >= 0 ?
									new StringBuffer(" (URI ").
										append(this.executorServices.get()[
						                       executorServiceIndex].getURI()).
										append(")").toString()
								: "").
						append(" of the component ").
						append(this.reflectionInboundPortURI).
						append(".").toString());
		}

		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceIndex(executorServiceIndex) :
						new PreconditionException(
								"validExecutorServiceIndex("
										+ "executorServiceIndex)");
			assert	this.isSchedulable(executorServiceIndex) :
						new PreconditionException(
								"isSchedulable(executorServiceIndex)");

			request.setOwnerReference(this);
			return this.getSchedulableExecutorService(executorServiceIndex).
													schedule(request, delay, u);
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}
	/**
	 * schedule a service for execution after a given delay.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isStarted()}
	 * pre	{@code canScheduleTasks()}
	 * pre	{@code s != null && delay > 0 && u != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param <T>							the type of the value returned by the request.
	 * @param executorServiceURI			URI of the executor service that will run the task.
	 * @param request						service request to be scheduled.
	 * @param delay							delay after which the task must be run.
	 * @param u								time unit in which the delay is expressed.
	 * @return								a scheduled future to synchronise with the task.
	 * @throws AssertionError				if the component is not started, this index is not valid, the executor is not schedulable or the request in null.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected <T> ScheduledFuture<T>	scheduleRequest(
		String executorServiceURI,
		ComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null : new PreconditionException("request != null");
		assert	delay >= 0 && u != null;

		this.executorServicesLock.readLock().lock();
		try {
			assert	this.validExecutorServiceURI(executorServiceURI) :
						new PreconditionException(
								"validExecutorServiceURI(executorServiceURI)");
			assert	this.isSchedulable(executorServiceURI) :
						new PreconditionException(
								"isSchedulable(executorServiceURI)");

			int index = this.getExecutorServiceIndex(executorServiceURI);
			return this.scheduleRequest(index, request, delay, u);
		} finally {
			this.executorServicesLock.readLock().unlock();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleRequestSync(int, fr.sorbonne_u.components.ComponentI.ComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T 		scheduleRequestSync(
		int executorServiceIndex,
		ComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null && delay >= 0 && u != null;

		return this.scheduleRequest(
						executorServiceIndex, request, delay, u).get();
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleRequestSync(fr.sorbonne_u.components.ComponentI.ComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T		scheduleRequestSync(
		ComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null && delay >= 0 && u != null;

		return this.scheduleRequestSync(this.getExecutorServiceIndex(
											STANDARD_SCHEDULABLE_HANDLER_URI),
										request, delay, u);
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleRequestSync(fr.sorbonne_u.components.ComponentI.FComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T		scheduleRequestSync(
		FComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null && delay >= 0 && u != null;

		return this.scheduleRequestSync(
								new AbstractService<T>() {
									@Override
									public T call() throws Exception {
										return this.callServiceLambda(request);
									}
								}, delay, u);
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleRequestSync(java.lang.String, fr.sorbonne_u.components.ComponentI.ComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T		scheduleRequestSync(
		String executorServiceURI,
		ComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null && delay >= 0 && u != null;

		return this.scheduleRequest(
							executorServiceURI, request, delay, u).get();
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleRequestSync(java.lang.String, fr.sorbonne_u.components.ComponentI.FComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T		scheduleRequestSync(
		String executorServiceURI,
		FComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null && delay >= 0 && u != null;

		return this.scheduleRequestSync(
								executorServiceURI,
								new AbstractService<T>() {
									@Override
									public T call() throws Exception {
										return this.callServiceLambda(request);
									}
								}, delay, u);
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleRequestSync(int, fr.sorbonne_u.components.ComponentI.FComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T		scheduleRequestSync(
		int executorServiceIndex,
		FComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException,
					InterruptedException,
					ExecutionException
	{
		assert	this.isStarted() : new PreconditionException("isStarted()");
		assert	request != null && delay >= 0 && u != null;

		return this.scheduleRequestSync(
								executorServiceIndex,
								new AbstractService<T>() {
									@Override
									public T call() throws Exception {
										return this.callServiceLambda(request);
									}
								}, delay, u);
	}

	// -------------------------------------------------------------------------
	// Reflection facility
	// FIXME: experimental...
	// To use the reflection facility:
	//    - the jar tools.jar from the Java distribution must be in the
	//      classpath of the compiler and of the JVM
	//    - the JVM must be passed the argument "-javaagent:hotswap.jar"
	//      with the jar "hotswap.jar" accessible from the base directory
	//      (or the appropriate path given in the argument
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getComponentDefinitionClassName()
	 */
	@Override
	public String		getComponentDefinitionClassName()
	throws Exception
	{
		return this.getClass().getCanonicalName();
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getComponentAnnotations()
	 */
	@Override
	public Annotation[]	getComponentAnnotations() throws Exception
	{
		return this.getClass().getAnnotations();
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getComponentLoader()
	 */
	@Override
	public ClassLoader	getComponentLoader() throws Exception
	{
		return this.getClass().getClassLoader();
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getComponentServiceSignatures()
	 */
	@Override
	public ServiceSignature[]	getComponentServiceSignatures()
	throws Exception
	{
		Vector<ServiceSignature> ret = new Vector<ServiceSignature>();
		Class<?> clazz = this.getClass();
		while (clazz != AbstractComponent.class) {
			Method[] ms = clazz.getDeclaredMethods();
			for (int i = 0; i < ms.length; i++) {
				if (Modifier.isPublic(ms[i].getModifiers())) {
					ret.add(new ServiceSignature(
									ms[i].getReturnType(),
									ms[i].getParameterTypes()));
				}
			}
			clazz = clazz.getSuperclass();
		}
		return ret.toArray(new ServiceSignature[0]);
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getComponentConstructorSignatures()
	 */
	@Override
	public ConstructorSignature[]	getComponentConstructorSignatures()
	throws Exception
	{
		Constructor<?>[] cons = this.getClass().getConstructors();
		ConstructorSignature[] ret = new ConstructorSignature[cons.length];
		for (int i = 0; i < cons.length; i++) {
			ret[i] = new ConstructorSignature(cons[i].getParameterTypes());
		}
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#invokeService(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object		invokeService(String name, Object[] params)
	throws Exception
	{
		assert	this.isStarted();
		assert	name != null && params != null;

		Class<?>[] pTypes = new Class<?>[params.length];
		for (int i = 0; i < params.length; i++) {
			pTypes[i] = params[i].getClass();
		}
		Method m = this.getClass().getMethod(name, pTypes);
		int index;
		if (this.validExecutorServiceURI(STANDARD_REQUEST_HANDLER_URI)) {
			index = this.getExecutorServiceIndex(STANDARD_REQUEST_HANDLER_URI);
		} else {
			index = this.getExecutorServiceIndex(
										STANDARD_SCHEDULABLE_HANDLER_URI);
		}
		return this.baselineHandleRequest(
						index,
						new AbstractService<Object>() {
							@Override
							public Object call() throws Exception {
								return m.invoke(this.getServiceOwner(), params);
							}
						});
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#invokeServiceSync(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object		invokeServiceSync(String name, Object[] params)
	throws Exception
	{
		Class<?>[] pTypes = new Class<?>[params.length];
		for (int i = 0; i < params.length; i++) {
			pTypes[i] = params[i].getClass();
		}
		Method m = this.getClass().getMethod(name, pTypes);
		return this.handleRequest(
						new AbstractService<Object>() {
							@Override
							public Object call() throws Exception {
								return m.invoke(this.getServiceOwner(), params);
							}
						});
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#invokeServiceAsync(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void			invokeServiceAsync(String name, Object[] params)
	throws Exception
	{
		Class<?>[] pTypes = new Class<?>[params.length];
		for (int i = 0; i < params.length; i++) {
			pTypes[i] = params[i].getClass();
		}
		Method m = this.getClass().getMethod(name, pTypes);
		this.runTask(new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								m.invoke(this.getTaskOwner(), params);
							} catch (IllegalAccessException |
									 IllegalArgumentException |
									 InvocationTargetException e) {
								e.printStackTrace();
							}
						}
					 });
	}

	/** Javassist classpool containing the component classes.				*/
	protected static ClassPool	javassistClassPool;
	/** The Javassist CtClass representation of the compoennt's class.	*/
	protected CtClass			javassistClassForComponent;

	/**
	 * ensure that the Javassist representation of the component's class
	 * is loaded and can be accessed by the reflective code.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @throws NotFoundException	<i>to do</i>.
	 */
	protected void		ensureLoaded() throws NotFoundException
	{
		if (AbstractComponent.javassistClassPool == null) {
			AbstractComponent.javassistClassPool = javassist.ClassPool.getDefault();
			String rtpath = System.getProperty("java.home") +
						File.separator + "lib" + File.separator + "rt.jar";
			AbstractComponent.javassistClassPool.appendClassPath(rtpath);
		}
		if (this.javassistClassForComponent == null) {
			this.javassistClassForComponent =
					AbstractComponent.javassistClassPool.
								get(this.getClass().getCanonicalName());
		}
	}

	/**
	 * get a declared method from the Javassist representation of the
	 * component's class.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code methodName != null}
	 * pre	{@code parametersCanonicalClassNames != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param methodName					name of the method to be retrieved.
	 * @param parametersCanonicalClassNames	names of the classes typing the parameters of the method.
	 * @return								the corresponding method.
	 * @throws NotFoundException			if no method is found.
	 */
	protected CtMethod	getDeclaredMethod(
		String methodName,
		String[] parametersCanonicalClassNames
		) throws NotFoundException
	{
		assert	methodName != null :
					new PreconditionException("Method name is null!");
		assert	parametersCanonicalClassNames != null :
					new PreconditionException("Parameter type names array"
													+ " can't be null!");

		CtClass[] paramCtClass =
						new CtClass[parametersCanonicalClassNames.length];
		for (int i = 0; i < parametersCanonicalClassNames.length; i++) {
			paramCtClass[i] =
				AbstractComponent.javassistClassPool.
									get(parametersCanonicalClassNames[i]);
		}
		CtMethod m = this.javassistClassForComponent.
							getDeclaredMethod(methodName, paramCtClass);
		return m;
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
					new PreconditionException("Service name is null!");
		assert	parametersCanonicalClassNames != null :
					new PreconditionException("Parameter type names array"
														+ " is null!");
		assert	code != null :
					new PreconditionException("Code to be added is null!");

		this.ensureLoaded();
		CtMethod m = this.getDeclaredMethod(methodName,
										   parametersCanonicalClassNames);
		m.insertBefore(code);
		HotSwapAgent.redefine(this.getClass(),
							  this.javassistClassForComponent);
		this.javassistClassForComponent.defrost();
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
					new PreconditionException("Service name is null!");
		assert	parametersCanonicalClassNames != null :
					new PreconditionException("Parameter type names array"
														+ " is null!");
		assert	code != null :
					new PreconditionException("Code to be added is null!");

		this.ensureLoaded();
		CtMethod m = this.getDeclaredMethod(methodName,
										   parametersCanonicalClassNames);
		m.insertAfter(code);
		HotSwapAgent.redefine(this.getClass(),
							  this.javassistClassForComponent);
		this.javassistClassForComponent.defrost();
	}
}
// -----------------------------------------------------------------------------