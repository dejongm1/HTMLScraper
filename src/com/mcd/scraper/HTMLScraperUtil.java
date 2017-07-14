package com.mcd.scraper;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

public class HTMLScraperUtil {
	
	private HTMLScraperUtil(){}

	public static boolean offline(){
		try {
			return (Boolean.valueOf(System.getProperty("runInEclipse")) || !InetAddress.getByName("google.com").isReachable(5000));
		} catch (IOException e) {
			return true;
		}
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
		List<Map.Entry<K, V>> list = new LinkedList<>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				return ( o1.getValue() ).compareTo( o2.getValue() );
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}
}
