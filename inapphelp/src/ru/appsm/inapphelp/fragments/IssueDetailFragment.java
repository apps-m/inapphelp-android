//  IssueDetailFragment
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import ru.appsm.inapphelp.activities.EditAttachmentActivity;
import ru.appsm.inapphelp.activities.IAHActivityManager;
import ru.appsm.inapphelp.helper.IAHBaseExpandableListAdapter;
import ru.appsm.inapphelp.logic.IAHSource;
import ru.appsm.inapphelp.logic.IAHUtils;
import ru.appsm.inapphelp.logic.OnFetchedArraySuccessListener;
import ru.appsm.inapphelp.model.IAHAttachment;
import ru.appsm.inapphelp.model.IAHTicketUpdate;
import ru.appsm.inapphelp.model.IAHUser;
import ru.appsm.inapphelp.service.DownloadAttachmentUtility;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

public class IssueDetailFragment extends IAHFragmentParent
{

	private final int REQUEST_CODE_PHOTO_PICKER = 100;
	
	public IssueDetailFragment() {
	}
	
	private ExpandableListView mExpandableListView;
	private LocalAdapter mAdapter;
	private ImageView sendButton;
	private EditText replyEditTextView;
	private ImageView mAttachmentButton;

	private IAHUser user;
	private IAHSource gearSource;
	private IAHTicketUpdate[] fetchedUpdates;
	private IAHAttachment selectedAttachment;

	private Boolean updateTicketInProgress = false;
	private Handler updateHandler;

	private int updateInterval = 10000;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(ru.appsm.inapphelp.R.layout.iah_fragment_issue_detail, null);
		
		
		replyEditTextView = (EditText) rootView.findViewById(ru.appsm.inapphelp.R.id.replyEditText);
		sendButton = (ImageView)rootView.findViewById(ru.appsm.inapphelp.R.id.button1);
		sendButton.setOnClickListener(sendReplyListener);
		
		mExpandableListView = (ExpandableListView) rootView.findViewById(ru.appsm.inapphelp.R.id.expandableList);
		mAttachmentButton = (ImageView) rootView.findViewById(ru.appsm.inapphelp.R.id.attachmentbutton);
		
		mAttachmentButton.setOnClickListener(attachmentClickListener);
		
        mAdapter = new LocalAdapter(getActivity());
        
        mExpandableListView.setAdapter(mAdapter);
        mExpandableListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        
        gearSource = IAHSource.getInstance(getActivity());

        this.replyEditTextView.setText(gearSource.getDraftReplyMessage());

        if (gearSource.getDraftReplyAttachments() != null && gearSource.getDraftReplyAttachments().length > 0) {
            this.selectedAttachment = gearSource.getDraftReplyAttachments()[0];
            resetAttachmentImage();
        }
		
        mAdapter.setOnChildItemClickListener(listChildClickListener);

		fetchedUpdates = new IAHTicketUpdate[0];

