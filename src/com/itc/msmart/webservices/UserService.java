package com.itc.msmart.webservices;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/UserService")
public class UserService {
	
	@POST
	@Path("/validate")
	public void validate(){
		System.out.println("--------user::");	
	}	

}
