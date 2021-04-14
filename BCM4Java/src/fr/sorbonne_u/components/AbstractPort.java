package fr.sorbonne_u.components;

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

import fr.sorbonne_u.components.connectors.ConnectorI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.interfaces.ComponentInterface;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractPort</code> represents the basic properties and
 * behaviours of ports in the component model.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * A port implement an interface on behalf of a component that owns it.  The
 * port is the entity that is seen from other components when connecting to
 * each others using connectors.  Hence, ports are used entry and exit points
 * in components to handle calls or data exchanges among them. 
 * </p>
 * 
 * <p><i>Connection protocol</i></p>
 * 
 * <p>
 * The connection protocol begins with the two port URIs and a connector or
 * a connector class name with which on of the methods
 * <code>doPortConnection</code> defined on components. The component
 * extracts its port object from its recorded ones and calls one of
 * the methods <code>doConnection</code> on it. This method first calls
 * <code>doMyConnection</code>, a protected method that performs the
 * port connection to the connector. Then, it calls the method
 * <code>connect</code> on the connector to connect it with the two
 * ports. After this, the connection from the initiator port to the
 * other one is operational. The initiator port then calls the method
 * <code>obeyConnection</code> that will perform the connection on the
 * other port. When the connection is remote and server-side ports can
 * call back the client side (data and two way ports), the other port
 * creates another connector on the server side to connect back with
 * the initiator side port.
 * </p>
 * <pre>
 *                  Component            Initiator Port       Connector              Other Port
 *                                                         (initiator side)
 *                      |                      |                  |                      |
 *                      |                      |                  |                      |
 * doPortConnection ----|                      |                  |                      |
 *                      |     doConnection     |                  |                      |
 *                      |----------------------|                  |                      |
 *                      |                      |                  |                      |
 *                      |                 |----|                  |                      |
 *                      |  doMyConnection |    |                  |                      |
 *                      |                 |----|                  |                      |
 *                      |                      |     connect      |                      |
 *                      |                      |------------------|                      |
 *                      |                      |                  |                      |
 *                      |                      |  obeyConnection  |                      |
 *                      |                      |------------------|                      |
 *                      |                      |                  |    obeyConnection    |
 *                      |                      |                  | ---------------------|
 *                      |                      |                  |                      |
 *                      |                      |                  |                      |
 *                                ---  if remote connection ---
 *                      |                      |                  |      Connector       |
 *                      |                      |                  |     (other side)     |
 *                      |                      |                  |          |           |
 *                      |                      |                  |          |           |
 *                      |                      |                  |          |  connect  |
 *                      |                      |                  |          |-----------|
 *                      |                      |                  |          |           |
 * </pre>
 * 
 * <p>
 * The disconnection protocol follows similar but mirror steps. The
 * disconnection starts with a call of the method
 * <code>doPortDisconnection</code> the component, which than calls
 * the method <code>doDisconnection</code> on initiator port. The
 * initiator port first calls the method <code>obeyDisconnection</code>
 * on the connection, which forwards it to the other port. The other port
 * calls <code>disconnect</code> on the connector (which is the other
 * side one if the connection is remote) and perform its own
 * disconnection. The initiator then performs its own disconnection,
 * calling <code>disconnect</code> on the connector if the connection
 * is remote.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 *              // TODO: the next can't be verified yet for inbound port
 *              // as their connection status ins not correctly tracked.
 * invariant	{@code !connected() || isPublished()}
 * </pre>
 * 
 * <p>Created on : 2012-01-04</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractPort
