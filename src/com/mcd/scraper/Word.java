package com.mcd.scraper;

public class Word implements Comparable<Word>{
	
	private int count;
	private String word;
	
	public Word(String word, int count) {
		this.word = word;
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public void increment(){
		this.count = count+1;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return word.equals(((Word)obj).word);
	}
	
	@Override
	public int compareTo(Word w) {
		return w.count - count;
	}
}
