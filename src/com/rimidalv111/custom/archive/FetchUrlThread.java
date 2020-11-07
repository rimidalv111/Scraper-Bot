package com.rimidalv111.custom.archive;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.rimidalv111.scrapebox.FetchUrlData;
import com.rimidalv111.scrapebox.ScraperBot;

//## custom fetch change this import
import com.rimidalv111.custom.archive.FetchUrlThread;

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
							//####  filter only web archive pages  ###
							//off for testing
							if(f.contains("web.archive.org/web/"))
							{
								//make sure the date is good ex: 20050210152521

								String[] tailingUrlInfo = f.split("/web/");

								if(tailingUrlInfo[1].startsWith("2018") || tailingUrlInfo[1].startsWith("2017"))
								{
									break; //dont even que, just die
								}

								//now filter make sure we only add x amount of links per one domain
								String archivedUrl = tailingUrlInfo[1].substring(tailingUrlInfo[1].indexOf("/") + 1, tailingUrlInfo[1].length());	
								
								//System.out.println("Archived url: " + archivedUrl);
								
								int url_scraped_count = 0;
								
								//see how many times we already scraped this root domain
								if(instance.getUrl_count().containsKey(archivedUrl))
								{
									url_scraped_count = instance.getUrl_count().get(archivedUrl);
								}
								
								//only scrape 500 of each archived url
								if(url_scraped_count < 2)
								{									
									//update root domain URL count
									instance.getUrl_count().put(archivedUrl, url_scraped_count + 1);
									
									
									//add the domain to scraped urls (this is the file we save)
									instance.getScraped_urls().add(f); 
		
									System.out.println("Found: " + f);
									
									
									//don't add soon to be found out of depth URL's to que
									if(!(level >= instance.getDepth()))
									{
										instance.getQued_urls().add(new FetchUrlData(f, level));
									}
								
								}

							}

						}

					} catch(Exception io)
					{
						//out of bounds just ignore
						io.printStackTrace();
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
		} catch(Exception io)
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
