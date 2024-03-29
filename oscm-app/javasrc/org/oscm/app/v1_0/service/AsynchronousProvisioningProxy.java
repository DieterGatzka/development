/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.app.v1_0.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;

import org.oscm.string.Strings;
import org.oscm.app.business.APPlatformControllerFactory;
import org.oscm.app.business.AsynchronousProvisioningProxyImpl;
import org.oscm.app.business.InstanceParameterFilter;
import org.oscm.app.business.ProductProvisioningServiceFactoryBean;
import org.oscm.app.business.ProvisioningResults;
import org.oscm.app.business.UserMapper;
import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.business.exceptions.ServiceInstanceInProcessingException;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.i18n.Messages;
import org.oscm.app.v1_0.data.InstanceDescription;
import org.oscm.app.v1_0.data.InstanceStatus;
import org.oscm.app.v1_0.data.InstanceStatusUsers;
import org.oscm.app.v1_0.data.LocalizedText;
import org.oscm.app.v1_0.data.ProvisioningSettings;
import org.oscm.app.v1_0.data.ServiceUser;
import org.oscm.app.v1_0.exceptions.APPlatformException;
import org.oscm.app.v1_0.intf.APPlatformController;
import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceRequest;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.ServiceParameter;
import org.oscm.provisioning.data.User;
import org.oscm.provisioning.data.UserResult;
import org.oscm.provisioning.intf.ProvisioningService;

/**
 * Implements the latest OSCM provisioning service interface.
 * <p>
 * Please note that almost all signatures of the provisioning service methods
 * contain a user object which represents the calling user. Not all public
 * members of this object are set when the provisionig call is done. Currently,
 * following members are provided by OSCM:<br />
 * <code>email, locale, first name, last name, user id</code>
 * 
 * @author kulle
 * 
 */
@Stateless
@WebService(serviceName = "ProvisioningService", targetNamespace = "http://oscm.org/xsd", portName = "ProvisioningServicePort", endpointInterface = "org.oscm.provisioning.intf.ProvisioningService")
public class AsynchronousProvisioningProxy implements ProvisioningService {

    @Inject
    protected transient Logger logger;

    @Inject
    protected ProvisioningResults provResult;

    @Inject
    protected AsynchronousProvisioningProxyImpl appImpl;

    @PersistenceContext(name = "persistence/em", unitName = "oscm-app")
    protected EntityManager em;

    @EJB
    protected ProductProvisioningServiceFactoryBean provisioningFactory;

    @EJB
    protected APPConfigurationServiceBean configService;

    @EJB
    protected APPTimerServiceBean timerService;

    @EJB
    protected ServiceInstanceDAO instanceDAO;

    @Override
    public BaseResult asyncCreateInstance(InstanceRequest request,
            User requestingUser) {
        logger.info("Create instance for organization {} for subscription {}.",
                request.getOrganizationId(), request.getSubscriptionId());
        ServiceInstance instance = null;
        try {
            final InstanceDescription descr = getInstanceDescription(request,
                    requestingUser);
            instance = createPersistentServiceInstance(request, descr);
            timerService.initTimers();

            List<LocalizedText> descriptions = descr.getDescription();

            String successMsg = null;
            if (descriptions != null) {
                for (LocalizedText description : descriptions) {
                    if (description.getLocale().equals(
                            getLocale(requestingUser))) {
                        successMsg = description.getText();
                    }
                }
            }

            return provResult.getSuccesfulResult(BaseResult.class, successMsg);

        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return provResult.getErrorResult(BaseResult.class, e,
                    getLocale(requestingUser), instance, null);
        }
    }

    ServiceInstance createPersistentServiceInstance(InstanceRequest request,
            InstanceDescription descr) throws BadResultException {
        final HashMap<String, String> parameters = createParameterMap(request
                .getParameterValue());
        String controllerId = parameters.get(InstanceParameter.CONTROLLER_ID);
        ServiceInstance si = new ServiceInstance();
        si.setOrganizationId(request.getOrganizationId());
        si.setOrganizationName(request.getOrganizationName());
        si.setSubscriptionId(request.getSubscriptionId());
        si.setDefaultLocale(request.getDefaultLocale());
        si.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
        si.setControllerId(controllerId);
        si.setBesLoginURL(request.getLoginUrl());
        si.setRequestTime(System.currentTimeMillis());
        si.setInstanceParameters(createParameters(si,
                descr.getChangedParameters()));
        si.setServiceBaseURL(descr.getBaseUrl());
        si.setInstanceId(descr.getInstanceId());
        em.persist(si);
        return si;
    }

