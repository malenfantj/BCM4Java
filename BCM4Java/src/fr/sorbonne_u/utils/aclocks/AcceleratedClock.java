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

import java.io.Serializable;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>AcceleratedClock</code> implements a relative clock working
 * on an accelerated time based on the class {@code Instant} in parallel with
 * a Unix epoch time underlying system clock; its main purpose is to ease the
 * implementation of time-triggered test scenarios, especially for
 * cyber-physical but also for timed test scenarios in general.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The purpose of this class is to provide a clock that keeps track of the
 * time in two different time lines. One time line is given by instants,
 * represented as {@code java.time.Instant}, that is used to express scenarios,
 * especially in test scenarios. The second time line is the Unix epoch time
 * provided by the hardware clock. The goal is that this clock can be shared
 * among several processes to synchronise their actions on the same accelerated
 * time in a time-triggered approach. Moreover, an acceleration factor can be
 * given so that the instant time is keep synchronised with the Unix epoch time
 * modulo the acceleration factor from some start time:
 * </p>
 * <pre>
 *                         start instant              accelerated instant
 * Accelerated instants -------|------------------------------|--------------
 *                              \             ^              /
 *                               \            |             /
 *                                \      acceleration      /
 *                                 \        factor        /
 *                                  \         |          /
 * Unix epoch time      -------------|------------------|-------------------
 *                             start time         Unix epoch time
 * </pre>
 * <p>
 * With this class, one can plan scenarios in human readable {@code Instant} but
 * can compute delays and time in Unix epoch time corresponding to the instants
 * in order to schedule tasks on Java thread pools so that they execute in an
 * accelerated real time line.
 * </p>
 * <p>
 * The clock is instantiated with an acceleration factor, a start instant and a
 * start time corresponding to the Unix epoch system time. By default, the Unix
 * epoch start time is given by {@code System.currentTimeMillis()}, but a
 * specific value can be given to help creating several clock instances with the
 * same Unix epoch start time as base time for scheduling. Also, by default, the
 * start instant is given by {@code Instant.now()}, but a specific value can be
 * given to simplify the elaboration of time scenarios with an easily manageable
 * base time. The class offers methods to go from one time line to the other.
 * </p>
 * <p>
 * To go from a Unix epoch system time to an instant, there are:
 * </p>
 * <ul>
 * <li>{@code currentInstant} takes the current Unix epoch system time given
 *   by {@code System.currentTimeMillis()} and converts it in an instant based
 *   on the start instant and taking into account the acceleration factor.</li>
 * <li>{@code instantOfEpochInNanos} performs the same computation but with a
 *   given a Unix epoch system time in nanoseconds passed as parameter rather
 *   than the current system time.</li>
 * </ul>
 * <p>
 * To compute real time delays from instants, there are:
 * </p>
 * <ul>
 * <li>{@code unixEpochTimeInNanosFromInstant} takes an accelerated instant
 *   and return the corresponding Unix epoch time in nanoseconds given the
 *   Unix epoch start time.</li>
 * <li>{@code nanoDelayUntilAcceleratedInstant} takes an accelerated instant
 *   return the delay in nanoseconds from the current system time to the moment
 *   in Unix epoch real time at which some computation must be scheduled on a
 *   thread to execute as if it happens at the provided instant in accelerated
 *   time. For example, assume a clock accelerated 10 times is started at an
 *   instant {@code I} and a Unix epoch system time T. Then, assume that the
 *   program wants to schedule some computation to be executed 10 minutes after
 *   {@code I} in accelerated time. In this situation, the call
 *   {@code nanoDelayUntilAcceleratedInstant(I.plusSeconds(600))} returns
 *   the delay d in real time (and in nanoseconds) to be used immediately in
 *   order to schedule the computation so that it will execute at
 *   {@code I.plusSeconds(600)} in accelerated time.</li>
 * <li>{@code nanoDelayToAcceleratedInstantFromEpochTime} performs the same
 *   computation but to give a delay from a given system epoch time in
 *   milliseconds rather than from the current system time.</li>
 * </ul>
 * <p>
 * The computations of the delays are correct only after the start time has been
 * reached. The method {@code startTimeNotReached} allows to test if the start
 * has not been reached yet. When the start time is still in the future, the
 * method {@code waitUntilStart} will block the calling thread until the start
 * time is reached. The method {@code waitingDelayUntilStartInMillis} returns
 * the remaining time in milliseconds until the start time.
 * </p>
 * 
 * <p><i>Good practices</i></p>
 * 
 * <p>
 * On standard (non real time) Unix systems, hardware clocks are in practice
 * precise to the milliseconds and even to the tens of milliseconds. Therefore,
 * care must be taken not to plan scenarios where the delays between scheduled
 * actions are less than these values. And indeed, this is even more important
 * when large acceleration factors are used as the acceleration factor equally
 * applies to the precision (for example, on my Mac, the precision is usually
 * around a few milliseconds; with an acceleration factor of 10, the useful
 * precision (shortest accurate delays between scheduled actions before
 * acceleration) is more a few tens of milliseconds, and with an acceleration
 * factor 100, the precision is no more than a few hundreds of milliseconds,
 * hence a few tenth of a second.
 * </p>
 * <p>
 * The {@code Instant} class offers quite flexible ways to compute instants from
 * other instants, such as adding some amount of seconds to an existing instant
 * with the method {@code Instant.plusSeconds(long)}. This allows an
 * {@code AcceleratedClock} user to build scenario patterns like:
 * </p>
 * <pre>
 * AcceleratedClock ac =new AcceleratedClock(
 *                              TimeUnit.MILLISECONDS.toNanos(
 *                                      System.currentTimeMillis() + 5000L),
 *                          Instant.parse("2022-11-07T06:00:00.000Z"),
 *                          1.0);
 *          ac.waitUntilStart();
 *          Instant observed = ac.currentInstant();
 *          Instant nextAction = observed.plusSeconds(5);
 *          long delay = ac.nanoDelayUntilInstant(nextAction);
 *          Thread.sleep(TimeUnit.NANOSECONDS.toMillis(delay));
 *          // perform some action
 * </pre>
 * <p>
 * This kind of scenarios tends to drift as the imprecision of the hardware
 * clock and of the scheduling will accumulate over the computation of series
 * of instants from imprecise observed instants.
 * </p>
 * <p>
 * To avoid as much as possible imprecision in test scenarios executions, it
 * is better to:
 * </p>
 * <ol>
 * <li>Choose an acceleration factor such that when applied to the shortest
 *   delay between two scheduled actions, it will not give a waiting time
 *   that is under 10 milliseconds between two successive actions (to be adapted
 *   to the precision of your operating system and hardware).</li>
 * <li>Prepare test scenarios with absolute {@code Instant} only <i>i.e.</i>,
 *   instants that are created from a string like
 *   {@code Instant.parse("2022-11-07T06:00:00.000Z")}. With absolute instants,
 *   the computation of delays tends to absorb the drift along the scenario
 *   execution, hence leaving less imprecision over whole executions.
 * </ol>
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
 * invariant	{@code getAccelerationFactor() > 0.0}
 * invariant	{@code getStartEpochNanos() > 0}
 * invariant	{@code getStartInstant() != null}
 * </pre>
 * 
 * <p>Created on : 2022-11-04</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			AcceleratedClock
implements	Serializable
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** when true, trace actions.											*/
	public static boolean		VERBOSE = false;

	/** the URI of this clock.												*/
	protected final String		clockURI;
	/** acceleration factor between durations in Unix epoch system time
	 *  and durations between instants.										*/
	protected final double		accelerationFactor;
	/** start time in Unix epoch system time.								*/
	protected final long		unixEpochStartTimeInNanos;
	/** start instant.														*/
	protected final Instant		startInstant;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an accelerated clock with the given acceleration factor, taking
	 * the both start times as now.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getStartEpochNanos() <= TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis())}
	 * post	{@code getStartInstant().equals(Instant.ofEpochMilli(TimeUnit.NANOSECONDS.toMillis(getStartEpochNanos())))}
	 * post	{@code getAccelerationFactor() == accelerationFactor}
	 * </pre>
	 *
	 * @param clockURI				URI attributed to the clock.
	 * @param accelerationFactor	acceleration factor to be applied.
	 */
	public				AcceleratedClock(
		String clockURI,
		double accelerationFactor)
	{
		super();

		assert	clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"clockURI != null && !clockURI.isEmpty()");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		long currentTime = System.currentTimeMillis();
		this.unixEpochStartTimeInNanos =
								TimeUnit.MILLISECONDS.toNanos(currentTime);
		this.startInstant = Instant.ofEpochMilli(currentTime);
		this.accelerationFactor = accelerationFactor;
		this.clockURI = clockURI;

		assert	getStartEpochNanos() <=
					TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()) :
				new PostconditionException(
						"getStartEpochNanos() <= TimeUnit.MILLISECONDS.toNanos("
						+ "System.currentTimeMillis())");
		assert	getStartInstant().equals(Instant.ofEpochMilli(
						TimeUnit.NANOSECONDS.toMillis(getStartEpochNanos()))) :
				new PostconditionException(
						"getStartInstant().equals(Instant.ofEpochMilli("
						+ "TimeUnit.NANOSECONDS.toMillis("
						+ "getStartEpochNanos())))");
	}

	/**
	 * create an accelerated clock with the given start time in Unix epoch time
	 * in nanoseconds and the given acceleration factor taking the start instant
	 * as {@code Instant.ofEpochMilli(unixEpochStartTimeInNanos/1000000)}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code unixEpochStartTimeInNanos > 0}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getStartEpochNanos() == unixEpochStartTimeInNanos}
	 * post	{@code getStartInstant().equals(Instant.ofEpochMilli(TimeUnit.NANOSECONDS.toMillis(getStartEpochNanos())))}
	 * post	{@code getAccelerationFactor() == accelerationFactor}
	 * </pre>
	 *
	 * @param clockURI				URI attributed to the clock.
	 * @param unixEpochStartTimeInNanos	start time in Unix epoch time in nanoseconds.
	 * @param accelerationFactor		acceleration factor to be applied.
	 */
	public				AcceleratedClock(
		String clockURI,
		long unixEpochStartTimeInNanos,
		double accelerationFactor
		)
	{
		assert	clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"clockURI != null && !clockURI.isEmpty()");
		assert	unixEpochStartTimeInNanos > 0 :
				new PreconditionException("unixEpochStartTimeInNanos > 0");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		this.unixEpochStartTimeInNanos = unixEpochStartTimeInNanos;
		this.startInstant =
			Instant.ofEpochMilli(
				TimeUnit.NANOSECONDS.toMillis(this.unixEpochStartTimeInNanos));
		this.accelerationFactor = accelerationFactor;
		this.clockURI = clockURI;

		assert	getStartInstant().equals(Instant.ofEpochMilli(
					TimeUnit.NANOSECONDS.toMillis(getStartEpochNanos()))) :
				new PostconditionException(
						"getStartInstant().equals(Instant.ofEpochMilli("
						+ "TimeUnit.NANOSECONDS.toMillis("
						+ "unixEpochStartTimeInNanos)))");
	}

	/**
	 * create an accelerated clock with the given start instant and acceleration
	 * factor, taking the start time in Unix epoch time as now.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code startInstant != null}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getStartEpochNanos() <= TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis())}
	 * post	{@code getStartInstant().equals(startInstant)}
	 * post	{@code getAccelerationFactor() == accelerationFactor}
	 * </pre>
	 *
	 * @param clockURI				URI attributed to the clock.
	 * @param startInstant			start time as {@code Instant}.
	 * @param accelerationFactor	acceleration factor to be applied.
	 */
	public				AcceleratedClock(
		String clockURI,
		Instant	startInstant,
		double accelerationFactor
		)
	{
		assert	clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"clockURI != null && !clockURI.isEmpty()");
		assert	startInstant != null :
				new PreconditionException("startInstant != null");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		this.unixEpochStartTimeInNanos =
					TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
		this.startInstant = startInstant;
		this.accelerationFactor = accelerationFactor;
		this.clockURI = clockURI;

		assert	getStartEpochNanos() <=
					TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()) :
				new PostconditionException(
						"getStartEpochNanos() <= TimeUnit.MILLISECONDS.toNanos("
						+ "System.currentTimeMillis())");
	}

	/**
	 * create an accelerated clock with the given start time in Unix epoch time
	 * in nanoseconds, start instant and acceleration factor.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code unixEpochStartTimeInNanos > 0}
	 * pre	{@code startInstant != null}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getStartEpochNanos() == unixEpochStartTimeInNanos}
	 * post	{@code getStartInstant().equals(startInstant)}
	 * post	{@code getAccelerationFactor() == accelerationFactor}
	 * </pre>
	 *
	 * @param clockURI				URI attributed to the clock.
	 * @param unixEpochStartTimeInNanos	start time in Unix epoch time in nanoseconds.
	 * @param startInstant				start time as {@code Instant}.
	 * @param accelerationFactor		acceleration factor to be applied.
	 */
	public				AcceleratedClock(
		String clockURI,
		long unixEpochStartTimeInNanos,
		Instant	startInstant,
		double accelerationFactor
		)
	{
		assert	clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"clockURI != null && !clockURI.isEmpty()");
		assert	unixEpochStartTimeInNanos > 0 :
				new PreconditionException("unixEpochStartTimeInNanos > 0");
		assert	startInstant != null :
				new PreconditionException("startInstant != null");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		this.unixEpochStartTimeInNanos = unixEpochStartTimeInNanos;
		this.startInstant = startInstant;
		this.accelerationFactor = accelerationFactor;
		this.clockURI = clockURI;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * return the URI of the clock when it is set or null.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && !return.isEmpty()}
	 * </pre>
	 *
	 * @return	the URI of the clock.
	 */
	public String		getClockURI()
	{
		return this.clockURI;
	}

	/**
	 * return an identity of the time reference <i>e.g.</i>, the IP address of
	 * the host which hardware clock serves as reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return						an identity of the time reference <i>e.g.</i>, the IP address of the host which hardware clock serves as reference.
	 * @throws UnknownHostException	<i>to do </i>.
	 */
	public String		getTimeReferenceIdentity() throws UnknownHostException
	{
		return java.net.Inet4Address.getLocalHost().getHostAddress();
	}

	/**
	 * return the acceleration factor associated to this clock.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret > 0.0}
	 * </pre>
	 *
	 * @return	the acceleration factor associated to this clock.
	 */
	public double		getAccelerationFactor()
	{
		return this.accelerationFactor;
	}

	/**
	 * return the start time of this clock as a Unix epoch time in nanoseconds.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret > 0}
	 * </pre>
	 *
	 * @return	the start time of this clock as a Unix epoch time in nanoseconds.
	 */
	public long			getStartEpochNanos()
	{
		return this.unixEpochStartTimeInNanos;
	}

	/**
	 * return the start time of this clock as an {@code Instant}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return	the start time of this clock as an {@code Instant}.
	 */
	public Instant		getStartInstant()
	{
		return this.startInstant;
	}

	/**
	 * return true if the start time of the clock has not been reached.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the start time of the clock has not been reached.
	 */
	public boolean		startTimeNotReached()
	{
		return this.unixEpochStartTimeInNanos
					> TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
	}

	/**
	 * return true if the start time of the clock has not been reached at Unix
	 * epoch time {@code current}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param current	current Unix epoch time in milliseconds (as given by {@code System.currentTimeMillis()}).
	 * @return			true if the start time of the clock has not been reached at Unix epoch time {@code current}.
	 */
	protected boolean	startTimeNotReached(long current)
	{
		assert	current > 0 : new PreconditionException("current > 0");
		return this.unixEpochStartTimeInNanos
									> TimeUnit.MILLISECONDS.toNanos(current);
	}

	/**
	 * return the time in milliseconds to wait until the start time defined for
	 * this clock; if the result is less than 0, the start time is passed.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code startTimeNotReached()}
	 * post	{@code ret > 0}
	 * </pre>
	 *
	 * @return	the time in milliseconds to wait until the start time defined for this clock.
	 */
	public long			waitingDelayUntilStartInMillis()
	{
		long current = System.currentTimeMillis();
		assert	startTimeNotReached(current) :
				new PreconditionException("startTimeNotReached(current)");

		return TimeUnit.NANOSECONDS.toMillis(
							this.unixEpochStartTimeInNanos
									- TimeUnit.MILLISECONDS.toNanos(current));
	}

	/**
	 * block the calling thread until the start time of the clock has been
	 * reached.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code startTimeNotReached()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws InterruptedException	<i>to do</i>.
	 */
	public void			waitUntilStart() throws InterruptedException
	{
		long current = System.currentTimeMillis();
		assert	startTimeNotReached(current) :
				new PreconditionException("startTimeNotReached(current)");

		long delay = TimeUnit.NANOSECONDS.toMillis(
							this.unixEpochStartTimeInNanos
									- TimeUnit.MILLISECONDS.toNanos(current));
		if (delay > 0) {
			Thread.sleep(delay);
		} else {
			throw new RuntimeException(
							"AcceleratedClock#waitUntilStart " +
							" negative delay until start, no waiting.");
		}
	}

	/**
	 * return the current accelerated instant computed from the current
	 * system time and the acceleration factor.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !startTimeNotReached()}
	 * post	{@code return != null}
	 * post	{@code return.equals(getStartInstant()) || return.isAfter(getStartInstant())}
	 * </pre>
	 *
	 * @return	the current accelerated instant.
	 */
	public Instant		currentInstant()
	{
		long current = System.currentTimeMillis();
		assert	!startTimeNotReached(current) :
				new PreconditionException("!startTimeNotReached(current)");

		long currentInNanos = TimeUnit.MILLISECONDS.toNanos(current);
		long elapsedInNanos = currentInNanos - this.getStartEpochNanos();
		long acceleratedElapsedInNanos =
				(long) (elapsedInNanos * this.getAccelerationFactor());
		long baseInNanos = 
				TimeUnit.MILLISECONDS.toNanos(this.startInstant.toEpochMilli());

		Instant ret = Instant.ofEpochMilli(
							TimeUnit.NANOSECONDS.toMillis(
									baseInNanos + acceleratedElapsedInNanos));

		if (VERBOSE) {
			System.out.println(
					"AcceleratedClock#currentInstant startInstant "
													+ this.getStartInstant());
			System.out.println(
					"AcceleratedClock#currentInstant returns " + ret);
		}

		assert	ret != null : new PostconditionException("return != null");
		assert	ret.equals(getStartInstant()) || ret.isAfter(getStartInstant()) :
				new PostconditionException(
						"return.equals(getStartInstant()) || "
						+ "return.isAfter(getStartInstant())");

		return ret;
	}

	/**
	 * return the accelerated instant for the Unix epoch time
	 * {@code epochInNanos} computed from the Unix epoch start time and the
	 * acceleration factor.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code epochInNanos > getStartEpochNanos()}
	 * post	{@code return != null}
	 * post	{@code return.equals(getStartInstant()) || return.isAfter(getStartInstant())}
	 * </pre>
	 *
	 * @param epochTimeInNanos	Unix epoch time for which the accelerated instant must be computed.
	 * @return					the accelerated instant corresponding to {@code epochInNanos}.
	 */
	protected Instant	instantOfEpochTimeInNanos(long epochTimeInNanos)
	{
		assert	epochTimeInNanos > this.getStartEpochNanos() :
				new PreconditionException("epochInNanos > getStartEpochNanos()");

		long elapsedInNanos = epochTimeInNanos - this.unixEpochStartTimeInNanos;
		long acceleratedElapsedInNanos =
				(long) (elapsedInNanos * this.accelerationFactor);
		long baseInNanos = 
				TimeUnit.MILLISECONDS.toNanos(this.startInstant.toEpochMilli());

		Instant ret =
			Instant.ofEpochMilli(
					TimeUnit.NANOSECONDS.toMillis(
									baseInNanos + acceleratedElapsedInNanos));

		assert	ret != null : new PostconditionException("return != null");
		assert	ret.equals(getStartInstant()) || ret.isAfter(getStartInstant()) :
				new PostconditionException(
						"return.equals(getStartInstant()) || "
						+ "return.isAfter(getStartInstant())");

		return ret;
	}

	/**
	 * compute the Unix epoch real time corresponding to an accelerated instant
	 * {@code i}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code i.isAfter(getStartInstant())}
	 * post	{@code return > getStartEpochNanos()}
	 * </pre>
	 *
	 * @param i	accelerated instant from which to compute a Unix epoch real time.
	 * @return	the Unix epoch real time corresponding to {@code i}.
	 */
	public long			unixEpochTimeInNanosFromInstant(Instant i)
	{
		assert	i.isAfter(getStartInstant()) :
				new PreconditionException("i.isAfter(getStartInstant())");

		long acceleratedElapsedInNanos =
				TimeUnit.MILLISECONDS.toNanos(
						(i.toEpochMilli() - this.startInstant.toEpochMilli()));
		long elapsedInNanos =
				(long) (acceleratedElapsedInNanos/this.accelerationFactor);
		long ret = this.unixEpochStartTimeInNanos + elapsedInNanos;
		
		assert	ret > this.getStartEpochNanos() :
				new PostconditionException("return > getStartEpochNanos()");

		return ret;
	}

	/**
	 * compute real time delay from now to the real time at which to schedule
	 * immediately the execution given an accelerated {@code Instant} at which
	 * the execution must occur. For example, if C is the current time
	 * (from {@code System.currentTimeMillis()}) and I the accelerated
	 * {@code Instant}, I is first converted into a real Unix epoch time R using
	 * the stored start {@code Instant} and the Unix epoch time start time,
	 * which give the result as R - C.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code acceleratedInstant != null && acceleratedInstant.isAfter(currentInstant())}
	 * post	{@code return >= 0}
	 * </pre>
	 *
	 * @param i	accelerated {@code Instant} at which to the execution must occur.
	 * @return						real time delay from now to the real time at which to schedule immediately the execution.
	 */
	public long			nanoDelayUntilInstant(Instant i)
	{
		assert	i != null && i.isAfter(currentInstant()) :
				new PreconditionException(
								"i != null && i.isAfter(currentInstant())");

		long baseInNanos =
				TimeUnit.MILLISECONDS.toNanos(this.startInstant.toEpochMilli());
		long epochOfInstantInNanos =
				TimeUnit.MILLISECONDS.toNanos(i.toEpochMilli());
		long accElapsedInNanos = epochOfInstantInNanos - baseInNanos;

		long realElapsedInNanos =
				(long) (accElapsedInNanos/this.accelerationFactor);
		long forseenInNanos =
				this.unixEpochStartTimeInNanos + realElapsedInNanos;
		long currentInNanos =
				TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
		long delayInNanos = forseenInNanos - currentInNanos;

		if (VERBOSE) {
			System.out.println(
					"AcceleratedClock#nanoDelayUntilAcceleratedInstant 1 "
					+ accElapsedInNanos + " ++ " + realElapsedInNanos);
			System.out.println(
					"AcceleratedClock#nanoDelayUntilAcceleratedInstant 2 "
					+ this.unixEpochStartTimeInNanos + " ++ "
								+ currentInNanos + " -- " + forseenInNanos);
		}

		assert	delayInNanos >= 0 : new PostconditionException("return >= 0");

		return delayInNanos;
	}

	/**
	 * compute real time delay from {@code baseEpochTimeInMillis} to the real
	 * time at which to schedule the execution given an accelerated
	 * {@code Instant} at which this execution must occur in accelerated time.
	 * For example, if C is the {@code baseEpochTimeInMillis} and I the
	 * accelerated {@code Instant}, I is first converted into a real Unix epoch
	 * time R using the stored start {@code Instant} and the Unix epoch time
	 * start time from which the result is obtained as R - C.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code TimeUnit.MILLISECONDS.toNanos(baseEpochTimeInMillis) > getStartEpochNanos()}
	 * pre	{@code acceleratedInstant != null && acceleratedInstant.isAfter(instantOfEpochInNanos(TimeUnit.MILLISECONDS.toNanos(baseEpochTimeInMillis))}
	 * post	{@code return > 0}
	 * </pre>
	 *
	 * @param baseEpochTimeInMillis	base time from which the delay is computed as Unix epoch time in milliseconds.
	 * @param acceleratedInstant	accelerated {@code Instant} at which to the execution must occur.
	 * @return						real time delay from now to the real time at which to schedule immediately the execution.
	 */
	public long			nanoDelayToAcceleratedInstantFromEpochTime(
		long baseEpochTimeInMillis,
		Instant acceleratedInstant
		)
	{
		assert	TimeUnit.MILLISECONDS.toNanos(baseEpochTimeInMillis) >
														getStartEpochNanos() :
				new PreconditionException(
					"TimeUnit.MILLISECONDS.toNanos(baseEpochTimeInMillis)"
					+ " > getStartEpochNanos()");
		assert	acceleratedInstant != null &&
					acceleratedInstant.isAfter(
						instantOfEpochTimeInNanos(
								TimeUnit.MILLISECONDS.toNanos(
													baseEpochTimeInMillis))) :
				new PreconditionException(
							"acceleratedInstant != null && "
							+ "acceleratedInstant.isAfter("
							+ "instantOfEpochInNanos("
							+ "TimeUnit.MILLISECONDS.toNanos("
							+ "baseEpochTimeInMillis)))");

		long baseInNanos =
				TimeUnit.MILLISECONDS.toNanos(this.startInstant.toEpochMilli());
		long epochOfInstantInNanos =
				TimeUnit.MILLISECONDS.toNanos(acceleratedInstant.toEpochMilli());
		long realElapsedInNanos =
				(long) ((epochOfInstantInNanos - baseInNanos)/
													this.accelerationFactor);
		long forseenInNanos =
				this.unixEpochStartTimeInNanos + realElapsedInNanos;
		long delayInNanos =
				forseenInNanos -
						TimeUnit.MILLISECONDS.toNanos(baseEpochTimeInMillis);

		if (VERBOSE) {
			System.out.println(
				"AcceleratedClock#nanoDelayToAcceleratedInstantFromEpochTime "
				+ this.unixEpochStartTimeInNanos + " ++ "
				+ TimeUnit.MILLISECONDS.toNanos(baseEpochTimeInMillis)
				+ " -- " + forseenInNanos);
		}

		assert	delayInNanos > 0 : new PostconditionException("return > 0");

		return delayInNanos;
	}

	// -------------------------------------------------------------------------
	// Tests
	// -------------------------------------------------------------------------

	/**
	 * perform some tests.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param args	command line arguments.
	 */
	public static void	main(String[] args)
	{
		try {
			Instant start = Instant.parse("2022-11-07T06:00:00.000Z");
			Instant[] actions = new Instant[10];
			actions[0] = Instant.parse("2022-11-07T06:00:05.000Z");
			actions[1] = Instant.parse("2022-11-07T06:00:10.000Z");
			actions[2] = Instant.parse("2022-11-07T06:00:15.000Z");
			actions[3] = Instant.parse("2022-11-07T06:00:20.000Z");
			actions[4] = Instant.parse("2022-11-07T06:00:25.000Z");
			actions[5] = Instant.parse("2022-11-07T06:00:30.000Z");
			actions[6] = Instant.parse("2022-11-07T06:00:35.000Z");
			actions[7] = Instant.parse("2022-11-07T06:00:40.000Z");
			actions[8] = Instant.parse("2022-11-07T06:00:45.000Z");
			actions[9] = Instant.parse("2022-11-07T06:00:50.000Z");

			System.out.print('.');
			double accFactor = 1.0;
			StringBuffer logs = new StringBuffer("Run with acceleration factor: ");
			logs.append(accFactor);
			logs.append('\n');
			long realStartTime = System.currentTimeMillis();
			AcceleratedClock clock =
					new AcceleratedClock(
							"testURI1",
							TimeUnit.MILLISECONDS.toNanos(
									System.currentTimeMillis() + 5000L),
							start,
							accFactor);
			System.out.print('.');
			long waitingTime = clock.waitingDelayUntilStartInMillis();
			clock.waitUntilStart();
			Instant observedStart = clock.currentInstant();
			logs.append("starting at ");
			logs.append(clock.getStartInstant());
			logs.append('\n');
			logs.append("waiting time until start ");
			logs.append(waitingTime);
			logs.append('\n');
			logs.append("observedStart: ");
			logs.append(observedStart);
			logs.append('\n');
			System.out.print('.');
			for (int j = 0 ; j < actions.length ; j++) {
				long d = clock.nanoDelayUntilInstant(actions[j]);
				Thread.sleep(TimeUnit.NANOSECONDS.toMillis(d));
				Instant observedAction = clock.currentInstant();
				logs.append("action");
				logs.append(j);
				logs.append(": ");
				logs.append(actions[j]);
				logs.append('\n');
				logs.append("delay to ");
				logs.append(j);
				logs.append(": ");
				logs.append(d);
				logs.append('\n');
				logs.append("observedAction");
				logs.append(j);
				logs.append(": ");
				logs.append(observedAction);
				logs.append('\n');
			}
			System.out.println('.');
			long realEndTime = System.currentTimeMillis();
			logs.append("duration (in millis): ");
			logs.append((realEndTime - realStartTime));
			logs.append('\n');
			// printing is done at the end to avoid perturbing the timing too
			// much, yet small discrepancies are expected
			System.out.println(logs.toString());

			System.out.print('.');
			accFactor = 10.0;
			logs = new StringBuffer("\nRun with acceleration factor: ");
			logs.append(accFactor);
			logs.append('\n');
			realStartTime = System.currentTimeMillis();
			clock = new AcceleratedClock(
							"testURI2",
							TimeUnit.MILLISECONDS.toNanos(
									System.currentTimeMillis() + 5000L),
							start,
							accFactor);
			System.out.print('.');
			waitingTime = clock.waitingDelayUntilStartInMillis();
			clock.waitUntilStart();
			observedStart = clock.currentInstant();
			logs.append("starting at ");
			logs.append(clock.getStartInstant());
			logs.append('\n');
			logs.append("waiting time until start ");
			logs.append(waitingTime);
			logs.append('\n');
			logs.append("observedStart: ");
			logs.append(observedStart);
			logs.append('\n');
			System.out.print('.');
			for (int j = 0 ; j < actions.length ; j++) {
				long d = clock.nanoDelayUntilInstant(actions[j]);
				Thread.sleep(TimeUnit.NANOSECONDS.toMillis(d));
				Instant observedAction = clock.currentInstant();
				logs.append("action");
				logs.append(j);
				logs.append(": ");
				logs.append(actions[j]);
				logs.append('\n');
				logs.append("delay to ");
				logs.append(j);
				logs.append(": ");
				logs.append(d);
				logs.append('\n');
				logs.append("observedAction");
				logs.append(j);
				logs.append(": ");
				logs.append(observedAction);
				logs.append('\n');
			}
			System.out.println('.');
			realEndTime = System.currentTimeMillis();
			logs.append("duration (in millis): ");
			logs.append((realEndTime - realStartTime));
			logs.append('\n');
			// printing is done at the end to avoid perturbing the timing too
			// much, yet small discrepancies are expected
			System.out.println(logs.toString());

			System.out.print('.');
			accFactor = 100.0;
			logs = new StringBuffer("\nRun with acceleration factor: ");
			logs.append(accFactor);
			logs.append('\n');
			realStartTime = System.currentTimeMillis();
			System.out.print('.');
			clock = new AcceleratedClock(
							"testURI3",
							TimeUnit.MILLISECONDS.toNanos(
									System.currentTimeMillis() + 5000L),
							start,
							accFactor);
			System.out.print('.');
			waitingTime = clock.waitingDelayUntilStartInMillis();
			clock.waitUntilStart();
			observedStart = clock.currentInstant();
			logs.append("starting at ");
			logs.append(clock.getStartInstant());
			logs.append('\n');
			logs.append("waiting time until start ");
			logs.append(waitingTime);
			logs.append('\n');
			logs.append("observedStart: ");
			logs.append(observedStart);
			logs.append('\n');
			System.out.print('.');
			for (int j = 0 ; j < actions.length ; j++) {
				long d = clock.nanoDelayUntilInstant(actions[j]);
				Thread.sleep(TimeUnit.NANOSECONDS.toMillis(d));
				Instant observedAction = clock.currentInstant();
				logs.append("action");
				logs.append(j);
				logs.append(": ");
				logs.append(actions[j]);
				logs.append('\n');
				logs.append("delay to ");
				logs.append(j);
				logs.append(": ");
				logs.append(d);
				logs.append('\n');
				logs.append("observedAction");
				logs.append(j);
				logs.append(": ");
				logs.append(observedAction);
				logs.append('\n');
			}
			System.out.println('.');
			realEndTime = System.currentTimeMillis();
			logs.append("duration (in millis): ");
			logs.append((realEndTime - realStartTime));
			logs.append('\n');
			// printing is done at the end to avoid perturbing the timing too
			// much, yet small discrepancies are expected
			System.out.println(logs.toString());

		} catch (InterruptedException e) {
			throw new RuntimeException(e) ;
		}
	}
}
// -----------------------------------------------------------------------------
