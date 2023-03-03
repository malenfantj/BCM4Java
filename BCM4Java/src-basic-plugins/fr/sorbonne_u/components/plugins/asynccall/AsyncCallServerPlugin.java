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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.plugins.asynccall.connections.AsyncCallInboundPort;
import fr.sorbonne_u.components.plugins.asynccall.connections.AsyncCallResultReceptionConnector;
import fr.sorbonne_u.components.plugins.asynccall.connections.AsyncCallResultReceptionOutboundPort;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.exceptions.PreconditionException;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

// -----------------------------------------------------------------------------
/**
 * The class <code>AsyncCallServerPlugin</code> implements a protocol to be
 * called asynchronously by another component to which the result is sent
 * back using another asynchronous call.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * To use this plug-in properly, a server classical component must first create
 * an instance of the plug-in, set its URI (as usual for plug-ins), set its
 * preferred execution service URI and then install it.
 * </p>
 * <p>
 * In asynchronous calls, the caller provides the URI of an inbound port to
 * which the server must connect to send back the result of the call when
 * it becomes available. In this implementation of the server-side plug-in,
 * the choice is made to connect to this inbound port once at the first
 * reception of a call from a given client and not to disconnect until the
 * uninstallation of the plug-in. If a lot of clients are used, this choice
 * should be changed to connect/disconnect at result sending time.
 * </p>
 * <p>
 * TODO: One limitation of the current implementation is the thread safety that
 * can be challenged if the call can be executed in parallel with the connection of
 * the outbound port used to return the result. No waiting is put in place to
 * ensure that this connection is done before the method {@code sendResult} is
 * called, hence it is possible that the precondition that this connection has
 * been done before the call to {@code sendResult} may fail due to race
 * conditions.
 * </p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2021-04-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			AsyncCallServerPlugin
extends		AbstractPlugin
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long					serialVersionUID = 1L;
	/** port through which the asynchronous calls are received.				*/
	protected AsyncCallInboundPort				inPort;
	/** ports through which results are sent back the the caller
	 *  components.															*/
	protected ConcurrentHashMap<String,AsyncCallResultReceptionOutboundPort>
												resultReceptionOutboundPorts;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a plug-in instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public				AsyncCallServerPlugin()
	{
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

		if (!this.getOwner().isOfferedInterface(AsyncCallCI.class)) {
			this.addOfferedInterface(AsyncCallCI.class);
		}
		if (!this.getOwner().isRequiredInterface(
										AsyncCallResultReceptionCI.class)) {
			this.addRequiredInterface(AsyncCallResultReceptionCI.class);
		}
		this.resultReceptionOutboundPorts = new ConcurrentHashMap<>();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPlugin#initialise()
	 */
	@Override
	public void			initialise() throws Exception
	{
		super.initialise();

		this.inPort = new AsyncCallInboundPort(this.getOwner(),
											   this.getPluginURI());
		this.inPort.publishPort();
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		for (AsyncCallResultReceptionOutboundPort p :
								this.resultReceptionOutboundPorts.values()) {
			this.getOwner().doPortDisconnection(p.getPortURI());
			p.unpublishPort();
		}
		this.resultReceptionOutboundPorts.clear();

		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#uninstall()
	 */
	@Override
	public void			uninstall() throws Exception
	{
		this.inPort.unpublishPort();

		super.uninstall();
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * return true if {@code receptionPortURI} corresponds to an outbound port
	 * connected to the designated inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code receptionPortURI != null && !receptionPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param receptionPortURI	URI of a reception inbound port.
	 * @return					true if {@code receptionPortURI} corresponds to an outbound port connected to the designated inbound port.
	 * @throws Exception		<i>to do</i>.
	 */
	public boolean		receptionPortConnected(String receptionPortURI)
	throws Exception
	{
		assert	receptionPortURI != null && !receptionPortURI.isEmpty() :
				new PreconditionException("receptionPortURI != null && !receptionPortURI.isEmpty()");
		if (!this.resultReceptionOutboundPorts.containsKey(receptionPortURI)) {
			return false;
		} else {
			PortI p = this.resultReceptionOutboundPorts.get(receptionPortURI);
			if (p == null) {
				return false;
			} else {
				return p.connected();
			}
		}
	}

	/**
	 * connect a result reception outbound port to the inbound port which URI
	 * is given, if not connected yet.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code receptionPortURI != null && receptionPortURI.length() != 0}
	 * post	{@code receptionPortConnected(receptionPortURI)}
	 * </pre>
	 *
	 * @param receptionPortURI	URI of the port waiting for the result of a call.
	 * @throws Exception		<i>to do</i>.
	 */
	public void			connectReceptionPort(String receptionPortURI)
	throws Exception
	{
		assert	receptionPortURI != null && receptionPortURI.length() != 0;

		try {
			AsyncCallResultReceptionOutboundPort p =
					new AsyncCallResultReceptionOutboundPort(this.getOwner());
			AsyncCallResultReceptionOutboundPort p1 =
					this.resultReceptionOutboundPorts.
											putIfAbsent(receptionPortURI, p);
			if (p1 == null) {
				p.publishPort();
				this.getOwner().doPortConnection(
					p.getPortURI(),
					receptionPortURI,
					AsyncCallResultReceptionConnector.class.getCanonicalName());
			} else {
				assert	p1.connected();
			}
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * execute the given asynchronous call as a task on the owner component,
	 * which will then send back its result.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code c != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param c				asynchronous call to be executed.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			asyncCall(AbstractAsyncCall c) throws Exception
	{
		assert	c != null;

		c.setCalleeInfo((AbstractComponent)this.getOwner(), this);
		this.runTaskOnComponent(this.getPreferredExecutionServiceIndex(),
								new AbstractComponent.AbstractTask() {
									@Override
									public void run() {
										try {
											c.execute();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});
	}

	/**
	 * send the result of a call back to the caller component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code callURI != null && callURI.length() > 0}
	 * pre	{@code receptionPortURI != null && receptionPortURI.length() > 0}
	 * pre	{@code receptionPortConnected(receptionPortURI)}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param callURI			URI of the call.
	 * @param result			result to be returned to the caller.
	 * @param receptionPortURI	URI of the port waiting for the result of the call.
	 * @throws Exception		<i>to do</i>.
	 */
	public void			sendResult(
		String callURI,
		Serializable result,
		String receptionPortURI
		) throws Exception
	{
		assert	callURI != null && callURI.length() > 0 :
				new PreconditionException(
								"callURI != null && callURI.length() > 0");
		assert	receptionPortURI != null && receptionPortURI.length() > 0 :
				new PreconditionException(
								"receptionPortURI != null && "
								+ "receptionPortURI.length() > 0");
		assert	this.receptionPortConnected(receptionPortURI) :
				new PreconditionException(
								"receptionPortConnected(receptionPortURI)");

		try {
			this.resultReceptionOutboundPorts.get(receptionPortURI).
												acceptResult(callURI, result);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
// -----------------------------------------------------------------------------
