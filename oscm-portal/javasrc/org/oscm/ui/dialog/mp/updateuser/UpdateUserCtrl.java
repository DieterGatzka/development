/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                                                                                 
 *  Creation Date: 04.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.updateuser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.oscm.string.Strings;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.classic.manageusers.UserRole;
import org.oscm.ui.dialog.mp.createuser.Subscription;
import org.oscm.ui.dialog.mp.createuser.UserGroup;
import org.oscm.ui.dialog.state.TableState;
import org.oscm.ui.model.User;
import org.oscm.ui.profile.FieldData;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.constants.HiddenUIConstants;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.usermanagement.POServiceRole;
import org.oscm.internal.usermanagement.POSubscription;
import org.oscm.internal.usermanagement.POUser;
import org.oscm.internal.usermanagement.POUserAndSubscriptions;
import org.oscm.internal.usermanagement.POUserDetails;
import org.oscm.internal.usermanagement.UserService;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author weiser
 * 
 */
@ManagedBean
@ViewScoped
public class UpdateUserCtrl {

    private static final String ROLE_NAME_TEMPLATE = "UserRoleType.%s.enum";
    private static final String UNIT_ROLE_NAME_TEMPLATE = "UnitRoleType.%s.enum";
    private static final Set<UserAccountStatus> LOCKED = Collections
            .unmodifiableSet(EnumSet
                    .of(UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS));

    private UiDelegate ui;

    @ManagedProperty(value = "#{updateUserModel}")
    private UpdateUserModel model;
    @ManagedProperty(value = "#{appBean}")
    private ApplicationBean appBean;
    @ManagedProperty(value = "#{tableState}")
    private TableState ts;

    /**
     * EJB injected through setters.
     */
    private UserService userService;
    private UserGroupService userGroupService;
    private IdentityService identityService;

    transient Boolean rolesColumnVisible;

    @PostConstruct
    public void postConstruct() {
        ui = new UiDelegate();
        String userId = getSelectedUserId();
        try {
            POUserAndSubscriptions user = getUserService()
                    .getUserAndSubscriptionDetails(userId);
            model.setUser(new User(new VOUserDetails()));
            model.getUser().setUserId(userId);
            init(user);
            model.setErrorOnRead(false);
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
            model.setErrorOnRead(true);
        }
    }

    /**
     * TODO: think about different value passing between views. NOT through the session bean.
     * @return
     */
    public String getSelectedUserId() {
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return req.getParameter("userId");
    }

    List<UserGroup> initUserGroups() {
        List<POUserGroup> orgUserGroups = getUserGroupService()
                .getGroupListForOrganizationWithoutDefault();
        List<POUserGroup> userGroups = getUserGroupService()
                .getUserGroupListForUserWithRolesWithoutDefault(model.getUser().getUserId());
        Map<Long, POUserGroup> userGroupMap = new HashMap<>();
        for (POUserGroup g : userGroups) {
            userGroupMap.put(Long.valueOf(g.getKey()), g);
        }
        List<UserGroup> groups = new ArrayList<>();
        for (POUserGroup poUserGroup : orgUserGroups) {
            UserGroup userGroup = new UserGroup();
            userGroup.setPoUserGroup(poUserGroup);
            if (userGroupMap.containsKey(Long.valueOf(poUserGroup.getKey()))) {
                userGroup.setSelected(true);
            } else {
                userGroup.setSelected(false);
            }
            userGroup.setName(poUserGroup.getGroupName());
            userGroup.setDescription(poUserGroup.getGroupDescription());
            userGroup.setReferenceId(poUserGroup.getGroupReferenceId());
            List<SelectItem> roles = new ArrayList<>();
            for (UnitRoleType unitRoleType : UnitRoleType.values()) {
                SelectItem unitRole = new SelectItem(unitRoleType.name(),
                        formatRoleName(UNIT_ROLE_NAME_TEMPLATE,
                                unitRoleType.name()));
                roles.add(unitRole);
            }
            userGroup.setRoles(roles);
            if (userGroupMap.containsKey(Long.valueOf(poUserGroup.getKey()))) {
                userGroup.setSelectedRole(userGroupMap.get(Long.valueOf(poUserGroup.getKey())).getSelectedRole());
            } else {
                userGroup.setSelectedRole(UnitRoleType.USER.name());
            }
            groups.add(userGroup);
        }
        return groups;
    }

