package fr.sorbonne_u.components.examples.cedp_cs.connections;

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
import fr.sorbonne_u.components.endpoints.BCMEndPoint;
import fr.sorbonne_u.components.examples.cedp_cs.interfaces.MultipleURIConsumerCI;
import fr.sorbonne_u.components.examples.cedp_cs.interfaces.MultipleURIProviderCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>MultipleURIServiceEndpoint</code> implements the BCM endpoint
 * for the multiple URI service in the basic client/server example.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-01-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			MultipleURIServiceEndpoint
extends BCMEndPoint<MultipleURIConsumerCI>
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(
		MultipleURIServiceEndpoint instance
		)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
//		ret &= InvariantChecking.checkImplementationInvariant(invariantExpression, MultipleURIServiceEndpoint.class,
//				instance, "");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(MultipleURIServiceEndpoint instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
//		ret &= InvariantChecking.checkInvariant(invariantExpression, MultipleURIServiceEndpoint.class, instance, "");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a URI service end point with the given inbound port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param inboundPortURI	URI of the inbound port providing the offered interface.
	 */
	public					MultipleURIServiceEndpoint(
		String inboundPortURI
		)
	{
		super(MultipleURIConsumerCI.class, MultipleURIProviderCI.class,
			  inboundPortURI);
		
		// Invariant checking
		assert	MultipleURIServiceEndpoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"MultipleURIServiceEndpoint.implementationInvariants(this)");
		assert	MultipleURIServiceEndpoint.invariants(this) :
				new InvariantException(
						"MultipleURIServiceEndpoint.invariants(this)");
		
	}

	/**
	 * create a URI service end point with a generated inbound port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public					MultipleURIServiceEndpoint()
	{
		super(MultipleURIConsumerCI.class, MultipleURIProviderCI.class);
		
		// Invariant checking
		assert	MultipleURIServiceEndpoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"MultipleURIServiceEndpoint.implementationInvariants(this)");
		assert	MultipleURIServiceEndpoint.invariants(this) :
				new InvariantException(
						"MultipleURIServiceEndpoint.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPoint#makeInboundPort(fr.sorbonne_u.components.AbstractComponent, java.lang.String)
	 */
	@Override
	protected AbstractInboundPort	makeInboundPort(
		AbstractComponent c,
		String inboundPortURI
		) throws Exception
	{
		// Preconditions checking
		assert	c != null : new PreconditionException("c != null");
		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");

		MultipleURIProviderInboundPort p =
				new MultipleURIProviderInboundPort(inboundPortURI, c);
		p.publishPort();

		// Postconditions checking
		assert	p != null && p.isPublished() :
				new PostconditionException(
						"return != null && return.isPublished()");
		assert	((AbstractPort)p).getPortURI().equals(inboundPortURI) :
				new PostconditionException(
						"((AbstractPort)return).getPortURI().equals(inboundPortURI)");
		assert	getServerSideInterface().isAssignableFrom(p.getClass()) :
				new PostconditionException(
						"getOfferedComponentInterface()."
						+ "isAssignableFrom(return.getClass())");
		// Invariant checking
		assert	MultipleURIServiceEndpoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"MultipleURIServiceEndpoint.implementationInvariants(this)");
		assert	MultipleURIServiceEndpoint.invariants(this) :
				new InvariantException(
						"MultipleURIServiceEndpoint.invariants(this)");
		
		return p;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPoint#makeOutboundPort(fr.sorbonne_u.components.AbstractComponent, java.lang.String)
	 */
	@Override
	protected MultipleURIConsumerCI	makeOutboundPort(
		AbstractComponent c,
		String inboundPortURI
		) throws Exception
	{
		// Preconditions checking
		assert	c != null : new PreconditionException("c != null");

		MultipleURIConsumerOutboundPort p =
				new MultipleURIConsumerOutboundPort(c);
		p.publishPort();
		c.doPortConnection(
				p.getPortURI(),
				inboundPortURI,
				MultipleURIServiceConnector.class.getCanonicalName());

		// Postconditions checking
		assert	p != null && p.isPublished() && p.connected() :
				new PostconditionException(
						"return != null && return.isPublished() && "
						+ "return.connected()");
		assert	((AbstractPort)p).getServerPortURI().equals(getInboundPortURI()) :
				new PostconditionException(
						"((AbstractPort)return).getServerPortURI()."
						+ "equals(getInboundPortURI())");
		assert	getClientSideInterface().isAssignableFrom(p.getClass()) :
				new PostconditionException(
						"getImplementedInterface().isAssignableFrom("
						+ "return.getClass())");
		
		// Invariant checking
		assert	implementationInvariants(this) :
				new ImplementationInvariantException(
						"implementationInvariants(this)");
		assert	invariants(this) : new InvariantException("invariants(this)");
		
		return p;
	}
}
// -----------------------------------------------------------------------------
