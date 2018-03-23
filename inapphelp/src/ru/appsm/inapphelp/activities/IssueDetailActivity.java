//  IssueDetailActivity
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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import ru.appsm.inapphelp.IAHHelpDesk;
import ru.appsm.inapphelp.fragments.IAHFragmentManager;
import ru.appsm.inapphelp.fragments.IssueDetailFragment;
import ru.appsm.inapphelp.model.IAHUser;

public class IssueDetailActivity extends IAHActivityParent {
	
	public static final String EXTRAS_USER = "user";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ru.appsm.inapphelp.R.layout.iah_activity_issue_detail);
		if (savedInstanceState == null) {
			IssueDetailFragment mIssueDetailFragment = IAHFragmentManager.getIssueDetailFragment();
			IAHFragmentManager.putFragmentInActivity(this, ru.appsm.inapphelp.R.id.container, mIssueDetailFragment, "IssueDetail");
			Intent intent = getIntent();
			IAHUser user;
			if (intent.getBooleanExtra("fromPush", false)) {
				user = IAHUser.createNewUserWithDetails(null, null, intent.getExtras().getString("email"), intent.getExtras().getString("userid"), intent.getExtras().getString("secretkey"));
				IAHHelpDesk.init(this, intent.getExtras().getString("company"), intent.getExtras().getString("appid"), intent.getExtras().getString("appkey"));
				IAHHelpDesk.setUserId(intent.getExtras().getString("userid"));

				if (intent.getExtras().containsKey("secretkey") && !intent.getExtras().getString("secretkey").equals(""))
					IAHHelpDesk.setUserSecret(intent.getExtras().getString("secretkey"));
			} else {
				user = (IAHUser) intent.getExtras().getSerializable(EXTRAS_USER);
			}
			mIssueDetailFragment.setUser(user);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(ru.appsm.inapphelp.R.menu.iah_issue_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		}
		if(id == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

}
