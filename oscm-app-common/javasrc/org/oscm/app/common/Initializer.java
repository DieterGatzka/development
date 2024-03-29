/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                        
 *                                                                              
 *  Creation Date: 2014-05-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.common;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oscm.app.common.intf.ControllerAccess;

@Singleton
@Startup
public class Initializer {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(Initializer.class);

    // Default name of log4j template
    private String LOG4J_TEMPLATE = "log4j.properties.template";

    // Timer based log file monitoring
    private long TIMER_DELAY_VALUE = 60000;

    @Resource
    private TimerService timerService;
    private File logFile;
    private long logFileLastModified = 0;
    private boolean logFileWarning = false;

    private ControllerAccess controllerAccess;

    @Inject
    public void setControllerAccess(final ControllerAccess controllerAccess) {
        this.controllerAccess = controllerAccess;
    }

    @PostConstruct
    private void postConstruct() {
        try {
            // Get default config folder of GF instance
            String instanceRoot = System
                    .getProperty("com.sun.aas.instanceRoot");
            String controllerId = controllerAccess.getControllerId();
            if (instanceRoot != null) {
                File root = new File(instanceRoot);
                if (root.isDirectory()) {
                    // Determine log file
                    logFile = new File(root, "/config/log4j." + controllerId
                            + ".properties");

                    // If the target file does not exist we will provide it once
                    // from the template
                    if (!logFile.exists()) {
                        publishTemplateFile();
                    }

                    // Read configuration
                    handleOnChange(logFile);

                    // And init timer based service
                    LOGGER.debug("Enable timer service for monitoring modification of "
                            + logFile.getPath());
                    initTimer();

                } else {
                    LOGGER.error("Failed to initialize log file: invalid instanceRoot "
                            + instanceRoot);
                    logFile = null;
                }
            } else {
                LOGGER.error("Failed to initialize log file: missing system property 'com.sun.aas.instanceRoot'");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to initialize log file", e);
            logFile = null;
        }
    }

    /**
     * Initialize timer service.
     */
    private void initTimer() {
        Collection<?> timers = timerService.getTimers();
        if (timers.isEmpty()) {
            timerService.createTimer(0, TIMER_DELAY_VALUE, null);
        }
    }

    /**
     * Copy template file to default destination
     */
    private void publishTemplateFile() throws Exception {
        InputStream is = null;
        try {
            // Search resource in controller package
            is = controllerAccess.getClass().getClassLoader()
                    .getResourceAsStream(LOG4J_TEMPLATE);
            if (is == null) {
                LOGGER.warn("Template file not found: " + LOG4J_TEMPLATE);
            } else if (logFile.getParentFile().exists()) {
                FileUtils
                        .writeByteArrayToFile(logFile, IOUtils.toByteArray(is));
            }
        } catch (Exception e) {
            // ignore
            LOGGER.error("Failed to publish template file from "
                    + LOG4J_TEMPLATE + " to " + logFile.getAbsolutePath(), e);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Handles the timer event.
     */
    @Timeout
    public void handleTimer(@SuppressWarnings("unused") Timer timer) {
        if (logFile != null) {
            handleOnChange(logFile);
        }
    }

    /**
     * On change event
     */
    void handleOnChange(File logFile) {
        try {
            long lastModif = logFile.lastModified();
            if (lastModif > logFileLastModified) {
                logFileLastModified = lastModif;
                LOGGER.debug("Reload log4j configuration from "
                        + logFile.getAbsolutePath());
                new PropertyConfigurator().doConfigure(
                        logFile.getAbsolutePath(),
                        LogManager.getLoggerRepository());
                logFileWarning = false;
            }
        } catch (Exception e) {
            if (!logFileWarning) {
                logFileWarning = true;
                LOGGER.error(logFile.getAbsolutePath(), e);
            }
        }
    }
}
