package fr.sorbonne_u.components.examples.basic_cs;

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
import fr.sorbonne_u.components.examples.basic_cs.components.URIConsumer;
import fr.sorbonne_u.components.examples.basic_cs.components.URIProvider;
import fr.sorbonne_u.components.examples.basic_cs.connectors.URIServiceConnector;
import fr.sorbonne_u.components.helpers.CVMDebugModes;

// -----------------------------------------------------------------------------
/**
 * The class <code>DistributedCVM</code> implements the multi-JVM assembly for
 * the basic client/server example.
 *
 * <p><strong>Description</strong></p>
 * 
 * An URI provider component defined by the class <code>URIProvider</code>
 * offers an URI creation service, which is used by an URI consumer component
 * defined by the class <code>URIConsumer</code>.
 * 
 * The URI provider is deployed within a JVM running an instance of the CVM
 * called <code>provider</code> in the <code>config.xml</code> file. The URI
 * consumer is deployed in the instance called <code>consumer</code>.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2014-01-22</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			DistributedCVM
extends		AbstractDistributedCVM
{
	protected static final String	PROVIDER_COMPONENT_URI = "my-URI-provider";
	protected static final String	CONSUMER_COMPONENT_URI = "my-URI-consumer";

	// URI of the CVM instances as defined in the config.xml file
	protected static String			PROVIDER_JVM_URI = "provider";
	protected static String			CONSUMER_JVM_URI = "consumer";

	protected static String			URIGetterOutboundPortURI = "oport";
	protected static String			URIProviderInboundPortURI = "iport";

	/** Reference to the provider component to share between deploy
	 *  and shutdown.														*/
	protected String	uriProviderURI;
	/** Reference to the consumer component to share between deploy
	 *  and shutdown.														*/
	protected String	uriConsumerURI;

	public				DistributedCVM(String[] args, int xLayout, int yLayout)
	throws Exception
	{
		super(args, xLayout, yLayout);
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
		if (thisJVMURI.equals(PROVIDER_JVM_URI)) {

			// create the provider component
			this.uriProviderURI =
					AbstractComponent.createComponent(
							URIProvider.class.getCanonicalName(),
							new Object[]{PROVIDER_COMPONENT_URI,
										 URIProviderInboundPortURI});
			assert	this.isDeployedComponent(this.uriProviderURI);
			// make it trace its operations; comment and uncomment the line to see
			// the difference
			this.toggleTracing(this.uriProviderURI);
			this.toggleLogging(this.uriProviderURI);
			assert	this.uriConsumerURI == null && this.uriProviderURI != null;

		} else if (thisJVMURI.equals(CONSUMER_JVM_URI)) {

			// create the consumer component
			this.uriConsumerURI =
					AbstractComponent.createComponent(
							URIConsumer.class.getCanonicalName(),
							new Object[]{CONSUMER_COMPONENT_URI,
										 URIGetterOutboundPortURI});
			assert	this.isDeployedComponent(this.uriConsumerURI);
			// make it trace its operations; comment and uncomment the line to see
			// the difference
			this.toggleTracing(this.uriConsumerURI);
			this.toggleLogging(this.uriConsumerURI);
			assert	this.uriConsumerURI != null && this.uriProviderURI == null;

		} else {

			System.out.println("Unknown JVM URI... " + thisJVMURI);

		}

		super.instantiateAndPublish();
	}

	/**
	 * interconnect the components.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.cvm.AbstractDistributedCVM#interconnect()
	 */
	@Override
	public void			interconnect() throws Exception
	{
		assert	this.isIntantiatedAndPublished();

		if (thisJVMURI.equals(PROVIDER_JVM_URI)) {

			assert	this.uriConsumerURI == null && this.uriProviderURI != null;

		} else if (thisJVMURI.equals(CONSUMER_JVM_URI)) {

			assert	this.uriConsumerURI != null && this.uriProviderURI == null;
			// do the connection
			this.doPortConnection(
				this.uriConsumerURI,
				URIGetterOutboundPortURI,
				URIProviderInboundPortURI,
				URIServiceConnector.class.getCanonicalName());

		} else {

			System.out.println("Unknown JVM URI... " + thisJVMURI);

		}

		super.interconnect();
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractDistributedCVM#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		// Port disconnections can be done here for static architectures
		// otherwise, they can be done in the finalise methods of components.

		if (thisJVMURI.equals(PROVIDER_JVM_URI)) {

			assert	this.uriConsumerURI == null && this.uriProviderURI != null;
			// nothing to be done on the provider side

		} else if (thisJVMURI.equals(CONSUMER_JVM_URI)) {

			assert	this.uriConsumerURI != null && this.uriProviderURI == null;
			this.doPortDisconnection(this.uriConsumerURI,
													URIGetterOutboundPortURI);

		} else {

			System.out.println("Unknown JVM URI... " + thisJVMURI);

		}

		super.finalise() ;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractDistributedCVM#shutdown()
	 */
	@Override
	public void			shutdown() throws Exception
	{
		if (thisJVMURI.equals(PROVIDER_JVM_URI)) {

			assert	this.uriConsumerURI == null && this.uriProviderURI != null;
			// any disconnection not done yet can be performed here

		} else if (thisJVMURI.equals(CONSUMER_JVM_URI)) {

			assert	this.uriConsumerURI != null && this.uriProviderURI == null;
			// any disconnection not done yet can be performed here

		} else {

			System.out.println("Unknown JVM URI... " + thisJVMURI);

		}

		super.shutdown();
	}

	public static void	main(String[] args)
	{
		try {
			DistributedCVM da  = new DistributedCVM(args, 2, 5);
			da.startStandardLifeCycle(15000L);
			Thread.sleep(10000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
// -----------------------------------------------------------------------------
