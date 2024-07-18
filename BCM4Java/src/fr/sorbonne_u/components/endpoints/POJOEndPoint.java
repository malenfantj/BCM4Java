package fr.sorbonne_u.components.endpoints;

import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>POJOEndPointDescriptor</code> defines a plain old Java
 * object (POJO) reference as a end point.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
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
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#copyWithSharable()
	 */
	@Override
	public EndPointI<I>	copyWithSharable()
	{
		if (this.reference == null) {
			return new POJOEndPoint<I>(this.implementedInterface);
		} else {
			return new POJOEndPoint<I>(this.implementedInterface,
									   this.reference);
		}
	}
}
