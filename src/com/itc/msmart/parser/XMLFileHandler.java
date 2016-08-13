package com.itc.msmart.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class XMLFileHandler {
	
	public static void main (String[] args){
		XMLFileHandler obj = new XMLFileHandler();
//		obj.createUserLoginXML("user1", "pass", "MSMART", "98900");
		
		try {
			FileInputStream fio = new FileInputStream(new File("E:\\shipgrptdy.xml"));
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fio);
			JSONObject jobj = new JSONObject();
			
			jobj = obj.parseShipmentAttributes(jobj, doc);
//			jobj = obj.parseOrderAttributes(jobj, doc);
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}	
	
	private static final Logger logger = Logger.getLogger(XMLFileHandler.class);

	public JSONObject parseShipmentXML(InputStream inputFile){
		JSONObject jsonObject = new JSONObject();
		try {
			System.out.println("---- found shipment file");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();

			jsonObject = parseDashboardAttributes(jsonObject, doc);
			System.out.println("---- parseDashboardAttributes done");
			
			jsonObject = parseShipmentAttributes(jsonObject, doc);
			System.out.println("---- parseShipmentAttributes done");
			
			jsonObject = parseOrderAttributes(jsonObject, doc);
			System.out.println("---- parseOrderAttributes done");


		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
//			new File(filepath).deleteOnExit();
		}
		return jsonObject;
	}
	
	private JSONObject parseDashboardAttributes (JSONObject obj, Document doc){
		try {
			JSONObject dashboard = new JSONObject();
			NodeList nList = doc.getElementsByTagName("ShipmentGroupHeader");			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					dashboard.put("oreder_pending_delivery", eElement.getElementsByTagName("AttributeNumber1").item(0).getTextContent());
					dashboard.put("shipment_enroute", eElement.getElementsByTagName("AttributeNumber2").item(0).getTextContent());
					dashboard.put("pending_tenders", eElement.getElementsByTagName("AttributeNumber3").item(0).getTextContent());
					dashboard.put("shipment_to_picked", eElement.getElementsByTagName("AttributeNumber4").item(0).getTextContent());
				}
			}			
			obj.put("dashboard", dashboard);	
			
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return obj;
	}
	
	
	private JSONObject parseShipmentAttributes (JSONObject obj, Document doc){
		
		try {
		
			ArrayList<JSONObject> shipments = new ArrayList<JSONObject>();
			// list of shipments
			NodeList nList = doc.getElementsByTagName("Shipment");
			for (int i = 0; i < nList.getLength(); i++) {
				
				JSONObject shipment = new JSONObject();
				XPath xPath =  XPathFactory.newInstance().newXPath();
				String shipmentpath = (new StringBuilder()).append("/Transmission/TransmissionBody/GLogXMLElement/ShipmentGroup/Shipment").
						append("[").append(String.valueOf(i+1)).append("]").toString();
				
				String nodepath = null;
				
				// SET shipment ID
				String domain  =  xPath.compile(shipmentpath+ "/ShipmentHeader/ShipmentGid/Gid/DomainName").evaluate(doc);
				String xid = xPath.compile(shipmentpath+ "/ShipmentHeader/ShipmentGid/Gid/Xid").evaluate(doc);
				String shipmentId = domain+"."+xid;				
				shipment.put("shipmentId", shipmentId);
				System.out.println("------shipmentId::"+shipmentId);
				
				// SET carrier				
				String careerid = xPath.compile(shipmentpath + "/ShipmentHeader/ServiceProviderGid/Gid/Xid").evaluate(doc);
				shipment.put("careerid", careerid);
//				System.out.println("--------careerid::"+careerid);
				NodeList locations = (NodeList)xPath.compile(shipmentpath + "/Location").evaluate(doc, XPathConstants.NODESET);
				for(int j =0; j<locations.getLength(); j++){
					Node location = locations.item(j);
					if (location.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) location;
						String locationXid = eElement.getElementsByTagName("Xid").item(0).getTextContent();
						if (careerid.equalsIgnoreCase(locationXid)){							
							String carrier = "";
							if(eElement.getElementsByTagName("LocationName").item(0) != null){
								carrier = eElement.getElementsByTagName("LocationName").item(0).getTextContent();
							}							
							shipment.put("carrier", carrier);
							break;
						}
						
					}
				}
				
				// SET source
				nodepath = shipmentpath+"/ShipmentHeader/SourceLocationRef/LocationRef/LocationGid/Gid/Xid";				
				shipment.put("source",xPath.compile(nodepath).evaluate(doc));
				
				
				// SET destination
				nodepath = shipmentpath+"/ShipmentHeader/DestinationLocationRef/LocationRef/LocationGid/Gid/Xid";				
				shipment.put("destination",xPath.compile(nodepath).evaluate(doc));
				
				// SET Source Date Time
				nodepath = shipmentpath + "/ShipmentHeader/StartDt/GLogDate";
				String str = xPath.compile(nodepath).evaluate(doc);
				String startDate = (new StringBuilder()).append(str.substring(6, 8)).append(".").append(str.substring(4, 6)).append(".").append(str.substring(0, 4)).toString();
				String startTime = (new StringBuilder()).append(str.substring(8, 10)).append(":").append(str.substring(10, 12)).toString();				
				shipment.put("startDate", startDate);
				shipment.put("startTime", startTime);
				
				// SET Destination Time				
				NodeList stops = (NodeList)xPath.compile(shipmentpath + "/ShipmentStop").evaluate(doc, XPathConstants.NODESET);				
				String tempstop = xPath.compile(shipmentpath+"/ShipmentStop"+"["+String.valueOf(stops.getLength())+"]"+"/ArrivalTime/EventTime/EstimatedTime/GLogDate").evaluate(doc);				
				String destDate = (new StringBuilder()).append(tempstop.substring(6, 8)).append(".").append(tempstop.substring(4, 6)).append(".").append(tempstop.substring(0, 4)).toString();
				String destTime = (new StringBuilder()).append(tempstop.substring(8, 10)).append(":").append(tempstop.substring(10, 12)).toString();				
				shipment.put("destDate", destDate);
				shipment.put("destTime", destTime);	
				System.out.println("--------destDate::"+destDate);
				System.out.println("---------destTime::"+destTime);
				
				// SET Delivery Status
				nodepath = shipmentpath + "/ShipmentHeader/FlexFieldStrings/Attribute11";				
				shipment.put("delivery_status", xPath.compile(nodepath).evaluate(doc));				
				
				// SET Last Event
				nodepath = shipmentpath + "/ShipmentHeader/FlexFieldStrings/Attribute4";
				shipment.put("last_event", xPath.compile(nodepath).evaluate(doc));
				
				// SET Shipment Ref
				nodepath = shipmentpath + "/ShipmentHeader/FlexFieldStrings/Attribute13";				
				shipment.put("shipment_ref",xPath.compile(nodepath).evaluate(doc));
				
				//SET Truck type
				nodepath = shipmentpath + "/SEquipment/EquipmentGroupGid/Gid/Xid";				
				shipment.put("truck_type",xPath.compile(nodepath).evaluate(doc));
				
				//SET Truck number
				nodepath = shipmentpath + "/ShipmentHeader/FlexFieldStrings/Attribute1";				
				shipment.put("truck_number",xPath.compile(nodepath).evaluate(doc));
				
				//SET Driver Name
				nodepath = shipmentpath + "/ShipmentHeader/FlexFieldStrings/Attribute2";				
				shipment.put("driver_name",xPath.compile(nodepath).evaluate(doc));
				
				//SET Driver Contact
				nodepath = shipmentpath + "/ShipmentHeader/FlexFieldStrings/Attribute3";				
				shipment.put("driver_contact",xPath.compile(nodepath).evaluate(doc));
				
				//SET Map location details
				NodeList stoplist = (NodeList)xPath.compile(shipmentpath + "/ShipmentStop").evaluate(doc, XPathConstants.NODESET);				
				ArrayList<HashMap<String, String>> mapLocations = new ArrayList<HashMap<String, String>>();
				ArrayList<String> stopLocations = new ArrayList<String>();
				for(int j=0; j<stoplist.getLength(); j++){

					String ATA = xPath.compile(shipmentpath + "/ShipmentStop"+"["+String.valueOf(j+1)+"]"+"/ArrivalTime/EventTime/ActualTime/GLogDate").evaluate(doc);
					String ETA = xPath.compile(shipmentpath + "/ShipmentStop"+"["+String.valueOf(j+1)+"]"+"/ArrivalTime/EventTime/EstimatedTime/GLogDate").evaluate(doc);
					String ATD = xPath.compile(shipmentpath + "/ShipmentStop"+"["+String.valueOf(j+1)+"]"+"/DepartureTime/EventTime/ActualTime/GLogDate").evaluate(doc);
					String ETD = xPath.compile(shipmentpath + "/ShipmentStop"+"["+String.valueOf(j+1)+"]"+"/DepartureTime/EventTime/EstimatedTime/GLogDate").evaluate(doc);
					String location = xPath.compile(shipmentpath + "/ShipmentStop"+"["+String.valueOf(j+1)+"]"+"/LocationRef/LocationGid/Gid/Xid").evaluate(doc);

					HashMap<String, String> tempMap = new HashMap<String, String>();
					tempMap.put("ATA", ATA);
					tempMap.put("ETA", ETA);
					tempMap.put("ATD", ATD);
					tempMap.put("ETD", ETD);
					tempMap.put("location", location);
					mapLocations.add(tempMap);
					
					String stopSequence = xPath.compile(shipmentpath + "/ShipmentStop"+"["+String.valueOf(j+1)+"]"+"/StopSequence").evaluate(doc);
					String stopLocation = xPath.compile(shipmentpath + "/ShipmentStop"+"["+String.valueOf(j+1)+"]"+"/LocationRef/LocationGid/Gid/Xid").evaluate(doc);
					stopLocations.add(stopSequence+" "+stopLocation);	
//					System.out.println("---------::"+stopSequence+" "+stopLocation);
				}
				shipment.put("map_locations",mapLocations);
				
				Collections.sort(stopLocations);
				shipment.put("stop_location",stopLocations);
				System.out.println("------shipment::"+shipment);
				
				shipments.add(shipment);				
			}			
			
			obj.put("shipments", shipments);
			
		} catch (DOMException e) {
			logger.error("ERROR : parseShipmentAttributes, while paring shipment XML. "+e.getMessage());		
		} catch (XPathExpressionException e) {
			logger.error("ERROR : parseShipmentAttributes, while paring shipment XML. "+e.getMessage());
		} catch (JSONException e) {
			logger.error("ERROR : parseShipmentAttributes, while paring shipment XML. "+e.getMessage());
		}
		
		return obj;
	}

	
	private JSONObject parseOrderAttributes (JSONObject obj, Document doc){
		try {
			
			ArrayList<JSONObject> orders = new ArrayList<JSONObject>();
			// list of shipments
			NodeList nList = doc.getElementsByTagName("Shipment");
			for (int i = 0; i < nList.getLength(); i++) {
				JSONObject order = new JSONObject();
				XPath xPath =  XPathFactory.newInstance().newXPath();
				String shipmentpath = (new StringBuilder()).append("/Transmission/TransmissionBody/GLogXMLElement/ShipmentGroup/Shipment").
						append("[").append(String.valueOf(i+1)).append("]").toString();
				String[] ordernumbers = xPath.compile(shipmentpath+"/ShipmentHeader/FlexFieldStrings/Attribute19").evaluate(doc).split(","); 
				
				for(int j =0 ; j<ordernumbers.length; j++){
					String ordernumber = ordernumbers[j].substring(ordernumbers[j].indexOf(".")+1, ordernumbers[j].length());
					
					NodeList release = (NodeList)xPath.compile(shipmentpath + "/Release").evaluate(doc, XPathConstants.NODESET);
					
					for(int k=0; k<release.getLength(); k++){
						String releasepath = shipmentpath+"/Release"+"["+String.valueOf(k+1)+"]";
						String str = xPath.compile(releasepath +"/ReleaseGid/Gid/Xid").evaluate(doc);
						if (ordernumber.equalsIgnoreCase(str)){
							order.put("order_number", ordernumber);
//							System.out.println("-----ordernumber::"+ordernumber);
							order.put("customer_order_number", xPath.compile(releasepath+"/ReleaseHeader/ReleaseName").evaluate(doc));
//							System.out.println("-------customer_order_number::"+xPath.compile(releasepath+"/ReleaseHeader/ReleaseName").evaluate(doc));
							
							// GET Customer Name, contact person and contact number
							String tempxid = xPath.compile(releasepath+"/ShipToLocationRef/LocationRef/LocationGid/Gid/Xid").evaluate(doc);
//							System.out.println("----------tempxid::"+tempxid);
							
							NodeList locations = (NodeList)xPath.compile(shipmentpath + "/Location").evaluate(doc, XPathConstants.NODESET);
							
							for(int l=0; l<locations.getLength(); l++){
								String locationpath = shipmentpath+"/Location"+"["+String.valueOf(l+1)+"]";
								
								if(tempxid.equalsIgnoreCase(xPath.compile(locationpath+"/LocationGid/Gid/Xid").evaluate(doc)) &&
										"Y".equalsIgnoreCase(xPath.compile(locationpath+"/Contact/IsPrimaryContact").evaluate(doc))){									
									order.put("customer_name", xPath.compile(locationpath+"/Contact/FirstName").evaluate(doc));	
//									System.out.println("-------------customer name::"+xPath.compile(locationpath+"/Contact/FirstName").evaluate(doc));
									break;
								} else{
									order.put("customer_name", "");
								}
							}
							for(int l=0; l<locations.getLength(); l++){
								String locationpath = shipmentpath+"/Location"+"["+String.valueOf(l+1)+"]";
									
								if (tempxid.equalsIgnoreCase(xPath.compile(locationpath+"/LocationGid/Gid/Xid").evaluate(doc))){
									order.put("contact_person", xPath.compile(locationpath+"/Contact/FirstName").evaluate(doc));
									order.put("contact_number", xPath.compile(locationpath+"/Contact/CellPhone").evaluate(doc));
									break;
								}
							}
							
							// GET number of packages							
							order.put("number_of_packages", xPath.compile(releasepath+"/TotalPackagedItemCount").evaluate(doc));
//							System.out.println("-----number_of_packages::"+xPath.compile(releasepath+"/TotalPackagedItemCount").evaluate(doc));
							
							// GET confirm delivery
							order.put("confirm_delivery", xPath.compile(releasepath+"/ReleaseHeader/FlexFieldStrings/Attribute5").evaluate(doc));
//							System.out.println("--------confirm_delivery::"+xPath.compile(releasepath+"/ReleaseHeader/FlexFieldStrings/Attribute5").evaluate(doc));
							
//							System.out.println("---- JSON order::"+order);
							// ADD order
							orders.add(order);	
							
						}
					}
					
				}

							
			}
//			System.out.println("------------orders::"+orders.size());
			obj.put("orders", orders);
		}catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return obj;
	}
	

	public String createUserLoginXML(String user, String password, String domain, String mobile){
		
		StringWriter writer = new StringWriter();
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			// root element
			Element rootElement = doc.createElement("Transmission");
			doc.appendChild(rootElement);

			//  TransmissionHeader element
			Element header = doc.createElement("TransmissionHeader");
			rootElement.appendChild(header);

			//  UserName element
			Element username = doc.createElement("UserName");
			username.setTextContent(domain+"."+user);
			header.appendChild(username);

			//  Password element
			Element pass = doc.createElement("Password");
			pass.setTextContent(password);
			header.appendChild(pass);


			//  TransmissionBody element
			Element body = doc.createElement("TransmissionBody");
			rootElement.appendChild(body);

			//  GLogXMLElement element
			Element gLog = doc.createElement("GLogXMLElement");
			body.appendChild(gLog);

			//  Contact element
			Element contact = doc.createElement("Contact");
			gLog.appendChild(contact);

			//  ContactGid element
			Element contactGrid = doc.createElement("ContactGid");
			contact.appendChild(contactGrid);


			//  Gid element
			Element gid = doc.createElement("Gid");
			contactGrid.appendChild(gid);


			//  DomainName element
			Element domainName = doc.createElement("DomainName");
			domainName.setTextContent(domain);
			gid.appendChild(domainName);


			//  Xid element
			Element xid = doc.createElement("Xid");
			xid.setTextContent(user+"_"+mobile);
			gid.appendChild(xid);

			//  TransactionCode element
			Element transactionCode = doc.createElement("TransactionCode");
			transactionCode.setTextContent("IU");
			contact.appendChild(transactionCode);


			//  FirstName element
			Element firstName = doc.createElement("FirstName");
			contact.appendChild(firstName);

			//  LastName element
			Element lastName = doc.createElement("LastName");
			contact.appendChild(lastName);

			//  Telex element
			Element telex = doc.createElement("Telex");
			telex.setTextContent("Y");
			contact.appendChild(telex);

			//  CellPhone element
			Element cellPhone = doc.createElement("CellPhone");
			cellPhone.setTextContent(mobile);
			contact.appendChild(cellPhone);

			//  FlexFieldStrings element
			Element flex = doc.createElement("FlexFieldStrings");
			contact.appendChild(flex);

			//  Attribute1 element
			Element attb1 = doc.createElement("Attribute1");
			attb1.setTextContent("Y");
			flex.appendChild(attb1);


			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			
			
			DOMSource source = new DOMSource(doc);
			
//			StreamResult result = new StreamResult(new File("D:\\file.xml"));
			StreamResult result = new StreamResult(writer);
			
			transformer.transform(source, result);
			
			logger.debug("DEBUG: createUserLoginXML, user login XML :"+writer.toString());
			
		} catch (DOMException e) {
			logger.error("ERROR in createUserLoginXML, while creating XML for user login validation. "+e.getMessage());			
		} catch (ParserConfigurationException e) {
			logger.error("ERROR in createUserLoginXML, while creating XML for user login validation. "+e.getMessage());
		} catch (TransformerConfigurationException e) {
			logger.error("ERROR in createUserLoginXML, while creating XML for user login validation. "+e.getMessage());			
		} catch (TransformerException e) {
			logger.error("ERROR in createUserLoginXML, while creating XML for user login validation. "+e.getMessage());		
		}
		
		return writer.toString();

	}


	public String addShipmentEventXML(Map<String, String[]> paramMap){
		
		String user = paramMap.get("user")[0];
		String password = paramMap.get("password")[0];
		String domain = paramMap.get("domain")[0];		
		String shipment_id = paramMap.get("shipment_id")[0];
//		String truck_no = paramMap.get("truck_no")[0];
		String event_type = paramMap.get("event_type")[0];
		String stop_location =  paramMap.get("stop_location")[0];
		String current_location = paramMap.get("current_location")[0];
		String current_dt_time = paramMap.get("current_dt_time")[0];
		String remarks = paramMap.get("remarks")[0];
		String service_provider = paramMap.get("service_provider")[0];
		String stopSequence =  stop_location.substring(0, stop_location.indexOf(" "));
		current_dt_time = changeDateFormat(current_dt_time, "yyyyMMddHHmmss");
		System.out.println("---user::"+user);
		System.out.println("password::"+password);
		System.out.println("domain::"+domain);
		System.out.println("shipment_id::"+shipment_id);
		System.out.println("event_type::"+event_type);
		System.out.println("stop_location:"+stop_location);
		System.out.println("current_location::"+current_location);
		System.out.println("current_dt_time::"+current_dt_time);
		System.out.println("remarks::"+remarks);
		System.out.println("service_provider::"+service_provider);
		System.out.println("stopSequence::"+stopSequence);
		if ("Gate In".equalsIgnoreCase(event_type)){
			event_type = "GIN";
		} else if("Gate Out".equalsIgnoreCase(event_type)){
			event_type = "GOT";
		} else if ("Reported at Gate".equalsIgnoreCase(event_type)){
			event_type = "RAG";
		} else if ("Vessel Arrival".equalsIgnoreCase(event_type)){
			event_type = "VA";
		} else if ("Vessel Departure".equalsIgnoreCase(event_type)){
			event_type = "VD";
		} 
		
		StringWriter writer = new StringWriter();
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			// root element
			Element rootElement = doc.createElement("Transmission");
			doc.appendChild(rootElement);

			//  TransmissionHeader element
			Element header = doc.createElement("TransmissionHeader");
			rootElement.appendChild(header);

			//  UserName element
			Element username = doc.createElement("UserName");
			username.setTextContent(domain+"."+user);
			header.appendChild(username);

			//  Password element
			Element pass = doc.createElement("Password");
			pass.setTextContent(password);
			header.appendChild(pass);

			//  TransmissionBody element
			Element body = doc.createElement("TransmissionBody");
			rootElement.appendChild(body);

			//  GLogXMLElement element
			Element gLog = doc.createElement("GLogXMLElement");
			body.appendChild(gLog);

			//  Contact element
			Element shipmentStatus = doc.createElement("ShipmentStatus");
			gLog.appendChild(shipmentStatus);

			// ServiceProviderAlias element
			Element serviceProviderAlias = doc.createElement("ServiceProviderAlias");
			shipmentStatus.appendChild(serviceProviderAlias);
			
			// ServiceProviderAliasQualifierGid element
			Element serviceProviderAliasQualifierGid = doc.createElement("ServiceProviderAliasQualifierGid");
			serviceProviderAlias.appendChild(serviceProviderAliasQualifierGid);
			
			// Gid element
			Element gid = doc.createElement("Gid");
			serviceProviderAliasQualifierGid.appendChild(gid);
			
			// Xid element
			Element xid = doc.createElement("Xid");
			xid.setTextContent("GLOG");
			gid.appendChild(xid);

			// ServiceProviderAliasValue element
			Element serviceProviderAliasValue = doc.createElement("ServiceProviderAliasValue");
			serviceProviderAliasValue.setTextContent(domain+"."+service_provider); 
			serviceProviderAlias.appendChild(serviceProviderAliasValue);
			
			// ShipmentRefnum element
			Element shipmentRefnum = doc.createElement("ShipmentRefnum");
			shipmentStatus.appendChild(shipmentRefnum);
			
			// ShipmentRefnumQualifierGid element
			Element shipmentRefnumQualifierGid = doc.createElement("ShipmentRefnumQualifierGid");
			shipmentRefnum.appendChild(shipmentRefnumQualifierGid);
						
			// Gid element
			Element gid1 = doc.createElement("Gid");
			shipmentRefnumQualifierGid.appendChild(gid1);

			// Xid element
			Element xid1 = doc.createElement("Xid");
			xid1.setTextContent("GLOG");
			gid1.appendChild(xid1);			

			// ShipmentRefnumValue element
			Element shipmentRefnumValue = doc.createElement("ShipmentRefnumValue");
			shipmentRefnumValue.setTextContent(shipment_id);
			shipmentRefnum.appendChild(shipmentRefnumValue);
			
			// ShipmentStatusType element
			Element shipmentStatusType = doc.createElement("ShipmentStatusType");
			shipmentStatusType.setTextContent("Shipment");
			shipmentStatus.appendChild(shipmentStatusType);
			
			// StatusLevel element
			Element statusLevel = doc.createElement("StatusLevel");
			shipmentStatus.appendChild(statusLevel);			
			
			// StatusCodeGid element
			Element statusCodeGid = doc.createElement("StatusCodeGid");
			shipmentStatus.appendChild(statusCodeGid);
			
			// Gid element
			Element gid2 = doc.createElement("Gid");
			statusCodeGid.appendChild(gid2);

			// Xid element
			Element xid2 = doc.createElement("Xid");
			xid2.setTextContent(event_type);
			gid2.appendChild(xid2);		
			
			// ResponsiblePartyGid element
			Element responsiblePartyGid = doc.createElement("ResponsiblePartyGid");
			shipmentStatus.appendChild(responsiblePartyGid);
			
			// Gid element
			Element gid3 = doc.createElement("Gid");
			responsiblePartyGid.appendChild(gid3);

			// Xid element
			Element xid3 = doc.createElement("Xid");
			xid3.setTextContent("PLANNER");
			gid3.appendChild(xid3);				

			// EventDt element
			Element eventDt = doc.createElement("EventDt");
			shipmentStatus.appendChild(eventDt);

			// GLogDate element
			Element gLogDate = doc.createElement("GLogDate");
			gLogDate.setTextContent(current_dt_time); 
			eventDt.appendChild(gLogDate);	
			
			//SSRemarks element
			Element SSRemarks = doc.createElement("SSRemarks");
			SSRemarks.setTextContent(remarks);
			shipmentStatus.appendChild(SSRemarks);
			
			//FlexFieldStrings element
			Element flexFieldStrings = doc.createElement("FlexFieldStrings");
			shipmentStatus.appendChild(flexFieldStrings);
			
			//Attribute2 element
			Element attribute2 = doc.createElement("Attribute2");
			attribute2.setTextContent(current_location);
			flexFieldStrings.appendChild(attribute2);

			// SSStop element
			Element SSStop = doc.createElement("SSStop");
			shipmentStatus.appendChild(SSStop);

			// SSStopSequenceNum element
			Element SSStopSequenceNum = doc.createElement("SSStopSequenceNum");
			SSStopSequenceNum.setTextContent(stopSequence); 
			SSStop.appendChild(SSStopSequenceNum);			
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			
			
			DOMSource source = new DOMSource(doc);
			
//			StreamResult result = new StreamResult(new File("D:\\file.xml"));
			StreamResult result = new StreamResult(writer);
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(source, result);
			
			logger.debug("DEBUG: createUserLoginXML, user login XML :"+writer.toString());
			
			System.out.println("-------- writer::"+writer.toString());

		} catch (DOMException e) {
			logger.error("ERROR in createUserLoginXML, while creating XML for user login validation. "+e.getMessage());	
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			logger.error("ERROR in createUserLoginXML, while creating XML for user login validation. "+e.getMessage());
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			logger.error("ERROR in createUserLoginXML, while creating XML for user login validation. "+e.getMessage());
			e.printStackTrace();
		} catch (TransformerException e) {
			logger.error("ERROR in createUserLoginXML, while creating XML for user login validation. "+e.getMessage());
			e.printStackTrace();
		}
		
		return writer.toString();

	}
	
	public String addOrderReleaseEventXML(Map<String, String[]> paramMap){
				
		String order_no = paramMap.get("order_no")[0];
		String tmp_current_dt_time = paramMap.get("current_dt_time")[0];
		String current_location = paramMap.get("current_location")[0];
		String remarks = paramMap.get("remarks")[0];
		String user = paramMap.get("user")[0];
		String password = paramMap.get("password")[0];
		String domain = paramMap.get("domain")[0];
		String delivery_status = paramMap.get("delivery_status")[0];
		String current_dt_time = changeDateFormat(tmp_current_dt_time, "yyyyMMddHHmmss");
		System.out.println("------------addOrderReleaseEventXML---------------");
		System.out.println("---order_no::"+order_no);
		System.out.println("---current_dt_time::"+current_dt_time);
		System.out.println("---current_location::"+current_location);
		System.out.println("---remarks::"+remarks);
		System.out.println("---user::"+user);
		System.out.println("---password::"+password);
		System.out.println("---domain::"+domain);
		System.out.println("---delivery_status::"+delivery_status);
		System.out.println("---");
		
		StringWriter writer = new StringWriter();
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			// root element
			Element rootElement = doc.createElement("Transmission");
			doc.appendChild(rootElement);

			//  TransmissionHeader element
			Element header = doc.createElement("TransmissionHeader");
			rootElement.appendChild(header);

			//  UserName element
			Element username = doc.createElement("UserName");
			username.setTextContent(domain+"."+user);
			header.appendChild(username);

			//  Password element
			Element pass = doc.createElement("Password");
			pass.setTextContent(password);
			header.appendChild(pass);

			//  TransmissionBody element
			Element body = doc.createElement("TransmissionBody");
			rootElement.appendChild(body);

			//  GLogXMLElement element
			Element gLog = doc.createElement("GLogXMLElement");
			body.appendChild(gLog);

			//  Contact element
			Element shipmentStatus = doc.createElement("ShipmentStatus");
			gLog.appendChild(shipmentStatus);
			
			//  ReleaseRefnum element
			Element releaseRefnum = doc.createElement("ReleaseRefnum");
			shipmentStatus.appendChild(releaseRefnum);


			//  ReleaseRefnumQualifierGid element
			Element releaseRefnumQualifierGid = doc.createElement("ReleaseRefnumQualifierGid");
			releaseRefnum.appendChild(releaseRefnumQualifierGid);

			//  Gid element
			Element gid = doc.createElement("Gid");
			releaseRefnumQualifierGid.appendChild(gid);

			//  Xid element
			Element xid = doc.createElement("Xid");
			xid.setTextContent("GLOG");
			gid.appendChild(xid);

			//  ReleaseRefnum element
			Element releaseRefnumValue = doc.createElement("ReleaseRefnumValue");
			releaseRefnumValue.setTextContent(domain+"."+order_no);
			releaseRefnum.appendChild(releaseRefnumValue);

			//  StatusLevel element
			Element statusLevel = doc.createElement("StatusLevel");
			statusLevel.setTextContent("ORDER");
			shipmentStatus.appendChild(statusLevel);
			
			//  StatusCodeGid element
			Element statusCodeGid = doc.createElement("StatusCodeGid");
			shipmentStatus.appendChild(statusCodeGid);

			//  Gid element
			Element gid1 = doc.createElement("Gid");
			statusCodeGid.appendChild(gid1);

			//  DomainName element
			Element domainName = doc.createElement("DomainName");
			domainName.setTextContent(domain);
			gid1.appendChild(domainName);

			//  Xid element
			Element xid1 = doc.createElement("Xid");
			xid1.setTextContent(delivery_status);
			gid1.appendChild(xid1);
			
			//  EventDt element
			Element eventDt = doc.createElement("EventDt");
			shipmentStatus.appendChild(eventDt);

			//  GLogDate element
			Element gLogDate = doc.createElement("GLogDate");
			gLogDate.setTextContent(current_dt_time);
			eventDt.appendChild(gLogDate);

			//  tZId element
//			Element tZId = doc.createElement("TZId");
//			eventDt.appendChild(tZId);
			
			//  TZOffset element
//			Element tZOffset = doc.createElement("TZOffset");
//			eventDt.appendChild(tZOffset);
			
			//  EventDt element
			Element SSRemarks = doc.createElement("SSRemarks");
			SSRemarks.setTextContent(remarks);
			shipmentStatus.appendChild(SSRemarks);
		
			//  EventDt element
			Element flexFieldStrings = doc.createElement("FlexFieldStrings");			
			shipmentStatus.appendChild(flexFieldStrings);
			
			//Attribute2 element
			Element attribute2 = doc.createElement("Attribute2");
			attribute2.setTextContent(current_location+"||"+changeDateFormat(tmp_current_dt_time,"yyyy-MM-dd HH:mm:ss"));
			flexFieldStrings.appendChild(attribute2);
			
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			
			
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(writer);	
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(source, result);
			
			logger.debug("DEBUG: createUserLoginXML, user login XML :"+writer.toString());
			
			System.out.println("-------- writer::"+writer.toString());

		} catch (DOMException e) {
			logger.error("ERROR in createUserLoginXML, while creating XML for user login validation. "+e.getMessage());			
		} catch (ParserConfigurationException e) {
			logger.error("ERROR in createUserLoginXML, while creating XML for user login validation. "+e.getMessage());
		} catch (TransformerConfigurationException e) {
			logger.error("ERROR in createUserLoginXML, while creating XML for user login validation. "+e.getMessage());			
		} catch (TransformerException e) {
			logger.error("ERROR in createUserLoginXML, while creating XML for user login validation. "+e.getMessage());		
		}		
	
		return writer.toString();
	}
	
	private String changeDateFormat(String date, String newFormat){

		String OLD_FORMAT = "dd.M.yyyy h:mm a"; 
//		String NEW_FORMAT = "yyyyMMddHHmmss";
		SimpleDateFormat olddate = new SimpleDateFormat(OLD_FORMAT);
		SimpleDateFormat newdate = new SimpleDateFormat(newFormat);
		Date old = new Date();
		try {
			old = olddate.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("-----test::"+newdate.format(old));
		return newdate.format(old);
		
	}



}
