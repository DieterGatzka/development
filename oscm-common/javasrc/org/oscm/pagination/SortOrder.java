/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                                                                                 
 *  Creation Date: 30.03.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.pagination;

/**
 * Sort order ascending, descending, unsorted
 */
public enum SortOrder {

    ASC, DESC, UNSORTED;

    public boolean isSorted() {
        return !UNSORTED.equals(this);
    }
}
