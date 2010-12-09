/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.todo;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple todo database access helper class. Defines the basic CRUD operations
 * for the To Do example, and gives the ability to list all todos as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class NotesDbAdapter {

	public static final String KEY_ROWID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";
	public static final String KEY_PRIORITY = "priority";
	public static final String KEY_PRIORITY_TEXT = "priorityText";
	public static final String KEY_DAY = "day";
	public static final String KEY_MONTH = "month";
	public static final String KEY_YEAR = "year";
	public static final String KEY_DATE = "dueDate";
	public static final String KEY_STATUS = "status";

	private static final String TAG = "NotesDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE = "todo";
	private static final int DATABASE_VERSION = 4;

	private static final String DATABASE_CREATE =
	// "create table todo (_id integer primary key autoincrement, "
	// + "title text not null, body text not null);";

	/*
	 * "CREATE TABLE " + DATABASE_TABLE + " (" + "_ID INTEGER PRIMARY KEY," +
	 * "TITLE TEXT not null," + "BODY TEXT not null," + "PRIORITY INTEGER" +
	 * ");";
	 * 
	 * 
	 * "create table " + DATABASE_TABLE + " (" +
	 * "_id integer primary key autoincrement, " + "title text not null, " +
	 * "body text not null, " + "priority integer, " +
	 * "priorityText text not null" + ");";
	 */

	/*
	 * "create table " + DATABASE_TABLE + " (" +
	 * "_id integer primary key autoincrement, " + "title text not null, " +
	 * "body text not null, " + "priority integer, " +
	 * "priorityText text not null, " + "day integer, " + "month integer, " +
	 * "year integer, " + "date text not null " + ");";
	 */
	"create table " + DATABASE_TABLE + " (" 
			+ KEY_ROWID + " integer primary key autoincrement, " 
			+ KEY_TITLE + " text not null, " 
			+ KEY_BODY + " text not null, " 
			+ KEY_STATUS + " boolean,"
			+ KEY_PRIORITY + " integer, " 
			+ KEY_DATE + " text not null " + ");";

	private final Context mCtx;
	private String[] mPriorities;
	public static final int PRIORITY_LOW = 0;
	public static final int PRIORITY_MED = 1;
	public static final int PRIORITY_HI = 2;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			System.out.println("go to onCreate");
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS todo");
			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public NotesDbAdapter(Context ctx) {
		this.mCtx = ctx;
		getPriorities();
	}

	/**
	 * Open the todo database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public NotesDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		// mDbHelper.onUpgrade(mDb, 2, 3);
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	/*
	 * static public String getPriorityText(int priority) { final String[]
	 * myStrs = new String[3];
	 * 
	 * myStrs[0] = "Low"; myStrs[1] = "Med"; myStrs[2] = "Hi"; if ( (priority >=
	 * 0) && (priority <= 3) ) return myStrs[priority]; return myStrs[0]; }
	 */

	/**
	 * Create a new note using the title and body provided. If the note is
	 * successfully created return the new rowId for that note, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param title
	 *            the title of the note
	 * @param body
	 *            the body of the note
	 * @return rowId or -1 if failed
	 */
	public long createNote(String title, String body, int priority, int day,
			int month, int year) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TITLE, title);
		initialValues.put(KEY_BODY, body);
		initialValues.put(KEY_STATUS, false);
		initialValues.put(KEY_PRIORITY, priority);
		/*
		 * initialValues.put(KEY_PRIORITY_TEXT, getPriorityText(priority));
		 * initialValues.put(KEY_DAY, day); initialValues.put(KEY_MONTH, month);
		 * initialValues.put(KEY_YEAR, year);
		 */
		String sDate = ((Integer) (year)).toString() + "-"
				+ ((Integer) (month)).toString() + "-"
				+ ((Integer) (day)).toString();
		initialValues.put(KEY_DATE, sDate);

		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Delete the note with the given rowId
	 * 
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteNote(long rowId) {

		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all todos in the database
	 * 
	 * @return Cursor over all todos
	 */
	public Cursor fetchAllNotes(String sSort) {

		// return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
		// KEY_BODY, KEY_PRIORITY, KEY_PRIORITY_TEXT, KEY_DAY, KEY_MONTH,
		// KEY_YEAR, KEY_DATE},
		// null, null, null, null, null);
		/*
		 * return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
		 * KEY_BODY, KEY_PRIORITY, KEY_PRIORITY_TEXT, KEY_DAY, KEY_MONTH,
		 * KEY_YEAR, KEY_DATE}, null, null, null, null, sSort);
		 */
		if (sSort.compareTo(KEY_PRIORITY) == 0)
			sSort += " desc";
		String sql = "select *, " + "CASE " + "WHEN priority = " + PRIORITY_LOW
				+ " THEN '" + this.mPriorities[PRIORITY_LOW] + "' "
				+ "WHEN priority = " + PRIORITY_MED + " THEN '"
				+ this.mPriorities[PRIORITY_MED] + "' " + "WHEN priority = "
				+ PRIORITY_HI + " THEN '" + this.mPriorities[PRIORITY_HI]
				+ "' " + "ELSE '" + this.mPriorities[PRIORITY_LOW] + "' "
				+ "END " + "as " + KEY_PRIORITY_TEXT + ", " + "strftime('%d', "
				+ KEY_DATE + ") as " + KEY_DAY + ", " + "strftime('%m', "
				+ KEY_DATE + ") as " + KEY_MONTH + ", " + "strftime('%Y', "
				+ KEY_DATE + ") as " + KEY_YEAR + " from " + DATABASE_TABLE
				+ " order by " + sSort;

		return mDb.rawQuery(sql, new String[] {});
	}

	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 * 
	 * @param rowId
	 *            id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */
	public Cursor fetchNote(long rowId) throws SQLException {

		/*
		 * Cursor mCursor =
		 * 
		 * mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
		 * KEY_BODY, KEY_PRIORITY, KEY_PRIORITY_TEXT, KEY_DAY, KEY_MONTH,
		 * KEY_YEAR, KEY_DATE}, KEY_ROWID + "=" + rowId, null, null, null, null,
		 * null);
		 */

		/*
		 * String sql = "select * from " + DATABASE_TABLE + " WHERE " +
		 * KEY_ROWID + "=" + rowId;
		 */
		String sql = "select *, " + "CASE " + "WHEN priority = " + PRIORITY_LOW
				+ " THEN '" + this.mPriorities[PRIORITY_LOW] + "' "
				+ "WHEN priority = " + PRIORITY_MED + " THEN '"
				+ this.mPriorities[PRIORITY_MED] + "' " + "WHEN priority = "
				+ PRIORITY_HI + " THEN '" + this.mPriorities[PRIORITY_HI]
				+ "' " + "ELSE '" + this.mPriorities[PRIORITY_LOW] + "' "
				+ "END " + "as " + KEY_PRIORITY_TEXT + ", " + "strftime('%d', "
				+ KEY_DATE + ") as " + KEY_DAY + ", " + "strftime('%m', "
				+ KEY_DATE + ") as " + KEY_MONTH + ", " + "strftime('%Y', "
				+ KEY_DATE + ") as " + KEY_YEAR + " from " + DATABASE_TABLE
				+ " WHERE " + KEY_ROWID + "=" + rowId;

		Cursor mCursor = mDb.rawQuery(sql, new String[] {});

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	/**
	 * Update the note using the details provided. The note to be updated is
	 * specified using the rowId, and it is altered to use the title and body
	 * values passed in
	 * 
	 * @param rowId
	 *            id of note to update
	 * @param title
	 *            value to set note title to
	 * @param body
	 *            value to set note body to
	 * @return true if the note was successfully updated, false otherwise
	 */
	public boolean updateNote(long rowId, String title, String body,
			int priority, int day, int month, int year) {
		ContentValues args = new ContentValues();
		args.put(KEY_TITLE, title);
		args.put(KEY_BODY, body);
		args.put(KEY_PRIORITY, priority);

		/*
		 * args.put(KEY_PRIORITY_TEXT, getPriorityText(priority));
		 * args.put(KEY_DAY, day); args.put(KEY_MONTH, month);
		 * args.put(KEY_YEAR, year);
		 */
		String sDate = ((Integer) (year)).toString() + "-"
				+ ((Integer) (month)).toString() + "-"
				+ ((Integer) (day)).toString();
		args.put(KEY_DATE, sDate);

		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/*
	 * Update the Status field of a note using the RowID and flag.
	 * 
	 * @param rowId id of note to update return true if note successfully
	 * updated.
	 */
	public boolean updateNoteStatus(long rowId, boolean status) {
		ContentValues args = new ContentValues();
		args.put(KEY_STATUS, status);
		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public void getPriorities() {
		Resources res = mCtx.getResources();
		mPriorities = res.getStringArray(R.array.short_priorities_array);
	}
}
