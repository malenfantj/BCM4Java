package fr.sorbonne_u.components.examples.edp_cs.connections;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.examples.edp_cs.components.URIProvider;
import fr.sorbonne_u.components.examples.basic_cs.interfaces.URIProviderCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>URIProviderInboundPort</code> defines the inbound port
 * exposing the interface <code>URIProviderI</code> for components of
 * type <code>URIProvider</code>.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This is exactly the same code as in the basic client/server example. It is
 * copied here because the code of the component {@code URIProvider} is modified
 * in this variant, hence the port must import the new variant of this component
 * rather than the component defined in the basic client/server packages.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-01-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			URIProviderInboundPort
extends		AbstractInboundPort
implements	URIProviderCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** required by UnicastRemonteObject.									*/
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * create the port under some given URI and for a given owner.
	 * 
	 * The constructor for <code>AbstractInboundPort</code> requires the
	 * interface that the port is implementing as an instance of
	 * <code>java.lang.CLass</code>, but this is statically known so
	 * the constructor does not need to receive the information as parameter.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && owner instanceof URIProvider}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri			URI under which the port will be published.
	 * @param owner			component owning the port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				URIProviderInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		// the implemented interface is statically known
		super(uri, URIProviderCI.class, owner);

		assert	uri != null && owner instanceof URIProvider :
				new PreconditionException(
						"uri != null && owner instanceof URIProvider");
	}

	/**
	 * create the port for a given owner.
	 * 
	 * The constructor for <code>AbstractInboundPort</code> requires the
	 * interface that the port is implementing as an instance of
	 * <code>java.lang.CLass</code>, but this is statically known so
	 * the constructor does not need to receive the information as parameter.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && owner instanceof URIProvider}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param owner		component owning the port.
	 * @throws Exception	<i>todo.</i>
	 */
	public				URIProviderInboundPort(
		ComponentI owner
		) throws Exception
	{
		// the implemented interface is statically known
		super(URIProviderCI.class, owner);

		assert	owner instanceof URIProvider :
			new PreconditionException(
					"uri != null && owner instanceof URIProvider");
	}

	//-------------------------------------------------------------------------
	// Methods
	//-------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.examples.basic_cs.interfaces.URIProviderCI#provideURI()
	 */
	@Override
	public String		provideURI() throws Exception
	{
		// the handleRequestSync wait for the result before returning to the
		// caller; hence it is a synchronous remote method invocation.
		// Here, we illustrate the use of Java 8 lambda expressions in
		// defining the service to be called (to be contrasted with the
		// anonymous class used in provideURIs.
		return this.getOwner().handleRequest(
						owner -> ((URIProvider)owner).provideURIService());
	}

	/**
	 * @see fr.sorbonne_u.components.examples.basic_cs.interfaces.URIProviderCI#provideURIs(int)
	 */
	@Override
	public String[]		provideURIs(final int numberOfRequestedURIs)
	throws Exception
	{
		// use another way to call the service method within by explicitly
		// creating a task submitted to the component
		return this.getOwner().handleRequest(
				new AbstractComponent.AbstractService<String[]>() {
					@Override
					public String[] call() throws Exception {
						return ((URIProvider)this.getServiceOwner()).
								provideURIsService(numberOfRequestedURIs);
					}
				}) ;
	}
}
// -----------------------------------------------------------------------------
