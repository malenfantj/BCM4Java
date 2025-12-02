package fr.sorbonne_u.components.utils.tests;

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

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI.ComponentTask;
import fr.sorbonne_u.components.ComponentI.FComponentTask;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;

// -----------------------------------------------------------------------------
/**
 * The class <code>TestStep</code> defines an action to be performed by a
 * component in a test scenario.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code clock != null}
 * invariant	{@code componentRIP_URI != null && !componentRIP_URI.isEmpty()}
 * invariant	{@code instantOfOccurrence != null}
 * invariant	{@code instantOfOccurrence.isAfter(clock.getStartInstant()) && instantOfOccurrence.isBefore(clock.getEndInstant())}
 * invariant	{@code stepFunction != null || stepTask != null}
 * invariant	{@code !(stepFunction != null && stepTask != null)}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-11-10</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			TestStep
implements	TestStepI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;
	/** URI of the clock providing the time reference for the test
	 *  scenario.															*/
	protected final String			clockURI;
	/** URI of the reflection inbound port of the component performing the
	 *  test step.															*/
	protected final String			componentRIP_URI;
	/** instant of occurrence of the test step.								*/
	protected final Instant			instantOfOccurrence;
	/** a lambda implementing the test step to be performed.				*/
	protected final FComponentTask	stepFunction;
	/** a task implementing the test step to be performed.					*/
	protected final ComponentTask	stepTask;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	// Invariants are ensured by the use of final fields and preconditions
	// checked by the constructors

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(
		TestStep instance
		)
	{
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(TestStep instance)
	{
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a component test step to be performed by the component having the
	 * given reflection inbound port URI at the given time of occurrence subject
	 * to the clock time reference to perform the viven step funciton.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code componentRIP_URI != null && !componentRIP_URI.isEmpty()}
	 * pre	{@code instantOfOccurrence != null}
	 * pre	{@code instantOfOccurrence.isAfter(clock.getStartInstant()) && instantOfOccurrence.isBefore(clock.getEndInstant())}
	 * pre	{@code stepFunction != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param clockURI				URI of the clock providing the time reference for the test scenario.
	 * @param componentRIP_URI		URI of the reflection inbound port of the component performing the test step.
	 * @param instantOfOccurrence	instant of occurrence of the test step.
	 * @param stepFunction			a lambda implementing the test step to be performed.
	 */
	public				TestStep(
		String clockURI,
		String componentRIP_URI,
		Instant instantOfOccurrence,
		FComponentTask stepFunction
		)
	{
		super();

		assert	clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"clockURI != null && !clockURI.isEmpty()");
		assert	componentRIP_URI != null && !componentRIP_URI.isEmpty() :
				new PreconditionException(
						"componentRIP_URI != null && "
						+ "!componentRIP_URI.isEmpty()");
		assert	instantOfOccurrence != null :
				new PreconditionException("instantOfOccurrence != null");
		assert	stepFunction != null :
				new PreconditionException("stepFunction != null");

		this.clockURI = clockURI;
		this.componentRIP_URI = componentRIP_URI;
		this.instantOfOccurrence = instantOfOccurrence;
		this.stepFunction = stepFunction;
		this.stepTask = null;

		// Invariant checking
		assert	TestStep.implementationInvariants(this) :
				new ImplementationInvariantException(
						"ComponentTestStep.implementationInvariants(this)");
		assert	TestStep.invariants(this) :
				new InvariantException("ComponentTestStep.invariants(this)");
	}

	/**
	 * create a component test step to be performed by the component having the
	 * given reflection inbound port URI at the given time of occurrence subject
	 * to the clock time reference to perform the viven step funciton.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code componentRIP_URI != null && !componentRIP_URI.isEmpty()}
	 * pre	{@code instantOfOccurrence != null}
	 * pre	{@code instantOfOccurrence.isAfter(clock.getStartInstant()) && instantOfOccurrence.isBefore(clock.getEndInstant())}
	 * pre	{@code stepTask != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param clockURI				URI of the clock providing the time reference for the test scenario.
	 * @param componentRIP_URI		URI of the reflection inbound port of the component performing the test step.
	 * @param instantOfOccurrence	instant of occurrence of the test step.
	 * @param stepTask				a lambda implementing the test step to be performed.
	 */
	public				TestStep(
		String clockURI,
		String componentRIP_URI,
		Instant instantOfOccurrence,
		ComponentTask stepTask
		)
	{
		super();

		assert	clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"clockURI != null && !clockURI.isEmpty()");
		assert	componentRIP_URI != null && !componentRIP_URI.isEmpty() :
				new PreconditionException(
						"componentRIP_URI != null && "
						+ "!componentRIP_URI.isEmpty()");
		assert	instantOfOccurrence != null :
				new PreconditionException("instantOfOccurrence != null");
		assert	stepTask != null :
				new PreconditionException("stepTask != null");

		this.clockURI = clockURI;
		this.componentRIP_URI = componentRIP_URI;
		this.instantOfOccurrence = instantOfOccurrence;
		this.stepFunction = null;
		this.stepTask = stepTask;

		// Invariant checking
		assert	TestStep.implementationInvariants(this) :
				new ImplementationInvariantException(
						"ComponentTestStep.implementationInvariants(this)");
		assert	TestStep.invariants(this) :
				new InvariantException("ComponentTestStep.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.utils.tests.TestStepI#getPerformingEntityURI()
	 */
	@Override
	public String		getPerformingEntityURI()
	{
		return this.componentRIP_URI;
	}

	/**
	 * @see fr.sorbonne_u.components.utils.tests.TestStepI#getInstantOfOccurrence()
	 */
	@Override
	public Instant		getInstantOfOccurrence()
	{
		return this.instantOfOccurrence;
	}

	/**
	 * execute the step for execution on {@code performer}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code performer != null}
	 * pre	{@code clock != null}
	 * pre	{@code clock.getClockURI().equals(this.clockURI)}
	 * pre	{@code instantOfOccurrence.isAfter(clock.getStartInstant()) && instantOfOccurrence.isBefore(clock.getEndInstant())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param performer	the component that must perform the step.
	 */
	public void			executeComponentTestStep(
		AbstractComponent performer
		)
	{
		// TODO: make it possible to check that the URI of the reflection
		// inbound port of performer is equal to  componentRIP_URI
		assert	performer != null :
				new PreconditionException("performer != null");

		if (this.stepFunction != null) {
			this.stepFunction.run(performer);;
		} else {
			assert this.stepTask != null;
			this.stepTask.setOwnerReference(performer);
			this.stepTask.run();
		}
	}

	/**
	 * schedule the step for execution on {@code performer} on its schedulable
	 * executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code performer != null}
	 * pre	{@code performer.getReflectionInboundPortURI().equals(getPerformingEntityURI())}
	 * pre	{@code clock != null}
	 * pre	{@code clock.getClockURI().equals(this.clockURI)}
	 * pre	{@code instantOfOccurrence.isAfter(clock.getStartInstant()) && instantOfOccurrence.isBefore(clock.getEndInstant())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param performer	the component that must perform the step.
	 * @param clock		clock providing the time reference for instants in the test scenario.
	 */
	public void			planTestStep(
		AbstractComponent performer,
		AcceleratedClock clock
		)
	{
		assert	performer != null :
				new PreconditionException("performer != null");
		assert	performer.getReflectionInboundPortURI().equals(
													getPerformingEntityURI()) :
				new PreconditionException(
						"performer.getReflectionInboundPortURI().equals("
						+ "getPerformingEntityURI())");
		assert	clock != null : new PreconditionException("clock != null");
		assert	clock.getClockURI().equals(this.clockURI) :
				new PreconditionException("clock.getClockURI().equals(this.clockURI)");
		assert	this.instantOfOccurrence.isAfter(clock.getStartInstant())
					&& this.instantOfOccurrence.isBefore(clock.getEndInstant()) :
				new PreconditionException(
						"instantOfOccurrence.isAfter(clock.getStartInstant()) "
						+ "&& instantOfOccurrence.isBefore("
						+ "clock.getEndInstant())");

		long delayInNanos =
					clock.nanoDelayUntilInstant(this.getInstantOfOccurrence());
		assert	delayInNanos >= 0 : new BCMException("delayInNanos >= 0");
		performer.logMessage(
				"ComponentTestStep::planComponentTestStep schedules the step "
				+ "at instant " + this.instantOfOccurrence + " in "
				+ TimeUnit.NANOSECONDS.toMillis(delayInNanos) + " "
				+ TimeUnit.MILLISECONDS);
		if (this.stepFunction != null) {
			performer.scheduleTask(this.stepFunction,
								   delayInNanos, TimeUnit.NANOSECONDS);
		} else {
			assert this.stepTask != null;
			performer.scheduleTask(this.stepTask,
								   delayInNanos, TimeUnit.NANOSECONDS);
		}
	}
}
// -----------------------------------------------------------------------------
