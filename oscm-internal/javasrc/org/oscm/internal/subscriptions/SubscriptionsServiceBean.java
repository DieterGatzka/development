/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                                                                                 
 *  Creation Date: Nov 12, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.subscriptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.subscriptionservice.assembler.SubscriptionAssembler;
import org.oscm.subscriptionservice.local.SubscriptionListServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.internal.assembler.POSubscriptionAndCustomerAssembler;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.tables.Pagination;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUserSubscription;

/**
 * @author tokoda
 *
 */
@Stateless
@Remote(SubscriptionsService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class SubscriptionsServiceBean implements SubscriptionsService {

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @EJB(beanInterface = SubscriptionListServiceLocal.class)
    SubscriptionListServiceLocal slService;

    @EJB(beanInterface = SubscriptionService.class)
    SubscriptionService subscriptionService;

    @EJB(beanInterface = SubscriptionServiceLocal.class)
    SubscriptionServiceLocal subscriptionServiceLocal;

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER" })
    public Response getSubscriptionsForOrg(Set<SubscriptionStatus> states) {
        List<Subscription> subscriptions = slService.getSubscriptionsForOrganization(states);
        List<POSubscriptionForList> result = localize(subscriptions);

        return new Response(result);

    }

    LocalizerFacade getLocalizerFacade() {
        return new LocalizerFacade(localizer, dm.getCurrentUser().getLocale());
    }

    POSubscriptionForList toPOSubscriptionForList(Subscription subscription,
            LocalizerFacade facade) {
        POSubscriptionForList po = new POSubscriptionForList();
        po.setSubscriptionId(subscription.getSubscriptionId());
        po.setSubscriptionKey(subscription.getKey());
        po.setProvisioningProgress(facade.getText(subscription.getKey(),
                LocalizedObjectTypes.SUBSCRIPTION_PROVISIONING_PROGRESS));
        po.setNumberOfAssignedUsers(subscription.getUsageLicenses().size());
        po.setPurchaseOrderNumber(subscription.getPurchaseOrderNumber());

        if(subscription.getActivationDate()!=null){
        	Date activationDate = new Date(subscription.getActivationDate().longValue());
        	po.setActivationDate(activationDate);
        }

        if(subscription.getUserGroup() != null) {
            po.setUnit(subscription.getUserGroup().getName());
        }

        SubscriptionStatus status = subscription.getStatus();
        po.setStatusTextKey(SubscriptionStatus.class.getSimpleName() + "."
                + status.name());
        po.setStatusText(status.name().toLowerCase());

        po.setStatusActive(status == SubscriptionStatus.ACTIVE);
        po.setStatusPending(status == SubscriptionStatus.PENDING);
        po.setStatusPendingUpd(status == SubscriptionStatus.PENDING_UPD);
        po.setProvisioningProgressRendered(po.isStatusPending()
                && po.getProvisioningProgress() != null);

        Product product = subscription.getProduct();
        po.setServiceKey(product.getKey());
        po.setServiceName(facade.getText(product.getProductTemplate().getKey(),
        		LocalizedObjectTypes.PRODUCT_MARKETING_NAME));

        TechnicalProduct techProduct = product.getTechnicalProduct();
        po.setServiceAccessInfo(facade.getText(techProduct.getKey(),
                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));
        po.setAccessViaAccessInfo(techProduct.getAccessType() == ServiceAccessType.DIRECT
                || techProduct.getAccessType() == ServiceAccessType.USER);

        if (product.getTemplate().getVendor() != null) {
            Organization vender = product.getTemplate().getVendor();
            po.setSupplierName(vender.getName());
            po.setSupplierOrganizationId(vender.getOrganizationId());
        }

        return po;
    }

    @Override
    public Response getMySubscriptions() {
        List<VOUserSubscription> mySubscriptions = subscriptionService
                .getSubscriptionsForCurrentUser();

        List<POSubscription> result = new ArrayList<>();
        for (VOUserSubscription voSubscription : mySubscriptions) {
            result.add(toPOSubscription(voSubscription));
        }

        return new Response(result);
    }

    private POSubscription toPOSubscription(VOSubscription subscription) {

    	LocalizerFacade facade = getLocalizerFacade();

        POSubscription result = new POSubscription(subscription);
        result.setServiceName(facade.getText(subscription.getServiceKey(),
        		LocalizedObjectTypes.PRODUCT_MARKETING_NAME));

        result.setStatus(subscription.getStatus());
        result.setStatusText(subscription.getStatus().name().toLowerCase());
        result.setStatusTextKey(SubscriptionStatus.class.getSimpleName() + "."
                + subscription.getStatus().name());
        result.setNumberOfAssignedUsers(subscription.getNumberOfAssignedUsers());
        result.setSupplierName(subscription.getSellerName());
        return result;
    }

    @Override
    public Response getSubscriptionsAndCustomersForManagers()
            throws OrganizationAuthoritiesException {
        List<POSubscriptionAndCustomer> poSubscriptionAndCustomers = new ArrayList<>();
        List<Subscription> subscriptions = subscriptionServiceLocal
                .getSubscriptionsForManagers();
        for (Subscription subscription : subscriptions) {
            poSubscriptionAndCustomers.add(POSubscriptionAndCustomerAssembler
                    .toPOSubscriptionAndCustomer(subscription));
        }
        return new Response(poSubscriptionAndCustomers);
    }

    @Override
    public Response getSubscriptionsAndCustomersForManagers(
            Pagination pagination) throws OrganizationAuthoritiesException {
        List<POSubscriptionAndCustomer> poSubscriptionAndCustomers = new ArrayList<>();
        List<Subscription> subscriptions = subscriptionServiceLocal
                .getSubscriptionsForManagers(pagination);
        for (Subscription subscription : subscriptions) {
            poSubscriptionAndCustomers.add(POSubscriptionAndCustomerAssembler
                    .toPOSubscriptionAndCustomer(subscription, getLocalizerFacade()));
        }
        return new Response(poSubscriptionAndCustomers);
    }

    @Override
    @SuppressWarnings("boxing")
    public Integer getSubscriptionsAndCustomersForManagersSize(Pagination pagination) throws OrganizationAuthoritiesException {
    	return subscriptionServiceLocal
                .getSubscriptionsForManagers(pagination).size();
    }

    @Override
    public Response getSubscriptionsForOrg(Set<SubscriptionStatus> states, Pagination pagination)
            throws OrganizationAuthoritiesException {

        List<Subscription> subscriptions = slService.getSubscriptionsForOrganization(states, pagination);
        List<POSubscriptionForList> result = localize(subscriptions);

        return new Response(result);
    }

    /**
     * @param subscriptions
     * @return
     */
    private List<POSubscriptionForList> localize(List<Subscription> subscriptions) {
        LocalizerFacade facade = getLocalizerFacade();
        List<POSubscriptionForList> result = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
            result.add(toPOSubscriptionForList(subscription, facade));
        }
        return result;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public Integer getSubscriptionsForOrgSize(Set<SubscriptionStatus> states,
            Pagination pagination) throws OrganizationAuthoritiesException {
        return Integer.valueOf(slService.getSubscriptionsForOrganization(
                states, pagination).size());
    }


    @Override
    public Response getMySubscriptions(Pagination pagination) throws OrganizationAuthoritiesException {
        List<Subscription> mySubscriptions = subscriptionServiceLocal.getSubscriptionsForCurrentUser(pagination);
        List<POSubscription> result = toPOUserSubscriptionList(mySubscriptions);
        return new Response(result);
    }

    private List<POSubscription> toPOUserSubscriptionList(List<Subscription> subs) {
        PlatformUser user = dm.getCurrentUser();
        LocalizerFacade facade = new LocalizerFacade(localizer, user.getLocale());
        ArrayList<POSubscription> result = new ArrayList<>();
        SubscriptionAssembler.prefetchData(subs, facade);
        for (Subscription sub : subs) {
            VOUserSubscription voSub = SubscriptionAssembler.toVOUserSubscription(sub, user, facade);
            result.add(toPOSubscription(voSub));
        }
        return result;
    }

    @Override
    @SuppressWarnings("boxing")
    public Integer getMySubscriptionsSize(Pagination pagination) throws OrganizationAuthoritiesException {
        return subscriptionServiceLocal.getSubscriptionsForCurrentUser(pagination).size();
    }

	@Override
	public boolean isSubscriptionVisible(long subscriptionKey) {

		try {
			Subscription subscription = subscriptionServiceLocal
					.loadSubscription(subscriptionKey);
			return Subscription.VISIBLE_SUBSCRIPTION_STATUS
					.contains(subscription.getStatus());
		} catch (ObjectNotFoundException e) {
			return false;
		}
	}

    @Override
    public VOSubscriptionDetails getSubscriptionDetails(long subscriptionKey) throws ObjectNotFoundException {
        Subscription subscription = subscriptionServiceLocal
                .loadSubscription(subscriptionKey);
        LocalizerFacade facade = new LocalizerFacade(localizer, dm
                .getCurrentUser().getLocale());

        return SubscriptionAssembler.toVOSubscriptionDetails(subscription,
                facade);
    }

	@Override
	public boolean isCurrentUserAssignedToSubscription(long subscriptionKey) {

		UsageLicense usageLicense = subscriptionServiceLocal
				.getSubscriptionUsageLicense(dm.getCurrentUser(), Long.valueOf(subscriptionKey));
		return usageLicense != null;
	}
}
