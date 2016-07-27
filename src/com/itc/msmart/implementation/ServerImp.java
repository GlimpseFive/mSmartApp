package com.itc.msmart.implementation;

import java.util.Map;

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
		
		String role = ""; 
		if(paramMap.get("role")[0] !=null){
			role = paramMap.get("role")[0] ;
		}
		
		logger.debug("user:"+user+"\n password:"+password+ "\n domain:"+domain+ "\n mobile:"+mobile+"\n role:"+role);
		
		XMLFileHandler handler = new XMLFileHandler();
		JSONObject json = new JSONObject();
		
		try {
			
			// Create login XML file
			String xml =  handler.createUserLoginXML(user, password, domain, mobile);
			// TODO : validation from OTM Server using created XML
			if (user.equalsIgnoreCase("user1")){ //TODO: change with OTM Validation
				json = handler.parseShipmentXML("E:\\Projects\\01 mSmart App\\Shipment_group.xml");
			}else{
				json.put("info", "fail"); // TODO: may change JSON
			}

			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error("Error while creating user validation XML. "+e.getMessage());
			return json;
		}
		
		return json;
		
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
