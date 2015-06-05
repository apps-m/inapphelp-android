//  HSSource
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

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import ru.appsm.inapphelp.IAHHelpDesk;
import ru.appsm.inapphelp.activities.IAHActivityManager;
import ru.appsm.inapphelp.fragments.IAHFragmentParent;
import ru.appsm.inapphelp.model.IAHAttachment;
import ru.appsm.inapphelp.model.IAHDraft;
import ru.appsm.inapphelp.model.IAHCachedUser;
import ru.appsm.inapphelp.model.IAHKBItem;
import ru.appsm.inapphelp.model.IAHUploadAttachment;
import ru.appsm.inapphelp.model.IAHUser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class IAHSource {
	private static final String TAG = IAHSource.class.getSimpleName();
	
	private static final String HELPSTACK_DIRECTORY = "helpstack";
	private static final String HELPSTACK_TICKETS_FILE_NAME = "tickets";
	private static final String HELPSTACK_TICKETS_USER_DATA = "user_credential";
	private static final String HELPSTACK_DRAFT = "draft";
	
    private static IAHSource singletonInstance = null;
    
    /**
    *
    * @param context
    * @return singleton instance of this class.
    */
	public static IAHSource getInstance(Context context) {
		if (singletonInstance == null) {
			synchronized (IAHSource.class) { // 1
				if (singletonInstance == null) // 2
				{
					Log.d(TAG, "New Instance");
					singletonInstance = new IAHSource(
							context.getApplicationContext()); // 3
				}		
			}
		}
		//As this singleton can be called even before gear is set, refreshing it
		singletonInstance.setGear(IAHHelpDesk.getInstance().getGear());

		return singletonInstance;
	}
    
	private IAHGear gear;
	private Context mContext;
	private RequestQueue mRequestQueue;

	private String user_id; //pending storage;
	private String user_secret; //pending storage;

	private IAHCachedUser cachedUser;

    private IAHDraft draftObject;
	
	private IAHSource(Context context) {
		this.mContext = context;

		this.user_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID); //default user_id
		this.user_secret = null; //default user secret;

		setGear(IAHHelpDesk.getInstance().getGear());
		mRequestQueue = IAHHelpDesk.getInstance().getRequestQueue();
		
		refreshFieldsFromCache();
	}

	public void setUserId (String user_id) {
		this.user_id = user_id;
		if (cachedUser.getUser() != null) {
			if (cachedUser.getUser().getUserId() == null || !cachedUser.getUser().getUserId().equals(user_id)){ //new user id
				doSaveNewUserPropertiesForGearInCache(null); //delete cached user.
			}
		}
	}

	public void setUserSecret(String user_secret) {
		this.user_secret = user_secret;
		if (cachedUser.getUser() != null) {
			if (cachedUser.getUser().getUserSecret() == null || !cachedUser.getUser().getUserSecret().equals(user_secret)) { //new user secret
				doSaveNewUserPropertiesForGearInCache(null); //delete cached user.
			}
		}
	}

	public void requestKBArticle(String cancelTag, IAHKBItem section, OnFetchedArraySuccessListener success, ErrorListener errorListener ) {
		if (gear.haveImplementedKBFetching()) {
			gear.fetchKBArticle(cancelTag, section, mRequestQueue,  new SuccessWrapper(success) {
				@Override
				public void onSuccess(Object[] successObject) {
					
					assert successObject != null  : "It seems requestKBArticle was not implemented in gear" ;

					// Do your work here, may be caching, data validation etc.
					super.onSuccess(successObject);
					
				}
			}, new ErrorWrapper("Fetching KB articles", errorListener));
		} else {
			try {
				IAHArticleReader reader = new IAHArticleReader(gear.getLocalArticleResourceId());
				success.onSuccess(reader.readArticlesFromResource(mContext));
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				throwError(errorListener, "Unable to parse local article XML");
			} catch (IOException e) {
				e.printStackTrace();
				throwError(errorListener, "Unable to read local article XML");
			}
		}	
	}

	public void checkForUserDetailsValidity(String cancelTag, String firstName, String lastName, String email, OnFetchedSuccessListener success, ErrorListener errorListener) {
		gear.registerNewUser(cancelTag, firstName, lastName, email, this.user_id, this.user_secret, mRequestQueue, success, new ErrorWrapper("Registering New User", errorListener));
	}
	
	public void createNewTicket(String cancelTag, IAHUser user, String message, IAHAttachment[] attachments, OnNewTicketFetchedSuccessListener successListener, ErrorListener errorListener) {
		gear.addReplyOnATicket(cancelTag, message, getDeviceInformation(mContext), convertAttachmentArrayToUploadAttachment(attachments), cachedUser.getToken(), 0l, user, mRequestQueue, new OnFetchedArraySuccessListenerWrapper(successListener, "Creating New Ticket", attachments), new ErrorWrapper("Creating New Ticket", errorListener));
	}
	
	public void requestUpdatesOnTicket(String cancelTag, Long from_time, IAHUser user, OnFetchedArraySuccessListener success, ErrorListener errorListener) {
		gear.fetchUpdateOnTicket(cancelTag, from_time, user, mRequestQueue, success, new ErrorWrapper("Fetching updates on Ticket", errorListener));
	}
	
	public void addReplyOnATicket(String cancelTag, String message, IAHAttachment[] attachments, Long get_updates_from_time,  IAHUser user,  OnFetchedArraySuccessListener success, ErrorListener errorListener) {
		gear.addReplyOnATicket(cancelTag, message, getDeviceInformation(mContext), convertAttachmentArrayToUploadAttachment(attachments), cachedUser.getToken(), get_updates_from_time, user, mRequestQueue, success, new ErrorWrapper("Adding reply to a ticket", errorListener));
	}

	public IAHGear getGear() {
		return gear;
	}

	private void setGear(IAHGear gear) {
		this.gear = gear;
	}
	
	public boolean isNewUser() {
		return cachedUser.getUser() == null || cachedUser.getUser().getUserId() == null || cachedUser.getUser().getEmail() == null || cachedUser.getUser().getFirstName() == null || cachedUser.getUser().getLastName() == null ;
	}
	
	public void refreshUser() {
		doReadUserFromCache();
	}

	public IAHUser getUser() {
		return cachedUser.getUser();
	}

	public String getPushToken() {
		return cachedUser.getToken();
	}

	public void setPushToken(String push_token) {
		cachedUser.setToken(push_token);
		doSaveNewUserPropertiesForGearInCache(cachedUser.getUser());
	}

    public String getDraftMessage() {
        if(draftObject != null) {
            return draftObject.getMessage();
        }
        return null;
    }

    public IAHUser getDraftUser() {
        if(draftObject != null) {
			return draftObject.getDraftUser();
        }
        return null;
    }

    public IAHAttachment[] getDraftAttachments() {
        if(draftObject != null) {
            return draftObject.getAttachments();
        }
        return null;
    }

    public String getDraftReplyMessage() {
        if(draftObject != null) {
            return draftObject.getDraftReplyMessage();
        }
        return null;
    }

    public IAHAttachment[] getDraftReplyAttachments() {
        if(draftObject != null) {
            return draftObject.getDraftReplyAttachments();
        }
        return null;
    }

    public void saveTicketDetailsInDraft(String message, IAHAttachment[] attachmentsArray) {
        doSaveTicketDraftForGearInCache(message, attachmentsArray);
    }

    public void saveUserDetailsInDraft(IAHUser user) {
		doSaveUserDraftForGearInCache(user);
    }

    public void saveReplyDetailsInDraft(String message, IAHAttachment[] attachmentsArray) {
		doSaveReplyDraftForGearInCache(message, attachmentsArray);
    }

	/***
	 * 
	 * Depending on the setting set on gear, it launches new ticket activity.
	 * 
	 * if email : launches email [Done]
	 * else: 
	 * if user logged in : launches user details [Done] 
	 * else: launches new ticket [Done]
	 * 
	 * @param fragment
	 * @param requestCode
	 */
	public void launchCreateNewTicketScreen(IAHFragmentParent fragment, int requestCode) {
		if(isNewUser()) {
			IAHActivityManager.startNewIssueActivity(fragment, null, requestCode);
		} else {
			IAHActivityManager.startIssueDetailActivity(fragment.getActivity(), getUser());
		}
	}

	private static String getDeviceInformation(Context activity) {
		StringBuilder builder = new StringBuilder();
		builder.append("Android version:");
		builder.append(Build.VERSION.SDK_INT);
		builder.append(",Device brand : ");
		builder.append(Build.MODEL);
		builder.append(",Application package:");
		try {
			builder.append(activity.getPackageManager().getPackageInfo(activity.getPackageName(),0).packageName);
		} catch (NameNotFoundException e) {
			builder.append("NA");
		}
		builder.append(",Application version:");
		try {
			builder.append(activity.getPackageManager().getPackageInfo(activity.getPackageName(),0).versionCode);
		} catch (NameNotFoundException e) {
			builder.append("NA");
		}

		return builder.toString();
	}

	public void cancelOperation(String cancelTag) {
		mRequestQueue.cancelAll(cancelTag);
	}
	
	
	/////////////////////////////////////////////////
	////////     Utility Functions  /////////////////
	/////////////////////////////////////////////////
	
	public void refreshFieldsFromCache() {
		// read the ticket data from cache and maintain here
		doReadUserFromCache();
        doReadDraftFromCache();
	}
	
	/**
	 * Opens a file and read its content. Return null if any error occured or file not found
	 * @param file
	 * @return
	 */
	private String readJsonFromFile(File file) {
		
		if (!file.exists()) {
			return null;
		}
		
		String json = null;
		FileInputStream inputStream;
		
		try {
			StringBuilder datax = new StringBuilder();
			inputStream = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader ( inputStream ) ;
            BufferedReader buffreader = new BufferedReader ( isr ) ;

            String readString = buffreader.readLine ( ) ;
            while ( readString != null ) {
                datax.append(readString);
                readString = buffreader.readLine ( ) ;
            }

            isr.close();

			json = datax.toString();
            return json;
            
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void writeJsonIntoFile (File file, String json) {
		FileOutputStream outputStream;

		try {
		  outputStream = new FileOutputStream(file);
		  outputStream.write(json.getBytes());
		  outputStream.close();
		} catch (Exception e) {
		  e.printStackTrace();
		}
	}
	
	public void doSaveNewUserPropertiesForGearInCache(IAHUser user) {
		
		cachedUser.setUser(user);
		
		Gson gson = new Gson();
		String userjson = gson.toJson(cachedUser);
		
		File userFile = new File(getProjectDirectory(), HELPSTACK_TICKETS_USER_DATA);
		
		writeJsonIntoFile(userFile, userjson);

	}

	protected void doReadUserFromCache() {
		
		File userFile = new File(getProjectDirectory(), HELPSTACK_TICKETS_USER_DATA);

		String json = readJsonFromFile(userFile);
		
		if (json == null) {
			cachedUser = new IAHCachedUser();
		}
		else {
			Gson gson = new Gson();
			cachedUser = gson.fromJson(json, IAHCachedUser.class);
		}
	}

    protected void doReadDraftFromCache() {
        File draftFile = new File(getProjectDirectory(), HELPSTACK_DRAFT);

        String json = readJsonFromFile(draftFile);

        if (json == null) {
        	draftObject = new IAHDraft();
        }
        else {
        	Gson gson = new Gson();
            draftObject = gson.fromJson(json, IAHDraft.class);
        }
    }

    protected void doSaveTicketDraftForGearInCache(String message, IAHAttachment[] attachmentsArray) {
        draftObject.setDraftMessage(message);
        draftObject.setDraftAttachments(attachmentsArray);

        writeDraftIntoFile();
    }

    protected void doSaveUserDraftForGearInCache(IAHUser user) {
        draftObject.setDraftUSer(user);
        writeDraftIntoFile();
    }

    protected void doSaveReplyDraftForGearInCache(String message, IAHAttachment[] attachmentsArray) {
        draftObject.setDraftReplyMessage(message);
        draftObject.setDraftReplyAttachments(attachmentsArray);

        writeDraftIntoFile();
    }


    private void writeDraftIntoFile() {
        Gson gson = new Gson();
        String draftJson = gson.toJson(draftObject);
        File draftFile = new File(getProjectDirectory(), HELPSTACK_DRAFT);

        writeJsonIntoFile(draftFile, draftJson);
    }

    protected File getProjectDirectory() {
		
		File projDir = new File(mContext.getFilesDir(), HELPSTACK_DIRECTORY);
		if (!projDir.exists())
		    projDir.mkdirs();
		
		return projDir;
	}

    public void clearTicketDraft() {
        saveTicketDetailsInDraft("", null);
    }

    public void clearReplyDraft() {
        saveReplyDetailsInDraft("", null);
    }

    private class NewTicketSuccessWrapper implements OnNewTicketFetchedSuccessListener
	{

		private OnNewTicketFetchedSuccessListener lastListner;

		public NewTicketSuccessWrapper(OnNewTicketFetchedSuccessListener lastListner) {
			this.lastListner = lastListner;
		}
		
		@Override
		public void onSuccess() {
			if (lastListner != null)
				lastListner.onSuccess();
		}
		
	}
	
	protected IAHUploadAttachment[] convertAttachmentArrayToUploadAttachment(IAHAttachment[] attachment) {
		
		IAHUploadAttachment[] upload_attachments = new IAHUploadAttachment[0];
		
		if (attachment != null && attachment.length > 0) {
			int attachmentCount = gear.getNumberOfAttachmentGearCanHandle();
			assert attachmentCount >=  attachment.length : "Gear cannot handle more than "+attachmentCount+" attachmnets";
			upload_attachments = new IAHUploadAttachment[attachment.length];
			for (int i = 0; i < upload_attachments.length; i++) {
				upload_attachments[i] = new IAHUploadAttachment(mContext, attachment[i]);
			}	
		}
		
		return upload_attachments;
	}
	
	private class SuccessWrapper implements OnFetchedArraySuccessListener
	{

		private OnFetchedArraySuccessListener lastListner;

		public SuccessWrapper(OnFetchedArraySuccessListener lastListner) {
			this.lastListner = lastListner;
		}
		
		@Override
		public void onSuccess(Object[] successObject) {
			if (lastListner != null)
				lastListner.onSuccess(successObject);
		}
		
	}

    private class OnFetchedArraySuccessListenerWrapper implements OnFetchedArraySuccessListener {

        private OnNewTicketFetchedSuccessListener listener;
        protected String message;
        protected IAHAttachment[] attachments;

        private OnFetchedArraySuccessListenerWrapper(OnNewTicketFetchedSuccessListener listener, String message, IAHAttachment[] attachments) {
            this.listener = listener;
            this.message = message;
            this.attachments = attachments;
        }


        @Override
        public void onSuccess(Object successObject[]) {
            if (this.listener != null) {
                this.listener.onSuccess();
            }
        }
    }
	
	private class ErrorWrapper implements ErrorListener {

		private ErrorListener errorListener;
		private String methodName;

		public ErrorWrapper(String methodName, ErrorListener errorListener) {
			this.errorListener = errorListener;
			this.methodName = methodName;
		}
		
		@Override
		public void onErrorResponse(VolleyError error) {
			printErrorDescription(methodName, error);
			this.errorListener.onErrorResponse(error);
		}
	}
	
	public static void throwError(ErrorListener errorListener, String error) {
		VolleyError volleyError = new VolleyError(error);
		printErrorDescription(null, volleyError);
		errorListener.onErrorResponse(volleyError);
	}
	
	private static void printErrorDescription (String methodName, VolleyError error)
	{
		if (methodName == null) {
			Log.e(IAHHelpDesk.LOG_TAG, "Error occurred in HelpStack");
		}
		else {
			Log.e(IAHHelpDesk.LOG_TAG, "Error occurred when executing " + methodName);
		}
		
		Log.e(IAHHelpDesk.LOG_TAG, error.toString());
		if (error.getMessage() != null) {
			Log.e(IAHHelpDesk.LOG_TAG, error.getMessage());
		}
		
		if (error.networkResponse != null && error.networkResponse.data != null) {
			try {
				Log.e(IAHHelpDesk.LOG_TAG, new String(error.networkResponse.data, "utf-8"));
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		error.printStackTrace();
	}
}
