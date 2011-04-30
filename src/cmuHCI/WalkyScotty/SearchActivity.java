package cmuHCI.WalkyScotty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;
import cmuHCI.WalkyScotty.entities.Building;
import cmuHCI.WalkyScotty.entities.Location;
import cmuHCI.WalkyScotty.entities.LocationType;
import cmuHCI.WalkyScotty.entities.Restaurant;
import cmuHCI.WalkyScotty.entities.Room;
import cmuHCI.WalkyScotty.entities.Service;

public class SearchActivity extends WSActivity {
	private final String[] PLACES = { "Buildings", "Food Places", "Rooms",
			"Services", "Other" };
	
	private String[][] SUBPLACES = new String[5][4];
	private Location[][] LOCATIONS = new Location[5][4];
	
	// List of items to give to the autocomplete text box

	private String[] AC_PLACES;
	
	private List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
    private List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
    private final String NAME = "NAME";

	// Search text box
	AutoCompleteTextView searchBox;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.search);
		
		Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      doSearch(query);
	    }
		
		// Set up browse menus
		ExpandableListView blv = (ExpandableListView) findViewById(R.id.browse_list);
		
		buildSubCategories();
        buildList();
        
        // Set up our adapter
        ExpandableListAdapter mAdapter = new SimpleExpandableListAdapter(
                this,
                groupData,
                android.R.layout.simple_expandable_list_item_1,
                new String[] { NAME },
                new int[] { android.R.id.text1},
                childData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] { NAME },
                new int[] { android.R.id.text1}
                );
        blv.setAdapter(mAdapter);
        
        blv.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
					if(childData.get(groupPosition).get(childPosition).containsValue("More..."))
						navigateMorePage(groupPosition);
					else
						navigateDetailsPage(LOCATIONS[groupPosition][childPosition]);
				return true;
			}
        	
        });
		
		// Set up autocomplete for search box
        buildSearchComponents();
		
		searchBox = (AutoCompleteTextView) findViewById(R.id.search_box);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.autocomplete_list_item, AC_PLACES);
		searchBox.setAdapter(adapter);
		
		searchBox.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				doSearch(searchBox.getText().toString());
			}
			
		});
		
		// Set up form actions (search)
		
		Button searchButton = (Button) findViewById(R.id.search_form_enter);
		
		searchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doSearch(searchBox.getText().toString());
			}
			
		});
		
		// TODO: set up form actions (browse)
		
	}

	private void doSearch(String key) {
		Location loc = getLoc(key);
		if (loc != null) {
			navigateDetailsPage(loc);
		} else {
			Toast.makeText(this, "Can't find anything corresponding to " + key,
					Toast.LENGTH_LONG).show();
		}
	}
	
	private Location getLoc(String loc){
		DBAdapter adp = new DBAdapter(this);
		try {
			adp.createDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		adp.openDataBase();
		
		Location l = adp.getLocation(loc);
		adp.close();
		return l;
	}
	
	private void navigateDetailsPage(Location loc){
		if(loc.getId() <= 0)
			throw new RuntimeException("Bad Location ID");
		
		Intent i;
		switch(loc.getlType()){
			case RESTAURANTS:
				i = new Intent(this, FoodInfo.class);
				break;
			case SHUTTLES:
				i = new Intent(this, ShuttleInfo.class);
				break;
			case ESCORTS:
				i = new Intent(this, EscortInfo.class);
				break;
			case ROOMS:
				i = new Intent(this, RoomInfo.class);
				break;
			case SERVICES:
				i = new Intent(this, OtherInfo.class);
				break;
			default:
				i = new Intent(this, BakerInfo.class);
		}
		
		i.putExtra("lID", loc.getId());
		this.startActivity(i);
	}
	
	private void navigateMorePage(int category){
		if(category <0)
			throw new RuntimeException("Bat category id");
		
		Intent i = new Intent(this, MoreItemsActivity.class);
		i.putExtra("which", LocationType.fromInt(category).toString());
		this.startActivity(i);
	}
	
	private void buildList(){
		for (int i = 0; i < PLACES.length; i++) {
        	if(PLACES[i] == null || PLACES[i].equals(""))
        		continue;
            Map<String, String> curGroupMap = new HashMap<String, String>();
            groupData.add(curGroupMap);
            curGroupMap.put(NAME, PLACES[i]);

            List<Map<String, String>> children = new ArrayList<Map<String, String>>();
            for (int j = 0; j < SUBPLACES[i].length; j++) {
            	if(SUBPLACES[i][j] == null || SUBPLACES[i][j].equals(""))
            		continue;
                Map<String, String> curChildMap = new HashMap<String, String>();
                children.add(curChildMap);
                curChildMap.put(NAME, SUBPLACES[i][j]);
            }
            childData.add(children);
        }
		
	}
	
	private void buildSubCategories(){
		DBAdapter adp = new DBAdapter(this);
		try {
			adp.createDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		adp.openDataBase();
		
		int i=0;
		/*for(Building b: adp.getBuildings()){
			if(i>= 3) break;
			SUBPLACES[0][i] = b.getName();
			LOCATIONS[0][i] = b;
			i++;
		}*/
		Building b = adp.getBuilding(1);
		SUBPLACES[0][0] = b.getName();
		LOCATIONS[0][0] = b;
		b = adp.getBuilding(6);
		SUBPLACES[0][1] = b.getName();
		LOCATIONS[0][1] = b;
		b = adp.getBuilding(2);
		SUBPLACES[0][2] = b.getName();
		LOCATIONS[0][2] = b;
		SUBPLACES[0][3] = "More...";
		
		/*
		i=0;
		for(Restaurant r:adp.getRestaurants()){
			if(i>= 3) break;
			SUBPLACES[1][i] = r.getName();
			LOCATIONS[1][i] = r;
			i++;
		}
		
		*/
		Restaurant r = adp.getRestaurant(37);
		SUBPLACES[1][0] = r.getName();
		LOCATIONS[1][0] = r;
		r = adp.getRestaurant(25);
		SUBPLACES[1][1] = r.getName();
		LOCATIONS[1][1] = r;
		r = adp.getRestaurant(39);
		SUBPLACES[1][2] = r.getName();
		LOCATIONS[1][2] = r;
		SUBPLACES[1][3] = "More...";
		
		/*
		i=0;
		for(Room h:adp.getRooms()){
			if(i>= 3) break;
			SUBPLACES[2][i] = h.getName();
			LOCATIONS[2][i] = h;
			i++;
		}
		*/
		
		Room h = adp.getRoom(126);
		SUBPLACES[2][0] = h.getName();
		LOCATIONS[2][0] = h;
		h = adp.getRoom(114);
		SUBPLACES[2][1] = h.getName();
		LOCATIONS[2][1] = h;
		h = adp.getRoom(120);
		SUBPLACES[2][2] = h.getName();
		LOCATIONS[2][2] = h;
		SUBPLACES[2][3] = "More...";
		
		i=0;
		for(Service s:adp.getServices()){
			if(i>= 3) break;
			SUBPLACES[3][i] = s.getName();
			LOCATIONS[3][i] = s;
			i++;
		}
		SUBPLACES[3][i] = "More...";
		
		i=0;
		for(Location e:adp.getOther()){
			if(i>= 3) break;
			SUBPLACES[4][i] = e.getName();
			LOCATIONS[4][i] = e;
			i++;
		}
		SUBPLACES[4][i] = "More...";
		
		adp.close();
	}
	
	private void buildSearchComponents(){
		ArrayList<Location> locs;
		
		DBAdapter adp = new DBAdapter(this);
		try {
			adp.createDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		adp.openDataBase();
		locs = adp.getAllLocations();
		adp.close();
		
		AC_PLACES = new String[locs.size()];
		
		int i = 0;
		for(Location l:locs){
			AC_PLACES[i] = l.getName();
			i++;
		}
	}
}
