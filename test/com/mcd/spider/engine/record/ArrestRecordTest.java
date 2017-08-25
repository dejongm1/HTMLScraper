package com.mcd.spider.engine.record;

import com.mcd.spider.entities.record.ArrestRecord;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Michael De Jong
 *
 */

public class ArrestRecordTest {

	@BeforeTest
	public void setUp() {
		
	}

	@AfterTest
	public void tearDown() {
		
	}

	@Test
	public void testMerge() {
		//multiple of these
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testMatch() {
		//Arlena_Ramirez_34029315	Arlena  Ramirez	Arlena		Ramirez	Aug-20-2017 04:09 AM		28	Female	Urbandale	Iowa	Polk	5'05"	200 lbs	Black	Brown		#1 ASSAULT CAUSING BODILY INJURY OR MENTAL ILLNESS STATUTE: SR308623 BOND: $1000; 		
		//115922	ARLENA RAMIREZ				Aug-20-2017 04:09 AM		28	Female			Polk	5 foot, 5 inches	200 pounds	Black	Brown		ASSAULT CAUSING BODILY INJURY OR MENTAL ILLNESS;
		
		//use readRowIntoRecord once a test case is written for it
		ArrestRecord one = new ArrestRecord();
		one.setId("Arlena_Ramirez_34029315");
		one.setFullName("Arlena  Ramirez");
		one.setFirstName("Arlena");
		one.setLastName("Ramirez");
		Calendar arrestDate = Calendar.getInstance();
		Date date = new Date("Aug-20-2017");
		arrestDate.setTime(date);
		String arrestTimeText = "04:09 AM";
        arrestDate.set(Calendar.HOUR, Integer.parseInt(arrestTimeText.substring(0, arrestTimeText.indexOf(':'))));
        arrestDate.set(Calendar.MINUTE, Integer.parseInt(arrestTimeText.substring(arrestTimeText.indexOf(':')+1, arrestTimeText.indexOf(' '))));
        arrestDate.set(Calendar.AM, arrestTimeText.substring(arrestTimeText.indexOf(' ')+1)=="AM"?1:0);
		one.setArrestDate(arrestDate);
		one.setArrestAge(28);
		one.setGender("Female");
		one.setCity("Urbandale");
		one.setState("Iowa");
		one.setCounty("Polk");
		one.setHeight("5'05");
		one.setWeight("200 lbs");
		one.setHairColor("Black");
		one.setEyeColor("Brown");
		one.setCharges(new String[]{"#1 ASSAULT CAUSING BODILY INJURY OR MENTAL ILLNESS STATUTE: SR308623 BOND: $1000;"});
		
		ArrestRecord two = new ArrestRecord();
		two.setId("115922");
		two.setFullName("ARLENA RAMIREZ");
		two.setFirstName(null);
		two.setLastName(null);
		arrestDate = Calendar.getInstance();
		date = new Date("Aug-20-2017");
		arrestDate.setTime(date);
		arrestTimeText = "04:09 AM";
        arrestDate.set(Calendar.HOUR, Integer.parseInt(arrestTimeText.substring(0, arrestTimeText.indexOf(':'))));
        arrestDate.set(Calendar.MINUTE, Integer.parseInt(arrestTimeText.substring(arrestTimeText.indexOf(':')+1, arrestTimeText.indexOf(' '))));
        arrestDate.set(Calendar.AM, arrestTimeText.substring(arrestTimeText.indexOf(' ')+1)=="AM"?1:0);
        two.setArrestDate(arrestDate);
        two.setArrestAge(28);
        two.setGender("FEMALE");
        two.setCity(null);
        two.setState(null);
        two.setCounty("POLK ");
        two.setHeight("5 foot, 5 inches");
        two.setWeight("200 pounds");
        two.setHairColor("black");
        two.setEyeColor("brown");
        two.setCharges(new String[]{"ASSAULT CAUSING BODILY INJURY OR MENTAL ILLNESS;"});
        
        Assert.assertTrue(one.matches(two));
        Assert.assertTrue(two.matches(one));
	}
	

}
