/*

listView 내에서 정보를 보여주는 Layout class 이다.

현재는 Location Name 과 Location address를 출력하도록 처리가 되어 있다.

 */

package org.techtown.push.mapkeywordsearch;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LocationItemView extends LinearLayout {
    TextView textView; // location name
    TextView textView2; // Location address

    public LocationItemView(Context context){
        super(context);
        init(context);
    }

    public LocationItemView(Context context, AttributeSet attrs){
        super(context);
        init(context);
    }

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.location_item_view, this, true);
        textView = findViewById(R.id.textViewName);
        textView2 = findViewById(R.id.textViewAddress);
    }

    public void setName(String name){
        textView.setText(name);
    }

    public void setAddress(String address){
        textView2.setText(address);
    }
}
