/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                              
 *  Author: tokoda                                                     
 *                                                                              
 *  Creation Date: Sep 9, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 9, 2011                                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.webservices;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.intf.EventService;

/**
 * @author tokoda
 * 
 */
public class EventServiceWSTest {

    private EventServiceWS serviceWS;
    private EventService serviceMock;
    private HttpServletRequest requestMock;

    @Before
    public void setup() {
        serviceMock = mock(EventService.class);
        serviceWS = new EventServiceWS();
        serviceWS.wsContext = createWebServiceContextMock("127.0.0.1", "99999");
        serviceWS.delegate = serviceMock;
        PlatformUser user = mock(PlatformUser.class);
        DataService ds = mock(DataService.class);
        Mockito.when(ds.getCurrentUser()).thenReturn(user);
        serviceWS.ds = ds;
    }

    private WebServiceContext createWebServiceContextMock(String expectedIP,
            String expectedUser) {
        requestMock = mock(HttpServletRequest.class);
        when(requestMock.getRemoteAddr()).thenReturn(expectedIP);

        Principal principalMock = mock(Principal.class);
        when(principalMock.getName()).thenReturn(expectedUser);

        MessageContext msgContextMock = mock(MessageContext.class);
        when(msgContextMock.get(anyString())).thenReturn(requestMock);

        WebServiceContext wsContextMock = mock(WebServiceContext.class);
        when(wsContextMock.getUserPrincipal()).thenReturn(principalMock);
        when(wsContextMock.getMessageContext()).thenReturn(msgContextMock);

        return wsContextMock;
    }

    @Test
    public void testLogInjectionForEventServiceWS() throws Exception {
        serviceWS.recordEventForInstance(null, null, null);
        serviceWS.recordEventForSubscription(0, null);

        verify(requestMock, times(2)).getRemoteAddr();

        verify(serviceMock, times(1)).recordEventForInstance(null, null, null);
        verify(serviceMock, times(1)).recordEventForSubscription(0, null);

    }

}