		updateHandler = new Handler();
		updateChecker.run();
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState == null ) {
			refreshUpdateFromServer();
		}
		else {
            Gson gson = new Gson();
            fetchedUpdates  = gson.fromJson(savedInstanceState.getString("updates"), IAHTicketUpdate[].class);
			selectedAttachment = (IAHAttachment) savedInstanceState.getSerializable("selectedAttachment");
			replyEditTextView.setText(savedInstanceState.getString("replyEditTextView"));

			refreshList();
			resetAttachmentImage();

			refreshUpdateFromServer();
		}
		
		refreshList();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        Gson json = new Gson();
		outState.putString("updates", json.toJson(fetchedUpdates));
		outState.putSerializable("selectedAttachment", selectedAttachment);
		outState.putSerializable("replyEditTextView", replyEditTextView.getText().toString());
	}

    @Override
    public void onPause() {
        super.onPause();

        IAHAttachment[] attachmentArray = null;

        if (selectedAttachment != null) {
            attachmentArray = new IAHAttachment[1];
            attachmentArray[0] = selectedAttachment;
        }
		updateHandler.removeCallbacks(updateChecker);
		gearSource.saveReplyDetailsInDraft(replyEditTextView.getText().toString(), attachmentArray);
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if(requestCode == REQUEST_CODE_PHOTO_PICKER && resultCode == Activity.RESULT_OK)
		{

            Uri selectedImage = Uri.parse(intent.getStringExtra("URI"));
			
			//User had pick an image.
	        Cursor cursor = getActivity().getContentResolver().query(selectedImage, new String[] { 
	        		ImageColumns.DATA,
	        		ImageColumns.DISPLAY_NAME, 
	        		ImageColumns.MIME_TYPE }, null, null, null);
	        cursor.moveToFirst();
			
	        String display_name = cursor.getString(cursor.getColumnIndex(ImageColumns.DISPLAY_NAME));
	        String mime_type = cursor.getString(cursor.getColumnIndex(ImageColumns.MIME_TYPE));
	        
	        cursor.close();
	        
	        selectedAttachment = IAHAttachment.createAttachment(selectedImage.toString(), display_name, mime_type);
			
			resetAttachmentImage();
        }
	};
	
	@Override
	public void onDetach() {

		updateHandler.removeCallbacks(updateChecker);
		gearSource.cancelOperation("REPLY_TO_A_TICKET");
		gearSource.cancelOperation("ALL_UPDATES");

		super.onDetach();
	}
	
	private void refreshUpdateFromServer() {

		if (updateTicketInProgress)
			return;

		getInapphelpActivity().setProgressBarIndeterminateVisibility(true);
		updateTicketInProgress = true;
		Long last_message_id = 0l;
		if (fetchedUpdates.length > 0) {
			last_message_id = fetchedUpdates[fetchedUpdates.length-1].getId();
		}
		gearSource.requestUpdatesOnTicket("ALL_UPDATES", last_message_id, user, new OnFetchedArraySuccessListener() {

			@Override
			public void onSuccess(Object[] successObject) {

				updateTicketInProgress = false;

				if (fetchedUpdates.length > 0) {
					ArrayList<IAHTicketUpdate> both = new ArrayList<IAHTicketUpdate>(Arrays.asList(fetchedUpdates));
					both.addAll(Arrays.asList((IAHTicketUpdate[]) successObject));
					fetchedUpdates = (IAHTicketUpdate[]) both.toArray(fetchedUpdates);
				} else {
					fetchedUpdates = (IAHTicketUpdate[]) successObject;
				}

				getInapphelpActivity().setProgressBarIndeterminateVisibility(false);
				if (successObject.length > 0) {
					refreshList();
					scrollListToBottom();
				}
				updateHandler.postDelayed(updateChecker, updateInterval);
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				updateTicketInProgress = false;
				IAHUtils.showAlertDialog(getActivity(), getResources().getString(ru.appsm.inapphelp.R.string.iah_error), getResources().getString(ru.appsm.inapphelp.R.string.iah_error_fetching_ticket_updates));
				getInapphelpActivity().setProgressBarIndeterminateVisibility(false);
			}
		});
	}
	
	private IAHBaseExpandableListAdapter.OnChildItemClickListener listChildClickListener = new IAHBaseExpandableListAdapter.OnChildItemClickListener() {
		
		@Override
		public boolean onChildListItemLongClick(int groupPosition,
				int childPosition, String type, Object map) {
			return false;
		}
		
		@Override
		public void onChildListItemClick(int groupPosition, int childPosition,
				String type, Object map) {
			showAttachments(((IAHTicketUpdate)map).getAttachments());
		}
		
		@Override
		public void onChildCheckedListner(int groupPosition, int childPosition,
				String type, Object map, boolean checked) {
			
		}
	};
	

	private OnClickListener attachmentClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if (selectedAttachment == null) {
                Intent intent = new Intent(getActivity(), EditAttachmentActivity.class);
                startActivityForResult(intent, REQUEST_CODE_PHOTO_PICKER);
			}
			else {
				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
				alertBuilder.setTitle(getResources().getString(ru.appsm.inapphelp.R.string.iah_attachment));
				alertBuilder.setIcon(ru.appsm.inapphelp.R.drawable.iah_attachment_img);
				String[] attachmentOptions = {getResources().getString(ru.appsm.inapphelp.R.string.iah_change), getResources().getString(ru.appsm.inapphelp.R.string.iah_remove)};
				alertBuilder.setItems(attachmentOptions, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
                            Intent intent = new Intent(getActivity(), EditAttachmentActivity.class);
                            startActivityForResult(intent, REQUEST_CODE_PHOTO_PICKER);
						}
						
						else if (which == 1) {
							selectedAttachment = null;
							resetAttachmentImage();
						}
					}
				});
				alertBuilder.create().show();
			}
			
		}
	};
	
	private OnClickListener sendReplyListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			String message = replyEditTextView.getText().toString();
			if(message.trim().length() == 0) {
				return;
			}
			
			getInapphelpActivity().setProgressBarIndeterminateVisibility(true);
			sendButton.setEnabled(false);
            sendButton.setAlpha((float)0.4);
			
			IAHAttachment[] attachmentArray = null;
			
			if (selectedAttachment != null) {
				attachmentArray = new IAHAttachment[1];
				attachmentArray[0] = selectedAttachment;
			}
			
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
				      Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(replyEditTextView.getWindowToken(), 0);

			Long last_message_id = 0l;
			if (fetchedUpdates.length > 0) {
				last_message_id = fetchedUpdates[fetchedUpdates.length-1].getId();
			}

			//Add reply, and get in answer from server updates array;
			gearSource.addReplyOnATicket("REPLY_TO_A_TICKET", message, attachmentArray, last_message_id, user, new OnFetchedArraySuccessListener() {
				
				@Override
				public void onSuccess(Object[] successObject) {

					clearFormData();
					sendButton.setEnabled(true);
					sendButton.setAlpha((float) 1.0);

					if (fetchedUpdates.length > 0) {
						ArrayList<IAHTicketUpdate> both = new ArrayList<IAHTicketUpdate>(Arrays.asList(fetchedUpdates));
						both.addAll(Arrays.asList((IAHTicketUpdate[]) successObject));
						fetchedUpdates = (IAHTicketUpdate[]) (both.toArray(fetchedUpdates));
					} else {
						fetchedUpdates = (IAHTicketUpdate[]) successObject;
					}

					refreshList();
					getInapphelpActivity().setProgressBarIndeterminateVisibility(false);

					if (((IAHTicketUpdate[]) successObject).length > 0)
						scrollListToBottom();
				}
			}, new ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					IAHUtils.showAlertDialog(getActivity(), getResources().getString(ru.appsm.inapphelp.R.string.iah_error), getResources().getString(ru.appsm.inapphelp.R.string.iah_error_posting_reply));
					sendButton.setEnabled(true);
                    sendButton.setAlpha((float)1.0);
					getInapphelpActivity().setProgressBarIndeterminateVisibility(false);
				}
			});
		}
	};

    private void clearFormData() {
        replyEditTextView.setText("");
        gearSource.clearReplyDraft();
		selectedAttachment = null;
		resetAttachmentImage();
	}

    private void expandAll() {
		int count = mAdapter.getGroupCount();
		for (int i = 0; i < count; i++) {
			mExpandableListView.expandGroup(i);
		}
	}
	
	private void refreshList() {
		
		mAdapter.clearAll();
		
		if (fetchedUpdates != null) {
			mAdapter.addParent(1, "");
			for (int i = 0; i < fetchedUpdates.length; i++) {
				mAdapter.addChild(1, fetchedUpdates[i]);
			}
		}
		
		mAdapter.notifyDataSetChanged();
		
		expandAll();
	}

	private void showAttachments(final IAHAttachment[] attachmentsArray) {
		
		if (attachmentsArray.length == 1) {
			IAHAttachment attachmentToShow = attachmentsArray[0];
			openAttachment(attachmentToShow);
			return;
		}
		
		ArrayList<String> attachments = new ArrayList<String>();
		for(IAHAttachment attachment : attachmentsArray) {
			attachments.add(attachment.getFileName());
		}
		String[] attachmentNames = attachments.toArray(new String[attachments.size()]);
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
        View convertView = (View) inflater.inflate(ru.appsm.inapphelp.R.layout.iah_attachment_dialog, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle(getResources().getString(ru.appsm.inapphelp.R.string.iah_attachments));
        final AlertDialog dialog = alertDialog.create();
        
        ListView lv = (ListView) convertView.findViewById(ru.appsm.inapphelp.R.id.listView1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,attachmentNames);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				IAHAttachment attachmentToShow = attachmentsArray[position];
				openAttachment(attachmentToShow);
				dialog.dismiss();
			}
		});
        
        dialog.show();
	}


	private class LocalAdapter extends IAHBaseExpandableListAdapter
	{
		public LocalAdapter(Context context) {
			super(context);
		}

		@Override
		public View getChildView(final int groupPosition, final int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			ChildViewHolder holder;
			if (convertView == null) {
				holder = new ChildViewHolder();
				if (getChildType(groupPosition, childPosition) == 0) {
					convertView = mLayoutInflater.inflate(ru.appsm.inapphelp.R.layout.iah_expandable_child_issue_detail_staff_reply, null);
				}
				else {
					convertView = mLayoutInflater.inflate(ru.appsm.inapphelp.R.layout.iah_expandable_child_issue_detail_user_reply, null);
				}
				
				holder.textView1 = (TextView) convertView.findViewById(ru.appsm.inapphelp.R.id.textView1);
				holder.nameField = (TextView) convertView.findViewById(ru.appsm.inapphelp.R.id.name);
				holder.timeField = (TextView) convertView.findViewById(ru.appsm.inapphelp.R.id.time);
				holder.attachmentButton = (ImageView) convertView.findViewById(ru.appsm.inapphelp.R.id.attachment_icon);
                holder.textView_no_message = (TextView) convertView.findViewById(ru.appsm.inapphelp.R.id.textView_no_message);
				
				convertView.setTag(holder);
			}
			else {
				holder = (ChildViewHolder) convertView.getTag();
			}
			
			// This is a dummy view as only 1 group is gonna be used.
			final IAHTicketUpdate update = (IAHTicketUpdate) getChild(groupPosition, childPosition);
			holder.textView1.setMovementMethod(LinkMovementMethod.getInstance());

            String text = update.getText().trim();
            if (text == null || text.length() == 0 ) {
                holder.textView_no_message.setVisibility(View.VISIBLE);
                holder.textView1.setVisibility(View.GONE);
                holder.textView1.setText("");
            }
            else {
                holder.textView_no_message.setVisibility(View.GONE);
                holder.textView1.setVisibility(View.VISIBLE);
                holder.textView1.setText(text);
            }


			
			if(update.isUserUpdate()) {
				holder.nameField.setText(getResources().getString(ru.appsm.inapphelp.R.string.iah_me));
			} else {
				if(update.name != null) {
					holder.nameField.setText(update.name);
				} else {
					holder.nameField.setText(getResources().getString(ru.appsm.inapphelp.R.string.iah_staff));
				}
			}
			
			if(update.isAttachmentEmtpy()) {
				holder.attachmentButton.setVisibility(View.INVISIBLE);
			}else {
				holder.attachmentButton.setVisibility(View.VISIBLE);
				holder.attachmentButton.setFocusable(true);
				holder.attachmentButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						sendChildClickEvent(groupPosition, childPosition, "attachment", update);
					}
				});
			}
			
			Date updatedTime = update.getUpdatedTime();
			
			String dateString = IAHUtils.convertToHumanReadableTime(updatedTime, Calendar.getInstance().getTimeInMillis());
			holder.timeField.setText(dateString.trim());
			
			return convertView;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			ParentViewHolder holder;
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(ru.appsm.inapphelp.R.layout.iah_expandable_parent_issue_detail_default, null);
				holder = new ParentViewHolder();
				holder.parent = convertView;
				
				convertView.setTag(holder);
			}
			else {
				holder = (ParentViewHolder) convertView.getTag();
			}
			
			// This is a dummy view as only 1 group is gonna be used.
			holder.parent.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// Empty to avoid expand/collapse
				}
			});
			
			return convertView;
		}
		
		@Override
		public int getChildTypeCount() {
			return 2;
		}
		
		@Override
		public int getChildType(int groupPosition, int childPosition) {
			IAHTicketUpdate update = (IAHTicketUpdate) getChild(groupPosition, childPosition);
			return update.isStaffUpdate()?0:1;
		}
		
		private class ParentViewHolder {
			View parent;
		}
		
		private class ChildViewHolder {
			public TextView textView1;
			public TextView nameField;
			public TextView timeField;
			public ImageView attachmentButton;
            public TextView textView_no_message;
		}
	}
	
	private void resetAttachmentImage() {
		if (selectedAttachment == null) {
			this.mAttachmentButton.setImageResource(ru.appsm.inapphelp.R.drawable.iah_add_attachment);
		}
		else {
			
			try {
				Uri uri = Uri.parse(selectedAttachment.getUrl());
				Bitmap selectedBitmap;
				selectedBitmap = NewIssueFragment.downscaleAndReadBitmap(getActivity(), uri);
				this.mAttachmentButton.setImageBitmap(selectedBitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	private void scrollListToBottom() {
		mExpandableListView.setSelectedChild(0, mAdapter.getChildrenCount(0) - 1, true);
	}
	

	/**
	 * @return the ticket
	 */
	public IAHUser getUser() {
		return user;
	}


	/**
	 * @param user user to set
	 */
	public void setUser(IAHUser user) {
		this.user = user;
	}
	
	
	
	/// Attachments
	private void openAttachment(IAHAttachment attachment) {
		if(knownAttachmentType(attachment)) {
			IAHActivityManager.startImageAttachmentDisplayActivity(getInapphelpActivity(), attachment.getUrl(), attachment.getFileName());
		}
		else {
			downloadAttachment(attachment);
		}

	}
	
	private boolean knownAttachmentType(IAHAttachment attachment)
	{
		String mime_type = attachment.getMime_type();
		if (mime_type != null  && mime_type.startsWith("image")) {
			return true;
		}
		String file_name = attachment.getFileName();
		if (file_name != null && isKnowFileNameType(file_name)) {
			return true;
		}
		return false;
	}
	
	private boolean isKnowFileNameType(String file_name) {
		// get the type of file
		StringTokenizer strtok = new StringTokenizer(file_name, ".");

		// getting the last token
		String fileType = null;
		while (strtok.hasMoreTokens()) {
			// parsing to get last token
			fileType = strtok.nextToken();
		}
		String[] knownFileType = {"png", "jpg", "jpeg"};
		if(containString(knownFileType,fileType.toLowerCase())) {
			return true;
		}
		return false;
	}
	
	private boolean containString(String[] array, String data) {
		for (int i = 0; i < array.length; i++) {
			if(array[i].contains(data)) {
				return true;
			}
		}
		return false;
	}

	public void downloadAttachment(IAHAttachment attachment) {
		DownloadAttachmentUtility.downloadAttachment(getActivity(), attachment.getUrl(), attachment.getFileName());
	}

	Runnable updateChecker = new Runnable() {
		@Override
		public void run() {
			refreshUpdateFromServer(); //this function can change value of mInterval.
		}
	};


}
