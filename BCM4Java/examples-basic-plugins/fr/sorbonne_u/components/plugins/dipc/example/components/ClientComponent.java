package fr.sorbonne_u.components.plugins.dipc.example.components;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.PluginI;
import fr.sorbonne_u.components.connectors.DataConnector;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.plugins.dipc.DataInterfacePushControlClientSidePlugin;
import fr.sorbonne_u.components.plugins.dipc.example.interfaces.PairDataCI;
import fr.sorbonne_u.components.plugins.dipc.example.ports.PairDataOutboundPort;

//------------------------------------------------------------------------------
/**
 * The class <code>ClientComponent</code> implements a component that uses
 * the push control of the push control plug-in to start the pushing of data
 * from a server component and then consume these data before stopping.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2019-03-06</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			ClientComponent
extends		AbstractComponent
{
	// ------------------------------------------------------------------------
	// Component constants and variables
	// ------------------------------------------------------------------------

	/** URI of the client-side push control plug-in.					*/
	protected static final String	PUSH_CONTROL_CS_PLUGIN_URI = "cc-pcpuri" ;
	/** the data outbound port through which the push data are received.*/
	protected PairDataOutboundPort	pairDataOBP ;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * component creation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	this.isInstalled(PUSH_CONTROL_CS_PLUGIN_URI)
	 * </pre>
	 *
	 * @throws Exception	<i>to do.</i>
	 */
	protected			ClientComponent() throws Exception
	{
		this("ClientComponent-" + AbstractPort.generatePortURI()) ;
	}

	/**
	 * component creation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	reflectionInboundPortURI != null
	 * post	this.isInstalled(PUSH_CONTROL_CS_PLUGIN_URI)
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection inbound port of the component.
	 * @throws Exception				<i>to do.</i>
	 */
	protected			ClientComponent(String reflectionInboundPortURI)
	throws Exception
	{
		super(reflectionInboundPortURI, 2, 0);

		this.getTracer().setTitle("ClientComponent") ;
		this.getTracer().setRelativePosition(1,  1) ;

		// Add the standard component interfaces for data exchanging
		// components.
		this.addOfferedInterface(DataRequiredCI.PushCI.class) ;
		this.addRequiredInterface(DataRequiredCI.PullCI.class) ;

		// create and publish the data outbound port.
		this.pairDataOBP = new PairDataOutboundPort(this) ;
		this.pairDataOBP.publishPort() ;

		// create the client-side push control plug-in and install it
		// on the current component.
		PluginI plugin = new DataInterfacePushControlClientSidePlugin() ;
		plugin.setPluginURI(PUSH_CONTROL_CS_PLUGIN_URI) ;
		this.installPlugin(plugin) ;

		assert	this.isInstalled(PUSH_CONTROL_CS_PLUGIN_URI) ;
	}

	// ------------------------------------------------------------------------
	// Component life-cycle methods
	// ------------------------------------------------------------------------

	/**
	 * connects the data outbound port to the server side.
	 * 
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public void			start() throws ComponentStartException
	{
		super.start() ;

		this.logMessage("client component starts.") ;

		try {
			this.doPortConnection(
					this.pairDataOBP.getPortURI(),
					ServerComponent.SS_DATAINBOUNDPORT_URI,
					DataConnector.class.getCanonicalName()) ;
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}

		this.logMessage("client component connected to server for data.") ;
	}

	/**
	 * the scenario first launch a limited series of 10 pushes every
	 * 100ms and then wait 2 seconds before starting an unlimited
	 * series of pushes every 25ms that is stopped after 1 second.
	 * 
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		super.execute() ;

		this.logMessage("client component requires starting 10 pushes.") ;

		DataInterfacePushControlClientSidePlugin p =
			(DataInterfacePushControlClientSidePlugin)
						this.getPlugin(PUSH_CONTROL_CS_PLUGIN_URI) ;
		p.connectWithServerSide(
						ServerComponent.SC_ReflectionInboundPort_URI) ;
		p.startLimitedPushing(
						ServerComponent.SS_DATAINBOUNDPORT_URI, 100L, 10) ;

		Thread.sleep(2000L) ;

		this.logMessage("client component requires starting pushes for 1s.") ;

		p.startUnlimitedPushing(
						ServerComponent.SS_DATAINBOUNDPORT_URI, 25L) ;
		
		Thread.sleep(1000L) ;

		this.logMessage("client component stops pushes.") ;

		p.stopPushing(ServerComponent.SS_DATAINBOUNDPORT_URI) ;
		
		this.logMessage("client component activity ends, turns passive.") ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		this.doPortDisconnection(this.pairDataOBP.getPortURI()) ;

		super.finalise() ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
			this.pairDataOBP.unpublishPort() ;
			this.pairDataOBP.destroyPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}

		super.shutdown() ;
	}

	// ------------------------------------------------------------------------
	// Component service methods
	// ------------------------------------------------------------------------

	/**
	 * consume the data i.e., log it on the trace.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	d != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param d	the pushed data.
	 */
	public void			consumeData(PairDataCI.PairI d)
	{
		assert	d != null ;

		this.logMessage("new data received = (" +
							d.getFirst() + ", " +
							d.getSecond() +")") ;
	}
}
//------------------------------------------------------------------------------
