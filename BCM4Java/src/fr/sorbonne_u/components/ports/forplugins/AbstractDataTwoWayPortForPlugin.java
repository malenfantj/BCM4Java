package fr.sorbonne_u.components.ports.forplugins;

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

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractDataTwoWayPort;

//-----------------------------------------------------------------------------
/**
 * The class <code>AbstractDataTwoWayPortForPlugin</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-08-28</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractDataTwoWayPortForPlugin
extends		AbstractDataTwoWayPort
{
	// ------------------------------------------------------------------------
	// Instance variables and constructors
	// ------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	protected final String		pluginURI ;

	/**
	 * create and initialise data two-way ports, with a given URI and a
	 * given plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param pluginURI				URI of the plug-in implementing the services.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>to do.</i>
	 */
	public				AbstractDataTwoWayPortForPlugin(
		String uri,
		Class<?> implementedInterface,
		String pluginURI,
		ComponentI owner
		) throws Exception
	{
		super(uri, implementedInterface, owner) ;

		assert	pluginURI != null ;
		assert	owner.isInstalled(pluginURI) ;

		this.pluginURI = pluginURI ;
	}

	/**
	 * create and initialise data two-way ports, with a given plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param implementedInterface	pull interface implemented by this port.
	 * @param pluginURI				URI of the plug-in implementing the services.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>to do.</i>
	 */
	public				AbstractDataTwoWayPortForPlugin(
		Class<?> implementedInterface,
		String pluginURI,
		ComponentI owner
		) throws Exception
	{
		super(implementedInterface, owner);

		assert	pluginURI != null ;
		assert	owner.isInstalled(pluginURI) ;

		this.pluginURI = pluginURI ;
	}
}
//-----------------------------------------------------------------------------
