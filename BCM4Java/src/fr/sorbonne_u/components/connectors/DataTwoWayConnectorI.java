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

// -----------------------------------------------------------------------------
/**
 * The interface <code>DataTwoWayConnectorI</code> is the generic interface for
 * connectors that mediate between requiring and offering components that
 * exchange data rather than calling services from each others, but using the
 * same interface on both sides and act as peers in the exchange.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2012-01-23</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		DataTwoWayConnectorI
extends		DataTwoWayCI
{
	/**
	 * translates data as defined by the second two way interface into data as
	 * defined in the first two way interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	{@code (d == null) == (ret == null)}
	 * </pre>
	 *
	 * @param d	data to be translated
	 * @return	data resulting from the translation
	 */
	public DataTwoWayCI.DataI 	second2first(DataTwoWayCI.DataI d);

	/**
	 * translates data as defined by the first two way interface into data as
	 * defined in the second two way interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	{@code (d == null) == (ret == null)}
	 * </pre>
	 *
	 * @param d	data to be translated
	 * @return	data resulting from the translation
	 */
	public DataTwoWayCI.DataI	first2second(DataTwoWayCI.DataI d);
}
// -----------------------------------------------------------------------------
