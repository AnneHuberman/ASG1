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

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class ChooseSort extends Activity {
    public static final String KEY_FIELD_CHOICE = "fieldChoice";

    private int nSortSel;

    
    RadioButton rBtns[] = new RadioButton[3];

    private OnClickListener radio_listener = new OnClickListener() {
	    public void onClick(View v) {
	        // Perform action on clicks
	        RadioButton rb = (RadioButton) v;
	        int nRbID = rb.getId();
	        
	        switch (nRbID)
	        {
		        case R.id.radio_sortByPriority:
		        	nSortSel = 0;
		        	break;
	
		        case R.id.radio_sortByDate:
		        	nSortSel = 1;
		        	break;
	
		        case R.id.radio_sortByTitle:
		        	nSortSel = 2;
		        	break;

        }
//   	        Toast.makeText(HelloFormStuff.this, rb.getText(), Toast.LENGTH_SHORT).show();
	    }
	};	    	    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_sort_field);
        
        final RadioButton rPrioritySort = (RadioButton) findViewById(R.id.radio_sortByPriority);
        final RadioButton rDateSort = (RadioButton) findViewById(R.id.radio_sortByDate);
        final RadioButton rTitleSort = (RadioButton) findViewById(R.id.radio_sortByTitle);
        rBtns[0] = rPrioritySort;
        rBtns[1] = rDateSort;
        rBtns[2] = rTitleSort;

        rPrioritySort.setOnClickListener(radio_listener);
        rDateSort.setOnClickListener(radio_listener);
        rTitleSort.setOnClickListener(radio_listener);
        
        Bundle extras = getIntent().getExtras();
        nSortSel = extras != null ? extras.getInt(KEY_FIELD_CHOICE)
                                : 0;
    
        populateFields();
        Button confirmButton = (Button) findViewById(R.id.confirm);

        confirmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
            	Intent i = new Intent();
            	
            	i.putExtra(KEY_FIELD_CHOICE, nSortSel);
            	
                setResult(RESULT_OK, i);
                finish();
            }        	
        });
    }

    private void populateFields() {
    	for (int i = 0; i < 3; i++)
    	{
    		if (i == nSortSel)
    			rBtns[i].setChecked(true);
    		else
    			rBtns[i].setChecked(false);
    			
    	}	
     }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//??        saveState();
        outState.putSerializable(KEY_FIELD_CHOICE, nSortSel);
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
        	nSortSel = extras.getInt(KEY_FIELD_CHOICE);
    
       populateFields();
    }
    
}
 