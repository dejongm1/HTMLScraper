package com.mcd.spider.exception;

import com.mcd.spider.entities.record.State;

/**
 * 
 * @author Michael De Jong
 *
 */

public class StateNotReadyException extends SpiderException {
	
	private static final long serialVersionUID = 1L;
	
	private State state;

	public StateNotReadyException(State state) {
		this.state = state;
	}
	
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
	
	

}
