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

import java.util.concurrent.ThreadFactory;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>BCM4JavaComponentThreadFactory</code> proposes a standard
 * thread factory for BCM4Java creating threads that are instances of
 * {@code BCM4JavaComponentThread}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code componentURI != null && !componentURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2026-03-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			BCM4JavaComponentThreadFactory
implements	ThreadFactory
{
	// ---------------------------------------------------------------------
	// Constants and variables
	// ---------------------------------------------------------------------

	/** URI of the component owning this the thread to be created.			*/
	protected final String	componentURI;

	// ---------------------------------------------------------------------
	// Constructors
	// ---------------------------------------------------------------------

	/**
	 * create a new thread which name will be prefixed by {@code componentURI}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code componentURI != null && !componentURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param componentURI	URI of the component owning this thread.
	 */
	public				BCM4JavaComponentThreadFactory(String componentURI)
	{
		super();

		// Preconditions checking
		assert	componentURI != null && !componentURI.isEmpty() :
				new PreconditionException(
						"componentURI != null && !componentURI.isEmpty()");

		this.componentURI = componentURI;
	}

	// ---------------------------------------------------------------------
	// Methods
	// ---------------------------------------------------------------------

	/**
	 * create a new thread which name will be prefixed by the owner component
	 * URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code r != null}
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
	public Thread		newThread(Runnable r)
	{
		// Preconditions checking
		assert	r != null : new PreconditionException("r != null");
		
		return new BCM4JavaComponentThread(r, this.componentURI);
	}
}
// -----------------------------------------------------------------------------
