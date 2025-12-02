package fr.sorbonne_u.components.utils.tests;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>TestsStatistics</code> implements an object used to
 * collect statistics about test scenarios.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-09-22</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			TestsStatistics
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** number of test scenarios that succeeded.							*/
	protected int		numberOfSuccessfulScenarios;
	/** number of test scenarios that failed.								*/
	protected int		numberOfFailedScenarios;
	/** keeps track of the success of conditions in each scenario.			*/
	protected boolean	conditions = true;
	/** keeps track of the correctness of results in each scenario.			*/
	protected boolean	correctResult = true;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a test statistics collector.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public				TestsStatistics()
	{
		this.initialise();
	}

	/**
	 * initialise the statistics.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void			initialise()
	{
		this.numberOfFailedScenarios = 0;
		this.numberOfSuccessfulScenarios = 0;
		this.conditions = true;
		this.correctResult = true;

	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * signal a failed condition.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void			failedCondition()
	{
		this.conditions &= false;
	}

	/**
	 * signal an incorrect result.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void			incorrectResult()
	{
		this.correctResult &= false;
	}

	/**
	 * update the statistics on the successful and failed scenario and
	 * reinitialise for the next scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 */
	public void			updateStatistics()
	{
		if (this.conditions && this.correctResult) {
			this.numberOfSuccessfulScenarios++;
		} else {
			this.numberOfFailedScenarios++;
		}
		this.conditions = true;
		this.correctResult = true;
	}

	/**
	 * log the tests statistics.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code c != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @param c	the component executing the tests.
	 */
	public void			statisticsReport(AbstractComponent c)
	{
		assert	c != null : new PreconditionException("c != null");

		c.logMessage("Number of successful scenarios: "
											+ this.numberOfSuccessfulScenarios);
		c.logMessage("Number of failed scenarios: "
											+ this.numberOfFailedScenarios);
	}
}
// -----------------------------------------------------------------------------
