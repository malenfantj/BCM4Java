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

import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.BCMException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// -----------------------------------------------------------------------------
/**
 * The class <code>TestScenario</code> implements a test scenario to be executed
 * by components. 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * A test scenario describe a run were some of the actions taken by components
 * are imposed by the scenario. Hence, at the heart of a test scenario, there is
 * a sequence of test steps performed by components, which are identified by
 * their reflection inbound port URI. Each test step is defined by the class
 * {@code TestStep}.
 * </p>
 * <p>
 * The time management used in test steps and test scenarios is based on the
 * time line provided by the class {@code Instant}, hence test steps are planned
 * at instants using this class. A test scenario has a start instant and an end
 * instant. The time of occurrence of a test step is an instant between the
 * start and end instants. Steps are provided by an array of {@code TestStep}
 * that must be ordered by ascending instants of occurrence. To help
 * constructing complex scenarios intertwining actions performed by different
 * components, the array of steps contains all of the actions in a scenario,
 * including all components the scenario will involve. However, the execution of
 * the test scenario makes advances per component.
 * </p>
 * <p>
 * Optional beginning and ending messages can also be provided.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code testSteps != null && testSteps.length > 0}
 * invariant	{@code ordered(testSteps)}
 * invariant	{@code participantsURIs != null && participantsURIs.size() > 0}
 * invariant	{@code startInstant != null}
 * invariant	{@code endInstant != null}
 * invariant	{@code startInstant.isBefore(endInstant)}
 * invariant	{@code nextSteps != null}
 * invariant	{@code nextSteps.size() == participantsURIs.size()}
 * invariant	{@code nextSteps.keySet().stream().allMatch(uri -> participantsURIs.contains(uri))}
 * invariant	{@code nextSteps.values().stream().allMatch(index -> index >= 0 && index <= instance.testSteps.length)}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-10-20</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			TestScenario
implements	Serializable
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	/** when true, trace the test scenario.									*/
	public static boolean					VERBOSE = true;
	/** when true, print debugging information on the test scenario.		*/
	public static boolean					DEBUG = true;

	/** message to be print at the beginning of the scenario when
	 *  {@code VERBOSE} is true.											*/
	protected final String					beginningMessage;
	/** message to be print at the end of the scenario when {@code VERBOSE}
	 *  is true.															*/
	protected final String					endingMessage;

	/** URI of the clock providing the time reference for this scenario.	*/
	protected final String					clockURI;
	/** start instant of the simulation run.								*/
	protected final Instant					startInstant;
	/** end instant of the simulation run.									*/
	protected final Instant					endInstant;
	/** the steps in the test scenario.										*/
	protected final TestStepI[]				testSteps;
	/** URIs of the entities performing test actions in this scenario.		*/
	protected final Set<String>				participantsURIs;
	/** indexes if the next steps for every simulation models.				*/
	protected final Map<String, Integer>	nextSteps;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

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
	protected static boolean	implementationInvariants(TestScenario instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.testSteps != null && instance.testSteps.length > 0,
				TestScenario.class, instance,
				"testSteps != null && testSteps.length > 0");
		ret &= AssertionChecking.checkImplementationInvariant(
				ordered(instance.testSteps),
				TestScenario.class, instance,
				"ordered(testSteps)");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.participantsURIs != null &&
										instance.participantsURIs.size() > 0,
				TestScenario.class, instance,
				"participantsURIs != null && participantsURIs.size() > 0");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.nextSteps != null,
				TestScenario.class, instance,
				"nextSteps != null");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.nextSteps.size() == instance.participantsURIs.size(),
				TestScenario.class, instance,
				"nextSteps.size() == participantsURIs.size()");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.nextSteps.keySet().stream().allMatch(
					uri -> instance.participantsURIs.contains(uri)),
				TestScenario.class, instance,
				"nextSteps.keySet().stream().allMatch("
				+ "uri -> performingAtomicModelsURIs.contains(uri)");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.nextSteps.values().stream().allMatch(
					index -> index >= 0 &&
								index <= instance.testSteps.length),
				TestScenario.class, instance,
				"nextSteps.values().stream().allMatch("
				+ "index -> index >= 0 && index <= simulationTestSteps.length)");
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
	protected static boolean	invariants(TestScenario instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	/**
	 * create a test scenario with the given messages and test steps.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code startInstant != null}
	 * pre	{@code endInstant != null}
	 * pre	{@code startInstant.isBefore(endInstant)}
	 * pre	{@code testSteps != null && testSteps.length > 0}
	 * pre	{@code ordered(testSteps)}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param clockURI							URI of the clock providing the time reference for this scenario.
	 * @param startInstant						start instant of the simulation run.
	 * @param endInstant						end instant of the simulation run.
	 * @param testSteps							test steps in the scenario.
	 */
	public				TestScenario(
		String clockURI,
		Instant startInstant,
		Instant endInstant,
		TestStepI[] testSteps
		)
	{
		this(null, null, clockURI, startInstant, endInstant, testSteps);
	}

	/**
	 * create a test scenario maybe including simulation steps with the given
	 * messages, set up and test steps.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code startInstant != null}
	 * pre	{@code endInstant != null}
	 * pre	{@code startInstant.isBefore(endInstant)}
	 * pre	{@code testSteps != null && testSteps.length > 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param beginingMessage	message to be output on sysout at the beginning of the scenario.
	 * @param endingMessage		message to be output on sysout at the end of the scenario.
	 * @param clockURI			URI of the clock providing the time reference for this scenario.
	 * @param startInstant		start instant of the simulation run.
	 * @param endInstant		end instant of the simulation run.
	 * @param testSteps			test steps in the scenario.
	 */
	public				TestScenario(
		String beginingMessage,
		String endingMessage,
		String clockURI,
		Instant startInstant,
		Instant endInstant,
		TestStepI[] testSteps
		)
	{
		// Preconditions checking
		assert	startInstant != null :
				new PreconditionException("startInstant != null");
		assert	endInstant != null :
				new PreconditionException("endInstant != null");
		assert	startInstant.isBefore(endInstant) :
				new PreconditionException("startInstant.isBefore(endInstant)");
		assert	testSteps != null && testSteps.length > 0 :
				new PreconditionException(
						"testSteps != null && testSteps.length > 0");
		assert	clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"clockURI != null && !clockURI.isEmpty()");

		this.clockURI = clockURI;
		this.beginningMessage = beginingMessage;
		this.endingMessage = endingMessage;
		this.startInstant = startInstant;
		this.endInstant = endInstant;
		this.testSteps = testSteps;
		Arrays.sort(this.testSteps,
				(t1, t2) ->
					((TestStepI)t1).getInstantOfOccurrence().isBefore(
									((TestStepI)t1).getInstantOfOccurrence()) ?
						-1
					:	((TestStepI)t1).getInstantOfOccurrence().equals(
									((TestStepI)t1).getInstantOfOccurrence()) ?
							0
						:	1);

		this.participantsURIs = new HashSet<>();
		this.nextSteps = new HashMap<>();
		for (int i = 0 ; i < testSteps.length ; i++) {
			String uri = testSteps[i].getPerformingEntityURI();
			this.participantsURIs.add(uri);
			if (!this.nextSteps.containsKey(uri)) {
				// the first step in which the model with this URI appears in
				this.nextSteps.put(uri, i);
			}
		}

		// Invariant checking
		assert	TestScenario.implementationInvariants(this) :
				new ImplementationInvariantException(
						"TestScenario.implementationInvariants(this)");
		assert	TestScenario.invariants(this) :
				new InvariantException("TestScenario.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods common to all types of steps
	// -------------------------------------------------------------------------

	/**
	 * return true if the steps in {@code testSteps} appear in increasing order
	 * of instant of occurrence, otherwise false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code testSteps != null && testSteps.length > 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param testSteps	test steps in a scenario.
	 * @return			true if the steps in {@code testSteps} appear in increasing order of instant of occurrence, otherwise false.
	 */
	public static boolean	ordered(TestStepI[] testSteps)
	{
		assert	testSteps != null && testSteps.length > 0 :
				new PreconditionException(
						"testSteps != null && testSteps.length > 0");

		boolean ret = true;
		Instant old = testSteps[0].getInstantOfOccurrence();
		for (int i = 1 ; ret && i < testSteps.length ; i++) {
			Instant current = testSteps[i].getInstantOfOccurrence();
			ret &= old.isBefore(current) || old.equals(current);
			old = current;
		}
		return ret;
	}

	/**
	 * return the URI of the clock providing the time reference for this scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the URI of the clock providing the time reference for this scenario.
	 */
	public String		getClockURI()
	{
		return this.clockURI;
	}

	/**
	 * return the start instant of this test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the start instant of this test scenario.
	 */
	public Instant		getStartInstant()
	{
		return this.startInstant;
	}

	/**
	 * return the end instant of this test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the end instant of this test scenario.
	 */
	public Instant		getEndInstant()
	{
		return this.endInstant;
	}

	/**
	 * return true if {@code uri} is the URI of an entity performing some
	 * actions in this test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri	an URI.
	 * @return		true if {@code uri} is the URI of an entity performing some actions in this test scenario.
	 */
	public boolean		entityAppearsIn(String uri)
	{
		if (uri == null) {
			return false;
		} else {
			return this.participantsURIs.contains(uri);
		}
	}

	/**
	 * return the index of the next step in the scenario that the entity which
	 * URI is {@code uri} must perform or an index out of the range of the
	 * steps in this tests scenario if the entity has performed all of its steps.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code entityAppearsIn(uri)}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri	URI of an entity that must perform test steps in this scenario.
	 * @return		the index of the next step in the scenario that the model m must perform.
	 */
	protected int		findNextStepIndex(String uri)
	{
		assert	this.entityAppearsIn(uri) :
				new PreconditionException("entityAppearsIn(uri)");

		int ret = this.nextSteps.get(uri) + 1;
		for (int i = ret ; i < this.testSteps.length ; i++) {
			if (!uri.equals(
					this.testSteps[i].getPerformingEntityURI())) {
				ret++;
			} else {
				break;
			}
		}
		return ret;
	}

	/**
	 * return the instant of occurrence of the next step in this scenario for
	 * the entity which URI is {@code uri}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code m != null && atomicModelAppearsIn(m.getURI())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri	URI of an entity that must perform test steps in this scenario.
	 * @return		the instant of occurrence of the next step in this scenario for the entity which URI is {@code uri}.
	 */
	public Instant		getInstantOfNextStep(String uri)
	{
		assert	this.entityAppearsIn(uri) :
				new PreconditionException("entityAppearsIn(uri)");

		return this.testSteps[this.nextSteps.get(uri)].getInstantOfOccurrence();
	}

	/**
	 * return true if the entity which URI is {@code uri} has not terminated its
	 * actions in this scenario yet.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code entityAppearsIn(uri)}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri	URI of an entity that must perform test steps in this scenario.
	 * @return		true if the entity has not terminated its actions in this scenario yet.
	 */
	public boolean		scenarioTerminated(String uri)
	{
		if (this.entityAppearsIn(uri)) {
			return this.nextSteps.get(uri) >= this.testSteps.length;
		} else {
			return true;
		}
	}

	/**
	 * advance to the next test step in this scenario for the entity which URI
	 * is {@code uri}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code entityAppearsIn(uri)}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri	URI of an entity that must perform test steps in this scenario.
	 */
	public void			advanceToNextStep(String uri)
	{
		assert	this.entityAppearsIn(uri) :
				new PreconditionException("entityAppearsIn(uri)");

		this.nextSteps.put(uri, this.findNextStepIndex(uri));

		// Invariant checking
		assert	TestScenario.implementationInvariants(this) :
				new ImplementationInvariantException(
						"TestScenario.implementationInvariants(this)");
		assert	TestScenario.invariants(this) :
				new InvariantException("TestScenario.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods used in simulation test steps
	// -------------------------------------------------------------------------

	/**
	 * return the message to be output at the beginning of the test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the message to be output at the beginning of the test scenario.
	 */
	public String		beginMessage()
	{
		return this.beginningMessage;
	}

	/**
	 * return the message to be output at the end of the test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the message to be output at the end of the test scenario.
	 */
	public String		endMessage()
	{
		return this.endingMessage;
	}

	/**
	 * return the time delay to the next step in Unix epoch time in nanoseconds.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code clock != null}
	 * post	{@code return >= 0}	// no postcondition.
	 * </pre>
	 *
	 * @param uri	URI of the reflection inbound port of the component that must perform the step.
	 * @param clock	the clock used as time reference for the test scenario.
	 * @return		the time delay to the next step in Unix epoch time in nanoseconds.
	 */
	public long			unixEpochTimeDelayToNextStepInNanos(
		String uri,
		AcceleratedClock clock
		)
	{
		assert	uri != null && !uri.isEmpty() :
				new PreconditionException("uri != null && !uri.isEmpty()");
		assert	clock != null : new PreconditionException("clock != null");

		Instant nextInstant =
			this.testSteps[this.nextSteps.get(uri)].getInstantOfOccurrence();
		return clock.nanoDelayToInstantFromEpochTime(
									System.currentTimeMillis(), nextInstant);
	}

	/**
	 * perform the next step for the given component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code performer != null}
	 * pre	{@code entityAppearsIn(uri)}
	 * pre	{@code !scenarioTerminated(uri)}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri		URI of the reflection inbound port of the component that must perform the step.
	 * @param performer	component that must perform the step.
	 */
	public void			performNextStep(
		String uri,
		AbstractComponent performer
		)
	{
		assert	uri != null && !uri.isEmpty() :
				new PreconditionException("uri != null && !uri.isEmpty()");
		assert	performer != null :
				new PreconditionException("performer != null");
		assert	this.entityAppearsIn(uri) :
				new PreconditionException("entityAppearsIn(uri)");
		assert	!this.scenarioTerminated(uri) :
				new PreconditionException("!scenarioTerminated(uri)");

		TestStepI testStep = this.testSteps[this.nextSteps.get(uri)];
		assert testStep instanceof TestStep :
				new BCMException(
						"Precondition violation: testStep instanceof "
						+ "ComponentTestStep");
		((TestStep)testStep).executeComponentTestStep(performer);
	}

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code performer != null}
	 * pre	{@code clock != null}
	 * pre	{@code entityAppearsIn(uri)}
	 * pre	{@code !scenarioTerminated(uri)}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri		URI of the reflection inbound port of the component that must perform the step.
	 * @param performer	component that must perform the step.
	 * @param clock		the clock used as time reference for the test scenario.
	 */
	public void		scheduleNextStep(
		String uri,
		AbstractComponent performer,
		AcceleratedClock clock
		)
	{
		assert	uri != null && !uri.isEmpty() :
				new PreconditionException("uri != null && !uri.isEmpty()");
		assert	performer != null :
				new PreconditionException("performer != null");
		assert	clock != null : new PreconditionException("clock != null");
		assert	this.entityAppearsIn(uri) :
				new PreconditionException("entityAppearsIn(uri)");
		assert	!this.scenarioTerminated(uri) :
				new PreconditionException("!scenarioTerminated(uri)");

		TestStepI testStep = this.testSteps[this.nextSteps.get(uri)];
		((TestStep)testStep).planTestStep(performer, clock);		
	}
}
// -----------------------------------------------------------------------------
