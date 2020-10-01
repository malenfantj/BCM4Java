package fr.sorbonne_u.components.plugins.dconnection;

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

import java.util.HashMap;
import java.util.Map;
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.plugins.dconnection.connectors.DynamicConnectionRequestConnector;
import fr.sorbonne_u.components.plugins.dconnection.interfaces.DynamicConnectionDescriptorI;
import fr.sorbonne_u.components.plugins.dconnection.interfaces.DynamicConnectionRequestCI;
import fr.sorbonne_u.components.plugins.dconnection.ports.DynamicConnectionRequestOutboundPort;
import fr.sorbonne_u.components.ports.OutboundPortI;
import fr.sorbonne_u.components.reflection.connectors.ReflectionConnector;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;
import fr.sorbonne_u.components.reflection.ports.ReflectionOutboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>DynamicConnectionClientSidePlugin</code> implements the
 * client side behaviour of a component dynamic connection pattern.
 * See the package documentation for a complete description of the pattern
 * and its implementation.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The class implements the required behaviours for the client side i.e.,
 * the component requiring some interface through which the dynamic connection
 * need to be done. The class <code>DynamicConnectionServerSidePlugin</code>
 * implements the behaviours for the server side.
 * </p>
 * <p>
 * To use this plug-in, the user must create a class that implements the
 * interface <code>DynamicConnectionDescriptorI</code>, which defines the
 * type of outbound port to be created and the connector class name for the
 * connection itself. A component can participate in dynamic connections
 * for several different offered interfaces, and therefore this class may
 * have to be able to create different types of outbound ports using
 * different types of connectors.
 * </p>
 * <p>
 * When two components want to use the dynamic connection plug-in, the two
 * components must first be connected through the <code>ReflectionI</code>
 * interface and then install their respective plug-ins (client side and
 * server side) and connect them through the client side plug-in.
 * </p>
 * <p>
 * To perform the dynamic connection, the client side component calls its plug-in
 * method <code>doDynamicConnection</code>. If the two components are not
 * connected through the plug-in before calling <code>doDynamicConnection</code>
 * they will be. On the other hand, they are not disconnected after to make
 * more efficient the dynamic connections when several ones must be done in
 * a row between the two components. The client side component can either
 * do the disconnection explicitly by calling
 * <code>disconnectFromServerSide</code> otherwise the finalisation of the
 * plug-in will do it.
 * </p>
 * <p>
 * This plug-in is a singleton one, so it can only be installed once at a
 * time on a component.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true		// TODO
 * </pre>
 * 
 * <p>Created on : 2017-02-15</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class 			DynamicConnectionClientSidePlugin
