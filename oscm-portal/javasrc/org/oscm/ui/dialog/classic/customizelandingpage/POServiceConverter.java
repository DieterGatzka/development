/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.ui.dialog.classic.customizelandingpage;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.oscm.internal.landingpageconfiguration.POService;

public class POServiceConverter implements Converter {

    public static final String SEPARATOR = "$%&/$%";

    public Object getAsObject(FacesContext fc, UIComponent component,
            String value) {
        if (value == null)
            return null;
        String[] tokens = value.split("\\$%&/\\$%");

        POService po = new POService();
        po.setKey(Long.parseLong(tokens[0]));
        po.setVersion(Integer.parseInt(tokens[1]));
        po.setServiceName(tokens[2]);

        po.setProviderName(tokens[3]);
        po.setPictureUrl(tokens[4]);
        po.setStatusSymbol(tokens[5]);
        return po;
    }

    public String getAsString(FacesContext fc, UIComponent component,
            Object value) {
        if (value == null)
            return null;
        POService po = (POService) value;
        return po.getKey() + SEPARATOR + po.getVersion() + SEPARATOR
                + po.getServiceName() + SEPARATOR + po.getProviderName()
                + SEPARATOR + po.getPictureUrl() + SEPARATOR
                + po.getStatusSymbol();
    }

}
