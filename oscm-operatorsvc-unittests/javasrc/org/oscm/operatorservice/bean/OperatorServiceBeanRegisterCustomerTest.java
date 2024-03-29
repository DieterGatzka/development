/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                                                                                 
 *  Creation Date: 2012-7-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Properties;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author Wenxin Gao
 * 
 */
public class OperatorServiceBeanRegisterCustomerTest {
    private OperatorServiceBean operatorServiceBean;
    private SessionContext sessionCtxMock;
    private AccountServiceLocal accountServiceMock;
    private DataService dm;
    private VOOrganization organization;
    private VOUserDetails userDetails;
    private final String marketplaceId = "marketplace_1";
    @Captor
    ArgumentCaptor<String> ac;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        operatorServiceBean = new OperatorServiceBean();
        sessionCtxMock = mock(SessionContext.class);
        operatorServiceBean.sessionCtx = sessionCtxMock;
        accountServiceMock = mock(AccountServiceLocal.class);
        operatorServiceBean.accMgmt = accountServiceMock;
        dm = mock(DataService.class);
        operatorServiceBean.dm = dm;
        LocalizerServiceLocal localizer = mock(LocalizerServiceLocal.class);
        operatorServiceBean.localizer = localizer;
        createOrganization();
        createUser();
    }

    /**
     * when create customer, the marketplaceID is mandatory
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void registerOrganization_CreateCustomerWithMarketplaceID_Null()
            throws Exception {
        try {
            operatorServiceBean.registerOrganization(organization, null,
                    userDetails, null, null);
        } finally {
            verify(sessionCtxMock, times(1)).setRollbackOnly();
        }
    }

    @Test(expected = ValidationException.class)
    public void registerOrganization_CreateCustomerWithBlankMarketplaceID()
            throws Exception {
        try {
            operatorServiceBean.registerOrganization(organization, null,
                    userDetails, null, "");
        } finally {
            verify(sessionCtxMock, times(1)).setRollbackOnly();
        }
    }

    /**
     * when create a customer with invalid marketplaceID,
     * ObjectNotFoundException should be thrown
     * 
     * @throws Exception
     */
    @Test(expected = ObjectNotFoundException.class)
    public void registerOrganization_CreateCustomerWithInvalidMarketplaceID()
            throws Exception {
        doThrow(new ObjectNotFoundException()).when(dm)
                .getReferenceByBusinessKey(any(Marketplace.class));
        try {
            operatorServiceBean.registerOrganization(organization, null,
                    userDetails, null, marketplaceId);
        } finally {
            verify(accountServiceMock, never()).registerOrganization(
                    any(Organization.class), any(ImageResource.class),
                    any(VOUserDetails.class), any(Properties.class),
                    anyString(), anyString(), anyString());
            verify(sessionCtxMock, times(1)).setRollbackOnly();
        }
    }

    /**
     * when create supplier, the marketplaceID should be ignored.
     * 
     * @throws Exception
     */
    @Test
    public void registerOrganization_CreateSupplierWithMarketplaceID()
            throws Exception {
        prepareForRegisterOrganization();
        organization.setOperatorRevenueShare(BigDecimal.valueOf(15));
        VOOrganization result = new VOOrganization();
        try {
            result = operatorServiceBean.registerOrganization(organization,
                    null, userDetails, null, marketplaceId,
                    OrganizationRoleType.SUPPLIER);
        } finally {
            // check the marketplaceID has been set null and the
            // OrganizationRoleType has been send correctly
            verify(accountServiceMock, times(1)).registerOrganization(
                    any(Organization.class), any(ImageResource.class),
                    any(VOUserDetails.class), any(Properties.class),
                    anyString(), (String) eq(null), anyString(),
                    eq(OrganizationRoleType.SUPPLIER));
            // the organization should be returned correctly
            assertEquals(organization.getEmail(), result.getEmail());
        }
    }

    @Test
    public void registerOrganization_CreateCustomerWithMarketplaceID()
            throws Exception {
        prepareForRegisterOrganization();
        VOOrganization result = new VOOrganization();
        try {
            result = operatorServiceBean.registerOrganization(organization,
                    null, userDetails, null, marketplaceId);
        } finally {
            verify(accountServiceMock, times(1)).registerOrganization(
                    any(Organization.class), any(ImageResource.class),
                    any(VOUserDetails.class), any(Properties.class),
                    anyString(), ac.capture(), anyString());
            assertEquals(marketplaceId, ac.getValue());
            assertEquals(organization.getEmail(), result.getEmail());
        }
    }

    private void createOrganization() {
        organization = new VOOrganization();
        organization.setOrganizationId("MyOrg");
        organization.setName("MyOrganization");
        organization.setEmail("test@fnst.fujitsu.com");
        organization.setPhone("+12345678");
        organization.setUrl("http://www.fujitsu.com");
        organization.setAddress("Test Address");
        organization.setLocale("en");
    }

    private void createUser() {
        userDetails = new VOUserDetails();
        userDetails.setFirstName("FirstName");
        userDetails.setLastName("LastName");
        userDetails.setEMail("test@fnst.fujitsu.com");
        userDetails.setUserId("admin");
        userDetails.setSalutation(Salutation.MR);
        userDetails.setPhone("12345678");
        userDetails.setLocale("en");
    }

    private void prepareForRegisterOrganization() throws Exception {
        Organization createdOrganization = new Organization();
        createdOrganization.setEmail(organization.getEmail());
        // mock registerOrganization method for creating supplier
        doReturn(createdOrganization).when(accountServiceMock)
                .registerOrganization(any(Organization.class),
                        any(ImageResource.class), any(VOUserDetails.class),
                        any(Properties.class), anyString(), anyString(),
                        anyString(), any(OrganizationRoleType.class));
        // mock registerOrganization method for creating customer
        doReturn(createdOrganization).when(accountServiceMock)
                .registerOrganization(any(Organization.class),
                        any(ImageResource.class), any(VOUserDetails.class),
                        any(Properties.class), anyString(), anyString(),
                        anyString());
        PlatformUser user = new PlatformUser();
        user.setLocale("en");
        doReturn(user).when(dm).getCurrentUser();
    }
}
