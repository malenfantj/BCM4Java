package fr.sorbonne_u.components.examples.ddeployment_cs.components;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.examples.basic_cs.components.URIProvider;
import fr.sorbonne_u.components.examples.basic_cs.connectors.URIServiceConnector;
import fr.sorbonne_u.components.examples.ddeployment_cs.connectors.URIConsumerLaunchConnector;
import fr.sorbonne_u.components.examples.ddeployment_cs.interfaces.URIConsumerLaunchI;
import fr.sorbonne_u.components.examples.ddeployment_cs.ports.URIConsumerLaunchOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.sorbonne_u.components.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.sorbonne_u.components.reflection.connectors.ReflectionConnector;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionI;
import fr.sorbonne_u.components.reflection.ports.ReflectionOutboundPort;

//-----------------------------------------------------------------------------
/**
 * The class <code>DynamicAssembler</code> implements a component that
 * creates the other components of the dynamic deployment example, makes
 * them interconnect, and starts them.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2014-03-14</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = {DynamicComponentCreationI.class,
							   URIConsumerLaunchI.class})
public class				DynamicAssembler
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
    // Constants and variables
	// -------------------------------------------------------------------------

	protected static final String PROVIDER_COMPONENT_URI = "my-URI-provider" ;
	protected static final String CONSUMER_COMPONENT_URI = "my-URI-consumer" ;
	protected static final String CONSUMER_LAUNCH_INBOUNDPORT_URI =
														  "consumer-launch" ;

	protected DynamicComponentCreationOutboundPort	portToConsumerJVM ;
	protected DynamicComponentCreationOutboundPort	portToProviderJVM ;

	protected String		consumerJVMURI ;
	protected String		providerJVMURI ;
	protected String		consumerOutboundPortURI ;
	protected String		providerInboundPortURI ;

	// -------------------------------------------------------------------------
    // Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the dynamic assembler component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param consumerJVMURI	 			URI of the JVM that will hold the consumer component or the empty string for single-JVM executions.
	 * @param providerJVMURI				URI of the JVM that will hold the provider component or the empty string for single-JVM executions.
	 * @param consumerOutboundPortURI	URI of the URI consumer outbound port 
	 * @param providerInboundPortURI		URI of the URI provider inbound port
	 * @throws Exception					<i>todo.</i>
	 */
	public				DynamicAssembler(
		String consumerJVMURI,
		String providerJVMURI,
		String consumerOutboundPortURI,
		String providerInboundPortURI
		) throws Exception
	{
		super(1, 0) ;
		this.consumerJVMURI = consumerJVMURI ;
		this.providerJVMURI = providerJVMURI ;
		this.consumerOutboundPortURI = consumerOutboundPortURI ;
		this.providerInboundPortURI = providerInboundPortURI ;
	}

	// -------------------------------------------------------------------------
    // Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public void			start() throws ComponentStartException
	{
		try {
			this.portToConsumerJVM =
				new DynamicComponentCreationOutboundPort(this) ;
			this.addPort(this.portToConsumerJVM) ;
			this.portToConsumerJVM.localPublishPort() ;
			this.portToConsumerJVM.doConnection(
				this.consumerJVMURI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				DynamicComponentCreationConnector.class.getCanonicalName()) ;

			this.portToProviderJVM =
				new DynamicComponentCreationOutboundPort(this) ;
			this.addPort(this.portToProviderJVM) ;
			this.portToProviderJVM.localPublishPort() ;
			this.portToProviderJVM.doConnection(
				this.providerJVMURI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				DynamicComponentCreationConnector.class.getCanonicalName()) ;
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}

		super.start() ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		if (this.portToConsumerJVM.connected()) {
			this.portToConsumerJVM.doDisconnection() ;
		}
		if (this.portToProviderJVM.connected()) {
			this.portToProviderJVM.doDisconnection() ;
		}

		super.finalise() ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
			this.portToConsumerJVM.unpublishPort() ;
			this.portToProviderJVM.unpublishPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}

		super.shutdown();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdownNow()
	 */
	@Override
	public void			shutdownNow() throws ComponentShutdownException
	{
		try {
			this.portToConsumerJVM.unpublishPort() ;
			this.portToProviderJVM.unpublishPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}

		super.shutdownNow() ;
	}

	// -------------------------------------------------------------------------
    // Component services
	// -------------------------------------------------------------------------

	/**
	 * launch the example by calling the <code>getURIandPrint</code> service
	 * on the URI consumer component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>todo.</i>
	 */
	public void			launch() throws Exception
	{
		URIConsumerLaunchOutboundPort p =
									new URIConsumerLaunchOutboundPort(this) ;
		this.addPort(p) ;
		p.publishPort() ;
		this.doPortConnection(
				p.getPortURI(),
				CONSUMER_LAUNCH_INBOUNDPORT_URI,
				URIConsumerLaunchConnector.class.getCanonicalName()) ;
		p.getURIandPrint() ;
		this.doPortDisconnection(p.getPortURI()) ;
		p.unpublishPort() ;
		p.destroyPort() ;
	}

	/**
	 * perform the creation and the connection of the components.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>todo.</i>
	 */
	public void			dynamicDeploy() throws Exception
	{
		// call the dynamic component creator of the provider JVM to create
		// the provider component
		this.portToProviderJVM.createComponent(
								URIProvider.class.getCanonicalName(),
								new Object[]{PROVIDER_COMPONENT_URI,
											 this.providerInboundPortURI}) ;
		// call the dynamic component creator of the consumer JVM to create
		// the provider component
		this.portToConsumerJVM.createComponent(
								DynamicURIConsumer.class.getCanonicalName(),
								new Object[]{CONSUMER_COMPONENT_URI,
											 this.consumerOutboundPortURI,
											 CONSUMER_LAUNCH_INBOUNDPORT_URI}) ;

		this.addRequiredInterface(ReflectionI.class) ;
		ReflectionOutboundPort rop = new ReflectionOutboundPort(this) ;
		this.addPort(rop) ;
		rop.localPublishPort() ;

		// connect to the consumer (client) component
		rop.doConnection(DynamicAssembler.CONSUMER_COMPONENT_URI,
						 ReflectionConnector.class.getCanonicalName()) ;
		// toggle logging on the consumer component
		rop.toggleTracing() ;
		// connect the consumer outbound port top the provider inbound one.
		rop.doPortConnection(this.consumerOutboundPortURI,
							 this.providerInboundPortURI,
							 URIServiceConnector.class.getCanonicalName()) ;
		rop.doDisconnection() ;

		// connect to the provider (server) component
		rop.doConnection(PROVIDER_COMPONENT_URI,
						 ReflectionConnector.class.getCanonicalName()) ;
		// toggle logging on the providerer component
		rop.toggleTracing() ;
		rop.doDisconnection() ;
		rop.unpublishPort() ;
		this.removeRequiredInterface(ReflectionI.class) ;
		rop.destroyPort() ;
	}
}
//-----------------------------------------------------------------------------
