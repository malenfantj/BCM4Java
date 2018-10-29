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
 * The interface <code>DistributedComponentVirtualMachineI</code> defines the
 * common behaviours of component virtual machines that are deployed on multiple
 * JVM, themselves possibly running on multiple hosts.
 *
 * <p><strong>Description</strong></p>
 * 
 * DCVM are meant to be deployed over distinct Java virtual machines executing
 * on a single host or a network of hosts.  As an object, a DCVM site is
 * executed on each JVM on which it is deployed, so it creates on each site only
 * the local components and connect them to the components created on other
 * sites using RMI connections published in a RMI registry.
 * 
 * The deployment obeys to three distinct phases:
 * <ol>
 * <li>initialisation of the RMI registry references;
 * <li>instantiation of the local static components and publication of their
 *   ports on the RMI registry.</li>
 * <li>interconnection of the components, querying the RMI registry when
 *   necessary to connect distant components.</li>
 * </ol>
 * 
 * Only when these three phases are completed the starting the execution of the
 * static components can be done.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true		// TODO
 * </pre>
 * 
 * <p>Created on : 2012-05-22</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface			DistributedComponentVirtualMachineI
extends		ComponentVirtualMachineI
{
	/**
	 * initialise the CVM (mainly the RMI registry, when necessary).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!this.isInitialised()
	 * post	this.isInitialised()
	 * </pre>
	 *
	 * @throws Exception	<i>to do.</i>
	 */
	public void			initialise() throws Exception ;

	/**
	 * instantiate all of the local static components and publish the ports for
	 * connections in the registry.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isInitialised()
	 * post	this.isIntantiatedAndPublished()
	 * </pre>
	 * @throws Exception	<i>to do.</i>
	 */
	public void			instantiateAndPublish() throws Exception ;

	/**
	 * connects the components on the current site and with components on other
	 * sites through the registry.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isIntantiatedAndPublished()
	 * post	this.isInterconnected()
	 * </pre>
	 * @throws Exception	<i>to do.</i>
	 */
	public void			interconnect() throws Exception ;
}
//-----------------------------------------------------------------------------
