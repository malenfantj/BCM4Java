package fr.sorbonne_u.components.examples.dpp.example;

import fr.sorbonne_u.components.examples.dpp.components.DataSource;
import fr.sorbonne_u.components.examples.dpp.connections.TransmissionConnector;
import fr.sorbonne_u.components.exceptions.ComponentStartException;

// -----------------------------------------------------------------------------
/**
 * The class <code>Source</code> implements a simple data source component in a
 * data processing pipeline, by extending the component abstrcat class
 * {@code DataSource}.
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
public class			Source
extends		DataSource
{
	// -------------------------------------------------------------------------
	// Variables and constants
	// -------------------------------------------------------------------------

	/** URI of the inbound port of the first component in the data
	 *  processing pipeline.												*/
	protected String	firstProcessingComponentInboundPortURI;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a data source component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code firstProcessingComponentInboundPortURI != null}
	 * pre	{@code !firstProcessingComponentInboundPortURI.isEmpty()}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param firstProcessingComponentInboundPortURI	URI of the inbound port of the first component in the data processing pipeline.
	 * @throws Exception								<i>to do</i>.
	 */
	protected			Source(String firstProcessingComponentInboundPortURI)
	throws Exception
	{
		super(1, 0);

		assert	firstProcessingComponentInboundPortURI != null;
		assert	!firstProcessingComponentInboundPortURI.isEmpty();

		this.firstProcessingComponentInboundPortURI =
								firstProcessingComponentInboundPortURI;
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
		try {
			this.doPortConnection(
					this.transmissionOutboundPort.getPortURI(),
					this.firstProcessingComponentInboundPortURI,
					TransmissionConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
		super.start();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		for (int i = 0 ; i < 10 ; i++) {
			this.transmissionOutboundPort.transmit(new ExamplePipelineData(i));
		}
	}
}
// -----------------------------------------------------------------------------
