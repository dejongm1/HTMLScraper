package com.mcd.spider.entities.record.filter;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;

public class RecordFilterTest {


	@Test
	public void findFilter_Alcohol() {
		RecordFilterEnum result = RecordFilterEnum.findFilter("ALCOHOL");

		Assert.assertEquals(result, RecordFilterEnum.ALCOHOL);
	}

	@Test
	public void findFilter_TrafficDifferentCase() {
		RecordFilterEnum result = RecordFilterEnum.findFilter("tRaFFic");

		Assert.assertEquals(result, RecordFilterEnum.TRAFFIC);
	}

	@Test
	public void findFilter_None() {
		RecordFilterEnum nullResult = RecordFilterEnum.findFilter(null);
		RecordFilterEnum emptyResult = RecordFilterEnum.findFilter("");
		RecordFilterEnum noneResult = RecordFilterEnum.findFilter("none");

		Assert.assertEquals(nullResult, RecordFilterEnum.NONE);
		Assert.assertEquals(emptyResult, RecordFilterEnum.NONE);
		Assert.assertEquals(noneResult, RecordFilterEnum.NONE);
	}

	@Test
	public void filter_Alcohol() {
		String matchingChargeOne = "#1: DUI";
		String matchingChargeTwo = "#OWI";
		String matchingChargeThree = "driving under the influence";
		String matchingChargeFour = "public consumption";
		String matchingChargeFive = "Operating While Intoxicated";
		String matchingChargeSix = "Pub. intox.";
		String matchingChargeSeven = "operating a car while drunk";

		String nonMatchingChargeOne = "OVI";
		String nonMatchingChargeTwo = "operating a car while stoned";
		String nonMatchingChargeThree = "dWU";
		String nonMatchingChargeFour = "assault and battery";

		Assert.assertTrue(RecordFilter.filter(matchingChargeOne, RecordFilterEnum.ALCOHOL));
		Assert.assertTrue(RecordFilter.filter(matchingChargeTwo, RecordFilterEnum.ALCOHOL));
		Assert.assertTrue(RecordFilter.filter(matchingChargeThree, RecordFilterEnum.ALCOHOL));
		Assert.assertTrue(RecordFilter.filter(matchingChargeFour, RecordFilterEnum.ALCOHOL));
		Assert.assertTrue(RecordFilter.filter(matchingChargeFive, RecordFilterEnum.ALCOHOL));
		Assert.assertTrue(RecordFilter.filter(matchingChargeSix, RecordFilterEnum.ALCOHOL));
		Assert.assertTrue(RecordFilter.filter(matchingChargeSeven, RecordFilterEnum.ALCOHOL));

		Assert.assertFalse(RecordFilter.filter(nonMatchingChargeOne, RecordFilterEnum.ALCOHOL));
		Assert.assertFalse(RecordFilter.filter(nonMatchingChargeTwo, RecordFilterEnum.ALCOHOL));
		Assert.assertFalse(RecordFilter.filter(nonMatchingChargeThree, RecordFilterEnum.ALCOHOL));
		Assert.assertFalse(RecordFilter.filter(nonMatchingChargeFour, RecordFilterEnum.ALCOHOL));
	}

	@Test
	public void filter_Traffic() {
		String matchingChargeOne = "failure to obey traffic signs";
		String matchingChargeTwo = "driving over the posted speed limit";
		String matchingChargeThree = "extreme speed";

		String nonMatchingChargeOne = "driving safely";
		String nonMatchingChargeTwo = "walking";

		Assert.assertTrue(RecordFilter.filter(matchingChargeOne, RecordFilterEnum.TRAFFIC));
		Assert.assertTrue(RecordFilter.filter(matchingChargeTwo, RecordFilterEnum.TRAFFIC));
		Assert.assertTrue(RecordFilter.filter(matchingChargeThree, RecordFilterEnum.TRAFFIC));

		Assert.assertFalse(RecordFilter.filter(nonMatchingChargeOne, RecordFilterEnum.TRAFFIC));
		Assert.assertFalse(RecordFilter.filter(nonMatchingChargeTwo, RecordFilterEnum.TRAFFIC));
	}
}
