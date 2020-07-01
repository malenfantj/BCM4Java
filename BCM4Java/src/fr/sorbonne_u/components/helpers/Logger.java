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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>Logger</code> implements a simple logging facility to
 * be used in the implementation of BCM.
 *
 * <p><strong>Description</strong></p>
 * 
 * The basic idea of the logger is to log messages during the execution
 * and output them to a file at the end. Log messages are accumulated in
 * an array list of strings so that the operation takes as little time
 * as possible and perturb the least possible the execution so that
 * execution with and without logging should be the same, as much as
 * possible.
 * 
 * A static configuration allows to define the directory in which the$
 * log file will be output as well as a few others parameters. Each log
 * is tagged by the current system time clearly separated from the rest
 * of the log entry by a known and statically modifiable character so
 * that log files can be input by a spreadsheet program with other log
 * files and them entries be sorted by the system time to analyse the
 * cooperative behavior among separate processes in an application.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-08-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			Logger
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** Default Character used to separate the time stamp from the log
	 *  message, which eases the processing of the file as a csv file
	 *  by spreadsheets.													*/
	public final static char	SEPARATION_CHARACTER = '|';
	/** initial size of in-memory buffer for logging messages.				*/
	public final static int		INITIAL_SIZE = 100;
	/** canonical name of the directory in which logs are written.			*/
	protected String			directory;
	/** name for the log file.												*/
	protected String			logFileName;
	/** file extension of logging files.									*/
	protected String			logFileExtension;
	/** character used to separate the time stamp from the log message.		*/
	protected char				separationChar;
	/** initial size of the log array in number of messages.				*/
	protected int				initialSize;
	/**	True if the component is doing logging of its actions.				*/
	protected boolean			loggingStatus = false;
	/**	The buffer in which logging messages are accumulated until their
	 *  writing on the logging file.										*/
	protected ArrayList<String>	executionLog;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a logger with a given file name.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	fileName != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param fileName	name of the file to output the log.
	 */
	public				Logger(String fileName)
	{
		this(System.getProperty("user.dir"), fileName, "log",
			 SEPARATION_CHARACTER, 100);

		assert	fileName != null :
					new PreconditionException(
							"creating a Logger with a null file name!");
	}

	/**
	 * create a logger with the given information.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code directory != null}
	 * pre	{@code fileName != null}
	 * pre	{@code extension != null}
	 * pre	{@code initialSize > 0}
	 * post	{@code this.getDirectory().equals(directory)}
	 * post	{@code this.getFileName().equals(fileName)}
	 * post	{@code this.getFileExtension().equals(extension)}
	 * post	{@code this.getSeparationCharacter() == separationChar}
	 * </pre>
	 *
	 * @param directory			directory in which the log file will be output.
	 * @param fileName			name of the file to output the log.
	 * @param extension			file extension of the log file.
	 * @param separationChar	character used to separate the time stamp from the log message.
	 * @param initialSize		initial size of the log array in number of messages.
	 */
	public				Logger(
		String directory,
		String fileName,
		String extension,
		char separationChar,
		int initialSize
		)
	{
		super();

		assert	directory != null :
					new PreconditionException(
							"creating a Logger with a null directory name!");
		assert	fileName != null :
					new PreconditionException(
							"creating a Logger with a null file name!");
		assert	extension != null :
					new PreconditionException(
							"creating a Logger with a null file extension name!");
		assert	initialSize > 0 :
					new PreconditionException(
							"creating a Logger with a non positive initial size:"
							+ initialSize + "!");

		this.directory = directory;
		this.logFileName = fileName;
		this.logFileExtension = extension;
		this.separationChar = separationChar;
		this.initialSize = initialSize;

		assert	this.getDirectory().equals(directory) :
					new PostconditionException(
							"Logger created but the directory is not correctly"
							+ " initialised!");
		assert	this.getFileName().equals(fileName) :
					new PostconditionException(
							"Logger created but the file name is not correctly"
							+ " initialised!");
		assert	this.getFileExtension().equals(extension) :
					new PostconditionException(
							"Logger created but the file extension is not "
							+ "correctly initialised!");
		assert	this.getSeparationCharacter() == separationChar :
					new PostconditionException(
							"Logger created but the separation character is "
							+ "not correctly initialised!");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * return the name of the directory in which the log file will be written.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the name of the directory in which the log file will be written.
	 */
	public String		getDirectory()
	{
		return this.directory;
	}

	/**
	 * set the name of the directory in which the log file will be written.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	directory != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param directory	the name of the directory in which the log file will be written.
	 */
	public void			setDirectory(String directory)
	{
		assert	directory != null :
					new PreconditionException(
							"Logger#setDirectory called with a null directory!");

		this.directory = directory;
	}

	/**
	 * return the name of the log file.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the name of the log file.
	 */
	public String		getFileName()
	{
		return this.logFileName;
	}

	/**
	 * set the name of the log file.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	fileName != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param fileName	the name of the log file.
	 */
	public void			setFileName(String fileName)
	{
		assert	fileName != null :
					new PreconditionException(
							"Logger#setFileName called with a null file name!");

		this.logFileName = fileName;
	}

	/**
	 * return the log file extension.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the log file extension.
	 */
	public String		getFileExtension()
	{
		return this.logFileExtension;
	}

	/**
	 * set the log file extension.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	extension != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param extension	the log file extension.
	 */
	public void			setFileExtension(String extension)
	{
		assert	extension != null :
					new PreconditionException(
							"Logger#setFileExtension called with a null file "
							+ "extension!");

		this.logFileExtension = extension;
	}

	/**
	 * return the initial size of the log or its actual one if different.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the initial size of the log or its actual one if different.
	 */
	public int			getSize()
	{
		return	this.executionLog == null ?
					this.initialSize
				:	this.executionLog.size();
	}

	/**
	 * return the separation character between the time stamp and the
	 * message in log entries.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the separation character between the time stamp and the message in log entries.
	 */
	public char			getSeparationCharacter()
	{
		return this.separationChar;
	}

	/**
	 * set the separation character between the time stamp and the
	 * message in log entries.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param separationChar		the separation character between the time stamp and the message in log entries.
	 */
	public void			setSeparationCharacter(char separationChar)
	{
		this.separationChar = separationChar;
	}

	/**
	 * toggle the logging status from logging to not logging or vice versa.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public void			toggleLogging()
	{
		this.loggingStatus = !this.loggingStatus;
		if (this.isLogging() && this.executionLog == null) {
			this.executionLog = new ArrayList<String>(this.initialSize);
		}
	}

	/**
	 * clear the accumulated log entries.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public void			clearLog()
	{
		this.executionLog = null;
		if (this.isLogging()) {
			this.executionLog = new ArrayList<String>(this.initialSize);
		}
	}

	/**
	 * return the logging status.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the logging status.
	 */
	public boolean		isLogging()
	{
		return this.loggingStatus;
	}

	/**
	 * add an entry in the log if the logging status is true.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	message != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param message	message provided by the log entry.
	 */
	public synchronized void	logMessage(String message)
	{
		assert	message != null :
					new PreconditionException(
							"Logger#logMessage called with a null message!") ;

		if (this.loggingStatus) {
			StringBuffer entry = new StringBuffer();
			entry.append(System.currentTimeMillis()).
					append(this.separationChar).append(message) ;
			this.executionLog.add(entry.toString()) ;
		}
	}

	/**
	 * output the execution log in the default log file.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws FileNotFoundException <i>todo.</i>
	 */
	public void			printExecutionLog()
	throws	FileNotFoundException
	{
		this.printExecutionLogOnFile(this.logFileName) ;
	}

	/**
	 * output the execution log in the given log file or the default
	 * one if null.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param fileName					a file name for output.
	 * @throws FileNotFoundException	<i>todo.</i>
	 */
	public void			printExecutionLogOnFile(String fileName)
	throws	FileNotFoundException
	{
		if (this.executionLog != null) {
			String name = null;
			if (fileName == null) {
				name = this.logFileName;
			} else {
				name = fileName;
			}
			if (name == null) {
				throw new FileNotFoundException(
						"Logger#printExecutionLogOnFile: fileName is null.");
			}
			StringBuffer extendedFileName =
									new StringBuffer(this.getDirectory());
			extendedFileName.append(File.separator).append(name).append('.').
												append(this.logFileExtension);
			File f = new File(extendedFileName.toString());
			PrintStream ps = new PrintStream(f);
			for (int i = 0 ; i < this.executionLog.size() ; i++) {
				ps.println(this.executionLog.get(i));
			}
			ps.close();
		}
	}
}
// -----------------------------------------------------------------------------
