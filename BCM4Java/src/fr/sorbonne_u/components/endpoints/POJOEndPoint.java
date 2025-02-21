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
import java.util.HashMap;
import java.util.Map;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.exceptions.ConnectionException;

/**
 * The class <code>POJOEndPoint</code> defines a plain old Java object (POJO)
 * reference implementing in the Java sense the interface {@code I} as an end
 * point.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * A POJO end point is an abstraction of a standard Java reference but
 * conforming to the end point protocols, so this implementation merely uses the
 * reference to the Java server object as the reference to be used by the client
 * to call it. As an end point, it only adds the explicit knowledge of the
 * implemented interface inherited from {@code EndPoint<I>}. 
 * </p>
 * <p>
 * This implementation is not thread safe.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code endPointURI != null && !endPointURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code GLOBAL_ENDPOINT_REFERENCES != null}
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

	/** map from end point URIs to Java object references to server objects;
	 *  this map is neede to respect the same protocol as other end points
	 *  <i>i.e.</i>, using {@code copyWithSharable} to pass end points both
	 *  to the server and the client sides and then
	 *  {@code initialiseServerSide} before {@code initialiseClientSide}
	 *  which must retrieve the Java server object reference.			  	*/
	protected static final Map<String,Object>	GLOBAL_ENDPOINT_REFERENCES;

	// Sharable information
	/** URI of the end point.												*/
	protected final String		endPointURI;

	// Non sharable information; here the transient modifier indicates
	// that the information is not sharable though it is meaningless in
	// the Java sense as POJO end points are not meant to be serialised.

	/** direct reference to the POJO.										*/
	protected transient I		reference;

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

	static {
		GLOBAL_ENDPOINT_REFERENCES = new HashMap<>();
	}

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

		// just a convenient reuse of an existing BCM URI generator, though
		// ports and end points are conceptually related
		this.endPointURI = AbstractPort.generatePortURI();
		this.reference = null;

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
		return GLOBAL_ENDPOINT_REFERENCES.containsKey(this.endPointURI);
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#initialiseServerSide(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void			initialiseServerSide(Object serverSideEndPointOwner)
	throws ConnectionException
	{
		assert	!serverSideInitialised() :
				new PreconditionException("!serverSideInitialised()");
		assert	serverSideEndPointOwner != null :
				new PreconditionException("serverSideEndPointOwner != null");

		try {
			this.reference = (I) serverSideEndPointOwner;
		} catch(ClassCastException e) {
			throw new ConnectionException(
						"The reference " + serverSideEndPointOwner
						+ " does not conform to the server side interface "
						+ this.clientSideInterface.getCanonicalName()
						+ "of this end point.", e);
		}
		GLOBAL_ENDPOINT_REFERENCES.put(this.endPointURI,
											   serverSideEndPointOwner);

		assert	serverSideInitialised() :
				new PostconditionException("serverSideInitialised()");
		assert	POJOEndPoint.implementationInvariants(this) :
				new ImplementationInvariantException(
								"POJOEndPoint.implementationInvariants(this)");
		assert	POJOEndPoint.invariants(this) :
				new InvariantException("POJOEndPoint.invariants(this)");
	}

	/**
	 * This implementation is only valid when executed on the client side object
	 * owning the end point.
	 * 
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#clientSideInitialised()
	 */
	@Override
	public boolean		clientSideInitialised()
	{
		return this.reference != null;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#initialiseClientSide(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void			initialiseClientSide(Object clientSideEndPointOwner)
	throws ConnectionException
	{
		assert	serverSideInitialised() :
				new PreconditionException("serverSideInitialised()");
		assert	!clientSideInitialised() :
				new PreconditionException("!clientSideInitialised()");
		assert	clientSideEndPointOwner != null :
				new PreconditionException("clientSideEndPointOwner != null");

		this.reference = (I) GLOBAL_ENDPOINT_REFERENCES.get(this.endPointURI);

		assert	clientSideInitialised() :
				new PostconditionException("clientSideInitialised()");
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
	 * @see fr.sorbonne_u.components.endpoints.AbstractEndPointI#serverSideClean()
	 */
	@Override
	public boolean		serverSideClean()
	{
		return this.reference == null &&
					!GLOBAL_ENDPOINT_REFERENCES.containsKey(this.endPointURI);
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#cleanUpServerSide()
	 */
	@Override
	public void			cleanUpServerSide()
	{
		assert	!serverSideClean() :
				new PreconditionException("serverSideInitialised()");

		GLOBAL_ENDPOINT_REFERENCES.remove(this.endPointURI);
		this.reference = null;

		assert	serverSideClean() :
				new PostconditionException("serverSideClean()");
		assert	POJOEndPoint.implementationInvariants(this) :
				new ImplementationInvariantException(
								"POJOEndPoint.implementationInvariants(this)");
		assert	POJOEndPoint.invariants(this) :
				new InvariantException("POJOEndPoint.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.AbstractEndPointI#clientSideClean()
	 */
	@Override
	public boolean		clientSideClean()
	{
		return this.reference == null;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#cleanUpClientSide()
	 */
	@Override
	public void			cleanUpClientSide()
	{
		assert	!clientSideClean() :
				new PreconditionException("!clientSideClean()");

		this.reference = null;

		assert	clientSideClean() :
				new PostconditionException("clientSideClean()");
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
			assert	ret.serverSideInitialised() == serverSideInitialised() :
					new PostconditionException(
							"ret.serverSideInitialised() == "
							+ "serverSideInitialised()");

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
		sb.append(this.serverSideInitialised());
		sb.append(", ");
		sb.append(this.clientSideInitialised());
		sb.append(", ");
		sb.append(this.reference.getClass().getSimpleName());
	}
}
