package com.rimidalv111.scrapebox;

public class RenewalThread extends Thread
{
	private ScraperBot instance;
	
	public RenewalThread(ScraperBot i)
	{
		instance = i;
	}
	
	public void forceRenewal()
	{
		instance.setRenewal_in_progress(true);
		
		instance.setNumber_threads(800);
		instance.getScraped_urls().clear();
		instance.getRunning_threads().clear();
		instance.getUrl_count().clear();
		instance.getQued_urls().clear();
		instance.setRenewal_in_progress(false);
	}
}
