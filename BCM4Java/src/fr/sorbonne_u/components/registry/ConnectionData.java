package fr.sorbonne_u.components.registry;

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

import fr.sorbonne_u.components.registry.exceptions.BadConnectionDataException;

// -----------------------------------------------------------------------------
/**
 * The class <code>ConnectionData</code> represents the data stored by the
 * reqistry to know for each port on which host the port is published on the
 * RMI registry.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2012-10-22</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			ConnectionData
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// --+----------------------------------------------------------------------

	/**	RMI, socket (to be implemented), others ??							*/
	protected ConnectionType	type;
	/** host running the RMI registry on which the port is published.		*/
	protected String			hostname;
	/** port number on which the RMI registry can be called.				*/
	protected int				port;

	// -------------------------------------------------------------------------
	// Constructors
	// --+----------------------------------------------------------------------

	/**
	 * create a connection data object from the information received by the
	 * registry through a socket communication (hence strings).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hostname != null}
	 * pre	{@code port > 0}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param type		type of connection
	 * @param hostname	name of the host on which RMI registry the port is published.
	 * @param port		port number of the RMI registry.
	 */
	public				ConnectionData(
		ConnectionType type,
		String hostname,
		int port
		)
	{
		super();
		assert	hostname != null :
					new BadConnectionDataException(
									"hostname of the RMI registry is null!");
		assert	port > 0 :
					new BadConnectionDataException(
						"port of the RMI registry is not positive: " + port);

		this.type = type;
		this.hostname = hostname;
		this.port = port;
	}

	/**
	 * create a connection data object from the raw information received by the
	 * registry through a socket communication (hence one string).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code value != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param value	string containing the information about the published port.
	 * @throws BadConnectionDataException when the conenction data is erroneous.
	 */
	public				ConnectionData(
		String value
		) throws BadConnectionDataException
	{
		assert	value != null :
					new BadConnectionDataException(
						"result from the global registry is null: " + value);

		String[] temp1 = value.split("=") ;
		if (temp1[0].equals("rmi")) {
			this.type = ConnectionType.RMI;
			assert	temp1[1] != null :
						new BadConnectionDataException(
									"hostname of the RMI registry is null!");
			this.hostname = temp1[1];
		} else {
			assert	temp1[0].equals("socket") :
						new BadConnectionDataException(
									"unknown connection type: " + temp1[0]);

			this.type = ConnectionType.SOCKET;
			String[] temp2 = temp1[1].split(":");
			assert	temp2[0] != null :
						new BadConnectionDataException(
								"hostname of the RMI registry is null!");
			this.hostname = temp2[0] ;
			try {
				this.port = Integer.parseInt(temp2[1]) ;
				assert	this.port > 0 :
							new BadConnectionDataException(
									"port of the RMI registry is not positive: "
									+ this.port);
			} catch(NumberFormatException e) {
				throw new BadConnectionDataException(
							"bad RMI registry port information: " + temp2[1],
							e);
			}
		}
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @return the type
	 */
	public ConnectionType	getType() {
		return this.type;
	}

	/**
	 * @return the hostname
	 */
	public String			getHostname() {
		return this.hostname;
	}

	/**
	 * @return the port
	 */
	public int				getPort() {
		return this.port;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = null;
		if (this.type == ConnectionType.RMI) {
			sb = new StringBuffer("rmi=");
			sb.append(this.hostname);
		} else {
			sb = new StringBuffer("socket=");
			sb.append(this.hostname).append(":").append(this.port);
		}
		return sb.toString();
	}
}
// -----------------------------------------------------------------------------
