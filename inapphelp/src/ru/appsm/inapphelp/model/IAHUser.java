//  HSUser
//
//Copyright (c) 2014 HelpStack (http://helpstack.io)
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.

package ru.appsm.inapphelp.model;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author Nalin Chhajer
 *
 */
public class IAHUser implements Serializable {

	@SerializedName("first_name")
	private String first_Name;
	
	@SerializedName("last_name")
	private String last_Name;
	
	@SerializedName("email")
	private String emailAddress;
	
	@SerializedName("user_id")
	private String user_id;

	@SerializedName("user_secret")
	private String user_secret;

	public IAHUser() {

	}
	
	public static IAHUser createNewUserWithDetails(String first_name, String last_name, String email, String user_id, String user_secret) {
		IAHUser user = new IAHUser();
		user.first_Name = first_name;
		user.last_Name = last_name;
		user.emailAddress = email;
		user.user_id = user_id;
		user.user_secret = user_secret;
		return user;
	}

	public String getFirstName() {
		return first_Name;
	}
	
	public String getLastName() {
		return last_Name;
	}
	
	public String getFullName() {
		return ""+first_Name+" "+last_Name;
	}
	
	public String getEmail() {
		return emailAddress;
	}
	
	public String getUserId() {
		return user_id;
	}

	public String getUserSecret() {
		return user_secret;
	}

	public static IAHUser appendCredentialOnUserDetail(IAHUser user, String userId, String access_token) {
		user.user_id = userId;
		return user;
	}
}
