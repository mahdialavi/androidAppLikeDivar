package ir.mojtaba.mymarketplace;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.FacebookSdk;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ir.mojtaba.mymarketplace.app.App;
import ir.mojtaba.mymarketplace.common.ActivityBase;
import ir.mojtaba.mymarketplace.service.RegistrationIntentService;
import ir.mojtaba.mymarketplace.util.CustomRequest;

public class AppActivity extends ActivityBase {

    Button loginBtn, signupBtn;

    RelativeLayout loadingScreen;
    LinearLayout contentScreen;

    Boolean restore = false;
    Boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        FacebookSdk.sdkInitialize(getApplicationContext());

        if (savedInstanceState != null) {

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");

        } else {

            restore = false;
            loading = false;
        }

        contentScreen = (LinearLayout) findViewById(R.id.contentScreen);
        loadingScreen = (RelativeLayout) findViewById(R.id.loadingScreen);

        loginBtn = (Button) findViewById(R.id.loginBtn);
        signupBtn = (Button) findViewById(R.id.signupBtn);

        startService(new Intent(this, RegistrationIntentService.class));

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(AppActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(AppActivity.this, SignupActivity.class);
                startActivity(i);
            }
        });

        if (loading) showLoadingScreen();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("restore", true);
        outState.putBoolean("loading", loading);
    }

    @Override
    protected void onStart() {

        super.onStart();

        if (!loading) {

            showLoadingScreen();

            loading = true;

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_AUTHORIZE, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (App.getInstance().authorize(response)) {

                                if (App.getInstance().getState() != ACCOUNT_STATE_ENABLED) {

                                    App.getInstance().logout();
                                }

                                App.getInstance().updateGeoLocation();

                            } else {

                                App.getInstance().removeData();
                                App.getInstance().readData();
                            }

                            loading = false;

                            Intent intent = new Intent(AppActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    showContentScreen();

                    loading = false;
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("clientId", CLIENT_ID);
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());
                    params.put("gcm_regId", App.getInstance().getGcmToken());

                    return params;
                }
            };

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void showContentScreen() {

        loadingScreen.setVisibility(View.GONE);

        contentScreen.setVisibility(View.VISIBLE);
    }

    public void showLoadingScreen() {

        contentScreen.setVisibility(View.GONE);

        loadingScreen.setVisibility(View.VISIBLE);
    }
}
