package fr.sorbonne_u.components.ports;

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

import java.rmi.Remote;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.connectors.ConnectorI;
import fr.sorbonne_u.components.interfaces.ComponentInterface;

// -----------------------------------------------------------------------------
/**
 * The interface <code>PortI</code> provides for a common supertype for all
 * ports in the component model.
 * 
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * In the component model, a port represent an entry point through which
 * components can be connected using connectors.  On the implementation side,
 * ports are objects through which a client component calls its provider
 * components, and also through which a provider component is called by its
 * client components.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code isDestroyed() == (getOwner() == null)}
 * invariant	{@code !isDestroyed() || getOwner().isPortExisting(getPortURI())}
 * invariant	{@code !isDistributedlyPublished() || isPublished()}
 * invariant	{@code getOwner().isInterface(getImplementedInterface())}
 * </pre>
 * 
 * <p>Created on : 2011-11-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		PortI
extends		Remote
{
	// -------------------------------------------------------------------------
	// Self-properties management
	// -------------------------------------------------------------------------

	/**
	 * return true if the port has been destroyed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return				true if the port has been destroyed, false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isDestroyed() throws Exception;

	/**
	 * return the component that owns this port; if not, the port has been
	 * destroyed and must not be used anymore.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				the component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public ComponentI	getOwner() throws Exception;

	/**
	 * return the interface implemented by this port on behalf of the
	 * component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return				the Class object representing the port component interface.
	 * @throws Exception	<i>to do</i>.
	 */
	public Class<? extends ComponentInterface>	getImplementedInterface()
	throws Exception;
	
	/**
	 * return the unique identifier of this port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return				the unique identifier of this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public String		getPortURI() throws Exception;

	// -------------------------------------------------------------------------
	// Registry publication management
	// -------------------------------------------------------------------------

	/**
	 * return true if the port has been published.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if the port has been published.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isPublished() throws Exception;

	/**
	 * return true if the port has been distributedly published.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if the port has been distributedly published.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isDistributedlyPublished() throws Exception;

	/**
	 * publish the port on the local registry.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code getOwner().isPortExisting(getPortURI())}
	 * pre	{@code !isPublished()}
	 * post	{@code isPublished()}
	 * </pre>
	 * 
	 * @throws Exception <i>to do</i>;
	 */
	public void			localPublishPort() throws Exception;

	/**
	 * publish the port both on the local and the global registry.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code getOwner().isPortExisting(getPortURI())}
	 * pre	{@code !isPublished()}
	 * post	{@code isPublished()}
	 * </pre>
	 * 
	 * @throws Exception <i>to do</i>.
	 */
	public void			publishPort() throws Exception;

	/**
	 * unpublish the port on the local registry and on the global one if
	 * required.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code getOwner().isPortExisting(this.getPortURI())}
	 * pre	{@code isPublished()}
	 * post	{@code !isPublished()}
	 * </pre>
	 * 
	 * @throws Exception	<i>to do</i>.
	 */
	public void			unpublishPort() throws Exception;

	// -------------------------------------------------------------------------
	// Life-cycle management
	// -------------------------------------------------------------------------

	/**
	 * destroy this port and removing it from the ports known to the owner
	 * component
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code getOwner().isPortExisting(getPortURI())}
	 * pre	{@code !isPublished()}
	 * post	{@code isDestroyed()}
	 * post	{@code !getOwner()@pre.isPortExisting(this.getPortURI()@pre)}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			destroyPort() throws Exception;

	// -------------------------------------------------------------------------
	// Connection management
	// -------------------------------------------------------------------------

	/**
	 *  sets the URI of the client port to which this port is connected.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code clientPortURI != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param clientPortURI	URI of the client port.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setClientPortURI(String clientPortURI)
	throws Exception;

	/**
	 * sets the URI of the server port to which this port is connected.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code serverPortURI != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param serverPortURI	URI of the server port.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setServerPortURI(String serverPortURI)
	throws Exception;

	/**
	 * sets the URI of the client port to which this port is connected.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	{@code getClientPortURI() == null}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			unsetClientPortURI()
	throws Exception;

	/**
	 * sets the URI of the server port to which this port is connected.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	{@code getServerPortURI() == null}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			unsetServerPortURI()
	throws Exception;

	/**
	 * return the URI of the client port in the connection enabled by
	 * this port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				the URI of the client port.
	 * @throws Exception	<i>to do</i>.
	 */
	public String		getClientPortURI() throws Exception;

	/**
	 * return the URI of the server port in the connection enabled by
	 * this port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				the URI of the server port.
	 * @throws Exception	<i>to do</i>.
	 */
	public String		getServerPortURI() throws Exception;

	/**
	 * check whether or not the port is connected to some connector.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if connected to some connector, and false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		connected() throws Exception;

	/**
	 * when connected, return true if the connection is remote and
	 * false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				when connected, true if the connection is remote and false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isRemotelyConnected() throws Exception;

	/**
	 * connect the port where the owner is calling its port to initiate the
	 * connection.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code isPublished()}
	 * pre	{@code !connected()}
	 * pre	{@code otherPortURI != null && ccname != null}
	 * post	{@code connected()}
	 * </pre>
	 *
	 * @param otherPortURI	URI of the other port to be connected with.
	 * @param ccname		connector class name to be used in the connection.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			doConnection(String otherPortURI, String ccname)
	throws Exception;

	/**
	 * connect the port where the owner is calling its port to initiate the
	 * connection.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code isPublished()}
	 * pre	{@code !.connected()}
	 * pre	{@code otherPortURI != null && connector != null}
	 * post	{@code connected()}
	 * </pre>
	 *
	 * @param otherPortURI	URI of the other port to be connected with.
	 * @param connector		connector to be used in the connection.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			doConnection(
		String otherPortURI,
		ConnectorI connector
		) throws Exception;

	/**
	 * connect when the other component is the initiator of the connection or
	 * called by the owner after it has requested the connection from the other
	 * component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code isPublished()}
	 * pre	{@code !connected()}
	 * pre	{@code otherPortURI != null && ccname != null}
	 * post	{@code connected()}
	 * </pre>
	 *
	 * @param otherPortURI	URI of the other port to be connected with.
	 * @param ccname		connector class name to be used in the connection.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			obeyConnection(String otherPortURI, String ccname)
	throws Exception;

	/**
	 * connect when the other component is the initiator of the connection.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code isPublished()}
	 * pre	{@code !connected()}
	 * pre	{@code otherPortURI != null && connector != null}
	 * post	{@code connected()}
	 * </pre>
	 *
	 * @param otherPortURI	URI of the other port to be connected with.
	 * @param connector		connector to be used in the connection.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			obeyConnection(
		String otherPortURI,
		ConnectorI connector
		) throws Exception;

	/**
	 * disconnect the port where the owner is calling its port to initiate the
	 * disconnection.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code connected()}
	 * post	{@code !connected()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			doDisconnection() throws Exception;

	/**
	 * disconnect when the other component is the initiator of the disconnection.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code connected()}
	 * post	{@code !connected()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			obeyDisconnection() throws Exception;
}
// -----------------------------------------------------------------------------
