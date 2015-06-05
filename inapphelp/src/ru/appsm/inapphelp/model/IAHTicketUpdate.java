//  HSTicketUpdate
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
import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class IAHTicketUpdate implements Serializable {
	
	public static final int TYPE_STAFF = 0;
	public static final int TYPE_USER = 1;

	@SerializedName("update_id")
	private Long updateId;
	
	@SerializedName("text")
	private String text;
	
	@SerializedName("update_by")
	private int updateBy; // 0 - Staff, 1 - User
	
	@SerializedName("update_time")
	private Date updateTime;
	
	@SerializedName("name")
	public String name;
	
	@SerializedName("attachments")
	private IAHAttachment[] attachments;
	
	// Date, Attachments etc will come here
	
	public static IAHTicketUpdate createUpdateByStaff(Long updateId, String name, String text, Date update_time, IAHAttachment[] attachments) {
		IAHTicketUpdate update = new IAHTicketUpdate();
		update.updateBy = TYPE_STAFF;
		update.text = text;
		update.updateId = updateId;
		update.name = name;
		update.updateTime = update_time;
		update.attachments = attachments;
		return update;
	}
	
	public static IAHTicketUpdate createUpdateByUser(Long updateId, String name, String text, Date update_time, IAHAttachment[] attachments) {
		IAHTicketUpdate update = new IAHTicketUpdate();
		update.updateBy = TYPE_USER;
		update.text = text;
		update.updateId = updateId;
		update.name = name;
		update.updateTime = update_time;
		update.attachments = attachments;
		return update;
	}
	
	public boolean isStaffUpdate() {
		return updateBy == TYPE_STAFF;
	}
	
	public boolean isUserUpdate() {
		return updateBy == TYPE_USER;
	}
	
	public String getText() {
		return text;
	}

	public Long getId() {
		return this.updateId;
	}

	public Date getUpdatedTime() {
		return updateTime;
	}
	
	public IAHAttachment[] getAttachments() {
		return attachments;
	}
	
	public boolean isAttachmentEmtpy() {
		if(attachments == null) {
			return true;
		} else {
			if(attachments.length == 0) {
				return true;
			}
		}
		return false;	
	}
	
 }
