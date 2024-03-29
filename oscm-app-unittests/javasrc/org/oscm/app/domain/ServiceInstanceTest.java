/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.v1_0.data.InstanceStatus;

/**
 * Unit tests for {@link ServiceInstance}.
 * 
 * @author hoffmann
 */
public class ServiceInstanceTest {

    private ServiceInstance instance;
    private final EntityManager em = Mockito.mock(EntityManager.class);

    @Before
    public void setup() throws Exception {
        instance = new ServiceInstance();
        instance.setServiceBaseURL("baseURL");
        instance.setBesLoginURL("besLoginURL");
        instance.setDefaultLocale("de");
        instance.setOrganizationId("orgId");
        instance.setSubscriptionId("subId");
        instance.setInstanceId("appInstanceId");
        instance.setControllerId("ess.vmware");
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
        Mockito.doNothing().when(em).persist(Matchers.any());
    }

    @Test
    public void testGetParameterForKey() {
        final InstanceParameter p = new InstanceParameter();
        p.setParameterKey("param1");
        p.setParameterValue("value1");
        instance.setInstanceParameters(Arrays.asList(p));
        assertEquals("value1", instance.getParameterForKey("param1")
                .getParameterValue());
    }

    @Test
    public void getIdentifier_nullId() {
        // given
        ServiceInstance si = new ServiceInstance();
        si.setTkey(1L);
        si.setInstanceId(null);

        // then
        assertEquals("1", si.getIdentifier());
    }

    @Test
    public void getIdentifier_emptyId() {
        // given
        ServiceInstance si = new ServiceInstance();
        si.setTkey(1L);
        si.setInstanceId("");

        // then
        assertEquals("1", si.getIdentifier());
    }

    @Test
    public void getIdentifier_Id() {
        // given
        ServiceInstance si = new ServiceInstance();
        si.setTkey(1L);
        si.setInstanceId("instanceId");

        // then
        assertEquals("instanceId", si.getIdentifier());
    }

    @Test
    public void testNullKeyEncryption() throws Exception {
        final InstanceParameter p = new InstanceParameter();
        p.setParameterKey(null);
        p.setDecryptedValue("value1");
        assertEquals(p.getParameterValue(), p.getDecryptedValue());
    }

    @Test
    public void testGetParameterForKeyNegative() {
        final InstanceParameter p = new InstanceParameter();
        p.setParameterKey("param1");
        p.setParameterValue("value1");
        instance.setInstanceParameters(Arrays.asList(p));
        assertNull(instance.getParameterForKey("param2"));
    }

    @Test
    public void testGetParameterMap() throws Exception {
        final InstanceParameter p1 = new InstanceParameter();
        p1.setParameterKey("param1");
        p1.setParameterValue("value1");
        final InstanceParameter p2 = new InstanceParameter();
        p2.setParameterKey("param2");
        p2.setParameterValue("value2");
        instance.setInstanceParameters(Arrays.asList(p1, p2));

        final Map<String, String> expected = new HashMap<String, String>();
        expected.put("param1", "value1");
        expected.put("param2", "value2");
        assertEquals(expected, instance.getParameterMap());
    }

    @Test
    public void markForDeletion() {
        // given
        ServiceInstance si = new ServiceInstance();
        si.setSubscriptionId("subscriptionId");
        si.setLocked(true);

        // when
        si.markForDeletion();

        // then
        assertFalse(si.isLocked());
        assertTrue(si.getSubscriptionId().contains("#"));
    }

    @Test
    public void prepareForDeletion_twice() {
        // given
        ServiceInstance si = new ServiceInstance();
        si.setSubscriptionId("subscriptionId");
        si.setLocked(true);

        // when
        si.markForDeletion();
        String subId = si.getSubscriptionId();
        si.markForDeletion();

        // then
        assertFalse(si.isLocked());
        assertTrue(si.getSubscriptionId().contains("#"));
        assertEquals(subId, si.getSubscriptionId());
    }

    @Test
    public void getOriginalSubscriptionId_original() {

        // given
        ServiceInstance si = new ServiceInstance();

        // when
        si.setSubscriptionId("subscriptionId");

        // then
        assertEquals("subscriptionId", si.getOriginalSubscriptionId());

    }

    @Test
    public void getOriginalSubscriptionId_afterMarkForDeletion() {
        // given
        ServiceInstance si = new ServiceInstance();

        // when
        si.setSubscriptionId("subscriptionId#1234jsdhfsdjklnfsdj");

        // then
        assertEquals("subscriptionId", si.getOriginalSubscriptionId());

    }

    @Test
    public void getOriginalSubscriptionId_aftermultiMarkForDeletion() {

        // given
        ServiceInstance si = new ServiceInstance();

        // when
        si.setSubscriptionId("s#9821#dsad#dsadas");

        // then
        assertEquals("s", si.getOriginalSubscriptionId());
    }

