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

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.OutboundPortI;

// -----------------------------------------------------------------------------
/**
 * The interface <code>DynamicConnectionDescriptorI</code> allows client side
 * components that wants to do dynamic connections to define the way to
 * create the client side port and the name of the class of connector to be
 * used in the dynamic connection plug-in.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2017-02-15</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		DynamicConnectionDescriptorI
{
	/**
	 * in a dynamic connection, create the client side outbound port that
	 * will then be connected.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	requiredInterface != null
	 * pre	owner != null
	 * post	result != null
	 * </pre>
	 *
	 * @param requiredInterface	client-side interface through which the connection is made.
	 * @param owner				the component owning the plug-in on which the port will be created.
	 * @return					the port that was created.
	 */
	public OutboundPortI	createClientSideDynamicPort(
		Class<?> requiredInterface,
		ComponentI owner
		) ;

	/**
	 * provide the connector class name required to connect over the dynamic
	 * connection.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	requiredInterface != null
	 * post	result != null
	 * </pre>
	 *
	 * @param requiredInterface	client-side interface through which the connection is made.
	 * @return					a connector class name to be used in the dynamic connection.
	 */
	public String		dynamicConnectorClassName(
		Class<?> requiredInterface
		) ;
}
// -----------------------------------------------------------------------------
