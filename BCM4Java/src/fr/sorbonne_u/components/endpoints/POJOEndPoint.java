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

import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>POJOEndPoint</code> defines a plain old Java
 * object (POJO) reference implementing in the Java sense the interface
 * {@code I} as an end point.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * A POJO end point merely uses the reference to the Java server object as the
 * reference to be used by the client to call it. As an end point, it only adds
 * the explicit knowledge of the implemented interface inherited from
 * {@code EndPoint<I>}.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p>Created on : 2024-06-25</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			POJOEndPoint<I>
extends		EndPoint<I>
implements	Cloneable
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** direct reference to the POJO.										*/
	protected I			reference;
	/** true if the client side has performed the initialisation, false
	 *  otherwise.															*/
	protected boolean	clientSideInitialised;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a POJO endpoint.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code implementedInterface != null}
	 * post	{@code !serverSideInitialised()}
	 * post	{@code !clientSideInitialised()}
	 * </pre>
	 *
	 * @param implementedInterface	the interface implemented by this end point.
	 */
	public				POJOEndPoint(Class<I> implementedInterface)
	{
		super(implementedInterface);

		this.reference = null;
		this.clientSideInitialised = false;
	}

	/**
	 * create a POJO endpoint from the given Java object reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code implementedInterface != null}
	 * pre	{@code reference != null}
	 * post	{@code serverSideInitialised()}
	 * post	{@code !clientSideInitialised()}
	 * </pre>
	 *
	 * @param implementedInterface	the interface implemented by this end point.
	 * @param reference	the reference to a Java object embedded in this POJO end point.
	 */
	private				POJOEndPoint(Class<I> implementedInterface, I reference)
	{
		super(implementedInterface);

		assert	reference != null :
				new PreconditionException("reference != null");

		this.reference = reference;
		this.clientSideInitialised = false;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#serverSideInitialised()
	 */
	@Override
	public boolean		serverSideInitialised()
	{
		return this.reference != null;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#initialiseServerSide(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void			initialiseServerSide(Object serverSideEndPointOwner)
	{
		assert	!serverSideInitialised() :
				new PreconditionException("!serverSideInitialised()");
		assert	serverSideEndPointOwner != null :
				new PreconditionException("serverSideEndPointOwner != null");

		this.reference = (I) serverSideEndPointOwner;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#clientSideInitialised()
	 */
	@Override
	public boolean		clientSideInitialised()
	{
		return this.clientSideInitialised && this.reference != null;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#initialiseClientSide(java.lang.Object)
	 */
	@Override
	public void			initialiseClientSide(Object clientSideEndPointOwner)
	{
		assert	!clientSideInitialised() :
				new PreconditionException("!clientSideInitialised()");

		assert	this.reference != null :
				new RuntimeException(
						"the end point must have an initialised reference!");

		this.clientSideInitialised = true;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#getReference()
	 */
	@Override
	public I			getReference()
	{
		assert	clientSideInitialised() :
				new PreconditionException("clientSideInitialised()");

		return this.reference;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#cleanUpClientSide()
	 */
	@Override
	public void			cleanUpClientSide()
	{
		this.clientSideInitialised = false;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#cleanUpServerSide()
	 */
	@Override
	public void			cleanUpServerSide()
	{
		this.reference = null;
	}

	/**
	 * return a copy of this POJO end point.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * As a POJO end point is meant to represent connections among entities
	 * that reside in the same address space, the reference to the server
	 * side is considered as sharable.
	 * </p>
	 * <p>
	 * By default, this implementation merely clone (<i>i.e.</i>, shallow copy)
	 * the end point. If a subclass introduces non sharable elements, it will
	 * have to redefine the method to avoid copying these.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code ret.getReference().equals(getReference())}
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#copyWithSharable()
	 */
	@Override
	public POJOEndPoint<I>	copyWithSharable()
	{
		try {
			@SuppressWarnings("unchecked")
			POJOEndPoint<I> ret = (POJOEndPoint<I>) this.clone();
			assert	ret.getImplementedInterface().
									equals(this.getImplementedInterface()) :
					new PostconditionException(
							"return.getImplementedInterface().equals("
							+ "getImplementedInterface())");
			assert	ret.getReference().equals(this.getReference()) :
					new PostconditionException(
							"ret.getReference().equals(getReference())");
			return ret;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPoint#addLocalContentToStringBuffer(java.lang.StringBuffer)
	 */
	@Override
	protected void addLocalContentToStringBuffer(StringBuffer sb)
	{
		super.addLocalContentToStringBuffer(sb);
		sb.append(", ");
		sb.append(this.clientSideInitialised);
		sb.append(", ");
		sb.append(this.reference.getClass().getSimpleName());
	}
}
