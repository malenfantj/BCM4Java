package fr.sorbonne_u.components.examples.edp_cs.components;

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
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.examples.basic_cs.interfaces.URIConsumerCI;
import fr.sorbonne_u.components.examples.edp_cs.connections.URIServiceEndPoint;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.exceptions.ConnectionException;

//-----------------------------------------------------------------------------
/**
 * The class <code>URIConsumer</code> implements a component that gets URI
 * from a URI provider component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The component declares its required service through the required interface
 * <code>URIConsumerI</code> which has a <code>getURI</code> requested service
 * signature.  The internal method <code>getURIandPrint</code> implements the
 * main task of the component, as it calls the provider component through the
 * outbound port implementing the connection.  It does that repeatedly ten
 * times then disconnect and halt.  The <code>start</code> method initiates
 * this process. 
 * </p>
 * <p>
 * In this version, the connection between this component and the URI provider
 * one is done in the virtual machine code, in a form of external assembly of
 * components following one of the vision of the concept of component assembly.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 *
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-01-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// The next annotation requires that the referenced interface is added to
// the required interfaces of the component.
@RequiredInterfaces(required = {URIConsumerCI.class})
public class			URIConsumer
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** number of URIs that will be required by the consumer when calling
	 *  for several URIs at the same time.									*/
	protected final static int			N = 2 ;

	/** the endpoint of the URI service consumed by this component.			*/
	protected final URIServiceEndPoint uriServiceEndPoint;
	/**	counting service invocations.										*/
	protected int						counter ;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * @param uri					URI of the component
	 * @param uriServiceEndPoint	service endpoint for the URI provider.
	 * @throws Exception			<i>todo</i>.
	 */
	protected				URIConsumer(
		String uri,
		URIServiceEndPoint uriServiceEndPoint
		) throws Exception
	{
		// the reflection inbound port URI is the URI of the component
		// no simple thread and one schedulable thread
		super(uri, 0, 1) ;
		// if the required interface is not declared in the annotation
		// on the component class, it can be added manually with the
		// following instruction:
		//this.addRequiredInterface(URIConsumerI.class) ;

		this.uriServiceEndPoint = uriServiceEndPoint;
		this.counter = 0 ;

		if (AbstractCVM.isDistributed) {
			this.getLogger().setDirectory(System.getProperty("user.dir")) ;
		} else {
			this.getLogger().setDirectory(System.getProperty("user.home")) ;
		}
		this.getTracer().setTitle("consumer") ;
		this.getTracer().setRelativePosition(1, 1) ;

		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
	}

	//-------------------------------------------------------------------------
	// Component internal services
	//-------------------------------------------------------------------------

	/**
	 * method that implements the component's behaviour: call the URI service
	 * ten times and print the URI on the terminal, waiting a second between
	 * each call.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>todo.</i>
	 */
	public void			getURIandPrint() throws Exception
	{
		this.counter++ ;
		if (this.counter <= 10) {
			// Get the next URI and print it
			String uri = this.uriServiceEndPoint.getClientSideReference().getURI();
			this.logMessage("consumer getting a new URI no "
							+ this.counter + ": " + uri + ".");

			// Get a set of new URIs and print them
			String[] uris = this.uriServiceEndPoint.getClientSideReference().
													getURIs(URIConsumer.N);
			StringBuffer mes = new StringBuffer();
			for (int i = 0 ; i < URIConsumer.N ; i++) {
				mes.append(uris[i]);
				if (i < URIConsumer.N - 1) {
					mes.append(", ");
				}
			}
			this.logMessage("consumer getting a new set of URIs no "
							+ this.counter + " [" + mes + "].");

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
								((URIConsumer)this.getTaskOwner()).
													getURIandPrint();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					},
					1000, TimeUnit.MILLISECONDS) ;
		}
	}

	//-------------------------------------------------------------------------
	// Component life-cycle
	//-------------------------------------------------------------------------

	/**
	 * a component is always started by calling this method, so intercept the
	 * call and make sure the task of the component is executed.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public void			start() throws ComponentStartException
	{
		this.logMessage("starting consumer component.") ;
		super.start() ;
		// initialisation code can be put here; do not however call any
		// services of this component or of another component as they will
		// not have started yet, hence not able to execute any incoming calls.

		// connect to the server side
		try {
			this.uriServiceEndPoint.initialiseClientSide(this);
		} catch (ConnectionException e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		// application execution code (similar to a main method in Java) is
		// put here.

		this.logMessage("executing consumer component.") ;

		// Run the first service method invocation; the code of the method run
		// below will be executed asynchronously as a separate task, hence this
		// method execute will be free to finish its execution and free the
		// thread that is executing it.
		this.runTask(
			new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						((URIConsumer)this.getTaskOwner()).getURIandPrint() ;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		this.logMessage("stopping consumer component.") ;
		this.printExecutionLogOnFile("consumer");
		// This is the place where to clean up resources, such as
		// disconnecting ports and unpublishing outbound ports that
		// will be destroyed when shutting down.
		// In static architectures like in this example, endpoints can also
		// be disconnected by the finalise method of the component
		// virtual machine.
		this.uriServiceEndPoint.cleanUpClientSide();

		// This called at the end to make the component internal
		// state move to the finalised state.
		super.finalise();
	}
}
//-----------------------------------------------------------------------------
