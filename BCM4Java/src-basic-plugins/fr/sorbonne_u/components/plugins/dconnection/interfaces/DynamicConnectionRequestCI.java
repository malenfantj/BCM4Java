package fr.sorbonne_u.components.plugins.dconnection.interfaces;

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

// -----------------------------------------------------------------------------
/**
 * The interface <code>DynamicConnectionRequestCI</code> is offered by
 * components that propose a dynamic connection through some other
 * dynamically connected interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * See the <code>fr.sorbonne_u.components.patterns.dconnection</code>
 * package documentation.
 * 
 * <p>Created on : 2013-01-23</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		DynamicConnectionRequestCI
extends		OfferedCI,
			RequiredCI
{
	/**
	 * provides the URI of a new port implementing some interface that is
	 * offered through a dynamic connection, so the receiver creates a port
	 * and sends its URI back.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	offeredInterface != null
	 * post	result != null
	 * </pre>
	 *
	 * @param offeredInterface	interface of the connection to be established.
	 * @return					the URI of an inbound port implementing <code>offeredInterface</code>.
	 * @throws Exception		<i>to do</i>.
	 */
	public String		requestDynamicPortURI(
		Class<? extends OfferedCI> offeredInterface
		) throws Exception ;

	/**
	 * remove the inbound port with the given URI that implements the given
	 * offered interface, if it exists.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	offeredInterface != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param offeredInterface	server-side offered interface.
	 * @param uri				URI of a previously created port.
	 * @throws Exception 		<i>to do</i>.
	 */
	public void			removeDynamicPort(
		Class<? extends OfferedCI> offeredInterface,
		String uri
		) throws Exception ;
}
// -----------------------------------------------------------------------------
