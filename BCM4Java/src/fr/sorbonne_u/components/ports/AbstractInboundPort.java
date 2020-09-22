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

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.connectors.ConnectorI;
import fr.sorbonne_u.components.exceptions.ConnectionException;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

import java.util.concurrent.atomic.AtomicInteger;

import fr.sorbonne_u.components.AbstractPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractInboundPort</code> partially implements an inbound
 * port which implements the offered interface of the provider component so
 * that the provider can be called through this port.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * A concrete port class must implement the offered interface of the component
 * with methods that call the corresponding implementation services of their
 * owner component, paying attention to the discipline (synchronized, ...)
 * with which these calls must be made for the given implementation of the
 * component.
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
public abstract class	AbstractInboundPort
extends		AbstractPort
implements	InboundPortI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long		serialVersionUID = 1L;
	/** URI of the plug-in to be called in the owner or null if none.		*/
	protected final String			pluginURI;
	/** URI of the executor service to be used to execute the service on the
	 *  owner or null if none.												*/
	protected final String			executorServiceURI;
	/** index of the executor service in the owner (beware that this can
	 *  change over the execution of the owner if executor services are shut
	 *  down dynamically).													*/
	protected final AtomicInteger	executorServiceIndex =
													new AtomicInteger(-1);

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * check the implementation invariant of the class on an instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code p != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param p				object on which the invariant must be checked.
	 * @throws Exception	<i>to do</i>.
	 */
	protected static void	checkImplementationInvariant(AbstractInboundPort p)
	throws Exception
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
		}
	}

	/**
	 * check the invariant of the class on an instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code p != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param p				object on which the invariant must be checked.
	 * @throws Exception	<i>to do</i>.
	 */
	protected static void	checkInvariant(AbstractInboundPort p)
	throws Exception
	{
		assert	p != null ;

		synchronized (p) {
			// From InboundPortI
			assert	p.getOwner().isOfferedInterface(p.getImplementedInterface()) :
						new InvariantException(p.getImplementedInterface() +
								" must be declared as an offered component "
								+ "interface by its owner " + p.getOwner()
								+ "!") ;
		}
	}

	/**
	 * create and initialise inbound ports, with a given URI and given plug-in
	 * and executor service URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && owner != null && implementedInterface != null}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>todo.</i>
	 */
	public				AbstractInboundPort(
		String uri,
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		super(uri, implementedInterface, owner) ;

		assert	pluginURI == null || owner.isInstalled(pluginURI) :
					new PreconditionException(
							"owner component does not have an installed "
							+ "plug-in with URI: " + pluginURI);
		assert	executorServiceURI == null ||
						owner.validExecutorServiceURI(executorServiceURI) :
					new PreconditionException(
							"owner component does not have an executor "
							+ "service with URI: " + executorServiceURI);

		this.pluginURI = pluginURI;
		this.executorServiceURI = executorServiceURI;
		if (executorServiceURI != null) {
			this.executorServiceIndex.set(
							this.getExecutorServiceIndex(executorServiceURI));
		}

		AbstractInboundPort.checkImplementationInvariant(this);
		AbstractInboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
	}

	/**
	 * create and initialise inbound ports, with a given plug-in and executor
	 * service URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null && implementedInterface != null}
	 * pre	{@code implementedInterface.isAssignableFrom(this.getClass())}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>todo.</i>
	 */
	public				AbstractInboundPort(
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		this(AbstractPort.generatePortURI(implementedInterface),
			 implementedInterface, owner, pluginURI, executorServiceURI);
	}

	/**
	 * create and initialise inbound ports, with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && owner != null && implementedInterface != null}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>todo.</i>
	 */
	public				AbstractInboundPort(
		String uri,
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		this(uri, implementedInterface, owner, null, null) ;
	}

	/**
	 * create and initialise inbound ports with an automatically generated URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null && implementedInterface != null}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>todo.</i>
	 */
	public				AbstractInboundPort(
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		this(AbstractPort.generatePortURI(implementedInterface),
			 implementedInterface, owner) ;
	}

	// -------------------------------------------------------------------------
	// Self-properties management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractPort#getImplementedInterface()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends OfferedCI>	getImplementedInterface()
	throws Exception
	{
		return (Class<? extends OfferedCI>) super.getImplementedInterface();
	}
	// -------------------------------------------------------------------------
	// Connection management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#connected()
	 */
	@Override
	public boolean		connected() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		// FIXME: always return true, as an inbound port do not know
		// if it is connected or not.
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#isRemotelyConnected()
	 */
	@Override
	public boolean		isRemotelyConnected() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		// FIXME: always return true, as an inbound port do not know
		// if it is connected or not.
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#setClientPortURI(java.lang.String)
	 */
	@Override
	public void			setClientPortURI(String clientPortURI)
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	clientPortURI != null :
					new PreconditionException("clientPortURI can't be null!");

		// Inbound ports do not know their client port, as they may have
		// many clients.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unsetClientPortURI()
	 */
	@Override
	public void			unsetClientPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		// Inbound ports do not know their client port, as they may have
		// many clients.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getClientPortURI()
	 */
	@Override
	public String		getClientPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		throw new ConnectionException(
						"Can't get the client port URI of an inbound port!");
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#setServerPortURI(java.lang.String)
	 */
	@Override
	public void			setServerPortURI(String serverPortURI)
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	this.getPortURI().equals(serverPortURI);

		// Do nothing, this is their own port URI.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unsetServerPortURI()
	 */
	@Override
	public void			unsetServerPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		throw new ConnectionException("Can't unset the server port URI "
													+ "of an inbound port!");
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getServerPortURI()
	 */
	@Override
	public String		getServerPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		return this.getPortURI();
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#doConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public void			doConnection(String otherPortURI, String ccname)
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	this.isPublished() :
					new PreconditionException("isPublished()");
		assert	otherPortURI != null && ccname != null :
					new PreconditionException("otherPortURI != null && "
														+ "ccname != null");

		throw new ConnectionException(
						"Attempt to connect a server component port "
						+ this.getPortURI()
						+ " to a client component port " + otherPortURI
						+ " from the server side; must be done from"
						+ " the client side!") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#doConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			doConnection(String otherPortURI, ConnectorI connector)
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	this.isPublished() :
					new PreconditionException("isPublished()");
		assert	otherPortURI != null && connector != null :
					new PreconditionException("otherPortURI != null && "
													+ "connector != null");

		throw new ConnectionException(
						"Attempt to connect a server component port "
						+ this.getPortURI()
						+ " to a client component port " + otherPortURI
						+ " from the server side; must be done from"
						+ " the client side!") ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPort#doMyConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	protected void		doMyConnection(
		String otherPortURI,
		ConnectorI connector
		) throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		// Nothing to be done.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#obeyConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public void			obeyConnection(String otherPortURI, String ccname)
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	otherPortURI != null && ccname != null :
					new PreconditionException("otherPortURI != null && "
														+ "ccname != null");

		// Not needed currently!
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#obeyConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			obeyConnection(String otherPortURI, ConnectorI connector)
	throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	otherPortURI != null && connector != null :
					new PreconditionException("otherPortURI != null && "
													+ "connector != null");

		// Not needed currently!
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#doDisconnection()
	 */
	@Override
	public void			doDisconnection() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		throw new ConnectionException(
					"Attempt to disconnect an inbound port "
					+ this.getPortURI() + "; must be done from"
					+ " the client side!") ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPort#doMyDisconnection()
	 */
	@Override
	protected void		doMyDisconnection() throws Exception
	{
		// Nothing to be done.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#obeyDisconnection()
	 */
	@Override
	public void			obeyDisconnection() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		// As inbound ports do not hold data about their clients, nothing needs
		// to be done when disconnecting.
		throw new ConnectionException(
					"Attempt to disconnect an inbound port "
					+ this.getPortURI() + "; should be done from"
					+ " the client side!") ;
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
							"Inbound port " + this.uri +
												" has no executor service!");

		this.executorServiceIndex.set(
							this.getExecutorServiceIndex(executorServiceURI));

		assert	this.getExecutorServiceIndex() ==
							this.getExecutorServiceIndex(
												this.getExecutorServiceURI()) :
					new PostconditionException(
							"executor service with URI " +
							this.getExecutorServiceURI() +
							" does not have index "
							+ this.getExecutorServiceIndex() + " in owner!");
	}
}
// -----------------------------------------------------------------------------
