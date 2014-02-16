/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hacktech.wherenext;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
import it.gmariotti.cardslib.library.internal.Card.OnLongCardClickListener;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This example illustrates a common usage of the DrawerLayout widget in the
 * Android support library.
 * <p/>
 * <p>
 * When a navigation (left) drawer is present, the host activity should detect
 * presses of the action bar's Up affordance as a signal to open and close the
 * navigation drawer. The ActionBarDrawerToggle facilitates this behavior. Items
 * within the drawer should fall into one of two categories:
 * </p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic
 * policies as list or tab navigation in that a view switch does not create
 * navigation history. This pattern should only be used at the root activity of
 * a task, leaving some form of Up navigation active for activities further down
 * the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an
 * alternate parent for Up navigation. This allows a user to jump across an
 * app's navigation hierarchy at will. The application should treat this as it
 * treats Up navigation from a different task, replacing the current task stack
 * using TaskStackBuilder or similar. This is the only form of navigation drawer
 * that should be used outside of the root activity of a task.</li>
 * </ul>
 * <p/>
 * <p>
 * Right side drawers should be used for actions, not navigation. This follows
 * the pattern established by the Action Bar that navigation should be to the
 * left and actions to the right. An action should be an operation performed on
 * the current contents of the window, for example enabling or disabling a data
 * overlay on top of the current content.
 * </p>
 */
public class MainActivity extends FragmentActivity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	public static ArrayList<Place> places = new ArrayList<Place>();
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mCategoryTitles;

	private static final String ID = "7yivJNkpxgFJYms5";

	private LocationManager locManager;
	private String locProvider;
	
	public static int currentHour;
	private static Location curLoc;

	public static List<NameValuePair> data;
	public static List<String> idList = new ArrayList<String>();
	public static boolean flag = false;
	public static long spentTime;
	public static String category;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria crit = new Criteria();
		locProvider = locManager.getBestProvider(crit, false);
		curLoc = locManager.getLastKnownLocation(locProvider);

		if (curLoc != null) {
			System.out.println("LAT: " + curLoc.getLatitude());
			System.out.println("LNG: " + curLoc.getLongitude());

			// write all data
			data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("id", ID));
			// data.add(new BasicNameValuePair("log", place.getCategory()));
			data.add(new BasicNameValuePair("curLng", Double.toString(curLoc
					.getLongitude())));
			data.add(new BasicNameValuePair("curLat", Double.toString(curLoc
					.getLatitude())));

			try {
				new HTTPPostTask().execute();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			System.err.println("Location could not be retrieved.");
		}

		mTitle = mDrawerTitle = getResources().getString(R.string.app_name);
		getActionBar().setTitle(mTitle);
		mCategoryTitles = getResources().getStringArray(R.array.category_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mCategoryTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				// getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				// getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			selectItem(0);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_websearch:
			// create intent to perform web search for this planet
			Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
			intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
			// catch event that there's no activity to handle intent
			if (intent.resolveActivity(getPackageManager()) != null) {
				startActivity(intent);
			} else {
				Toast.makeText(this, R.string.app_not_available,
						Toast.LENGTH_LONG).show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		// update the main content by replacing fragments
		Fragment fragment = new PlanetFragment();
		Bundle args = new Bundle();
		args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
		fragment.setArguments(args);

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment).commit();

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mCategoryTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		// mTitle = title;
		// getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/**
	 * Fragment that appears in the "content_frame", shows a planet
	 */
	static ArrayList<Card> cards;

	public static class PlanetFragment extends Fragment {
		public static final String ARG_PLANET_NUMBER = "planet_number";
	
		private OnCardClickListener clickListener

		= new OnCardClickListener() {

			@Override
			public void onClick(Card card, View view) {
				spentTime = System.currentTimeMillis()
						- ((CustomCard) card).getTime();

				sendTimeBackToServer(spentTime, card.getCardExpand().getTitle());

			}

		};

		public PlanetFragment() {
			// Empty constructor required for fragment subclasses
		}

		protected static void sendTimeBackToServer(Long spentTime,
				String categoryLocal) {

			Calendar c = new Calendar() {

				@Override
				public void add(int field, int value) {
					// TODO Auto-generated method stub

				}

				@Override
				protected void computeFields() {
					// TODO Auto-generated method stub

				}

				@Override
				protected void computeTime() {
					// TODO Auto-generated method stub

				}

				@Override
				public int getGreatestMinimum(int field) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public int getLeastMaximum(int field) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public int getMaximum(int field) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public int getMinimum(int field) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public void roll(int field, boolean increment) {
					// TODO Auto-generated method stub

				}

			};
			currentHour = c.get(Calendar.HOUR_OF_DAY);
			category = categoryLocal;
			Log.e("DATA SENT", data.toString());
			try {
				new HTTPGetTask().execute();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onActivityCreated(savedInstanceState);

			CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(
					getActivity(), cards);

			CardListView listView = (CardListView) getActivity().findViewById(
					R.id.myList1);
			if (listView != null) {
				listView.setAdapter(mCardArrayAdapter);
			}
			int i = getArguments().getInt(ARG_PLANET_NUMBER);
			String category = getResources().getStringArray(
					R.array.category_array)[i];

			getActivity().getActionBar().setSubtitle(category);

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_planet,
					container, false);

			cards = new ArrayList<Card>();

			for (Place p1 : places) {

				// Create a Card
				CustomCard card = new CustomCard(getActivity());
				CardExpand expand = new CardExpand(getActivity());
				// Create a CardHeader
				CardHeader header = new CardHeader(getActivity());
				String phone;
				if(p1.getPhone()!=null)
					phone="\nPhone: "+p1.getPhone();
				else
					phone="";
				header.setTitle(p1.getName() + "\n" + Float.toString(p1.getDistance()).substring(0, 4)+" mi.");
				card.setTitle(p1.getAddress()+ phone);

				
				card.setExpanded(true);
				// Add Header to card
				card.addCardHeader(header);
				expand.setTitle(p1.getLat()+"\",\"");
				card.addCardExpand(expand);
				card.setSwipeable(true);
				cards.add(card);

				card.setOnClickListener(clickListener);
				card.setOnLongClickListener(new OnLongCardClickListener() {
					
					@Override
					public boolean onLongClick(Card card, View view) {
						String name = card.getCardHeader().getTitle();
					String address =card.getTitle();
						String latlong=card.getCardExpand().getTitle();
					final Intent intent = new Intent(Intent.ACTION_VIEW,
							Uri.parse("http://maps.google.com/maps?" 
							+ "&daddr="+address ));

							intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");

							startActivity(intent); 
						return false;
					}
				});
				
			}

			return rootView;
		}
	}
}

