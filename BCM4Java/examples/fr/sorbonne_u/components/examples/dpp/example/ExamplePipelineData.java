package fr.sorbonne_u.components.examples.dpp.example;

import fr.sorbonne_u.components.examples.dpp.interfaces.PipelineDataI;

// -----------------------------------------------------------------------------
/**
 * The class <code>ExamplePipelineData</code> implements a simple data object
 * for the data processing pipeline example.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The data itself is an integer. However, to keep track of the initial
 * value passed to the pipeline, the object keeps it and work on a result
 * that will be changed at each step in the pipeline. The method
 * {@code isResultSet} is used to distinguish the initial case <i>i.e.</i>,
 * the first processing component that must use the initial value, from
 * the other cases where it is the result of the previous processing component
 * that must be processed.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}
 * </pre>
 * 
 * <p>Created on : 2022-06-03</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			ExamplePipelineData
implements	PipelineDataI
{
	private static final long serialVersionUID = 1L;
	/** initial value pushed into the pipeline.								*/
	protected int		initialParameter;
	/** true if the next value to be processed is the {@code result}.		*/
	protected boolean	resultSet;
	/**	current result of the pipeline.									 	*/
	protected int		result;

	/**
	 * create a piece of data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code !isResultSet()}
	 * </pre>
	 *
	 * @param initialParameter	initial value pushed into the pipeline.
	 */
	public				ExamplePipelineData(int initialParameter)
	{
		this.initialParameter = initialParameter;
		this.resultSet = false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String		toString()
	{
		return "(" + this.initialParameter + ", " + this.result + ")";
	}

	/**
	 * return the initial value passed to the pipeline.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the initial value passed to the pipeline.
	 */
	public int			getInitialParameter()
	{
		return this.initialParameter;
	}

	/**
	 * return true if the next value to be processed is the result of a
	 * previous step.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the next value to be processed is the result of a previous step.
	 */
	public boolean		isResultSet()
	{
		return this.resultSet;
	}

	/**
	 * copy the current piece of data setting the result of the new piece of
	 * data to {@code r}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret.isResultSet()}
	 * </pre>
	 *
	 * @param r	the result assigned to the new piece of data.
	 * @return	the new piece of data.
	 */
	public ExamplePipelineData	setResult(int r)
	{
		ExamplePipelineData ret =
				new ExamplePipelineData(this.getInitialParameter());
		ret.resultSet = true;
		ret.result = r;
		return ret;
	}

	/**
	 * return	the result assigned to this piece of data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isResultSet()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the result assigned to this piece of data.
	 */
	public int			getResult()
	{
		assert	this.isResultSet();
		return this.result;
	}
}
// -----------------------------------------------------------------------------
