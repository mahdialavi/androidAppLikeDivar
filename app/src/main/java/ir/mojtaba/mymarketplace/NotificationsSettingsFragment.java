package ir.mojtaba.mymarketplace;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ir.mojtaba.mymarketplace.app.App;
import ir.mojtaba.mymarketplace.constants.Constants;
import ir.mojtaba.mymarketplace.util.CustomRequest;

public class NotificationsSettingsFragment extends PreferenceFragment implements Constants {

    private CheckBoxPreference mAllowReviewsGCM, mAllowCommentsGCM, mAllowMessagesGCM, mAllowCommentReplyGCM;

    private ProgressDialog pDialog;

    int mAllowReviews, mAllowComments, mAllowMessages, mAllowCommentReply;

    private Boolean loading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        initpDialog();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.notifications_settings);

        mAllowReviewsGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowReviewsGCM");

        mAllowReviewsGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowReviews = 1;

                    } else {

                        mAllowReviews = 0;
                    }

                    if (App.getInstance().isConnected()) {

                        setAllowReviews();

                    } else {

                        Toast.makeText(getActivity().getApplicationContext(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            }
        });

        mAllowCommentsGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowCommentsGCM");

        mAllowCommentsGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowComments = 1;

                    } else {

                        mAllowComments = 0;
                    }

                    if (App.getInstance().isConnected()) {

                        setAllowComments();

                    } else {

                        Toast.makeText(getActivity().getApplicationContext(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            }
        });

        mAllowMessagesGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowMessagesGCM");

        mAllowMessagesGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowMessages = 1;

                    } else {

                        mAllowMessages = 0;
                    }

                    if (App.getInstance().isConnected()) {

                        setAllowMessages();

                    } else {

                        Toast.makeText(getActivity().getApplicationContext(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            }
        });

        mAllowCommentReplyGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowCommentReplyGCM");

        mAllowCommentReplyGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowCommentReply = 1;

                    } else {

                        mAllowCommentReply = 0;
                    }

                    if (App.getInstance().isConnected()) {

                        setAllowCommentReply();

                    } else {

                        Toast.makeText(getActivity().getApplicationContext(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            }
        });

        checkAllowReviews(App.getInstance().getAllowReviewsGCM());
        checkAllowComments(App.getInstance().getAllowCommentsGCM());
        checkAllowMessages(App.getInstance().getAllowMessagesGCM());
        checkAllowCommentReply(App.getInstance().getAllowCommentReplyGCM());
    }

    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            loading = savedInstanceState.getBoolean("loading");

        } else {

            loading = false;
        }

        if (loading) {

            showpDialog();
        }
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("loading", loading);
    }

    public void checkAllowReviews(int value) {

        if (value == 1) {

            mAllowReviewsGCM.setChecked(true);
            mAllowReviews = 1;

        } else {

            mAllowReviewsGCM.setChecked(false);
            mAllowReviews = 0;
        }
    }

    public void checkAllowComments(int value) {

        if (value == 1) {

            mAllowCommentsGCM.setChecked(true);
            mAllowComments = 1;

        } else {

            mAllowCommentsGCM.setChecked(false);
            mAllowComments = 0;
        }
    }

    public void checkAllowMessages(int value) {

        if (value == 1) {

            mAllowMessagesGCM.setChecked(true);
            mAllowMessages = 1;

        } else {

            mAllowMessagesGCM.setChecked(false);
            mAllowMessages = 0;
        }
    }

    public void checkAllowCommentReply(int value) {

        if (value == 1) {

            mAllowCommentReplyGCM.setChecked(true);
            mAllowCommentReply = 1;

        } else {

            mAllowCommentReplyGCM.setChecked(false);
            mAllowCommentReply = 0;
        }
    }

    public void setAllowReviews() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_SETTINGS_REVIEWS_GCM, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                App.getInstance().setAllowReviewsGCM(response.getInt("allowReviewsGCM"));

                                checkAllowReviews(App.getInstance().getAllowReviewsGCM());
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loading = false;

                hidepDialog();

                Toast.makeText(getActivity().getApplicationContext(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("clientId", CLIENT_ID);
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("allowReviewsGCM", Integer.toString(mAllowReviews));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void setAllowComments() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_SETTINGS_COMMENTS_GCM, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                App.getInstance().setAllowCommentsGCM(response.getInt("allowCommentsGCM"));

                                checkAllowComments(App.getInstance().getAllowCommentsGCM());
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loading = false;

                hidepDialog();

                Toast.makeText(getActivity().getApplicationContext(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("clientId", CLIENT_ID);
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("allowCommentsGCM", Integer.toString(mAllowComments));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void setAllowMessages() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_SETTINGS_MESSAGES_GCM, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                App.getInstance().setAllowMessagesGCM(response.getInt("allowMessagesGCM"));

                                checkAllowMessages(App.getInstance().getAllowMessagesGCM());
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loading = false;

                hidepDialog();

                Toast.makeText(getActivity().getApplicationContext(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("clientId", CLIENT_ID);
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("allowMessagesGCM", Integer.toString(mAllowMessages));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void setAllowCommentReply() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_SETTINGS_COMMENT_REPLY_GCM, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                App.getInstance().setAllowCommentReplyGCM(response.getInt("allowCommentReplyGCM"));

                                checkAllowCommentReply(App.getInstance().getAllowCommentReplyGCM());
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loading = false;

                hidepDialog();

                Toast.makeText(getActivity().getApplicationContext(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("clientId", CLIENT_ID);
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("allowCommentReplyGCM", Integer.toString(mAllowCommentReply));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing())
            pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}