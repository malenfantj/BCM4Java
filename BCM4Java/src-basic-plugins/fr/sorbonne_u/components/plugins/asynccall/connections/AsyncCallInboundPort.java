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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.BCMRuntimeException;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.plugins.asynccall.AbstractAsyncCall;
import fr.sorbonne_u.components.plugins.asynccall.AsyncCallCI;
import fr.sorbonne_u.components.plugins.asynccall.AsyncCallServerSidePlugin;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>AsyncCallInboundPort</code> implements an inbound port for
 * the <code>AsyncCallCI</code> component interface.
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
 * <p>Created on : 2021-04-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			AsyncCallInboundPort
extends		AbstractInboundPort
implements	AsyncCallCI
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
	public				AsyncCallInboundPort(
		String uri,
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		super(uri, implementedInterface, owner, pluginURI,
			  executorServiceURI == null ? true : false,
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
	public				AsyncCallInboundPort(
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
	 * pre	{@code AsyncCallCI.class.isAssignableFrom(getClass())}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(AsyncCallCI.class)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AsyncCallInboundPort(
		String uri,
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		this(uri, AsyncCallCI.class, owner, pluginURI, executorServiceURI);
	}

	/**
	 * create and initialise an inbound port with a given plug-in and executor
	 * service URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code AsyncCallCI.class.isAssignableFrom(this.getClass())}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * pre	{@code executorServiceURI == null || owner.validExecutorServiceURI(executorServiceURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(AsyncCallCI.class)}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AsyncCallInboundPort(
		ComponentI owner,
		String pluginURI,
		String executorServiceURI
		) throws Exception
	{
		this(AbstractPort.generatePortURI(AsyncCallCI.class),
			 AsyncCallCI.class, owner, pluginURI, executorServiceURI);
	}

	/**
	 * create and initialise an inbound port with a given URI and given plug-in
	 * URI but no executor service URI, interpreted as the caller thread will
	 * run the called method.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code AsyncCallCI.class.isAssignableFrom(getClass())}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(AsyncCallCI.class)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AsyncCallInboundPort(
		String uri,
		ComponentI owner,
		String pluginURI
		) throws Exception
	{
		this(uri, AsyncCallCI.class, owner, pluginURI, null);
	}

	/**
	 * create and initialise an inbound port with a given plug-in URI but no
	 * executor service URI, interpreted as the caller thread will
	 * run the called method.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code AsyncCallCI.class.isAssignableFrom(this.getClass())}
	 * pre	{@code pluginURI == null || owner.isInstalled(pluginURI)}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(AsyncCallCI.class)}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @param owner					component that owns this port.
	 * @param pluginURI				URI of the plug-in to be called in the owner or null if none.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				AsyncCallInboundPort(
		ComponentI owner,
		String pluginURI
		) throws Exception
	{
		this(AbstractPort.generatePortURI(AsyncCallCI.class),
			 AsyncCallCI.class, owner, pluginURI, null);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.plugins.asynccall.AsyncCallCI#disconnectClient(java.lang.String)
	 */
	@Override
	public void			disconnectClient(String receptionPortURI)
	throws Exception
	{
		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			System.out.println(
					"AsyncCallInboundPort::disconnectClient "
					+ receptionPortURI);
		}

		assert	this.hasPlugin() : new PreconditionException("hasPlugin()");

		if (this.callerRuns) {
			((AsyncCallServerSidePlugin)
					this.getInstalledPlugin(this.getPluginURI())).
									disconnectReceptionPort(receptionPortURI);
		} else if (this.hasExecutorService()) {
			this.getOwner().runTask(
				this.getExecutorServiceIndex(),
				new AbstractComponent.AbstractTask(this.getPluginURI()) {
					@Override
					public void run() {
						try {
							((AsyncCallServerSidePlugin)
								this.getTaskProviderReference()).
									disconnectReceptionPort(receptionPortURI);
						} catch (Exception e) {
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
							((AsyncCallServerSidePlugin)
								this.getTaskProviderReference()).
									disconnectReceptionPort(receptionPortURI);
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}
				});
		}
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.asynccall.AsyncCallCI#asyncCall(fr.sorbonne_u.components.plugins.asynccall.AbstractAsyncCall)
	 */
	@Override
	public void			asyncCall(AbstractAsyncCall c) throws Exception
	{
		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			System.out.println(
					"AsyncCallInboundPort::asyncCall "
					+ c.getClass().getSimpleName());
		}

		assert	this.hasPlugin() : new PreconditionException("hasPlugin()");

		if (this.callerRuns) {
			((AsyncCallServerSidePlugin)
					this.getInstalledPlugin(this.getPluginURI())).asyncCall(c);
		} else if (this.hasExecutorService()) {
			this.getOwner().runTask(
				this.getExecutorServiceIndex(),
				new AbstractComponent.AbstractTask(this.getPluginURI()) {
					@Override
					public void run() {
						try {
							((AsyncCallServerSidePlugin)
								this.getTaskProviderReference()).asyncCall(c);
						} catch (Exception e) {
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
							((AsyncCallServerSidePlugin)
								this.getTaskProviderReference()).asyncCall(c);
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}
				});
		}
	}
}
// -----------------------------------------------------------------------------
