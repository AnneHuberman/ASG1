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

import java.util.Locale;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.speech.tts.TextToSpeech;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
//import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ResourceCursorAdapter;


public class ToDo extends ListActivity  implements TextToSpeech.OnInitListener {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    private static final int ACTIVITY_SELECT_SORT = 3;
    private static final int ACTIVITY_SELECT_BACKGROUND = 4;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int SORT_ID = Menu.FIRST + 2;
    private static final int SET_BACKGROUND_ID = Menu.FIRST + 3;
    
    private int nSortChoice = 0;
    private int nPicChoice = 1;
    
    private final static String[] sSortStrings = {NotesDbAdapter.KEY_PRIORITY, 
    										NotesDbAdapter.KEY_YEAR + ", " + NotesDbAdapter.KEY_MONTH + ", " + NotesDbAdapter.KEY_DAY, 
    										NotesDbAdapter.KEY_TITLE};

    private NotesDbAdapter mDbHelper;
    private Cursor mNotesCursor;
    TextToSpeech myTTS;
    boolean myTTSInited;
    OnCreateContextMenuListener onCreateContextMenu;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_list);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
        
        myTTS = new TextToSpeech(this, this);
    }

    public void onDestroy(){
    	super.onDestroy();
    	myTTS.shutdown();
    }
 

    
    private void fillData() {
    	
        // Get all of the rows from the database and create the item list
    	mNotesCursor = mDbHelper.fetchAllNotes(sSortStrings[nSortChoice]);
        startManagingCursor(mNotesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
 //       String[] from = new String[]{NotesDbAdapter.KEY_TITLE, NotesDbAdapter.KEY_PRIORITY_TEXT, 
 //       		NotesDbAdapter.KEY_DATE};
        int nRowID;

        // and an array of the fields we want to bind those fields to (in this case just text1)
//        int[] to = new int[]{R.id.text1, R.id.priority, R.id.date};
        
        switch(nPicChoice)
        {
	        case 2:  //"Water"
	        case 3:  //"Leaves"
	        	nRowID = R.layout.notes_row_black_text;
	        	break;
	
	        case 0:  //"None"
	        case 1:  //"Moonrse"
	        default:
	        	nRowID = R.layout.notes_row;
	        	break;
        }

/*        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = 
        new SimpleCursorAdapter(this, nRowID, notesCursor, from, to);
        setListAdapter(notes);
*/
        
        // Snippet obtained from Q&A forum on checkboxes in listview
		MyAdapter myListAdapter = new MyAdapter(this, mNotesCursor, nRowID);
        setListAdapter(myListAdapter);
        if(myTTSInited){
        	myTTS.speak("Here is To Do list ", TextToSpeech.QUEUE_FLUSH, null);
        	int nosRows = mNotesCursor.getCount();
        	mNotesCursor.moveToFirst();
        	if (nosRows > 10) nosRows = 10;
        	for (int i=1;i<nosRows;i++){
        		String rowTitle = mNotesCursor.getString(2);
        		myTTS.speak(rowTitle, TextToSpeech.QUEUE_ADD, null);
        		mNotesCursor.moveToNext();
        	}
        	
//String[] zzz = 	mNotesCursor.getColumnNames();

        	if (nosRows > 0)
        	{
		//		String rowTitle = mNotesCursor.getString(2);
				String rowTitle = mNotesCursor.getString(mNotesCursor.getColumnIndex("title"));
		        myTTS.speak(rowTitle, TextToSpeech.QUEUE_ADD, null);
        	}
        }
    }  // fillData

    private class ToDOOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			ViewParent linearParent = v.getParent();
			ListView listParent = (ListView)linearParent.getParent();
			long id = (Long) ((View) linearParent).getTag();
			int position = listParent.getPositionForView((View) linearParent);
			Cursor c = mNotesCursor;c.moveToPosition(position);
	        Intent i = new Intent(v.getContext(), NoteEdit.class);
	        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
	        i.putExtra(NotesDbAdapter.KEY_TITLE, c.getString(
	        		c.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
	        i.putExtra(NotesDbAdapter.KEY_BODY, c.getString(
	        		c.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
	        startActivityForResult(i, ACTIVITY_EDIT);
		}
    }
    
    
    
    private class MyAdapter extends ResourceCursorAdapter 
    { 
    	int nXML;
   	 
        public MyAdapter(Context context, Cursor cur) { 
            super(context, R.layout.notes_row, cur); 
        	nXML = R.layout.notes_row;
        } 
 
        public MyAdapter(Context context, Cursor cur, int aXML) { 
            super(context, aXML, cur); 
        	nXML = aXML;
        } 
 
        @Override 
        public View newView(Context context, Cursor cur, ViewGroup parent) { 
            LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
            return li.inflate(nXML, parent, false); 
        } 
 
        @Override 
        public void bindView(View view, Context context, final Cursor cur) { 
            TextView tvListTitle = (TextView)view.findViewById(R.id.text1); 
            TextView tvListDate = (TextView)view.findViewById(R.id.date); 
            TextView tvListPriority = (TextView)view.findViewById(R.id.priority); 
            CheckBox cbListCheck = (CheckBox)view.findViewById(R.id.checkBox); 

            long rowID = cur.getLong(cur.getColumnIndex(NotesDbAdapter.KEY_ROWID));
            view.setTag(rowID);  //save rowID for listeners
            tvListTitle.setText(cur.getString(cur.getColumnIndex(NotesDbAdapter.KEY_TITLE))); 
            tvListDate.setText(cur.getString(cur.getColumnIndex(NotesDbAdapter.KEY_DATE))); 
            tvListPriority.setText(cur.getString(cur.getColumnIndex(NotesDbAdapter.KEY_PRIORITY_TEXT))); 
            
            cbListCheck.setChecked((cur.getInt(cur.getColumnIndex(NotesDbAdapter.KEY_STATUS))==0? false:true));
 // Click listener for list item to edit
            tvListTitle.setOnClickListener(new ToDOOnClickListener());
            tvListDate.setOnClickListener(new ToDOOnClickListener());
            tvListPriority.setOnClickListener(new ToDOOnClickListener());
 			/*         tvListText.setOnLongClickListener(new OnLongClickListener(){
            	
            	@Override
            	public boolean onLongClick(View v){
            		System.out.println("onLongClick called");
					return true;
            	}
            });*/
            tvListTitle.setOnCreateContextMenuListener(onCreateContextMenu);
            tvListDate.setOnCreateContextMenuListener(onCreateContextMenu);
            tvListPriority.setOnCreateContextMenuListener(onCreateContextMenu);

            // Click listener for checkbox to toggle the status field
            cbListCheck.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					ViewParent linearParent = v.getParent();
					long id = (Long) ((View) linearParent).getTag();
					boolean itemCheckbox = ((CheckBox) v).isChecked();
					mDbHelper.updateNoteStatus(id, itemCheckbox);
				}           	
            });
        }
    } 
     
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        menu.add(0, SORT_ID, 0, R.string.menu_sort);
        menu.add(0, SET_BACKGROUND_ID, 0, R.string.menu_bkrnd);
        
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	Intent i;
        switch(item.getItemId()) {
            case INSERT_ID:
                createNote();
                return true;
                
            case SORT_ID:
                i = new Intent(this, ChooseSort.class);
                i.putExtra(ChooseSort.KEY_FIELD_CHOICE, nSortChoice);
                startActivityForResult(i, ACTIVITY_SELECT_SORT);
                return true;
                
            case SET_BACKGROUND_ID:
                i = new Intent(this, ChooseBackground.class);
                i.putExtra(ChooseBackground.KEY_PIC_CHOICE, nPicChoice);
                startActivityForResult(i, ACTIVITY_SELECT_BACKGROUND);
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
        System.out.println("onCreateContextMenu called");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData();
                System.out.println("onContextItemSelected called");
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createNote() {
        Intent i = new Intent(this, NoteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, NoteEdit.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        int x = nSortChoice;
        final int[] ids = {0, R.drawable.moonrise, R.drawable.pool, R.drawable.leaves};
        
        if (intent != null)
        {   
            Bundle extras = intent.getExtras();
	        switch (requestCode) 
	        {
		        case ACTIVITY_SELECT_SORT:
		        {	        	
			        if (resultCode == RESULT_OK)
			        {
				       if (extras != null)
				       {
				        	x = extras.getInt(ChooseSort.KEY_FIELD_CHOICE);
				        	nSortChoice = x;
				       }
			        }
			        break;
		        }
	
		        case ACTIVITY_SELECT_BACKGROUND:
		        {	        	
			        if (resultCode == RESULT_OK)
			        {
				       if (extras != null)
				       {
				    	    LinearLayout myView;
				            ListView lv = (ListView)findViewById(R.id.Bglist);
				            ArrayAdapter adapter;

				            x = extras.getInt(ChooseBackground.KEY_PIC_CHOICE);
				        	nPicChoice = x;
				        	
				        	myView = (LinearLayout) findViewById(R.id.mainview);
				            if ( (nPicChoice >= 0) && (nPicChoice <= 3))
				                myView.setBackgroundResource(ids[nPicChoice]);
				            else
				            	myView.setBackgroundResource(R.drawable.moonrise);
				            
				            if (nPicChoice == 0)
				            	myView.setBackgroundColor(0xff0000aa);
				       }
			        }
			        break;
		        }

	        }
        }
        fillData();
     }

	@Override
	public void onInit(int status) {
		Locale loc = new Locale ("en","", "");
		if (myTTS.isLanguageAvailable(loc)>=TextToSpeech.LANG_AVAILABLE){
			myTTS.setLanguage(loc);
		}
		myTTSInited = true;
	}
    
}
