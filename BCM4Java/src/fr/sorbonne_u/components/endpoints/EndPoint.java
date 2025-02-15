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

import fr.sorbonne_u.components.AbstractEndPoint;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The abstract class <code>EndPoint</code> provides a baseline partial
 * implementation of an end point, to be extended to fully implement actual
 * implementation-dependent end points.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This partial implementation is thread safe.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code clientSideInterface != null}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p>Created on : 2024-07-12</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	EndPoint<I>
extends		AbstractEndPoint
implements	EndPointI<I>
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	// Sharable information

	/** the interface implemented by this end point.						*/
	protected final Class<I>	clientSideInterface;

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
		EndPoint<?> instance
		)
	{
		assert instance != null : new PreconditionException("instance != null");
		boolean ret = true;
		ret &= InvariantChecking.checkImplementationInvariant(
					instance.clientSideInterface != null,
					EndPoint.class, instance,
					"clientSideInterface != null");
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
	protected static boolean	invariants(EndPoint<?> instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * initialise this end point.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clientSideInterface != null}
	 * post	{@code !serverSideInitialised()}
	 * post	{@code !clientSideInitialised()}
	 * </pre>
	 *
	 * @param clientSideInterface	the interface proposed to the client by this end point.
	 */
	public				EndPoint(Class<I> clientSideInterface)
	{
		assert	clientSideInterface != null :
				new PreconditionException("clientSideInterface != null");

		this.clientSideInterface = clientSideInterface;

		assert	!serverSideInitialised() :
				new PostconditionException("!serverSideInitialised()");
		assert	!clientSideInitialised() :
				new PostconditionException("!clientSideInitialised()");

		assert	EndPoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"EndPoint.implementationInvariants(this)");
		assert	EndPoint.invariants(this) :
				new InvariantException("EndPoint.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#getClientSideInterface()
	 */
	@Override
	public Class<I>		getClientSideInterface()
	{
		return this.clientSideInterface;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String		toString()
	{
		StringBuffer res = new StringBuffer();
		this.toStringBuffer(res);
		return res.toString();
	}

	/**
	 * append to the string buffer a description of this object.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code sb != null}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sb	a string buffer to add to.
	 */
	public void			toStringBuffer(StringBuffer sb)
	{
		sb.append(this.getClass().getSimpleName());
		sb.append('[');
		this.addLocalContentToStringBuffer(sb);
		sb.append(']');
	}

	/**
	 * add the local content to the given string buffer.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code sb != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sb	a string buffer to which the content will be added.
	 */
	protected void		addLocalContentToStringBuffer(StringBuffer sb)
	{
		assert	sb != null : new PreconditionException("sb != null");

		sb.append(this.clientSideInterface.getSimpleName());
	}
}
