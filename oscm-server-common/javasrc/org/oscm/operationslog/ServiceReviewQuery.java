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
public class ServiceReviewQuery extends UserOperationLogQuery {

    private static final String SERVICE_REVIEW = "";

    private static final String[] fieldNames = new String[] {
            COMMON_COLUMN_MODDATE, "op", "user", COMMON_COLUMN_OBJVERSION,
            "service", "supplier", "id", "user id", "rating", "title" };

    @Override
    public LogMessageIdentifier getLogMessageIdentifier() {
        return LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE_REVIEW;
    }

    @Override
    public String getQuery() {
        return SERVICE_REVIEW;
    }

    @Override
    public String[] getFieldNames() {
        return fieldNames;
    }

    @Override
    public String getLogType() {
        return "SERVICE_REVIEW";
    }
}
