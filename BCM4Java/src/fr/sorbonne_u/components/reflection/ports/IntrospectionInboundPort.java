package fr.sorbonne_u.components.reflection.ports;

//Copyright Jacques Malenfant, Sorbonne Universite.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import java.lang.annotation.Annotation;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ComponentStateI;
import fr.sorbonne_u.components.PluginI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.components.reflection.interfaces.IntrospectionI;
import fr.sorbonne_u.components.reflection.utils.ConstructorSignature;
import fr.sorbonne_u.components.reflection.utils.ServiceSignature;

//-----------------------------------------------------------------------------
/**
 * The class <code>IntrospectionInboundPort</code> defines the inbound port
 * associated the interface <code>IntrospectionI</code>.
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
public class				IntrospectionInboundPort
extends		AbstractInboundPort
implements	IntrospectionI
{
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public				IntrospectionInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, IntrospectionI.class, owner) ;
	}

	public				IntrospectionInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(IntrospectionI.class, owner) ;
	}

	// ------------------------------------------------------------------------
	// Plug-ins facilities
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#hasInstalledPlugins()
	 */
	@Override
	public boolean		hasInstalledPlugins() throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().hasInstalledPlugins() ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#isInstalled(java.lang.String)
	 */
	@Override
	public boolean		isInstalled(final String pluginId) throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().isInstalled(pluginId) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getPlugin(java.lang.String)
	 */
	@Override
	public PluginI		getPlugin(final String pluginURI)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<PluginI>() {
						@Override
						public PluginI call() throws Exception {
							return this.getOwner().getPlugin(pluginURI) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#isInitialised(java.lang.String)
	 */
	@Override
	public boolean		isInitialised(final String pluginURI)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().isInitialised(pluginURI) ;
						}
					}) ;
	}

	// ------------------------------------------------------------------------
	// Logging facilities
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#isLogging()
	 */
	@Override
	public boolean		isLogging() throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().isLogging() ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#isTracing()
	 */
	@Override
	public boolean		isTracing() throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().isTracing() ;
						}
					}) ;
	}

	// ------------------------------------------------------------------------
	// Internal behaviour requests
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#isInStateAmong(fr.sorbonne_u.components.ComponentStateI[])
	 */
	@Override
	public boolean		isInStateAmong(final ComponentStateI[] states)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().isInStateAmong(states) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#notInStateAmong(fr.sorbonne_u.components.ComponentStateI[])
	 */
	@Override
	public boolean		notInStateAmong(final ComponentStateI[] states)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().notInStateAmong(states) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#hasItsOwnThreads()
	 */
	@Override
	public boolean		hasItsOwnThreads() throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().hasItsOwnThreads() ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#hasSerialisedExecution()
	 */
	@Override
	public boolean		hasSerialisedExecution() throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().hasSerialisedExecution() ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#canScheduleTasks()
	 */
	@Override
	public boolean		canScheduleTasks() throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().canScheduleTasks() ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getTotalNUmberOfThreads()
	 */
	@Override
	public int			getTotalNUmberOfThreads() throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Integer>() {
						@Override
						public Integer call() throws Exception {
							return this.getOwner().getTotalNUmberOfThreads() ;
						}
					}) ;
	}

	// ------------------------------------------------------------------------
	// Implemented interfaces management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getInterfaces()
	 */
	@Override
	public Class<?>[]	getInterfaces() throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Class<?>[]>() {
						@Override
						public Class<?>[] call() throws Exception {
							return this.getOwner().getInterfaces() ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getInterface(final Class<?> inter) throws Exception
	{
		return this.getOwner().handleRequestSync(
				new AbstractComponent.AbstractService<Class<?>>() {
					@Override
					public Class<?> call() throws Exception {
						return this.getOwner().getInterface(inter) ;
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getRequiredInterfaces()
	 */
	@Override
	public Class<?>[]	getRequiredInterfaces() throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Class<?>[]>() {
						@Override
						public Class<?>[] call() throws Exception {
							return this.getOwner().getRequiredInterfaces() ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getRequiredInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getRequiredInterface(final Class<?> inter)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
				new AbstractComponent.AbstractService<Class<?>>() {
					@Override
					public Class<?> call() throws Exception {
						return this.getOwner().getRequiredInterface(inter) ;
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getOfferedInterfaces()
	 */
	@Override
	public Class<?>[]	getOfferedInterfaces() throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Class<?>[]>() {
						@Override
						public Class<?>[] call() throws Exception {
							return this.getOwner().getOfferedInterfaces() ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getOfferedInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getOfferedInterface(final Class<?> inter)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
				new AbstractComponent.AbstractService<Class<?>>() {
					@Override
					public Class<?> call() throws Exception {
						return this.getOwner().getOfferedInterface(inter) ;
					}
				}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#isInterface(java.lang.Class)
	 */
	@Override
	public boolean		isInterface(final Class<?> inter) throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().isInterface(inter) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#isRequiredInterface(java.lang.Class)
	 */
	@Override
	public boolean		isRequiredInterface(final Class<?> inter)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().isRequiredInterface(inter) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#isOfferedInterface(java.lang.Class)
	 */
	@Override
	public boolean		isOfferedInterface(final Class<?> inter) throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().isOfferedInterface(inter) ;
						}
					}) ;
	}

	// ------------------------------------------------------------------------
	// Port management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#findPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findPortURIsFromInterface(final Class<?> inter)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<String[]>() {
						@Override
						public String[] call() throws Exception {
							return this.getOwner().findPortURIsFromInterface(inter) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#findInboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findInboundPortURIsFromInterface(final Class<?> inter)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<String[]>() {
						@Override
						public String[] call() throws Exception {
							return this.getOwner().findInboundPortURIsFromInterface(inter) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#findOutboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findOutboundPortURIsFromInterface(final Class<?> inter)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<String[]>() {
						@Override
						public String[] call() throws Exception {
							return this.getOwner().findOutboundPortURIsFromInterface(inter) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getPortImplementedInterface(java.lang.String)
	 */
	@Override
	public Class<?>		getPortImplementedInterface(final String portURI)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Class<?>>() {
						@Override
						public Class<?> call() throws Exception {
							return this.getOwner().getPortImplementedInterface(portURI) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#isPortExisting(java.lang.String)
	 */
	@Override
	public boolean			isPortExisting(String portURI) throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().isPortExisting(portURI) ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#isPortConnected(java.lang.String)
	 */
	@Override
	public boolean		isPortConnected(final String portURI)
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return this.getOwner().isPortConnected(portURI) ;
						}
					}) ;
	}

	// ------------------------------------------------------------------------
	// Reflection facility
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getComponentDefinitionClassName()
	 */
	@Override
	public String		getComponentDefinitionClassName() throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<String>() {
						@Override
						public String call() throws Exception {
							return this.getOwner().getComponentDefinitionClassName() ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getComponentAnnotations()
	 */
	@Override
	public Annotation[]	getComponentAnnotations() throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<Annotation[]>() {
						@Override
						public Annotation[] call() throws Exception {
							return this.getOwner().getComponentAnnotations() ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getComponentLoader()
	 */
	@Override
	public ClassLoader	getComponentLoader() throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<ClassLoader>() {
						@Override
						public ClassLoader call() throws Exception {
							return this.getOwner().getComponentLoader() ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getComponentServiceSignatures()
	 */
	@Override
	public ServiceSignature[]	getComponentServiceSignatures()
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<ServiceSignature[]>() {
						@Override
						public ServiceSignature[] call() throws Exception {
							return this.getOwner().getComponentServiceSignatures() ;
						}
					}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getComponentConstructorSignatures()
	 */
	@Override
	public ConstructorSignature[]	getComponentConstructorSignatures()
	throws Exception
	{
		return this.getOwner().handleRequestSync(
					new AbstractComponent.AbstractService<ConstructorSignature[]>() {
						@Override
						public ConstructorSignature[] call() throws Exception {
							return this.getOwner().getComponentConstructorSignatures() ;
						}
					}) ;
	}
}
//-----------------------------------------------------------------------------
