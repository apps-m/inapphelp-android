//  HSArticleReader
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

package ru.appsm.inapphelp.logic;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import ru.appsm.inapphelp.model.IAHKBItem;

/**
 * 
 * @author Nalin Chhajer
 *
 */
public class IAHArticleReader {

	private int articleResourceId;

	public IAHArticleReader(int articlesResourceId) {
		this.articleResourceId = articlesResourceId;
	}
	
	public IAHKBItem[] readArticlesFromResource(Context context) throws XmlPullParserException, IOException {
		  ArrayList<IAHKBItem> articles = new ArrayList<IAHKBItem>();
		  XmlPullParser xpp = context.getResources().getXml(articleResourceId);

		  while (xpp.getEventType()!=XmlPullParser.END_DOCUMENT) {
		    if (xpp.getEventType()==XmlPullParser.START_TAG) {
		      
		    	if (xpp.getName().equals("article")) {
		    	  
		    	  int attributeCount = xpp.getAttributeCount();
		    	  String subject = null;
		    	  String text = null;
		    	  for (int i = 0; i < attributeCount; i++) {
		    		String attrName = xpp.getAttributeName(i);
					if (attrName.equals("subject")) {
						subject = xpp.getAttributeValue(i);
					}
					if (attrName.equals("text")) {
						text = xpp.getAttributeValue(i);
					}
		    	  }
		    	  
		    	  assert subject != null : "Subject was not specified in xml for article @ index "+articles.size()+1;
		    	  assert text != null : "Text was not specified in xml for article @ index "+articles.size()+1;
		    	  articles.add(new IAHKBItem(null, subject, text));
		      
		    	}
		    }

		    xpp.next();
		  }
		  
		  IAHKBItem[] articleArray = new IAHKBItem[0];
		  articleArray = articles.toArray(articleArray);
		  return articleArray;
	}
	
	
}
