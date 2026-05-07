package fr.sorbonne_u.components.helpers;

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>TracerWindow</code> implements a simple tracer for BCM
 * printing trace messages in a window.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * Trace windows are managed as Java AWT windows. In this toolkit, the position,
 * width and height of a window are set in screen pixels unit having a
 * {@code (0, 0)} origin in screen units at the top left of the screen. When
 * creating an instance of {@code TracerWindow}, its position can be set in
 * screen pixels units.
 * </p>
 * To facilitate even more the positioning of trace windows, this class also
 * allows its user to use a tiling coordinates system. In this system, the
 * screen is divided into a number of tiles in width and height and the windows
 * are positioned in X (width) and Y (height) using zero-based tile coordinates
 * rather than the screen pixels unit. The number of tiles in width and height
 * is set by default at 5 in both directions, but this can be changed using the
 * static method {@code setTilingSize}.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code screenWidth > 0}
 * invariant	{@code screenHeight > 0}
 * invariant	{@code frameWidth > 0}
 * invariant	{@code frameHeight > 0}
 * invariant	{@code TILING_WIDTH > 0}
 * invariant	{@code TILING_HEIGHT > 0}
 * invariant	{@code BANNER_HEIGHT >= 0}
 * invariant	{@code !useTilingCoordinates || xTilingRelativePos >= 0 && xTilingRelativePos < TILING_WIDTH}
 * invariant	{@code !useTilingCoordinates || yTilingRelativePos >= 0 && yTilingRelativePos < TILING_HEIGHT}
 * invariant	{@code xScreenPos >= 0 && xScreenPos < frameWidth - xOrigin}
 * invariant	{@code yScreenPos >= 0 && yScreenPos < frameHeight - yOrigin}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2018-08-30</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class 			TracerWindow
