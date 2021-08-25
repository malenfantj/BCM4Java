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
import fr.sorbonne_u.components.examples.basic_cs.connectors.URIServiceConnector;
import fr.sorbonne_u.components.examples.basic_cs.interfaces.URIConsumerCI;
import fr.sorbonne_u.components.examples.basic_cs.interfaces.URIProviderCI;
import fr.sorbonne_u.components.examples.ddeployment_cs.connectors.URIConsumerLaunchConnector;
import fr.sorbonne_u.components.examples.ddeployment_cs.interfaces.URIConsumerLaunchCI;
import fr.sorbonne_u.components.examples.ddeployment_cs.ports.URIConsumerLaunchOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationCI;
import fr.sorbonne_u.components.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.sorbonne_u.components.reflection.connectors.ReflectionConnector;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;
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
@RequiredInterfaces(required = {DynamicComponentCreationCI.class,
							   URIConsumerLaunchCI.class})
public class			DynamicAssembler
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
    // Constants and variables
	// -------------------------------------------------------------------------

	protected static final String PROVIDED_URI_PREFIX = "generated-URI-" ;

	protected DynamicComponentCreationOutboundPort	portToConsumerJVM ;
	protected DynamicComponentCreationOutboundPort	portToProviderJVM ;

	protected String		consumerJVMURI ;
	protected String		providerJVMURI ;
	protected String		consumerOutboundPortURI ;
	protected String		providerInboundPortURI ;
	protected String		consumerLaunchInboundPortURI ;

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
	 * @throws Exception					<i>todo.</i>
	 */
	protected			DynamicAssembler(
		String consumerJVMURI,
		String providerJVMURI
		) throws Exception
	{
		super(1, 0) ;
		this.consumerJVMURI = consumerJVMURI ;
		this.providerJVMURI = providerJVMURI ;
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
		super.start() ;

		try {
			this.portToConsumerJVM =
				new DynamicComponentCreationOutboundPort(this) ;
			this.portToConsumerJVM.localPublishPort() ;
			this.doPortConnection(
				this.portToConsumerJVM.getPortURI(),
				this.consumerJVMURI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				DynamicComponentCreationConnector.class.getCanonicalName()) ;

			this.portToProviderJVM =
				new DynamicComponentCreationOutboundPort(this) ;
			this.portToProviderJVM.localPublishPort() ;
			this.doPortConnection(
				this.portToProviderJVM.getPortURI(),
				this.providerJVMURI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				DynamicComponentCreationConnector.class.getCanonicalName()) ;

			AbstractComponent.checkImplementationInvariant(this);
			AbstractComponent.checkInvariant(this);

			this.runTask(
					new AbstractComponent.AbstractTask() {
							@Override
							public void run() {
								try {
									((DynamicAssembler)this.getTaskOwner()).
															dynamicDeploy() ;
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}) ;

		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		if (this.portToConsumerJVM.connected()) {
			this.doPortDisconnection(this.portToConsumerJVM.getPortURI()) ;
		}
		if (this.portToProviderJVM.connected()) {
			this.doPortDisconnection(this.portToProviderJVM.getPortURI()) ;
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
		p.publishPort() ;
		this.doPortConnection(
				p.getPortURI(),
				this.consumerLaunchInboundPortURI,
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
		assert	this.portToConsumerJVM != null ;
		assert	this.portToConsumerJVM.connected() ;
		assert	this.portToProviderJVM != null ;
		assert	this.portToProviderJVM.connected() ;

		// call the dynamic component creator of the provider JVM to create
		// the provider component
		String providerRIPURI =
			this.portToProviderJVM.createComponent(
								DynamicURIProvider.class.getCanonicalName(),
								new Object[]{PROVIDED_URI_PREFIX}) ;
		this.portToProviderJVM.startComponent(providerRIPURI) ;

		// call the dynamic component creator of the consumer JVM to create
		// the provider component
		String consumerRIPURI =
			this.portToConsumerJVM.createComponent(
								DynamicURIConsumer.class.getCanonicalName(),
								new Object[]{}) ;
		this.portToConsumerJVM.startComponent(consumerRIPURI) ;

		this.addRequiredInterface(ReflectionCI.class) ;
		ReflectionOutboundPort rop = new ReflectionOutboundPort(this) ;
		rop.localPublishPort() ;

		// connect to the provider (server) component
		rop.doConnection(providerRIPURI,
						 ReflectionConnector.class.getCanonicalName()) ;
		// toggle logging on the provider component
		rop.toggleTracing() ;
		// get the URI of the URI provider inbound port of the provider
		// component.
		String[] uris =
			rop.findInboundPortURIsFromInterface(URIProviderCI.class) ;
		assert	uris != null && uris.length == 1 ;
		this.providerInboundPortURI = uris[0] ;
		this.doPortDisconnection(rop.getPortURI()) ;

		// connect to the consumer (client) component
		rop.doConnection(consumerRIPURI,
						 ReflectionConnector.class.getCanonicalName()) ;
		// toggle logging on the consumer component
		rop.toggleTracing() ;
		// get the URI of the launch inbound port of the consumer component.
		uris = rop.findInboundPortURIsFromInterface(URIConsumerLaunchCI.class) ;
		assert	uris != null && uris.length == 1 ;
		this.consumerLaunchInboundPortURI = uris[0] ;
		// get the URI of the URI consumer outbound port of the consumer
		// component.
		uris = rop.findOutboundPortURIsFromInterface(URIConsumerCI.class) ;
		assert	uris != null && uris.length == 1 ;
		this.consumerOutboundPortURI = uris[0] ;
		// connect the consumer outbound port top the provider inbound one.
		rop.doPortConnection(this.consumerOutboundPortURI,
							 this.providerInboundPortURI,
							 URIServiceConnector.class.getCanonicalName()) ;
		this.doPortDisconnection(rop.getPortURI()) ;
		rop.unpublishPort() ;

		this.runTask(
				new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								((DynamicAssembler)this.getTaskOwner()).launch() ;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}) ;
	}
}
//-----------------------------------------------------------------------------
