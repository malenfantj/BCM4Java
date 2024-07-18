package fr.sorbonne_u.components.endpoints;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to implement
// a simulation of a map-reduce kind of system in BCM4Java.
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

/**
 * The interface <code>AbstractEndPointI</code> defines the signature of
 * methods common to all end points;
 * @see fr.sorbonne_u.components.endpoints.EndPointI for more details
 * about end points and their usage.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p>Created on : 2024-07-11</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		AbstractEndPointI
{
	/**
	 * return true if the server side of this end point is initialised, false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the server side of this end point is initialised, false otherwise.
	 */
	public boolean		serverSideInitialised();

	/**
	 * initialise the server side of this end point, performing whatever action
	 * needed to make this reference readily usable.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !serverSideInitialised()}
	 * pre	{@code serverSideEndPointOwner != null}
	 * post	{@code serverSideInitialised()}
	 * </pre>
	 *
	 * @param serverSideEndPointOwner	server side end point owner.
	 */
	public void			initialiseServerSide(Object serverSideEndPointOwner);

	/**
	 * return true if the client side of this end point is initialised, false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the client side of this end point is initialised, false otherwise.
	 */
	public boolean		clientSideInitialised();

	/**
	 * initialise the client side reference embedded in this end point,
	 * performing whatever action needed to make this reference readily usable.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !clientSideInitialised()}
	 * pre	{@code clientSideEndPointOwner != null}
	 * post	{@code clientSideInitialised()}
	 * </pre>
	 *
	 * @param clientSideEndPointOwner	client side end point owner.
	 */
	public void			initialiseClientSide(Object clientSideEndPointOwner);

	
	/**
	 * clean up the server side of this end point, performing whatever action
	 * required to disable it definitively.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code !serverSideInitialised()}
	 * </pre>
	 *
	 */
	public void			cleanUpServerSide();

	/**
	 * clean up the client side of this end point, performing whatever action
	 * required to disable it definitively.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code !clientSideInitialised()}
	 * </pre>
	 *
	 */
	public void			cleanUpClientSide();
}
