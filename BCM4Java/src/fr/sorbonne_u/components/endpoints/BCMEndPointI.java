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

import java.io.Serializable;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

/**
 * The interface <code>BCMEndPointI</code>defines an end point connecting two
 * BCM4Java components using an outbound port, a connector and an inbound port;
 * as the end point is meant to be used both from the caller and the callee
 * sides, it abstracts the outbound port for the former but provides the URI of
 * the callee inbound port to enable the connection.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The BCM4Java end point follows the protocol defined by {@code EndPointI},
 * embedding in the different steps the BCM dependent actions needed to
 * establish the connection. In BCM, an end point is used both on the client
 * and the server side where, after their initialisation, they hold the outbound
 * port for the former and the inbound port for the latter. Hence, on the
 * client side, they cannot be "client side initialised" and on the server side,
 * they cannot be "client side initialised".
 * </p>
 * <p>
 * The redefinitions of the methods in this interface simply makes some typing
 * constraints as well as pre- and postconditions more precise for BCM end
 * points compared to generic end points.
 * </p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2024-06-25</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		BCMEndPointI<CI extends RequiredCI>
extends		EndPointI<CI>,
			Serializable
{
	// -------------------------------------------------------------------------
	// From AbstractEndPointI, adding more precise information.
	// -------------------------------------------------------------------------

	/**
	 * create and publish the inbound port on the client component.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * In BCM end points, the end points is used both by the client and the 
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !clientSideInitialised()}
	 * pre	{@code serverSideEndPointOwner instanceof AbstractComponent}
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#initialiseServerSide(java.lang.Object)
	 */
	@Override
	public void			initialiseServerSide(Object serverSideEndPointOwner);

	/**
	 * create the outbound port, publish it and connect it to the server side
	 * inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !serverSideInitialised()}
	 * pre	{@code clientSideEndPointOwner instanceof AbstractComponent}
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#initialiseClientSide(java.lang.Object)
	 */
	@Override
	public void			initialiseClientSide(Object clientSideEndPointOwner);

	/**
	 * unpublish and destroy the inbound port on the server side component.
	 * <p><strong>Contract</strong></p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#cleanUpServerSide()
	 */
	@Override
	public void			cleanUpServerSide();

	/**
	 * disconnect, unpublish and destroy the outbound port on the client side
	 * component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.endpoints.AbstractEndPointI#cleanUpClientSide()
	 */
	@Override
	public void			cleanUpClientSide();

	// -------------------------------------------------------------------------
	// From EndPointI, adding more precise information.
	// -------------------------------------------------------------------------

	/**
	 * return the (client side) required interface of this BCM end point.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the required interface of this end point.
	 */
	public Class<CI>	getImplementedInterface();

	/**
	 * return the reference proposing the required interface of this end point.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#getReference()
	 */
	@Override
	public CI			getReference();

	/**
	 * copy this BCM end point except its transient information <i>i.e.</i>,
	 * keeping only the information that is shared among copies of the end point.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code return != null}
	 * post	{@code return.getInboundPortURI().equals(getInboundPortURI())}
	 * post	{@code return.getOfferedComponentInterface().equals(getOfferedComponentInterface())}
	 * post	{@code }
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.endpoints.EndPointI#copyWithSharable()
	 */
	@Override
	public BCMEndPointI<CI> copyWithSharable();

	// -------------------------------------------------------------------------
	// Local signatures
	// -------------------------------------------------------------------------

	/**
	 * return the URI of the inbound port embedded in this BCM end point.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && !return.isEmpty()}
	 * </pre>
	 *
	 * @return	the URI of the inbound port embedded in this BCM end point.
	 */
	public String		getInboundPortURI();

	/**
	 * return the (server side) offered interface of this BCM end point.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @param <OCI>	the type of the offered interface.
	 * @return		the offered interface of this end point.
	 */
	public <OCI extends OfferedCI> Class<OCI>	getOfferedComponentInterface();
}
