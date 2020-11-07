package com.rimidalv111.scrapebox;

public class FetchUrlData
{
	private String url;
	private int level;

	public FetchUrlData(String u, int l)
	{
		url = u;
		level = l;
	}

	public String getUrl()
	{
		return url;
	}

	public int getLevel()
	{
		return level;
	}
}
