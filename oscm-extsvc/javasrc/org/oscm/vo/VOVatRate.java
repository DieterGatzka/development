/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                        
 *       
 *  Creation Date: 2010-11-19                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.math.BigDecimal;

/**
 * Represents a VAT rate.
 * 
 */
public class VOVatRate extends BaseVO {

    private static final long serialVersionUID = -5866685756310941443L;

    /**
     * The VAT rate.
     */
    private BigDecimal rate = BigDecimal.ZERO;

    /**
     * Returns the value of the VAT rate.
     * 
     * @return the VAT rate
     */
    public BigDecimal getRate() {
        return rate;
    }

    /**
     * Sets the value of the VAT rate.
     * 
     * @param rate
     *            the VAT rate
     */
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

}
