package fr.sorbonne_u.components.reflection.interfaces;

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

import fr.sorbonne_u.components.ComponentStateI;
import fr.sorbonne_u.components.PluginI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.reflection.utils.ConstructorSignature;
import fr.sorbonne_u.components.reflection.utils.ServiceSignature;
import java.lang.annotation.Annotation;
import fr.sorbonne_u.components.interfaces.ComponentInterface;

// -----------------------------------------------------------------------------
/**
 * The interface <code>IntrospectionCI</code> defines the introspection
 * services offered by the Basic Component Model.
 *
 * <p><strong>Description</strong></p>
 * 
 * The Basic Component Model reflection facility is still under construction.
 * 
 * The interface <code>IntrospectionCI</code> mirrors to a large extent
 * the interface <code>ComponentI</code> implemented by the abstract
 * class <code>AbstractComponent</code> and exposes the methods that
 * allow to consult the internal representation of the component.
 * 
 * <p>Created on : 2016-02-25</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		IntrospectionCI
extends		OfferedCI,
			RequiredCI
{
	// -------------------------------------------------------------------------
	// Plug-ins facilities
	// -------------------------------------------------------------------------

	/**
	 * return true if the component has some installed plug-ins.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if the component has some installed plug-ins.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		hasInstalledPlugins() throws Exception;

	/**
	 * test if a plug-in is installed into this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code pluginURI != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param pluginURI		unique plug-in identifier.
	 * @return 				true if the named plug-in is installed into this component.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isInstalled(String pluginURI) throws Exception;

	/**
	 * access a named plug-in into this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code pluginURI != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param pluginURI		unique plug-in identifier.
	 * @return				the corresponding installed plug-in or null if none.
	 * @throws Exception	<i>to do</i>.
	 */
	public PluginI		getPlugin(String pluginURI) throws Exception;

	/**
	 * return true if the plug-in with the passed URI is initialised.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code pluginURI != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param pluginURI		URI of the plug-in to be tested.
	 * @return				true if the plug-in is installed and initialised.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isInitialised(String pluginURI) throws Exception;

	// -------------------------------------------------------------------------
	// Logging facilities
	// -------------------------------------------------------------------------

	/**
	 * return true if the logging is currently active, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if the logging is currently active, false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isLogging() throws Exception;

	/**
	 * return true if the tracing is currently active, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if the tracing is currently active, false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isTracing() throws Exception;

	// -------------------------------------------------------------------------
	// Internal behaviour requests
	// -------------------------------------------------------------------------

	/**
	 * return true if the component is in one of the mentioned component states.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code states != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param states		states in which the components is tested to be.
	 * @return				true if the component is in one of the given states.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isInStateAmong(ComponentStateI[] states)
	throws Exception;

	/**
	 * return true if the component is in none of the mentioned component states.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code states != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param states		states in which the components is tested not to be.
	 * @return				true if the component is in none of the given states.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		notInStateAmong(ComponentStateI[] states)
	throws Exception;

	/**
	 * true if the component executes concurrently with its own thread pool.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if the component executes concurrently with its own threads.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		hasItsOwnThreads() throws Exception;

	/**
	 * return the number of threads, schedulable or not, in the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				the number of threads, schedulable or not, in the component.
	 * @throws Exception	<i>to do</i>.
	 */
	public int			getTotalNumberOfThreads() throws Exception;

	/**
	 * return true if the component guarantees a serialised execution of its services.
	 * 
	 * <p>
	 * In the baseline definition, this returns true if the component has its own
	 * threads and if the total number of threads is exactly one. A component can
	 * guarantee its serialised execution in other ways and then should redefine
	 * the method for example, by making all externally callable services
	 * implemented by synchronised methods.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if the component guarantees a serialised execution of its services.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		hasSerialisedExecution() throws Exception;

	/**
	 * true if the component executes concurrently with its own thread pool and
	 * can schedule tasks running after a specific delay or periodically.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if the component can schedule tasks running after a specific delay or periodically.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		canScheduleTasks() throws Exception;

	// -------------------------------------------------------------------------
	// Implemented interfaces management
	// -------------------------------------------------------------------------

	/**
	 * return all interfaces required or offered by this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				interfaces required and offered by the component.
	 * @throws Exception	<i>to do</i>.
	 */
	public Class<? extends ComponentInterface>[]	getInterfaces()
	throws Exception;

	/**
	 * return the interface, required or offered by this component, which
	 * corresponds to the given interface; the result may be the same interface
	 * of a sub-interface this "covering" the given one.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code inter != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param inter			the interface to be checked for.
	 * @return				the corresponding component interface or null if any.
	 * @throws Exception	<i>to do</i>.
	 */
	public Class<? extends ComponentInterface>		getInterface(
		Class<? extends ComponentInterface> inter
		) throws Exception;

	/**
	 * return all the required interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				required interfaces of this component.
	 * @throws Exception	<i>to do</i>.
	 */
	public Class<? extends RequiredCI>[]	getRequiredInterfaces()
	throws Exception;

	/**
	 * return the interface required by this component, which corresponds to
	 * the given interface; the result may be the same interface of a
	 * sub-interface this "covering" the given one.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code inter != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param inter			the interface to be checked for.
	 * @return				the corresponding component interface or null if any.
	 * @throws Exception	<i>to do</i>.
	 */
	public Class<? extends RequiredCI>	getRequiredInterface(
		Class<? extends RequiredCI> inter
		) throws Exception;

	/**
	 * return all the offered interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				offered interfaces of this component.
	 * @throws Exception	<i>to do</i>.
	 */
	public Class<? extends OfferedCI>[]	getOfferedInterfaces()
	throws Exception;

	/**
	 * return the interface offered by this component, which corresponds to
	 * the given interface; the result may be the same interface of a
	 * sub-interface "covering" the given one.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code inter != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param inter			the interface to be checked for.
	 * @return				the corresponding component interface or null if any.
	 * @throws Exception	<i>to do</i>.
	 */
	public Class<? extends OfferedCI>	getOfferedInterface(
		Class<? extends OfferedCI> inter
		) throws Exception;

	/**
	 * check if an interface is one of this component or a super-interface of
	 * one of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param inter			interface to be checked for.
	 * @return				true if inter is an interface of this component.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isInterface(Class<? extends ComponentInterface> inter)
	throws Exception;

	/**
	 * check if an interface is a required one of this component or a
	 * super-interface of a required one of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param inter			interface to be checked for.
	 * @return				true if inter is a required interface of this component.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isRequiredInterface(Class<? extends RequiredCI> inter)
	throws Exception;

	/**
	 * check if an interface is an offered one of this component or a
	 * super-interface of an offered one of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param inter			interface to be checked for.
	 * @return				true if inter is an offered interface of this component.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isOfferedInterface(Class<? extends OfferedCI> inter)
	throws Exception;

	// -------------------------------------------------------------------------
	// Port management
	// -------------------------------------------------------------------------

	/**
	 * find the port URIs of this component that expose the interface inter.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code inter != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param inter			interface for which ports are sought.
	 * @return				array of port URIs exposing <code>inter</code>.
	 * @throws Exception	<i>to do</i>.
	 */
	public String[]		findPortURIsFromInterface(
		Class<? extends ComponentInterface> inter
		) throws Exception;

	/**
	 * find the inbound port URIs of this component that expose the interface
	 * inter.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code inter != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param inter			interface for which ports are sought.
	 * @return				array of inbound port URIs exposing inter.
	 * @throws Exception	<i>to do</i>.
	 */
	public String[]		findInboundPortURIsFromInterface(
		Class<? extends OfferedCI> inter
		) throws Exception;

	/**
	 * find the outbound port URIs of this component that expose the interface inter.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code inter != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param inter			interface for which ports are sought.
	 * @return				array of outbound port URIs exposing inter.
	 * @throws Exception	<i>to do</i>.
	 */
	public String[]		findOutboundPortURIsFromInterface(
		Class<? extends RequiredCI> inter
		) throws Exception;

	/**
	 * true if the port with the given URI exists in this component, false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code portURI != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param portURI		port URI to be tested.
	 * @return				true if the port with the given URI exists.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isPortExisting(String portURI) throws Exception;

	/**
	 * return the interface implemented by the port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code portURI != null && isPortExisting(portURI)}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param portURI		URI of the component's port.
	 * @return				the interface implemented by the port.
	 * @throws Exception	if such a port does not exist in the component.
	 */
	public Class<? extends ComponentInterface>	getPortImplementedInterface(
		String portURI
		) throws Exception;

	/**
	 * true if the port with the given URI is connected, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code portURI != null && isPortExisting(portURI)}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param portURI		URI of the component's port.
	 * @return				true if the port with the given URI is connected.
	 * @throws Exception	if such a port does not exist in the component.
	 */
	public boolean		isPortConnected(String portURI)
	throws Exception;

	// -------------------------------------------------------------------------
	// Reflection facility
	// -------------------------------------------------------------------------

	/**
	 * return the canonical name of the Java class implementing this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				the canonical name of the Java class implementing this component.
	 * @throws Exception	<i>to do</i>.
	 */
	public String		getComponentDefinitionClassName() throws Exception;

	/**
	 * return the annotations put on the Java class implementing the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				the annotations put on the Java class implementing the component.
	 * @throws Exception	<i>to do</i>.
	 */
	public Annotation[]	getComponentAnnotations() throws Exception;

	/**
	 * return the loader of the Java class implementing the component;
	 * meaningless if called from a different JVM.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				the loader of the Java class implementing the component.
	 * @throws Exception	<i>to do</i>.
	 */
	public ClassLoader	getComponentLoader() throws Exception;

	/**
	 * return the signatures of the component services.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				the signatures of the component services.
	 * @throws Exception	<i>to do</i>.
	 */
	public ServiceSignature[]		getComponentServiceSignatures()
	throws Exception;

	/**
	 * return the signatures of the component constructors.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				the signatures of the component constructors.
	 * @throws Exception	<i>to do</i>.
	 */
	public ConstructorSignature[]	getComponentConstructorSignatures()
	throws Exception;
}
// -----------------------------------------------------------------------------