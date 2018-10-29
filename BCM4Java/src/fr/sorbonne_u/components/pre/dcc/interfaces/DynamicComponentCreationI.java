package fr.sorbonne_u.components.pre.dcc.interfaces;

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

import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;

//-----------------------------------------------------------------------------
/**
 * The interface <code>DynamicComponentCreationI</code> defines the component
 * creation service offered and required interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * This interface is meant to be implemented as an offered interface by
 * dynamic component creator components, and used as required interface by
 * components that want to use this service.
 * 
 * <p>Created on : 2014-03-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface			DynamicComponentCreationI
extends		OfferedI,
			RequiredI
{
	/**
	 * create a component from the class of the given class name, invoking its
	 * constructor matching the given parameters ; beware not to have parameters
	 * of base types (<code>int</code>, <code>boolean</code>, etc.) but rather
	 * reified versions (<code>Integer</code>, <code>Boolean</code>, etc.) in
	 * order for the reflection-based instantiation to work smoothly.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	classname != null and constructorParams != null
	 * post	return != null
	 * </pre>
	 *
	 * @param classname			name of the class from which to instantiate the component.
	 * @param constructorParams	parameters to be passed to the constructor of the component instantiation class.
	 * @return					URI of the reflection inbound port of the created component.
	 * @throws Exception			<i>todo.</i>
	 */
	public String		createComponent(
		String classname,
		Object[] constructorParams
		) throws Exception ;
}
//-----------------------------------------------------------------------------
