package com.itc.msmart.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertyUtil {
	private static final Logger logger = Logger.getLogger(PropertyUtil.class);
	private static final String CONFIG_FILE_NAME = "config.properties";
	private static Properties properties = null;

	private Properties getProperties() throws IOException {
		properties = new Properties();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
		properties.load(inputStream);
		
		return properties;
	}
	
	public static Properties getPropValues() {
		try {
			if(properties == null) {
				return new PropertyUtil().getProperties();
			} else {
				return properties;
			}
		} catch(Exception exception) {
			logger.error("Error getting when reading property file");
			return properties;
		}
	}
}
