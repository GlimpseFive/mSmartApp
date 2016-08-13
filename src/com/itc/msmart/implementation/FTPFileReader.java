package com.itc.msmart.implementation;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FTPFileReader {
	public static void main(String[] args){
		FTPFileReader reader = new FTPFileReader();
		reader.readShipmentData("MSMART", "CUST1", "9564589411");
	}
	
	public String readShipmentData(String domain, String user, String mobile_number){
		StringBuilder sb = new StringBuilder();
		try {
			FTPClient ftp = new FTPClient();
			Properties prop = PropertyUtil.getPropValues();
			String serverAddress = prop.getProperty("FTP_SERVER_ADDRESS").trim();
//			String serverAddress = "Otm637.itcinfotech.com";
			String userId = prop.getProperty("userId").trim();
            String password = prop.getProperty("password").trim();
            String remoteDirectory = prop.getProperty("FTP_Directory").trim();
//            String remoteDirectory= "/u01/MobileApp";
//            int port = Integer.valueOf(prop.getProperty("FTP_SERVER_PORT").trim());
            int port = 21;
			
			ftp.connect(serverAddress, port);
			System.out.println("-----------loging::"+ftp.login(userId, password));
			if(!ftp.login(userId, password)){
				ftp.logout();
                return sb.toString();
			}
			
			int reply = ftp.getReplyCode();
            //FTPReply stores a set of constants for FTP reply codes. 
            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftp.disconnect();
                return sb.toString();
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
                    System.out.println("File is " + file.getName());
                    if(file.getName().startsWith(domain+"."+user)){
                    	fileNames.add(file.getName());
                    }
                   
                   
//                    //get output stream
//                    OutputStream output;
//                    output = new FileOutputStream(localDirectory + "/" + file.getName());
//                    //get the file from the remote system
//                    ftp.retrieveFile(file.getName(), output);
//                    //close output stream
//                    output.close();
                   
                    
                   
                }
            }
            
            // Read latest customer shipment data file
            Collections.sort(fileNames);
//            FileInputStream fio = new FileInputStream(new File(fileNames.get(fileNames.size()-1)));
//            BufferedInputStream io = new BufferedInputStream(fio);
            System.out.println("--------"+fileNames.size());
            InputStream inputStream = ftp.retrieveFileStream(fileNames.get(fileNames.size()-1));
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            
            String line ;
            while ((line = br.readLine())!= null){
            	sb.append(line);
            }
            
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("E:\\Shipment.xml")));
            bw.write(sb.toString());
            bw.close();
            
//            ftp.logout();
            ftp.disconnect();

            
            
			return sb.toString();	
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
		
	}

}
