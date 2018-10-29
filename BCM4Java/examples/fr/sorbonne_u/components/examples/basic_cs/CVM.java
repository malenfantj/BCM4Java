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

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.examples.basic_cs.components.URIConsumer;
import fr.sorbonne_u.components.examples.basic_cs.components.URIProvider;
import fr.sorbonne_u.components.examples.basic_cs.connectors.URIServiceConnector;

//-----------------------------------------------------------------------------
/**
 * The class <code>CVM</code> implements the single JVM assembly for the basic
 * client/server example.
 *
 * <p><strong>Description</strong></p>
 * 
 * An URI provider component defined by the class <code>URIProvider</code>
 * offers an URI creation service, which is used by an URI consumer component
 * defined by the class <code>URIConsumer</code>. Both are deployed within a
 * single JVM.
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
public class				CVM
extends		AbstractCVM
{
	/** URI of the provider component (convenience).						*/
	protected static final String	PROVIDER_COMPONENT_URI = "my-URI-provider" ;
	/** URI of the consumer component (convenience).						*/
	protected static final String	CONSUMER_COMPONENT_URI = "my-URI-consumer" ;
	/** URI of the provider outbound port (simplifies the connection).	*/
	protected static final String	URIGetterOutboundPortURI = "oport" ;
	/** URI of the consumer inbound port (simplifies the connection).		*/
	protected static final String	URIProviderInboundPortURI = "iport" ;

	public				CVM() throws Exception
	{
		super() ;
	}

	/** Reference to the provider component to share between deploy
	 *  and shutdown.													*/
	protected URIProvider	uriProvider ;
	/** Reference to the consumer component to share between deploy
	 *  and shutdown.													*/
	protected URIConsumer	uriConsumer ;

	/**
	 * instantiate the components, publish their port and interconnect them.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!this.deploymentDone()
	 * post	this.deploymentDone()
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#deploy()
	 */
	@Override
	public void			deploy() throws Exception
	{
		assert	!this.deploymentDone() ;

		// --------------------------------------------------------------------
		// Configuration phase
		// --------------------------------------------------------------------

		// debugging mode configuration; comment and uncomment the line to see
		// the difference
//		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.PUBLIHSING) ;
//		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CONNECTING) ;
//		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.COMPONENT_DEPLOYMENT) ;

		// --------------------------------------------------------------------
		// Creation phase
		// --------------------------------------------------------------------

		// create the provider component
		this.uriProvider = new URIProvider(PROVIDER_COMPONENT_URI,
										   URIProviderInboundPortURI) ;
		// make it trace its operations; comment and uncomment the line to see
		// the difference
		this.uriProvider.toggleTracing() ;
		this.uriProvider.toggleLogging() ;
		// add it to the deployed components
		this.deployedComponents.add(uriProvider) ;

		// create the consumer component
		this.uriConsumer = new URIConsumer(CONSUMER_COMPONENT_URI,
									  	   URIGetterOutboundPortURI) ;
		// make it trace its operations; comment and uncomment the line to see
		// the difference
		this.uriConsumer.toggleTracing() ;
		this.uriConsumer.toggleLogging() ;
		// add it to the deployed components
		this.deployedComponents.add(uriConsumer) ;
		
		// --------------------------------------------------------------------
		// Connection phase
		// --------------------------------------------------------------------

		// do the connection
		this.uriConsumer.doPortConnection(
				URIGetterOutboundPortURI,
				URIProviderInboundPortURI,
				URIServiceConnector.class.getCanonicalName()) ;
		
		// --------------------------------------------------------------------
		// Deployment done
		// --------------------------------------------------------------------

		super.deploy();
		assert	this.deploymentDone() ;
	}

	/**
	 * disconnect the components and then call the base shutdown method.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#shutdown()
	 */
	@Override
	public void				shutdown() throws Exception
	{
		assert	this.allFinalised() ;
		// any disconnection not done yet can be performed here

		// print logs on files, if activated
		this.uriConsumer.printExecutionLogOnFile("consumer") ;
		this.uriProvider.printExecutionLogOnFile("provider") ;

		super.shutdown();
	}

	public static void		main(String[] args)
	{
		try {
			// Create an instance of the defined component virtual machine.
			CVM a = new CVM() ;
			// Execute the application.
			a.startStandardLifeCycle(15000L) ;
			// Give some time to see the traces (convenience).
			Thread.sleep(10000L) ;
			// Simplifies the termination (termination has yet to be treated
			// properly in BCM).
			System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
//-----------------------------------------------------------------------------
