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

import java.io.Serializable;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.PluginI;
import fr.sorbonne_u.components.ComponentI.ComponentTask;
import fr.sorbonne_u.components.helpers.Logger;
import fr.sorbonne_u.components.helpers.TracerWindow;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

// -----------------------------------------------------------------------------
/**
 * The interface <code>IntercessionCI</code> defines the intercession
 * services offered by the Basic Component Model.
 *
 * <p><strong>Description</strong></p>
 * 
 * The Basic Component Model reflection facility is still under construction.
 * 
 * The interface <code>IntercessionCI</code> mirrors to a large extent
 * the interface <code>ComponentI</code> implemented by the abstract
 * class <code>AbstractComponent</code> and exposes the methods that
 * allow to modify the internal representation of the component.
 * 
 * <p>Created on : 2016-02-25</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		IntercessionCI
extends		OfferedCI,
			RequiredCI
{
	// -------------------------------------------------------------------------
	// Plug-ins facilities
	// -------------------------------------------------------------------------

	/**
	 * install a plug-in into this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isInstalled(plugin.getPluginURI())}
	 * post	{@code isIntalled(plugin.getPluginURI())}
	 * </pre>
	 *
	 * @param plugin		plug-in implementation object.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			installPlugin(PluginI plugin) throws Exception;

	/**
	 * finalise the plug-in, at least when finalising the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code pluginURI != null && isIntalled(pluginURI)}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param pluginURI		unique plug-in identifier.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			finalisePlugin(String pluginURI) throws Exception;

	/**
	 * uninstall a plug-in from this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code pluginURI != null && isIntalled(pluginURI)}
	 * post	{@code !isIntalled(pluginURI)}
	 * </pre>
	 *
	 * @param pluginURI		unique plug-in identifier.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			uninstallPlugin(String pluginURI) throws Exception;

	/**
	 * initialise the identified plug-in by adding to the owner component every
	 * specific information, ports, etc. required to run the plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code pluginURI != null && !isInitialised(pluginURI)}
	 * post	{@code isInitialised(pluginURI)}
	 * </pre>
	 *
	 * @param pluginURI		unique plug-in identifier.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			initialisePlugin(String pluginURI) throws Exception;

	// -------------------------------------------------------------------------
	// Logging facilities
	// -------------------------------------------------------------------------

	/**
	 * set a logger for this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code logger != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param logger		the logger to be set.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setLogger(Logger logger) throws Exception;

	/**
	 * toggle the logging mode.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			toggleLogging() throws Exception;

	/**
	 * add a log message to the log buffer, tagging it with the current time
	 * on the computer (<code>System.currentTimeMillis()</code>); messages
	 * are also traced.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param message		string to be written on the log.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			logMessage(String message) throws Exception;

	/**
	 * print the execution log on the predefined file.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			printExecutionLog() throws Exception;

	/**
	 * print the execution log on the given file.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code fileName != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param fileName		the file to output the log.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			printExecutionLogOnFile(String fileName)
	throws	Exception;

	/**
	 * set a tracer for this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param tracer		the tracer to be added.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setTracer(TracerWindow tracer) throws	Exception;

	/**
	 * toggle the tracing mode.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			toggleTracing() throws Exception;

	/**
	 * add a trace message to the tracing console.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param message		trace message to be output.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			traceMessage(String message) throws	Exception;

	// -------------------------------------------------------------------------
	// Implemented interfaces management
	// -------------------------------------------------------------------------

	/**
	 * add a required interface to the required interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code !isRequiredInterface(inter)}
	 * post	{@code isRequiredInterface(inter)}
	 * </pre>
	 *
	 * @param inter			required interface to be added.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			addRequiredInterface(Class<? extends RequiredCI> inter)
	throws Exception;

	/**
	 * remove a required interface from the required interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code isRequiredInterface(inter)}
	 * pre	{@code findPortsFromInterface(inter) == null || findPortsFromInterface(inter).isEmpty()}
	 * post	{@code !isRequiredInterface(inter)}
	 * </pre>
	 *
	 * @param inter 		required interface to be removed.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			removeRequiredInterface(
		Class<? extends RequiredCI> inter
		) throws Exception;

	/**
	 * add an offered interface to the offered interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code !isOfferedInterface(inter)}
	 * post	{@code isOfferedInterface(inter)}
	 * </pre>
	 *
	 * @param inter 		offered interface to be added.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			addOfferedInterface(Class<? extends OfferedCI> inter)
	throws Exception;

	/**
	 * remove an offered interface from the offered interfaces of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code isOfferedInterface(inter)}
	 * pre	{@code findPortsFromInterface(inter) == null || findPortsFromInterface(inter).isEmpty()}
	 * post	{@code !isOfferedInterface(inter)}
	 * </pre>
	 *
	 * @param inter			offered interface to be removed
	 * @throws Exception	<i>to do</i>.
	 */
	public void			removeOfferedInterface(Class<? extends OfferedCI> inter)
	throws Exception;

	// -------------------------------------------------------------------------
	// Port management
	// -------------------------------------------------------------------------

	/**
	 * connect the component port to another component's port using the
	 * specified type of connector.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code portURI != null && otherPortURI != null && connector != null}
	 * pre	{@code isPortExisting(portURI)}
	 * pre	{@code !isPortConnected(portURI)}
	 * post	{@code isPortConnected(portURI)}
	 * </pre>
	 *
	 * @param portURI		URI of the component's port to be connected.
	 * @param otherPortURI	URI of the other port to be connected with.
	 * @param ccname		connector class name to be used in the connection.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			doPortConnection(
		String portURI,
		String otherPortURI,
		String ccname
		) throws Exception;

	/**
	 * connect the component port to another component's port using the
	 * specified type of connector.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})}
	 * pre	{@code portURI != null && otherPortURI != null && ccname != null}
	 * pre	{@code isPortExisting(portURI)}
	 * pre	{@code !isPortConnected(portURI)}
	 * post	{@code isPortConnected(portURI)}
	 * </pre>
	 *
	 * @param portURI		URI of the component's port to be connected.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			doPortDisconnection(
		String portURI
		) throws Exception;

	// -------------------------------------------------------------------------
	// Reflection facility
	// -------------------------------------------------------------------------

	/**
	 * invoke a component service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code name != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param name			name of the service.
	 * @param params		parameters to be passed to the service.
	 * @return				the result of the service invocation.
	 * @throws Exception	<i>to do</i>.
	 */
	public Object		invokeService(String name, Object[] params)
	throws Exception;

	/**
	 * invoke a component service synchronously.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code name != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param name			name of the service.
	 * @param params		parameters to be passed to the service.
	 * @return				the result of the service invocation.
	 * @throws Exception	<i>to do</i>.
	 */
	public Object		invokeServiceSync(String name, Object[] params)
	throws Exception;

	/**
	 * invoke a component service asynchronously.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code name != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param name			name of the service.
	 * @param params		parameters to be passed to the service.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			invokeServiceAsync(String name, Object[] params)
	throws Exception;

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
	 * pre	{@code isStarted()}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			execute() throws Exception;

	/**
	 * The class <code>AbstractRemoteComponentTask</code> allows to create
	 * tasks that can be passed as parameters in inter-components RMI calls,
	 * otherwise these tasks look like a <code>ComponentTask</code>.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	true
	 * </pre>
	 * 
	 * <p>Created on : 2020-02-13</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static abstract class	AbstractRemoteComponentTask
	implements		Serializable
	{
		private static final long serialVersionUID = 1L;
		transient protected ComponentTask	task;

		/**
		 * set the reference to the component task when ready to execute on
		 * the remote component.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code t != null}
		 * post	true		// no postcondition.
		 * </pre>
		 *
		 * @param t				the component task that will be submitted to the remote component.
		 * @throws Exception	<i>to do</i>.
		 */
		public void			setComponentTask(ComponentTask t) throws Exception
		{
			assert	t != null;
			this.task = t;
		}
		/**
		 * return the reference to the component owner of the executor service
		 * that will execute this task.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true		// no precondition.
		 * post	true		// no postcondition.
		 * </pre>
		 *
		 * @return	the reference to the component owner of the executor service that will execute this task.
		 * @throws Exception	<i>to do</i>.
		 */
		public ComponentI	getTaskOwner() throws Exception
		{
			return this.task.getTaskOwner();
		}

		/**
		 * return the reference to the component owner or its plug-in
		 * that will execute this task.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true		// no precondition.
		 * post	true		// no postcondition.
		 * </pre>
		 *
		 * @return	 the reference to the component owner or its plug-in that will execute this task.
		 * @throws Exception	<i>to do</i>.
		 */
		public Object		getTaskProviderReference() throws Exception
		{
			return this.task.getTaskProviderReference();
		}

		/**
		 * the method to be implemented by the user which represents the work
		 * the task will execute.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true		// no precondition.
		 * post	true		// no postcondition.
		 * </pre>
		 *
		 * @throws Exception	<i>to do</i>.
		 */
		public abstract void run() throws Exception;
	}

	/**
	 * run the <code>ComponentTask</code> as a task of the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isStarted()}
	 * pre	{@code t != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param t				component task to be executed as main task.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			runTask(AbstractRemoteComponentTask t)
	throws Exception;

	/**
	 * run the <code>ComponentTask</code> on the given executor service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isStarted()}
	 * pre	{@code t != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param executorServiceURI	URI of the executor service that will run the task.
	 * @param t						component task to be executed as main task.
	 * @throws Exception			<i>to do</i>.
	 */
	public void			runTask(
		String executorServiceURI,
		AbstractRemoteComponentTask t
		) throws Exception;

	/**
	 * insert a piece of code at the beginning of the specified component
	 * service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code serviceName != null}
	 * pre	{@code parametersCanonicalClassNames != null}
	 * pre	{@code code != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param methodName					name of the service method to be modified.
	 * @param parametersCanonicalClassNames	names of the types of the parameters of the method.
	 * @param code							code to be inserted.
	 * @throws Exception					<i>to do</i>.
	 */
	public void			insertBeforeService(
		String methodName,
		String[] parametersCanonicalClassNames,
		String code
		) throws Exception;

	/**
	 * insert a piece of code at the end of the specified component
	 * service.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code serviceName != null}
	 * pre	{@code parametersCanonicalClassNames != null}
	 * pre	{@code code != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param methodName					name of the service method to be modified.
	 * @param parametersCanonicalClassNames	names of the types of the parameters of the method.
	 * @param code							code to be inserted.
	 * @throws Exception					<i>to do</i>.
	 */
	public void			insertAfterService(
		String methodName,
		String[] parametersCanonicalClassNames,
		String code
		) throws Exception;
}
// -----------------------------------------------------------------------------
