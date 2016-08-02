package com.itc.msmart.webservices;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.itc.msmart.implementation.PropertyUtil;

public class SmartAppService {
	
	public static void main(String[] args){
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader("E:\\Shipment_group.xml"));
			String         line = null;
			StringBuilder  stringBuilder = new StringBuilder();
			String         ls = System.getProperty("line.separator");


			while((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			reader.close();
			
			SmartAppService obj = new SmartAppService();
			System.out.println("------xml::"+stringBuilder.toString());
			boolean sucess = obj.getShipmentXML(stringBuilder.toString());
			System.out.println("------ sucess::"+sucess);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	
	private static final Logger logger = Logger.getLogger(SmartAppService.class);
	
	public boolean getShipmentXML(String xml){
		
		try {
//			File file = new File("E:\\text.xml");
			// if file doesnt exists, then create it
			

			//convert string to XML to get userid

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
			DocumentBuilder builder;  

			builder = factory.newDocumentBuilder();  
			Document doc = builder.parse( new InputSource( new StringReader( xml ) ) ); 
			XPath xPath =  XPathFactory.newInstance().newXPath();
			String username = xPath.compile("/Transmission/TransmissionHeader/UserName").evaluate(doc);
		
			Properties prop = PropertyUtil.getPropValues();	
			// create director if does not exist
			if(!new File(prop.getProperty("SHIPMENT_FILE_LOCATION")).exists()){
				new File(prop.getProperty("SHIPMENT_FILE_LOCATION")).mkdirs();
			}
			
			// create file using username
			File file = new File (prop.getProperty("SHIPMENT_FILE_LOCATION")+username+".xml");
			
			if (!file.exists()) {				
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(xml);
			bw.close();

			System.out.println("Done");			
			
			return true;
		} catch (IOException e) {
			logger.error("ERROR in getShipmentXML, while creating XML file from OTM response. "+e.getMessage());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			logger.error("ERROR in getShipmentXML, while creating XML file from OTM response. "+e.getMessage());
			e.printStackTrace();
		} catch (SAXException e) {
			logger.error("ERROR in getShipmentXML, while creating XML file from OTM response. "+e.getMessage());
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			logger.error("ERROR in getShipmentXML, while creating XML file from OTM response. "+e.getMessage());
			e.printStackTrace();
		} 
		return false;
	}

}
