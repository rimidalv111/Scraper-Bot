package com.rimidalv111.scrapebox;

//## custom fetch change this import
import com.rimidalv111.custom.archive.FetchUrlThread;

public class QueNextThread extends Thread
{	
	private ScraperBot instance;
	
	//how many seconds to wait between next que's
	private int que_wait = 5000;
	

	public QueNextThread(ScraperBot i, int qw)
	{
		instance = i;
		que_wait = qw;
	}

	public void run()
	{
		try
		{
			boolean keepRunning = true;
			while (keepRunning)
			{
				//if renewal is in progress then return
				if(instance.isRenewal_in_progress())
				{
					
				} else
				
				//if save is in progress then return
				if(instance.isCurrently_saving())
				{
					
				} else
				
				//if available threads are full then return
				if(instance.getRunning_threads().size() >= instance.getNumber_threads())
				{
				
				} else
				
				//if no new url's found then return
				if(instance.getQued_urls().isEmpty())
				{
					
				} else
				
				//auto save		
				if(instance.getScraped_urls().size() > 20000) //save to a new file every time we reach a 20,000
				{
					instance.getSave_thread().forceSave();
					Thread.sleep(3000);// sleep for 3 seconds while saving
				} else
					
				//when we scrape 400,000, renew everything 
			
					if(instance.getQued_urls().size() > 100000)
					{
						instance.getRenew_thread().forceRenewal();
					} else
				//que next
				{
					queNext();
				}
				Thread.sleep(que_wait);
			}
		} catch(Exception io)
		{
			io.printStackTrace();
		}
	}

	public void queNext()
	{	
		try
		{
			if(instance.getQued_urls().isEmpty())
			{
				return;
			}
			
			//pick the first entry that is que'd
			FetchUrlData fetchUrlData = instance.getQued_urls().get(0);
			
			//check to see if the depth is okay, if its too high then return
			if(fetchUrlData.getLevel() >= instance.getDepth())
			{
				//first remove the url from list then re run this method to make up for the loss, and return
				instance.getQued_urls().remove(0);
				queNext();
				return;
			}
			
			//everything is good lets start up new thread
			
			//remove the entry from the que so it doesn't run twice
			instance.getQued_urls().remove(0);
			
			//set up the new fetch thread, add it to running threads with thread id, start fetch thread.
			FetchUrlThread fut = new FetchUrlThread(instance, fetchUrlData.getUrl(), fetchUrlData.getLevel());
			
			instance.getRunning_threads().put(fut, fut.getId());
			
			//update the stats with new thread count (save memory and ignore this)
			instance.updateStats();
			
			fut.start();
			
		} catch(Exception io)
		{
			io.printStackTrace();
		}
	}
}
