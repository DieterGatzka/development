/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                                                                                 
 *  Creation Date: 2013-12-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.oscm.domobjects.enums.ModifiedEntityType;

/**
 * Data container for the domain object <code>ModifiedUda</code>.
 * 
 * @author Zhou
 */
@Embeddable
public class ModifiedUdaData extends DomainDataContainer {

    private static final long serialVersionUID = -4968210541500748628L;

    @Column(nullable = false)
    private long targetObjectKey;

    private String value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModifiedEntityType targetObjectType;

    @Column(nullable = false)
    private long subscriptionKey;

    public long getTargetObjectKey() {
        return targetObjectKey;
    }

    public void setTargetObjectKey(long targetObjectKey) {
        this.targetObjectKey = targetObjectKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ModifiedEntityType getTargetObjectType() {
        return targetObjectType;
    }

    public void setTargetObjectType(ModifiedEntityType targetObjectType) {
        this.targetObjectType = targetObjectType;
    }

    public long getSubscriptionKey() {
        return subscriptionKey;
    }

    public void setSubscriptionKey(long subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }

}
