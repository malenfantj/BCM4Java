package fr.sorbonne_u.components.helpers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

//------------------------------------------------------------------------------
/**
 * The class <code>ComponentSchedulableExecutorServiceManager</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2019-01-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				ComponentSchedulableExecutorServiceManager
extends		ComponentExecutorServiceManager
{
	// ------------------------------------------------------------------------
	// Constants and variables
	// ------------------------------------------------------------------------

	/** schedulable executor service to schedule and run requests and
	 *  tasks. 															*/
	protected ScheduledExecutorService	ses ;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * create a schedulable executor service manager with the given URI and
	 * the given number of threads.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null
	 * pre	nbThreads &gt; 0
	 * post	this.executorServiceCreated()
	 * </pre>
	 *
	 * @param uri		unique identifier of the executor service within the component.
	 * @param nbThreads	number of threads in the executor service.
	 */
	public				ComponentSchedulableExecutorServiceManager(
		String uri,
		int nbThreads
		)
	{
		super(uri, nbThreads) ;
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * return true if the executor service can schedule tasks and false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the executor service can schedule tasks and false otherwise.
	 */
	@Override
	public boolean		isSchedulable()
	{
		return true ;
	}

	/**
	 * return true of the executor service has been created.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true of the executor service has been created.
	 */
	@Override
	public boolean		executorServiceCreated()
	{
		return this.ses != null ;
	}

	/**
	 * create the schedulable executor service with the defined number of
	 * threads.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	this.executorServiceCreated()
	 * </pre>
	 *
	 */
	@Override
	public void			createExecutorService()
	{
		if (this.nbThreads == 1) {
			this.ses = Executors.newSingleThreadScheduledExecutor() ;
		} else {
			this.ses = Executors.newScheduledThreadPool(this.nbThreads) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.helpers.ComponentExecutorServiceManager#getExecutorService()
	 */
	@Override
	public ExecutorService	getExecutorService()
	{
		return this.getScheduledExecutorService() ;
	}

	/**
	 * return	the schedulable executor service held by this manager.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isSchedulable()
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the schedulable executor service held by this manager.
	 */
	public ScheduledExecutorService	getScheduledExecutorService()
	{
		assert	this.isSchedulable() ;

		return this.ses ;
	}

	/**
	 * shutdown the executor service of this manager.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public void			shutdown()
	{
		this.ses.shutdown() ;
	}

	/**
	 * shutdown the executor service of this manager now.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public void			shutdownNow()
	{
		this.ses.shutdownNow() ;
	}

	/**
	 * return true if the schedulable executor service is shutdown.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the schedulable executor service is shutdown.
	 */
	public boolean		isShutdown()
	{
		return this.ses.isShutdown() ;
	}

	/**
	 * return true if the schedulable executor service is terminated.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the schedulable executor service is terminated.
	 */
	public boolean		isTerminated()
	{
		return this.ses.isTerminated() ;
	}

	/**
	 * await the termination of the schedulable executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	timeout &gt;= 0
	 * preaunit != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param timeout				maximum time to wait for the termination.
	 * @param unit					time unit to interpret the time out value.
	 * @return						true if this executor terminated and false if the timeout elapsed before termination.
	 * @throws InterruptedException	if interrupted while waiting.
	 */
	public boolean		awaitTermination(long timeout, TimeUnit unit)
	throws	InterruptedException
	{
		return this.ses.awaitTermination(timeout, unit) ;
	}
}
//------------------------------------------------------------------------------
