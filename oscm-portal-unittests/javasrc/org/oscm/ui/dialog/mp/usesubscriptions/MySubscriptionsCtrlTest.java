/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                                                                                 
 *  Creation Date: 12.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.usesubscriptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.model.SelectItem;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.subscriptions.OperationModel;
import org.oscm.internal.subscriptions.OperationParameterModel;
import org.oscm.internal.subscriptions.POSubscription;
import org.oscm.internal.subscriptions.SubscriptionsService;
import org.oscm.internal.types.enumtypes.OperationParameterType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOServiceOperationParameterValues;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOTechnicalServiceOperation;

public class MySubscriptionsCtrlTest {

    private static final String BASE_URL_HTTP = "http://localhost:8080/oscm-portal";
    private static final String BASE_URL_HTTPS = "https://localhost:8080/oscm-portal";

    MySubscriptionsCtrl ctrl;
    MySubscriptionsLazyDataModel model;
    ApplicationBean appBean = mock(ApplicationBean.class);

    private SubscriptionService subSvc;

    UiDelegate ui;

    @Before
    public void setup() {
        ctrl = new MySubscriptionsCtrl();
        ctrl.setApplicationBean(appBean);

        ExternalContext extContext = mock(ExternalContext.class);
        when(extContext.getRequestContextPath()).thenReturn(
                "/oscm-portal");

        ui = mock(UiDelegate.class);
        when(ui.getExternalContext()).thenReturn(extContext);

        ctrl.ui = ui;
        ctrl.selectId = "componentid";
        model = spy(new MySubscriptionsLazyDataModel());
        ctrl.setModel(model);

        ctrl.subscriptionsService = mock(SubscriptionsService.class);
        when(ctrl.applicationBean.getServerBaseUrlHttps()).thenReturn(
                BASE_URL_HTTPS);
        when(ctrl.applicationBean.getServerBaseUrl()).thenReturn(BASE_URL_HTTP);

        subSvc = mock(SubscriptionService.class);
        ctrl.setSubscriptionService(subSvc);
    }

    @Test
    public void executeOperation_NoSubscription() throws Exception {
        assertEquals(MySubscriptionsCtrl.OUTCOME_ERROR, ctrl.executeOperation());
    }

    @Test
    public void executeOperation_NoOperation() throws Exception {
        initSubscription(model);

        assertEquals(MySubscriptionsCtrl.OUTCOME_ERROR, ctrl.executeOperation());
    }

    @Test
    public void executeOperation() throws Exception {
        POSubscription sub = initSubscription(model);
        OperationModel om = initOperation(sub);

        assertEquals(MySubscriptionsCtrl.OUTCOME_SUCCESS,
                ctrl.executeOperation());

        verify(subSvc).executeServiceOperation(same(sub.getVOSubscription()),
                same(om.getOperation()));
        verify(ctrl.ui).handle(eq(MySubscriptionsCtrl.INFO_OPERATION_EXECUTED),
                eq(om.getOperation().getOperationName()));
    }

    @Test
    public void executeOperation_ConcurrentlyChanged() throws Exception {
        POSubscription sub = initSubscription(model);
        OperationModel om = initOperation(sub);
        doThrow(new ConcurrentModificationException()).when(subSvc)
                .executeServiceOperation(any(VOSubscription.class),
                        any(VOTechnicalServiceOperation.class));

        String result = ctrl.executeOperation();

        assertEquals(MySubscriptionsCtrl.OUTCOME_ERROR, result);
        verify(subSvc).executeServiceOperation(same(sub.getVOSubscription()),
                same(om.getOperation()));
        verify(ctrl.ui).handleError(anyString(),
                eq(MySubscriptionsCtrl.ERROR_SUBSCRIPTION_CONCURRENTMODIFY));
    }

    @Test
    public void operationChanged_Empty() throws Exception {
        POSubscription sub = initSubscription(model);
        initOperation(sub);
        sub.setSelectedOperationId("");

        assertEquals(MySubscriptionsCtrl.OUTCOME_SUCCESS,
                ctrl.operationChanged());

        assertTrue(sub.isExecuteDisabled());
        assertNull(sub.getSelectedOperation());
        assertNull(sub.getSelectedOperationId());
        verify(subSvc, never()).getServiceOperationParameterValues(
                any(VOSubscription.class),
                any(VOTechnicalServiceOperation.class));
    }

    public void operationChanged_NoParameter() throws Exception {
        POSubscription sub = initSubscription(model);
        OperationModel om = initOperation(sub);

        assertEquals(MySubscriptionsCtrl.OUTCOME_SUCCESS,
                ctrl.operationChanged());

        assertFalse(sub.isExecuteDisabled());
        assertSame(om.getOperation(), sub.getSelectedOperation().getOperation());
        verify(subSvc, never()).getServiceOperationParameterValues(
                any(VOSubscription.class),
                any(VOTechnicalServiceOperation.class));
    }

    @Test
    public void operationChanged_ParameterNoRequest() throws Exception {
        POSubscription sub = initSubscription(model);
        OperationModel om = initOperation(sub);
        initOperationParameter(om, OperationParameterType.INPUT_STRING);

        assertEquals(MySubscriptionsCtrl.OUTCOME_SUCCESS,
                ctrl.operationChanged());

        assertFalse(sub.isExecuteDisabled());
        assertSame(om.getOperation(), sub.getSelectedOperation().getOperation());
        verify(subSvc, never()).getServiceOperationParameterValues(
                any(VOSubscription.class),
                any(VOTechnicalServiceOperation.class));
    }

