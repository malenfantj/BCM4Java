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

import java.lang.reflect.Constructor;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.connectors.AbstractDataConnector;
import fr.sorbonne_u.components.connectors.ConnectorI;
import fr.sorbonne_u.components.connectors.DataConnectorI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.exceptions.InvariantException;
import fr.sorbonne_u.components.exceptions.PostconditionException;
import fr.sorbonne_u.components.exceptions.PreconditionException;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.interfaces.DataOfferedI;
import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;

//-----------------------------------------------------------------------------
/**
 * The class <code>AbstractDataInboundPort</code> partially implements an
 * inbound port for data exchanging components.
 *
 * <p><strong>Description</strong></p>
 * 
 * Data exchanging components focus their interaction on the exchange of
 * pieces of data rather than calling each others services.  Hence, the
 * required and offered interfaces merely implements a simple protocol in
 * terms of methods used to pass data from the provider to the clients.
 * But data exchanges can be done in two modes: pull (the primary one) and push.
 * For inbound port, representing interfaces through which a provider is called,
 * the port implements the offered pull interface, while the connector
 * implements the offered push interface through which data can be pushed
 * from the provider towards the client.
 * 
 * A concrete inbound connector must therefore implement the method
 * <code>get</code> which will ask the owner component for a piece of data
 * and provide as result.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		this.connectors != null
 * invariant		DataOfferedI.PullI.class.isAssignableFrom(this.implementedInterface)
 * invariant		DataOfferedI.PushI.class.isAssignableFrom(this.implementedPushInterface)
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
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Port instance variables and constructors
	// ------------------------------------------------------------------------

	/** push interface implemented by this port, to send data to the client. */
	protected final Class<?>		implementedPushInterface ;
	/** URI of the client port to which this port is connected.			*/
	protected String				clientPortURI ;
	/** connectors of this port towards the client components.				*/
	protected DataOfferedI.PushI	connector ;
	/** when connected, true if the connection is remote and false
	 *  otherwise.														*/
	protected boolean			isRemotelyConnected ;
	/** Lock object used to wait for connection.							*/
	protected final Object		lock ;

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
	protected static void	checkInvariant(AbstractDataInboundPort p)
	throws Exception
	{
		assert	p != null ;

		assert	!p.connected() || p.getServerPortURI().equals(p.getPortURI()) :
					new InvariantException("!p.connected() || "
							+ "p.getServerPortURI().equals(p.getPortURI())") ;
		assert	!p.connected() || p.getClientPortURI() != null :
					new InvariantException("!p.connected() || "
										+ "p.getClientPortURI() != null") ;
		assert	!p.connected() || 
					p.getImplementedInterface().equals(
										p.getImplementedPullInterface()) :
					new InvariantException(
							"!p.connected() || " + 
									"p.getImplementedInterface().equals(" + 
										"p.getImplementedPullInterface())") ;
	}

	public				AbstractDataInboundPort(
		Class<?>	implementedInterface,
		ComponentI	owner
		) throws Exception
	{
		 // avoids missing super constructor error
		super(implementedInterface, owner) ;
		throw new RuntimeException("AbstractDataInboundPort: must use the " +
				"three or four parameters version of the constructor.") ;
	}

	public				AbstractDataInboundPort(
		String		uri,
		Class<?>		implementedInterface,
		ComponentI	owner
		) throws Exception
	{
		 // avoids missing super constructor error
		super(uri, implementedInterface, owner);
		throw new RuntimeException("AbstractDataInboundPort: must use the " +
				"three or four parameters version of the constructor.") ;
	}

	/**
	 * create and initialise data inbound ports, with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null and owner != null
	 * pre	DataOfferedI.PullI.class.isAssignableFrom(implementedPullInterface)
	 * pre	DataOfferedI.PushI.class.isAssignableFrom(implementedPushInterface)
	 * post	this.getPortURI().equals(uri)
	 * post	this.getOwner().equals(owner)
	 * post	this.getImplementedInterface().equals(implementedPullInterface)
	 * post	this.getImplementedPushInterface().equals(implementedPushInterface)
	 * </pre>
	 *
	 * @param uri						unique identifier of the port.
	 * @param implementedPullInterface	pull interface implemented by this port.
	 * @param implementedPushInterface	push interface implemented by this port.
	 * @param owner						component that owns this port.
	 * @throws Exception 				<i>to do.</i>
	 */
	public				AbstractDataInboundPort(
		String		uri,
		Class<?>		implementedPullInterface,
		Class<?>		implementedPushInterface,
		ComponentI	owner
		) throws Exception
	{
		super(uri, implementedPullInterface, owner) ;
		// the implemented interfaces are coming from a data offered interface
		assert DataOfferedI.PullI.class.
									isAssignableFrom(implementedPullInterface) :
					new PreconditionException("DataOfferedI.PullI.class.\n"
								+ "isAssignableFrom(implementedPullInterface)") ;
		assert DataOfferedI.PushI.class.
									isAssignableFrom(implementedPushInterface) :
					new PreconditionException("DataOfferedI.PushI.class.\n"
								+ "isAssignableFrom(implementedPushInterface)") ;

		this.implementedPushInterface = implementedPushInterface ;
		this.lock = new Object() ;

		AbstractDataInboundPort.checkInvariant(this) ;
		assert	this.getImplementedPullInterface().
											equals(implementedPullInterface) :
					new PostconditionException(
							"this.getImplementedPullInterface()." + 
										"equals(implementedPullInterface)") ;
	}

	/**
	 * create and initialise a data inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner != null
	 * pre	DataOfferedI.PullI.class.isAssignableFrom(implementedPullInterface)
	 * pre	DataOfferedI.PushI.class.isAssignableFrom(implementedPushInterface)
	 * post	this.getOwner().equals(owner)
	 * post	this.getImplementedInterface().equals(implementedPullInterface)
	 * post	this.getImplementedPushInterface().equals(implementedPushInterface)
	 * </pre>
	 *
	 * @param implementedPullInterface	pull interface implemented by this port.
	 * @param implementedPushInterface	push interface implemented by this port.
	 * @param owner						component that owns this port.
	 * @throws Exception 				<i>to do.</i>
	 */
	public				AbstractDataInboundPort(
		Class<?>		implementedPullInterface,
		Class<?>		implementedPushInterface,
		ComponentI	owner
		) throws Exception
	{
		this(AbstractPort.generatePortURI(implementedPullInterface),
			 implementedPullInterface, implementedPushInterface, owner) ;
	}

	// ------------------------------------------------------------------------
	// Self-properties management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractPort#getImplementedInterface()
	 */
	@Override
	public Class<?>		getImplementedInterface() throws Exception
	{
		// make sure this method is always used to get the pull interface
		return this.getImplementedPullInterface() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.DataInboundPortI#getImplementedPullInterface()
	 */
	@Override
	public Class<?>		getImplementedPullInterface() throws Exception
	{
		// the pull interface is stored as the original implemented interface.
		return super.getImplementedInterface() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.DataInboundPortI#getImplementedPushInterface()
	 */
	@Override
	public Class<?>		getImplementedPushInterface() throws Exception
	{
		return this.implementedPushInterface ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#setClientPortURI(java.lang.String)
	 */
	@Override
	public void			setClientPortURI(String clientPortURI)
	throws Exception
	{
		assert	clientPortURI != null ;

		this.clientPortURI = clientPortURI ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unsetClientPortURI()
	 */
	@Override
	public void			unsetClientPortURI() throws Exception
	{
		this.clientPortURI = null ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#getClientPortURI()
	 */
	@Override
	public String		getClientPortURI() throws Exception
	{
		return this.clientPortURI ;
	}

	// ------------------------------------------------------------------------
	// Registry management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unpublishPort()
	 */
	@Override
	public void			unpublishPort() throws Exception
	{
		assert	!this.connected() :
					new PreconditionException("!this.connected()") ;

		super.unpublishPort() ;
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
		// until the AbstractInboundPort can know if they are connected.
		assert	!this.connected() :
					new PreconditionException("!this.connected()") ;

		super.destroyPort() ;
	}

	// ------------------------------------------------------------------------
	// Connection management
	// ------------------------------------------------------------------------

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
		assert	c != null : new PreconditionException("c != null") ;

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CONNECTING)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.CONNECTING,
				"AbstractDataInboundPort setting connector " + c.toString()) ;
		}

		this.connector = (DataOfferedI.PushI) c ;
		synchronized (this.lock) {
			this.lock.notifyAll() ;
		}

		assert	this.getConnector() == c :
					new PostconditionException("this.getConnector() == c") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.DataInboundPortI#unsetConnector()
	 */
	@Override
	public void			unsetConnector() throws Exception
	{
		this.connector = null ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.DataInboundPortI#getConnector()
	 */
	@Override
	public DataConnectorI	getConnector() throws Exception
	{
		return (DataConnectorI) this.connector ;
	}

	/**
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.ports.DataInboundPortI#awaitConnection()
	 */
	@Override
	public void			awaitConnection() throws Exception
	{
		synchronized (this.lock) {
			if (!this.connected()) { this.lock.wait() ; }
		}
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#connected()
	 */
	@Override
	public boolean		connected() throws Exception
	{
		return this.connector != null ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#isRemotelyConnected()
	 */
	@Override
	public boolean		isRemotelyConnected() throws Exception
	{
		assert	this.connected() :
					new PreconditionException("this.connected()") ;

		return this.isRemotelyConnected;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#doConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public void			doConnection(String otherPortURI, String ccname)
	throws	Exception
	{
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("this.isPublished() && "
													+ "!this.connected()") ;
		assert	otherPortURI != null && ccname != null :
					new PreconditionException("otherPortURI != null && "
													+ "ccname != null") ;

		Class<?> cc = Class.forName(ccname) ;
		Constructor<?> c = cc.getConstructor(new Class<?>[]{}) ;
		ConnectorI connector = (ConnectorI) c.newInstance() ;
		this.doConnection(otherPortURI, connector) ;

		AbstractDataInboundPort.checkInvariant(this) ;
		assert	this.connected() :
					new PostconditionException("this.connected()") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#doConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			doConnection(String otherPortURI, ConnectorI connector)
	throws	Exception
	{
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("this.isPublished() && "
													+ "!this.connected()") ;
		assert	otherPortURI != null && connector != null :
					new PreconditionException("otherPortURI != null && "
													+ "connector != null") ;

		this.doMyConnection(otherPortURI, connector) ;
		// When the connection is remote, this call will serialise the
		// connector object and its deserialisation in the other JVM
		// hence duplicated. When is its local,  no duplication occurs.
		((AbstractDataConnector)this.connector).obeyConnection(this, connector) ;

		AbstractDataInboundPort.checkInvariant(this) ;
		assert	this.connected() :
					new PostconditionException("this.connected()") ;
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
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("this.isPublished() && "
													+ "!this.connected()") ;
		assert	otherPortURI != null && connector != null :
					new PreconditionException("otherPortURI != null && "
													+ "connector != null") ;

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		this.setConnector((DataConnectorI) connector) ;
		this.setClientPortURI(otherPortURI) ;
		PortI clientPort =
			AbstractCVM.getFromLocalRegistry(this.getClientPortURI()) ;
		if (clientPort == null) {
			this.isRemotelyConnected = true ;
			clientPort = (PortI) AbstractDistributedCVM.getCVM().
							getRemoteReference(this.getClientPortURI()) ;
		} else {
			this.isRemotelyConnected = false ;
		}
		assert	clientPort != null :
					new RuntimeException("Unknown port URI: " +
											this.getClientPortURI()) ;

		this.getConnector().connect((OfferedI)this, (RequiredI)clientPort) ;

		assert	this.connected() :
					new PostconditionException("this.connected()") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#obeyConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public void			obeyConnection(String otherPortURI, String ccname)
	throws	Exception
	{
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

		AbstractDataInboundPort.checkInvariant(this) ;
		assert	this.connected() :
					new PostconditionException("this.connected()") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#obeyConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			obeyConnection(String otherPortURI, ConnectorI connector)
	throws	Exception
	{
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("this.isPublished() && "
													+ "!this.connected()") ;
		assert	otherPortURI != null && connector != null :
					new PreconditionException("otherPortURI != null && "
													+ "connector != null") ;

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		this.setConnector((DataConnectorI)connector) ;
		this.setClientPortURI(otherPortURI) ;
		PortI clientPort =
				AbstractCVM.getFromLocalRegistry(this.getClientPortURI()) ;
		if (clientPort == null) {
			this.isRemotelyConnected = true ;
			clientPort = (PortI) AbstractDistributedCVM.getCVM().
								getRemoteReference(this.getClientPortURI()) ;
			((DataConnectorI)this.getConnector()).
							connect((OfferedI)this, (RequiredI)clientPort) ;
		} else {
			this.isRemotelyConnected = false ;
		}

		AbstractDataInboundPort.checkInvariant(this) ;
		assert	this.connected() :
					new PostconditionException("this.connected()") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#doDisconnection()
	 */
	@Override
	public void			doDisconnection() throws Exception
	{
		assert	this.connected() :
					new PreconditionException("this.connected()") ;

		((AbstractDataConnector)this.connector).obeyDisconnection(this) ;
		this.doMyDisconnection() ;

		AbstractDataInboundPort.checkInvariant(this) ;
		assert	!this.connected() :
					new PostconditionException("!this.connected()") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#doMyDisconnection()
	 */
	@Override
	protected void		doMyDisconnection() throws Exception
	{
		if (this.isRemotelyConnected()) {
			this.getConnector().disconnect() ;
		}
		this.unsetClientPortURI() ;
		this.connector = null ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#obeyDisconnection()
	 */
	@Override
	public void			obeyDisconnection() throws Exception
	{
		assert	this.connected() :
					new PreconditionException("this.connected()") ;

		((DataConnectorI)this.getConnector()).disconnect() ;
		this.unsetClientPortURI() ;
		this.connector = null ;

		AbstractDataInboundPort.checkInvariant(this) ;
		assert	!this.connected() :
					new PostconditionException("!this.connected()") ;
	}

	// ------------------------------------------------------------------------
	// Request handling
	// ------------------------------------------------------------------------

	/**
	 * sends data to the connected component in the push mode.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.connected()
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @throws Exception <i>todo.</i>
	 * 
	 * @see fr.sorbonne_u.components.interfaces.DataOfferedI.PushI#send(fr.sorbonne_u.components.interfaces.DataOfferedI.DataI)
	 */
	@Override
	public void			send(DataOfferedI.DataI d)
	throws	Exception
	{
		assert	this.connected() ;

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			AbstractCVM.getCVM().logDebug(
						CVMDebugModes.CALLING,
						"AbstractDataInboundPort sends... " + d.toString() +
						" ...on connector " + connector.toString()) ;
		}

		this.connector.send(d) ;

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			AbstractCVM.getCVM().logDebug(
						CVMDebugModes.CALLING,
						"...AbstractDataInboundPort sent! " + d.toString()) ;
		}
	}
}
//-----------------------------------------------------------------------------
