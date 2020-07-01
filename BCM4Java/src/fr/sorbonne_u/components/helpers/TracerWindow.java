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
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
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
	protected int		screenWidth;
	/** Height of the screen accessible to the Java AWT toolkit.			*/
	protected int		screenHeight;

	/** Frame that will display the tracer on the screen.					*/
	protected JFrame	frame;
	/** Text area in which the trace message will be output.				*/
	protected JTextPane	textArea;
	/** Title to be displayed by the tracer frame.							*/
	protected String 	title;
	/** X coordinate of the top left point of the application tracers.		*/
	protected int 		xOrigin;
	/** Y coordinate of the top left point of the application tracers.		*/
	protected int		yOrigin;
	/** Width of the frame in screen coordinates.							*/
	protected int		frameWidth;
	/** Height of the frame in screen coordinates.							*/
	protected int		frameHeight;
	/** X position of the frame among the application tracers.				*/
	protected int		xRelativePos;
	/** Y position of the frame among the application tracers.				*/
	protected int		yRelativePos;

	/** True if traces must be output and false otherwise.					*/
	protected boolean	tracingStatus;
	/** True if the trace is suspended and false otherwise.					*/
	protected boolean	suspendStatus;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a tracer with default parameters.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	{@code !this.isTracing()}
	 * post	{@code !this.isSuspended()}
	 * </pre>
	 *
	 */
	public				TracerWindow()
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.screenWidth = screenSize.width;
		this.screenHeight = screenSize.height;

		this.title = WINDOW_TITLE_PREFIX;
		this.xOrigin = 0;
		this.yOrigin = 0;
		this.frameWidth = screenSize.width / 4;
		this.frameHeight = screenSize.height / 5;

		// Given that in distributed execution, the global registry uses
		// 0 in standard, put this frame to its right.
		this.xRelativePos = 1 ;
		this.yRelativePos = 0 ;

		this.tracingStatus = false;
		this.suspendStatus = false;
	}

	/**
	 * create a tracer with the given parameters.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code xRelativePos >= 0}
	 * pre	{@code yRelativePos >= 0}
	 * post	{@code !this.isTracing()}
	 * post	{@code !this.isSuspended()}
	 * </pre>
	 *
	 * @param title			title to put on the frame.
	 * @param xRelativePos	x position of the frame in the group of frames.
	 * @param yRelativePos	x position of the frame in the group of frames.
	 */
	public				TracerWindow(
		String title,
		int xRelativePos,
		int yRelativePos
		)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.screenWidth = screenSize.width;
		this.screenHeight = screenSize.height;

		assert	xRelativePos >= 0 :
					new PreconditionException(
							"TracerWindow called with "
							+ "negative position: x = " + xRelativePos + "!");
		assert	yRelativePos >= 0 :
					new PreconditionException(
							"TracerWindow#setRelativePosition called with "
							+ "negative position: y = " + yRelativePos + "!");

		this.title = WINDOW_TITLE_PREFIX + ":" + title;
		this.xOrigin = 0;
		this.yOrigin = 0;
		this.frameWidth = screenSize.width / 4;
		this.frameHeight = screenSize.height / 5;
		this.xRelativePos = xRelativePos;
		this.yRelativePos = yRelativePos;

		this.tracingStatus = false;
		this.suspendStatus = false;
	}

	/**
	 * create a tracer with the given parameters.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code xOrigin >= 0}
	 * pre	{@code xOrigin < this.getScreenWidth()}
	 * pre	{@code yOrigin >= 0}
	 * pre	{@code yOrigin < this.getScreenHeight()}
	 * pre	{@code frameWidth > 0}
	 * pre	{@code frameHeight > 0}
	 * pre	{@code xRelativePos >= 0}
	 * pre	{@code yRelativePos >= 0}
	 * post	{@code !this.isTracing()}
	 * post	{@code !this.isSuspended()}
	 * </pre>
	 *
	 * @param title			title to put on the frame.
	 * @param xOrigin		x origin in screen unit.
	 * @param yOrigin		y origin in screen unit.
	 * @param frameWidth	width of the tracer frame.
	 * @param frameHeight	height of the tracer frame.
	 * @param xRelativePos	x position of the frame in the group of frames.
	 * @param yRelativePos	x position of the frame in the group of frames.
	 */
	public				TracerWindow(
		String title,
		int xOrigin,
		int yOrigin,
		int frameWidth,
		int frameHeight,
		int xRelativePos,
		int yRelativePos
		)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize() ;
		this.screenWidth = screenSize.width ;
		this.screenHeight = screenSize.height ;

		assert	xOrigin >= 0 :
					new PreconditionException(
							"TracerWindow called with negative x origin: "
							+ xOrigin + "!");
		assert	xOrigin < this.getScreenWidth() :
					new PreconditionException(
							"TracerWindow called with x origin "
							+ "outside the screen: " + xOrigin + "!");
		assert	yOrigin >= 0 :
					new PreconditionException(
							"TracerWindow called with negative "
							+ "y origin: " + yOrigin + "!");
		assert	yOrigin < this.screenHeight :
					new PreconditionException(
							"TracerWindow called with y origin "
							+ "outside the screen: " + yOrigin + "!");
		assert	frameWidth > 0 :
					new PreconditionException(
							"TracerWindow called with non positive frame "
							+ "width: " + frameWidth + "!");
		assert	frameHeight > 0 :
					new PreconditionException(
							"TracerWindow called with non positive frame "
							+ "height: " + frameHeight + "!");
		assert	xRelativePos >= 0 :
					new PreconditionException(
							"TracerWindow called with "
							+ "negative position: x = " + xRelativePos + "!");
		assert	yRelativePos >= 0 :
					new PreconditionException(
							"TracerWindow#setRelativePosition called with "
							+ "negative position: y = " + yRelativePos + "!");

		this.title = "Tracer:" + title;
		this.xOrigin = xOrigin;
		this.yOrigin = yOrigin;
		this.frameWidth = frameWidth;
		this.frameHeight = frameHeight;
		this.xRelativePos = xRelativePos;
		this.yRelativePos = yRelativePos;

		this.tracingStatus = false;
		this.suspendStatus = false;
	}

	/**
	 * intialise the trace window.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
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

		this.frame = new JFrame(this.title);
		this.frame.setBounds(
				this.xOrigin + this.xRelativePos * this.frameWidth,
				this.yOrigin + (this.yRelativePos * this.frameHeight) + 25,
				this.frameWidth,
				this.frameHeight);

		this.frame.getContentPane().add(
						new JScrollPane(textArea), BorderLayout.CENTER);
		this.frame.addWindowListener(this);
		this.frame.setVisible(true);
	}

	// -------------------------------------------------------------------------
	// TracerWindow specific methods
	// -------------------------------------------------------------------------

	/**
	 * return the screen width.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the screen width.
	 */
	public int			getScreenWidth()
	{
		return this.screenWidth ;
	}

	/**
	 * return the screen height.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the screen height.
	 */
	public int			getScreenHeight()
	{
		return this.screenHeight;
	}

	/**
	 * set the title of the tracer frame.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param title		title to be put on the frame.
	 */
	public void			setTitle(String title)
	{
		this.title = WINDOW_TITLE_PREFIX + ":" + title;
	}

	/**
	 * set the coordinate of the top left point in screen coordinates.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code xOrigin >= 0}
	 * pre	{@code xOrigin < this.getScreenWidth()}
	 * pre	{@code yOrigin >= 0}
	 * pre	{@code yOrigin < this.getScreenHeight()}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param xOrigin	x coordinate of the top left point in screen coordinates.
	 * @param yOrigin	y coordinate of the top left point in screen coordinates.
	 */
	public void			setOrigin(int xOrigin, int yOrigin)
	{
		assert	xOrigin >= 0 :
					new PreconditionException(
							"TracerWindow#setOrigin called with negative "
							+ "x origin: " + xOrigin + "!");
		assert	xOrigin < this.getScreenWidth() :
					new PreconditionException(
							"TracerWindow#setOrigin called with x origin "
							+ "outside the screen: " + xOrigin + "!");
		assert	yOrigin >= 0 :
					new PreconditionException(
							"TracerWindow#setOrigin called with negative "
							+ "y origin: " + yOrigin + "!");
		assert	yOrigin < this.screenHeight :
					new PreconditionException(
							"TracerWindow#setOrigin called with y origin "
							+ "outside the screen: " + yOrigin + "!");

		this.xOrigin = xOrigin;
		this.yOrigin = yOrigin;
	}

	/**
	 * set the tracer frame relative coordinates among the frames of the
	 * application.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code x >= 0}
	 * pre	{@code y >= 0}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param x	x relative coordinates among the frames of the application.
	 * @param y	y relative coordinates among the frames of the application.
	 */
	public void			setRelativePosition(int x, int y)
	{
		assert	x >= 0 : new PreconditionException(
							"TracerWindow#setRelativePosition called with "
							+ "negative position: x = " + x + "!");
		assert	y >= 0 : new PreconditionException(
							"TracerWindow#setRelativePosition called with "
							+ "negative position: y = " + y + "!");

		this.xRelativePos = x;
		this.yRelativePos = y;
	}

	/**
	 * return the tracing console visibility status.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the tracing console visibility status.
	 */
	public boolean		isVisible()
	{
		return this.frame.isVisible();
	}

	/**
	 * invert the visibility status of the tracing console.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isTracing()
	 * post	this.isVisible() == !this.isVisible()@pre
	 * </pre>
	 *
	 */
	public synchronized void		toggleVisible()
	{
		assert	this.isTracing();
		this.frame.setVisible(!this.frame.isVisible());
	}

	/**
	 * close the window.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
	 */
	public synchronized void		windowClosing(WindowEvent evt)
	{
		if (this.frame != null) {
			frame.setVisible(false);
			frame.dispose();
		}
	}

	// -------------------------------------------------------------------------
	// Tracer methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.helpers.TracerI#toggleTracing()
	 */
	@Override
	public synchronized void		toggleTracing()
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
	public synchronized void		traceMessage(String message)
	{
		if (this.tracingStatus && !this.suspendStatus) {
			StyledDocument doc =
						(StyledDocument) this.textArea.getDocument();
			try {
				doc.insertString(
						doc.getLength(),
						message,
						doc.getStyle("ConsoleStyle"));
			} catch (BadLocationException e) {
				throw new RuntimeException(
							"TracerWindow#traceMessage trying to show the"
							+ "message \"" + message + "\" but failed!", e);
			}
			this.textArea.setCaretPosition(
							 this.textArea.getDocument().getLength());
		}
	}
}
// -----------------------------------------------------------------------------
