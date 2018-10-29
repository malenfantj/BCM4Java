package fr.sorbonne_u.components;

// Copyright Jacques Malenfant, Sorbonne Universite.
//
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
import fr.sorbonne_u.components.exceptions.PostconditionException;
import fr.sorbonne_u.components.exceptions.PreconditionException;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.components.reflection.connectors.ReflectionConnector;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionI;
import fr.sorbonne_u.components.reflection.ports.ReflectionOutboundPort;

//-----------------------------------------------------------------------------
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
 * invariant		true		// TODO
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

	// --------------------------------------------------------------------
	// Inner classes
	// --------------------------------------------------------------------

	/**
	 * The static class <code>Fake</code> implements a fake component used to
	 * call the services of the component on which the plug-in is to be
	 * installed or uninstalled.
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
	 * @version	$Name$ -- $Revision$ -- $Date$
	 */
	@RequiredInterfaces(required = {ReflectionI.class})
	protected static class	Fake
	extends		AbstractComponent
	{
		/** the outbound port used to call plug-in management services of the
		 * other component.													*/
		protected ReflectionOutboundPort	cpObp ;

		/**
		 * create a fake component with a component plug-in outbound port
		 * to be connected to the plug-in component owner.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @throws Exception		<i>todo.</i>
		 */
		public			Fake() throws Exception
		{
			super(0, 0) ;

			this.cpObp = new ReflectionOutboundPort(this) ;
			this.addPort(this.cpObp) ;
			this.cpObp.publishPort() ;
		}

		/**
		 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
		 */
		@Override
		public void finalise() throws Exception
		{
			this.cpObp.unpublishPort() ;
			this.removeRequiredInterface(ReflectionI.class) ;
			super.finalise();
		}

		/**
		 * install a plug-in on the component designated by the URI of its
		 * plug-in inbound port URI.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	plugin != null and pluginInboundPortURI != null
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param plugin					plug-in to be installed.
		 * @param pluginInboundPortURI	URI of the plug-in inbound port of the owner component.
		 * @throws Exception		<i>todo.</i>
		 */
		public void		doInstallPluginOn(
			PluginI plugin,
			String pluginInboundPortURI
			) throws Exception
		{
			assert	plugin != null && pluginInboundPortURI != null ;

			this.doPortConnection(
						this.cpObp.getPortURI(),
						pluginInboundPortURI,
						ReflectionConnector.class.getCanonicalName()) ;
			this.cpObp.installPlugin(plugin) ;
			this.doPortDisconnection(this.cpObp.getPortURI()) ;
		}

		/**
		 * 
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	pluginInboundPortURI != null and pluginURI != null
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param pluginInboundPortURI	inbound port URI of the plug-in.
		 * @param pluginURI				URI of the plug-in.
		 * @throws Exception		<i>todo.</i>
		 */
		public void		doFinalise(
			String pluginInboundPortURI,
			String pluginURI
			) throws Exception
		{
			this.doPortConnection(
					this.cpObp.getPortURI(),
					pluginInboundPortURI,
					ReflectionConnector.class.getCanonicalName()) ;
			this.cpObp.finalisePlugin(pluginURI) ;
			this.doPortDisconnection(this.cpObp.getPortURI()) ;
		}

		/**
		 * uninstall a plug-in on the owner component designated by the URI of
		 * its plug-in inbound port URI.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	pluginInboundPortURI != null and pluginURI != null
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param pluginInboundPortURI	URI of the plug-in inbound port of the owner component.
		 * @param pluginURI				URI of the plug-in to be uninstalled.
		 * @throws Exception		<i>todo.</i>
		 */
		public void		doUnistallPluginFrom(
			String pluginInboundPortURI,
			String pluginURI
			) throws Exception
		{
			this.doPortConnection(
					this.cpObp.getPortURI(),
					pluginInboundPortURI,
					ReflectionConnector.class.getCanonicalName()) ;
			this.cpObp.uninstallPlugin(pluginURI) ;
			this.doPortDisconnection(this.cpObp.getPortURI()) ;
		}
	}

	// --------------------------------------------------------------------
	// Plug-in static services
	// --------------------------------------------------------------------

	/**
	 * install a plug-in on a component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginInboundPortURI != null and plugin != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pluginInboundPortURI	URI of the plug-in management inbound port of the component.
	 * @param plugin					plug-in to be installed.
	 * @throws Exception		<i>todo.</i>
	 */
	public static void	installPluginOn(
		final String pluginInboundPortURI,
		final PluginI plugin
		) throws Exception
	{
		assert	pluginInboundPortURI != null && plugin != null ;

		Fake fake = new Fake() {} ;
		fake.runTask(new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								((Fake)this.getOwner()).doInstallPluginOn(
												plugin, pluginInboundPortURI) ;
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					 }) ;		
	}

	/**
	 * finalise a plug-in on a component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginInboundPortURI != null and pluginURI != null
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
		assert	pluginInboundPortURI != null && pluginURI != null ;


		Fake fake = new Fake() {} ;
		fake.runTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							((Fake) this.getOwner()).doFinalise(
										pluginInboundPortURI, pluginURI) ;
						} catch (Exception e) {
							throw new RuntimeException(e) ;
						}
					}
				 }) ;

	}

	/**
	 * uninstall a plug-in from a component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginInboundPortURI != null and pluginURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pluginInboundPortURI	URI of the plug-in management inbound port of the component.
	 * @param pluginURI				URI of the plug-in.
	 * @throws Exception		<i>todo.</i>
	 */
	public static void	uninstallPluginFrom(
		final String pluginInboundPortURI,
		final String pluginURI
		) throws Exception
	{
		assert	pluginInboundPortURI != null && pluginURI != null ;

		Fake fake = new Fake() {} ;
		fake.runTask(new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								((Fake) this.getOwner()).
										doUnistallPluginFrom(
											pluginInboundPortURI, pluginURI) ;
							} catch (Exception e) {
								throw new RuntimeException(e) ;
							}
						}
					 }) ;
	}

	// --------------------------------------------------------------------
	// Plug-in instance variables and base constructor
	// --------------------------------------------------------------------

	/** component holding this plug-in								*/
	protected ComponentI		owner ;
	/** The URI of the plug-in.										*/
	protected String			plugInURI ;
	
	public				AbstractPlugin()
	{
		super() ;
		this.plugInURI = null ;
	}

	// --------------------------------------------------------------------
	// Plug-in base services
	// --------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.PluginI#getPluginURI()
	 */
	@Override
	public String		getPluginURI()
	{
		return this.plugInURI ;
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#initialise()
	 */
	@Override
	public void			initialise() throws Exception
	{
		// By default, do nothing.
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#setPluginURI(java.lang.String)
	 */
	@Override
	public void			setPluginURI(String uri)
	throws Exception
	{
		assert	uri != null ;

		if (this.plugInURI != null) {
			throw new Exception("The URI of a plug-in can be set only once!") ;
		}
		this.plugInURI = uri ;
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#installOn(fr.sorbonne_u.components.ComponentI)
	 */
	@Override
	public void			installOn(ComponentI owner) throws Exception
	{
		assert	owner != null ;
		assert	this.getPluginURI() != null ;
		assert	!owner.isInstalled(this.getPluginURI()) ;

		this.owner = owner ;
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#isInitialised()
	 */
	@Override
	public boolean		isInitialised() throws Exception
	{
		return this.owner != null ;
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		// By default, do nothing.
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#uninstall()
	 */
	@Override
	public void			uninstall() throws Exception
	{
		this.finalise() ;
	}

	// --------------------------------------------------------------------
	// Plug-in methods linking it to the base services of components
	// --------------------------------------------------------------------

	/**
	 * add a port to the owner component, a method used in plug-in objects to
	 * access their owner component in a way other objects can't.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	p != null ;
	 * post	this.owner.isPortExisting(p.getPortURI())
	 * </pre>
	 *
	 * @param p port to be added.
	 * @throws Exception		<i>todo.</i>
	 */
	protected void		addPort(PortI p) throws Exception
	{
		assert	p != null : new PreconditionException("p != null") ;

		((AbstractComponent) this.owner).addPort(p) ;

		assert	this.owner.isPortExisting(p.getPortURI()) :
					new PostconditionException(
						"this.owner.isPortExisting(p.getPortURI())") ;
	}

	/**
	 * find a port in the owner component, a method used in plug-in
	 * objects to access their owner component in a way other objects
	 * can't.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	portURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param portURI	the URI a the sought port.
	 * @return			the port with the given URI or null if not found.
	 */
	protected PortI		findPortFromURI(String portURI)
	{
		assert	portURI != null :
					new PreconditionException("portURI != null") ;

		return ((AbstractComponent) this.owner).findPortFromURI(portURI) ;
	}
	/**
	 * log a message using the owner component logging facilities.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param message	string to be logged.
	 */
	protected void		logMessage(String message)
	{
		this.owner.logMessage(message) ;
	}
}
//-----------------------------------------------------------------------------
