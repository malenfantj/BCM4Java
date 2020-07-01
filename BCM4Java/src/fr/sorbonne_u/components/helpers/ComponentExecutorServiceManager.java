package fr.sorbonne_u.components.helpers;

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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>ComponentExecutorServiceManager</code> defines objects
 * used to manage user-defined executor services in components.
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
public class			ComponentExecutorServiceManager
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** unique identifier of the executor service within the component. 	*/
	protected final String			uri;
	/** number of threads in the executor service.					 		*/
	protected final int				nbThreads;
	/** executor service to run requests and tasks.					 		*/
	protected final ExecutorService	es;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a manager with the given URI and the given number of threads.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null}
	 * pre	{@code nbThreads > 0}
	 * pre	{@code es != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri		unique identifier of the executor service within the component.
	 * @param nbThreads	number of threads in the executor service.
	 * @param es		executor service to run requests and tasks.
	 */
	public				ComponentExecutorServiceManager(
		String uri,
		int nbThreads,
		ExecutorService	es
		)
	{
		super() ;

		assert	uri != null :
					new PreconditionException(
							"creating a ComponentExecutorServiceManager with " +
							"a null URI!");
		assert	nbThreads > 0 :
					new PreconditionException(
							"creating a ComponentExecutorServiceManager with " +
							"a wrong number of threads: " + nbThreads + "!");
		assert	es != null :
					new PreconditionException(
							"creating a ComponentExecutorServiceManager with " +
							"a null executor service!");
		assert	!(this instanceof ComponentExecutorServiceManager)
											|| es instanceof ExecutorService :
					new PreconditionException(
							"creating a ComponentExecutorServiceManager with " +
							"an executor service that is : " + nbThreads + "!"); 

		this.uri = uri;
		this.nbThreads = nbThreads;
		this.es = es;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * return the URI associated with the executor service in its component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the URI associated with the executor service in its component.
	 */
	public String		getURI()
	{
		return this.uri ;
	}

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
	public boolean		isSchedulable()
	{
		return false;
	}

	/**
	 * return the number of threads in the executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	{@code ret > 0}
	 * </pre>
	 *
	 * @return	the number of threads in the executor service.
	 */
	public int			getNumberOfThreads()
	{
		return this.nbThreads ;
	}

	/**
	 * return the executor service held by this manager.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.executorServiceCreated()
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the executor service held by this manager.
	 */
	public ExecutorService	getExecutorService()
	{
		return this.es ;
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
		this.es.shutdown() ;
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
		this.es.shutdownNow() ;
	}

	/**
	 * return true if the executor service is shutdown.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the executor service is shutdown.
	 */
	public boolean		isShutdown()
	{
		return this.es.isShutdown() ;
	}

	/**
	 * return true if the executor service is terminated.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the executor service is terminated.
	 */
	public boolean		isTerminated()
	{
		return this.es.isTerminated() ;
	}

	/**
	 * await the termination of the executor service.
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
		return this.es.awaitTermination(timeout, unit) ;
	}
}
// -----------------------------------------------------------------------------
