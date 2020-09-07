package org.techtown.push.mapkeywordsearch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LocationResultPresent extends AppCompatActivity {

    //TextView textView;
    String result;
    ListView listView;
    LocationAdapter adapter;
    //String[] locationResult;
    ArrayList<LocationInformation> locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_result_present);

        //textView = findViewById(R.id.textView3);

        Intent intent = getIntent(); // Location Search Activity 에서 받은 intent 받기
        result = intent.getStringExtra("result");

        locations = processingResult(result);

        listView=(ListView)findViewById(R.id.listView);
        adapter = new LocationAdapter();

        for (LocationInformation location:locations){
            //Log.d("result in onCreate",location.getAddress_name());
            adapter.addItem(new LocationInformation(location.getPlace_name(),
                    location.getAddress_name(),
                    location.getRoad_address_name(),
                    location.getX(),
                    location.getY()));
        }

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LocationInformation location = (LocationInformation) adapter.getItem(i);
                Toast.makeText(getApplicationContext(), "선택된 장소: "+location.getPlace_name(), Toast.LENGTH_LONG).show();

                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                        Intent.FLAG_ACTIVITY_SINGLE_TOP|
                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("x",location.getX());
                intent.putExtra("y",location.getY());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) { // 이미 만들어진 상태라면...!
        //processIntent(intent);
        super.onNewIntent(intent);
    }

    public ArrayList processingResult(String data){

        /*
        String place_name;
        String address_name;
        String road_address_name;
        String x;
        String y;
        String[] resultArray;
         */

        ArrayList<LocationInformation> locations = new ArrayList<LocationInformation>();

        try {
            JSONObject jsonObject = new JSONObject(data);
            String location_info = jsonObject.getString("documents");
            JSONArray jsonArray = new JSONArray(location_info);
            //resultArray = new String[jsonArray.length()];

            for (int i=0; i < jsonArray.length(); i++) {
                JSONObject subJsonObject = jsonArray.getJSONObject(i);

                String place_name = subJsonObject.getString("place_name");
                String address_name = subJsonObject.getString("address_name");
                String road_address_name = subJsonObject.getString("road_address_name");
                String x = subJsonObject.getString("x");
                String y = subJsonObject.getString("y");

                // LocationInformationClass 객체를 저장한다.
                locations.add(new LocationInformation(place_name,address_name,road_address_name,x,y));

                //resultArray[i] = place_name +","+address_name+","+road_address_name+","+x+","+y;
                //Log.d("result",resultArray[i]);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        return locations;
    }


    class LocationAdapter extends BaseAdapter{
        ArrayList<LocationInformation> items = new ArrayList<LocationInformation>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(LocationInformation item){
            items.add(item);
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LocationItemView itemView = new LocationItemView(getApplicationContext());
            LocationInformation item = items.get(i);
            itemView.setName(item.getPlace_name());
            itemView.setAddress(item.getAddress_name());
            return itemView;
        }
    }
}