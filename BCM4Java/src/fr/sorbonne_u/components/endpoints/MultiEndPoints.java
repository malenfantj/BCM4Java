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

import java.util.HashMap;
import java.util.Map;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.util.ArrayList;

/**
 * The class <code>BCMMultiEndPoints</code> partially implements generic multiple
 * end points.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code numberOfEndPoints > 1}
 * invariant	{@code endPointsMap.size() == numberOfEndPoints || !serverSideInitialised() && !clientSideInitialised()}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2024-07-11</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	MultiEndPoints
implements	MultiEndPointsI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	// Sharable information

	/** number of expected end points in this multiple end point.			*/
	protected final int							numberOfEndPoints;
	/** map from implemented interfaces to single end points.				*/
	private final Map<Class<?>,EndPointI<?>>	endPointsMap;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * initialise a multiple end points instance.
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
	public				MultiEndPoints(int numberOfEndPoints)
	{
		assert	numberOfEndPoints > 1 :
				new PreconditionException("numberOfEndPoints > 1");

		this.numberOfEndPoints = numberOfEndPoints;
		this.endPointsMap = new HashMap<>();
	}

	/**
	 * create a new multiple end points with the given end points; to be used
	 * to implement the method {@code copyWithSharable}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code initialEndPoints != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param initialEndPoints	end points to be put in the multiple end points.
	 */
	protected			MultiEndPoints(
		ArrayList<EndPointI<?>> initialEndPoints
		)
	{
		assert	initialEndPoints != null :
				new PreconditionException("initialEndPoints != null");

		this.numberOfEndPoints = initialEndPoints.size();
		this.endPointsMap = new HashMap<>();
		for (EndPointI<?> e : initialEndPoints) {
			this.endPointsMap.put(e.getImplementedInterface(), e);
		}
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * return true if an end point in this multiple end points implements
	 * {@code inter} and false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inter != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param inter	an interface to be tested.
	 * @return		true if an end point in this multiple end points implements {@code inter} and false otherwise.
	 */
	protected boolean	hasImplementedInterface(Class<?> inter)
	{
		assert	inter != null : new PreconditionException("inter != null");

		return this.endPointsMap.containsKey(inter);
	}

	/**
	 * add the given end point to this multiple end points.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code endPoint != null}
	 * pre	{@code !complete()}
	 * pre	{@code !hasImplementedInterface(endPoint.getImplementedInterface())}
	 * post	{@code hasImplementedInterface(endPoint.getImplementedInterface())}
	 * </pre>
	 *
	 * @param endPoint	end point to be added to the multi-end point
	 */
	protected void		addEndPoint(EndPointI<?> endPoint)
	{
		assert	endPoint != null : new PreconditionException("endPoint != null");
		assert	!complete() : new PreconditionException("!complete()");
		assert	!hasImplementedInterface(endPoint.getImplementedInterface()) :
				new PreconditionException(
						"!hasImplementedInterface("
						+ "endPoint.getImplementedInterface())");

		this.endPointsMap.put(endPoint.getImplementedInterface(), endPoint);
	}

	/**
	 * return the end point implementing {@code inter}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inter != null}
	 * pre	{@code hasImplementedInterface(inter)}
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @param <I>	the type if the interface.
	 * @param inter	interface implemented by the sought end point. 
	 * @return		the end point implementing {@code inter}.
	 */
	@SuppressWarnings("unchecked")
	protected <I> EndPoint<I>	retrieveEndPoint(Class<I> inter)
	{
		assert	inter != null : new PreconditionException("inter != null");
		assert	hasImplementedInterface(inter) :
				new PreconditionException("hasImplementedInterface(inter)");

		return (EndPoint<I>) this.endPointsMap.get(inter);
	}

	/**
	 * remove the end point implementing {@code inter} from this multiple end
	 * points.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inter != null}
	 * pre	{@code hasImplementedInterface(inter)}
	 * post	{@code !hasImplementedInterface(inter)}
	 * post	{@code !complete()}
	 * </pre>
	 *
	 * @param inter	interface implemented by the sought end point. 
	 */
	protected void		removeEndPoint(Class<? extends RequiredCI> inter)
	{
		assert	inter != null : new PreconditionException("inter != null");

		this.endPointsMap.remove(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.MultiEndPointsI#complete()
	 */
	public boolean		complete()
	{
		return this.endPointsMap.size() == this.numberOfEndPoints;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.AbstractEndPointI#serverSideInitialised()
	 */
	@Override
	public boolean		serverSideInitialised()
	{
		return	this.complete() &&
				this.endPointsMap.values().stream().
								allMatch(e -> e.serverSideInitialised());
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.AbstractEndPointI#initialiseServerSide(java.lang.Object)
	 */
	@Override
	public void			initialiseServerSide(Object serverSideEndPointOwner)
	{
		assert	serverSideEndPointOwner != null :
				new PreconditionException("serverSideEndPointOwner != null");
		assert	!serverSideInitialised() :
				new PreconditionException("!serverSideInitialised()");
		assert	complete() : new PreconditionException("complete()");

		this.endPointsMap.values().stream().
				forEach(e -> e.initialiseServerSide(serverSideEndPointOwner));
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.AbstractEndPointI#clientSideInitialised()
	 */
	@Override
	public boolean		clientSideInitialised()
	{
		return	this.complete() &&
				this.endPointsMap.values().stream().
									allMatch(e -> e.clientSideInitialised());
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.AbstractEndPointI#initialiseClientSide(java.lang.Object)
	 */
	@Override
	public void			initialiseClientSide(Object clientSideEndPointOwner)
	{
		assert	clientSideEndPointOwner != null :
				new PreconditionException("clientSideEndPointOwner != null");
		assert	!clientSideInitialised() :
				new PreconditionException("!clientSideInitialised()");
		assert	complete() : new PreconditionException("complete()");

		this.endPointsMap.values().stream().
				forEach(e -> e.initialiseClientSide(clientSideEndPointOwner));
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.MultiEndPointsI#getEndPoint(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <I> EndPointI<I>	getEndPoint(Class<I> inter)
	{
		assert	inter != null : new PreconditionException("inter != null");
		assert	clientSideInitialised() :
				new PreconditionException("clientSideInitialised()");

		assert	hasImplementedInterface(inter) :
				new RuntimeException("hasImplementedInterface(inter)");

		return (EndPointI<I>) this.endPointsMap.get(inter);
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.AbstractEndPointI#cleanUpServerSide()
	 */
	@Override
	public void			cleanUpServerSide()
	{
		this.endPointsMap.values().stream().forEach(e -> e.cleanUpServerSide());
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.AbstractEndPointI#cleanUpClientSide()
	 */
	@Override
	public void			cleanUpClientSide()
	{
		this.endPointsMap.values().stream().forEach(e -> e.cleanUpClientSide());
	}
}
