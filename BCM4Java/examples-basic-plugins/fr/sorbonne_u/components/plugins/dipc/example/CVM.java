package fr.sorbonne_u.components.plugins.dipc.example;

import fr.sorbonne_u.components.AbstractComponent;

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

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.plugins.dipc.example.components.ClientComponent;
import fr.sorbonne_u.components.plugins.dipc.example.components.ServerComponent;

//------------------------------------------------------------------------------
/**
 * The class <code>CVM</code> deploys and executes the data interfaces
 * push control plug-in example.
 *
 * <p><strong>Description</strong></p>
 * 
 * The client-side and server-side components are merely created and
 * deployed. Their behaviour implements the plug-ins creation and
 * installation as well as the connections and the execution scenario.
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
public class				CVM
extends		AbstractCVM
{
	public					CVM() throws Exception
	{
		super() ;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#deploy()
	 */
	@Override
	public void				deploy() throws Exception
	{
		// ---------------------------------------------------------------------
		// Configuration phase
		// ---------------------------------------------------------------------

		// debugging mode configuration; comment and uncomment the line to see
		// the difference
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.LIFE_CYCLE);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.INTERFACES);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.PORTS);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CONNECTING);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CALLING);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.EXECUTOR_SERVICES);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.PLUGIN);

		// ---------------------------------------------------------------------
		// Creation phase
		// ---------------------------------------------------------------------

		String clientURI =
				AbstractComponent.createComponent(
						ClientComponent.class.getCanonicalName(),
						new Object[]{});
		this.toggleTracing(clientURI);

		String serverURI =
				AbstractComponent.createComponent(
						ServerComponent.class.getCanonicalName(),
						new Object[]{});
		this.toggleTracing(serverURI);

		super.deploy() ;
	}

	public static void		main(String[] args)
	{
		CVM c;
		try {
			c = new CVM();
			CVM.DEBUG_MODE.add(CVMDebugModes.PLUGIN);
			c.startStandardLifeCycle(5000L) ;
			Thread.sleep(10000L) ;
			System.exit(0) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
//------------------------------------------------------------------------------
