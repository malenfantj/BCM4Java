package fr.sorbonne_u.components.cvm.utils;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// new implementation of the DEVS simulation standard for Java.
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

//------------------------------------------------------------------------------
/**
 * The class <code>CyclicBarrierProtocol</code> is simply grouping the
 * interfaces and classes that implement a command design pattern for the cyclic
 * barrier.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The BCM4Java cyclic barrier implements a socket-based protocol with the
 * following requests and responses:
 * </p>
 * <pre>
 *                    Requests                    Response
 * 	
 * Wait request:      jvm-URI hostname portNo     "resume"
 * Shutdown request:  null string                 no response
 * </pre>
 * <p>
 * where jvm-URI is the URI of the JVM as declared in the configuration file,
 * hostname is the name of the host (DNS identifier or IP number, as a string)
 * and portNo is the (Unix) port number on which the emitter of the wait request
 * is looking for the response. The shutdown request is simply the null string
 * and no response is returned to the shutdown request as it is emitted when
 * the socket is closed.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2020-06-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	CyclicBarrierProtocol
{
	// -------------------------------------------------------------------------
	// Cyclic barrier requests
	// -------------------------------------------------------------------------

	/**
	 * The interface <code>RequestI</code> declares the common behaviours of
	 * request objects for the cyclic barrier.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2020-06-22</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static interface	RequestI
	{
		/**
		 * return true if the request is a wait request object.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @return	true if the request is a wait request object.
		 */
		public boolean		isWaitRequest();

		/**
		 * return true if the request is a shutdown request object.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @return	true if the request is a wait request object.
		 */
		public boolean		isShutdownRequest();

		/**
		 * return the string representation of the request.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @return	the string representation of the request.
		 */
		public String		request2string();
	}

	/**
	 * The abstract class <code>Request</code> defines the common properties
	 * of requests for the cyclic barrier.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2020-06-22</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static abstract class	Request
	implements	RequestI
	{
		/** true if the request contains an error.							*/
		protected boolean	isError;
		/** the erroneous request, if any.									*/
		protected String	requestString;

		/**
		 * create a request from the string read from the socket.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param request	string read from the socket.
		 * @return			the corresponding request object.
		 */
		public static RequestI	string2request(String request)
		{
			if (request == null) {
				return new ShutdownRequest();
			} else {
				String[] tokens = request.split("\\s");
				if (tokens.length != 3) {
					// erroneous wait request.
					return new WaitRequest(request);
				} else {
					int portNo = 0;
					try {
						portNo = Integer.parseInt(tokens[2]);
					} catch(NumberFormatException e) {
						// erroneous wait request (bad port number).
						return new WaitRequest(request);
					}
					return new WaitRequest(tokens[0], tokens[1],portNo);
				}
			}
		}

		/**
		 * @see fr.sorbonne_u.components.cvm.utils.CyclicBarrierProtocol.RequestI#isShutdownRequest()
		 */
		@Override
		public boolean		isShutdownRequest()	{ return false; }

		/**
		 * @see fr.sorbonne_u.components.cvm.utils.CyclicBarrierProtocol.RequestI#isWaitRequest()
		 */
		@Override
		public boolean		isWaitRequest()		{ return false; }
	}

	/**
	 * The class <code>WaitRequest</code> implements a wait request sent by
	 * synchronised processes to the cyclic barrier.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2020-06-22</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class	WaitRequest
	extends 	Request
	{
		/** URI of the JVM calling the cyclic barrier.						*/
		protected String	jvmURI;
		/** name of the host running the JVM (DNS or IP number).			*/
		protected String	hostname;
		/** number of the port on which the JVM waits the response.			*/
		protected int		socketLocalPort;

		/**
		 * create an erroneous wait request.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param request	the erroneous request.
		 */
		protected		WaitRequest(String request)
		{
			super();
			this.isError = true;
			this.requestString = request;
		}

		/**
		 * create a wait request that will be erroneous if the repcondition is
		 * false.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code jvmURI != null && hostname != null && socketLocalPort > 0}
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param jvmURI			URI of the JVM calling the cyclic barrier.
		 * @param hostname			name of the host running the JVM (DNS or IP number).
		 * @param socketLocalPort	number of the port on which the JVM waits the response.
		 */
		protected		WaitRequest(
			String jvmURI,
			String hostname,
			int socketLocalPort
			)
		{
			super();
			if (jvmURI != null && hostname != null && socketLocalPort > 0) {
				this.isError = false;
				this.jvmURI = jvmURI;
				this.hostname = hostname;
				this.socketLocalPort = socketLocalPort;
			} else {
				this.isError = true;
				this.requestString = jvmURI + " " + hostname + " "
															+ socketLocalPort;
			}
		}

		/**
		 * @see fr.sorbonne_u.components.cvm.utils.CyclicBarrierProtocol.Request#isWaitRequest()
		 */
		@Override
		public boolean	isWaitRequest()			{ return true; }

		/**
		 * return true if the request is erroneous.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @return	true if the request is erroneous.
		 */
		public boolean	isErroneous()			{ return this.isError; }

		
		/** @return the jvmURI												*/
		public String	getJvmURI() 			{ return jvmURI; }

		/** @return the hostname											*/
		public String	getHostname()			{ return hostname; }

		/** @return the socketLocalPort										*/
		public int		getSocketLocalPort()	{ return socketLocalPort; }

		/**
		 * @see fr.sorbonne_u.components.cvm.utils.CyclicBarrierProtocol.RequestI#request2string()
		 */
		public String	request2string()
		{
			return this.jvmURI + " " + this.hostname + " "
													+ this.socketLocalPort;
		}
	}

	/**
	 * The class <code>ShutdownRequest</code> implements a wait request sent by
	 * synchronised processes to the cyclic barrier.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2020-06-22</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class	ShutdownRequest
	extends		Request
	{
		/**
		 * create an erroneous shutdown request.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param request	the erroneous request.
		 */
		protected		ShutdownRequest(String request)
		{
			super();
			this.isError = true;
			this.requestString = request;
		}

		/**
		 * create an shutdown request.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 */
		protected		ShutdownRequest()
		{
			super();
			this.isError = false;
		}

		/**
		 * @see fr.sorbonne_u.components.cvm.utils.CyclicBarrierProtocol.Request#isShutdownRequest()
		 */
		@Override
		public boolean	isShutdownRequest()		{ return true; }

		/**
		 * @see fr.sorbonne_u.components.cvm.utils.CyclicBarrierProtocol.RequestI#request2string()
		 */
		public String	request2string()		{ return null; }
	}

	// -------------------------------------------------------------------------
	// Cyclic barrier responses
	// -------------------------------------------------------------------------

	/**
	 * The interface <code>ResponseI</code> declares the common behaviours of
	 * response objects for the cyclic barrier.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2020-06-22</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static interface ResponseI
	{
		/**
		 * return the string representation of the response.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @return	the string representation of the response.
		 */
		public String		response2string();
	}

	/**
	 * The class <code>Response</code>
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2020-06-22</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static abstract class	Response
	implements	ResponseI
	{
		public static final String	RESUME_RESPONSE = "resume";
		protected boolean		isError = false;

		protected					Response(boolean isError)
		{
			super();
			this.isError = isError;
		}

		/**
		 * create the response object from its string representation.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param response	string representation of the response.
		 * @return			correspond object representation.
		 */
		public static ResponseI	string2response(String response)
		{
			if (RESUME_RESPONSE.equals(response)) {
				return new ResumeResponse(false);
			} else {
				return new ResumeResponse(true);
			}
		}
	}

	/**
	 * The class <code>ResumeResponse</code>
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2020-06-22</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class	ResumeResponse
	extends		Response
	{
		/**
		 * create a resume response object.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param isError	true if the response is erroneous (bad format, ...).
		 */
		public			ResumeResponse(boolean isError)
		{
			super(isError);
		}

		/**
		 * @see fr.sorbonne_u.components.cvm.utils.CyclicBarrierProtocol.ResponseI#response2string()
		 */
		@Override
		public String	response2string()
		{
			return Response.RESUME_RESPONSE;
		}
	}
}
// -----------------------------------------------------------------------------
