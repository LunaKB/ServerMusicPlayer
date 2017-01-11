package com.moncrieffe.android.servermusicplayer.HTTP;

import android.content.Context;

import com.moncrieffe.android.servermusicplayer.Credentials.Credentials;
import com.moncrieffe.android.servermusicplayer.Credentials.CredentialsManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Chaz-Rae on 9/6/2016.
 */
public class RunnableFunctions {
    private static final String TAG = "FTP";

    /* HTTP Functions */
    public RunnableFunctions(){}

    public class ReadFile{
        private UUID id;
        private List<String> strings = new ArrayList<>();
        private Context context;
        String d;
        public CountDownLatch l = new CountDownLatch(1);

        public ReadFile(UUID uuid, Context c, String directory){
            id = uuid;
            context = c;
            d = directory;
        }


        public void run() {
            try {
                // Create a URL for the desired page
                Credentials c = CredentialsManager.get(context).getCredentials(id);
                d = d.replace(" ", "%20");
                URL url = new URL(c.getWebaddress() + d + "/" + "list.txt");

                // Read all the text returned by the server
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String str;
                while ((str = in.readLine()) != null) {
                    strings.add(str);
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            l.countDown();
        }

        public List<String> getList(){
            return strings;
        }
    }

    public class ReadDirectory{
        private UUID id;
        private List<String> strings = new ArrayList<>();
        private Context context;
        public CountDownLatch l = new CountDownLatch(1);

        public ReadDirectory(UUID uuid, Context c){
            id = uuid;
            context = c;
        }

     //   @Override
        public void run() {
            try {
                // Create a URL for the desired page
                Credentials c = CredentialsManager.get(context).getCredentials(id);
                URL url = new URL(c.getWebaddress() + "list.txt");

                // Read all the text returned by the server
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String str;
                while ((str = in.readLine()) != null) {
                    strings.add(str);
                }
                in.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            l.countDown();
        }

        public List<String> getList(){
            return strings;
        }
    }
}
