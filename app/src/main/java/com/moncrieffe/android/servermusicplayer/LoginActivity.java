package com.moncrieffe.android.servermusicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.moncrieffe.android.servermusicplayer.Credentials.Credentials;
import com.moncrieffe.android.servermusicplayer.Credentials.CredentialsManager;

import java.util.List;

/**
 * Created by Chaz-Rae on 9/7/2016.
 */
public class LoginActivity extends AppCompatActivity{
    private List<Credentials> mCredentials;
    private EditText mWebAddress;
    private Button mGo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mWebAddress = (EditText)findViewById(R.id.web_address);

        try{
             mCredentials = CredentialsManager
                    .get(LoginActivity.this)
                    .getCredentials();
            mWebAddress.setText(mCredentials.get(0).getWebaddress());
        }
        catch (Exception e){
            e.printStackTrace();
        }

        mGo = (Button)findViewById(R.id.go_button);
        mGo.setOnClickListener(new View.OnClickListener() {
            Boolean found = false;
            @Override
            public void onClick(View v) {
                String web = mWebAddress.getText().toString();

                Credentials credentials = new Credentials(web);
                for(Credentials c:mCredentials){
                    if(c.getID().equals(credentials.getID())){
                       found = true;
                    }
                }

                if(found){
                    CredentialsManager.get(LoginActivity.this).updateCredentials(credentials);
                }
                else {
                    CredentialsManager.get(LoginActivity.this).addCredentials(credentials);
                }

                Intent i = DirectoryMenuActivity.newIntent(LoginActivity.this, credentials.getID());
                startActivity(i);
            }
        });
    }
}
