//  HSHelpStack
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

package ru.appsm.inapphelp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import ru.appsm.inapphelp.activities.IssueDetailActivity;
import ru.appsm.inapphelp.gears.IAHInapphelpGear;
import ru.appsm.inapphelp.logic.IAHGear;
import ru.appsm.inapphelp.logic.IAHSource;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * 
 * Contains methods and function to set Gear and show Help.
 * 
 * @author Nalin Chhajer
 *
 */
public class IAHHelpDesk {
	private static final String TAG = IAHHelpDesk.class.getSimpleName();
    public static final String LOG_TAG = IAHHelpDesk.class.getSimpleName();

    private static NotificationManager mNotificationManager;

    /**
     *
     * Init.
     *
     * @param context
     * @param company
     * @param app_id
     * @param app_key
     */

    public static void init(Context context, String company, String app_id, String app_key) {
        assert context != null : "Context cannot be null";
        assert company != null : "Company name cannot be null";
        assert app_id != null : "App id cannot be null";
        assert app_key != null : "App key cannot be null";

        synchronized (IAHHelpDesk.class) { // 1
            if (singletonInstance == null) // 2
            {
                singletonInstance = new IAHHelpDesk(context.getApplicationContext(), company, app_id, app_key);
                IAHSource iAHSource = IAHSource.getInstance(context.getApplicationContext());
            } else {
                Log.e(TAG, "Inapphelp has been already inited");
            }
        }
    }

    /**
     *
     * @return singleton instance of this class.
     */
	public static IAHHelpDesk getInstance() {
		if (singletonInstance == null)
            Log.e(TAG, "Helpstack not inited");

		return singletonInstance;
	}

    /**
     *
     * Open support for push data. Special cordova method.
     *
     * @param data
     * @param activity
     */
    public static void OpenSupportForData(JSONObject data, Activity activity) {
        Log.i(TAG, "handle push");
        if (data != null && data.has("secretkey") && data.has("userid") && data.has("appkey") && data.has("appid") && data.has("email") && data.has("message") && data.has("title") && data.has("notId") && data.has("msgId")) {
            try {
                Intent notificationIntent = new Intent(activity.getApplicationContext(), IssueDetailActivity.class);
                notificationIntent.putExtra("fromPush", true);
                notificationIntent.putExtra("userid", data.getString("userid"));
                notificationIntent.putExtra("appid", data.getString("appid"));
                notificationIntent.putExtra("appkey", data.getString("appkey"));
                notificationIntent.putExtra("secretkey", data.getString("secretkey"));
                notificationIntent.putExtra("email", data.getString("email"));
                notificationIntent.putExtra("msgId", data.getString("msgId"));
                activity.startActivity(notificationIntent);
            } catch (JSONException e ) {
                Log.i(TAG, "Fail to parse push data");
            }
        } else {
            Log.i(TAG, "Empty or wrong push data");
        }
    }

