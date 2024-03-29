/*
 * Copyright 2010 TimeSheet authors.
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
package com.googlecode.iqapps.IQTimeSheet.test;

import android.app.Instrumentation;
import android.content.Context;
import android.database.SQLException;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.Suppress;
import android.widget.ListView;

import com.googlecode.iqapps.IQTimeSheet.MenuItems;
import com.googlecode.iqapps.IQTimeSheet.TimeSheetActivity;
import com.googlecode.iqapps.IQTimeSheet.TimeSheetDbAdapter;
import com.googlecode.iqapps.testtools.Helpers;
import com.jayway.android.robotium.solo.Solo;

@Suppress //#$##
public class ReviveTaskTest extends
		ActivityInstrumentationTestCase2<TimeSheetActivity> {
	// private Log log = LogFactory.getLog(TimeSheetActivityTest.class);
	private static final String TAG = "ReviveTaskTest";
	private static final int SLEEPTIME = 50;

	private ListView result;
	private TimeSheetActivity mActivity;
	private Solo solo;
	private Context mCtx;
	private Instrumentation mInstr;
	private TimeSheetDbAdapter db;

	public ReviveTaskTest() {
		super(TimeSheetActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();

		mActivity = getActivity();
		mInstr = getInstrumentation();
		mCtx = mInstr.getTargetContext();
		solo = new Solo(mInstr, mActivity);
		result = (ListView) solo
				.getView(com.googlecode.iqapps.IQTimeSheet.R.layout.tasklist);

		// Open the database
		db = new TimeSheetDbAdapter(mActivity);
		try {
			db.open();
		} catch (SQLException e) {
			assertFalse(e.toString(), true);
		}
	}

	/**
	 * Restore the database after we're done messing with it.
	 */
	public void testzzzRestoreDB() {
		mActivity = getActivity();
		assertNotNull(mActivity);

		Helpers.restore(solo, mInstr, mActivity);
	}

	@Override
	public void tearDown() {
		db.close();

		solo.finishOpenedActivities();
	}

	/**
	 * Take a backup of the database so that we can restore the current state
	 * later..
	 */
	public void test001BackupDB() {
		mActivity = getActivity();
		assertNotNull(mActivity);

		Helpers.backup(solo, mInstr, mActivity);
	}

	/**
	 * Make sure the application is ready for us to test it.
	 */
	public void test002EraseDB() {
		eraseDatabase();
	}

	/**
	 * Physically remove the database file
	 */
	private void eraseDatabase() {
		// Delete the databases associated with the project.
		String[] databases = mCtx.databaseList();
		for (String database : databases) {
			// assertTrue("dbList: " + databases[db], false);
			mCtx.deleteDatabase(database);
		}
	}

	// Perform Initial Setup
	public void test009InitialSetup() {
		db.runSQL("DELETE FROM tasks;");
		db.runSQL("INSERT INTO tasks (task, active, usage) " + "VALUES ('"
				+ Helpers.text1 + "', 1, 40);");
		db.runSQL("INSERT INTO tasks (task, active, usage) " + "VALUES ('"
				+ Helpers.text5 + "', 0, 30);");
		db.runSQL("INSERT INTO tasks (task, active, usage) " + "VALUES ('"
				+ Helpers.text3 + "', 1, 50);");
		db.runSQL("INSERT INTO tasks (task, active, usage) " + "VALUES ('"
				+ Helpers.text4 + "', 1, 20);");
		db.runSQL("DELETE FROM timesheet;");

		assertTrue(true);
	}

	// Verify the database is empty.
	public void test010VerifyDBisSetup() {
		assertTrue(solo.searchText(Helpers.text1));
	}

	public void test020SetupDatabase() {
		// Bring up the context menu
		assertTrue(solo.searchText(Helpers.text4));
		solo.clickLongOnText(Helpers.text4);

		assertTrue(solo.waitForText(Helpers.RETIRETASK, 0, 1500));
		// Select the edit task option
		solo.clickOnText(Helpers.EDITTASKNAME);
		assertTrue(solo.waitForText(Helpers.EDITTASKNAME, 0, 1500));

		// Clear the existing text and replace with new.
		solo.clearEditText(0);
		solo.enterText(0, Helpers.text2);
		solo.clickOnText(solo
				.getString(com.googlecode.iqapps.IQTimeSheet.R.string.accept));
		assertTrue(solo.waitForText(Helpers.text2, 0, 1500));
	}

	public void test030VerifyEntries() {
		assertTrue(solo.searchText(Helpers.text3));
		assertTrue(solo.searchText(Helpers.text1));
		assertTrue(solo.searchText(Helpers.text2));
		assertFalse(solo.searchText(Helpers.text5));
	}

	public void test040reviveTask() {
		// Bring up the revive task activity.
		int menuItemID = mActivity.getOptionsMenu()
				.getItem(MenuItems.REVIVE_TASK.ordinal()).getItemId();
		assertTrue(mInstr.invokeMenuActionSync(mActivity, menuItemID, 0));
		solo.sleep(SLEEPTIME);

		solo.clickOnText(solo
				.getString(com.googlecode.iqapps.IQTimeSheet.R.string.menu_revive_task));
	}

	public void test050VerifyEntries() {
		assertTrue(solo.searchText(Helpers.text3));
		assertTrue(solo.searchText(Helpers.text1));
		assertTrue(solo.searchText(Helpers.text2));
		assertTrue(solo.searchText(Helpers.text5));
	}

	public void test060retireTask() {
		// Bring up the retire task activity.
		solo.clickLongOnText(Helpers.text5);
		solo.sleep(SLEEPTIME);

		solo.clickOnText(solo
				.getString(com.googlecode.iqapps.IQTimeSheet.R.string.taskretire));
	}

	public void test070VerifyEntries() {
		assertTrue(solo.searchText(Helpers.text3));
		assertTrue(solo.searchText(Helpers.text1));
		assertTrue(solo.searchText(Helpers.text2));
		assertFalse(solo.searchText(Helpers.text5));
	}
}