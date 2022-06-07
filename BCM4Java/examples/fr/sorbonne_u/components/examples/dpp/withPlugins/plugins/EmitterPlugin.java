package fr.sorbonne_u.components.examples.dpp.withPlugins.plugins;

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

import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.examples.dpp.connections.TransmissionConnector;
import fr.sorbonne_u.components.examples.dpp.connections.TransmissionOutBoundPort;
import fr.sorbonne_u.components.examples.dpp.interfaces.PipelineDataI;
import fr.sorbonne_u.components.examples.dpp.interfaces.TransmissionCI;

// -----------------------------------------------------------------------------
/**
 * The class <code>EmitterPlugin</code> implements the functionalities that
 * are required by components that act as data sources in the data processing
 * pipeline.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}
 * </pre>
 * 
 * <p>Created on : 2022-06-03</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			EmitterPlugin
extends		AbstractPlugin
{
	// -------------------------------------------------------------------------
	// Variables and constants
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** outbound port used to transmit data pieces to the first component
	 *  in a data processing pipeline.										*/
	protected TransmissionOutBoundPort	transmissionOutboundPort;
	/** URI of the inbound port of the first processing componenet in the
	 *  data processing pipeline.											*/
	protected String					nextProcessingComponentInboundPortURI;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public				EmitterPlugin()
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

		this.addRequiredInterface(TransmissionCI.class);
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPlugin#initialise()
	 */
	@Override
	public void			initialise() throws Exception
	{
		this.transmissionOutboundPort =
						new TransmissionOutBoundPort(this.getOwner());
		this.transmissionOutboundPort.publishPort();
		super.initialise();
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		if (this.transmissionOutboundPort.connected()) {
			this.getOwner().doPortDisconnection(
					this.transmissionOutboundPort.getPortURI());
		}
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#uninstall()
	 */
	@Override
	public void			uninstall() throws Exception
	{
		this.transmissionOutboundPort.unpublishPort();
		this.transmissionOutboundPort.destroyPort();
		this.removeRequiredInterface(TransmissionCI.class);
		super.uninstall();
	}

	// -------------------------------------------------------------------------
	// Plug-in methods
	// -------------------------------------------------------------------------

	/**
	 * initialise the URI of the inbound port of the first component in the
	 * data processing pipeline; this method is useful to avoid changing the
	 * constructor and thus maintain the same creation protocol for all
	 * plug-ins; it must be called before trying to connect to the next
	 * component, indeed.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code nextTransmissionInboundPortURI != null}
	 * pre	{@code !nextTransmissionInboundPortURI.isEmpty()}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param nextTransmissionInboundPortURI	URI of the inbound port of the first processing componenet in the data processing pipeline.
	 * @throws Exception						<i>to do</i>.
	 */
	public void			setNextInboundPortURI(
		String nextTransmissionInboundPortURI
		) throws Exception
	{
		assert	nextTransmissionInboundPortURI != null;
		assert	!nextTransmissionInboundPortURI.isEmpty();
		this.nextProcessingComponentInboundPortURI =
											nextTransmissionInboundPortURI;
	}

	/**
	 * connect the outbound port of this plug-in to the inbound port of the
	 * next component in the data processing pipeline; this connection cannot be
	 * done within usual plug-in life-cycle methods as we can't know when the
	 * corresponding plug-in in the next component will be ready.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			connect()
	throws Exception
	{
		this.getOwner().doPortConnection(
				this.transmissionOutboundPort.getPortURI(),
				this.nextProcessingComponentInboundPortURI,
				TransmissionConnector.class.getCanonicalName());
	}

	/**
	 * transmit a piece of data to the next component in the data processing
	 * pipeline.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param d				the piece of data to be transmitted.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			transmit(PipelineDataI d) throws Exception
	{
		this.transmissionOutboundPort.transmit(d);
	}
}
// -----------------------------------------------------------------------------
