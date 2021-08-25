package fr.sorbonne_u.components.cvm;

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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.helpers.CVMDebugModesI;
import fr.sorbonne_u.components.helpers.Logger;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.components.pre.dcc.DynamicComponentCreator;

//-----------------------------------------------------------------------------
/**
 * The class <code>AbstractCVM</code> defines the common properties of
 * component virtual machines in the component model.
 *
 * <p><strong>Description</strong></p>
 * 
 * Local CVM are deployed on a single Java virtual machine and have local
 * component interconnections only.  A CVM must define a <code>deploy</code>
 * method that includes all the code necessary to instantiate and interconnect
 * the static components in the application.  Then, they must define a
 * <code>start</code> method that plays the role of a <code>main</code> method
 * in object-oriented Java applications.
 * 
 * <p><strong>Usage</strong></p>
 * 
 * Local CVM are defined as subclasses of this abstract class.  A CVM has to
 * redefine <code>deploy</code> and may redefine <code>start</code>.  The
 * method <code>start</code> defined here defaults to starting all of the
 * components registered as deployed on this site by calling the method
 * <code>addDeployedComponent</code>.
 * 
 * Every port that will be used to connect components must be published in
 * the local registry by calling the method <code>localPublishPort</code> and
 * it can be unpublished by calling the method <code>localUnpublishPort</code>.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		AbstractCVM.getCVM() != null
 * </pre>
 * 
 * <p>Created on : 2011-11-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractCVM
implements	ComponentVirtualMachineI
{
	// ------------------------------------------------------------------------
	// CVM state
	// ------------------------------------------------------------------------

	/** URI of the (unique) JVM running BCM for mono-JVM deployments.		*/
	public static final String				MONO_JVM_JVMURI = "thisJVM";
	/** name of the host running BCM for mono-JVM deployments.				*/
	public static final String				MONO_JVM_HOSTNAME = "localhost";

	/** The singleton pattern: one instance of CVM per JVM.					*/
	protected static AbstractCVM			theCVM;
	/** URI of the current JVM in the deployment platform; note that
	 *  only one object from {@code AbstractCVM} can exist in one JVM.		*/
	protected final String					thisJVMURI;
	/** name of the host on which the JVM is running; this field must be
	 *  effectively final after the instance creation.						*/
	protected String						thisHostname;
	/**	Enables or not debugging messages.									*/
	public static final Set<CVMDebugModesI>	DEBUG_MODE =
												new HashSet<CVMDebugModesI>();
	/** suffix for the dynamic component creator component inbound port URI.*/
	public static final String				DCC_INBOUNDPORT_URI_SUFFIX = "-dcc";
	// ------------------------------------------------------------------------
	// Assertions status checking
	// ------------------------------------------------------------------------

	/**
	 * check if the assertions have been enabled on the JVM running this code
	 * and print a warning if not.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public static void	checkAssertionsEnabled()
	{
		boolean assertionsEnabled = false ;
		// The assignment as expression returns the assigned value, hence the
		// next assertion will assign true to assertionsEnabled if it is
		// executed i.e., if the assertions are enabled.
		assert	assertionsEnabled = true ;
		if (!assertionsEnabled) {
			System.out.println(
			"************************************************************\n" +
			"* WARNING! -- BCM is executed without having enabled the   *\n" +
			"* assertions. To get the benefits of assertions checking,  *\n" +
			"* checking, the JVM should be launched with the parameter  *\n" +
			"* \"-ea\" or \"-enableassertions\".                            *\n" +
			"************************************************************") ;
		}
	}

	// ------------------------------------------------------------------------
	// Accessing the current component virtual machine information
	// ------------------------------------------------------------------------

	/**
	 * return a reference on the component virtual machine instance running
	 * on this Java virtual machine.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	ret != null
	 * </pre>
	 *
	 * @return	a reference on the component virtual machine instance running on this Java virtual machine.
	 */
	public static AbstractCVM	getCVM()
	{
		return AbstractCVM.theCVM;
	}

	/**
	 * return the URI of the JVM running this component virtual machine.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the URI of the JVM running this component virtual machine.
	 */
	public static String	getThisJVMURI()
	{
		return AbstractCVM.getCVM().thisJVMURI;
	}

	/**
	 * return the name of the host running this component virtual machine.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the name of the host running this component virtual machine.
	 */
	public static String	getHostname()
	{
		return AbstractCVM.getCVM().thisHostname;
	}

	// ------------------------------------------------------------------------
	// Local registry
	// ------------------------------------------------------------------------

	/** initial number of potential entries in the local registry.		*/
	protected static int					LOCAL_REGISTRY_INIT_SIZE = 1000;
	/** local registry linking port URI to local port objects.			*/
	protected static final
		ConcurrentHashMap<String,PortI> LOCAL_REGISTRY =
				new ConcurrentHashMap<String,PortI>(LOCAL_REGISTRY_INIT_SIZE);

	/**
	 * return true if the local registry has been initialised.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the local registry has been initialised.
	 */
	protected static boolean		localRegistryInitialised()
	{
		return AbstractCVM.LOCAL_REGISTRY != null;
	}

	/**
	 * return true if the <code>key</code> correspond to a publication
	 * in the local registry.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code AbstractCVM.localRegistryInitialised()}
	 * pre	{@code key != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param key	key to be tested.
	 * @return	true if the <code>key</code> correspond to a publication in the local registry.
	 */
	public static boolean	isPublishedInLocalRegistry(String key)
	{
		assert	AbstractCVM.localRegistryInitialised();
		assert	key != null;

		boolean ret = AbstractCVM.LOCAL_REGISTRY.containsKey(key);

//		if (DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
//			AbstractCVM.getCVM().logDebug(CVMDebugModes.PORTS,
//					"called isPublishedInLocalRegistry(" + key + ")" +
//					" returning " + ret);
//		}

		return ret;
	}

	/**
	 * publish a port in the local registry.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code AbstractCVM.localRegistryInitialised()}
	 * pre	{@code key != null}
	 * pre	{@code p != null}
	 * pre	{@code !AbstractCVM.isPublishedInLocalRegistry(key)}
	 * post	{@code AbstractCVM.isPublishedInLocalRegistry(key)}
	 * </pre>
	 *
	 * @param key		key under which the publication is done.
	 * @param p			the port to be published.
	 * @throws Exception <i>TODO.</i>
	 */
	protected static void		publishInLocalRegistry(String key, PortI p)
	throws Exception
	{
		assert	AbstractCVM.localRegistryInitialised();
		assert	key != null;
		assert	p != null;
		assert	!AbstractCVM.isPublishedInLocalRegistry(key);
		assert	key.equals(p.getPortURI());

		AbstractCVM.LOCAL_REGISTRY.put(key, p);

		if (DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
			AbstractCVM.getCVM().logDebug(CVMDebugModes.PORTS,
					"called publishInLocalRegistry(" + key + ", "
					+ p.getPortURI() + ") ...done.");
		}

		assert	AbstractCVM.isPublishedInLocalRegistry(key);
	}

	/**
	 * get the port published under the given key.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code AbstractCVM.localRegistryInitialised()}
	 * pre	{@code key != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param key		the key under which the publication has been done.
	 * @return			the port published under the given key.
	 * @throws Exception <i>TODO.</i>
	 */
	public static PortI		getFromLocalRegistry(String key) throws Exception
	{
		assert	AbstractCVM.localRegistryInitialised();
		assert	key != null;

		PortI p = AbstractCVM.LOCAL_REGISTRY.get(key);

//		if (DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
//			AbstractCVM.getCVM().logDebug(CVMDebugModes.PORTS,
//					"called getFromLocalRegistry(" + key + ")"
//					+ " returning "	+ (p == null ? p : p.getPortURI()) + ")");
//		}

		return p;
	}

	/**
	 * unpublish a port from the local registry.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code AbstractCVM.localRegistryInitialised()}
	 * pre	{@code key != null}
	 * pre	{@code AbstractCVM.isPublishedInLocalRegistry(key)}
	 * post	{@code !AbstractCVM.isPublishedInLocalRegistry(key)}
	 * </pre>
	 *
	 * @param key	key under which the publication has been done.
	 */
	protected static void	unpublishFromLocalRegistry(String key)
	{
		assert	AbstractCVM.localRegistryInitialised();
		assert	key != null;
		assert	AbstractCVM.isPublishedInLocalRegistry(key);

		AbstractCVM.LOCAL_REGISTRY.remove(key);	

		if (DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
			AbstractCVM.getCVM().logDebug(CVMDebugModes.PORTS,
					"called unpublishFromLocalRegistry(" + key + ")"
					+ " ...done. ");
		}

		assert	!AbstractCVM.isPublishedInLocalRegistry(key) ;
	}

	// ------------------------------------------------------------------------
	// Internal information about components in the CVM and CVM life-cycle
	// management.
	// ------------------------------------------------------------------------

	/** map from URI of reflection inbound ports to deployed components.	*/
	protected final ConcurrentHashMap<String, ComponentI>	uri2component;
	/** the state of the component virtual machine.							*/
	protected CVMState						state;
	/** true if the CVM currently running is distributed.					*/
	public static boolean					isDistributed;
	/** the logger used for debugging log entries.							*/
	protected Logger						debugginLogger;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * create and initialise a local CVM.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @throws Exception <i>to do.</i>
	 */
	public				AbstractCVM() throws Exception
	{
		this(false);
	}

	/**
	 * create and initialise the CVM, tagged as distributed if the parameter is
	 * true, and as local otherwise; note however that distributed CVM must be
	 * created as subclasses of the class <code>AbstractDistributedCVM</code>,
	 * so this constructor should never be called directly but only through the
	 * other constructor of this class or through one of the constructors of
	 * <code>AbstractDistributedCVM</code>.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	AbstractCVM.localRegistryInitialised()
	 * </pre>
	 *
	 * @param isDistributed	true if the CVM is distributed, false otherwise.
	 * @throws Exception	<i>to do.</i>
	 */
	public				AbstractCVM(
		boolean isDistributed
		) throws Exception
	{
		this(isDistributed, AbstractCVM.MONO_JVM_JVMURI);
	}

	/**
	 * actual creator for CVM.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code thisJVMURI != null && thisHostName != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param isDistributed	true if the CVM is distributed, false otherwise.
	 * @param thisJVMURI	URI of the current JVM in the deployment platform.
	 * @throws Exception	true if the CVM is distributed, false otherwise.
	 */
	protected			AbstractCVM(
		boolean isDistributed,
		String thisJVMURI
		) throws Exception
	{
		super();

		AbstractCVM.theCVM = this;
		this.thisJVMURI = thisJVMURI;
		this.thisHostname = AbstractCVM.MONO_JVM_HOSTNAME;
		this.uri2component = new ConcurrentHashMap<>();
		this.state = null;
		AbstractCVM.isDistributed = isDistributed;

		AbstractCVM.checkAssertionsEnabled();

		if (!isDistributed) {
			this.debugginLogger = new Logger("cvm");

			// when distributed, the dynamic component creator is created and
			// initialised in the method initialise of AbstractDistributedCVM
			try {
				String dccURI =
					AbstractComponent.createComponent(
						DynamicComponentCreator.class.getCanonicalName(),
						new Object[]{AbstractCVM.getThisJVMURI() +
											DCC_INBOUNDPORT_URI_SUFFIX});
				assert	this.isDeployedComponent(dccURI) ;
			} catch (Exception e) {
				this.logDebug(null, "WARNING! -- The dynamic component "
									+ "creator has not been "
									+ "successfully deployed!") ;
				throw e;
			}
		}

		assert	AbstractCVM.localRegistryInitialised();
	}

	// ------------------------------------------------------------------------
	// Static Methods
	// ------------------------------------------------------------------------

	/**
	 * publish the port in the local registry.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	port != null
	 * pre	AbstractCVM.localRegistryInitialised()
	 * pre	!AbstractCVM.isPublishedInLocalRegistry(port.getPortURI())
	 * post AbstractCVM.isPublishedInLocalRegistry(port.getPortURI())
	 * post	AbstractCVM.getFromLocalRegistry(port.getPortURI()) != null
	 * post port == AbstractCVM.getFromLocalRegistry(port.getPortURI())
	 * </pre>
	 * 
	 * @param port		port to be published
	 * @throws Exception <i>to do.</i>
	 */
	public synchronized static void	localPublishPort(PortI port)
	throws	Exception
	{
		assert	port != null;
		assert	AbstractCVM.localRegistryInitialised() ;
		assert	!AbstractCVM.isPublishedInLocalRegistry(port.getPortURI());

		AbstractCVM.publishInLocalRegistry(port.getPortURI(), port);

		if (DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
			AbstractCVM.getCVM().logDebug(CVMDebugModes.PORTS,
				"called localPublishPort(" + port.getPortURI() + ") ...done.");
		}

		assert	AbstractCVM.isPublishedInLocalRegistry(port.getPortURI());
		PortI p = AbstractCVM.getFromLocalRegistry(port.getPortURI());
		assert	p != null && port == p;
	}

	/**
	 * unpublish the port in the local registry.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	port != null
	 * pre	AbstractCVM.localRegistryInitialised()
	 * pre	AbstractCVM.isPublishedInLocalRegistry(port.getPortURI()) != null
	 * pre	port == AbstractCVM.getFromLocalRegistry(port.getPortURI())
	 * pre	port == AbstractCVM.getFromLocalRegistry(port.getPortURI())
	 * post	!AbstractCVM.isPublishedInLocalRegistry(port.getPortURI())
	 * </pre>
	 *
	 * @param port		port to be unpublished.
	 * @throws Exception <i>to do.</i>
	 */
	public synchronized static void	localUnpublishPort(PortI port)
	throws	Exception
	{
		assert	port != null;
		assert	AbstractCVM.localRegistryInitialised();
		assert	AbstractCVM.isPublishedInLocalRegistry(port.getPortURI());
		PortI p = AbstractCVM.getFromLocalRegistry(port.getPortURI());
		assert	p != null && port == p;

		AbstractCVM.unpublishFromLocalRegistry(port.getPortURI());

		if (DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
			AbstractCVM.getCVM().logDebug(CVMDebugModes.PORTS,
				"called localUnpublishPort(" + port.getPortURI() + ") ...done.");
		}

		assert	!AbstractCVM.isPublishedInLocalRegistry(port.getPortURI());
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * simply set the <code>deploymentDone</code> flag to true, so it should
	 * be called at the end of the user's own <code>deploy</code> method.
	 *
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!this.deploymentDone()
	 * post	this.deploymentDone()
	 * </pre>
	 * 
	 * @throws Exception  <i>to do.</i>
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#deploy()
	 */
	@Override
	public void			deploy() throws Exception
	{
		assert	!this.deploymentDone();

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			this.logDebug(CVMDebugModes.LIFE_CYCLE, "called deploy() ...done.");
		}

		this.state = CVMState.DEPLOYMENT_DONE;
	}
	
	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#isDeployedComponent(java.lang.String)
	 */
	@Override
	public boolean		isDeployedComponent(String componentURI)
	{
		assert	componentURI != null;

		boolean ret = this.uri2component.containsKey(componentURI);

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			this.logDebug(CVMDebugModes.LIFE_CYCLE,
						  "called isDeployedComponent(" + componentURI
						  + ") returning " + ret + ".");
		}

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#addDeployedComponent(java.lang.String, fr.sorbonne_u.components.ComponentI)
	 */
	@Override
	public void			addDeployedComponent(
		String componentURI,
		ComponentI component
		)
	{
		assert	componentURI != null && component != null;
		assert	!this.isDeployedComponent(componentURI);

		this.uri2component.put(componentURI, component);

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			this.logDebug(CVMDebugModes.LIFE_CYCLE,
						  "called addDeployedComponent(" + component
						  + ") ...done.");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#removeDeployedComponent(java.lang.String)
	 */
	@Override
	public void			removeDeployedComponent(String componentURI)
	{
		assert	componentURI != null;
		assert	this.isDeployedComponent(componentURI);

		this.uri2component.remove(componentURI);

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			this.logDebug(CVMDebugModes.LIFE_CYCLE,
						  "called removeDeployedComponent(" + componentURI
						  + ") ...done.");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#start()
	 */
	@Override
	public void			start() throws Exception
	{
		assert	this.deploymentDone();

		for(ComponentI c : this.uri2component.values()) {
			if (!c.isStarted()) {
				c.start() ;	
			}
		}
		this.state = CVMState.START_DONE;

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			this.logDebug(CVMDebugModes.LIFE_CYCLE, "called start() ...done");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#startComponent(java.lang.String)
	 */
	@Override
	public void			startComponent(String componentURI)
	throws Exception
	{
		assert	componentURI != null;
		assert	this.isDeployedComponent(componentURI);

		this.uri2component.get(componentURI).start();

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			this.logDebug(CVMDebugModes.LIFE_CYCLE,
						 "called startComponent(" + componentURI + ") ...done");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		assert	this.allStarted();

		for(ComponentI c : this.uri2component.values()) {
			if (c.hasItsOwnThreads()) {
				c.runTask(new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							this.getTaskOwner().execute() ;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		}

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			this.logDebug(CVMDebugModes.LIFE_CYCLE, "called execute() ...done");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#executeComponent(java.lang.String)
	 */
	@Override
	public void			executeComponent(String componentURI)
	throws Exception
	{
		assert	componentURI != null;
		assert	this.isStartedComponent(componentURI);

		this.uri2component.get(componentURI).runTask(
				new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								this.getTaskOwner().execute() ;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
				}) ;

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			this.logDebug(CVMDebugModes.LIFE_CYCLE,
					"called executeComponent(" + componentURI + ") ...done");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		assert	this.allStarted();

		for(ComponentI c : this.uri2component.values()) {
			c.finalise();
		}

		this.state = CVMState.FINALISE_DONE;

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			this.logDebug(CVMDebugModes.LIFE_CYCLE, "called finalise() ...done");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#finaliseComponent(java.lang.String)
	 */
	@Override
	public void			finaliseComponent(String componentURI)
	throws Exception
	{
		assert	componentURI != null;
		assert	this.isStartedComponent(componentURI);

		this.uri2component.get(componentURI).finalise();

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			this.logDebug(CVMDebugModes.LIFE_CYCLE,
					"called finaliseComponent(" + componentURI + ") ...done");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#shutdown()
	 */
	@Override
	public void			shutdown() throws Exception
	{
		assert	this.allFinalised();

		for(ComponentI c : this.uri2component.values()) {
			c.shutdown();
		}

		this.state = CVMState.SHUTDOWN;

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			AbstractCVM.getCVM().logDebug(CVMDebugModes.LIFE_CYCLE,
											"called shutdown() ...done");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#shutdownComponent(java.lang.String)
	 */
	@Override
	public void			shutdownComponent(String componentURI)
	throws Exception
	{
		assert	componentURI != null;
		assert	this.isFinalisedComponent(componentURI);

		this.uri2component.get(componentURI).shutdown();

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			AbstractCVM.getCVM().logDebug(CVMDebugModes.LIFE_CYCLE,
				"called shutdownComponent(" + componentURI + ") ...done");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#shutdownNow()
	 */
	@Override
	public void			shutdownNow() throws Exception
	{
		assert	this.allFinalised();

		for(ComponentI c : this.uri2component.values()) {
			c.shutdownNow();
		}

		this.state = CVMState.SHUTDOWN;

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			this.logDebug(CVMDebugModes.LIFE_CYCLE,
						  "called shutdownNow() ...done");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#shutdownNowComponent(java.lang.String)
	 */
	@Override
	public void			shutdownNowComponent(String componentURI)
	throws Exception
	{
		assert	componentURI != null;
		assert	this.isFinalisedComponent(componentURI);

		this.uri2component.get(componentURI).shutdown();

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			AbstractCVM.getCVM().logDebug(CVMDebugModes.LIFE_CYCLE,
				"called shutdownNowComponent(" + componentURI + ") ...done");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#isInitialised()
	 */
	@Override
	public boolean		isInitialised()
	{
		return	this.state == CVMState.INITIALISED ||
				this.state == CVMState.INSTANTIATED_AND_PUBLISHED ||
				this.state == CVMState.INTERCONNECTED ||
				this.state == CVMState.DEPLOYMENT_DONE;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#isIntantiatedAndPublished()
	 */
	@Override
	public boolean		isIntantiatedAndPublished()
	{
		return	this.state == CVMState.INITIALISED ||
				this.state == CVMState.INSTANTIATED_AND_PUBLISHED ||
				this.state == CVMState.INTERCONNECTED ||
				this.state == CVMState.DEPLOYMENT_DONE;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#isInterconnected()
	 */
	@Override
	public boolean		isInterconnected()
	{
		return	this.state == CVMState.INITIALISED ||
				this.state == CVMState.INSTANTIATED_AND_PUBLISHED ||
				this.state == CVMState.INTERCONNECTED ||
				this.state == CVMState.DEPLOYMENT_DONE;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#deploymentDone()
	 */
	@Override
	public boolean		deploymentDone()
	{
		return	this.state == CVMState.INITIALISED ||
				this.state == CVMState.INSTANTIATED_AND_PUBLISHED ||
				this.state == CVMState.INTERCONNECTED ||
				this.state == CVMState.DEPLOYMENT_DONE;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#allStarted()
	 */
	@Override
	public boolean		allStarted()
	{
		return this.state == CVMState.START_DONE;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#isStartedComponent(java.lang.String)
	 */
	@Override
	public boolean		isStartedComponent(String componentURI)
	{
		assert	componentURI != null;
		assert	this.isDeployedComponent(componentURI);

		return this.uri2component.get(componentURI).isStarted();
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#allFinalised()
	 */
	@Override
	public boolean		allFinalised()
	{
		return this.state == CVMState.FINALISE_DONE;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#isFinalisedComponent(java.lang.String)
	 */
	@Override
	public boolean		isFinalisedComponent(String componentURI)
	{
		assert	componentURI != null;
		assert	this.isDeployedComponent(componentURI);

		return this.uri2component.get(componentURI).isFinalised();
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#isShutdown()
	 */
	@Override
	public boolean		isShutdown()
	{
		boolean ret = true ;
		for(ComponentI c : this.uri2component.values()) {
			ret = ret && c.isShutdown();
		}
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#isShutdownComponent(java.lang.String)
	 */
	@Override
	public boolean		isShutdownComponent(String componentURI)
	{
		assert	componentURI != null;
		assert	this.isDeployedComponent(componentURI);

		return this.uri2component.get(componentURI).isShutdown();
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#isTerminated()
	 */
	@Override
	public boolean		isTerminated()
	{
		boolean ret = true;
		for(ComponentI c : this.uri2component.values()) {
			ret = ret && c.isTerminated();
		}
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#isTerminatedComponent(java.lang.String)
	 */
	@Override
	public boolean		isTerminatedComponent(String componentURI)
	{
		assert	componentURI != null;
		assert	this.isDeployedComponent(componentURI);

		return this.uri2component.get(componentURI).isTerminated();
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#startStandardLifeCycle(long)
	 */
	@Override
	public boolean		startStandardLifeCycle(long duration)
	{
		try {
			assert	duration > 0;
			this.deploy();
			System.out.println("starting...");
			this.start();
			System.out.println("executing...");
			this.execute();
			Thread.sleep(duration);
			System.out.println("finalising...");
			this.finalise();
			System.out.println("shutting down...");
			this.shutdown();
			System.out.println("ending...");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#awaitTermination(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public boolean		awaitTermination(long timeout, TimeUnit unit)
	throws InterruptedException
	{
		// TODO needs more reflection... how to await termination of several
		// entities?
		return false;
	}

	// ------------------------------------------------------------------------
	// Component management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#doPortConnection(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void			doPortConnection(
		String componentURI,
		String outboundPortURI,
		String inboundPortURI,
		String connectorClassname
		) throws Exception
	{
		assert	componentURI != null && outboundPortURI != null &&
					inboundPortURI != null && connectorClassname != null;
		assert	this.isDeployedComponent(componentURI);

		this.uri2component.get(componentURI).doPortConnection(
					outboundPortURI, inboundPortURI, connectorClassname);
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#doPortDisconnection(java.lang.String, java.lang.String)
	 */
	@Override
	public void			doPortDisconnection(
		String componentURI,
		String outboundPortURI
		) throws Exception
	{
		assert	componentURI != null && outboundPortURI != null;
		assert	this.isDeployedComponent(componentURI);

		this.uri2component.get(componentURI).
								doPortDisconnection(outboundPortURI);
	}

	// ------------------------------------------------------------------------
	// Debugging
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#logPrefix()
	 */
	public String		logPrefix()
	{
		return "CVM";
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#logDebug(fr.sorbonne_u.components.helpers.CVMDebugModesI, java.lang.String)
	 */
	public void			logDebug(CVMDebugModesI dm, String message)
	{
		StringBuffer logEntry =
			new StringBuffer().append(System.currentTimeMillis()).append("|").
					append(this.logPrefix()).append("|").append(dm).
					append("|").append(message);
		System.out.println(logEntry);
		this.debugginLogger.logMessage(logEntry.toString());
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#toggleTracing(java.lang.String)
	 */
	@Override
	public void			toggleTracing(String componentURI)
	{
		assert	componentURI != null;
		assert	this.isDeployedComponent(componentURI);

		this.uri2component.get(componentURI).toggleTracing();
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#toggleLogging(java.lang.String)
	 */
	@Override
	public void			toggleLogging(String componentURI)
	{
		assert	componentURI != null;
		assert	this.isDeployedComponent(componentURI);

		this.uri2component.get(componentURI).toggleLogging();
	}
}
//-----------------------------------------------------------------------------
