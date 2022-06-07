package fr.sorbonne_u.components.examples.dpp.components;

import fr.sorbonne_u.components.examples.dpp.connections.TransmissionOutBoundPort;
import fr.sorbonne_u.components.examples.dpp.interfaces.ProcessingI;
import fr.sorbonne_u.components.examples.dpp.interfaces.TransmissionCI;

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

import fr.sorbonne_u.components.exceptions.ComponentShutdownException;

// -----------------------------------------------------------------------------
/**
 * The abstract class <code>ProcessingComponent</code> implements a data
 * processing component in a data processing pipeline.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}
 * </pre>
 * 
 * <p>Created on : 2022-06-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	ProcessingComponent
extends		DataSink
implements	ProcessingI
{
	// -------------------------------------------------------------------------
	// Variables and constants
	// -------------------------------------------------------------------------

	/** outbound port through which the component transmits data.			*/
	protected TransmissionOutBoundPort	tramissionOutboundPort;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a processing component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code nbThreads >= 0}
	 * pre	{@code nbSchedulableThreads >= 0}
	 * pre	{@code nbThreads + nbSchedulableThreads > 0}
	 * pre	{@code transmissionInboundPortURI != null}
	 * pre	{@code !transmissionInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param nbThreads						number of standard threads.
	 * @param nbSchedulableThreads			number of schedulable threads.
	 * @param transmissionInboundPortURI	URI of the inbound port through which the component receives data.
	 * @throws Exception 					<i>to do</i>.
	 */
	protected			 ProcessingComponent(
		int nbThreads,
		int nbSchedulableThreads,
		String transmissionInboundPortURI
		) throws Exception
	{
		super(nbThreads, nbSchedulableThreads, transmissionInboundPortURI);
	}

	/**
	 * create a processing component with the specified reflection inbound
	 * port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code reflectionInboundPortURI != null}
	 * pre	{@code !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code nbThreads >= 0}
	 * pre	{@code nbSchedulableThreads >= 0}
	 * pre	{@code nbThreads + nbSchedulableThreads > 0}
	 * pre	{@code transmissionInboundPortURI != null}
	 * pre	{@code !transmissionInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI		URI of the reflection inbound port of the component.
	 * @param nbThreads						number of standard threads.
	 * @param nbSchedulableThreads			number of schedulable threads.
	 * @param transmissionInboundPortURI	URI of the inbound port through which the component receives data.
	 * @throws Exception 					<i>to do</i>.
	 */
	protected 			ProcessingComponent(
		String reflectionInboundPortURI,
		int nbThreads,
		int nbSchedulableThreads,
		String transmissionInboundPortURI
		) throws Exception
	{
		super(reflectionInboundPortURI, nbThreads, nbSchedulableThreads,
			  transmissionInboundPortURI);
	}

	/**
	 *  initialise the processor component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code transmissionInboundPortURI != null}
	 * pre	{@code !transmissionInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param transmissionInboundPortURI	URI of the inbound port through which the component receives data.
	 * @throws Exception					<i>to do</i>.
	 */
	protected void		initialise(String transmissionInboundPortURI)
	throws Exception
	{
		super.initialise(transmissionInboundPortURI);

		this.addRequiredInterface(TransmissionCI.class);
		this.tramissionOutboundPort = new TransmissionOutBoundPort(this);
		this.tramissionOutboundPort.publishPort();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		if (this.tramissionOutboundPort.connected()) {
			this.doPortDisconnection(this.tramissionOutboundPort.getPortURI());
		}
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.tramissionOutboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
