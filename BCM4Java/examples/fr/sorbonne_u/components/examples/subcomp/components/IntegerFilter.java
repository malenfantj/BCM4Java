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
import fr.sorbonne_u.components.examples.subcomp.connectors.IntegerProcessingConnector;
import fr.sorbonne_u.components.examples.subcomp.interfaces.IntegerFilteringCI;
import fr.sorbonne_u.components.examples.subcomp.interfaces.IntegerFilteringI;
import fr.sorbonne_u.components.examples.subcomp.interfaces.IntegerProcessingCI;
import fr.sorbonne_u.components.examples.subcomp.ports.IntegerFilteringInboundPort;
import fr.sorbonne_u.components.examples.subcomp.ports.IntegerProcessingOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import java.util.function.Predicate;

// -----------------------------------------------------------------------------
/**
 * The class <code>IntegerFilter</code> implements a component that filters
 * flows of integers.
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
@OfferedInterfaces(offered = {IntegerFilteringCI.class})
@RequiredInterfaces(required = {IntegerProcessingCI.class})
// -----------------------------------------------------------------------------
public class			IntegerFilter
extends		AbstractComponent
implements 	IntegerFilteringI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** the predicate used by the component to filter incoming integers.	*/
	protected Predicate<Integer>			p ;
	/** the inbound port of this component.									*/
	protected IntegerFilteringInboundPort	inPort ;
	/** the outbound port to connect to the processing component.			*/
	protected IntegerProcessingOutboundPort	outPort ;
	/** the URI of the inbound port to which this component is connected.	*/
	protected String						toIBP_URI ;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an integer filtering subcomponent.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	p != null
	 * pre	myInPort_URI != null
	 * pre	toIBP_URI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param p				the predicate used by the component to filter incoming integers.
	 * @param myInPort_URI	the URI of the inbound port of this component.
	 * @param toIBP_URI		the URI of the inbound port of the component to which this one will connect.
	 * @throws Exception	<i>to do</i>.
	 */
	protected			IntegerFilter(
		Predicate<Integer> p,
		String myInPort_URI,
		String toIBP_URI
		) throws Exception
	{
		super(1, 0) ;
		this.initialise(p, myInPort_URI, toIBP_URI) ;

		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
	}

	/**
	 * create an integer filtering subcomponent.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	reflectionInboundPortURI != null
	 * pre	p != null
	 * pre	myInPort_URI != null
	 * pre	toIBP_URI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection inbound port of this component.
	 * @param p				the predicate used by the component to filter incoming integers.
	 * @param myInPort_URI	the URI of the inbound port of this component.
	 * @param toIBP_URI		the URI of the inbound port of the component to which this one will connect.
	 * @throws Exception	<i>to do</i>.
	 */
	protected			IntegerFilter(
		String reflectionInboundPortURI,
		Predicate<Integer> p,
		String myInPort_URI,
		String toIBP_URI
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0) ;
		this.initialise(p, myInPort_URI, toIBP_URI) ;

		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
	}

	/**
	 * initialise the integer filtering component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	p != null
	 * pre	myInPort_URI != null
	 * pre	toIBP_URI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param p				the predicate used by the component to filter incoming integers.
	 * @param myInPort_URI	the URI of the inbound port of this component.
	 * @param toIBP_URI		the URI of the inbound port of the component to which this one will connect.
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		initialise(
		Predicate<Integer> p,
		String myInPort_URI,
		String toIBP_URI
		) throws Exception
	{
		assert	p != null ;
		assert	myInPort_URI != null ;
		assert	toIBP_URI != null ;

		this.p = p ;
		this.toIBP_URI = toIBP_URI ;
		this.inPort = new IntegerFilteringInboundPort(myInPort_URI, this) ;
		this.outPort = new IntegerProcessingOutboundPort(this) ;
		// as a subcomponent, connections will always be local to the JVM
		// running the component
		this.inPort.localPublishPort() ;
		this.outPort.localPublishPort() ;
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
						IntegerProcessingConnector.class.getCanonicalName()) ;
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		this.doPortDisconnection(this.outPort.getPortURI()) ;
		super.finalise() ;
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
		super.shutdown() ;
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
		super.shutdownNow() ;
	}

	// -------------------------------------------------------------------------
	// Service implementation methods 
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.examples.subcomp.interfaces.IntegerFilteringI#filter(int)
	 */
	@Override
	public void			filter(int i) throws Exception
	{
		if (this.p.test(i)) {
			this.outPort.process(i) ;
		}
	}
}
// -----------------------------------------------------------------------------
