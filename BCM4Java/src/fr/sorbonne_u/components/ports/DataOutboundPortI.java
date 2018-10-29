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

import fr.sorbonne_u.components.interfaces.DataRequiredI;

//-----------------------------------------------------------------------------
/**
 * The interface <code>DataOutboundPortI</code> represents the category of
 * ports used by components exchanging data rather than calling providers for
 * their services.
 *
 * <p><strong>Description</strong></p>
 * 
 * An outbound port is associated with a <code>BasicDataRequiredI</code>
 * interface used to implement a basic interface to get data from a provider
 * component.  The primary way to get the data is the pull mode, where the
 * client component calls the provider through the <code>request()</code>
 * method which returns a piece of data.  But the port can also be used in a
 * push mode where the provider delivers the data to the client by calling a
 * <code>receive</code> method with a piece of data as parameter.
 * 
 * <p>Created on : 2011-11-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface			DataOutboundPortI
extends		OutboundPortI,
			DataRequiredI.PullI,		// to be called from the inside to
									// request data
			DataRequiredI.PushI		// to be called from the outside to
									// receive data
{
	// ------------------------------------------------------------------------
	// Self-properties management
	// ------------------------------------------------------------------------

	/**
	 * get the pull interface implemented by this port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	DataRequiredI.PullI.class.isAssignableFrom(return)
	 * </pre>
	 *
	 * @return			the implemented pull interface.
	 * @throws Exception	<i>to do.</i>
	 */
	public Class<?> 		getImplementedPullInterface() throws Exception ;

	/**
	 * get the push interface implemented by this port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	DataRequiredI.PushI.class.isAssignableFrom(return)
	 * </pre>
	 *
	 * @return			the implemented push interface.
	 * @throws Exception	<i>to do.</i>
	 */
	public Class<?> 		getImplementedPushInterface() throws Exception ;

	/**
	 * return the reference to the Java object that is implementing the
	 * services called through the data outbound port and its push interface;
	 * usually, it is an object implementing the owner component (i.e.,
	 * implementing the interface <code>ComponentI</code> and inheriting
	 * from <code>AbstractComponent</code>), but it can also be a plug-in
	 * instance held by the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	ret != null
	 * </pre>
	 *
	 * @return			the reference to the Java object that is implementing the services called through the data outbound port.
	 * @throws Exception	<i>todo.</i>
	 */
	public Object		getServiceProviderReference() throws Exception ;
}
//-----------------------------------------------------------------------------
