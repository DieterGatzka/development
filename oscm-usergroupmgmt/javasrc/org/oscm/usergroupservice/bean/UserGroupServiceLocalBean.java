/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                                                                                 
 *  Creation Date: 2014-6-25                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.usergroupservice.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.communicationservice.data.SendMailStatus.SendMailStatusItem;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UnitRoleAssignment;
import org.oscm.domobjects.UnitUserRole;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserGroupToInvisibleProduct;
import org.oscm.domobjects.UserGroupToUser;
import org.oscm.interceptor.AuditLogDataInterceptor;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.DeletingUnitWithSubscriptionsNotPermittedException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.pagination.Pagination;
import org.oscm.subscriptionservice.local.SubscriptionListServiceLocal;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.usergroupservice.auditlog.UserGroupAuditLogCollector;
import org.oscm.usergroupservice.auditlog.UserGroupAuditLogOperation;
import org.oscm.usergroupservice.dao.UserGroupDao;
import org.oscm.validation.ArgumentValidator;

/**
 * @author yuyin
 * 
 */
@Stateless
@LocalBean
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class,
        AuditLogDataInterceptor.class })
public class UserGroupServiceLocalBean {
    @EJB(beanInterface = DataService.class)
    private DataService dm;

    @EJB(beanInterface = CommunicationServiceLocal.class)
    private CommunicationServiceLocal cs;

    @EJB(beanInterface = UserGroupDao.class)
    private UserGroupDao userGroupDao;

    @EJB(beanInterface = UserGroupAuditLogCollector.class)
    UserGroupAuditLogCollector audit;
    
    @EJB(beanInterface = SubscriptionListServiceLocal.class)
    SubscriptionListServiceLocal slsl;

