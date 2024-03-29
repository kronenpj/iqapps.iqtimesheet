package com.googlecode.iqapps.IQTimeSheet;

/**
 * Class to export the current database to the SDcard.
 *
 * @author Paul Kronenwetter <kronenpj@gmail.com>
 * @author Neil http://stackoverflow.com/users/319625/user319625
 */

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SDBackup {
    private static final String TAG = "SDBackup";

    public static boolean doSDBackup(String databaseName, String packageName) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            Log.d(TAG, "SDBackup: databaseName: " + databaseName);
            Log.d(TAG, "SDBackup: packageName: " + packageName);

            if (sd.canWrite()) {
                String currentDBPath = "/data/" + packageName + "/databases/"
                        + databaseName;
                String backupDBPath = packageName
                        .substring(packageName.lastIndexOf('.') + 1);
                File currentDB = new File(data, currentDBPath);
                File backupDir = new File(sd, backupDBPath);
                File backupDB = new File(sd, backupDBPath + "/" + databaseName);

                String currentPrefPath = "/data/" + packageName + "/shared_prefs/"
                        + packageName + "_preferences.xml";
                File currentPref = new File(data, currentPrefPath);
                File backupPref = new File(sd, backupDBPath + "/" + "preferences.xml");

                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                Date currentTime_1 = new Date();
                String dateString = formatter.format(currentTime_1);
                File backupDBDate = new File(sd, backupDBPath + "/"
                        + databaseName + "-" + dateString);

                Log.d(TAG, "SDBackup: currentDBPath: " + currentDBPath);
                Log.d(TAG, "SDBackup: backupDBPath: " + backupDBPath);
                Log.d(TAG, "SDBackup: backupDBDatePath: " + backupDBPath + "-"
                        + dateString);

                if (currentDB.exists()) {
                    Log.d(TAG, "SDBackup: Checking directory: " + backupDBPath);
                    if (!backupDir.exists()) {
                        Log.d(TAG, "SDBackup: Creating directory: "
                                + backupDBPath);
                        if (!backupDir.mkdir()) {
                            Log.d(TAG, "SDBackup: Directory creation failed.");
                        }
                    } else {
                        Log.d(TAG, "SDBackup: " + backupDBPath + " exists.");
                    }
                    FileChannel src = new FileInputStream(currentDB)
                            .getChannel();
                    FileChannel dst = new FileOutputStream(backupDB)
                            .getChannel();
                    dst.transferFrom(src, 0, src.size());
                    dst.close();
                    src.close();
                    // Make a second backup with today's date.
                    src = new FileInputStream(currentDB).getChannel();
                    dst = new FileOutputStream(backupDBDate).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    dst.close();
                    src.close();

                    // Make a backup of the preferences
                    try {
                        src = new FileInputStream(currentPref).getChannel();
                        dst = new FileOutputStream(backupPref).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        dst.close();
                        src.close();
                    } catch (FileNotFoundException e) {
                        Log.i(TAG, "FileNotFoundException: " + e.toString());
                    }

                    return true;
                } else {
                    Log
                            .d(TAG, "SDBackup: " + currentDBPath
                                    + " doesn't exist.");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "SDBackup threw exception: " + e.toString());
        }
        return false;
    }

    public static boolean doSDRestore(String databaseName, String packageName) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            Log.d(TAG, "SDRestore: databaseName: " + databaseName);
            Log.d(TAG, "SDRestore: packageName: " + packageName);

            if (sd.canWrite()) {
                String currentDBPath = "/data/" + packageName + "/databases/"
                        + databaseName;
                String backupDBPath = packageName
                        .substring(packageName.lastIndexOf('.') + 1);
                File currentDB = new File(data, currentDBPath);
                File currentDBbak = new File(data, currentDBPath + ".bak");
                File backupDB = new File(sd, backupDBPath + "/" + databaseName);
                Log.d(TAG, "SDRestore: currentDBPath: " + currentDBPath);
                Log.d(TAG, "SDRestore: backupDBPath: " + backupDBPath);

                String currentPrefPath = "/data/" + packageName + "/shared_prefs/"
                        + packageName + "_preferences.xml";
                File currentPref = new File(data, currentPrefPath);
                File backupPref = new File(sd, backupDBPath + "/" + "preferences.xml");

                if (currentDBbak.exists()) currentDBbak.delete();

                FileChannel src;
                FileChannel dst;
                if (backupDB.exists()) {
                    currentDB.renameTo(currentDBbak);
                    src = new FileInputStream(backupDB).getChannel();
                    dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    return true;
                } else {
                    Log.d(TAG, "SDRestore: " + currentDBPath
                            + " doesn't exist.");
                }

                // Make a backup of the preferences
                try {
                    src = new FileInputStream(backupPref).getChannel();
                    dst = new FileOutputStream(currentPref).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    dst.close();
                    src.close();
                } catch (FileNotFoundException e) {
                    Log.i(TAG, "FileNotFoundException: " + e.toString());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "SDRestore threw exception: " + e.toString());
            Log.e(TAG, e.getLocalizedMessage());
        }
        return false;
    }
}