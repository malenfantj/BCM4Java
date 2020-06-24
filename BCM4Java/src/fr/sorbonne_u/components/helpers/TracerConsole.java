package fr.sorbonne_u.components.helpers;

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

import fr.sorbonne_u.components.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>TracerConsole</code> implements a simple tracer for BCM
 * printing trace messages on the console associated to the process.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2020-06-24</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			TracerConsole
implements	TracerI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** True if traces must be output and false otherwise.					*/
	protected boolean	tracingStatus;
	/** True if the trace is suspended and false otherwise.					*/
	protected boolean	suspendStatus;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	{@code !this.isTracing()}
	 * post	{@code !this.isSuspended()}
	 * </pre>
	 *
	 */
	public				TracerConsole()
	{
		this.tracingStatus = false;
		this.suspendStatus = false;
	}

	// -------------------------------------------------------------------------
	// Tracer methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#toggleTracing()
	 */
	@Override
	public void			toggleTracing()
	{
		this.tracingStatus = !this.tracingStatus;
		this.suspendStatus = !this.tracingStatus;
	}

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#toggleSuspend()
	 */
	@Override
	public void			toggleSuspend()
	{
		assert	this.isTracing() :
					new PreconditionException(
							"TracerConsole#toggleSuspend called but tracing "
							+ "is not activated!");

		this.suspendStatus = !this.suspendStatus;
	}

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#isTracing()
	 */
	@Override
	public boolean		isTracing()
	{
		return this.tracingStatus;
	}

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#isSuspended()
	 */
	@Override
	public boolean		isSuspended()
	{
		return this.suspendStatus;
	}

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#traceMessage(java.lang.String)
	 */
	@Override
	public void			traceMessage(String message)
	{
		if (this.tracingStatus && !this.suspendStatus) {
			System.out.print(message);
		}
	}
}
// -----------------------------------------------------------------------------
