package com.itc.msmart.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.itc.msmart.parser.XMLFileHandler;

public class ServerImp {
	
	public static void main(String[] args){
		Map<String, String[]> map = new HashMap<String, String[]>();
		String[] cust = {"CUST1"};
		String[] pass = {"CHANGEME"};
		String[] domain = {"MSMART"};
		String[] mobile = {"9215934823"};
		String[] url = {"https://otm637.itcinfotech.com:4444/GC3/glog.webserver.servlet.umt.Login"};
		String[] shipment_id = {"S20160704-0002"};		
		String[] event_type = {"Vessel Arrival"};		
		String[] current_location = {"INNSA"};
		String[] current_dt_time = {"11.8.2016 12:05 PM"};
		String[] remarks = {"Remark"};
		String[] service_provider = {"MATAJI_TRANSPORTERS"};
		String[] stopSequence = {"1"};
		String[] order_no = {"SO-20160504-0003"};
		String[] delivery_status = {"CONFIRMED_DELIVERY"};
		String[] stop_location = {"1 PUN"};
		
		
		map.put("user", cust);
		map.put("password", pass);
		map.put("domain", domain);
		map.put("mobile", mobile);
		map.put("url", url);
		map.put("shipment_id", shipment_id);
		map.put("event_type", event_type);
		map.put("current_location", current_location);
		map.put("current_dt_time", current_dt_time);
		map.put("remarks", remarks);
		map.put("service_provider", service_provider);
		map.put("stopSequence", stopSequence);
		map.put("order_no", order_no);
		map.put("delivery_status", delivery_status);
		map.put("stop_location", stop_location);
		
		ServerImp obj = new ServerImp();
		
//		obj.validateUser(map);
		obj.addShipmentEvent(map);
//		obj.setDeliveryStatus(map);
		
	}
	
	private String user_validation = "user_validation"; 
	private String addEvent = "addEvent";
	private String delivery_status = "delivery_status";
	private static final Logger logger = Logger.getLogger(ServerImp.class);
	
	public JSONObject callAction(Map<String, String[]> paramMap){
		String action = paramMap.get("action")[0];
		System.out.println("------action::"+action);
		JSONObject obj = new JSONObject();
		if (user_validation.equalsIgnoreCase(action)){
			obj = validateUser(paramMap);
		} else if (addEvent.equalsIgnoreCase(action)){			
			obj = addShipmentEvent(paramMap);
		} else if (delivery_status.equalsIgnoreCase(action)){
			obj = setDeliveryStatus(paramMap);
		}
		
		return obj;
		
	}
	
	public JSONObject validateUser(Map<String, String[]> paramMap){
		
		String user = "";
		if(paramMap.get("user")[0]!=null){
			user = paramMap.get("user")[0].trim();
		}
		
		String password = "";
		if(paramMap.get("password")[0]!=null){
			password = paramMap.get("password")[0].trim();
		}
				
		String domain = "";
		if(paramMap.get("domain")[0]!=null){
			domain = paramMap.get("domain")[0].trim();
		}
		
		String mobile = "";
		if(paramMap.get("mobile")[0] !=null){
			mobile = paramMap.get("mobile")[0].trim();
		}
		
		String url = ""; 
		if(paramMap.get("url")[0] !=null){
			url = paramMap.get("url")[0].trim() ;
		}
		
		System.out.println("user:"+user+"\n password:"+password+ "\n domain:"+domain+ "\n mobile:"+mobile+"\n url:"+url);
		
		logger.debug("user:"+user+"\n password:"+password+ "\n domain:"+domain+ "\n mobile:"+mobile+"\n url:"+url);
		
		XMLFileHandler handler = new XMLFileHandler();
		JSONObject json = new JSONObject();
		
		try {
			
			// Create login XML file
			String xml =  handler.createUserLoginXML(user, password, domain, mobile);
			// POST user login to OTM
			postXML2OTM(url, xml);
			// Read File from FTP location
			InputStream inputfile = readShipmentData(domain, user, mobile);			
			
			if(inputfile !=null){				
				// parse shipment data and send json to App
				json = handler.parseShipmentXML(inputfile);
			} else{			
				// return fail for invalid user
				json.put("info", "fail"); // TODO: may change JSON
			}	
			
		} catch (JSONException e) {
			logger.error("Error while creating user validation XML. "+e.getMessage());
			e.printStackTrace();
			return json;
		}
		
		return json;
		
	}
	
