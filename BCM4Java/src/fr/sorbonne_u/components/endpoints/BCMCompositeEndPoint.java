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

import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.util.ArrayList;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ConnectionException;

/**
 * The class <code>BCMCompositeEndPoint</code> implements specialisation of
 * generic composite end points to the BCM4Java case.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The only addition to the implementation of generic composite end point
 * is the type verification made as pre- and postconditions in the redefinition
 * of the {@code getEndPoint(Class<I>)} method.
 * </p>
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
 * <p>Created on : 2024-07-12</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	BCMCompositeEndPoint
extends		CompositeEndPoint
implements	BCMCompositeEndPointI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** default serial version UID.											*/
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
		BCMCompositeEndPoint instance
		)
	{
		assert instance != null : new PreconditionException("instance != null");
		boolean ret = true;
		CompositeEndPoint.implementationInvariants(instance);
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
	protected static boolean	invariants(BCMCompositeEndPoint instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		CompositeEndPoint.invariants(instance);
		return ret;
	}

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
	public				BCMCompositeEndPoint(int numberOfEndPoints)
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
	 * pre	{@code initialEndPoints.stream().allMatch(e -> !e.serverSideInitialised())}
	 * pre	{@code initialEndPoints.stream().allMatch(e -> !e.clientSideInitialised())}
	 * pre	{@code initialEndPoints.stream().allMatch(e -> e instanceof BCMEndPoint<?>)}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param initialEndPoints	end points to be put in the multiple end points.
	 */
	protected			BCMCompositeEndPoint(
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
	 * @see fr.sorbonne_u.components.endpoints.CompositeEndPoint#getEndPoint(java.lang.Class)
	 */
	@Override
	public <I, J extends I> EndPointI<J>	getEndPoint(Class<I> inter)
	{
		assert	RequiredCI.class.isAssignableFrom(inter) :
				new PreconditionException(
						"RequiredCI.class.isAssignableFrom(inter)");

		EndPointI<J> res = super.getEndPoint(inter);

		assert	BCMEndPoint.class.isAssignableFrom(res.getClass()) :
				new PostconditionException(
						"BCMEndPoint.class.isAssignableFrom(res.getClass())");

		return res;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.CompositeEndPoint#initialiseServerSide(java.lang.Object)
	 */
	@Override
	public void			initialiseServerSide(Object serverSideEndPointOwner)
	throws ConnectionException
	{
		assert	serverSideEndPointOwner instanceof AbstractComponent :
				new PreconditionException(
						"serverSideEndPointOwner instanceof AbstractComponent");

		super.initialiseServerSide(serverSideEndPointOwner);
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.CompositeEndPoint#initialiseClientSide(java.lang.Object)
	 */
	@Override
	public void			initialiseClientSide(Object clientSideEndPointOwner)
	throws ConnectionException
	{
		assert	clientSideEndPointOwner instanceof AbstractComponent :
				new PreconditionException(
						"clientSideEndPointOwner instanceof AbstractComponent");

		super.initialiseClientSide(clientSideEndPointOwner);
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMCompositeEndPointI#copyWithSharable()
	 */
	@Override
	public BCMCompositeEndPointI	copyWithSharable()
	{
		CompositeEndPointI ret = super.copyWithSharable();

		assert	ret instanceof BCMCompositeEndPointI :
				new RuntimeException(
						new BCMException(
								"ret instanceof BCMCompositeEndPointI"));

		assert	BCMCompositeEndPoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"BCMCompositeEndPoint.implementationInvariants(this)");
		assert	BCMCompositeEndPoint.invariants(this) :
				new InvariantException(
						"BCMCompositeEndPoint.invariants(this)");

		return (BCMCompositeEndPointI) ret;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMCompositeEndPointI#isServerComponent(fr.sorbonne_u.components.AbstractComponent)
	 */
	@Override
	public boolean		isServerComponent(AbstractComponent c)
	{
		assert	serverSideInitialised() :
				new PreconditionException("serverSideInitialised()");
		
		return this.endPointsMap.values().stream().allMatch(
							e -> ((BCMEndPoint<?>)e).isServerComponent(c));
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMCompositeEndPointI#isClientComponent(fr.sorbonne_u.components.AbstractComponent)
	 */
	@Override
	public boolean		isClientComponent(AbstractComponent c)
	{
		assert	clientSideInitialised() :
				new PreconditionException("clientSideInitialised()");
		
		return this.endPointsMap.values().stream().allMatch(
							e -> ((BCMEndPoint<?>)e).isClientComponent(c));
	}
}
// -----------------------------------------------------------------------------