//  HSCachedTicket
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

package ru.appsm.inapphelp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.annotations.SerializedName;

public class IAHCachedTicket implements Serializable {

	@SerializedName("data")
	IAHTicket[] tickets;
	
	public IAHCachedTicket() {
		tickets = new IAHTicket[0];
	}
	
	public IAHTicket[] getTickets() {
		return tickets;
	}
	
	public void setTickets(IAHTicket[] tickets) {
		this.tickets = tickets;
	}

	public void addTicketAtStart(IAHTicket ticket) {
		// append the data . and save in cache 
		ArrayList<IAHTicket> ticketsList = new ArrayList<IAHTicket>();
		ticketsList.add(ticket);
		ticketsList.addAll(Arrays.asList(tickets));
		
		IAHTicket[] ticketsArray = new IAHTicket[0];
		ticketsArray = ticketsList.toArray(ticketsArray);
		
		setTickets(ticketsArray);
	}
	
}
