//  HSGear
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

package ru.appsm.inapphelp.logic;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import ru.appsm.inapphelp.model.IAHKBItem;
import ru.appsm.inapphelp.model.IAHUploadAttachment;
import ru.appsm.inapphelp.model.IAHUser;


/**
 * @author Nalin Chhajer
 *
 */
public abstract class IAHGear {
	
	public IAHGear() {

	}

    public void init () {

    }

	public String getRefer () {
		return "";
	}

	/**
     *
     * @param cancelTag
     * @param section
     * @param queue
     * @param success
     * @param errorListener
     */
	public void fetchKBArticle(String cancelTag, IAHKBItem section, RequestQueue queue,  OnFetchedArraySuccessListener success, ErrorListener errorListener )
	{
		errorListener.onErrorResponse(new VolleyError("Not implemented method"));
	}

    /**
     *
     * @param cancelTag
     * @param firstName
     * @param lastname
	 * @param emailAddress
	 * @param userId
	 * @param userSecret
     * @param queue
     * @param success
     * @param errorListener
     */
	public void registerNewUser(String cancelTag, String firstName, String lastname, String emailAddress, String userId, String userSecret, RequestQueue queue, OnFetchedSuccessListener success, ErrorListener errorListener)
	{
		success.onSuccess(IAHUser.createNewUserWithDetails(firstName, lastname, emailAddress, userId, userSecret));
	}
	
	/**
	 *
     * HSUploadAttachment.getAttachment() can contain mime/Type and filename
	 * 
	 * @param user
	 * @param body
	 * @param deviceInfo
	 * @param successListener
	 * @param errorListener
	 */
	public void createNewTicket(String cancelTag, String pushToken, IAHUser user, String body, String deviceInfo, IAHUploadAttachment[] attachments, RequestQueue queue, OnNewTicketFetchedSuccessListener successListener, ErrorListener errorListener ) {
		errorListener.onErrorResponse(new VolleyError("Not implemented method"));
	}

    /**
     *
     * @param cancelTag
     * @param get_updates_from_time
     * @param user
     * @param queue
     * @param success
     * @param errorListener
     */
	public void fetchUpdateOnTicket(String cancelTag, Long get_updates_from_time, IAHUser user, RequestQueue queue, OnFetchedArraySuccessListener success, ErrorListener errorListener)
	{
		errorListener.onErrorResponse(new VolleyError("Not implemented method"));
	}
	
	/***
	 * 
	 * @param message
	 * @param deviceInfo
	 * @param user
	 * @param queue
	 * @param success
	 * @param errorListener
	 */
	public void addReplyOnATicket(String cancelTag, String message, String deviceInfo, IAHUploadAttachment[] attachments, String pushToken, Long get_updates_from_time, IAHUser user, RequestQueue queue, OnFetchedArraySuccessListener success, ErrorListener errorListener) {
		errorListener.onErrorResponse(new VolleyError("Not implemented method"));
	}

    /**
     * Set this parameter, if gear is not implementing handling of Issues. Doing this, default email client will be open with given support Email Address.
     * Then there is no need to implement issues fetching related methods.
     *
     * @default  it is considered that gear is gonna implement ticket fetching.
     *
     * @param companySupportEmailAddress
     */
	public void setNotImplementingTicketsFetching(String companySupportEmailAddress) {
		implementsTicketFetching = false;
		this.companySupportEmailAddress = companySupportEmailAddress;
	}

    /**
     *
     * @return Company support email address set in {@link #setNotImplementingTicketsFetching(java.lang.String) setNotImplementingTicketsFetching}
     */
    public String getCompanySupportEmailAddress() {
        return companySupportEmailAddress;
    }
    /**
     * Returns if gear have implemented Ticket Fetching. Modify this parameter using {@link #setNotImplementingTicketsFetching(java.lang.String) setNotImplementingTicketsFetching}
     *
     * @default true
     *
     * @return
     */
    public boolean haveImplementedTicketFetching() {
        return implementsTicketFetching;
    }
    /**
     * Set this parameter, if gear is not implementing handling of FAQ. Doing this, FAQ will be fetched from article path.
     * Then there is no need to implement issues fetching related methods.
     *
     * @default  it is considered that gear is gonna implement email fetching.
     *
     * @param articleResid
     */
	public void setNotImplementingKBFetching (int articleResid) {
		implementsKBFetching = false;
		this.articleResid = articleResid;
	}

    /**
     *
     * @return Local article id set in {@link #setNotImplementingKBFetching(int) setNotImplementingKBFetching}
     */
    public int getLocalArticleResourceId() {
        return articleResid;
    }

    /**
     * Returns if gear have implemented KB Fetching. Modify this parameter using {@link #setNotImplementingKBFetching(int) setNotImplementingKBFetching}
     *
     * @default true
     *
     * @return
     */
    public boolean haveImplementedKBFetching() {
        return implementsKBFetching;
    }

    /**
     *
     * If this parameter is set, message written by user in chat screen and new issue screen will be returned in HTML format.
     *
     * @default false
     *
     * @param htmlEnabled
     */
	public void uploadMessageAsHtmlString(boolean htmlEnabled) {
		this.supportHtmlMessage = htmlEnabled;
	}

    /**
     * returns if gear can upload message as html string.
     *
     * @default false
     *
     * @return
     */
	public boolean canUplaodMessageAsHtmlString() {
		return supportHtmlMessage;
	}


    /**
     *
     * Sets maximum number of attachment gears can handle.
     *
     * @default 1
     *
     * @param numberOfAttachmentGearCanHandle
     */
	public void setNumberOfAttachmentGearCanHandle (int numberOfAttachmentGearCanHandle) {
		this.numberOfAttachmentGearCanHandle = numberOfAttachmentGearCanHandle;
	}

    /**
     *
     * @return maximum number of attachment gear can handle.
     * @default is 1
     */
	public int getNumberOfAttachmentGearCanHandle() {
		return numberOfAttachmentGearCanHandle;
	}


    /**
     * if true, gear don't have to return back HSTicketUpdate object, after a reply is been added.
     *
     * @default: false, means it is necessary for the gear to return HSTicketUpdate object, after a reply is been added.
     *
     * @param canIgnore
     */
    public void ignoreTicketUpdateInformationAfterAddingReply(boolean canIgnore) {
        this.ignoreTicketUpdateInformationAfterAddingReply = canIgnore;
    }

    /**
     *
     * @return if the HSTicketUpdate object, after a reply is added should be used in UI.
     *
     */
    public boolean canIgnoreTicketUpdateInformationAfterAddingReply() {
        return this.ignoreTicketUpdateInformationAfterAddingReply;
    }


    ////////////////////////////////////////////////////
    /////////////   Private Variables   ///////////////
    ///////////////////////////////////////////////////

	private int numberOfAttachmentGearCanHandle = 1;
	
	// If this is true, we don't call kb article functions, will open email app is required.
	private boolean implementsTicketFetching = true;
	
	private boolean implementsKBFetching = true;
	
	private int articleResid;
	
	private String companySupportEmailAddress;
	
	private boolean supportHtmlMessage = false;

    private boolean ignoreTicketUpdateInformationAfterAddingReply = false;


}
