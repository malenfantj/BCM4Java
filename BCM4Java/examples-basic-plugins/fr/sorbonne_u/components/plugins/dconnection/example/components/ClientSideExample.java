package fr.sorbonne_u.components.plugins.dconnection.example.components;

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
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.annotations.AddPlugin;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.plugins.dconnection.DynamicConnectionClientSidePlugin;
import fr.sorbonne_u.components.plugins.dconnection.example.connectors.ExampleConnector;
import fr.sorbonne_u.components.plugins.dconnection.example.interfaces.ExampleCI;
import fr.sorbonne_u.components.plugins.dconnection.example.ports.ExampleOutboundPort;
import fr.sorbonne_u.components.plugins.dconnection.interfaces.DynamicConnectionDescriptorI;
import fr.sorbonne_u.components.ports.OutboundPortI;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;

// -----------------------------------------------------------------------------
/**
 * The class <code>ClientSideExample</code> shows how a client component can
 * use the dynamic connection plug-in to connect itself with a server
 * component and call its services.
 *
 * <p><strong>Description</strong></p>
 * 
 * To benefit from the example, carefully read the code and adapt it to your
 * needs in your own code.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2017-02-17</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// -----------------------------------------------------------------------------
@RequiredInterfaces(required = {ReflectionCI.class, ExampleCI.class})
@AddPlugin(pluginClass = DynamicConnectionClientSidePlugin.class,
		   pluginURI = ClientSideExample.DYNAMIC_CONNECTION_PLUGIN_URI)
// -----------------------------------------------------------------------------
public class			ClientSideExample
extends		AbstractComponent
// -----------------------------------------------------------------------------
{
	public final static String	DYNAMIC_CONNECTION_PLUGIN_URI =
														"clientSidePluginURI" ;

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the reflection inbound port of the server component.			*/
	protected String					serverSideReflectionInboundPortURI ;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	/**
	 * create the client side component with required interface
	 * <code>ExampleI</code> and with the plug-in for dynamic connection
	 * between components installed.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	serverSideReflectionInboundPortURI != null
	 * post	isInstalled(DynamicConnectionClientSidePlugin.PLUGIN_URI)
	 * </pre>
	 *
	 * @param serverSideReflectionInboundPortURI	URI of the reflection inbound port of the server component
	 * @throws Exception							<i>to do.</i>
	 */
	protected			ClientSideExample(
		String serverSideReflectionInboundPortURI
		) throws Exception
	{
		super(1, 0) ;

		assert	serverSideReflectionInboundPortURI != null ;

		this.serverSideReflectionInboundPortURI =
									serverSideReflectionInboundPortURI ;
		this.toggleTracing() ;
	}

	// -------------------------------------------------------------------------
	// Life-cycle methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		super.execute() ;

		// Connecting the dynamic connection plug-ins
		DynamicConnectionClientSidePlugin dconnectionPlugIn =
				(DynamicConnectionClientSidePlugin)
						this.getPlugin(ClientSideExample.
											DYNAMIC_CONNECTION_PLUGIN_URI) ;
		dconnectionPlugIn.connectWithServerSide(
									this.serverSideReflectionInboundPortURI) ;

		// Use the dynamic connection facilities to connect the example
		// ports.
		ExampleOutboundPort top =
			(ExampleOutboundPort)
				dconnectionPlugIn.doDynamicConnection(
					ExampleCI.class,
					ExampleCI.class,
					new DynamicConnectionDescriptorI() {
						@Override
						public OutboundPortI	 createClientSideDynamicPort(
								Class<?> requiredInterface,
								ComponentI owner) {
							try {
								assert	requiredInterface.equals(ExampleCI.class) ;
								return new ExampleOutboundPort(owner) ;
							} catch (Exception e) {
								throw new RuntimeException(e) ;
							}
						}

						@Override
						public String dynamicConnectorClassName(
							Class<?> requiredInterface
							)
						{
							assert	requiredInterface.equals(ExampleCI.class) ;
							return ExampleConnector.class.getCanonicalName() ;
						}
					}) ;

		this.traceMessage("client about to call the server...\n") ;
		int res = top.exampleCall(10) ;
		this.traceMessage("...result = " + res + "\n") ;
		dconnectionPlugIn.doDynamicDisconnection(ExampleCI.class) ;
		dconnectionPlugIn.disconnectFromServerSide() ;
	}
}
// -----------------------------------------------------------------------------
