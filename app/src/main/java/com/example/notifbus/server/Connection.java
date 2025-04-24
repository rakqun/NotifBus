package com.example.notifbus.server;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Connection {
    public java.sql.Connection connection(Context context) {
        ApplicationInfo ai = null;
        try {
            ai = context.getApplicationContext().getPackageManager()
                    .getApplicationInfo(context.getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        Bundle metaData = ai.metaData;
        String ip = metaData.getString("IP_KEY");
        String user = metaData.getString("USER_KEY");
        String db = metaData.getString("DB_KEY");
        String pass = metaData.getString("PAS_KEY");
        StrictMode.ThreadPolicy p = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(p);
        java.sql.Connection con = null;
        String url = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            url = "jdbc:jtds:sqlserver://" + ip + ";" + "databaseName=" + db + ";" + "user=" + user + ";" + "password=" + pass + ";";
            con = DriverManager.getConnection(url);
        } catch (SQLException a){
            Log.e("Error SQL:", a.getMessage());
        } catch (Exception e) {
            Log.e("Error :", e.getMessage());
        }
        return con;
    }
}
