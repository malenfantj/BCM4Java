package fr.sorbonne_u.utils.aclocks;

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

import java.time.Instant;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>ClocksServerInboundPort</code> implements the inbound port
 * for the component interface {@code ClocksServerCI}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code getOwner() instanceof ClocksServer}
 * </pre>
 * 
 * <p>Created on : 2022-11-15</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			ClocksServerInboundPort
extends		AbstractInboundPort
implements	ClocksServerCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner instanceof ClocksServer}
	 * post	{@code getOwner() instanceof ClocksServer}
	 * </pre>
	 *
	 * @param owner			component owning the port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				ClocksServerInboundPort(ComponentI owner)
	throws Exception
	{
		super(ClocksServerCI.class, owner);
		assert	owner instanceof ClocksServer :
				new PreconditionException("owner instanceof ClocksServer");
	}

	/**
	 * create the inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner instanceof ClocksServer}
	 * post	{@code getOwner() instanceof ClocksServer}
	 * </pre>
	 *
	 * @param uri			URI of the port.
	 * @param owner			component owning the port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				ClocksServerInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, ClocksServerCI.class, owner);
		assert	owner instanceof ClocksServer :
				new PreconditionException("owner instanceof ClocksServer");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * the call is handled in a synchronous way, hence when the caller receives
	 * the result, the clock is immediately accessible to other components.
	 * 
	 * @see fr.sorbonne_u.utils.aclocks.ClocksServerCI#createClock(java.lang.String, long, java.time.Instant, double)
	 */
	@Override
	public AcceleratedClock	createClock(
		String clockURI,
		long unixEpochStartTimeInNanos,
		Instant startInstant,
		double accelerationFactor
		) throws Exception
	{
		return this.getOwner().handleRequest(
									o -> ((ClocksServer)o).
											createClock(
													clockURI,
													unixEpochStartTimeInNanos,
													startInstant,
													accelerationFactor));
	}

	/**
	 * @see fr.sorbonne_u.utils.aclocks.ClocksServerCI#getClock(java.lang.String)
	 */
	@Override
	public AcceleratedClock	getClock(String clockURI) throws Exception
	{
		// As getClock may block if the clock has not been created yet, the
		// call is made using the caller thread hence avoiding the need for
		// an unbounded number of internal threads in the component to take
		// care of an unlimited number of callers.
		return ((ClocksServer)this.getOwner()).getClock(clockURI);
	}
}
// -----------------------------------------------------------------------------
