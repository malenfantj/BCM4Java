package fr.sorbonne_u.components.plugins.asynccall.example;

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
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.plugins.asynccall.AbstractAsyncCall;
import fr.sorbonne_u.components.plugins.asynccall.AsyncCallServerPlugin;
import java.io.Serializable;

// -----------------------------------------------------------------------------
/**
 * The class <code>Server</code> implements a server component to be called
 * asynchronously.
 *
 * <p><strong>Description</strong></p>
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
public class			Server
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the asynchronous call client plug-in installed by this
	 *  component.															*/
	protected static final String	PLUGIN_URI = "server plugin URI";

	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	/**
	 * The class <code>Add</code> implements a command in a Command pattern to
	 * allow calling the service <code>add</code> asynchronously with a result
	 * sent back using another asynchronous call.
	 *
	 * <p><strong>Description</strong></p>
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
	 * <p>Created on : 2021-04-21</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class	Add
	extends		AbstractAsyncCall
	{
		private static final long serialVersionUID = 1L;

		/**
		 * create the command with the given actual parameters.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code parameters != null}
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @param parameters	parameters to be passed to the service.
		 */
		public			Add(Serializable[] parameters)
		{
			super(parameters);
		}

		/**
		 * @see fr.sorbonne_u.components.plugins.asynccall.AbstractAsyncCall#execute()
		 */
		@Override
		public void		execute() throws Exception
		{
			// the add command must have two and only two integer parameters.
			assert	this.parameters.length == 2;
			assert	this.parameters[0] instanceof Integer;
			assert	this.parameters[1] instanceof Integer;

			this.sendResult(((Server)this.receiver).add(
												(Integer)this.parameters[0],
												(Integer)this.parameters[1]));
		}
	}

	/**
	 * The class <code>Show</code> implements a command in a Command
	 * pattern to allow calling the service <code>show</code>
	 * asynchronously with a termination signal sent back using another
	 * asynchronous call.
	 *
	 * <p><strong>Description</strong></p>
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
	 * <p>Created on : 2021-04-21</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class	Show
	extends		AbstractAsyncCall
	{
		private static final long serialVersionUID = 1L;

		/**
		 * create the command with the given actual parameters.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code parameters != null}
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @param parameters	parameters to be passed to the service.
		 */
		public			Show(Serializable[] parameters)
		{
			super(parameters);
		}

		/**
		 * @see fr.sorbonne_u.components.plugins.asynccall.AbstractAsyncCall#execute()
		 */
		@Override
		public void		execute() throws Exception
		{
			assert	this.parameters.length == 1;
			assert	this.parameters[0] instanceof String;

			((Server)this.receiver).show((String)this.parameters[0]);
			// when the service has a void return type, the value null is sent
			// back to signal the completion of the call.
			this.sendResult(null);
		}
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a server component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected			Server()
	{
		super(1, 0);

		this.getTracer().setTitle("Server");
		this.getTracer().setRelativePosition(0, 1);
		this.toggleTracing();
	}

	/**
	 * create a server component with the given reflection inbound port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code reflectionInboundPortURI != null && reflectionInboundPortURI.length() > 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI for its reflection inbound port.
	 */
	protected			Server(String reflectionInboundPortURI)
	{
		super(reflectionInboundPortURI, 1, 0);

		this.getTracer().setTitle("Server");
		this.getTracer().setRelativePosition(0, 1);
		this.toggleTracing();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void start() throws ComponentStartException
	{
		super.start();

		try {
			this.traceMessage("Installing plugin...\n");
			AsyncCallServerPlugin plugin = new AsyncCallServerPlugin();
			plugin.setPluginURI(PLUGIN_URI);
			plugin.setPreferredExecutionServiceURI(
											STANDARD_REQUEST_HANDLER_URI);
			this.installPlugin(plugin);
			this.traceMessage("Plugin installed...\n");
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	// -------------------------------------------------------------------------
	// Component services
	// -------------------------------------------------------------------------

	/**
	 * add the two operands and return the result.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param 	a	first operand.
	 * @param 	b	second operand.
	 * @return	the result of the addition of the two operands.
	 */
	public int			add(int a, int b)
	{
		this.traceMessage("add called...\n");
		try {
			// add some waiting time simulating a long computation to show
			// the interest of futures
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			throw new RuntimeException(e) ;
		}
		this.traceMessage("add returns...\n");
		return a + b;
	}

	/**
	 * show the given message on the trace console.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code mes != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param mes	string to be shown.
	 */
	public void			show(String mes)
	{
		assert	mes != null;
		this.traceMessage("show called...\n");
		try {
			// add some waiting time simulating a long computation to show
			// the interest of futures
			Thread.sleep(1000L);
			this.traceMessage(mes);
			
		} catch (InterruptedException e) {
			throw new RuntimeException(e) ;
		}
		this.traceMessage("show returns...\n");
	}
}
// -----------------------------------------------------------------------------
