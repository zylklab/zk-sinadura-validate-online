package net.zylk.validate.online.webscripts.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesManager {
	
	private static final String CONFIG_FILENAME = "alfresco/extension/zk-validate-online.properties";
	
	
	private static final Properties properties = new Properties();
	
	
	public static String getProperty(String property) {
		String value = "";
		
		try {
		
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
		    InputStream is = cl.getResourceAsStream(CONFIG_FILENAME);
			properties.load(is);
			value = properties.getProperty(property);

		
			
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		return value;		
	}
	




}