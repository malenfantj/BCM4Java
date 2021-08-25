package fr.sorbonne_u.components.examples.cps.components;

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

import java.util.Vector;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.pre.controlflowhelpers.AbstractLocalComposedContinuation;
import fr.sorbonne_u.components.pre.controlflowhelpers.AbstractContinuation;
import fr.sorbonne_u.components.pre.controlflowhelpers.AbstractLocalContinuation;
import fr.sorbonne_u.exceptions.PreconditionException;

//-----------------------------------------------------------------------------
/**
 * The class <code>ContinuationExamples</code> implements a component for
 * illustrating the use of BCM continuation-passing style.
 *
 * <p><strong>Description</strong></p>
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
public class				ContinuationExamples
extends		AbstractComponent
{
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	protected static void	checkInvariant(ContinuationExamples ce)
	{
		
	}

	protected				ContinuationExamples()
	{
		super(1, 0) ;

		this.getTracer().setTitle("ContinuationExamples") ;
		this.getTracer().setRelativePosition(1, 0) ;
	}

	// ------------------------------------------------------------------------
	// Component life-cycle
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		super.execute() ;

		final ContinuationExamples ce = this ;
		AbstractContinuation<Double> finalContinuation1 =
			new AbstractLocalContinuation<Double>(ce) {
				@Override
				public void	runWith(Double awaitedResult)
				{
					ce.traceMessage(
						"(sumVectorUnitask) Sum of the vector is: "
												+ awaitedResult + "\n") ;
				}
			} ;

		ContinuationExamples.checkInvariant(this) ;
		this.runTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run()
					{
						Vector<Double> vector = new Vector<Double>() ;
						for (int i = 1 ; i <= 10 ; i++) {
							vector.add((double) i) ;
						}
						try {
							ce.sumVectorUnitask(vector, finalContinuation1) ;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}) ;

		AbstractContinuation<Double> finalContinuation2 =
				new AbstractLocalContinuation<Double>(ce) {
					@Override
					public void	runWith(Double awaitedResult)
					{
						ce.traceMessage(
							"(sumVectorMultitask) Sum of the vector is: "
												+ awaitedResult + "\n") ;
					}
				} ;

		ContinuationExamples.checkInvariant(this) ;
		this.runTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run()
					{
						Vector<Double> vector = new Vector<Double>() ;
						for (int i = 10 ; i >= 1 ; i--) {
							vector.add((double) i) ;
						}
						try {
							ce.sumVectorMultitask(vector, finalContinuation2) ;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}) ;
	}

	// ------------------------------------------------------------------------
	// Services
	// ------------------------------------------------------------------------

	/**
	 * sum the vector passing the result to the given continuation ; in this
	 * example, continuations are called within the same task as the call to
	 * <code>sumVectorUnitask</code>.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	vector != null and vector.size() &gt;= 1
	 * pre	continuation != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param vector			vector to be summed.
	 * @param continuation	continuation to which the sum is passed.
	 * @throws Exception		<i>todo.</i>
	 */
	public void			sumVectorUnitask(
		Vector<Double> vector,
		AbstractContinuation<Double> continuation
		) throws Exception
	{
		assert	vector != null && vector.size() >= 1 :
					new PreconditionException(
							"vector != null && vector.size() >= 1") ;
		assert	continuation != null :
					new PreconditionException("continuation != null") ;

		if (vector.size() > 1) {
			final Double first = vector.remove(0) ;
			this.traceMessage(
					"(sumVectorUnitask) next value = " + first + "\n") ;
			final ContinuationExamples ce = this ;
			this.sumVectorUnitask(
				vector,
				new AbstractLocalComposedContinuation<Double>(
												this, continuation, false)
				{
					@Override
					protected void runWith(Double awaitedResult)
					{
						ce.traceMessage("runWith: " + awaitedResult + "\n") ;
						this.getSubContinuation().
										applyTo(awaitedResult + first) ;
					}					
				}) ;
		} else {
			Double first = vector.remove(0) ;
			this.traceMessage(
					"(sumVectorUnitask) last value = " + first + "\n") ;
			continuation.applyTo(first).run() ;
		}
	}

	/**
	 * sum the vector passing the result to the given continuation ; in this
	 * example, continuations are called as tasks different from the one of
	 * the call to <code>sumVectorUnitask</code> to release the component
	 * thread between each call.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	vector != null and vector.size() &gt;= 1
	 * pre	continuation != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param vector			vector to be summed.
	 * @param continuation	continuation to which the sum is passed.
	 * @throws Exception		<i>todo.</i>
	 */
	public void			sumVectorMultitask(
		Vector<Double> vector,
		AbstractContinuation<Double> continuation
		) throws Exception
	{
		assert	vector != null && vector.size() >= 1 :
					new PreconditionException(
							"vector != null && vector.size() >= 1") ;
		assert	continuation != null :
					new PreconditionException("continuation != null") ;

		if (vector.size() > 1) {
			Double first = vector.remove(0) ;
			this.traceMessage(
					"(sumVectorMultitask) next value = " + first + "\n") ;
			final ContinuationExamples ce = this ;
			ContinuationExamples.checkInvariant(this) ;
			this.runTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void		run()
					{
						try {
							ce.sumVectorMultitask(
								vector,
								new AbstractLocalComposedContinuation<Double>(
													ce, continuation, true)
								{
									@Override
									protected void runWith(Double awaitedResult)
									{
										ce.traceMessage("runWith: " + awaitedResult + "\n") ;
										ContinuationExamples.checkInvariant(ce) ;
										this.getSubContinuation().applyTo(
														awaitedResult + first) ;
									}
								}) ;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}) ;
		} else {
			Double first = vector.remove(0) ;
			this.traceMessage(
					"(sumVectorMultitask) last value = " + first + "\n") ;
			ContinuationExamples.checkInvariant(this) ;
			continuation.applyTo(first).runAsTask() ;
		}
	}

}
//-----------------------------------------------------------------------------
