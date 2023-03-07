package fr.sorbonne_u.components.helpers;

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

// -----------------------------------------------------------------------------
/**
 * The class <code>TracerI</code> declares the common behaviours of tracers
 * for BCM.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p>Created on : 2020-06-24</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		TracerI
{
	/**
	 * return the screen width; a zero-width means no access to the information.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret >= 0}
	 * </pre>
	 *
	 * @return	the screen width.
	 */
	default int			getScreenWidth()	{ return 0; }

	/**
	 * return the screen height; a zero-width means no access to the information.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret >= 0}
	 * </pre>
	 *
	 * @return	the screen height.
	 */
	default int			getScreenHeight()	{ return 0; }

	/**
	 * set the title of the tracer frame, if possible.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param title		title to be put on the frame.
	 */
	default void		setTitle(String title)	{ }

	/**
	 * set the coordinate of the top left point in screen coordinates, if
	 * possible.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code xOrigin >= 0}
	 * pre	{@code xOrigin < getScreenWidth()}
	 * pre	{@code yOrigin >= 0}
	 * pre	{@code yOrigin < getScreenHeight()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param xOrigin	x coordinate of the top left point in screen coordinates.
	 * @param yOrigin	y coordinate of the top left point in screen coordinates.
	 */
	default void		setOrigin(int xOrigin, int yOrigin)	{ }

	/**
	 * set the tracer frame relative coordinates among the frames of the
	 * application, if possible.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code x >= 0}
	 * pre	{@code y >= 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param x	x relative coordinates among the frames of the application.
	 * @param y	y relative coordinates among the frames of the application.
	 */
	default void		setRelativePosition(int x, int y)	{ }

	/**
	 * return the tracing console visibility status; true by default.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the tracing console visibility status.
	 */
	default boolean		isVisible() { return true; }

	/**
	 * invert the visibility status of the tracing console, if possible.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isTracing()}
	 * post	{@code isVisible() == !isVisible()@pre}
	 * </pre>
	 *
	 */
	default void		toggleVisible() { }

	/**
	 * toggle the tracing status.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code isTracing() == !isTracing()@pre}
	 * post	{@code isTracing() == !isSuspended()}
	 * </pre>
	 *
	 */
	public void			toggleTracing();

	/**
	 * toggle the suspend status of the trace.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isTracing()}
	 * post	{@code isSuspended() == !isSuspended()@pre}
	 * </pre>
	 *
	 */
	public void			toggleSuspend();

	/**
	 * return the tracing status.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the tracing status.
	 */
	public boolean		isTracing();

	/**
	 * return the trace suspension status.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the trace suspension status.
	 */
	public boolean		isSuspended();

	/**
	 * show the trace message if the tracing status is true and the suspension
	 * status is false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param message	trace message to be output.
	 */
	public void			traceMessage(String message);
}
// -----------------------------------------------------------------------------
