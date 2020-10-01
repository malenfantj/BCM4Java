package fr.sorbonne_u.components.plugins.dconnection.example.components;

// Copyright Jacques Malenfant, Sorbonne Universite.
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
import fr.sorbonne_u.components.annotations.AddPlugin;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.plugins.dconnection.DynamicConnectionServerSidePlugin;
import fr.sorbonne_u.components.plugins.dconnection.example.interfaces.ExampleCI;
import fr.sorbonne_u.components.plugins.dconnection.example.ports.ExampleInboundPort;
import fr.sorbonne_u.components.ports.InboundPortI;
import fr.sorbonne_u.components.ports.PortI;

/**
 * The class <code>ServerSideExample</code> show how a server component
 * uses the dynamic connection plug-in to be connected with a client
 * component and call its services.
 *
 * <p><strong>Description</strong></p>
 * 
 * To benefit from the example, carefully read the code and adapt it to your
 * needs in your own code.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 2017-02-17</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// -----------------------------------------------------------------------------
@OfferedInterfaces(offered = {ExampleCI.class})
@AddPlugin(pluginClass = ServerSideExample.ServerSidePlugin.class,
		   pluginURI = ServerSideExample.DYNAMIC_CONNECTION_PLUGIN_URI)
// -----------------------------------------------------------------------------
public class			ServerSideExample
extends		AbstractComponent
// -----------------------------------------------------------------------------
{
	public final static String	DYNAMIC_CONNECTION_PLUGIN_URI =
													"serverSidePluginURI";

	// -------------------------------------------------------------------------
	// Inner classes
	// -------------------------------------------------------------------------

	/**
	 * The class <code>ServerSidePlugin</code> implements the server side
	 * plug-in by providing an implementation for the method
	 * <code>createServerSideDynamicPort</code>.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	true
	 * </pre>
	 * 
	 * <p>Created on : 2017-02-17</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class	ServerSidePlugin
	extends		DynamicConnectionServerSidePlugin
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @see fr.sorbonne_u.components.plugins.dconnection.DynamicConnectionServerSidePlugin#createAndPublishServerSideDynamicPort(java.lang.Class)
		 */
		@Override
		protected InboundPortI createAndPublishServerSideDynamicPort(
			Class<? extends OfferedCI> offeredInterface
			) throws Exception
		{
			assert	ExampleCI.class.isAssignableFrom(offeredInterface) &&
						offeredInterface.isAssignableFrom(ExampleCI.class);

			InboundPortI ret = new ExampleInboundPort(this.getOwner());
			ret.publishPort();
			return ret;
		}
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the server component xith the given reflection inbound port URI
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code reflectionInboundPortURI != null}
	 * post	{@code isInstalled(DynamicConnectionServerSidePlugin.PLUGIN_URI)}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection inbound port for this component.
	 * @throws Exception 				<i>to do</i>.
	 */
	protected			ServerSideExample(
		String reflectionInboundPortURI
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0);

		assert	reflectionInboundPortURI != null;
		this.toggleTracing();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
			String[] uris =
					this.findInboundPortURIsFromInterface(ExampleCI.class);
			PortI p = this.findPortFromURI(uris[0]);
			p.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services
	// -------------------------------------------------------------------------

	/**
	 * simple service in this example: add 1 to the parameter and
	 * return this result.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param i	integer that must be incremented by 1.
	 * @return	the parameter incremented by 1.
	 */
	public int			add1(int i)
	{
		this.logMessage("server side called with " + i);
		return i + 1;
	}
}
// -----------------------------------------------------------------------------
