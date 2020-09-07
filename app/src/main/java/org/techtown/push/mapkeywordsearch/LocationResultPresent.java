/*

Location 검색 결과를 present하는 "Activity" 이다.
Location Search Activity에서 검색 결과 값을 String (JSON형식)으로 가져와서 Parsing 한다.

*/

package org.techtown.push.mapkeywordsearch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LocationResultPresent extends AppCompatActivity {

    String result; // 검색 결과 값을 저장하는 변수

    ListView listView;
    LocationAdapter adapter;
    ArrayList<LocationInformation> locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_result_present);

        Intent intent = getIntent(); // Location Search Activity 에서 받은 intent 받기
        result = intent.getStringExtra("result");

        locations = processingResult(result); // Parsing 후에 결과 값을 저장한다.

        listView= findViewById(R.id.listView);
        adapter = new LocationAdapter();

        for (LocationInformation location:locations){

            //결과로 받아온 장소 정보를 listView에 추가한다.
            adapter.addItem(new LocationInformation(location.getPlace_name(),
                    location.getAddress_name(),
                    location.getRoad_address_name(),
                    location.getX(),
                    location.getY()));
        }

        listView.setAdapter(adapter);

        // itemView touch 할 때 처리
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LocationInformation location = (LocationInformation) adapter.getItem(i); // listview에 저장된 item을 index 값으로 가져온다.
                Toast.makeText(getApplicationContext(), "선택된 장소: "+location.getPlace_name(), Toast.LENGTH_LONG).show();

                //x값과 y값을 Location Search Activity로 되돌려 보낸다.
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
    protected void onDestroy() {
        super.onDestroy();
        Log.d("destroy","activity 사망");
    }

    public ArrayList processingResult(String data){

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

                locations.add(new LocationInformation(place_name,address_name,road_address_name,x,y));

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