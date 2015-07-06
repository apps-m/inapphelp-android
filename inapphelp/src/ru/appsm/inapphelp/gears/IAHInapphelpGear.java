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

package ru.appsm.inapphelp.gears;


import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import ru.appsm.inapphelp.logic.IAHGear;
import ru.appsm.inapphelp.logic.OnFetchedArraySuccessListener;
import ru.appsm.inapphelp.logic.OnFetchedSuccessListener;
import ru.appsm.inapphelp.logic.OnNewTicketFetchedSuccessListener;
import ru.appsm.inapphelp.model.IAHAttachment;
import ru.appsm.inapphelp.model.IAHKBItem;
import ru.appsm.inapphelp.model.IAHTicketUpdate;
import ru.appsm.inapphelp.model.IAHUploadAttachment;
import ru.appsm.inapphelp.model.IAHUser;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class IAHInapphelpGear extends IAHGear {
	private static final String TAG = IAHInapphelpGear.class.getSimpleName();

	private String company;
	private String app_key;
	private String app_id;
	private String section_id;

    // This are cached here so server call can be minimized and improve the speed of UI
    JSONArray allSectionsArray;

	public IAHInapphelpGear(String company, String app_id, String app_key) {

		assert company != null : "Company name cannot be null";
		assert app_id != null : "App id cannot be null";
		assert app_key != null : "App key cannot be null";

        this.company = company;
		this.app_key = app_key;
		this.app_id = app_id;
    }

    @Override
    public String getRefer () {
        return String.format("http://wwww.%s.inapphelp.com/", company);
    }

	// If user taps on a section, then section is send as a paremeter to the function
	@Override
	public void fetchKBArticle(String cancelTag, IAHKBItem section, RequestQueue queue, OnFetchedArraySuccessListener success, ErrorListener errorListener) {
        String url = getApiUrl().concat("faq/" + this.app_id);

        JsonArrayRequest request = new JsonArrayRequest(url, new InapphelpArrayBaseListener<JSONArray>(success, errorListener) {
            @Override
            public void onResponse(JSONArray articlesArray) {
                try {
                    IAHKBItem[] array = retrieveArticlesFromArray(articlesArray);
                    successCallback.onSuccess(array);
                } catch (JSONException e) {
                    errorListener.onErrorResponse(new VolleyError("Fail to parse JSON"));
                }
            }
        }, errorListener);

        request.setRetryPolicy(new DefaultRetryPolicy(0, 3, 1f));
        // to avoid server overload call

        request.setTag(cancelTag);
        queue.add(request);
        queue.start();
	}

	@Override
	public void registerNewUser(String cancelTag, String firstName, String lastname,
			String emailAddress, String userId, String userSecret, RequestQueue queue,OnFetchedSuccessListener success,
			ErrorListener error) {

        //TODO request to server for validate!!!!!
		IAHUser user = IAHUser.createNewUserWithDetails(firstName, lastname, emailAddress, userId, userSecret);
		success.onSuccess(user);
	}

	@Override
	public void fetchUpdateOnTicket(String cancelTag, Long from_time, IAHUser user, RequestQueue queue,
			OnFetchedArraySuccessListener success, ErrorListener errorListener) {

        QueryString query = new QueryString();
        query.addToUri("userid", user.getUserId());
        query.addToUri("appid", this.app_id);
        query.addToUri("appkey", this.app_key);
        query.addToUri("from", from_time.toString());
        if (user.getUserSecret() != null)
            query.addToUri("secretkey", user.getUserSecret());

        String url = getApiUrl().concat("chat/updates?" + query.toString());

        JsonArrayRequest request = new JsonArrayRequest(url, new InapphelpArrayBaseListener<JSONArray>(success, errorListener) {
            @Override
            public void onResponse(JSONArray updateArray) {
                try {
                    IAHTicketUpdate[] array =  retrieveTicketUpdateFromArray(updateArray);
                    successCallback.onSuccess(array);
                } catch (JSONException e) {
                    errorListener.onErrorResponse(new VolleyError("Fail to parse JSON"));
                }
            }
        }, errorListener);

        request.setRetryPolicy(new DefaultRetryPolicy(0,
                2, 1f));

        request.setTag(cancelTag);
        queue.add(request);
        queue.start();

	}

	@Override
	public void addReplyOnATicket(String cancelTag, String message, Map[] deviceInfo, IAHUploadAttachment[] attachments,  String pushToken, Long get_updates_from_time, IAHUser user,
			RequestQueue queue, OnFetchedArraySuccessListener success,
			ErrorListener errorListener) {

        Properties properties = new Properties();
        properties.put("name", user.getFullName());
        properties.put("email", user.getEmail());
        properties.put("platform", "android");
        properties.put("userid", user.getUserId());
        properties.put("appid", this.app_id);
        properties.put("appkey", this.app_key);

        if (user.getUserSecret() != null)
            properties.put("secretkey", user.getUserSecret());

        if (pushToken != null)
            properties.put("pushtoken", pushToken);


        properties.put("text", message);
        properties.put("from", get_updates_from_time.toString());

        properties.put("info", deviceInfo);

        String url = getApiUrl().concat("chat/submit");

        TicketPostRequest request = new TicketPostRequest(url, properties, attachments, new InapphelpArrayBaseListener<JSONArray>(success, errorListener) {
            @Override
            public void onResponse(JSONArray updateArray) {
                try {
                    IAHTicketUpdate[] array =  retrieveTicketUpdateFromArray(updateArray);
                    successCallback.onSuccess(array);
                } catch (JSONException e) {
                    errorListener.onErrorResponse(new VolleyError("Fail to parse JSON"));
                }
            }
        }, errorListener);

        request.setTag(cancelTag);
        queue.add(request);
        queue.start();
	}


    public String getApiUrl() {
		return "http://" + this.company + ".inapphelp.com/api/";
	}

	private abstract class InapphelpArrayBaseListener<T> implements Listener<T> {

		protected OnFetchedArraySuccessListener successCallback;
		protected ErrorListener errorListener;

		public InapphelpArrayBaseListener(OnFetchedArraySuccessListener success,
                                         ErrorListener errorListener) {
			this.successCallback = success;
			this.errorListener = errorListener;
		}

	}

	private abstract class InapphelpBaseListner<T> implements Listener<T> {

		protected OnFetchedSuccessListener successCallback;
		protected ErrorListener errorListener;

		public InapphelpBaseListner(OnFetchedSuccessListener success,
				ErrorListener errorListener) {
			this.successCallback = success;
			this.errorListener = errorListener;
		}

	}

	private abstract class CreateNewTicketSuccessListener<T> implements Listener<T>
	{

		protected OnNewTicketFetchedSuccessListener successListener;
		protected ErrorListener errorListener;

		public CreateNewTicketSuccessListener(OnNewTicketFetchedSuccessListener successListener,
				ErrorListener errorListener) {
			this.successListener = successListener;
			this.errorListener = errorListener;
		}
	}

    private class QueryString {

        private StringBuilder builder = new StringBuilder();

        public void addToUri(String name, String property) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(name).append("=").append(URLEncoder.encode(property));
        }

        @Override
        public String toString() {
            return builder.toString();
        }
    }

    private IAHTicketUpdate[] retrieveTicketUpdateFromArray(JSONArray updateArray) throws JSONException {
        ArrayList<IAHTicketUpdate> ticketUpdates = new ArrayList<IAHTicketUpdate>();
        int updateLen = updateArray.length();

        for (int i = 0; i < updateLen; i++) {
            JSONObject updateObject = updateArray.getJSONObject(i);
            IAHTicketUpdate update;
            IAHAttachment[] attachment = null;
            if (!updateObject.isNull("a") && !updateObject.isNull("am")) {
                attachment = new IAHAttachment[]{IAHAttachment.createAttachment(updateObject.getString("a"), "Attachment", updateObject.getString("am"))};
            }
            if (!updateObject.isNull("s")) {
                Long timestamp = updateObject.getLong("ts");
                String text = updateObject.getString("t");
                String s = updateObject.getString("s");
                Date date = new Date(timestamp);
                update = IAHTicketUpdate.createUpdateByStaff(timestamp, s, text, date, attachment);
            } else {
                Long timestamp = updateObject.getLong("ts");
                String text = updateObject.getString("t");
                Date date = new Date(timestamp);
                update = IAHTicketUpdate.createUpdateByUser(timestamp, "me", text, date, attachment);
            }
            if (update != null)
                ticketUpdates.add(update);
        }

        IAHTicketUpdate[] array = new IAHTicketUpdate[0];
        array = ticketUpdates.toArray(array);
        return  array;
    }

    private IAHKBItem[] retrieveArticlesFromArray(JSONArray articlesObject) throws JSONException {
        ArrayList<IAHKBItem> kbArticleArray = new ArrayList<IAHKBItem>();

        for (int j = 0; j < articlesObject.length(); j++) {
            JSONObject arrayObject = articlesObject.getJSONObject(j);
            IAHKBItem item = IAHKBItem.createForArticle(arrayObject.getString("_id"), arrayObject.getString("title").trim(), arrayObject.getString("text"));
            kbArticleArray.add(item);
        }

        IAHKBItem[] array = new IAHKBItem[0];
        array = kbArticleArray.toArray(array);
        return array;
    }

    private class TicketPostRequest extends Request<JSONArray> {

        /** Socket timeout in milliseconds for image requests */
        protected static final int TIMEOUT_MS = 0;

        /** Default number of retries for image requests */
        protected static final int MAX_RETRIES = 0;

        /** Default backoff multiplier for image requests */
        protected static final float BACKOFF_MULT = 1f;

        private Listener<JSONArray> mListener;

        private MultipartEntity entity;

        public TicketPostRequest(String url, Properties requestProperties, IAHUploadAttachment[] attachments_to_upload, Listener<JSONArray> listener,
                                 ErrorListener errorListener) {
            super(Method.POST, url, errorListener);
            mListener = listener;

            setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, BACKOFF_MULT));

            entity = new MultipartEntity(HttpMultipartMode.STRICT, null, Charset.forName("UTF-8"));

            // iter properties
            Enumeration<Object> enumKey = requestProperties.keys();
            while(enumKey.hasMoreElements()) {
                String key = (String) enumKey.nextElement();
                if (key.equals("info")) {
                    int count = 0;
                    Map<String, String>[] val = (Map<String, String>[]) requestProperties.get(key);
                    for (Map<String, String> info: val) {
                        for (Map.Entry<String, String> entry : info.entrySet()) {
                            String infoKey = entry.getKey();
                            String infoValue = entry.getValue();
                            try {
                                entity.addPart(key +"["+count+"]"+"["+infoKey+"]", new StringBody(infoValue));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                        count++;
                    }
                } else {
                    String val = requestProperties.getProperty(key);
                    try {
                        entity.addPart(key, new StringBody(val, Charset.forName("UTF-8")));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Adding attachments if any
            if (attachments_to_upload != null) {
                for (int i = 0; i < attachments_to_upload.length; i++) {
                    try {
                        entity.addPart("attachments", attachments_to_upload[i].generateStreamToUpload());
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "Attachment upload failed");
                        e.printStackTrace();
                    }
                }
            }

        }

        @Override
        public String getBodyContentType()
        {
            if (entity == null) {
                return super.getBodyContentType();
            }
            return entity.getContentType().getValue();
        }

        @Override
        public byte[] getBody() throws AuthFailureError
        {
            if (entity == null) {
                return super.getBody();
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try
            {
                entity.writeTo(bos);
            }
            catch (IOException e)
            {
                VolleyLog.e("IOException writing to ByteArrayOutputStream");
            }
            return bos.toByteArray();
        }

        @Override
        protected void deliverResponse(JSONArray response) {
            mListener.onResponse(response);
        }

        @Override
        protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString =
                        new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                return Response.success(new JSONArray(jsonString),
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }
    }
}
