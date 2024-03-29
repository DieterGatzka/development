/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                        
 *       
 *  Creation Date: 2010-04-26                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.vo.BaseVO;
import org.oscm.internal.vo.VOPaymentType;

/**
 * Represents a payment type setting for an organization.
 * 
 */
public class VOPaymentType extends BaseVO {

    private static final long serialVersionUID = 8873568637825169223L;

    private String name;
    private String paymentTypeId;
    private PaymentCollectionType collectionType;

    /**
     * Default constructor.
     */
    public VOPaymentType() {

    }

    /**
     * Retrieves the identifier of the payment type.
     * 
     * @return the payment type ID
     */
    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    /**
     * Sets the identifier of the payment type.
     * 
     * @param paymentTypeId
     *            the payment type ID
     */
    public void setPaymentTypeId(String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    /**
     * Indicates whether the given object is equal to this one.
     * 
     * @param obj
     *            the reference object with which to compare
     * @return <code>true</code> if this object is the same as the
     *         <code>obj</code> argument; <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof VOPaymentType) {
            VOPaymentType other = (VOPaymentType) obj;
            if (paymentTypeId != null) {
                return paymentTypeId.equals(other.paymentTypeId);
            }
        }
        return false;
    }

    /**
     * Returns a hash code value for this object.
     * 
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return paymentTypeId == null ? 0 : paymentTypeId.hashCode();
    }

    /**
     * Sets the payment collection type for the payment type.
     * 
     * @param collectionType
     *            the payment collection type
     */
    public void setCollectionType(PaymentCollectionType collectionType) {
        this.collectionType = collectionType;
    }

    /**
     * Retrieves the payment collection type set for the payment type.
     * 
     * @return the payment collection type
     */
    public PaymentCollectionType getCollectionType() {
        return collectionType;
    }

    /**
     * Retrieves the name of the payment type.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the payment type.
     * 
     * @param name
     *            the name
     */
    public void setName(String name) {
        this.name = name;
    }

}
