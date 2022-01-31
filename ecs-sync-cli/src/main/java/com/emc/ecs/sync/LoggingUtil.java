package com.emc.ecs.sync;

import com.emc.ecs.sync.rest.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public final class LoggingUtil {
    private static final Logger log = LoggerFactory.getLogger(LoggingUtil.class);

    public static LogLevel getRootLogLevel() {
        org.apache.logging.log4j.Level level = org.apache.logging.log4j.LogManager.getRootLogger().getLevel();
        if (org.apache.logging.log4j.Level.TRACE.equals(level) || org.apache.logging.log4j.Level.ALL.equals(level)) {
            return LogLevel.trace;
        } else if (org.apache.logging.log4j.Level.DEBUG.equals(level)) {
            return LogLevel.debug;
        } else if (org.apache.logging.log4j.Level.INFO.equals(level)) {
            return LogLevel.verbose;
        } else if (org.apache.logging.log4j.Level.WARN.equals(level)) {
            return LogLevel.quiet;
        }
        return LogLevel.silent;
    }

    // Note: this will *only* take effect if the log implementation is log4j2
    public static void setRootLogLevel(LogLevel logLevel) {
        // try to avoid a runtime dependency on log4j (untested)
        try {
            if (LogLevel.trace == logLevel) {
                org.apache.logging.log4j.core.config.Configurator.setRootLevel(org.apache.logging.log4j.Level.TRACE);
                java.util.logging.LogManager.getLogManager().getLogger("").setLevel(java.util.logging.Level.FINEST);
            } else if (LogLevel.debug == logLevel) {
                org.apache.logging.log4j.core.config.Configurator.setRootLevel(org.apache.logging.log4j.Level.DEBUG);
                java.util.logging.LogManager.getLogManager().getLogger("").setLevel(java.util.logging.Level.FINE);
            } else if (LogLevel.verbose == logLevel) {
                org.apache.logging.log4j.core.config.Configurator.setRootLevel(org.apache.logging.log4j.Level.INFO);
                java.util.logging.LogManager.getLogManager().getLogger("").setLevel(java.util.logging.Level.INFO);
            } else if (LogLevel.quiet == logLevel) {
                org.apache.logging.log4j.core.config.Configurator.setRootLevel(org.apache.logging.log4j.Level.WARN);
                java.util.logging.LogManager.getLogManager().getLogger("").setLevel(java.util.logging.Level.WARNING);
            } else if (LogLevel.silent == logLevel) {
                org.apache.logging.log4j.core.config.Configurator.setRootLevel(org.apache.logging.log4j.Level.ERROR);
                java.util.logging.LogManager.getLogManager().getLogger("").setLevel(java.util.logging.Level.SEVERE);
            }
        } catch (Throwable t) {
            log.warn("could not configure log4j (perhaps you're using a different logger, which is fine)", t);
        }
    }

    private LoggingUtil() {
    }
}
