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
import fr.sorbonne_u.components.examples.pingpong.interfaces.PingPongCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

//----------------------------------------------------------------------------
/**
 * The class <code>PingPongInboundPort</code> implements an inbound port for
 * the <code>PingPongI</code> interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-03-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				PingPongInboundPort
extends		AbstractInboundPort
implements	PingPongCI
{
	private static final long serialVersionUID = 1L;

	public				PingPongInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, PingPongCI.class, owner);
	}

	public				PingPongInboundPort(ComponentI owner)
	throws Exception
	{
		super(PingPongCI.class, owner);
	}

	/**
	 * @see fr.sorbonne_u.components.examples.pingpong.interfaces.PingPongCI#play()
	 */
	@Override
	public void			play() throws Exception
	{
		this.owner.runTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							((PingPongPlayer)this.getTaskOwner()).play() ;
						} catch (Exception e) {
							e.printStackTrace() ;
						}
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.examples.pingpong.interfaces.PingPongCI#playOnDataPull()
	 */
	@Override
	public void			playOnDataPull() throws Exception
	{
		this.owner.runTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							((PingPongPlayer)this.getTaskOwner()).playOnDataPull() ;
						} catch (Exception e) {
							e.printStackTrace() ;
						}
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.examples.pingpong.interfaces.PingPongCI#playOnDataReception(fr.sorbonne_u.components.examples.pingpong.components.Ball)
	 */
	@Override
	public void			playOnDataReception(Ball b) throws Exception
	{
		this.owner.runTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							((PingPongPlayer)this.getTaskOwner()).playOnDataReception(b) ;
						} catch (Exception e) {
							e.printStackTrace() ;
						}
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.examples.pingpong.interfaces.PingPongCI#pingPong()
	 */
	@Override
	public void			pingPong() throws Exception
	{
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

	/**
	 * @see fr.sorbonne_u.components.examples.pingpong.interfaces.PingPongCI#goToService()
	 */
	@Override
	public void			goToService() throws Exception
	{
		this.owner.runTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						((PingPongPlayer)this.getTaskOwner()).goToService() ;
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.examples.pingpong.interfaces.PingPongCI#hit(fr.sorbonne_u.components.examples.pingpong.components.Ball)
	 */
	@Override
	public void			hit(Ball b) throws Exception
	{
		this.owner.runTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							((PingPongPlayer)this.getTaskOwner()).hit(b) ;
						} catch (Exception e) {
							e.printStackTrace() ;
						}
					}
				}) ;
	}
}
//----------------------------------------------------------------------------
