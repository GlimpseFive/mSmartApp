package com.itc.msmart.implementation;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Client {

	public static void main(String[] args) {
    try {
            
            URL url = new URL("https://otm637.itcinfotech.com:4444/GC3/glog.integration.servlet.WMServlet");
                        
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

           
           BufferedReader reader = new BufferedReader(new FileReader("D:\\file.xml"));
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
           System.out.println("-----------"+stringBuilder.toString());
           
           con.setRequestMethod("POST");
           con.setDoInput(true);
           con.setDoOutput(true);
           DataOutputStream dos = new DataOutputStream(con.getOutputStream());
           dos.writeBytes(stringBuilder.toString());
           
            
           int responseCode = con.getResponseCode();
           BufferedReader in = new BufferedReader(new InputStreamReader(
                               con.getInputStream()));
           String inputLine;
           while ((inputLine = in.readLine()) != null) 
               System.out.println(inputLine);

           dos.flush();
           dos.close();
           in.close();

            
        }
        catch ( MalformedURLException ex ) {
            // a real program would need to handle this exception
        }
        catch ( IOException ex ) {
            // a real program would need to handle this exception
        }
    

	}

}
