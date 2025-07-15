package fr.sorbonne_u.components.plugins.asynccall;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide an
// implementation of the BCM component model.
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

import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.BCMRuntimeException;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.plugins.asynccall.connections.AsyncCallConnector;
import fr.sorbonne_u.components.plugins.asynccall.connections.AsyncCallOutboundPort;
import fr.sorbonne_u.components.plugins.asynccall.connections.AsyncCallResultReceptionInboundPort;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.components.reflection.connectors.ReflectionConnector;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;
import fr.sorbonne_u.components.reflection.ports.ReflectionOutboundPort;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.exceptions.VerboseException;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

// -----------------------------------------------------------------------------
/**
 * The class <code>AsyncCallClientSidePlugin</code> implements a protocol to
 * call asynchronously another component with a limited form of promise or
 * future variable as result.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This plug-in is designed to connect a client component to a server that
 * has installed the corresponding server side plug-in. Each pair of client
 * and server component that needs to be connected to perform asynchronous
 * calls using this protocol must implement a pair of corresponding plug-ins
 * to manage each connection.
 * </p>
 * <p>
 * To use this plug-in properly, a client component must first create an
 * instance of the plug-in, set its URI (as usual for plug-ins) and then
 * install it. Before doing any call, the connection to the server component
 * must be established by calling the method <code>connectToServer</code> with
 * the URI of the server reflection inbound port. After that, the client
 * component can perform asynchronous calls using the method
 * <code>asyncCall</code>. When no more calls are needed, the client component
 * can disconnect from the server by calling the method
 * <code>disconnectFromServer</code>.
 * </p>
 * <p>
 * When performing an asynchronous call, the calling code will receive a
 * completable future as immediate result. This future object can be used as
 * in Java, except that it is restricted to the scope of the component (it
 * can't be passed to another component).
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
 * <p>Created on : 2021-04-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			AsyncCallClientSidePlugin
extends		AbstractPlugin
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long						serialVersionUID = 1L;
	/** component interface required to perform asynchronous calls.			*/
	protected Class<? extends AsyncCallCI>			asyncCallRequiredInterface;
	/** port used to perform asynchronous calls to the server.				*/
	protected AsyncCallCI							outPort;
	/** component interface offered to receive the results of asynchronous
	 *  calls.																*/
	protected Class<? extends AsyncCallResultReceptionCI>
													resultReceptionInterface;
	/** port used ot receive the results of the asynchronous calls.			*/
	protected AsyncCallResultReceptionCI			inPort;

	/** hash map containing the completable futures awaiting to be completed
	 *  when the results of the corresponding calls will be received.		*/
	protected ConcurrentHashMap<String,RemoteCompletableFuture<Serializable>>
													awaitingResults;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * create a new asynchronous call client side plug-in instance which will
	 * use the standard request handler executor service to execute its code.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public				AsyncCallClientSidePlugin()
	{
		super();
	}

	/**
	 * create a new asynchronous call client side plug-in which will use the
	 * executor service with URI {@code executorServiceURI} to execute its code.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code executorServiceURI != null && !executorServiceURI.isEmpty()}
	 * post	{@code getPreferredExecutionServiceURI().equals(executorServiceURI)}
	 * </pre>
	 *
	 * @param executorServiceURI	URI of the executor service to be used to execute the service on the component or null if none.
	 * @throws VerboseException 	if {@code executorServiceURI == null || executorServiceURI.isEmpty()}.
	 */
	public				AsyncCallClientSidePlugin(String executorServiceURI)
	throws VerboseException
	{
		super(executorServiceURI);
	}

	// -------------------------------------------------------------------------
	// Plug-in life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractPlugin#installOn(fr.sorbonne_u.components.ComponentI)
	 */
	@Override
	public void			installOn(ComponentI owner) throws Exception
	{
		super.installOn(owner);

		if (this.asyncCallRequiredInterface == null) {
			this.asyncCallRequiredInterface = AsyncCallCI.class;
		}
		if (this.resultReceptionInterface == null) {
			this.resultReceptionInterface = AsyncCallResultReceptionCI.class;
		}

		if (!this.getOwner().isRequiredInterface(
											this.asyncCallRequiredInterface)) {
			this.addRequiredInterface(this.asyncCallRequiredInterface);
		}
		if (!this.getOwner().isOfferedInterface(this.resultReceptionInterface)) {
			this.addOfferedInterface(this.resultReceptionInterface);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPlugin#initialise()
	 */
	@Override
	public void			initialise() throws Exception
	{
		super.initialise();

		this.awaitingResults = new ConcurrentHashMap<>();

		this.outPort = (AsyncCallCI) this.createAsyncCallOutboundPort();
		this.getOutboundPort().publishPort();
		this.inPort =
				(AsyncCallResultReceptionCI)
									this.createResultReceptionInboundPort();
		this.getInboundPort().publishPort();
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		if (this.getOutboundPort() != null) {
			if (this.getOutboundPort().connected()) {
				this.getOwner().doPortDisconnection(
										this.getOutboundPort().getPortURI());
			}
			this.getOutboundPort().unpublishPort();
		}
		this.awaitingResults.clear();

		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#uninstall()
	 */
	@Override
	public void			uninstall() throws Exception
	{
		this.getInboundPort().unpublishPort();

		super.uninstall();
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * return true if the component is connected to its server.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the component is connected to its server.
	 */
	public boolean		isConnectedToServer()
	{
		try {
			return this.getOutboundPort() != null &&
										this.getOutboundPort().connected();
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * connect this component to the one with the given reflection inbound port
	 * URI to perform asynchronous calls.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isConnectedToServer()}
	 * post	{@code isConnectedToServer()}
	 * </pre>
	 *
	 * @param serverReflectionInboundPortURI	URI of the reflection inbound port of the server component.
	 * @throws Exception						<i>to do</i>.
	 */
	public void			connectToServer(String serverReflectionInboundPortURI)
	throws Exception
	{
		// TODO: use the dynamic connection plug-in?

		assert	!this.isConnectedToServer();
		assert	serverReflectionInboundPortURI != null &&
								serverReflectionInboundPortURI.length() != 0;

		boolean wasRequiringReflectionCI = true;
		if (!this.getOwner().isRequiredInterface(ReflectionCI.class)) {
			this.addRequiredInterface(ReflectionCI.class);
			wasRequiringReflectionCI = false;
		}

		ReflectionOutboundPort rop =
							new ReflectionOutboundPort(this.getOwner());
		rop.publishPort();
		this.getOwner().doPortConnection(
				rop.getPortURI(),
				serverReflectionInboundPortURI,
				ReflectionConnector.class.getCanonicalName());
		String[] serverInboundPortURIs =
				rop.findInboundPortURIsFromInterface(AsyncCallCI.class);
		assert	serverInboundPortURIs != null &&
											serverInboundPortURIs.length == 1;

		this.getOwner().doPortDisconnection(rop.getPortURI());
		rop.unpublishPort();
		rop.destroyPort();
		if (!wasRequiringReflectionCI) {
			this.removeRequiredInterface(ReflectionCI.class);
		}

		this.getOwner().doPortConnection(
				this.getOutboundPort().getPortURI(),
				serverInboundPortURIs[0],
				AsyncCallConnector.class.getCanonicalName());

		assert	this.isConnectedToServer();
	}

	/**
	 * disconnect this component from the server component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isConnectedToServer()}
	 * post	{@code !isConnectedToServer()}
	 * </pre>
	 * 
	 * @throws Exception	<i>to do</i>.
	 *
	 */
	public void			disconnectFromServer() throws Exception
	{
		this.outPort.disconnectClient(this.getInboundPort().getPortURI());
		this.getOwner().doPortDisconnection(this.getOutboundPort().getPortURI());
		this.getOutboundPort().unpublishPort();
		this.getOutboundPort().destroyPort();
		this.outPort = null;
	}

	/**
	 * call the server side asynchronously.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code c != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param c				an asynchronous call object (Command pattern).
	 * @throws Exception	<i>to do</i>.
	 */
	public void			asyncCall(AbstractAsyncCall c) throws Exception
	{
		assert	c != null : new PreconditionException("c != null");

		this.outPort.asyncCall(c);
	}

	/**
	 * call the server side asynchronously using the protocol implemented by
	 * this plug-in to synchronise with the termination of the remote task,
	 * potentially getting a result.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code c != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param c				an asynchronous call object.
	 * @return				a completable future that will contain the result or null if no explicit result is awaited.
	 * @throws Exception	<i>to do</i>.
	 */
	public RemoteCompletableFuture<Serializable>	asyncCallWithFuture(
		AbstractAsyncCall c
		) throws Exception
	{
		assert	c != null : new PreconditionException("c != null");

		// see http://www.asciiarmor.com/post/33736615/java-util-uuid-mini-faq
		String callURI = java.util.UUID.randomUUID().toString();
		RemoteCompletableFuture<Serializable> cf =
									new RemoteCompletableFuture<Serializable>();
		this.awaitingResults.put(callURI, cf);
		c.setResultReceptionInfo(callURI, this.getInboundPort().getPortURI());
		this.outPort.asyncCall(c);
		return cf;
	}

	/**
	 * receive the result of an asynchronous call, setting the corresponding
	 * completable future value.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code callURI != null && !callURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param callURI		URI attributed to the call when it was passed to the server.
	 * @param result		the result of the call coming back from the server.
	 * @throws BCMException	<i>to do</i>.
	 */
	public void			receive(String callURI, Serializable result)
	throws BCMException
	{
		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			this.getOwner().traceMessage(
					"AsyncCallClientPlugin[" + this.getPluginURI()
					+ "]::receive(" + callURI + ", " + result + ")\n");
		}

		assert	callURI != null && !callURI.isEmpty() :
				new PreconditionException(
								"callURI != null && !callURI.isEmpty()");

		assert	this.awaitingResults.containsKey(callURI) :
				new BCMException("unknown call URI: " + callURI);

		RemoteCompletableFuture<Serializable> cf =
									this.awaitingResults.remove(callURI);

		assert	cf != null : new BCMRuntimeException("cf != null");
		assert	!cf.isDone() : new BCMRuntimeException("!cf.isDone()");

		cf.complete(result);
	}

	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	/**
	 * return the outbound port to perform asynchronous calls.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the outbound port to perform asynchronous calls.
	 */
	protected AbstractOutboundPort	getOutboundPort()
	{
		return (AbstractOutboundPort) this.outPort;
	}

	/**
	 * return	the inbound port receiving results.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the inbound port receiving results.
	 */
	protected AbstractInboundPort	getInboundPort()
	{
		return (AbstractInboundPort) this.inPort;
	}

	/**
	 * create a result reception inbound port for this plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && AsyncCallResultReceptionCI.class.isAssignableFrom(return.getClass())}
	 * </pre>
	 *
	 * @return				a result reception inbound port.
	 * @throws Exception	<i>to do</i>.
	 */
	protected AbstractInboundPort	createResultReceptionInboundPort()
	throws Exception
	{
		AbstractInboundPort ret =
				new AsyncCallResultReceptionInboundPort(
							this.getOwner(),
							this.getPluginURI(),
							this.getPreferredExecutionServiceURI());

		assert	AsyncCallResultReceptionCI.class.isAssignableFrom(ret.getClass()) :
				new PostconditionException(
						"AsyncCallResultReceptionCI.class.isAssignableFrom("
						+ "ret.getClass())");
		assert	this.resultReceptionInterface.isAssignableFrom(ret.getClass()) :
				new BCMException(
						"resultReceptionInterface.isAssignableFrom("
						+ "ret.getClass())"); 

		return ret;
	}

	/**
	 * create an asynchronous call outbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && AsyncCallCI.class.isAssignableFrom(return.getClass()}
	 * </pre>
	 *
	 * @return				an asynchronous call outbound port.
	 * @throws Exception	<i>to do</i>.
	 */
	protected AbstractOutboundPort	createAsyncCallOutboundPort()
	throws Exception
	{
		AbstractOutboundPort ret = new AsyncCallOutboundPort(this.getOwner());

		assert	AsyncCallCI.class.isAssignableFrom(ret.getClass()) :
				new PostconditionException(
						"AsyncCallCI.class.isAssignableFrom(ret.getClass()");
		assert	this.asyncCallRequiredInterface.isAssignableFrom(ret.getClass()) :
				new BCMException(
						"asyncCallRequiredInterface.isAssignableFrom("
						+ "ret.getClass())");

		return ret;
	}
}
// -----------------------------------------------------------------------------
