package fr.sorbonne_u.components;

import fr.sorbonne_u.components.helpers.Logger;
import fr.sorbonne_u.components.helpers.TracerOnConsole;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.components.reflection.interfaces.IntercessionI;

//-----------------------------------------------------------------------------
/**
 * The class <code>IntercessionInboundPort</code> defines the inbound port
 * associated the interface <code>IntercessionI</code>.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-02-16</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				IntercessionInboundPort
extends		AbstractInboundPort
implements	IntercessionI
{
	private static final long serialVersionUID = 1L;

	public				IntercessionInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, IntercessionI.class, owner) ;
	}

	public				IntercessionInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(IntercessionI.class, owner) ;
	}

	// ------------------------------------------------------------------------
	// Plug-ins facilities
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#installPlugin(fr.sorbonne_u.components.PluginI)
	 */
	@Override
	public void			installPlugin(final PluginI plugin) throws Exception
	{
		this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().installPlugin(plugin) ;
							return null ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#initialisePlugin(java.lang.String)
	 */
	@Override
	public void			initialisePlugin(final String pluginURI)
	throws Exception
	{
		this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().initialisePlugin(pluginURI) ;
							return null ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#finalisePlugin(java.lang.String)
	 */
	@Override
	public void			finalisePlugin(String pluginURI) throws Exception
	{
		this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().finalisePlugin(pluginURI) ;
							return null;
						}
					}) ;

	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#uninstallPlugin(java.lang.String)
	 */
	@Override
	public void			uninstallPlugin(final String pluginId) throws Exception
	{
		this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().uninstallPlugin(pluginId) ;
							return null;
						}
					}) ;
	}

	// ------------------------------------------------------------------------
	// Logging facilities
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#toggleLogging()
	 */
	@Override
	public void			toggleLogging() throws Exception
	{
		this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().toggleLogging() ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#setLogger(fr.sorbonne_u.components.helpers.Logger)
	 */
	@Override
	public void			setLogger(Logger logger) throws Exception
	{
		this.getOwner().handleRequestSync(
				new AbstractComponent.AbstractService<Void>() {
					@Override
					public Void call() throws Exception {
						this.getServiceOwner().setLogger(logger) ;
						return null ;
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#toggleTracing()
	 */
	@Override
	public void			toggleTracing() throws Exception
	{
		this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().toggleTracing() ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#setTracer(fr.sorbonne_u.components.helpers.TracerOnConsole)
	 */
	@Override
	public void			setTracer(TracerOnConsole tracer) throws Exception
	{
		this.getOwner().handleRequestSync(
				new AbstractComponent.AbstractService<Void>() {
					@Override
					public Void call() throws Exception {
						this.getServiceOwner().setTracer(tracer) ;
						return null ;
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#logMessage(java.lang.String)
	 */
	@Override
	public void			logMessage(final String message) throws Exception
	{
		this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().logMessage(message) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#printExecutionLog()
	 */
	@Override
	public void			printExecutionLog() throws Exception
	{
		this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().printExecutionLog() ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#printExecutionLogOnFile(java.lang.String)
	 */
	@Override
	public void			printExecutionLogOnFile(final String fileName)
	throws Exception
	{
		this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().
										printExecutionLogOnFile(fileName) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#traceMessage(java.lang.String)
	 */
	@Override
	public void			traceMessage(String message) throws Exception
	{
		this.getOwner().handleRequestSync(
				new AbstractComponent.AbstractService<Void>() {
					@Override
					public Void call() throws Exception {
						this.getServiceOwner().traceMessage(message) ;
						return null ;
					}
				}) ;
	}

	// ------------------------------------------------------------------------
	// Implemented interfaces management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#addRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			addRequiredInterface(final Class<?> inter)
	throws Exception
	{
		this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().
											addRequiredInterface(inter) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#removeRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			removeRequiredInterface(final Class<?> inter)
	throws Exception
	{
		this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().
											removeRequiredInterface(inter) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#addOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			addOfferedInterface(final Class<?> inter) throws Exception
	{
		this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().addOfferedInterface(inter) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#removeOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			removeOfferedInterface(final Class<?> inter)
	throws Exception
	{
		this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().
											removeOfferedInterface(inter) ;
							return null;
						}
					}) ;
	}

	// ------------------------------------------------------------------------
	// Port management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#doPortConnection(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void			doPortConnection(
		final String portURI,
		final String otherPortURI,
		final String ccname
		) throws Exception
	{
		this.getOwner().handleRequestSync(
				new AbstractComponent.AbstractService<Void>() {
					@Override
					public Void call() throws Exception {
						this.getServiceOwner().
							doPortConnection(portURI, otherPortURI, ccname) ;
						return null ;
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#doPortDisconnection(java.lang.String)
	 */
	@Override
	public void			doPortDisconnection(final String portURI)
	throws Exception
	{
		this.getOwner().handleRequestSync(
				new AbstractComponent.AbstractService<Void>() {
					@Override
					public Void call() throws Exception {
						this.getServiceOwner().doPortDisconnection(portURI) ;
						return null ;
					}
				}) ;
	}

	// ------------------------------------------------------------------------
	// Reflection facility
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#newInstance(java.lang.Object[])
	 */
	@Override
	public ComponentI		newInstance(Object[] parameters)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<ComponentI>() {
						@Override
						public ComponentI call() throws Exception {
							return this.getServiceOwner().
													newInstance(parameters) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#invokeService(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object		invokeService(String name, Object[] params)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Object>() {
						@Override
						public Object call() throws Exception {
							return this.getServiceOwner().
												invokeService(name, params) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#invokeServiceSync(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object		invokeServiceSync(String name, Object[] params)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Object>() {
						@Override
						public Object call() throws Exception {
							return this.getServiceOwner().
											invokeServiceSync(name, params) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#invokeServiceAsync(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void			invokeServiceAsync(String name, Object[] params)
	throws Exception
	{
		this.getOwner().handleRequestSync(
			new AbstractComponent.AbstractService<Void>() {
				@Override
				public Void call() throws Exception {
					this.getServiceOwner().invokeServiceAsync(name, params) ;
					return null ;
				}
			}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#insertBeforeService(java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public void			insertBeforeService(
		String methodName,
		String[] parametersCanonicalClassNames,
		String code
		) throws Exception
	{
		this.getOwner().handleRequestSync(
				new AbstractComponent.AbstractService<Void>() {
					@Override
					public Void call() throws Exception {
						this.getServiceOwner().insertBeforeService(
											methodName,
											parametersCanonicalClassNames,
											code) ;
						return null ;
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#insertAfterService(java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public void			insertAfterService(
		String methodName,
		String[] parametersCanonicalClassNames,
		String code
		) throws Exception
	{
		this.getOwner().handleRequestSync(
				new AbstractComponent.AbstractService<Void>() {
					@Override
					public Void call() throws Exception {
						this.getServiceOwner().insertBeforeService(
									methodName,
									parametersCanonicalClassNames,
									code) ;
						return null ;
					}
				}) ;
	}
}
//-----------------------------------------------------------------------------
