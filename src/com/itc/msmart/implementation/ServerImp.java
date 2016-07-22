package com.itc.msmart.implementation;

import java.util.Map;

import javax.xml.transform.dom.DOMSource;

import org.json.JSONException;
import org.json.JSONObject;

import com.itc.msmart.parser.XMLFileHandler;

public class ServerImp {
	
	public JSONObject validateUser(Map<String, String[]> paramMap){
		System.out.println("------- user::"+paramMap.size());
		String username = paramMap.get("user")[0];
	
		XMLFileHandler handler = new XMLFileHandler();
		JSONObject json = new JSONObject();
		try {
			if ( username!=null ){
				DOMSource source =  handler.createUserLoginXML(username, "pass", "http://test", "90000");
				// TODO : validation from OTM Server using created XML
				if (username.equalsIgnoreCase("user1")){ //TODO: change with OTM Validation					
					json = handler.parseShipmentXML("E:\\Projects\\01 mSmart App\\Shipment_group.xml");
				}else{
					json.put("info", "fail"); // TODO: may change JSON
				}
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONObject validateUser1(Map<String, String[]> paramMap){
		System.out.println("------ param::"+paramMap);
		String username = paramMap.get("user")[0];
		String password = paramMap.get("password")[0];
		String login_url = paramMap.get("login_url")[0];
		String mobile_number = paramMap.get("mobile_number")[0];
		System.out.println("------- user::"+username);
		XMLFileHandler handler = new XMLFileHandler();
		JSONObject json = new JSONObject();
		
		try {
			if ( username!=null && password !=null && login_url!=null && mobile_number!=null){
				DOMSource source =  handler.createUserLoginXML(username, password, login_url, mobile_number);
				// TODO : validation from OTM Server using created XML
				if (username.equalsIgnoreCase("user1")){ //TODO: change with OTM Validation					
//					json = handler.parseShipmentXML("E:\\Projects\\01 mSmart App\\Shipment_group.xml");
					json = handler.parseShipmentXML("E:\\Projects\\01 mSmart App\\Shipment_group.xml");
				}else{
					json.put("info", "fail"); // TODO: may change JSON
				}
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
		
	}

}
