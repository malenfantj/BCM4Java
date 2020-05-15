package fr.sorbonne_u.components.cvm;

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

import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.helpers.CVMDebugModesI;

//-----------------------------------------------------------------------------
/**
 * The interface <code>ComponentVirtualMachineI</code> defines the common
 * behaviours of component virtual machines for component-based applications.
 *
 * <p><strong>Description</strong></p>
 * 
 * A CVM is meant to create a set of components, to initialise and to
 * interconnect them before starting them to execute the application.
 * 
 * Applications can include concurrent components, which themselves rely on the
 * Java Executor framework to execute requests with their own pool of threads.
 * Hence, CVM also expose part of the <code>ExecutorService</code> interface
 * regarding the life cycle of components and therefore their own life cycle.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true		// TODO
 * </pre>
 * 
 * <p>Created on : 2011-11-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface			ComponentVirtualMachineI
{
	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * return the host name of this component virtual machine.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the host name of this component virtual machine.
	 */
	//public String		getHostName() ;

	/**
	 * instantiate, publish and interconnect the components.
	 *
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!this.deploymentDone()
	 * post	this.deploymentDone()
	 * </pre>
	 * 
	 * @throws Exception	<i>to do.</i>
	 */
	public void			deploy() throws Exception ;

	/**
	 * tests if a component is in the set of deployed components on the CVM.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param componentURI	URI of the component to be tested.
	 * @return				true if component is deployed, false otherwise.
	 */
	public boolean		isDeployedComponent(String componentURI) ;

	/**
	 * add a component to the set of deployed components on the CVM.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	component != null
	 * pre	!this.isDeployedComponent(componentURI)
	 * post	this.isDeployedComponent(componentURI)
	 * </pre>
	 *
	 * @param componentURI	URI of the component to be added.
	 * @param component		component to be added.
	 */
	public void			addDeployedComponent(
		String componentURI,
		ComponentI component) ;

	/**
	 * remove a component from the set of deployed components on the CVM.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	this.isDeployedComponent(componentURI)
	 * post	!this.isDeployedComponent(componentURI)
	 * </pre>
	 *
	 * @param componentURI	URI of the component to be removed.
	 */
	public void			removeDeployedComponent(String componentURI) ;

	/**
	 * check if the deployment is completed and start the execution of
	 * the deployed components.
	 * 
	 * <pre>
	 * pre	this.deploymentDone()
	 * post	this.allStarted()
	 * </pre>
	 * @throws Exception	<i>to do.</i>
	 */
	public void			start() throws Exception ;

	/**
	 * start the given component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	this.isDeployedComponent(componentURI)
	 * post	this.componentStarted(componentURI)
	 * </pre>
	 *
	 * @param componentURI	URI of the component to be started.
	 * @throws Exception	<i>to do.</i>
	 */
	public void			startComponent(String componentURI) throws Exception ;

	/**
	 * check if all of the deployed component have been started and perform
	 * the execute method on all of the deployed components, calling it as
	 * an asynchronous task.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.allStarted()
	 * post	true				// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do.</i>
	 */
	public void			execute() throws Exception ;

	/**
	 * make the execute method of the component run as a task.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	this.componentStarted(componentURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param componentURI	URI of the component to be executed.
	 * @throws Exception	<i>to do.</i>
	 */
	public void			executeComponent(String componentURI) throws Exception ;

	/**
	 * perform the execute method on all of the deployed components, calling it
	 * as a synchronous task.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.allStarted()
	 * post	this.allFinalised()
	 * </pre>
	 *
	 * @throws Exception	<i>to do.</i>
	 */
	public void			finalise() throws Exception ;

	/**
	 * finalise the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	this.componentStarted(componentURI)
	 * post	this.componentFinalised(componentURI)
	 * </pre>
	 *
	 * @param componentURI	URI of the component to be finalised.
	 * @throws Exception	<i>to do.</i>
	 */
	public void			finaliseComponent(String componentURI)
	throws Exception ;

	/**
	 * shut down the CVM, synchronising with the other JVM when distributed,
	 * i.e. all of the deployed components in the CVM are shut down; inspired
	 * from the Java Executor framework.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.allFinalised()
	 * post	this.isShutdown()	// The shutting down state has still to be
	 *                           // correctly implemented
	 * </pre>
	 * 
	 * @throws Exception	<i>to do.</i>
	 */
	public void			shutdown() throws Exception ;

	/**
	 * shutdown the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	this.componentFinalised(componentURI)
	 * post	this.componentShutdown(componentURI)
	 * </pre>
	 *
	 * @param componentURI	URI of the component to be shutdown.
	 * @throws Exception	<i>to do.</i>
	 */
	public void			shutdownComponent(String componentURI)
	throws Exception ;

	/**
	 * shut down the CVM now, synchronising with the other sites when
	 * distributed, i.e. all of the locally deployed components in the
	 * CVM are shut down; inspired from the Java Executor framework.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.allFinalised()
	 * post	this.isShutdown()
	 * </pre>
	 * @throws Exception	<i>to do.</i>
	 */
	public void			shutdownNow() throws Exception ;

	/**
	 * shutdown the component immediately.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	this.componentFinalised(componentURI)
	 * post	this.componentShutdown(componentURI)
	 * </pre>
	 *
	 * @param componentURI	URI of the component to be shutdown now.
	 * @throws Exception	<i>to do.</i>
	 */
	public void			shutdownNowComponent(String componentURI)
	throws Exception ;

	/**
	 * true if the initialisation has been done.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return				true if the initialisation has been done.
	 */
	public boolean		isInitialised() ;

	/**
	 * return true if all of the static components have been instantiated and
	 * their ports published when necessary.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return				true if all of the static components have been instantiated and their ports published when necessary.
	 */
	public boolean		isIntantiatedAndPublished() ;

	/**
	 * return true if all of the static components have their ports connected
	 * when necessary.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return				true if all of the static components have their ports connected when necessary.
	 */
	public boolean		isInterconnected() ;

	/**
	 * return true if the deployment has been done.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return				true if the deployment has been done.
	 */
	public boolean		deploymentDone() ;

	/**
	 * return true if the CVM has been started (i.e. all of the locally
	 * statically deployed components in the CVM).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return				true if the CVM has been started.
	 */
	public boolean		allStarted() ;

	/**
	 * return true if the component has been started.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	this.isDeployedComponent(componentURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param componentURI	URI of the component to be tested.
	 * @return				true if the component has been started.
	 */
	public boolean		isStartedComponent(String componentURI) ;

	/**
	 * return true if the CVM has been finalised (i.e. all of the locally
	 * deployed components in the CVM).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return				true if the CVM has been finalised.
	 */
	public boolean		allFinalised() ;

	/**
	 * return true if the CVM has been finalised (i.e. all of the locally
	 * deployed components in the CVM).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	this.isDeployedComponent(componentURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param componentURI	URI of the component to be tested.
	 * @return				true if the component has been finalised.
	 */
	public boolean		isFinalisedComponent(String componentURI) ;

	/**
	 * return true if the CVM has been shut down (i.e. all of the locally
	 * statically deployed components in the CVM).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the component has been shut down.
	 */
	public boolean		isShutdown() ;

	/**
	 * return true if the component has been shut down.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	this.isDeployedComponent(componentURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param componentURI	URI of the component to be tested.
	 * @return				true if the component has been shut down.
	 */
	public boolean		isShutdownComponent(String componentURI) ;

	/**
	 * return true if the CVM has terminated (i.e. all of the locally
	 * statically deployed components in the CVM).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the CVM has terminated.
	 */
	public boolean		isTerminated() ;

	/**
	 * return true if the component has terminated.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	this.isDeployedComponent(componentURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param componentURI	URI of the component to be tested.
	 * @return				true if the CVM has terminated.
	 */
	public boolean		isTerminatedComponent(String componentURI) ;

	/**
	 * start the complete standard life-cycle of the component virtual
	 * machine to execute the application.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	duration &gt; 0
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 *@param	 duration	duration of the execution before the cutoff in ms.
	 * @return			true if normal termination, false otherwise.
	 */
	public boolean		startStandardLifeCycle(long duration) ;

	/**
	 * wait for the termination of the CVM (i.e. all of the locally deployed
	 * components in the CVM); inspired from the Java Executor framework.
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
	throws	InterruptedException ;

	// ------------------------------------------------------------------------
	// Component management
	// ------------------------------------------------------------------------

	/**
	 * connect the given outbound port of the given component to the
	 * inbound port of another component using the given connector class
	 * to instantiate the connector.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	outboundPortURI != null
	 * pre	inboundPortURI != null
	 * pre	connectorClassname != null
	 * pre	this.isDeployedComponent(componentURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param componentURI			URI of the component reflection inbound port.
	 * @param outboundPortURI		URI of the outbound port of the component.
	 * @param inboundPortURI		URI of the inbound port of the other component.
	 * @param connectorClassname	name of the class instantiating the connector.
	 * @throws Exception 			<i>todo.</i>
	 */
	public void			doPortConnection(
		String componentURI,
		String outboundPortURI,
		String inboundPortURI,
		String connectorClassname
		) throws Exception ;

	/**
	 * disconnect the given outbound port of the given component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	outboundPortURI != null
	 * pre	this.isDeployedComponent(componentURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param componentURI		URI of the component reflection inbound port.
	 * @param outboundPortURI	URI of the outbound port of the component.
	 * @throws Exception 		<i>todo.</i>
	 */
	public void			doPortDisconnection(
		String componentURI,
		String outboundPortURI
		) throws Exception ;

	// ------------------------------------------------------------------------
	// Debugging
	// ------------------------------------------------------------------------

	/**
	 * return the string that will be used to tag log entries for this CVM.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	ret != null
	 * </pre>
	 *
	 * @return	the string that will be used to tag log entries for this CVM.
	 */
	public String		logPrefix() ;

	/**
	 * log an entry in the debugging trace.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param dm		the debug mode triggering this entry.
	 * @param message	the debugging message to be added to the entry.
	 */
	public void			logDebug(CVMDebugModesI dm, String message) ;

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	this.isDeployedComponent(componentURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param componentURI	URI of the component
	 */
	public void			toggleTracing(String componentURI) ;

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	componentURI != null
	 * pre	this.isDeployedComponent(componentURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param componentURI	URI of the component
	 */
	public void			toggleLogging(String componentURI) ;
}
//-----------------------------------------------------------------------------