    @Resource
    private SessionContext sessionCtx;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(UserGroupServiceLocalBean.class);

    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public UserGroup createUserGroup(UserGroup group,
            List<Product> visibleProducts, List<Product> invisibleProducts,
            String marketplaceId) throws NonUniqueBusinessKeyException,
            OperationNotPermittedException, MailOperationException,
            ObjectNotFoundException {

        ArgumentValidator.notNull("group", group);
        ArgumentValidator.notNull("marketplaceId", marketplaceId);
        validateNotDefaultUserGroup(group);
        group.setOrganization(dm.getCurrentUser().getOrganization());

        validateActiveProduct(visibleProducts);
        validateActiveProduct(invisibleProducts);

        try {
            dm.persist(group);
            List<UserGroupToInvisibleProduct> userGroupToInvisibleProducts = group
                    .getUserGroupToInvisibleProducts();
            for (UserGroupToInvisibleProduct userGroupToInvisibleProduct : userGroupToInvisibleProducts) {
                dm.persist(userGroupToInvisibleProduct);
            }
            List<UserGroupToUser> userGroupToUsers = group
                    .getUserGroupToUsers();
            List<PlatformUser> assignedUsers = new ArrayList<>();
            for (UserGroupToUser userGroupToUser : userGroupToUsers) {
                loadPlatformUser(userGroupToUser.getPlatformuser());
                assignedUsers.add(userGroupToUser.getPlatformuser());
                dm.persist(userGroupToUser);
            }
            dm.flush();

            String[] params = new String[] { group.getName() };
            List<PlatformUser> users = group.getUsers();
            if (users != null && users.size() > 0)
                sendMailToUser(users, EmailType.GROUP_USER_ASSIGNED, params);

            if (!(visibleProducts == null || visibleProducts.isEmpty())) {
                audit.accessToServices(dm,
                        UserGroupAuditLogOperation.ENABLE_ACCESS_TO_SERVICES,
                        group, visibleProducts, marketplaceId);
            }
            if (!assignedUsers.isEmpty()) {
                audit.assignUsersToGroup(dm, group, assignedUsers);
            }
        } catch (NonUniqueBusinessKeyException | MailOperationException
                | ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
        return group;
    }
    
    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public void deleteUserGroup(String userGroupName)
            throws ObjectNotFoundException, OperationNotPermittedException,
            DeletingUnitWithSubscriptionsNotPermittedException,
            MailOperationException {

        ArgumentValidator.notEmptyString("userGroupName", userGroupName);

        try {
            UserGroup userGroup = getUserGroupByName(userGroupName);

            validateNotDefaultUserGroup(userGroup);
            validateNotUnitWithSubscriptions(userGroup);

            dm.remove(userGroup);

            List<PlatformUser> users = userGroup.getUsers();
            if (users.size() > 0)
                sendMailToUser(users, EmailType.USER_GROUP_DELETED,
                        new String[] { userGroup.getName() });
            else {
                audit.removeUsersFromGroup(dm, userGroup, users);
            }
        } catch (ObjectNotFoundException | MailOperationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public boolean deleteUserGroup(UserGroup groupToBeDeleted)
            throws OperationNotPermittedException, ObjectNotFoundException,
            MailOperationException,
            DeletingUnitWithSubscriptionsNotPermittedException {
    
        ArgumentValidator.notNull("group", groupToBeDeleted);
        validateNotDefaultUserGroup(groupToBeDeleted);
        validateNotUnitWithSubscriptions(groupToBeDeleted);
    
        try {
            UserGroup userGroup = dm.getReference(UserGroup.class,
                    groupToBeDeleted.getKey());
            List<PlatformUser> users = userGroup.getUsers();
            dm.remove(userGroup);
            // the GroupToUser and GroupToProduct should be removed
            // automatically
            // mail all the group users
            String[] params = new String[1];
            params[0] = groupToBeDeleted.getName();
            if (users != null && users.size() > 0)
                sendMailToUser(users, EmailType.USER_GROUP_DELETED, params);
            if (!(users == null || users.isEmpty())) {
                audit.removeUsersFromGroup(dm, userGroup, users);
            }
        } catch (ObjectNotFoundException | MailOperationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
        return false;
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public UserGroup updateUserGroup(UserGroup newGroup,
            List<Product> visibleProducts, List<Product> invisibleProducts,
            String marketplaceId, List<PlatformUser> usersToAssign,
            List<PlatformUser> usersToDeassign)
            throws OperationNotPermittedException, ObjectNotFoundException,
            NonUniqueBusinessKeyException, MailOperationException {
        ArgumentValidator.notNull("group", newGroup);
        ArgumentValidator.notNull("marketplaceId", marketplaceId);
        ArgumentValidator.notNull("usersToAssign", usersToAssign);
        ArgumentValidator.notNull("usersToDeassign", usersToDeassign);
        UserGroup userGroup = dm.getReference(UserGroup.class,
                newGroup.getKey());

        validateActiveProduct(visibleProducts);
        validateActiveProduct(invisibleProducts);

        checkIfAllowedToModify(newGroup, userGroup);
        String[] params = new String[2];
        params[0] = userGroup.getName();
        params[1] = newGroup.getName();
        try {
            newGroup.setOrganization(dm.getCurrentUser().getOrganization());
            dm.validateBusinessKeyUniqueness(newGroup);
            if (!usersToDeassign.isEmpty()) {
                revokeUsersFromGroupInt(userGroup, usersToDeassign);
            }
            List<PlatformUser> users = userGroup.getUsers();
            userGroup.setName(newGroup.getName());
            userGroup.setDescription(newGroup.getDescription());
            userGroup.setReferenceId(newGroup.getReferenceId());
            updateProducts(userGroup, newGroup, visibleProducts,
                    invisibleProducts, marketplaceId);
            if (!usersToAssign.isEmpty()) {
                assignUsersToGroupInt(userGroup, usersToAssign);
            }
            dm.flush();

            if (!userGroup.isDefault() && !params[0].equals(params[1])
                    && users != null && users.size() > 0) {
                sendMailToUser(users, EmailType.USER_GROUP_UPDATED, params);
            }
            dm.refresh(userGroup);
        } catch (NonUniqueBusinessKeyException e) {
            e.setMessageParams(new String[] { newGroup.getName() });
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (MailOperationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
        return userGroup;
    }

    public UserGroup getUserGroupByName(String name)
            throws ObjectNotFoundException {
        UserGroup userGroup = new UserGroup();
        userGroup.setName(name);
        userGroup.setOrganization(dm.getCurrentUser().getOrganization());
        userGroup = (UserGroup) dm.getReferenceByBusinessKey(userGroup);
        return userGroup;
    }

    private void updateProducts(UserGroup existingUserGroup,
            UserGroup newUserGroup, List<Product> visibleProducts,
            List<Product> invisibleProds, String marketplaceId)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {

        List<UserGroupToInvisibleProduct> existingGroupToProducts = existingUserGroup
                .getUserGroupToInvisibleProducts();

        Map<Long, UserGroupToInvisibleProduct> existingInvisibilities = getExistingInvisibilityRelations(existingGroupToProducts);

        List<Product> newInvisibleProds = handleInvisibileRelationsToBeAdded(
                newUserGroup, existingInvisibilities, invisibleProds);

        List<Product> newVisibleProds = handleInvisibleRelationsToBeRemoved(
                visibleProducts, existingInvisibilities);

        audit.accessToServices(dm,
                UserGroupAuditLogOperation.ENABLE_ACCESS_TO_SERVICES,
                newUserGroup, newVisibleProds, marketplaceId);
        audit.accessToServices(dm,
                UserGroupAuditLogOperation.DISABLE_ACCESS_TO_SERVICES,
                newUserGroup, newInvisibleProds, marketplaceId);
    }

    List<Product> handleInvisibleRelationsToBeRemoved(
            List<Product> visibleProducts,
            Map<Long, UserGroupToInvisibleProduct> existingInvisibilities) {
        final List<Long> newVisibleProductKeys = getNewVisibleProductKeys(
                visibleProducts);

        List<Product> newVisibleProds = new ArrayList<>();
        for (Entry<Long, UserGroupToInvisibleProduct> existingInvisibility : existingInvisibilities
                .entrySet()) {
            Product product = existingInvisibility.getValue().getProduct();
            if (newVisibleProductKeys.contains(Long.valueOf(product.getKey()))) {
                newVisibleProds.add(product);
                dm.remove(existingInvisibility.getValue());
            }
        }
        return newVisibleProds;
    }

    List<Product> handleInvisibileRelationsToBeAdded(UserGroup newUserGroup,
            Map<Long, UserGroupToInvisibleProduct> existingInvisibilities,
            List<Product> invisibleProds) throws NonUniqueBusinessKeyException {
        List<Product> newInvisibleProds = new ArrayList<>();
        for (Product invisibleProd : invisibleProds) {
            if (!existingInvisibilities.keySet().contains(
                    Long.valueOf(invisibleProd.getKey()))) {
                newInvisibleProds.add(invisibleProd);
                UserGroupToInvisibleProduct grpToProd = new UserGroupToInvisibleProduct();
                grpToProd.setProduct(invisibleProd);
                grpToProd.setUserGroup(newUserGroup);
                dm.persist(grpToProd);
            }
        }
        return newInvisibleProds;
    }

    Map<Long, UserGroupToInvisibleProduct> getExistingInvisibilityRelations(
            List<UserGroupToInvisibleProduct> oldGroupToProducts) {
        Map<Long, UserGroupToInvisibleProduct> existingProductInvisibilities = new HashMap<>();

        for (UserGroupToInvisibleProduct grpToProd : oldGroupToProducts) {
            existingProductInvisibilities.put(
                    Long.valueOf(grpToProd.getProduct_tkey()), grpToProd);
        }
        return existingProductInvisibilities;
    }

    List<Long> getNewVisibleProductKeys(List<Product> visibleProducts) {
        List<Long> newVisibleProductKeys = new ArrayList<>();
        for (Product p : visibleProducts) {
            newVisibleProductKeys.add(Long.valueOf(p.getKey()));
        }
        return newVisibleProductKeys;
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public List<UserGroup> getUserGroupsForOrganization() {
        return userGroupDao.getUserGroupsForOrganization();
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public Map<UserGroup, UnitRoleType> getUserGroupsForUserWithRoles(String userId) {
        Map<UserGroup, UnitRoleType> groupsWithRoles = new HashMap<>();
        List<UserGroup> groups = userGroupDao.getUserGroupsForUser(userId);
        for (UserGroup group : groups) {
            UnitRoleAssignment unitRoleAssignment = userGroupDao
                    .getRoleAssignmentByUserAndGroup(group.getKey(), userId);
            if (unitRoleAssignment == null) {
                groupsWithRoles.put(group, UnitRoleType.USER);
                continue;
            }
            groupsWithRoles.put(group, unitRoleAssignment.getUnitUserRole().getRoleName());
        }
        return groupsWithRoles;
    }
    
    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public Map<UserGroup, UnitRoleType> getUserGroupsForUserWithRolesWithoutDefault(String userId) {
        Map<UserGroup, UnitRoleType> groupsWithRoles = new HashMap<>();
        List<UserGroup> groups = userGroupDao.getUserGroupsForUserWithoutDefault(userId);
        for (UserGroup group : groups) {
            UnitRoleAssignment unitRoleAssignment = userGroupDao
                    .getRoleAssignmentByUserAndGroup(group.getKey(), userId);
            if (unitRoleAssignment == null) {
                groupsWithRoles.put(group, UnitRoleType.USER);
                continue;
            }
            groupsWithRoles.put(group, unitRoleAssignment.getUnitUserRole().getRoleName());
        }
        return groupsWithRoles;
    }

    public List<UserGroup> getUserGroupsForUserWithoutDefault(String userId) {
        return userGroupDao.getUserGroupsForUserWithoutDefault(userId);
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public List<UserGroup> getUserGroupsForUser(String userId) {
        return userGroupDao.getUserGroupsForUser(userId);
    }

    public UserGroup getUserGroupDetails(long groupKey)
            throws ObjectNotFoundException {
        return userGroupDao.getUserGroupDetails(groupKey);
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public List<UserGroup> getUserGroupsForOrganizationWithoutDefault() {
        return userGroupDao.getUserGroupsForOrganizationWithoutDefault();
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public UserGroup assignUsersToGroup(UserGroup group,
            List<PlatformUser> users) throws NonUniqueBusinessKeyException,
            ObjectNotFoundException, MailOperationException,
            OperationNotPermittedException {
        ArgumentValidator.notNull("group", group);
        ArgumentValidator.notNull("users", users);

        UserGroup userGroup = dm.getReference(UserGroup.class, group.getKey());

        try {
            assignUsersToGroupInt(userGroup, users);
        } catch (NonUniqueBusinessKeyException | MailOperationException
                | ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
        return userGroup;

    }

    private void assignUsersToGroupInt(UserGroup group, List<PlatformUser> users)
            throws MailOperationException, NonUniqueBusinessKeyException,
            ObjectNotFoundException, OperationNotPermittedException {
        validateUserGroupOfOrganization(group);
        for (PlatformUser user : users) {
            PlatformUser pu = loadPlatformUser(user);
            UserGroupToUser userGroupToUser = new UserGroupToUser();
            userGroupToUser.setUserGroup(group);
            userGroupToUser.setPlatformuser(pu);
            dm.persist(userGroupToUser);
        }
        dm.flush();
        dm.refresh(group);

        if (!group.isDefault()) {
            sendMailToUser(users, EmailType.GROUP_USER_ASSIGNED,
                    new Object[] { group.getName() });
        }
        if (!users.isEmpty()) {
            audit.assignUsersToGroup(dm, group, users);
        }
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public UserGroup revokeUsersFromGroup(UserGroup group,
            List<PlatformUser> users) throws OperationNotPermittedException,
            ObjectNotFoundException, MailOperationException {
        ArgumentValidator.notNull("group", group);
        ArgumentValidator.notNull("users", users);
        UserGroup oldUserGroup = dm.getReference(UserGroup.class,
                group.getKey());

        try {
            revokeUsersFromGroupInt(oldUserGroup, users);
        } catch (MailOperationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
        return oldUserGroup;
    }

    private UserGroup revokeUsersFromGroupInt(UserGroup group,
            List<PlatformUser> users) throws OperationNotPermittedException,
            ObjectNotFoundException, MailOperationException {
        validateUserGroupOfOrganization(group);
        validatePlatformUserOfOrganization(users.toArray(new PlatformUser[users
                .size()]));

        for (PlatformUser user : users) {
            for (UserGroupToUser userGroupToUser : group.getUserGroupToUsers()) {
                if (userGroupToUser.getPlatformuser().getUserId()
                        .equals(user.getUserId())) {
                    userGroupToUser.getPlatformuser().getUserGroupToUsers().remove(userGroupToUser);
                    removeSubscriptionOwner(userGroupToUser.getPlatformuser());
                    dm.remove(userGroupToUser);
                }
            }
        }

        dm.flush();
        dm.refresh(group);
        sendMailToUser(users, EmailType.GROUP_USER_REVOKED,
                new Object[] { group.getName() });
        if (!users.isEmpty()) {
            audit.removeUsersFromGroup(dm, group, users);
        }
        return group;
    }
    
    /**
     * Used while removing user from unit. If user is only unit admin and has
     * subscriptions that he is owner of then ownership has to be removed from
     * subscriptions.
     * 
     * @param user
     *            - PlatformUser
     */
    private void removeSubscriptionOwner(PlatformUser user) {
        if (!isUnitAdmin(user)
                && !user.hasRole(UserRoleType.SUBSCRIPTION_MANAGER)
                && !user.hasRole(UserRoleType.ORGANIZATION_ADMIN)) {
            List<Subscription> subscriptions = slsl
                    .getSubscriptionsForOwner(user);
            for (Subscription subscription : subscriptions) {
                subscription.setOwner(null);
                dm.merge(subscription);
            }

            RoleAssignment adminAssignment = user
                    .getAssignedRole(UserRoleType.UNIT_ADMINISTRATOR);
            dm.remove(adminAssignment);
            dm.flush();
        }
    }

    /**
     * Checks if user is still allowed to have UserRoleType.UNIT_ADMINISTRATOR
     * role based on existing user group assignments and roles in this groups.
     * 
     * @param user
     *            - PlatformUser
     * @return - true if user is still unit administrator in any unit, false
     *         otherwise
     */
    private boolean isUnitAdmin(PlatformUser user) {
        for (UserGroupToUser userGroupToUser : user.getUserGroupToUsers()) {
            if (!userGroupToUser.getUnitRoleAssignments().isEmpty()) {
                UnitRoleAssignment roleAssignment = userGroupToUser
                        .getUnitRoleAssignments().get(0);
                if (roleAssignment.getUnitUserRole().getRoleName()
                        .equals(UnitRoleType.ADMINISTRATOR)) {
                    return true;
                }
            }
        }

        return false;
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public boolean handleRemovingCurrentUserFromGroup() {
        PlatformUser currentUser = dm.getCurrentUser();
        if (!currentUser.isUnitAdmin()) {
            return false;
        }
        List<UserGroup> userGroups = getUserGroupsForUserWithRole(currentUser.getKey(), UnitRoleType.ADMINISTRATOR.getKey());
        if (userGroups.isEmpty()) {
            RoleAssignment adminAssignment = currentUser.getAssignedRole(UserRoleType.UNIT_ADMINISTRATOR);
            dm.remove(adminAssignment);
            dm.flush();
            dm.refresh(currentUser);
            return true;
        }
        return false;
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public PlatformUser assignUserToGroups(PlatformUser user,
            Map<UserGroup, UnitUserRole> groups) throws NonUniqueBusinessKeyException,
            MailOperationException {
        ArgumentValidator.notNull("user", user);
        ArgumentValidator.notNull("groups", groups);

        String groupNames = "";

        try {
            for (Entry<UserGroup, UnitUserRole> entry : groups.entrySet()) {
                if (!entry.getKey().isDefault()) {
                    groupNames = groupNames.concat(entry.getKey().getName()).concat(",");
                }
                UserGroupToUser userGroupToUser = new UserGroupToUser();
                userGroupToUser.setUserGroup(entry.getKey());
                userGroupToUser.setPlatformuser(user);
                dm.persist(userGroupToUser);
                userGroupToUser.getKey();
                UnitRoleAssignment unitRoleAssignment = new UnitRoleAssignment();
                unitRoleAssignment.setUnitUserRole(entry.getValue());
                unitRoleAssignment.setUserGroupToUser(userGroupToUser);
                dm.persist(unitRoleAssignment);
            }
            dm.flush();
            if (!groupNames.isEmpty()) {
                sendMailToUser(Collections.singletonList(user),
                        EmailType.GROUP_USER_ASSIGNED,
                        new Object[] { removeTailString(groupNames) });
            }
            audit.assignUserToGroups(dm, groups.keySet(), user);
        } catch (NonUniqueBusinessKeyException | MailOperationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

        return user;
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public PlatformUser revokeUserFromGroups(PlatformUser user,
            List<UserGroup> groups) throws OperationNotPermittedException,
            ObjectNotFoundException, MailOperationException {

        ArgumentValidator.notNull("user", user);
        ArgumentValidator.notNull("groups", groups);
        validatePlatformUserOfOrganization(user);
        String groupNames = "";
        try {
            for (UserGroup userGroup : groups) {
                UserGroup oldUserGroup = dm.getReference(UserGroup.class,
                        userGroup.getKey());
                validateUserGroupOfOrganization(oldUserGroup);
                for (UserGroupToUser userGroupToUser : oldUserGroup
                        .getUserGroupToUsers()) {
                    if (userGroupToUser.getPlatformuser().getUserId()
                            .equals(user.getUserId())) {
                        dm.remove(userGroupToUser);
                        groupNames = groupNames.concat(userGroup.getName())
                                .concat(",");
                    }
                }
            }
            dm.flush();
            sendMailToUser(Collections.singletonList(user),
                    EmailType.GROUP_USER_REVOKED,
                    new Object[] { removeTailString(groupNames) });
            audit.removeUserFromGroups(dm, groups, user);
        } catch (ObjectNotFoundException | MailOperationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
        return user;
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public void addLogEntryWhenDeleteUser(PlatformUser user) {
        ArgumentValidator.notNull("user", user);
        List<UserGroup> groups = getUserGroupsForUserWithoutDefault(user
                .getUserId());
        audit.removeUserFromGroups(dm, groups, user);
    }

    public List<Long> getInvisibleProductKeysForUser(long userKey)
            throws ObjectNotFoundException {
        if (userGroupDao.getUserGroupCountForUser(userKey) > 0) {
            return userGroupDao.getInvisibleProductKeysForUser(userKey);
        } else {
            UserGroup userGroup = getDefaultUserGroupForUser(userKey);
            return userGroupDao.getInvisibleProductKeysForGroup(userGroup
                    .getKey());
        }
    }

    public List<Long> getInvisibleProductKeysForGroup(long groupKey) {
        return userGroupDao.getInvisibleProductKeysForGroup(groupKey);
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public long getUserCountForGroup(long groupKey, boolean isDefaultGroup) {
        long count;
        if (isDefaultGroup) {
            count = userGroupDao.getUserCountForDefaultGroup(dm
                    .getCurrentUser().getOrganization().getOrganizationId());
        } else {
            count = userGroupDao.getUserCountForGroup(groupKey);
        }
        return count;
    }

    public List<String> getAssignedUserIdsForUserGroup(long groupKey) {
        return userGroupDao.getAssignedUserIdsForGroup(groupKey);
    }

    private void validateUserGroupOfOrganization(UserGroup... userGroups)
            throws OperationNotPermittedException {
        Organization org = dm.getCurrentUser().getOrganization();
        for (UserGroup userGroup : userGroups) {
            if (userGroup.isDefault()) {
                String message = String
                        .format("It is not permitted to operate on default user group '%s'.",
                                userGroup.getName());
                OperationNotPermittedException e = new OperationNotPermittedException(
                        message);
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.WARN_DEFAULT_USERGROUP_OPERATION_NOT_PERMITTED);
                throw e;
            }
            if (org.getKey() != userGroup.getOrganization().getKey()) {
                String message = String
                        .format("User group '%s' does not belong to organization '%s'.",
                                userGroup.getName(), org.getOrganizationId());
                OperationNotPermittedException e = new OperationNotPermittedException(
                        message);
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.WARN_USERGROUP_NOT_BELONG_TO_ORGANIZATION);
                throw e;
            }
        }
    }

    private void validatePlatformUserOfOrganization(
            PlatformUser... platformUsers)
            throws OperationNotPermittedException, ObjectNotFoundException {
        Organization org = dm.getCurrentUser().getOrganization();
        for (PlatformUser platformUser : platformUsers) {
            PlatformUser user = (PlatformUser) dm.find(platformUser);
            if (user == null) {
                ObjectNotFoundException onf = new ObjectNotFoundException(
                        ObjectNotFoundException.ClassEnum.USER,
                        platformUser.getUserId());
                logger.logWarn(Log4jLogger.SYSTEM_LOG, onf,
                        LogMessageIdentifier.WARN_USER_NOT_FOUND);
                throw onf;
            } else if (user.getOrganization().getKey() != org.getKey()) {
                String message = String
                        .format("PlatformUser '%s' does not belong to organization '%s'.",
                                platformUser.getUserId(),
                                org.getOrganizationId());
                OperationNotPermittedException e = new OperationNotPermittedException(
                        message);
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.WARN_PLATFORMUSER_NOT_BELONG_TO_ORGANIZATION);
                throw e;
            }
        }
    }

    private void validateActiveProduct(List<Product> products)
            throws OperationNotPermittedException {
        if (products == null) {
            return;
        }
        for (Product product : products) {
            String productId = product.getProductId();
            if (!product.getStatus().equals(ServiceStatus.ACTIVE)) {
                OperationNotPermittedException ope = new OperationNotPermittedException();
                ope.setMessageKey("ex.OperationNotPermittedException.NOT_AVALIABLE_SERVICE");
                ope.setMessageParams(new String[] { productId });
                logger.logWarn(Log4jLogger.SYSTEM_LOG, ope,
                        LogMessageIdentifier.WARN_SERVICE_NOT_AVAILABLE,
                        productId);
                throw ope;
            }
        }
    }

    private void checkIfAllowedToModify(UserGroup newGroup, UserGroup oldGroup)
            throws OperationNotPermittedException {
        if (oldGroup.isDefault() != newGroup.isDefault()) {
            throw new OperationNotPermittedException(
                    "You cannot update user group's default attribute.");
        }
    }

    private void validateNotDefaultUserGroup(UserGroup group)
            throws OperationNotPermittedException {
        if (group.isDefault()) {
            throw new OperationNotPermittedException(
                    "You cannot create or delete the default user group.");
        }

    }

    void validateNotUnitWithSubscriptions(UserGroup unit)
            throws DeletingUnitWithSubscriptionsNotPermittedException {
        if (userGroupDao.isSubscriptionAssignedToUnit(unit.getKey())) {
            throw new DeletingUnitWithSubscriptionsNotPermittedException(
                    "You cannot delete the unit that subscriptions are assigned to.",
                    new String[] { unit.getName() });
        }
    }

    private void sendMailToUser(List<PlatformUser> recipients, EmailType type,
            Object[] params) throws MailOperationException {
        SendMailStatus<PlatformUser> mailStatus = cs.sendMail(type, params,
                null, recipients.toArray(new PlatformUser[recipients.size()]));
        if (mailStatus != null) {
            for (SendMailStatusItem<PlatformUser> sendMailStatusItem : mailStatus
                    .getMailStatus()) {
                if (sendMailStatusItem.errorOccurred()) {
                    MailOperationException mpe = new MailOperationException();
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            sendMailStatusItem.getException(),
                            LogMessageIdentifier.WARN_MAIL_USERGROUP_UPDATED_FAILED);
                    throw mpe;
                }
            }
        }
    }

    private UserGroup getDefaultUserGroupForUser(long userKey)
            throws ObjectNotFoundException {
        PlatformUser user = dm.getReference(PlatformUser.class, userKey);
        return getDefaultUserGroupForOrganization(user.getOrganization());
    }

    private UserGroup getDefaultUserGroupForOrganization(Organization org)
            throws ObjectNotFoundException {
        ArgumentValidator.notNull("organization", org);
        for (UserGroup group : org.getUserGroups()) {
            if (group.isDefault()) {
                return group;
            }
        }
        throw new ObjectNotFoundException(
                "Cannot find the default group for Organization '"
                        + org.getOrganizationId() + "'.");
    }

    private PlatformUser loadPlatformUser(PlatformUser platformUser)
            throws ObjectNotFoundException {
        PlatformUser pu = (PlatformUser) dm.find(platformUser);
        if (pu == null) {
            ObjectNotFoundException onf = new ObjectNotFoundException(
                    ObjectNotFoundException.ClassEnum.USER,
                    platformUser.getUserId());
            logger.logWarn(Log4jLogger.SYSTEM_LOG, onf,
                    LogMessageIdentifier.WARN_USER_NOT_FOUND);
            throw onf;
        }
        return pu;
    }

    private String removeTailString(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return name.substring(0, name.lastIndexOf(","));
    }

    private PlatformUser findUser(PlatformUser user)
            throws ObjectNotFoundException {
        if (user.getKey() == 0) {
            return (PlatformUser) dm.getReferenceByBusinessKey(user);
        }

        return dm.getReference(PlatformUser.class, user.getKey());
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public List<UserGroup> getOrganizationalUnits(Pagination pagination) {

        if (pagination != null) {
            return userGroupDao.getUserGroupsForOrganization(pagination);
        }

        return userGroupDao.getUserGroupsForOrganization();
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public void grantUserRoles(PlatformUser user,
            List<UnitRoleType> unitRoleTypes, UserGroup userGroup)
            throws ObjectNotFoundException, OperationNotPermittedException {
        ArgumentValidator.notNull("user", user);
        ArgumentValidator.notNull("unitRoleTypes", unitRoleTypes);
        ArgumentValidator.notNull("userGroup", userGroup);

        PlatformUser dbUser = findUser(user);
        UserGroupToUser userGroupToUser = userGroupDao.getUserGroupAssignment(
                userGroup, dbUser);
        
        validatePlatformUserOfOrganization(dbUser);
        validateUserGroupOfOrganization(userGroupToUser.getUserGroup());

        UnitRoleAssignment roleAssignment;
        for (UnitRoleType roleType : unitRoleTypes) {
            roleAssignment = new UnitRoleAssignment();
            UnitUserRole userRole = new UnitUserRole();

            userRole.setKey(roleType.getKey());
            userRole.setRoleName(roleType);

            roleAssignment.setUnitUserRole(userRole);
            roleAssignment.setUserGroupToUser(userGroupToUser);

            try {
                dm.persist(roleAssignment);
                dm.flush();
            } catch (NonUniqueBusinessKeyException ignored) {
                // if role assignment exists, nothing should be done
            }

        }
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public void revokeUserRoles(PlatformUser user,
            List<UnitRoleType> unitRoleTypes, UserGroup userGroup)
            throws ObjectNotFoundException, OperationNotPermittedException {
        ArgumentValidator.notNull("user", user);
        ArgumentValidator.notNull("unitRoleTypes", unitRoleTypes);
        ArgumentValidator.notNull("userGroup", userGroup);

        PlatformUser dbUser = findUser(user);
        UserGroupToUser userGroupToUser = userGroupDao.getUserGroupAssignment(
                userGroup, dbUser);
    
        validatePlatformUserOfOrganization(dbUser);
        validateUserGroupOfOrganization(userGroupToUser.getUserGroup());

        UnitRoleAssignment roleAssignment;
        for (UnitRoleType roleType : unitRoleTypes) {
            roleAssignment = new UnitRoleAssignment();
            UnitUserRole userRole = new UnitUserRole();

            userRole.setKey(roleType.getKey());
            userRole.setRoleName(roleType);

            roleAssignment.setUnitUserRole(userRole);
            roleAssignment.setUserGroupToUser(userGroupToUser);

            try {
                roleAssignment = (UnitRoleAssignment) dm
                        .getReferenceByBusinessKey(roleAssignment);
                dm.remove(roleAssignment);
            } catch (ObjectNotFoundException ignored) {
                // if role assignment does not exist, nothing should be done
            }
        }
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public UserGroup createUserGroup(String unitName, String description,
            String referenceId) throws NonUniqueBusinessKeyException {
        ArgumentValidator.notEmptyString("unitName", unitName);

        UserGroup userGroup = new UserGroup();

        userGroup.setName(unitName);
        userGroup.setDescription(description);
        userGroup.setReferenceId(referenceId);
        userGroup.setOrganization(dm.getCurrentUser().getOrganization());

        try {
            dm.persist(userGroup);
            dm.flush();
        } catch (NonUniqueBusinessKeyException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

        return userGroup;
    }

    /**
     * Method gets all unit roles which can be assigned to user.
     * 
     * @return list of available unit roles.
     */
    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public List<UnitUserRole> getRolesAvailableForUnit() {
        return userGroupDao.getRolesAvailableForUnit();
    }
    
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR", "SUBSCRIPTION_MANAGER" })
    public List<UserGroup> getUserGroupsForUserWithRole(long userKey,
            long userRoleKey) {
        return userGroupDao.getUserGroupsForUserWithRole(userKey, userRoleKey);
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public List<UserGroup> getUserGroupsForUserWithRoleWithoutDefault(
            long userKey, long userRoleKey) {
        return userGroupDao.getUserGroupsForUserWithRoleWithoutDefault(userKey, userRoleKey);
    }

    public DataService getDm() {
        return dm;
    }

    public void setDm(DataService dm) {
        this.dm = dm;
    }

    public CommunicationServiceLocal getCs() {
        return cs;
    }

    public void setCs(CommunicationServiceLocal cs) {
        this.cs = cs;
    }

    public UserGroupDao getUserGroupDao() {
        return userGroupDao;
    }

    public void setUserGroupDao(UserGroupDao userGroupDao) {
        this.userGroupDao = userGroupDao;
    }

    public UserGroupAuditLogCollector getAudit() {
        return audit;
    }

    public void setAudit(UserGroupAuditLogCollector audit) {
        this.audit = audit;
    }

    public SessionContext getSessionCtx() {
        return sessionCtx;
    }

    public void setSessionCtx(SessionContext sessionCtx) {
        this.sessionCtx = sessionCtx;
    }
    
    public UnitUserRole getUnitRoleByName(String roleName) {
        return userGroupDao.getUnitRoleByName(roleName);
    }
}
