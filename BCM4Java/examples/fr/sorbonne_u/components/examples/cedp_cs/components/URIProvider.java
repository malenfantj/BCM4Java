package fr.sorbonne_u.components.examples.cedp_cs.components;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.examples.cedp_cs.connections.CompositeURIServiceEndpoint;
import fr.sorbonne_u.components.examples.cedp_cs.interfaces.MultipleURIProviderCI;
import fr.sorbonne_u.components.examples.cedp_cs.interfaces.SingleURIProviderCI;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.util.stream.Stream;

// -----------------------------------------------------------------------------
/**
 * The class <code>URIProvider</code> implements a component that provides
 * URI creation services.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code	this.uriPrefix != null}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code	c.isOfferedInterface(SingleURIProviderI.class)}
 * invariant	{@code	c.isOfferedInterface(MultipleURIProviderI.class)}
 * </pre>
 * 
 * <p>Created on : 2025-01-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered = {SingleURIProviderCI.class,
							  MultipleURIProviderCI.class})
public class			URIProvider
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constructors and instance variables
	// -------------------------------------------------------------------------

	/**	a string prefix that will identify the URI provider.				*/
	protected String						uriPrefix;
	/** composite end point used to offer provider services.				*/
	protected CompositeURIServiceEndpoint	compositeURIServiceEndpoint;

	/**
	 * check the invariant of the class on an instance.
	 *
	 * @param c	the component to be tested.
	 */
	protected static void	checkInvariant(URIProvider c)
	{
		assert	c.uriPrefix != null :
				new InvariantException("The URI prefix is null!");
		assert	c.isOfferedInterface(SingleURIProviderCI.class) :
				new InvariantException(
						"The URI component should offer the interface "
						+ "SingleURIProviderI!");
		assert	c.isOfferedInterface(MultipleURIProviderCI.class) :
				new InvariantException(
						"The URI component should offer the interface "
						+ "MultipleURIProviderI!");
	}

	/**
	 * create a component with a given uri prefix and that will expose its
	 * service through the given composite end point.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uriPrefix != null and compositeURIServiceEndpoint != null != null
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uriPrefix						the URI prefix of this provider.
	 * @param compositeURIServiceEndpoint	composite end point used to offer provider services.
	 * @throws Exception					<i>to do</i>.
	 */
	protected				URIProvider(
		String uriPrefix,
		CompositeURIServiceEndpoint compositeURIServiceEndpoint
		) throws Exception
	{
		// the reflection inbound port URI is the URI of the component
		super(uriPrefix, 1, 0);

		assert	uriPrefix != null :
				new PreconditionException("uriPrefix can't be null!");
		assert	compositeURIServiceEndpoint != null :
				new PreconditionException(
						"compositeURIServiceEndpoint can't be null!");

		this.uriPrefix = uriPrefix;

		// if the offered interfaces are not declared in an annotation on
		// the component class, it can be added manually with the
		// following instructions:
		//this.addOfferedInterface(SingleURIProviderI.class);
		//this.addOfferedInterface(MultipleURIProviderI.class);

		// create the port that exposes the offered interface with the
		// given URI to ease the connection from client components.
		this.compositeURIServiceEndpoint = compositeURIServiceEndpoint;
		compositeURIServiceEndpoint.initialiseServerSide(this);

		if (AbstractCVM.isDistributed) {
			this.getLogger().setDirectory(System.getProperty("user.dir"));
		} else {
			this.getLogger().setDirectory(System.getProperty("user.home"));
		}
		this.getTracer().setTitle("provider");
		this.getTracer().setRelativePosition(1, 0);

		URIProvider.checkInvariant(this);
		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
		assert	this.uriPrefix.equals(uriPrefix) :
				new PostconditionException(
						"The URI prefix has not been initialised!");
	}

	//--------------------------------------------------------------------------
	// Component life-cycle
	//--------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public void			start() throws ComponentStartException
	{
		this.logMessage("starting provider component.");
		super.start();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		this.logMessage("stopping provider component.");
		this.printExecutionLogOnFile("provider");
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
			this.compositeURIServiceEndpoint.cleanUpServerSide();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdownNow()
	 */
	@Override
	public void			shutdownNow() throws ComponentShutdownException
	{
		try {
			this.compositeURIServiceEndpoint.cleanUpServerSide();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdownNow();
	}

	//--------------------------------------------------------------------------
	// Component internal services
	//--------------------------------------------------------------------------

	/**
	 * produce and return an URI beginning with a substring that identifies the
	 * provider.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * Starting point is to define the service methods of the server component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && ret.startsWith(this.uriPrefix))}
	 * </pre>
	 *
	 * @return	the produced URI.
	 */
	public String		provideURIService()
	{
		this.logMessage("provider creates a new URI and returns it.");
		// see http://www.asciiarmor.com/post/33736615/java-util-uuid-mini-faq
		String ret = this.uriPrefix + "-" +
									java.util.UUID.randomUUID().toString();

		assert	ret != null : new PostconditionException("Result is null!");
		assert	ret.startsWith(this.uriPrefix) :
					new PostconditionException("Result does not begin by the"
														+ " URI prefix!");

		return ret;
	}

	/**
	 * produce and return <code>n</code> URIs, each beginning with a substring
	 * that identifies the provider.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code n > 0}
	 * post	{@code return != null && ret.size() == n}
	 * post	{@code Stream.of(ret).allMatch(uri -> uri != null)}
	 * </pre>
	 *
	 * @param n				number of requested URIs.
	 * @return				array of {@code n} URIs implemented as strings.
	 * @throws Exception	<i>to do</i>.
	 */
	public String[]		provideURIsService(final int n) throws Exception
	{
		assert	n > 0 :
				new PreconditionException(
						"n must be greater than 0"
						+ " but is equal to: " + n + "!");

		this.logMessage("provider creates " + n + " new URI and returns them.");
		String[] ret = new String[n];
		for (int i = 0 ; i < n ; i++) {
			// see http://www.asciiarmor.com/post/33736615/java-util-uuid-mini-faq
			ret[i] = this.uriPrefix + "-" +
									java.util.UUID.randomUUID().toString();
		}

		assert	ret != null :
				new PostconditionException("the result is null!");
		assert	ret.length == n :
				new PostconditionException("The length of the result is not n!");
		assert	Stream.of(ret).allMatch(uri -> uri != null) :
				new PostconditionException(
						"Stream.of(ret).allMatch(uri -> uri != null)");

		return ret;
	}
}
// -----------------------------------------------------------------------------
