package fr.sorbonne_u.components.plugins.asynccall.example;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;

// -----------------------------------------------------------------------------
/**
 * The class <code>DistributedCVM</code> implements the multi-JVM assembly for
 * the asynchronous call plug-in example.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * A server component defined by the class <code>Server</code> implements two
 * simple services, add and show, which will be called asynchronously by a
 * client component defined by the class <code>Client</code>. The call is
 * made through the <code>asynccall</code> plug-in that offers a limited
 * asynchronous call with future capability.
 * </p>
 * <p>
 * The server component is deployed within a JVM running an instance of the CVM
 * called <code>server</code> in the <code>config.xml</code> file. The client
 * component is deployed in the instance called <code>client</code>.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2014-01-22</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			DistributedCVM
extends		AbstractDistributedCVM
{
	protected static final String	SERVER_COMPONENT_URI = "my-server";
	protected static final String	CLIENT_COMPONENT_URI = "my-client";

	// URI of the CVM instances as defined in the config.xml file
	protected static String			SERVER_JVM_URI = "server";
	protected static String			CLIENT_JVM_URI = "client";

	/** URI is fixed so this solution could be readily distributed.			*/
	protected static final String	SERVER_REFLECTION_INBOUND_PORT_URI =
																	"my-server";

	public				DistributedCVM(String[] args)
	throws Exception
	{
		super(args);
	}

	/**
	 * do some initialisation before anything can go on.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.cvm.AbstractDistributedCVM#initialise()
	 */
	@Override
	public void			initialise() throws Exception
	{
		// ---------------------------------------------------------------------
		// Configuration phase
		// ---------------------------------------------------------------------

		// debugging mode configuration; comment and uncomment the line to see
		// the difference
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.LIFE_CYCLE);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.INTERFACES);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.PORTS);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CONNECTING);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CALLING);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.EXECUTOR_SERVICES);

		super.initialise();
		// any other application-specific initialisation must be put here

	}

	/**
	 * instantiate components and publish their ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.cvm.AbstractDistributedCVM#instantiateAndPublish()
	 */
	@Override
	public void			instantiateAndPublish() throws Exception
	{
		if (thisJVMURI.equals(SERVER_JVM_URI)) {

			// create the server component
			AbstractComponent.createComponent(
					Server.class.getCanonicalName(),
					new Object[]{SERVER_REFLECTION_INBOUND_PORT_URI});

		} else if (thisJVMURI.equals(CLIENT_JVM_URI)) {

			// create the client component
			AbstractComponent.createComponent(
					Client.class.getCanonicalName(),
					new Object[]{SERVER_REFLECTION_INBOUND_PORT_URI});

		} else {

			System.out.println("Unknown JVM URI... " + thisJVMURI);

		}

		super.instantiateAndPublish();
	}

	public static void	main(String[] args)
	{
		try {
			DistributedCVM da  = new DistributedCVM(args);
			da.startStandardLifeCycle(10000L);
			Thread.sleep(10000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
// -----------------------------------------------------------------------------
