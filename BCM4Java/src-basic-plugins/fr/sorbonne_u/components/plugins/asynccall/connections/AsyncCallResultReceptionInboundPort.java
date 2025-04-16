package fr.sorbonne_u.components.plugins.asynccall.connections;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide an
// implementation of the BCM component model.
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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.BCMRuntimeException;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.plugins.asynccall.AsyncCallClientPlugin;
import fr.sorbonne_u.components.plugins.asynccall.AsyncCallResultReceptionCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>AsyncCallResultReceptionInboundPort</code> implements
 * an inbound port for the <code>AsyncCallResultReceptionCI</code>
 * component interface.
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
 * <p>Created on : 2021-04-12</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			AsyncCallResultReceptionInboundPort
extends		AbstractInboundPort
implements	AsyncCallResultReceptionCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create and initialise an inbound port with a given URI and given plug-in
	 * and executor service URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * pre	{@code implementedInterface != null}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code implementedInterface.isAssignableFrom(getClass())}
	 * pre	{@code pluginURI != null}
	 * pre	{@code owner.isInstalled(pluginURI)}
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
	public				AsyncCallResultReceptionInboundPort(
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

		assert	pluginURI != null :
				new PreconditionException("pluginURI != null");
		assert	owner.isInstalled(pluginURI) :
				new PreconditionException("owner.isInstalled(pluginURI)");
	}

	/**
	 * create and initialise an inbound port with a generated URI and given
	 * plug-in and executor service URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
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
	public				AsyncCallResultReceptionInboundPort(
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner,
		boolean callerRuns,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		this(AbstractPort.generatePortURI(implementedInterface),
			 implementedInterface, owner, pluginURI, callerRuns,
			 executorServiceURI);
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
	public				AsyncCallResultReceptionInboundPort(
		String uri,
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		this(uri, implementedInterface, owner, pluginURI, false,
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
	public				AsyncCallResultReceptionInboundPort(
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		this(AbstractPort.generatePortURI(implementedInterface),
			 implementedInterface, owner, pluginURI, executorServiceURI);
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
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code AsyncCallResultReceptionCI.class.isAssignableFrom(getClass())}
	 * pre	{@code pluginURI != null}
	 * pre	{@code owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || !callerRuns}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(AsyncCallResultReceptionCI.class)}
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
	public				AsyncCallResultReceptionInboundPort(
		String uri,
		ComponentI owner,
		String pluginURI,
		boolean callerRuns,
		String executorServiceURI
		) throws Exception
	{
		this(uri, AsyncCallResultReceptionCI.class, owner, pluginURI, callerRuns,
			 executorServiceURI);
	}

	/**
	 * create and initialise an inbound port with a generated URI and given
	 * plug-in and executor service URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code AsyncCallResultReceptionCI.class.isAssignableFrom(getClass())}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || !callerRuns}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(AsyncCallResultReceptionCI.class)}
	 * </pre>
	 *
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param callerRuns			if true, the call to the owner component must be executed by the caller component thread.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AsyncCallResultReceptionInboundPort(
		ComponentI owner,
		boolean callerRuns,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		this(AbstractPort.generatePortURI(AsyncCallResultReceptionCI.class),
			 AsyncCallResultReceptionCI.class, owner, pluginURI, callerRuns,
			 executorServiceURI);
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
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code AsyncCallResultReceptionCI.class.isAssignableFrom(getClass())}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(AsyncCallResultReceptionCI.class)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AsyncCallResultReceptionInboundPort(
		String uri,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		this(uri, AsyncCallResultReceptionCI.class, owner, pluginURI,
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
	 * pre	{@code AsyncCallResultReceptionCI.class.isAssignableFrom(this.getClass())}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(AsyncCallResultReceptionCI.class)}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AsyncCallResultReceptionInboundPort(
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		this(AbstractPort.generatePortURI(AsyncCallResultReceptionCI.class),
			 AsyncCallResultReceptionCI.class, owner, pluginURI,
			 executorServiceURI);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * call the owner to make it receive an awaited result.
	 * 
	 * <pre>
	 * pre	{@code hasPlugin()}
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.plugins.asynccall.AsyncCallResultReceptionCI#acceptResult(java.lang.String, java.io.Serializable)
	 */
	@Override
	public void			acceptResult(String callURI, Serializable result)
	throws Exception
	{
		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			System.out.println(
					"AsyncCallResultReceptionInboundPort::acceptResult("
					+ callURI + ", " + result);
		}

		assert	this.hasPlugin() : new PreconditionException("hasPlugin()");

		if (this.isCallerRuns()) {
			((AsyncCallClientPlugin)this.getOwnerPlugin(this.getPluginURI())).
													receive(callURI, result);
		} else if (this.hasExecutorService()) {
			this.getOwner().runTask(
					this.getExecutorServiceIndex(),
					new AbstractComponent.AbstractTask(this.getPluginURI()) {
						@Override
						public void run() {
							try {
								((AsyncCallClientPlugin)
									this.getTaskProviderReference()).
													receive(callURI, result);
							} catch (BCMException e) {
								throw new BCMRuntimeException(e) ;
							}
						}
					});
		} else {
			this.getOwner().runTask(
					new AbstractComponent.AbstractTask(this.getPluginURI()) {
						@Override
						public void run() {
							try {
								((AsyncCallClientPlugin)
									this.getTaskProviderReference()).
													receive(callURI, result);
							} catch (BCMException e) {
								throw new BCMRuntimeException(e) ;
							}
						}
					});
		}
	}
}
// -----------------------------------------------------------------------------
