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
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.examples.dpp.withPlugins.plugins.SinkPlugin;

//------------------------------------------------------------------------------
/**
 * The class <code>Sink</code> implements a data sink component in a data
 * processing pipeline using the {@code SinkPlugin} to acquire the required
 * functionality.
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
public class			Sink
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the sink component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code transmissionInboundPortURI != null}
	 * pre	{@code !transmissionInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param transmissionInboundPortURI	URI of the inbound port used to receive data pieces.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			Sink(String transmissionInboundPortURI)
	throws Exception
	{
		super(1, 0);

		assert	transmissionInboundPortURI != null;
		assert	!transmissionInboundPortURI.isEmpty();

		SinkPlugin splugin = new SinkPlugin();
		splugin.setPluginURI(AbstractPort.generatePortURI());
		this.installPlugin(splugin);
		splugin.createAndPublishInboundPort(transmissionInboundPortURI);
	}
}
// -----------------------------------------------------------------------------
