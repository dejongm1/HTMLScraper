package com.mcd.scraper;

import java.io.IOException;
import java.net.InetAddress;

public class HTMLScraperUtil {
	
	public static boolean offline(){
		try {
			return (Boolean.valueOf(System.getProperty("runInEclipse")) || !InetAddress.getByName("google.com").isReachable(5000));
		} catch (IOException e) {
			return true;
		}
	}
}
