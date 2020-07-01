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

import java.io.IOException;
import java.io.InputStream;

import fr.sorbonne_u.exceptions.PreconditionException;

//-----------------------------------------------------------------------------
/**
 * The class <code>SocketUtilities</code> implements utilities for managing
 * socket communications for the registry.
 *
 * <p><strong>Description</strong></p>
 * 
 * TODO: to be completed.  Really needed ?
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
 */
public class				SocketUtilities
{
	/** Size of the buffer used to read commands from the sockets.			*/ 
	protected static int	BUFFER_SIZE = 512;

	/**
	 * Reads the content of a socket input stream and returns it as a String.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	is != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param is			input stream on which to read.
	 * @return				the string just read.
	 * @throws IOException	<i>to do.</i>
	 */
	public static String	readInputStream(InputStream is)
	throws IOException
	{
		assert	is != null : new PreconditionException("is != null");

		StringBuffer sb = new StringBuffer(BUFFER_SIZE);
		char[] tampon = new char[BUFFER_SIZE];

		char b = (char) is.read();
		int i ;		
		for (i = 0 ; b != '\n' ; i++) {
			tampon[i] = (char) b;
			if (i >= BUFFER_SIZE - 1) {
				sb.append(tampon, 0, BUFFER_SIZE);
				i = -1;
			}
			b = (char) is.read();
		}
		sb.append(new String(tampon, 0, i));
		return sb.toString();
	}
}
//-----------------------------------------------------------------------------
