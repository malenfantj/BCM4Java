package fr.sorbonne_u.components.pre.dcc;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.sorbonne_u.components.pre.dcc.ports.DynamicComponentCreationInboundPort;

//-----------------------------------------------------------------------------
/**
 * The class <code>DynamicComponentCreator</code> defines components that will
 * be automatically added in each of the sites of a distributed component
 * assembly to allow for the dynamic remote creation of components on the
 * virtual where the component is running.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2014-03-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered = {DynamicComponentCreationI.class})
public class			DynamicComponentCreator
extends		AbstractComponent
{
	protected DynamicComponentCreationInboundPort	p ;

	/**
	 * create the component, publish its offered interface and its inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	dynamicComponentCreationInboundPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param dynamicComponentCreationInboundPortURI	URI of the port offering the service
	 * @throws Exception <i>todo.</i>
	 */
	protected			DynamicComponentCreator(
		String dynamicComponentCreationInboundPortURI
		) throws Exception
	{
		super(1, 0) ;

		assert	dynamicComponentCreationInboundPortURI != null ;

		this.p = new DynamicComponentCreationInboundPort(
								dynamicComponentCreationInboundPortURI, this) ;
		if (AbstractCVM.isDistributed) {
			this.p.publishPort() ;
		} else {
			this.p.localPublishPort() ;
		}
	}

	//-------------------------------------------------------------------------
	// Component life-cycle
	//-------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
			this.p.unpublishPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

	//-------------------------------------------------------------------------
	// Component internal services
	//-------------------------------------------------------------------------

	/**
	 * create and start a component instantiated from the class of the given
	 * class name and initialised by the constructor which parameters are
	 * given.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	reflInboundPortURI != null and classname != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param classname			name of the class from which the component is created.
	 * @param constructorParams	parameters to be passed to the constructor.
	 * @return					the URI of the reflection inbound port of the new component.
	 * @throws Exception		if the creation did not succeed.
	 */
	public String		createOtherComponent(
		String classname,
		Object[] constructorParams
		) throws Exception
	{
		assert	classname != null && constructorParams != null ;

		String componentURI =
				AbstractComponent.createComponent(
									classname, constructorParams) ;
		return componentURI ;
	}

	/**
	 * start a previously created component on the CVM executing this method.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection inbound port of the created component.
	 * @throws Exception				<i>todo.</i>
	 */
	public void			startComponent(String reflectionInboundPortURI)
	throws Exception
	{
		assert	AbstractCVM.getCVM().isDeployedComponent(
													reflectionInboundPortURI) ;

		AbstractCVM.getCVM().startComponent(reflectionInboundPortURI) ;
	}
	
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
	public void			executeComponent(String componentURI) throws Exception
	{
		assert	AbstractCVM.getCVM().isStartedComponent(componentURI) ;

		AbstractCVM.getCVM().executeComponent(componentURI) ;
	}

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
	throws Exception 
	{
		assert	AbstractCVM.getCVM().isStartedComponent(componentURI) ;

		AbstractCVM.getCVM().finaliseComponent(componentURI) ;
	}

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
	throws Exception
	{
		assert	AbstractCVM.getCVM().isFinalisedComponent(componentURI) ;

		AbstractCVM.getCVM().shutdownComponent(componentURI) ;
	}

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
	throws Exception
	{
		assert	AbstractCVM.getCVM().isFinalisedComponent(componentURI) ;

		AbstractCVM.getCVM().shutdownNowComponent(componentURI) ;
	}

	/**
	 * return true if the component having the given reflection inbound port
	 * URI is deployed on the CVM executing this method.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	reflectionInboundPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection inbound port of the created component.
	 * @return							true if the corresponding component is deployed on the CVM executing this method.
	 */
	public boolean		isDeployedComponent(String reflectionInboundPortURI)
	{
		return AbstractCVM.getCVM().isDeployedComponent(
											reflectionInboundPortURI) ;
	}

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
	public boolean		isStartedComponent(String componentURI)
	{
		return AbstractCVM.getCVM().isStartedComponent(componentURI) ;
	}

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
	public boolean		isFinalisedComponent(String componentURI)
	{
		return AbstractCVM.getCVM().isFinalisedComponent(componentURI) ;
	}

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
	public boolean		isShutdownComponent(String componentURI)
	{
		return AbstractCVM.getCVM().isShutdownComponent(componentURI) ;
	}

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
	public boolean		isTerminatedComponent(String componentURI) 
	{
		return AbstractCVM.getCVM().isTerminatedComponent(componentURI) ;
	}
}
//-----------------------------------------------------------------------------
