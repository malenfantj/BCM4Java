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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>BCMEndPoint</code> partially implements an end point that
 * uses a BCM connection (outbound port -- connector -- inbound port); must
 * be extended by actual end points on precise required/offered component
 * interfaces.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code serverSideOfferedInterface != null}
 * invariant	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
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
implements	BCMEndPointI<CI>
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
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a new BCM end point descriptor with a generated inbound port URI.
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
			 AbstractPort.generatePortURI());
	}

	/**
	 * create a new BCM end point descriptor with the given inbound port URI.
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

		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");
		assert	serverSideOfferedInterface != null :
				new PreconditionException("serverSideOfferedInterface != null");

		this.serverSideOfferedInterface = serverSideOfferedInterface;
		this.inboundPortURI = inboundPortURI;
		this.server = null;
		this.inboundPort = null;
		this.client = null;
		this.outboundPort = null;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#getInboundPortURI()
	 */
	@Override
	public String		getInboundPortURI()
	{
		return this.inboundPortURI;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#getOfferedComponentInterface()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <OCI extends OfferedCI> Class<OCI>	getOfferedComponentInterface()
	{
		return (Class<OCI>) this.serverSideOfferedInterface;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#serverSideInitialised()
	 */
	@Override
	public boolean		serverSideInitialised()
	{
		return this.inboundPort != null;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#initialiseServerSide(java.lang.Object)
	 */
	@Override
	public void			initialiseServerSide(Object serverSideEndPointOwner)
	{
		assert	!serverSideInitialised() :
				new PreconditionException("!serverSideInitialised()");
		assert	serverSideEndPointOwner instanceof AbstractComponent :
				new PreconditionException(
						"serverSideEndPointOwner instanceof "
						+ "AbstractComponent");

		this.server = (AbstractComponent) serverSideEndPointOwner;
		try {
			this.inboundPort =
					this.makeInboundPort(
							(AbstractComponent) serverSideEndPointOwner,
							this.inboundPortURI);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * create, publish and return the inbound port on the server side component
	 * {@code c} with the given inbound port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code c != null}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * post	{@code return != null && return.isPublished()}
	 * post	{@code ((AbstractPort)return).getPortURI.equals(inboundPortURI)}
	 * post	{@code getOfferedComponentInterface().isAssignableFrom(return.getClass())}
	 * </pre>
	 *
	 * @param c					component that will own the inbound port.
	 * @param inboundPortURI	URI of the inbound port to be created.
	 * @return					the created inbound port.
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
	public boolean			clientSideInitialised()
	{
		return this.outboundPort != null;
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#initialiseClientSide(java.lang.Object)
	 */
	@Override
	public void				initialiseClientSide(Object clientSideEndPointOwner)
	{
		assert	!clientSideInitialised() :
				new PreconditionException("!clientSideInitialised()");
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		try {
			assert	((AbstractPort)this.outboundPort).getServerPortURI().
											equals(this.getInboundPortURI()) :
					new RuntimeException(
							"BCMEndPoint::initialiseClientSide connection "
							+ "exception: outbound port not connected to the "
							+ "inbound port with URI "
							+ this.getInboundPortURI());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * create, publish, connect and return the outbound port requiring the
	 * component interface {@code CI} on the client side component {@code c}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code c != null}
	 * post	{@code return != null && return.isPublished() && return.connected()}
	 * post	{@code ((AbstractPort)return).getServerPortURI().equals(this.getInboundPortURI())}
	 * post	{@code getImplementedInterface().isAssignableFrom(return.getClass())}
	 * </pre>
	 *
	 * @param c					component that will own the outbound port.
	 * @param inboundPortURI	URI of the inbound prt to which the end point must be connected.
	 * @return					the created outbound port.
	 * @throws Exception		<i>to do</i>.
	 */
	protected abstract CI	makeOutboundPort(
		AbstractComponent c,
		String inboundPortURI
		) throws Exception;

	/**
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#getReference()
	 */
	@Override
	public CI			getReference()
	{
		assert	clientSideInitialised() :
				new PreconditionException("clientSideInitialised()");

		return this.outboundPort;
	}

	/**
	 * unpublish and destroy the inbound port.
	 * 
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#cleanUpServerSide()
	 */
	@Override
	public void			cleanUpServerSide()
	{
		try {
			if (this.inboundPort != null) {
				if (this.inboundPort.isPublished()) {
					this.inboundPort.unpublishPort();
				}
				this.inboundPort.destroyPort();
				this.inboundPort = null;
				this.server = null;
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * disconnect, unpublish and destroy the outbound port.
	 * 
	 * @see fr.sorbonne_u.components.endpoints.BCMEndPointI#cleanUpClientSide()
	 */
	@Override
	public void			cleanUpClientSide()
	{
		if (this.outboundPort != null) {
			try {
				if (((AbstractOutboundPort)this.outboundPort).connected()) {
					this.client.doPortDisconnection(
						((AbstractOutboundPort)this.outboundPort).getPortURI());
				}
				if (((AbstractOutboundPort)this.outboundPort).isPublished()) {
					((AbstractOutboundPort)this.outboundPort).unpublishPort();
				}
				((AbstractOutboundPort)this.outboundPort).destroyPort();
				this.outboundPort = null;
				this.client = null;
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
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
		sb.append(']');
	}

	/**
	 * @see fr.sorbonne_u.components.endpoints.EndPoint#addLocalContentToStringBuffer(java.lang.StringBuffer)
	 */
	@Override
	protected void		addLocalContentToStringBuffer(StringBuffer sb)
	{
		super.addLocalContentToStringBuffer(sb);
		sb.append(", ");
		sb.append(this.inboundPortURI);
		sb.append(", ");
		sb.append(this.serverSideInitialised());
		sb.append(", ");
		sb.append(this.clientSideInitialised());
		if (this.clientSideInitialised()) {
			sb.append(", ");
			try {
				sb.append(((AbstractPort)this.outboundPort).getPortURI());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
