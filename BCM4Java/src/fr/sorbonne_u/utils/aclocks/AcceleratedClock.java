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
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getStartEpochNanos() <= TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis())}
	 * post	{@code getStartInstant().equals(Instant.ofEpochMilli(TimeUnit.NANOSECONDS.toMillis(getStartEpochNanos())))}
	 * post	{@code getAccelerationFactor() == accelerationFactor}
	 * </pre>
	 *
	 * @param accelerationFactor	acceleration factor to be applied.
	 */
	public				AcceleratedClock(double accelerationFactor)
	{
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		long currentTime = System.currentTimeMillis();
		this.unixEpochStartTimeInNanos =
								TimeUnit.MILLISECONDS.toNanos(currentTime);
		this.startInstant = Instant.ofEpochMilli(currentTime);
		this.accelerationFactor = accelerationFactor;

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
	 * pre	{@code unixEpochStartTimeInNanos > 0}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getStartEpochNanos() == unixEpochStartTimeInNanos}
	 * post	{@code getStartInstant().equals(Instant.ofEpochMilli(TimeUnit.NANOSECONDS.toMillis(getStartEpochNanos())))}
	 * post	{@code getAccelerationFactor() == accelerationFactor}
	 * </pre>
	 *
	 * @param unixEpochStartTimeInNanos	start time in Unix epoch time in nanoseconds.
	 * @param accelerationFactor		acceleration factor to be applied.
	 */
	public				AcceleratedClock(
		long unixEpochStartTimeInNanos,
		double accelerationFactor
		)
	{
		assert	unixEpochStartTimeInNanos > 0 :
				new PreconditionException("unixEpochStartTimeInNanos > 0");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		this.unixEpochStartTimeInNanos = unixEpochStartTimeInNanos;
		this.startInstant =
			Instant.ofEpochMilli(
				TimeUnit.NANOSECONDS.toMillis(this.unixEpochStartTimeInNanos));
		this.accelerationFactor = accelerationFactor;

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
	 * pre	{@code startInstant != null}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getStartEpochNanos() <= TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis())}
	 * post	{@code getStartInstant().equals(startInstant)}
	 * post	{@code getAccelerationFactor() == accelerationFactor}
	 * </pre>
	 *
	 * @param startInstant			start time as {@code Instant}.
	 * @param accelerationFactor	acceleration factor to be applied.
	 */
	public				AcceleratedClock(
		Instant	startInstant,
		double accelerationFactor
		)
	{
		assert	startInstant != null :
				new PreconditionException("startInstant != null");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		this.unixEpochStartTimeInNanos =
					TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
		this.startInstant = startInstant;
		this.accelerationFactor = accelerationFactor;

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
	 * pre	{@code unixEpochStartTimeInNanos > 0}
	 * pre	{@code startInstant != null}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getStartEpochNanos() == unixEpochStartTimeInNanos}
	 * post	{@code getStartInstant().equals(startInstant)}
	 * post	{@code getAccelerationFactor() == accelerationFactor}
	 * </pre>
	 *
	 * @param unixEpochStartTimeInNanos	start time in Unix epoch time in nanoseconds.
	 * @param startInstant				start time as {@code Instant}.
	 * @param accelerationFactor		acceleration factor to be applied.
	 */
	public				AcceleratedClock(
		long unixEpochStartTimeInNanos,
		Instant	startInstant,
		double accelerationFactor
		)
	{
		assert	unixEpochStartTimeInNanos > 0 :
				new PreconditionException("unixEpochStartTimeInNanos > 0");
		assert	startInstant != null :
				new PreconditionException("startInstant != null");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		this.unixEpochStartTimeInNanos = unixEpochStartTimeInNanos;
		this.startInstant = startInstant;
		this.accelerationFactor = accelerationFactor;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

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
	 * return	true if the start time of the clock has not been reached.
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
		return TimeUnit.NANOSECONDS.toMillis(this.unixEpochStartTimeInNanos)
												> System.currentTimeMillis();
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
		assert	this.startTimeNotReached() :
				new PreconditionException("startTimeNotReached()");

		return TimeUnit.NANOSECONDS.toMillis(this.unixEpochStartTimeInNanos)
												- System.currentTimeMillis();
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
		assert	this.startTimeNotReached() :
				new PreconditionException("startTimeNotReached()");

		long delay = this.waitingDelayUntilStartInMillis();
		if (delay > 0) {
			Thread.sleep(delay);
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
	 * post	{@code return != null && return.isAfter(getStartInstant())}
	 * </pre>
	 *
	 * @return	the current accelerated instant.
	 */
	public Instant		currentInstant()
	{
		assert	!startTimeNotReached() :
				new PreconditionException("!startTimeNotReached()");

		long currentInNanos =
				TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
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
					"AcceleratedClock#currentInstant " +
					Instant.now() + " -- " + elapsedInNanos + " -- "+ ret);
		}

		assert	ret != null && ret.isAfter(getStartInstant()) :
				new PostconditionException(
						"return != null && return.isAfter(getStartInstant())");

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
	 * post	{@code return != null && return.isAfter(getStartInstant())}
	 * </pre>
	 *
	 * @param epochTimeInNanos	Unix epoch time for which the accelerated instant must be computed.
	 * @return					the accelerated instant corresponding to {@code epochInNanos}.
	 */
	public Instant		instantOfEpochTimeInNanos(long epochTimeInNanos)
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
					(long) (baseInNanos + acceleratedElapsedInNanos)/1000000);

		assert	ret != null && ret.isAfter(getStartInstant()) :
				new PostconditionException(
						"return != null && return.isAfter(getStartInstant())");

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
	 * @param acceleratedInstant	accelerated {@code Instant} at which to the execution must occur.
	 * @return						real time delay from now to the real time at which to schedule immediately the execution.
	 */
	public long			nanoDelayUntilAcceleratedInstant(
		Instant acceleratedInstant
		)
	{
		assert	acceleratedInstant != null &&
								acceleratedInstant.isAfter(currentInstant()) :
				new PreconditionException(
							"acceleratedInstant != null && "
							+ "acceleratedInstant.isAfter(currentInstant())");

		long baseInNanos =
				TimeUnit.MILLISECONDS.toNanos(this.startInstant.toEpochMilli());
		long epochOfInstantInNanos =
				TimeUnit.MILLISECONDS.toNanos(acceleratedInstant.toEpochMilli());
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
			// sets the start time now
			// captures the current Instant, equivalent to the start time
			Instant s1 = Instant.now();
			Instant s2 = Instant.parse("2022-11-07T06:00:00.00Z");

			AcceleratedClock clock1 = new AcceleratedClock(10.0);
			AcceleratedClock clock2 = new AcceleratedClock(s2, 10.0);

			if (VERBOSE) {
				// should print the same value, but in different representations
				System.out.println(clock1.getStartInstant() + " -- " + s1);
			}

			Thread.sleep(6000L);
			// 6 real seconds elapsed gives 60 seconds in accelerated time,
			// hence is i + 50 seconds
			Instant i1 = clock1.currentInstant();
			Instant i2 = clock2.currentInstant();
			// i now represents the start time + 600 seconds in accelerated
			// time
			Instant i11 = s1.plusSeconds(600);
			Instant i21 = s2.plusSeconds(600);
			// as the current real time is start time + 6 seconds, hence 60
			// seconds in accelerated time, the delay in accelerated time is
			// 540 seconds hence 54 seconds in real time
			long d1 = clock1.nanoDelayUntilAcceleratedInstant(i11);
			long d2 = clock2.nanoDelayUntilAcceleratedInstant(i21);
			// take a real time as current i.e., start time + 6 seconds, and
			// add 45 real seconds, so c is start time + 51 seconds in real time
			// but start time + 510 seconds in accelerated time
			long c = System.currentTimeMillis() + 45000L;
			// i is start time + 600 seconds in accelerated
			// time) is therefore 90 seconds in accelerated time but 9 seconds
			// in real time
			long d11 = clock1.nanoDelayToAcceleratedInstantFromEpochTime(c, i11);
			long d21 = clock2.nanoDelayToAcceleratedInstantFromEpochTime(c, i21);

			// printing is done at the end to avoid perturbing the timing too
			// much, yet small discrepancies are expected
			System.out.println("starting at " + clock1.getStartInstant());
			System.out.println("after 6 real seconds (60 accelerated): " + i1);
			System.out.println("delay to execute at " + i11 + " is " + d1
								+ " (nanos) -- " + d1/1000000000.0 + " (sec) "
								+ "[expected = 54 seconds]");
			System.out.println("delay to execute at " + i11 + " from " + c
								+ " (millis) is " + d11 + " (nanos) -- "
								+ d11/1000000000.0 + " (sec) "
								+ "[expected = 9 seconds]");

			System.out.println("starting at " + clock2.getStartInstant());
			System.out.println("after 6 real seconds (60 accelerated): " + i2);
			System.out.println("delay to execute at " + i21 + " is " + d2
								+ " (nanos) -- " + d2/1000000000.0 + " (sec) "
								+ "[expected = 54 seconds]");
			System.out.println("delay to execute at " + i21 + " from " + c
								+ " (millis) is " + d21 + " (nanos) -- "
								+ d21/1000000000.0 + " (sec) "
								+ "[expected = 9 seconds]");
		} catch (InterruptedException e) {
			throw new RuntimeException(e) ;
		}
	}
}
// -----------------------------------------------------------------------------
