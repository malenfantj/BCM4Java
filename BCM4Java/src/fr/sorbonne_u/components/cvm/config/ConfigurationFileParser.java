package fr.sorbonne_u.components.cvm.config;

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
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.cvm.config.exceptions.ConfigurationException;

// -----------------------------------------------------------------------------
/**
 * The class <code>ConfigurationFileParser</code> provides methods to validate
 * and parse component deployment configuration files.
 *
 * <p><strong>Description</strong></p>
 * 
 * The class relies on packages for XML processing to validate the configuration
 * file using the Relax NG schema <code>deployment.rnc</code> assumed to be
 * available in a directory <code>config</code> accessible from the base
 * directory of the running application.  The method
 * <code>parseConfigurationFile</code> parses the file and return the
 * information as an instance of the class <code>ConfigurationParameters</code>
 * that it returns as its result.
 * 
 * TODO: put the schema location in the configuration file?
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true	
 * </pre>
 * 
 * <p>Created on : 2012-10-26</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			ConfigurationFileParser
{
	/** standard file name for the schema file used to validate the
	 *  XML configuration file.												*/
	public static String	SCHEMA_FILENAME = "config" + File.separatorChar +
															"deployment.rnc" ;
	/** the XML document builder used to parse the configuration file.		*/
	protected DocumentBuilder db ;

	/**
	 * create the configuration file parser.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>

	 * @throws ConfigurationException <i>todo.</i>
	 *
	 */
	public				ConfigurationFileParser() throws ConfigurationException
	{
		super();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance() ;
		try {
			this.db = dbf.newDocumentBuilder() ;
		} catch (ParserConfigurationException e) {
			throw new ConfigurationException(
						"ConfigurationFileParser can't configure the XML "
						+ "document builder!", e) ;
		}
	}

	/**
	 * validate a configuration file against the configuration Relax NG schema
	 * <code>deployment.rnc</code> assumed to be available in a directory
	 * <code>config</code> accessible from the base directory of the running
	 * application
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param configFile				the File object which reference must be validated.
	 * @return							true if the file is valid, false otherwise
	 * @throws ConfigurationException 	<i>to do.</i>
	 */
	public boolean		validateConfigurationFile(File configFile)
	throws	ConfigurationException
	{
		// Specify you want a factory for RELAX NG
		System.setProperty(
			SchemaFactory.class.getName() + ":" + XMLConstants.RELAXNG_NS_URI,
			"com.thaiopensource.relaxng.jaxp.CompactSyntaxSchemaFactory");
		SchemaFactory factory =
			SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI);

		// Load the specific schema you want.
		// Here I load it from a java.io.File, but we could also use a
		// java.net.URL or a javax.xml.transform.Source
		File schemaLocation = new File(SCHEMA_FILENAME);

		// Compile the schema.
		Schema schema;
		try {
			schema = factory.newSchema(schemaLocation);
		} catch (SAXException e) {
			throw new ConfigurationException(e) ;
		}
		// Get a validator from the schema.
		Validator validator = schema.newValidator();
		// And finally, validate the file.
		try {
			validator.validate(new StreamSource(configFile));
		} catch (SAXException e) {
			throw new ConfigurationException(
								"configuration file XML validation problem "
								+ "(invalid format)", e) ;
		} catch (IOException e) {
			throw new ConfigurationException(
								"configuration file I/0 problem", e) ;
		}
		return true ;
	}

	/**
	 * parse the configuration file and return the information as an instance
	 * of <code>ConfigurationParameters</code>.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param configFile				the File object which reference must be parsed.
	 * @return							the configuration parameters.
	 * @throws ConfigurationException	<i>to do.</i>
	 */
	public ConfigurationParameters	parseConfigurationFile(File configFile)
	throws	ConfigurationException
	{
		String 						codebaseHostname = null ;
		String						codebaseDirectory = null ;
		Hashtable<String,String>	hosts2dirs = new Hashtable<String,String>() ;
		String						cyclicBarrierHostname = null ;
		int							cyclicBarrierPort = -1 ;
		String						globalRegistryHostname = null ;
		int							globalRegistryPort = -1 ;
		int							rmiRegistryPort = -1 ;
		String[]					jvmURIs = null ;
		Hashtable<String,String>	jvmURIs2hosts = new Hashtable<String,String>() ;
		Hashtable<String,String>	jvmURIs2mainclasses = new Hashtable<String,String>() ;
		HashSet<String> 			rmiRegistryCreators = new HashSet<String>() ;
		HashSet<String> 			rmiRegistryHosts = new HashSet<String>() ;

		Document doc = null ;
		try {
			doc = this.db.parse(configFile);
		} catch (SAXException e) {
			throw new ConfigurationException(
								"configuration file XML parsing problem "
								+ "(invalid format)", e) ;
		} catch (IOException e) {
			throw new ConfigurationException(
								"configuration file I/0 problem", e) ;
		}

		XPath xpathEvaluator = XPathFactory.newInstance().newXPath() ;

		Node codebaseNode;
		try {
			codebaseNode = ((Node)xpathEvaluator.evaluate(
											"/deployment/codebase",
											doc,
											XPathConstants.NODE));
		} catch (XPathExpressionException e) {
			throw new ConfigurationException(
						"error fetching the code base node", e) ;
		}
		if (codebaseNode != null) {
			try {
				codebaseHostname =
						((Node)xpathEvaluator.evaluate(
								"@hostname",
								codebaseNode,
								XPathConstants.NODE)).getNodeValue() ;
			} catch (DOMException e) {
				throw new ConfigurationException(
							"node access error for the code base hostname node",
							e) ;
			} catch (XPathExpressionException e) {
				throw new ConfigurationException(
							"error fetching the code base hostname node", e) ;
			}
			try {
				codebaseDirectory = ((Node)xpathEvaluator.evaluate(
											"@directory",
											codebaseNode,
											XPathConstants.NODE)).getNodeValue() ;
			} catch (DOMException e) {
				throw new ConfigurationException(
							"node access error for the code base "
							+ " directory node", e) ;
			} catch (XPathExpressionException e) {
				throw new ConfigurationException(
							"error fetching the code base directory node", e) ;
			}
		}

		NodeList hs;
		try {
			hs = (NodeList)xpathEvaluator.evaluate(
											"/deployment/hosts/host",
											doc,
											XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new ConfigurationException(
						"error fetching the list of host nodes", e) ;
		}
		for (int i = 0 ; i < hs.getLength() ; i++) {
			String name;
			try {
				name = ((Node)xpathEvaluator.evaluate(
									"@name", hs.item(i), XPathConstants.NODE)).
																getNodeValue();
			} catch (DOMException e) {
				throw new ConfigurationException(
							"node access error for the name attribute of a "
							+ "host node", e) ;
			} catch (XPathExpressionException e) {
				throw new ConfigurationException(
							"error fetching the name attribute of a host node",
							e) ;
			}
			String dir;
			try {
				dir = ((Node)xpathEvaluator.evaluate(
									"@dir", hs.item(i), XPathConstants.NODE)).
																getNodeValue();
			} catch (DOMException e) {
				throw new ConfigurationException(
							"node access error for the dir attribute of the "
							+ "host node " + name, e) ;
			} catch (XPathExpressionException e) {
				throw new ConfigurationException(
						"error fetching the dir attribute of the host node "
						+ name, e) ;
			}
			hosts2dirs.put(name, dir) ;
		}

		try {
			cyclicBarrierHostname =
					((Node)xpathEvaluator.evaluate(
							"/deployment/cyclicBarrier/@hostname",
							doc,
							XPathConstants.NODE)).getNodeValue() ;
		} catch (DOMException e) {
			throw new ConfigurationException(
						"node access error for the hostname attribute of the "
						+ "cyclicBarrier node", e) ;
		} catch (XPathExpressionException e) {
			throw new ConfigurationException(
						"error fetching the hostname attribute of the "
						+ "cyclicBarrier node", e) ;
		}
		assert	cyclicBarrierHostname != null :
					new ConfigurationException(
									"Cyclic barrier hostname undefined!") ;
		try {
			cyclicBarrierPort =
					Integer.parseInt(
						((Node)xpathEvaluator.evaluate(
								"/deployment/cyclicBarrier/@port",
								doc,
								XPathConstants.NODE)).getNodeValue()) ;
		} catch (NumberFormatException e) {
			throw new ConfigurationException(
						"cyclic barrier port number not a number!", e) ;
		} catch (DOMException e) {
			throw new ConfigurationException(
						"node access error for the port attribute of the "
						+ "cyclicBarrier node", e) ;
		} catch (XPathExpressionException e) {
			throw new ConfigurationException(
						"error fetching the port attribute of the "
						+ "cyclicBarrier node", e) ;
		}
		assert	cyclicBarrierPort > 0 && cyclicBarrierPort <= 65535 :
					new ConfigurationException(
							"Cyclic barrier port illegal: "
										  		+ cyclicBarrierPort + "!") ;

		try {
			globalRegistryHostname =
					((Node)xpathEvaluator.evaluate(
							"/deployment/globalRegistry/@hostname",
							doc,
							XPathConstants.NODE)).getNodeValue() ;
		} catch (DOMException e) {
			throw new ConfigurationException(
						"node access error for the hostname attribute of the "
						+ "globalRegistry node", e) ;
		} catch (XPathExpressionException e) {
			throw new ConfigurationException(
						"error fetching the hostname attribute of the "
						+ "globalRegistry node", e) ;
		}
		assert	globalRegistryHostname != null :
					new ConfigurationException(
							"Global registry hostname undefined!") ;
		try {
			globalRegistryPort =
					Integer.parseInt(
							((Node)xpathEvaluator.evaluate(
									"/deployment/globalRegistry/@port",
									doc,
									XPathConstants.NODE)).getNodeValue()) ;
		} catch (NumberFormatException e) {
			throw new ConfigurationException(
						"global registry port number not a number!", e) ;
		} catch (DOMException e) {
			throw new ConfigurationException(
						"node access error for the port attribute of the "
						+ "globalRegistry node", e) ;
		} catch (XPathExpressionException e) {
			throw new ConfigurationException(
						"error fetching the port attribute of the "
						+ "globalRegistry node", e) ;
		}
		assert	globalRegistryPort > 0 && globalRegistryPort <= 65535 :
					new ConfigurationException(
							"Global registry port illegal: "
												+ globalRegistryPort + "!") ;

		try {
			rmiRegistryPort =
					Integer.parseInt(
							((Node)xpathEvaluator.evaluate(
									"/deployment/rmiRegistryPort/@no",
									doc,
									XPathConstants.NODE)).getNodeValue()) ;
		} catch (NumberFormatException e) {
			throw new ConfigurationException(
					"rmi registry port number not a number!", e) ;
		} catch (DOMException e) {
			throw new ConfigurationException(
						"node access error for the no attribute of the "
						+ "rmiRegistryPort node", e) ;
		} catch (XPathExpressionException e) {
			throw new ConfigurationException(
						"error fetching the no attribute of the "
						+ "rmiRegistryPort node", e) ;
		}
		assert	rmiRegistryPort > 0 && rmiRegistryPort <= 65535 :
					new ConfigurationException(
							"RMI registry port illegal: "
										   + rmiRegistryPort + "!") ;

		NodeList ns;
		try {
			ns = (NodeList)xpathEvaluator.evaluate(
						"/deployment/jvms2hostnames/jvm2hostname/@jvmuri",
						doc,
						XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new ConfigurationException(
						"error fetching the list of jvmuri attributes of "
						+ "jvm2hostname nodes", e) ;
		}
		jvmURIs = new String[ns.getLength()] ;
		for (int i = 0 ; i < ns.getLength() ; i++) {
			jvmURIs[i] = ns.item(i).getNodeValue() ;
			assert	jvmURIs[i] != null :
						new ConfigurationException("JVM uri undefined!") ;
		}
		try {
			ns = (NodeList)xpathEvaluator.evaluate(
						"/deployment/jvms2hostnames/jvm2hostname",
						doc,
						XPathConstants.NODESET) ;
		} catch (XPathExpressionException e) {
			throw new ConfigurationException(
						"error fetching the list of jvm2hostname nodes", e) ;
		}
		Set<String> allHostnames = new HashSet<String>() ;
		Set<String> reflectiveJVM_URIs = new HashSet<String>() ;
		for (int i = 0 ; i < ns.getLength() ; i++) {
			String uri;
			try {
				uri = ((Node)xpathEvaluator.evaluate(
								"@jvmuri", ns.item(i), XPathConstants.NODE)).
																getNodeValue();
			} catch (DOMException e) {
				throw new ConfigurationException(
							"node access error for the jvmuri attribute of a "
							+ "jvm2hostname node", e) ;
			} catch (XPathExpressionException e) {
				throw new ConfigurationException(
							"error fetching the jvmuri attribute of a "
							+ "jvm2hostname node", e) ;
			}
			assert	uri != null :
						new ConfigurationException("JVM uri undefined!") ;
			String hostname;
			try {
				hostname = ((Node)xpathEvaluator.evaluate(
								"@hostname", ns.item(i), XPathConstants.NODE)).
																getNodeValue();
			} catch (DOMException e) {
				throw new ConfigurationException(
							"node access error for the hostname attribute of "
							+ "the jvm2hostname node " + uri, e) ;
			} catch (XPathExpressionException e) {
				throw new ConfigurationException(
							"error fetching the hostname attribute of "
							+ "the jvm2hostname node " + uri, e) ;
			}
			assert	hostname != null :
						new ConfigurationException("Hostname of JVM " + uri
															+ " undefined!");
			allHostnames.add(hostname) ;
			jvmURIs2hosts.put(uri, hostname) ;

			String mainclass;
			try {
				mainclass = ((Node)xpathEvaluator.evaluate(
									"@mainclass", ns.item(i),
										XPathConstants.NODE)).getNodeValue();
			} catch (DOMException e) {
				throw new ConfigurationException(
							"node access error for the mainclass attribute of "
							+ "the jvm2hostname node " + uri, e) ;
			} catch (XPathExpressionException e) {
				throw new ConfigurationException(
							"error fetching the mainclass attribute of "
							+ "the jvm2hostname node " + uri, e) ;
			}
			assert	mainclass != null :
					new ConfigurationException(
							"Main class undefined for the JVM " + uri + "!") ;

			jvmURIs2mainclasses.put(uri, mainclass) ;

			Node reflectiveNode;
			try {
				reflectiveNode =
						((Node)xpathEvaluator.evaluate(
							"@reflective", ns.item(i), XPathConstants.NODE));
			} catch (XPathExpressionException e) {
				throw new ConfigurationException(
							"error fetching the reflective attribute of "
							+ "the jvm2hostname node " + uri + "!", e) ;
			}
			String reflective = null ;
			if (reflectiveNode != null) {
				reflective = reflectiveNode.getNodeValue() ;
			}
			if (reflective != null && reflective.equals("true")) {
				reflectiveJVM_URIs.add(uri) ;
			}

			String rmiRegistryCreator;
			try {
				rmiRegistryCreator =
						((Node)xpathEvaluator.evaluate(
								"@rmiRegistryCreator", ns.item(i),
										XPathConstants.NODE)).getNodeValue();
			} catch (DOMException e) {
				throw new ConfigurationException(
							"node access error for the rmiRegistryCreator "
							+ "attribute of the jvm2hostname node " + uri, e) ;
			} catch (XPathExpressionException e) {
				throw new ConfigurationException(
						"error fetching the rmiRegistryCreator "
						+ "attribute of the jvm2hostname node " + uri, e) ;
			}
			assert	rmiRegistryCreator != null :
						new ConfigurationException(
								"attribute rmiRegistryCreator " + 
								"of the jvm2hostname node " + uri +
								" undefined!");
			if (rmiRegistryCreator.equals("true")) {
				rmiRegistryCreators.add(uri) ;
				rmiRegistryHosts.add(hostname) ;
			}
		}

		if (AbstractDistributedCVM.RMI_REGISTRY_ON_ALL_HOSTS) {
			boolean	allHostsHaveRMIRegistryCreator = true ;
			for (String s : allHostnames) {
				allHostsHaveRMIRegistryCreator = rmiRegistryHosts.contains(s) ;
			}
			assert	allHostsHaveRMIRegistryCreator :
						new ConfigurationException(
							"Some hosts do not have a RMI registry creator!") ;
		} else {
			assert	rmiRegistryHosts.size() > 0 :
						new ConfigurationException(
								"RMI registry creator undefined!") ;
		}

		return new ConfigurationParameters(codebaseHostname,
										   codebaseDirectory,
										   hosts2dirs,
										   cyclicBarrierHostname,
										   cyclicBarrierPort,
										   globalRegistryHostname,
										   globalRegistryPort,
										   rmiRegistryPort,
										   jvmURIs,
										   jvmURIs2hosts,
										   jvmURIs2mainclasses,
										   rmiRegistryCreators,
										   rmiRegistryHosts,
										   reflectiveJVM_URIs) ;
	}
}
// -----------------------------------------------------------------------------
