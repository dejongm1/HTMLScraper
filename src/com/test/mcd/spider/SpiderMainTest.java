package com.test.mcd.spider;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SpiderMainTest {
	@BeforeMethod
	public void beforeMethod() {
	}

	@AfterMethod
	public void afterMethod() {
	}
	
	@Test
	public void f() {
	}
	/**
	 * Test no inputs
	 * 
	 * For each scrape type
	 * 		Test 1 input
	 * 		Test 2 inputs
	 * 		Test 3 inputs
	 * 
	 * Test all variations of state input
	 * 		TX, IA, IL
	 * 		IL, AI
	 * 		RI, IA 
	 * 		All
	 * 		All, IA, IL
	 * 
	 * Test online
	 * Test offline
	 * Test sorting
	 * 
	 * 	Test validators: positive and negative
	 * 
	 * Test scraping results with offline pages
	 * 
	 * Test for vague results for online pages, to verify HTML structure unchanged
	 * 
	 */

}
