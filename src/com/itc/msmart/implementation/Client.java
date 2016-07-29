package com.itc.msmart.implementation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class Client {	
	public static final String CONTENT_TYPE = "application/raw";

	public static void main(String[] args) {
		try {

			DefaultHttpClient httpClient = new DefaultHttpClient();
			Properties prop = PropertyUtil.getPropValues();
			System.out.println("-----------OTMURL::"+prop.getProperty("OTMURL"));
			HttpPost postRequest = new HttpPost(prop.getProperty("OTMURL"));


			BufferedReader reader = new BufferedReader(new FileReader("E:\\MSMART_CUST1.xml"));
			String         line = null;
			StringBuilder  stringBuilder = new StringBuilder();
			String         ls = System.getProperty("line.separator");

			try {
				while((line = reader.readLine()) != null) {
					stringBuilder.append(line);
					stringBuilder.append(ls);
				}

			} finally {
				reader.close();
			}
//			System.out.println("-----------"+stringBuilder.toString());

			StringEntity body = new StringEntity(stringBuilder.toString());
			body.setContentType(CONTENT_TYPE);

			postRequest.setEntity(body);
			HttpResponse esbResponse = httpClient.execute(postRequest);
//			System.out.println("----------esbResponse::"+esbResponse);
			if (esbResponse.getStatusLine().getStatusCode() == 200){
				System.out.println("user login file has been posted to OTM");
//				System.out.println("response:"+esbResponse.toString());
			}

		}
		catch ( MalformedURLException ex ) {
			ex.printStackTrace();
		}
		catch ( IOException ex ) {			
			ex.printStackTrace();
		}


	}

}
