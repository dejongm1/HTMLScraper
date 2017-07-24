package com.mcd.spider.main.exception;

import com.mcd.spider.main.entities.record.State;

/**
 * 
 * @author u569220
 *
 */

public class StateNotReadyException extends Exception {
	
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
