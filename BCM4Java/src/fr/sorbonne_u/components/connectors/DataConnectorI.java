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

import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;

// -----------------------------------------------------------------------------
/**
 * The interface <code>DataConnectorI</code> is the generic interface for
 * connectors that mediate between requiring and offering components that
 * exchange data rather than calling services from each others.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The interfaces <code>DataRequiredCI.PullCI</code> and
 * <code>DataOfferedCI.PushCI</code> contains methods that have different
 * signatures, so that the connector know which way the calls are going.
 * If extensions want to have part of their methods with the same
 * signature, techniques similar to the ones used in data two
 * way interfaces will be required to implement the connector.
 * If the two interfaces need to be the same then data two way
 * interfaces and connectors should be used instead.
 * </p>
 * 
 * <p>
 * When components exchange data rather than calling services from each
 * others, the offering component is considered as the producer of data
 * while the requiring one is considered as the consumer. And the two
 * interfaces are assumed to be developed independently, the data
 * interfaces in the required and the offered interfaces need not be the
 * same (not declare methods with the same signature). The connector is
 * therefore responsible for the implementation of translating methods
 * <code>to</code> and <code>from</code>.
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
public interface		DataConnectorI
extends		ConnectorI,
			DataRequiredCI.PullCI,	// to be called from the client side.
			DataOfferedCI.PushCI	// to be called from the provider side.
{
	/**
	 * translate data as defined by the required interface into data as
	 * defined in the offered interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no preconditions.
	 * post	{@code (d == null) == (return == null)}
	 * </pre>
	 *
	 * @param d	data to be translated
	 * @return	data resulting from the translation
	 */
	public DataOfferedCI.DataI	required2offered(DataRequiredCI.DataI d);

	/**
	 * translate data as defined by the offered interface into data as
	 * defined in the required interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	{@code (d == null) == (return == null)}
	 * </pre>
	 *
	 * @param d	data to be translated
	 * @return	data resulting from the translation
	 */
	public DataRequiredCI.DataI	offered2required(DataOfferedCI.DataI d);
}
// -----------------------------------------------------------------------------