    @Test
    public void isDeleted_positive() {
        // given
        ServiceInstance si = new ServiceInstance();
        si.setSubscriptionId("subscriptionId");
        si.setLocked(true);

        // when
        si.markForDeletion();

        // then
        assertTrue(si.isDeleted());
    }

    @Test
    public void isDeleted_negative() {
        // given
        ServiceInstance si = new ServiceInstance();
        si.setSubscriptionId("subscriptionId");

        // then
        assertFalse(si.isDeleted());
    }

    @Test
    public void updateStatus_deletedInstance() {
        // given
        ServiceInstance si = new ServiceInstance();
        si.setSubscriptionId("subscriptionId");
        si.setRunWithTimer(false);
        si.markForDeletion();

        // when
        si.updateStatus(em, new InstanceStatus());

        // then
        Mockito.verify(em, Mockito.times(0)).persist(si);
    }

    @Test
    public void updateStatus_serviceAccessInfoIsNull() {
        // given
        instance.setServiceAccessInfo(null);
        instance.setSubscriptionId("subscriptionId");
        InstanceStatus status = new InstanceStatus();
        status.setAccessInfo("accessInfo");
        ServiceInstance si = new ServiceInstance();
        si.setSubscriptionId("subscriptionId");
        si.setRunWithTimer(false);
        si.setServiceAccessInfo(status.getAccessInfo());

        // when
        si.updateStatus(em, status);

        // then
        Mockito.verify(em, Mockito.times(1)).persist(si);
    }

    @Test
    public void updateStatus_statusAccessInfoIsNull() {
        // given
        instance.setServiceAccessInfo("accessInfo");
        instance.setSubscriptionId("subscriptionId");
        InstanceStatus status = new InstanceStatus();
        status.setAccessInfo(null);
        ServiceInstance si = new ServiceInstance();
        si.setSubscriptionId("subscriptionId");
        si.setRunWithTimer(false);
        si.setServiceAccessInfo(instance.getServiceAccessInfo());

        // when
        si.updateStatus(em, status);

        // then
        Mockito.verify(em, Mockito.times(1)).persist(si);
    }

    @Test
    public void updateStatus() {
        // given
        instance.setServiceAccessInfo("accessInfo1");
        instance.setSubscriptionId("subscriptionId");
        InstanceStatus status = new InstanceStatus();
        status.setAccessInfo("accessInfo2");
        ServiceInstance si = new ServiceInstance();
        si.setSubscriptionId("subscriptionId");
        si.setRunWithTimer(false);
        si.setServiceAccessInfo(status.getAccessInfo());

        // when
        si.updateStatus(em, status);

        // then
        Mockito.verify(em, Mockito.times(1)).persist(si);
    }

    @Test
    public void prepareRollback() throws Exception {
        // given
        ServiceInstance si = Mockito.spy(new ServiceInstance());
        si.setSubscriptionId("subscriptionId");
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><properties><entry key=\"KEY2\">VALUE2</entry><entry key=\"ROLLBACK_SUBSCRIPTIONID\">subscriptionId</entry><entry key=\"KEY1\">VALUE1</entry></properties>";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("KEY1", "VALUE1");
        params.put("KEY2", "VALUE2");

        Mockito.doReturn(params).when(si).getParameterMap();

        // when
        si.prepareRollback();

        // then
        assertEquals(expectedXML, removeFormatting(si.getRollbackParameters()));
    }

    @Test
    public void convertPropertiesToXML() throws Exception {
        // given
        ServiceInstance si = Mockito.spy(new ServiceInstance());
        Properties props = new Properties();
        props.put("KEY1", "VALUE1");
        props.put("KEY2", "VALUE2");
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><properties><entry key=\"KEY2\">VALUE2</entry><entry key=\"KEY1\">VALUE1</entry></properties>";

        // when
        String resultXML = si.convertPropertiesToXML(props);

        // then
        assertEquals(expectedXML, removeFormatting(resultXML));
    }

    @Test
    public void convertXMLToProperties() throws Exception {
        // given
        ServiceInstance si = Mockito.spy(new ServiceInstance());
        Properties props = new Properties();
        props.put("KEY1", "VALUE1");
        props.put("KEY2", "VALUE2");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\r\n<properties>\r\n<entry key=\"KEY2\">VALUE2</entry>\r\n<entry key=\"KEY1\">VALUE1</entry>\r\n</properties>\r\n";

        // when
        Properties resultProps = si.convertXMLToProperties(xml);

        // then
        assertEquals(props, resultProps);
    }

