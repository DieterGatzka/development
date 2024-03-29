/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                        
 *                                                                              
 *  Creation Date: 14.05.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.aws.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;

import javax.naming.InitialContext;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.app.aws.EC2Communication;
import org.oscm.app.aws.EC2Mockup;
import org.oscm.app.aws.data.FlowState;
import org.oscm.app.aws.data.Operation;
import org.oscm.app.v1_0.data.InstanceStatus;
import org.oscm.app.v1_0.data.PasswordAuthentication;
import org.oscm.app.v1_0.data.ProvisioningSettings;
import org.oscm.app.v1_0.data.User;
import org.oscm.app.v1_0.intf.APPlatformService;

public class EC2ProcessorTest extends EJBTestBase {

    private EC2Processor ec2proc;
    private HashMap<String, String> parameters;
    private HashMap<String, String> configSettings;
    private ProvisioningSettings settings;
    private PropertyHandler ph;
    private AmazonEC2Client ec2;
    private EC2Mockup ec2mock;
    private APPlatformService platformService;
    @Captor
    private ArgumentCaptor<String> subject;
    @Captor
    private ArgumentCaptor<String> text;
    
    private final String KEY_PAIR_NAME=" Key pair name: ";

    @Override
    protected void setup(TestContainer container) throws Exception {
        MockitoAnnotations.initMocks(this);
        // Define controller settings
        configSettings = new HashMap<String, String>();
        configSettings.put(PropertyHandler.SECRET_KEY_PWD, "secret_key");
        configSettings.put(PropertyHandler.ACCESS_KEY_ID_PWD, "access_key");

        // Define parameters
        parameters = new HashMap<String, String>();
        parameters.put(PropertyHandler.REGION, "test");
        parameters.put(PropertyHandler.KEY_PAIR_NAME, "key_pair");
        parameters.put(PropertyHandler.INSTANCE_TYPE, "type1");
        parameters.put(PropertyHandler.INSTANCENAME, "name1");

        settings = new ProvisioningSettings(parameters, configSettings, "en");
        settings.setOrganizationId("orgId");
        settings.setSubscriptionId("subId");

        ph = new PropertyHandler(settings) {
            @Override
            public <T> T getWebService(java.lang.Class<T> serviceClass)
                    throws Exception {
                return mock(serviceClass);
            };
        };
        PropertyHandler.useMock(null);

        ec2mock = new EC2Mockup();
        ec2 = ec2mock.getEC2();
        EC2Communication.useMock(ec2);

        ec2mock.createDescribeImagesResult("image1");
        ec2mock.createRunInstancesResult("instance1");

        platformService = mock(APPlatformService.class);
        enableJndiMock();
        InitialContext context = new InitialContext();
        context.bind(APPlatformService.JNDI_NAME, platformService);

        doNothing().when(platformService).sendMail(anyListOf(String.class),
                subject.capture(), text.capture());
        User user = new User();
        user.setLocale("de");
        doReturn(user).when(platformService).authenticate(anyString(),
                any(PasswordAuthentication.class));
        ec2proc = new EC2Processor(ph, "instance1");

        doReturn("eventUrl").when(platformService).getEventServiceUrl();
    }

    @Test
    public void process_CREATION_REQUESTED() throws Exception {
        ph.setOperation(Operation.EC2_CREATION);
        ph.setState(FlowState.CREATION_REQUESTED);
        InstanceStatus result = ec2proc.process();
        assertFalse(result.isReady());
        assertEquals(FlowState.CREATING, ph.getState());

        verify(ec2).runInstances(any(RunInstancesRequest.class));
    }

    @Test
    public void process_CREATING() throws Exception {
        ec2mock.createDescribeInstancesResult("instance1", "running", "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");

        ph.setOperation(Operation.EC2_CREATION);
        ph.setState(FlowState.CREATING);
        InstanceStatus result = ec2proc.process();
        assertTrue(result.isReady());
        assertEquals(FlowState.FINISHED, ph.getState());
        assertTrue(result.getAccessInfo().contains(KEY_PAIR_NAME));
        assertTrue(result.getAccessInfo().contains(ph.getKeyPairName()));
    }

    @Test
    public void process_CREATING_inprogress1() throws Exception {
        ec2mock.createDescribeInstancesResult("instance1", "in_progress",
                "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");

        ph.setOperation(Operation.EC2_CREATION);
        ph.setState(FlowState.CREATING);
        InstanceStatus result = ec2proc.process();
        assertFalse(result.isReady());
        assertEquals(FlowState.CREATING, ph.getState());
    }

    @Test
    public void process_CREATING_inprogress2() throws Exception {
        ec2mock.createDescribeInstancesResult("instance1", "running", "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "notok",
                "notok", "notok");

        ph.setOperation(Operation.EC2_CREATION);
        ph.setState(FlowState.CREATING);
        InstanceStatus result = ec2proc.process();
        assertFalse(result.isReady());
        assertEquals(FlowState.CREATING, ph.getState());
    }

