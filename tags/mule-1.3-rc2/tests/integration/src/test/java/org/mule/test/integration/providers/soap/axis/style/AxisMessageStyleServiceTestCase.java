/*
=* AxisMessageStyleServiceTestCase.java
=*
=* Created on October 21, 2005, 1:11 PM
=*
=* To change this template, choose Tools | Options and locate the =emplate under
=* the Source Creation and Management node. Right-click the template and =hoose
=* Open. You can then make changes to the template in the Source Editor.
=*/
package org.mule.test.integration.providers.soap.axis.style;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.lang.StringUtils;
import org.mule.extras.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.umo.UMOMessage;
import org.mule.providers.soap.SoapMethod;
import org.mule.providers.soap.NamedParameter;
import org.mule.providers.soap.axis.AxisConnector;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Rutter
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
*/

public class AxisMessageStyleServiceTestCase extends FunctionalTestCase {
    private static String expectedResult = "TEST RESPONSE";

    public AxisMessageStyleServiceTestCase() {
        setDisposeManagerPerSuite(true);
    }

    public String getConfigResources() {
        return "org/mule/test/integration/providers/soap/axis/style/axis-mule-message-config.xml";
    }

    protected static String getServiceEndpoint() {
        return "http://localhost:8088/ServiceEntryPoint";
    }

    public void testDocumentWithNamesapce() throws Exception {
        doSoapRequest(new QName("http://muleumo.org", "document"), "axis:" + getServiceEndpoint(), false, false, false);
    }

    public void testDocumentWithQName() throws Exception {
        doSoapRequest(new QName("http://muleumo.org", "document"), "axis:" + getServiceEndpoint(), false, false, true);
    }

    public void testDocumentWithAxisApi() throws Exception {
        doSoapRequest(new QName("http://muleumo.org", "document"), getServiceEndpoint(), true, false, false);
    }

    public void testDocumentWithSoapMethod() throws Exception {
        doSoapRequest(new QName("http://muleumo.org", "document"), "axis:" + getServiceEndpoint(), false, true, false);
    }

    public void testElementArrayWithSoapMethod() throws Exception {
        doSoapRequest(new QName("http://muleumo.org", "elementArray"), "axis:" + getServiceEndpoint(), false, true, false);
    }

    public void testElementArrayWithNamesapce() throws Exception {
        doSoapRequest(new QName("http://muleumo.org", "elementArray"), "axis:" + getServiceEndpoint(), false, false, false);
    }

    public void testElementArrayWithQName() throws Exception {
        doSoapRequest(new QName("http://muleumo.org", "elementArray"), "axis:" + getServiceEndpoint(), false, false, true);
    }

    public void testElementArrayWithAxisApi() throws Exception {
        doSoapRequest(new QName("http://muleumo.org", "elementArray"), getServiceEndpoint(), true, false, false);
    }


    public void testSoapBodyElementWithSoapMethod() throws Exception {
        doSoapRequest(new QName("http://muleumo.org", "soapBodyElement"), "axis:" + getServiceEndpoint(), false, true, false);
    }

    public void testSoapBodyElementWithNamesapce() throws Exception {
        doSoapRequest(new QName("http://muleumo.org", "soapBodyElement"), "axis:" + getServiceEndpoint(), false, false, false);
    }

    public void testSoapBodyElementWithQName() throws Exception {
        doSoapRequest(new QName("http://muleumo.org", "soapBodyElement"), "axis:" + getServiceEndpoint(), false, false, true);
    }

    public void testSoapBodyElementWithAxisApi() throws Exception {
        doSoapRequest(new QName("http://muleumo.org", "soapBodyElement"), getServiceEndpoint(), true, false, false);
    }

    //Todo does work , complains about generated namespace...TestNS1
//    public void testSoapRequestResponseWithSoapMethod() throws Exception {
//        doSoapRequest(new QName("http://muleumo.org", "soapRequestResponse"), "axis:" + getServiceEndpoint(), false, true, false);
//    }
//
//    public void testSoapRequestResponseWithNamesapce() throws Exception {
//        doSoapRequest(new QName("http://muleumo.org", "soapRequestResponse"), "axis:" + getServiceEndpoint(), false, false, false);
//    }
//
//    public void testSoapRequestResponseWithQName() throws Exception {
//        doSoapRequest(new QName("http://muleumo.org", "soapRequestResponse"), "axis:" + getServiceEndpoint(), false, false, true);
//    }

    public void testSoapRequestResponseWithAxisApi() throws Exception {
        doSoapRequest(new QName("http://muleumo.org", "soapRequestResponse"), getServiceEndpoint(), true, false, false);
    }

    protected void doSoapRequest(QName method, String endpoint, boolean useAxisApi, boolean useSoapMethod, boolean useQNameMethod) throws Exception {

        if(useAxisApi) {
            Service service = new Service();
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));
            call.setOperationName(method);
            String ret = (String) call.invoke(new Object[]{expectedResult});
            assertNotNull(ret);
            assertEquals(ret, expectedResult);
        } else {

            //Now try with the MuleClient
            MuleClient client = new MuleClient();
            Map props = new HashMap();
            if(useSoapMethod) {
                SoapMethod soapMethod = new SoapMethod(method);
                soapMethod.addNamedParameter(new QName(method.getNamespaceURI(), method.getLocalPart()), NamedParameter.XSD_STRING, ParameterMode.IN);
                props.put("method", soapMethod);
            }else if(useQNameMethod) {
                props.put("method", method);
            } else {
                endpoint += "?method=" + method.getLocalPart();
                if(StringUtils.isNotBlank(method.getNamespaceURI())) {
                    props.put("methodNamespace", method.getNamespaceURI());
                }
            }

            UMOMessage result = client.send(endpoint, expectedResult, props);
            assertNotNull(result);
            assertEquals(expectedResult, result.getPayloadAsString());
        }
    }
}

