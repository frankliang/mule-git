/* 
* $Header$
* $Revision$
* $Date$
* ------------------------------------------------------------------------------------------------------
* 
* Copyright (c) SymphonySoft Limited. All rights reserved.
* http://www.symphonysoft.com
* 
* The software in this package is published under the terms of the BSD
* style license a copy of which has been included with this distribution in
* the LICENSE.txt file. 
*
*/
package org.mule.config.builders;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.NodeCreateRule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleManager;
import org.mule.config.ConfigurationException;
import org.mule.config.MuleDtdResolver;
import org.mule.config.ReaderResource;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.container.MuleContainerContext;
import org.mule.umo.UMOFilter;
import org.mule.umo.manager.ContainerException;
import org.mule.umo.manager.UMOContainerContext;
import org.mule.util.ClassHelper;
import org.mule.util.Utility;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A base classs for configuration schemes that use digester to parse the
 * documents.
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public abstract class AbstractDigesterConfiguration
{
    public static final String DEFAULT_CONTAINER_CONTEXT = MuleContainerContext.class.getName();
    public static final String FILTER_INTERFACE = UMOFilter.class.getName();

    /**
     * logger used by this class
     */
    protected transient Log logger = LogFactory.getLog(getClass());

    protected Digester digester;
    protected List containerReferences = new ArrayList();

    protected AbstractDigesterConfiguration(boolean validate, String dtd) {
        // This is a hack to stop Digester spitting out unnecessary warnings
        // where there is
        // a customer error handler registered
        digester = new Digester() {
            public void warning(SAXParseException e) throws SAXException {
                if (errorHandler != null) {
                    errorHandler.warning(e);
                }
            }
        };

        digester.setValidating(validate);
        digester.setEntityResolver(new MuleDtdResolver(dtd));

        digester.setErrorHandler(new ErrorHandler() {
            public void error(SAXParseException exception) throws SAXException {
                logger.error(exception.getMessage(), exception);
                throw new SAXException(exception);
            }

            public void fatalError(SAXParseException exception) throws SAXException {
                logger.fatal(exception.getMessage(), exception);
                throw new SAXException(exception);
            }

            public void warning(SAXParseException exception) {
                logger.warn(exception.getMessage());
            }
        });
    }

    protected ReaderResource[] parseResources(String configResources) throws ConfigurationException {
        String[] resources = Utility.split(configResources, ",");
        MuleManager.getConfiguration().setConfigResources(resources);
        ReaderResource[] readers = new ReaderResource[resources.length];
        for (int i = 0; i < resources.length; i++) {
            try {
                readers[i] = new ReaderResource(resources[i].trim(),
                        new InputStreamReader(loadConfig(resources[i].trim()), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new ConfigurationException(e);
            }
        }
        return readers;
    }

    protected Object process(ReaderResource[] configResources) throws ConfigurationException {
        Object result = null;
        Reader configResource = null;
        for (int i = 0; i < configResources.length; i++) {
            try {
                configResource = configResources[i].getReader();
                result = digester.parse(configResource);
            } catch (Exception e) {
                throw new ConfigurationException(new Message(Messages.FAILED_TO_PARSE_CONFIG_RESOURCE_X,
                        configResources[i].getDescription()), e);
            }
        }

        return result;
    }

    /**
     * ConfigResource can be a url, a path on the local file system or a
     * resource name on the classpath Finds and loads the configuration resource
     * by doing the following - 1. load it form the classpath 2. load it from
     * from the local file system 3. load it as a url
     *
     * @param configResource
     * @return an inputstream to the resource
     * @throws ConfigurationException
     */
    protected InputStream loadConfig(String configResource) throws ConfigurationException {
        InputStream is = ClassHelper.getResourceAsStream(configResource, getClass());
        if (is == null) {
            File file = new File(configResource);
            if (file.exists()) {
                try {
                    is = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    throw new ConfigurationException(new Message(Messages.CANT_LOAD_X_FROM_CLASSPATH_FILE,
                            configResource), e);
                }
            } else {
                try {
                    URL url = new URL(configResource);
                    is = url.openStream();
                } catch (Exception e) {
                    throw new ConfigurationException(new Message(Messages.CANT_LOAD_X_FROM_CLASSPATH_FILE,
                            configResource));
                }
            }
        }
        return is;
    }

    public abstract String getRootName();

    protected void addContainerContextRules(String path, String setterMethod, int parentIndex) throws ConfigurationException {
        // Create container Context
        digester.addObjectCreate(path, DEFAULT_CONTAINER_CONTEXT, "className");
        addMulePropertiesRule(path, digester);

        NodeCreateRule nodeCreateRule = null;
        try {
            nodeCreateRule = new NodeCreateRule(Node.DOCUMENT_FRAGMENT_NODE) {
                private String encoding;
                private String doctype;

                public void begin(String endpointName, String endpointName1, Attributes attributes) throws Exception {
                    encoding = attributes.getValue("encoding");
                    doctype = attributes.getValue("doctype");
                    super.begin(endpointName, endpointName1, attributes);
                }

                public void end(String endpointName, String endpointName1) throws Exception {
                    super.end(endpointName, endpointName1);

                    DocumentFragment config = (DocumentFragment) digester.pop();
                    StringWriter s = new StringWriter();
                    StreamResult streamResult = new StreamResult(s);
                    TransformerFactory tFactory = TransformerFactory.newInstance();
                    try {
                        Transformer transformer = tFactory.newTransformer();
                        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                        transformer.transform(new DOMSource(config), streamResult);
                    } catch (TransformerException e) {
                        throw new ContainerException(new Message(Messages.COULD_NOT_RECOVER_CONTIANER_CONFIG), e);
                    }
                    Reader reader = new StringReader(s.toString());
                    UMOContainerContext ctx = (UMOContainerContext) digester.peek();
                    ctx.configure(reader, doctype, encoding);
                }
            };
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException(e);
        }
        digester.addRule(path + "/configuration", nodeCreateRule);
        //Set the container on the parent object
        digester.addSetNext(path, setterMethod);
    }

    protected void addServerPropertiesRules(String path, String setterMethod, int parentIndex) {
        // Set environment properties
        int i = path.lastIndexOf("/");
        addMulePropertiesRule(path.substring(0, i), digester, setterMethod, path.substring(i + 1));
    }

    protected void addSetPropertiesRule(String path, Digester digester) {
        digester.addRule(path, new MuleSetPropertiesRule());
    }

    protected void addSetPropertiesRule(String path, Digester digester, String[] s1, String[] s2) {
        digester.addRule(path, new MuleSetPropertiesRule(s1, s2));
    }

    protected void addMulePropertiesRule(String path, Digester digester) {
        digester.addRuleSet(new MulePropertiesRuleSet(path, containerReferences));
    }

    protected void addMulePropertiesRule(String path, Digester digester, String propertiesSetter) {
        digester.addRuleSet(new MulePropertiesRuleSet(path, propertiesSetter, containerReferences));
    }

    protected void addMulePropertiesRule(String path, Digester digester, String propertiesSetter, String parentElement) {
        digester.addRuleSet(new MulePropertiesRuleSet(path, propertiesSetter, containerReferences, parentElement));
    }

    protected void addFilterRules(Digester digester, String path) throws ConfigurationException
    {
        // three levels
        addSingleFilterRule(digester, path);
        path += "/filter";
        addFilterGroupRule(digester, path);

        addFilterGroupRule(digester, path + "/left-filter");
        addFilterGroupRule(digester, path + "/right-filter");
        addFilterGroupRule(digester, path + "/filter");

        addFilterGroupRule(digester, path + "/left-filter/left-filter");
        addFilterGroupRule(digester, path + "/left-filter/right-filter");
        addFilterGroupRule(digester, path + "/left-filter/filter");

        addFilterGroupRule(digester, path + "/right-filter/left-filter");
        addFilterGroupRule(digester, path + "/right-filter/right-filter");
        addFilterGroupRule(digester, path + "/right-filter/filter");

        addFilterGroupRule(digester, path + "/filter/left-filter");
        addFilterGroupRule(digester, path + "/filter/right-filter");
        addFilterGroupRule(digester, path + "/filter/filter");

        // digester.addSetNext(path, "setFilter");
    }

    protected void addFilterGroupRule(Digester digester, String path) throws ConfigurationException
    {
        addLeftFilterRule(digester, path);
        addRightFilterRule(digester, path);
        addSingleFilterRule(digester, path);
    }

    protected void addLeftFilterRule(Digester digester, String path) throws ConfigurationException
    {
        path += "/left-filter";
        digester.addObjectCreate(path, FILTER_INTERFACE, "className");
        addSetPropertiesRule(path, digester);
        addMulePropertiesRule(path, digester);
        digester.addSetNext(path, "setLeftFilter");
    }

    protected void addRightFilterRule(Digester digester, String path) throws ConfigurationException
    {
        path += "/right-filter";
        digester.addObjectCreate(path, FILTER_INTERFACE, "className");
        addSetPropertiesRule(path, digester);
        addMulePropertiesRule(path, digester);
        digester.addSetNext(path, "setRightFilter");
    }

    protected void addSingleFilterRule(Digester digester, String path) throws ConfigurationException
    {
        path += "/filter";
        digester.addObjectCreate(path, FILTER_INTERFACE, "className");
        addSetPropertiesRule(path, digester);
        addMulePropertiesRule(path, digester);
        digester.addSetNext(path, "setFilter");
    }
}
