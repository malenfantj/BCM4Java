package fr.sorbonne_u.components;

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

import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.PluginException;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.components.reflection.connectors.ReflectionConnector;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;
import fr.sorbonne_u.components.reflection.ports.ReflectionOutboundPort;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import fr.sorbonne_u.components.AbstractComponent.ExecutorServiceFactory;
import fr.sorbonne_u.components.ComponentI.ComponentService;
import fr.sorbonne_u.components.ComponentI.ComponentTask;

// -----------------------------------------------------------------------------
/**
 * The abstract class <code>AbstractPlugin</code> defines the most generic
 * methods and data for component plug-ins.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * Plug-ins are objects designed to extend the functionalities of component
 * and enable a form of reuse among components. The idea if to implement
 * component services as methods in plug-ins and call these plug-in methods
 * directly from the component service implementation methods or from
 * inbound ports (inbound ports for plug-ins provided in BCM ease this
 * process). A plug-in has an URI and a component can only have one plug-in
 * object of a given URI (though more than one plug-in object of the same
 * plug-in class may exist in a component albeit with different URIs).
 * <code>AbstractComponent</code> implements the plug-in management, among
 * which it provides a way to retrieve the plug-in object reference from
 * its URI.
 * </p>
 * <p>
 * A plug-in has its own life-cycle, including initialisation which can be
 * used to add interfaces and ports to their hosting component. Hence, the
 * typical usage of plug-ins is to implement some services which are exposed
 * as offered interfaces and inbound ports or to require some services which
 * are exposed as required interfaces and outbound ports. Hence a complete
 * client/server relationship between two components can be implemented
 * through a client plug-in installed on the client component and a server
 * plug-in installed in the server component, with the two complementary
 * plug-ins completely hiding from the component programmer the issues
 * revolving around required/offered interfaces and outbound/inbound ports
 * to be used.
 * </p>
 * <p>
 * Plug-in objects are created from their class and installed on a component
 * using components plug-in management services implemented by all components.
 * Every component offers the interface <code>ComponentPluginI</code> and has
 * a <code>ComponentPluginInboundPort</code> automatically added at creation
 * time to offer these services.
 * </p>
 * <p>
 * <code>AbstractPlugin</code> is placed in the same package as
 * <code>AbstractComponent</code> to provide it with an access to a package
 * visibility method <code>doAddPort</code> allowing to add a port to the
 * plug-in owner component without resorting to a public method to do so.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code getPreferredExecutionServiceURI() != null && return >= 0 || getPreferredExecutionServiceURI() == null && return < 0}
 * invariant	true		// TODO: complete
 * </pre>
 * 
 * <p>Created on : 2016-02-03</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractPlugin
implements	PluginI
{
	private static final long	serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Inner classes
	// -------------------------------------------------------------------------

	/**
	 * The static class <code>FakeComponent</code> implements a fake component
	 * used to call the reflection services of the component on which the
	 * plug-in is to be installed, finalised or uninstalled.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	true
	 * </pre>
	 * 
	 * <p>Created on : 2017-01-10</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	@RequiredInterfaces(required = {ReflectionCI.class})
	protected static class	FakeComponent
	extends		AbstractComponent
	{
		/** the outbound port used to call plug-in management services of the
		 * other component.													*/
		protected final ReflectionOutboundPort	cpObp;

		/**
		 * create a proxy component with a component plug-in outbound port
		 * to be connected to the plug-in component owner.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true	// no precondition.
		 * post	true	// no postcondition.
		 * </pre>
		 *
		 * @throws Exception	<i>to do</i>.
		 */
		public			FakeComponent() throws Exception
		{
			super(0, 0);

			this.cpObp = new ReflectionOutboundPort(this);
			this.cpObp.publishPort();
		}

		/**
		 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
		 */
		@Override
		public void		finalise() throws Exception
		{
			assert	!this.cpObp.connected() :
						new PluginException(
								"reflection outbound port still connected!");

			this.cpObp.unpublishPort();
			this.removeRequiredInterface(ReflectionCI.class);
			super.finalise();
		}

		/**
		 * install a plug-in on the component designated by the URI of its
		 * plug-in inbound port URI.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code plugin != null && pluginInboundPortURI != null}
		 * post	true	// no postcondition.
		 * </pre>
		 *
		 * @param plugin				plug-in to be installed.
		 * @param pluginInboundPortURI	URI of the reflection inbound port of the component holding the plug-in to be finalised.
		 * @throws Exception			<i>to do</i>.
		 */
		public void		doInstallPluginOn(
			PluginI plugin,
			String pluginInboundPortURI
			) throws Exception
		{
			assert	plugin != null && pluginInboundPortURI != null :
						new PreconditionException(
								"plugin != null && "
										+ "pluginInboundPortURI != null");

			this.doPortConnection(
						this.cpObp.getPortURI(),
						pluginInboundPortURI,
						ReflectionConnector.class.getCanonicalName());
			this.cpObp.installPlugin(plugin);
			this.doPortDisconnection(this.cpObp.getPortURI());

			assert	!this.cpObp.connected();
		}

		/**
		 * finalise a plug-in installed on another component.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code pluginInboundPortURI != null && pluginURI != null}
		 * post	true	// no postcondition.
		 * </pre>
		 *
		 * @param pluginInboundPortURI	URI of the reflection inbound port of the component holding the plug-in to be finalised.
		 * @param pluginURI				URI of the plug-in to be finalised.
		 * @throws Exception			<i>to do</i>.
		 */
		public void		doFinalisePlugin(
			String pluginInboundPortURI,
			String pluginURI
			) throws Exception
		{
			assert	pluginInboundPortURI != null && pluginURI != null :
						new PreconditionException(
								"pluginInboundPortURI != null && "
										+ "pluginURI != null");

			this.doPortConnection(
					this.cpObp.getPortURI(),
					pluginInboundPortURI,
					ReflectionConnector.class.getCanonicalName());
			this.cpObp.finalisePlugin(pluginURI);
			this.doPortDisconnection(this.cpObp.getPortURI());

			assert	!this.cpObp.connected();
		}

		/**
		 * uninstall a plug-in on the owner component designated by the URI of
		 * its plug-in inbound port URI.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code pluginInboundPortURI != null && pluginURI != null}
		 * post	true	// no postcondition.
		 * </pre>
		 *
		 * @param pluginInboundPortURI	URI of the reflection inbound port of the component holding the plug-in to be finalised.
		 * @param pluginURI				URI of the plug-in to be uninstalled.
		 * @throws Exception			<i>to do</i>.
		 */
		public void		doUnistallPluginFrom(
			String pluginInboundPortURI,
			String pluginURI
			) throws Exception
		{
			assert	pluginInboundPortURI != null && pluginURI != null :
						new PreconditionException(
								"pluginInboundPortURI != null && "
										+ "pluginURI != null");

			this.doPortConnection(
					this.cpObp.getPortURI(),
					pluginInboundPortURI,
					ReflectionConnector.class.getCanonicalName());
			this.cpObp.uninstallPlugin(pluginURI);
			this.doPortDisconnection(this.cpObp.getPortURI());

			assert	!this.cpObp.connected();
		}
	}

	// -------------------------------------------------------------------------
	// Plug-in static services
	// -------------------------------------------------------------------------

	/**
	 * install a plug-in on a component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code pluginInboundPortURI != null && pluginToInstall != null}
	 * pre	{@code pluginToInstall.getPluginURI() != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param pluginInboundPortURI	URI of the plug-in management inbound port of the component.
	 * @param pluginToInstall		plug-in to be installed.
	 * @throws Exception			<i>to do</i>.
	 */
	public static void	installPluginOn(
		final String pluginInboundPortURI,
		final PluginI pluginToInstall
		) throws Exception
	{
		assert	pluginInboundPortURI != null && pluginToInstall != null :
					new PreconditionException(
							"pluginInboundPortURI != null && "
									+ "pluginToInstall != null");
		assert	pluginToInstall.getPluginURI() != null :
					new PreconditionException(
							"pluginToInstall.getPluginURI() != null");

		FakeComponent fake = new FakeComponent() {};
		fake.runTask(
			new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						((FakeComponent)this.getTaskOwner()).doInstallPluginOn(
									pluginToInstall, pluginInboundPortURI);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});		
	}

	/**
	 * finalise a plug-in on a component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code pluginInboundPortURI != null && pluginURI != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pluginInboundPortURI	URI of the plug-in management inbound port of the component.
	 * @param pluginURI				URI of the plug-in.
	 * @throws Exception		<i>todo.</i>
	 */
	public static void		finalisePluginOn(
		final String pluginInboundPortURI,
		final String pluginURI
		) throws Exception
	{
		assert	pluginInboundPortURI != null && pluginURI != null :
					new PreconditionException(
							"pluginInboundPortURI != null && "
									+ "pluginURI != null");

		FakeComponent fake = new FakeComponent() {};
		fake.runTask(
			new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						((FakeComponent) this.getTaskOwner()).doFinalisePlugin(
								pluginInboundPortURI, pluginURI);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

	}

	/**
	 * uninstall a plug-in from a component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code pluginInboundPortURI != null && pluginURI != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pluginInboundPortURI	URI of the plug-in management inbound port of the component.
	 * @param pluginURI				URI of the plug-in.
	 * @throws Exception			<i>to do</i>.
	 */
	public static void	uninstallPluginFrom(
		final String pluginInboundPortURI,
		final String pluginURI
		) throws Exception
	{
		assert	pluginInboundPortURI != null && pluginURI != null :
					new PreconditionException(
							"pluginInboundPortURI != null && "
									+ "pluginURI != null");

		FakeComponent fake = new FakeComponent() {};
		fake.runTask(
			new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						((FakeComponent) this.getTaskOwner()).
							doUnistallPluginFrom(
									pluginInboundPortURI, pluginURI);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
	}

	// -------------------------------------------------------------------------
	// Plug-in instance variables and base constructor
	// -------------------------------------------------------------------------

	/** URI of the plug-in.													*/
	private final AtomicReference<String>		plugInURI;
	/** component holding this plug-in										*/
	private final AtomicReference<ComponentI>	owner;
	/** URI of the preferred executor service used to execute services
	 *  on owner or null if none.											*/
	private final AtomicReference<String>		preferredExecutorServiceURI;
	/** index of the preferred executor service used to execute services
	 *  on owner; negative if none.											*/
	private final AtomicInteger					preferredExecutorServiceIndex;

	/**
	 * create a new plug-in instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public				AbstractPlugin()
	{
		super();
		this.owner = new AtomicReference<ComponentI>(null);
		this.plugInURI = new AtomicReference<String>(null);
		this.preferredExecutorServiceURI = new AtomicReference<String>(null);
		this.preferredExecutorServiceIndex = new AtomicInteger(-1);
	}
	
	// --------------------------------------------------------------------
	// Plug-in base services
	// --------------------------------------------------------------------

	/**
	 * get the component owner of this plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the component owner of this plug-in.
	 */
	protected ComponentI	getOwner()
	{
		return this.owner.get();
	}

	/**
	 * set the component owner of this plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code getOwner() == null}
	 * pre	{@code owner != null}
	 * post	{@code getOwner() != null}
	 * </pre>
	 *
	 * @param owner		the component that will own this plug-in.
	 */
	protected void		setOwner(ComponentI owner)
	{
		assert	owner != null : new PreconditionException("owner != null");
		assert	this.getOwner() == null :
					new PreconditionException("getOwner() == null");

		this.owner.set(owner);
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#getPluginURI()
	 */
	@Override
	public String		getPluginURI()
	{
		return this.plugInURI.get();
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#setPluginURI(java.lang.String)
	 */
	@Override
	public void			setPluginURI(String uri)
	{
		assert	uri != null : new PreconditionException("uri != null");
		assert	this.getPluginURI() == null :
					new PreconditionException("getPluginURI() == null");

		this.plugInURI.set(uri);
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#getPreferredExecutionServiceURI()
	 */
	@Override
	public String		getPreferredExecutionServiceURI()
	{
		return this.preferredExecutorServiceURI.get();
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#getPreferredExecutionServiceIndex()
	 */
	@Override
	public int			getPreferredExecutionServiceIndex()
	{
		return this.preferredExecutorServiceIndex.get();
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#setPreferredExecutionServiceURI(java.lang.String)
	 */
	@Override
	public void			setPreferredExecutionServiceURI(
		String executorServiceURI
		)
	{
		assert	executorServiceURI != null :
					new PreconditionException("executorServiceURI != null");
		assert	this.getOwner() == null ||
							this.getOwner().validExecutorServiceURI(
														executorServiceURI) :
					new PreconditionException(
							"getOwner() == null || " + 
							"this.getOwner().validExecutorServiceURI(" +
							"executorServiceURI)");

		this.preferredExecutorServiceURI.set(executorServiceURI);

		assert	executorServiceURI.equals(
									this.getPreferredExecutionServiceURI());
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#installOn(fr.sorbonne_u.components.ComponentI)
	 */
	@Override
	public void			installOn(ComponentI owner) throws Exception
	{
		assert	owner != null : new PreconditionException("owner != null");
		assert	this.getPluginURI() != null :
					new PreconditionException("getPluginURI() != null");
		assert	!owner.isInstalled(this.getPluginURI()) :
					new PreconditionException(
							"!owner.isInstalled(getPluginURI())");
		assert	getPreferredExecutionServiceURI() == null ||
				 			owner.validExecutorServiceURI(
				 						getPreferredExecutionServiceURI()) :
				 	new PreconditionException(
				 			"getPreferredExecutionServiceURI() == null || " + 
				 			"owner.validExecutorServiceURI(" + 
				 			"getPreferredExecutionServiceURI())");

		this.owner.set(owner);
		if (this.getPreferredExecutionServiceURI() != null) {
			this.preferredExecutorServiceIndex.set(
					((AbstractComponent)this.getOwner()).
							getExecutorServiceIndex(
									this.getPreferredExecutionServiceURI()));
		}
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#initialise()
	 */
	@Override
	public void			initialise() throws Exception
	{
		assert	AbstractPlugin.initialisedAtAbstractPluginLevel(this) :
					new PostconditionException("isInitialised()");
	}

	/**
	 * check if the plug-in is initialised but only at the
	 * <code>AbstractPlugin</code> level.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param p	plug-in to be tested.
	 * @return	true if <code>p</code> appears as initialised at the <code>AbstractPlugin</code> level.
	 */
	private static boolean	initialisedAtAbstractPluginLevel(
		AbstractPlugin p
		)
	{
		return p.getOwner() != null;
	}

	/**
	 * Default behaviour; should be extended in subclasses.
	 * @see fr.sorbonne_u.components.PluginI#isInitialised()
	 */
	@Override
	public boolean		isInitialised()
	{
		return AbstractPlugin.initialisedAtAbstractPluginLevel(this);
	}

	// -------------------------------------------------------------------------
	// Plug-in methods linking it to the base services of components
	// -------------------------------------------------------------------------

	/**
	 * find a port in the owner component, a method used in plug-in
	 * objects to access their owner component in a way other objects
	 * can't.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code portURI != null}
	 * pre	{@code getOwner() != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param portURI	the URI a the sought port.
	 * @return			the port with the given URI or null if not found.
	 */
	protected PortI		findPortFromURI(String portURI)
	{
		assert	portURI != null :
					new PreconditionException("portURI != null");
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");


		return ((AbstractComponent)this.getOwner()).findPortFromURI(portURI);
	}

	/**
	 * add a required interface to the required interfaces of the
	 * owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code getOwner() != null}
	 * pre	{@code getOwner().notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code !getOwner().isRequiredInterface(inter)}
	 * post	{@code isRequiredInterface(inter)}
	 * </pre>
	 *
	 * @param inter		required interface to be added.
	 */
	protected void		addRequiredInterface(Class<? extends RequiredCI> inter)
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");

		((AbstractComponent)this.getOwner()).addRequiredInterface(inter);
	}

	/**
	 * add an offered interface to the offered interfaces of the
	 * owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code getOwner() != null}
	 * pre	{@code getOwner().notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code !getOwner().isOfferedInterface(inter)}
	 * post	{@code getOwner().isOfferedInterface(inter)}
	 * </pre>
	 *
	 * @param inter		offered interface to be added.
	 */
	protected void		addOfferedInterface(Class<? extends OfferedCI> inter)
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");

		((AbstractComponent)this.getOwner()).addOfferedInterface(inter);
	}

	/**
	 * remove a required interface from the required interfaces of the
	 * owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code getOwner() != null}
	 * pre	{@code getOwner().notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code getOwner().isRequiredInterface(inter)}
	 * pre	{@code getOwner().findPortsFromInterface(inter) == null || getOwner().findPortsFromInterface(inter).isEmpty()}
	 * post	{@code !getOwner().isRequiredInterface(inter)}
	 * </pre>
	 *
	 * @param inter required interface to be removed.
	 */
	protected void		removeRequiredInterface(
		Class<? extends RequiredCI> inter
		)
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");

		((AbstractComponent)this.getOwner()).removeRequiredInterface(inter);
	}

	/**
	 * remove an offered interface from the offered interfaces of the
	 * owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code getOwner() != null}
	 * pre	{@code getOwner().notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code getOwner().isOfferedInterface(inter)}
	 * pre	{@code getOwner().findPortsFromInterface(inter) == null || getOwner().findPortsFromInterface(inter).isEmpty()}
	 * post	{@code !getOwner().isOfferedInterface(inter)}
	 * </pre>
	 *
	 * @param inter	offered interface to be removed
	 */
	protected void		removeOfferedInterface(
		Class<? extends OfferedCI> inter
		)
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");

		((AbstractComponent)this.getOwner()).removeOfferedInterface(inter);
	}

	/**
	 * log a message using the owner component logging facilities.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code getOwner() != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param message	string to be logged.
	 */
	protected void		logMessage(String message)
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");

		this.getOwner().logMessage(message);
	}

	/**
	 * create a new user-defined executor service under the given URI and
	 * with the given number of threads.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null}
	 * pre	{@code nbThreads > 0}
	 * pre 	{@code getOwner() != null}
	 * pre	{@code !getOwner().validExecutorServiceURI(uri)}
	 * post	{@code getOwner().validExecutorServiceURI(uri)}
	 * </pre>
	 *
	 * @param uri			URI of the new executor service.
	 * @param nbThreads		number of threads of the new executor service.
	 * @param schedulable	if true, the new executor service is schedulable otherwise it is not.
	 * @return				the index associated with the new executor service.
	 */
	protected int			createNewExecutorService(
		String uri,
		int nbThreads,
		boolean schedulable
		)
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	!this.getOwner().validExecutorServiceURI(uri) :
					new PreconditionException(
									"getOwner().validExecutorServiceURI(uri)");

		return ((AbstractComponent)this.getOwner()).
						createNewExecutorService(uri, nbThreads, schedulable);
	}

	/**
	 * create a new user-defined executor service under the given URI and
	 * with the given number of threads.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null}
	 * pre	{@code nbThreads > 0}
	 * pre 	{@code getOwner() != null}
	 * pre	{@code !getOwner().validExecutorServiceURI(uri)}
	 * pre	{@code factory != null}
	 * post	{@code getOwner().validExecutorServiceURI(uri)}
	 * </pre>
	 *
	 * @param uri			URI of the new executor service.
	 * @param nbThreads		number of threads of the new executor service.
	 * @param factory		an executor service factory used to create the new thread pool.
	 * @return				the index associated with the new executor service.
	 */
	protected int			createNewExecutorService(
		String uri,
		int nbThreads,
		ExecutorServiceFactory factory
		)
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	!this.getOwner().validExecutorServiceURI(uri) :
					new PreconditionException(
									"getOwner().validExecutorServiceURI(uri)");

		return ((AbstractComponent)this.getOwner()).
							createNewExecutorService(uri, nbThreads, factory);
	}

	/**
	 * run the <code>ComponentTask</code> on the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * pre	{@code getOwner().validExecutorServiceIndex(executorServiceIndex)}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceIndex			index of the executor service that will run the task.
	 * @param t								component task to be executed as main task.
	 * @return								a future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected Future<?>		runTaskOnComponent(
		int executorServiceIndex,
		ComponentTask t
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	this.getOwner().validExecutorServiceIndex(executorServiceIndex) :
					new PreconditionException(
							"getOwner().validExecutorServiceIndex("
													+ "executorServiceIndex)");

		return ((AbstractComponent)this.getOwner()).
								runTaskOnComponent(executorServiceIndex, t);
	}

	/**
	 * run the <code>ComponentTask</code> on the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * pre	{@code getOwner().validExecutorServiceURI(executorServiceURI)}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceURI			URI of the executor service that will run the task.
	 * @param t								component task to be executed as main task.
	 * @return								a future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected Future<?>		runTaskOnComponent(
		String executorServiceURI,
		ComponentTask t
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	this.getOwner().validExecutorServiceURI(executorServiceURI) :
					new PreconditionException(
							"getOwner().validExecutorServiceURI("
													+ "executorServiceURI)");

		return ((AbstractComponent)this.getOwner()).
								runTaskOnComponent(executorServiceURI, t);
	}

	/**
	 * run the <code>ComponentTask</code> on the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param t								component task to be executed as main task.
	 * @return								a future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected Future<?>		runTaskOnComponent(ComponentTask t)
	throws	AssertionError,
			RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");

		if (this.getPreferredExecutionServiceURI() != null) {
			return ((AbstractComponent)this.getOwner()).runTaskOnComponent(
								this.getPreferredExecutionServiceIndex(), t);
		} else {
			return ((AbstractComponent)this.getOwner()).runTaskOnComponent(t);
		}
	}	

	/**
	 * schedule a <code>ComponentTask</code> to be run after a given delay
	 * on the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * pre	{@code getOwner().validExecutorServiceIndex(executorServiceIndex)}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceIndex			index of the executor service that will run the task.
	 * @param t								task to be scheduled.
	 * @param delay							delay after which the task must be run.
	 * @param u								time unit in which the delay is expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskOnComponent(
		int executorServiceIndex,
		ComponentTask t,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	this.getOwner().validExecutorServiceIndex(executorServiceIndex) :
					new PreconditionException(
							"getOwner().validExecutorServiceIndex("
													+ "executorServiceIndex)");

		return ((AbstractComponent)this.getOwner()).
					scheduleTaskOnComponent(executorServiceIndex, t, delay, u);
	}


	/**
	 * schedule a <code>ComponentTask</code> to be run after a given delay
	 * on the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * pre	{@code getOwner().validExecutorServiceURI(executorServiceURI)}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceURI			URI of the executor service that will run the task.
	 * @param t								task to be scheduled.
	 * @param delay							delay after which the task must be run.
	 * @param u								time unit in which the delay is expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskOnComponent(
		String executorServiceURI,
		ComponentTask t,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	this.getOwner().validExecutorServiceURI(executorServiceURI) :
					new PreconditionException(
							"getOwner().validExecutorServiceURI("
													+ "executorServiceURI)");

		return ((AbstractComponent)this.getOwner()).
					scheduleTaskOnComponent(executorServiceURI, t, delay, u);
	}


	/**
	 * schedule a <code>ComponentTask</code> to be run after a given delay
	 * on the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param t								task to be scheduled.
	 * @param delay							delay after which the task must be run.
	 * @param u								time unit in which the delay is expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskOnComponent(
		ComponentTask t,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");

		if (this.getPreferredExecutionServiceURI() != null) {
			return ((AbstractComponent)this.getOwner()).
					scheduleTaskOnComponent(
						this.getPreferredExecutionServiceIndex(), t, delay, u);
		} else {
			return ((AbstractComponent)this.getOwner()).
									scheduleTaskOnComponent(t, delay, u);
		}
	}


	/**
	 * schedule a <code>ComponentTask</code> that becomes enabled first after
	 * the given initial delay, and subsequently with the given period; that
	 * is executions will commence after <code>initialDelay</code> then
	 * <code>initialDelay+period</code>, the
	 * <code>initialDelay + 2 * period</code>, and so on. If any execution
	 * of the task encounters an exception, subsequent executions are suppressed.
	 * Otherwise, the task will only terminate via cancellation or termination
	 * of the executor. If any execution of this task takes longer than its
	 * period, then subsequent executions may start late, but will not
	 * concurrently execute.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * pre	{@code getOwner().validExecutorServiceIndex(executorServiceIndex)}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceIndex	index of the executor service that will run the task.
	 * @param t						task to be scheduled.
	 * @param initialDelay			delay after which the task begins to run.
	 * @param period				period between successive executions.
	 * @param u						time unit in which the initial delay and the period are expressed.
	 * @return						a scheduled future allowing to cancel and synchronize on the task execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskAtFixedRateOnComponent(
		int executorServiceIndex,
		ComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	this.getOwner().validExecutorServiceIndex(executorServiceIndex) :
					new PreconditionException(
							"getOwner().validExecutorServiceIndex("
													+ "executorServiceIndex)");

		return ((AbstractComponent)this.getOwner()).
					scheduleTaskAtFixedRateOnComponent(
							executorServiceIndex, t, initialDelay, period, u);
	}

	/**
	 * schedule a <code>ComponentTask</code> that becomes enabled first after
	 * the given initial delay, and subsequently with the given period; that
	 * is executions will commence after <code>initialDelay</code> then
	 * <code>initialDelay+period</code>, the
	 * <code>initialDelay + 2 * period</code>, and so on. If any execution
	 * of the task encounters an exception, subsequent executions are suppressed.
	 * Otherwise, the task will only terminate via cancellation or termination
	 * of the executor. If any execution of this task takes longer than its
	 * period, then subsequent executions may start late, but will not
	 * concurrently execute.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * pre	{@code getOwner().validExecutorServiceURI(executorServiceURI)}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceURI			URI of the executor service that will run the task.
	 * @param t								task to be scheduled.
	 * @param initialDelay					delay after which the task begins to run.
	 * @param period						period between successive executions.
	 * @param u								time unit in which the initial delay and the period are expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskAtFixedRateOnComponent(
		String executorServiceURI,
		ComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	this.getOwner().validExecutorServiceURI(executorServiceURI) :
					new PreconditionException(
							"getOwner().validExecutorServiceURI("
													+ "executorServiceURI)");

		return ((AbstractComponent)this.getOwner()).
					scheduleTaskAtFixedRateOnComponent(
							executorServiceURI, t, initialDelay, period, u);
	}

	/**
	 * schedule a <code>ComponentTask</code> that becomes enabled first after
	 * the given initial delay, and subsequently with the given period; that
	 * is executions will commence after <code>initialDelay</code> then
	 * <code>initialDelay+period</code>, the
	 * <code>initialDelay + 2 * period</code>, and so on. If any execution
	 * of the task encounters an exception, subsequent executions are suppressed.
	 * Otherwise, the task will only terminate via cancellation or termination
	 * of the executor. If any execution of this task takes longer than its
	 * period, then subsequent executions may start late, but will not
	 * concurrently execute.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param t								task to be scheduled.
	 * @param initialDelay					delay after which the task begins to run.
	 * @param period						period between successive executions.
	 * @param u								time unit in which the initial delay and the period are expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskAtFixedRateOnComponent(
		ComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");

		if (this.getPreferredExecutionServiceURI() != null) {
			return ((AbstractComponent)this.getOwner()).
					scheduleTaskAtFixedRateOnComponent(
								this.getPreferredExecutionServiceIndex(),
								t, initialDelay, period, u);
		} else {
			return ((AbstractComponent)this.getOwner()).
							scheduleTaskAtFixedRateOnComponent(
												t, initialDelay, period, u);
		}
	}

	/**
	 * schedule a <code>ComponentTask</code> that becomes enabled first after
	 * the given initial delay, and subsequently with the given delay between
	 * the termination of one execution and the beginning of the next. If any
	 * execution of the task encounters an exception, subsequent executions
	 * are suppressed. Otherwise, the task will only terminate via cancellation
	 * or termination of the executor.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * pre	{@code getOwner().validExecutorServiceIndex(executorServiceIndex)}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceIndex			index of the executor service that will run the task.
	 * @param t								task to be scheduled.
	 * @param initialDelay					delay after which the task begins to run.
	 * @param delay							delay between the termination of one execution and the beginning of the next.
	 * @param u								time unit in which the initial delay and the delay are expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskWithFixedDelayOnComponent(
		int executorServiceIndex,
		ComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	this.getOwner().validExecutorServiceIndex(executorServiceIndex) :
					new PreconditionException(
							"getOwner().validExecutorServiceIndex("
													+ "executorServiceIndex)");

		return ((AbstractComponent)this.getOwner()).
					scheduleTaskWithFixedDelayOnComponent(
							executorServiceIndex, t, initialDelay, delay, u);
	}

	/**
	 * schedule a <code>ComponentTask</code> that becomes enabled first after
	 * the given initial delay, and subsequently with the given delay between
	 * the termination of one execution and the beginning of the next. If any
	 * execution of the task encounters an exception, subsequent executions
	 * are suppressed. Otherwise, the task will only terminate via cancellation
	 * or termination of the executor.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * pre	{@code getOwner().validExecutorServiceURI(executorServiceURI)}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceURI			URI of the executor service that will run the task.
	 * @param t								task to be scheduled.
	 * @param initialDelay					delay after which the task begins to run.
	 * @param delay							delay between the termination of one execution and the beginning of the next.
	 * @param u								time unit in which the initial delay and the delay are expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskWithFixedDelayOnComponent(
		String executorServiceURI,
		ComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	this.getOwner().validExecutorServiceURI(executorServiceURI) :
					new PreconditionException(
							"getOwner().validExecutorServiceURI("
													+ "executorServiceURI)");

		return ((AbstractComponent)this.getOwner()).
					scheduleTaskWithFixedDelayOnComponent(
							executorServiceURI, t, initialDelay, delay, u);
	}

	/**
	 * schedule a <code>ComponentTask</code> that becomes enabled first after
	 * the given initial delay, and subsequently with the given delay between
	 * the termination of one execution and the beginning of the next. If any
	 * execution of the task encounters an exception, subsequent executions
	 * are suppressed. Otherwise, the task will only terminate via cancellation
	 * or termination of the executor.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param t								task to be scheduled.
	 * @param initialDelay					delay after which the task begins to run.
	 * @param delay							delay between the termination of one execution and the beginning of the next.
	 * @param u								time unit in which the initial delay and the delay are expressed.
	 * @return								a scheduled future allowing to cancel and synchronize on the task execution.
	 * @throws AssertionError				if the preconditions are not satisfied.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected ScheduledFuture<?>	scheduleTaskWithFixedDelayOnComponent(
		ComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");

		if (this.getPreferredExecutionServiceURI() != null) {
			return ((AbstractComponent)this.getOwner()).
					scheduleTaskWithFixedDelayOnComponent(
								this.getPreferredExecutionServiceIndex(),
								t, initialDelay, delay, u);
		} else {
			return ((AbstractComponent)this.getOwner()).
						scheduleTaskWithFixedDelayOnComponent(
												t, initialDelay, delay, u);
		}
	}

	/**
	 * execute a request represented by a <code>ComponentService</code> on the
	 * owner component.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * pre	{@code getOwner().validExecutorServiceIndex(executorServiceIndex)}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param <T>							the type of the value returned by the request.
	 * @param executorServiceIndex			index of the executor service that will run the task.
	 * @param request						service request to be executed on the component.
	 * @return								a future value embedding the result of the task.
	 * @throws AssertionError				if the component is not started, the index is not valid or the request is null.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected <T> Future<T>		handleRequest(
		int executorServiceIndex,
		ComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	this.getOwner().validExecutorServiceIndex(executorServiceIndex) :
					new PreconditionException(
							"getOwner().validExecutorServiceIndex("
													+ "executorServiceIndex)");

		return ((AbstractComponent)this.getOwner()).baselineHandleRequest(
											executorServiceIndex, request);
	}

	/**
	 * execute a request represented by a <code>ComponentService</code> on the
	 * owner component.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * pre	{@code getOwner().validExecutorServiceURI(executorServiceURI)}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param <T>					the type of the value returned by the request.
	 * @param executorServiceURI	URI of the executor service that will run the task.
	 * @param request				service request to be executed on the component.
	 * @return						a future value embedding the result of the task.
	 * @throws AssertionError				if the component is not started, the URI is not valid or the request is null.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected <T> Future<T>		handleRequest(
		String executorServiceURI,
		ComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	this.getOwner().validExecutorServiceURI(executorServiceURI) :
					new PreconditionException(
							"getOwner().validExecutorServiceURI("
													+ "executorServiceURI)");

		return ((AbstractComponent)this.getOwner()).baselineHandleRequest(
												executorServiceURI, request);
	}


	/**
	 * execute a request represented by a <code>ComponentService</code> on the
	 * owner component.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * This method is meant to be used when programmers need to manage within
	 * a component requests with futures. It can be requests executed as
	 * services of the component or calls to other components which are
	 * synchronous but that the calling component wants to manage as
	 * asynchronous tasks.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param <T>							the type of the value returned by the request.
	 * @param request						service request to be executed on the component.
	 * @return								a future value embedding the result of the task.
	 * @throws AssertionError				if the component is not started or the request is null.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected <T> Future<T>		handleRequest(
		ComponentService<T> request
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");

		if (this.getPreferredExecutionServiceURI() != null) {
			return ((AbstractComponent)this.getOwner()).baselineHandleRequest(
							this.getPreferredExecutionServiceIndex(), request);
		} else {
			return ((AbstractComponent)this.getOwner()).baselineHandleRequest(request);
		}
	}

	/**
	 * schedule a service for execution on the given executor service after a
	 * given delay.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * pre	{@code getOwner().validExecutorServiceIndex(executorServiceIndex)}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param <T>							the type of the value returned by the request.
	 * @param executorServiceIndex			index of the executor service that will run the task.
	 * @param request						service request to be scheduled.
	 * @param delay							delay after which the task must be run.
	 * @param u								time unit in which the delay is expressed.
	 * @return								a scheduled future to synchronise with the task.
	 * @throws AssertionError				if the component is not started, this index is not valid, the executor is not schedulable or the request in null.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected <T> ScheduledFuture<T>	scheduleRequest(
		int executorServiceIndex,
		ComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	this.getOwner().validExecutorServiceIndex(executorServiceIndex) :
					new PreconditionException(
							"getOwner().validExecutorServiceIndex("
													+ "executorServiceIndex)");

		return ((AbstractComponent)this.getOwner()).scheduleRequest(
									executorServiceIndex, request, delay, u);
	}

	/**
	 * schedule a service for execution on the given executor service after a
	 * given delay.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * pre	{@code getOwner().validExecutorServiceURI(executorServiceURI)}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param <T>							the type of the value returned by the request.
	 * @param executorServiceURI			URI of the executor service that will run the task.
	 * @param request						service request to be scheduled.
	 * @param delay							delay after which the task must be run.
	 * @param u								time unit in which the delay is expressed.
	 * @return								a scheduled future to synchronise with the task.
	 * @throws AssertionError				if the component is not started, this index is not valid, the executor is not schedulable or the request in null.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected <T> ScheduledFuture<T>	scheduleRequest(
		String executorServiceURI,
		ComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	this.getOwner().validExecutorServiceURI(executorServiceURI) :
					new PreconditionException(
							"getOwner().validExecutorServiceURI("
													+ "executorServiceURI)");

		return ((AbstractComponent)this.getOwner()).scheduleRequest(
									executorServiceURI, request, delay, u);
	}

	/**
	 * schedule a service for execution on the preferred executor service after
	 * a given delay.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre 	{@code getOwner() != null}
	 * pre	{@code getPreferredExecutionServiceURI() != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param <T>							the type of the value returned by the request.
	 * @param request						service request to be scheduled.
	 * @param delay							delay after which the task must be run.
	 * @param u								time unit in which the delay is expressed.
	 * @return								a scheduled future to synchronise with the task.
	 * @throws AssertionError				if the component is not started, this index is not valid, the executor is not schedulable or the request in null.
	 * @throws RejectedExecutionException	if the task cannot be scheduled for execution.
	 */
	protected <T> ScheduledFuture<T>	scheduleRequest(
		ComponentService<T> request,
		long delay,
		TimeUnit u
		) throws	AssertionError,
					RejectedExecutionException
	{
		assert	this.getOwner() != null :
					new PreconditionException("getOwner() != null");
		assert	this.getPreferredExecutionServiceURI() != null :
					new PreconditionException(
								"getPreferredExecutionServiceURI() != null");

		return ((AbstractComponent)this.getOwner()).scheduleRequest(
									this.getPreferredExecutionServiceIndex(),
									request, delay, u);
	}
}
// -----------------------------------------------------------------------------
