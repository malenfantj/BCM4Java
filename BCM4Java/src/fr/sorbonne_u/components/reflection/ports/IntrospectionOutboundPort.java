package fr.sorbonne_u.components.reflection.ports;

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
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ComponentStateI;
import fr.sorbonne_u.components.PluginI;
import fr.sorbonne_u.components.interfaces.ComponentInterface;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI;
import fr.sorbonne_u.components.reflection.utils.ConstructorSignature;
import fr.sorbonne_u.components.reflection.utils.ServiceSignature;

// -----------------------------------------------------------------------------
/**
 * The class <code>IntrospectionOutboundPort</code> defines the outbound port
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
public class			IntrospectionOutboundPort
extends		AbstractOutboundPort
implements	IntrospectionCI
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public				IntrospectionOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, IntrospectionCI.class, owner);
	}

	public				IntrospectionOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(IntrospectionCI.class, owner);
	}


	// -------------------------------------------------------------------------
	// Plug-ins facilities
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#hasInstalledPlugins()
	 */
	@Override
	public boolean		hasInstalledPlugins() throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).hasInstalledPlugins();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#isInstalled(java.lang.String)
	 */
	@Override
	public boolean		isInstalled(String pluginId) throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).isInstalled(pluginId);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getPlugin(java.lang.String)
	 */
	@Override
	public PluginI		getPlugin(String pluginURI) throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).getPlugin(pluginURI);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#isInitialised(java.lang.String)
	 */
	@Override
	public boolean		isInitialised(String pluginURI) throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).isInitialised(pluginURI);
	}

	// -------------------------------------------------------------------------
	// Logging facilities
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#isLogging()
	 */
	@Override
	public boolean		isLogging() throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).isLogging();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#isTracing()
	 */
	@Override
	public boolean		isTracing() throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).isTracing();
	}

	// -------------------------------------------------------------------------
	// Internal behaviour requests
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#isInStateAmong(fr.sorbonne_u.components.ComponentStateI[])
	 */
	@Override
	public boolean		isInStateAmong(ComponentStateI[] states)
	throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).isInStateAmong(states);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#notInStateAmong(fr.sorbonne_u.components.ComponentStateI[])
	 */
	@Override
	public boolean		notInStateAmong(ComponentStateI[] states)
	throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).notInStateAmong(states);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#hasItsOwnThreads()
	 */
	@Override
	public boolean		hasItsOwnThreads() throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).hasItsOwnThreads();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#hasSerialisedExecution()
	 */
	@Override
	public boolean		hasSerialisedExecution() throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).hasSerialisedExecution();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#canScheduleTasks()
	 */
	@Override
	public boolean		canScheduleTasks() throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).canScheduleTasks();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getTotalNumberOfThreads()
	 */
	@Override
	public int			getTotalNumberOfThreads() throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).getTotalNumberOfThreads();
	}

	// -------------------------------------------------------------------------
	// Implemented interfaces management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getInterfaces()
	 */
	@Override
	public Class<? extends ComponentInterface>[]	getInterfaces()
	throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).getInterfaces();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getInterface(java.lang.Class)
	 */
	@Override
	public Class<? extends ComponentInterface>	getInterface(
		Class<? extends ComponentInterface> inter
		) throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).getInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getRequiredInterfaces()
	 */
	@Override
	public Class<? extends RequiredCI>[]	getRequiredInterfaces()
	throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).getRequiredInterfaces();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getRequiredInterface(java.lang.Class)
	 */
	@Override
	public Class<? extends RequiredCI>	getRequiredInterface(
		Class<? extends RequiredCI> inter
		) throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).
											getRequiredInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getOfferedInterfaces()
	 */
	@Override
	public Class<? extends OfferedCI>[]	getOfferedInterfaces() throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).getOfferedInterfaces();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getOfferedInterface(java.lang.Class)
	 */
	@Override
	public Class<? extends OfferedCI>	getOfferedInterface(
		Class<? extends OfferedCI> inter
		) throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).getOfferedInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#isInterface(java.lang.Class)
	 */
	@Override
	public boolean		isInterface(Class<? extends ComponentInterface> inter)
	throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).isInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#isRequiredInterface(java.lang.Class)
	 */
	@Override
	public boolean		isRequiredInterface(Class<? extends RequiredCI> inter)
	throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).isRequiredInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#isOfferedInterface(java.lang.Class)
	 */
	@Override
	public boolean		isOfferedInterface(Class<? extends OfferedCI> inter)
	throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).isOfferedInterface(inter);
	}

	// -------------------------------------------------------------------------
	// Port management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#findPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findPortURIsFromInterface(
		Class<? extends ComponentInterface> inter
		) throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).
									findPortURIsFromInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#findInboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findInboundPortURIsFromInterface(
		Class<? extends OfferedCI> inter
		) throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).
									findInboundPortURIsFromInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#findOutboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findOutboundPortURIsFromInterface(
		Class<? extends RequiredCI> inter
		) throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).
									findOutboundPortURIsFromInterface(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getPortImplementedInterface(java.lang.String)
	 */
	@Override
	public Class<? extends ComponentInterface>	getPortImplementedInterface(
		String portURI
		) throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).
										getPortImplementedInterface(portURI);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#isPortExisting(java.lang.String)
	 */
	@Override
	public boolean		isPortExisting(String portURI) throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).isPortExisting(portURI);
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#isPortConnected(java.lang.String)
	 */
	@Override
	public boolean		isPortConnected(String portURI)
	throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).isPortConnected(portURI);
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
		return ((IntrospectionCI)this.getConnector()).
									getComponentDefinitionClassName();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getComponentAnnotations()
	 */
	@Override
	public Annotation[]	getComponentAnnotations() throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).getComponentAnnotations();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getComponentLoader()
	 */
	@Override
	public ClassLoader	getComponentLoader() throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).getComponentLoader();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getComponentServiceSignatures()
	 */
	@Override
	public ServiceSignature[]	getComponentServiceSignatures()
	throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).
										getComponentServiceSignatures();
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntrospectionCI#getComponentConstructorSignatures()
	 */
	@Override
	public ConstructorSignature[]	getComponentConstructorSignatures()
	throws Exception
	{
		return ((IntrospectionCI)this.getConnector()).
										getComponentConstructorSignatures();
	}	
}
// -----------------------------------------------------------------------------
