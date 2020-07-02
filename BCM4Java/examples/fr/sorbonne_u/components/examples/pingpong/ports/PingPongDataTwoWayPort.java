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
import fr.sorbonne_u.components.examples.pingpong.components.Ball;
import fr.sorbonne_u.components.examples.pingpong.components.PingPongPlayer;
import fr.sorbonne_u.components.interfaces.DataTwoWayCI;
import fr.sorbonne_u.components.ports.AbstractDataTwoWayPort;

//-----------------------------------------------------------------------------
/**
 * The class <code>PingPongDataTwoWayPort</code> implements the methods
 * <code>send</code> of the <code>DataTwoWayCI.PushCI</code> interface, but
 * throws an exception for the method <code>request</code> of the
 * <code>DataTwoWayCI.PullCI</code> interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * Only the push interface is used.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-03-15</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				PingPongDataTwoWayPort
extends		AbstractDataTwoWayPort
{
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public				PingPongDataTwoWayPort(
		String uri, 
		ComponentI owner
		) throws Exception
	{
		super(uri, DataTwoWayCI.class, owner) ;
	}

	// ------------------------------------------------------------------------
	// Services
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractDataTwoWayPort#send(fr.sorbonne_u.components.interfaces.DataTwoWayCI.DataI)
	 */
	@Override
	public void			send(DataTwoWayCI.DataI d) throws Exception
	{
		this.owner.runTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							((PingPongPlayer)this.getTaskOwner()).hit((Ball) d) ;
						} catch (Exception e) {
							e.printStackTrace() ;
						}
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.ports.AbstractDataTwoWayPort#request()
	 */
	@Override
	public DataTwoWayCI.DataI		request() throws Exception
	{
		throw new Exception("Not implemented yet!") ;		
	}
}
//-----------------------------------------------------------------------------
