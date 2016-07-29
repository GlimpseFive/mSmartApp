package com.itc.msmart.implementation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.itc.msmart.parser.XMLFileHandler;

public class ServerImp {
	private String user_validation = "user_validation"; 
	private String addEvent = "addEvent";
	private String delivery_status = "delivery_status";
	private static final Logger logger = Logger.getLogger(ServerImp.class);
	
	public JSONObject callAction(Map<String, String[]> paramMap){
		String action = paramMap.get("action")[0];
		JSONObject obj = new JSONObject();
		if (user_validation.equalsIgnoreCase(action)){
			obj = validateUser(paramMap);
		} else if (addEvent.equalsIgnoreCase(action)){
			obj = addEvent(paramMap);
		} else if (delivery_status.equalsIgnoreCase(action)){
			obj = setDeliveryStatus(paramMap);
		}
		
		return obj;
		
	}
	
//	public JSONObject validateUser(Map<String, String[]> paramMap){
//		System.out.println("------- user::"+paramMap.size());
//		String username = paramMap.get("user")[0];
//		System.out.println("------- password::"+paramMap.get("password")[0]);
//	
//		XMLFileHandler handler = new XMLFileHandler();
//		JSONObject json = new JSONObject();
//		try {
//			if ( username!=null ){
//				DOMSource source =  handler.createUserLoginXML(username, "pass", "http://test", "90000");
//				// TODO : validation from OTM Server using created XML
//				if (username.equalsIgnoreCase("user1")){ //TODO: change with OTM Validation					
//					json = handler.parseShipmentXML("E:\\Projects\\01 mSmart App\\Shipment_group.xml");
//				}else{
//					json.put("info", "fail"); // TODO: may change JSON
//				}
//				
//			}
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return json;
//	}
	
	public JSONObject validateUser(Map<String, String[]> paramMap){
		
		String user = "";
		if(paramMap.get("user")[0]!=null){
			user = paramMap.get("user")[0];
		}
		
		String password = "";
		if(paramMap.get("password")[0]!=null){
			password = paramMap.get("password")[0];
		}
				
		String domain = "";
		if(paramMap.get("domain")[0]!=null){
			domain = paramMap.get("domain")[0];
		}
		
		String mobile = "";
		if(paramMap.get("mobile")[0] !=null){
			mobile = paramMap.get("mobile")[0];
		}
		
		String url = ""; 
		if(paramMap.get("url")[0] !=null){
			url = paramMap.get("url")[0] ;
		}
		
		logger.debug("user:"+user+"\n password:"+password+ "\n domain:"+domain+ "\n mobile:"+mobile+"\n url:"+url);
		
		XMLFileHandler handler = new XMLFileHandler();
		JSONObject json = new JSONObject();
		
		try {
			
			// Create login XML file
			String xml =  handler.createUserLoginXML(user, password, domain, mobile);
			
			// POST user login to OTM
			postXML2OTM(url, xml);
			
			
			// TODO : validation from OTM Server using created XML
			if (user.equalsIgnoreCase("CUST1") && domain.equalsIgnoreCase("MSMART")){ 
				
				//TODO: change with OTM Validation
//				json = handler.parseShipmentXML("E:\\Projects\\01 mSmart App\\Shipment_group.xml");
				Properties prop = PropertyUtil.getPropValues();	
				String filepath = prop.getProperty("SHIPMENT_FILE_LOCATION")+domain+"."+user+".xml";
				json = handler.parseShipmentXML(filepath);
				
			}else{
				json.put("info", "fail"); // TODO: may change JSON
			}

			
		} catch (JSONException e) {
			logger.error("Error while creating user validation XML. "+e.getMessage());
			return json;
		}
		
		return json;
		
	}
	
	private void postXML2OTM(String url, String xml){
		try {

			DefaultHttpClient httpClient = new DefaultHttpClient();
			Properties prop = PropertyUtil.getPropValues();			
			HttpPost postRequest = new HttpPost(prop.getProperty("OTMURL"));

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
	
	public JSONObject addEvent(Map<String, String[]> paramMap){
		System.out.println("--------action:"+paramMap.get("action")[0]);
		System.out.println("--------shipment_id:"+paramMap.get("shipment_id")[0]);
		System.out.println("--------truck_no:"+paramMap.get("truck_no")[0]);
		System.out.println("--------event_type:"+paramMap.get("event_type")[0]);
		System.out.println("--------stop_location:"+paramMap.get("stop_location")[0]);
		System.out.println("--------current_location:"+paramMap.get("current_location")[0]);
		System.out.println("--------current_dt_time:"+paramMap.get("current_dt_time")[0]);
		System.out.println("--------remarks:"+paramMap.get("remarks")[0]);
		JSONObject json = new JSONObject();
		try {
			json.put("info", "successful");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return json;
	}
	
	public JSONObject setDeliveryStatus(Map<String, String[]> paramMap){
		System.out.println("--------action:"+paramMap.get("action")[0]);
		System.out.println("--------order_no:"+paramMap.get("order_no")[0]);
		System.out.println("--------current_dt_time:"+paramMap.get("current_dt_time")[0]);
		System.out.println("--------current_location:"+paramMap.get("current_location")[0]);
		System.out.println("--------remarks:"+paramMap.get("remarks")[0]);
		JSONObject json = new JSONObject();
		try {
			json.put("info", "successful");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return json;
	}

}