    /**
     *
     * Handle push notification.
     *
     * @param intent
     * @param context
     */
    public static void HandelPushIntentWithContext(Intent intent, Context context){
        Log.i(TAG, "handle push");
        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey("secretkey") && extras.containsKey("userid") && extras.containsKey("appkey") && extras.containsKey("appid") && extras.containsKey("email") && extras.containsKey("message") && extras.containsKey("title") && extras.containsKey("notId") && extras.containsKey("msgId")) {
            JSONObject data = new JSONObject();
            int notId = 1;
            try {
                notId = Integer.parseInt(extras.getString("notId"));
            } catch (NumberFormatException e ) {
                notId = 1;
            }
            try {
                data.put("notId", notId);
                data.put("userid", extras.getString("userid"));
                data.put("appid", extras.getString("appid"));
                data.put("appkey", extras.getString("appkey"));
                data.put("secretkey", extras.getString("secretkey"));
                data.put("email", extras.getString("email"));
                data.put("title", extras.getString("title"));
                data.put("message", extras.getString("message"));
                data.put("msgId",  extras.getString("msgId"));
                data.put("sound", extras.getString("sound"));
                IAHHelpDesk.BuildNotificationForDataWithContext(data, context);
            } catch (JSONException e) {
                Log.i(TAG, "Fail to parse push data");
            }
        } else {
            Log.i(TAG, "Empty or wrong push intent");
        }
    }

    /**
     *
     * Handle push notification. Cordova.
     *
     * @param data
     * @param context
     */
    public static void BuildNotificationForDataWithContext(JSONObject data, Context context){
        Log.i(TAG, "Create notifications");
        if (data != null && data.has("secretkey") && data.has("userid") && data.has("appkey") && data.has("appid") && data.has("email") && data.has("message") && data.has("title") && data.has("notId") && data.has("msgId")) {
            try {
                int notId = 1;
                try {
                    notId = data.getInt("notId");
                } catch (JSONException e ) {
                    notId = 1;
                }

                mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent notificationIntent = new Intent(context, IssueDetailActivity.class);
                notificationIntent.putExtra("fromPush", true);
                notificationIntent.putExtra("userid", data.getString("userid"));
                notificationIntent.putExtra("appid", data.getString("appid"));
                notificationIntent.putExtra("appkey", data.getString("appkey"));
                notificationIntent.putExtra("secretkey", data.getString("secretkey"));
                notificationIntent.putExtra("email", data.getString("email"));

                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(getApplicationIcon(context))
                        .setContentTitle(data.getString("title"))
                        .setContentText(data.getString("message"))
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent);

                if (data.getString("sound").equals("default")) {
                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
                }

                mNotificationManager.notify(notId, mBuilder.build());
            } catch (JSONException e) {
                Log.i(TAG, "Fail to parse push data");
            }
        } else {
            Log.i(TAG, "Empty or wrong push data");
        }
    }

    private static int getApplicationIcon(Context context) {
        int appIconResId = 0;

        appIconResId = getIconValue(context.getPackageName(), "ic_notify");

        if (appIconResId == -1)
            appIconResId = context.getApplicationInfo().icon;

        return appIconResId;
    }

    private static int getIconValue(String className, String iconName) {
        try {
            Class<?> clazz  = Class.forName(className + ".R$drawable");
            return (Integer) clazz.getDeclaredField(iconName).get(Integer.class);
        } catch (Exception ignore) {}
        return -1;
    }

    /**
     *
     * Set custom user id
     *
     * @param userId
     */
    public static void setUserId(String userId) {
        if (singletonInstance != null) {
            IAHSource.getInstance(singletonInstance.mContext).setUserId(userId);
        } else {
            Log.e(TAG, "Fail to set user id. Inapphelp not inited");
        }
    }

    /**
     *
     * Set custom user secret
     *
     * @param userSecret
     */
    public static void setUserSecret(String userSecret) {
        if (singletonInstance != null) {
            IAHSource.getInstance(singletonInstance.mContext).setUserSecret(userSecret);
        } else {
            Log.e(TAG, "Fail to set user secret. Inapphelp not inited");
        }
    }

    /**
     *
     * Set push token
     *
     * @param pushToken
     */
    public static void setPushToken(String pushToken) {
        if (singletonInstance != null) {
            IAHSource.getInstance(singletonInstance.mContext).setPushToken(pushToken);
        } else {
            Log.e(TAG, "Fail to set push token. Inapphelp not inited");
        }
    }

    /**
     *
     * @return gear which HelpStack has to use.
     */
	public IAHGear getGear() {
		return this.gear;
	}

    /**
     *
     * Starts a Help activity. It shows all FAQ and also let user report new issue if not found in FAQ.
     *
     * @param activity
     */
	public static void showHelp(Activity activity) {
        if (singletonInstance != null) {
            activity.startActivity(new Intent("ru.appsm.inapphelp.ShowHelp"));
        } else {
            Log.e(TAG, "Inapphelp not inited");
        }
	}

    /**
     * Call this, if you want to override gear method of article handling, in this case, you can provide articles locally and let HelpStack choose from it.
     *
     * It is light weight call. Call this after calling setGear.
     *
     * @param articleResId
     */
    public void overrideGearArticlesWithLocalArticlePath(int articleResId) {
        assert gear != null : "Some gear has to be set before overriding gear with local article path";
        gear.setNotImplementingKBFetching(articleResId);
    }


    /**
     *
     *
     *
     * @return RequestQueue object which was created during initialization. It is used by all the activity to store and perform network operation.
     */
	public RequestQueue getRequestQueue() {
		return mRequestQueue;
	}
	

    ////////////////////////////////////////////////////
    /////////////   Private Variables   ///////////////
    ///////////////////////////////////////////////////

    private static IAHHelpDesk singletonInstance = null;
    private Context mContext;

    private IAHGear gear;
    private RequestQueue mRequestQueue;
    private boolean showCredits;

    private IAHHelpDesk(Context context, final String company, String app_id, String app_key) {
        this.mContext = context;
        this.gear = new IAHInapphelpGear(company, app_id, app_key);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            HurlStack stack = new HurlStack() {
                @Override
                public HttpResponse performRequest(Request<?> request, Map<String, String> headers)
                        throws IOException, AuthFailureError {

                    headers.put("referer", String.format("http://www.%s.inapphelp.com/", company));
                    return super.performRequest(request, headers);
                }
            };
            mRequestQueue = Volley.newRequestQueue(context, stack);
        } else {
            HttpClientStack stack = new HttpClientStack(AndroidHttpClient.newInstance("volley/0")) {
                @Override
                public HttpResponse performRequest(Request<?> request, Map<String, String> headers)
                        throws IOException, AuthFailureError {

                    headers.put("referer", String.format("http://www.%s.inapphelp.com/", company));
                    return super.performRequest(request, headers);
                }
            };
            mRequestQueue = Volley.newRequestQueue(context, stack);
        }
    }
}

