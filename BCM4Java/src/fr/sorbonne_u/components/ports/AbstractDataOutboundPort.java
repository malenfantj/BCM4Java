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
import java.util.concurrent.atomic.AtomicInteger;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.connectors.AbstractDataConnector;
import fr.sorbonne_u.components.connectors.ConnectorI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractDataOutboundPort</code> partially implements an
 * outbound port for data exchanging components.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * Data exchanging components focus their interaction on the exchange of
 * pieces of data rather than calling each others services.  Hence, the
 * required and offered interfaces merely implements a simple protocol in
 * terms of methods used to pass data from the provider to the clients.
 * But data exchanges can be done in two modes: pull (the primary one) and push.
 * For outbound port, representing interfaces through which a client calls the
 * provider, the port uses the required pull interface, that is also implemented
 * by the connector, while the port implements the required push interface
 * through which data can be received in push mode from the provider.
 * </p>
 * <p>
 * A concrete outbound connector must therefore implement the method
 * <code>receive</code> which will receive a piece of data as parameter
 * and pass it to the owner component.
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
public abstract class	AbstractDataOutboundPort
extends		AbstractOutboundPort
implements	DataOutboundPortI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long		serialVersionUID = 1L;
	/** push interface implemented by this port, to receive data from the
	 *  provider.									 						*/
	protected final Class<? extends DataRequiredCI.PushCI>
												implementedPushInterface;
	/** URI of the plug-in to be called in the owner or null if none.		*/
	protected final String			pluginURI;
	/** URI of the executor service to be used to execute the service on the
	 *  owner or null if none.												*/
	protected final String			executorServiceURI;
	/** index of the executor service in the owner (beware that this can
	 *  change over the execution of the owner if executor services are shut
	 *  down dynamically).													*/
	protected final AtomicInteger	executorServiceIndex = new AtomicInteger(-1);

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * check the invariant of the class.
	 *
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code p != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param p				the object on which the invariant must be checked.
	 * @throws Exception	<i>todo.</i>
	 */
	protected static void	checkImplementationInvariant(
		AbstractDataOutboundPort p
		) throws Exception
	{
		assert	p != null;

		synchronized (p) {
			assert	(p.executorServiceURI == null) ==
										(p.executorServiceIndex.get() < 0) :
						new ImplementationInvariantException(
								"(executorServiceURI == null) == " + 
								"(executorServiceIndex == -1)");
			assert	p.executorServiceURI == null ||
							p.executorServiceIndex.get() ==
									p.getExecutorServiceIndex(
													p.executorServiceURI) :
						new ImplementationInvariantException(
								"executorServiceURI == null || " +
								"executorServiceIndex == " +
								"owner.getExecutorServiceIndex(" +
								"executorServiceURI)");
			try {
				assert	!p.hasPlugin() ||
								p.getOwner().isInstalled(p.getPluginURI()) :
							new ImplementationInvariantException(
									"owner component does not have an " +
									"installed plug-in with URI: " +
									p.getPluginURI());
				assert	!p.hasExecutorService() ||
									p.getOwner().validExecutorServiceURI(
													p.getExecutorServiceURI()) :
							new ImplementationInvariantException(
									"owner component does not have an executor "
									+ "service with URI: "
									+ p.getExecutorServiceURI());
				assert	!p.hasExecutorService() ||
								p.getExecutorServiceIndex() ==
									p.getExecutorServiceIndex(
													p.getExecutorServiceURI()) :
							new ImplementationInvariantException(
									"executor service with URI " +
									p.getExecutorServiceURI() +
									" no longer have index " +
									p.getExecutorServiceIndex() +
									" in owner!");
			} catch (Exception e) {
				throw new Exception(e) ;
			}
		}
	}

	/**
	 * check the invariant of the class.
	 *
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code p != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param p				the object on which the invariant must be checked.
	 * @throws Exception	<i>todo.</i>
	 */
	protected static void	checkInvariant(AbstractDataOutboundPort p)
	throws Exception
	{
		assert	p != null ;

		synchronized (p) {
			// From DataOutboundPortI
			assert	p.getImplementedInterface().equals(
											p.getImplementedPullInterface()) :
						new InvariantException(
								"!connected() || " + 
								"getImplementedInterface().equals(" + 
								"getImplementedPullInterface())");
			assert	!p.isRemotelyConnected() || p.isDistributedlyPublished() :
						new InvariantException(
								"!isRemotelyConnected() || "
											+ "isDistributedlyPublished()");
		}
	}

	/*
	 * Disallowed! (use another constructor)
	 */
	public				AbstractDataOutboundPort(
		Class<? extends RequiredCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super(implementedInterface, owner);
		throw new Exception("AbstractDataOutboundPort: must use the " +
				"three or four parameters version of the constructor.");
	}

	/*
	 * Disallowed! (use another constructor)
	 */
	public				AbstractDataOutboundPort(
		String uri,
		Class<? extends RequiredCI> implementedInterface,
		ComponentI	owner
		) throws Exception
	{
		super(uri, implementedInterface, owner);
		throw new Exception("AbstractDataOutboundPort: must use the " +
				"three or four parameters version of the constructor.");
	}

	/**
	 * create and initialize a data outbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null and owner != null}
	 * pre	{@code implementedPullInterface != null}
	 * pre	{@code implementedPushInterface != null}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
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
	 * @throws Exception  				<i>todo.</i>
	 */
	public				AbstractDataOutboundPort(
		String uri,
		Class<?  extends DataRequiredCI.PullCI> implementedPullInterface,
		Class<? extends DataRequiredCI.PushCI> implementedPushInterface,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		super(uri, implementedPullInterface, owner) ;

		assert	implementedPushInterface != null :
					new PreconditionException(
							"implementedPushInterface != null");
		assert	DataRequiredCI.PullCI.class.
								isAssignableFrom(implementedPullInterface);
					new PreconditionException(
							"DataRequiredCI.PullCI.class." + 
							"isAssignableFrom(implementedPullInterface)");
		assert	DataRequiredCI.PushCI.class.
								isAssignableFrom(implementedPushInterface);
					new PreconditionException(
							"DataRequiredCI.PushCI.class." + 
							"isAssignableFrom(implementedPushInterface)");
		assert	pluginURI == null || owner.isInstalled(pluginURI) :
					new PreconditionException(
							"owner component does not have an installed "
							+ "plug-in with URI: " + pluginURI);
		assert	executorServiceURI == null ||
						owner.validExecutorServiceURI(executorServiceURI) :
					new PreconditionException(
							"owner component does not have an executor "
							+ "service with URI: " + executorServiceURI);

		this.implementedPushInterface = implementedPushInterface;
		this.pluginURI = pluginURI;
		this.executorServiceURI = executorServiceURI;
		if (executorServiceURI != null) {
			this.executorServiceIndex.set(
							this.getExecutorServiceIndex(executorServiceURI));
		}

		AbstractDataOutboundPort.checkImplementationInvariant(this);
		AbstractDataOutboundPort.checkInvariant(this);
		AbstractOutboundPort.checkImplementationInvariant(this);
		AbstractOutboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	this.getImplementedPullInterface().
											equals(implementedPullInterface) :
					new PostconditionException(
							"this.getImplementedPullInterface()." + 
									"equals(implementedPullInterface)");
	}

	/**
	 * create and initialize a data outbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code implementedPullInterface != null}
	 * pre	{@code implementedPushInterface != null}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
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
	 * @throws Exception  				<i>todo.</i>
	 */
	public				AbstractDataOutboundPort(
		Class<? extends DataRequiredCI.PullCI> implementedPullInterface,
		Class<? extends DataRequiredCI.PushCI> implementedPushInterface,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		this(AbstractPort.generatePortURI(implementedPullInterface),
			 implementedPullInterface, implementedPushInterface, owner,
			 pluginURI, executorServiceURI);
	}

	/**
	 * create and initialize a data putbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null and owner != null}
	 * pre	{@code implementedPullInterface != null}
	 * pre	{@code implementedPushInterface != null}
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
	 * @throws Exception  				<i>todo.</i>
	 */
	public				AbstractDataOutboundPort(
		String uri,
		Class<? extends DataRequiredCI.PullCI> implementedPullInterface,
		Class<? extends DataRequiredCI.PushCI> implementedPushInterface,
		ComponentI owner
		) throws Exception
	{
		this(uri, implementedPullInterface, implementedPushInterface, owner,
			 null, null);
	}

	/**
	 * create and initialise a data putbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code implementedPullInterface != null}
	 * pre	{@code implementedPushInterface != null}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(uri)}
	 * post	{@code getImplementedInterface().equals(implementedPullInterface)}
	 * post	{@code getImplementedPushInterface().equals(implementedPushInterface)}
	 * </pre>
	 *
	 * @param implementedPullInterface	pull interface implemented by this port.
	 * @param implementedPushInterface	push interface implemented by this port.
	 * @param owner						component that owns this port.
	 * @throws Exception  				<i>todo.</i>
	 */
	public				AbstractDataOutboundPort(
		Class<? extends DataRequiredCI.PullCI> implementedPullInterface,
		Class<? extends DataRequiredCI.PushCI> implementedPushInterface,
		ComponentI	owner
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
	public Class<? extends DataRequiredCI.PullCI>	getImplementedInterface()
	throws Exception
	{
		// make sure this method is always used to get the pull interface
		return this.getImplementedPullInterface();
	}

	/**
	 * @see fr.sorbonne_u.components.ports.DataOutboundPortI#getImplementedPullInterface()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends DataRequiredCI.PullCI>	getImplementedPullInterface()
	throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return (Class<? extends DataRequiredCI.PullCI>)
										super.getImplementedInterface();
	}

	/**
	 * @see fr.sorbonne_u.components.ports.DataOutboundPortI#getImplementedPushInterface()
	 */
	@Override
	public Class<? extends DataRequiredCI.PushCI> getImplementedPushInterface()
	throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return this.implementedPushInterface;
	}

	// -------------------------------------------------------------------------
	// Connection management
	// -------------------------------------------------------------------------

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
					new PreconditionException("!isDestroyed()");
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("isPublished() && "
													+ "!connected()");
		assert	otherPortURI != null && connector != null :
					new PreconditionException("otherPortURI != null && "
													+ "connector != null");

		this.doMyConnection(otherPortURI, connector);
		this.getConnector().obeyConnection(this, connector);

		AbstractDataOutboundPort.checkImplementationInvariant(this);
		AbstractDataOutboundPort.checkInvariant(this) ;
		AbstractOutboundPort.checkImplementationInvariant(this);
		AbstractOutboundPort.checkInvariant(this) ;
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this) ;
		assert	this.connected() :
					new PostconditionException("this.connected()");
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#obeyConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public void			obeyConnection(String otherPortURI, String ccname)
	throws	Exception
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
		this.obeyConnection(otherPortURI, connector);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#obeyConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			obeyConnection(
		String otherPortURI,
		ConnectorI connector
		) throws Exception
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

		this.setConnector(connector);
		this.setServerPortURI(otherPortURI);
		PortI serverPort =
			AbstractCVM.getFromLocalRegistry(this.getServerPortURI());
		if (serverPort == null && AbstractCVM.isDistributed) {
			this.isRemotelyConnected.set(true);
			serverPort =
				(PortI) AbstractDistributedCVM.getCVM().
							getRemoteReference(this.getServerPortURI());
			this.getConnector().connect((OfferedCI) serverPort, this);
		} else {
			this.isRemotelyConnected.set(false);
		}

		AbstractDataOutboundPort.checkImplementationInvariant(this);
		AbstractDataOutboundPort.checkInvariant(this);
		AbstractOutboundPort.checkImplementationInvariant(this);
		AbstractOutboundPort.checkInvariant(this);
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
					new PreconditionException("!isDestroyed()");
		assert	this.connected() &&
							((ConnectorI)this.getConnector()).connected() :
					new PreconditionException(
							"connected() && "
							+ "((ConnectorI)getConnector()).connected()");

		((AbstractDataConnector)this.getConnector()).obeyDisconnection(this);
		this.doMyDisconnection();

		AbstractDataOutboundPort.checkImplementationInvariant(this);
		AbstractDataOutboundPort.checkInvariant(this);
		AbstractOutboundPort.checkImplementationInvariant(this);
		AbstractOutboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	!this.connected() :
					new PostconditionException("!connected()");
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractOutboundPort#doMyDisconnection()
	 */
	@Override
	protected void		doMyDisconnection() throws Exception
	{
		assert	this.connected() : new PreconditionException("connected()");

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		this.unsetServerPortURI();
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
					new PreconditionException("!isDestroyed()");
		assert	this.connected() :
					new PreconditionException("connected()");

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		this.getConnector().disconnect();
		this.unsetServerPortURI();
		this.unsetConnector();
		this.isRemotelyConnected.set(false);
	}

	// -------------------------------------------------------------------------
	// Plug-in and executor service management
	// -------------------------------------------------------------------------

	/**
	 * return true if this inbound port has an associated plug-in in its owner.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	true if this inbound port has an associated plug-in in its owner.
	 */
	protected boolean	hasPlugin()
	{
		assert	!this.isDestroyed.get() :
					new PreconditionException("!isDestroyed()");

		return this.pluginURI != null;
	}

	/**
	 * return true if this inbound port has an associated executor service in
	 * its owner.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	true if this inbound port has an associated executor service in its owner.
	 */
	protected boolean	hasExecutorService()
	{
		assert	!this.isDestroyed.get() :
					new PreconditionException("!isDestroyed()");

		return this.executorServiceURI != null;
	}

	/**
	 * return the URI of the plug-in that this inbound port must call in its
	 * owner.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code hasPlugin()}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	the URI of the plug-in that this inbound port must call in its owner.
	 */
	protected String	getPluginURI()
	{
		assert	!this.isDestroyed.get() :
					new PreconditionException("!isDestroyed()");
		assert	this.hasPlugin() :
					new PreconditionException(
							"Inbound port " + this.uri + " has no plug-in!");

		return this.pluginURI;
	}

	/**
	 * return the URI of the executor service that this inbound port must use
	 * in its owner.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code hasExecutorService()}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	the URI of the executor service that this inbound port must use in its owner.
	 */
	protected String	getExecutorServiceURI()
	{
		assert	!this.isDestroyed.get() :
					new PreconditionException("!isDestroyed()");
		assert	this.hasExecutorService() :
					new PreconditionException(
							"Inbound port " + this.uri +
												" has no executor service!");

		return this.executorServiceURI;
	}

	/**
	 * return the index of the executor service that this inbound port must
	 * use in its owner.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code hasExecutorService()}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	the index of the executor service that this inbound port must use in its owner.
	 */
	protected int		getExecutorServiceIndex()
	{
		assert	!this.isDestroyed.get() :
					new PreconditionException("!isDestroyed()");
		assert	this.hasExecutorService() :
					new PreconditionException(
							"Inbound port " + this.uri +
												" has no executor service!");

		return this.executorServiceIndex.get();
	}

	/**
	 * update the index of the executor service that this inbound port must
	 * use in its owner after a change in the latter.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code hasExecutorService()}
	 * post	{@code getExecutorServiceIndex() == owner.getExecutorServiceIndex(getExecutorServiceURI())}
	 * </pre>
	 *
	 */
	public void			updateExecutorServiceIndex()
	{
		assert	!this.isDestroyed.get() :
					new PreconditionException("!isDestroyed()");
		assert	this.hasExecutorService() :
					new PreconditionException(
							"Data outbound port " + this.uri +
												" has no executor service!");

		this.executorServiceIndex.set(
					this.getExecutorServiceIndex(this.executorServiceURI));

		assert	this.getExecutorServiceIndex() ==
					this.getExecutorServiceIndex(this.getExecutorServiceURI()) :
						new PostconditionException(
								"executor service with URI " +
								this.getExecutorServiceURI() +
								" does not have index "
								+ this.getExecutorServiceIndex() + " in owner!");
	}

	// -------------------------------------------------------------------------
	// Request handling
	// -------------------------------------------------------------------------

	/**
	 * called by the requiring component in pull mode to trigger the obtaining
	 * of a piece of data from the offering one; this definition imposes the
	 * synchronized nature of the method, as it is called by the owner to get
	 * data from server components.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!isDestroyed()
	 * pre	connected() ;
	 * post	true			// no precondition.
	 * </pre>
	 * 
	 * @throws Exception  <i>todo.</i>
	 * 
	 * @see fr.sorbonne_u.components.interfaces.DataRequiredCI.PullCI#request()
	 */
	@Override
	public DataRequiredCI.DataI	request()
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	this.connected() :
					new PreconditionException("connected()");

		return ((DataRequiredCI.PullCI)this.getConnector()).request();
	}
}
// -----------------------------------------------------------------------------
