/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015
 *  <p/>
 *  Creation Date: 2015-06-02
 *******************************************************************************/
package org.oscm.ui.validator;

import org.oscm.internal.subscriptions.POSubscription;
import org.oscm.internal.subscriptions.SubscriptionsService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOSubscriptionDetails;

public class MySubscriptionStatusValidator implements Validator {

    @Override
    public boolean supports(Class<?> objCls, Class<?> paramCls) {
        return SubscriptionsService.class.isAssignableFrom(objCls)
                && POSubscription.class.isAssignableFrom(paramCls);
    }

    @Override
    public boolean validate(Object obj, Object param) {
        SubscriptionsService service = (SubscriptionsService) obj;
        POSubscription poSubscription = (POSubscription) param;
        long subId = poSubscription.getKey();

        try {
            VOSubscriptionDetails sub = service.getSubscriptionDetails(subId);
            if (!sub.getStatus().equals(poSubscription.getStatus())) {
                return false;
            }
        } catch (ObjectNotFoundException e) {
            return false;
        }
        return true;
    }
}
