package ir.mojtaba.mymarketplace.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ir.mojtaba.mymarketplace.R;
import ir.mojtaba.mymarketplace.app.App;
import ir.mojtaba.mymarketplace.constants.Constants;
import ir.mojtaba.mymarketplace.model.Item;

public class Api extends Application implements Constants {

    Context context;

    public Api (Context context) {

        this.context = context;
    }

    public void profileReport(final long profileId, final int reason) {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_REPORT, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Toast.makeText(context, context.getText(R.string.label_profile_reported), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(context, context.getText(R.string.label_profile_reported), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profileId));
                params.put("reason", Integer.toString(reason));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void itemShare(Item item) {

        String shareText = "";

        if (item.getTitle().length() > 0) {

            shareText = item.getTitle() + " \n\n" + context.getString(R.string.app_share_text);

        } else {

            if (item.getImgUrl() == null || item.getImgUrl().length() == 0) {

                shareText = item.getContent();
            }
        }


        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);

        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, (String) context.getString(R.string.app_name));
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);

        if (item.getImgUrl().length() > 0) {

            ImageView image;
            ImageLoader imageLoader = App.getInstance().getImageLoader();

            image = new ImageView(context);

            if (imageLoader == null) {

                imageLoader = App.getInstance().getImageLoader();
            }

            imageLoader.get(item.getImgUrl(), ImageLoader.getImageListener(image, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

            Drawable mDrawable = image.getDrawable();
            Bitmap mBitmap = ((BitmapDrawable)mDrawable).getBitmap();

            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), mBitmap, "Image Description", null);

            Uri uri = Uri.parse(path);

            shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
        }

        shareIntent.setType("text/plain");

        context.startActivity(Intent.createChooser(shareIntent, "Item post"));
    }

    public void itemDelete(final long itemId) {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEMS_REMOVE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(context, context.getString(R.string.error_data_loading), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(itemId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void itemReport(final long itemId, final int reasonId) {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEMS_REPORT, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Toast.makeText(context, context.getString(R.string.label_post_reported), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(context, context.getString(R.string.label_post_reported), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(itemId));
                params.put("abuseId", Integer.toString(reasonId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void commentDelete(final long commentId) {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_COMMENTS_REMOVE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(context, context.getString(R.string.label_post_reported), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("commentId", Long.toString(commentId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }
}
