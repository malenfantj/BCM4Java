package fr.sorbonne_u.components.cvm.config;

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

import java.util.Hashtable;
import java.util.Set;

//-----------------------------------------------------------------------------
/**
 * The class <code>ConfigurationParameters</code> defines objects holding the
 * component deployment configuration parameters.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true		// TODO
 * </pre>
 * 
 * <p>Created on : 2012-10-26</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				ConfigurationParameters
{
	protected String						codebaseHostname ;
	protected String						codebaseDirectory ;
	protected Hashtable<String,String>	hosts2dirs ;
	protected String						cyclicBarrierHostname ;
	protected int						cyclicBarrierPort ;
	protected String						globalRegistryHostname ;
	protected int						globalRegistryPort ;
	protected int						rmiregistryPort ;
	protected String[]					jvms ;
	protected Set<String>				rmiRegistryCreators ;
	protected Set<String>				rmiRegistryHosts ;
	protected Hashtable<String,String>	jvms2hosts ;
	protected Hashtable<String,String>	jvms2mainclasses ;
	protected Set<String>				reflectiveJVMs ;

	public				ConfigurationParameters(
		String						codebaseHostname,
		String						codebaseDirectory,
		Hashtable<String,String>		hosts2dirs,
		String						cyclicBarrierHostname,
		int							cyclicBarrierPort,
		String						globalRegistryHostname,
		int							globalRegistryPort,
		int							rmiregistryPort,
		String[]						jvms,
		Hashtable<String,String>		jvms2hosts,
		Hashtable<String,String>		jvms2mainclasses,
		Set<String>					rmiRegistryCreators,
		Set<String>					rmiRegistryHosts,
		Set<String>					reflectiveJVMs
		)
	{
		super();
		this.codebaseHostname = codebaseHostname ;
		this.codebaseDirectory = codebaseDirectory ;
		this.hosts2dirs = hosts2dirs ;
		this.cyclicBarrierHostname = cyclicBarrierHostname ;
		this.cyclicBarrierPort = cyclicBarrierPort ;
		this.globalRegistryHostname = globalRegistryHostname;
		this.globalRegistryPort = globalRegistryPort;
		this.rmiregistryPort = rmiregistryPort ;
		this.jvms = jvms;
		this.jvms2hosts = jvms2hosts ;
		this.jvms2mainclasses = jvms2mainclasses ;
		this.rmiRegistryCreators = rmiRegistryCreators ;
		this.rmiRegistryHosts = rmiRegistryHosts ;
		this.reflectiveJVMs = reflectiveJVMs ;
	}

	/**
	 * @return the codebaseHostname
	 */
	public String		getCodebaseHostname() {
		return this.codebaseHostname;
	}

	/**
	 * @return the codebaseDirectory
	 */
	public String		getCodebaseDirectory() {
		return this.codebaseDirectory;
	}

	/**
	 * @return the hosts2dirs
	 */
	public Hashtable<String, String>	getHosts2dirs()
	{
		return hosts2dirs;
	}

	/**
	 * @return the synchronizerHostname
	 */
	public String		getCyclicBarrierHostname() {
		return this.cyclicBarrierHostname;
	}

	/**
	 * @return the synchronizerPort
	 */
	public int			getCyclicBarrierPort() {
		return this.cyclicBarrierPort;
	}

	/**
	 * @return the globalRegistryHostname
	 */
	public String		getGlobalRegistryHostname() {
		return this.globalRegistryHostname;
	}

	/**
	 * @return the globalRegistryPort
	 */
	public int			getGlobalRegistryPort() {
		return this.globalRegistryPort;
	}

	/**
	 * @return the rmiregistryPort
	 */
	public int			getRmiregistryPort() {
		return this.rmiregistryPort;
	}

	/**
	 * @return the jvms
	 */
	public String[]		getJvms() {
		return this.jvms;
	}

	/**
	 * @return the jvms2hosts
	 */
	public Hashtable<String, String> getJvms2hosts() {
		return this.jvms2hosts;
	}

	/**
	 * @return the jvms2mainclasses
	 */
	public Hashtable<String, String> getJvms2mainclasses() {
		return jvms2mainclasses;
	}

	/**
	 * @return the reflectiveJVMs
	 */
	public Set<String>	getReflectiveJVMs() {
		return reflectiveJVMs;
	}

	/**
	 * @return the rmiRegistryCreators
	 */
	public Set<String>	getRmiRegistryCreators() {
		return this.rmiRegistryCreators;
	}

	/**
	 * @return the rmiRegistryHosts
	 */
	public Set<String>	getRmiRegistryHosts() {
		return this.rmiRegistryHosts;
	}

	@Override
	public String		toString() {
		String rjvms = "{" ;
		for (int i = 0 ; i < this.jvms.length ; i++) {
			rjvms += this.jvms[i] ;
			if (i < this.jvms.length -1) {
				rjvms += ", " ;
			}
		}
		rjvms += "}" ;
		String theHosts = "{" ;
		int n = 0 ;
		for(String name : this.hosts2dirs.keySet()) {
			theHosts += name + " ==> " + this.hosts2dirs.get(name) ;
			n++ ;
			if (n < this.hosts2dirs.size() - 1) {
				theHosts += ", " ;
			}
		}
		theHosts += "}" ;
		String theMainClasses = "{" ;
		n = 0 ;
		for (String uri : this.jvms2mainclasses.keySet()) {
			theMainClasses += uri + " ==> " + this.jvms2mainclasses.get(uri) ;
			if (n < this.jvms2mainclasses.size() - 1) {
				theMainClasses += ", " ;
			}
		}
		theMainClasses += "}" ;
		String theReflectives = "{" ;
		n = 0 ;
		for (String jvms : this.reflectiveJVMs) {
			theReflectives += jvms ;
			n++ ;
			if (n < this.reflectiveJVMs.size() - 1) {
				theReflectives += ", " ;
			}
		}
		theReflectives += "}" ;
		return "ConfigurationParameters[" +
					"codebase hostname: " + this.codebaseHostname + "; " +
					"codebase directory: " + this.codebaseDirectory + "; " +
					"hosts: " + theHosts + "; " +
					"cyclicBarrier hostname: " + this.cyclicBarrierHostname + "; " +
					"cyclicBarrier port: " + this.cyclicBarrierPort + "; " +
					"global registry hostname: " + this.globalRegistryHostname + "; " +
					"global registry port: " + this.globalRegistryPort + "; " +
					"RMI registry creators: " + this.rmiRegistryCreators + "; " +
					"RMI registry hosts: " + this.rmiRegistryHosts + "; " +
					"rmiregistry port: " + this.rmiregistryPort + "; " +
					"jvms: " + rjvms + "; " +
					"main classes: " + theMainClasses + "; " +
					"reflective JVMs: " + theReflectives + "; " +
				"]" ;
	}
}
//-----------------------------------------------------------------------------
