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
import fr.sorbonne_u.components.exceptions.InvariantException;
import fr.sorbonne_u.components.exceptions.PreconditionException;
import fr.sorbonne_u.components.interfaces.OfferedI;

//----------------------------------------------------------------------------
/**
 * The class <code>AbstractInboundPort</code> partially implements an inbound
 * port which implements the offered interface of the provider component so
 * that the provider can be called through this port.
 *
 * <p><strong>Description</strong></p>
 * 
 * A concrete port class must implement the offered interface of the component
 * with methods that call the corresponding implementation services of their
 * owner component, paying attention to the discipline (synchronised, ...)
 * with which these calls must be made for the given implementation of the
 * component.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		this.getImplementedInterface().isAssignableFrom(this.getClass())
 * invariant		this instanceof AbstractDataOutboundPort ?
 *                   RequiredI.class.isAssignableFrom(implementedInterface)
 *              :    OfferedI.class.isAssignableFrom(implementedInterface))
 * invariant		(this instanceof AbstractDataOutboundPort ?
 *                  this.getOwner().isRequiredInterface(
 *                                           this.getImplementedInterface())
 *              :   this.getOwner().isOfferedInterface(
 *                                           this.getImplementedInterface()))
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
	// ------------------------------------------------------------------------
	// Instance variables and constructors
	// ------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;


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
	 * @param p		object on which the invariant must be checked.
	 * @throws Exception		<i>todo.</i>
	 */
	protected static void	checkInvariant(AbstractInboundPort p)
	throws Exception
	{
		assert	p != null ;

		// From AbstractInboundPort
		assert	p.getOwner().isOfferedInterface(p.getImplementedInterface()) :
					new PreconditionException(p.getImplementedInterface() +
							" must be declared as an offered interface by "
							+ "its owner!") ;
		assert	OfferedI.class.isAssignableFrom(p.getImplementedInterface()) :
					new PreconditionException(p.getImplementedInterface() +
							" must be an offered interface!") ;

		assert	p.getImplementedInterface().isAssignableFrom(p.getClass()) :
					new InvariantException("Port must implement its "
							+ "declared implemented interface") ;
	}

	/**
	 * create and initialise inbound ports, with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null and owner != null and implementedInterface != null
	 * pre	OfferedI.class.isAssignableFrom(implementedInterface)
	 * pre	implementedInterface.isAssignableFrom(this.getClass())
	 * post	this.getPortURI().equals(uri)
	 * post	this.getOwner().equals(owner)
	 * post	this.getImplementedInterface().equals(implementedInterface)
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>todo.</i>
	 */
	public				AbstractInboundPort(
		String		uri,
		Class<?>		implementedInterface,
		ComponentI	owner
		) throws Exception
	{
		super(uri, implementedInterface, owner) ;

		assert	OfferedI.class.isAssignableFrom(implementedInterface) :
					new PreconditionException(implementedInterface
							+ " must be an offered interface!") ;

		AbstractInboundPort.checkInvariant(this) ;
	}

	/**
	 * create and initialise inbound ports with an automatically generated URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner != null and implementedInterface != null
	 * pre	OfferedI.class.isAssignableFrom(implementedInterface)
	 * pre	implementedInterface.isAssignableFrom(this.getClass())
	 * post	this.getOwner().equals(owner)
	 * post	this.getImplementedInterface().equals(implementedInterface)
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>todo.</i>
	 */
	public				AbstractInboundPort(
		Class<?>		implementedInterface,
		ComponentI	owner
		) throws Exception
	{
		this(AbstractPort.generatePortURI(implementedInterface),
			 implementedInterface, owner) ;
	}

	// ------------------------------------------------------------------------
	// Connection management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#connected()
	 */
	@Override
	public boolean		connected() throws Exception
	{
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
		assert	clientPortURI != null ;

		// Inbound ports do not know their client port, as they may have
		// many clients.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unsetClientPortURI()
	 */
	@Override
	public void			unsetClientPortURI() throws Exception
	{
		// Inbound ports do not know their client port, as they may have
		// many clients.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getClientPortURI()
	 */
	@Override
	public String		getClientPortURI() throws Exception
	{
		throw new Exception("Can't get the client port URI of a simple"
													+ " inbound port!") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#setServerPortURI(java.lang.String)
	 */
	@Override
	public void			setServerPortURI(String serverPortURI)
	throws	Exception
	{
		assert	this.getPortURI().equals(serverPortURI) ;

		// Do nothing, this is their own port URI.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#unsetServerPortURI()
	 */
	@Override
	public void			unsetServerPortURI() throws Exception
	{
		throw new Exception("Can't unset the server port URI "
										+ "of an inbound port!") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getServerPortURI()
	 */
	@Override
	public String		getServerPortURI() throws Exception
	{
		return this.getPortURI() ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#doConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public void			doConnection(String otherPortURI, String ccname)
	throws	Exception
	{
		assert	this.isPublished() :
					new PreconditionException("this.isPublished()") ;
		assert	otherPortURI != null && ccname != null :
					new PreconditionException("otherPortURI != null && "
														+ "ccname != null") ;

		throw new Error("Attempt to connect a server component port "
						+ this.getPortURI()
						+ " to a client component port " + otherPortURI
						+ " from the server side; should be done from"
						+ " the client side!") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#doConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			doConnection(String otherPortURI, ConnectorI connector)
	throws	Exception
	{
		assert	this.isPublished() :
					new PreconditionException("this.isPublished()") ;
		assert	otherPortURI != null && connector != null :
					new PreconditionException("otherPortURI != null && "
													+ "connector != null");

		throw new Error("Attempt to connect a server component port "
						+ this.getPortURI()
						+ " to a client component port " + otherPortURI
						+ " from the server side; should be done from"
						+ " the client side!") ;
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
		// Nothing to be done.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#obeyConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public void			obeyConnection(String otherPortURI, String ccname)
	throws	Exception
	{
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
		assert	otherPortURI != null && connector != null :
					new PreconditionException("otherPortURI != null && "
													+ "connector != null");

		// Not needed currently!
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractPort#doMyDisconnection()
	 */
	@Override
	protected void		doMyDisconnection() throws Exception
	{
		// Nothing to be done.
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#doDisconnection()
	 */
	@Override
	public void			doDisconnection() throws Exception
	{
		throw new Error("Attempt to disconnect an inbound port "
				+ this.getPortURI() + "; should be done from"
				+ " the client side!") ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#obeyDisconnection()
	 */
	@Override
	public void			obeyDisconnection() throws Exception
	{
		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		// As inbound ports do not hold data about their clients, nothing needs
		// to be done when disconnecting.
		throw new Error("Attempt to disconnect an inbound port "
				+ this.getPortURI() + "; should be done from"
				+ " the client side!") ;
	}
}
//----------------------------------------------------------------------------
