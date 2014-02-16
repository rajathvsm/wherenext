package com.hacktech.wherenext;

import android.content.Context;
import it.gmariotti.cardslib.library.internal.Card;

public class CustomCard extends Card {

	
	private long startTime;
	public CustomCard(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		startTime = System.currentTimeMillis();
	}
	
	@Override
	public void onSwipeCard() {
		// TODO Auto-generated method stub
		super.onSwipeCard();

	}
	
	public long getTime(){
		return startTime;
	}
	
	

}
