package fr.sorbonne_u.components.plugins.helpers;

// Copyright Jacques Malenfant, Sorbonne Universite.
//
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

import fr.sorbonne_u.components.ports.AbstractOutboundPort;

//------------------------------------------------------------------------------
/**
 * The interface <code>ClientSidePluginI</code> declares the methods
 * that a client plug-in uses to connect with a server-side plug-in
 * on a given component interface with given ports and connector.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This interface and its companion abstract class can be used to
 * facilitate the creation of client-side plug-in that needs to connect
 * dynamically to their server-side component.
 * </p>
 * <p>
 * The important aspect of this plug-in is that the URI management between
 * the client and server components is limited to the fact that the client
 * component initiating the connection knows the URI of the reflection
 * inbound port of the server component. Using this information, it
 * can connect to this port and retrieve the information about other
 * ports URI of the server using the <code>ReflectionI</code> component
 * interface.
 * </p>
 * <p>
 * The two major ways to get the reflection inbound port URI of a
 * component are:
 * </p>
 * <ol>
 * <li>to have a constant string accessible to both components and which
 *   is passed as parameter to the server component constructor;</li>
 * <li>to create the server component using the dynamic component creator
 *   using the <code>createComponent</code> method which returns this URI.
 *   </li>
 * </ol>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2019-03-06</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface			ClientSidePluginI
{
	/**
	 * true if the plug-in is connected to a server side plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return				true if the plug-in is connected to a server side plug-in.
	 * @throws Exception	<i>to do.</i>
	 */
	public boolean			isConnectedToServerSide() throws Exception ;
	/**
	 * connect the outbound port to the server of the given reflection
	 * inbound port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	serverReflectionInboundPortURI != null
	 * post	!ret || this.isConnectedToServerSide()
	 * </pre>
	 *
	 * @param serverReflectionInboundPortURI	URI of the reflection inbound port of the server component.
	 * @return									true if the connection was made successfully.
	 * @throws Exception						if the server-side component does not have an inbound port of the needed offered interface.
	 */
	public boolean			connectWithServerSide(
		String serverReflectionInboundPortURI
		) throws Exception ;

	/**
	 * disconnect the outbound port from the server-side.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isConnectedToServerSide()
	 * post	!this.isConnectedToServerSide()
	 * </pre>
	 *
	 * @throws Exception	if not connected.
	 */
	public void				disconnectFromServerSide() throws Exception ;

	/**
	 * return the outbound port managed by this plug-in or null if it has
	 * not been created yet; should be overridden with the precise return
	 * type of the actual outbound port required interface  to ease the
	 * calling of this interface methods.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the outbound port managed by this plug-in.
	 */
	public AbstractOutboundPort	getOutboundPort() ;
}
//------------------------------------------------------------------------------