    @Test
    public void process_CREATING_MANUAL() throws Exception {
        // given
        ec2mock.createDescribeInstancesResult("instance1", "running", "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");
        ph.setOperation(Operation.EC2_CREATION);
        ph.setState(FlowState.CREATING);
        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, "mail1");
        // when
        InstanceStatus result = ec2proc.process();
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result.isReady()));
        assertEquals(FlowState.MANUAL, ph.getState());
        assertEquals(Boolean.FALSE, Boolean.valueOf(result.getRunWithTimer()));
    }

    @Test
    public void process_FINISHED() throws Exception {
        // given
        ec2mock.createDescribeInstancesResult("instance1", "running", "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");
        ph.setOperation(Operation.EC2_CREATION);
        ph.setState(FlowState.FINISHED);
        // when
        InstanceStatus result = ec2proc.process();
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.isReady()));
        assertEquals(FlowState.FINISHED, ph.getState());
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.getRunWithTimer()));
        assertNotNull(result.getAccessInfo());
        assertTrue(result.getAccessInfo().contains(KEY_PAIR_NAME));
        assertTrue(result.getAccessInfo().contains(ph.getKeyPairName()));
    }

    @Test
    public void process_MODIFICATION_REQUESTED() throws Exception {
        ph.setOperation(Operation.EC2_MODIFICATION);
        ph.setState(FlowState.MODIFICATION_REQUESTED);
        InstanceStatus result = ec2proc.process();
        assertFalse(result.isReady());
        assertEquals(FlowState.UPDATING, ph.getState());
    }

    @Test
    public void process_UPDATING() throws Exception {
        ec2mock.createDescribeInstancesResult("instance1", "in_progress",
                "1.2.3.4");
        ph.setOperation(Operation.EC2_MODIFICATION);
        ph.setState(FlowState.UPDATING);
        InstanceStatus result = ec2proc.process();
        assertTrue(result.isReady());
        assertEquals(FlowState.FINISHED, ph.getState());
        assertTrue(result.getAccessInfo().contains(KEY_PAIR_NAME));
        assertTrue(result.getAccessInfo().contains(ph.getKeyPairName()));
    }

    @Test
    public void process_UPDATING_MANUAL() throws Exception {
        // given
        ec2mock.createDescribeInstancesResult("instance1", "running", "1.2.3.4");
        ph.setOperation(Operation.EC2_MODIFICATION);
        ph.setState(FlowState.UPDATING);
        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, "mail1");
        // when
        InstanceStatus result = ec2proc.process();
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.isReady()));
        assertEquals(FlowState.FINISHED, ph.getState());
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.getRunWithTimer()));
    }

    @Test
    public void process_DELETION_REQUESTED() throws Exception {
        ph.setOperation(Operation.EC2_DELETION);
        ph.setState(FlowState.DELETION_REQUESTED);
        InstanceStatus result = ec2proc.process();
        assertFalse(result.isReady());
        assertEquals(FlowState.DELETING, ph.getState());

        verify(ec2).terminateInstances(any(TerminateInstancesRequest.class));
    }

    @Test
    public void process_DELETING_inprogress() throws Exception {
        ec2mock.createDescribeInstancesResult("instance1", "in_progress",
                "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");

        ph.setOperation(Operation.EC2_DELETION);
        ph.setState(FlowState.DELETING);
        InstanceStatus result = ec2proc.process();
        assertFalse(result.isReady());
        assertEquals(FlowState.DELETING, ph.getState());
    }

    @Test
    public void process_DELETING() throws Exception {
        ec2mock.createDescribeInstancesResult("instance1", "terminated",
                "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");

        ph.setOperation(Operation.EC2_DELETION);
        ph.setState(FlowState.DELETING);
        InstanceStatus result = ec2proc.process();
        assertTrue(result.isReady());
        assertEquals(FlowState.DESTROYED, ph.getState());
    }

    @Test
    public void process_DELETING_MANUAL() throws Exception {
        // given
        ec2mock.createDescribeInstancesResult("instance1", "terminated",
                "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");
        ph.setOperation(Operation.EC2_DELETION);
        ph.setState(FlowState.DELETING);
        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, "mail1");
        // when
        InstanceStatus result = ec2proc.process();
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.isReady()));
        assertEquals(FlowState.DESTROYED, ph.getState());
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.getRunWithTimer()));
    }

    @Test
    public void process_ACTIVATION_REQUESTED() throws Exception {
        // given
        ph.setOperation(Operation.EC2_ACTIVATION);
        ph.setState(FlowState.ACTIVATION_REQUESTED);

        // when
        InstanceStatus result = ec2proc.process();

        // then
        assertFalse(result.isReady());
        assertEquals(FlowState.STARTING, ph.getState());
        verify(ec2).startInstances(any(StartInstancesRequest.class));
    }

    @Test
    public void process_DEACTIVATION_REQUESTED() throws Exception {
        // given
        ph.setOperation(Operation.EC2_ACTIVATION);
        ph.setState(FlowState.DEACTIVATION_REQUESTED);

        // when
        InstanceStatus result = ec2proc.process();

        // then
        assertFalse(result.isReady());
        assertEquals(FlowState.STOPPING, ph.getState());
        verify(ec2).stopInstances(any(StopInstancesRequest.class));
    }

    @Test
    public void process_START_REQUESTED() throws Exception {
        // given
        ph.setOperation(Operation.EC2_OPERATION);
        ph.setState(FlowState.START_REQUESTED);

        // when
        InstanceStatus result = ec2proc.process();

        // then
        assertFalse(result.isReady());
        assertEquals(FlowState.STARTING, ph.getState());
        verify(ec2).startInstances(any(StartInstancesRequest.class));
    }

    @Test
    public void process_STARTING() throws Exception {
        // given
        ec2mock.createDescribeInstancesResult("instance1", "running", "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");
        ph.setOperation(Operation.EC2_OPERATION);
        ph.setState(FlowState.STARTING);

        // when
        InstanceStatus result = ec2proc.process();

        // then
        assertTrue(result.isReady());
        assertEquals(FlowState.FINISHED, ph.getState());
        assertTrue(result.getAccessInfo().contains(KEY_PAIR_NAME));
        assertTrue(result.getAccessInfo().contains(ph.getKeyPairName()));
    }

    @Test
    public void process_STOP_REQUESTED() throws Exception {
        // given
        ph.setOperation(Operation.EC2_OPERATION);
        ph.setState(FlowState.STOP_REQUESTED);

        // when
        InstanceStatus result = ec2proc.process();

        // then
        assertFalse(result.isReady());
        assertEquals(FlowState.STOPPING, ph.getState());
        verify(ec2).stopInstances(any(StopInstancesRequest.class));
    }

    @Test
    public void process_STOPING() throws Exception {
        // given
        ec2mock.createDescribeInstancesResult("instance1", "stopped", "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");
        ph.setOperation(Operation.EC2_OPERATION);
        ph.setState(FlowState.STOPPING);

        // when
        InstanceStatus result = ec2proc.process();

        // then
        assertTrue(result.isReady());
        assertEquals(FlowState.FINISHED, ph.getState());

        verify(ec2).describeInstances(any(DescribeInstancesRequest.class));
    }

    @Test
    public void process_UNKNOWN_OPERATION() throws Exception {
        ph.setOperation(Operation.UNKNOWN);
        ph.setState(FlowState.CREATING);
        InstanceStatus result = ec2proc.process();
        assertFalse(result.isReady());
        assertEquals(FlowState.CREATING, ph.getState());
    }

    @Test
    public void process_UNKNOWN_FLOWSTATE1() throws Exception {
        ph.setOperation(Operation.EC2_CREATION);
        ph.setState(FlowState.STOP_REQUESTED);
        InstanceStatus result = ec2proc.process();
        assertFalse(result.isReady());
        assertEquals(FlowState.STOP_REQUESTED, ph.getState());
    }

    @Test
    public void process_UNKNOWN_FLOWSTATE2() throws Exception {
        ph.setOperation(Operation.EC2_ACTIVATION);
        ph.setState(FlowState.CREATING);
        InstanceStatus result = ec2proc.process();
        assertFalse(result.isReady());
        assertEquals(FlowState.CREATING, ph.getState());
    }

    @Test
    public void process_UNKNOWN_FLOWSTATE3() throws Exception {
        ph.setOperation(Operation.EC2_OPERATION);
        ph.setState(FlowState.CREATING);
        InstanceStatus result = ec2proc.process();
        assertFalse(result.isReady());
        assertEquals(FlowState.CREATING, ph.getState());
    }

    @Test
    public void dispatchManualOperation_CREATION() throws Exception {
        // given
        ph.setOperation(Operation.EC2_CREATION);
        FlowState newState = null;

        // when
        newState = ec2proc.dispatchManualOperation("controllerId",
                "instanceId", ph, "mail");

        // then
        assertEquals(FlowState.MANUAL, newState);
        assertTrue(subject.getValue().contains("subId"));
        assertTrue(text.getValue().contains("subId"));

    }

    @Test
    public void dispatchManualOperation_MODIFICATION() throws Exception {
        // given
        ph.setOperation(Operation.EC2_MODIFICATION);
        FlowState newState = null;

        // when
        newState = ec2proc.dispatchManualOperation("controllerId",
                "instanceId", ph, "mail");

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void dispatchManualOperation_DELETION() throws Exception {
        // given
        ph.setOperation(Operation.EC2_DELETION);
        FlowState newState = null;

        // when
        newState = ec2proc.dispatchManualOperation("controllerId",
                "instanceId", ph, "mail");

        // then
        assertEquals(FlowState.DESTROYED, newState);
    }
}
