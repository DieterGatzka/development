/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.test.stubs;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.UnitUserRole;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.BulkUserImportException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.UserActiveException;
import org.oscm.internal.types.exception.UserDeletionConstraintException;
import org.oscm.internal.types.exception.UserModificationConstraintException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;

public class IdentityServiceStub implements IdentityService,
        IdentityServiceLocal {

    @Override
    public void grantUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserRoleAssignmentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void revokeUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException,
            UserModificationConstraintException, UserActiveException,
            OperationNotPermittedException, UserRoleAssignmentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUserDetails createUser(VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void confirmAccount(VOUser user, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteUser(VOUser user, String marketplaceId)
            throws OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUser getUser(VOUser user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUserDetails getCurrentUserDetails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUserDetails getCurrentUserDetailsIfPresent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUserDetails getUserDetails(VOUser user)
            throws OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOUserDetails> getUsersForOrganization() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void importLdapUsers(List<VOUserDetails> users, String marketplaceId)
            throws MailOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void lockUserAccount(VOUser userToBeLocked,
            UserAccountStatus newStatus, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUserDetails updateUser(VOUserDetails userDetails)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notifyOnLoginAttempt(VOUser user, boolean attemptSuccessful)
            throws ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void requestResetOfUserPassword(VOUser user, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOUserDetails> searchLdapUsers(String userIdPattern)
            throws ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendAccounts(String email, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unlockUserAccount(VOUser user, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createOrganizationAdmin(VOUserDetails userDetails,
            Organization organization, String password, Long serviceKey,
            Marketplace marketplace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deletePlatformUser(PlatformUser user, Marketplace marketplace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getOperatorLogInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PlatformUser> getOverdueOrganizationAdmins(long currentTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlatformUser getPlatformUser(String userId,
            boolean validateOrganization) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUserAccountStatus(PlatformUser user,
            UserAccountStatus newStatus) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlatformUser modifyUserData(PlatformUser existingUser,
            VOUserDetails tempUser, boolean modifyOwnUser, boolean sendMail)
            throws OperationNotPermittedException,
            NonUniqueBusinessKeyException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetPasswordForUser(PlatformUser user, Marketplace marketplace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<UserRoleType> getAvailableUserRoles(VOUser user)
            throws ObjectNotFoundException, OperationNotPermittedException {
        return Collections.emptyList();
    }

    @Override
    public void setUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserModificationConstraintException, UserRoleAssignmentException,
            UserActiveException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeInactiveOnBehalfUsers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeInactiveOnBehalfUsersImpl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUserDetails createOnBehalfUser(String organizationId, String string)
            throws ObjectNotFoundException, OperationNotPermittedException,
            NonUniqueBusinessKeyException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanUpCurrentUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void grantUserRoles(PlatformUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserRoleAssignmentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshLdapUser() throws ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUserDetails createUserInt(TriggerProcess tp)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PlatformUser> getOrganizationUsers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<UserRoleType> getAvailableUserRolesForUser(PlatformUser pu) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetUserPassword(PlatformUser platformUser,
            String marketplaceId) throws UserActiveException,
            MailOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteUser(PlatformUser pUser, String marketplaceId)
            throws OperationNotPermittedException,
            UserDeletionConstraintException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendUserUpdatedMail(PlatformUser existingUser,
            PlatformUser oldUser) {
    }

    @Override
    public void notifySubscriptionsAboutUserUpdate(PlatformUser existingUser) {
    }

    @Override
    public void setUserRolesInt(Set<UserRoleType> roles, PlatformUser pUser)
            throws UserModificationConstraintException, UserActiveException,
            OperationNotPermittedException, UserRoleAssignmentException,
            ObjectNotFoundException {
    }

    @Override
    public void verifyIdUniquenessAndLdapAttributes(PlatformUser existingUser,
            PlatformUser modUser) throws NonUniqueBusinessKeyException {
    }

    @Override
    public void sendMailToCreatedUser(String password, boolean userLocalLdap,
            Marketplace marketplace, PlatformUser pu)
            throws MailOperationException {
    }

    @Override
    public boolean isUserLoggedIn(long userKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void importUser(VOUserDetails user, String marketplaceId)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            ObjectNotFoundException {
    }

    @Override
    public void importUsersInOwnOrganization(byte[] csvData,
            String marketplaceId) throws BulkUserImportException,
            ObjectNotFoundException, IllegalArgumentException {
    }

    @Override
    public void importUsers(byte[] csvData, String organizationId,
            String marketplaceId) throws BulkUserImportException,
            ObjectNotFoundException, IllegalArgumentException {
    }

    @Override
    public boolean isCallerOrganizationAdmin() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addRevokeUserUnitAssignment(String unitName,
            List<VOUser> usersToBeAdded, List<VOUser> usersToBeRevoked)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, NonUniqueBusinessKeyException {
        return false;
    }

    @Override
    public boolean searchLdapUsersOverLimit(String userIdPattern)
            throws ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendAdministratorNotifyMail(PlatformUser administrator,
            String userId) {

    }

    @Override
    public VOUserDetails createUserWithGroups(VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId,
            Map<Long, UnitUserRole> groups)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void grantUnitRole(PlatformUser user, UserRoleType role) {
    }

    @Override
    public void revokeUnitRole(PlatformUser user, UserRoleType role)
            throws UserModificationConstraintException {
    }
}
