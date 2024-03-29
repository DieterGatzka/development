/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                                                                                 
 *  Creation Date: 28.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import java.util.Calendar;

/**
 * @author kulle
 * 
 */
class VerifierConfiguration {

    private String requestId;
    private String acsUrl;
    private String acsUrlHttps;
    private Calendar referenceTime;

    public VerifierConfiguration(String requestId, String acsUrl,
            String acsUrlHttps, Calendar referenceTime) {
        this.requestId = requestId;
        this.acsUrl = acsUrl;
        this.acsUrlHttps = acsUrlHttps;
        this.referenceTime = referenceTime;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getAcsUrl() {
        return acsUrl;
    }

    public String getAcsUrlHttps() {
        return acsUrlHttps;
    }

    public Calendar getReferenceTime() {
        return referenceTime;
    }

}
