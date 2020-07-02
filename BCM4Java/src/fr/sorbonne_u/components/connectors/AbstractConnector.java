package fr.sorbonne_u.components.connectors;

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

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractConnector</code> partially implements an abstract
 * connector between two components by assuming that the offering component
 * implements the offered interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 2011-11-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractConnector
implements	ConnectorI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** port of the component providing the service.						*/
	protected OfferedCI		offering ;
	/** URI of the offering port.											*/
	protected String		offeringPortURI;
	/** true if the offering port runs on a remote JVM.						*/
	protected boolean		isOfferingRemote;
	/** port of the component requiring the service.						*/
	protected RequiredCI	requiring;
	/** URI of the requiring port.											*/
	protected String		requiringPortURI;
	/** true if the requiring port runs on a remote JVM.					*/
	protected boolean		isRequiringRemote;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * check the implementation invariant of the class on the given instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param c				instance to be checked.
	 * @throws Exception	<i>to do</i>.
	 */
	public static void	checkImplementationInvariant(AbstractConnector c)
	throws Exception
	{
		assert	c != null;

		synchronized (c) {
			assert	!c.connected() ||
								(c.offering != null && c.requiring != null) :
						new ImplementationInvariantException(
								"!c.connected() || " + 
								"(c.offering != null && c.requiring != null)");
			assert	(c.offering == null) == (c.offeringPortURI == null) :
						new ImplementationInvariantException(
								"(offering == null) == "
								+ "(offeringPortURI == null)");
			assert	(c.requiring == null) == (c.requiringPortURI == null) :
						new ImplementationInvariantException(
								"(requiring == null) == "
								+ "(requiringPortURI == null)");
		}
	}

	/**
	 * create a connector.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 */
	public				AbstractConnector()
	{
		super();

		try {
			AbstractConnector.checkImplementationInvariant(this);
		} catch (Exception e) {
			// this is not to create to much changes in existing code.
			throw new RuntimeException(e);
		}
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * determine and memorise which of the offering and the requiring ports
	 * are remote compared to the connector.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code requiringPortURI != null && offeringPortURI != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param requiringPortURI	URI of the requiring port.
	 * @param offeringPortURI	URI of the offering port.
	 * @throws Exception		<i>to do</i>.
	 */
	protected void		setRemoteStatus(
		String requiringPortURI,
		String offeringPortURI
		) throws Exception
	{
		assert	requiringPortURI != null && offeringPortURI != null :
					new PreconditionException(
							"requiringPortURI != null "
									+ "&& offeringPortURI != null");

		this.offeringPortURI = offeringPortURI;
		this.isOfferingRemote =
				!AbstractCVM.isPublishedInLocalRegistry(offeringPortURI);
		this.requiringPortURI = requiringPortURI;
		this.isRequiringRemote =
				!AbstractCVM.isPublishedInLocalRegistry(requiringPortURI);
	}

	/**
	 * @see fr.sorbonne_u.components.connectors.ConnectorI#connected()
	 */
	@Override
	public boolean		connected() throws Exception
	{
		return this.offering != null && this.requiring != null;
	}

	/**
	 * @return the isRemote
	 */
	@Override
	public boolean		isRemote() throws Exception
	{
		return this.isRequiringPortRemote() || this.isOfferringPortRemote();
	}

	/**
	 * return the URI of the first peer port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	the URI of the requiring port.
	 */
	protected String	getRequiringPortURI()
	{
		return this.requiringPortURI;
	}

	/**
	 * @see fr.sorbonne_u.components.connectors.ConnectorI#isRequiringPortRemote()
	 */
	@Override
	public boolean		isRequiringPortRemote() throws Exception
	{
		return this.isRequiringRemote;
	}

	/**
	 * return the URI of the requiring port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	the URI of the offering port.
	 */
	protected String	getOfferingPortURI()
	{
		return this.offeringPortURI;
	}

	/**
	 * @see fr.sorbonne_u.components.connectors.ConnectorI#isOfferringPortRemote()
	 */
	@Override
	public boolean		isOfferringPortRemote() throws Exception
	{
		return this.isOfferingRemote;
	}

	/**
	 * @see fr.sorbonne_u.components.connectors.ConnectorI#connect(fr.sorbonne_u.components.interfaces.OfferedCI, fr.sorbonne_u.components.interfaces.RequiredCI)
	 */
	@Override
	public void			connect(OfferedCI offering, RequiredCI requiring)
	throws	Exception
	{
		assert	!this.connected() :
					new PreconditionException("!connected()");
		assert	offering != null && requiring != null :
					new PreconditionException(
							"requiringPortURI != null "
									+ "&& offeringPortURI != null");

		this.offering = offering;
		this.requiring = requiring;
		this.setRemoteStatus(
				((PortI)this.requiring).getPortURI(),
				((PortI)this.offering).getPortURI());

		AbstractConnector.checkImplementationInvariant(this);
		assert	this.connected() : new PostconditionException("connected()");
	}
	/**
	 * @see fr.sorbonne_u.components.connectors.ConnectorI#obeyConnection(fr.sorbonne_u.components.ports.PortI, java.lang.String)
	 */
	@Override
	public void			obeyConnection(PortI sender, String ccname)
	throws Exception
	{
		assert	sender != null && ccname != null :
					new PreconditionException(
							"sender != null && ccname != null");

		if (sender == this.requiring) {
			((PortI)this.offering).obeyConnection(sender.getPortURI(), ccname);
		} else {
			((PortI)this.requiring).obeyConnection(sender.getPortURI(), ccname);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.connectors.ConnectorI#obeyConnection(fr.sorbonne_u.components.ports.PortI, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			obeyConnection(PortI sender, ConnectorI connector)
	throws Exception
	{
		assert	sender != null && connector != null :
					new PreconditionException(
							"sender != null && connector != null");

		if (sender == this.requiring) {
			if (this.isOfferringPortRemote()) {
				((PortI)this.offering).
						obeyConnection(sender.getPortURI(),
									connector.getClass().getCanonicalName());
			} else {
				((PortI)this.offering).
						obeyConnection(sender.getPortURI(), connector);
			}
		} else {
			if (this.isRequiringPortRemote()) {
				((PortI)this.requiring).
						obeyConnection(sender.getPortURI(),
								connector.getClass().getCanonicalName());
			} else {
				((PortI)this.requiring).
						obeyConnection(sender.getPortURI(), connector);
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.components.connectors.ConnectorI#disconnect()
	 */
	@Override
	public void			disconnect() throws Exception
	{
		assert	this.connected() :
					new PreconditionException("connected()");

		this.offering = null;
		this.requiring = null;
		this.offeringPortURI = null;
		this.requiringPortURI = null;

		AbstractConnector.checkImplementationInvariant(this);
		assert	!this.connected() : new PostconditionException("!connected()");
	}

	/**
	 * @see fr.sorbonne_u.components.connectors.ConnectorI#obeyDisconnection(fr.sorbonne_u.components.ports.PortI)
	 */
	@Override
	public void			obeyDisconnection(PortI sender)
	throws Exception
	{
		assert	sender != null : new PreconditionException("sender != null");

		if (sender == this.requiring) {
			((PortI)this.offering).obeyDisconnection();
		} else {
			((PortI)this.requiring).obeyDisconnection();
		}
	}
}
// -----------------------------------------------------------------------------
