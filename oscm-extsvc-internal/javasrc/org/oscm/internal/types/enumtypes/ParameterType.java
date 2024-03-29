/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                        
 *       
 *  Creation Date: 2009-06-29                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

/**
 * Specifies the types of service parameters.
 * 
 */
public enum ParameterType {

    /**
     * The parameter is controlled by the platform.
     */
    PLATFORM_PARAMETER,
    /**
     * The parameter is not interpreted by the platform but sent to the service
     * with tenant provisioning.
     */
    SERVICE_PARAMETER;

}
