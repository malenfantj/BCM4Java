package fr.sorbonne_u.components.helpers;

import fr.sorbonne_u.exceptions.PreconditionException;

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
