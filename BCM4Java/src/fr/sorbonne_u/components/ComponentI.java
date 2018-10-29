package fr.sorbonne_u.components;

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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.connectors.ConnectorI;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.helpers.Logger;
import fr.sorbonne_u.components.helpers.TracerOnConsole;
import fr.sorbonne_u.components.reflection.utils.ConstructorSignature;
import fr.sorbonne_u.components.reflection.utils.ServiceSignature;

//-----------------------------------------------------------------------------
/**
 * The interface <code>ComponentI</code> serves as common supertype for all
 * classes that implements components in this component model.
 * 
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * Components in the model are represented by objects and therefore
 * instances of standard Java classes. HOwever, the component virtual
 * machine is defined both as virtual machines operations but also
 * as operations on components that this interface declares and that
 * the abstract class <code>AbstractComponent</code> implements.
 * </p>
 * <p>
 * Components offer and require interfaces, and they provide methods to
 * retrieve their required and offered interfaces represented as instances
 * of <code>Class</code>.  Components have ports that are used to connect
 * them together through their interfaces.  Outbound ports expose required
 * interfaces, while inbound ones expose the offered interfaces.  Components
 * can also be queried for the ports that expose some given interface.
 * </p>
 * <p>
 * The model include both sequential and concurrent components.  As the
 * concurrent ones rely on the Java Executor framework to handle requests
 * with a pool of threads, both sequential and concurrent share the same
 * protocol to execute requests: a <code>handleRequest</code> method capable
 * of executing a <code>Callable</code> task.  Tasks then call methods on the
 * object that implement the component; these methods represent the services
 * that the component offers.
 * </p>
 * <p>
 * A plug-in facility eases the definition of reusable component services
 * and behaviours. Operations to add and remove plug-ins are therefore
 * provided as well as methods used in the internal management of plug-ins
 * and their life-cycle between plug-in and component objects.
 * </p>
 * <p>
 * Logging and tracing facilities are also provided to ease the programming
 * and the debugging of component-based applications. Logging entries can
 * be made visible in logging windows and can be stored in logging files
 * for post-mortem analysis. Tracing entries are meant to be shown on
 * windows.
 * </p>
 *
 * <p>Created on : 2012-11-06</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface			ComponentI
{
	// ------------------------------------------------------------------------
	// Internal behaviour requests
	// ------------------------------------------------------------------------

	/**
	 * return true if the component is in one of the mentioned component states.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	states != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param states		states in which the components is tested to be.
	 * @return			true if the component is in one of the given states.
	 */
	public boolean		isInStateAmong(ComponentStateI[] states) ;

	/**
	 * return true if the component is in none of the mentioned component states.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	states != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param states		states in which the components is tested not to be.
	 * @return			true if the component is in none of the given states.
	 */
	public boolean		notInStateAmong(ComponentStateI[] states) ;

	/**
	 * true if the component executes concurrently with its own thread pool.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the component executes concurrently with its own threads.
	 */
	public boolean		hasItsOwnThreads() ;

	/**
	 * return the number of threads, schedulable or not, in the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the number of threads, schedulable or not, in the component.
	 */
	public int			getTotalNUmberOfThreads() ;

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
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the component guarantees a serialised execution of its services.
	 */
	public boolean		hasSerialisedExecution() ;

	/**
	 * true if the component executes concurrently with its own thread pool and
	 * can schedule tasks running after a specific delay or periodically.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the component can schedule tasks running after a specific delay or periodically.
	 */
	public boolean		canScheduleTasks() ;

	// ------------------------------------------------------------------------
	// Implemented interfaces
	// ------------------------------------------------------------------------

	/**
	 * return all interfaces required or offered by this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	interfaces required and offered by the component.
	 */
	public Class<?>[]	getInterfaces() ;

	/**
	 * return the interface, required or offered by this component, which
	 * corresponds to the given interface; the result may be the same interface
	 * of a sub-interface this "covering" the given one.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	inter != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param inter	the interface to be checked for.
	 * @return		the corresponding component interface or null if any.
	 */
	public Class<?>		getInterface(Class<?> inter) ;

	/**
	 * return all the required interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	required interfaces of this component.
	 */
	public Class<?>[]	getRequiredInterfaces() ;

	/**
	 * return the interface required by this component, which corresponds to
	 * the given interface; the result may be the same interface of a
	 * sub-interface this "covering" the given one.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	inter != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param inter	the interface to be checked for.
	 * @return		the corresponding component interface or null if any.
	 */
	public Class<?>		getRequiredInterface(Class<?> inter) ;

	/**
	 * return all the offered interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	offered interfaces of this component.
	 */
	public Class<?>[]	getOfferedInterfaces() ;

	/**
	 * return the interface offered by this component, which corresponds to
	 * the given interface; the result may be the same interface of a
	 * sub-interface "covering" the given one.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	inter != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param inter	the interface to be checked for.
	 * @return		the corresponding component interface or null if any.
	 */
	public Class<?>		getOfferedInterface(Class<?> inter) ;

	/**
	 * add a required interface to the required interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	RequiredI.class.isAssignableFrom(inter)
	 * pre	!this.isRequiredInterface(inter)
	 * post	this.isRequiredInterface(inter)
	 * </pre>
	 *
	 * @param inter	required interface to be added.
	 */
	public void			addRequiredInterface(Class<?> inter) ;

	/**
	 * remove a required interface from the required interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	this.isRequiredInterface(inter)
	 * pre	this.findPortsFromInterface(inter) == null || this.findPortsFromInterface(inter).isEmpty()
	 * post	!this.isRequiredInterface(inter)
	 * </pre>
	 *
	 * @param inter required interface to be removed.
	 */
	public void			removeRequiredInterface(Class<?> inter) ;

	/**
	 * add an offered interface to the offered interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	OfferedI.class.isAssignableFrom(inter)
	 * pre	!this.isOfferedInterface(inter)
	 * post	this.isOfferedInterface(inter)
	 * </pre>
	 *
	 * @param inter offered interface to be added.
	 */
	public void			addOfferedInterface(Class<?> inter) ;

	/**
	 * remove an offered interface from the offered interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	this.isOfferedInterface(inter)
	 * pre	this.findPortsFromInterface(inter) == null || this.findPortsFromInterface(inter).isEmpty()
	 * post	!this.isOfferedInterface(inter)
	 * </pre>
	 *
	 * @param inter	offered interface ot be removed
	 */
	public void			removeOfferedInterface(Class<?> inter) ;

	/**
	 * check if an interface is one of this component or a super-interface of
	 * one of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param inter	interface to be checked for.
	 * @return		true if inter is an interface of this component.
	 */
	public boolean		isInterface(Class<?> inter) ;

	/**
	 * check if an interface is a required one of this component or a
	 * super-interface of a required one of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param inter	interface to be checked for.
	 * @return		true if inter is a required interface of this component.
	 */
	public boolean		isRequiredInterface(Class<?> inter) ;

	/**
	 * check if an interface is an offered one of this component or a
	 * super-interface of an offered one of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param inter	interface to be checked for.
	 * @return		true if inter is an offered interface of this component.
	 */
	public boolean		isOfferedInterface(Class<?> inter) ;

	// ------------------------------------------------------------------------
	// Port management
	// ------------------------------------------------------------------------

	/**
	 * return the interface implemented by the port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	portURI != null and this.isPortExisting(portURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param portURI	URI of the component's port.
	 * @return			the interface implemented by the port.
	 * @throws Exception	if such a port does not exist in the component.
	 */
	public Class<?>		getPortImplementedInterface(String portURI)
	throws Exception ;

	/**
	 * find the port URIs of this component that expose the interface inter.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	inter != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param inter		interface for which ports are sought.
	 * @return			array of port URIs exposing <code>inter</code>.
	 * @throws Exception <i>todo.</i>
	 */
	public String[]		findPortURIsFromInterface(Class<?> inter)
	throws Exception ;

	/**
	 * find the inbound port URIs of this component that expose the interface
	 * inter.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	inter != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param inter		interface for which ports are sought.
	 * @return			array of inbound port URIs exposing inter.
	 * @throws Exception	<i>todo.</i>
	 */
	public String[]		findInboundPortURIsFromInterface(Class<?> inter)
	throws Exception ;

	/**
	 * find the outbound port URIs of this component that expose the interface inter.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	inter != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param inter		interface for which ports are sought.
	 * @return			array of outbound port URIs exposing inter.
	 * @throws Exception	<i>todo.</i>
	 */
	public String[]		findOutboundPortURIsFromInterface(Class<?> inter)
	throws Exception ;

	/**
	 * true if the port with the given URI exists in this component, false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	portURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param portURI	port URI to be tested.
	 * @return			true if the port with the given URI exists.
	 * @throws Exception	<i>todo.</i>
	 */
	public boolean		isPortExisting(String portURI)
	throws Exception ;

	/**
	 * true if the port with the given URI is connected, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	portURI != null and this.isPortExisting(portURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param portURI	URI of the component's port.
	 * @return			true if the port with the given URI is connected.
	 * @throws Exception	if such a port does not exist in the component.
	 */
	public boolean		isPortConnected(String portURI)
	throws Exception ;

	/**
	 * connect the component port to another component's port using the
	 * specified type of connector.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	portURI != null and otherPortURI != null and connector != null
	 * pre	this.isPortExisting(portURI)
	 * pre	!this.isPortConnected(portURI)
	 * post	this.isPortConnected(portURI)
	 * </pre>
	 *
	 * @param portURI		URI of the component's port to be connected.
	 * @param otherPortURI	URI of the other port to be connected with.
	 * @param connector		connector to be used in the connection.
	 * @throws Exception		<i>todo.</i>
	 */
	public void			doPortConnection(
		String portURI,
		String otherPortURI,
		ConnectorI connector
		) throws Exception ;

	/**
	 * connect the component port to another component's port using the
	 * specified type of connector.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	portURI != null and otherPortURI != null and ccname != null
	 * pre	this.isPortExisting(portURI)
	 * pre	!this.isPortConnected(portURI)
	 * post	this.isPortConnected(portURI)
	 * </pre>
	 *
	 * @param portURI		URI of the component's port to be connected.
	 * @param otherPortURI	URI of the other port to be connected with.
	 * @param ccname			connector class name to be used in the connection.
	 * @throws Exception		<i>todo.</i>
	 */
	public void			doPortConnection(
		String portURI,
		String otherPortURI,
		String ccname
		) throws Exception ;

	/**
	 * disconnect the component port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	portURI != null and this.isPortExisting(portURI)
	 * pre	this.isPortConnected(portURI)
	 * post	!this.isPortConnected(portURI)
	 * </pre>
	 *
	 * @param portURI	URI of the component's port to be connected.
	 * @throws Exception	<i>todo.</i>
	 */
	public void			doPortDisconnection(
		String portURI
		) throws Exception ;

	/**
	 * remove a port from the set of ports of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	portURI != null and this.isPortExisting(portURI)
	 * post	!this.isPortExisting(portURI)
	 * </pre>
	 *
	 * @param portURI	URI of the port to be removed.
	 * @throws Exception	if this port does not exist or os still connected.
	 */
	public void			removePort(String portURI) throws Exception ;

	// ------------------------------------------------------------------------
	// Plug-ins facilities
	// ------------------------------------------------------------------------

	/**
	 * install a plug-in into this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!this.isInstalled(plugin.getPluginURI())
	 * post	this.isIntalled(plugin.getPluginURI())
	 * </pre>
	 *
	 * @param plugin		plug-in implementation object.
	 * @throws Exception	<i>todo.</i>
	 */
	public void			installPlugin(
		PluginI plugin
		) throws Exception ;

	/**
	 * return true if the component has some installed plug-ins.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the component has some installed plug-ins.
	 */
	public boolean		hasInstalledPlugins() ;

	/**
	 * finalise the plug-in, at least when finalising the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginURI != null and this.isIntalled(pluginURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pluginURI	unique plug-in identifier.
	 * @throws Exception	<i>todo.</i>
	 */
	public void			finalisePlugin(String pluginURI) throws Exception ;

	/**
	 * uninstall a plug-in from this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginURI != null and this.isIntalled(pluginURI)
	 * post	!this.isIntalled(pluginURI)
	 * </pre>
	 *
	 * @param pluginURI	unique plug-in identifier.
	 * @throws Exception	<i>todo.</i>
	 */
	public void			uninstallPlugin(String pluginURI) throws Exception ;

	/**
	 * test if a plug-in is installed into this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pluginURI	unique plug-in identifier.
	 * @return 			true if the named plug-in is installed into this component.
	 * @throws Exception	<i>todo.</i>
	 */
	public boolean		isInstalled(String pluginURI) throws Exception ;

	/**
	 * access a named plug-in into this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginURI != null
	 * pre	
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pluginURI	unique plug-in identifier.
	 * @return			the corresponding installed plug-in or null if none.
	 * @throws Exception	<i>todo.</i>
	 */
	public PluginI		getPlugin(String pluginURI) throws Exception ;

	/**
	 * initialise the identified plug-in by adding to the owner component every
	 * specific information, ports, etc. required to run the plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginURI != null and !this.isInitialised(pluginURI)
	 * post	this.isInitialised(pluginURI)
	 * </pre>
	 *
	 * @param pluginURI	unique plug-in identifier.
	 * @throws Exception	<i>todo.</i>
	 */
	public void			initialisePlugin(String pluginURI) throws Exception ;

	/**
	 * return true if the plug-in with the passed URI is initialised.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pluginURI	URI of the plug-in to be tested.
	 * @return			true if the plug-in is installed and initialised.
	 * @throws Exception	<i>todo.</i>
	 */
	public boolean		isInitialised(String pluginURI) throws Exception ;

	// ------------------------------------------------------------------------
	// Logging facilities
	// ------------------------------------------------------------------------

	/**
	 * set a logger for this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	logger != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param logger		the logger to be set.
	 */
	public void			setLogger(Logger logger) ;

	/**
	 * return true if the logging is currently active, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the logging is currently active, false otherwise.
	 */
	public boolean		isLogging() ;

	/**
	 * toggle the logging mode.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public void			toggleLogging() ;

	/**
	 * add a log message to the log buffer, tagging it with the current time
	 * on the computer (<code>System.currentTimeMillis()</code>); messages
	 * are also traced.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param message	string to be written on the log.
	 */
	public void			logMessage(String message) ;

	/**
	 * print the execution log on the predefined file.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public void			printExecutionLog() ;

	/**
	 * print the execution log on the given file.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	fileName != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param fileName	the file to output the log.
	 */
	public void			printExecutionLogOnFile(String fileName) ;

	/**
	 * set a tracer for this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param tracer			the tracer to be added.
	 */
	public void			setTracer(TracerOnConsole tracer) ;

	/**
	 * toggle the tracing mode.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public void			toggleTracing() ;

	/**
	 * add a trace message to the tracing console.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param message	trace message to be output.
	 */
	public void			traceMessage(String message) ;

	/**
	 * return true if the tracing is currently active, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the tracing is currently active, false otherwise.
	 */
	public boolean		isTracing() ;

	// ------------------------------------------------------------------------
	// Component life cycle
	// ------------------------------------------------------------------------

	/**
	 * start the component; this method is automatically called by the component
	 * virtual machine after all components have been initialised and the
	 * component calls in turn its own inner components. Implementations of this
	 * method must always return the control.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isInitialised()
	 * post	this.isStarted()
	 * </pre>
	 *
	 * @throws ComponentStartException	<i>todo.</i>
	 */
	public void			start() throws ComponentStartException ;

	/**
	 * execute a task on the component after all components have been started;
	 * this method is automatically called by the component virtual machine
	 * after all components have been started and the component calls in turn
	 * its own inner components. Implementations of this method can keep the
	 * control as long as they wish (contrary to <code>start</code>) but they
	 * need to begin by a call to super if inner components also have an
	 * execute method to be called; it is the responsibility of programmers to
	 * make sure that this does not provoke deadlocks.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>todo.</i>
	 */
	public void			execute() throws Exception ;

	/**
	 * finalise the component, freeing resources that need to be, before
	 * shutting it down.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * post	this.isFinalised()
	 * </pre>
	 *
	 * @throws Exception	<i>todo.</i>
	 */
	public void			finalise() throws Exception ;

	/**
	 * shutdown the component; inspired from the Java Executor framework.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isFinalised()
	 * post	this.isShuttingDown() || this.isShutdown()
	 * </pre>
	 * 
	 * @throws ComponentShutdownException	<i>todo.</i>
	 */
	public void			shutdown() throws ComponentShutdownException ;

	/**
	 * shutdown the component now; inspired from the Java Executor framework.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isFinalised()
	 * post	this.isShutdown()
	 * </pre>
	 *
	 * @throws ComponentShutdownException	<i>todo.</i>
	 */
	public void			shutdownNow() throws ComponentShutdownException ;

	/**
	 * true if the component is in the initialised state.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the component is in the initialised state.
	 */
	public boolean		isInitialised() ;

	/**
	 * true if the component is in the start state.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the component is in the start state.
	 */
	public boolean		isStarted() ;

	/**
	 * true if the component is in the finalised state.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the component is in the finalised state.
	 */
	public boolean		isFinalised() ;

	/**
	 * true if the component is in the shutting down state.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the component is in the shutting down state.
	 */
	public boolean		isShuttingDown() ;

	/**
	 * true if the component is in the shut down state.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the component is in the shut down state.
	 */
	public boolean		isShutdown() ;

	/**
	 * true if the component is in the terminated state.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the component is in the terminated state.
	 */
	public boolean		isTerminated() ;

	/**
	 * wait for the termination of the component; inspired from the Java
	 * Executor framework.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param timeout	the maximum time to wait.
	 * @param unit		the time unit of the timeout argument.
	 * @return			<code>true</code> if this executor terminated and <code>false</code> if the timeout elapsed before termination.
	 * @throws InterruptedException	if interrupted while waiting.
	 */
	public boolean		awaitTermination(long timeout, TimeUnit unit)
	throws InterruptedException ;

	// ------------------------------------------------------------------------
	// Task execution
	// ------------------------------------------------------------------------

	/**
	 * The interface <code>ComponentTask</code> is meant to group under a
	 * same interface all of the tasks for this component.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * As the Java Executor framework use <code>Callable</code> and
	 * <code>Runnable</code>, this interface is meant to be used to implement
	 * the runnable tasks of this component.
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2012-06-12</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 * @version	$Name$ -- $Revision$ -- $Date$
	 */
	public interface			ComponentTask
	extends Runnable
	{
		/**
		 * sets a reference to the owner component that can be used in
		 * the code of the task to access its services.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	owner != null
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param owner		the component owner of the executor service that will execute this task.
		 */
		public void			setOwnerReference(ComponentI owner) ;

		/**
		 * return the reference to the component owner of the executor service
		 * that will execute this task.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @return	the reference to the component owner of the executor service that will execute this task.
		 */
		public ComponentI	getOwner() ;
	}

	/**
	 * run the component task as main task of the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	t != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param t	component task to be executed as main task.
	 * @return	a <code>Future</code> representing pending completion of the task.
	 */
	public Future<?>		runTask(ComponentTask t) ;

	/**
	 * schedule a task to be run after a given delay.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.canScheduleTasks()
	 * pre	t != null and delay &gt; 0 and u != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param t		task to be scheduled.
	 * @param delay	delay after which the task must be run.
	 * @param u		time unit in which the delay is expressed.
	 * @return		a <code>ScheduledFuture</code> representing pending completion of the task, and whose <code>get()</code> method will throw an exception upon cancellation.
	 */
	public ScheduledFuture<?>	scheduleTask(
		ComponentTask t,
		long delay, 
		TimeUnit u) ;

	/**
	 * schedule a task that becomes enabled first after the given initial delay,
	 * and subsequently with the given period; that is executions will commence
	 * after <code>initialDelay</code> then <code>initialDelay+period</code>,
	 * then <code>initialDelay + 2 * period</code>, and so on. If any execution
	 * of the task encounters an exception, subsequent executions are suppressed.
	 * Otherwise, the task will only terminate via cancellation or termination
	 * of the executor. If any execution of this task takes longer than its
	 * period, then subsequent executions may start late, but will not
	 * concurrently execute.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.canScheduleTasks()
	 * pre	t != null and initialDelay &gt;= 0 and period &gt; 0 and u != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param t				task to be scheduled.
	 * @param initialDelay	delay after which the task begins to run.
	 * @param period			period between successive executions.
	 * @param u				time unit in which the initial delay and the period are expressed.
	 * @return				a <code>ScheduledFuture</code> representing pending completion of the task, and whose <code>get()</code> method will throw an exception upon cancellation.
	 */
	public ScheduledFuture<?>	scheduleTaskAtFixedRate(
		ComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u) ;

	/**
	 * schedule a task that becomes enabled first after the given initial delay,
	 * and subsequently with the given delay between the termination of one
	 * execution and the commencement of the next. If any execution of the task
	 * encounters an exception, subsequent executions are suppressed. Otherwise,
	 * the task will only terminate via cancellation or termination of the
	 * executor.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.canScheduleTasks()
	 * pre	t != null and initialDelay &gt;= 0 and delay &gt;= 0 and u != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param t				task to be scheduled.
	 * @param initialDelay	delay after which the task begins to run.
	 * @param delay			delay between the termination of one execution and the beginning of the next.
	 * @param u				time unit in which the initial delay and the delay are expressed.
	 * @return				a <code>ScheduledFuture</code> representing pending completion of the task, and whose <code>get()</code> method will throw an exception upon cancellation.
	 */
	public ScheduledFuture<?>	scheduleTaskWithFixedDelay(
		ComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u) ;

	// ------------------------------------------------------------------------
	// Request handling
	// ------------------------------------------------------------------------

	/**
	 * The interface <code>ComponentService</code> is meant to group under a
	 * same interface all of the requests that return results for this
	 * component.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * As the Java Executor framework use <code>Callable</code> and
	 * <code>Runnable</code>, this interface is meant to be used to implement
	 * the callable tasks representing requests to this component services.
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2012-06-12</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 * @version	$Name$ -- $Revision$ -- $Date$
	 */
	public interface			ComponentService<V>
	extends Callable<V>
	{
		/**
		 * sets a reference to the owner component that can be used in
		 * the code of the request to access its services.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	owner != null
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param owner		the component owner of the executor service that will execute this task.
		 */
		public void			setOwnerReference(ComponentI owner) ;

		/**
		 * return the reference to the component owner of the executor service
		 * that will execute this service call.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @return	the reference to the component owner of the executor service that will execute this service call.
		 */
		public ComponentI	getOwner() ;

		/**
		 * return the reference to the component owner or its plugin
		 * that will execute this request.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @return	the reference to the component owner of the executor service that will execute this task.
		 */
		public Object		getServiceProviderReference() ;
	}

	/**
	 * execute a request represented by a <code>Callable</code> on the
	 * component, but synchronously, i.e. waiting for the result and returning
	 * it to the caller.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	task != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param request	service request to be executed on the component.
	 * @return			the result of the task.
	 * @throws Exception	if exception raised by the task.
	 */
	public <T> T		handleRequestSync(ComponentService<T> request)
	throws Exception ;

	/**
	 * execute a request represented by a <code>ComponentService</code> on
	 * the component, but asynchronously, i.e. without waiting for the result
	 * but returning the control immediately to the caller.
	 * 
	 * TODO: introduce distributed future variables to return a future value.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	task != null 
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param request	service request to be executed on the component.
	 * @throws Exception	if exception raised by the task.
	 */
	public <T> void		handleRequestAsync(ComponentService<T> request)
	throws Exception ;

	/**
	 * schedule a service for execution after a given delay, forcing the caller
	 * to wait for the result.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.canScheduleTasks()
	 * pre	s != null and delay &gt; 0 and u != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param request	service request to be scheduled.
	 * @param delay		delay after which the task must be run.
	 * @param u			time unit in which the delay is expressed.
	 * @return			a scheduled future to synchronise with the task.
	 * @throws ExecutionException	<i>todo.</i>
	 * @throws InterruptedException	<i>todo.</i>
	 */
	public <T> T		scheduleRequestSync(
		ComponentService<T> request,
		long delay, 
		TimeUnit u) throws InterruptedException, ExecutionException ;

	/**
	 * schedule a service for execution after a given delay, without making the
	 * caller wait of giving it a possibility to synchronise with the result.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isStarted()
	 * pre	this.canScheduleTasks()
	 * pre	s != null and delay &gt; 0 and u != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param request	service request to be scheduled.
	 * @param delay		delay after which the task must be run.
	 * @param u			time unit in which the delay is expressed.
	 */
	public void			scheduleRequestAsync(
		ComponentService<?> request,
		long delay, 
		TimeUnit u) ;

	// ------------------------------------------------------------------------
	// Reflection facility
	// ------------------------------------------------------------------------

	/**
	 * return the canonical name of the Java class implementing this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the canonical name of the Java class implementing this component.
	 * @throws Exception		<i>todo.</i>
	 */
	public String		getComponentDefinitionClassName() throws Exception ;

	/**
	 * return the annotations put on the Java class implementing the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the annotations put on the Java class implementing the component.
	 * @throws Exception		<i>todo.</i>
	 */
	public Annotation[]	getComponentAnnotations() throws Exception ;

	/**
	 * return the loader of the Java class implementing the component;
	 * meaningless if called from a different JVM.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the loader of the Java class implementing the component.
	 * @throws Exception		<i>todo.</i>
	 */
	public ClassLoader	getComponentLoader() throws Exception ;

	/**
	 * return the signatures of the component services.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the signatures of the component services.
	 * @throws Exception		<i>todo.</i>
	 */
	public ServiceSignature[]		getComponentServiceSignatures()
	throws Exception ;

	/**
	 * return the signatures of the component constructors.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the signatures of the component constructors.
	 * @throws Exception		<i>todo.</i>
	 */
	public ConstructorSignature[]	getComponentConstructorSignatures()
	throws Exception ;
	/**
	 * create a new instance of the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param parameters		parameters to be passed to the component constructor.
	 * @return				a Java refercne to the object implementing the component.
	 * @throws Exception		<i>todo.</i>
	 */
	public ComponentI	newInstance(Object[] parameters) throws Exception ;

	/**
	 * invoke a component service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	name != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param name		name of the service.
	 * @param params		parameters to be passed to the service.
	 * @return			the result of the service invocation.
	 * @throws Exception		<i>todo.</i>
	 */
	public Object		invokeService(String name, Object[] params)
	throws Exception ;

	/**
	 * invoke a component service synchronously.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	name != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param name		name of the service.
	 * @param params		parameters to be passed to the service.
	 * @return			the result of the service invocation.
	 * @throws Exception		<i>todo.</i>
	 */
	public Object		invokeServiceSync(String name, Object[] params)
	throws Exception ;

	/**
	 * invoke a component service asynchronously.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	name != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param name		name of the service.
	 * @param params		parameters to be passed to the service.
	 * @throws Exception		<i>todo.</i>
	 */
	public void			invokeServiceAsync(String name, Object[] params)
	throws Exception ;

	/**
	 * insert a piece of code at the beginning of the specified component
	 * service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	serviceName != null
	 * pre	parametersCanonicalClassNames != null
	 * pre	code != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param serviceName					name of the service method to be modified.
	 * @param parametersCanonicalClassNames	names of the types of the parameters of the method.
	 * @param code							code to be inserted.
	 * @throws Exception						<i>todo.</i>
	 */
	public void			insertBeforeService(
		String serviceName,
		String[] parametersCanonicalClassNames,
		String code
		) throws Exception ;

	/**
	 * insert a piece of code at the end of the specified component
	 * service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	serviceName != null
	 * pre	parametersCanonicalClassNames != null
	 * pre	code != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param serviceName					name of the service method to be modified.
	 * @param parametersCanonicalClassNames	names of the types of the parameters of the method.
	 * @param code							code to be inserted.
	 * @throws Exception						<i>todo.</i>
	 */
	public void			insertAfterService(
		String serviceName,
		String[] parametersCanonicalClassNames,
		String code
		) throws Exception ;
}
//-----------------------------------------------------------------------------
