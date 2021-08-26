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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;

//-----------------------------------------------------------------------------
/**
 * The class <code>AbstractComposedContinuation</code>
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
public abstract class	AbstractLocalComposedContinuation<ParameterType>
extends		AbstractLocalContinuation<ParameterType>
implements	ComposedContinuationI<ParameterType>
{
	protected AbstractContinuation<ParameterType>	following ;
	protected boolean								runFollowerAsTask ;

	public				AbstractLocalComposedContinuation(
		ComponentI owner,
		AbstractContinuation<ParameterType> following,
		boolean runFollowerAsTask
		)
	{
		super(owner) ;

		assert	following != null ;

		this.following = following ;
		this.runFollowerAsTask = runFollowerAsTask ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.controlflowhelpers.ComposedContinuationI#getSubContinuation()
	 */
	@Override
	public AbstractContinuation<ParameterType>	getSubContinuation()
	{
		return this.following ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.controlflowhelpers.AbstractContinuation#run()
	 */
	@Override
	public void			run()
	{
		assert	this.parameterInitialised() ;

		this.owner.traceMessage("AbstractComposedContinuation#run " + this + "\n");
		super.run() ;
		try {
			this.following.waitUntilParameterInitialised() ;
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e) ;
		}
		if (this.runFollowerAsTask) {
			this.following.runAsTask() ;
		} else {
			this.following.run() ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.pre.controlflowhelpers.AbstractContinuation#runAsTask()
	 */
	@Override
	public void			runAsTask()
	{
		assert	this.parameterInitialised() ;

		this.owner.traceMessage("AbstractComposedContinuation#runAsTask " + this + "\n");
		final AbstractLocalComposedContinuation<ParameterType> k = this ;
		this.owner.runTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						k.runWith(k.continuationParameter) ;
						try {
							k.getSubContinuation().
										waitUntilParameterInitialised() ;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (k.runFollowerAsTask) {
							k.following.runAsTask() ;
						} else {
							k.following.run() ;
						}
					}
				}) ;
	}
}
//-----------------------------------------------------------------------------
