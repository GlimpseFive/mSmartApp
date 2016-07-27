package com.itc.msmart.webservices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SmartAppService {
	
	public boolean getShipmentXML(String xml){
		
		try {
			File file = new File("E:\\text.xml");
			// if file doesnt exists, then create it
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} 	
	}

}
