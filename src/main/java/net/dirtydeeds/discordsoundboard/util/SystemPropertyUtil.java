package net.dirtydeeds.discordsoundboard.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemPropertyUtil {
    
    private static final Logger LOG = LoggerFactory.getLogger(SystemPropertyUtil.class);
    
    /**
     * Loads in the properties from the app.properties file
     * @return 
     */
    public static Properties loadProperties() {
        Properties appProperties = new Properties();
        appProperties.putAll(System.getenv());
        InputStream stream = null;
        try {
            stream = new FileInputStream(System.getProperty("user.dir") + "/app.properties");
            appProperties.load(stream);
            stream.close();
            return appProperties;
        } catch (FileNotFoundException e) {
            LOG.warn("Could not find app.properties file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (stream == null) {
            LOG.warn("Loading app.properties file from resources folder");
            try {
                stream = SystemPropertyUtil.class.getResourceAsStream("/app.properties");
                if (stream != null) {
                    appProperties.load(stream);
                    stream.close();
                } else {
                    //TODO: Would be nice if we could auto create a default app.properties here.
                    LOG.error("You do not have an app.properties file. Please create one or ensure all properties are set in environment.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return appProperties;
    }
}
