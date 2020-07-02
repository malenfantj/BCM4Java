package fr.sorbonne_u.components.connectors;

// Copyright Jacques Malenfant, Sorbonne Universite.
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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;

// -----------------------------------------------------------------------------
/**
 * The class <code>ActiveDataConnector</code> partially implements an
 * active connector that can be used to connect active components in a
 * synchronised way, on a mix of active and passive components using a full or
 * partial pull mode.
 *
 * TODO: needs rethinking and redesign so to be used only when the two
 *       components use different push/pull mode so to connect them
 *       transparently.
 * 
 * <p><strong>Description</strong></p>
 * 
 * As there are two possible mode for transmitting data, push and pull, the
 * connector establishes a two way connection between the two components,
 * implementing the offering <code>PushCI</code> interface with methods calling
 * the requiring <code>PushCI</code> and implementing the requiring
 * <code>PullCI</code> interface with methods calling the offering
 * <code>PullCI</code> one.
 * 
 * A fully active connector has two threads: one to actively push data to
 * the requiring component by calling its <code>receive</code> method, and
 * one to actively pull data from the offering component by calling its
 * <code>get</code> method.
 * 
 * The connector acts as a bounded buffer between the two components.  If
 * the requiring component actively pull data from the connector, then the
 * connector can be configured as a passive pusher, while if the offering
 * component actively pushes data to the connector, then it can be configured
 * as a passive puller.
 * 
 * When the connector is configured as a passive pusher, then its implementation
 * of the <code>request</code> method can be used by the requiring component
 * to obtain new datum by accessing directly and synchronisely the buffer.
 * In the same way, when the connector is configured as a passive puller, then
 * its implementation of the <code>send</code> method can be used by the
 * offering component to push new datum by putting them into the buffer
 * in a synchronized way.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	isActivePuller =&gt; dataPuller != null
 * 				isActivePusher =&gt; dataPusher != null
 * 										// TODO: to be completed...
 * </pre>
 * 
 * <p>Created on : 2011-11-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	ActiveDataConnector
extends		AbstractDataConnector
{
	protected static int		DEFAULT_CAPACITY = 1 ;
	protected static boolean	DEFAULT_ACTIVE_PUSH_BEHAVIOR = true ;
	protected static int		DEFAULT_PUSH_INTERVAL = 100 ;
	protected static boolean	DEFAULT_ACTIVE_PULL_BEHAVIOR = true ;
	protected static int		DEFAULT_PULL_INTERVAL = 100 ;

	protected BlockingQueue<DataOfferedCI.DataI>	bq ;
	protected boolean			isActivePuller ;
	protected Thread			dataPuller ;
	protected int				pullInterval ;
	protected boolean			isActivePusher ;
	protected Thread			dataPusher ;
	protected int				pushInterval ;
	protected boolean			stillActive ;

	public				ActiveDataConnector()
	{
		this(DEFAULT_CAPACITY) ;
	}

	/**
	 * creates a new connector with some data buffer capacity, and with the
	 * default behavior of being both an active puller and pusher with
	 * minimum intervals between each push and each pull of 100 milliseconds.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param bufferingCapacity <i>todo.</i>
	 */
	public				ActiveDataConnector(
		int		bufferingCapacity
		)
	{
		this(bufferingCapacity,
				DEFAULT_ACTIVE_PULL_BEHAVIOR, DEFAULT_PULL_INTERVAL,
				DEFAULT_ACTIVE_PUSH_BEHAVIOR, DEFAULT_PUSH_INTERVAL) ;
	}

	/**
	 * creates a new active connector and fully configure it.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param bufferingCapacity	capacity of the data buffer.
	 * @param isActivePuller	true if the connector must be an active puller.
	 * @param pullInterval		minimum interval between pulls in milliseconds.
	 * @param isActivePusher	true if the connector must e an active pusher.
	 * @param pushInterval		minimum interval between pushes in milliseconds.
	 */
	public				ActiveDataConnector(
		int		bufferingCapacity,
		boolean	isActivePuller,
		int		pullInterval,
		boolean	isActivePusher,
		int		pushInterval
		)
	{
		super() ;
		this.bq = new ArrayBlockingQueue<DataOfferedCI.DataI>(
														bufferingCapacity) ;
		this.isActivePuller = isActivePuller ;
		this.isActivePusher = isActivePusher ;
		if (isActivePuller) {
			this.pullInterval = pullInterval ;
			this.dataPuller = this.createPullerThread() ;
		}
		if (isActivePusher) {
			this.pushInterval = pushInterval ;
			this.dataPusher = this.createPusherThread() ;
		}
	}

	/**
	 * creates a pusher thread that calls the requiring component to
	 * receive a datum when a new one is available in the data buffer, blocking
	 * if the buffer is empty.  A new datum is pushed each
	 * <code>pushInterval</code> milliseconds, unless the data buffer is empty
	 * and then the pushing is delayed until a new datum becomes available in
	 * the data buffer.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return		a pushing thread.
	 */
	protected Thread	createPusherThread() {
//		final ActiveDataConnector self = this ;
		return new Thread() {
			public void run() {
//				while (self.stillActive) {
//					DataRequiredCI.DataI d = self.request() ;
//					self.requiring.receive(d) ;
//					try {
//						Thread.sleep(pushInterval) ;
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
			}
		};
	}

	/**
	 * creates a puller thread that repeatedly calls the offering component to
	 * get a new datum that is then put into the data buffer, blocking if it
	 * is full.  A new datum is pulled each <code>pullInterval</code>
	 * milliseconds, unless the data buffer is full and then the pulling is
	 * delayed until space becomes available in the data buffer.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return		a puller thread.
	 */
	protected Thread	createPullerThread() {
		final ActiveDataConnector self = this ;
		return new Thread(){
			public void run() {
				while (self.stillActive) {
//					OI.DataI d = self.offering.get() ;
//					self.send(d) ;
					try {
						Thread.sleep(pullInterval) ;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}

	/**
	 * starts the data pushing and data pulling threads depending on the
	 * configuration of the connector.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public void			start() {
		this.stillActive = true ;
		if (this.isActivePuller) {
			this.dataPuller.start() ;
		}
		if (this.isActivePusher) {
			this.dataPusher.start() ;
		}
	}

	public void			stop() {
		this.stillActive = false ;
	}

	/**
	 * implements the required pull interface by taking a value from the data
	 * buffer if any, blocking if none.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.interfaces.DataRequiredCI.PullCI#request()
	 */
	@Override
	public DataRequiredCI.DataI request() {
		DataRequiredCI.DataI ret = null ;
		try {
			ret = this.offered2required(this.bq.take());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return ret ;
	}

	/**
	 * implements the offered push interface by putting a value into the data
	 * buffer, blocking if the buffer is full.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.interfaces.DataOfferedCI.PushCI#send(fr.sorbonne_u.components.interfaces.DataOfferedCI.DataI)
	 */
	@Override
	public void send(DataOfferedCI.DataI d) {
		try {
			this.bq.put(d) ;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
// -----------------------------------------------------------------------------
