package fr.sorbonne_u.utils.aclocks;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

// -----------------------------------------------------------------------------
/**
 * The class <code>ClocksServer</code> implements a component that can be used
 * to share clocks among other components, and more specifically accelerated
 * clocks so that all components can share the same starting Unix epoch time,
 * start instant and acceleration factor.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * In time-triggered test scenarios, actions performed by components must be 
 * synchronised upon a shared view of time. <code>AcceleratedClock</code> 
 * provides methods to share access to a common time line, which in fact is two
 * time lines kept synchronised in presence of an acceleration factor. This
 * acceleration factor allows to perform long scenarios in less than actual real
 * time. But having components share the same clock is not so easy. In
 * centralised executions, it would be possible to pass the same
 * <code>AcceleratedClock</code> instance to all components.
 * </p>
 * <p>
 * In distributed executions however, it is mandatory to have only one process
 * creating the <code>AcceleratedClock</code> instance. This raises the question
 * about how to get access to this clock for components that reside in other
 * processes. The <code>ClocksServer</code> component provides a solution:
 * create only one clock server accessible to all components an create in it
 * a global clock that will be shared by simply having components retrieving it
 * after its creation. 
 * </p>
 * <p>
 * Note that in distributed executions, sharing does not mean actually sharing
 * the same <code>AcceleratedClock</code> instance. No simple reference can
 * provide access to the same object from different processes. One solution
 * would have been to have an <code>AcceleratedClock</code> <b>component</b>
 * instead of an object and then having all components accessing this unique
 * component. This solution has one drawback: the latency of RMI calls among
 * processes and across the network. As the <code>AcceleratedClock</code> can
 * easily be copied and copies can be used with the same semantics as sharing
 * a single instance, having copies of the clock within the different processes
 * solves the latency problem. But, indeed, it causes another problem: the
 * synchronisation of the copies. <code>AcceleratedClock</code> use the
 * underlying computer physical clock to evolve the its two time lines. When
 * copies of the same clock execute on different computers, they are no more
 * synchronised than the physical clocks of the two computer are. This is
 * especially important when high acceleration factors are used, as they tend
 * to shorten the actual real time between events and therefore may cause
 * instants inversions among events executed on different computers. A good
 * rule of thumb is to used smaller acceleration factors when executing on
 * several computers and make sure that the computers used for this execution
 * have their clocks synchronised the best possible.
 * </p>
 * 
 * <p><i>Mutual exclusion</i></p>
 * 
 * <p>
 * As the component is created with only one thread that is systematically used
 * by the inbound port to execute the service calls, they are <i>de facto</i>
 * executed in mutual exclusion. Hence, retrieving a clock with {@code getClock}
 * cannot interfere negatively with creating a clock with {@code createClock}.
 * </p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2022-11-15</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// -----------------------------------------------------------------------------
@OfferedInterfaces(offered={ClocksServerCI.class})
// -----------------------------------------------------------------------------
public class			ClocksServer
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** standard URI of the inbound port if no other is supplied.			*/
	public static final String	STANDARD_INBOUNDPORT_URI = "clock-server-101";
	/** when true, methods trace their actions.								*/
	public static boolean		VERBOSE = false;

	/** map that contains the clocks previously created; must be
	 *  synchronised.														*/
	protected final Map<String,CompletableFuture<AcceleratedClock>>	clocks;
	/** lock protecting accesses to the clocks map.							*/
	protected final ReentrantLock	clocksLock;
	/** URI of the inbound port offering the {@code ClockServerCI} component
	 *  interface.															*/
	protected final String							inboundPortURI;
	/** inbound port through which the component offers the component
	 *  interface {@code ClockServerCI}.									*/
	protected ClocksServerInboundPort				inboundPort;
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the clock server component with an inbound port offering the
	 * component interface {@code ClocksServerCI} with as URI the value of
	 * {@code STANDARD_INBOUNDPORT_URI}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception <i>to do</i>.
	 */
	protected			ClocksServer() throws Exception
	{
		this(AbstractPort.generatePortURI(ReflectionCI.class),
			 STANDARD_INBOUNDPORT_URI);
	}

	/**
	 * create the clock server component with an inbound port offering the
	 * component interface {@code ClocksServerCI} with as URI the value of
	 * {@code STANDARD_INBOUNDPORT_URI}; the created clock server component
	 * will also have a first clock created from the given parameters.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code getClock(clockURI) == null}
	 * pre	{@code unixEpochStartTimeInNanos > 0}
	 * pre	{@code startInstant != null}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getClock(clockURI).getStartEpochNanos() == unixEpochStartTimeInNanos}
	 * post	{@code getClock(clockURI).getStartInstant().equals(startInstant)}
	 * post	{@code getClock(clockURI).getAccelerationFactor() == accelerationFactor}
	 * </pre>
	 *
	 * @param clockURI					URI designating the first created clock.
	 * @param unixEpochStartTimeInNanos	start time in Unix epoch time expressed in nanoseconds.
	 * @param startInstant				start instant to be aligned with the {@code unixEpochStartTimeInNanos}.
	 * @param accelerationFactor		acceleration factor to be applied between elapsed time as {@code Instant} and elapsed time as Unix epoch time in nanoseconds.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			ClocksServer(
		String clockURI,
		long unixEpochStartTimeInNanos,
		Instant	startInstant,
		double accelerationFactor
		) throws Exception
	{
		this(AbstractPort.generatePortURI(ReflectionCI.class),
			 STANDARD_INBOUNDPORT_URI,
			 clockURI, unixEpochStartTimeInNanos, startInstant,
			 accelerationFactor);
	}

	/**
	 * create the clock server component with an inbound port offering the
	 * component interface {@code ClocksServerCI} having the given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param inboundPortURI	URI of the inbound port offering the {@code ClockServerCI} component interface.
	 * @throws Exception 		<i>to do</i>.
	 */
	protected			ClocksServer(
		String inboundPortURI
		) throws Exception
	{
		// access to this.clocks in mutual exclusion is ensured by having only
		// one thread to execute the component services.
		super(1, 0);

		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");

		this.inboundPortURI = inboundPortURI;
		this.clocksLock = new ReentrantLock();
		this.clocks = new HashMap<>();
		this.initialise();
	}

	/**
	 * create the clock server component with an inbound port having the
	 * given reflection inbound port URI and the given URI for the inbound
	 * port offering the component interface {@code ClocksServerCI}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection inbound port.
	 * @param inboundPortURI			URI of the inbound port offering the {@code ClockServerCI} component interface.
	 * @throws Exception 				<i>to do</i>.
	 */
	protected			ClocksServer(
		String reflectionInboundPortURI,
		String inboundPortURI
		) throws Exception
	{
		// access to this.clocks in mutual exclusion is ensured by having only
		// one thread to execute the component services.
		super(reflectionInboundPortURI, 1, 0);

		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");

		this.inboundPortURI = inboundPortURI;
		this.clocksLock = new ReentrantLock();
		this.clocks = new HashMap<>();
		this.initialise();
	}

	/**
	 * create the clock server component with an inbound port having the given
	 * URI for the inbound port offering the component interface
	 * {@code ClocksServerCI}; the created clock server component will also have
	 * a first clock created from the given parameters..
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code getClock(clockURI) == null}
	 * pre	{@code unixEpochStartTimeInNanos > 0}
	 * pre	{@code startInstant != null}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getClock(clockURI) != null}
	 * post	{@code getClock(clockURI).getStartEpochNanos() == unixEpochStartTimeInNanos}
	 * post	{@code getClock(clockURI).getStartInstant().equals(startInstant)}
	 * post	{@code getClock(clockURI).getAccelerationFactor() == accelerationFactor}
	 * </pre>
	 *
	 * @param inboundPortURI			URI of the inbound port offering the {@code ClockServerCI} component interface.
	 * @param clockURI					URI designating the created clock.
	 * @param unixEpochStartTimeInNanos	start time in Unix epoch time expressed in nanoseconds.
	 * @param startInstant				start instant to be aligned with the {@code unixEpochStartTimeInNanos}.
	 * @param accelerationFactor		acceleration factor to be applied between elapsed time as {@code Instant} and elapsed time as Unix epoch time in nanoseconds.
	 * @throws Exception 				<i>to do</i>.
	 */
	protected			ClocksServer(
		String inboundPortURI,
		String clockURI,
		long unixEpochStartTimeInNanos,
		Instant	startInstant,
		double accelerationFactor
		) throws Exception
	{
		this(AbstractPort.generatePortURI(ReflectionCI.class),
			 inboundPortURI, clockURI, unixEpochStartTimeInNanos,
			 startInstant, accelerationFactor);
	}

	/**
	 * create the clock server component with an inbound port having the
	 * given reflection inbound port URI and the given URI for the inbound
	 * port offering the component interface {@code ClocksServerCI}; the created
	 * clock server component will also have a first clock created from the
	 * given parameters..
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code getClock(clockURI) == null}
	 * pre	{@code unixEpochStartTimeInNanos > 0}
	 * pre	{@code startInstant != null}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getClock(clockURI) != null}
	 * post	{@code getClock(clockURI).getStartEpochNanos() == unixEpochStartTimeInNanos}
	 * post	{@code getClock(clockURI).getStartInstant().equals(startInstant)}
	 * post	{@code getClock(clockURI).getAccelerationFactor() == accelerationFactor}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection inbound port.
	 * @param inboundPortURI			URI of the inbound port offering the {@code ClockServerCI} component interface.
	 * @param clockURI					URI designating the created clock.
	 * @param unixEpochStartTimeInNanos	start time in Unix epoch time expressed in nanoseconds.
	 * @param startInstant				start instant to be aligned with the {@code unixEpochStartTimeInNanos}.
	 * @param accelerationFactor		acceleration factor to be applied between elapsed time as {@code Instant} and elapsed time as Unix epoch time in nanoseconds.
	 * @throws Exception 				<i>to do</i>.
	 */
	protected			ClocksServer(
		String reflectionInboundPortURI,
		String inboundPortURI,
		String clockURI,
		long unixEpochStartTimeInNanos,
		Instant	startInstant,
		double accelerationFactor
		) throws Exception
	{
		// access to this.clocks in mutual exclusion is ensured by having only
		// one thread to execute the component services.
		super(reflectionInboundPortURI, 1, 0);

		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");
		assert	clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"clockURI != null && !clockURI.isEmpty()");
		assert	unixEpochStartTimeInNanos > 0 :
				new PreconditionException("unixEpochStartTimeInNanos > 0");
		assert	startInstant != null :
				new PreconditionException("startInstant != null");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		this.inboundPortURI = inboundPortURI;
		this.clocksLock = new ReentrantLock();
		this.clocks = new HashMap<>();
		this.createClock(clockURI, unixEpochStartTimeInNanos,
										 startInstant, accelerationFactor);

		this.initialise();

		if (VERBOSE) {
			this.traceMessage("Creating the clock " + clockURI + ".\n");
		}

		assert	getClock(clockURI) != null :
				new PostconditionException("getClock(clockURI) != null");
		assert	getClock(clockURI).getStartEpochNanos() ==
													unixEpochStartTimeInNanos :
				new PostconditionException(
						"getClock(clockURI).getStartEpochNanos() "
						+ "== unixEpochStartTimeInNanos");
		assert	getClock(clockURI).getStartInstant().equals(startInstant) :
				new PostconditionException(
						"getClock(clockURI).getStartInstant()."
						+ "equals(startInstant)");
		assert	getClock(clockURI).getAccelerationFactor() ==
													accelerationFactor :
				new PostconditionException(
						"getClock(clockURI).getAccelerationFactor() == "
						+ "accelerationFactor");
	}

	/**
	 * initialise the clock server component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		initialise() throws Exception
	{
		this.inboundPort =
				new ClocksServerInboundPort(this.inboundPortURI, this);
		this.inboundPort.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Clock Server component");
			this.tracer.get().setRelativePosition(2, 0);
			this.toggleTracing();		
		}
	}
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();
		this.logMessage("component starts.");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		this.logMessage("component shuts down.");
		try {
			this.inboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services
	// -------------------------------------------------------------------------

	/**
	 * create an accelerated clock with the given parameters associated with the
	 * given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code unixEpochStartTimeInNanos > 0}
	 * pre	{@code startInstant != null}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code return != null}
	 * post	{@code return.getStartEpochNanos() == unixEpochStartTimeInNanos}
	 * post	{@code return.getStartInstant().equals(startInstant)}
	 * post	{@code return.getAccelerationFactor() == accelerationFactor}
	 * post	{@code getClock(clockURI).equals(return)}
	 * </pre>
	 *
	 * @param clockURI					URI designating the created clock.
	 * @param unixEpochStartTimeInNanos	start time in Unix epoch time expressed in nanoseconds.
	 * @param startInstant				start instant to be aligned with the {@code unixEpochStartTimeInNanos}.
	 * @param accelerationFactor		acceleration factor to be applied between elapsed time as {@code Instant} and elapsed time as Unix epoch time in nanoseconds.
	 * @return							the newly created clock.
	 * @throws Exception				<i>to do</i>.
	 */
	public AcceleratedClock		createClock(
		String clockURI,
		long unixEpochStartTimeInNanos,
		Instant	startInstant,
		double accelerationFactor
		) throws Exception
	{
		if (VERBOSE) {
			this.traceMessage(
					"Verifying preconditions before creating the clock " +
					clockURI + ".\n");
		}
		assert	clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"clockURI != null && !clockURI.isEmpty()");
		assert	unixEpochStartTimeInNanos > 0 :
				new PreconditionException("unixEpochStartTimeInNanos > 0");
		assert	startInstant != null :
				new PreconditionException("startInstant != null");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		if (VERBOSE) {
			this.traceMessage("Creating the clock " + clockURI + ".\n");
		}

		AcceleratedClock ret =
				new AcceleratedClock(clockURI,
									 unixEpochStartTimeInNanos,
									 startInstant,
									 accelerationFactor);

		if (VERBOSE) {
			this.traceMessage("Clock " + clockURI + " created.\n");
		}

		this.clocksLock.lock();
		try {
			CompletableFuture<AcceleratedClock> f = this.clocks.get(clockURI);
			if (VERBOSE) {
				this.traceMessage("Completable future is " + f + ".\n");
			}
			if (f == null)  {
				f = new CompletableFuture<AcceleratedClock>();
				this.clocks.put(clockURI, f);
			}
			if (VERBOSE) {
				this.traceMessage("Completing f with " + ret + ".\n");
			}
			f.complete(ret);
		} finally {
			this.clocksLock.unlock();
		}

		if (VERBOSE) {
			this.traceMessage(
					"Verifying postconditions before returning the clock " +
					clockURI + ".\n");
		}

		assert	ret.getStartEpochNanos() == unixEpochStartTimeInNanos :
				new PostconditionException(
						"return.getStartEpochNanos() == "
												+ "unixEpochStartTimeInNanos");
		assert	ret.getStartInstant().equals(startInstant) :
				new PostconditionException(
						"return.getStartInstant().equals(startInstant)");
		assert	ret.getAccelerationFactor() == accelerationFactor :
				new PostconditionException(
						"return.getAccelerationFactor() == accelerationFactor");
		assert	getClock(clockURI).equals(ret) :
				new PostconditionException("getClock(clockURI).equals(return)");

		if (VERBOSE) {
			this.traceMessage(
					"Returning the clock " + clockURI + ".\n");
		}

		return ret;
	}

	/**
	 * return the clock associated with {@code clockURI} of null if none; the
	 * result will be serialised and deserialised if the call is made among
	 * distributed components, hence creating a copy of the clock local to the
	 * receiver process using the physical clock of the receiver host as time
	 * base for evolution; the call will block and wait if the clock of the
	 * given URI has not been created yet.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param clockURI		URI of a previously created clock.
	 * @return				the clock associated with {@code clockURI} or null if none.
	 * @throws Exception	<i>to do</i>.
	 */
	public AcceleratedClock	getClock(String clockURI) throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("Getting the clock " + clockURI + ".\n");
		}

		this.clocksLock.lock();
		CompletableFuture<AcceleratedClock> f = null;
		try {
			f = this.clocks.get(clockURI);
			if (f == null)  {
				f = new CompletableFuture<AcceleratedClock>();
				this.clocks.put(clockURI, f);
			}
		} finally {
			this.clocksLock.unlock();
		}

		// As getClock may block if the clock has not been created yet, the
		// call is made using the caller thread hence avoiding the need for
		// an unbounded number of internal threads in the component to take
		// care of an unlimited number of callers.
		return f.get();
	}
}
// -----------------------------------------------------------------------------
