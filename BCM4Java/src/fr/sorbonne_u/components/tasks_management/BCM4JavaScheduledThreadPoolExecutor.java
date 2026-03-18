package fr.sorbonne_u.components.tasks_management;

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

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>BCM4JavaScheduledThreadPoolExecutor</code> implements
 * specific scheduled thread pool executors for BCM4Java that allows
 * asynchronous callers to be notified when a call ends up abnormally (raising
 * a throwable).
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2026-03-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			BCM4JavaScheduledThreadPoolExecutor
extends		ScheduledThreadPoolExecutor
{
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a new BCM4Java scheduled thread pool executor.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code corePoolSize >= 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param corePoolSize	the number of threads to keep in the pool, even if they are idle, unless allowCoreThreadTimeOut is set.
	 */
	public				BCM4JavaScheduledThreadPoolExecutor(int corePoolSize)
	{
		super(corePoolSize);
	}

	/**
	 * create a new BCM4Java scheduled thread pool executor.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code corePoolSize >= 0}
	 * pre	{@code threadFactory != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param corePoolSize	the number of threads to keep in the pool, even if they are idle, unless allowCoreThreadTimeOut is set.
	 * @param threadFactory	the factory to use when the executor creates a new thread.
	 */
	public				BCM4JavaScheduledThreadPoolExecutor(
		int corePoolSize,
		ThreadFactory threadFactory
		)
	{
		super(corePoolSize, threadFactory);
	}

	/**
	 * create a new BCM4Java scheduled thread pool executor.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code corePoolSize >= 0}
	 * pre	{@code handler != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param corePoolSize	the number of threads to keep in the pool, even if they are idle, unless allowCoreThreadTimeOut is set.
	 * @param handler		the handler to use when execution is blocked because the thread bounds and queue capacities are reached.
	 */
	public				BCM4JavaScheduledThreadPoolExecutor(
		int corePoolSize,
		RejectedExecutionHandler handler
		)
	{
		super(corePoolSize, handler);
	}

	/**
	 * create a new BCM4Java scheduled thread pool executor.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code corePoolSize >= 0}
	 * pre	{@code threadFactory != null}
	 * pre	{@code handler != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param corePoolSize	the number of threads to keep in the pool, even if they are idle, unless allowCoreThreadTimeOut is set.
	 * @param threadFactory	the factory to use when the executor creates a new thread.
	 * @param handler		the handler to use when execution is blocked because the thread bounds and queue capacities are reached
	 */
	public				BCM4JavaScheduledThreadPoolExecutor(
		int corePoolSize,
		ThreadFactory threadFactory,
		RejectedExecutionHandler handler
		)
	{
		super(corePoolSize, threadFactory, handler);
	}

	// -------------------------------------------------------------------------
	// Static methods
	// -------------------------------------------------------------------------

	/**
	 * create a new BCM4Java scheduled thread pool executor with one and only
	 * one thread.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code threadFactory != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param threadFactory	factory to be used to create new threads.
	 * @return				the new BCM4Java scheduled thread pool executor.
	 */
	public static BCM4JavaScheduledThreadPoolExecutor
						newSingleThreadScheduledExecutor(
		ThreadFactory threadFactory
		)
	{
		assert	threadFactory != null :
				new PreconditionException("threadFactory != null");

		return new BCM4JavaScheduledThreadPoolExecutor(1, threadFactory);
	}

	/**
	 * create a new BCM4Java thread pool executor  with {@code nThreads} and
	 * using {@code threadFactory} to create new threads.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code nThreads > 0}
	 * pre	{@code threadFactory != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param nThreads		number of threads in the scheduled thread pool executor.
	 * @param threadFactory	factory to be used to create new threads.
	 * @return				the new BCM4Java scheduled thread pool executor.
	 */
	public static BCM4JavaScheduledThreadPoolExecutor
						newScheduledThreadPool(
		int nThreads,
		BCM4JavaComponentThreadFactory threadFactory
		)
	{
		assert	nThreads > 0 : new PreconditionException("nThreads > 0");
		assert	threadFactory != null :
				new PreconditionException("threadFactory != null");

		return new BCM4JavaScheduledThreadPoolExecutor(nThreads, threadFactory);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable, java.lang.Throwable)
	 */
	@Override
	protected void		afterExecute(Runnable r, Throwable t)
	{
		super.afterExecute(r, t);

		Thread thread = Thread.currentThread();
		if (thread instanceof BCM4JavaComponentThread && t == null) {
			// when everything ran correctly, clean up the thread contextual
			// information inn preparation for the next call, otherwise it will
			// be done in the uncaught exception handler
			((BCM4JavaComponentThread)thread).cleanUp();
		}
	}
}
// -----------------------------------------------------------------------------
