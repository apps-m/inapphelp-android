//  NewIssueActivity
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

import ru.appsm.inapphelp.R;
import ru.appsm.inapphelp.fragments.IAHFragmentManager;
import ru.appsm.inapphelp.fragments.NewIssueFragment;

public class NewIssueActivity extends IAHActivityParent {

    public static final String EXTRAS_USER = "user";
    public static final String RESULT_USER = "result_user";

    public static final String EXTRAS_MESSAGE = "message";
    public static final String EXTRAS_ATTACHMENT = "attachment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iah_activity_new_issue);

        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            NewIssueFragment newIssueFragment = IAHFragmentManager.getNewIssueFragment();
            IAHFragmentManager.putFragmentInActivity(this, R.id.container, newIssueFragment, "Issue");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finishSafe();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void finishSafe() {
        Intent intent = new Intent();
        setResult(IAHActivityManager.resultCode_cancelled,intent);
        finish();
    }



}
