//  HSActivityManager
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

package ru.appsm.inapphelp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import ru.appsm.inapphelp.fragments.IAHFragmentParent;
import ru.appsm.inapphelp.model.IAHAttachment;
import ru.appsm.inapphelp.model.IAHKBItem;
import ru.appsm.inapphelp.model.IAHUser;

/**
 * 
 * Contains a function call to start any activity used in HelpStack
 * 
 * @author Nalin Chhajer
 *
 */
public class IAHActivityManager {
	
	public final static int resultCode_sucess = Activity.RESULT_OK;
	public final static int resultCode_cancelled = Activity.RESULT_CANCELED;

	public static void startHomeActivity(Context context) {
		Intent intent = new Intent(context, HomeActivity.class);
		context.startActivity(intent);
	}

	public static void startNewIssueActivity(IAHFragmentParent context, IAHUser user, int requestCode) {
		Intent intent = new Intent(context.getActivity(), NewIssueActivity.class);
        if(user != null) {
            intent.putExtra(NewIssueActivity.EXTRAS_USER, user);
        }
		context.startActivityForResult(intent, requestCode);
	}

	public static void startSectionActivity(IAHFragmentParent context, IAHKBItem kbItem, int requestCode) {
		Intent intent = new Intent(context.getActivity(), SectionActivity.class);
		intent.putExtra(SectionActivity.EXTRAS_SECTION_ITEM, kbItem);
		context.startActivityForResult(intent, requestCode);
	}
	
	public static void startArticleActivity(IAHFragmentParent context, IAHKBItem kbItem, int requestCode) {
		Intent intent = new Intent(context.getActivity(), ArticleActivity.class);
		intent.putExtra(ArticleActivity.EXTRAS_ARTICLE_ITEM, kbItem);
		context.startActivityForResult(intent, requestCode);
	}

	public static void startNewUserActivity(IAHFragmentParent context, int requestCode, String message, IAHAttachment[] attachmentArray) {
		Intent intent = new Intent(context.getActivity(), NewUserActivity.class);
        intent.putExtra(NewIssueActivity.EXTRAS_MESSAGE, message);
        if (attachmentArray != null) {
        	Gson json = new Gson();
        	intent.putExtra(NewIssueActivity.EXTRAS_ATTACHMENT, json.toJson(attachmentArray));
        }
		context.startActivityForResult(intent, requestCode);
	}
	
	public static void startIssueDetailActivity(Activity context, IAHUser user) {
		Intent intent = new Intent(context, IssueDetailActivity.class);
		intent.putExtra(IssueDetailActivity.EXTRAS_USER, user);
		context.startActivity(intent);
	}
	
	public static void startImageAttachmentDisplayActivity(Activity context, String url, String title) {
		Intent intent = new Intent(context, ImageAttachmentDisplayActivity.class);
		intent.putExtra(ImageAttachmentDisplayActivity.EXTRAS_STRING_URL, url);
		intent.putExtra(ImageAttachmentDisplayActivity.EXTRAS_TITLE, title);
		context.startActivity(intent);
	}
	
	public static void finishSafe(Activity context) {
		Intent intent = new Intent();
		context.setResult(IAHActivityManager.resultCode_cancelled,intent);
		context.finish();
	}
	
	public static void sendSuccessSignal(Activity context, Intent result) {
		context.setResult(IAHActivityManager.resultCode_sucess,result);
		context.finish();
	}
}
