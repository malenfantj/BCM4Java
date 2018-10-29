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

import fr.sorbonne_u.components.ComponentI;

//-----------------------------------------------------------------------------
/**
 * The interface <code>ContinuationI</code> extends the
 * <code>ComponentI.ComponentService</code> to define a calling
 * protocol for continuations in the component model
 * continuation-passing style.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * A continuation needs to receive a value and perform the rest of the
 * computation with it. When creating a continuation, the programmer must
 * define a method <code>callWith</code> to do so. In the traditional
 * definition of the continuation-passing style, continuations do not
 * usually return result. This can be implemented by defining them
 * with a return type <code>Void</code>. Otherwise, the can be seen
 * as partial continuations.
 * </p>
 * <p>
 * Two calling protocol can be used on continuations. If the programmer
 * want to execute a continuation within the same thread as the one
 * that executes the method in which the continuation is called, then
 * he/she uses the method <code>callWith</code>. If he/she wants
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
public interface			ContinuationI<ParameterType>
extends		ComponentI.ComponentTask
{
	/**
	 * make the continuation ready for execution by a component thread
	 * though <code>runTask</code>.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!this.parameterInitialised()
	 * post	this.parameterInitialised()
	 * </pre>
	 *
	 * @return			a <code>Runnable</code> that executes the continuation when called.
	 */
	public ContinuationI<ParameterType>	applyTo() ;

	/**
	 * memorise the parameter of the continuation in the runnable object,
	 * making it ready for execution by a component thread though
	 * <code>runTask</code>.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!this.parameterInitialised()
	 * post	this.parameterInitialised()
	 * </pre>
	 *
	 * @param awaitedResult	result required by the continuation to execute.
	 * @return				a <code>Runnable</code> that executes the continuation when called.
	 */
	public ContinuationI<ParameterType>	applyTo(
		ParameterType awaitedResult
		) ;

	/**
	 * return true if the parameter has been initialised and the
	 * continuation is ready to be called.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the parameter has been initialised.
	 */
	public boolean		parameterInitialised() ;

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @throws InterruptedException <i>todo.</i>
	 */
	public void			waitUntilParameterInitialised()
	throws InterruptedException ;

	/**
	 * run the continuation as a separate task in the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.parameterInitialised()
	 * post	true			// no postcondition.
	 * </pre>
	 */
	public void			runAsTask() ;
}
//-----------------------------------------------------------------------------
