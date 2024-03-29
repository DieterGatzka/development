/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.xsd.v1;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.2.4-b01 Generated
 * source version: 2.2
 * 
 */
@WebService(name = "ClientSoapHandlerService", targetNamespace = "http://oscm.org/xsd")
@XmlSeeAlso({ ObjectFactory.class })
public interface ClientSoapHandlerService {

    /**
     * 
     */
    @WebMethod
    @RequestWrapper(localName = "initMessageList", targetNamespace = "http://oscm.org/xsd", className = "org.oscm.xsd.v1.InitMessageList")
    @ResponseWrapper(localName = "initMessageListResponse", targetNamespace = "http://oscm.org/xsd", className = "org.oscm.xsd.v1.InitMessageListResponse")
    @Action(input = "http://oscm.org/xsd/ClientSoapHandlerService/initMessageListRequest", output = "http://oscm.org/xsd/ClientSoapHandlerService/initMessageListResponse")
    public void initMessageList();

    /**
     * 
     * @return returns java.util.List<java.lang.String>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getCollectedMessages", targetNamespace = "http://oscm.org/xsd", className = "org.oscm.xsd.v1.GetCollectedMessages")
    @ResponseWrapper(localName = "getCollectedMessagesResponse", targetNamespace = "http://oscm.org/xsd", className = "org.oscm.xsd.v1.GetCollectedMessagesResponse")
    @Action(input = "http://oscm.org/xsd/ClientSoapHandlerService/getCollectedMessagesRequest", output = "http://oscm.org/xsd/ClientSoapHandlerService/getCollectedMessagesResponse")
    public List<String> getCollectedMessages();

}
