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
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.exceptions.InvariantException;
import fr.sorbonne_u.components.exceptions.PostconditionException;
import fr.sorbonne_u.components.exceptions.PreconditionException;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.interfaces.DataRequiredI;
import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;

//-----------------------------------------------------------------------------
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
 * 
 * <p>
 * A concrete outbound connector must therefore implement the method
 * <code>receive</code> which will receive a piece of data as parameter
 * and pass it to the owner component.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		DataRequiredI.PullI.class.isAssignableFrom(this.implementedInterface)
 * invariant		DataRequiredI.PushI.class.isAssignableFrom(this.implementedPushInterface)
 * </pre>
 * 
 * <p>Created on : 2011-11-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractDataOutboundPort
// extends		AbstractInboundPort	// for the push interface
extends		AbstractOutboundPort
implements	DataOutboundPortI
{
	// ------------------------------------------------------------------------
	// Port instance variables and constructors
	// ------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	/** push interface implemented by this port, to receive data from the provider. */
	protected final Class<?>		implementedPushInterface ;

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
	protected static void	checkInvariant(AbstractDataOutboundPort p)
	throws Exception
	{
		assert	p != null ;

		assert	p.getImplementedInterface().equals(
										p.getImplementedPullInterface()) :
					new InvariantException(
							"!p.connected() || " + 
									"p.getImplementedInterface().equals(" + 
										"p.getImplementedPullInterface())") ;
	}

	public				AbstractDataOutboundPort(
		Class<?>		implementedInterface,
		ComponentI	owner
		) throws Exception
	{
		super(implementedInterface, owner);
		throw new RuntimeException("AbstractDataOutboundPort: must use the " +
				"three or four parameters version of the constructor.") ;
	}

	public				AbstractDataOutboundPort(
		String		uri,
		Class<?>		implementedInterface,
		ComponentI	owner
		) throws Exception
	{
		super(uri, implementedInterface, owner);
		throw new RuntimeException("AbstractDataOutboundPort: must use the " +
				"three or four parameters version of the constructor.") ;
	}

	/**
	 * create and initialize a data putbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null and owner != null
	 * pre	DataRequiredI.PullI.class.isAssignableFrom(implementedPullInterface)
	 * pre	DataRequiredI.PushI.class.isAssignableFrom(implementedPushInterface)
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
	 * @throws Exception  				<i>todo.</i>
	 */
	public				AbstractDataOutboundPort(
		String		uri,
		Class<?>		implementedPullInterface,
		Class<?>		implementedPushInterface,
		ComponentI	owner
		) throws Exception
	{
		super(uri, implementedPullInterface, owner) ;
		// the implemented interfaces are coming from a data required interface
		assert DataRequiredI.PullI.class.
								isAssignableFrom(implementedPullInterface) ;
		assert DataRequiredI.PushI.class.
								isAssignableFrom(implementedPushInterface) ;

		this.implementedPushInterface = implementedPushInterface ;
//		this.lock = new Object() ;

		AbstractDataOutboundPort.checkInvariant(this) ;
		assert	this.getImplementedPullInterface().
											equals(implementedPullInterface) :
					new PostconditionException(
							"this.getImplementedPullInterface()." + 
									"equals(implementedPullInterface)") ;
	}

	/**
	 * create and initialise a data putbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner != null
	 * pre	DataRequiredI.PullI.class.isAssignableFrom(implementedPullInterface)
	 * pre	DataRequiredI.PushI.class.isAssignableFrom(implementedPushInterface)
	 * post	this.getOwner().equals(owner)
	 * post	this.getImplementedInterface().equals(implementedPullInterface)
	 * post	this.getImplementedPushInterface().equals(implementedPushInterface)
	 * </pre>
	 *
	 * @param implementedPullInterface	pull interface implemented by this port.
	 * @param implementedPushInterface	push interface implemented by this port.
	 * @param owner						component that owns this port.
	 * @throws Exception  				<i>todo.</i>
	 */
	public				AbstractDataOutboundPort(
		Class<?>	implementedPullInterface,
		Class<?>	implementedPushInterface,
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
	public Class<?> 		getImplementedInterface() throws Exception
	{
		// make sure this method is always used to get the pull interface
		return this.getImplementedPullInterface() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.DataOutboundPortI#getImplementedPullInterface()
	 */
	@Override
	public Class<?>		getImplementedPullInterface() throws Exception
	{
		// the pull interface is stored as the original implemented interface.
		return super.getImplementedInterface() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.DataOutboundPortI#getImplementedPushInterface()
	 */
	@Override
	public Class<?> 		getImplementedPushInterface() throws Exception
	{
		return this.implementedPushInterface ;
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
		assert	this.isPublished ;
		assert	!this.connected() ;

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
		assert	!this.connected() ;
		assert	!this.isPublished() ;

		super.destroyPort() ;

		assert	!this.isPublished() ;
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
		assert	c != null : new PreconditionException("c != null") ;

		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CONNECTING)) {
			AbstractCVM.getCVM().logDebug(
				CVMDebugModes.CONNECTING,
				"AbstractDataOutboundPort setting connector " + c.toString()) ;
		}

		this.connector = (RequiredI)c ;

		assert	this.getConnector() == c :
					new PostconditionException("this.getConnector() == c") ;
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

		super.doConnection(otherPortURI, ccname) ;

		AbstractDataOutboundPort.checkInvariant(this) ;
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
		this.getConnector().obeyConnection(this, connector) ;

		AbstractDataOutboundPort.checkInvariant(this) ;
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

		AbstractDataOutboundPort.checkInvariant(this) ;
		assert	this.connected() :
					new PostconditionException("this.connected()") ;
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
			this.getConnector().connect((OfferedI) serverPort, this) ;
		} else {
			this.isRemotelyConnected = false ;
		}

		AbstractDataOutboundPort.checkInvariant(this) ;
		assert	this.connected() :
					new PostconditionException("this.connected()") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#doDisconnection()
	 */
	@Override
	public void			doDisconnection() throws Exception
	{
		assert	this.connected() && ((ConnectorI)this.connector).connected() :
			new PreconditionException(
					"this.connected() && "
					+ "((ConnectorI)this.connector).connected()") ;

		((AbstractDataConnector)this.connector).obeyDisconnection(this) ;
		this.doMyDisconnection() ;

		AbstractDataOutboundPort.checkInvariant(this) ;
		assert	!this.connected() :
					new PostconditionException("!this.connected()") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractInboundPort#obeyDisconnection()
	 */
	@Override
	public void			obeyDisconnection() throws Exception
	{
		assert	this.connected() :
					new PreconditionException("this.connected()") ;

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		this.getConnector().disconnect() ;
		this.unsetServerPortURI() ;
		this.connector = null ;
	}

	// ------------------------------------------------------------------------
	// Data outbound port service provider for pushes.
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.DataOutboundPortI#getServiceProviderReference()
	 */
	@Override
	public Object		getServiceProviderReference() throws Exception
	{
		// Standard implementation: the owner provides the services (when
		// plug-ins are used, a plug-in can do so in place of the owner).
		return this.getOwner() ;
	}

	// ------------------------------------------------------------------------
	// Request handling
	// ------------------------------------------------------------------------

	/**
	 * called by the requiring component in pull mode to trigger the obtaining
	 * of a piece of data from the offering one; this definition imposes the
	 * synchronized nature of the method, as it is called by the owner to get
	 * data from server components.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.connected() ;
	 * post	true			// no precondition.
	 * </pre>
	 * 
	 * @throws Exception  <i>todo.</i>
	 * 
	 * @see fr.sorbonne_u.components.interfaces.DataRequiredI.PullI#request()
	 */
	@Override
	public DataRequiredI.DataI	request()
	throws	Exception
	{
		assert	this.connected() :
					new PreconditionException("this.connected()") ;

		return ((DataRequiredI.PullI) this.connector).request() ;
	}
}
//-----------------------------------------------------------------------------
