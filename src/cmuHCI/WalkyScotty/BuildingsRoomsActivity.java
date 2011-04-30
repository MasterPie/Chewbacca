package cmuHCI.WalkyScotty;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import cmuHCI.WalkyScotty.entities.Building;
import cmuHCI.WalkyScotty.entities.Location;
import cmuHCI.WalkyScotty.entities.Room;



public class BuildingsRoomsActivity extends WSActivity{

	private String[] ROOMS;
	private Room[] LOCATIONS;
	private ArrayList<Room> rooms;
	private String building;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buildingrooms);
        
        getRooms();
        
        ListView brlv = (ListView) findViewById(R.id.buildingrooms_list);
		brlv.setTextFilterEnabled(true);
		brlv.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, ROOMS));
		brlv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				navigateDetailsPage(LOCATIONS[arg2]);
				
			}
		});
		
		TextView bc = (TextView) findViewById(R.id.buildingrooms_breadcrumb_building);
        bc.setText(building);

    }
	
	private void getRooms(){
		Building b;
		DBAdapter adp = new DBAdapter(this);
		try {
			adp.createDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		adp.openDataBase();
		b = adp.getBuilding(getIntent().getIntExtra("bID", 1));
		
		if(b != null){
			//TODO This should load the name of the location into building so it can get loaded into the breadcrumb
			building = b.getName();
			
			rooms = adp.getRooms(b);
			adp.close();
			ROOMS = new String[rooms.size()];
			LOCATIONS = new Room[rooms.size()];
			int i = 0;
			for(Room r: rooms){
				LOCATIONS[i] = r;
				ROOMS[i] = r.getName();
				i++;
			}
		}
		else{
			building = null;
			ROOMS = new String[0];
			LOCATIONS = new Room[0];
			rooms = null;
		}
		
		
	}
	private void navigateDetailsPage(Location loc){
		if(loc.getId() <= 0)
			throw new RuntimeException("Bad Location ID");
		
		Intent i = new Intent(this, RoomInfo.class);
		
		i.putExtra("lID", loc.getId());
		this.startActivity(i);
	}
}
