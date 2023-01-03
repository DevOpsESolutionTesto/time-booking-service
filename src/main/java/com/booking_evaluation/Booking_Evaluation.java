package com.booking_evaluation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Booking_Evaluation {
    public static Connection connect;
	
	private static ZonedDateTime bookingTemp = null;
	private static ZonedDateTime bookingStart = null;
	private static ZonedDateTime bookingEnd = null;
	
	private static DateTimeFormatter dayFormat;
	
	private static Duration duration;
	
	private static long time;
	private static long timeSampler = 0;
	
	
	
	/*The time for a personal number is calculated.
	 *Here the time span can be determined via the parameter. 
	 *The table is previously filtered by SQL commands.
	 *
	 *The date and the personnel number are expected as parameters. 
	  
	 */	
	public static void getWorkingHours(String date, String persNumber) {
		
		
		dayFormat = DateTimeFormatter.ofPattern("DD");
		
		String tempBooking1;
		
		 try {
				
			//TODO the connection to the table must be created here.
			//changes from the outside become visible and the cursor can be moved as desired
			Statement state = connect.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			// get all entries from the table
	        ResultSet rs = state.executeQuery("SELECT * FROM timebooking WHERE buchung LIKE '"+ date+"%"+"' AND  personalnummer LIKE '"+persNumber+"'");                  
	       
	       
	        
	        
            while (rs.next()){   //passed through all entries	            	
            	
            		tempBooking1 = rs.getString("buchung");
            		bookingTemp =  ZonedDateTime.parse(tempBooking1); //if the date was saved as a string.
            		
            		//no entry has been passed yet
            		if (bookingStart == null)
            			bookingStart = bookingTemp; // then set the entry
            		
            		//else check
            		else
            			//there are two bookings in one day
            			if (bookingStart.format(dayFormat).equals(bookingTemp.format(dayFormat)) ){
            				
            				bookingEnd = bookingTemp;
            				duration = Duration.between(bookingStart, bookingEnd); //get the difference
            				time = duration.toMinutes(); //Saves the result in minutes	            				   
            				
            				timeSampler = timeSampler + time;  //saved the total time          				
            				
            				bookingStart = null;
            				bookingEnd = null;
            				bookingTemp = null;
            				
            			}           	
            	}
            
            	//there is an entry
	            if (timeSampler !=0)
	            	//then transfer them. For example, a transfer could be made to a table with a time account.
	            	System.out.println(timeSampler / 60 + "." +timeSampler %60);
	            
	            
	            //TODO close the connection
							
		 	}
		 
		 	
			catch (Exception e) {
				System.out.println(e.toString());
				
						
			}	 
	}
	
	
	
	
	/*
	 *It is checked whether there are two entries with the same personnel number in the table on one day.
	 * 	
	 *The call can be made, for example, automatically when the personnel department logs into the program.
	 *
	 *A date is expected as a parameter in the format (YYYY-MM-DD)
	 */		
	public static void checkBooking(String date) {
		
		// create a array list.The list remembers the entries.
		ArrayList<String> list = new ArrayList<>(); 
		
		
		String tempPersonalNumber;	
		
		
		 try {
				
			//TODO the connection to the table must be created here.
		 
			//changes from the outside become visible and the cursor can be moved as desired
			Statement state = connect.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			// get all entries from the table
	        ResultSet rs = state.executeQuery("SELECT * FROM timebooking WHERE buchung LIKE '"+ date+"%'");  
	        
	        
            while (rs.next()){   //passed through all entries	            	
            	
            		tempPersonalNumber = rs.getString("personalnummer"); //get the personal number
            		
            		//is the number not yet in the list, or is the list empty....
            		if (!list.contains(tempPersonalNumber) || list.isEmpty()) 
            			list.add(tempPersonalNumber); //then add the number to the list
            		
            		//there is the number
            		else 
            			list.remove(tempPersonalNumber);  //then delete the entry 
            		
            }
            
            //if the list is not empty after the run, a warning must be issued.
            //the list then contains the numbers that have forgotten to book.
            if (!list.isEmpty())            
            	System.out.println(list);
           
            
            
            //TODO close the connection
						
		}	 	
		catch (Exception e) {
			System.out.println(e.toString());
			
					
		}	 
		
	}

    
}
