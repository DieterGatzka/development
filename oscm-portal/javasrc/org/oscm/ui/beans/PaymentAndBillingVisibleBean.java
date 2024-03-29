/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2015
 *
 *  Creation Date: 27.08.15 14:22
 *
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.Collection;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;

/**
 * Created by ChojnackiD on 2015-08-27.
 */
@ManagedBean
@ViewScoped
public class PaymentAndBillingVisibleBean implements Serializable {

    private static final long serialVersionUID = 2394547300236898442L;

    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    @ManagedProperty(value = "#{billingContactBean}")
    private BillingContactBean billingContactBean;

    public boolean isPaymentVisible(Collection<VOPaymentType> enabledPaymentTypes,
                                    Collection<VOPaymentInfo> paymentInfosForSubscription) {
        if (userBean.isLoggedInAndAdmin()) {
            return (enabledPaymentTypes != null && !enabledPaymentTypes.isEmpty());
        }

        if (userBean.isLoggedInAndAllowedToSubscribe()) {
            return (paymentInfosForSubscription != null && !paymentInfosForSubscription.isEmpty());
        }

        return false;
    }

    public boolean isBillingContactVisible() {

        if (userBean.isLoggedInAndAdmin()) {
            return true;
        }
        if ((userBean.isLoggedInAndSubscriptionManager() || userBean.isLoggedInAndUnitAdmin())
                && (billingContactBean.getBillingContacts() != null && !billingContactBean
                .getBillingContacts().isEmpty())) {
            return true;
        }

        return false;
    }

    public UserBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public BillingContactBean getBillingContactBean() {
        return billingContactBean;
    }

    public void setBillingContactBean(BillingContactBean billingContactBean) {
        this.billingContactBean = billingContactBean;
    }
}
