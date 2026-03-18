package fr.sorbonne_u.components.tasks_management;

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

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.tasks_management.connection.AbnormalTerminationNotificationOutboundPort;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.URIGenerator;
import java.io.Serializable;
import fr.sorbonne_u.components.exceptions.BCMRuntimeException;

// -----------------------------------------------------------------------------
/**
 * The class <code>BCM4JavaComponentThread</code> defines a specific type of
 * threads for rBCM4Java which names derives from the URI of their owner
 * component and which possess an {@code Thread.UncaughtExceptionHandler}
 * printing a message on {@code System.err}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code DEFAULT_NAME_PREFIX != null}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2026-03-12</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			BCM4JavaComponentThread
extends		Thread
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** default name prefix for threads when the URI of the owner
	 *  component is not supplied.											*/
	public static final String DEFAULT_NAME_PREFIX = "BCM4JavaComponentThread";

	/** outbound port through which the notification has to be performed.	*/
	protected AbnormalTerminationNotificationOutboundPort
												notificationDestination;
	/** URI of the inbound port that must receive the notification.			*/
	protected String							notificationInboundPortURI;
	/** interface offered by the notifier through which the faulty call
	 *  was made.															*/
	protected Class<? extends OfferedCI>		offeredInterface;
	/** URI of the inbound port offering {@code offeredInterface} through
	 *  which the faulty call was made.										*/
	protected String							serviceInboundPortURI;
	/** name of the faultly called service in {@code offeredInterface}.	 	*/
	protected String							serviceName;
	/** parameters passed to the faultly called service.					*/
	protected Serializable[]					actualParameters;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a new thread with the default name prefix.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code target != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param target	runnable to be executed by the new thread.
	 */
	public				BCM4JavaComponentThread(Runnable target)
	{
		this(target,
			 AssertionChecking.assertNonNullOrThrow(
				DEFAULT_NAME_PREFIX,
				() -> new ImplementationInvariantException(
						"DEFAULT_NAME_PREFIX != null")));
	}

	/**
	 * create a new thread with the given name prefix.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code target != null}
	 * pre	{@code namePrefix != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param target		runnable to be executed by the new thread.
	 * @param namePrefix	prefix for the name of the new thread.
	 */
	public				BCM4JavaComponentThread(
		Runnable target,
		String namePrefix
		)
	{
		super(AssertionChecking.assertNonNullOrThrow(
					target,
					() -> new PreconditionException("target != null")),
			  AssertionChecking.assertTrueAndReturnOrThrow(
					namePrefix != null,
					namePrefix,
					() -> new PreconditionException("target != null"))
				+ "-" + URIGenerator.generateURI());

		setUncaughtExceptionHandler(
			new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e)
				{
					if (notificationDestination != null) {
						try {
							notificationDestination.
								doNotifyAbnormalTermination(
									notificationInboundPortURI,
									offeredInterface,
									serviceInboundPortURI,
									serviceName,
									actualParameters,
									// checked exceptions are passed as the
									// cause of a BCMRuntimeException
									e instanceof BCMRuntimeException ?
										e.getCause()
									:	e);
						} catch(Exception e1) {
							StringBuffer sb = new StringBuffer("Thread ");
							sb.append(t.getName());
							sb.append(" exits on uncaught exception ");
							sb.append(e.toString());
							sb.append(" for service ");
							sb.append(serviceName);
							sb.append(" called with actual parameters [");
							for (int i = 0 ; i < actualParameters.length ; i++) {
								sb.append(actualParameters[i]);
								if (i < actualParameters.length - 1) {
									sb.append(", ");
								}
							}
							sb.append("] on the port with URI ");
							sb.append(serviceInboundPortURI);
							sb.append(" offering the interface ");
							sb.append(offeredInterface.toString());
							System.err.println(sb.toString());
						} finally {
							cleanUp();
						}
					} else {
						System.err.println(
								"Thread " + t.getName() +
								" exits on uncaught exception " + e);
					}
				}
			});
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * set the reference through which the notification must be made and the
	 * information about the faulty call.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notificationDestination != null}
	 * pre	{@code offeredInterface != null}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code serviceName != null && !serviceName.isEmpty()}
	 * pre	{@code actualParameters != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param notificationDestination		outbound port through which the notification has to be performed, by default it should be the reference to an outbound port requiring {@code AbnormalTerminationNotificationCI}.
	 * @param notificationInboundPortURI	URI of the inbound port that must receive the notification.
	 * @param offeredInterface				interface offered by the notifier through which the faulty call was made.
	 * @param serviceInboundPortURI			URI of the inbound port offering {@code offeredInterface} through which the faulty call was made.
	 * @param serviceName					name of the faultly called service in {@code offeredInterface}.
	 * @param actualParameters				parameters passed to the faultly called service.
	 */
	public void			setServiceInvocationInfo(
		AbnormalTerminationNotificationOutboundPort notificationDestination,
		String notificationInboundPortURI,
		Class<? extends OfferedCI> offeredInterface,
		String serviceInboundPortURI,
		String serviceName,
		Serializable[] actualParameters
		)
	{
		assert	notificationDestination != null :
				new PreconditionException("notificationDestination != null");
		assert	offeredInterface != null :
				new PreconditionException("offeredInterface != null");
		assert	serviceInboundPortURI != null && !serviceInboundPortURI.isEmpty():
				new PreconditionException("inboundPortURI != null && !inboundPortURI.isEmpty()");
		assert	serviceName != null && !serviceName.isEmpty() :
				new PreconditionException("serviceName != null && !serviceName.isEmpty()");
		assert	actualParameters != null :
				new PreconditionException("actualParameters != null");

		this.notificationDestination = notificationDestination;
		this.notificationInboundPortURI = notificationInboundPortURI;
		this.offeredInterface = offeredInterface;
		this.serviceInboundPortURI = serviceInboundPortURI;
		this.serviceName = serviceName;
		this.actualParameters = actualParameters;
	}

	/**
	 * clean up the call information; to be performed upon finishing the task.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void			cleanUp()
	{
		this.notificationDestination = null;
		this.notificationInboundPortURI = null;
		this.offeredInterface = null;
		this.serviceInboundPortURI = null;
		this.serviceName = null;
		this.actualParameters = null;
	}
}
// -----------------------------------------------------------------------------
