/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.integrationtests.mockproduct;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Data object describing the WS target.
 * 
 * @author hoffmann
 */
public class ConnectionInfo {

    private String baseUrl;

    private String version;

    private String username;

    private String password;

    private boolean clientCert;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isClientCert() {
        return clientCert;
    }

    public void setClientCert(boolean clientCert) {
        this.clientCert = clientCert;
    }

    public static ConnectionInfo createDefault() {
        final ConnectionInfo info = new ConnectionInfo();
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            host = "unknown";
        }
        info.setBaseUrl("https://" + host + ":8181/");
        info.setUsername("10000");
        info.setPassword("secret");
        info.setVersion("v1.9");
        return info;
    }

    public static ConnectionInfo get(HttpServletRequest request) {
        final HttpSession session = request.getSession();
        ConnectionInfo info = (ConnectionInfo) session
                .getAttribute("connectionInfo");
        if (info == null) {
            info = createDefault();
            session.setAttribute("connectionInfo", info);
        }
        return info;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
