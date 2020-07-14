package fr.sorbonne_u.components.examples.subcomp.components;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// new implementation of the DEVS simulation standard for Java.
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
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.examples.subcomp.connectors.IntegerFilteringConnector;
import fr.sorbonne_u.components.examples.subcomp.interfaces.IntegerFilteringCI;
import fr.sorbonne_u.components.examples.subcomp.interfaces.IntegerProcessingCI;
import fr.sorbonne_u.components.examples.subcomp.interfaces.IntegerProcessingI;
import fr.sorbonne_u.components.examples.subcomp.ports.IntegerFilteringOutboundPort;
import fr.sorbonne_u.components.examples.subcomp.ports.IntegerProcessingInboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;

//------------------------------------------------------------------------------
/**
 * The class <code>PipelineClient</code> implements a simple client for the
 * pipeline component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2020-01-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// -----------------------------------------------------------------------------
@OfferedInterfaces(offered = {IntegerProcessingCI.class})
@RequiredInterfaces(required = {IntegerFilteringCI.class})
// -----------------------------------------------------------------------------
public class			PipelineClient
extends		AbstractComponent
implements	IntegerProcessingI
{
	/** URI of the inbound port of the pipeline component.					*/
	protected String						toIBP_URI ;
	/** outbound port connected to the pipeline component.					*/
	protected IntegerFilteringOutboundPort	outPort ;
	/** inbound port connected from the pipeline component.					*/
	protected IntegerProcessingInboundPort	inPort ;

	/**
	 * create the pipeline client component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	myIBP_URI != null
	 * pre	toIBP_URI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param myIBP_URI		URI of the inbound port of this component.
	 * @param toIBP_URI		URI of the inbound port of the pipeline component.
	 * @throws Exception	<i>to do</i>.
	 */
	protected			PipelineClient(
		String myIBP_URI,
		String toIBP_URI
		) throws Exception
	{
		super(2, 0) ;
		this.initialise(myIBP_URI, toIBP_URI) ;

		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
	}

	/**
	 * create the pipeline client component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	reflectionInboundPortURI != null
	 * pre	myIBP_URI != null
	 * pre	toIBP_URI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection inbound port of this component.
	 * @param myIBP_URI		URI of the inbound port of this component.
	 * @param toIBP_URI		URI of the inbound port of the pipeline component.
	 * @throws Exception	<i>to do</i>.
	 */
	protected			PipelineClient(
		String reflectionInboundPortURI,
		String myIBP_URI,
		String toIBP_URI
		) throws Exception
	{
		super(reflectionInboundPortURI, 2, 0) ;
		this.initialise(myIBP_URI, toIBP_URI) ;

		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
	}

	/**
	 * initialise the pipeline client component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	myIBP_URI != null
	 * pre	toIBP_URI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param myIBP_URI		URI of the inbound port of this component.
	 * @param toIBP_URI		URI of the inbound port of the pipeline component.
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		initialise(
		String myIBP_URI,
		String toIBP_URI
		) throws Exception
	{
		assert	myIBP_URI != null ;
		assert	toIBP_URI != null ;

		this.toIBP_URI = toIBP_URI ;

		this.outPort = new IntegerFilteringOutboundPort(this) ;
		this.outPort.publishPort() ;	

		this.inPort = new IntegerProcessingInboundPort(myIBP_URI, this) ;
		this.inPort.publishPort() ;
	}

	// -------------------------------------------------------------------------
	// Life-cycle methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public void			start() throws ComponentStartException
	{
		super.start() ;

		try {
			this.doPortConnection(
						this.outPort.getPortURI(),
						toIBP_URI,
						IntegerFilteringConnector.class.getCanonicalName()) ;
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		super.execute() ;

		this.outPort.filter(1) ;
		this.outPort.filter(2) ;
		this.outPort.filter(3) ;
		this.outPort.filter(4) ;
		this.outPort.filter(5) ;
		this.outPort.filter(6) ;
		this.outPort.filter(7) ;
		this.outPort.filter(8) ;
		this.outPort.filter(9) ;
		this.outPort.filter(10) ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		this.doPortDisconnection(this.outPort.getPortURI()) ;
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
			this.outPort.unpublishPort() ;
			this.inPort.unpublishPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdownNow()
	 */
	@Override
	public void			shutdownNow() throws ComponentShutdownException
	{
		try {
			this.outPort.unpublishPort() ;
			this.inPort.unpublishPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdownNow();
	}

	// -------------------------------------------------------------------------
	// Service implementation methods 
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.examples.subcomp.interfaces.IntegerProcessingI#process(int)
	 */
	@Override
	public void			process(int i) throws Exception
	{
		System.out.println("result is: " + i) ;
	}
}
//------------------------------------------------------------------------------
