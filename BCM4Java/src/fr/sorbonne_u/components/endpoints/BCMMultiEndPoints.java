package fr.sorbonne_u.components.endpoints;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to implement
// a simulation of a map-reduce kind of system in BCM4Java.
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

import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.util.ArrayList;

/**
 * The class <code>BCMMultiEndPoints</code> partially implements BCM multiple
 * end points.
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
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2024-07-12</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	BCMMultiEndPoints
extends		MultiEndPoints
implements	BCMMultiEndPointsI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * initialise a BCMMultiEndPoints instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code numberOfEndPoints > 1}
	 * post	{@code !serverSideInitialised()}
	 * post	{@code !clientSideInitialised()}
	 * </pre>
	 *
	 * @param numberOfEndPoints	number of expected end points in this multiple end point.
	 */
	public				BCMMultiEndPoints(int numberOfEndPoints)
	{
		super(numberOfEndPoints);
	}

	/**
	 * create a new multiple end points with the given end points; to be used
	 * to implement the method {@code copyWithSharable}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code initialEndPoints != null}
	 * pre	{@code initialEndPoints.stream().allMatch(e -> e instanceof BCMEndPoint<?>)}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param initialEndPoints	end points to be put in the multiple end points.
	 */
	protected			BCMMultiEndPoints(
		ArrayList<EndPointI<?>> initialEndPoints
		)
	{
		super(initialEndPoints);

		assert	initialEndPoints.stream().
									allMatch(e -> e instanceof BCMEndPoint<?>) :
			new PreconditionException(
					"initialEndPoints.stream()."
					+ "allMatch(e -> e instanceof BCMEndPoint<?>)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.endpoints.MultiEndPoints#getEndPoint(java.lang.Class)
	 */
	@Override
	public <I> EndPointI<I>	getEndPoint(Class<I> inter)
	{
		assert	RequiredCI.class.isAssignableFrom(inter) :
				new PreconditionException(
						"RequiredCI.class.isAssignableFrom(inter)");

		EndPointI<I> res = super.getEndPoint(inter);

		assert	BCMEndPoint.class.isAssignableFrom(res.getClass()) :
				new PostconditionException(
						"BCMEndPoint.class.isAssignableFrom(res.getClass())");

		return res;
	}
}
