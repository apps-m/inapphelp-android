//  HSBaseExpandableListAdapter
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

package ru.appsm.inapphelp.helper;

import java.util.ArrayList;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

/**
 * 
 * This is simple adapter that helps in storing Parent and Child value, 
 * this simplifying the amount of code written by developer.
 * 
 * Just call addParent and addChild to add custom properties of your choice.
 * 
 * @author Nalin Chhajer
 *
 */
public abstract class IAHBaseExpandableListAdapter extends BaseExpandableListAdapter {
	
	ArrayList<Object> childrens = new ArrayList<Object>();
	SparseArray<Parent> groups = new SparseArray<Parent>();
	
	protected final LayoutInflater mLayoutInflater;
	protected final Context context;
	public IAHBaseExpandableListAdapter(Context context) {
		super();
		this.context = context;
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	class Parent {
		int id;
		Object parent;
		ArrayList<Integer> childs = new ArrayList<Integer>();
	}
	
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}
	
	public void clearAll() {
		childrens.clear();
		groups.clear();
	}

	public void clearParent(int parentId) {
		if (groups.get(parentId) != null) {
			groups.get(parentId).childs.clear();	
		}
	}
	
	// Please note the value of the previous parent will be overridden
	public void addParent(int parentId, Object parent) {
		assert parent!=null;
		Parent parentholder = new Parent();
		parentholder.id = parentId;
		parentholder.parent = parent;
		groups.put(parentId, parentholder);
	}
	
	public void addChild(int parentId, Object child) {
		childrens.add(child);
		int pos = childrens.indexOf(child);
		groups.get(parentId).childs.add(pos);
	}
	
	public void addChildAll(int parentId, ArrayList child) {
		for (Object object : child) {
			addChild(parentId, object);
		}
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		int childPos = groups.valueAt(groupPosition).childs.get(childPosition);
		return childrens.get(childPos);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		int childPos = groups.valueAt(groupPosition).childs.get(childPosition);
		return childPos;
	}

	@Override
	public abstract View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent);
	
	@Override
	public int getChildrenCount(int groupPosition) {
		Parent mParent = groups.valueAt(groupPosition);
		if(mParent == null) {
			return 0;
		}
		return mParent.childs.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groups.valueAt(groupPosition).parent;
	}

	@Override
	public int getGroupCount() {
		return groups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	
	public long getParentId(int groupPosition) {
		return groups.valueAt(groupPosition).id;
	}

	@Override
	public abstract View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent);

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

//Listeners
	
	protected OnChildItemClickListener childlistener;
	protected OnParentItemClickListener parentlistener;
	
	protected void sendChildClickEvent(int groupPosition, int childPosition,String type, Object map) {
		if(childlistener!=null) {
			childlistener.onChildListItemClick(groupPosition,childPosition,type,map);
		}
	}
	
	protected void sendParentItemClickEvent(int groupPosition,String type, Object obj) {
		if(parentlistener!=null) {
			parentlistener.onParentItemClicked(groupPosition,type,obj);
		}
	}
	
	protected boolean sendChildLongClickEvent(int groupPosition, int childPosition,String type, Object map) {
		if(childlistener!=null) {
			return childlistener.onChildListItemLongClick(groupPosition,childPosition,type, map);
		}
		return false;
	}
	
	protected void sendChildCheckedEvent(int groupPosition, int childPosition,String type,Object map,boolean checked) {
		if(childlistener!=null) {
			childlistener.onChildCheckedListner(groupPosition,childPosition,type, map,checked);
		}
	}
		
	public void setOnChildItemClickListener(OnChildItemClickListener listener) {
		this.childlistener = listener;
	}
	
	public void setOnParentItemClickListener(OnParentItemClickListener listener) {
		this.parentlistener = listener;
	}
	
	public interface OnChildItemClickListener {
		void onChildListItemClick(int groupPosition, int childPosition,String type,Object map);
		void onChildCheckedListner(int groupPosition, int childPosition,String type,Object map,boolean checked);
		boolean onChildListItemLongClick(int groupPosition, int childPosition,String type,Object map);
	}
	
	public interface OnParentItemClickListener {
		void onParentItemClicked(int groupPosition,String type, Object obj);
	}
}
