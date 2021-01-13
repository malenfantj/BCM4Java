package fr.sorbonne_u.components.examples.chm.ports;

// Copyright Jacques Malenfant, Sorbonne Universite.
//
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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.examples.chm.components.ConcurrentMapComponent;
import fr.sorbonne_u.components.examples.chm.interfaces.MapWritingCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

//------------------------------------------------------------------------------
/**
 * The class <code>MapWritingInboundPort</code> implements the inbound for
 * map services that are changing the state of the map.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2019-01-22</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				MapWritingInboundPort<K,V>
extends		AbstractInboundPort
implements	MapWritingCI<K,V>
{
	private static final long serialVersionUID = 1L;
	protected final int	executorIndex ;

	/**
	 * create a map writing inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner.validExecutorServiceIndex(executorIndex)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri			the URI to be attributed to the port.
	 * @param executorIndex	the index of the thread pool to be used to execute the services in the owner component.
	 * @param owner			the owner component.
	 * @throws Exception		<i>to do.</i>
	 */
	public				MapWritingInboundPort(
		String uri,
		int executorIndex,
		ComponentI owner
		) throws Exception
	{
		super(uri, MapWritingCI.class, owner);

		assert	owner.validExecutorServiceIndex(executorIndex) ;

		this.executorIndex = executorIndex ;
	}

	/**
	 * create a map writing inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner.validExecutorServiceIndex(executorIndex)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorIndex	the index of the thread pool to be used to execute the services in the owner component.
	 * @param owner			the owner component.
	 * @throws Exception		<i>to do.</i>
	 */
	public				MapWritingInboundPort(
		int executorIndex,
		ComponentI owner
		) throws Exception
	{
		super(MapWritingCI.class, owner);

		assert	owner.validExecutorServiceIndex(executorIndex) ;

		this.executorIndex = executorIndex ;
	}

	/**
	 * @see fr.sorbonne_u.components.examples.chm.interfaces.MapWritingCI#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V			put(K key, V value) throws Exception
	{
		assert	key != null ;

		return this.getOwner().handleRequest(
				executorIndex,			// identifies the pool of threads to be used
				new AbstractComponent.AbstractService<V>() {
					@SuppressWarnings("unchecked")
					@Override
					public V call() throws Exception {
						return ((ConcurrentMapComponent<K,V>)
										this.getServiceOwner()).put(key, value) ;
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.examples.chm.interfaces.MapWritingCI#remove(java.lang.Object)
	 */
	@Override
	public V			remove(K key) throws Exception
	{
		assert	key != null ;

		return this.getOwner().handleRequest(
				executorIndex,			// identifies the pool of threads to be used
				new AbstractComponent.AbstractService<V>() {
					@SuppressWarnings("unchecked")
					@Override
					public V call() throws Exception {
						return ((ConcurrentMapComponent<K,V>)
										this.getServiceOwner()).remove(key) ;
					}
				}) ;
	}
}
//------------------------------------------------------------------------------
