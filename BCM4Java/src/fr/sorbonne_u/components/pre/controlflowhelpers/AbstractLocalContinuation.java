package fr.sorbonne_u.components.pre.controlflowhelpers;

import fr.sorbonne_u.components.ComponentI;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractLocalContinuation</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-09-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractLocalContinuation<ParameterType>
extends		AbstractContinuation<ParameterType>
{
	// -------------------------------------------------------------------------
	// Variables and constants
	// -------------------------------------------------------------------------

	protected final ComponentI	owner ;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a new continuation for the given component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param owner		the component that will be able to execute this continuation.
	 */
	public				AbstractLocalContinuation(ComponentI owner)
	{
		super() ;

		assert	owner != null ;

		this.owner = owner ;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void			run()
	{
//		this.owner.traceMessage("AbstractContinuation#run " + this + "\n") ;
		assert	this.parameterInitialised() ;

		this.runWith(this.continuationParameter) ;
		this.isInitialised = false ;
		this.continuationParameter = null ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.controlflowhelpers.ContinuationI#runAsTask()
	 */
	@Override
	public void			runAsTask()
	{
//		this.owner.traceMessage("AbstractContinuation#runAsTask " + this + "\n");
		assert	this.parameterInitialised() ;

		this.owner.runTask(this) ;
	}
}
// -----------------------------------------------------------------------------
