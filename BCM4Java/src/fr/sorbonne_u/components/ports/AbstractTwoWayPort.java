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
import fr.sorbonne_u.components.connectors.AbstractTwoWayConnector;
import fr.sorbonne_u.components.connectors.ConnectorI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.interfaces.TwoWayCI;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractTwoWayPort</code> partially implements a two-way
 * port for components calling each others in a peer-to-peer fashion.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * As components connected in a two-way peer-to-peer relationship where both
 * can call each others using the same methods, the two-way port is modeled
 * upon the inbound ports that admit multiple clients connected to them.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code !connected() || (getServerPortURI() != null)}
 * invariant	{@code !connected() || (getClientPortURI() != null)}
 * invariant	{@code !connected() == (getConnector() == null)}
 * invariant	{@code !connected() || (getPortURI().equals(getClientPortURI()) || getPortURI().equals(getServerPortURI()))}
 * invariant	{@code !connected() || (getOut() != null)}
 * invariant	{@code !isRemotelyConnected() || connected()}
 * invariant	{@code !isRemotelyConnected() || isDistributedlyPublished()}
 * </pre>
 * 
 * <p>Created on : 2012-01-23</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractTwoWayPort<TWI extends TwoWayCI>
extends		AbstractInboundPort
implements	TwoWayPortI<TWI>
{
	private static final long	serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Inner classes
	// -------------------------------------------------------------------------

	/**
	 * The class <code>OutProxy</code>  implements an object that forwards
	 * calls through a two way interface to the other peer component.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * This class must be extended by two way ports to implement their two way
	 * interface methods.
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2018-03-26</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 * @version	$Name$ -- $Revision$ -- $Date$
	 */
	protected static abstract class	OutProxy<T extends TwoWayCI>
	implements TwoWayCI
	{
		protected final	AbstractTwoWayPort<T>	owner ;

		/**
		 * create the out proxy object.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	owner != null
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param owner	the port that owns this proxy.
		 */
		public			OutProxy(AbstractTwoWayPort<T> owner)
		{
			super() ;
			assert	owner != null ;
			this.owner = owner;
		}

		/**
		 * return a connector reference towards the other component.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @return			a connector reference towards the other component.
		 * @throws Exception	<i>to do.</i>
		 */
		protected T		getProxyTowardsOtherComponent()
		throws Exception
		{
			return this.owner.getConnector().
						getProxyTowardsOtherComponent(
												this.owner.getPortURI()) ;
		}
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the client port to which this port is connected.				*/
	protected AtomicReference<String>		clientPortURI ;
	/** URI of the server port to which this port is connected.				*/
	protected AtomicReference<String>		serverPortURI ;
	/** connector used to link with the other component.					*/
	protected AtomicReference<AbstractTwoWayConnector<TWI>>	connector ;
	/** proxy used to forward calls to the other component.					*/
	protected AtomicReference<TWI>			out ;
	/** when connected, true if the connection is remote and false
	 *  otherwise.															*/
	protected AtomicBoolean					isRemotelyConnected ;

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
	 * @param p			the object on which the invariant must be checked.
	 * @throws Exception	<i>todo.</i>
	 */
	protected static void	checkImplementationInvariant(
		AbstractTwoWayPort<?> p
		) throws Exception
	{
		assert	p != null;

		synchronized (p) {
			assert	p.connected() == (p.connector.get() != null) :
						new ImplementationInvariantException(
								"connected() == (connector.get() != null)");
			assert	(p.connector.get() == null) ==
											(p.clientPortURI.get() == null) :
						new ImplementationInvariantException(
								"(connector.get() == null) == "
										+ "(clientPortURI.get() == null)");
			assert	(p.connector.get() == null) ==
										(p.clientPortURI.get() == null) :
						new ImplementationInvariantException(
								"(connector.get() == null) == "
										+ "(clientPortURI.get() == null)");
			assert	(p.connector.get() == null) || (p.out.get() != null) :
						new ImplementationInvariantException(
								"(connector.get() == null) || "
										+ "(out.get() != null)");
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
	 * @param p			the object on which the invariant must be checked.
	 * @throws Exception	<i>todo.</i>
	 */
	protected static void	checkInvariant(AbstractTwoWayPort<?> p) 
	throws Exception
	{
		assert	p != null ;

		synchronized (p) {
			assert	!p.connected() || (p.getServerPortURI() != null) :
						new InvariantException(
								"!connected() || (getServerPortURI() != null)");
			assert	!p.connected() || (p.getClientPortURI() != null) :
						new InvariantException(
								"!connected() || getClientPortURI() != null");
			assert	!p.connected() == (p.getConnector() == null) :
						new InvariantException(
								"!connected() == (getConnector() == null)");
			assert	!p.connected() ||
						(p.getPortURI().equals(p.getClientPortURI()) ||
							p.getPortURI().equals(p.getServerPortURI())) :
						new InvariantException(
								"!connected() || "
								+ "(getPortURI().equals(getClientPortURI()) || "
								+ "getPortURI().equals(getServerPortURI()))") ;
			assert	!p.connected() || (p.getOut() != null) :
						new InvariantException(
								"!connected() || (getOut() != null)");
			assert	!p.isRemotelyConnected() || p.connected() :
						new InvariantException(
								"!isRemotelyConnected() || connected()");
			assert	!p.isRemotelyConnected() || p.isDistributedlyPublished() :
						new InvariantException(
								"!isRemotelyConnected() || "
										+ "p.isDistributedlyPublished()");
		}
	}

	/**
	 * create and initialise two-way ports, with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null && uri != null}
	 * pre	{@code implementedInterface != null}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(uri)}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do.</i>
	 */
	public				AbstractTwoWayPort(
		String uri,
		Class<? extends TwoWayCI> implementedInterface,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		super(uri, implementedInterface, owner, pluginURI, executorServiceURI);

		this.clientPortURI= new AtomicReference<String>(null);
		this.serverPortURI= new AtomicReference<String>(null);
		this.connector =
					new AtomicReference<AbstractTwoWayConnector<TWI>>(null);
		this.out = new AtomicReference<TWI>(null);
		this.isRemotelyConnected = new AtomicBoolean(false);

		AbstractTwoWayPort.checkImplementationInvariant(this);
		AbstractTwoWayPort.checkInvariant(this) ;
		AbstractInboundPort.checkImplementationInvariant(this);
		AbstractInboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	!this.connected() : new PostconditionException("!connected()");
		assert	!this.isRemotelyConnected() :
					new PostconditionException("!isRemotelyConnected()");
	}

	/**
	 * create and initialise two-way ports, with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null && uri != null}
	 * pre	{@code implementedInterface != null}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(uri)}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>to do.</i>
	 */
	public				AbstractTwoWayPort(
		String uri,
		Class<? extends TwoWayCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		this(uri, implementedInterface, owner, null, null);
	}

	/**
	 * create and initialise two-way ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code implementedInterface != null}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do.</i>
	 */
	public				AbstractTwoWayPort(
		Class<? extends TwoWayCI> implementedInterface,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		this(AbstractPort.generatePortURI(implementedInterface),
			 implementedInterface, owner, pluginURI, executorServiceURI);
	}

	/**
	 * create and initialise two-way ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code implementedInterface != null}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>to do.</i>
	 */
	public				AbstractTwoWayPort(
		Class<? extends TwoWayCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		this(implementedInterface, owner, null, null);
	}

	// -------------------------------------------------------------------------
	// Self-properties management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#getImplementedInterface()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends TwoWayCI>	getImplementedInterface() throws Exception
	{
		return (Class<? extends TwoWayCI>) super.getImplementedInterface();
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
		assert	!this.connected() : new PreconditionException("!connected()");

		super.unpublishPort() ;
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
		assert	!this.connected() : new PreconditionException("!connected()");

		super.destroyPort() ;
	}

	// -------------------------------------------------------------------------
	// Self-properties management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#setClientPortURI(java.lang.String)
	 */
	@Override
	public void			setClientPortURI(String clientPortURI)
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	clientPortURI != null :
					new PreconditionException("client port URI can't be null!");

		this.clientPortURI.set(clientPortURI);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#setServerPortURI(java.lang.String)
	 */
	@Override
	public void			setServerPortURI(String serverPortURI)
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	serverPortURI != null :
					new PreconditionException("server port URI can't be null!");

		this.serverPortURI.set(serverPortURI);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unsetClientPortURI()
	 */
	@Override
	public void			unsetClientPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		this.clientPortURI.set(null);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unsetServerPortURI()
	 */
	@Override
	public void			unsetServerPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		this.serverPortURI.set(null);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#getClientPortURI()
	 */
	@Override
	public String		getClientPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return this.clientPortURI.get();
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#getServerPortURI()
	 */
	@Override
	public String		getServerPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return this.serverPortURI.get();
	}

	// -------------------------------------------------------------------------
	// Connection management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.OutboundPortI#getConnector()
	 */
	@Override
	public AbstractTwoWayConnector<TWI>	getConnector() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return this.connector.get();
	}

	/**
	 * @see fr.sorbonne_u.components.ports.OutboundPortI#setConnector(fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void 		setConnector(ConnectorI c)
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	c != null : new PreconditionException("c != null") ;

		this.connector.set((AbstractTwoWayConnector<TWI>)c);

		assert	this.getConnector() == c :
					new PostconditionException("getConnector() == c") ;
	}

	@Override
	public void 		unsetConnector() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		this.connector.set(null);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#connected()
	 */
	@Override
	public boolean		connected() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return this.connector.get() != null ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#isRemotelyConnected()
	 */
	@Override
	public boolean		isRemotelyConnected() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return this.isRemotelyConnected.get();
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#doConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public void			doConnection(String otherPortURI, String ccname)
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("this.isPublished() && "
													+ "!this.connected()") ;
		assert	otherPortURI != null && ccname != null :
					new PreconditionException("otherPortURI != null && "
													+ "ccname != null") ;

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		// As two-way connections assume that components are peers, they
		// are symmetric rather than assymetric.  Hence, the same code will
		// be executed on both sides: the code that appears here, in this class.
		// Therefore, we must make sure that both sides will use the same ports
		// to act as client and server ports.  The connection made here assumes
		// that the current port is the server port, and the other port is the
		// client.
		Class<?> cc = Class.forName(ccname) ;
		Constructor<?> c = cc.getConstructor(new Class<?>[]{}) ;
		ConnectorI connector = (ConnectorI) c.newInstance() ;
		this.doConnection(otherPortURI, connector) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#doConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			doConnection(String otherPortURI, ConnectorI connector)
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("this.isPublished() && "
													+ "!this.connected()") ;
		assert	otherPortURI != null && connector != null :
					new PreconditionException("otherPortURI != null && "
													+ "connector != null") ;

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		this.doMyConnection(otherPortURI, connector) ;
		// As two-way connections assume that components are peers, they
		// are symmetric rather than assymetric.  Hence, the same code will
		// be executed on both sides: the code that appears here, in this class.
		// Therefore, we must make sure that both sides will use the same ports
		// to act as client and server ports.  The connection made here assumes
		// that the current port is the server port, and the other port is the
		// client.
		this.getConnector().obeyConnection(this, connector) ;

		AbstractTwoWayPort.checkImplementationInvariant(this);
		AbstractTwoWayPort.checkInvariant(this) ;
		AbstractInboundPort.checkImplementationInvariant(this);
		AbstractInboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	this.connected() :
					new PostconditionException("this.connected()") ;
	}

	/**
	 * connect this port, knowing that this port initiated the connection
	 * and that in the connection protocol of two way ports, the initiator will
	 * always be considered as the requiring/client side and the other side
	 * the offering/server (to be compatible with the connection protocol of
	 * plain outbound and inbound ports.
	 * 
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#doMyConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	protected void		doMyConnection(
		String otherPortURI,
		ConnectorI connector
		) throws Exception
	{
		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		this.setConnector(connector) ;
		this.setServerPortURI(otherPortURI) ;
		this.setClientPortURI(this.getPortURI()) ;
		PortI serverPort =
				AbstractCVM.getFromLocalRegistry(this.getServerPortURI()) ;
		if (serverPort == null && AbstractCVM.isDistributed) {
			this.isRemotelyConnected.set(true);
			serverPort =
				(PortI) AbstractDistributedCVM.getCVM().
								getRemoteReference(this.getServerPortURI()) ;
		} else {
			this.isRemotelyConnected.set(false);
		}
		assert	serverPort != null :
					new Exception("Unkown port URI: " + this.getServerPortURI());
		this.getConnector().connect((OfferedCI)serverPort, this) ;
	}

	/**
	 * connect this port, knowing that the other port initiated the connection
	 * and that in the connection protocol of two way ports, the initiator will
	 * always be considered as the requiring/client side and the other side
	 * the offering/server (to be compatible with the connection protocol of
	 * plain outbound and inbound ports.
	 * 
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#obeyConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public void			obeyConnection(String otherPortURI, String ccname)
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("this.isPublished() && "
													+ "!this.connected()") ;
		assert	otherPortURI != null && ccname != null :
					new PreconditionException("otherPortURI != null && "
													+ "ccname != null") ;

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		Class<?> cc = Class.forName(ccname) ;
		Constructor<?> c = cc.getConstructor(new Class<?>[]{}) ;
		ConnectorI connector = (ConnectorI) c.newInstance() ;
		this.obeyConnection(otherPortURI, connector) ;
	}

	/**
	 * connect this port, knowing that the other port initiated the connection
	 * and that in the connection protocol of two way ports, the initiator will
	 * always be considered as the requiring/client side and the other side
	 * the offering/server (to be compatible with the connection protocol of
	 * plain outbound and inbound ports.
	 * 
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#obeyConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			obeyConnection(String otherPortURI, ConnectorI connector)
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("this.isPublished() && "
													+ "!this.connected()") ;
		assert	otherPortURI != null && connector != null :
					new PreconditionException("otherPortURI != null && "
													+ "connector != null") ;

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		this.setServerPortURI(this.getPortURI()) ;
		this.setClientPortURI(otherPortURI) ;
		this.setConnector(connector) ;
		PortI clientPort =
				AbstractCVM.getFromLocalRegistry(this.getClientPortURI()) ;
		if (clientPort == null && AbstractCVM.isDistributed) {
			this.isRemotelyConnected.set(true);
			clientPort = (PortI) AbstractDistributedCVM.getCVM().
								getRemoteReference(this.getClientPortURI()) ;
			this.getConnector().
							connect((OfferedCI)this, (RequiredCI)clientPort) ;
		} else {
			this.isRemotelyConnected.set(false);
		}

		AbstractTwoWayPort.checkImplementationInvariant(this);
		AbstractTwoWayPort.checkInvariant(this) ;
		AbstractInboundPort.checkImplementationInvariant(this);
		AbstractInboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	this.connected() :
					new PostconditionException("this.connected()") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#doDisconnection()
	 */
	@Override
	public void			doDisconnection() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	this.connected() :
					new PreconditionException("this.connected()") ;

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		this.getConnector().obeyDisconnection(this) ;
		this.doMyDisconnection() ;

		AbstractTwoWayPort.checkImplementationInvariant(this);
		AbstractTwoWayPort.checkInvariant(this) ;
		AbstractInboundPort.checkImplementationInvariant(this);
		AbstractInboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	!this.connected() : new PostconditionException("!connected()") ;
	}

	/**
	 * disconnect this port, knowing that this port initiated the
	 * disconnection.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.connected()
	 * post	!this.connected()
	 * </pre>
	 *
	 * @throws Exception		<i>todo.</i>
	 */
	protected void		doMyDisconnection() throws Exception
	{
		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		if (this.isRemotelyConnected()) {
			this.getConnector().disconnect() ;
		}
		this.unsetConnector() ;
		this.unsetClientPortURI() ;
		this.unsetServerPortURI() ;
		this.isRemotelyConnected.set(false);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#obeyDisconnection()
	 */
	@Override
	public void			obeyDisconnection() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	this.connected() :
					new PreconditionException("this.connected()") ;

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		this.getConnector().disconnect() ;
		this.unsetConnector() ;
		this.unsetClientPortURI() ;
		this.unsetServerPortURI() ;
		this.isRemotelyConnected.set(false);

		AbstractTwoWayPort.checkImplementationInvariant(this);
		AbstractTwoWayPort.checkInvariant(this) ;
		AbstractInboundPort.checkImplementationInvariant(this);
		AbstractInboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	!this.connected() : new PostconditionException("!connected()") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.TwoWayPortI#getOut()
	 */
	@Override
	public TWI			getOut() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return this.out.get();
	}

	/**
	 * sets the proxy to be used by the owner component when calling
	 * components connected to this port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	out != null
	 * pre	this.getOut() == null
	 * post	this.getOut() == out
	 * </pre>
	 *
	 * @param out	proxy used when calling components connected to this port.
	 * @throws Exception <i>todo.</i>
	 */
	@SuppressWarnings("unchecked")
	protected void		setOut(TwoWayCI out) throws Exception
	{
		assert	out != null :
					new PreconditionException("out != null") ;
		assert	this.getOut() == null :
					new PreconditionException("getOut() == null") ;
		
		this.out.set((TWI)out);

		assert	this.getOut() == out :
					new PostconditionException("getOut() == out") ;

	}
}
// -----------------------------------------------------------------------------
