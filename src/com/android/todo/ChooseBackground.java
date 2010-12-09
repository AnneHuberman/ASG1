/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.todo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ChooseBackground extends Activity {
    public static final String KEY_PIC_CHOICE = "picChoice";
    private int nPicChoice = 0;
    
    
    static final String[] PIX = new String[] { "None", "Moonrse", "Water", "Leaves"};
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bkground_choice);

        Bundle extras = getIntent().getExtras();
        nPicChoice = extras != null ? extras.getInt(KEY_PIC_CHOICE)
                                : 0;
        ListView lv = (ListView)findViewById(R.id.Bglist);
        ArrayAdapter adapter;
        
        switch(nPicChoice)
        {
	        case 2:  //"Water"
	        case 3:  //"Leaves"
	            adapter = new ArrayAdapter(this, R.layout.bkground_choice_row_black_text, R.id.picTitle, PIX);
	        	break;
	
	        case 0:  //"None"
	        case 1:  //"Moonrse"
	        default:
	            adapter = new ArrayAdapter(this, R.layout.bkground_choice_row, R.id.picTitle, PIX);
	        	break;
        }
        
//???    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	lv.setAdapter(adapter);
        
 //       ListAdapter notes = 
   //         new ArrayAdapter(this, R.layout.bkground_choice_row, PIX);
 //       SimpleAdapter x;
        
 //       setListAdapter(new ArrayAdapter<String>(lv, R.id.picTitle, PIX));
        lv.setSelection(nPicChoice);
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
        	  nPicChoice = (int)id;
        	  setBkground();
            // When clicked, show a toast with the TextView text
            Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
                Toast.LENGTH_SHORT).show();
          }
        });
        
        
        
        setBkground();
    
        Button confirmButton = (Button) findViewById(R.id.confirm);

        confirmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
            	Intent i = new Intent();
            	
            	i.putExtra(KEY_PIC_CHOICE, nPicChoice);
            	
                setResult(RESULT_OK, i);
                finish();
            }        	
        });        
    }

    public void setBkground()
    {
        LinearLayout myView = (LinearLayout) findViewById(R.id.choose_bkground);
        ListView lv = (ListView)findViewById(R.id.Bglist);
        final int[] ids = {0, R.drawable.moonrise, R.drawable.pool, R.drawable.leaves};
        ArrayAdapter adapter;

        if ( (nPicChoice >= 0) && (nPicChoice <= 3))
            myView.setBackgroundResource(ids[nPicChoice]);
        else
        	myView.setBackgroundResource(R.drawable.moonrise);
        
        if (nPicChoice == 0)
        	myView.setBackgroundColor(0xff0000aa);
              
		switch(nPicChoice)
		{
			case 2:  //"Water"
			case 3:  //"Leaves"
			    adapter = new ArrayAdapter(this, R.layout.bkground_choice_row_black_text, R.id.picTitle, PIX);
				break;
			
			case 0:  //"None"
			case 1:  //"Moonrse"
			default:
				adapter = new ArrayAdapter(this, R.layout.bkground_choice_row, R.id.picTitle, PIX);
				break;
		}
      	lv.setAdapter(adapter);
              
   }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//??        saveState();
        outState.putSerializable(KEY_PIC_CHOICE, nPicChoice);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
 //??       saveState();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        	nPicChoice = extras.getInt(KEY_PIC_CHOICE);
    
 //??      populateFields();
    }
    
}