    private String formatRoleName(String template, String roleName) {
        return ui.getText(String.format(template, roleName));
    }
    
    public String save() throws SaaSApplicationException {

        if (!model.isTokenValid()) {
            return null;
        }
        POUserAndSubscriptions user = toPOUserAndSubscriptions();
        String outcome = BaseBean.OUTCOME_SUCCESS;
        Response response;
        try {
            response = getUserService().saveUserAndSubscriptionAssignment(user,
                    getAllUserGroups(model.getUserGroups()));
        } catch (SaaSApplicationException ex) {
            model.resetToken();
            throw ex;
        }
        if (response.getReturnCodes().size() > 0) {
            outcome = BaseBean.OUTCOME_PENDING;
        }
        if (Strings.areStringsEqual(ui.getMyUserId(), model.getUser().getUserId())) {
            ui.handle(response, BaseBean.INFO_USER_SAVED_ITSELF,
                    user.getUserId());
            refreshUser();
            ui.updateAndVerifyViewLocale();
        } else {
            ui.handle(response, BaseBean.INFO_USER_SAVED, user.getUserId());
        }
        model.resetToken();
        model.getUser().setUserId(user.getUserId());

        return outcome;
    }

    public String resetPwd() throws SaaSApplicationException {

        if (!model.isTokenValid()) {
            return BaseBean.OUTCOME_RESET_PWD + "?userId=" + model.getUser().getUserId();
        }
        POUser user = toPOUser();
        Response r = getUserService().resetUserPassword(user,
                ui.getMarketplaceId());
        ui.handle(r, BaseBean.INFO_USER_PWD_RESET);
        model.resetToken();

        return BaseBean.OUTCOME_RESET_PWD + "?userId=" +  model.getUser().getUserId();
    }

    public String delete() throws SaaSApplicationException {

        if (!model.isTokenValid()) {
            return null;
        }
        POUser user = toPOUser();
        try {
            Response r = getUserService().deleteUser(user,
                    ui.getMarketplaceId());
            ui.handle(r, BaseBean.INFO_USER_DELETED, user.getUserId());
        } catch (TechnicalServiceNotAliveException
                | TechnicalServiceOperationException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, BaseBean.ERROR_DELETE_USER_FROM_EXPIRED_SUBSCRIPTION);
            return "";
        }
        // reset user table paging if user was deleted
        ts.resetActivePages();
        model.resetToken();

