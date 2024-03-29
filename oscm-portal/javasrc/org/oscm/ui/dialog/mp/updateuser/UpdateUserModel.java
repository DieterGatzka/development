/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                                                                                 
 *  Creation Date: 04.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.updateuser;

import org.oscm.ui.dialog.mp.createuser.CreateUserModel;
import org.oscm.ui.model.User;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * @author weiser
 * 
 */
@ViewScoped
@ManagedBean(name="updateUserModel")
public class UpdateUserModel extends CreateUserModel {

    private static final long serialVersionUID = -9060882990344583201L;

    private boolean locked;
    private boolean ldapManaged;
    private boolean errorOnRead;
    private long key;
    private int version;
    private String userName;
    private User user;

    public UpdateUserModel() {
        super();
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isLdapManaged() {
        return ldapManaged;
    }

    public void setLdapManaged(boolean resetPasswordButtonVisible) {
        this.ldapManaged = resetPasswordButtonVisible;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setErrorOnRead(boolean errorOnRead) {
        this.errorOnRead = errorOnRead;
    }

    public boolean isErrorOnRead() {
        return errorOnRead;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
