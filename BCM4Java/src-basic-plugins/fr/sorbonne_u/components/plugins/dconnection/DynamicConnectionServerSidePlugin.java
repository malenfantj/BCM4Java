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
import fr.sorbonne_u.components.plugins.dconnection.interfaces.DynamicConnectionRequestCI;
import fr.sorbonne_u.components.plugins.dconnection.ports.DynamicConnectionRequestInboundPort;
import fr.sorbonne_u.components.ports.InboundPortI;

// -----------------------------------------------------------------------------
/**
 * The class <code>DynamicConnectionServerSidePlugin</code> implements the
 * server side behaviour of a component dynamic interconnection pattern.
 * See the package documentation for a complete description of the pattern
 * and its implementation.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The class implements the required behaviours for the server side i.e.,
 * the component offering some interface through which the dynamic connection
 * needs to be done. The present class implements the behaviours for the
 * client side.
 * </p>
 * <p>
 * To use this plug-in, the user must create a subclass of this plug-in class
 * that implements the method <code>createServerSideDynamicPort</code>. This
 * method takes an interface offered by the component and return the inbound
 * port to which the dynamic connection will be done. A component can
 * participate in dynamic connections for several different offered
 * interfaces (but only one connection per offered interface), and therefore
 * the method may have to be able to return different types of inbound ports
 * for its different possible offered interfaces.
 * </p>
 * <p>
 * When two components want to use the dynamic connection plug-in, the
 * client-side component must first know the URI of the inbound port of
 * the server-side component offering the <code>ReflectionI</code>
 * interface. The two must then install their respective plug-ins
 * (client-side and server-side) and connect them. For the connection, the
 * client side component can use either the method
 * <code>connectWithServerSide</code> or directly the method
 * <code>doDynamicConnection</code> with the URI of the server
 * reflection inbound port.
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
 * <p>Created on : 2013-03-04</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	DynamicConnectionServerSidePlugin
extends		AbstractPlugin
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Plug-in internal constants and variables
	// -------------------------------------------------------------------------

	/** URI of the plug-in used in the plug-in call protocol.				*/
	public static final String		PLUGIN_URI =
										"DCONNECTION_SERVER_SIDE_PLUGIN" ;
	/** Port through which dynamic connection requests are received.		*/
	protected DynamicConnectionRequestInboundPort	dcrip ;
	/** The ports used in the dynamic connections.							*/
	protected Map<Class<?>,InboundPortI>			dynamicInboundPorts ;

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
		// offered interfaces to the ones of the component.
		this.addOfferedInterface(DynamicConnectionRequestCI.class);
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#initialise()
	 */
	@Override
	public void			initialise() throws Exception
	{
		// The port offering DynamicConnectionRequestI is created and published.
		this.dcrip =
			new DynamicConnectionRequestInboundPort(
										this.getPluginURI(), this.getOwner());
		this.dcrip.publishPort();

		this.dynamicInboundPorts = new HashMap<>();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPlugin#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPlugin#uninstall()
	 */
	@Override
	public void			uninstall() throws Exception
	{
		for(InboundPortI p : this.dynamicInboundPorts.values()) {
			p.unpublishPort();
			p.destroyPort();
		}
		this.dynamicInboundPorts.clear();
		// When uninstalling the plug-in, the ports and the interfaces added
		// to the component at installation time are removed.
		this.dcrip.unpublishPort();
		this.dcrip.destroyPort();
		this.removeOfferedInterface(DynamicConnectionRequestCI.class);
	}

	// -------------------------------------------------------------------------
	// Plug-in specific methods
	// -------------------------------------------------------------------------

	/**
	 * on the server side, create a new server side dynamic port, publish it
	 * using the method <code>createAndPublishDynamicPort</code> and return
	 * its unique identifier (URI).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code offeredInterface != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param offeredInterface	server-side interface through which the connection is made.
	 * @return					the URI of the newly created port.
	 * @throws Exception			<i>to do.</i>
	 */
	public String		requestDynamicPortURI(
		Class<? extends OfferedCI> offeredInterface
		) throws Exception
	{
		assert	offeredInterface != null;
		assert	this.getOwner().isOfferedInterface(offeredInterface);

		InboundPortI p = null;
		if (this.dynamicInboundPorts.containsKey(offeredInterface)) {
			p = this.dynamicInboundPorts.get(offeredInterface);
		} else {
			p = this.createAndPublishServerSideDynamicPort(offeredInterface);
			this.dynamicInboundPorts.put(offeredInterface, p);
		}

		return p.getPortURI();
	}

	/**
	 * remove the inbound port with the given URI that implements the given
	 * offered interface, if it exists.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code offeredInterface != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param offeredInterface	server-side offered interface.
	 * @param uri				URI of a previously created port.
	 * @throws Exception 		<i>to do</i>.
	 */
	public void			removeDynamicPort(
		Class<?> offeredInterface,
		String uri
		) throws Exception
	{
		if (this.dynamicInboundPorts.containsKey(offeredInterface)) {
			InboundPortI p = this.dynamicInboundPorts.get(offeredInterface);
			if (p.getPortURI().equals(uri)) {
				this.dynamicInboundPorts.remove(offeredInterface);
				if (!p.connected()) {
					p.unpublishPort();
					p.destroyPort();
				}
			}
		}
	}

	/**
	 * on the server side, create dynamically the port to be dynamically
	 * connected given an offered interface, and therefore determine what
	 * type of port must be created for that interface; the port must also
	 * be published, so the client side has the possibility to return an
	 * already created and published port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code offeredInterface != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param offeredInterface	server-side interface through which the connection is made.
	 * @return					the newly created port.
	 * @throws Exception 		<i>to do.</i>
	 */
	protected abstract InboundPortI	createAndPublishServerSideDynamicPort(
		Class<? extends OfferedCI> offeredInterface
		) throws Exception;
}
// -----------------------------------------------------------------------------