        return BaseBean.OUTCOME_SUCCESS;
    }

    void init(POUserAndSubscriptions u) {
        boolean isLocked = LOCKED.contains(u.getStatus());
        Set<SettingType> set = u.getMappedAttributes();
        model.setEmail(new FieldData<>(u.getEmail(), set
                .contains(SettingType.LDAP_ATTR_EMAIL) || isLocked, true));
        model.setFirstName(new FieldData<>(u.getFirstName(), set
                .contains(SettingType.LDAP_ATTR_FIRST_NAME) || isLocked));
        model.setLastName(new FieldData<>(u.getLastName(), set
                .contains(SettingType.LDAP_ATTR_LAST_NAME) || isLocked));
        model.setLocale(new FieldData<>(u.getLocale(), isLocked, true));
        model.setUserId(new FieldData<>(u.getUserId(),
                set.contains(SettingType.LDAP_ATTR_UID)
                        || isLocked
                        || (Strings.areStringsEqual(ui.getMyUserId(),
                                u.getUserId()) && !appBean
                                .isInternalAuthMode()), true));
        String sal = null;
        if (u.getSalutation() != null) {
            sal = u.getSalutation().name();
        }
        model.setSalutation(new FieldData<>(sal, isLocked));
        model.setKey(u.getKey());
        model.setVersion(u.getVersion());
        model.setRoles(initUserRoles(u));
        model.setLocked(isLocked);
        List<Subscription> subs = initSubscription(u);
        model.setSubscriptions(subs);
        model.setUserName(createUserName(u));
        model.setLdapManaged(!set.isEmpty());
        model.setUserGroups(initUserGroups());

       appBean.checkLocaleValidation(u.getLocale());

    }

    List<UserRole> initUserRoles(POUserDetails u) {

        List<UserRole> result = new ArrayList<>();
        Set<UserRoleType> roles = u.getAvailableRoles();
        for (UserRoleType r : roles) {
            String name = ui
                    .getText(String.format(ROLE_NAME_TEMPLATE, r.name()));
            UserRole role = new UserRole(r, name, u.getAssignedRoles()
                    .contains(r));
            result.add(role);
        }

        return result;
    }

    List<Subscription> initSubscription(POUserAndSubscriptions data) {

        List<Subscription> result = new ArrayList<>();
        List<POSubscription> list = data.getSubscriptions();
        for (POSubscription s : list) {
            Subscription sub = new Subscription();
            sub.setId(s.getId());
            List<POServiceRole> roles = s.getRoles();
            sub.setRolesRendered(!roles.isEmpty());
            List<SelectItem> items = new ArrayList<>();
            for (POServiceRole r : roles) {
                SelectItem si = new SelectItem(String.format("%s:%s",
                        Long.valueOf(r.getKey()), r.getId()), r.getName());
                if(r.getName().equalsIgnoreCase(UnitRoleType.USER.name())) {
                    items.add(0, si);
                } else {
                    items.add(si);
                }
            }
            sub.setRoles(items);
            sub.setSelected(s.isAssigned());
            String selectedRole = null;
            if (s.getUsageLicense() != null
                    && s.getUsageLicense().getPoServieRole() != null) {
                POServiceRole poServiceRole = s.getUsageLicense()
                        .getPoServieRole();
                selectedRole = String.format("%s:%s",
                        Long.valueOf(poServiceRole.getKey()),
                        poServiceRole.getId());

                sub.setLicKey(s.getUsageLicense().getKey());
                sub.setLicVersion(s.getUsageLicense().getVersion());
            }
            sub.setSelectedRole(selectedRole);
            result.add(sub);
        }

        return result;
    }

    POUserAndSubscriptions toPOUserAndSubscriptions() {

        POUserAndSubscriptions uas = new POUserAndSubscriptions();
        uas.setAssignedRoles(getSelectedUserRoles(model.getRoles()));
        uas.setEmail(model.getEmail().getValue());
        uas.setFirstName(model.getFirstName().getValue());
        uas.setLastName(model.getLastName().getValue());
        uas.setLocale(model.getLocale().getValue());
        String sal = model.getSalutation().getValue();
        if (!Strings.isEmpty(sal)) {
            uas.setSalutation(Salutation.valueOf(sal));
        }
        uas.setUserId(model.getUserId().getValue());
        uas.setKey(model.getKey());
        uas.setVersion(model.getVersion());
        uas.setSubscriptions(getSubscriptionAssignment(model.getSubscriptions()));
        uas.setGroupsToBeAssigned(getSelectedUserGroups(model.getUserGroups()));
        return uas;
    }

    List<POUserGroup> getAllUserGroups(List<UserGroup> groups) {
        List<POUserGroup> poUserGroups = new ArrayList<>();
        for (UserGroup g : groups) {
            poUserGroups.add(g.getPoUserGroup());
        }
        return poUserGroups;
    }

    List<POUserGroup> getSelectedUserGroups(List<UserGroup> groups) {
        List<POUserGroup> poUserGroups = new ArrayList<>();
        for (UserGroup g : groups) {
            if (g.isSelected()) {
                poUserGroups.add(g.getPoUserGroup());
            }
        }
        return poUserGroups;
    }

    List<POSubscription> getSubscriptionAssignment(List<Subscription> subs) {

        List<POSubscription> result = new ArrayList<>();
        for (Subscription sub : subs) {
            if (!sub.isSelected()) {
                continue;
            }
            POSubscription s = new POSubscription();
            s.setId(sub.getId());
            s.getUsageLicense().setKey(sub.getLicKey());
            s.getUsageLicense().setVersion(sub.getLicVersion());

            String role = sub.getSelectedRole();
            if (!Strings.isEmpty(role)) {
                String[] selectedRole = role.split(":");
                long key = Long.parseLong(selectedRole[0]);
                POServiceRole r = new POServiceRole();
                r.setId(selectedRole[1]);
                r.setKey(key);
                s.getUsageLicense().setPoServieRole(r);
            }
            result.add(s);
        }

        return result;
    }

    Set<UserRoleType> getSelectedUserRoles(List<UserRole> roles) {

        Set<UserRoleType> result = new HashSet<>();
        for (UserRole r : roles) {
            if (r.isSelected()) {
                result.add(r.getType());
            }
        }

        return result;
    }

    public boolean isRolesDisabled() {
        return model.isLocked() || model.isErrorOnRead();
    }

    public boolean isSaveDisabled() {
        return model.isLocked() || model.isErrorOnRead();
    }

    public boolean isResetPwdDisabled() {
        return Strings.areStringsEqual(ui.getMyUserId(),
                model.getUser().getUserId())
                || model.isErrorOnRead();
    }

    public boolean isDeleteDisabled() {
        return Strings.areStringsEqual(ui.getMyUserId(),
                model.getUser().getUserId())
                || model.isErrorOnRead();
    }

    public boolean isRoleColumnRendered() {
        if (rolesColumnVisible == null) {
            boolean rolesDefined = false;
            List<Subscription> subs = model.getSubscriptions();
            for (int i = 0; i < subs.size() && !rolesDefined; i++) {
                Subscription subscription = subs.get(i);
                rolesDefined = subscription.isRolesRendered();
            }
            rolesColumnVisible = Boolean.valueOf(rolesDefined);
        }

        return rolesColumnVisible.booleanValue();
    }

    public boolean isResetPwdRendered() {

        boolean isLdapNotManaged = !model.isLdapManaged();
        boolean isInternalMode =appBean.isInternalAuthMode();
        return isLdapNotManaged && isInternalMode;
    }

    public boolean isSubTableRendered() {


        return (!model.getSubscriptions().isEmpty())
                && (!appBean.isUIElementHidden(
                HiddenUIConstants.PANEL_USER_LIST_SUBSCRIPTIONS))
                && !model.isErrorOnRead();
    }

    public String getDeleteMsgForUser() {

        return ui.getText("marketplace.account.deleteMsg",
                model.getUserName());
    }

    String createUserName(POUserAndSubscriptions u) {
        String userName = "";
        if (!Strings.isEmpty(u.getFirstName())) {
            userName = u.getFirstName().trim();
        }
        if (!Strings.isEmpty(u.getLastName())) {
            if (ui.isNameSequenceReversed()) {
                userName = u.getLastName().trim() + " " + userName;
            } else {
                userName = userName + " " + u.getLastName().trim();
            }
        }
        userName = !Strings.isEmpty(userName) ? userName.trim() : u.getUserId();

        return userName;
    }

    POUser toPOUser() {
        POUser u = new POUser();
        u.setKey(model.getKey());
        // delete and resetPassword read the user by id - changes must be
        // ignored - so taking id from selection and not from model (may be
        // changed by input)
        u.setUserId(model.getUser().getUserId());
        u.setVersion(model.getVersion());

        return u;
    }

    public boolean isCurrentUserRolesChanged() {
        return new BaseBean(){}.isCurrentUserRolesChanged();
    }

    private void refreshUser() {
        VOUserDetails u = getIdentityService()
                .getCurrentUserDetails();
        ui.getSession(true).setAttribute(Constants.SESS_ATTR_USER, u);
    }

    protected void addMessage(FacesMessage.Severity severity, String key) {
        JSFUtils.addMessage(null, severity,
                key, null);
    }

    public UiDelegate getUi() {
        return ui;
    }

    public void setUi(UiDelegate ui) {
        this.ui = ui;
    }

    private UserService getUserService() {
        if (userService == null) {
            userService = ui.findService(UserService.class);
        }
        return userService;
    }

    public UserGroupService getUserGroupService() {
        if (userGroupService == null) {
            userGroupService = ui.findService(UserGroupService.class);
        }
        return userGroupService;
    }

    public IdentityService getIdentityService() {
        if (identityService == null) {
            identityService = ui.findService(IdentityService.class);
        }
        return identityService;
    }

    @EJB
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @EJB
    public void setIdentityService(IdentityService identityService) {
        this.identityService = identityService;
    }

    @EJB
    public void setUserGroupService(UserGroupService userGroupService) {
        this.userGroupService = userGroupService;
    }

    public void setAppBean(ApplicationBean appBean) {
        this.appBean = appBean;
    }

    public void setTs(TableState ts) {
        this.ts = ts;
    }

    public void setModel(UpdateUserModel model) {
        this.model = model;
    }
}
