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
import fr.sorbonne_u.components.connectors.ConnectorI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.exceptions.InvariantException;
import fr.sorbonne_u.components.exceptions.PostconditionException;
import fr.sorbonne_u.components.exceptions.PreconditionException;
import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;

//-----------------------------------------------------------------------------
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
 * invariant		true
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
	// ------------------------------------------------------------------------
	// Instance variables and constructors
	// ------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** URI of the server port to which this port is connected.			*/
	protected String				serverPortURI ;
	/** connector used to link with the provider component.				*/
	protected RequiredI			connector ;
	/** when connected, true if the connection is remote and false
	 *  otherwise.														*/
	protected boolean			isRemotelyConnected ;

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
	 * @param p	the object on which the invariant must be checked.
	 * @throws Exception		<i>todo.</i>
	 */
	protected static void	checkInvariant(AbstractOutboundPort p)
	throws Exception
	{
		assert	p != null ;

		// From OutboundPortI
		assert	RequiredI.class.isAssignableFrom(
										p.getImplementedInterface()) :
					new InvariantException(
							"RequiredI.class.isAssignableFrom("
									+ "p.getImplementedInterface()") ;
		assert	p.getOwner().isRequiredInterface(p.getImplementedInterface()) :
					new PreconditionException(
							"p.getOwner().isRequiredInterface("
											+ "p.getImplementedInterface())"
								+ p.getImplementedInterface() + "]") ;
		assert	!p.connected() || p.getServerPortURI() != null :
					new InvariantException("!p.connected() || "
								+ "p.getServerPortURI() != null") ;
		assert	!p.connected() || p.getPortURI().equals(p.getClientPortURI()) :
					new InvariantException(
							"p.connected() => "
								+ "p.getPortURI()."
										+ "equals(p.getClientPortURI())") ;
		assert	!p.connected() || p.getConnector() != null :
					new InvariantException(
							"p.connected() => p.getConnector() != null") ;
	}

	/**
	 * create and initialise outbound ports, with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner != null and uri != null and implementedInterface != null
	 * pre	RequiredI.class.isAssignableFrom(implementedInterface)
	 * pre	implementedInterface.isAssignableFrom(this.getClass())
	 * post	this.getImplementedInterface().equals(implementedInterface)
	 * post	this.getOwner().equals(owner)
	 * post	this.getPortURI().equals(uri)
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>todo.</i>
	 */
	public				AbstractOutboundPort(
		String		uri,
		Class<?>		implementedInterface,
		ComponentI	owner
		) throws Exception
	{
		super(uri, implementedInterface, owner) ;

		// All outbound ports implement a required interface
		assert	RequiredI.class.isAssignableFrom(implementedInterface) :
					new PreconditionException(
							"RequiredI.class."
								+ "isAssignableFrom(implementedInterface)") ;

		AbstractOutboundPort.checkInvariant(this) ;
	}

	/**
	 * create and initialize outbound ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner != null
	 * pre	RequiredI.class.isAssignableFrom(implementedInterface)
	 * post	this.getImplementedInterface().equals(implementedInterface)
	 * post	this.getOwner().equals(owner)
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception		<i>todo.</i>
	 */
	public				AbstractOutboundPort(
		Class<?>		implementedInterface,
		ComponentI	owner
		) throws Exception
	{
		this(AbstractPort.generatePortURI(implementedInterface),
			 implementedInterface, owner) ;
 	}

	// ------------------------------------------------------------------------
	// Self-properties management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractPort#setServerPortURI(java.lang.String)
	 */
	@Override
	public void			setServerPortURI(String serverPortURI)
	throws Exception
	{
		this.serverPortURI = serverPortURI ;

		assert	this.getServerPortURI().equals(serverPortURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractPort#unsetServerPortURI()
	 */
	@Override
	public void			unsetServerPortURI() throws Exception
	{
		this.serverPortURI = null ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getServerPortURI()
	 */
	@Override
	public String		getServerPortURI()
	throws Exception
	{
		return this.serverPortURI ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractPort#setClientPortURI(java.lang.String)
	 */
	@Override
	public void			setClientPortURI(String clientPortURI)
	throws Exception
	{
		assert	clientPortURI != null ;
		assert	this.getPortURI().equals(clientPortURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractPort#unsetClientPortURI()
	 */
	@Override
	public void			unsetClientPortURI() throws Exception
	{
		throw new Exception("Can't unset the client port URI "
									+ "of an outbound port!") ;

	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getClientPortURI()
	 */
	@Override
	public String		getClientPortURI()
	throws Exception
	{
		return this.getPortURI() ;
	}

	// ------------------------------------------------------------------------
	// Connection management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.OutboundPortI#setConnector(fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			setConnector(ConnectorI c)
	throws	Exception
	{
		assert	c != null ;

		this.connector = (RequiredI)c ;

		assert	this.getConnector() == c ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.OutboundPortI#unsetConnector()
	 */
	@Override
	public void			unsetConnector() throws Exception
	{
		this.connector = null ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.OutboundPortI#getConnector()
	 */
	@Override
	public ConnectorI	getConnector() throws Exception
	{
		return (ConnectorI) this.connector ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#connected()
	 */
	@Override
	public boolean		connected() throws Exception
	{
		return this.getConnector() != null && this.getConnector().connected() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#isRemotelyConnected()
	 */
	@Override
	public boolean		isRemotelyConnected() throws Exception
	{
		assert	this.connected() ;

		return this.isRemotelyConnected ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#doConnection(java.lang.String, java.lang.String)
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

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		Class<?> cc = Class.forName(ccname) ;
		Constructor<?> c = cc.getConstructor(new Class<?>[]{}) ;
		ConnectorI connector = (ConnectorI) c.newInstance() ;
		this.doConnection(otherPortURI, connector) ;

		AbstractOutboundPort.checkInvariant(this) ;
		assert	this.connected() :
					new PostconditionException("this.connected()") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#doConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
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

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		// In a simple client/server connection, where a plain outbound port is
		// connected to a plain inbound port, be it remote or local, the
		// connection is done one way on the client (outbound port) side,
		// so we need only to connect this side.
		this.doMyConnection(otherPortURI, connector) ;

		AbstractOutboundPort.checkInvariant(this) ;
		assert	this.connected() :
					new PostconditionException("this.connected()") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractPort#doMyConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
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

		this.setConnector(connector) ;
		this.setServerPortURI(otherPortURI) ;
		PortI serverPort =
				AbstractCVM.getFromLocalRegistry(this.getServerPortURI()) ;
		if (serverPort == null) {
			this.isRemotelyConnected = true ;
			serverPort =
				(PortI) AbstractDistributedCVM.getCVM().
								getRemoteReference(this.getServerPortURI()) ;
		} else {
			this.isRemotelyConnected = false ;
		}
		assert	serverPort != null :
					new RuntimeException("Unknown port URI: " +
											this.getServerPortURI()) ;

		this.getConnector().connect((OfferedI)serverPort, this) ;

		assert	this.connected() :
					new PostconditionException("this.connected()") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#obeyConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public void			obeyConnection(String otherPortURI, String ccname)
	throws	Exception
	{
		throw new Exception("Can't call obeyConnection on simple"
												+ " outbound ports.") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#obeyConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			obeyConnection(String otherPortURI, ConnectorI connector)
	throws	Exception
	{
		throw new Exception("Can't call obeyConnection on simple"
												+ " outbound ports.") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#doDisconnection()
	 */
	@Override
	public void			doDisconnection() throws Exception
	{
		assert	this.connected() && ((ConnectorI)this.connector).connected() :
					new PreconditionException(
							"this.connected() && "
							+ "((ConnectorI)this.connector).connected()") ;

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		// In a simple client/server connection, where a plain outbound port is
		// connected to a plain inbound port, be it remote or local, the
		// connection is done one way on the client (outbound port) side,
		// so we need only to disconnect this side.
		this.doMyDisconnection() ;

		AbstractOutboundPort.checkInvariant(this) ;
		assert	!this.connected() :
					new PostconditionException("!this.connected()") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractPort#doMyDisconnection()
	 */
	@Override
	protected void		doMyDisconnection() throws Exception
	{
		assert	this.connected() ;
		assert	((ConnectorI)this.connector).connected() ;

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		((ConnectorI)this.connector).disconnect() ;
		this.unsetServerPortURI() ;
		this.connector = null ;

		assert	!this.connected() :
					new PostconditionException("!this.connected()");
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#obeyDisconnection()
	 */
	@Override
	public void			obeyDisconnection() throws Exception
	{
		throw new Exception("Can't call obeyDisconnection on simple"
													+ " outbound ports.") ;
	}
}
//-----------------------------------------------------------------------------
