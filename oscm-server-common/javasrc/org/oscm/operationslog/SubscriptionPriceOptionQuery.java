/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Oct 13, 2011                                                      
 *                                                                              
 *  Completion Time: Oct 13, 2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.operationslog;

import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * 
 * @author tokoda
 * 
 */
public class SubscriptionPriceOptionQuery extends UserOperationLogQuery {

    private static final String SUBSCR_PRICE_OPTION = "";

    private static final String[] fieldNames = new String[] {
            COMMON_COLUMN_MODDATE, "op", "user", COMMON_COLUMN_OBJVERSION,
            "subscription", "customer", "customer id", "parameter", "option",
            "price", "price per user", "currency" };

    @Override
    public LogMessageIdentifier getLogMessageIdentifier() {
        return LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_PRICE_OPTION;
    }

    @Override
    public String getQuery() {
        return SUBSCR_PRICE_OPTION;
    }

    @Override
    public String[] getFieldNames() {
        return fieldNames;
    }

    @Override
    public String getLogType() {
        return "SUBSCR_PRICE_OPTION";
    }
}
