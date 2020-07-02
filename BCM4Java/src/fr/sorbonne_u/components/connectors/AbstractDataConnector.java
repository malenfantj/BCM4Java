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

import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractDataConnector</code> partially implements a basic
 * data connector between components exchanging data rather than calling each
 * others services.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This abstract data connector merely implements the translator methods
 * that mediate between data types from the required and the offered interfaces,
 * and the <code>connect</code> method to connect components through the
 * connector.
 * </p>
 * <p>
 * The <code>to</code> and <code>from</code> translating methods both assumes
 * that the actual class providing for the data to be exchanged implements
 * both <code>DataI</code> interfaces from the required and the offered
 * interfaces, so that a simple cast will do for the translation. Any other
 * behaviour must be implemented by redefining these methods.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2011-11-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractDataConnector
extends		AbstractConnector
implements	DataConnectorI
{	
	/**
	 * @see fr.sorbonne_u.components.connectors.DataConnectorI#required2offered(fr.sorbonne_u.components.interfaces.DataRequiredCI.DataI)
	 */
	@Override
	public DataOfferedCI.DataI required2offered(DataRequiredCI.DataI d)
	{
		// the data class must implement both the RequiredActuatorI
		// and the OfferedActuatorI ActuatorDataI interfaces.
		return (DataOfferedCI.DataI) d;
	}

	/**
	 * @see fr.sorbonne_u.components.connectors.DataConnectorI#offered2required(fr.sorbonne_u.components.interfaces.DataOfferedCI.DataI)
	 */
	@Override
	public DataRequiredCI.DataI offered2required(DataOfferedCI.DataI d)
	{
		// the data class must implement both the RequiredActuatorI
		// and the OfferedActuatorI ActuatorDataI interfaces.
		return (DataRequiredCI.DataI) d;
	}

	/**
	 * connect data ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	offering instanceof DataOfferedCI.PullCI
	 * pre	requiring instanceof DataRequiredCI.PullCI
	 * post	true		// no postcondition.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.connectors.AbstractConnector#connect(fr.sorbonne_u.components.interfaces.OfferedCI, fr.sorbonne_u.components.interfaces.RequiredCI)
	 */
	@Override
	public void			connect(OfferedCI offering, RequiredCI requiring)
	throws Exception
	{
		// the only reason to redefine this method is to test these
		assert	offering instanceof DataOfferedCI.PullCI :
					new PreconditionException(
							"offering instanceof DataOfferedCI.PullCI");
		assert	requiring instanceof DataRequiredCI.PullCI :
					new PreconditionException(
							"requiring instanceof DataRequiredCI.PullCI");

		super.connect(offering, requiring);
	}
}
// -----------------------------------------------------------------------------
