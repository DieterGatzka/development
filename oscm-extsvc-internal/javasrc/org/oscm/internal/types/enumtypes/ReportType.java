/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                        
 *                                                                              
 *  Creation Date: 2012-04-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

/**
 * Specifies the reports which are to be included in the lists generated by
 * <code>ReportingService.getAvailableReports</code>.
 */
public enum ReportType {

    /**
     * Reports for customers are not included in the list.
     */
    NON_CUSTOMER,

    /**
     * All available reports are listed.
     */
    ALL;
}
