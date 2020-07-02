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

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.ports.PortI;

// -----------------------------------------------------------------------------
/**
 * The interface <code>ConnectorI</code> is the generic interface for
 * connectors that mediate between requiring and offering components.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * Connectors are objects that are used to connect components through their
 * required, offered and two-way interfaces.  A connector first serves as a
 * translator from calls using the required interface to the ones provided by
 * the offered interface.  It therefore implements a required interface by
 * calling the corresponding methods in the offered interface.  For two-way
 * interfaces, connectors have to forward correctly the calls among the peers.
 * </p>
 * <p>
 * Basically, a connector connects a requiring component to an offering one
 * by implementing the requiring interface with methods that call the
 * offering component through its corresponding offered interface.  A connector
 * therefore implements a mapping between the required and the offered
 * interfaces.  There is no limit to the number of required and offered
 * interfaces that a component may use or implement.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 2011-11-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface 		ConnectorI
extends		RequiredCI	// to be called from the client side
{
	/**
	 * test if this connector is already connected.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if already connected, false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		connected() throws Exception;

	/**
	 * test if the connection is remote (among different processes) or local.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if the connection is across processes, false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isRemote() throws Exception;

	/**
	 * return true if the requiring/client port is remote.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if the requiring/client port is remote.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isRequiringPortRemote() throws Exception;

	/**
	 * return true if the offering/server port is remote.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if the offering/server port is remote.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isOfferringPortRemote() throws Exception;

	/**
	 * connect a requiring component to a requiring one.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !connected()}
	 * pre	{@code offering != null && requiring != null}
	 * post	{@code connected()}
	 * </pre>
	 *
	 * @param offering		in port of the component implementing the offered interface.
	 * @param requiring		out port of the component requiring the required interface.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			connect(OfferedCI offering, RequiredCI requiring)
	throws Exception;

	/**
	 * transmits the connection request to the other port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code sender != null && ccname != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param sender		port from which the request originate.
	 * @param ccname		connector class name to be used in the connection.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			obeyConnection(PortI sender, String ccname)
	throws Exception;

	/**
	 * transmits the connection request to the other port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code sender != null && connector != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param sender		port from which the request originate.
	 * @param connector		connector to be used in the connection.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			obeyConnection(PortI sender, ConnectorI connector)
	throws Exception;

	/**
	 * disconnect the components.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code connected()}
	 * post	{@code !connected()}
	 * </pre>
	 * 
	 * @throws Exception	<i>to do</i>.
	 */
	public void			disconnect() throws Exception;

	/**
	 * transmits the disconnection request to the other port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code sender != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param sender		port from which the request originate
	 * @throws Exception	<i>to do</i>.
	 */
	public void			obeyDisconnection(PortI sender) throws Exception;
}
// -----------------------------------------------------------------------------
