package fr.sorbonne_u.components.ports;

//Copyright Jacques Malenfant, Sorbonne Universite.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.connectors.ConnectorI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.exceptions.InvariantException;
import fr.sorbonne_u.components.exceptions.PostconditionException;
import fr.sorbonne_u.components.exceptions.PreconditionException;
import fr.sorbonne_u.components.interfaces.ComponentServiceI;
import java.rmi.server.UnicastRemoteObject;

//-----------------------------------------------------------------------------
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
 * invariant		this.getImplementedInterface().isAssignableFrom(this.getClass())
 * invariant		p.connected() implies (p.isRemotelyConnected() implies p.isDistributedlyPublished())
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
	// ------------------------------------------------------------------------
	// Port unique identifier management
	// ------------------------------------------------------------------------

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
	public static String		generatePortURI(Class<?> implementedInterface)
	{
		assert	implementedInterface != null :
					new PreconditionException("Implemented interface is null!") ;

		String ret = implementedInterface.getName() + "-" + generatePortURI() ;

		assert	ret != null :
					new PostconditionException("Result shouldn't be null!") ;

		return ret ;
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
	public static String		generatePortURI()
	{
		// see http://www.asciiarmor.com/post/33736615/java-util-uuid-mini-faq
		String ret = java.util.UUID.randomUUID().toString() ;

		assert	ret != null :
					new PostconditionException("Result shouldn't be null!") ;

		return ret ;
	}

	// ------------------------------------------------------------------------
	// Instance variables and constructors
	// ------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L ;
	/** the unique identifier used to publish this entry point.			*/
	protected final String		uri ;
	/** the interface implemented by this port.							*/
	protected final Class<?>		implementedInterface ;
	/** the component owning this port.									*/
	protected final ComponentI	owner ;
	/** the port has been locally published.								*/
	protected boolean			isPublished = false ;
	/** the port has been distributedly published.						*/
	protected boolean			isDistributedlyPublished = false ;

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
	 * @param p			object on which the invariant must be checked.
	 * @throws Exception	<i>todo.</i>
	 */
	protected static void	checkInvariant(AbstractPort p) throws Exception
	{
		assert	p != null ;

		// From PortI
		assert	p.getPortURI() != null :
					new InvariantException("this.getPortURI() != null") ;
		assert	p.getOwner() != null :
					new InvariantException("this.getOwner() != null") ;
		assert	p.getImplementedInterface() != null :
					new InvariantException(
							"this.getImplementedInterface() != null") ;
		assert	ComponentServiceI.class.
							isAssignableFrom(p.getImplementedInterface()) :
					new InvariantException(
						"ComponentServiceI.class.isAssignableFrom("
								+ "this.getImplementedInterface()) ["
									+ p.getImplementedInterface() + "]") ;
		assert	p.getOwner().isInterface(p.getImplementedInterface()) :
					new InvariantException(
						"this.getOwner().isInterface("
							+ "this.getImplementedInterface())"
								+ " [" + p.getImplementedInterface() + "]") ;
		assert	!p.isDistributedlyPublished() || p.isPublished() :
					new InvariantException(
							"this.isDistributedlyPublished() => "
											+ "this.isPublished()") ;

		// From AbstractPort
		assert	p.getImplementedInterface().isAssignableFrom(p.getClass()) :
					new InvariantException(
							"p.getImplementedInterface()."
									+ "isAssignableFrom(p.getClass())") ;
	}

	/**
	 * create and initialise a port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null and owner != null and implementedInterface != null
	 * pre	ComponentImplementedI.class.isAssignableFrom(implementedInterface)
	 * pre	implementedInterface.isAssignableFrom(this.getClass())
	 * post	this.getPortURI().equals(uri)
	 * post	this.getOwner().equals(owner)
	 * post this.getImplementedInterface().equals(implementedInterface)
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception  			<i>to do.</i>
	 */
	public				AbstractPort(
		String		uri,
		Class<?>		implementedInterface,
		ComponentI	owner
		) throws Exception
	{
		super() ;
		assert	uri != null : new PreconditionException("uri != null") ;
		assert	owner != null : new PreconditionException("owner != null") ;
		assert	implementedInterface != null :
					new PreconditionException("implementedInterface != null") ;
		assert	implementedInterface.isAssignableFrom(this.getClass()) :
					new PreconditionException(
							"implementedInterface.isAssignableFrom("
													+ "this.getClass())") ;
		assert	ComponentServiceI.class.
									isAssignableFrom(implementedInterface) :
					new PreconditionException(
							"ComponentServiceI.class."
								+ "isAssignableFrom(implementedInterface)") ;

		this.uri = uri ;
		this.owner = owner ;
		this.implementedInterface = implementedInterface ;

		AbstractPort.checkInvariant(this) ;
	}

	/**
	 * create and initialise a port with an automatically generated URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner != null and implementedInterface != null
	 * pre	ComponentImplementedI.class.isAssignableFrom(implementedInterface)
	 * pre	implementedInterface.isAssignableFrom(this.getClass())
	 * post	this.getOwner().equals(owner)
	 * post this.getImplementedInterface().equals(implementedInterface)
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>to do.</i>
	 */
	public				AbstractPort(
		Class<?> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		this(AbstractPort.generatePortURI(implementedInterface),
			 implementedInterface, owner) ;
	}

	// ------------------------------------------------------------------------
	// Self-properties management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getOwner()
	 */
	@Override
	public ComponentI	getOwner() throws Exception
	{
		return this.owner ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getImplementedInterface()
	 */
	@Override
	public Class<?>		getImplementedInterface() throws Exception
	{
		return this.implementedInterface;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getPortURI()
	 */
	@Override
	public String		getPortURI() throws Exception
	{
		return this.uri ;
	}

	// ------------------------------------------------------------------------
	// Registry management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#isPublished()
	 */
	@Override
	public boolean		isPublished()
	{
		return this.isPublished;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#isDistributedlyPublished()
	 */
	@Override
	public boolean		isDistributedlyPublished()
	{
		return this.isDistributedlyPublished;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#localPublishPort()
	 */
	@Override
	public void			localPublishPort() throws Exception
	{
		assert	this.getOwner().isPortExisting(this.getPortURI()) :
					new PreconditionException(
							"this.getOwner().isPortExisting("
												+ "this.getPortURI()) ["
								+ this.getPortURI() + "]") ;
		assert	!this.isPublished() :
					new PreconditionException("!this.isPublished() ["
												+ this.getPortURI() + "]") ;
		assert	!this.isDistributedlyPublished() :
					new PreconditionException(
							"!this.isDistributedlyPublished() ["
												+ this.getPortURI() + "]") ;

		AbstractCVM.localPublishPort(this) ;
		this.isPublished = true ;
		this.isDistributedlyPublished = false ;

		AbstractPort.checkInvariant(this) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#publishPort()
	 */
	@Override
	public void			publishPort() throws Exception
	{
		assert	this.getOwner().isPortExisting(this.getPortURI()) :
					new PreconditionException(
							"this.getOwner().isPortExisting("
												+ "this.getPortURI()) ["
								+ this.getPortURI() + "]") ;
		assert	!this.isPublished() :
					new PreconditionException("!this.isPublished() ["
												+ this.getPortURI() + "]") ;
		assert	!this.isDistributedlyPublished() :
					new PreconditionException(
							"!this.isDistributedlyPublished() ["
												+ this.getPortURI() + "]") ;

		if (AbstractCVM.isDistributed) {
			AbstractDistributedCVM.publishPort(this) ;
			this.isPublished = true ;
			this.isDistributedlyPublished = true ;
		} else {
			this.localPublishPort() ;
		}

		AbstractPort.checkInvariant(this) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unpublishPort()
	 */
	@Override
	public void			unpublishPort() throws Exception
	{
		assert	this.getOwner().isPortExisting(this.getPortURI()) :
					new PreconditionException(
							"this.getOwner().isPortExisting("
												+ "this.getPortURI()) ["
								+ this.getPortURI() + "]") ;
		assert	this.isPublished() :
					new PreconditionException("this.isPublished() ["
												+ this.getPortURI() + "]") ;
		// FIXME: connection status for inbound port is not yet correctly
		// tracked. Must be fixed before testing the next assertion.
		// assert	!this.connected() ;

		if (this.isDistributedlyPublished) {
			AbstractDistributedCVM.unpublishPort(this) ;
		} else {
			AbstractCVM.localUnpublishPort(this) ;
		}
		this.isPublished = false ;
		this.isDistributedlyPublished = false ;

		AbstractPort.checkInvariant(this) ;
	}

	// ------------------------------------------------------------------------
	// Life-cycle management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#destroyPort()
	 */
	@Override
	public void			destroyPort() throws Exception
	{
		assert	this.getOwner().isPortExisting(this.getPortURI()) :
					new PreconditionException(
							"this.getOwner().isPortExisting("
												+ "this.getPortURI()) ["
								+ this.getPortURI() + "]") ;
		assert	!this.isPublished() :
					new PreconditionException("!this.isPublished() ["
											+ this.getPortURI() + "]") ;
		// FIXME: connection status for inbound port is not yet correctly
		// tracked. Must be fixed before testing the next assertion.
		// assert	!this.connected() ;

		this.owner.removePort(this.getPortURI()) ;

		assert	!this.getOwner().isPortExisting(this.getPortURI()) :
					new PostconditionException(
							"this.getOwner().isPortExisting(" + 
												"this.getPortURI()) ["
								+ this.getPortURI() + "]") ;
	}

	// ------------------------------------------------------------------------
	// Connection management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#setClientPortURI(java.lang.String)
	 */
	@Override
	public void			setClientPortURI(String clientPortURI)
	throws	Exception
	{
		assert	clientPortURI != null ;

		// Do nothing, by default.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#setServerPortURI(java.lang.String)
	 */
	@Override
	public void			setServerPortURI(String serverPortURI)
	throws	Exception
	{
		assert	serverPortURI != null ;

		// Do nothing, by default.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unsetClientPortURI()
	 */
	@Override
	public void			unsetClientPortURI() throws Exception
	{
		// Do nothing, by default.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unsetServerPortURI()
	 */
	@Override
	public void			unsetServerPortURI() throws Exception
	{
		// Do nothing, by default.
	}

	/**
	 * connect this port, knowing that this port initiated the connection.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isPublished() and !this.connected()
	 * pre	otherPortURI != null and connector != null
	 * post	this.connected()
	 * </pre>
	 *
	 * @param otherPortURI	URI of the other port to be connected with this one.
	 * @param connector		connector to be used to connect with the other port.
	 * @throws Exception		<i>todo.</i>
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
	 * pre	this.connected()
	 * post	!this.connected()
	 * </pre>
	 *
	 * @throws Exception	<i>todo.</i>
	 */
	protected abstract void	doMyDisconnection() throws Exception ;
}
//-----------------------------------------------------------------------------
