package fr.sorbonne_u.components.pre.controlflowhelpers;

//Copyright Jacques Malenfant, Sorbonne Universite.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import java.util.concurrent.Semaphore;

import fr.sorbonne_u.components.AbstractComponent;

//-----------------------------------------------------------------------------
/**
 * The abstract class <code>AbstractContinuation</code> implements the
 * baseline methods for the component model continuation-passing style
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * To use the continuation-passing style, a method must declare a
 * continuation parameter of type {@code AbstractContinuation<ParameterType>}
 * with the desired parameter type. Then, to call the method a continuation
 * must be created as a concrete subclass of
 * {@code AbstractContinuation<ParameterType>}
 * that implements the protected abstract method <code>runWith</code>.
 * This method receives as actual parameter the value computed before
 * and defines in its body the rest of the computation, including most
 * of the time a previous continuation to be activated.
 * </p>
 * <p>
 * Two calling protocol can be used on continuations. If the programmer
 * want to execute a continuation within the same thread as the one
 * that executes the method in which the continuation is called, then
 * he/she uses the method <code>runWith</code>. If he/she wants
 * to execute the continuation as a different task, for example to
 * release the component thread before calling the continuation, then
 * he/she uses to method <code>applyTo</code>, which memorises the
 * continuation parameter and returns a component request that
 * must be executed through a call to <code>handleRequest</code> or
 * <code>handleRequestAsync</code>.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-03-21</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractContinuation<ParameterType>
extends		AbstractComponent.AbstractTask
implements	ContinuationI<ParameterType>
{
	// --------------------------------------------------------------------------
	// Variables and constants
	// --------------------------------------------------------------------------

	protected boolean			isInitialised ;
	protected ParameterType		continuationParameter ;
	protected Semaphore			parameterSemaphore ;

	// --------------------------------------------------------------------------
	// Constructors
	// --------------------------------------------------------------------------

	/**
	 * create a new continuation for the given component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public				AbstractContinuation()
	{
		super() ;

		this.isInitialised = false ;
		this.parameterSemaphore = new Semaphore(1) ;
		try {
			this.parameterSemaphore.acquire() ;
		} catch (InterruptedException e) {
			throw new RuntimeException(e) ;
		}
	}

	// --------------------------------------------------------------------------
	// Methods
	// --------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.pre.controlflowhelpers.ContinuationI#parameterInitialised()
	 */
	@Override
	public boolean		parameterInitialised()
	{
		return this.isInitialised ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.controlflowhelpers.ContinuationI#waitUntilParameterInitialised()
	 */
	@Override
	public void			waitUntilParameterInitialised()
	throws InterruptedException
	{
		if (!this.parameterInitialised()) {
			this.parameterSemaphore.acquire() ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.pre.controlflowhelpers.ContinuationI#applyTo()
	 */
	@Override
	public ContinuationI<ParameterType>	applyTo()
	{
		assert	!this.parameterInitialised() ;

		this.continuationParameter = null ;
		this.isInitialised = true ;
		this.parameterSemaphore.release() ;
		return this ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.controlflowhelpers.ContinuationI#applyTo(java.lang.Object)
	 */
	@Override
	public ContinuationI<ParameterType>	applyTo(
		ParameterType awaitedResult
		)
	{
		assert	!this.parameterInitialised() ;

		this.continuationParameter = awaitedResult ;
		this.isInitialised = true ;
		this.parameterSemaphore.release() ;
		return this ;
	}

	/**
	 * execute the continuation with the given parameter; all of the concrete
	 * implementation of a continuation must define this method which is meant
	 * to define the actual work the continuation must do.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.parameterInitialised()
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param awaitedResult	result required by the continuation to execute.
	 */
	protected abstract void	runWith(ParameterType awaitedResult) ;
}
//-----------------------------------------------------------------------------
