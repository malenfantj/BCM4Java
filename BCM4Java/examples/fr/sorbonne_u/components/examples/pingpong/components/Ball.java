package fr.sorbonne_u.components.examples.pingpong.components;

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

import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.interfaces.DataTwoWayCI;


//-----------------------------------------------------------------------------
/**
 * The class <code>Ball</code> define ball objects for the
 * ping pong example.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		this.getNumberOfHits() &gt;= 0
 * </pre>
 * 
 * <p>Created on : 2018-03-15</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				Ball
implements	DataOfferedCI.DataI,
			DataRequiredCI.DataI,
			DataTwoWayCI.DataI
{
	private static final long serialVersionUID = 1L;
	protected int		numberOfHits ;

	/**
	 * create a ball with 0 initial hits.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	this.getNumberOfHits() == 0
	 * </pre>
	 *
	 */
	public				Ball()
	{
		this.numberOfHits = 0 ;

		assert	this.getNumberOfHits() == 0 ;
	}

	/**
	 * return the number of hits.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	ret &gt;= 0
	 * </pre>
	 * 
	 * @return	the number of hits.
	 */
	public int			getNumberOfHits()
	{
		return numberOfHits;
	}

	/**
	 * increment the number of hits on the ball.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	this.getNumberOfHits() == this.getNumberOfHits()@pre + 1
	 * </pre>
	 *
	 */
	public void			incrementNumberOfHits()
	{
		this.numberOfHits++;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String		toString() {
		return "Ball[" + this.getNumberOfHits() + "]" ;
	}
}
//-----------------------------------------------------------------------------
