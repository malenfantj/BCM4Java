package fr.sorbonne_u.components.plugins.dipc.interfaces;

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

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

// -----------------------------------------------------------------------------
/**
 * The interface <code>PushingControlCI</code> proposes basic component
 * services to control the regular exchanges of data via a
 * <code>DataOfferedCI</code> interface in push mode.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The interface simply adds to <code>PushControlImplementationI</code>
 * the <code>OfferedCI</code> and <code>RequiredCI</code> to make it a
 * component interface.
 * </p>
 * <p>
 * Nota: since Java 8, Oracle jdk release 241, methods inherited by a remote
 * interface are no longer considered remote if the inherited interface is
 * not itself remote; to bypass this, methods must be redefined in the remote
 * method. Marking them with {@code @Override} stresses the design objective to
 * share identical signatures between the implementation interface and the
 * component interface.
 * </p>
 * 
 * <p>Created on : 2018-01-26</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface			PushControlCI
extends		PushControlImplementationI,
			OfferedCI,
			RequiredCI
{
	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#isPortExisting(java.lang.String)
	 */
	@Override
	public boolean			isPortExisting(String portURI) throws Exception ;

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#startUnlimitedPushing(java.lang.String, long)
	 */
	@Override
	public void				startUnlimitedPushing(
		String portURI,
		final long interval
		) throws Exception ;

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#startLimitedPushing(java.lang.String, long, int)
	 */
	@Override
	public void				startLimitedPushing(
		String portURI,
		final long interval,
		final int n
		) throws Exception ;

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#currentlyPushesData(java.lang.String)
	 */
	@Override
	public boolean			currentlyPushesData(String portURI)
	throws Exception ;

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#stopPushing(java.lang.String)
	 */
	@Override
	public void				stopPushing(String portURI) throws Exception ;
}
// -----------------------------------------------------------------------------
