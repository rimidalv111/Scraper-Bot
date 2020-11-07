package com.rimidalv111.scrapebox;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

//## custom fetch change this import
import com.rimidalv111.custom.archive.FetchUrlThread;

public class ScraperBot extends JFrame
{
	/**
	 * Gui Variables
	 */
	private static final long serialVersionUID = 1L;
	private static ScraperBot instance;
	private JPanel contentPane;
	private JLabel lblScrapedUrls;
	private JLabel thin_console;
	private JButton start_button;
	private JButton save_button;
	private JButton clear_button;
	private JEditorPane console;
	private JScrollPane console_scrolle_pane;
	private JTextField search_field;
	private JLabel lblThreads;

	/**
	 * StartThread Variables
	 */
	private int number_threads = 100; //how many threads total
	private boolean renewal_in_progress = false;
	private boolean currently_saving = false;
	private boolean catch_up_in_progress = false;
	
	//all scraped urls found
	private ArrayList<String> scraped_urls = new ArrayList<String>();
	
	private int depth = 10; //how deep do you want the scraping to go: link --> link --> link = 2

	private HashMap<FetchUrlThread, Long> running_threads = new HashMap<FetchUrlThread, Long>(); //url, thread id

	private HashMap<String, Integer> url_count = new HashMap<String, Integer>(); //raw domain, count | used to only scrape 1 domain x amount of times
	
	private ArrayList<FetchUrlData> qued_urls = new ArrayList<FetchUrlData>(); //what url's are qued to be processed
	
	private QueNextThread que_next_thread;
	private SaveThread save_thread;
	private RenewalThread renew_thread;
	private CatchUpThread catch_up_thread;
	
