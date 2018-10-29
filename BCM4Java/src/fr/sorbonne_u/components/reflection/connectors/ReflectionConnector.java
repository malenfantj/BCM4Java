package fr.sorbonne_u.components.reflection.connectors;

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
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ComponentStateI;
import fr.sorbonne_u.components.PluginI;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.helpers.Logger;
import fr.sorbonne_u.components.helpers.TracerOnConsole;
import fr.sorbonne_u.components.reflection.interfaces.IntercessionI;
import fr.sorbonne_u.components.reflection.interfaces.IntrospectionI;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionI;
import fr.sorbonne_u.components.reflection.utils.ConstructorSignature;
import fr.sorbonne_u.components.reflection.utils.ServiceSignature;

//-----------------------------------------------------------------------------
/**
 * The class <code>ReflectionConnector</code> defines the connector associated
 * the interface <code>ReflectionI</code>.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2016-02-25</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				ReflectionConnector
extends		AbstractConnector
implements	ReflectionI
{
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public				 ReflectionConnector()
	{
		super() ;
	}

	// ------------------------------------------------------------------------
	// Plug-ins facilities
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#installPlugin(fr.sorbonne_u.components.PluginI)
	 */
	@Override
	public void			installPlugin(PluginI plugin) throws Exception
	{
		((ReflectionI)this.offering).installPlugin(plugin) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#hasInstalledPlugins()
	 */
	@Override
	public boolean		hasInstalledPlugins() throws Exception
	{
		return ((ReflectionI)this.offering).hasInstalledPlugins() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#finalisePlugin(java.lang.String)
	 */
	@Override
	public void			finalisePlugin(String pluginURI) throws Exception
	{
		((ReflectionI)this.offering).finalisePlugin(pluginURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#uninstallPlugin(java.lang.String)
	 */
	@Override
	public void			uninstallPlugin(String pluginId) throws Exception
	{
		((ReflectionI)this.offering).uninstallPlugin(pluginId) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#isInstalled(java.lang.String)
	 */
	@Override
	public boolean		isInstalled(String pluginId) throws Exception
	{
		return ((ReflectionI)this.offering).isInstalled(pluginId) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#getPlugin(java.lang.String)
	 */
	@Override
	public PluginI		getPlugin(String pluginURI) throws Exception
	{
		return ((ReflectionI)this.offering).getPlugin(pluginURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#initialisePlugin(java.lang.String)
	 */
	@Override
	public void			initialisePlugin(String pluginURI) throws Exception
	{
		((ReflectionI)this.offering).initialisePlugin(pluginURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#isInitialised(java.lang.String)
	 */
	@Override
	public boolean		isInitialised(String pluginURI) throws Exception
	{
		return ((ReflectionI)this.offering).isInitialised(pluginURI) ;
	}

	// ------------------------------------------------------------------------
	// Logging facilities
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#toggleLogging()
	 */
	@Override
	public void			toggleLogging() throws Exception
	{
		((ReflectionI)this.offering).toggleLogging() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#setLogger(fr.sorbonne_u.components.helpers.Logger)
	 */
	@Override
	public void			setLogger(Logger logger) throws Exception
	{
		((IntercessionI)this.offering).setLogger(logger) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#toggleTracing()
	 */
	@Override
	public void			toggleTracing() throws Exception
	{
		((ReflectionI)this.offering).toggleTracing() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#logMessage(java.lang.String)
	 */
	@Override
	public void			logMessage(String message) throws Exception
	{
		((ReflectionI)this.offering).logMessage(message) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#isLogging()
	 */
	@Override
	public boolean		isLogging() throws Exception
	{
		return ((ReflectionI)this.offering).isLogging() ;
	}
	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#setTracer(fr.sorbonne_u.components.helpers.TracerOnConsole)
	 */
	@Override
	public void			setTracer(TracerOnConsole tracer) throws Exception
	{
		((IntercessionI)this.offering).setTracer(tracer) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#traceMessage(java.lang.String)
	 */
	@Override
	public void			traceMessage(String message) throws Exception
	{
		((IntercessionI)this.offering).traceMessage(message) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#isTracing()
	 */
	@Override
	public boolean		isTracing() throws Exception
	{
		return ((ReflectionI)this.offering).isTracing() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#printExecutionLog()
	 */
	@Override
	public void			printExecutionLog() throws Exception
	{
		((ReflectionI)this.offering).printExecutionLog() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#printExecutionLogOnFile(java.lang.String)
	 */
	@Override
	public void			printExecutionLogOnFile(String fileName)
	throws Exception
	{
		((ReflectionI)this.offering).printExecutionLogOnFile(fileName) ;
	}

	// ------------------------------------------------------------------------
	// Internal behaviour requests
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#isInStateAmong(fr.sorbonne_u.components.ComponentStateI[])
	 */
	@Override
	public boolean		isInStateAmong(ComponentStateI[] states)
	throws Exception
	{
		return ((ReflectionI)this.offering).isInStateAmong(states) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#notInStateAmong(fr.sorbonne_u.components.ComponentStateI[])
	 */
	@Override
	public boolean		notInStateAmong(ComponentStateI[] states)
	throws Exception
	{
		return ((ReflectionI)this.offering).notInStateAmong(states) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#hasItsOwnThreads()
	 */
	@Override
	public boolean		hasItsOwnThreads() throws Exception
	{
		return ((ReflectionI)this.offering).hasItsOwnThreads() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#hasSerialisedExecution()
	 */
	@Override
	public boolean		hasSerialisedExecution() throws Exception
	{
		return ((ReflectionI)this.offering).hasSerialisedExecution() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#canScheduleTasks()
	 */
	@Override
	public boolean		canScheduleTasks() throws Exception
	{
		return ((ReflectionI)this.offering).canScheduleTasks() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getTotalNUmberOfThreads()
	 */
	@Override
	public int			getTotalNUmberOfThreads() throws Exception
	{
		return ((IntrospectionI)this.offering).getTotalNUmberOfThreads() ;
	}

	// ------------------------------------------------------------------------
	// Implemented interfaces management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#getInterfaces()
	 */
	@Override
	public Class<?>[]	getInterfaces() throws Exception
	{
		return ((ReflectionI)this.offering).getInterfaces() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#getInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getInterface(Class<?> inter) throws Exception
	{
		return ((ReflectionI)this.offering).getInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#getRequiredInterfaces()
	 */
	@Override
	public Class<?>[]	getRequiredInterfaces() throws Exception
	{
		return ((ReflectionI)this.offering).getRequiredInterfaces() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#getRequiredInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getRequiredInterface(Class<?> inter) throws Exception
	{
		return ((ReflectionI)this.offering).getRequiredInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#getOfferedInterfaces()
	 */
	@Override
	public Class<?>[]	getOfferedInterfaces() throws Exception
	{
		return ((ReflectionI)this.offering).getOfferedInterfaces() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#getOfferedInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getOfferedInterface(Class<?> inter) throws Exception
	{
		return ((ReflectionI)this.offering).getOfferedInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#addRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			addRequiredInterface(Class<?> inter)
	throws Exception
	{
		((ReflectionI)this.offering).addRequiredInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#removeRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			removeRequiredInterface(Class<?> inter)
	throws Exception
	{
		((ReflectionI)this.offering).removeRequiredInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#addOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			addOfferedInterface(Class<?> inter) throws Exception
	{
		((ReflectionI)this.offering).addOfferedInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#removeOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			removeOfferedInterface(Class<?> inter)
	throws Exception
	{
		((ReflectionI)this.offering).removeOfferedInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#isInterface(java.lang.Class)
	 */
	@Override
	public boolean		isInterface(Class<?> inter) throws Exception
	{
		return ((ReflectionI)this.offering).isInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#isRequiredInterface(java.lang.Class)
	 */
	@Override
	public boolean		isRequiredInterface(Class<?> inter)
	throws Exception
	{
		return ((ReflectionI)this.offering).isRequiredInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#isOfferedInterface(java.lang.Class)
	 */
	@Override
	public boolean		isOfferedInterface(Class<?> inter) throws Exception
	{
		return ((ReflectionI)this.offering).isOfferedInterface(inter) ;
	}

	// ------------------------------------------------------------------------
	// Port management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#findPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findPortURIsFromInterface(Class<?> inter)
	throws Exception
	{
		return ((ReflectionI)this.offering).findPortURIsFromInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#findInboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findInboundPortURIsFromInterface(Class<?> inter)
	throws Exception
	{
		return ((ReflectionI)this.offering).findInboundPortURIsFromInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#findOutboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findOutboundPortURIsFromInterface(Class<?> inter)
	throws Exception
	{
		return ((ReflectionI)this.offering).findOutboundPortURIsFromInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#getPortImplementedInterface(java.lang.String)
	 */
	@Override
	public Class<?>		getPortImplementedInterface(String portURI)
	throws Exception
	{
		return ((ReflectionI)this.offering).
										getPortImplementedInterface(portURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#isPortExisting(java.lang.String)
	 */
	@Override
	public boolean			isPortExisting(String portURI) throws Exception
	{
		return ((IntrospectionI)this.offering).isPortExisting(portURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#isPortConnected(java.lang.String)
	 */
	@Override
	public boolean		isPortConnected(String portURI)
	throws Exception
	{
		return ((ReflectionI)this.offering).isPortConnected(portURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#doPortConnection(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void			doPortConnection(
		String portURI,
		String otherPortURI,
		String ccname
		) throws Exception
	{
		((ReflectionI)this.offering).
							doPortConnection(portURI, otherPortURI, ccname) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionI#doPortDisconnection(java.lang.String)
	 */
	@Override
	public void			doPortDisconnection(String portURI)
	throws Exception
	{
		((ReflectionI)this.offering).doPortDisconnection(portURI) ;
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
		return ((IntrospectionI)this.offering).
										getComponentDefinitionClassName() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getComponentAnnotations()
	 */
	@Override
	public Annotation[]	getComponentAnnotations() throws Exception
	{
		return ((IntrospectionI)this.offering).getComponentAnnotations() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getComponentLoader()
	 */
	@Override
	public ClassLoader	getComponentLoader() throws Exception
	{
		return ((IntrospectionI)this.offering).getComponentLoader() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getComponentServiceSignatures()
	 */
	@Override
	public ServiceSignature[]	getComponentServiceSignatures()
	throws Exception
	{
		return ((IntrospectionI)this.offering).getComponentServiceSignatures() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionI#getComponentConstructorSignatures()
	 */
	@Override
	public ConstructorSignature[]	getComponentConstructorSignatures()
	throws Exception
	{
		return ((IntrospectionI)this.offering).
									getComponentConstructorSignatures() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#newInstance(java.lang.Object[])
	 */
	@Override
	public ComponentI	newInstance(Object[] parameters) throws Exception
	{
		return ((IntercessionI)this.offering).newInstance(parameters) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#invokeService(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object		invokeService(String name, Object[] params)
	throws Exception
	{
		return ((IntercessionI)this.offering).invokeService(name, params) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#invokeServiceSync(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object		invokeServiceSync(String name, Object[] params)
	throws Exception
	{
		return ((IntercessionI)this.offering).invokeServiceSync(name, params) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionI#invokeServiceAsync(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void			invokeServiceAsync(String name, Object[] params)
	throws Exception
	{
		((IntercessionI)this.offering).invokeServiceAsync(name, params) ;
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
		((ReflectionI)this.offering).
			insertBeforeService(
					methodName, parametersCanonicalClassNames, code) ;
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
		((ReflectionI)this.offering).
			insertAfterService(
					methodName, parametersCanonicalClassNames, code) ;
	}
}
//-----------------------------------------------------------------------------
