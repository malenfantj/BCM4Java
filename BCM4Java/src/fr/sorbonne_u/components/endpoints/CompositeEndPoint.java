package fr.sorbonne_u.components.endpoints;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.util.ArrayList;

/**
 * The class <code>CompositeEndPoint</code> implements generic composite of
 * several end points connecting the same client and server.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * A composite end point contains several end points that can be retrieved
 * to get their references to be used by the client to call the server.
 * Methods inherited from {@code AbstractEndPointI} are usually applied to
 * all of the end points in the composite.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code numberOfEndPoints > 1}
 * invariant	{@code endPointsMap.size() == numberOfEndPoints || !serverSideInitialised() && !clientSideInitialised()}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2024-07-11</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			CompositeEndPoint
implements	CompositeEndPointI,
			Cloneable
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
	public				CompositeEndPoint(int numberOfEndPoints)
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
	protected			CompositeEndPoint(
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
	// Methods from AbstractEndPointI
	// -------------------------------------------------------------------------

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

	// -------------------------------------------------------------------------
	// Methods added by MultiEndPointsI
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.endpoints.CompositeEndPointI#complete()
	 */
	public boolean		complete()
	{
		return this.endPointsMap.size() == this.numberOfEndPoints;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.CompositeEndPointI#getEndPoint(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <I,J extends I> EndPointI<J>	getEndPoint(Class<I> inter)
	{
		assert	inter != null : new PreconditionException("inter != null");
		assert	clientSideInitialised() :
				new PreconditionException("clientSideInitialised()");

		assert	hasCompatibleInterface(inter) :
				new RuntimeException("hasImplementedInterface(inter)");

		Class<?> found  = null;
		EndPointI<J> res = null;
		for (Entry<Class<?>,EndPointI<?>> i : this.endPointsMap.entrySet()) {
			if (inter.isAssignableFrom(i.getKey())) {
				if (found == null || found != i.getKey() &&
									 found.isAssignableFrom(i.getKey())) {
					found = i.getKey();
					res = (EndPointI<J>) i.getValue();
				}
			}
		}
		return res;
	}

	/**
	 * return a copy of this composite end point with only the sharable
	 * information.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * In a {@code CompositeEndPoint}, the following elements are considered as
	 * sharable:
	 * </p>
	 * <ul>
	 * <li>the number of end points in the composite;</li>
	 * <li>all of the end points with their sharable information only.</li>
	 * </ul>
	 * <p>
	 * By default, this implementation clone  (<i>i.e.</i>, shallow copy) the
	 * composite end point, clear references to the end points and replace them
	 * by fresh references to copies with sharable information of each end point
	 * (by calling {@code copyWithSharable} on each of the ones included in this
	 * composite). If a subclass introduces more non sharable elements, it will
	 * have to redefine the method to avoid copying these.
	 * </p>
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.endpoints.CompositeEndPointI#copyWithSharable()
	 */
	@Override
	public CompositeEndPointI	copyWithSharable()
	{
		try {
			CompositeEndPoint ret = (CompositeEndPoint) this.clone();
			ret.endPointsMap.clear();
			for (Entry<Class<?>,EndPointI<?>> e : this.endPointsMap.entrySet()) {
				ret.endPointsMap.put(e.getKey(),
									 e.getValue().copyWithSharable());
			}
			return ret;
		} catch (CloneNotSupportedException e1) {
			throw new RuntimeException(e1) ;
		}
	}

	// -------------------------------------------------------------------------
	// Internal methods
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
	 * @return		true if an end point in this multiple end points is {@code inter} and false otherwise.
	 */
	protected boolean	hasImplementedInterface(Class<?> inter)
	{
		assert	inter != null : new PreconditionException("inter != null");

		return this.endPointsMap.containsKey(inter);
	}

	/**
	 * return true if an end point in this multiple end points implements
	 * {@code inter} or a subclass of it and false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inter != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param inter	an interface to be tested.
	 * @return	true if an end point in this multiple end points implements {@code inter} or a subclass of it and false otherwise.
	 */
	protected boolean	hasCompatibleInterface(Class<?> inter)
	{
		assert	inter != null : new PreconditionException("inter != null");

		return this.endPointsMap.keySet().stream().anyMatch(
											i -> i.isAssignableFrom(inter));
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
	 * @param <I>	the type of the interface.
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
	 * return the end point compatible with {@code inter}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inter != null}
	 * pre	{@code hasCompatibleInterface(inter)}
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @param <I>	type of requested interface.
	 * @param <J>	type of interface proposed by the end point.
	 * @param inter	interface implemented by the sought end point. 
	 * @return		the end point implementing {@code inter}.
	 */
	@SuppressWarnings("unchecked")
	protected <I, J extends I> EndPoint<J>	retrieveCompatibleEndPoint(
		Class<I> inter
		)
	{
		assert	inter != null : new PreconditionException("inter != null");
		assert	hasCompatibleInterface(inter) :
				new PreconditionException("hasCompatibleInterface(inter)");

		Class<?> found  = null;
		EndPoint<J> res = null;
		for (Entry<Class<?>,EndPointI<?>> i : this.endPointsMap.entrySet()) {
			if (inter.isAssignableFrom(i.getKey())) {
				if (found == null || found != i.getKey() &&
									 found.isAssignableFrom(i.getKey())) {
					found = i.getKey();
					res = (EndPoint<J>) i.getValue();
				}
			}
		}
		return res;
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
}