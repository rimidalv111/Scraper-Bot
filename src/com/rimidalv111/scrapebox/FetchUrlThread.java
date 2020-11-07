package com.rimidalv111.scrapebox;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class FetchUrlThread extends Thread
{
	private ScraperBot instance;
	private String currentUrl;
	private int level = 0;
	
	private URL url = null;
	private BufferedReader in = null;

	public FetchUrlThread(ScraperBot i, String u, int l)
	{
		//initialize variables
		instance = i;
		currentUrl = u;
		level = l + 1;
	}

	public void run()
	{
		/*
		 * Connect and retrieve HTML code from URL
		 */
		try
		{
			// Create a URL for the desired page
			url = new URL(currentUrl);

			// Read all the text returned by the server
			in = new BufferedReader(new InputStreamReader(url.openStream()));
		} catch(Exception io) 
		{
			//couldn't make connection
			finalizeThread();
			return;
		}
		if(in == null || url == null) //not valid link so dispose this thread
		{
			finalizeThread();
			return;
		}

		/*
		 * Read HTML code and save the found URL's
		 */
		try
		{
			String str;
			while ((str = in.readLine()) != null)
			{
				str = str.toString();

				if(str.contains("href="))
				{
					String raw = str;

					String[] sp = raw.split("href=\"");

					try
					{
						String[] q = sp[1].split("\"");

						String f = q[0];

						if((f.startsWith("https://") || f.startsWith("http://") || f.startsWith("www."))) //we only want href's with these links
						{

							//only filter popular TLD's
							if(f.contains(".com") || f.contains(".net") || f.contains(".org") || f.contains(".edu") || f.contains(".gov") || f.contains(".biz") || f.contains(".mil") || f.contains(".info"))
							{
								//filter only one domain
								if(f.contains("drgapin"))
								{
									//get the trimmed root of the current found URL (ex. google.com)
									String trimmedRoot = instance.getBaseDomain(f);
									
									/*
									 * We only want to scrape 1 domain x amount of times
									 */
	
									int url_scraped_count = 0;
	
									//see how many times we already scraped this root domain
									if(instance.getUrl_count().containsKey(trimmedRoot))
									{
										url_scraped_count = instance.getUrl_count().get(trimmedRoot);
									}
									
									//only scrape 500 of each root domain
									if(url_scraped_count < 500)
									{									
										//update root domain URL count
										instance.getUrl_count().put(trimmedRoot, url_scraped_count + 1);
	
										//add the root domain to scraped urls (this is the file we save)
										instance.getScraped_urls().add(f); //for ONLY DOMAINS - instance.getScraped_urls().add(trimmedRoot);
										
										//add the full URL to the qued URL's list (not root domain because we are still searching these URL's for new ones)
										
										//don't add soon to be found out of depth URL's to que
										if(!(level >= instance.getDepth()))
										{
											instance.getQued_urls().add(new FetchUrlData(f, level));
										}
									}
								}
							}

						}

					} catch(Exception io)
					{
						//out of bounds just ignore
					}
				}
			}
		} catch(MalformedURLException e)
		{
			//e.printStackTrace();
		} catch(IOException e)
		{
			//e.printStackTrace();
		}
		//finalize thread after all loops
		finalizeThread();
	}

	private void finalizeThread()
	{
		//remove this thread from running threads
		instance.getRunning_threads().remove(this);
		
		instance.updateStats();
		
		instance.updateThinConsole("Completed: " + currentUrl);
		try
		{
			in.close();
		} catch( Exception io)
		{
			//couldn't close buffered reader
		}
		return;
	}

	public ScraperBot getInstance()
    {
    	return instance;
    }

	public void setInstance(ScraperBot instance)
    {
    	this.instance = instance;
    }

	public String getCurrentUrl()
    {
    	return currentUrl;
    }

	public void setCurrentUrl(String currentUrl)
    {
    	this.currentUrl = currentUrl;
    }

	public URL getUrl()
    {
    	return url;
    }

	public void setUrl(URL url)
    {
    	this.url = url;
    }

	public BufferedReader getIn()
    {
    	return in;
    }

	public void setIn(BufferedReader in)
    {
    	this.in = in;
    }
}
