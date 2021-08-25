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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.examples.cps.connectors.ValueProvidingConnector;
import fr.sorbonne_u.components.examples.cps.interfaces.ValueProvidingCI;
import fr.sorbonne_u.components.examples.cps.interfaces.ports.ValueProvidingOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.pre.controlflowhelpers.AbstractLocalComposedContinuation;
import fr.sorbonne_u.components.pre.controlflowhelpers.AbstractContinuation;
import fr.sorbonne_u.components.pre.controlflowhelpers.AbstractLocalContinuation;
import fr.sorbonne_u.exceptions.InvariantException;

/**
 * The class <code>ValueConsumer</code> implements a component that uses
 * the integer value providing service defined by the interface
 * <code>ValueProvidingI</code> by getting ten values, computing their
 * mean and printing the result on the terminal.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This component is meant to illustrate the usage of the
 * continuation-passing style to free the calling component thread
 * when a call to another component is synchronous. Instead of
 * using the component thread to make the call and block it until
 * the result is returned, a temporary thread is created to do so
 * and the remaining code that required the result is put in a
 * "continuation" method which will be called with the result and
 * executed using a component thread. The methods that show the
 * technique are <code>computeAndThenPrint</code> and its
 * continuation <code>computeAndThenPrintContinuation</code>.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		numberOfIterationsLeft &gt;= 0
 * </pre>
 * 
 * <p>Created on : 2018-03-17</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@RequiredInterfaces(required = {ValueProvidingCI.class})
public class				ValueConsumer
extends		AbstractComponent
//-----------------------------------------------------------------------------
{
	protected ValueProvidingOutboundPort	outboundPort ;
	protected String						valueProvidingInboundPortURI ;
	protected int							sum ;
	protected int							numberOfIterationsLeft ;

	// ------------------------------------------------------------------------
	// Inner classes
	// ------------------------------------------------------------------------

	/**
	 * The class <code>ComputeAndThenPrintContinuation</code> implements
	 * the continuation for <code>computeAndThePrint</code> to sum the
	 * current value with the sum of other processed values which is
	 * received as parameter when called.
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
	public static class		ComputeAndThenPrintContinuation
	extends		AbstractLocalComposedContinuation<Integer>
	{
		protected int							value ;

		/**
		 * 
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	owner != null and continuation != null
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param owner			component executing the continuation.
		 * @param currentValue	the value that has been obtained.
		 * @param continuation	continuation to which the result will be passed.
		 */
		public				ComputeAndThenPrintContinuation(
			ValueConsumer owner,
			Integer currentValue,
			AbstractContinuation<Integer> continuation
			)
		{
			super(owner, continuation, true) ;

			assert	owner != null && continuation != null ;

			this.owner.traceMessage("Continuation created with value: "
													+ currentValue + "\n") ;
			this.value = currentValue ;
		}

		/**
		 * @see fr.sorbonne_u.components.pre.controlflowhelpers.AbstractContinuation#runWith(java.lang.Object)
		 */
		@Override
		public void			runWith(Integer awaitedResult)
		{
			this.owner.traceMessage("Value is: " + awaitedResult + ".\n") ;
			this.getSubContinuation().applyTo(this.value) ;
		}
	}

	/**
	 * The class <code>ComputeAndThenPrintFinalContinuation</code> implements
	 * the final continuation for <code>computeAndThePrint</code> that simply
	 * output the result to the component tracer.
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
	public static class		ComputeAndThenPrintFinalContinuation
	extends		AbstractLocalContinuation<Integer>
	{
		public				ComputeAndThenPrintFinalContinuation(
			ValueConsumer owner
			)
		{
			super(owner) ;
		}

		@Override
		public void			runWith(Integer awaitedResult)
		{
			this.owner.traceMessage("Value is: " + awaitedResult + ".\n") ;
		}
	}

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * check the invariant of the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	vc != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param vc		component to be checked.
	 */
	protected static void	checkInvariant(ValueConsumer vc)
	{
		assert	vc != null ;

		assert	vc.numberOfIterationsLeft >= 0 :
					new InvariantException("numberOfIterationsLeft >= 0") ;
	}

	/**
	 * create a value consumer component passing it the inbound port URI
	 * of the value provider component to which it must connect
	 * to get its values.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	valueProvidingInboundPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param valueProvidingInboundPortURI	inbound port URI of the value provider component.
	 * @throws Exception						<i>todo.</i>
	 */
	protected				ValueConsumer(String valueProvidingInboundPortURI)
	throws Exception
	{
		super(1, 0);
		assert	valueProvidingInboundPortURI != null ;

		this.numberOfIterationsLeft = 0 ;
		this.valueProvidingInboundPortURI = valueProvidingInboundPortURI ;
		this.outboundPort = new ValueProvidingOutboundPort(this) ;
		this.outboundPort.publishPort() ;

		this.getTracer().setTitle("ValueConsumer") ;
		this.getTracer().setRelativePosition(1, 2) ;

		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
	}

	/**
	 * create a value consumer component passing it the inbound port URI
	 * of the value provider component inbound port to which it must connect
	 * to get its values.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	reflectionInboundPortURI, != null
	 * pre	valueProvidingInboundPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI		reflection inbound port URI o the component.
	 * @param valueProvidingInboundPortURI	inbound port URI of the value provider component.
	 * @throws Exception						<i>todo.</i>
	 */
	protected				ValueConsumer(
		String reflectionInboundPortURI,
		String valueProvidingInboundPortURI
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0);
		assert	valueProvidingInboundPortURI != null ;

		this.numberOfIterationsLeft = 0 ;
		this.valueProvidingInboundPortURI = valueProvidingInboundPortURI ;
		this.outboundPort = new ValueProvidingOutboundPort(this) ;
		this.outboundPort.publishPort();

		this.getTracer().setTitle("ValueConsumer") ;
		this.getTracer().setRelativePosition(1, 2) ;
	}

	// ------------------------------------------------------------------------
	// Component life-cycle
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public void			start() throws ComponentStartException
	{
		super.start();

		// start is a good place to do the connections between components.
		try {
			this.doPortConnection(
					this.outboundPort.getPortURI(),
					valueProvidingInboundPortURI,
					ValueProvidingConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		super.execute()  ;

		this.sum = 0 ;
		// do 10 iterations
		this.numberOfIterationsLeft = 10 ;

		this.runTask(
			new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						((ValueConsumer)this.getTaskOwner()).
												computeAndThenPrint() ;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}) ;
//		this.runTask(
//			new ComponentI.ComponentTask() {
//				@Override
//				public void run() {
//					try {
//						((ValueConsumer)this.getOwner()).
//							computeAndThenPrint2(
//								10,
//								new ComputeAndThenPrintFinalContinuation(
//										((ValueConsumer)this.getOwner()))) ;
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		// finalise is a good place to disconnect and unpublish
		// outbound ports.
		this.doPortDisconnection(this.outboundPort.getPortURI());
		this.outboundPort.unpublishPort() ;
		super.finalise();
	}

	// ------------------------------------------------------------------------
	// Services
	// ------------------------------------------------------------------------

	/**
	 * the baseline service that gets the values, sum them and print the
	 * result after the number of iterations has been reached; this version
	 * uses a "manually" managed continuation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>todo.</i>
	 */
	public void			computeAndThenPrint() throws Exception
	{
		assert	this.numberOfIterationsLeft > 0 ;

			// Always check the invariant of the component before
			// releasing control of its threads to avoid having service
			// calls received and executed in an incoherent state.
			ValueConsumer.checkInvariant(this) ;
			// Call the other component service with a "continuation"
			// by using a thread that will wait for the answer and
			// then call the continuation with the result.
			final ValueConsumer vc = this ;
			(new Thread() {
				public void run() {
					try {
						// This call blocks because the continuation needs
						// the result to be executed (even using an
						// asynchronous call with a future variable would
						// lead to block this component thread.
						int result = vc.outboundPort.getValue() ;
						// To avoid perturbing the potential mutual
						// exclusion properties of the component, the
						// continuation must be run by a component
						// thread, hence the handleRequest.
						vc.runTask(
							new AbstractComponent.AbstractTask() {
								@Override
								public void run() {
									vc.computeAndThenPrintContinuation(result) ;
								}
							}) ;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start() ;
			// After the start on the thread, computeAndPrint must release
			// the control on the component thread.
			this.traceMessage("ValueConsumer released (iteration " +
								this.numberOfIterationsLeft + ").\n") ;

	}

	/**
	 * the "manually managed" continuation of the
	 * <code>computeAndThenPrint</code> that uses the result of the call to
	 * <code>getValue</code> and call again <code>computeAndThenPrint</code>
	 * if there are remaining iterations to be performed.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param result		result of the previous computation.
	 */
	public void			computeAndThenPrintContinuation(int result)
	{
		this.logMessage(
			"ValueConsumer continuation receives the value: " + result) ;

		// the "computational" continuation is to add the new value to
		// the sum, decrement the number of iterations left and call again
		// computeAndThenPrint to continue the "loop".
		this.sum += result ;
		this.numberOfIterationsLeft-- ;
		if (this.numberOfIterationsLeft > 0) {
			this.runTask(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								((ValueConsumer)this.getTaskOwner()).
													computeAndThenPrint() ;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}) ;
		} else {
			this.traceMessage("Result is: " + this.sum + "\n") ;
		}
	}

	/**
	 * the baseline service rewritten with BCM continuations to get the
	 * values and call itself recursively with a new continuation to
	 * consume the sum of the values.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	n @gt;= 0 and continuation != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param n				number of iterations.
	 * @param continuation	continuation waiting for the current result.
	 * @throws Exception		<i>todo.</i>
	 */
	public void			computeAndThenPrint2(
		int n,
		AbstractContinuation<Integer> continuation
		) throws Exception
	{
		assert	n >= 0 && continuation != null ;

		if (n > 0) {
			// Always check the invariant of the component before
			// releasing control of its threads to avoid having service
			// calls received and executed in an incoherent state.
			ValueConsumer.checkInvariant(this) ;
			// Call the other component service with a "continuation"
			// by using a thread that will wait for the answer and
			// then call the continuation with the result.
			final ValueConsumer vc = this ;
			(new Thread() {
				public void run() {
					try {
						// This call blocks because the continuation needs
						// the result to be executed (even using an
						// asynchronous call with a future variable would
						// lead to block this component thread.
						int result = vc.outboundPort.getValue() ;
						// To avoid perturbing the potential mutual
						// exclusion properties of the component, the
						// continuation must be run by a component
						// thread, hence the handleRequest.
						vc.runTask(
							new AbstractComponent.AbstractTask() {
								@Override
								public void run() {
									try {
										vc.computeAndThenPrint2(
											n - 1,
											new ComputeAndThenPrintContinuation(
													vc, result, continuation
													)) ;
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start() ;
			// After the start on the thread, computeAndPrint must release
			// the control on the component thread.
			this.traceMessage(
					"ValueConsumer released2 (iteration " + n + ").\n") ;

		} else {
			continuation.applyTo(0).runAsTask() ;
		}
	}
}
//-----------------------------------------------------------------------------
