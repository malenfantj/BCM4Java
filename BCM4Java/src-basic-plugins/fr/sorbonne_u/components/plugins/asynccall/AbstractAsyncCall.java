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

import java.io.Serializable;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractAsyncCall</code> implements a form of command
 * pattern to provide a limited asynchronous call with future capability
 * to BCM.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The idea is that the commands receives an array of objects representing the
 * parameters of the call at creation time. Next, the caller side sets its
 * information: a call URI used to match the result when it is received and
 * the URI of an inbound port offering the
 * <code>AsynchronousResultReceptionCI</code> component interface to which the
 * result must be sent. This command object is passed to the server side, which
 * sets its own information: the reference to the component that will execute
 * the service corresponding to the call and the reference to the server side
 * plug-in through which the result is sent back to the caller. Then, the
 * command can be executed by calling its <code>execute</code> method.
 * </p>
 * <p>
 * Hence, an asynchronous call is implemented on the server side by a class that
 * extends <code>AbstractAsyncCall</code> to implement the method
 * <code>execute</code>. This method will call the component service and
 * explicitly send back the result through the protocol, using the method
 * <code>sendResult</code>.
 * </p>
 * <p>
 * For example, if the component service implementation interface is the
 * following:
 * </p>
 * <pre>
 * public interface TestI {
 *     public int add(int a, int b);
 *     public void increment();
 * }
 * </pre>
 * <p>
 * Then, the execute method is implemented as follows for calls with explicit
 * result:
 * </p>
 * <pre>
 * protected void execute() throws Exception
 * {
 *     this.sendResult(((TestI)this.receiver).
 *                               add((Integer)this.parameters[0],
 *                                   (Integer)this.parameters[1])));
 * }
 * </pre>
 * <p>
 * and as follows for ones without explicit result:
 * </p>
 * <pre>
 * protected void execute() throws Exception
 * {
 *     ((TestI)this.invoker).increment();
 *     this.sendResult(null);
 * }
 * </pre>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code parameters != null}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p>Created on : 2021-04-12</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractAsyncCall
implements	AsyncCallI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	/** component that will execute the call.							 	*/
	transient protected AbstractComponent		receiver;
	/** the real time asynchronous call server plug-in through which this
	 *  call is processed.													*/
	transient protected AsyncCallServerPlugin	plugin;

	/** URI of this call.													*/
	protected String				callURI;
	/** actual parameters of the call.										*/
	protected final Serializable[]	parameters;
	/** URI of the result reception inbound port to which the result must
	 *  be sent.															*/
	protected String				receptionPortURI;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a new asynchronous call instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public				AbstractAsyncCall()
	{
		this(new Serializable[]{});
	}

	/**
	 * create a new asynchronous call instance with the given actual parameters.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code params != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param params	actual parameters of the call.
	 */
	public				AbstractAsyncCall(Serializable[] params)
	{
		super();

		assert	params != null;

		this.parameters = params;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.plugins.asynccall.AsyncCallI#callInfoSet()
	 */
	@Override
	public boolean		callInfoSet()
	{
		return this.callURI != null && this.receptionPortURI != null;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.asynccall.AsyncCallI#setCallInfo(java.lang.String, java.lang.String)
	 */
	@Override
	public void			setCallInfo(String callURI, String receptionPortURI)
	{
		assert	!this.callInfoSet() : new PreconditionException("");
		assert	callURI != null && callURI.length() > 0 : new PreconditionException("");
		assert	receptionPortURI != null && receptionPortURI.length() > 0 : new PreconditionException("");

		this.callURI = callURI;
		this.receptionPortURI = receptionPortURI;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.asynccall.AsyncCallI#calleeInfoSet()
	 */
	@Override
	public boolean		calleeInfoSet()
	{
		return this.receiver != null && this.plugin != null;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.asynccall.AsyncCallI#setCalleeInfo(fr.sorbonne_u.components.AbstractComponent, fr.sorbonne_u.components.plugins.asynccall.AsyncCallServerPlugin)
	 */
	@Override
	public void			setCalleeInfo(
		AbstractComponent server,
		AsyncCallServerPlugin plugin
		) throws Exception
	{
		assert	!this.calleeInfoSet() :
				new PreconditionException("calleeInfoSet()");
		assert	server != null && plugin != null :
				new PreconditionException("server != null && plugin != null");
		assert	server.isInstalled(plugin.getPluginURI()) :
				new PreconditionException(
								"server.isInstalled(plugin.getPluginURI())");

		this.receiver = server;
		this.plugin = plugin;
		this.plugin.connectReceptionPort(this.receptionPortURI);
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.asynccall.AsyncCallI#sendResult(java.io.Serializable)
	 */
	@Override
	public void			sendResult(Serializable result) throws Exception
	{
		assert	this.callInfoSet() : new PreconditionException("callInfoSet()");

		this.plugin.sendResult(this.callURI, result, this.receptionPortURI);
	}
}
// -----------------------------------------------------------------------------
