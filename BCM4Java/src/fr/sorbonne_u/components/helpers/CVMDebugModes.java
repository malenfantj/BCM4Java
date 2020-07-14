package fr.sorbonne_u.components.helpers;

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

// -----------------------------------------------------------------------------
/**
 * The enumeration <code>CVMDebugModes</code> defines the different debug
 * modes that can be set on a BCM virtual machine to trigger traces on
 * the corresponding processes in the execution of a program (publishing
 * ports, calling methods across ports and connectors, etc.).
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * BCM provides a trace and logging mechanism to help debug programs. Indeed,
 * as threaded and distributed programs, BCM component-based applications are
 * difficult top debug. Java debuggers rarely handle correctly thread
 * interruptions, so putting breakpoints to inspect the state of the program
 * do not work.
 * </p>
 * <p>
 * The class <code>AbstractComponent</code> describes the component tracing and
 * logging facility. The main method is logMessage which allows to put in the
 * code logging instructions to get values of variables (state of the program)
 * at important points in the control flow. To avoid putting and removing such
 * instructions when going from debugging to testing and then to production,
 * it is possible to put them in if statements according to debugging modes.
 * </p>
 * <p>
 * The <code>CVMDebugModes</code> enumeration defines the core debug modes
 * used in the BCM kernel. Adding a debug mode into the set
 * <code>AbstractCVM.DEBUG_MODES</code> enables the logging associated to
 * the corresponding mode. The set <code>AbstractCVM.DEBUG_MODES</code>
 * types its content with the interface <code>CVMDebugModesI</code> so
 * that users can extend the debugging modes with its own ones.
 * </p>
 * 
 * <p>Created on : 2018-08-27</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public enum				CVMDebugModes
implements	CVMDebugModesI
{
	/** trace the actions done by registries.								*/
	REGISTRY,
	/** trace the actions done by the distributed CVM cyclic barrier.		*/
	CYCLIC_BARRIER,
	/** trace the actions made to manage the life cycle of the component
	 *  virtual machine.													*/
	LIFE_CYCLE,
	/** trace the actions made for the management of component interfaces.	*/
	INTERFACES,
	/** trace the actions made for the management of ports.					*/
	PORTS,
	/** trace the actions pertaining to the connections of ports.			*/
	CONNECTING,
	/** trace the actions made when calling component services through
	 *  ports, connectors and executor services.							*/
	CALLING,
	/** trace the actions when managing executor services.					*/
	EXECUTOR_SERVICES,
	/** trace the actions done for plug-in management.						*/
	PLUGIN,
	/** trace the actions when managing subcomponents.						*/
	INNER_COMPONENTS
}
// -----------------------------------------------------------------------------
