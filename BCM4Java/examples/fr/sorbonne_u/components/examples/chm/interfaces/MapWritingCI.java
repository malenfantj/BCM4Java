package fr.sorbonne_u.components.examples.chm.interfaces;

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
 * The interface <code>MapWritingCI</code> defines services that change the
 * state of a map.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2019-01-22</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		MapWritingCI<K,V>
extends		RequiredCI,
			OfferedCI
{
	/**
	 * associate the given key to the given value in the map, returning the
	 * value previously associated to the key or null if none.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	key != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param key			the key to be associated to the given value.
	 * @param value			the value to be associated to the given key.
	 * @return				the value previously associated to the key or null if none.
	 * @throws Exception		<i>to do.</i>
	 */
	public V			put(K key, V value) throws Exception ;

	/**
	 * remove the value associated to the given key, returning the value
	 * previously associated to the key or null if none.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	key != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param key			key which association must be removed.
	 * @return				the value previously associated to the key or null if none.
	 * @throws Exception	<i>to do.</i>
	 */
	public V			remove(K key) throws Exception ;
}
// -----------------------------------------------------------------------------
