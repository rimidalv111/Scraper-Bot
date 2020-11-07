package com.rimidalv111.scrapebox;

public class CatchUpThread extends Thread
{
	private ScraperBot instance;

	public CatchUpThread(ScraperBot i)
	{
		instance = i;
	}

	public void run()
	{
		//every 10 seconds check to see if we need to start more threads
		try
		{
			while(true)
			{
				//if we have available threads (if we drop down 100 thread difference then catch up)
				if((instance.getNumber_threads() - instance.getRunning_threads().size()) > 100)
				{
					//System.out.println("running catch up");
					runCatchUp();
				}
				
				Thread.sleep(2000);
				//run this method again to check every 10 seconds			
			}
		} catch (Exception io)
		{
			
		}
	}
	
	public void runCatchUp()
	{
		instance.setCatch_up_in_progress(true);
		
		//if the que is empty ignore catch up
		if(instance.getQued_urls().isEmpty())
		{
			return;
		}
		
		//find the difference in running / max threads
		int startThisManyThreads = instance.getNumber_threads() - instance.getRunning_threads().size();
		
		//start this many threads
		for(int i = 0; i < startThisManyThreads; i++)
		{
			//just fire the que next to que more threads (as long as everything passes)
			instance.getQue_next_thread().queNext();
		}
		
		instance.setCatch_up_in_progress(false);
	}
}