    @Test
    public void operationChanged_ParameterRequest() throws Exception {
        POSubscription sub = initSubscription(model);
        OperationModel om = initOperation(sub);
        initOperationParameter(om, OperationParameterType.REQUEST_SELECT);
        List<String> list = initValues(subSvc, sub, om);

        assertEquals(MySubscriptionsCtrl.OUTCOME_SUCCESS,
                ctrl.operationChanged());

        assertFalse(sub.isExecuteDisabled());
        assertSame(om.getOperation(), sub.getSelectedOperation().getOperation());
        verify(subSvc).getServiceOperationParameterValues(
                eq(sub.getVOSubscription()), eq(om.getOperation()));
        List<SelectItem> values = model.getMySubscriptions().get(0)
                .getSelectedOperation().getParameters().get(0).getValues();
        assertEquals(list.size(), values.size());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(list.get(i), values.get(i).getValue());
            assertEquals(list.get(i), values.get(i).getLabel());
        }
    }

    @Test
    public void bug11696_checkSubscriptionsNotEmpty_success() throws Exception {

        // given
        POSubscription mockedSub = mock(POSubscription.class);
        VOSubscriptionDetails mockSubDetail = mock(VOSubscriptionDetails.class);
        SubscriptionsService mockedServ = ctrl.subscriptionsService;
        model.setSelectedSubscription(mockedSub);
        when(mockedSub.getStatus()).thenReturn(SubscriptionStatus.ACTIVE);
        when(mockSubDetail.getStatus()).thenReturn(SubscriptionStatus.ACTIVE);
        when(mockedSub.getKey()).thenReturn(1L);
        when(mockedServ.getSubscriptionDetails(1L)).thenReturn(mockSubDetail);
        when(mockedServ.isSubscriptionVisible(1L)).thenReturn(true);
        when(mockedServ.isCurrentUserAssignedToSubscription(1L)).thenReturn(
                true);
        // when
        ctrl.checkSubscriptionsNotEmpty();
        // then
        verify(mockedSub).getStatus();
        verify(mockSubDetail).getStatus();
        verify(mockedSub, atLeastOnce()).getKey();
        verify(mockedServ).getSubscriptionDetails(1L);
        verify(mockedServ).isSubscriptionVisible(1L);
        verify(mockedServ).isCurrentUserAssignedToSubscription(1L);
        assertNotNull(model.getSelectedSubscription());
    }

    @Test
    public void bug11696_checkSubscriptionsNotEmpty_fail() throws Exception {

        // given
        POSubscription mockedSub = mock(POSubscription.class);
        VOSubscriptionDetails mockSubDetail = mock(VOSubscriptionDetails.class);
        SubscriptionsService mockedServ = ctrl.subscriptionsService;

        model.setSelectedSubscription(mockedSub);
        when(mockedSub.getStatus()).thenReturn(SubscriptionStatus.ACTIVE);
        when(mockSubDetail.getStatus()).thenReturn(SubscriptionStatus.ACTIVE);
        when(mockedSub.getKey()).thenReturn(1L);
        when(mockedServ.getSubscriptionDetails(1L)).thenReturn(mockSubDetail);
        when(mockedServ.isSubscriptionVisible(1L)).thenReturn(true);
        when(mockedServ.isCurrentUserAssignedToSubscription(1L)).thenReturn(
                false);
        // when
        ctrl.checkSubscriptionsNotEmpty();
        // then
        verify(mockedSub, atLeastOnce()).getKey();
        verify(mockedServ).isSubscriptionVisible(1L);
        verify(mockedServ).isCurrentUserAssignedToSubscription(1L);
        assertNull(model.getSelectedSubscription());
    }

    private static final List<String> initValues(SubscriptionService subSvc,
            POSubscription sub, OperationModel om) throws Exception {

        VOServiceOperationParameterValues value = new VOServiceOperationParameterValues();
        value.setParameterId(om.getOperation().getOperationParameters().get(0)
                .getParameterId());
        List<String> list = Arrays.asList("1", "2", "3");
        value.setValues(list);
        when(
                subSvc.getServiceOperationParameterValues(
                        eq(sub.getVOSubscription()), eq(om.getOperation())))
                .thenReturn(Arrays.asList(value));
        return list;
    }

    private static final OperationParameterModel initOperationParameter(
            OperationModel om, OperationParameterType type) {
        VOServiceOperationParameter sop = new VOServiceOperationParameter();
        sop.setMandatory(true);
        sop.setParameterId("TEST");
        sop.setParameterName("Test");
        sop.setType(type);
        OperationParameterModel opm = new OperationParameterModel();
        opm.setParameter(sop);
        om.setParameters(Arrays.asList(opm));
        om.getOperation().setOperationParameters(Arrays.asList(sop));
        return opm;
    }

    private static final OperationModel initOperation(POSubscription sub) {
        VOTechnicalServiceOperation tso = new VOTechnicalServiceOperation();
        tso.setOperationId("operationId");
        tso.setOperationName("operationName");
        OperationModel om = new OperationModel();
        om.setOperation(tso);
        sub.setTechnicalServiceOperations(Arrays.asList(tso));
        sub.setSelectedOperationId(tso.getOperationId());
        return om;
    }

    private static final POSubscription initSubscription(
            MySubscriptionsLazyDataModel model) {
        VOSubscription vo = new VOSubscription();
        vo.setSubscriptionId("subscriptionId");
        vo.setStatus(SubscriptionStatus.ACTIVE);
        POSubscription subscription = new POSubscription(vo);
        when(model.getMySubscriptions())
                .thenReturn(Arrays.asList(subscription));
        when(model.getMySubscriptions())
                .thenReturn(Arrays.asList(subscription));
        model.setSubscriptionIdForOperation(vo.getSubscriptionId());
        model.setSelectedSubscription(subscription);
        return subscription;
    }
}
