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
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class NoteEdit extends Activity {

	private boolean mStatus;
	private EditText mTitleText;
    private EditText mBodyText;
    private Long mRowId;
    private NotesDbAdapter mDbHelper;
    private Spinner spinner;
    private TextView mDateDisplay;
    private Button mPickDate;
    private int mYear;
    private int mMonth;
    private int mDay;

    static final int DATE_DIALOG_ID = 0;
    
    public class MyOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
 //         Toast.makeText(parent.getContext(), "Priority: " +
 //             parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }    
    
    // the callback received when the user "sets" the date in the dialog
 		   private DatePickerDialog.OnDateSetListener mDateSetListener =
	            new DatePickerDialog.OnDateSetListener() {
			@Override
	                public void onDateSet(android.widget.DatePicker view, int year, 
	                                      int monthOfYear, int dayOfMonth) {
	                    mYear = year;
	                    mMonth = monthOfYear + 1;
	                    mDay = dayOfMonth;
	                    String sDate = ((Integer)(mMonth)).toString() + "/" + 
	           				((Integer)(mDay)).toString() + "/" + 
	           				((Integer)(mYear)).toString();
	                    mPickDate.setText(sDate);
	                }
 		   	};
    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit);
        
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.priorities_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
        
        
        
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        
        setTitle(R.string.edit_note);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                                    : null;
        }

        // capture our View elements
        mDateDisplay = (TextView) findViewById(R.id.dateDisplay);
        mPickDate = (Button) findViewById(R.id.pickDate);

        // add a click listener to the button
        mPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        
        
        populateFields();
        confirmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                setResult(RESULT_OK);
                finish();
            }        	
        });
    }

    private void populateFields() {
    	String sDate;

    	if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            
            startManagingCursor(note);
            mTitleText.setText(note.getString(
                        note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
            spinner.setSelection(note.getInt(
            		note.getColumnIndexOrThrow(NotesDbAdapter.KEY_PRIORITY)));
            
            mDay = note.getInt(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DAY));
            mMonth = note.getInt(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_MONTH));
            mYear = note.getInt(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_YEAR));
            
            sDate = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATE)); 

//            sDate = ((Integer)(mMonth)).toString() + "/" + 
//   			((Integer)(mDay)).toString() + "/" + 
//   			((Integer)(mYear)).toString();
            mPickDate.setText(sDate);
          }
        else
        {
            // get the current date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH) + 1;
            mDay = c.get(Calendar.DAY_OF_MONTH);
            sDate = ((Integer)(mMonth)).toString() + "/" + 
   			((Integer)(mDay)).toString() + "/" + 
   			((Integer)(mYear)).toString();
            mPickDate.setText(sDate);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }
    
    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();
        long priority = spinner.getSelectedItemId();
        
        if (priority == -1 )
        	priority = 0;

        if (mRowId == null) {
            long id = mDbHelper.createNote(title, body, (int)priority, mDay, mMonth, mYear);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, title, body, (int)priority, mDay, mMonth, mYear);
        }
    }    
    
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth - 1, mDay);
        }
        return null;
    }}
