package fr.sorbonne_u.components.examples.basic_cs.components;

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
import fr.sorbonne_u.components.examples.basic_cs.interfaces.URIProviderCI;
import fr.sorbonne_u.components.examples.basic_cs.ports.URIProviderInboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>URIProvider</code> implements a component that provides
 * URI creation services.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		this.uriPrefix != null
 * invariant		c.isOfferedInterface(URIProviderI.class)
 * </pre>
 * 
 * <p>Created on : 2014-01-22</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered = {URIProviderCI.class})
public class			URIProvider
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constructors and instance variables
	// -------------------------------------------------------------------------

	/**	a string prefix that will identify the URI provider.				*/
	protected String		uriPrefix;

	/**
	 * check the invariant of the class on an instance.
	 *
	 * @param c	the component to be tested.
	 */
	protected static void	checkInvariant(URIProvider c)
	{
		assert	c.uriPrefix != null :
					new InvariantException("The URI prefix is null!");
		assert	c.isOfferedInterface(URIProviderCI.class) :
					new InvariantException("The URI component should "
							+ "offer the interface URIProviderI!");
	}

	/**
	 * create a component with a given uri prefix and that will expose its
	 * service through a port of the given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uriPrefix != null and providerPortURI != null
	 * post	this.uriPrefix.equals(uriPrefix)
	 * post	this.isPortExisting(providerPortURI)
	 * post	this.findPortFromURI(providerPortURI).getImplementedInterface().equals(URIProviderI.class)
	 * post	this.findPortFromURI(providerPortURI).isPublished()
	 * </pre>
	 *
	 * @param uriPrefix			the URI prefix of this provider.
	 * @param providerPortURI	the URI of the port exposing the service.
	 * @throws Exception			<i>todo.</i>
	 */
	protected				URIProvider(
		String uriPrefix,
		String providerPortURI
		) throws Exception
	{
		// the reflection inbound port URI is the URI of the component
		super(uriPrefix, 1, 0) ;

		assert	uriPrefix != null :
					new PreconditionException("uri can't be null!");
		assert	providerPortURI != null :
					new PreconditionException("providerPortURI can't be null!");

		this.uriPrefix = uriPrefix ;

		// if the offered interface is not declared in an annotation on
		// the component class, it can be added manually with the
		// following instruction:
		//this.addOfferedInterface(URIProviderI.class) ;

		// create the port that exposes the offered interface with the
		// given URI to ease the connection from client components.
		PortI p = new URIProviderInboundPort(providerPortURI, this);
		// publish the port
		p.publishPort();

		if (AbstractCVM.isDistributed) {
			this.getLogger().setDirectory(System.getProperty("user.dir"));
		} else {
			this.getLogger().setDirectory(System.getProperty("user.home"));
		}
		this.getTracer().setTitle("provider");
		this.getTracer().setRelativePosition(1, 0);

		URIProvider.checkInvariant(this) ;
		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
		assert	this.uriPrefix.equals(uriPrefix) :
					new PostconditionException("The URI prefix has not "
												+ "been initialised!");
		assert	this.isPortExisting(providerPortURI) :
					new PostconditionException("The component must have a "
							+ "port with URI " + providerPortURI);
		assert	this.findPortFromURI(providerPortURI).
					getImplementedInterface().equals(URIProviderCI.class) :
					new PostconditionException("The component must have a "
							+ "port with implemented interface URIProviderI");
		assert	this.findPortFromURI(providerPortURI).isPublished() :
					new PostconditionException("The component must have a "
							+ "port published with URI " + providerPortURI);
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
			PortI[] p = this.findPortsFromInterface(URIProviderCI.class);
			p[0].unpublishPort();
		} catch (Exception e) {
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
			PortI[] p = this.findPortsFromInterface(URIProviderCI.class);
			p[0].unpublishPort();
		} catch (Exception e) {
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
	 * Starting point is to define the service methods of the server component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	ret != null and ret.startsWith(this.uriPrefix))
	 * </pre>
	 *
	 * @return	the produced URI
	 */
	public String		provideURIService()
	{
		this.logMessage("provider create a new URI and returns it.") ;
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
	 * pre	n &gt; 0
	 * post	ret != null and ret.size() == n
	 * post	post forall i in 0..numberOfRequestedURIs-1, ret[i] != null and ret[i].startsWith(this.uriPrefix))
	 * </pre>
	 *
	 * @param n	number of requested URIs.
	 * @return	array of <code>n</code> URIs considered as strings.
	 * @throws Exception	<i>todo.</i>
	 */
	public String[]		provideURIsService(final int n)
	throws Exception
	{
		assert	n > 0 : new PreconditionException("n must be greater than 0"
											+ " but equal to: " + n + "!");

		String[] ret = new String[n] ;
		for (int i = 0 ; i < n ; i++) {
			// see http://www.asciiarmor.com/post/33736615/java-util-uuid-mini-faq
			ret[i] = this.uriPrefix + "-" +
									java.util.UUID.randomUUID().toString();
		}

		assert	ret != null :
					new PostconditionException("the result is null!");
		assert	ret.length == n :
					new PostconditionException(
										"The length of the result is not n!");
		boolean allNonNull = true;
		for (int i = 0 ; allNonNull && i < n ; i++) {
			allNonNull = (ret[i] != null && ret[i].startsWith(this.uriPrefix));
		}
		assert	allNonNull :
					new PostconditionException("An URI is the result is null!");

		return ret;
	}
}
// -----------------------------------------------------------------------------
