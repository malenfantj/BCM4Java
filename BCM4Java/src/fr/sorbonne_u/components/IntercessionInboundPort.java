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

import fr.sorbonne_u.components.helpers.Logger;
import fr.sorbonne_u.components.helpers.TracerWindow;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.components.reflection.interfaces.IntercessionCI;

// -----------------------------------------------------------------------------
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
implements	IntercessionCI
{
	private static final long serialVersionUID = 1L;

	public				IntercessionInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, IntercessionCI.class, owner) ;
	}

	public				IntercessionInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(IntercessionCI.class, owner) ;
	}

	// -------------------------------------------------------------------------
	// Plug-ins facilities
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#installPlugin(fr.sorbonne_u.components.PluginI)
	 */
	@Override
	public void			installPlugin(final PluginI plugin) throws Exception
	{
		this.getOwner().handleRequest(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().installPlugin(plugin) ;
							return null ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#initialisePlugin(java.lang.String)
	 */
	@Override
	public void			initialisePlugin(final String pluginURI)
	throws Exception
	{
		this.getOwner().handleRequest(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().initialisePlugin(pluginURI) ;
							return null ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#finalisePlugin(java.lang.String)
	 */
	@Override
	public void			finalisePlugin(String pluginURI) throws Exception
	{
		this.getOwner().handleRequest(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().finalisePlugin(pluginURI) ;
							return null;
						}
					}) ;

	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#uninstallPlugin(java.lang.String)
	 */
	@Override
	public void			uninstallPlugin(final String pluginId) throws Exception
	{
		this.getOwner().handleRequest(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().uninstallPlugin(pluginId) ;
							return null;
						}
					}) ;
	}

	// -------------------------------------------------------------------------
	// Logging facilities
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#toggleLogging()
	 */
	@Override
	public void			toggleLogging() throws Exception
	{
		this.getOwner().handleRequest(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().toggleLogging() ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#setLogger(fr.sorbonne_u.components.helpers.Logger)
	 */
	@Override
	public void			setLogger(Logger logger) throws Exception
	{
		this.getOwner().handleRequest(
				new AbstractComponent.AbstractService<Void>() {
					@Override
					public Void call() throws Exception {
						this.getServiceOwner().setLogger(logger) ;
						return null ;
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#toggleTracing()
	 */
	@Override
	public void			toggleTracing() throws Exception
	{
		this.getOwner().handleRequest(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().toggleTracing() ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#setTracer(fr.sorbonne_u.components.helpers.TracerWindow)
	 */
	@Override
	public void			setTracer(TracerWindow tracer) throws Exception
	{
		this.getOwner().handleRequest(
				new AbstractComponent.AbstractService<Void>() {
					@Override
					public Void call() throws Exception {
						this.getServiceOwner().setTracer(tracer) ;
						return null ;
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#logMessage(java.lang.String)
	 */
	@Override
	public void			logMessage(final String message) throws Exception
	{
		this.getOwner().handleRequest(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().logMessage(message) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#printExecutionLog()
	 */
	@Override
	public void			printExecutionLog() throws Exception
	{
		this.getOwner().handleRequest(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().printExecutionLog() ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#printExecutionLogOnFile(java.lang.String)
	 */
	@Override
	public void			printExecutionLogOnFile(final String fileName)
	throws Exception
	{
		this.getOwner().handleRequest(
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
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#traceMessage(java.lang.String)
	 */
	@Override
	public void			traceMessage(String message) throws Exception
	{
		this.getOwner().handleRequest(
				new AbstractComponent.AbstractService<Void>() {
					@Override
					public Void call() throws Exception {
						this.getServiceOwner().traceMessage(message) ;
						return null ;
					}
				}) ;
	}

	// -------------------------------------------------------------------------
	// Implemented interfaces management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#addRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			addRequiredInterface(
		final Class<? extends RequiredCI> inter
		) throws Exception
	{
		this.getOwner().handleRequest(
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
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#removeRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			removeRequiredInterface(
		final Class<? extends RequiredCI> inter
		) throws Exception
	{
		this.getOwner().handleRequest(
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
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#addOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			addOfferedInterface(
		final Class<? extends OfferedCI> inter
		) throws Exception
	{
		this.getOwner().handleRequest(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().addOfferedInterface(inter) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#removeOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			removeOfferedInterface(
		final Class<? extends OfferedCI> inter
		) throws Exception
	{
		this.getOwner().handleRequest(
					new AbstractComponent.AbstractService<Void>() {
						@Override
						public Void call() throws Exception {
							this.getServiceOwner().
											removeOfferedInterface(inter) ;
							return null;
						}
					}) ;
	}

	// -------------------------------------------------------------------------
	// Port management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#doPortConnection(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void			doPortConnection(
		final String portURI,
		final String otherPortURI,
		final String ccname
		) throws Exception
	{
		this.getOwner().handleRequest(
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
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#doPortDisconnection(java.lang.String)
	 */
	@Override
	public void			doPortDisconnection(final String portURI)
	throws Exception
	{
		this.getOwner().handleRequest(
				new AbstractComponent.AbstractService<Void>() {
					@Override
					public Void call() throws Exception {
						this.getServiceOwner().doPortDisconnection(portURI) ;
						return null ;
					}
				}) ;
	}

	// -------------------------------------------------------------------------
	// Reflection facility
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#invokeService(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object		invokeService(String name, Object[] params)
	throws Exception
	{
		return this.getOwner().handleRequest(
					new AbstractComponent.AbstractService<Object>() {
						@Override
						public Object call() throws Exception {
							return this.getServiceOwner().
												invokeService(name, params) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#invokeServiceSync(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object		invokeServiceSync(String name, Object[] params)
	throws Exception
	{
		return this.getOwner().handleRequest(
					new AbstractComponent.AbstractService<Object>() {
						@Override
						public Object call() throws Exception {
							return this.getServiceOwner().
											invokeServiceSync(name, params) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#invokeServiceAsync(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void			invokeServiceAsync(String name, Object[] params)
	throws Exception
	{
		this.getOwner().handleRequest(
			new AbstractComponent.AbstractService<Void>() {
				@Override
				public Void call() throws Exception {
					this.getServiceOwner().invokeServiceAsync(name, params) ;
					return null ;
				}
			}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		this.getOwner().runTask(
			new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						this.getTaskOwner().execute() ;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#runTask(fr.sorbonne_u.components.reflection.interfaces.IntercessionCI.AbstractRemoteComponentTask)
	 */
	@Override
	public void			runTask(AbstractRemoteComponentTask t)
	throws Exception
	{
		AbstractComponent.AbstractTask task =
				new AbstractComponent.AbstractTask() {
					public void run() {
						try {
							t.run() ;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} ;
		t.setComponentTask(task) ;
		this.getOwner().runTask(task) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#runTask(java.lang.String, fr.sorbonne_u.components.reflection.interfaces.IntercessionCI.AbstractRemoteComponentTask)
	 */
	@Override
	public void			runTask(
		String executorServiceURI,
		AbstractRemoteComponentTask t
		) throws Exception
	{
		AbstractComponent.AbstractTask task =
				new AbstractComponent.AbstractTask() {
					public void run() {
						try {
							t.run() ;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} ;
		t.setComponentTask(task) ;
		this.getOwner().runTask(executorServiceURI, task) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#insertBeforeService(java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public void			insertBeforeService(
		String methodName,
		String[] parametersCanonicalClassNames,
		String code
		) throws Exception
	{
		this.getOwner().handleRequest(
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
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#insertAfterService(java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public void			insertAfterService(
		String methodName,
		String[] parametersCanonicalClassNames,
		String code
		) throws Exception
	{
		this.getOwner().handleRequest(
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
// -----------------------------------------------------------------------------
