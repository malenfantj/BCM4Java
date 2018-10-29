package fr.sorbonne_u.components.examples.ddeployment_cs.components;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.examples.basic_cs.interfaces.URIConsumerI;
import fr.sorbonne_u.components.examples.basic_cs.ports.URIConsumerOutboundPort;
import fr.sorbonne_u.components.examples.ddeployment_cs.interfaces.URIConsumerLaunchI;
import fr.sorbonne_u.components.examples.ddeployment_cs.ports.URIConsumerLaunchInboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;

//-----------------------------------------------------------------------------
/**
 * The class <code>DynamicURIConsumer</code> is the dynamically deployed
 * version of the component <code>URIConsumer</code> of the basic
 * client/server example.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2014-03-14</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = {URIConsumerI.class})
@OfferedInterfaces(offered = {URIConsumerLaunchI.class})
public class				DynamicURIConsumer
extends		AbstractComponent
{
	// ------------------------------------------------------------------------
	// Constructors and instance variables
	// ------------------------------------------------------------------------

	protected final static int	N = 2 ;

	/**	the outbound port used to call the service.						*/
	protected URIConsumerOutboundPort		uriGetterPort ;
	/** URI of the port through with the component is lauched.			*/
	protected URIConsumerLaunchInboundPort	launchInboundPort ;
	/**	counting service invocations.									*/
	protected int							counter ;

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri					URI of the component.
	 * @param outboundPortURI		URI of the outbound port.
	 * @param launchInboundPortURI	URI of the inbound port used to launch the component execution.
	 * @throws Exception				<i>todo.</i>
	 */
	public				DynamicURIConsumer(
		String uri,
		String outboundPortURI,
		String launchInboundPortURI
		) throws Exception
	{
		// the reflection inbound port URI is the URI of the component
		// no simple thread but one schedulable thread
		super(uri, 0, 1) ;

		this.counter = 0 ;

		this.uriGetterPort =
					new URIConsumerOutboundPort(outboundPortURI, this) ;
		// add the port to the set of ports of the component
		this.addPort(this.uriGetterPort) ;
		// publish the port
		this.uriGetterPort.localPublishPort() ;

		this.launchInboundPort =
				new URIConsumerLaunchInboundPort(launchInboundPortURI, this) ;
		this.addPort(this.launchInboundPort) ;
		this.launchInboundPort.publishPort() ;

		this.tracer.setTitle("dynamic consumer") ;
		this.tracer.setRelativePosition(1, 1) ;
	}

	// ------------------------------------------------------------------------
	// Component life-cycle
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		this.doPortDisconnection(this.uriGetterPort.getPortURI()) ;
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
			this.uriGetterPort.unpublishPort() ;
			this.launchInboundPort.unpublishPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		
		super.shutdown();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdownNow()
	 */
	@Override
	public void			shutdownNow() throws ComponentShutdownException
	{
		try {
			this.uriGetterPort.unpublishPort() ;
			this.launchInboundPort.unpublishPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		
		super.shutdownNow();
	}

	// ------------------------------------------------------------------------
	// Component services
	// ------------------------------------------------------------------------

	public void			getURIandPrint() throws Exception
	{
		this.counter++ ;
		if (this.counter <= 10) {
			// Get the next URI and print it
			this.traceMessage("consumer getting a new URI.\n") ;
			String uri = this.uriGetterPort.getURI() ;
			this.traceMessage("URI no " + this.counter + ": " + uri + "\n") ;

			// Get a set of new URIs and print them
			this.traceMessage("consumer getting a new set of URIs.\n") ;
			String[] uris = this.uriGetterPort.getURIs(DynamicURIConsumer.N) ;
			String out = "URI set no " + this.counter + " [" ;
			for (int i = 0 ; i < DynamicURIConsumer.N ; i++) {
				out += uris[i] ;
				if (i < DynamicURIConsumer.N - 1) {
					out += ", " ;
				}
			}
			this.traceMessage(out + "]\n") ;

			// Schedule the next service method invocation in one second.
			// All tasks and services of a component must be called through
			// the methods for running tasks and handling requests.  These
			// methods (from the CVM) handles the internal concurrency of
			// the component when required, and therefore ensure their good
			// properties (like synchronisation).
			this.scheduleTask(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								((DynamicURIConsumer)this.getOwner()).
														getURIandPrint() ;
							} catch (Exception e) {
								throw new RuntimeException(e) ;
							}
						}
					},
					1000, TimeUnit.MILLISECONDS) ;
		}
	}
}
//-----------------------------------------------------------------------------
