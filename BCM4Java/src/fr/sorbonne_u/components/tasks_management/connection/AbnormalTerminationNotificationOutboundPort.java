package fr.sorbonne_u.components.tasks_management.connection;

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

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.exceptions.BCMRuntimeException;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.components.tasks_management.interfaces.AbnormalTerminationNotificationCI;
import java.io.Serializable;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbnormalTerminationNotificationOutboundPort</code> implements
 * an outbound port requiring the component interface
 * {@code AbnormalTerminationNotificationCI}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2026-03-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			AbnormalTerminationNotificationOutboundPort
extends		AbstractOutboundPort
implements	AbnormalTerminationNotificationCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Fixed implemented interface versions

	/**
	 * create and initialise outbound ports, with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null && uri != null}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getImplementedInterface().equals(AbnormalTerminationNotificationCI.class)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, AbnormalTerminationNotificationCI.class, owner);
	}

	/**
	 * create and initialize outbound ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * post	{@code !isDestroyed()}
	 * post	{@code getImplementedInterface().equals(AbnormalTerminationNotificationCI.class)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @param owner					component that owns this port.
	 * @throws Exception			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(AbnormalTerminationNotificationCI.class, owner);
	}

	// Extendable implemented interface versions

	/**
	 * create and initialise outbound ports, with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null && uri != null && implementedInterface != null}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationOutboundPort(
		String uri,
		Class<? extends RequiredCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super(uri, implementedInterface, owner);
	}

	/**
	 * create and initialize outbound ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null && implementedInterface != null}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationOutboundPort(
		Class<? extends RequiredCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super(implementedInterface, owner);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * perform the notification, first connecting the port, next notify and
	 * finally disconnect the port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param notificationInboundPortURI	URI of the inbound port that must receive the notification.
	 * @param offeredInterface				interface offered by the notifier through which the faulty call was made.
	 * @param serviceInboundPortURI			URI of the inbound port offering {@code offeredInterface} through which the faulty call was made.
	 * @param serviceName					name of the faultly called service in {@code offeredInterface}.
	 * @param actualParameters				parameters passed to the faultly called service.
	 * @param t								the thrown throwable.
	 */
	public synchronized void	doNotifyAbnormalTermination(
		String notificationInboundPortURI,
		Class<? extends OfferedCI> offeredInterface,
		String serviceInboundPortURI,
		String serviceName,
		Serializable[] actualParameters,
		Throwable t
		)
	{
		try {
			this.getOwner().doPortConnection(
					this.getPortURI(),
					notificationInboundPortURI,
					AbnormalTerminationNotificationConnector.class.
														getCanonicalName());
			this.notifyAbnormalTermination(
					offeredInterface,
					serviceInboundPortURI,
					serviceName,
					actualParameters,
					t);
			this.getOwner().doPortDisconnection(this.getPortURI());
		} catch (Exception e) {
			throw new BCMRuntimeException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.tasks_management.interfaces.AbnormalTerminationNotificationCI#notifyAbnormalTermination(java.lang.Class, java.lang.String, java.lang.String, java.io.Serializable[], java.lang.Throwable)
	 */
	@Override
	public synchronized void	notifyAbnormalTermination(
		Class<? extends OfferedCI> offeredInterface,
		String inboundPortURI,
		String serviceName,
		Serializable[] actualParameters,
		Throwable t
		) throws Exception
	{
		((AbnormalTerminationNotificationCI)this.getConnector()).
				notifyAbnormalTermination(offeredInterface, inboundPortURI,
										  serviceName, actualParameters, t);
	}
}
// -----------------------------------------------------------------------------
