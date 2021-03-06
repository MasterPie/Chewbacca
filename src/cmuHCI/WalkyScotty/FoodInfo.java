package cmuHCI.WalkyScotty;

import java.io.IOException;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cmuHCI.WalkyScotty.entities.Location;
import cmuHCI.WalkyScotty.entities.Restaurant;

public class FoodInfo extends WSActivity{
	
    /** Called when the activity is first created. */
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foodinfo);
        
        // Set title of the page
        TextView title = (TextView) findViewById(R.id.BakerHallText);
        title.setText(R.string.BakerHall);
        
        // Add photo to screen
        Drawable myImage = (this.getResources()).getDrawable(DBAdapter.getImage(getIntent().getIntExtra("lID", 1)));
        ImageView photo = (ImageView) findViewById(R.id.BakerHallPhoto);
        photo.setImageDrawable(myImage);
        
        // Set title of the description
        TextView desc = (TextView) findViewById(R.id.BakerHallDes);
        desc.setText(R.string.BakerDes);
        
        loadDetails(getIntent().getIntExtra("lID", 1));
        
        // Set up Button Actions
        
        Button directions = (Button) findViewById(R.id.BakerHall_Direction_Button);
        
        directions.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				FoodInfo.this.startActivity(new Intent(FoodInfo.this, DirectionsMainActivity.class));
			}
        	
        });
        
        
        Button nearby = (Button) findViewById(R.id.BakerHall_Nearby_Button);
        
        nearby.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Intent i = new Intent(FoodInfo.this, BakerNearby.class);
				i.putExtra("lID", getIntent().getIntExtra("lID", 1));
				FoodInfo.this.startActivity(i);
			}
        	
        });
    }
    
    private void loadDetails(int locID){
    	Restaurant loc;
    	DBAdapter adp = new DBAdapter(this);
		try {
			adp.createDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		adp.openDataBase();
		
		loc = adp.getRestaurant(locID); 
		
		adp.close();
		
		TextView title = (TextView) findViewById(R.id.BakerHallText);
		title.setText(loc.getName());
		
		TextView desc = (TextView) findViewById(R.id.BakerHallDes);
        desc.setText(loc.getDescription());
        
        TextView nick = (TextView) findViewById(R.id.BuildingNick);
        nick.setText(loc.getAbbreviation());
        
        TextView hours = (TextView) findViewById(R.id.FoodHours);
        hours.setText(loc.getHours());
        
        TextView menu = (TextView) findViewById(R.id.FoodMenu);
        if(loc.getMenu_link() != null && !loc.getMenu_link().equals("")){
	        SpannableString ss = new SpannableString("Menu");
	        ss.setSpan(new URLSpan(loc.getMenu_link()), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	        menu.setText(ss);
	        menu.setMovementMethod(LinkMovementMethod.getInstance());
        }
        else
        	menu.setText("No menu available.");
        
        
        hours.setText(loc.getHours());
        
        TextView crumb = (TextView) findViewById(R.id.bakerinfo_breadcrumb_building);
        crumb.setText(loc.getName());
    }

}