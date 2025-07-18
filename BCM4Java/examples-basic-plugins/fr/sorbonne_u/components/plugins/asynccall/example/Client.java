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
import fr.sorbonne_u.components.plugins.asynccall.AsyncCallClientSidePlugin;
import fr.sorbonne_u.components.plugins.asynccall.RemoteCompletableFuture;

import java.io.Serializable;

// -----------------------------------------------------------------------------
/**
 * The class <code>Client</code> implements a client component calling services
 * using a limited form of asynchronous call with future <i>i.e.</i>, with a
 * result managed as a local future (to the caller component).
 *
 * <p><strong>Description</strong></p>
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
public class			Client
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the asynchronous call client plug-in installed by this
	 *  component.															*/
	protected static final String		PLUGIN_URI = "client plugin URI";
	/** The reference to the asynchronous call client plug-in installed
	 *  by this component.													*/
	protected AsyncCallClientSidePlugin plugin;
	/** URI of the reflection inbound port of the server component.			*/
	protected final String				serverReflectionInboundPortURI;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the client component with the given URI of the server component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code serverURI != null && serverURI.length() > 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param serverReflectionInboundPortURI	URI of the server component reflection inbound port URI.
	 */
	protected			Client(String serverReflectionInboundPortURI)
	{
		// one thread for execute, one to receive the result
		super(2, 0);

		assert	serverReflectionInboundPortURI != null && serverReflectionInboundPortURI.length() > 0;

		this.serverReflectionInboundPortURI = serverReflectionInboundPortURI;
		this.getTracer().setTitle("Client");
		this.getTracer().setRelativePosition(1, 0);
		this.toggleTracing();
	}

	/**
	 * create the client component with the given URI for its reflection
	 * inbound port and the given URI of the server component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code reflectionInboundPortURI != null && reflectionInboundPortURI.length() > 0}
	 * pre	{@code serverURI != null && serverURI.length() > 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI			URI for its reflection inbound port.
	 * @param serverReflectionInboundPortURI	URI of the server component reflection inbound port URI.
	 */
	protected			Client(
		String reflectionInboundPortURI,
		String serverReflectionInboundPortURI
		)
	{
		super(reflectionInboundPortURI, 2, 0);

		assert	reflectionInboundPortURI != null &&
										reflectionInboundPortURI.length() > 0;
		assert	serverReflectionInboundPortURI != null && serverReflectionInboundPortURI.length() > 0;

		this.serverReflectionInboundPortURI = serverReflectionInboundPortURI;
		this.getTracer().setTitle("Client");
		this.toggleTracing();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		try {
			this.traceMessage(
					"Installing the asynchronous call client plug-in...\n");
			this.plugin = new AsyncCallClientSidePlugin();
			this.plugin.setPluginURI(PLUGIN_URI);
			this.installPlugin(this.plugin);
			this.traceMessage("Plug-in installed...\n");
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void	execute() throws Exception
	{
		this.traceMessage("Connecting to server "
							+ this.serverReflectionInboundPortURI + "...\n");
		this.plugin.connectToServer(this.serverReflectionInboundPortURI);

		assert	this.plugin.isConnectedToServer();
		this.traceMessage("Server connected...\n");

		this.traceMessage("Calling add...\n");
		RemoteCompletableFuture<Serializable> cf1 =
			this.plugin.asyncCallWithFuture(new Server.Add(1, 2));
		this.traceMessage("Calling show...\n");
		// CompletableFuture<T> can be used as type, but cf2 will indeed contain
		// a RemoteCompletableFuture, hence that can't be cancelled...
		RemoteCompletableFuture<Serializable> cf2 =
			this.plugin.asyncCallWithFuture(
								new Server.Show("message from client.\n"));

		if (!cf1.isDone()) {
			this.traceMessage("Waiting for the result of add...\n");
		}
		this.traceMessage("Result of add: " + cf1.get() + "\n");
		if (!cf2.isDone()) {
			this.traceMessage("Waiting for the show to return...\n");
		}
		this.traceMessage("Returning from show: " + cf2.get() + "\n");

		this.traceMessage("Disconnecting from server...\n");
		this.plugin.disconnectFromServer();
		this.traceMessage("Disconnected from server...\n");
	}
}
// -----------------------------------------------------------------------------
