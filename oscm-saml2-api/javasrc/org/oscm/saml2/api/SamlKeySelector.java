/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                                                                                 
 *  Creation Date: 31.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dsig.SignatureMethod;

/**
 * @author kulle
 * 
 */
abstract class SamlKeySelector extends KeySelector {

    final String ALGORITHM_RSA = "RSA";
    final String ALGORITHM_DSA = "DSA";

    boolean algorithmCompatibleWithMethod(String signatureMethod,
            String algorithmName) {
        if (ALGORITHM_DSA.equalsIgnoreCase(algorithmName)
                && SignatureMethod.DSA_SHA1.equalsIgnoreCase(signatureMethod)) {
            return true;
        } else if (ALGORITHM_RSA.equalsIgnoreCase(algorithmName)
                && SignatureMethod.RSA_SHA1.equalsIgnoreCase(signatureMethod)) {
            return true;
        }

        return false;
    }

}
