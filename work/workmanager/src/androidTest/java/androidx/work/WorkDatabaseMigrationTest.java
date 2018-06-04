/*
 * Copyright 2018 The Android Open Source Project
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

package androidx.work;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.arch.persistence.room.testing.MigrationTestHelper;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import androidx.work.impl.WorkDatabase;
import androidx.work.impl.WorkDatabaseMigrations;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class WorkDatabaseMigrationTest {

    private static final String TEST_DATABASE = "workdatabase-test";
    private static final boolean VALIDATE_DROPPED_TABLES = true;
    private static final int NEW_VERSION = 2;
    private static final String COLUMN_WORKSPEC_ID = "work_spec_id";
    private static final String COLUMN_SYSTEM_ID = "system_id";

    // Queries
    private static final String INSERT_ALARM_INFO = "INSERT INTO alarmInfo VALUES (?, ?)";
    private static final String CHECK_SYSTEM_ID_INFO = "SELECT * FROM systemIdInfo";
    private static final String CHECK_TABLE_NAME = "SELECT * FROM %s";
    private static final String TABLE_ALARM_INFO = "alarmInfo";
    private static final String TABLE_WORKSPEC = "WorkSpec";
    private static final String TABLE_WORKTAG = "WorkTag";
    private static final String TABLE_WORKNAME = "WorkName";

    private File mDatabasePath;

    @Rule
    public MigrationTestHelper mMigrationTestHelper = new MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            WorkDatabase.class.getCanonicalName(),
            new FrameworkSQLiteOpenHelperFactory());

    @Before
    public void setUp() {
        // Delete the database if it exists.
        mDatabasePath = InstrumentationRegistry.getContext().getDatabasePath(TEST_DATABASE);
        if (mDatabasePath.exists()) {
            mDatabasePath.delete();
        }
    }

    @Test
    @MediumTest
    public void testMigrationVersion1To2() throws IOException {
        SupportSQLiteDatabase database =
                mMigrationTestHelper.createDatabase(TEST_DATABASE, 1);

        String workSpecId1 = UUID.randomUUID().toString();
        String workSpecId2 = UUID.randomUUID().toString();

        // insert alarmInfos
        database.execSQL(INSERT_ALARM_INFO, new Object[]{workSpecId1, 1});
        database.execSQL(INSERT_ALARM_INFO, new Object[]{workSpecId2, 2});

        database.close();

        database = mMigrationTestHelper.runMigrationsAndValidate(
                TEST_DATABASE,
                NEW_VERSION,
                VALIDATE_DROPPED_TABLES,
                WorkDatabaseMigrations.MIGRATION_1_2);

        Cursor cursor = database.query(CHECK_SYSTEM_ID_INFO);
        assertThat(cursor.getCount(), is(2));
        cursor.moveToFirst();
        assertThat(cursor.getString(cursor.getColumnIndex(COLUMN_WORKSPEC_ID)), is(workSpecId1));
        assertThat(cursor.getInt(cursor.getColumnIndex(COLUMN_SYSTEM_ID)), is(1));
        cursor.moveToNext();
        assertThat(cursor.getString(cursor.getColumnIndex(COLUMN_WORKSPEC_ID)), is(workSpecId2));
        assertThat(cursor.getInt(cursor.getColumnIndex(COLUMN_SYSTEM_ID)), is(2));
        cursor.close();

        assertThat(checkExists(database, TABLE_ALARM_INFO), is(false));
        assertThat(checkExists(database, TABLE_WORKSPEC), is(true));
        assertThat(checkExists(database, TABLE_WORKTAG), is(true));
        assertThat(checkExists(database, TABLE_WORKNAME), is(true));
        database.close();
    }

    private boolean checkExists(SupportSQLiteDatabase database, String tableName) {
        Cursor cursor = null;
        try {
            cursor = database.query(String.format(CHECK_TABLE_NAME, tableName));
            return true;
        } catch (SQLiteException ignored) {
            // Should fail with a SQLiteException (no such table: tableName)
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}