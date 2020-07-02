package fr.sorbonne_u.components.ports;

//Copyright Jacques Malenfant, Sorbonne Universite.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import fr.sorbonne_u.components.connectors.DataConnectorI;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;

//-----------------------------------------------------------------------------
/**
 * The interface <code>DataInboundPortI</code> represents the category of
 * ports used by components exchanging data rather than being called for
 * their services.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * A data inbound port is associated with a <code>DataOfferedCI</code>
 * interface used to implement a basic interface to get data from a provider
 * component.  The primary way to get the data is the pull mode, where the
 * client component calls the provider through the <code>get()</code> method
 * which returns a piece of data.  But the port can also be used in a push
 * mode where the provider delivers the data to the client by calling a
 * <code>send</code> method with a piece of data as parameter.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code getImplementedInterface() == getImplementedPullInterface()}
 * invariant	{@code !connected() || isPublished()}
 * invariant	{@code !connected() || getPortURI().equals(getClientPortURI())}
 * invariant	{@code !connected() || (getServerPortURI() != null)}
 * invariant	{@code !connected() == (getConnector() == null)}
 * invariant	{@code !isRemotelyConnected() || connected()}
 * invariant	{@code !isRemotelyConnected() || isDistributedlyPublished()}
 * </pre>
 * 
 * <p>Created on : 2011-11-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface			DataInboundPortI
extends		InboundPortI,
			DataOfferedCI.PullCI,	// to be called from the outside to get data
			DataOfferedCI.PushCI	// to be called from the inside to send data
{
	// ------------------------------------------------------------------------
	// Self-properties management
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.ports.InboundPortI#getImplementedInterface()
	 */
	@Override
	public Class<? extends DataOfferedCI.PullCI> getImplementedInterface()
	throws Exception;

	/**
	 * get the pull interface implemented by this port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return				the implemented pull interface.
	 * @throws Exception	<i>to do</i>.
	 */
	public Class<? extends DataOfferedCI.PullCI> getImplementedPullInterface()
	throws Exception;

	/**
	 * get the push interface implemented by this port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return				the implemented push interface.
	 * @throws Exception	<i>to do</i>.
	 */
	public Class<? extends DataOfferedCI.PushCI> getImplementedPushInterface()
	throws Exception;

	// ------------------------------------------------------------------------
	// Connection management
	// ------------------------------------------------------------------------

	/**
	 * returns the connector to which this port is attached, and null if none.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return				the connector to which this port is attached, and null if none.
	 * @throws Exception	<i>to do</i>.
	 */
	public DataConnectorI	getConnector() throws Exception ;

	/**
	 * set the connector of the port, establishing the connection with a client.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * pre	{@code c != null}
	 * post	{@code this.getConnector() == c}
	 * </pre>
	 *
	 * @param c				connector to be set.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setConnector(DataConnectorI c) throws Exception ;

	/**
	 * unset the connector of the port, cutting the connection with a client.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isDestroyed()}
	 * post	{@code this.getConnector() == null}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			unsetConnector() throws Exception ;
}
//-----------------------------------------------------------------------------
