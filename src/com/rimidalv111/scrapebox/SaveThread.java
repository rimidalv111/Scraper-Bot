package com.rimidalv111.scrapebox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SaveThread extends Thread
{
	private ScraperBot instance;
	
	public SaveThread(ScraperBot i)
	{
		instance = i;
	}

	public void forceSave()
	{
		//surround code with saving boolean
		instance.setCurrently_saving(true);
		
		ArrayList<String> to_save_urls = new ArrayList<String>(); //copy scraped URL's over
		to_save_urls.addAll(instance.getScraped_urls());

		instance.getScraped_urls().clear(); // clean the scrapped urls

		//save the scraped urls in text file
		DateFormat df = new SimpleDateFormat("HH-mm-ss");
		Date dateobj = new Date();

		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter((new File(".")).getAbsolutePath() + "/urls_" + df.format(dateobj) + ".txt"));

			for(String k : to_save_urls)
			{
				writer.write(k + "\r\n");
			}
		} catch(Exception e)
		{
			try
			{
				writer.close();
			} catch(Exception ee)
			{
				//e.printStackTrace();
			}
		} finally
		{
			try
			{
				writer.close();
			} catch(Exception e)
			{
				//e.printStackTrace();
			}
		}
		
		instance.setCurrently_saving(false);
	}
}
