//  NewUserFragment
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

package ru.appsm.inapphelp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import ru.appsm.inapphelp.activities.IAHActivityManager;
import ru.appsm.inapphelp.activities.NewIssueActivity;
import ru.appsm.inapphelp.logic.IAHSource;
import ru.appsm.inapphelp.logic.IAHUtils;
import ru.appsm.inapphelp.logic.OnFetchedSuccessListener;
import ru.appsm.inapphelp.logic.OnNewTicketFetchedSuccessListener;
import ru.appsm.inapphelp.model.IAHAttachment;
import ru.appsm.inapphelp.model.IAHUser;

public class NewUserFragment extends IAHFragmentParent {

    private static final String RESULT_USER = NewIssueActivity.RESULT_USER;
    private static final String EXTRAS_MESSAGE = NewIssueFragment.EXTRAS_MESSAGE;
    private static final String EXTRAS_ATTACHMENT = NewIssueFragment.EXTRAS_ATTACHMENT;
    private static final String EXTRAS_FIRST_NAME = "first_name";
    private static final String EXTRAS_LAST_NAME = "last_name";
    private static final String EXTRAS_EMAIL = "email";

    private String message;
    private IAHAttachment[] attachmentArray;

    public NewUserFragment() {

	}
	
	EditText firstNameField, lastNameField, emailField;
	
	IAHSource gearSource;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
		
		setHasOptionsMenu(true);
		
		View rootView = inflater.inflate(ru.appsm.inapphelp.R.layout.iah_fragment_new_user, container, false);
		
		this.firstNameField = (EditText) rootView.findViewById(ru.appsm.inapphelp.R.id.firstname);
		this.lastNameField = (EditText) rootView.findViewById(ru.appsm.inapphelp.R.id.lastname);
		this.emailField = (EditText) rootView.findViewById(ru.appsm.inapphelp.R.id.email);

        
		
		return rootView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(EXTRAS_FIRST_NAME, firstNameField.getText().toString());
		outState.putString(EXTRAS_LAST_NAME, lastNameField.getText().toString());
		outState.putString(EXTRAS_EMAIL, emailField.getText().toString());
		outState.putString(EXTRAS_MESSAGE, message);
		if (attachmentArray != null) {
			Gson gson = new Gson();
			outState.putString(EXTRAS_ATTACHMENT, gson.toJson(attachmentArray));
		}
	}

    @Override
    public void onPause() {
        super.onPause();

        IAHUser userDetails = IAHUser.createNewUserWithDetails(
				this.firstNameField.getText().toString(),
				this.lastNameField.getText().toString(),
				this.emailField.getText().toString(),
				null,
				null);

        gearSource.saveUserDetailsInDraft(userDetails);
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle args = savedInstanceState;
        if (args == null) {
            args = getArguments();
        }
        
        if (args != null) {
            message = args.getString(EXTRAS_MESSAGE, null);
            if (args.containsKey(EXTRAS_ATTACHMENT)) {
            	String json = args.getString(EXTRAS_ATTACHMENT);
            	Gson gson = new Gson();
            	attachmentArray = gson.fromJson(json, IAHAttachment[].class);
            }
            
            String first_name = args.getString(EXTRAS_FIRST_NAME, null);
            if (first_name != null) firstNameField.setText(first_name);
            String last_name = args.getString(EXTRAS_LAST_NAME, null);
            if (last_name != null) lastNameField.setText(last_name);
            String email = args.getString(EXTRAS_EMAIL, null);
            if (email != null) emailField.setText(email);
        }
		
		gearSource = IAHSource.getInstance(getActivity());

        IAHUser draftUser = gearSource.getDraftUser();
        if (draftUser != null) {
            this.firstNameField.setText(draftUser.getFirstName());
            this.lastNameField.setText(draftUser.getLastName());
            this.emailField.setText(draftUser.getEmail());
        }
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(ru.appsm.inapphelp.R.menu.iah_new_issue, menu);
		
		MenuItem nextMenu = menu.findItem(ru.appsm.inapphelp.R.id.create_first_ticket_button);
		MenuItemCompat.setShowAsAction(nextMenu, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == ru.appsm.inapphelp.R.id.create_first_ticket_button) {
			
			if(getFirstName().trim().length() == 0 || getLastName().trim().length() == 0 || getEmailAdddress().trim().length() == 0) {
				IAHUtils.showAlertDialog(getActivity(), getResources().getString(ru.appsm.inapphelp.R.string.iah_error), getResources().getString(ru.appsm.inapphelp.R.string.iah_error_enter_all_fields_to_register));
				return false;
			}
			
			if(!android.util.Patterns.EMAIL_ADDRESS.matcher(getEmailAdddress()).matches()) {
				IAHUtils.showAlertDialog(getActivity(), getResources().getString(ru.appsm.inapphelp.R.string.iah_error_invalid_email), getResources().getString(ru.appsm.inapphelp.R.string.iah_error_enter_valid_email));
				return false;
			}

            getInapphelpActivity().setSupportProgressBarIndeterminateVisibility(true);

			gearSource.checkForUserDetailsValidity("NEW_USER", getFirstName(), getLastName(), 
					getEmailAdddress(), new OnFetchedSuccessListener() {
				
				@Override
				public void onSuccess(final Object successObject) {
                    String formattedBody = message;

                    gearSource.createNewTicket("NEW_TICKET", (IAHUser)successObject, formattedBody, attachmentArray,
                            new OnNewTicketFetchedSuccessListener() {
                                @Override
                                public void onSuccess() {
                                    getInapphelpActivity().setSupportProgressBarIndeterminateVisibility(false);
                                    sendSuccessSignal((IAHUser)successObject);
                                    gearSource.clearTicketDraft();
                                    Toast.makeText(getActivity(), getResources().getString(ru.appsm.inapphelp.R.string.iah_issue_created_raised), Toast.LENGTH_LONG).show();
                                }

                            }, new ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    IAHUtils.showAlertDialog(getActivity(), getResources().getString(ru.appsm.inapphelp.R.string.iah_error_reporting_issue), getResources().getString(ru.appsm.inapphelp.R.string.iah_error_check_network_connection));
                                    getInapphelpActivity().setSupportProgressBarIndeterminateVisibility(false);
                                }
                            });
				}
			}, new ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					getInapphelpActivity().setSupportProgressBarIndeterminateVisibility(false);
				}
			});
			
			return true;
		}
		
		
		return super.onOptionsItemSelected(item);
	}

    @Override
	public void onDetach() {
		gearSource.cancelOperation("NEW_USER");
		super.onDetach();
	}

	public String getFirstName() {
		return firstNameField.getText().toString();
	}

	public String getLastName() {
		return lastNameField.getText().toString();
	}

	public String getEmailAdddress() {
		return emailField.getText().toString();
	}

    public void sendSuccessSignal(IAHUser user) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_USER, user);
        IAHActivityManager.sendSuccessSignal(getActivity(), intent);
    }
}
