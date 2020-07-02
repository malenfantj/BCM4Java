package fr.sorbonne_u.components.connectors;

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

import fr.sorbonne_u.components.interfaces.DataTwoWayCI;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>DataTwoWayConnector</code> implements a standard connector
 * for components exchanging data in a peer-to-peer mode using standard data
 * two-way interfaces.
 * 
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * Compared to data connectors, the data two way connector cannot know from
 * which component a call is coming, so this information must be provided as a
 * parameter of the call.  Hence, when processing an incoming call, the
 * connector finds the other component and relays it the call.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 2012-10-23</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			DataTwoWayConnector
extends		AbstractDataTwoWayConnector
{
	// -------------------------------------------------------------------------
	// Inner classes
	// -------------------------------------------------------------------------

	/**
	 * The class <code>ProxyToOtherComponent</code> implements a directional
	 * proxy that can forward a call to the other port when the sender port
	 * identifies itself with its URI.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	true
	 * </pre>
	 * 
	 * <p>Created on : 2018-03-26</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	protected static class	ProxyToOtherComponent
	extends		AbstractTwoWayConnector.ProxyToOtherComponent<DataTwoWayCI>
	implements	DataTwoWayCI
	{
		/**
		 * create a proxy that can forward a call to the other port when the
		 * sender port identifies itself with its URI.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code owner != null && senderPortURI != null}
		 * post	true	// no postcondition.
		 * </pre>
		 *
		 * @param owner			owner of the port.
		 * @param senderPortURI	URI of the sender port.
		 */
		public			ProxyToOtherComponent(
			AbstractTwoWayConnector<DataTwoWayCI> owner,
			String senderPortURI
			)
		{
			super(owner, senderPortURI);
			assert	owner != null && senderPortURI != null :
						new PreconditionException(
								"owner != null && senderPortURI != null");
		}

		/**
		 * @see fr.sorbonne_u.components.interfaces.DataTwoWayCI#send(fr.sorbonne_u.components.interfaces.DataTwoWayCI.DataI)
		 */
		@Override
		public void		send(DataTwoWayCI.DataI d)
		throws	Exception
		{
			assert	this.connected() : new PreconditionException("connected()");

			if (this.senderPortURI.equals(this.getOfferingPortURI())) {
				((DataTwoWayCI)this.getRequiring()).send(
							((DataTwoWayConnectorI)this.owner).first2second(d));
			} else {
				((DataTwoWayCI)this.getOffering()).send(
							((DataTwoWayConnectorI)this.owner).second2first(d));
			}
		}

		/**
		 * @see fr.sorbonne_u.components.interfaces.DataTwoWayCI#request()
		 */
		@Override
		public DataTwoWayCI.DataI	request()
		throws	Exception
		{
			assert	this.connected() : new PreconditionException("connected()");

			if (this.senderPortURI.equals(this.getOfferingPortURI())) {
				return ((DataTwoWayConnectorI)this.owner).second2first(
								((DataTwoWayCI)this.getRequiring()).request());
			} else {
				return ((DataTwoWayConnectorI)this.owner).first2second(
								((DataTwoWayCI)this.getOffering()).request());
			}
		}
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.connectors.AbstractTwoWayConnector#getProxyTowardsOtherComponent(java.lang.String)
	 */
	@Override
	public DataTwoWayCI	getProxyTowardsOtherComponent(
		String senderPortURI
		) throws Exception
	{
		return (DataTwoWayCI)new ProxyToOtherComponent(this, senderPortURI);
	}

	// -------------------------------------------------------------------------
	// Service methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.interfaces.DataTwoWayCI#send(fr.sorbonne_u.components.interfaces.DataTwoWayCI.DataI)
	 */
	@Override
	public void			send(DataTwoWayCI.DataI d)
	throws	Exception
	{
		throw new Exception("DataTwoWayConnector>>send() must be "
				+ "called through the proxy to the other component.");
	}

	/**
	 * @see fr.sorbonne_u.components.interfaces.DataTwoWayCI#request()
	 */
	@Override
	public DataTwoWayCI.DataI	request()
	throws	Exception
	{
		throw new Exception("DataTwoWayConnector>>request() must be "
				+ "called through the proxy to the other component.");
	}
}
// -----------------------------------------------------------------------------
