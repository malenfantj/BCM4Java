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
import fr.sorbonne_u.components.examples.subcomp.interfaces.IntegerProcessingCI;
import fr.sorbonne_u.components.examples.subcomp.interfaces.IntegerProcessingI;
import fr.sorbonne_u.components.examples.subcomp.ports.IntegerProcessingInboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import java.util.function.Function;

// -----------------------------------------------------------------------------
/**
 * The class <code>IntegerProcessor</code> implements a component that processes
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
@OfferedInterfaces(offered = {IntegerProcessingCI.class})
@RequiredInterfaces(required = {IntegerProcessingCI.class})
// -----------------------------------------------------------------------------
public class			IntegerProcessor
extends		AbstractComponent
implements	IntegerProcessingI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** the function used to process incoming integers.						*/
	protected Function<Integer,Integer>		process ;
	/** the inbound port of this component.									*/
	protected IntegerProcessingInboundPort	inPort ;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the integer processing subcomponent.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	process != null
	 * pre	myIPibpURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param process		the function used to process incoming integers.
	 * @param myIPibpURI	the URI of the inbound port of this component.
	 * @throws Exception	<i>to do</i>.
	 */
	protected			IntegerProcessor(
		Function<Integer,Integer> process,
		String myIPibpURI
		) throws Exception
	{
		super(1, 0) ;
		this.initialise(process, myIPibpURI) ;

		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
	}

	/**
	 * create the integer processing subcomponent.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	reflectionInboundPortURI != null
	 * pre	process != null
	 * pre	myIPibpURI != null
	 * pre	toIPibpURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection inbound port of this component.
	 * @param process		the function used to process incoming integers.
	 * @param myIPibpURI	the URI of the inbound port of this component.
	 * @throws Exception	<i>to do</i>.
	 */
	protected			IntegerProcessor(
		String reflectionInboundPortURI,
		Function<Integer,Integer> process,
		String myIPibpURI
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0) ;
		this.initialise(process, myIPibpURI) ;

		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
	}

	/**
	 * initialise the integer processing component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	process != null
	 * pre	myIPibpURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param process		the function used to process incoming integers.
	 * @param myIPibpURI	the URI of the inbound port of this component.
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		initialise(
		Function<Integer,Integer> process,
		String myIPibpURI
		) throws Exception
	{
		assert	process != null ;
		assert	myIPibpURI != null ;

		this.process = process ;
		this.inPort = new IntegerProcessingInboundPort(myIPibpURI, this) ;
		// as a subcomponent, connections will always be local to the JVM
		// running the component
		this.inPort.localPublishPort() ;
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
			assert	this.isSubcomponent() ;
			assert	this.getCompositeComponentReference() instanceof
														IntegerProcessingI ;
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
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
		int v = this.process.apply(i) ;
		this.getCompositeComponentReference().runTask(
			new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						((IntegerProcessingI)this.getTaskOwner()).process(v) ;
					} catch (Exception e) {
						e.printStackTrace() ;
					}
				}
			}) ;
	}
}
// -----------------------------------------------------------------------------
