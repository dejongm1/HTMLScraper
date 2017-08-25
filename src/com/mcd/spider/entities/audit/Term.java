package com.mcd.spider.entities.audit;

public class Term implements Comparable<Term>{
	
	private int count;
	private String word;
	
	public Term(String word, int count) {
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
		return word.equals(((Term)obj).word);
	}
	
	public boolean generousMatch(Object obj)
	{
		//match variations of a term, or do I need to put this in the equals() or a new object
		return word.equals(((Term)obj).word);
	}
	
	@Override
	public int compareTo(Term t) {
		return t.getCount() - count;
	}
}
