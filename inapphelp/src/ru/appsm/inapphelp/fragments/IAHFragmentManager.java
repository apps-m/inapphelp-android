//  HSFragmentManager
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

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import ru.appsm.inapphelp.activities.IAHActivityParent;
import ru.appsm.inapphelp.model.IAHKBItem;

/**
 * 
 * Contatins functions that help in creating fragment used in HelpStack.
 * 
 * @author Nalin Chhajer
 *
 */
public class IAHFragmentManager {

	public static HomeFragment getHomeFragment() {
		HomeFragment fragment = new HomeFragment();
		return fragment;
	}

	public static NewIssueFragment getNewIssueFragment() {
		return NewIssueFragment.createNewIssueFragment();
	}

	public static IssueDetailFragment getIssueDetailFragment() {
		return new IssueDetailFragment();
	}
	
	public static SearchFragment getSearchFragment() {
		SearchFragment fragment = new SearchFragment();
		return fragment;
	}
	
	public static IAHFragmentParent getFragmentInActivity(IAHActivityParent activity,String tag) {
		FragmentManager fragMgr = activity.getSupportFragmentManager();
		return (IAHFragmentParent) fragMgr.findFragmentByTag(tag);
	}
	
	public static SectionFragment getSectionFragment(IAHActivityParent activity, IAHKBItem kbItem)
	{
		SectionFragment sectionFragment = new SectionFragment();
		sectionFragment.sectionItemToDisplay = kbItem;
		return sectionFragment;
	}
	
	public static ArticleFragment getArticleFragment(IAHActivityParent activity, IAHKBItem kbItem)
	{
		ArticleFragment sectionFragment = new ArticleFragment();
		sectionFragment.kbItem = kbItem;
		return sectionFragment;
	}
	
	public static ImageAttachmentDisplayFragment getImageAttachmentDisplayFragment(IAHActivityParent activity, String url)
	{
		ImageAttachmentDisplayFragment fragment = new ImageAttachmentDisplayFragment();
		fragment.image_url = url;
		return fragment;
	}
	
	
	
	public static void putFragmentInActivity(IAHActivityParent activity, int resid, IAHFragmentParent frag, String tag) {
		// above is proper.
		FragmentManager fragMgr = activity.getSupportFragmentManager();
		FragmentTransaction xact = fragMgr.beginTransaction();
		xact.replace(resid, frag, tag);
		xact.commit();	
	}
	
	public static void putFragmentBackStackInActivity(IAHActivityParent activity, int resid, IAHFragmentParent frag, String tag) {
		// above is proper.
		FragmentManager fragMgr = activity.getSupportFragmentManager();
		FragmentTransaction xact = fragMgr.beginTransaction();
		xact.replace(resid, frag);
		xact.addToBackStack(tag);
		xact.commit();	
	}
	
}
