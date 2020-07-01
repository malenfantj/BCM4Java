package fr.sorbonne_u.components.registry.protocol;

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

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.registry.ConnectionData;
import fr.sorbonne_u.components.registry.ConnectionType;
import fr.sorbonne_u.components.registry.exceptions.BadConnectionDataException;
import fr.sorbonne_u.components.registry.exceptions.GlobalRegistryResponseException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>ProtocolTest</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2020-06-17</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			ProtocolTest
{
	@Test
	public void			testLookupRequest()
	{
		String key = AbstractPort.generatePortURI();
		String request = Request.LOOKUP_REQUEST_NAME + " " + key;

		LookupRequest r = new LookupRequest(key);
		Assertions.assertTrue(r.isLookupRequest());
		Assertions.assertFalse(r.isPutRequest());
		Assertions.assertFalse(r.isRemoveRequest());
		Assertions.assertFalse(r.isShutdownRequest());
		Assertions.assertFalse(r.isUnknownRequest());
		Assertions.assertTrue(request.equals(r.request2string()));

		LookupRequest r1 = (LookupRequest)Request.string2request(request);
		Assertions.assertTrue(r.equals(r1));

		Assertions.assertThrows(
			AssertionError.class,
			() -> { new LookupRequest(null); });
		try {
			new LookupRequest(null);
		} catch(AssertionError e) {
			Assertions.assertTrue(
						e.getCause() instanceof PreconditionException);
		}
	}

	@Test
	public void			testPutRequest()
	{
		String key = AbstractPort.generatePortURI();
		String value =
			(new ConnectionData(ConnectionType.RMI, "localhost", 1)).
															toString();
		String request = Request.PUT_REQUEST_NAME + " " + key + " " + value;

		PutRequest r = new PutRequest(key, value);
		Assertions.assertFalse(r.isLookupRequest());
		Assertions.assertTrue(r.isPutRequest());
		Assertions.assertFalse(r.isRemoveRequest());
		Assertions.assertFalse(r.isShutdownRequest());
		Assertions.assertFalse(r.isUnknownRequest());
		Assertions.assertTrue(request.equals(r.request2string()));

		PutRequest r1 = (PutRequest)Request.string2request(request);
		Assertions.assertTrue(r.equals(r1));

		Assertions.assertThrows(
			AssertionError.class,
			() -> { new PutRequest(null, value); });
		Assertions.assertThrows(
				AssertionError.class,
				() -> { new PutRequest(key, null); });
		Assertions.assertThrows(
				AssertionError.class,
				() -> { new PutRequest(null, null); });
		try {
			new PutRequest(null, value);
		} catch(AssertionError e) {
			Assertions.assertTrue(
						e.getCause() instanceof PreconditionException);
		}
		try {
			new PutRequest(key, null);
		} catch(AssertionError e) {
			Assertions.assertTrue(
						e.getCause() instanceof PreconditionException);
		}
		try {
			new PutRequest(null, null);
		} catch(AssertionError e) {
			Assertions.assertTrue(
						e.getCause() instanceof PreconditionException);
		}
	}

	@Test
	public void			testRemoveRequest()
	{
		String key = AbstractPort.generatePortURI();
		String request = Request.REMOVE_REQUEST_NAME + " " + key;

		RemoveRequest r = new RemoveRequest(key);
		Assertions.assertFalse(r.isLookupRequest());
		Assertions.assertFalse(r.isPutRequest());
		Assertions.assertTrue(r.isRemoveRequest());
		Assertions.assertFalse(r.isShutdownRequest());
		Assertions.assertFalse(r.isUnknownRequest());
		Assertions.assertTrue(request.equals(r.request2string()));

		RemoveRequest r1 = (RemoveRequest)Request.string2request(request);
		Assertions.assertTrue(r.equals(r1));

		Assertions.assertThrows(
			AssertionError.class,
			() -> { new RemoveRequest(null); });
		try {
			new RemoveRequest(null);
		} catch(AssertionError e) {
			Assertions.assertTrue(
						e.getCause() instanceof PreconditionException);
		}
	}

	@Test
	public void			testShutdownRequest()
	{
		String request = Request.SHUTDOWN_REQUEST_NAME;

		ShutdownRequest r = new ShutdownRequest();
		Assertions.assertFalse(r.isLookupRequest());
		Assertions.assertFalse(r.isPutRequest());
		Assertions.assertFalse(r.isRemoveRequest());
		Assertions.assertTrue(r.isShutdownRequest());
		Assertions.assertFalse(r.isUnknownRequest());
		Assertions.assertTrue(request.equals(r.request2string()));

		ShutdownRequest r1 = (ShutdownRequest)Request.string2request(request);
		Assertions.assertTrue(r.equals(r1));
	}

	@Test
	public void			testUnknownRequest()
	{
		String request = AbstractPort.generatePortURI();

		UnknownRequest r = new UnknownRequest(request);
		Assertions.assertFalse(r.isLookupRequest());
		Assertions.assertFalse(r.isPutRequest());
		Assertions.assertFalse(r.isRemoveRequest());
		Assertions.assertFalse(r.isShutdownRequest());
		Assertions.assertTrue(r.isUnknownRequest());
		Assertions.assertTrue(request.equals(r.request2string()));

		UnknownRequest r1 = (UnknownRequest)Request.string2request(request);
		Assertions.assertTrue(r.equals(r1));
	}

	@Test
	public void			testLookupResponse()
	{
		String key = AbstractPort.generatePortURI();
		String value =
				(new ConnectionData(ConnectionType.RMI, "localhost", 1)).
																toString();

		String positive = Response.LOOKUP_RESPONSE_NAME + " "
							+ Response.OK + " "
							+ value;
		String negative = Response.LOOKUP_RESPONSE_NAME + " "
							+ Response.NOK + " "
							+ key;

		LookupResponse p = new LookupResponse(false, value);
		Assertions.assertTrue(positive.equals(p.response2string()));
		try {
			LookupResponse p1 =
						(LookupResponse)Response.string2response(positive);
			Assertions.assertTrue(p.equals(p1));
		} catch (GlobalRegistryResponseException e) {
			Assertions.assertTrue(false,
							"exception " + e + " should not be thrown!");
		}

		LookupResponse n = new LookupResponse(true, key);
		Assertions.assertTrue(negative.equals(n.response2string()));
		try {
			LookupResponse n1 =
						(LookupResponse)Response.string2response(negative);
			Assertions.assertTrue(n.equals(n1));
		} catch (GlobalRegistryResponseException e) {
			Assertions.assertTrue(false,
						"exception " + e + " should not be thrown!");
		}

		Assertions.assertThrows(
			AssertionError.class,
			() -> { new LookupResponse(true, null); });
		try {
			new LookupResponse(true, null);
		} catch(AssertionError e) {
			Assertions.assertTrue(
						e.getCause() instanceof PreconditionException);
		}

		try {
			Assertions.assertTrue(value.equals(p.interpret().toString()));
		} catch (GlobalRegistryResponseException |
											BadConnectionDataException e) {
			Assertions.assertTrue(false,
					"exception " + e + " should not be thrown!");
		}
		Assertions.assertThrows(GlobalRegistryResponseException.class,
								() -> { n.interpret(); });
	}

	@Test
	public void			testPutResponse()
	{
		String key = AbstractPort.generatePortURI();

		String positive = Response.PUT_RESPONSE_NAME + " " + Response.OK;
		String negative = Response.PUT_RESPONSE_NAME + " "
												+ Response.NOK + " " + key;

		PutResponse p = new PutResponse();
		Assertions.assertTrue(positive.equals(p.response2string()));
		try {
			PutResponse p1 = (PutResponse)Response.string2response(positive);
			Assertions.assertTrue(p.equals(p1));
		} catch (GlobalRegistryResponseException e) {
			Assertions.assertTrue(false,
							"exception " + e + " should not be thrown!");
		}

		PutResponse n = new PutResponse(key);
		Assertions.assertTrue(negative.equals(n.response2string()));
		try {
			PutResponse n1 = (PutResponse)Response.string2response(negative);
			Assertions.assertTrue(n.equals(n1));
		} catch (GlobalRegistryResponseException e) {
			Assertions.assertTrue(false,
						"exception " + e + " should not be thrown!");
		}

		try {
			Assertions.assertTrue(null == p.interpret());
		} catch (GlobalRegistryResponseException |
											BadConnectionDataException e) {
			Assertions.assertTrue(false,
					"exception " + e + " should not be thrown!");
		}
		Assertions.assertThrows(GlobalRegistryResponseException.class,
								() -> { n.interpret(); });
	}

	@Test
	public void			testRemoveResponse()
	{
		String key = AbstractPort.generatePortURI();

		String positive = Response.REMOVE_RESPONSE_NAME + " " + Response.OK;
		String negative = Response.REMOVE_RESPONSE_NAME + " "
												+ Response.NOK + " " + key;

		RemoveResponse p = new RemoveResponse();
		Assertions.assertTrue(positive.equals(p.response2string()));
		try {
			RemoveResponse p1 =
						(RemoveResponse)Response.string2response(positive);
			Assertions.assertTrue(p.equals(p1));
		} catch (GlobalRegistryResponseException e) {
			Assertions.assertTrue(false,
							"exception " + e + " should not be thrown!");
		}

		RemoveResponse n = new RemoveResponse(key);
		Assertions.assertTrue(negative.equals(n.response2string()));
		try {
			RemoveResponse n1 =
						(RemoveResponse)Response.string2response(negative);
			Assertions.assertTrue(n.equals(n1));
		} catch (GlobalRegistryResponseException e) {
			Assertions.assertTrue(false,
						"exception " + e + " should not be thrown!");
		}

		try {
			Assertions.assertTrue(null == p.interpret());
		} catch (GlobalRegistryResponseException |
											BadConnectionDataException e) {
			Assertions.assertTrue(false,
					"exception " + e + " should not be thrown!");
		}
		Assertions.assertThrows(GlobalRegistryResponseException.class,
								() -> { n.interpret(); });
	}

	@Test
	public void			testShutdownResponse()
	{
		String positive = Response.SHUTDOWN_RESPONSE_NAME + " " + Response.OK;

		ShutdownResponse p = new ShutdownResponse();
		Assertions.assertTrue(positive.equals(p.response2string()));
		try {
			ShutdownResponse p1 =
						(ShutdownResponse)Response.string2response(positive);
			Assertions.assertTrue(p.equals(p1));
		} catch (GlobalRegistryResponseException e) {
			Assertions.assertTrue(false,
							"exception " + e + " should not be thrown!");
		}

		try {
			Assertions.assertTrue(null == p.interpret());
		} catch (GlobalRegistryResponseException |
											BadConnectionDataException e) {
			Assertions.assertTrue(false,
					"exception " + e + " should not be thrown!");
		}
	}

	@Test
	public void			testErrorResponse()
	{
		String key = AbstractPort.generatePortURI();
		String request = "xyz " + key;
		String positive = Response.ERROR_RESPONSE_NAME + " " + request;

		ErrorResponse p = new ErrorResponse(request);
		Assertions.assertTrue(positive.equals(p.response2string()));
		try {
			ErrorResponse p1 =
						(ErrorResponse)Response.string2response(positive);
			Assertions.assertTrue(p.equals(p1));
		} catch (GlobalRegistryResponseException e) {
			Assertions.assertTrue(false,
							"exception " + e + " should not be thrown!");
		}

		Assertions.assertThrows(GlobalRegistryResponseException.class,
								() -> { p.interpret(); });
	}
}
// -----------------------------------------------------------------------------