extends		UnicastRemoteObject
implements	PortI
{
	// -------------------------------------------------------------------------
	// Port unique identifier management
	// -------------------------------------------------------------------------

	/**
	 * generate a unique identifier for the port which has the interface
	 * name as prefix.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	implementedInterface != null
	 * post	ret != null
	 * </pre>
	 *
	 * @param implementedInterface	interface to be implemented by the port.
	 * @return						a distributed system-wide unique id.
	 */
	public static String	generatePortURI(Class<?> implementedInterface)
	{
		assert	implementedInterface != null :
					new PreconditionException("Implemented interface is null!");

		String ret = implementedInterface.getName() + "-" + generatePortURI();

		assert	ret != null :
					new PostconditionException("Result shouldn't be null!");

		return ret;
	}

	/**
	 * generate a unique identifier for the port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	ret != null
	 * </pre>
	 *
	 * @return	a distributed system-wide unique id.
	 */
	public static String	generatePortURI()
	{
		// see http://www.asciiarmor.com/post/33736615/java-util-uuid-mini-faq
		String ret = java.util.UUID.randomUUID().toString();

		assert	ret != null :
					new PostconditionException("Result shouldn't be null!");

		return ret;
	}

	// -------------------------------------------------------------------------
	// Instance variables and constructors
	// -------------------------------------------------------------------------

	private static final long			serialVersionUID = 1L;
	/** the unique identifier used to publish this entry point.				*/
	protected final String				uri;
	/** the interface implemented by this port.								*/
	protected final Class<? extends ComponentInterface>	implementedInterface;
	/** the component owning this port.										*/
	protected final AbstractComponent	owner;
	/** the port has been locally published.								*/
	protected final AtomicBoolean		isPublished = new AtomicBoolean(false);
	/** the port has been distributedly published.							*/
	protected final AtomicBoolean		isDistributedlyPublished =
													new AtomicBoolean(false);
	/** true when the port has been destroyed, false otherwise.				*/
	protected final AtomicBoolean		isDestroyed = new AtomicBoolean(false);

	/**
	 * check the implementation invariant of the class.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code p != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param p				instance on which the invariant must be checked.
	 * @throws Exception	<i>to do</i>.
	 */
	protected static void	checkImplementationInvariant(AbstractPort p)
	throws Exception
	{
		assert	p != null;

		synchronized (p) {
			assert	p.uri != null :
						new ImplementationInvariantException("uri != null");
			assert	p.implementedInterface != null :
						new ImplementationInvariantException(
								"implementedInterface != null");
			assert	p.implementedInterface.isAssignableFrom(p.getClass()) :
						new ImplementationInvariantException(
								"implementedInterface.isAssignableFrom("
								+ "getClass()");
			assert	p.owner != null :
						new ImplementationInvariantException("owner != null");
			assert	!p.isDestroyed.get() || p.owner.isPortExisting(p.uri) :
						new ImplementationInvariantException(
								"isDestroyed.get() || "
								+ "owner.isPortExisting(uri)");
			assert	!p.isDistributedlyPublished.get() || p.isPublished.get() :
						new ImplementationInvariantException(
								"!isDistributedlyPublished.get() || "
								+ "isPublished.get()");
		}
	}

	/**
	 * check the invariant of the class.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code p != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param p				instance on which the invariant must be checked.
	 * @throws Exception	<i>to do</i>.
	 */
	protected static void	checkInvariant(AbstractPort p) throws Exception
	{
		assert	p != null;

		synchronized(p) {
			// From PortI
			assert	p.isDestroyed() == (p.getOwner() == null) :
						new InvariantException(
								"isDestroyed() == (getOwner() == null)");
			assert	!p.isDestroyed() ||
								p.getOwner().isPortExisting(p.getPortURI()) : 
						new InvariantException(
								"!isDestroyed() || "
								+ "getOwner().isPortExisting(getPortURI())");
			// FIXME: as the connected status of inbound port is not correctly
			// tracked yet, this can only be checked for outbound port.
			//assert	!p.connected() || p.isPublished() :
			//				new InvariantException("!connected() || isPublished()");
			assert	!p.isDistributedlyPublished() || p.isPublished() :
						new InvariantException(
								"!isDistributedlyPublished() || isPublished()");
			assert	p.getOwner().isInterface(p.getImplementedInterface()) :
						new InvariantException(
								"getOwner().isInterface("
										+ "getImplementedInterface())");
		}
	}

	/**
	 * create and initialise a port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && owner != null && implementedInterface != null}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getPortURI().equals(uri)}
	 * pre	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception  			<i>to do.</i>
	 */
	public				AbstractPort(
		String uri,
		Class<? extends ComponentInterface> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super();
		assert	uri != null : new PreconditionException("uri != null");
		assert	owner != null : new PreconditionException("owner != null");
		assert	implementedInterface != null :
					new PreconditionException("implementedInterface != null");
		assert	!owner.isPortExisting(uri) :
					new PreconditionException("owner.isPortExisting(uri)");
		assert	implementedInterface.isAssignableFrom(this.getClass()) :
					new PreconditionException(
							"implementedInterface.isAssignableFrom("
													+ "this.getClass())");

		this.uri = uri;
		this.owner = (AbstractComponent) owner;
		this.implementedInterface = implementedInterface;
		this.addPortToOwner();

		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	!this.isDestroyed() :
					new PostconditionException("!isDestroyed()");
		assert	owner.isPortExisting(uri) :
					new PostconditionException("owner.isPortExisting(uri)");
	}

	/**
	 * create and initialise a port with an automatically generated URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null && implementedInterface != null}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>to do.</i>
	 */
	public				AbstractPort(
		Class<? extends ComponentInterface> implementedInterface,
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
	 * add the port to the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !getOwner().isPortExisting(getPortURI())}
	 * post	{@code getOwner().isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		addPortToOwner() throws Exception
	{
		assert	!this.getOwner().isPortExisting(this.getPortURI()) :
					new PreconditionException(
							"!getOwner().isPortExisting(getPortURI())");

		this.owner.addPort(this);

		assert	this.getOwner().isPortExisting(this.getPortURI()) :
					new PostconditionException(
							"getOwner().isPortExisting(getPortURI())");
	}

	/**
	 * get the index of the executor service with the given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code getOwner().validExecutorServiceURI(uri)}
	 * post	{@code getOwner().validExecutorServiceIndex(ret)}
	 * </pre>
	 *
	 * @param uri	URI of the sought executor service.
	 * @return		the index of the executor service with the given URI.
	 */
	protected int		getExecutorServiceIndex(String uri)
	{
		return this.owner.getExecutorServiceIndex(uri);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#isDestroyed()
	 */
	@Override
	public boolean		isDestroyed() throws Exception
	{
		return this.isDestroyed.get();
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getOwner()
	 */
	@Override
	public ComponentI	getOwner() throws Exception
	{
		if (this.isDestroyed()) {
			return null;
		} else {
			return this.owner ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getImplementedInterface()
	 */
	@Override
	public Class<? extends ComponentInterface>	getImplementedInterface()
	throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return this.implementedInterface;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getPortURI()
	 */
	@Override
	public String		getPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return this.uri ;
	}

	// -------------------------------------------------------------------------
	// Registry publication management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#isPublished()
	 */
	@Override
	public boolean		isPublished() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return this.isPublished.get();
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#isDistributedlyPublished()
	 */
	@Override
	public boolean		isDistributedlyPublished() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");

		return this.isDistributedlyPublished.get();
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#localPublishPort()
	 */
	@Override
	public synchronized void	localPublishPort() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	this.getOwner().isPortExisting(this.getPortURI()) :
					new PreconditionException(
							"getOwner().isPortExisting(getPortURI()) ["
									+ this.getPortURI() + "]") ;
		assert	!this.isPublished() :
					new PreconditionException("!isPublished() ["
												+ this.getPortURI() + "]") ;

		AbstractCVM.localPublishPort(this) ;
		this.isPublished.set(true);
		this.isDistributedlyPublished.set(false);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#publishPort()
	 */
	@Override
	public synchronized void	publishPort() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	this.getOwner().isPortExisting(this.getPortURI()) :
					new PreconditionException(
							"getOwner().isPortExisting(getPortURI()) ["
									+ this.getPortURI() + "]") ;
		assert	!this.isPublished() :
					new PreconditionException("!isPublished() ["
												+ this.getPortURI() + "]") ;

		if (AbstractCVM.isDistributed) {
			AbstractDistributedCVM.publishPort(this) ;
			this.isPublished.set(true);
			this.isDistributedlyPublished.set(true);
		} else {
			this.localPublishPort() ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unpublishPort()
	 */
	@Override
	public synchronized void	unpublishPort() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException(
							"Port with URI " + this.uri + " is destroyed!");
		assert	this.getOwner().isPortExisting(this.getPortURI()) :
					new PreconditionException(
							"getOwner().isPortExisting(getPortURI()) ["
									+ this.getPortURI() + "]") ;
		assert	this.isPublished() :
					new PreconditionException("isPublished() ["
												+ this.getPortURI() + "]") ;
		// FIXME: connection status for inbound port is not yet correctly
		// tracked. Must be fixed before testing the next assertion.
		// assert	!this.connected() ;

		if (this.isDistributedlyPublished()) {
			AbstractDistributedCVM.unpublishPort(this) ;
		} else {
			AbstractCVM.localUnpublishPort(this) ;
		}
		this.isPublished.set(false);
		this.isDistributedlyPublished.set(false);
	}

	// -------------------------------------------------------------------------
	// Life-cycle management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#destroyPort()
	 */
	@Override
	public synchronized void	destroyPort() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	this.getOwner().isPortExisting(this.getPortURI()) :
					new PreconditionException(
							"getOwner().isPortExisting(getPortURI()) ["
									+ this.getPortURI() + "]");
		assert	!this.isPublished() :
					new PreconditionException("!isPublished() ["
											+ this.getPortURI() + "]");
		// FIXME: connection status for inbound port is not yet correctly
		// tracked. Must be fixed before testing the next assertion.
		// assert	!this.connected() ;

		this.owner.removePort(this.getPortURI());
		this.isDestroyed.set(true);

		assert	this.isDestroyed() :
					new PostconditionException("isDestroyed()");
		assert	!this.owner.isPortExisting(this.uri) :
					new PostconditionException(
							"getOwner()@pre.isPortExisting(" + 
							"getPortURI()@pre) with uri = [" +
							this.uri + "]");
	}

	// -------------------------------------------------------------------------
	// Connection management
	// -------------------------------------------------------------------------

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

		// Do nothing, by default.
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
		assert	serverPortURI != null :
					new PreconditionException("serverPortURI can't be null!");

		// Do nothing, by default.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unsetClientPortURI()
	 */
	@Override
	public void			unsetClientPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		// Do nothing, by default.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unsetServerPortURI()
	 */
	@Override
	public void			unsetServerPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		// Do nothing, by default.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getClientPortURI()
	 */
	@Override
	public String		getClientPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		return null;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getServerPortURI()
	 */
	@Override
	public String		getServerPortURI() throws Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");

		return null;
	}

	/**
	 * connect this port, knowing that this port initiated the connection.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code isPublished()}
	 * pre	{@code !connected()}
	 * pre	{@code otherPortURI != null && connector != null}
	 * post	{@code connected()}
	 * </pre>
	 *
	 * @param otherPortURI	URI of the other port to be connected with this one.
	 * @param connector		connector to be used to connect with the other port.
	 * @throws Exception	<i>to do</i>.
	 */
	protected abstract void	doMyConnection(
		String otherPortURI,
		ConnectorI connector
		) throws Exception ;

	/**
	 * disconnect this port, knowing that this port initiated the
	 * disconnection.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code connected()}
	 * post	{@code !connected()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	protected abstract void	doMyDisconnection() throws Exception ;

	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	/**
	 * get the plug-in with the given URI from the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code pluginURI != null && pluginURI.length() > 0}
	 * pre	{@code getOwner().isInstalled(pluginURI);}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param pluginURI	URI of the sought plug-in.
	 * @return			the reference to the plug-in in the owner component.
	 */
	protected PluginI	getOwnerPlugin(String pluginURI)
	{
		try {
			assert	pluginURI != null && pluginURI.length() > 0;
			assert	this.getOwner().isInstalled(pluginURI);
			return ((AbstractComponent)this.getOwner()).getPlugin(pluginURI);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
// -----------------------------------------------------------------------------
