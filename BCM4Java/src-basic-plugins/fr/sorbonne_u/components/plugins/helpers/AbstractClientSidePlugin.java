package fr.sorbonne_u.components.plugins.helpers;

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

import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.components.ports.OutboundPortI;
import fr.sorbonne_u.components.reflection.connectors.ReflectionConnector;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;
import fr.sorbonne_u.components.reflection.ports.ReflectionOutboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractClientSidePlugin</code> provides the functionalities
 * required to connect a client component to a server component for given
 * required and offered interfaces.
 *
 * <p><strong>Description</strong></p>
 * 
 * See {@link ClientSidePluginI ClientSidePluginI}.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 2019-03-06</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractClientSidePlugin
extends		AbstractPlugin
implements	ClientSidePluginI
{
	private static final long serialVersionUID = 1L;

	protected Class<? extends RequiredCI>	requiredInterface;
	protected AbstractOutboundPort			pluginOutboundPort;
	
	// -------------------------------------------------------------------------
	// Plug-in generic methods
	// -------------------------------------------------------------------------

	/**
	 * add the interface returned by @see{getRequiredInterface()} to the
	 * required interfaces of the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code !owner.isInstalled(getPluginURI())}
	 * post	true		// no postcondition.
	 * </pre>
	 * 
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

		this.requiredInterface = this.getRequiredInterface();
		this.addRequiredInterface(this.requiredInterface);
	}

	/**
	 * add the port returned by @see{createOutboundPort()} to the ports
	 * of the owner component and publish it.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.AbstractPlugin#initialise()
	 */
	@Override
	public void			initialise() throws Exception
	{
		this.pluginOutboundPort =
					(AbstractOutboundPort) this.createOutboundPort();
		this.pluginOutboundPort.publishPort();
	}

	/**
	 * disconnect the outbound port if not yet disconnected,
	 * unpublish it and destroy it.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.AbstractPlugin#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		if (this.pluginOutboundPort != null) {
			if (this.pluginOutboundPort.connected()) {
				this.getOwner().doPortDisconnection(
						this.pluginOutboundPort.getPortURI());
			}
			this.pluginOutboundPort.unpublishPort();
			this.pluginOutboundPort.destroyPort();
			this.pluginOutboundPort = null;
		}
	}

	/**
	 * remove the required interface from the required interfaces of the
	 * owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.AbstractPlugin#uninstall()
	 */
	@Override
	public void			uninstall() throws Exception
	{
		this.removeRequiredInterface(this.requiredInterface);
		this.requiredInterface = null;
	}

	// -------------------------------------------------------------------------
	// Plug-in specific methods
	// -------------------------------------------------------------------------

	/**
	 * return the interface required by the client side.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	the interface required by the client side.
	 */
	protected abstract Class<? extends RequiredCI>	getRequiredInterface();

	/**
	 * return the interface required by the client-side.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	the interface required by the client-side.
	 */
	protected abstract Class<? extends OfferedCI>	getOfferedInterface();

	/**
	 * return the outbound port instance that must be used to connect with
	 * the server side.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return				the outbound port instance that must be used to connect with the server side.
	 * @throws Exception	<i>to do</i>.
	 */
	protected abstract OutboundPortI	createOutboundPort() throws Exception;

	/**
	 * return the name of the connector class to be used to connect with the
	 * server side.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return	the name of the connector class to be used to connect with the server side.
	 */
	protected abstract String	getConnectorClassName();

	/**
	 * @see fr.sorbonne_u.components.plugins.helpers.ClientSidePluginI#isConnectedToServerSide()
	 */
	@Override
	public boolean		isConnectedToServerSide() throws Exception
	{
		return this.pluginOutboundPort != null &&
									this.pluginOutboundPort.connected();
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.helpers.ClientSidePluginI#connectWithServerSide(java.lang.String)
	 */
	@Override
	public boolean		connectWithServerSide(
		String serverReflectionInboundPortURI
		) throws Exception
	{
		assert	serverReflectionInboundPortURI != null;

		boolean ret = true;

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

		String[] otherInboundPortURI =
				rop.findInboundPortURIsFromInterface(
											this.getOfferedInterface());

		if (otherInboundPortURI == null || otherInboundPortURI.length == 0) {
			ret = false;
		} else {
			this.getOwner().doPortConnection(
					this.pluginOutboundPort.getPortURI(),
					otherInboundPortURI[0],
					this.getConnectorClassName());
		}

		this.getOwner().doPortDisconnection(rop.getPortURI());
		rop.unpublishPort();
		rop.destroyPort();

		if (!wasRequiringReflectionI) {
			this.removeRequiredInterface(ReflectionCI.class);
		}

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.helpers.ClientSidePluginI#disconnectFromServerSide()
	 */
	@Override
	public void			disconnectFromServerSide() throws Exception
	{
		assert	this.isConnectedToServerSide();

		this.getOwner().doPortDisconnection(
										this.pluginOutboundPort.getPortURI());
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.helpers.ClientSidePluginI#getOutboundPort()
	 */
	@Override
	public AbstractOutboundPort	getOutboundPort()
	{
		return this.pluginOutboundPort;
	}
}
// -----------------------------------------------------------------------------
