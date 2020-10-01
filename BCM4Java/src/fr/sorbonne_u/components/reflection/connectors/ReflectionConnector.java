package fr.sorbonne_u.components.reflection.connectors;

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

import java.lang.annotation.Annotation;
import fr.sorbonne_u.components.ComponentStateI;
import fr.sorbonne_u.components.PluginI;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.helpers.Logger;
import fr.sorbonne_u.components.helpers.TracerWindow;
import fr.sorbonne_u.components.interfaces.ComponentInterface;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.reflection.interfaces.IntercessionCI;
import fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;
import fr.sorbonne_u.components.reflection.utils.ConstructorSignature;
import fr.sorbonne_u.components.reflection.utils.ServiceSignature;

// -----------------------------------------------------------------------------
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
implements	ReflectionCI
{
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public				 ReflectionConnector()
	{
		super();
	}

	// -------------------------------------------------------------------------
	// Plug-ins facilities
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#installPlugin(fr.sorbonne_u.components.PluginI)
	 */
	@Override
	public void			installPlugin(PluginI plugin) throws Exception
	{
		((ReflectionCI)this.offering).installPlugin(plugin);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#hasInstalledPlugins()
	 */
	@Override
	public boolean		hasInstalledPlugins() throws Exception
	{
		return ((ReflectionCI)this.offering).hasInstalledPlugins();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#finalisePlugin(java.lang.String)
	 */
	@Override
	public void			finalisePlugin(String pluginURI) throws Exception
	{
		((ReflectionCI)this.offering).finalisePlugin(pluginURI);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#uninstallPlugin(java.lang.String)
	 */
	@Override
	public void			uninstallPlugin(String pluginId) throws Exception
	{
		((ReflectionCI)this.offering).uninstallPlugin(pluginId);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#isInstalled(java.lang.String)
	 */
	@Override
	public boolean		isInstalled(String pluginId) throws Exception
	{
		return ((ReflectionCI)this.offering).isInstalled(pluginId);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#getPlugin(java.lang.String)
	 */
	@Override
	public PluginI		getPlugin(String pluginURI) throws Exception
	{
		return ((ReflectionCI)this.offering).getPlugin(pluginURI);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#initialisePlugin(java.lang.String)
	 */
	@Override
	public void			initialisePlugin(String pluginURI) throws Exception
	{
		((ReflectionCI)this.offering).initialisePlugin(pluginURI);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#isInitialised(java.lang.String)
	 */
	@Override
	public boolean		isInitialised(String pluginURI) throws Exception
	{
		return ((ReflectionCI)this.offering).isInitialised(pluginURI);
	}

	// -------------------------------------------------------------------------
	// Logging facilities
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#toggleLogging()
	 */
	@Override
	public void			toggleLogging() throws Exception
	{
		((ReflectionCI)this.offering).toggleLogging();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#setLogger(fr.sorbonne_u.components.helpers.Logger)
	 */
	@Override
	public void			setLogger(Logger logger) throws Exception
	{
		((IntercessionCI)this.offering).setLogger(logger);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#toggleTracing()
	 */
	@Override
	public void			toggleTracing() throws Exception
	{
		((ReflectionCI)this.offering).toggleTracing();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#logMessage(java.lang.String)
	 */
	@Override
	public void			logMessage(String message) throws Exception
	{
		((ReflectionCI)this.offering).logMessage(message);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#isLogging()
	 */
	@Override
	public boolean		isLogging() throws Exception
	{
		return ((ReflectionCI)this.offering).isLogging();
	}
	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#setTracer(fr.sorbonne_u.components.helpers.TracerWindow)
	 */
	@Override
	public void			setTracer(TracerWindow tracer) throws Exception
	{
		((IntercessionCI)this.offering).setTracer(tracer);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#traceMessage(java.lang.String)
	 */
	@Override
	public void			traceMessage(String message) throws Exception
	{
		((IntercessionCI)this.offering).traceMessage(message);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#isTracing()
	 */
	@Override
	public boolean		isTracing() throws Exception
	{
		return ((ReflectionCI)this.offering).isTracing();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#printExecutionLog()
	 */
	@Override
	public void			printExecutionLog() throws Exception
	{
		((ReflectionCI)this.offering).printExecutionLog();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#printExecutionLogOnFile(java.lang.String)
	 */
	@Override
	public void			printExecutionLogOnFile(String fileName)
	throws Exception
	{
		((ReflectionCI)this.offering).printExecutionLogOnFile(fileName);
	}

	// -------------------------------------------------------------------------
	// Internal behaviour requests
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#isInStateAmong(fr.sorbonne_u.components.ComponentStateI[])
	 */
	@Override
	public boolean		isInStateAmong(ComponentStateI[] states)
	throws Exception
	{
		return ((ReflectionCI)this.offering).isInStateAmong(states);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#notInStateAmong(fr.sorbonne_u.components.ComponentStateI[])
	 */
	@Override
	public boolean		notInStateAmong(ComponentStateI[] states)
	throws Exception
	{
		return ((ReflectionCI)this.offering).notInStateAmong(states);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#hasItsOwnThreads()
	 */
	@Override
	public boolean		hasItsOwnThreads() throws Exception
	{
		return ((ReflectionCI)this.offering).hasItsOwnThreads();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#hasSerialisedExecution()
	 */
	@Override
	public boolean		hasSerialisedExecution() throws Exception
	{
		return ((ReflectionCI)this.offering).hasSerialisedExecution();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#canScheduleTasks()
	 */
	@Override
	public boolean		canScheduleTasks() throws Exception
	{
		return ((ReflectionCI)this.offering).canScheduleTasks();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getTotalNumberOfThreads()
	 */
	@Override
	public int			getTotalNumberOfThreads() throws Exception
	{
		return ((IntrospectionCI)this.offering).getTotalNumberOfThreads();
	}

	// -------------------------------------------------------------------------
	// Implemented interfaces management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#getInterfaces()
	 */
	@Override
	public Class<? extends ComponentInterface>[]	getInterfaces()
	throws Exception
	{
		return ((ReflectionCI)this.offering).getInterfaces();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#getInterface(java.lang.Class)
	 */
	@Override
	public Class<? extends ComponentInterface>	getInterface(
		Class<? extends ComponentInterface> inter
		) throws Exception
	{
		return ((ReflectionCI)this.offering).getInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#getRequiredInterfaces()
	 */
	@Override
	public Class<? extends RequiredCI>[]	getRequiredInterfaces()
	throws Exception
	{
		return ((ReflectionCI)this.offering).getRequiredInterfaces();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#getRequiredInterface(java.lang.Class)
	 */
	@Override
	public Class<? extends RequiredCI>	getRequiredInterface(
		Class<? extends RequiredCI> inter
		) throws Exception
	{
		return ((ReflectionCI)this.offering).getRequiredInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#getOfferedInterfaces()
	 */
	@Override
	public Class<? extends OfferedCI>[]	getOfferedInterfaces() throws Exception
	{
		return ((ReflectionCI)this.offering).getOfferedInterfaces();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#getOfferedInterface(java.lang.Class)
	 */
	@Override
	public Class<? extends OfferedCI>	getOfferedInterface(
		Class<? extends OfferedCI> inter
		) throws Exception
	{
		return ((ReflectionCI)this.offering).getOfferedInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#addRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			addRequiredInterface(Class<? extends RequiredCI> inter)
	throws Exception
	{
		((ReflectionCI)this.offering).addRequiredInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#removeRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			removeRequiredInterface(
		Class<? extends RequiredCI> inter
		) throws Exception
	{
		((ReflectionCI)this.offering).removeRequiredInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#addOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			addOfferedInterface(Class<? extends OfferedCI> inter)
	throws Exception
	{
		((ReflectionCI)this.offering).addOfferedInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#removeOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			removeOfferedInterface(Class<? extends OfferedCI> inter)
	throws Exception
	{
		((ReflectionCI)this.offering).removeOfferedInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#isInterface(java.lang.Class)
	 */
	@Override
	public boolean		isInterface(Class<? extends ComponentInterface> inter)
	throws Exception
	{
		return ((ReflectionCI)this.offering).isInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#isRequiredInterface(java.lang.Class)
	 */
	@Override
	public boolean		isRequiredInterface(Class<? extends RequiredCI> inter)
	throws Exception
	{
		return ((ReflectionCI)this.offering).isRequiredInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#isOfferedInterface(java.lang.Class)
	 */
	@Override
	public boolean		isOfferedInterface(Class<? extends OfferedCI> inter)
	throws Exception
	{
		return ((ReflectionCI)this.offering).isOfferedInterface(inter);
	}

	// -------------------------------------------------------------------------
	// Port management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#findPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findPortURIsFromInterface(
		Class<? extends ComponentInterface> inter
		) throws Exception
	{
		return ((ReflectionCI)this.offering).findPortURIsFromInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#findInboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findInboundPortURIsFromInterface(
		Class<? extends OfferedCI> inter
		) throws Exception
	{
		return ((ReflectionCI)this.offering).findInboundPortURIsFromInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#findOutboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findOutboundPortURIsFromInterface(
		Class<? extends RequiredCI> inter
		) throws Exception
	{
		return ((ReflectionCI)this.offering).findOutboundPortURIsFromInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#getPortImplementedInterface(java.lang.String)
	 */
	@Override
	public Class<? extends ComponentInterface>	getPortImplementedInterface(
		String portURI
		) throws Exception
	{
		return ((ReflectionCI)this.offering).
										getPortImplementedInterface(portURI);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#isPortExisting(java.lang.String)
	 */
	@Override
	public boolean			isPortExisting(String portURI) throws Exception
	{
		return ((IntrospectionCI)this.offering).isPortExisting(portURI);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#isPortConnected(java.lang.String)
	 */
	@Override
	public boolean		isPortConnected(String portURI)
	throws Exception
	{
		return ((ReflectionCI)this.offering).isPortConnected(portURI);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#doPortConnection(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void			doPortConnection(
		String portURI,
		String otherPortURI,
		String ccname
		) throws Exception
	{
		((ReflectionCI)this.offering).
							doPortConnection(portURI, otherPortURI, ccname);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.ReflectionCI#doPortDisconnection(java.lang.String)
	 */
	@Override
	public void			doPortDisconnection(String portURI)
	throws Exception
	{
		((ReflectionCI)this.offering).doPortDisconnection(portURI);
	}

	// -------------------------------------------------------------------------
	// Reflection facility
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getComponentDefinitionClassName()
	 */
	@Override
	public String		getComponentDefinitionClassName() throws Exception
	{
		return ((IntrospectionCI)this.offering).
										getComponentDefinitionClassName();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getComponentAnnotations()
	 */
	@Override
	public Annotation[]	getComponentAnnotations() throws Exception
	{
		return ((IntrospectionCI)this.offering).getComponentAnnotations();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getComponentLoader()
	 */
	@Override
	public ClassLoader	getComponentLoader() throws Exception
	{
		return ((IntrospectionCI)this.offering).getComponentLoader();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getComponentServiceSignatures()
	 */
	@Override
	public ServiceSignature[]	getComponentServiceSignatures()
	throws Exception
	{
		return ((IntrospectionCI)this.offering).getComponentServiceSignatures();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getComponentConstructorSignatures()
	 */
	@Override
	public ConstructorSignature[]	getComponentConstructorSignatures()
	throws Exception
	{
		return ((IntrospectionCI)this.offering).
									getComponentConstructorSignatures();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#invokeService(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object		invokeService(String name, Object[] params)
	throws Exception
	{
		return ((IntercessionCI)this.offering).invokeService(name, params);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#invokeServiceSync(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object		invokeServiceSync(String name, Object[] params)
	throws Exception
	{
		return ((IntercessionCI)this.offering).invokeServiceSync(name, params);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#invokeServiceAsync(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void			invokeServiceAsync(String name, Object[] params)
	throws Exception
	{
		((IntercessionCI)this.offering).invokeServiceAsync(name, params);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		((IntercessionCI)this.offering).execute();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#runTask(fr.sorbonne_u.components.reflection.interfaces.IntercessionCI.AbstractRemoteComponentTask)
	 */
	@Override
	public void			runTask(AbstractRemoteComponentTask t)
	throws Exception
	{
		((IntercessionCI)this.offering).runTask(t);
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
		((IntercessionCI)this.offering).runTask(executorServiceURI, t);
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
		((ReflectionCI)this.offering).
			insertBeforeService(
					methodName, parametersCanonicalClassNames, code);
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
		((ReflectionCI)this.offering).
			insertAfterService(
					methodName, parametersCanonicalClassNames, code);
	}
}
// -----------------------------------------------------------------------------
