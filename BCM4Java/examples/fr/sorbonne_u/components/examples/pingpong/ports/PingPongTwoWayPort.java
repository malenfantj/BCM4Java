package fr.sorbonne_u.components.examples.pingpong.ports;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.examples.pingpong.components.PingPongPlayer;
import fr.sorbonne_u.components.examples.pingpong.interfaces.PingPongTwoWayCI;
import fr.sorbonne_u.components.ports.AbstractTwoWayPort;

//----------------------------------------------------------------------------
/**
 * The class <code>PingPongTwoWayPort</code> implements the two way port
 * for the <code>PingPongI</code> interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * Note how the URI of the port is used in conjunction with the URI provided
 * by the call argument to identify which way the call must go.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-03-14</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				PingPongTwoWayPort
extends		AbstractTwoWayPort<PingPongTwoWayCI>
implements	PingPongTwoWayCI
{
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Inner classes
	// ------------------------------------------------------------------------

	/**
	 * The class <code>OutProxy</code> implements an object that forwards
	 * calls through the interface <code>PingPongTwoWayI</code> to the
	 * other peer component.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2018-03-23</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 * @version	$Name$ -- $Revision$ -- $Date$
	 */
	protected static class	PingPongOutProxy
	extends		AbstractTwoWayPort.OutProxy<PingPongTwoWayCI>
	implements	PingPongTwoWayCI
	{
		public				PingPongOutProxy(PingPongTwoWayPort owner)
		{
			super(owner);
		}

		/**
		 * @see fr.sorbonne_u.components.examples.pingpong.interfaces.PingPongTwoWayCI#pingPong()
		 */
		@Override
		public void			pingPong() throws Exception
		{
			//System.out.println("OutProxy>>pingPong()") ;
			((PingPongTwoWayCI)this.getProxyTowardsOtherComponent()).
															pingPong() ;
		}
	}

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public				PingPongTwoWayPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, PingPongTwoWayCI.class, owner) ;
		this.initialise() ;
	}

	public				PingPongTwoWayPort(ComponentI owner)
	throws Exception
	{
		super(PingPongTwoWayCI.class, owner) ;
		this.initialise() ;
	}

	/**
	 * initialise this port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do.</i>
	 */
	protected void		initialise() throws Exception
	{
		this.setOut(new PingPongOutProxy(this)) ;
	}

	// ------------------------------------------------------------------------
	// Services
	// ------------------------------------------------------------------------

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>todo.</i>
	 */
	@Override
	public void			pingPong() throws Exception
	{
		//System.out.println("PingPongTwoWayPort>>pingPong()") ;
		this.owner.runTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							((PingPongPlayer)this.getTaskOwner()).pingPong() ;
						} catch (Exception e) {
							e.printStackTrace() ;
						}
					}
				}) ;
	}
}
//----------------------------------------------------------------------------