    @Test
    public void rollbackInstanceParameters() throws Exception {
        // given
        ServiceInstance si = Mockito.spy(new ServiceInstance());
        HashMap<String, String> expectedParams = new HashMap<String, String>();
        expectedParams.put("KEY1", "VALUE1");
        expectedParams.put("KEY2", "VALUE2");

        String rollbackXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\r\n<properties>\r\n<entry key=\"KEY2\">VALUE2</entry>\r\n<entry key=\"ROLLBACK_SUBSCRIPTIONID\">subscriptionId</entry>\r\n<entry key=\"KEY1\">VALUE1</entry>\r\n</properties>\r\n";

        Mockito.doReturn(rollbackXML).when(si).getRollbackParameters();

        // when
        si.rollbackServiceInstance(null);

        // then
        Mockito.verify(si, Mockito.times(1)).setInstanceParameters(
                expectedParams);

    }

    @Test(expected = BadResultException.class)
    public void rollbackInstanceParameters_BadResult() throws Exception {
        // given
        ServiceInstance si = spy(new ServiceInstance());

        Mockito.doReturn(null).when(si).getRollbackParameters();

        // when
        try {
            si.rollbackServiceInstance(null);
        } catch (BadResultException be) {
            String message = org.oscm.app.i18n.Messages.get("en",
                    "error_missing_rollbackparameters", si.getInstanceId());
            assertEquals(message, be.getLocalizedMessage());

            throw be;
        }

    }

    @Test(expected = BadResultException.class)
    public void rollbackInstanceParameters_MissingSubscriptionID()
            throws Exception {
        // given
        ServiceInstance si = Mockito.spy(new ServiceInstance());
        HashMap<String, String> expectedParams = new HashMap<String, String>();
        expectedParams.put("KEY1", "VALUE1");
        expectedParams.put("KEY2", "VALUE2");

        String rollbackXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\r\n<properties>\r\n<entry key=\"KEY2\">VALUE2</entry>\r\n<entry key=\"KEY1\">VALUE1</entry>\r\n</properties>\r\n";

        Mockito.doReturn(rollbackXML).when(si).getRollbackParameters();

        // when
        try {
            si.rollbackServiceInstance(null);
        } catch (BadResultException be) {
            String message = org.oscm.app.i18n.Messages.get("en",
                    "error_missing_subscriptionId", si.getInstanceId());
            assertEquals(message, be.getLocalizedMessage());

            throw be;
        }
    }

    @Test
    public void setInstanceParameters() throws Exception {
        // given
        HashMap<String, String> newParameters = new HashMap<>();
        newParameters.put("KEY1", "NEWVALUE1");
        newParameters.put("KEY2", "NEWVALUE2");
        InstanceParameter ip = new InstanceParameter();
        ip.setParameterKey("KEY1");
        ip.setParameterValue("OLDVALUE1");
        ServiceInstance si = spy(new ServiceInstance());
        doReturn(ip).when(si).getParameterForKey("KEY1");

        // when
        si.setInstanceParameters(newParameters);

        // then
        assertEquals(newParameters, si.getParameterMap());

    }

    @Test
    public void removeParams() {
        // given
        ServiceInstance si = new ServiceInstance();
        ArrayList<InstanceParameter> currentIpList = new ArrayList<InstanceParameter>();
        InstanceParameter ip1 = new InstanceParameter();
        ip1.setParameterKey("KEY1");
        ip1.setParameterValue("VALUE1");
        currentIpList.add(ip1);
        InstanceParameter ip2 = new InstanceParameter();
        ip2.setParameterKey("KEY2");
        ip2.setParameterValue("VALUE2");
        currentIpList.add(ip2);
        si.setInstanceParameters(currentIpList);

        HashMap<String, String> newParameters = new HashMap<>();
        newParameters.put("KEY1", "NEWVALUE1");

        // when
        si.removeParams(newParameters, em);

        // then
        assertEquals(1, si.getInstanceParameters().size());
    }

    @Test
    public void removeParams_NothingToRemove() {
        // given
        ServiceInstance si = new ServiceInstance();
        ArrayList<InstanceParameter> currentIpList = new ArrayList<InstanceParameter>();
        InstanceParameter ip1 = new InstanceParameter();
        ip1.setParameterKey("KEY1");
        ip1.setParameterValue("VALUE1");
        currentIpList.add(ip1);
        InstanceParameter ip2 = new InstanceParameter();
        ip2.setParameterKey("KEY2");
        ip2.setParameterValue("VALUE2");
        currentIpList.add(ip2);
        si.setInstanceParameters(currentIpList);

        // when
        si.removeParams(null, em);

        // then
        assertEquals(2, si.getInstanceParameters().size());
    }

    private String removeFormatting(String xml) {
        String result = xml;
        result = result.replace("\n", "");
        result = result.replace("\r", "");

        return result;
    }
}
