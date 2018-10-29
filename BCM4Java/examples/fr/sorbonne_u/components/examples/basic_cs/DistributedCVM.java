package fr.sorbonne_u.components.examples.basic_cs;

//Copyright Jacques Malenfant, Sorbonne Universite.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.examples.basic_cs.components.URIConsumer;
import fr.sorbonne_u.components.examples.basic_cs.components.URIProvider;
import fr.sorbonne_u.components.examples.basic_cs.connectors.URIServiceConnector;

//-----------------------------------------------------------------------------
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
public class				DistributedCVM
extends		AbstractDistributedCVM
{
	protected static final String	PROVIDER_COMPONENT_URI = "my-URI-provider" ;
	protected static final String	CONSUMER_COMPONENT_URI = "my-URI-consumer" ;

	// URI of the CVM instances as defined in the config.xml file
	protected static String			PROVIDER_JVM_URI = "provider" ;
	protected static String			CONSUMER_JVM_URI = "consumer" ;

	protected static String			URIConsumerOutboundPortURI = "oport" ;
	protected static String			URIProviderInboundPortURI = "iport" ;

	protected URIProvider	uriProvider ;
	protected URIConsumer	uriConsumer ;

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
		// debugging mode configuration; comment and uncomment the line to see
		// the difference
//		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.PUBLIHSING) ;
//		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CONNECTING) ;
//		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.COMPONENT_DEPLOYMENT) ;

		super.initialise() ;
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
			this.uriProvider =
				new URIProvider(PROVIDER_COMPONENT_URI,
								URIProviderInboundPortURI) ;
			// make it trace its operations; comment and uncomment the line to see
			// the difference
			uriProvider.toggleTracing() ;
			uriProvider.toggleLogging() ;
			// add it to the deployed components
			this.addDeployedComponent(uriProvider) ;
			assert	this.uriConsumer == null && this.uriProvider != null ;

		} else if (thisJVMURI.equals(CONSUMER_JVM_URI)) {

			// create the consumer component
			this.uriConsumer = new URIConsumer(CONSUMER_COMPONENT_URI,
											   URIConsumerOutboundPortURI) ;
			// make it trace its operations; comment and uncomment the line to see
			// the difference
			uriConsumer.toggleTracing() ;
			uriConsumer.toggleLogging() ;
			// add it to the deployed components
			this.addDeployedComponent(uriConsumer) ;
			assert	this.uriConsumer != null && this.uriProvider == null ;

		} else {

			System.out.println("Unknown JVM URI... " + thisJVMURI) ;

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
		assert	this.isIntantiatedAndPublished() ;

		if (thisJVMURI.equals(PROVIDER_JVM_URI)) {

			assert	this.uriConsumer == null && this.uriProvider != null ;

		} else if (thisJVMURI.equals(CONSUMER_JVM_URI)) {

			assert	this.uriConsumer != null && this.uriProvider == null ;
			// do the connection
			this.uriConsumer.doPortConnection(
				URIConsumerOutboundPortURI,
				URIProviderInboundPortURI,
				URIServiceConnector.class.getCanonicalName()) ;
			assert	this.uriConsumer.isPortConnected(
												URIConsumerOutboundPortURI) ;

		} else {

			System.out.println("Unknown JVM URI... " + thisJVMURI) ;

		}

		super.interconnect();
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractDistributedCVM#shutdown()
	 */
	@Override
	public void			shutdown() throws Exception
	{
		if (thisJVMURI.equals(PROVIDER_JVM_URI)) {

			assert	this.uriConsumer == null && this.uriProvider != null ;
			// print logs on files, if activated
			this.uriProvider.printExecutionLogOnFile("provider") ;

			// any disconnection not done yet can be performed here

		} else if (thisJVMURI.equals(CONSUMER_JVM_URI)) {

			assert	this.uriConsumer != null && this.uriProvider == null ;
			// print logs on files, if activated
			this.uriConsumer.printExecutionLogOnFile("consumer") ;

			// any disconnection not done yet can be performed here

		} else {

			System.out.println("Unknown JVM URI... " + thisJVMURI) ;

		}

		super.shutdown();
	}

	public static void	main(String[] args)
	{
		try {
			DistributedCVM da  = new DistributedCVM(args, 2, 5) ;
			da.startStandardLifeCycle(15000L) ;
			Thread.sleep(10000L) ;
			System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
//-----------------------------------------------------------------------------