class HTTPPostTask extends AsyncTask<Void, Void, Void> {
	private static final String URL = "http://untravel.azurewebsites.net/untravel-get.php";
	private static final int FT_PER_MI = 5280;
	private boolean flag = false;

	@Override
	protected Void doInBackground(Void... params) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(URL);
		flag = false;
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(MainActivity.data));
			HttpResponse response = httpClient.execute(httpPost);
			if (response != null) {
				System.out.println("Response received.");
				JSONArray venues = null;
				HttpEntity entity = response.getEntity();
				InputStream input = null;
				String result = null;

				input = entity.getContent();
				// json is UTF-8 by default
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(input, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();

				String line = null;
				while ((line = reader.readLine()) != null)
					sb.append(line + "\n");

				System.err.println("STAGE ONE.");
				result = sb.toString();
				System.err.println("RESULT: " + result);

				// begin parsing JSON
				try {
					// System.err.println(json.toString());
					venues = new JSONArray(result);
					JSONObject area = null, location = null, cat = null;// img
																		// =
																		// null;
					String address = "";
					MainActivity.places = new ArrayList<Place>();
					for (int i = 0; i < venues.length(); i++) {
						area = venues.getJSONObject(i);
						location = area.getJSONObject("location");

						Place place = new Place();
						if (area.has("name"))
							place.setName(area.getString("name"));
						if (area.has("id"))
							place.setID(area.getString("id"));

						address = "";
						if (location.has("address"))
							address += location.getString("address");
						if (location.has("city"))
							address += ", " + location.getString("city");
						if (location.has("state"))
							address += ", " + location.getString("state");
						place.setAddress(address);

						if (area.has("contact")
								&& area.getJSONObject("contact").has(
										"formattedPhone"))
							place.setPhone(area.getJSONObject("contact")
									.getString("formattedPhone"));
						if (area.has("url"))
							place.setUrl(area.getString("url"));
						if (area.has("rating"))
							place.setRating(area.getDouble("rating"));
						place.setLng(location.getDouble("lng"));
						place.setLat(location.getDouble("lat"));
						place.setDistance((float) location.getInt("distance")
								/ FT_PER_MI);

						if (area.has("categories")
								&& area.getJSONArray("categories").length() > 0) {
							cat = area.getJSONArray("categories")
									.getJSONObject(0);
							place.setCategory(cat.getString("name"));
						}

						System.out.println(place.getName());
						MainActivity.places.add(place);
						// img = cat.getJSONObject("icon");
						// place.setImg(img.getString("prefix") + "88" +
						// img.getString("suffix"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			} else
				System.out.println("Response NULL.");
			flag = true;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}

class HTTPGetTask extends AsyncTask<Void, Void, Void> {

	@Override
	protected Void doInBackground(Void... params) {
		HttpClient httpClient = new DefaultHttpClient();

		String URL = "https://testtttt.azure-mobile.net/api/post?userid="
				+ "7yivJNkpxgFJYms5" + "&category=" + URLEncoder.encode(MainActivity.category)
				+ "&hour=" + (MainActivity.currentHour) + "&time_spent="
				+ MainActivity.spentTime;

		HttpGet httpGet = new HttpGet(URL);
		try {
			httpClient.execute(httpGet);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

}
