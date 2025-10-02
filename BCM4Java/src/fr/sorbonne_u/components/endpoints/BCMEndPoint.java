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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.BCMRuntimeException;
import fr.sorbonne_u.components.exceptions.ConnectionException;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>BCMEndPoint</code> partially implements an end point that
 * uses a BCM connection (outbound port -- connector -- inbound port); must
 * be extended by actual end points on precise required/offered component
 * interfaces.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This class implement the BCM4Java protocol for connecting components through
 * outbound ports, connectors and inbound ports. It is an abstract class because
 * it requires subclasses to provide a way to create the outbound and inbound
 * ports as well as the connector used to connect them. The user of this class
 * must first define the classes implementing the ports and the connector. Then,
 * he/she must define a subclass implementing the
 * {@code makeOutboundPort(AbstractComponent,String)} and
 * {@code makeInboundPort(AbstractComponent,String)} methods.
 * In the BCM4Java approach, {@code makeInboundPort()} creates the inbound port
 * with the provided URI on the given server component and publishes the port
 * in the registry.
 * {@code makeOutboundPort(AbstractComponent,String)} creates the outbound
 * port on the given client component and then connects this port to the
 * server component with the given inbound port URI using the appropriate
 * connector.
 * </p>
 * <p>
 * This partial implementation is not thread safe.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code serverSideOfferedInterface != null}
 * invariant	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
 * invariant	{@code serverSideInitialised() || inboundPort == null}
 * invariant	{@code clientSideInitialised() || outboundPort == null}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2024-06-24</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	BCMEndPoint<CI extends RequiredCI>
