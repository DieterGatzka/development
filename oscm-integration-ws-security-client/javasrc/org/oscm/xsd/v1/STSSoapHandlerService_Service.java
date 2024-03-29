/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.xsd.v1;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.2.4-b01 Generated
 * source version: 2.2
 * 
 */
@WebServiceClient(name = "STSSoapHandlerService", targetNamespace = "http://oscm.org/xsd", wsdlLocation = "http://localhost:8680/oscm-integrationtests-saml2-sts/STSSoapHandlerService?wsdl")
public class STSSoapHandlerService_Service extends Service {

    private final static URL STSSOAPHANDLERSERVICE_WSDL_LOCATION;
    private final static WebServiceException STSSOAPHANDLERSERVICE_EXCEPTION;
    private final static QName STSSOAPHANDLERSERVICE_QNAME = new QName(
            "http://oscm.org/xsd", "STSSoapHandlerService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL(
                    "http://localhost:8680/oscm-integrationtests-saml2-sts/STSSoapHandlerService?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        STSSOAPHANDLERSERVICE_WSDL_LOCATION = url;
        STSSOAPHANDLERSERVICE_EXCEPTION = e;
    }

    public STSSoapHandlerService_Service() {
        super(__getWsdlLocation(), STSSOAPHANDLERSERVICE_QNAME);
    }

    public STSSoapHandlerService_Service(WebServiceFeature... features) {
        super(__getWsdlLocation(), STSSOAPHANDLERSERVICE_QNAME, features);
    }

    public STSSoapHandlerService_Service(URL wsdlLocation) {
        super(wsdlLocation, STSSOAPHANDLERSERVICE_QNAME);
    }

    public STSSoapHandlerService_Service(URL wsdlLocation,
            WebServiceFeature... features) {
        super(wsdlLocation, STSSOAPHANDLERSERVICE_QNAME, features);
    }

    public STSSoapHandlerService_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public STSSoapHandlerService_Service(URL wsdlLocation, QName serviceName,
            WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return returns STSSoapHandlerService
     */
    @WebEndpoint(name = "STSSoapHandlerServiceImplPort")
    public STSSoapHandlerService getSTSSoapHandlerServiceImplPort() {
        return super.getPort(new QName("http://oscm.org/xsd",
                "STSSoapHandlerServiceImplPort"), STSSoapHandlerService.class);
    }

    /**
     * 
     * @param features
     *            A list of {@link javax.xml.ws.WebServiceFeature} to configure
     *            on the proxy. Supported features not in the
     *            <code>features</code> parameter will have their default
     *            values.
     * @return returns STSSoapHandlerService
     */
    @WebEndpoint(name = "STSSoapHandlerServiceImplPort")
    public STSSoapHandlerService getSTSSoapHandlerServiceImplPort(
            WebServiceFeature... features) {
        return super.getPort(new QName("http://oscm.org/xsd",
                "STSSoapHandlerServiceImplPort"), STSSoapHandlerService.class,
                features);
    }

    private static URL __getWsdlLocation() {
        if (STSSOAPHANDLERSERVICE_EXCEPTION != null) {
            throw STSSOAPHANDLERSERVICE_EXCEPTION;
        }
        return STSSOAPHANDLERSERVICE_WSDL_LOCATION;
    }

}
