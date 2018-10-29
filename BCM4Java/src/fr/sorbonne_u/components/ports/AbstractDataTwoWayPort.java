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
import fr.sorbonne_u.components.connectors.AbstractDataTwoWayConnector;
import fr.sorbonne_u.components.exceptions.PreconditionException;
import fr.sorbonne_u.components.interfaces.DataTwoWayI;

//-----------------------------------------------------------------------------
/**
 * The class <code>AbstractDataTwoWayPort</code> partially implements a data
 * two-way port for components exchanging data with each others in a
 * peer-to-peer fashion.
 *
 * <p><strong>Description</strong></p>
 * 
 * Strictly speaking, the class is not abstract.  But the method
 * <code>send</code> (resp. <code>request</code>) is used both to
 * send (respectively request) data to (resp. from) the other component
 * and to receive (respectively serve the request for) data from the other
 * component.  As in this case, the port must pass (respectively request)
 * the data to its owner component, it must know what method of its owner
 * component to call, a knowledge that depends upon the application
 * component, so the two methods must be extended in an application-specific
 * port to that end.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		DataTwoWayI.PullI.class.isAssignableFrom(this.getImplementedPullInterface())
 * invariant		DataTwoWayI.PushI.class.isAssignableFrom(this.getImplementedPushInterface())
 * </pre>
 * 
 * <p>Created on : 2012-01-23</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractDataTwoWayPort
extends		AbstractTwoWayPort<DataTwoWayI>
implements	DataTwoWayI
{
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Inner classes
	// ------------------------------------------------------------------------

	/**
	 * The class <code>OutProxy</code> implements a proxy object that
	 * forwards calls to the connector and to the other component.
	 *
	 * <p><strong>Description</strong></p>
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
	protected static class	OutProxy
	extends		AbstractTwoWayPort.OutProxy<DataTwoWayI>
	implements	DataTwoWayI
	{
		public 				OutProxy(AbstractTwoWayPort<DataTwoWayI> owner)
		{
			super(owner);
		}

		/**
		 * @see fr.sorbonne_u.components.interfaces.DataTwoWayI#send(fr.sorbonne_u.components.interfaces.DataTwoWayI.DataI)
		 */
		@Override
		public void			send(DataI d) throws Exception
		{
			this.getProxyTowardsOtherComponent().send(d) ;
		}

		/**
		 * @see fr.sorbonne_u.components.interfaces.DataTwoWayI#request()
		 */
		@Override
		public DataTwoWayI.DataI		request() throws Exception
		{
			return this.getProxyTowardsOtherComponent().request() ;
		}
	}

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

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
	protected static void	checkInvariant(AbstractDataTwoWayPort p)
	throws Exception
	{
		assert	p != null ;
	}

	/**
	 * create and initialise data two-way ports, with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null and owner != null
	 * pre	DataTwoWayI.class.isAssignableFrom(implementedInterface)
	 * post	this.getPortURI().equals(uri)
	 * post	this.getOwner().equals(owner)
	 * </pre>
	 *
	 * @param uri						unique identifier of the port.
	 * @param implementedInterface		interface implemented by this port.
	 * @param owner						component that owns this port.
	 * @throws Exception 				<i>to do.</i>
	 */
	public 				AbstractDataTwoWayPort(
		String		uri,
		Class<?>		implementedInterface,
		ComponentI	owner
		) throws Exception
	{
		super(uri, implementedInterface, owner) ;

		// All data two-way ports implement a data two-way interface
		assert	DataTwoWayI.class.isAssignableFrom(implementedInterface) :
					new PreconditionException(
							"DataTwoWayI.class.isAssignableFrom(" +
											implementedInterface + ")") ;

		this.initialise() ;
	}
	
	/**
	 * create and initialise data two-way ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	DataTwoWayI.class.isAssignableFrom(implementedInterface)
	 * post	this.getPortURI().equals(uri)
	 * post	this.getOwner().equals(owner)
	 * </pre>
	 *
	 * @param implementedInterface		interface implemented by this port.
	 * @param owner						component that owns this port.
	 * @throws Exception 				<i>to do.</i>
	 */
	public				AbstractDataTwoWayPort(
		Class<?>		implementedInterface,
		ComponentI	owner
		) throws Exception
	{
		this(AbstractPort.generatePortURI(implementedInterface),
			 implementedInterface, owner) ;
	}

	/**
	 * initialise the port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception <i>todo.</i>
	 */
	protected void		initialise() throws Exception
	{
		this.setOut(new AbstractDataTwoWayPort.OutProxy(this)) ;
	}

	// ------------------------------------------------------------------------
	// Self-properties management
	// ------------------------------------------------------------------------

	/**
	 * return the connector with a more precise type.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.ports.AbstractTwoWayPort#getConnector()
	 */
	@Override
	public AbstractDataTwoWayConnector	getConnector() throws Exception
	{
		return (AbstractDataTwoWayConnector) super.getConnector();
	}

	/**
	 * return the outbound proxy with a more precise type.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.ports.AbstractTwoWayPort#getOut()
	 */
	@Override
	public DataTwoWayI 	getOut() throws Exception
	{
		return super.getOut() ;
	}
}
//-----------------------------------------------------------------------------
