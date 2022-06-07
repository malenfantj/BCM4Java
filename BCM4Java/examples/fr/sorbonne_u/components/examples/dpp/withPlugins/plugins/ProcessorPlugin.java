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

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.examples.dpp.interfaces.PipelineDataI;
import fr.sorbonne_u.components.examples.dpp.interfaces.ProcessingI;

// -----------------------------------------------------------------------------
/**
 * The class <code>ProcessorPlugin</code> implements the functionalities that
 * are required by components that act as processors in the data processing
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
public class			ProcessorPlugin
extends		SinkPlugin
{
	// -------------------------------------------------------------------------
	// Variables and constants
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** reference to the emitter plug-in that will be used to transmit the
	 *  the result of the processing to the next component in the pipeline.	*/
	protected EmitterPlugin	emitterPlugin;

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
	public				ProcessorPlugin()
	{
	}

	// -------------------------------------------------------------------------
	// Plug-in life-cycle
	// -------------------------------------------------------------------------

	/**
	 * install the plug-in on a component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner instanceof ProcessingI}
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.examples.dpp.withPlugins.plugins.SinkPlugin#installOn(fr.sorbonne_u.components.ComponentI)
	 */
	@Override
	public void			installOn(ComponentI owner) throws Exception
	{
		assert	owner instanceof ProcessingI;
		super.installOn(owner);
	}

	// -------------------------------------------------------------------------
	// Plug-in specific methods and services
	// -------------------------------------------------------------------------

	/**
	 * link this plug-in with the emitter plug-in also installed on the
	 * component owning this plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code emitterPlugin != null}
	 * pre	{@code getOwner().isInstalled(emitterPlugin.getPluginURI())}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param emitterPlugin	the emitter plug-in to be referenced.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			linkToEmitterPlugin(EmitterPlugin emitterPlugin)
	throws Exception
	{
		assert	emitterPlugin != null;
		assert	this.getOwner().isInstalled(emitterPlugin.getPluginURI());

		this.emitterPlugin = emitterPlugin;
	}

	/**
	 * @see fr.sorbonne_u.components.examples.dpp.withPlugins.plugins.SinkPlugin#accept(fr.sorbonne_u.components.examples.dpp.interfaces.PipelineDataI)
	 */
	@Override
	public void			accept(PipelineDataI d) throws Exception
	{
		PipelineDataI r = ((ProcessingI)this.getOwner()).process(d);
		this.emitterPlugin.transmit(r);
	}
}
// -----------------------------------------------------------------------------
