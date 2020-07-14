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

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Set;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.config.ConfigurationFileParser;
import fr.sorbonne_u.components.cvm.config.ConfigurationParameters;
import fr.sorbonne_u.components.cvm.config.exceptions.ConfigurationException;
import fr.sorbonne_u.components.cvm.utils.DCVMCyclicBarrierClient;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.helpers.Logger;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.components.pre.dcc.DynamicComponentCreator;
import fr.sorbonne_u.components.registry.ConnectionData;
import fr.sorbonne_u.components.registry.ConnectionType;
import fr.sorbonne_u.components.registry.GlobalRegistry;
import fr.sorbonne_u.components.registry.GlobalRegistryClient;
import fr.sorbonne_u.components.registry.protocol.LookupResponse;
import fr.sorbonne_u.components.registry.protocol.Response;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractDistributedCVM</code> defines the common properties
 * of distributed component virtual machines in the component model.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * DCVM are deployed on a set of Java virtual machines, themselves running over
 * a set of different hosts.  A deployment uses:
 * </p>
 * <ul>
 * <li>one RMI registry per host (limitation of the RMI registry provided by
 *   Oracle that entries may be set only on a registry that runs on the same
 *   host);</li>
 * <li>one JVM running the global registry that is mapping port URIs to the
 *   host on which RMI registry it is published;</li>
 * <li>one JVM running a  distributed cyclic barrier used to synchronise the
 *   deployment processes among the different JVM running components;</li>
 * <li>at least one, but more pragmatically more than two JVM running
 *   components which are given a URI (this URI is used in the deployment code
 *   to know which components must be created by the current JVM and which are
 *   created by other virtual machines.</li>
 * </ul>
 * <p>
 * The configuration file provides application-wide informations required on
 * each virtual machine to make the system work properly.  This file is giving
 * the necessary information to all of the different JVM, and to the tools like
 * the cyclic barrier used to synchronise the different JVM.  The Relax NG
 * schema of the configuration file is as follows:
 * </p>
 * <pre>
 * start = deployment
 * deployment = element deployment {
 *   codebase?,             # localisation of the code base of the application
 *   hosts,                 # description of the hosts
 *   cyclicBarrier,         # configuration of the cyclic barrier
 *   globalRegistry,        # configuration of the global registry
 *   rmiRegistryPort,       # configuration of the RMI registry
 *   jvms2hostnames         # mapping from JVM to hosts running them
 * }
 * codebase = element codebase {
 *   attribute hostname  { text },  # host on which the code base may be found
 *   attribute directory { text },  # directory in which the code base may be found
 *   empty
 * }
 * hosts = element hosts { host+ }
 * host = element host {
 *   attribute name { text },	# the name of the host
 *   attribute dir  { text },	# absolute path to the execution directory
 *   empty
 * }
 * cyclicBarrier = element cyclicBarrier {
 *   attribute hostname  { text },   # host on which the cyclic barrier is running
 *   attribute port      { xsd:int } # port number listen by the cyclic barrier
 * }
 * globalRegistry = element globalRegistry {
 *   attribute hostname  { text },   # host on which the global registry is running
 *   attribute port      { xsd:int } # port number listen by the global registry
 * }
 * rmiRegistryPort = element rmiRegistryPort {
 *   attribute no        { xsd:int }  # port number listen by the RMI registry
 * }
 * jvms2hostnames = element jvms2hostnames { jvm2hostname+ }
 * jvm2hostname = element jvm2hostname {
 *   attribute jvmuri { xsd:anyURI },      # JVM URI
 *                                         # is this JVM creating the RMI registry
 *   attribute rmiRegistryCreator { xsd:boolean },
 *   attribute hostname { text }           # name of the host running that JVM
 *   attribute mainclass { text },		# canonical class name of the main class
 *   attribute reflective { xsd:boolean }?
 * }
 * </pre>
 * <p>
 * The DCVM object created from the user-defined subclass of this abstract class
 * is meant to be executed on every JVM running components used for the
 * deployment.  The configuration file and the code of the user must be written
 * in such a way that it identifies the static components to be instantiated
 * locally in order to create them and then publish on the RMI registry the
 * connection points of their inbound ports (the ones for the offered interfaces
 * of the components) so that the client components will be able to get the
 * information from the registry to do the connection.
 * </p>
 * <p>
 * In order to ensure the availability of the information and to avoid deadlocks
 * in this initialisation phase, all of the static creations and inbound
 * connection publications must be done before beginning to query the registry
 * to connect local components to distant ones.  The cyclic barrier serves as a
 * mean for synchronisation to this end.
 * </p>
 * <p>
 * Starting a component-based application entails:
 * </p>
 * <ol>
 * <li>Starting the global registry on the global registry host.</li>
 * <li>On each host, start the different JVM that must run on that host, paying
 *   attention to the fact that all of these JVM must be given the URI that is
 *   used in the deployment code to identify the components that must be
 *   created on that JVM.</li>
 * </ol>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true			// no invariant yet, TODO
 * </pre>
 * 
 * <p>Created on : 2012-05-22</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractDistributedCVM
extends		AbstractCVM
implements	DistributedComponentVirtualMachineI
{
	// -------------------------------------------------------------------------
	// Deployment information
	// -------------------------------------------------------------------------

	/** indicates whether a RMI registry needs to be executing on all
	 * hosts, as required by the Oracle implementation which forbids to
	 * register a service on a registry that is not on the host doing
	 *  the registration.													*/
	public final static boolean			RMI_REGISTRY_ON_ALL_HOSTS = true;
	/** parameters obtained form the xml configuration file.				*/
	protected ConfigurationParameters	configurationParameters;
	/** name of the JVMs creating RMI registry.								*/
	protected static Set<String>		rmiRegistryCreators;
	/** name of the hosts holding RMI registry.								*/
	protected static Set<String>		rmiRegistryHosts;
	/** port number used for the RMI registry.								*/
	protected static int				rmiRegistryPort;
	/**	reference to the RMI registry.										*/
	protected static Registry			theRMIRegistry;

	// -------------------------------------------------------------------------
	// Accessing the current component virtual machine
	// -------------------------------------------------------------------------

	/**
	 * return a reference on the component distributed virtual machine
	 * instance running on this Java virtual machine.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	ret != null
	 * </pre>
	 *
	 * @return	a reference on the component distributed virtual machine instance running on this Java virtual machine.
	 */
	public static AbstractDistributedCVM	getCVM()
	{
		return (AbstractDistributedCVM) AbstractCVM.theCVM;
	}

	// -------------------------------------------------------------------------
	// Registry management
	// -------------------------------------------------------------------------

	/** Global registry client; singleton.									*/
	protected final static GlobalRegistryClient	GLOBAL_REGISTRY_CLIENT =
													new GlobalRegistryClient();

	/**
	 * publish inbound ports (data inbound ports and two way ports) both
	 * locally and globally, which includes the RMI registry and the global
	 * component registry; outbound ports need not be published globally
	 * but only locally (this information is used by the connection builder to
	 * decide how the connections to that port must be done).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	port != null
	 * post	true				// no more postconditions.
	 * </pre>
	 *
	 * @param port			port to be published
	 * @throws Exception	<i>todo.</i>
	 */
	public static void	publishPort(PortI port)
	throws	Exception
	{
		assert	port != null;
		//	TODO not already published in the global registry
		//	TODO not already published in the RMI registry of the current host
		//	!LOCAL_REGISTRY.containsKey(port.getPortURI())

		String portURI = port.getPortURI();
		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PORTS) &&
												AbstractCVM.isDistributed) {
			AbstractCVM.getCVM().logDebug(CVMDebugModes.PORTS,
					"called publishPort(" + portURI +
					") on the host " + AbstractCVM.getHostname());
		}

		AbstractCVM.localPublishPort(port);
		if (AbstractCVM.isDistributed) {
			assert	AbstractDistributedCVM.theRMIRegistry != null;

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
				AbstractCVM.getCVM().logDebug(CVMDebugModes.PORTS,
						"publishPort calls RMIRegistry on " +
								((PortI)port).getPortURI() + " ...");
			}

			AbstractDistributedCVM.theRMIRegistry.bind(portURI, (Remote) port);

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
				AbstractCVM.getCVM().logDebug(CVMDebugModes.PORTS,
											 "... done");
				AbstractCVM.getCVM().logDebug(CVMDebugModes.PORTS,
						"publishPort calls GlobalRegistry on " +
													portURI + " ...");
			}

			AbstractDistributedCVM.GLOBAL_REGISTRY_CLIENT.
				put(portURI,
					(new ConnectionData(
						ConnectionType.RMI,
						AbstractCVM.getHostname(),
						AbstractDistributedCVM.rmiRegistryPort)).toString());

			if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PORTS)) {
				AbstractCVM.getCVM().logDebug(CVMDebugModes.PORTS,
															"... done");
			}
		}

		//	LOCAL_REGISTRY.containsKey(port.getPortURI())
		//	port == LOCAL_REGISTRY.get(port.getPortURI())
		//	TODO published in the global registry
		//	TODO published in the RMI registry of the current host
	}

	/**
	 * unpublish previously published inbound ports (data inbound ports and
	 * two way ports) both locally and globally, which includes the RMI
	 * registry and the global component registry; outbound ports need not be
	 * published globally but only locally.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	port != null
	 * post	true				// no more postconditions.
	 * </pre>
	 *
	 * @param port			port to be unpublished.
	 * @throws Exception	<i>todo.</i>
	 */
	public static void	unpublishPort(PortI port)
	throws	Exception
	{
		assert	port != null;
		//	TODO published in the global registry
		//	TODO published in the RMI registry of the current host
		//	LOCAL_REGISTRY.containsKey(port.getPortURI())
		//	port == LOCAL_REGISTRY.get(port.getPortURI())

		String portURI = port.getPortURI();

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PORTS) &&
												AbstractCVM.isDistributed) {
			AbstractCVM.getCVM().logDebug(CVMDebugModes.PORTS,
					"called unpublishPort( " + portURI +
					") on the host " + AbstractCVM.getHostname());
		}

		AbstractCVM.localUnpublishPort(port);
		if (AbstractCVM.isDistributed) {
			assert	AbstractDistributedCVM.theRMIRegistry != null;
			AbstractDistributedCVM.theRMIRegistry.unbind(portURI);
			AbstractDistributedCVM.GLOBAL_REGISTRY_CLIENT.remove(portURI);
		}

		//	LOCAL_REGISTRY.containsKey(port.getPortURI())
		//	TODO not published in the global registry
		//	TODO not published in the RMI registry of the current host
	}

	/**
	 * find the remote reference corresponding to a port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param remoteURI	uri identifying the port in the registry.
	 * @return			reference to the component port.
	 * @throws Exception	<i>todo.</i>
	 */
	public Remote		getRemoteReference(String remoteURI)
	throws Exception
	{
		assert	remoteURI != null;

		Remote reference = null;
		String response =
			AbstractDistributedCVM.GLOBAL_REGISTRY_CLIENT.lookup(remoteURI);
		ConnectionData cd =
			(ConnectionData)
				((LookupResponse)Response.string2response(response)).
																interpret();
		if (cd.getType() == ConnectionType.RMI) {
			try {
				reference = Naming.lookup(
								"//" + cd.getHostname() +
								":" + AbstractDistributedCVM.rmiRegistryPort +
								"/" + remoteURI);
			} catch (MalformedURLException e) {
				System.out.println("MalformedURLException thrown when trying to get the remote reference of "+ remoteURI);
				throw e ;
			} catch (RemoteException e) {
				System.out.println("RemoteException thrown when trying to get the remote reference of "+ remoteURI);
				throw e ;
			} catch (NotBoundException e) {
				System.out.println("NotBoundException thrown when trying to get the remote reference of "+ remoteURI);
				throw e ;
			}
		} else {
			// cd.getType() == ConnectionType.SOCKET -- NOT YET TERMINATED
			throw new Exception("not a RMI port!");
		}

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.PORTS) &&
												AbstractCVM.isDistributed) {
			AbstractCVM.getCVM().logDebug(CVMDebugModes.PORTS,
					"called getRemoteReference(" + remoteURI +
					") on the host " + AbstractCVM.getHostname()
					+ " returning " + reference + ".");
		}

		return reference;
	}

	// -------------------------------------------------------------------------
	// Cyclic barrier management
	// -------------------------------------------------------------------------

	/**	distributed implementation of a cyclic barrier for assemblies.		*/
	protected DCVMCyclicBarrierClient	cyclicBarrierClient;

	/**
	 * wait on the cyclic barrier until all of the JVM have done this call,
	 * after which all will be released.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception		<i>todo.</i>
	 */
	public void			waitOnCyclicBarrier() throws Exception
	{
		this.cyclicBarrierClient.waitBarrier() ;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * instantiate the DCVM object.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * The constructor gets from the command line arguments the logical
	 * name of the current JVM in the assembly and the name of an XML
	 * configuration file giving a mapping between the URI of hosts to their
	 * IP addresses in the current deployment.  This JVM URI must be in the
	 * static array JVM_URIs and all of the hosts URI in the array HOSTS_URIs
	 * and only these ones must appear in the XML configuration file
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	args.length &gt; 1
	 * post	true			// TODO
	 * </pre>
	 *
	 * @param args	command line arguments from the main method.
	 * @throws Exception 		<i>todo.</i>
	 */
	public				AbstractDistributedCVM(
		String[] args
		) throws Exception
	{
		this(args, 0, 0);
	}

	/**
	 * instantiate the DCVM object redirecting the stdout and the stderr
	 * to a window frame position at the coordinate
	 * <code>(xLayout, yLayout)</code> among the similar frames created
	 * by the same application  (e.g., xLayout = 1 and yLayout = 1 will
	 * put the frame at the right and below the origin frame).
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * The constructor gets from the command line arguments the logical
	 * name of the current JVM in the assembly and the name of an XML
	 * configuration file giving a mapping between the URI of hosts to their
	 * IP addresses in the current deployment.  This JVM URI must be in the
	 * static array JVM_URIs and all of the hosts URI in the array HOSTS_URIs
	 * and only these ones must appear in the XML configuration file
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	args != null and args.length &gt; 1
	 * post	true			// TODO
	 * </pre>
	 *
	 * @param args		command line arguments from the main method.
	 * @param xLayout	x coordinate of the relative position of the frame attached to the CVM.
	 * @param yLayout	y coordinate of the relative position of the frame attached to the CVM.
	 * @throws Exception <i>todo.</i>
	 */
	public				AbstractDistributedCVM(
		String[] args,
		int xLayout,
		int yLayout
		) throws Exception
	{
		super(true, args[0]);

		assert	args != null && args.length > 1;

		File configFile = new File(args[1]);
		ConfigurationFileParser cfp = new ConfigurationFileParser();
		if (!cfp.validateConfigurationFile(configFile)) {
			throw new Exception("invalid configuration file " + args[1]);
		}
		this.configurationParameters = cfp.parseConfigurationFile(configFile);
		this.thisHostname =
			this.configurationParameters.getJvmURIs2hosts().
											get(AbstractCVM.getThisJVMURI());
		assert	AbstractCVM.getHostname() != null :
				new ConfigurationException("Hostname of JVM " +
										   AbstractCVM.getThisJVMURI() +
										   " undefined!");

		this.debugginLogger =
			new Logger("dcvm_" + AbstractCVM.getHostname().replace('.', '_'));

		GlobalRegistry.REGISTRY_HOSTNAME =
					this.configurationParameters.getGlobalRegistryHostname();
		GlobalRegistry.REGISTRY_PORT =
					this.configurationParameters.getGlobalRegistryPort();
		AbstractDistributedCVM.rmiRegistryCreators =
					this.configurationParameters.getRmiRegistryCreators();
		AbstractDistributedCVM.rmiRegistryHosts =
					this.configurationParameters.getRmiRegistryHosts();
		AbstractDistributedCVM.rmiRegistryPort =
					this.configurationParameters.getRmiregistryPort();
		this.state = CVMState.CREATED;

		// RMI registry creation
		if (AbstractDistributedCVM.rmiRegistryCreators.contains(
												AbstractCVM.getThisJVMURI())) {
			AbstractDistributedCVM.theRMIRegistry =
							LocateRegistry.createRegistry(rmiRegistryPort);
		} else {
			AbstractDistributedCVM.theRMIRegistry = null;
		}

		// Cyclic barrier client initialisation
		this.cyclicBarrierClient =
			new DCVMCyclicBarrierClient(
					this.configurationParameters.getCyclicBarrierHostname(),
					this.configurationParameters.getCyclicBarrierPort(),
					AbstractCVM.getHostname(),
					AbstractCVM.getThisJVMURI());
	}

	// -------------------------------------------------------------------------
	// Life-cycle
	// -------------------------------------------------------------------------

	/**
	 * instantiate the components, publish their connection points on the
	 * registry and interconnect them.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!this.deploymentDone()
	 * post	this.deploymentDone()
	 * </pre>
	 * @throws Exception 		<i>todo.</i>
	 * 
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#deploy()
	 */
	@Override
	public void			deploy() throws Exception
	{
		assert	!this.deploymentDone();

		// Wait until all of the assembly object instantiation, and therefore
		// be sure that the RMI registry has been created.
		this.waitOnCyclicBarrier();
		// Initialise the local RMI registry.
		this.initialise();
		this.waitOnCyclicBarrier();
		// Instantiate the components and publish their ports on the
		// different registry.
		this.instantiateAndPublish();
		this.waitOnCyclicBarrier();
		// Interconnect the components, lookup for their ports on the
		// different registry.
		this.interconnect();
		this.waitOnCyclicBarrier();
		super.deploy();

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			AbstractCVM.getCVM().logDebug(CVMDebugModes.LIFE_CYCLE,
											"called deploy() ...done.");
		}

		assert	this.deploymentDone();
	}

	/**
	 * initialise the RMI registry reference for JVM that are not responsible
	 * for its creation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!this.isInitialised()
	 * post	AbstractDistributedAssembly.theRMIRegistry != null
	 * post	this.isInitialised()
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.cvm.DistributedComponentVirtualMachineI#initialise()
	 */
	@Override
	public void			initialise() throws Exception
	{
		assert	!this.isInitialised();

		// RMI registry reference
		if (AbstractDistributedCVM.theRMIRegistry == null) {
			// looking for a host that has a running RMI registry
			// BEWARE: with Oracle RMI registry implementation, the registry
			//         for publication must be on the same host as the
			//         publisher!!
			//         If another vendor's registry is used, the following code
			//         assumes that only one registry will be running.
			String registryHostname = null;
			if (!AbstractDistributedCVM.
								rmiRegistryHosts.contains(thisHostname)) {
				// Take the first, most probably the only one in this case
				registryHostname =
						AbstractDistributedCVM.rmiRegistryHosts.
														iterator().next();
			} else {
				registryHostname = thisHostname;
			}
			AbstractDistributedCVM.theRMIRegistry =
					LocateRegistry.getRegistry(
								registryHostname,
								AbstractDistributedCVM.rmiRegistryPort);
		}

		this.state = CVMState.INITIALISED;

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			AbstractCVM.getCVM().logDebug(CVMDebugModes.LIFE_CYCLE,
											"called initialise() ...done.");
		}

		assert	AbstractDistributedCVM.theRMIRegistry != null;
		assert	this.isInitialised();
	}

	/**
	 * simply establish that the instantiation of components and the publication
	 * of the entry points are completed, so it should be called at the end of
	 * the user's own <code>instantiateAndPublish</code> method.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isInitialised()
	 * post	this.isIntantiatedAndPublished()
	 * </pre>
	 * 
	 * @throws Exception 			<i>todo.</i>
	 * @see fr.sorbonne_u.components.cvm.DistributedComponentVirtualMachineI#instantiateAndPublish()
	 */
	@Override
	public void			instantiateAndPublish() throws Exception
	{
		assert	this.isInitialised();

		try {
			String dccURI =
				AbstractComponent.createComponent(
					DynamicComponentCreator.class.getCanonicalName(),
					new Object[]{AbstractCVM.getThisJVMURI() +
									DCC_INBOUNDPORT_URI_SUFFIX});
			assert	this.isDeployedComponent(dccURI);
		} catch (Exception e) {
			System.out.println("The dynamic component creator has not been "
											+ "successfully deployed!");
			throw e;
		}

		this.state = CVMState.INSTANTIATED_AND_PUBLISHED;

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			AbstractCVM.getCVM().logDebug(CVMDebugModes.LIFE_CYCLE,
								"called instantiateAndPublish() ...done.");
		}

		assert	this.isIntantiatedAndPublished();
	}

	/**
	 * simply check if instantiation of components and the publication of the
	 * entry points are completed, so it should be called at the beginning of
	 * the user's own <code>interconnect</code> method.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isIntantiatedAndPublished()
	 * post	this.isInterconnected()
	 * </pre>
	 * 
	 * @throws Exception 		<i>todo.</i>
	 * @see fr.sorbonne_u.components.cvm.DistributedComponentVirtualMachineI#interconnect()
	 */
	@Override
	public void			interconnect() throws Exception
	{
		assert	this.isIntantiatedAndPublished();

		this.state = CVMState.INTERCONNECTED;

		if (DEBUG_MODE.contains(CVMDebugModes.LIFE_CYCLE)) {
			AbstractCVM.getCVM().logDebug(CVMDebugModes.LIFE_CYCLE,
									"called interconnect() ...done.");
		}

		assert	this.isInterconnected();
	}

	/**
	 * simply check if the deployment and the interconnection are completed,
	 * so it should be called at the beginning of the user's own
	 * <code>start</code> method.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.deploymentDone()
	 * post	this.allStarted()
	 * </pre>

	 * @throws Exception  <i>todo.</i>
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#start()
	 */
	@Override
	public void			start() throws Exception
	{
		assert	this.deploymentDone();

		// Start all of the components that are running within the current
		// virtual machine
		super.start();

		assert	this.allStarted();
	}

	/**
	 * The method if called is right after calling <code>start</code>, hence it
	 * first synchronises all of the distributed CVM to be sure that all of the
	 * components have been started and then it calls the super method to perform
	 * the <code>execute</code> methods of each component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.allStarted()
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		assert	this.allStarted();

		this.waitOnCyclicBarrier();
		super.execute();
	}

	
	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		assert	this.allStarted();

		this.waitOnCyclicBarrier();
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#shutdown()
	 */
	@Override
	public void			shutdown() throws Exception
	{
		assert	this.allFinalised();

		this.waitOnCyclicBarrier();
		super.shutdown();
		this.waitOnCyclicBarrier();
		this.cyclicBarrierClient.closeBarrier();
		AbstractDistributedCVM.GLOBAL_REGISTRY_CLIENT.shutdown();

		assert	this.isShutdown();
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#shutdownNow()
	 */
	@Override
	public void			shutdownNow() throws Exception
	{
		assert	this.allFinalised();

		this.waitOnCyclicBarrier();
		super.shutdownNow();
		this.waitOnCyclicBarrier();
		this.cyclicBarrierClient.closeBarrier();
		AbstractDistributedCVM.GLOBAL_REGISTRY_CLIENT.shutdown();

		assert	this.isShutdown();
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#isInitialised()
	 */
	@Override
	public boolean		isInitialised()
	{
		return this.state == CVMState.INITIALISED;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#isIntantiatedAndPublished()
	 */
	@Override
	public boolean		isIntantiatedAndPublished()
	{
		return	this.state == CVMState.INSTANTIATED_AND_PUBLISHED;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.ComponentVirtualMachineI#isInterconnected()
	 */
	@Override
	public boolean		isInterconnected()
	{
		return	this.state == CVMState.INTERCONNECTED;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#deploymentDone()
	 */
	@Override
	public boolean		deploymentDone()
	{
		return this.state == CVMState.DEPLOYMENT_DONE;
	}

	// -------------------------------------------------------------------------
	// Instance Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#logPrefix()
	 */
	@Override
	public String		logPrefix()
	{
		return AbstractCVM.getThisJVMURI() ;
	}	
}
// -----------------------------------------------------------------------------