extends		WindowAdapter
implements	WindowListener,
			TracerI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** prefix of the title of the trace window.							*/
	protected static final String	WINDOW_TITLE_PREFIX = "TraceWindow";

	/** Width of the screen accessible to the Java AWT toolkit.				*/
	protected static final int		SCREEN_WIDTH;
	/** Height of the screen accessible to the Java AWT toolkit.			*/
	protected static final int		SCREEN_HEIGHT;

	/** Frame that will display the tracer on the screen.					*/
	protected JFrame	frame;
	/** Text area in which the trace message will be output.				*/
	protected JTextPane	textArea;
	/** Title to be displayed by the tracer frame.							*/
	protected String 	title;
	/** Width of the frame in screen coordinates.							*/
	protected int		frameWidth;
	/** Height of the frame in screen coordinates.							*/
	protected int		frameHeight;

	// Positioning the window on a tile-based screen display i.e., the screen
	// is divided into tiles on which the tracer windows are put.

	/** in the tiling display, number of tiles in the width or X axis.		*/
	private static int	TILING_WIDTH = 5;
	/** in the tiling display, number of tiles in the height or Y axis.		*/
	private static int	TILING_HEIGHT = 5;
	/** height of the banner put on tracer widows (holding the title and
	 *  the window buttons.												 	*/
	private static int	BANNER_HEIGHT = 25;
	/** when true, the window position is set using the tiling coordinates,
	 *  otherwise the position is set directly in screen coordinates.		*/
	protected boolean	useTilingCoordinates;
	/** X position of the frame among the application tracers in the tiling
	 *  coordinates.														*/
	protected int		xTilingRelativePos;
	/** Y position of the frame among the application tracers in the tiling
	 *  coordinates.														*/
	protected int		yTilingRelativePos;

	// Positioning the window in screen pixels unit relative to the origin.

	/** X position of the frame among the application tracers in the screen
	 *  coordinates.														*/
	protected int		xScreenPos;
	/** Y position of the frame among the application tracers in the screen
	 *  coordinates.														*/
	protected int		yScreenPos;

	/** True if traces must be output and false otherwise.					*/
	protected boolean	tracingStatus;
	/** True if the trace is suspended and false otherwise.					*/
	protected boolean	suspendStatus;

	static {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		SCREEN_WIDTH = screenSize.width;
		SCREEN_HEIGHT = screenSize.height;
	}
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Tracer windows defined in screen coordinates and pixel units

	/**
	 * create a tracer at the {@code (xScreenPos, yScreenPos)} position in the
	 * screen coordinates having a width of {@code frameWidth} and height
	 * {@code frameHeight}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code frameWidth > 0 && frameWidth < getScreenWidth()}
	 * pre	{@code frameHeight > 0 && frameHeight < getScreenHeight()}
	 * pre	{@code xScreenPos >= 0 && xScreenPos < getScreenWidth()}
	 * pre	{@code yScreenPos >= 0 && yScreenPos < getScreenHeight()}
	 * post	{@code !this.isTracing()}
	 * post	{@code !this.isSuspended()}
	 * </pre>
	 *
	 * @param title			title to put on the frame.
	 * @param frameWidth	width of the tracer frame.
	 * @param frameHeight	height of the tracer frame.
	 * @param xScreenPos	x position of the frame in screen pixels unit.
	 * @param yScreenPos	y position of the frame in screen pixels unit.
	 */
	public				TracerWindow(
		String title,
		int frameWidth,
		int frameHeight,
		int xScreenPos,
		int yScreenPos
		)
	{
		assert	frameWidth > 0 && frameWidth < this.getScreenWidth() :
				new PreconditionException(
						"frameWidth > 0 && frameWidth < getScreenWidth()");
		assert	frameHeight > 0 && frameHeight < this.getScreenHeight() :
				new PreconditionException(
						"frameHeight > 0 && frameHeight < getScreenHeight()");
		assert	xScreenPos >= 0 && xScreenPos < this.getScreenWidth() :
				new PreconditionException(
						"xScreenPos >= 0 && xScreenPos < getScreenWidth()");
		assert	yScreenPos >= 0 && yScreenPos < this.getScreenHeight() :
				new PreconditionException(
						"yScreenPos >= 0 && yScreenPos < getScreenHeight()");

		this.useTilingCoordinates = false;
		this.title = WINDOW_TITLE_PREFIX + ":" + title;; 
		this.xScreenPos = xScreenPos;
		this.yScreenPos = yScreenPos;

		this.tracingStatus = false;
		this.suspendStatus = false;
	}

	// Tracer windows defined in tile coordinates

	/**
	 * create a tracer at the {@code (1, 0)} position in the tiling display
	 * coordinates with the origin set at {@code (0, 0)} in the screen
	 * coordinates.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code !this.isTracing()}
	 * post	{@code !this.isSuspended()}
	 * </pre>
	 *
	 */
	public				TracerWindow()
	{
		this.title = WINDOW_TITLE_PREFIX;
		this.frameWidth = SCREEN_WIDTH / TILING_WIDTH;
		this.frameHeight = SCREEN_HEIGHT / TILING_HEIGHT;

		// Given that in distributed execution, the global registry uses
		// 0 in standard, put this frame to its right.
		this.useTilingCoordinates = true;
		this.xTilingRelativePos = 1 ;
		this.yTilingRelativePos = 0 ;

		this.tracingStatus = false;
		this.suspendStatus = false;
	}

	/**
	 * create a tracer with the relative position
	 * {@code (xRelativePos, yRelativePos)} in the tiling display coordinates
	 * with the origin set at {@code (0, 0)} in the screen coordinates.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code xTilingRelativePos >= 0 && xTilingRelativePos < getTilingWidth()}
	 * pre	{@code yTilingRelativePos >= 0 && yTilingRelativePos < getTilingHeight()}
	 * post	{@code !this.isTracing()}
	 * post	{@code !this.isSuspended()}
	 * </pre>
	 *
	 * @param title					title to put on the frame.
	 * @param xTilingRelativePos	x position of the frame in the group of frames.
	 * @param yTilingRelativePos	y position of the frame in the group of frames.
	 */
	public				TracerWindow(
		String title,
		int xTilingRelativePos,
		int yTilingRelativePos
		)
	{
		assert	xTilingRelativePos >= 0 && xTilingRelativePos < getTilingWidth() :
				new PreconditionException(
						"xTilingRelativePos >= 0 && "
						+ "xTilingRelativePos < getTilingWidth()");
		assert	yTilingRelativePos >= 0 && yTilingRelativePos < getTilingHeight() :
				new PreconditionException(
						"yTilingRelativePos >= 0 && "
						+ "yTilingRelativePos < getTilingHeight()");

		this.title = WINDOW_TITLE_PREFIX + ":" + title;
		this.frameWidth = SCREEN_WIDTH / TILING_WIDTH;
		this.frameHeight = SCREEN_HEIGHT / TILING_HEIGHT;

		this.useTilingCoordinates = true;
		this.xTilingRelativePos = xTilingRelativePos;
		this.yTilingRelativePos = yTilingRelativePos;

		this.tracingStatus = false;
		this.suspendStatus = false;
	}

	/**
	 * initialise the trace window.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected synchronized void		initialise()
	{
		this.textArea = new JTextPane();
		this.textArea.setEditable(false);
		this.textArea.setBackground(Color.WHITE);
		StyledDocument doc = (StyledDocument) textArea.getDocument();
		Style style = doc.addStyle("ConsoleStyle", null);
		StyleConstants.setFontFamily(style, "MonoSpaced");
		StyleConstants.setFontSize(style, 10);

		if (this.useTilingCoordinates) {
			this.computeScreenRelativePosition();
		}

		this.frame = new JFrame(this.title);
		this.frame.setBounds(
				this.xScreenPos,
				this.yScreenPos,
				this.frameWidth,
				this.frameHeight);

		this.frame.getContentPane().add(
						new JScrollPane(textArea), BorderLayout.CENTER);
		this.frame.addWindowListener(this);
		this.frame.setVisible(true);
	}

	// -------------------------------------------------------------------------
	// Static methods
	// -------------------------------------------------------------------------

	/**
	 * set the width and height of the tiling coordinates system; the positions
	 * and sizes of trace windows created before the changes to these sizes will
	 * *not* be modified.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param width
	 * @param height
	 */
	public static void	setTilingSize(int width, int height)
	{
		assert	width > 0 : new PreconditionException("width > 0");
		assert	height > 0 : new PreconditionException("height > 0");

		TILING_WIDTH = width;
		TILING_HEIGHT = height;
	}

	/**
	 * return the width of the tiling coordinate system in number of tiles.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return > 0}
	 * </pre>
	 *
	 * @return	the width of the tiling coordinate system in number of tiles.
	 */
	public static int	getTilingWidth()
	{
		return TILING_WIDTH;
	}

	/**
	 * return the height of the tiling coordinate system in number of tiles.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return > 0}
	 * </pre>
	 *
	 * @return	the height of the tiling coordinate system in number of tiles.
	 */
	public static int	getTilingHeight()
	{
		return TILING_HEIGHT;
	}

	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	/**
	 * compute the position of the window in screen pixels unit from its tiling
	 * relative position.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code useTilingCoordinates}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		computeScreenRelativePosition()
	{
		assert	useTilingCoordinates :
				new PreconditionException("useTilingCoordinates");

		this.xScreenPos =
				this.xTilingRelativePos * this.frameWidth;
		this.yScreenPos =
				(this.yTilingRelativePos * this.frameHeight) + BANNER_HEIGHT;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#getScreenWidth()
	 */
	@Override
	public int			getScreenWidth()
	{
		return SCREEN_WIDTH ;
	}

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#getScreenHeight()
	 */
	@Override
	public int			getScreenHeight()
	{
		return SCREEN_HEIGHT;
	}

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#setTitle(java.lang.String)
	 */
	@Override
	public void			setTitle(String title)
	{
		this.title = WINDOW_TITLE_PREFIX + ":" + title;
	}

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#setRelativePosition(int, int)
	 */
	@Override
	public void			setRelativePosition(int x, int y)
	{
		assert	x >= 0 :
				new PreconditionException(
						"TracerWindow#setRelativePosition called with "
						+ "negative position: x = " + x + "!");
		assert	y >= 0 :
				new PreconditionException(
						"TracerWindow#setRelativePosition called with "
						+ "negative position: y = " + y + "!");

		this.xTilingRelativePos = x;
		this.yTilingRelativePos = y;
	}

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#isVisible()
	 */
	@Override
	public boolean		isVisible()
	{
		return this.frame.isVisible();
	}

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#toggleVisible()
	 */
	@Override
	public synchronized void	toggleVisible()
	{
		assert	this.isTracing();
		this.frame.setVisible(!frame.isVisible());
	}

	/**
	 * close the window.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
	 */
	@Override
	public synchronized void	windowClosing(WindowEvent evt)
	{
		if (this.frame != null) {
			this.frame.setVisible(false);
			this.frame.dispose();
		}
	}

	// -------------------------------------------------------------------------
	// Tracer methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#toggleTracing()
	 */
	@Override
	public synchronized void	toggleTracing()
	{
		this.tracingStatus = !this.tracingStatus;
		if (this.tracingStatus) {
			this.initialise();
			this.suspendStatus = false;
		} else {
			this.frame.setVisible(false);
			this.frame.dispose();
			this.frame = null;
			this.suspendStatus = true;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#toggleSuspend()
	 */
	@Override
	public synchronized void	toggleSuspend()
	{
		assert	this.isTracing() :
				new PreconditionException(
						"TracerWindow#toggleSuspend called but tracing "
						+ "is not activated!");

		this.suspendStatus = !this.suspendStatus;
	}

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#isTracing()
	 */
	@Override
	public boolean		isTracing()
	{
		return this.tracingStatus;
	}

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#isSuspended()
	 */
	@Override
	public boolean		isSuspended()
	{
		return this.suspendStatus;
	}

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#traceMessage(java.lang.String)
	 */
	@Override
	public synchronized void	traceMessage(String message)
	{
		if (this.tracingStatus && !this.suspendStatus) {
			StyledDocument doc = (StyledDocument) this.textArea.getDocument();
			try {
				doc.insertString(doc.getLength(),
								 message,
								 doc.getStyle("ConsoleStyle"));
			} catch (BadLocationException e) {
				throw new RuntimeException(
							"TracerWindow#traceMessage trying to show the"
							+ "message \"" + message + "\" but failed!", e);
			}
			this.textArea.setCaretPosition(textArea.getDocument().getLength());
		}
	}
}
// -----------------------------------------------------------------------------
