//  ImageAttachmentDisplayFragment
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

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.polites.android.GestureImageView;
import ru.appsm.inapphelp.IAHHelpDesk;
import ru.appsm.inapphelp.logic.IAHUtils;
import ru.appsm.inapphelp.service.DownloadAttachmentUtility;

public class ImageAttachmentDisplayFragment extends IAHFragmentParent {
	
	private static final String TAG = ImageAttachmentDisplayFragment.class.getSimpleName();

	public String image_url;
	
	GestureImageView imageView;

	private RequestQueue queue;
	private static final String LoadImageCancelTag = "DownloadAttachment";

	private View progressView;
	boolean isAttachmentDownloaded = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		
		View rootView = inflater.inflate(
				ru.appsm.inapphelp.R.layout.iah_fragment_image_attachment_display, container,
				false);
		
		progressView = rootView.findViewById(ru.appsm.inapphelp.R.id.progressHolder);
		
		imageView = (GestureImageView) rootView.findViewById(ru.appsm.inapphelp.R.id.image);
		
		setHasOptionsMenu(true);

		queue = IAHHelpDesk.getInstance().getRequestQueue();


		if (savedInstanceState == null) {
			showLoading(true);
			loadImage();
		}
		else {
			isAttachmentDownloaded = savedInstanceState.getBoolean("isAttachmentDownloaded");
			if (isAttachmentDownloaded) {
				showLoading(false);
				imageView.setImageBitmap((Bitmap)savedInstanceState.getParcelable("bitmap"));
			}
			else {
				showLoading(true);
				loadImage();
			}
			
			
		}

		return rootView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("isAttachmentDownloaded", isAttachmentDownloaded);
		outState.putParcelable("bitmap", ((BitmapDrawable) imageView.getDrawable()).getBitmap());
	}
	
	@Override
	public void onDestroy() {
		queue.cancelAll(LoadImageCancelTag);
		super.onDestroy();
	}
	
	public void showLoading(boolean visible) {
		progressView.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(ru.appsm.inapphelp.R.menu.iah_image_attachment_display, menu);
	}

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem downloadItem = menu.findItem(ru.appsm.inapphelp.R.id.menu_download);
        if (image_url != null && image_url.startsWith("http")) {
            downloadItem.setVisible(true);
        }
        else {
            downloadItem.setVisible(false);
        }

    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == ru.appsm.inapphelp.R.id.menu_download) {
			DownloadAttachmentUtility.downloadAttachment(getActivity(), image_url, getInapphelpActivity().getTitle().toString());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void loadImage() {
        if (image_url.startsWith("http")) {
			showLoading(true);
			ImageRequest imgRequest = new ImageRequest(image_url, new Response.Listener<Bitmap>() {
				@Override
				public void onResponse(Bitmap response) {
					imageView.setImageBitmap(response);
					showLoading(false);
				}
			}, 0, 0, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					IAHUtils.showAlertDialog(getActivity(), getResources().getString(ru.appsm.inapphelp.R.string.iah_error), getResources().getString(ru.appsm.inapphelp.R.string.iah_error_fetching_attachment));
					showLoading(false);
				}
			});
			imgRequest.setTag(LoadImageCancelTag);
			queue.add(imgRequest);
			queue.start();
			getActivity().invalidateOptionsMenu();
        }
        else if (image_url.startsWith("content")) {
            Bitmap selectedBitmap;
            try {
                selectedBitmap = NewIssueFragment.downscaleAndReadBitmap(getActivity(), Uri.parse(image_url));
                imageView.setImageBitmap(selectedBitmap);
                showLoading(false);
            }
            catch (FileNotFoundException e) {
                Toast.makeText(getActivity(), "Sorry! could not open attachment, unknown image", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }

        }
        else {
            Toast.makeText(getActivity(), "Sorry! could not open attachment, unknown image", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }


	}
}
