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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ComponentI.ComponentTask;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.components.tasks_management.interfaces.AbnormalTerminationNotificationCI;
import fr.sorbonne_u.components.tasks_management.interfaces.AbnormalTerminationNotificationImplI;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.io.Serializable;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbnormalTerminationNotificationInboundPort</code> implements
 * an inbound port offering the component interface
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
public class			AbnormalTerminationNotificationInboundPort
extends		AbstractInboundPort
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
	 * create and initialise an inbound port with a given URI and given plug-in
	 * and executor service URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof AbnormalTerminationNotificationImplI}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || !callerRuns}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(AbnormalTerminationNotificationCI.class)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param callerRuns			if true, the call to the owner component must be executed by the caller component thread.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationInboundPort(
		String uri,
		ComponentI owner,
		String pluginURI,
		boolean callerRuns,
		String executorServiceURI
		) throws Exception
	{
		this(uri, AbnormalTerminationNotificationCI.class, owner, pluginURI,
			 callerRuns, executorServiceURI);
	}

	/**
	 * create and initialise an inbound port with a generated URI and given
	 * plug-in and executor service URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof AbnormalTerminationNotificationImplI}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || !callerRuns}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(AbnormalTerminationNotificationCI.class)}
	 * </pre>
	 *
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param callerRuns			if true, the call to the owner component must be executed by the caller component thread.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationInboundPort(
		ComponentI owner,
		String pluginURI,
		boolean callerRuns,
		String executorServiceURI
		) throws Exception
	{
		this(AbnormalTerminationNotificationCI.class, owner, pluginURI,
			 callerRuns, executorServiceURI);
	}

	/**
	 * create and initialise an inbound port with a given URI and given plug-in
	 * and executor service URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof AbnormalTerminationNotificationImplI}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(AbnormalTerminationNotificationCI.class)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationInboundPort(
		String uri,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		this(uri, AbnormalTerminationNotificationCI.class, owner, pluginURI,
			 executorServiceURI);
	}

	/**
	 * create and initialise an inbound port with a given plug-in and executor
	 * service URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof AbnormalTerminationNotificationImplI}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(AbnormalTerminationNotificationCI.class)}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationInboundPort(
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		this(AbnormalTerminationNotificationCI.class, owner, pluginURI,
			 executorServiceURI);
	}

	/**
	 * create and initialise an inbound port with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof AbnormalTerminationNotificationImplI}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(AbnormalTerminationNotificationCI.class)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, AbnormalTerminationNotificationCI.class, owner);
	}

	/**
	 * create and initialise an inbound port with an automatically generated URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof AbnormalTerminationNotificationImplI}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(AbnormalTerminationNotificationCI.class)}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(AbnormalTerminationNotificationCI.class, owner);
	}

	// Rxtendable implemented interface versions
	
	/**
	 * create and initialise an inbound port with a given URI and given plug-in
	 * and executor service URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof AbnormalTerminationNotificationImplI}
	 * pre	{@code implementedInterface != null}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || !callerRuns}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param callerRuns			if true, the call to the owner component must be executed by the caller component thread.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationInboundPort(
		String uri,
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner,
		String pluginURI,
		boolean callerRuns,
		String executorServiceURI
		) throws Exception
	{
		super(uri, implementedInterface, owner, pluginURI, callerRuns,
			  executorServiceURI);

		assert	owner instanceof AbnormalTerminationNotificationImplI :
				new PreconditionException(
						"owner instanceof AbnormalTerminationNotificationImplI");
	}

	/**
	 * create and initialise an inbound port with a generated URI and given
	 * plug-in and executor service URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof AbnormalTerminationNotificationImplI}
	 * pre	{@code implementedInterface != null}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || !callerRuns}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param callerRuns			if true, the call to the owner component must be executed by the caller component thread.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationInboundPort(
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner,
		String pluginURI,
		boolean callerRuns,
		String executorServiceURI
		) throws Exception
	{
		super(implementedInterface, owner, pluginURI, callerRuns,
			  executorServiceURI);


		assert	owner instanceof AbnormalTerminationNotificationImplI :
				new PreconditionException(
						"owner instanceof AbnormalTerminationNotificationImplI");
	}

	/**
	 * create and initialise an inbound port with a given URI and given plug-in
	 * and executor service URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof AbnormalTerminationNotificationImplI}
	 * pre	{@code implementedInterface != null}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationInboundPort(
		String uri,
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		super(uri, implementedInterface, owner, pluginURI, executorServiceURI);


		assert	owner instanceof AbnormalTerminationNotificationImplI :
				new PreconditionException(
						"owner instanceof AbnormalTerminationNotificationImplI");
	}

	/**
	 * create and initialise an inbound port with a given plug-in and executor
	 * service URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof AbnormalTerminationNotificationImplI}
	 * pre	{@code implementedInterface != null}
	 * pre	{@code implementedInterface.isAssignableFrom(this.getClass())}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationInboundPort(
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		super(implementedInterface, owner, pluginURI, executorServiceURI);
	}

	/**
	 * create and initialise an inbound port with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof AbnormalTerminationNotificationImplI}
	 * pre	{@code implementedInterface != null}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationInboundPort(
		String uri,
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super(uri, implementedInterface, owner);

		assert	owner instanceof AbnormalTerminationNotificationImplI :
				new PreconditionException(
						"owner instanceof AbnormalTerminationNotificationImplI");
	}

	/**
	 * create and initialise an inbound port with an automatically generated URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof AbnormalTerminationNotificationImplI}
	 * pre	{@code implementedInterface != null}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(implementedInterface)}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AbnormalTerminationNotificationInboundPort(
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super(implementedInterface, owner);

		assert	owner instanceof AbnormalTerminationNotificationImplI :
				new PreconditionException(
						"owner instanceof AbnormalTerminationNotificationImplI");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.tasks_management.interfaces.AbnormalTerminationNotificationCI#notifyAbnormalTermination(java.lang.Class, java.lang.String, java.lang.String, java.io.Serializable[], java.lang.Throwable)
	 */
	@Override
	public void			notifyAbnormalTermination(
		Class<? extends OfferedCI> offeredInterface,
		String inboundPortURI,
		String serviceName,
		Serializable[] actualParameters,
		Throwable t
		) throws Exception
	{
		ComponentTask task;
		if (this.hasPlugin()) {
			task = new AbstractComponent.AbstractTask(this.getPluginURI()) {
					@Override
					public void run() {
						try {
							((AbnormalTerminationNotificationImplI)
								this.getTaskProviderReference()).
									notifyAbnormalTermination(
											offeredInterface, inboundPortURI,
											serviceName, actualParameters, t);
						} catch (Throwable t) {
							// to avoid notifying an error in a notification, hence
							// potentially entering an infinite loop of notifications
							// when the caller also notifies its own callers
							System.err.println("notification failed!");
						}
					}
				};
		} else {
			task = new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							((AbnormalTerminationNotificationImplI)
								this.getTaskOwner()).notifyAbnormalTermination(
									offeredInterface, inboundPortURI,
									serviceName, actualParameters, t);
						} catch (Throwable t) {
							// to avoid notifying an error in a notification, hence
							// potentially entering an infinite loop of notifications
							// when the caller also notifies its own callers
							System.err.println("notification failed!");
						}
					}
				};
		}
		if (this.hasExecutorService()) {
			this.getOwner().runTask(this.getExecutorServiceIndex(), task);
		} else {
			this.getOwner().runTask(task);
		}
	}
}
// -----------------------------------------------------------------------------
