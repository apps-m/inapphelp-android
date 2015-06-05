//  NewIssueFragment
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

import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import ru.appsm.inapphelp.R;
import ru.appsm.inapphelp.activities.EditAttachmentActivity;
import ru.appsm.inapphelp.activities.IAHActivityManager;
import ru.appsm.inapphelp.activities.NewIssueActivity;
import ru.appsm.inapphelp.logic.IAHSource;
import ru.appsm.inapphelp.logic.IAHUtils;
import ru.appsm.inapphelp.model.IAHAttachment;

public class NewIssueFragment extends IAHFragmentParent {

    private final int REQUEST_CODE_PHOTO_PICKER = 100;

    public static final int REQUEST_CODE_NEW_TICKET = HomeFragment.REQUEST_CODE_NEW_TICKET;

    public static final String EXTRAS_MESSAGE = NewIssueActivity.EXTRAS_MESSAGE;
    public static final String EXTRAS_ATTACHMENT = NewIssueActivity.EXTRAS_ATTACHMENT;

    EditText messageField;
    ImageView imageView1;

    IAHAttachment selectedAttachment;
    IAHSource gearSource;

    public static NewIssueFragment createNewIssueFragment()
    {
        return new NewIssueFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.iah_fragment_new_issue, container, false);

        this.messageField = (EditText) rootView.findViewById(R.id.messageField);

        this.imageView1 = (ImageView) rootView.findViewById(R.id.imageView1);
        this.imageView1.setOnClickListener(attachmentClickListener);

        gearSource = IAHSource.getInstance(getActivity());

        this.messageField.setText(gearSource.getDraftMessage());

        if (gearSource.getDraftAttachments() != null && gearSource.getDraftAttachments().length > 0) {
            this.selectedAttachment = gearSource.getDraftAttachments()[0];
            resetAttachmentImage();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("message", messageField.getText().toString());
        outState.putSerializable("attachment", selectedAttachment);
    }

    @Override
    public void onPause() {
        super.onPause();

        IAHAttachment[] attachmentArray = null;

        if (selectedAttachment != null) {
            attachmentArray = new IAHAttachment[1];
            attachmentArray[0] = selectedAttachment;
        }

        gearSource.saveTicketDetailsInDraft(messageField.getText().toString(), attachmentArray);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            messageField.setText(savedInstanceState.getString("message"));
            selectedAttachment = (IAHAttachment) savedInstanceState.getSerializable("attachment");
        }

        resetAttachmentImage();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.iah_issue_menu, menu);

        MenuItem clearMenu = menu.findItem(R.id.clearItem);
        MenuItemCompat.setShowAsAction(clearMenu, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

        MenuItem doneMenu = menu.findItem(R.id.doneItem);

        doneMenu.setIcon(getResources().getDrawable(R.drawable.iah_action_forward));
        doneMenu.setTitle(getResources().getText(R.string.iah_next));

        MenuItemCompat.setShowAsAction(doneMenu, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.doneItem) {

            if(getMessage().trim().length() == 0) {
                IAHUtils.showAlertDialog(getActivity(), getResources().getString(R.string.iah_error), getResources().getString(R.string.iah_error_subject_message_empty));
                return false;
            }

            IAHAttachment[] attachmentArray = null;

            if (selectedAttachment != null) {
                attachmentArray = new IAHAttachment[1];
                attachmentArray[0] = selectedAttachment;
            }

            String formattedBody = getMessage();

            IAHActivityManager.startNewUserActivity(this, REQUEST_CODE_NEW_TICKET, formattedBody, attachmentArray);

            return true;
        }
        else if(id == R.id.clearItem) {
            clearFormData();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch(requestCode) {
            case REQUEST_CODE_PHOTO_PICKER:
                if(resultCode == Activity.RESULT_OK){

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
                    break;
                }
            case REQUEST_CODE_NEW_TICKET:
                if (resultCode == IAHActivityManager.resultCode_sucess) {
                    IAHActivityManager.sendSuccessSignal(getActivity(), intent);
                    break;
                }
        }
    }

    @Override
    public void onDetach() {
        gearSource.cancelOperation("NEW_TICKET");
        super.onDetach();
    }

    private OnClickListener attachmentClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (selectedAttachment == null) {
                Intent intent = new Intent(getActivity(), EditAttachmentActivity.class);
                startActivityForResult(intent, REQUEST_CODE_PHOTO_PICKER);
            }
            else {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                alertBuilder.setTitle(getResources().getString(R.string.iah_attachment));
                alertBuilder.setIcon(R.drawable.iah_attachment_img);
                String[] attachmentOptions = {getResources().getString(R.string.iah_change), getResources().getString(R.string.iah_remove)};
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

    private void resetAttachmentImage() {
        if (selectedAttachment == null) {
            this.imageView1.setImageResource(R.drawable.iah_add_attachment_img);
        }
        else {
            try {
                Uri uri = Uri.parse(selectedAttachment.getUrl());
                Bitmap selectedBitmap;
                selectedBitmap = downscaleAndReadBitmap(getActivity(), uri);
                this.imageView1.setImageBitmap(selectedBitmap);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    private void clearFormData() {
        this.messageField.setText("");
        this.selectedAttachment = null;

        resetAttachmentImage();

        gearSource.clearTicketDraft();
    }

    public String getMessage() {
        return messageField.getText().toString();
    }

    public static Bitmap downscaleAndReadBitmap(Context context, Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 140;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o2);

    }

}
