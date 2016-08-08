package com.itc.msmart.webservices;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Source;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPException;

@WebServiceProvider(
		targetNamespace = "http://52.0.13.82:8080",
		serviceName = "MSmartAppWSService"
		)
public class MSmartAppWS {
	@Resource(type=Object.class)
	protected WebServiceContext wsContext;

	public boolean invoke(Source source){
		System.out.println("------------inside MSmartAppWS -------------"+source);
		try{  

			MessageContext messageContext = wsContext.getMessageContext();

			// Obtain the HTTP mehtod of the input request.
			HttpServletRequest servletRequest = (HttpServletRequest)messageContext.get(
					MessageContext.SERVLET_REQUEST);
			String httpMethod = servletRequest.getMethod();
			System.out.println("-------------httpMethod::"+httpMethod);
			if (httpMethod.equalsIgnoreCase("GET"))
			{
				System.out.println("------------------ MSmartAppWS GET--------------::"+messageContext.toString());

				String query = (String)messageContext.get(MessageContext.QUERY_STRING); 
				if (query != null && query.contains("lat=") &&
						query.contains("long=")) {
					return createSource(query); 
				} else {
					System.err.println("Query String = "+query);
					throw new HTTPException(404);	                 
				}
			} else {
				// This operation only supports "GET"
				throw new HTTPException(405);
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw new HTTPException(500);
		}

	}
	private boolean createSource(String str) throws Exception {
		return true;
		
	}

	static class Transmission {
	
	}

}
