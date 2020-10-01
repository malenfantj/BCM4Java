package fr.sorbonne_u.components.examples.cps.components;

//Copyright Jacques Malenfant, Sorbonne Universite.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.examples.cps.interfaces.ValueProvidingCI;
import fr.sorbonne_u.components.examples.cps.interfaces.ports.ValueProvidingInboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

/**
 * The class <code>ValueProvider</code> defines a component implementing
 * the integer value providing service defined by the interface
 * <code>ValueProvidingI</code> by returning a random <code>int</code>
 * value.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-03-17</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// -----------------------------------------------------------------------------
@OfferedInterfaces(offered = {ValueProvidingCI.class})
public class			RandomValueProvider
extends		AbstractComponent
//-----------------------------------------------------------------------------
{
	protected final RandomGenerator				rg ;
	protected final ValueProvidingInboundPort	inboundPort ;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * create a value provider with the given URI for its value providing
	 * inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	valueProvidingInboundPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param valueProvidingInboundPortURI	URI to be used to create the inbound port.
	 * @throws Exception						<i>todo.</i>
	 */
	protected			RandomValueProvider(
		String valueProvidingInboundPortURI
		) throws Exception
	{
		// only one thread to ensure the serialised execution of services
		// inside the component.
		super(1, 0) ;
		assert	valueProvidingInboundPortURI != null ;

		this.rg = new Well19937c() ;
		this.inboundPort =
			new ValueProvidingInboundPort(valueProvidingInboundPortURI,
										 this) ;
		this.inboundPort.publishPort() ;

		this.getTracer().setTitle("RandomValueProvider") ;
		this.getTracer().setRelativePosition(1, 1) ;

		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
	}

	/**
	 * create a value provider with the given URI for its value providing
	 * inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	valueProvidingInboundPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI		URI of the component reflection inbound port.
	 * @param valueProvidingInboundPortURI	URI to be used to create the inbound port.
	 * @throws Exception						<i>todo.</i>
	 */
	protected			RandomValueProvider(
		String reflectionInboundPortURI,
		String valueProvidingInboundPortURI
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0);

		this.rg = new Well19937c() ;
		this.inboundPort =
			new ValueProvidingInboundPort(valueProvidingInboundPortURI,
										 this) ;
		this.inboundPort.publishPort();

		this.getTracer().setTitle("RandomValueProvider") ;
		this.getTracer().setRelativePosition(1, 1) ;
	}

	// ------------------------------------------------------------------------
	// Component life-cycle
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		// the shutdown is a good place to unpublish inbound ports.
		try {
			this.inboundPort.unpublishPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		};
		super.shutdown();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdownNow()
	 */
	@Override
	public void			shutdownNow() throws ComponentShutdownException
	{
		// the shutdown is a good place to unpublish inbound ports.
		try {
			this.inboundPort.unpublishPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		};
		super.shutdownNow();
	}

	// ------------------------------------------------------------------------
	// Services
	// ------------------------------------------------------------------------

	/**
	 * return a random integer value after waiting 100 milliseconds.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return			a random integer value.
	 * @throws Exception	<i>todo.</i>
	 */
	public int			returnIntegerValue() throws Exception
	{
		// wait to make clear that the caller is released before the
		// integer value is returned.
		Thread.sleep(100L) ;
		return this.rg.nextInt() ;
	}
}
