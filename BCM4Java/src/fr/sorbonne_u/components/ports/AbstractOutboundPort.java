package fr.sorbonne_u.components.ports;

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

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.connectors.ConnectorI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.exceptions.ConnectionException;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractOutboundPort</code> partially implements an outbound
 * port which implements the required interface of the owning component so
 * that it can call its providers through this port.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * A concrete port class must implement the required interface of the component
 * with methods that call the corresponding services of their provider
 * component using the connector.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 2011-11-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractOutboundPort
extends		AbstractPort
implements	OutboundPortI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long					serialVersionUID = 1L;
	/** URI of the server port to which this port is connected.				*/
	protected final AtomicReference<String>		serverPortURI ;
	/** connector used to link with the provider component.					*/
	protected final AtomicReference<RequiredCI>	connector ;
	/** when connected, true if the connection is remote and false
	 *  otherwise.															*/
	protected final AtomicBoolean				isRemotelyConnected ;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * check the implementation invariant of the class.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	p != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param p				instance on which the invariant must be checked.
	 * @throws Exception	<i>to do</i>.
	 */
	protected static void	checkImplementationInvariant(
		AbstractOutboundPort p
		) throws Exception
	{
		assert	p != null;

		synchronized (p) {
			assert	!p.connected() == (p.connector.get() == null) :
						new ImplementationInvariantException(
								"connected() == (connector.get() == null)");
			assert	(p.connector.get() == null) ==
											(p.serverPortURI.get() == null) :
						new ImplementationInvariantException(
								"(connector.get() == null) == "
										+ "(serverPortURI.get() == null)");
		}
	}

	/**
	 * check the invariant of the class.
	 *
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	p != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param p				the object on which the invariant must be checked.
	 * @throws Exception	<i>todo.</i>
	 */
	protected static void	checkInvariant(AbstractOutboundPort p)
	throws Exception
	{
		assert	p != null ;

		synchronized (p) {
			// From OutboundPortI
			assert	p.getOwner().isRequiredInterface(
												p.getImplementedInterface()) :
						new InvariantException(
								"getOwner().isRequiredInterface("
								+ "getImplementedInterface())"
								+ p.getImplementedInterface() + "]");
			assert	!p.connected() || p.isPublished() :
						new InvariantException("!connected() || isPublished()");
			assert	!p.connected() || p.getPortURI().equals(p.getClientPortURI()) :
						new InvariantException(
								"!connected() || "
								+ "getPortURI().equals(getClientPortURI())");
			assert	!p.connected() || p.getServerPortURI() != null :
						new InvariantException(
								"!connected() || getServerPortURI() != null");
			assert	!p.connected() == (p.getConnector() == null) :
						new InvariantException(
								"!connected() == (getConnector() == null)");
			assert	!p.connected() == (p.getConnector() == null) :
						new InvariantException(
								"!connected() == (getConnector() == null)");
			assert	!p.isRemotelyConnected() || p.connected() :
						new InvariantException(
								"!isRemotelyConnected() || connected()");
		}
	}

	/**
	 * create and initialise outbound ports, with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null && uri != null && implementedInterface != null}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>todo.</i>
	 */
	public				AbstractOutboundPort(
		String uri,
		Class<? extends RequiredCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super(uri, implementedInterface, owner) ;

		this.serverPortURI = new AtomicReference<String>(null);
		this.connector = new AtomicReference<RequiredCI>(null);
		this.isRemotelyConnected = new AtomicBoolean(false);

		AbstractOutboundPort.checkImplementationInvariant(this);
		AbstractOutboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	!this.connected() : new PostconditionException("!connected()");
		assert	!this.isRemotelyConnected() :
					new PostconditionException("!isRemotelyConnected()");
	}

	/**
	 * create and initialize outbound ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null && implementedInterface != null}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception			<i>todo.</i>
	 */
	public				AbstractOutboundPort(
		Class<? extends RequiredCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		this(AbstractPort.generatePortURI(implementedInterface),
			 implementedInterface, owner);
 	}

	// -------------------------------------------------------------------------
	// Self-properties management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.OutboundPortI#getImplementedInterface()
	 * @see fr.sorbonne_u.components.AbstractPort#getImplementedInterface()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends RequiredCI>	getImplementedInterface()
	throws Exception
	{
		return (Class<? extends RequiredCI>) super.getImplementedInterface();
	}
	// -------------------------------------------------------------------------
	// Registry management
	// -------------------------------------------------------------------------


	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unpublishPort()
	 */
	@Override
	public void			unpublishPort() throws Exception
	{
		assert	!this.connected() :
					new PreconditionException("!connected()");

		super.unpublishPort();
	}

	// -------------------------------------------------------------------------
	// Life-cycle management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#destroyPort()
	 */
	@Override
	public void			destroyPort() throws Exception
	{
		assert	!this.connected() :
					new PreconditionException("!connected()");

		super.destroyPort();
	}

	// -------------------------------------------------------------------------
	// Self-properties management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractPort#setServerPortURI(java.lang.String)
	 */
	@Override
	public void			setServerPortURI(String serverPortURI)
	throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	serverPortURI != null :
					new PreconditionException("serverPortURI != null");

		this.serverPortURI.set(serverPortURI);

		assert	this.getServerPortURI().equals(serverPortURI) :
					new PostconditionException(
								"getServerPortURI().equals(serverPortURI)");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPort#unsetServerPortURI()
	 */
	@Override
	public void			unsetServerPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		this.serverPortURI.set(null);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getServerPortURI()
	 */
	@Override
	public String		getServerPortURI()
	throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		return this.serverPortURI.get();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPort#setClientPortURI(java.lang.String)
	 */
	@Override
	public void			setClientPortURI(String clientPortURI)
	throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	clientPortURI != null :
					new PreconditionException("clientPortURI != null");
		assert	this.getPortURI().equals(clientPortURI) :
					new PreconditionException(
								"getPortURI().equals(clientPortURI)");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPort#unsetClientPortURI()
	 */
	@Override
	public void			unsetClientPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		throw new ConnectionException("Can't unset the client port URI "
												+ "of an outbound port!");

	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getClientPortURI()
	 */
	@Override
	public String		getClientPortURI()
	throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		return this.getPortURI();
	}

	// -------------------------------------------------------------------------
	// Connection management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.OutboundPortI#setConnector(fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			setConnector(ConnectorI c)
	throws	Exception
	{
		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CONNECTING)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.CONNECTING,
				this.getClass().getName() + " setting connector "
															+ c.toString());
		}

		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	c != null :
					new PreconditionException("c != null");

		this.connector.set((RequiredCI)c);

		assert	this.getConnector() == c :
					new PostconditionException("getConnector() == c");
	}

	/**
	 * @see fr.sorbonne_u.components.ports.OutboundPortI#unsetConnector()
	 */
	@Override
	public void			unsetConnector() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		this.connector.set(null);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.OutboundPortI#getConnector()
	 */
	@Override
	public ConnectorI	getConnector() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		return (ConnectorI)this.connector.get();
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#connected()
	 */
	@Override
	public boolean		connected() throws Exception
	{
		return this.connector.get() != null;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#isRemotelyConnected()
	 */
	@Override
	public boolean		isRemotelyConnected() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		return this.isRemotelyConnected.get();
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#doConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized void	doConnection(
		String otherPortURI,
		String ccname
		) throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("isPublished() && "
													+ "!connected()");
		assert	otherPortURI != null && ccname != null :
					new PreconditionException("otherPortURI != null && "
													+ "ccname != null");

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		Class<?> cc = Class.forName(ccname);
		Constructor<?> c = cc.getConstructor(new Class<?>[]{});
		ConnectorI connector = (ConnectorI) c.newInstance();
		this.doConnection(otherPortURI, connector);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#doConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public synchronized void	doConnection(
		String otherPortURI,
		ConnectorI connector
		) throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("isPublished() && "
													+ "!connected()");
		assert	otherPortURI != null && connector != null :
					new PreconditionException("otherPortURI != null && "
													+ "connector != null");

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		// In a simple client/server connection, where a plain outbound port is
		// connected to a plain inbound port, be it remote or local, the
		// connection is done one way on the client (outbound port) side,
		// so we need only to connect this side.
		this.doMyConnection(otherPortURI, connector);

		AbstractOutboundPort.checkImplementationInvariant(this);
		AbstractOutboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	this.connected() :
					new PostconditionException("connected()");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPort#doMyConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	protected synchronized void	doMyConnection(
		String otherPortURI,
		ConnectorI connector
		) throws Exception
	{
		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		this.setConnector(connector);
		this.setServerPortURI(otherPortURI);
		PortI serverPort =
				AbstractCVM.getFromLocalRegistry(this.getServerPortURI());
		if (serverPort == null && AbstractCVM.isDistributed) {
			this.isRemotelyConnected.set(true);
			serverPort = (PortI)AbstractDistributedCVM.getCVM().
									getRemoteReference(this.getServerPortURI());
		} else {
			this.isRemotelyConnected.set(false);
		}
		assert	serverPort != null :
					new ConnectionException("Unknown server port URI: " +
													this.getServerPortURI());

		this.getConnector().connect((OfferedCI)serverPort, this);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#obeyConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public void			obeyConnection(String otherPortURI, String ccname)
	throws	Exception
	{
		throw new ConnectionException("Can't call obeyConnection on simple"
														+ " outbound ports.");
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#obeyConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			obeyConnection(String otherPortURI, ConnectorI connector)
	throws	Exception
	{
		throw new ConnectionException("Can't call obeyConnection on simple"
														+ " outbound ports.");
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#doDisconnection()
	 */
	@Override
	public synchronized void	doDisconnection() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	this.connected() &&
							((ConnectorI)this.getConnector()).connected() :
					new PreconditionException(
							"connected() && "
							+ "((ConnectorI)connector).connected()");

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		// In a simple client/server connection, where a plain outbound port is
		// connected to a plain inbound port, be it remote or local, the
		// connection is done one way on the client (outbound port) side,
		// so we need only to disconnect this side.
		this.doMyDisconnection();

		AbstractOutboundPort.checkImplementationInvariant(this);
		AbstractOutboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	!this.connected() :
					new PostconditionException("!connected()");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPort#doMyDisconnection()
	 */
	@Override
	protected synchronized void	doMyDisconnection() throws Exception
	{
		assert	this.connected() :
					new PreconditionException("connected()");

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		((ConnectorI)this.getConnector()).disconnect();
		this.unsetServerPortURI();
		this.unsetConnector();
		this.isRemotelyConnected.set(false);

		assert	!this.connected() :
					new PostconditionException("!connected()");
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#obeyDisconnection()
	 */
	@Override
	public void			obeyDisconnection() throws Exception
	{
		throw new ConnectionException("Can't call obeyDisconnection on simple"
														+ " outbound ports.");
	}
}
// -----------------------------------------------------------------------------
