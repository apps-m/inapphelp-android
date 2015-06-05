//  NewUserActivity
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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import ru.appsm.inapphelp.model.IAHAttachment;

import java.io.FileNotFoundException;
import java.util.UUID;

public class EditAttachmentActivity extends ActionBarActivity {

    private final int REQUEST_CODE_PHOTO_PICKER = 100;

    private DrawingView drawView;
    private ImageButton currentPaint;
    private IAHAttachment selectedAttachment;
    private Bitmap originalBitmap;

    private TextView clearChangesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ru.appsm.inapphelp.R.layout.iah_activity_edit_attachment);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(ru.appsm.inapphelp.R.string.iah_attachment_edit);

        drawView = (DrawingView)findViewById(ru.appsm.inapphelp.R.id.drawing);
        drawView.setObserver(new DrawingView.ObserverInterface() {
            @Override
            public void activateClearOption(boolean isEnabled) {
                activityClearTextView(isEnabled);
            }
        });

        TextView clearChanges = (TextView) findViewById(ru.appsm.inapphelp.R.id.clear_change_text);
        clearChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.clearChanges();
            }
        });

        currentPaint = (ImageButton) findViewById(ru.appsm.inapphelp.R.id.iah_red_brush);
        if(android.os.Build.VERSION.SDK_INT < 16) {
            currentPaint.setBackgroundDrawable(getResources().getDrawable(ru.appsm.inapphelp.R.drawable.paint_pressed));
        }
        else {
            currentPaint.setBackground(getResources().getDrawable(ru.appsm.inapphelp.R.drawable.paint_pressed));
        }
        clearChangesTextView = (TextView)findViewById(ru.appsm.inapphelp.R.id.clear_change_text);

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(ru.appsm.inapphelp.R.string.iah_select_picture)), REQUEST_CODE_PHOTO_PICKER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(ru.appsm.inapphelp.R.menu.iah_edit_attachment, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            discardDraft();
            return true;
        }
        else if(id == ru.appsm.inapphelp.R.id.attach) {
            onSaveClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        discardDraft();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch(requestCode) {
            case REQUEST_CODE_PHOTO_PICKER:
                if(resultCode == Activity.RESULT_OK){
                    Uri selectedImage = intent.getData();

                    Cursor cursor = this.getContentResolver().query(selectedImage, new String[] {
                            MediaStore.Images.ImageColumns.DATA,
                            MediaStore.Images.ImageColumns.DISPLAY_NAME,
                            MediaStore.Images.ImageColumns.MIME_TYPE }, null, null, null);
                    cursor.moveToFirst();

                    String display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                    String mime_type = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE));

                    cursor.close();

                    selectedAttachment = IAHAttachment.createAttachment(selectedImage.toString(), display_name, mime_type);

                    try {
                        Uri uri = Uri.parse(selectedAttachment.getUrl());
                        originalBitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri), null, null);
                        drawView.setCanvasBitmap(originalBitmap);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                else {
                    finish();
                }
        }
    }


    private void onSaveClick() {
        drawView.setDrawingCacheEnabled(true);

        String imageSaved;
        if (drawView.hasBeenEdited()) {
            imageSaved = MediaStore.Images.Media.insertImage(getContentResolver(), drawView.getDrawingCache(),
                    UUID.randomUUID().toString() + ".png", "drawing");
        }
        else {
            imageSaved = MediaStore.Images.Media.insertImage(getContentResolver(), originalBitmap,
                    UUID.randomUUID().toString() + ".png", "drawing");
        }

        if(imageSaved!=null){
            Toast savedToast = Toast.makeText(getApplicationContext(),
                    "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
            savedToast.show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("URI", imageSaved);
            setResult(Activity.RESULT_OK, resultIntent);

            finish();
        }
        else{
            Toast unsavedToast = Toast.makeText(getApplicationContext(),
                    "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
            unsavedToast.show();
        }

        drawView.destroyDrawingCache();
    }


    public void paintColorClicked(View view) {
        if (view != currentPaint) {
            ImageButton imageButton = (ImageButton)view;
            String color = imageButton.getTag().toString();

            drawView.setColor(color);

            if(android.os.Build.VERSION.SDK_INT < 16) {
                imageButton.setBackgroundDrawable(getResources().getDrawable(ru.appsm.inapphelp.R.drawable.paint_pressed));
                currentPaint.setBackgroundDrawable(getResources().getDrawable(ru.appsm.inapphelp.R.drawable.paint));
            }
            else {
                imageButton.setBackground(getResources().getDrawable(ru.appsm.inapphelp.R.drawable.paint_pressed));
                currentPaint.setBackground(getResources().getDrawable(ru.appsm.inapphelp.R.drawable.paint));
            }
            currentPaint = (ImageButton) view;
        }
    }

    private void discardDraft() {
        if (drawView.hasBeenEdited()) {
            new AlertDialog.Builder(this)
                    .setTitle(ru.appsm.inapphelp.R.string.iah_discard)
                    .setMessage("Do you want to discard your changes?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(ru.appsm.inapphelp.R.string.iah_discard, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {
                                    EditAttachmentActivity.super.onBackPressed();
                                }
                            }
                    ).create().show();
        }
        else {
            IAHActivityManager.finishSafe(this);
        }
    }


    public void activityClearTextView(boolean isEnabled) {
        if (isEnabled) {
            clearChangesTextView.setTextColor(getResources().getColor(android.R.color.white));
        }
        else {
            clearChangesTextView.setTextColor(getResources().getColor(ru.appsm.inapphelp.R.color.iah_darkerGreycolor));
        }
    }
}