	private void postXML2OTM(String url, String xml){
		try {				
			String OTMURL = url.replaceFirst("glog.webserver.servlet.umt.Login", "glog.integration.servlet.WMServlet");			
			DefaultHttpClient httpClient = new DefaultHttpClient();			
			HttpPost postRequest = new HttpPost(OTMURL.trim());			
			StringEntity body = new StringEntity(xml);
			body.setContentType("application/raw");			
			postRequest.setEntity(body);
			HttpResponse esbResponse = httpClient.execute(postRequest);		

			if (esbResponse.getStatusLine().getStatusCode() == 200){
				System.out.println("user login file has been posted to OTM");
			}
		}
		catch ( MalformedURLException ex ) {
			ex.printStackTrace();
		}
		catch ( IOException ex ) {			
			ex.printStackTrace();
		}
	}
	
	public JSONObject addShipmentEvent(Map<String, String[]> paramMap){
		System.out.println("-----test::"+paramMap.get("url")[0]);
		String url = ""; 
		if(paramMap.get("url")[0] !=null){
			url = paramMap.get("url")[0].trim() ;
		}
		System.out.println("---------URL");
		JSONObject json = new JSONObject();
		try {
			XMLFileHandler handler = new XMLFileHandler();
			System.out.println("---------1");
			String xml = handler.addShipmentEventXML(paramMap);
			System.out.println("---------2");
			postXML2OTM(url, xml);
			json.put("info", "successful");
		} catch (JSONException e) {
			logger.error("Error while posting add event XML to OTM Server. "+e.getMessage());
			e.printStackTrace();			
		} 
		return json;
	}
	
	public JSONObject setDeliveryStatus(Map<String, String[]> paramMap){
		System.out.println("--------------setDeliveryStatus");
		String url = ""; 
		if(paramMap.get("url")[0] !=null){
			url = paramMap.get("url")[0].trim() ;
		}
		JSONObject json = new JSONObject();
		try {
			XMLFileHandler handler = new XMLFileHandler();
			String xml = handler.addOrderReleaseEventXML(paramMap);
			postXML2OTM(url, xml);
			json.put("info", "successful");
		} catch (JSONException e) {
			logger.error("Error while posting shipment event XML to OTM Server. "+e.getMessage());
			e.printStackTrace();			
		} 
		return json;
	}
	
	private InputStream readShipmentData(String domain, String user, String mobile_number){
		InputStream inputStream = null;
		FTPClient ftp = new FTPClient();
		try {
			
			Properties prop = PropertyUtil.getPropValues();
			String serverAddress = prop.getProperty("FTP_SERVER_ADDRESS").trim();
			String userId = prop.getProperty("userId").trim();
            String password = prop.getProperty("password").trim();
            String remoteDirectory = prop.getProperty("FTP_Directory").trim();
            int port = Integer.valueOf(prop.getProperty("FTP_SERVER_PORT").trim());
			
			ftp.connect(serverAddress, port);			
			if(!ftp.login(userId, password)){
				ftp.logout();
                return inputStream;
			}
			
			int reply = ftp.getReplyCode();
            //FTPReply stores a set of constants for FTP reply codes. 
            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftp.disconnect();
                return inputStream;
            }

            //enter passive mode
            ftp.enterLocalPassiveMode();
            //get system name
            System.out.println("Remote system is " + ftp.getSystemType());
            //change current directory
            ftp.changeWorkingDirectory(remoteDirectory);
            System.out.println("Current directory is " + ftp.printWorkingDirectory());

            //get list of filenames
            FTPFile[] ftpFiles = ftp.listFiles(); 
            ArrayList<String> fileNames = new ArrayList<String>();
            if (ftpFiles != null && ftpFiles.length > 0) {
                //loop thru files
                for (FTPFile file : ftpFiles) {
                    if (!file.isFile()) {
                        continue;
                    }
//                    System.out.println("-----------ALL Files::"+file.getName());
                    if(file.getName().startsWith((domain+"."+user).toUpperCase())){
                    	fileNames.add(file.getName());
                    	System.out.println("Current File is " + file.getName());
                    }
                }
            } else{
            	return inputStream;
            }
            
            // Read latest customer shipment data file
            Collections.sort(fileNames);
//            FileInputStream fio = new FileInputStream(new File(fileNames.get(fileNames.size()-1)));
//            BufferedInputStream io = new BufferedInputStream(fio);
            if(fileNames.size()>0){
            	inputStream = ftp.retrieveFileStream(fileNames.get(fileNames.size()-1));
            }
//            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//            StringBuilder sb = new StringBuilder();
//            String line ;
//            while ((line = br.readLine())!= null){
//            	sb.append(line);
//            }
//            System.out.println("------ shipment data::"+sb.toString());
//            logger.debug("Shipment data:"+sb.toString());
            
//            ftp.logout();
            ftp.disconnect();

            
            
			return inputStream;	
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
		      if(ftp.isConnected()) {
		          try {
		            ftp.disconnect();
		          } catch(IOException ioe) {
		            // do nothing
		          }
		        }
		}
		return inputStream;
		
	}


}
