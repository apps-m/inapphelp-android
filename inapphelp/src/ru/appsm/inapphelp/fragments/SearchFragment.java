//  SearchFragment
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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import ru.appsm.inapphelp.R;
import ru.appsm.inapphelp.activities.IAHActivityManager;
import ru.appsm.inapphelp.model.IAHKBItem;

/**
 * Search Fragment
 * 
 */
public class SearchFragment extends IAHFragmentParent {

	private View rootView;
	private SearchAdapter searchAdapter;
	private IAHKBItem[] allKbArticles;
	private ListView listView;
	private SearchView searchView;
	
	private OnReportAnIssueClickListener articleSelecetedListener;

	public SearchFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		rootView =  inflater.inflate(R.layout.iah_fragment_search, container, false);
		setVisibility(false);
		listView = (ListView)rootView.findViewById(R.id.searchList);
		searchAdapter = new SearchAdapter(this.allKbArticles);
		
		View report_an_issue_view = inflater.inflate(R.layout.iah_expandable_footer_report_issue, null);
        report_an_issue_view.findViewById(R.id.button1).setOnClickListener(reportIssueClickListener);

        listView.addFooterView(report_an_issue_view);
		
		listView.setAdapter(searchAdapter);
		
		listView.setOnItemClickListener(listItemClickListener);
		
		return rootView;
	}

	public void searchStarted() {
		searchAdapter.refreshList(allKbArticles);
		searchAdapter.getFilter().filter("");
		searchAdapter.notifyDataSetChanged();
	}

	public void doSearchForQuery(String q) {
		searchAdapter.getFilter().filter(q);
	}

	public boolean isSearchVisible() {
		if (rootView == null) {
			return false;
		}
		return rootView.getVisibility() == View.VISIBLE;
	}
	
	public void setVisibility(boolean visible) {
		if (visible) {
			rootView.setVisibility(View.VISIBLE);
		}
		else {
			rootView.setVisibility(View.GONE);
		}
	}
	
	public void setKBArticleList(IAHKBItem[] fetchedKbArticles) {
		this.allKbArticles = fetchedKbArticles;
		if (isSearchVisible()) {
			searchAdapter.refreshList(allKbArticles);
			searchAdapter.getFilter().filter("");
			searchAdapter.notifyDataSetChanged();
		}
	}
	
	protected OnItemClickListener listItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			IAHKBItem kbItemClicked = (IAHKBItem) searchAdapter.getItem(position);
			articleClickedOnPosition(kbItemClicked);
		}
	};
	
	protected void articleClickedOnPosition(IAHKBItem kbItemClicked) {
		if(kbItemClicked.getArticleType() == IAHKBItem.TYPE_ARTICLE) {
			IAHActivityManager.startArticleActivity(this, kbItemClicked, HomeFragment.REQUEST_CODE_NEW_TICKET);
			
		} else {
			IAHActivityManager.startSectionActivity(this, kbItemClicked, HomeFragment.REQUEST_CODE_NEW_TICKET);
		}
	}
	
	public void addSearchViewInMenuItem(Context context, MenuItem searchItem) {
		MenuItemCompat.setShowAsAction(searchItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS|MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		
		searchView = new SearchView(context);
		MenuItemCompat.setActionView(searchItem, searchView);
		searchView.setSubmitButtonEnabled(false);

		searchView.setOnSearchClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchStarted();
			}
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String q) {
				
				doSearchForQuery(q);
				
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				doSearchForQuery(newText);
				return true;
			}
		});


		MenuItemCompat.setOnActionExpandListener(searchItem, new OnActionExpandListener() {

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				setVisibility(true);
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				setVisibility(false);
				return true;
			}
		});
		
		if (Build.VERSION.SDK_INT >= 14) {
			//searchView.setQueryHint(getString(R.string.iah_search_hint)); // Works on android 4.0 and above, but crashes in below version.
			//TODO commented becouse not work.(crash on 4.04). Work around.
		}
		
		
	}
	
	private OnClickListener reportIssueClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if (articleSelecetedListener != null) {
				articleSelecetedListener.startReportAnIssue();
			}
		}
	};
	
	public void setOnReportAnIssueClickListener(OnReportAnIssueClickListener listener) {
		this.articleSelecetedListener = listener;
	}
	
	public interface OnReportAnIssueClickListener {
		public void startReportAnIssue();
	}
	
	private class SearchAdapter extends BaseAdapter implements Filterable{

		private IAHKBItem[] allKBItems;
		private IAHKBItem[] searchResults;
		private CustomFilter filter;
		
		public SearchAdapter(IAHKBItem[] list) {
			this.allKBItems = list;
		}
		
		public void refreshList(IAHKBItem[] list) {
			this.allKBItems = list;
		}
		
		@Override
		public int getCount() {
			if(searchResults == null) {
				return 0;
			}
			return searchResults.length;
		}

		@Override
		public Object getItem(int position) {
			return searchResults[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				LayoutInflater inflater = getActivity().getLayoutInflater();
				convertView = inflater.inflate(R.layout.iah_sectionlist_article, null);
				holder = new ViewHolder();
				holder.textview = (TextView)convertView.findViewById(R.id.sectionlisttextview);
				convertView.setTag(holder);
			}else {
				holder = (ViewHolder)convertView.getTag();
			}
			holder.textview.setText(((IAHKBItem)this.searchResults[position]).getSubject());
			return convertView;
		}
		
		private class ViewHolder {
			private TextView textview;
		}

		@Override
		public Filter getFilter() {
			if(filter == null) {
				filter = new CustomFilter();
			}
			return filter;
		}
		
		private class CustomFilter extends Filter {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				if(constraint == null || constraint.length() == 0){
					
					results.values = (IAHKBItem[])allKBItems;
					results.count = allKBItems.length;
					
				} else {
					// We perform filtering operation
			        List<IAHKBItem> filterList = new ArrayList<IAHKBItem>();
			         
			        for (IAHKBItem p : allKBItems) {
			            if (p.getSubject().toUpperCase().contains(constraint.toString().toUpperCase())) //.startsWith(constraint.toString().toUpperCase()))
			            	filterList.add(p);
			        }
			        IAHKBItem[] values = filterList.toArray(new IAHKBItem[filterList.size()]);
			        results.values = values;
			        results.count = filterList.size();
				}
				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				if(results == null) {
					notifyDataSetInvalidated();
				}else {
					searchResults = (IAHKBItem[]) results.values;
					notifyDataSetChanged();
				}
				
			}
			
		}
		
	}
}
