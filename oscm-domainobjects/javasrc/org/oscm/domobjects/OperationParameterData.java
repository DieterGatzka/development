/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                                                                                 
 *  Creation Date: 22.01.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.oscm.types.enumtypes.OperationParameterType;

/**
 * @author weiser
 * 
 */
@Embeddable
public class OperationParameterData extends DomainDataContainer implements
        Serializable {

    private static final long serialVersionUID = 1621019013716455043L;

    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OperationParameterType type;

    @Column
    private boolean mandatory;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OperationParameterType getType() {
        return type;
    }

    public void setType(OperationParameterType type) {
        this.type = type;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

}
