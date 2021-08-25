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
import fr.sorbonne_u.components.connectors.AbstractDataConnector;
import fr.sorbonne_u.components.connectors.ConnectorI;
import fr.sorbonne_u.components.connectors.DataConnectorI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.exceptions.ConnectionException;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractDataInboundPort</code> partially implements an
 * inbound port for data exchanging components.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * Data exchanging components focus their interaction on the exchange of
 * pieces of data rather than calling each others services.  Hence, the
 * required and offered interfaces merely implements a simple protocol in
 * terms of methods used to pass data from the provider to the clients.
 * But data exchanges can be done in two modes: pull (the primary one) and push.
 * For inbound port, representing interfaces through which a provider is called,
 * the port implements the offered pull interface, while the connector
 * implements the offered push interface through which data can be pushed
 * from the provider towards the client.
 * </p>
 * <p>
 * A concrete inbound connector must therefore implement the method
 * <code>get</code> which will ask the owner component for a piece of data
 * and provide as result.
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
public abstract class	AbstractDataInboundPort
extends		AbstractInboundPort
implements	DataInboundPortI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** push interface implemented by this port, to send data to the client.*/
	protected final Class<? extends DataOfferedCI.PushCI>
												implementedPushInterface;
	/** URI of the client port to which this port is connected.				*/
	protected final AtomicReference<String>		clientPortURI;
	/** connectors of this port towards the client components.				*/
	protected final AtomicReference<DataOfferedCI.PushCI>	connector;
	/** when connected, true if the connection is remote and false
	 *  otherwise.															*/
	protected final AtomicBoolean				isRemotelyConnected;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

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
	protected static void	checkImplementationInvariant(
		AbstractDataInboundPort p
		) throws Exception
	{
		assert p != null;

		synchronized (p) {
			assert	p.connected() == (p.connector.get() != null) :
						new ImplementationInvariantException(
								"connected() == (connector.get() != null)");
			assert	(p.connector.get() == null) ==
											(p.clientPortURI.get() == null) :
						new ImplementationInvariantException(
								"(connector.get() == null) == "
								+ "(clientPortURI.get() == null)");
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
	protected static void	checkInvariant(AbstractDataInboundPort p)
	throws Exception
	{
		assert	p != null;

		// From DataInboundPortI
		synchronized (p) {
			assert	p.getImplementedInterface() == p.getImplementedPullInterface() :
						new InvariantException(
								"getImplementedInterface() == "
										+ "getImplementedPullInterface()");
			assert	!p.connected() == (p.getConnector() == null) :
						new InvariantException(
								"!connected() == (getConnector() == null)");
			assert	!p.connected() || p.getServerPortURI().equals(p.getPortURI()) :
						new InvariantException(
								"!p.connected() || " +
								"p.getServerPortURI().equals(p.getPortURI())");
			assert	!p.connected() || p.getClientPortURI() != null :
						new InvariantException(
								"!p.connected() || "
										+ "p.getClientPortURI() != null");
			assert	!p.isRemotelyConnected() || p.connected() :
						new InvariantException(
								"!isRemotelyConnected() || connected()");
			assert	!p.isRemotelyConnected() || p.isDistributedlyPublished() :
						new InvariantException(
								"!isRemotelyConnected() || "
										+ "p.isDistributedlyPublished()");
		}
	}

	public				AbstractDataInboundPort(
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		 // avoids missing super constructor error
		super(implementedInterface, owner) ;
		throw new Exception("AbstractDataInboundPort: must use the " +
				"three or four parameters version of the constructor.");
	}

	public				AbstractDataInboundPort(
		String uri,
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		 // avoids missing super constructor error
		super(uri, implementedInterface, owner);
		throw new Exception("AbstractDataInboundPort: must use the " +
				"three or four parameters version of the constructor.");
	}

	/**
	 * create and initialise data inbound ports, with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null and owner != null}
	 * pre	{@code implementedPullInterface != null}
	 * pre	{@code implementedPushInterface != null}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(uri)}
	 * post	{@code getImplementedInterface().equals(implementedPullInterface)}
	 * post	{@code getImplementedPushInterface().equals(implementedPushInterface)}
	 * </pre>
	 *
	 * @param uri						unique identifier of the port.
	 * @param implementedPullInterface	pull interface implemented by this port.
	 * @param implementedPushInterface	push interface implemented by this port.
	 * @param owner						component that owns this port.
	 * @param pluginURI					URI of the plug-in to be called in the owner or null if none.
	 * @param executorServiceURI		URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 				<i>to do.</i>
	 */
	public				AbstractDataInboundPort(
		String uri,
		Class<? extends DataOfferedCI.PullCI> implementedPullInterface,
		Class<? extends DataOfferedCI.PushCI> implementedPushInterface,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		super(uri, implementedPullInterface, owner, pluginURI,
			  executorServiceURI);

		assert	implementedPullInterface != null :
					new PreconditionException(
							"implementedPullInterface != null");
		assert	implementedPushInterface != null :
					new PreconditionException(
							"implementedPushInterface != null");

		this.implementedPushInterface = implementedPushInterface;
		this.clientPortURI = new AtomicReference<String>(null);
		this.connector = new AtomicReference<DataOfferedCI.PushCI>(null);
		this.isRemotelyConnected = new AtomicBoolean(false);

		AbstractDataInboundPort.checkImplementationInvariant(this);
		AbstractDataInboundPort.checkInvariant(this);
		AbstractInboundPort.checkImplementationInvariant(this);
		AbstractInboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	this.getImplementedPullInterface().
											equals(implementedPullInterface) :
					new PostconditionException(
							"this.getImplementedPullInterface()." + 
										"equals(implementedPullInterface)");
		assert	!this.connected() : new PostconditionException("!connected()");
		assert	!this.isRemotelyConnected() :
					new PostconditionException("!isRemotelyConnected()");
	}

	/**
	 * create and initialise data inbound ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code implementedPullInterface != null}
	 * pre	{@code implementedPushInterface != null}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * post	{@code getImplementedInterface().equals(implementedPullInterface)}
	 * post	{@code getImplementedPushInterface().equals(implementedPushInterface)}
	 * </pre>
	 *
	 * @param implementedPullInterface	pull interface implemented by this port.
	 * @param implementedPushInterface	push interface implemented by this port.
	 * @param owner						component that owns this port.
	 * @param pluginURI					URI of the plug-in to be called in the owner or null if none.
	 * @param executorServiceURI		URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 				<i>to do.</i>
	 */
	public				AbstractDataInboundPort(
		Class<? extends DataOfferedCI.PullCI> implementedPullInterface,
		Class<? extends DataOfferedCI.PushCI> implementedPushInterface,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		this(AbstractPort.generatePortURI(implementedPullInterface),
			 implementedPullInterface, implementedPushInterface,
			 owner, pluginURI, executorServiceURI);
	}

	/**
	 * create and initialise data inbound ports, with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null and owner != null}
	 * pre	{@code implementedPullInterface != null}
	 * pre	{@code implementedPushInterface != null}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(uri)}
	 * post	{@code getImplementedInterface().equals(implementedPullInterface)}
	 * post	{@code getImplementedPushInterface().equals(implementedPushInterface)}
	 * </pre>
	 *
	 * @param uri						unique identifier of the port.
	 * @param implementedPullInterface	pull interface implemented by this port.
	 * @param implementedPushInterface	push interface implemented by this port.
	 * @param owner						component that owns this port.
	 * @throws Exception 				<i>to do.</i>
	 */
	public				AbstractDataInboundPort(
		String uri,
		Class<? extends DataOfferedCI.PullCI> implementedPullInterface,
		Class<? extends DataOfferedCI.PushCI> implementedPushInterface,
		ComponentI owner
		) throws Exception
	{
		this(uri, implementedPullInterface, implementedPushInterface,
			 owner, null, null);
	}

	/**
	 * create and initialise a data inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code implementedPullInterface != null}
	 * pre	{@code implementedPushInterface != null}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * post	{@code getImplementedInterface().equals(implementedPullInterface)}
	 * post	{@code getImplementedPushInterface().equals(implementedPushInterface)}
	 * </pre>
	 *
	 * @param implementedPullInterface	pull interface implemented by this port.
	 * @param implementedPushInterface	push interface implemented by this port.
	 * @param owner						component that owns this port.
	 * @throws Exception 				<i>to do.</i>
	 */
	public				AbstractDataInboundPort(
		Class<? extends DataOfferedCI.PullCI> implementedPullInterface,
		Class<? extends DataOfferedCI.PushCI> implementedPushInterface,
		ComponentI owner
		) throws Exception
	{
		this(AbstractPort.generatePortURI(implementedPullInterface),
			 implementedPullInterface, implementedPushInterface, owner);
	}

	// -------------------------------------------------------------------------
	// Self-properties management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractPort#getImplementedInterface()
	 */
	@Override
	public Class<? extends DataOfferedCI.PullCI>	getImplementedInterface()
	throws Exception
	{
		// make sure this method is always used to get the pull interface
		return this.getImplementedPullInterface();
	}

	/**
	 * @see fr.sorbonne_u.components.ports.DataInboundPortI#getImplementedPullInterface()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends DataOfferedCI.PullCI>	getImplementedPullInterface()
	throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return (Class<? extends DataOfferedCI.PullCI>)
											super.getImplementedInterface();
	}

	/**
	 * @see fr.sorbonne_u.components.ports.DataInboundPortI#getImplementedPushInterface()
	 */
	@Override
	public Class<? extends DataOfferedCI.PushCI>	getImplementedPushInterface()
	throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return this.implementedPushInterface;
	}

	// -------------------------------------------------------------------------
	// Registry management
	// -------------------------------------------------------------------------

	/**
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !connected()}
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.AbstractPort#unpublishPort()
	 */
	@Override
	public void			unpublishPort() throws Exception
	{
		// until the AbstractInboundPort can know if they are connected.
		assert	!this.connected() : new PreconditionException("!connected()");

		super.unpublishPort();
	}

	// -------------------------------------------------------------------------
	// Life-cycle management
	// -------------------------------------------------------------------------

	/**
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !connected()}
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.ports.PortI#destroyPort()
	 */
	@Override
	public void			destroyPort() throws Exception
	{
		// until the AbstractInboundPort can know if they are connected.
		assert	!this.connected() : new PreconditionException("!connected()");

		super.destroyPort();
	}

	// -------------------------------------------------------------------------
	// Self-properties management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#setClientPortURI(java.lang.String)
	 */
	@Override
	public void			setClientPortURI(String clientPortURI)
	throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	clientPortURI != null :
					new PreconditionException("client port URI can't be null!");

		this.clientPortURI.set(clientPortURI);
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

	// -------------------------------------------------------------------------
	// Connection management
	// -------------------------------------------------------------------------

	/**
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	c != null and !this.connected()
	 * post	this.connected() and this.connector == c
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.ports.DataInboundPortI#setConnector(fr.sorbonne_u.components.connectors.DataConnectorI)
	 */
	@Override
	public void			setConnector(DataConnectorI c)
	throws Exception
	{
		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CONNECTING)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.CONNECTING,
				this.getClass().getName() +
									" setting connector " + c.toString());
		}

		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	c != null : new PreconditionException("c != null");

		this.connector.set((DataOfferedCI.PushCI)c);

		assert	this.getConnector() == c :
					new PostconditionException("getConnector() == c");
	}

	/**
	 * @see fr.sorbonne_u.components.ports.DataInboundPortI#unsetConnector()
	 */
	@Override
	public void			unsetConnector() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		this.connector.set(null);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.DataInboundPortI#getConnector()
	 */
	@Override
	public DataConnectorI	getConnector() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return (DataConnectorI) this.connector.get();
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#connected()
	 */
	@Override
	public boolean		connected() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return this.connector.get() != null;
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
	public synchronized void	doConnection(
		String otherPortURI,
		String ccname
		) throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("isPublished() && "
													+ "!connected()");
		assert	otherPortURI != null && ccname != null :
					new PreconditionException("otherPortURI != null && "
													+ "ccname != null");

		Class<?> cc = Class.forName(ccname);
		Constructor<?> c = cc.getConstructor(new Class<?>[]{});
		ConnectorI connector = (ConnectorI) c.newInstance();
		this.doConnection(otherPortURI, connector);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#doConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public synchronized void	doConnection(
		String otherPortURI,
		ConnectorI connector
		) throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("isPublished() && "
													+ "!connected()");
		assert	otherPortURI != null && connector != null :
					new PreconditionException("otherPortURI != null && "
													+ "connector != null");

		this.doMyConnection(otherPortURI, connector);
		// When the connection is remote, this call will serialise the
		// connector object and its deserialisation in the other JVM
		// hence duplicated. When is its local,  no duplication occurs.
		((AbstractDataConnector)this.getConnector()).
										obeyConnection(this, connector);

		AbstractDataInboundPort.checkImplementationInvariant(this);
		AbstractDataInboundPort.checkInvariant(this);
		AbstractInboundPort.checkImplementationInvariant(this);
		AbstractInboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	this.connected() :
					new PostconditionException("connected()");
	}

	/**
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

		this.setConnector((DataConnectorI)connector);
		this.setClientPortURI(otherPortURI);
		PortI clientPort =
			AbstractCVM.getFromLocalRegistry(this.getClientPortURI());
		if (clientPort == null && AbstractCVM.isDistributed) {
			this.isRemotelyConnected.set(true);
			clientPort = (PortI) AbstractDistributedCVM.getCVM().
								getRemoteReference(this.getClientPortURI());
		} else {
			this.isRemotelyConnected.set(false);
		}
		assert	clientPort != null :
					new ConnectionException("Unknown port URI: " +
													this.getClientPortURI());

		this.getConnector().connect((OfferedCI)this, (RequiredCI)clientPort);
	}

	/**
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
		this.obeyConnection(otherPortURI, connector);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#obeyConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			obeyConnection(
		String otherPortURI,
		ConnectorI connector
		) throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("isPublished() && "
													+ "!connected()");
		assert	otherPortURI != null && connector != null :
					new PreconditionException("otherPortURI != null && "
													+ "connector != null");

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		this.setConnector((DataConnectorI)connector);
		this.setClientPortURI(otherPortURI);
		PortI clientPort =
				AbstractCVM.getFromLocalRegistry(this.getClientPortURI());
		if (clientPort == null && AbstractCVM.isDistributed) {
			this.isRemotelyConnected.set(true);
			clientPort = (PortI) AbstractDistributedCVM.getCVM().
								getRemoteReference(this.getClientPortURI());
			((DataConnectorI)this.getConnector()).
							connect((OfferedCI)this, (RequiredCI)clientPort);
		} else {
			this.isRemotelyConnected.set(false);
		}

		AbstractDataInboundPort.checkImplementationInvariant(this);
		AbstractDataInboundPort.checkInvariant(this);
		AbstractInboundPort.checkImplementationInvariant(this);
		AbstractInboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	this.connected() :
					new PostconditionException("connected()");
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
					new PreconditionException("this.connected()");

		((AbstractDataConnector)this.getConnector()).obeyDisconnection(this);
		this.doMyDisconnection();

		AbstractDataInboundPort.checkImplementationInvariant(this);
		AbstractDataInboundPort.checkInvariant(this);
		AbstractInboundPort.checkImplementationInvariant(this);
		AbstractInboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	!this.connected() :
					new PostconditionException("!connected()");
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#doMyDisconnection()
	 */
	@Override
	protected void		doMyDisconnection() throws Exception
	{
		if (this.isRemotelyConnected()) {
			this.getConnector().disconnect();
		}
		this.unsetClientPortURI();
		this.unsetConnector();
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
					new PreconditionException("this.connected()");

		((DataConnectorI)this.getConnector()).disconnect();
		this.unsetClientPortURI();
		this.unsetConnector();
		this.isRemotelyConnected.set(false);

		assert	!this.connected() :
					new PostconditionException("!this.connected()");
	}

	// -------------------------------------------------------------------------
	// Request handling
	// -------------------------------------------------------------------------

	/**
	 * sends data to the connected component in the push mode.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code connected()}
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @throws Exception <i>todo.</i>
	 * 
	 * @see fr.sorbonne_u.components.interfaces.DataOfferedCI.PushCI#send(fr.sorbonne_u.components.interfaces.DataOfferedCI.DataI)
	 */
	@Override
	public void			send(DataOfferedCI.DataI d)
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	this.connected() :
					new PreconditionException("port is not connected!");

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			AbstractCVM.getCVM().logDebug(
						CVMDebugModes.CALLING,
						"AbstractDataInboundPort sends... " + d.toString() +
						" ...on connector " + connector.toString());
		}

		this.getConnector().send(d);

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			AbstractCVM.getCVM().logDebug(
						CVMDebugModes.CALLING,
						"...AbstractDataInboundPort sent! " + d.toString());
		}
	}
}
// -----------------------------------------------------------------------------