    InstanceDescription getInstanceDescription(InstanceRequest request,
            User requestingUser) throws APPlatformException, BadResultException {
        final HashMap<String, String> parameters = createParameterMap(request
                .getParameterValue());
        String controllerId = parameters.get(InstanceParameter.CONTROLLER_ID);
        if (controllerId == null) {
            logger.warn("The technical service does not define a controller implementation");
            throw new BadResultException(Messages.get(
                    request.getDefaultLocale(), "error_configuration"));
        }

        HashMap<String, String> controllerSettings = configService
                .getControllerConfigurationSettings(controllerId);
        final ProvisioningSettings settings = new ProvisioningSettings(
                parameters, controllerSettings, request.getDefaultLocale());
        settings.setOrganizationId(request.getOrganizationId());
        settings.setOrganizationName(request.getOrganizationName());
        settings.setSubscriptionId(request.getSubscriptionId());
        settings.setBesLoginUrl(request.getLoginUrl());

        ServiceInstance si = new ServiceInstance();
        si.setInstanceParameters(createParameters(si, parameters));
        si.setControllerId(controllerId);

        settings.setAuthentication(configService
                .getAuthenticationForBESTechnologyManager(controllerId, si,
                        null));
        configService.copyCredentialsFromControllerSettings(settings,
                controllerSettings);
        settings.setRequestingUser(UserMapper.toServiceUser(requestingUser));

        final APPlatformController controller = APPlatformControllerFactory
                .getInstance(controllerId);

        final InstanceDescription descr = controller.createInstance(settings);

        // Check whether instanceId is filled and unique
        if (Strings.isEmpty(descr.getInstanceId())) {
            logger.error("Instance ID not specified by controller.");
            throw new BadResultException(Messages.get(
                    request.getDefaultLocale(), "error_instanceid_empty"));
        }
        if (instanceDAO.exists(descr.getInstanceId())) {
            logger.error("Instance ID " + descr.getInstanceId()
                    + " already used by another instance.");
            throw new BadResultException(Messages.get(
                    request.getDefaultLocale(), "error_instanceid_exists",
                    descr.getInstanceId()));
        }
        return descr;
    }

    private List<InstanceParameter> createParameters(final ServiceInstance si,
            final Map<String, String> src) throws BadResultException {
        final ArrayList<InstanceParameter> dest = new ArrayList<InstanceParameter>();
        if (src != null) {
            for (final String key : src.keySet()) {
                if (key != null) {
                    final InstanceParameter d = new InstanceParameter();
                    d.setServiceInstance(si);
                    d.setParameterKey(key);
                    d.setDecryptedValue(src.get(key));
                    dest.add(d);
                }
            }
        }
        return dest;
    }

