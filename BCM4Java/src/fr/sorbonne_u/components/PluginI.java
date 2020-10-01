package fr.sorbonne_u.components;

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

import java.io.Serializable;

// -----------------------------------------------------------------------------
/**
 * The interface <code>PluginI</code> defines the basic implementation
 * services of component plug-ins seen as objects.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This interface is implemented directly or indirectly by all objects
 * implementing a plug-in. The default 
 * </p>
 * 
 * <p>Created on : 2016-02-05</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		PluginI
extends		Serializable
{
	/**
	 * return the URI of this plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	the URI of this plug-in.
	 */
	public String		getPluginURI();

	/**
	 * set the plug-in URI; this can be done only once to define the URI,
	 * attempts to redo it will raise an exception.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code getPluginURI() == null}
	 * pre	{@code uri != null}
	 * post	{@code getPluginURI() != null}
	 * </pre>
	 *
	 * @param uri			the URI that will become the one of the plug-in.
	 */
	public void			setPluginURI(String uri);

	/**
	 * get the URI of the executor service used to execute services on the
	 * owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the URI of the executor service used to execute services on the owner component.
	 */
	public String		getPreferredExecutionServiceURI();

	/**
	 * get the index of the executor service used to execute services on the
	 * owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the index of the executor service used to execute services on the owner component.
	 */
	public int			getPreferredExecutionServiceIndex();

	/**
	 * set the URI of the executor service used to execute simulations; this
	 * method must be called at most once before installing the plug-in on
	 * the owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code executorServiceURI != null}
	 * pre	{@code getOwner() == null || getOwner().validExecutorServiceURI(executorServiceURI)} 
	 * post	{@code executorServiceURI.equals(getExecutionServiceURI())}
	 * </pre>
	 *
	 * @param executorServiceURI	URI of the executor service used to execute simulations.
	 */
	public void			setPreferredExecutionServiceURI(
		String executorServiceURI
		);

	/**
	 * initialise the plug-in reference to its owner component and add to the
	 * component every specific information, ports, etc. required to run the
	 * plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code getPluginURI() != null}
	 * pre	{@code !owner.isInstalled(getPluginURI())}
	 * pre 	{@code getPreferredExecutionServiceURI() == null || owner.validExecutorServiceURI(getPreferredExecutionServiceURI())}
	 * post	{@code owner == getOwner()}
	 * post	{@code getPreferredExecutionServiceURI() == null || getPreferredExecutionServiceIndex() == ((AbstractComponent)owner).getExecutorServiceIndex(getPreferredExecutionServiceURI())}
	 * </pre>
	 *
	 * @param owner			component on which the plug-in must be installed.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			installOn(ComponentI owner) throws Exception ;

	/**
	 * initialise the plug-in by adding to the owner component every
	 * specific information, ports, etc. required to run the plug-in;
	 * subclasses should add any other initialisation necessary to make
	 * the plug-in work in the context of its owner.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isInitialised()}
	 * post	{@code isInitialised()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			initialise() throws Exception;

	/**
	 * return true if the plug-in is fully initialised and ready to execute in
	 * the context of its owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return				true if the plug-in is fully initialised.
	 */
	public boolean		isInitialised();

	/**
	 * finalise the plug-in at least when the owner component is finalised.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isInitialised()
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	default void		finalise() throws Exception
	{
		// By default, do nothing.
	};

	/**
	 * uninstall the plug-in from its owner component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>todo.</i>
	 */
	default void		uninstall() throws Exception
	{
		// By default, do nothing.
	};
}
// -----------------------------------------------------------------------------

