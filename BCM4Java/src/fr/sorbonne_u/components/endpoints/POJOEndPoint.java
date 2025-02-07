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

import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
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

	// transient here is used to signify that the data is not sharable; indeed
	// a POJOEndPoint is not meant to be serialised, hence the declaration has
	// no sense from the Java point of view.

	/** when true, the client has initialised the end point, false otherwise.*/
	protected transient boolean		clientSideInitialised;

	/** direct reference to the POJO.										*/
	protected transient I			reference;

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
		POJOEndPoint<?> instance
		)
	{
		assert instance != null : new PreconditionException("instance != null");
		boolean ret = true;
		ret &= EndPoint.implementationInvariants(instance);
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
	protected static boolean	invariants(POJOEndPoint<?> instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= EndPoint.invariants(instance);
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a POJO endpoint.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clientSideInterface != null}
	 * post	{@code !serverSideInitialised()}
	 * post	{@code !clientSideInitialised()}
	 * </pre>
	 *
	 * @param clientSideInterface	the interface implemented by this end point.
	 */
	public				POJOEndPoint(Class<I> clientSideInterface)
	{
		super(clientSideInterface);

		this.reference = null;
		this.clientSideInitialised = false;

		assert	POJOEndPoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"POJOEndPoint.implementationInvariants(this)");
		assert	POJOEndPoint.invariants(this) :
				new InvariantException("POJOEndPoint.invariants(this)");
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
		assert	!clientSideInitialised() :
				new PreconditionException("!clientSideInitialised()");
		assert	serverSideEndPointOwner != null :
				new PreconditionException("serverSideEndPointOwner != null");

		this.reference = (I) serverSideEndPointOwner;

		assert	POJOEndPoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"POJOEndPoint.implementationInvariants(this)");
		assert	POJOEndPoint.invariants(this) :
				new InvariantException("POJOEndPoint.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#clientSideInitialised()
	 */
	@Override
	public boolean		clientSideInitialised()
	{
		return this.clientSideInitialised && this.serverSideInitialised();
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#initialiseClientSide(java.lang.Object)
	 */
	@Override
	public void			initialiseClientSide(Object clientSideEndPointOwner)
	{
		assert	serverSideInitialised() :
				new PreconditionException("serverSideInitialised()");
		assert	!clientSideInitialised() :
				new PreconditionException("!clientSideInitialised()");
		assert	clientSideEndPointOwner != null :
				new PreconditionException("clientSideEndPointOwner != null");

		this.clientSideInitialised = true;

		assert	clientSideInitialised() :
				new PreconditionException("clientSideInitialised()");
		assert	POJOEndPoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"POJOEndPoint.implementationInvariants(this)");
		assert	POJOEndPoint.invariants(this) :
				new InvariantException("POJOEndPoint.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#getClientSideReference()
	 */
	@Override
	public I			getClientSideReference()
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
		assert	serverSideInitialised() :
				new PreconditionException("serverSideInitialised()");
		assert	clientSideInitialised() :
				new PreconditionException("clientSideInitialised()");

		this.clientSideInitialised = false;

		assert	!clientSideInitialised() :
				new PostconditionException("!clientSideInitialised()");
		assert	POJOEndPoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"POJOEndPoint.implementationInvariants(this)");
		assert	POJOEndPoint.invariants(this) :
				new InvariantException("POJOEndPoint.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#cleanUpServerSide()
	 */
	@Override
	public void			cleanUpServerSide()
	{
		assert	serverSideInitialised() :
				new PreconditionException("serverSideInitialised()");
		assert	!clientSideInitialised() :
				new PreconditionException("!clientSideInitialised()");

		this.reference = null;

		assert	!serverSideInitialised() :
				new PostconditionException("!serverSideInitialised()");
		assert	POJOEndPoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"POJOEndPoint.implementationInvariants(this)");
		assert	POJOEndPoint.invariants(this) :
				new InvariantException("POJOEndPoint.invariants(this)");
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
	 * post	{@code return.getClientSideInterface().equals(getClientSideInterface()))}
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
			assert	ret.getClientSideInterface().
											equals(getClientSideInterface()) :
					new PostconditionException(
							"return.getClientSideInterface().equals("
							+ "getClientSideInterface())");

			assert	POJOEndPoint.implementationInvariants(ret) :
					new ImplementationInvariantException(
							"POJOEndPoint.implementationInvariants(ret)");
			assert	POJOEndPoint.invariants(ret) :
					new InvariantException("POJOEndPoint.invariants(ret)");

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
