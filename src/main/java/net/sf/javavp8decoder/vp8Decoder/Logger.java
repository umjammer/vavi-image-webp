
package net.sf.javavp8decoder.vp8Decoder;

import java.util.logging.Level;


public class Logger {
    private java.util.logging.Logger logger;

    private boolean enabled = false;

    public Logger() {
        logger = java.util.logging.Logger.getAnonymousLogger();
    }

    public void log(String s) {
        if (enabled)
            logger.log(Level.INFO, s);
    }

    public void error(String s) {
        logger.log(Level.SEVERE, s);
    }

    public void setEnabled(boolean debug) {
        this.enabled = debug;
    }
}
