//  HomeFragment
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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import ru.appsm.inapphelp.R;
import ru.appsm.inapphelp.activities.IAHActivityManager;
import ru.appsm.inapphelp.activities.NewIssueActivity;
import ru.appsm.inapphelp.fragments.SearchFragment.OnReportAnIssueClickListener;
import ru.appsm.inapphelp.helper.IAHBaseExpandableListAdapter;
import ru.appsm.inapphelp.logic.IAHSource;
import ru.appsm.inapphelp.logic.IAHUtils;
import ru.appsm.inapphelp.logic.OnFetchedArraySuccessListener;
import ru.appsm.inapphelp.model.IAHKBItem;
import ru.appsm.inapphelp.model.IAHTicket;
import ru.appsm.inapphelp.model.IAHUser;

/**
 * Initial Fragment of HelpStack that contains FAQ and Tickets
 *
 * @author Nalin Chhajer
 *
 */
public class HomeFragment extends IAHFragmentParent {

	public static final int REQUEST_CODE_NEW_TICKET = 1003;

	private ExpandableListView mExpandableListView;
	private LocalAdapter mAdapter;

	private SearchFragment mSearchFragment;

	private IAHSource gearSource;

	private  View rootView;
	private IAHKBItem[] fetchedKbArticles;

	// To show loading until both the kb and tickets are not fetched.
	private int numberOfServerCallWaiting = 0;