    @Override
    public UserResult createUsers(String instanceId, List<User> users,
            User requestingUser) {
        logger.info("Create users for service instance with ID {}.", instanceId);

        ServiceInstance instance = null;

        try {

            instance = instanceDAO.getInstanceById(instanceId);

            checkInstanceAvailability(instance);

            final APPlatformController controller = APPlatformControllerFactory
                    .getInstance(instance.getControllerId());
            final ProvisioningSettings settings = configService
                    .getProvisioningSettings(instance,
                            UserMapper.toServiceUser(requestingUser));

            // Forward request
            final InstanceStatusUsers status = controller.createUsers(
                    instance.getInstanceId(), settings,
                    mapToServiceUsers(users));
            if (status != null) {
                // get modified users
                if (status.getChangedUsers() != null) {
                    // merge returned users with existing users
                    mergeServiceUsers(users, status.getChangedUsers());
                }

                // forward call to provisioning service on application instance
                if (status.isInstanceProvisioningRequested()) {
                    final ProvisioningService provisioning = provisioningFactory
                            .getInstance(instance);
                    final UserResult result = provisioning.createUsers(
                            instanceId, users, requestingUser);
                    if (provResult.isError(result)) {
                        return result;
                    }

                    // Get modified users
                    if (result.getUsers() != null) {
                        users = result.getUsers();
                    }
                }

                // If everything worked well we will save all changed parameters
                instance.setInstanceParameters(status.getChangedParameters());
            }

            instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_USER_CREATION);
            em.persist(instance);

            timerService.initTimers();

            final UserResult result = provResult.getOKResult(UserResult.class);
            result.setUsers(users);
            return result;

        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return provResult.getErrorResult(UserResult.class, e,
                    getLocale(requestingUser), instance, instanceId);
        }
    }

    @Override
    public BaseResult updateUsers(String instanceId, List<User> users,
            User requestingUser) {
        logger.info("Update users for service instance {}.", instanceId);

        ServiceInstance instance = null;

        try {
            instance = instanceDAO.getInstanceById(instanceId);

            checkInstanceAvailability(instance);

            final APPlatformController controller = APPlatformControllerFactory
                    .getInstance(instance.getControllerId());
            final ProvisioningSettings settings = configService
                    .getProvisioningSettings(instance,
                            UserMapper.toServiceUser(requestingUser));

            // Forward request
            final InstanceStatus status = controller.updateUsers(
                    instance.getInstanceId(), settings,
                    mapToServiceUsers(users));
            if (status != null) {
                // forward call to provisioning service on application instance
                if (status.isInstanceProvisioningRequested()) {
                    final ProvisioningService provisioning = provisioningFactory
                            .getInstance(instance);
                    final BaseResult result = provisioning.updateUsers(
                            instanceId, users, requestingUser);
                    if (provResult.isError(result)) {
                        return result;
                    }
                }

                // If everything worked well we will save all changed parameters
                instance.setInstanceParameters(status.getChangedParameters());
            }

            instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_USER_MODIFICATION);
            em.persist(instance);

            timerService.initTimers();

            return provResult.newOkBaseResult();

        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return provResult.getErrorResult(BaseResult.class, e,
                    getLocale(requestingUser), instance, instanceId);
        }
    }

    @Override
    public BaseResult deleteUsers(String instanceId, List<User> users,
            User requestingUser) {
        logger.info("Delete users for instance {}.", instanceId);
        ServiceInstance instance = null;

        try {
            instance = instanceDAO.getInstanceById(instanceId);
            checkInstanceAvailability(instance);
            final APPlatformController controller = APPlatformControllerFactory
                    .getInstance(instance.getControllerId());
            final ProvisioningSettings settings = configService
                    .getProvisioningSettings(instance,
                            UserMapper.toServiceUser(requestingUser));

            // Forward activation request
            final InstanceStatus status = controller.deleteUsers(
                    instance.getInstanceId(), settings,
                    mapToServiceUsers(users));
            if (status != null) {
                // forward call to provisioning service on application instance
                if (status.isInstanceProvisioningRequested()) {
                    final ProvisioningService provisioning = provisioningFactory
                            .getInstance(instance);
                    final BaseResult result = provisioning.deleteUsers(
                            instanceId, users, requestingUser);
                    if (provResult.isError(result)) {
                        return result;
                    }
                }

                // If everything worked well we will save all changed parameters
                instance.setInstanceParameters(status.getChangedParameters());
            }

            instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_USER_DELETION);
            em.persist(instance);

            timerService.initTimers();

            return provResult.newOkBaseResult();

        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return provResult.getErrorResult(BaseResult.class, e,
                    getLocale(requestingUser), instance, instanceId);
        }
    }

    @Override
    public String sendPing(String arg) {
        return arg;
    }

    @Override
    public BaseResult asyncModifySubscription(String instanceId,
            String subscriptionId, List<ServiceParameter> parameterValues,
            User requestingUser) {
        final HashMap<String, String> parameterMap = createParameterMap(parameterValues);
        logger.info("Modify parameters for instance {}: {}", instanceId,
                parameterMap);
        return modifySubscription(instanceId, subscriptionId, parameterValues,
                parameterMap,
                ProvisioningStatus.WAITING_FOR_SYSTEM_MODIFICATION,
                requestingUser);
    }

    private BaseResult modifySubscription(String instanceId,
            String subscriptionId, List<ServiceParameter> parameterValues,
            final HashMap<String, String> parameterMap,
            ProvisioningStatus targetStatus, User requestingUser) {

        ServiceInstance instance = null;

        try {
            instance = instanceDAO.getInstanceById(instanceId);

            instance.prepareRollback();

            checkInstanceAvailability(instance);

            final HashMap<String, String> controllerSettings = configService
                    .getControllerConfigurationSettings(instance
                            .getControllerId());
            final APPlatformController controller = APPlatformControllerFactory
                    .getInstance(instance.getControllerId());

            final ProvisioningSettings currentSettings = configService
                    .getProvisioningSettings(instance,
                            UserMapper.toServiceUser(requestingUser));
            final ProvisioningSettings newSettings = new ProvisioningSettings(
                    parameterMap, controllerSettings,
                    instance.getDefaultLocale());
            newSettings.setAuthentication(currentSettings.getAuthentication());
            configService.copyCredentialsFromControllerSettings(newSettings,
                    controllerSettings);
            newSettings.setRequestingUser(UserMapper
                    .toServiceUser(requestingUser));
            newSettings.setSubscriptionId(subscriptionId);

            // Forward modification request
            final InstanceStatus status = controller.modifyInstance(
                    instance.getInstanceId(), currentSettings, newSettings);
            if (status != null) {
                // forward call to provisioning service on application instance
                if (status.isInstanceProvisioningRequested()) {
                    final ProvisioningService provisioning = provisioningFactory
                            .getInstance(instance);
                    final List<ServiceParameter> filteredParameters = InstanceParameterFilter
                            .getFilteredInstanceParametersForService(parameterValues);
                    final BaseResult result = provisioning.modifySubscription(
                            instanceId, subscriptionId, filteredParameters,
                            requestingUser);
                    if (provResult.isError(result)) {
                        return result;
                    }
                }

                // If everything worked well we will save all changed parameters
                instance.setInstanceParameters(status.getChangedParameters());
            }

            instance.setProvisioningStatus(targetStatus);
            instance.setSubscriptionId(subscriptionId);
            em.persist(instance);

            timerService.initTimers();

            return provResult.newOkBaseResult();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return provResult.getErrorResult(BaseResult.class, e,
                    getLocale(requestingUser), instance, instanceId);
        }
    }

    @Override
    public BaseResult deleteInstance(String instanceId, String organizationId,
            String subscriptionId, User requestingUser) {

        logger.info("Delete instance {}.", instanceId);
        return appImpl.deleteInstance(instanceId, organizationId,
                subscriptionId, requestingUser);
    }

    @Override
    public InstanceResult createInstance(InstanceRequest request,
            User requestingUser) {
        return provResult.getBaseResult(InstanceResult.class, 1, Messages.get(
                getLocale(requestingUser), "error_synchronous_provisioning"));
    }

    private HashMap<String, String> createParameterMap(
            List<ServiceParameter> parameterValues) {
        final HashMap<String, String> map = new HashMap<String, String>();
        if (parameterValues != null) {
            for (final ServiceParameter p : parameterValues) {
                String value = p.getValue();
                map.put(p.getParameterId(), value != null ? value : "");
            }
        }
        return map;
    }

    @Override
    public BaseResult activateInstance(String instanceId, User requestingUser) {
        logger.info("Activate instance {}.", instanceId);
        ServiceInstance instance = null;

        try {
            instance = instanceDAO.getInstanceById(instanceId);

            checkInstanceAvailability(instance);

            final APPlatformController controller = APPlatformControllerFactory
                    .getInstance(instance.getControllerId());
            final ProvisioningSettings settings = configService
                    .getProvisioningSettings(instance,
                            UserMapper.toServiceUser(requestingUser));

            // Forward request
            final InstanceStatus status = controller.activateInstance(
                    instance.getInstanceId(), settings);
            if (status != null) {
                // forward call to provisioning service on application instance
                if (status.isInstanceProvisioningRequested()) {
                    final ProvisioningService provisioning = provisioningFactory
                            .getInstance(instance);
                    final BaseResult result = provisioning.activateInstance(
                            instanceId, requestingUser);
                    if (provResult.isError(result)) {
                        return result;
                    }
                }

                // If everything worked well we will save all changed parameters
                instance.setInstanceParameters(status.getChangedParameters());
            }

            // Update current state
            instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_ACTIVATION);
            em.persist(instance);

            timerService.initTimers();

            return provResult.newOkBaseResult();

        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return provResult.getErrorResult(BaseResult.class, e,
                    getLocale(requestingUser), instance, instanceId);
        }
    }

    void checkInstanceAvailability(ServiceInstance instance)
            throws ServiceInstanceInProcessingException {
        if (!instance.isAvailable()) {
            throw new ServiceInstanceInProcessingException(
                    "Parallel processing requested, but not allowed: instance %s is in state %s",
                    instance.getInstanceId(), instance.getProvisioningStatus()
                            .name());
        }
    }

    @Override
    public BaseResult deactivateInstance(String instanceId, User requestingUser) {
        logger.info("Deactivate instance {}.", instanceId);
        ServiceInstance instance = null;

        try {

            instance = instanceDAO.getInstanceById(instanceId);

            checkInstanceAvailability(instance);

            final APPlatformController controller = APPlatformControllerFactory
                    .getInstance(instance.getControllerId());
            final ProvisioningSettings settings = configService
                    .getProvisioningSettings(instance,
                            UserMapper.toServiceUser(requestingUser));

            // Forward request
            final InstanceStatus status = controller.deactivateInstance(
                    instance.getInstanceId(), settings);
            if (status != null) {
                // forward call to provisioning service on application instance
                if (status.isInstanceProvisioningRequested()) {
                    final ProvisioningService provisioning = provisioningFactory
                            .getInstance(instance);
                    final BaseResult result = provisioning.deactivateInstance(
                            instanceId, requestingUser);
                    if (provResult.isError(result)) {
                        return result;
                    }
                }

                // If everything worked well we will save all changed parameters
                instance.setInstanceParameters(status.getChangedParameters());
            }

            // Update current state
            instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_DEACTIVATION);
            em.persist(instance);

            timerService.initTimers();

            return provResult.newOkBaseResult();

        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return provResult.getErrorResult(BaseResult.class, e,
                    getLocale(requestingUser), instance, instanceId);
        }
    }

    @Override
    public BaseResult modifySubscription(String instanceId,
            String subscriptionId, List<ServiceParameter> parameterValues,
            User requestingUser) {
        return provResult.getBaseResult(BaseResult.class, 1, Messages.get(
                getLocale(requestingUser), "error_synchronous_provisioning"));
    }

    @Override
    public BaseResult asyncUpgradeSubscription(String instanceId,
            String subscriptionId, List<ServiceParameter> parameterValues,
            User requestingUser) {
        final HashMap<String, String> parameterMap = createParameterMap(parameterValues);
        logger.info("Upgrade instance {}: {}", instanceId, parameterMap);
        return modifySubscription(instanceId, subscriptionId, parameterValues,
                parameterMap, ProvisioningStatus.WAITING_FOR_SYSTEM_UPGRADE,
                requestingUser);
    }

    @Override
    public BaseResult upgradeSubscription(String instanceId,
            String subscriptionId, List<ServiceParameter> parameterValues,
            User requestingUser) {
        return provResult.getBaseResult(BaseResult.class, 1, Messages.get(
                getLocale(requestingUser), "error_synchronous_provisioning"));
    }

    /**
     * Transforms a list of user entities into service user entities.
     */
    private List<ServiceUser> mapToServiceUsers(List<User> users) {
        List<ServiceUser> list = new ArrayList<ServiceUser>();
        for (User user : users) {
            list.add(UserMapper.toServiceUser(user));
        }
        return list;
    }

    /**
     * Merges updated application user ids to given list of users
     */
    private void mergeServiceUsers(List<User> users,
            List<ServiceUser> updatedUsers) {
        for (ServiceUser updatedUser : updatedUsers) {
            for (User user : users) {
                if (user.getUserId().equals(updatedUser.getUserId())) {
                    user.setApplicationUserId(updatedUser
                            .getApplicationUserId());
                    break;
                }
            }
        }
    }

    String getLocale(User requestingUser) {
        String locale = null;
        if (requestingUser != null) {
            locale = requestingUser.getLocale();
        }
        return locale;
    }

}
