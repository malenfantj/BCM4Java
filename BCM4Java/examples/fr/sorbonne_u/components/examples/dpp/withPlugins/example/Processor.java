package fr.sorbonne_u.components.examples.dpp.withPlugins.example;

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
import fr.sorbonne_u.components.examples.dpp.example.ExamplePipelineData;
import fr.sorbonne_u.components.examples.dpp.interfaces.PipelineDataI;
import fr.sorbonne_u.components.examples.dpp.interfaces.ProcessingI;
import fr.sorbonne_u.components.examples.dpp.withPlugins.plugins.EmitterPlugin;
import fr.sorbonne_u.components.examples.dpp.withPlugins.plugins.ProcessorPlugin;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.utils.URIGenerator;

// -----------------------------------------------------------------------------
/**
 * The class <code>Processor</code> implements a simple processor component
 * by using the plug-in {@code ProcessorPlugin} to accept and process the
 * pieces of data and {@code EmitterPlugin} to transmit the results to the next
 * component in the data processing pipeline.
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
public class			Processor
extends		AbstractComponent
implements	ProcessingI
{
	// -------------------------------------------------------------------------
	// Variables and constants
	// -------------------------------------------------------------------------

	/** URI	of the emitter plug-in.											*/
	protected String	emitterPluginURI;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a processor component in a data processing pipeline.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code transmissionInboundPortURI != null}
	 * pre	{@code !transmissionInboundPortURI.isEmpty()}
	 * pre	{@code nextTransmissionInboundPortURI != null}
	 * pre	{@code !nextTransmissionInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param transmissionInboundPortURI			URI of the inbound port of this component.
	 * @param nextProcessingComponentInboundPortURI	URI of the inbound port of the next component in the data processing pipeline.
	 * @throws Exception							<i>todo</i>.
	 */
	protected			Processor(
		String transmissionInboundPortURI,
		String nextProcessingComponentInboundPortURI
		) throws Exception
	{
		super(1, 0);
		ProcessorPlugin pplugin = new ProcessorPlugin();
		pplugin.setPluginURI(URIGenerator.generateURI());
		this.installPlugin(pplugin);
		pplugin.createAndPublishInboundPort(transmissionInboundPortURI);
		EmitterPlugin eplugin = new EmitterPlugin();
		this.emitterPluginURI = URIGenerator.generateURI();
		eplugin.setPluginURI(this.emitterPluginURI);
		eplugin.setNextInboundPortURI(nextProcessingComponentInboundPortURI);
		this.installPlugin(eplugin);
		pplugin.linkToEmitterPlugin(eplugin);
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
			((EmitterPlugin)this.getPlugin(this.emitterPluginURI)).connect();
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
		super.start();
	}

	// -------------------------------------------------------------------------
	// Component specific methods and services
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.examples.dpp.interfaces.ProcessingI#process(fr.sorbonne_u.components.examples.dpp.interfaces.PipelineDataI)
	 */
	@Override
	public PipelineDataI	process(PipelineDataI d) throws Exception
	{
		ExamplePipelineData epd = ((ExamplePipelineData)d);
		int currentValue;
		if (epd.isResultSet()) {
			currentValue = epd.getResult();
		} else {
			currentValue = epd.getInitialParameter();
		}
		return epd.setResult(currentValue * 100);
	}
}
// -----------------------------------------------------------------------------
