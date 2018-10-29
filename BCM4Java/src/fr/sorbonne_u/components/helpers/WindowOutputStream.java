package fr.sorbonne_u.components.helpers;

//Copyright Jacques Malenfant, Sorbonne Universite.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

//-----------------------------------------------------------------------------
/**
 * The class <code>WindowOutputStream</code> creates a JFrame with
 * a text area to which the stdout and the stderr are redirected.
 *
 * <p><strong>Description</strong></p>
 * 
 * Slightly adapted from an example found on the Internet...
 * TODO: No time right now to do better.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-08-28</p>
 * 
 * @author	unknown
 */
public class				WindowOutputStream
extends		WindowAdapter
implements	WindowListener,
			Runnable
{
	private JFrame frame;
	private JTextPane textArea;
	private Thread stdOutReader;
	private Thread stdErrReader;
	private boolean stopThreads;
	private final PipedInputStream stdOutPin = new PipedInputStream();
	private final PipedInputStream stdErrPin = new PipedInputStream();

	//Used to print error messages in red
	private StyledDocument doc;
	private Style style;

	/**
	 * create a new frame in a group where the first will be put at the
	 * position (x, y) while xLayout and yLayout allow to position the
	 * frame in the group (e.g., xLayout = 1 and yLayout = 1 will put
	 * the frame at the right and below the origin frame).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param frameTitle		title to be put on the frame.
	 * @param x				x origin of the first frame.
	 * @param y				y origin of the first frame.
	 * @param xLayout		increment in x (starting at 0) giving the relative position of the frame.
	 * @param yLayout		increment in y (starting at 0) giving the relative position of the frame.
	 */
	public				WindowOutputStream(
		String frameTitle,
		int x,
		int y,
		int xLayout,
		int yLayout
		)
	{
		// The area to which the output will be send to
		textArea = new JTextPane();
		textArea.setEditable(false);
		textArea.setBackground(Color.WHITE);
		doc = (StyledDocument) textArea.getDocument();
		style = doc.addStyle("ConsoleStyle", null);
		StyleConstants.setFontFamily(style, "MonoSpaced");
		StyleConstants.setFontSize(style, 10);

		// Main frame to which the text area will be added to, along with scroll bars
		frame = new JFrame(frameTitle);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize() ;
		Dimension frameSize = new Dimension(screenSize.width / 4, screenSize.height / 5);
		frame.setBounds(x + xLayout * frameSize.width,
						y + (yLayout * frameSize.height) + 25,
						frameSize.width,
						frameSize.height);

		frame.getContentPane().add(new JScrollPane(textArea),
								  BorderLayout.CENTER);
		frame.setVisible(true);

		frame.addWindowListener(this);

		try {
			PipedOutputStream stdOutPos = new PipedOutputStream(this.stdOutPin);
			System.setOut(new PrintStream(stdOutPos, true));
		} catch (java.io.IOException io) {
			textArea.setText("Couldn't redirect STDOUT to this console\n" + io.getMessage());
		} catch (SecurityException se) {
			textArea.setText("Couldn't redirect STDOUT to this console\n" + se.getMessage());
		}

		try {
			PipedOutputStream stdErrPos = new PipedOutputStream(this.stdErrPin);
			System.setErr(new PrintStream(stdErrPos, true));
		} catch (java.io.IOException io) {
			textArea.setText("Couldn't redirect STDERR to this console\n" + io.getMessage());
		} catch (SecurityException se) {
			textArea.setText("Couldn't redirect STDERR to this console\n" + se.getMessage());
		}

		stopThreads = false; // Will be set to true at closing time. This will stop the threads

		// Starting two threads to read the PipedInputStreams
		stdOutReader = new Thread(this);
		stdOutReader.setDaemon(true);
		stdOutReader.start();

		stdErrReader = new Thread(this);
		stdErrReader.setDaemon(true);
		stdErrReader.start();
	}

	/**
	 * Closes the window and stops the "stdOutReader" threads
	 *
	 * @param evt WindowEvent
	 */
	public synchronized void	windowClosed(WindowEvent evt)
	{
		// Notify the threads that they must stop
		stopThreads = true;
		this.notifyAll();

		try {
			stdOutReader.join(1000);
			stdOutPin.close();
		} catch (Exception e) {
		}
		try {
			stdErrReader.join(1000);
			stdErrPin.close();
		} catch (Exception e) {
		}
	}

	/** Close the window */
	public synchronized void	windowClosing(WindowEvent evt)
	{
		frame.setVisible(false) ;
		frame.dispose() ;
	}

	/** The real work... */
	public synchronized void		run()
	{
		try {
			while (Thread.currentThread() == stdOutReader) {
				try {
					this.wait(100);
				} catch (InterruptedException ie) {
				}
				if (stdOutPin.available() != 0) {
					String input = this.readLine(stdOutPin);
					StyleConstants.setForeground(style, Color.black);
					doc.insertString(doc.getLength(), input, style);
					// Make sure the last line is always visible
					textArea.setCaretPosition(textArea.getDocument().getLength());
				}
				if (stopThreads) {
					return;
				}
			}

			while (Thread.currentThread() == stdErrReader) {
				try {
					this.wait(100);
				} catch (InterruptedException ie) {
				}
				if (stdErrPin.available() != 0) {
					String input = this.readLine(stdErrPin);
					StyleConstants.setForeground(style, Color.red);
					doc.insertString(doc.getLength(), input, style);
					// Make sure the last line is always visible
					textArea.setCaretPosition(textArea.getDocument().getLength());
				}
				if (stopThreads) {
					return;
				}
			}
		} catch (Exception e) {
			textArea.setText("\nConsole reports an Internal error.");
			textArea.setText("The error is: " + e);
		}
	}

	private synchronized String	readLine(PipedInputStream in)
	throws IOException
	{
		String input = "";
		do {
			int available = in.available();
			if (available == 0) {
				break;
			}
			byte b[] = new byte[available];
			in.read(b);
			input += new String(b, 0, b.length);
		} while (!input.endsWith("\n") && !input.endsWith("\r\n") &&
				 !stopThreads);
		return input;
	}
}
//-----------------------------------------------------------------------------
