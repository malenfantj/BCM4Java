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
import fr.sorbonne_u.components.helpers.Logger;
import fr.sorbonne_u.components.helpers.TracerOnConsole;
import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;
import fr.sorbonne_u.components.ports.AbstractPort;
import fr.sorbonne_u.components.ports.InboundPortI;
import fr.sorbonne_u.components.ports.OutboundPortI;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionI;
import fr.sorbonne_u.components.reflection.ports.ReflectionInboundPort;
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
 * object. To relay the call, inbound ports create a component task implementing
 * <code>ComponentService</code> and then call the method
 * <code>handleRequest</code> passing it this task as parameter.
 * </p>
 * <p>
 * Components can be passive or active.  Passive components do not have their
 * own thread, so any call they serve will use the thread of the caller. 
 * <code>handleRequest</code> simply calls the component service in the
 * thread of the caller component. Active components use their own threads
 * to perform the tasks.which are managed through the Java Executor framework
 * that implements the concurrent servicing of requests.  At creation time,
 * components may be given 0, 1 or more threads as well as 0, 1 or more
 * schedulable threads.  Schedulable threads are useful when some service
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
 * <p><strong>Usage</strong></p>
 * 
 * This class is meant to be extended by any class implementing a kind of
 * components in the application.  Constructors and methods should be used only
 * in the code of the component so to hide technicalities of the implementation
 * from the component users.  The proper vision of the component model is to
 * consider the code in this package, and therefore in this class, as a virtual
 * machine to implement components rather that application code.
 * 
 * Components are indeed implemented as objects but calling from the outside of
 * this objects methods they define directly is something that should be done
 * only in virtual machine code and not in component code, essentially in
 * classes derived from AbstractCVM. The call should also use only methods
 * defined within this abstract class and not methods defined as services in
 * user components that must be called through the Executor framework.
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

	/** current state in the component life-cycle.							*/
	protected ComponentState				state ;
	/** inner components owned by this component.							*/
	protected final Vector<ComponentI>	innerComponents ;

	// ------------------------------------------------------------------------
	// Internal concurrency management
	// ------------------------------------------------------------------------

	/** true if the component executes concurrently.						*/
	protected boolean					isConcurrent ;
	/** true if the component can schedule tasks.						*/
	protected boolean					canScheduleTasks ;

	/** the executor service in charge of handling component requests.	*/
	protected ExecutorService			requestHandler ;
	/** number of threads in the <code>ExecutorService</code>.			*/
	protected int						nbThreads ;
	/** the executor service in charge of handling scheduled tasks.		*/
	protected ScheduledExecutorService	scheduledTasksHandler ;
	/** number of threads in the <code>ScheduledExecutorService</code>.	*/
	protected int						nbSchedulableThreads ;

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
	 * @throws Exception		<i>todo.</i>
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
	 * @throws Exception		<i>todo.</i>
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
	 * @see fr.sorbonne_u.components.ComponentI#installPlugin(fr.sorbonne_u.components.PluginI)
	 */
	@Override
	public void			installPlugin(
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
						+ plugin.getPluginURI()  + " not installed correctly!") ;
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
	 * @see fr.sorbonne_u.components.ComponentI#finalisePlugin(java.lang.String)
	 */
	@Override
	public void			finalisePlugin(String pluginURI) throws Exception
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
	 * @see fr.sorbonne_u.components.ComponentI#uninstallPlugin(java.lang.String)
	 */
	@Override
	public void			uninstallPlugin(String pluginURI) throws Exception
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
	 * @see fr.sorbonne_u.components.ComponentI#getPlugin(java.lang.String)
	 */
	@Override
	public PluginI		getPlugin(String pluginURI)
	{
		assert	this.isPluginFacilitiesConfigured() :
					new RuntimeException("Can't access plug-in, "
							+ "plug-in facilities are not configured!") ;
		assert	pluginURI != null :
					new PreconditionException("Plug-in URI is null!") ;

		return this.installedPlugins.get(pluginURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#initialisePlugin(java.lang.String)
	 */
	@Override
	public void			initialisePlugin(String pluginURI)
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
	protected Logger				executionLog ;
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
	public				AbstractComponent(
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
	public				AbstractComponent(
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

		this.innerComponents = new Vector<ComponentI>() ;
		this.isConcurrent = false ;
		this.canScheduleTasks = false ;
		this.requestHandler = null ;
		this.nbThreads = 0 ;
		this.scheduledTasksHandler = null ;
		this.nbSchedulableThreads = 0 ;
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
			this.isConcurrent = true ;
		}
		if (nbThreads == 1) {
			this.requestHandler = Executors.newSingleThreadExecutor() ;
		} else if (nbThreads > 1) {
			this.requestHandler = Executors.newFixedThreadPool(nbThreads) ;
		}
		
		this.nbSchedulableThreads = nbSchedulableThreads ;
		if (nbSchedulableThreads > 0) {
			this.canScheduleTasks = true ;
		}
		if (nbSchedulableThreads == 1) {
			this.scheduledTasksHandler =
								Executors.newSingleThreadScheduledExecutor() ;
		} else if (nbSchedulableThreads > 1) {
			this.scheduledTasksHandler =
						Executors.newScheduledThreadPool(nbSchedulableThreads) ;
		}

		this.addOfferedInterface(ReflectionI.class) ;
		try {
			ReflectionInboundPort rip =
					new ReflectionInboundPort(reflectionInboundPortURI, this) ;
			this.addPort(rip) ;
			rip.publishPort() ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}

		this.addInterfacesFromAnnotations() ;
		this.addPluginsFromAnnotations() ;

		assert	this.innerComponents != null ;
		assert	this.requiredInterfaces != null ;
		assert	this.offeredInterfaces != null ;
		assert	this.interfaces2ports != null ;
		assert	this.portURIs2ports != null ;
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
	 * @param ac		component object on which the invariant is checked.
	 * @return		true if the invariant holds, false otherwise.
	 */
	protected static boolean		checkInvariant(AbstractComponent ac)
	{
		assert	ac != null ;

		boolean ret = true ;

		ret &= ac.innerComponents != null ;
		ret &= ac.isConcurrent == (ac.requestHandler != null) ;
		ret &= ac.isConcurrent == (ac.nbThreads > 0) ;
		ret &= ac.canScheduleTasks == (ac.scheduledTasksHandler != null) ;
		ret &= ac.canScheduleTasks == (ac.nbSchedulableThreads > 0) ;
		ret &= ac.installedPlugins != null ;
		ret &= (ac.isLogging() == (ac.executionLog != null)) ;
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

		return this.isConcurrent || this.canScheduleTasks() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getTotalNUmberOfThreads()
	 */
	@Override
	public int			getTotalNUmberOfThreads()
	{
		return this.nbThreads + this.nbSchedulableThreads ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#hasSerialisedExecution()
	 */
	@Override
	public boolean		hasSerialisedExecution()
	{
		return this.hasItsOwnThreads() &&
				this.nbThreads + this.nbSchedulableThreads == 1 ;
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

		Vector<Class<?>> temp = new Vector<Class<?>>() ;
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
	 * @see fr.sorbonne_u.components.ComponentI#addRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			addRequiredInterface(Class<?> inter)
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
	 * @see fr.sorbonne_u.components.ComponentI#removeRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			removeRequiredInterface(Class<?> inter)
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
	 * @see fr.sorbonne_u.components.ComponentI#addOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			addOfferedInterface(Class<?> inter)
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
	 * @see fr.sorbonne_u.components.ComponentI#removeOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			removeOfferedInterface(Class<?> inter)
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
	 * @param p		port to be added.
	 * @throws Exception		<i>todo.</i>
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
	 * @param p		port to be removed.
	 * @throws Exception		<i>todo.</i>
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
			c.runTask(
				new AbstractTask() {
					@Override
					public void run() {
						try {
							this.getOwner().execute() ;
						} catch (Exception e) {
							throw new RuntimeException(e) ;
						}
				}
			}) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		assert	this.isStarted() ;

		for(ComponentI c : this.innerComponents) {
			c.runTask(
				new AbstractTask() {
					@Override
					public void run() {
						try {
							this.getOwner().finalise() ;
						} catch (Exception e) {
							throw new RuntimeException(e) ;
						}
				}
			}) ;
		}

		for (String pluginURI : this.installedPlugins.keySet()) {
			this.finalisePlugin(pluginURI) ;
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
		if (this.isConcurrent) {
			this.requestHandler.shutdown() ;
		}
		if (this.canScheduleTasks) {
			this.scheduledTasksHandler.shutdown() ;
		}
		this.state = ComponentState.SHUTTINGDOWN ;
		if (!this.isConcurrent && !this.canScheduleTasks) {
			this.state = ComponentState.SHUTDOWN ;
		}
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

		if (this.isConcurrent) {
			this.requestHandler.shutdownNow() ;
		}
		if (this.canScheduleTasks) {
			this.scheduledTasksHandler.shutdownNow() ;
		}
		this.state = ComponentState.SHUTDOWN ;
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
		boolean isShutdown = false ;

		if (this.state == ComponentState.SHUTDOWN) {
			return true ;
		}

		if (this.isConcurrent) {
			isShutdown = this.requestHandler.isShutdown() ;
			if (this.canScheduleTasks) {
				isShutdown = isShutdown &&
									this.scheduledTasksHandler.isShutdown() ;
			}
		} else {
			if (this.canScheduleTasks) {
				isShutdown = this.scheduledTasksHandler.isShutdown() ;
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
		boolean isTerminated = false ;

		if (this.state == ComponentState.TERMINATED) {
			return true ;
		}

		if (this.isConcurrent) {
			isTerminated = this.requestHandler.isTerminated() ;
		} else {
			isTerminated = this.isShutdown() ;
		}
		if (this.canScheduleTasks) {
			isTerminated = isTerminated &&
									this.scheduledTasksHandler.isTerminated() ;
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

		boolean status = false ;
		if (this.canScheduleTasks) {
			status =
					this.scheduledTasksHandler.awaitTermination(timeout, unit) ;
		}
		if (this.isConcurrent) {
			status = status &&
						this.requestHandler.awaitTermination(timeout, unit) ;
		} else {
			status = true ;
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
	public static abstract class		AbstractTask
	implements	ComponentI.ComponentTask
	{
		protected ComponentI		owner ;

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentTask#setOwnerReference(fr.sorbonne_u.components.ComponentI)
		 */
		@Override
		public void			setOwnerReference(ComponentI owner)
		{
			assert	owner != null ;

			this.owner = owner ;
		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentTask#getOwner()
		 */
		@Override
		public ComponentI	getOwner()
		{
			return this.owner ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#runTask(fr.sorbonne_u.components.ComponentI.ComponentTask)
	 */
	@Override
	public Future<?>		runTask(ComponentTask t)
	{
		assert	this.isStarted() ;
		assert	t != null ;

		t.setOwnerReference(this) ;
		Future<?> f = null ;
		if (this.hasItsOwnThreads()) {
			if (this.isConcurrent) {
				f = this.requestHandler.submit(t) ;
			} else {
				assert	this.canScheduleTasks ;
				f = this.scheduledTasksHandler.submit(t) ;
			}
		} else {
			t.run() ;
			f = new Future<Object>() {
						@Override
						public boolean	cancel(boolean arg0)
						{ return false ; }

						@Override
						public Object	get()
						throws	InterruptedException, ExecutionException
						{ return null ; }

						@Override
						public Object get(long arg0, TimeUnit arg1)
						throws 	InterruptedException, ExecutionException,
							   	TimeoutException
						{ return null ; }

						@Override
						public boolean	isCancelled()
						{ return false ; }

						@Override
						public boolean	isDone()
						{ return true ; }
					} ;
		}
		return f ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTask(fr.sorbonne_u.components.ComponentI.ComponentTask, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public ScheduledFuture<?>	scheduleTask(
		ComponentTask t,
		long delay,
		TimeUnit u
		)
	{
		assert	this.isStarted() ;
		assert	this.canScheduleTasks() ;
		assert	t != null && delay >= 0 && u != null ;

		t.setOwnerReference(this) ;
		return this.scheduledTasksHandler.schedule(t, delay, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTaskAtFixedRate(fr.sorbonne_u.components.ComponentI.ComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public ScheduledFuture<?>	scheduleTaskAtFixedRate(
		ComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u
		)
	{
		assert	this.isStarted() ;
		assert	this.canScheduleTasks() ;
		assert	t != null && initialDelay >= 0  && period > 0 && u != null ;

		t.setOwnerReference(this) ;
		return this.scheduledTasksHandler.
							scheduleAtFixedRate(t, initialDelay, period, u) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleTaskWithFixedDelay(fr.sorbonne_u.components.ComponentI.ComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public ScheduledFuture<?>	scheduleTaskWithFixedDelay(
		ComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u
		)
	{
		assert	this.isStarted() ;
		assert	this.canScheduleTasks() ;
		assert	t != null && initialDelay >= 0 && delay >= 0 && u != null ;

		t.setOwnerReference(this) ;
		return this.scheduledTasksHandler.
							scheduleWithFixedDelay(t, initialDelay, delay, u) ;
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
	public static abstract class		AbstractService<V>
	implements	ComponentI.ComponentService<V>
	{
		protected ComponentI		owner ;
		protected final String	pluginURI ;

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
			this.pluginURI = null ;
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
		 * @param pluginURI
		 */
		public				AbstractService(String pluginURI)
		{
			assert	pluginURI != null ;

			this.pluginURI = pluginURI ;
		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentTask#setOwnerReference(fr.sorbonne_u.components.ComponentI)
		 */
		@Override
		public void			setOwnerReference(ComponentI owner)
		{
			assert	owner != null ;

			try {
				assert	this.pluginURI == null ||
									owner.isInstalled(pluginURI) ;
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}

			this.owner = owner ;
		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentService#getOwner()
		 */
		@Override
		public ComponentI	getOwner()
		{
			assert	this.pluginURI == null ;

			return this.owner ;
		}

		/**
		 * @see fr.sorbonne_u.components.ComponentI.ComponentService#getServiceProviderReference()
		 */
		@Override
		public Object		getServiceProviderReference()
		{
			if (this.pluginURI == null) {
				return this.owner ;
			} else {
				try {
					return this.owner.getPlugin(this.pluginURI) ;
				} catch (Exception e) {
					throw new RuntimeException(e) ;
				}
			}
		}
	}

	/**
	 * execute a request represented by a <code>Callable</code> on the
	 * component.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * Uniform API entry to execute a call on the component.  The call, that
	 * represents a method call on the object representing the component, is
	 * embedded in a <code>Callable</code> object.  In concurrent components,
	 * the Java executor framework is used to handle such requests.  Sequential
	 * components may simply use this method to handle requests, or they may
	 * bypass it by directly calling the method on the object representing the
	 * component for the sought of efficiency.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	task != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param request	service request to be executed on the component.
	 * @return			a future value embedding the result of the task.
	 * @throws Exception					if exception raised by the task.
	 */
	public <T> Future<T>		handleRequest(ComponentService<T> request)
	throws Exception
	{
		assert	this.isStarted() ;
		assert	request != null ;

		request.setOwnerReference(this) ;
		if (this.hasItsOwnThreads()) {
			if (this.isConcurrent) {
				return this.requestHandler.submit(request) ;
			} else {
				assert	this.canScheduleTasks ;
				return this.scheduledTasksHandler.submit(request) ;
			}
		} else {
			final ComponentService<T> t = request ;
			return new Future<T>() {
							@Override
							public boolean	cancel(boolean arg0)
							{ return false ; }

							@Override
							public T		get()
							throws	InterruptedException, ExecutionException
							{
								try {
									return t.call() ;
								} catch (Exception e) {
									throw new ExecutionException(e) ;
								}
							}
							@Override
							public T		get(long arg0, TimeUnit arg1)
							throws	InterruptedException,
									ExecutionException, TimeoutException
							{ return null ; }

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
	 * @see fr.sorbonne_u.components.ComponentI#handleRequestSync(fr.sorbonne_u.components.ComponentI.ComponentService)
	 */
	@Override
	public <T> T			handleRequestSync(ComponentService<T> request)
	throws Exception
	{
		assert	this.isStarted() ;
		assert	request != null ;

		request.setOwnerReference(this) ;
		if (this.hasItsOwnThreads()) {
			return this.handleRequest(request).get() ;
		} else {
			return request.call() ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#handleRequestAsync(fr.sorbonne_u.components.ComponentI.ComponentService)
	 */
	@Override
	public <T> void		handleRequestAsync(ComponentService<T> request)
	throws Exception
	{
		assert	this.isStarted() ;
		assert	request != null ;

		request.setOwnerReference(this) ;
		if (this.hasItsOwnThreads()) {
			this.handleRequest(request) ;
		} else {
			request.call();
		}
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
	 * @param request	service request to be scheduled.
	 * @param delay		delay after which the task must be run.
	 * @param u			time unit in which the delay is expressed.
	 * @return			a scheduled future to synchronise with the task.
	 */
	protected <T> ScheduledFuture<T>		scheduleRequest(
		ComponentService<T> request,
		long delay,
		TimeUnit u
		)
	{
		assert	this.isStarted() ;
		assert	this.canScheduleTasks() ;
		assert	request != null && delay >= 0 && u != null ;

		request.setOwnerReference(this) ;
		return this.scheduledTasksHandler.schedule(request, delay, u) ;
	}

	/**
	 * FIXME: does not make sense in the remote call case unless we have
	 * distributed future variables!
	 * 
	 * @see fr.sorbonne_u.components.ComponentI#scheduleRequestSync(fr.sorbonne_u.components.ComponentI.ComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T		scheduleRequestSync(
		ComponentService<T> request,
		long delay,
		TimeUnit u
		) throws InterruptedException, ExecutionException
	{
		assert	this.isStarted() ;
		assert	this.canScheduleTasks() ;
		assert	request != null && delay >= 0 && u != null ;

		request.setOwnerReference(this) ;
		return this.scheduleRequest(request, delay, u).get() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#scheduleRequestAsync(fr.sorbonne_u.components.ComponentI.ComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleRequestAsync(
		ComponentService<?> request,
		long delay,
		TimeUnit u
		)
	{
		assert	this.isStarted() ;
		assert	this.canScheduleTasks() ;
		assert	request != null && delay >= 0 && u != null ;

		request.setOwnerReference(this) ;
		this.scheduleRequest(request, delay, u) ;
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
	public String		getComponentDefinitionClassName()
	throws Exception
	{
		return this.getClass().getCanonicalName() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getComponentAnnotations()
	 */
	@Override
	public Annotation[]	getComponentAnnotations() throws Exception
	{
		return this.getClass().getAnnotations() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ComponentI#getComponentLoader()
	 */
	@Override
	public ClassLoader	getComponentLoader() throws Exception
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
		Class<?>[] pTypes = new Class<?>[params.length] ;
		for (int i = 0 ; i < params.length ; i++) {
			pTypes[i] = params[i].getClass() ;
		}
		Method m = this.getClass().getMethod(name, pTypes) ;
		return this.handleRequest(
						new AbstractService<Object>() {
							@Override
							public Object call() throws Exception {
								return m.invoke(this.getOwner(), params) ;
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
								return m.invoke(this.getOwner(), params) ;
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
		this.handleRequestAsync(
						new AbstractService<Object>() {
							@Override
							public Object call() throws Exception {
								return m.invoke(this.getOwner(), params) ;
							}
						}) ;
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