package fr.sorbonne_u.components.examples.chm.components;

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
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.examples.chm.interfaces.MapReadingCI;
import fr.sorbonne_u.components.examples.chm.interfaces.MapTestingCI;
import fr.sorbonne_u.components.examples.chm.interfaces.MapWritingCI;
import fr.sorbonne_u.components.examples.chm.ports.MapReadingInboundPort;
import fr.sorbonne_u.components.examples.chm.ports.MapWritingInboundPort;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//------------------------------------------------------------------------------
/**
 * The class <code>ConcurrentMapComponent</code> implements a "componentised"
 * implementation of a hash map allowing multiple readers at the same time but
 * only one writer.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The component is meant to show how to use separate pools of threads to
 * execute non overlapping sets of service requests.
 * </p>
 * <p>
 * For this example of a concurrent hash map, one pool of thread is used to
 * execute the reader service requests and another is used for the writer ones.
 * The pool of thread for the writers is limited to one thread, so the
 * component will execute them in mutual exclusion anyway. The number of
 * threads for the readers is given as a parameter at creation time.
 * </p>
 * <p>
 * As this implementation uses more than one thread in the component, it also
 * illustrates how to use Java synchronisation tools to manage the accesses
 * to shared variables inside the component. a {@code ReentrantReadWriteLock}
 * is used to manage the concurrency among reading and writing threads. As
 * explained in the preceding paragraph, the fact that the thread pool used
 * to serve writing requests has only one thread itself guarantees the mutual
 * exclusion among writing actions, but the lock will also manage the access
 * rights between reading and writing threads. Such an organisation of
 * concurrency is actually an overshoot most of the time; here, it is used to
 * show in one example two distinct, overlapping but also complementary
 * techniques.
 * </p>
 * <p>
 * The URI used for the two pools of threads are provided as public String
 * constant to ease the sharing with the classes implementing the inbound
 * ports.
 * </p>
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
//-----------------------------------------------------------------------------
@OfferedInterfaces(offered = {MapReadingCI.class,
							  MapTestingCI.class,
							  MapWritingCI.class})
public class			ConcurrentMapComponent<K,V>
extends		AbstractComponent
//-----------------------------------------------------------------------------
{
	// ------------------------------------------------------------------------
	// Constants and variables
	// ------------------------------------------------------------------------

	/** set DEBUG to true to get a trace of the calls with counts.		*/
	public static final boolean			DEBUG = true ;
	/** URI of the readers' pool of threads.								*/
	public static final String			READ_ACCESS_HANDLER_URI = "rah" ;
	/** URI of the writers' pool of threads.								*/
	public static final String			WRITE_ACCESS_HANDLER_URI = "wah" ;

	/** the hash map implementing the data structure and containing the
	 *  entries.															*/
	protected final HashMap<K,V>				hm ;
	/** read/write lock controlling the accesses to the hash map.			*/
	protected final ReentrantReadWriteLock		hashMapLock ;

	/** inbound port for reading calls.									*/
	protected final MapReadingInboundPort<K,V>	readingInboundPort ;
	/** inbound port for writing calls.									*/
	protected final MapWritingInboundPort<K,V>	writingInboundPort ;

	/** count of all calls received (for the trace, when DEBUG is true).	*/
	protected int						callTotalCount ;
	/** count of received but not exited calls (for the trace, when DEBUG
	 *  is true).														*/
	protected int						nonExitedCount ;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * creating a concurrent map component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	reflectionInboundPortURI != null
	 * pre	nbReadingThreads &gt; 0
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of this component reflection inbound port.
	 * @param nbReadingThreads			number of threads in the readers' pool.
	 * @throws Exception					<i>to do.</i>
	 */
	protected			ConcurrentMapComponent(
		String reflectionInboundPortURI,
		int nbReadingThreads
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0) ;

		assert	reflectionInboundPortURI != null ;
		assert	nbReadingThreads > 0 ;

		this.getTracer().setTitle("ConcurrentMapComponent") ;
		this.getTracer().setRelativePosition(0, 0) ;

		this.hm = new HashMap<K,V>() ;
		this.hashMapLock = new ReentrantReadWriteLock() ;

		this.createNewExecutorService(READ_ACCESS_HANDLER_URI,
									 nbReadingThreads,
									 false) ;
		this.createNewExecutorService(WRITE_ACCESS_HANDLER_URI, 1, false) ;

		this.readingInboundPort =
			new MapReadingInboundPort<K,V>(
					this.getExecutorServiceIndex(READ_ACCESS_HANDLER_URI),
					this) ;
		this.readingInboundPort.publishPort() ;

		this.writingInboundPort =
			new MapWritingInboundPort<K,V>(
					this.getExecutorServiceIndex(WRITE_ACCESS_HANDLER_URI),
					this) ;
		this.writingInboundPort.publishPort() ;

		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * return true if the map contains the given value.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param value		value to be tested.
	 * @return			true if the map contains the given value.
	 */
	public boolean		containsValue(V value)
	{
		boolean res = false ;
		int c ;
		int ne ;

		this.hashMapLock.readLock().lock() ;
		try {
			if (DEBUG) {
				c = this.callTotalCount++ ;
				ne = ++this.nonExitedCount ;
				this.logMessage("[" + c + "," + ne + "]" +
								"---->> containsValue(" + value + ")") ;
			}
			res = this.hm.containsValue(value) ;
			if (DEBUG) {
				ne = --this.nonExitedCount ;
				this.logMessage("[" + c + "," + ne  + "]" +
								"<<---- containsValue(" + value + ")") ;
			}
		} finally {
			this.hashMapLock.readLock().unlock();
		}
		return res ;
	}

	/**
	 * return true if the map contains the given key.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param key		key to be tested.
	 * @return			true if the map contains the given key.
	 */
	public boolean		containsKey(K key)
	{
		boolean res = false ;
		int c ;
		int ne ;

		this.hashMapLock.readLock().lock() ;
		try {
			if (DEBUG) {
				c = this.callTotalCount++ ;
				ne = ++this.nonExitedCount ;
				this.logMessage("[" + c + "," + ne  + "]" +
								"---->> containsKey(" + key + ")") ;
			}
			res = this.hm.containsKey(key) ;
			if (DEBUG) {
				ne = --this.nonExitedCount ;
				this.logMessage("[" + c + "," + ne  + "]" +
								"<<---- containsKey(" + key + ")") ;
			}
		} finally {
			this.hashMapLock.readLock().unlock();
		}
		return res ;
	}

	/**
	 * return the value associated to the given key in the map of null if none.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param key		key to be accessed.
	 * @return			the value associated to the given key in the map of null if none.
	 */
	public V				get(K key)
	{
		V res ;
		int c ;
		int ne ;

		this.hashMapLock.readLock().lock() ;
		try {
			if (DEBUG) {
				c = this.callTotalCount++ ;
				ne = ++this.nonExitedCount ;
				this.logMessage("[" + c + "," + ne  + "]" +
								"---->> get(" + key + ")") ;
			}
			res = this.hm.get(key) ;
			if (DEBUG) {
				ne = --this.nonExitedCount ;
				this.logMessage("[" + c + "," + ne  + "]" +
								"<<---- get(" + key + ")") ;
			}
		} finally {
			this.hashMapLock.readLock().unlock();
		}
		return res ;
	}

	/**
	 * return true if the map is empty, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return			true if the map is empty, false otherwise.
	 */
	public boolean		isEmpty()
	{
		boolean res = false ;
		int c ;
		int ne ;

		this.hashMapLock.readLock().lock() ;
		try {
			if (DEBUG) {
				c = this.callTotalCount++ ;
				ne = ++this.nonExitedCount ;
				this.logMessage("[" + c + "," + ne  + "]" +
								"---->> isEmpty()") ;
			}
			res =  this.hm.isEmpty() ;
			if (DEBUG) {
				ne = --this.nonExitedCount ;
				this.logMessage("[" + c + "," + ne  + "]" +
								"<<---- isEmpty()") ;
			}
		} finally {
			this.hashMapLock.readLock().unlock();
		}
		return res ;
	}

	/**
	 * add or replace the value associated to the given key returning the
	 * value previously associated to the given key or null if none.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param key	the key to which the given value must be associated.
	 * @param value	the new value to associated to the given key.
	 * @return		the value previously associated to the given key or null if none.
	 */
	public V				put(K key, V value)
	{
		V res ;
		int c ;
		int ne ;

		this.hashMapLock.writeLock().lock() ;
		try {
			if (DEBUG) {
				c = this.callTotalCount++ ;
				ne = ++this.nonExitedCount ;
				this.logMessage("[" + c + "," + ne  + "]" +
								"====>> put(" + key + "," + value + ")") ;
			}
			res = this.hm.put(key, value) ;
			if (DEBUG) {
				ne = --this.nonExitedCount ;
				this.logMessage("[" + c + "," + ne  + "]" +
								"<<==== put(" + key + "," + value + ")") ;
			}
		} finally {
			this.hashMapLock.writeLock().unlock() ;
		}
		return res ;
	}

	/**
	 * remove the association of the given key in the map, returning the
	 * value that was associated to it or null if none.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param key	the key which association must be removed.
	 * @return		the value that was associated to the given key or null if none.
	 */
	public V				remove(K key)
	{
		V res ;
		int c ;
		int ne ;

		this.hashMapLock.writeLock().lock() ;
		try {
			if (DEBUG) {
				c = this.callTotalCount++ ;
				ne = ++this.nonExitedCount ;
				this.logMessage("[" + c + "," + ne  + "]" +
								"====>> remove(" + key + ")") ;
			}
			res = this.hm.remove(key) ;
			if (DEBUG) {
				ne = --this.nonExitedCount ;
				this.logMessage("[" + c + "," + ne  + "]" +
								"<<==== remove(" + key + ")") ;
			}
		} finally {
			this.hashMapLock.writeLock().unlock() ;
		}
		return res ;
	}

	/**
	 * return the number of associations kept in the map.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the number of associations kept in the map.
	 */
	public int			size()
	{
		int res ;
		int c ;
		int ne ;

		this.hashMapLock.readLock().lock() ;
		try {
			if (DEBUG) {
				c = this.callTotalCount++ ;
				ne = ++this.nonExitedCount ;
				this.logMessage("[" + c + "," + ne  + "]" + "---->> size()") ;
			}
			res = this.hm.size() ;
			if (DEBUG) {
				ne = --this.nonExitedCount ;
				this.logMessage("[" + c + "," + ne  + "]" + "<<---- size()") ;
			}
		} finally {
			this.hashMapLock.readLock().unlock();
		}
		return res ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		this.readingInboundPort.unpublishPort() ;
		this.writingInboundPort.unpublishPort() ;

		super.finalise();
	}
}
//------------------------------------------------------------------------------
