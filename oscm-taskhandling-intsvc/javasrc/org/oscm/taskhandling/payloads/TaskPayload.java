/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                              
 *  Author: tokoda                                                
 *                                                                              
 *  Creation Date: Nov 3, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 3, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.payloads;

import java.io.Serializable;

/**
 * @author tokoda
 * 
 */
public interface TaskPayload extends Serializable {
    public String getInfo();
}