extends		EndPoint<CI>
implements	BCMEndPointI<CI>,
			Cloneable
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long					serialVersionUID = 1L;

	// Sharable information

	/** the component interface offered by the server side of this
	 *  end point.															*/
	protected final Class<? extends OfferedCI>	serverSideOfferedInterface;
	/** URI of the inbound port offering {@code serverSideOfferedInterface}.*/
	protected final String						inboundPortURI;

	// Not sharable information

	/** when true, the server has initialised the end point, false otherwise.*/
	protected transient boolean					serverSideInitialised;
	/** reference to the server side component; only available on the
	 *  copy of the end point that resides on the server side otherwise it
	 *  is null.															*/
	protected transient AbstractComponent		server;	
	/** inbound port; only available on the copy of the end point that
	 *  resides on the server side otherwise it is null.					*/
	protected transient AbstractInboundPort		inboundPort;

	/** reference to the client side component; only available on the
	 *  copy of the end point that resides on the client side otherwise it
	 *  is null.															*/
	protected transient AbstractComponent		client;
	/** outbound port; only available on the copy of the end point that
	 *  resides on the client side otherwise it is null.					*/
	protected transient CI						outboundPort;

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
		BCMEndPoint<?> instance
		)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.serverSideOfferedInterface != null,
					BCMEndPoint.class, instance,
					"serverSideOfferedInterface != null");
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.inboundPortURI != null &&
										!instance.inboundPortURI.isEmpty(),
					BCMEndPoint.class, instance,
					"inboundPortURI != null && !inboundPortURI.isEmpty()");
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.serverSideInitialised() ||
											instance.inboundPort == null,
					BCMEndPoint.class, instance,
					"serverSideInitialised() || inboundPort == null");
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.clientSideInitialised() ||
											instance.outboundPort == null,
					BCMEndPoint.class, instance,
					"clientSideInitialised() || outboundPort == null");
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
	protected static boolean	invariants(BCMEndPoint<?> instance)
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
	 * create a new BCM end point with a generated inbound port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code implementedInterface != null}
	 * pre	{@code serverSideOfferedInterface != null}
	 * post	{@code !serverSideInitialised()}
	 * post	{@code !clientSideInitialised()}
	 * </pre>
	 *
	 * @param implementedInterface			the interface required by this end point.
	 * @param serverSideOfferedInterface	the component interface offered by the server side of this end point.
	 */
	public				BCMEndPoint(
		Class<CI> implementedInterface,
		Class<? extends OfferedCI> serverSideOfferedInterface
		)
	{
		this(implementedInterface,
			 serverSideOfferedInterface,
			 AbstractPort.generatePortURI(implementedInterface));
	}

	/**
	 * create a new BCM end point with the given inbound port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code implementedInterface != null}
	 * pre	{@code serverSideOfferedInterface != null}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * post	{@code !serverSideInitialised()}
	 * post	{@code !clientSideInitialised()}
	 * </pre>
	 *
	 * @param implementedInterface			the component interface required by this end point.
	 * @param serverSideOfferedInterface	the component interface offered by the server side of this end point.
	 * @param inboundPortURI				URI if the inbound port to which this end point connects.
	 */
	public				BCMEndPoint(
		Class<CI> implementedInterface,
		Class<? extends OfferedCI> serverSideOfferedInterface,
		String inboundPortURI
		)
	{
		super(implementedInterface);

		assert	serverSideOfferedInterface != null :
				new PreconditionException("serverSideOfferedInterface != null");
		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");

		// sharable information
		this.serverSideOfferedInterface = serverSideOfferedInterface;
		this.inboundPortURI = inboundPortURI;

		// transient information
		this.serverSideInitialised = false;
		this.server = null;
		this.inboundPort = null;
		this.client = null;
		this.outboundPort = null;

		assert	!serverSideInitialised() :
				new PostconditionException("!serverSideInitialised()");
		assert	!clientSideInitialised() :
				new PostconditionException("!clientSideInitialised()");
		assert	BCMEndPoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"BCMEndPoint.implementationInvariants(this)");
		assert	BCMEndPoint.invariants(this) :
				new InvariantException("BCMEndPoint.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#serverSideInitialised()
	 */
	@Override
	public synchronized boolean		serverSideInitialised()
	{
		if (this.serverSideInitialised) {
			return true;
		} else if (this.inboundPortURI == null) {
			return false;
		} else {
			boolean ret =
					AbstractCVM.isPublishedInLocalRegistry(this.inboundPortURI);
			if (!ret && AbstractCVM.isDistributed) {
				try {
					ret = AbstractDistributedCVM.isPublished(this.inboundPortURI);
				} catch (BCMException e) {
					throw new RuntimeException(e);
				}
			}
			this.serverSideInitialised = ret;
			return ret;
		}
	}

	/**
	 * if no inbound port with the URI attributed when creating the end point
	 * exists, the method calls {@code makeInboundPort} to create and publish
	 * an inbound port with the provided URI on the server component.
	 * 
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#initialiseServerSide(java.lang.Object)
	 */
	@Override
	public synchronized void	initialiseServerSide(
		Object serverSideEndPointOwner
		) throws ConnectionException
	{
		assert	!serverSideInitialised() :
				new PreconditionException("!serverSideInitialised()");
		assert	serverSideEndPointOwner != null :
				new PreconditionException("serverSideEndPointOwner != null");
		assert	serverSideEndPointOwner instanceof AbstractComponent :
				new PreconditionException(
						"serverSideEndPointOwner instanceof "
						+ "AbstractComponent");

		this.server = (AbstractComponent) serverSideEndPointOwner;
		try {
			PortI p = this.getPortFromURI(this.server, this.inboundPortURI);

			if (p == null) {
				this.inboundPort =
						this.makeInboundPort(
								(AbstractComponent) serverSideEndPointOwner,
								this.inboundPortURI);
			} else {
				assert	p instanceof AbstractInboundPort :
						new BCMRuntimeException(
								"a port with URI " + this.inboundPortURI
								+ " exists in this end point owner but is not an "
								+ "inbound port.");
				assert	p.isPublished() :
						new BCMRuntimeException(
								"an inbound port with URI " + this.inboundPortURI
								+ " exists on this end point owner component but"
								+ " it is not published");

				this.inboundPort = (AbstractInboundPort) p;
			}
		} catch (Throwable e) {
			throw new ConnectionException(e);
		}

		assert	serverSideInitialised() :
				new PostconditionException("serverSideInitialised()");
		assert	BCMEndPoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"BCMEndPoint.implementationInvariants(this)");
		assert	BCMEndPoint.invariants(this) :
				new InvariantException("BCMEndPoint.invariants(this)");
	}

	/**
	 * on the server side component only, create, publish and return an inbound
	 * port on the server side component {@code c} with the given inbound port
	 * URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code c != null}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * post	{@code return != null && return.isPublished()}
	 * post	{@code ((AbstractPort)return).getPortURI().equals(inboundPortURI)}
	 * post	{@code getOfferedComponentInterface().isAssignableFrom(return.getClass())}
	 * </pre>
	 *
	 * @param c					component that will own the inbound port.
	 * @param inboundPortURI	URI of the inbound port to be created.
	 * @return					the created inbound port which must be published.
	 * @throws Exception		<i>to do</i>.
	 */
	protected abstract AbstractInboundPort	makeInboundPort(
		AbstractComponent c,
		String inboundPortURI
		) throws Exception;

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#clientSideInitialised()
	 */
	@Override
	public synchronized boolean		clientSideInitialised()
	{
		return this.client != null && this.outboundPort != null;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#initialiseClientSide(java.lang.Object)
	 */
	@Override
	public synchronized void	initialiseClientSide(
		Object clientSideEndPointOwner
		) throws ConnectionException
	{
		assert	serverSideInitialised() :
				new PreconditionException("serverSideInitialised()");
		assert	!clientSideInitialised() :
				new PreconditionException("!clientSideInitialised()");
		assert	clientSideEndPointOwner != null :
				new PreconditionException("clientSideEndPointOwner != null");
		assert	clientSideEndPointOwner instanceof AbstractComponent :
				new PreconditionException(
						"clientSideEndPointOwner instanceof "
						+ "AbstractComponent");

		this.client = (AbstractComponent) clientSideEndPointOwner;
		try {
			this.outboundPort =
					this.makeOutboundPort(
							(AbstractComponent) clientSideEndPointOwner,
							this.inboundPortURI);
		} catch (Throwable e) {
			throw new ConnectionException(e);
		}

		try {
			assert	((AbstractPort)this.outboundPort).getServerPortURI().
											equals(this.getInboundPortURI()) :
					new ConnectionException(
							"BCMEndPoint::initialiseClientSide connection "
							+ "exception: outbound port not connected to the "
							+ "inbound port with URI "
							+ this.getInboundPortURI());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		assert	clientSideInitialised() :
				new PostconditionException("clientSideInitialised()");
		assert	BCMEndPoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"BCMEndPoint.implementationInvariants(this)");
		assert	BCMEndPoint.invariants(this) :
				new InvariantException("BCMEndPoint.invariants(this)");
	}

	/**
	 * on the client side component only, create, publish, connect and return
	 * the outbound port requiring the component interface {@code CI} on the
	 * client side component {@code c}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code c != null}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * post	{@code return != null && return.isPublished() && return.connected()}
	 * post	{@code ((AbstractPort)return).getServerPortURI().equals(getInboundPortURI())}
	 * post	{@code getImplementedInterface().isAssignableFrom(return.getClass())}
	 * </pre>
	 *
	 * @param c					component that will own the outbound port.
	 * @param inboundPortURI	URI of the inbound port to which the outbound port must be connected.
	 * @return					the created outbound port, which must be published and connected.
	 * @throws Exception		<i>to do</i>.
	 */
	protected abstract CI	makeOutboundPort(
		AbstractComponent c,
		String inboundPortURI
		) throws Exception;

	/**
	 * @see fr.sorbonne_u.components.endpoints.AbstractEndPointI#serverSideClean()
	 */
	@Override
	public boolean		serverSideClean()
	{
		return !this.serverSideInitialised;
	}

	/**
	 * unpublish and destroy the inbound port.
	 * 
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#cleanUpServerSide()
	 */
	@Override
	public void			cleanUpServerSide()
	{
		assert	!serverSideClean() :
				new PreconditionException("!serverSideClean()");

		try {
			if (this.inboundPort != null) {
				if (this.inboundPort.isPublished()) {
					this.inboundPort.unpublishPort();
				}
				this.inboundPort.destroyPort();
				this.inboundPort = null;
				this.server = null;
				this.serverSideInitialised = false;
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		assert	serverSideClean() :
				new PostconditionException("serverSideClean()");
		assert	BCMEndPoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"BCMEndPoint.implementationInvariants(this)");
		assert	BCMEndPoint.invariants(this) :
				new InvariantException("BCMEndPoint.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.AbstractEndPointI#clientSideClean()
	 */
	@Override
	public boolean		clientSideClean()
	{
		return !this.clientSideInitialised();
	}

	/**
	 * disconnect, unpublish and destroy the outbound port.
	 * 
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#cleanUpClientSide()
	 */
	@Override
	public void			cleanUpClientSide()
	{
		assert	!clientSideClean() :
				new PreconditionException("!clientSideClean()");

		if (this.outboundPort != null) {
			try {
				AbstractOutboundPort p =
								(AbstractOutboundPort) this.outboundPort;
				if (p.connected()) {
					this.client.doPortDisconnection(p.getPortURI());
				}
				if (p.isPublished()) {
					p.unpublishPort();
				}
				p.destroyPort();
				this.outboundPort = null;
				this.client = null;
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}

		assert	clientSideClean() :
				new PostconditionException("clientSideClean()");
		assert	BCMEndPoint.implementationInvariants(this) :
				new ImplementationInvariantException(
						"BCMEndPoint.implementationInvariants(this)");
		assert	BCMEndPoint.invariants(this) :
				new InvariantException("BCMEndPoint.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#getClientSideReference()
	 */
	@Override
	public CI			getClientSideReference()
	{
		assert	clientSideInitialised() :
				new PreconditionException("clientSideInitialised()");

		return this.outboundPort;
	}

	/**
	 * return a copy of this BCM end point with only the sharable information.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * In a {@code BCMEndPoint}, the following elements are considered as
	 * sharable:
	 * </p>
	 * <ul>
	 * <li>the inherited sharable elements;</li>
	 * <li>the component interface offered by the server side of the end
	 *     point;</li>
	 * <li>the URI of the inbound port offering the above component
	 *     interface.</li>
	 * </ul>
	 * <p>
	 * By default, this implementation clone  (<i>i.e.</i>, shallow copy) the
	 * end point and nullify the non sharable elements. If a subclass introduces
	 * more non sharable elements, it will have to redefine the method to avoid
	 * copying these.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code return != null}
	 * post	{@code return.getClientSideInterface().equals(getClientSideInterface())}
	 * post	{@code return.getInboundPortURI().equals(getInboundPortURI())}
	 * post	{@code return.getServerSideInterface().equals(getServerSideInterface())}
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#copyWithSharable()
	 */
	@Override
	public BCMEndPointI<CI>	copyWithSharable()
	{
		try {
			@SuppressWarnings("unchecked")
			BCMEndPoint<CI> ret = (BCMEndPoint<CI>) this.clone();
			ret.server = null;
			ret.inboundPort = null;
			ret.client = null;
			ret.outboundPort = null;

			assert	BCMEndPoint.implementationInvariants(ret) :
					new ImplementationInvariantException(
							"BCMEndPoint.implementationInvariants(ret)");
			assert	BCMEndPoint.invariants(ret) :
					new InvariantException("BCMEndPoint.invariants(ret)");

			assert	ret.getClientSideInterface().
									equals(this.getClientSideInterface()) :
					new PostconditionException(
							"return.getImplementedInterface().equals("
							+ "getImplementedInterface())");
			assert	ret.getInboundPortURI().equals(this.getInboundPortURI()) :
					new PostconditionException(
							"return.getInboundPortURI().equals("
							+ "getInboundPortURI())");
			assert	ret.getServerSideInterface().
											equals(getServerSideInterface()) :
					new PostconditionException(
							"return.getServerSideInterface().equals("
							+ "getServerSideInterface())");

			return ret;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#getInboundPortURI()
	 */
	@Override
	public String		getInboundPortURI()
	{
		return this.inboundPortURI;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#getServerSideInterface()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <OCI extends OfferedCI> Class<OCI>	getServerSideInterface()
	{
		return (Class<OCI>) this.serverSideOfferedInterface;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#isServerComponent(fr.sorbonne_u.components.AbstractComponent)
	 */
	@Override
	public boolean		isServerComponent(AbstractComponent c)
	{
		return c != null && c == this.server;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#isClientComponent(fr.sorbonne_u.components.AbstractComponent)
	 */
	@Override
	public boolean		isClientComponent(AbstractComponent c)
	{
		return c != null && c == this.client;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPoint#addLocalContentToStringBuffer(java.lang.StringBuffer)
	 */
	@Override
	protected void		addLocalContentToStringBuffer(StringBuffer sb)
	{
		super.addLocalContentToStringBuffer(sb);
		sb.append(", inboundPortURI = ");
		sb.append(this.inboundPortURI);
		sb.append(", serverSideInitialised() = ");
		sb.append(this.serverSideInitialised());
		sb.append(", serverSideClean() = ");
		sb.append(this.serverSideClean());
		sb.append(", clientSideInitialised() = ");
		sb.append(this.clientSideInitialised());
		if (this.clientSideInitialised()) {
			sb.append(", outbound port URI = ");
			try {
				sb.append(((AbstractPort)this.outboundPort).getPortURI());
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		sb.append(", clientSideClean() = ");
		sb.append(this.clientSideClean());
	}
}
