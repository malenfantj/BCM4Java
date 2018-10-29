package fr.sorbonne_u.components.cvm;

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

//-----------------------------------------------------------------------------
/**
 * The class <code>CVMState</code> describes the different states in which
 * a component virtual machine can be during its life-cycle.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-03-10</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public enum				CVMState
implements	CVMStateI
{
	CREATED,				// the component virtual machine has been created.
	INITIALISED,			// the component virtual machine has been initialised.
	INSTANTIATED_AND_PUBLISHED,	// all of the static components have been
								// instantiated and their ports published when
								// necessary.
	INTERCONNECTED,		// all of the static components have their ports
						// connected when necessary.
	DEPLOYMENT_DONE,		// all of the static component have been created
						// and connected when necessary
	START_DONE,			// all of the static components have been started.
	FINALISE_DONE,		// all of the components have been finalised.
	SHUTDOWN,			// all of the components have been shutdown.
	TERMINATED			// all of the components have been terminated.
}
//-----------------------------------------------------------------------------