	//split incoming values if bulk
	private String[] seedUrl = new String[]{};
	
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					ScraperBot frame = new ScraperBot();
					frame.setVisible(true);
					instance = frame;
				} catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ScraperBot()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 672, 250);
		setTitle("Scraper Bot | Monarch");
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		console_scrolle_pane = new JScrollPane();
		console_scrolle_pane.setBounds(10, 229, 634, 183);
		//contentPane.add(console_scrolle_pane);

		console = new JEditorPane();
		console_scrolle_pane.setViewportView(console);

		save_button = new JButton("Save");
		save_button.setBounds(18, 11, 226, 58);
		save_button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				save_thread.forceSave();
				//save current urls
			}
		});
		contentPane.add(save_button);

		start_button = new JButton("Start");
		start_button.setBounds(418, 11, 226, 58);
		start_button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//System.out.println("Starting...");
				String seeds = search_field.getText();
				
				//handle bulk searches
				if(seedUrl.length == 0)
				{
					if(seeds.contains(","))
					{
						seedUrl = seeds.split(",");
						seeds = seedUrl[0];
						seedUrl[0] = "completed";
					}
				}
				
				(new FetchUrlThread(instance, seeds, 0)).start();
				//save current urls
			}
		});
		contentPane.add(start_button);

		clear_button = new JButton("Renew");
		clear_button.setBounds(254, 11, 154, 58);
		clear_button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//				save_thread.save(); //first save then clear all fields (while it still runs)
				//				url_count.clear();
				//				scraped_urls.clear();
				//				threads.clear();
				//				start_helper_amount = 500;

				//renew_thread.runRenewal();
				
				catch_up_thread.runCatchUp();
			}
		});
		contentPane.add(clear_button);

		lblScrapedUrls = new JLabel("Scraped Stats: 0");
		lblScrapedUrls.setBounds(18, 142, 456, 20);
		contentPane.add(lblScrapedUrls);

		thin_console = new JLabel("waiting...");
		thin_console.setBounds(18, 173, 356, 20);
		contentPane.add(thin_console);

		search_field = new JTextField("");
		search_field.setBounds(136, 109, 356, 20);
		contentPane.add(search_field);
		search_field.setColumns(10);

		JButton threads_plus_btn = new JButton("+");
		threads_plus_btn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				number_threads = number_threads + 100;
				lblThreads.setText("Threads: " + number_threads);
				lblThreads.validate();
				lblThreads.repaint();

				if(!catch_up_in_progress)
				{
					catch_up_thread.runCatchUp();
				}
			}
		});
		threads_plus_btn.setBounds(71, 80, 43, 23);
		contentPane.add(threads_plus_btn);

		JButton thread_minus_tn = new JButton("-");
		thread_minus_tn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				number_threads = number_threads - 100;
				lblThreads.setText("Threads: " + number_threads);
				lblThreads.validate();
				lblThreads.repaint();
			}
		});
		thread_minus_tn.setBounds(18, 80, 43, 23);
		contentPane.add(thread_minus_tn);

		lblThreads = new JLabel("Threads: " + number_threads);
		lblThreads.setBounds(126, 84, 446, 14);
		contentPane.add(lblThreads);

		//initialize all classes
		(que_next_thread = (new QueNextThread(this, 50))).start();
		(catch_up_thread = (new CatchUpThread(this))).start();
		(save_thread = (new SaveThread(this))).start();
		(renew_thread = (new RenewalThread(this))).start();
		
	}

	public void updateStats()
	{
		lblScrapedUrls.setText("Scraped URL's: " + scraped_urls.size() + " Threads: " + running_threads.size() + "/" + number_threads + " Que: " + qued_urls.size());

		lblScrapedUrls.validate();
		lblScrapedUrls.repaint();
		contentPane.validate();
		contentPane.repaint();
		
		//check if next job needs to run
		if(running_threads.size() == 0)
		{
			if(seedUrl.length >= 1)
			{
				String nextUrl = "";
				
				for(int i = 0; i < seedUrl.length; i++)
				{
					if(!seedUrl[i].equalsIgnoreCase("completed"))
					{
						nextUrl = seedUrl[i];
						break;
					}
				}
				
				if(nextUrl.isEmpty())
				{
					System.out.println("Starting next search...");
					(new FetchUrlThread(instance, nextUrl, 0)).start();
					
				}
			}
		}
	}

	public void updateThinConsole(String s)
	{
		thin_console.setText(s);
		thin_console.validate();
		thin_console.repaint();
	}

	/**
	 * Will take a url such as http://www.stackoverflow.com and return www.stackoverflow.com
	 * 
	 * @param url
	 * @return
	 */
	private String getHost(String url)
	{
		if(url == null || url.length() == 0)
			return "";

		int doubleslash = url.indexOf("//");
		if(doubleslash == -1)
			doubleslash = 0;
		else
			doubleslash += 2;

		int end = url.indexOf('/', doubleslash);
		end = end >= 0 ? end : url.length();

		int port = url.indexOf(':', doubleslash);
		end = (port > 0 && port < end) ? port : end;

		return url.substring(doubleslash, end);
	}

	/**
	 * Based on : http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/2.3.3_r1/android/webkit/CookieManager.java#CookieManager.getBaseDomain%28java.lang.String%29
	 * Get the base domain for a given host or url. E.g. mail.google.com will return google.com
	 * 
	 * @param host
	 * @return
	 */
	public String getBaseDomain(String url)
	{
		String host = getHost(url);

		int startIndex = 0;
		int nextIndex = host.indexOf('.');
		int lastIndex = host.lastIndexOf('.');
		while (nextIndex < lastIndex)
		{
			startIndex = nextIndex + 1;
			nextIndex = host.indexOf('.', startIndex);
		}
		if(startIndex > 0)
		{
			return host.substring(startIndex);
		} else
		{
			return host;
		}
	}

	public HashMap<String, Integer> getUrl_count()
	{
		return url_count;
	}

	public void setUrl_count(HashMap<String, Integer> url_count)
	{
		this.url_count = url_count;
	}

	public ArrayList<String> getScraped_urls()
	{
		return scraped_urls;
	}

	public void setScraped_urls(ArrayList<String> scraped_urls)
	{
		this.scraped_urls = scraped_urls;
	}

	public int getDepth()
    {
    	return depth;
    }

	public void setDepth(int depth)
    {
    	this.depth = depth;
    }

	public int getNumber_threads()
    {
    	return number_threads;
    }

	public void setNumber_threads(int number_threads)
    {
    	this.number_threads = number_threads;
    }

	public HashMap<FetchUrlThread, Long> getRunning_threads()
    {
    	return running_threads;
    }

	public void setRunning_threads(HashMap<FetchUrlThread, Long> running_threads)
    {
    	this.running_threads = running_threads;
    }

	public SaveThread getSave_thread()
    {
    	return save_thread;
    }

	public void setSave_thread(SaveThread save_thread)
    {
    	this.save_thread = save_thread;
    }

	public RenewalThread getRenew_thread()
    {
    	return renew_thread;
    }

	public void setRenew_thread(RenewalThread renew_thread)
    {
    	this.renew_thread = renew_thread;
    }

	public ArrayList<FetchUrlData> getQued_urls()
    {
    	return qued_urls;
    }

	public void setQued_urls(ArrayList<FetchUrlData> qued_urls)
    {
    	this.qued_urls = qued_urls;
    }

	public boolean isRenewal_in_progress()
    {
    	return renewal_in_progress;
    }

	public void setRenewal_in_progress(boolean renewal_in_progress)
    {
    	this.renewal_in_progress = renewal_in_progress;
    }

	public boolean isCurrently_saving()
    {
    	return currently_saving;
    }

	public void setCurrently_saving(boolean currently_saving)
    {
    	this.currently_saving = currently_saving;
    }

	public QueNextThread getQue_next_thread()
    {
	    return que_next_thread;
    }

	public QueNextThread setQue_next_thread(QueNextThread que_next_thread)
    {
	    this.que_next_thread = que_next_thread;
	    return que_next_thread;
    }

	public boolean isCatch_up_in_progress()
    {
	    return catch_up_in_progress;
    }

	public void setCatch_up_in_progress(boolean catch_up_in_progress)
    {
	    this.catch_up_in_progress = catch_up_in_progress;
    }
}