	public HomeFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.iah_fragment_home, container, false);

		// ListView
		mExpandableListView = (ExpandableListView) rootView.findViewById(R.id.expandableList);
		mAdapter = new LocalAdapter(getActivity());

		// report an issue
		View report_an_issue_view = inflater.inflate(R.layout.iah_expandable_footer_report_issue, null);
		report_an_issue_view.findViewById(R.id.button1).setOnClickListener(reportIssueClickListener);
		mExpandableListView.addFooterView(report_an_issue_view);

		mExpandableListView.setAdapter(mAdapter);
		mExpandableListView.setOnChildClickListener(expandableChildViewClickListener);

		// Search fragment
		mSearchFragment = new SearchFragment();
		IAHFragmentManager.putFragmentInActivity(getInapphelpActivity(), R.id.search_container, mSearchFragment, "Search");
		mSearchFragment.setOnReportAnIssueClickListener(reportAnIssueLisener);
		// Add search Menu
		setHasOptionsMenu(true);

		// Initialize gear
		gearSource = IAHSource.getInstance(getActivity());

		if (!gearSource.isNewUser()) {
			Button mButton = (Button) report_an_issue_view.findViewById(R.id.button1);
			mButton.setText(getString(R.string.iah_viewissuebutton_title));
			mButton.setBackgroundColor(getResources().getColor(R.color.iah_view_issue_background_color));
			mButton.setTextColor(getResources().getColor(R.color.iah_view_issue_text_color));
		}

		// handle orientation
		if (savedInstanceState == null) {
			initializeView();
		} else {
			Gson gson = new Gson();
			fetchedKbArticles = gson.fromJson(savedInstanceState.getString("kbArticles"), IAHKBItem[].class);
			numberOfServerCallWaiting = savedInstanceState.getInt("numberOfServerCallWaiting");
			mSearchFragment.setKBArticleList(fetchedKbArticles);
			if (numberOfServerCallWaiting > 0) { // To avoid error during orientation
				initializeView(); // refreshing list from server
			} else {
				refreshList();
			}
		}

		return rootView;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Gson gson = new Gson();
		outState.putString("kbArticles", gson.toJson(fetchedKbArticles));
		outState.putInt("numberOfServerCallWaiting", numberOfServerCallWaiting);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_NEW_TICKET) {
			if (resultCode == IAHActivityManager.resultCode_sucess) {
				IAHUser user = (IAHUser) data.getSerializableExtra(NewIssueActivity.RESULT_USER);
				refreshList();
				gearSource.doSaveNewUserPropertiesForGearInCache(user);
				mExpandableListView.setSelectedGroup(1);

				Button mButton = (Button) rootView.findViewById(R.id.button1);
				mButton.setText(getString(R.string.iah_viewissuebutton_title));
				mButton.setBackgroundColor(getResources().getColor(R.color.iah_view_issue_background_color));
				mButton.setTextColor(getResources().getColor(R.color.iah_view_issue_text_color));
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.iah_search_menu, menu);

		MenuItem searchItem = menu.findItem(R.id.search);
		mSearchFragment.addSearchViewInMenuItem(getActivity(), searchItem);
	}

	@Override
	public void onDetach() {
		gearSource.cancelOperation("FAQ");
		super.onDetach();
	}

	private void initializeView() {

		startHomeScreenLoadingDisplay(true);

		// Show Loading
		gearSource.requestKBArticle("FAQ", null, new OnFetchedArraySuccessListener() {
			@Override
			public void onSuccess(Object[] kbArticles) {

				fetchedKbArticles = (IAHKBItem[]) kbArticles;
				mSearchFragment.setKBArticleList(fetchedKbArticles);
				refreshList();

				// Stop Loading
				startHomeScreenLoadingDisplay(false);
			}

		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError arg0) {
				// Stop Loading
				startHomeScreenLoadingDisplay(false);
				if(numberOfServerCallWaiting == 0) {
					IAHUtils.showAlertDialog(getActivity(), getResources().getString(R.string.iah_error), getResources().getString(R.string.iah_error_fetching_articles_issues));
				}
			}

		});

	}

	public void startHomeScreenLoadingDisplay(boolean loading) {
		if (loading) {
			numberOfServerCallWaiting = 1;
			getInapphelpActivity().setProgressBarIndeterminateVisibility(true);
		}
		else {
			numberOfServerCallWaiting--;
			if (numberOfServerCallWaiting == 0) {
				if (getInapphelpActivity() != null) { // To handle a crash that happens if activity is re-created and we receive network response after that.
					getInapphelpActivity().setProgressBarIndeterminateVisibility(false);
				}
			}
		}
	}

	protected OnChildClickListener expandableChildViewClickListener = new OnChildClickListener() {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
									int groupPosition, int childPosition, long id) {
			if (groupPosition == 0) {
				IAHKBItem kbItemClicked = (IAHKBItem) mAdapter.getChild(groupPosition, childPosition);
				articleClickedOnPosition(kbItemClicked);
				return true;
			}
			return false;
		}
	};

	protected OnClickListener reportIssueClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			gearSource.launchCreateNewTicketScreen(HomeFragment.this, REQUEST_CODE_NEW_TICKET);
		}
	};


	private OnReportAnIssueClickListener reportAnIssueLisener = new OnReportAnIssueClickListener() {
		@Override
		public void startReportAnIssue() {
			mSearchFragment.setVisibility(false);
			gearSource.launchCreateNewTicketScreen(HomeFragment.this, REQUEST_CODE_NEW_TICKET);
		}
	};



	//////////////////////////////////////
	// 		UTILITY FUNCTIONS         ///
	/////////////////////////////////////

	private void refreshList() {
		mAdapter.clearAll();
		mAdapter.addParent(0, getString(R.string.iah_articles_title));

		if (fetchedKbArticles != null) {
			for (int i = 0; i < fetchedKbArticles.length ; i++) {

				IAHKBItem item = (IAHKBItem) fetchedKbArticles[i];
				mAdapter.addChild(0, item);
			}
		}

		mAdapter.notifyDataSetChanged();
		expandAll();
	}

	private void expandAll() {
		int count = mAdapter.getGroupCount();
		for (int i = 0; i < count; i++) {
			mExpandableListView.expandGroup(i);
		}
	}

	protected void articleClickedOnPosition(IAHKBItem kbItemClicked) {
		if(kbItemClicked.getArticleType() == IAHKBItem.TYPE_ARTICLE) {
			IAHActivityManager.startArticleActivity(this, kbItemClicked, REQUEST_CODE_NEW_TICKET);

		} else {
			IAHActivityManager.startSectionActivity(this, kbItemClicked, REQUEST_CODE_NEW_TICKET);
		}
	}

	private class LocalAdapter extends IAHBaseExpandableListAdapter
	{

		public LocalAdapter(Context context) {
			super(context);
		}

		@Override
		public View getChildView(final int groupPosition,final int childPosition,
								 boolean isLastChild, View convertView, ViewGroup parent) {

			ChildViewHolder holder;

			if (convertView == null) {
				convertView = mLayoutInflater.inflate(R.layout.iah_expandable_child_home_default, null);
				holder = new ChildViewHolder();

				holder.textView1 = (TextView) convertView.findViewById(R.id.textView1);

				convertView.setTag(holder);
			}
			else {
				holder = (ChildViewHolder) convertView.getTag();
			}

			if (groupPosition == 0) {
				IAHKBItem item = (IAHKBItem) getChild(groupPosition, childPosition);
				holder.textView1.setText(item.getSubject());


			}
			else if (groupPosition == 1){
				IAHTicket item = (IAHTicket) getChild(groupPosition, childPosition);
				holder.textView1.setText(item.getSubject());
			}

			return convertView;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
								 View convertView, ViewGroup parent) {
			ParentViewHolder holder;

			if (convertView == null) {
				convertView = mLayoutInflater.inflate(R.layout.iah_expandable_parent_home_default, null);
				holder = new ParentViewHolder();

				holder.textView1 = (TextView) convertView.findViewById(R.id.textView1);

				convertView.setTag(holder);
			}
			else {
				holder = (ParentViewHolder) convertView.getTag();
			}

			String text = (String) getGroup(groupPosition);

			holder.textView1.setText(text);

			return convertView;
		}

		private class ParentViewHolder {
			TextView textView1;
		}

		private class ChildViewHolder {
			TextView textView1;
		}
	}
}
