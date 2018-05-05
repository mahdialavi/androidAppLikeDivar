package ir.mojtaba.mymarketplace;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import github.ankushsachdeva.emojicon.EmojiconEditText;
import ir.mojtaba.mymarketplace.app.App;
import ir.mojtaba.mymarketplace.constants.Constants;
import ir.mojtaba.mymarketplace.dialogs.ItemCategoryDialog;
import ir.mojtaba.mymarketplace.dialogs.ItemImageChooseDialog;
import ir.mojtaba.mymarketplace.util.CustomRequest;

public class NewItemFragment extends Fragment implements Constants {

    public static final int RESULT_OK = -1;

    private ProgressDialog pDialog;

    ArrayList<String> categories;

    EmojiconEditText mItemEdit;
    EditText mItemTitle, mItemPrice;
    ImageView mChoiceItemImg, mLocationDelete;
    CheckBox mAllowComments;

    TextView mLabelItemCategory, mLocationCountry, mLocationCity, mAddLocation;
    LinearLayout mItemCategoryChoose, mLocationContainer;

    String title = "", description = "", imgUrl = "", previewImgUrl = "", postArea = "", postCountry = "", postCity = "", postLat = "0.000000", postLng = "0.000000";
    private String selectedPostImg = "";
    private Uri selectedImage;
    private Uri outputFileUri;

    private int categoryId = 0, price = 0, allowComments = 1;

    private Boolean loading = false;

    public NewItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(true);

        initpDialog();

        Intent i = getActivity().getIntent();

        categoryId = i.getIntExtra("categoryId", 0);

        if (categoryId > 0) {

            categoryId--;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_new_item, container, false);

        if (loading) {

            showpDialog();
        }
		
		Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/irsans.ttf");
        mItemEdit = (EmojiconEditText) rootView.findViewById(R.id.itemDescription);
        mItemTitle = (EditText) rootView.findViewById(R.id.itemTitle);
        mItemPrice = (EditText) rootView.findViewById(R.id.itemPrice);
        mChoiceItemImg = (ImageView) rootView.findViewById(R.id.choiceItemImg);
        mAddLocation = (TextView) rootView.findViewById(R.id.addLocation);
		mAddLocation.setTypeface(font);
        mLocationDelete = (ImageView) rootView.findViewById(R.id.locationDelete);
        mAllowComments = (CheckBox) rootView.findViewById(R.id.allowComments);

        if (price != 0) {

            mItemPrice.setText(Integer.toString(price));
        }

        mItemTitle.setText(title);
        mItemEdit.setText(description);

        if (allowComments == 1) {

            mAllowComments.setChecked(true);

        } else {

            mAllowComments.setChecked(false);
        }

        setEditTextMaxLength(POST_CHARACTERS_LIMIT);

        mLocationDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteLocation();
            }
        });

        mAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().getCountry().length() != 0 || App.getInstance().getCity().length() != 0) {

                    setLocation();

                } else {

                    Intent i = new Intent(getActivity(), LocationActivity.class);
                    startActivityForResult(i, 77);
                }
            }
        });

        mLocationContainer = (LinearLayout) rootView.findViewById(R.id.locationContainer);

        mLocationCountry = (TextView) rootView.findViewById(R.id.locationCountry);
        mLocationCountry.setTypeface(font);
		mLocationCity = (TextView) rootView.findViewById(R.id.locationCity);
		mLocationCity.setTypeface(font);
        
		mLabelItemCategory = (TextView) rootView.findViewById(R.id.labelItemCategory);
        mLabelItemCategory.setTypeface(font);
		
		mItemCategoryChoose = (LinearLayout) rootView.findViewById(R.id.itemCategoryChoose);

        mItemCategoryChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseCategory();
            }
        });

        mChoiceItemImg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (selectedPostImg.length() == 0) {

                    choiceImage();

                } else {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle(getText(R.string.action_remove));

                    alertDialog.setMessage(getText(R.string.label_delete_img));
                    alertDialog.setCancelable(true);

                    alertDialog.setNeutralButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.cancel();
                        }
                    });

                    alertDialog.setPositiveButton(getText(R.string.action_remove), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            mChoiceItemImg.setImageResource(R.drawable.ic_camera);
                            selectedPostImg = "";
                            dialog.cancel();
                        }
                    });

                    alertDialog.show();
                }
            }
        });

        mItemEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                int cnt = s.length();

                if (cnt == 0) {

                    getActivity().setTitle(getText(R.string.title_activity_new_item));

                } else {

                    getActivity().setTitle(Integer.toString(POST_CHARACTERS_LIMIT - cnt));
                }
            }

        });

        mItemTitle.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                getActivity().setTitle(getText(R.string.title_activity_new_item));
            }

        });

        mItemPrice.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                getActivity().setTitle(getText(R.string.title_activity_new_item));
            }

        });

        if (selectedPostImg != null && selectedPostImg.length() > 0) {

            mChoiceItemImg.setImageURI(Uri.fromFile(new File(selectedPostImg)));
        }

        setItemCategoryText();
        showLocation();

        // Inflate the layout for this fragment
        return rootView;
    }

    public void setEditTextMaxLength(int length) {

        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(length);
        mItemEdit.setFilters(FilterArray);
    }

    public void setLocation() {

        postCountry = App.getInstance().getCountry();
        postCity = App.getInstance().getCity();
        postArea = App.getInstance().getArea();
        postLat = Double.toString(App.getInstance().getLat());
        postLng = Double.toString(App.getInstance().getLng());

        showLocation();
    }

    public void deleteLocation() {

        postCountry = "";
        postCity = "";
        postArea = "";
        postLat = "0.000000";
        postLng = "0.000000";

        showLocation();
    }

    public void showLocation() {

        if (postCountry.length() > 0 || postCity.length() > 0) {

            mLocationContainer.setVisibility(View.VISIBLE);

            mAddLocation.setVisibility(View.GONE);

            if (postCountry.length() > 0) {

                mLocationCountry.setText(postCountry);
                mLocationCountry.setVisibility(View.VISIBLE);

            } else {

                mLocationCountry.setVisibility(View.VISIBLE);
            }

            if (postCity.length() > 0) {

                mLocationCity.setText(postCity);
                mLocationCity.setVisibility(View.VISIBLE);

            } else {

                mLocationCity.setVisibility(View.VISIBLE);
            }

        } else {

            mLocationContainer.setVisibility(View.GONE);
            mAddLocation.setVisibility(View.VISIBLE);
        }
    }


    public void setItemCategoryText() {

        categories = App.getInstance().getCategoriesList();

        mLabelItemCategory.setText(categories.get(categoryId));
    }

    public void onItemCategory(int catId) {

        categoryId = catId;

        setItemCategoryText();
    }

    public void chooseCategory() {

        /** Getting the fragment manager */
        android.app.FragmentManager fm = getActivity().getFragmentManager();

        /** Instantiating the DialogFragment class */
        ItemCategoryDialog alert = new ItemCategoryDialog();

        /** Creating a bundle object to store the selected item's index */
        Bundle b  = new Bundle();

        /** Storing the selected item's index in the bundle object */
        b.putInt("categoryId", categoryId);

        /** Setting the bundle object to the dialog fragment object */
        alert.setArguments(b);

        /** Creating the dialog fragment object, which will in turn open the alert dialog window */

        alert.show(fm, "alert_dialog_post_mode");
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_post: {

                if (App.getInstance().isConnected()) {

                    if (mAllowComments.isChecked()) {

                        allowComments = 1;

                    } else {

                        allowComments = 0;
                    }

                    description = mItemEdit.getText().toString();
                    description = description.trim();

                    if (mItemPrice.getText().toString().length() > 0) {

                        price = Integer.parseInt(mItemPrice.getText().toString());

                    } else {

                        price = 0;
                    }

                    title = mItemTitle.getText().toString();
                    title = title.trim();

                    if (selectedPostImg != null && selectedPostImg.length() > 0) {

                        if (title.length() > 0) {

                            if (price > 0) {

                                if (description.length() > 0) {

                                    loading = true;

                                    showpDialog();

                                    File f = new File(Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER, "post.jpg");

                                    uploadFile(METHOD_ITEMS_UPLOAD_IMG, f);

                                } else {

                                    Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_item_select_description), Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }

                            } else {

                                Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_item_select_price), Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }

                        } else {

                            Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_item_select_title), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }

                    } else {

                        Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_item_select_img), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }

                } else {

                    Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

                return true;
            }

            default: {

                break;
            }
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_POST_IMG && resultCode == RESULT_OK && null != data) {

            selectedImage = data.getData();

            selectedPostImg = getImageUrlWithAuthority(getActivity(), selectedImage, "post.jpg");

            try {

                selectedPostImg = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + "post.jpg";

                mChoiceItemImg.setImageURI(Uri.fromFile(new File(selectedPostImg)));

            } catch (Exception e) {

                Log.e("OnSelectPostImage", e.getMessage());
            }

        } else if (requestCode == CREATE_POST_IMG && resultCode == getActivity().RESULT_OK) {

            try {

                selectedPostImg = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + "post.jpg";

                mChoiceItemImg.setImageURI(Uri.fromFile(new File(selectedPostImg)));

            } catch (Exception ex) {

                Log.v("OnCameraCallBack", ex.getMessage());
            }

        } else if (requestCode == 77 && resultCode == getActivity().RESULT_OK) {

            setLocation();
        }
    }

    public static String getImageUrlWithAuthority(Context context, Uri uri, String fileName) {

        InputStream is = null;

        if (uri.getAuthority() != null) {

            try {

                is = context.getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);

                return writeToTempImageAndGetPathUri(context, bmp, fileName).toString();

            } catch (FileNotFoundException e) {

                e.printStackTrace();

            } finally {

                try {

                    if (is != null) {

                        is.close();
                    }

                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static String writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage, String fileName) {

        String file_path = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER;
        File dir = new File(file_path);
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, fileName);

        try {

            FileOutputStream fos = new FileOutputStream(file);

            inImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.flush();
            fos.close();

        } catch (FileNotFoundException e) {

            Toast.makeText(inContext, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {

            e.printStackTrace();
        }

        return Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + "post.jpg";
    }

    public void choiceImage() {

        android.app.FragmentManager fm = getActivity().getFragmentManager();

        ItemImageChooseDialog alert = new ItemImageChooseDialog();

        alert.show(fm, "alert_dialog_image_choose");
    }

    public void imageFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, getText(R.string.label_select_img)), SELECT_POST_IMG);
    }

    public void imageFromCamera() {

        try {

            File root = new File(Environment.getExternalStorageDirectory(), APP_TEMP_FOLDER);

            if (!root.exists()) {

                root.mkdirs();
            }

            File sdImageMainDirectory = new File(root, "post.jpg");
            outputFileUri = Uri.fromFile(sdImageMainDirectory);

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(cameraIntent, CREATE_POST_IMG);

        } catch (Exception e) {

            Toast.makeText(getActivity(), "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendItem() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEMS_NEW, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            sendPostSuccess();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                sendPostSuccess();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("categoryId", Integer.toString(categoryId + 1));
                params.put("price", Integer.toString(price));
                params.put("allowComments", Integer.toString(allowComments));
                params.put("title", title);
                params.put("description", description);
                params.put("imgUrl", imgUrl);
                params.put("postArea", postArea);
                params.put("postCountry", postCountry);
                params.put("postCity", postCity);
                params.put("postLat", postLat);
                params.put("postLng", postLng);

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void sendPostSuccess() {

        loading = false;

        hidepDialog();

        Intent i = new Intent();
        getActivity().setResult(RESULT_OK, i);

        Toast.makeText(getActivity(), getText(R.string.msg_item_posted), Toast.LENGTH_SHORT).show();

        getActivity().finish();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



    public Boolean uploadFile(String serverURL, File file) {

        final OkHttpClient client = new OkHttpClient();

        try {

            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("uploaded_file", file.getName(), RequestBody.create(MediaType.parse("text/csv"), file))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .build();

            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(serverURL)
                    .addHeader("Accept", "application/json;")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(com.squareup.okhttp.Request request, IOException e) {

                    loading = false;

                    hidepDialog();

                    Log.e("failure", request.toString());
                }

                @Override
                public void onResponse(com.squareup.okhttp.Response response) throws IOException {

                    String jsonData = response.body().string();

                    Log.e("response", jsonData);

                    try {

                        JSONObject result = new JSONObject(jsonData);

                        if (!result.getBoolean("error")) {

                            imgUrl = result.getString("imgUrl");
                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                    } finally {

                        Log.e("response", jsonData);

                        sendItem();
                    }

                }
            });

            return true;

        } catch (Exception ex) {
            // Handle the error

            loading = false;

            hidepDialog();
        }

        return false;
    }
}