extends		AbstractPlugin
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Plug-in inner classes
	// -------------------------------------------------------------------------

	private static class	ConnectionDescriptor
	{
		public final OutboundPortI				outPort;
		public final Class<? extends OfferedCI>	offeredInterface;
		public final String						inboundPortURI;

		public				ConnectionDescriptor(
			OutboundPortI outPort,
			Class<? extends OfferedCI> offeredInterface,
			String inboundPortURI
			)
		{
			super();
			this.outPort = outPort;
			this.offeredInterface = offeredInterface;
			this.inboundPortURI = inboundPortURI;
		}
	}

	// -------------------------------------------------------------------------
	// Plug-in internal constants and variables
	// -------------------------------------------------------------------------

	/** URI of the plug-in used in the plug-in call protocol.				*/
	public static final String PLUGIN_URI = "DCONNECTION_CLIENT_SIDE_PLUGIN";

	/** Port through which dynamic connection requests are issued.			*/
	protected DynamicConnectionRequestOutboundPort	dcrop;
	/** The ports used in the dynamic connections.							*/
	protected Map<Class<? extends RequiredCI>,ConnectionDescriptor>
													dynamicOutboundPorts;

	// -------------------------------------------------------------------------
	// Plug-in generic methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractPlugin#installOn(fr.sorbonne_u.components.ComponentI)
	 */
	@Override
	public void			installOn(ComponentI owner) throws Exception
	{
		// A plug-in is installed on an existing component.
		assert	owner != null;
		// Only one plug-in instance of a given URI can be installed on
		// a component.
		assert	!owner.isInstalled(this.getPluginURI());

		super.installOn(owner);

		// At installation time on a component, the plug-in adds the plug-in
		// required interfaces to the ones of the component.
		this.addRequiredInterface(DynamicConnectionRequestCI.class);
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#initialise()
	 */
	@Override
	public void			initialise() throws Exception
	{
		this.dynamicOutboundPorts = new HashMap<>();
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		for (ConnectionDescriptor d : this.dynamicOutboundPorts.values()) {
			this.getOwner().doPortDisconnection(d.outPort.getPortURI());
			d.outPort.unpublishPort();
			d.outPort.destroyPort();
		}
		this.dynamicOutboundPorts.clear();
		if (this.isConnectedToServerSide()) {
			this.disconnectFromServerSide();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPlugin#uninstall()
	 */
	@Override
	public void			uninstall() throws Exception
	{
		// When uninstalling the plug-in, the ports and the interfaces added
		// to the component at installation time are removed.
		this.removeRequiredInterface(DynamicConnectionRequestCI.class);
	}

	// -------------------------------------------------------------------------
	// Plug-in specific methods
	// -------------------------------------------------------------------------

	/**
	 * true if the plug-in is connected to a server side plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if the plug-in is connected to a server side plug-in.
	 * @throws Exception	<i>to do.</i>
	 */
	public boolean		isConnectedToServerSide() throws Exception
	{
		return this.dcrop != null && this.dcrop.connected() ;
	}

	/**
	 * return true if a connection already exists on the given required
	 * interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code requiredInterface != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param requiredInterface	client-side interface through which the connection is made.
	 * @return					true if a connection already exists on the given required interface.
	 */
	public boolean		isDynamicallyConnectedThrough(
		Class<?> requiredInterface
		)
	{
		assert	requiredInterface != null;
		return this.dynamicOutboundPorts.containsKey(requiredInterface);
	}

	/**
	 * connect to the dynamic connection request port of the server side.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isConnectedToServerSide()}
	 * pre	{@code serverReflectionInboundPortURI != null}
	 * post	{@code isConnectedToServerSide()}
	 * </pre>
	 *
	 * @param serverReflectionInboundPortURI	URI of the reflection inbound port of the server-side component.
	 * @throws	Exception						<i>to do</i>.
	 */
	public void			connectWithServerSide(
		String serverReflectionInboundPortURI
		) throws Exception
	{
		assert	!this.isConnectedToServerSide();

		boolean wasRequiringReflectionI = true;
		if (!this.getOwner().isRequiredInterface(ReflectionCI.class)) {
			this.addRequiredInterface(ReflectionCI.class);
			wasRequiringReflectionI = false;
		}

		ReflectionOutboundPort rop =
							new ReflectionOutboundPort(this.getOwner());
		rop.publishPort();
		this.getOwner().doPortConnection(
				rop.getPortURI(),
				serverReflectionInboundPortURI,
				ReflectionConnector.class.getCanonicalName());

		// Connect to the other component using its dynamic connection request
		// inbound port.
		this.dcrop = new DynamicConnectionRequestOutboundPort(this.getOwner());
		this.dcrop.publishPort();

		String[] otherInboundPortURI =
				rop.findInboundPortURIsFromInterface(
									DynamicConnectionRequestCI.class);

		this.getOwner().doPortConnection(
				this.dcrop.getPortURI(),
				otherInboundPortURI[0],
				DynamicConnectionRequestConnector.class.getCanonicalName());

		this.getOwner().doPortDisconnection(rop.getPortURI());
		rop.unpublishPort();
		rop.destroyPort();
		if (!wasRequiringReflectionI) {
			this.removeRequiredInterface(ReflectionCI.class);
		}

		assert	this.isConnectedToServerSide();
	}

	/**
	 * connect dynamically : (1) request the URI of a server dynamic port,
	 * (2) create this client own port, (3) connect the client to the server.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code requiredInterface != null && !isDynamicallyConnectedThrough(requiredInterface)}
	 * pre	{@code offeredInterface != null}
	 * pre	{@code connectionDescriptor != null}
	 * pre	{@code isConnectedToServerSide()}
	 * post	{@code isDynamicallyConnectedThrough(requiredInterface)}
	 * </pre>
	 *
	 * @param requiredInterface		client-side interface through which the connection is made.
	 * @param offeredInterface		server-side interface through which the connection is made.
	 * @param connectionDescriptor	describes how to create the outbound port and the connector.
	 * @return						the URI of the outbound port of the connection.
	 * @throws Exception			<i>to do</i>.
	 */
	public OutboundPortI		doDynamicConnection(
		Class<? extends RequiredCI> requiredInterface,
		Class<? extends OfferedCI> offeredInterface,
		DynamicConnectionDescriptorI connectionDescriptor
		) throws Exception
	{
		assert	requiredInterface != null &&
					!this.isDynamicallyConnectedThrough(requiredInterface);
		assert	offeredInterface != null && connectionDescriptor != null;
		assert	this.isConnectedToServerSide();

		String otherDynamicPortURI =
					this.dcrop.requestDynamicPortURI(offeredInterface);
		OutboundPortI dynamicOutboundPort =
			connectionDescriptor.createClientSideDynamicPort(
													requiredInterface,
													this.getOwner());
		dynamicOutboundPort.publishPort();
		this.dynamicOutboundPorts.put(
				requiredInterface,
				new ConnectionDescriptor(dynamicOutboundPort,
										 offeredInterface,
										 otherDynamicPortURI));
		this.getOwner().doPortConnection(
				dynamicOutboundPort.getPortURI(),
				otherDynamicPortURI,
				connectionDescriptor.dynamicConnectorClassName(
													requiredInterface));

		assert	this.isConnectedToServerSide();
		assert	this.isDynamicallyConnectedThrough(requiredInterface);

		return dynamicOutboundPort;
	}

	/**
	 * disconnect a previous dynamic connection on the given required
	 * interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code requiredInterface != null && isDynamicallyConnectedThrough(requiredInterface)}
	 * post	{@code !isDynamicallyConnectedThrough(requiredInterface)}
	 * </pre>
	 *
	 * @param requiredInterface	client-side interface through which the connection is made.
	 * @throws Exception		<i>to do</i>.
	 */
	public void			doDynamicDisconnection(
		Class<? extends RequiredCI> requiredInterface
		) throws Exception
	{
		assert	requiredInterface != null &&
					this.isDynamicallyConnectedThrough(requiredInterface);

		ConnectionDescriptor d =
						this.dynamicOutboundPorts.get(requiredInterface);
		if (d != null) {
			this.getOwner().doPortDisconnection(d.outPort.getPortURI());
			this.dynamicOutboundPorts.remove(requiredInterface);
			d.outPort.unpublishPort();
			d.outPort.destroyPort();
			this.dcrop.removeDynamicPort(d.offeredInterface, d.inboundPortURI);
		}

		assert	!this.isDynamicallyConnectedThrough(requiredInterface) ;
	}

	/**
	 * connect from the dynamic connection request port of the server side.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isConnectedToServerSide()}
	 * post	{@code !isConnectedToServerSide()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			disconnectFromServerSide() throws Exception
	{
		assert	this.isConnectedToServerSide();

		this.getOwner().doPortDisconnection(this.dcrop.getPortURI());
		this.dcrop.unpublishPort();
		this.dcrop.destroyPort();

		assert	!this.isConnectedToServerSide();
	}
}
// -----------------------------------------------------------------------------
