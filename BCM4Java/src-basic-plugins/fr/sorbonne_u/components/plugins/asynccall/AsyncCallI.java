package fr.sorbonne_u.components.plugins.asynccall;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// new implementation of the DEVS simulation standard for Java.
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
import fr.sorbonne_u.components.AbstractComponent;

// -----------------------------------------------------------------------------
/**
 * The interface <code>AsyncCallI</code> declares the methods to be implemented
 * by an asynchronous call class.
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
 * <p>Created on : 2023-03-09</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		AsyncCallI
extends		Serializable
{
	/**
	 * return	true if the call information are set.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the call information are set.
	 */
	public boolean		callInfoSet();

	/**
	 * set the client side information to be used to execute the call.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code callURI != null && callURI.length() > 0}
	 * pre	{@code receptionPortURI != null && receptionPortURI.length() > 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param callURI			URI of this call.
	 * @param receptionPortURI	URI of the result reception inbound port to which the result must be sent.
	 */
	public void			setCallInfo(String callURI, String receptionPortURI);

	/**
	 * return	true if the callee information are set.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the callee information are set.
	 */
	public boolean		calleeInfoSet();

	/**
	 * set the server side references to be used to execute the call.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code calleeInfoSet()}
	 * pre	{@code server != null && plugin != null}
	 * pre	{@code server.isInstalled(plugin.getPluginURI())}
	 * post	{@code plugin.receptionPortConnected(receptionPortURI)}
	 * </pre>
	 *
	 * @param server		the server component.
	 * @param plugin		the server side asynchronous call plug-in.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setCalleeInfo(
		AbstractComponent server,
		AsyncCallServerPlugin plugin
		) throws Exception;

	/**
	 * execute the call on the runner, returning the result through the
	 * plug-in by the method <code>sendResult</code>.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code callInfoSet() && callInfoSet()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			execute() throws Exception;

	/**
	 * send the result to the caller through the plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code callInfoSet()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param result		result to be returned to the caller.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			sendResult(Serializable result) throws Exception;
}
// -----------------------------------------------------------------------------
