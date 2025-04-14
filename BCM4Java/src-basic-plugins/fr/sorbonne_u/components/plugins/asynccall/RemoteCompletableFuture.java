package fr.sorbonne_u.components.plugins.asynccall;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// new implementation of the DEVS simulation standard for Java.
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

import java.util.concurrent.CompletableFuture;

// -----------------------------------------------------------------------------
/**
 * The class <code>RemoteCompletableFuture</code> implements a kind of
 * <code>CompletableFuture</code> that can't be cancelled, to be used to
 * implement a limited form of RMI asynchronous calls with future.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * Futures in Java are not meant to be returned by RMI calls as they are not
 * serializable. Remote asynchronous calls with futures are provided by the
 * JMS framework. Several limited work around are used in plain J2SE, like
 * this one.
 * </p>
 * <p>
 * This class is merely a wrapper around a completable future that prevents the
 * user from trying to cancel the task that would compute the value of the
 * future. It is meant to be used to synchronise a caller in a JVM 1 with the
 * availability of a result computed by a JVM 2 but without making a connection
 * with the task computing this result. The result must be transferred manually
 * from JVM 2 and JVM 1 and then used to complete the future, hence freeing the
 * caller blocked on a get on the future. In such a protocol, the future is
 * never transmitted over the network, hence need not be serializable. It is
 * created and managed on the client side only and not known from the server
 * side of the call.
 * </p>
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
 * <p>Created on : 2023-03-03</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RemoteCompletableFuture<T>
extends		CompletableFuture<T>
{
	/**
	 * always throw a runtime exception; as {@code RemoteCompletableFuture}
	 * can't be cancelled.
	 * 
	 * @see java.util.concurrent.CompletableFuture#cancel(boolean)
	 */
	@Override
	public boolean		cancel(boolean mayInterruptIfRunning)
	{
		throw new RuntimeException(
						"RemoteCompletableFuture can't be cancelled!");
	}

	/**
	 * always return false; as {@code RemoteCompletableFuture} can't be
	 * cancelled.
	 * 
	 * @see java.util.concurrent.CompletableFuture#isCancelled()
	 */
	@Override
	public boolean		isCancelled()
	{
		return false;
	}
}
// -----------------------------------------------------------------------------
