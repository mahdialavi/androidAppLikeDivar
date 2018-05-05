package ir.mojtaba.mymarketplace;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ir.mojtaba.mymarketplace.adapter.StreamListAdapter;
import ir.mojtaba.mymarketplace.app.App;
import ir.mojtaba.mymarketplace.constants.Constants;
import ir.mojtaba.mymarketplace.dialogs.NearbySettingsDialog;
import ir.mojtaba.mymarketplace.model.Item;
import ir.mojtaba.mymarketplace.util.CustomRequest;

public class ItemsNearbyFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_LIST = "State Adapter Data";

    Menu MainMenu;

    RecyclerView mRecyclerView;
    TextView mMessage, mDetails;
    ImageView mSplash, mCloseSpotLight;

    SwipeRefreshLayout mItemsContainer;

    LinearLayout mSpotLight;

    private ArrayList<Item> itemsList;
    private StreamListAdapter itemsAdapter;

    private int itemId = 0;
    private int arrayLength = 0;
    private Boolean loadingMore = false;
    private Boolean viewMore = false;
    private Boolean restore = false;
    private Boolean spotlight = true;

    int pastVisiblesItems = 0, visibleItemCount = 0, totalItemCount = 0;

    private int distance = 50;      // im miles

    public ItemsNearbyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

//        setRetainInstance(true);

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            itemsAdapter = new StreamListAdapter(getActivity(), itemsList);

            restore = savedInstanceState.getBoolean("restore");
            spotlight = savedInstanceState.getBoolean("spotlight");
            itemId = savedInstanceState.getInt("itemId");
            distance = savedInstanceState.getInt("distance");
            viewMore = savedInstanceState.getBoolean("viewMore");

        } else {

            itemsList = new ArrayList<Item>();
            itemsAdapter = new StreamListAdapter(getActivity(), itemsList);

            restore = false;
            spotlight = true;
            itemId = 0;
            distance = 50;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_items_nearby, container, false);

        mItemsContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.container_items);
        mItemsContainer.setOnRefreshListener(this);

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/irsans.ttf");
		mMessage = (TextView) rootView.findViewById(R.id.message);
        mMessage.setTypeface(font);
		
		mSplash = (ImageView) rootView.findViewById(R.id.splash);

        mSpotLight = (LinearLayout) rootView.findViewById(R.id.spotlight);
        mDetails = (TextView) rootView.findViewById(R.id.openLocationSettings);
        mDetails.setTypeface(font);
		
		mCloseSpotLight = (ImageView) rootView.findViewById(R.id.closeSpotlight);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        final GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setAdapter(itemsAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) //check for scroll down
                {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (!loadingMore)
                    {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && (viewMore) && !(mItemsContainer.isRefreshing()))
                        {
                            loadingMore = true;
                            Log.e("...", "Last Item Wow !");

                            getItems();
                        }
                    }
                }
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Item i = (Item) itemsList.get(position);

                Intent intent = new Intent(getActivity(), ViewItemActivity.class);
                intent.putExtra("itemId", i.getId());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // ...
            }
        }));

        if (itemsAdapter.getItemCount() == 0) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();
        }

        if (!restore) {

            showMessage(getText(R.string.msg_loading_2).toString());

            getItems();
        }

        mCloseSpotLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSpotLight.setVisibility(View.GONE);

                spotlight = false;
            }
        });


        mDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), LocationActivity.class);
                startActivityForResult(i, 101);
            }
        });

        updateSpotLight();


        // Inflate the layout for this fragment
        return rootView;
    }

    public void updateSpotLight() {

        if (spotlight) {

            if (App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {

                mSpotLight.setVisibility(View.GONE);

                spotlight = false;

            } else {

                mSpotLight.setVisibility(View.VISIBLE);
            }

        } else {

            mSpotLight.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {

        super.onStart();

        updateSpotLight();
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            itemId = 0;

            getItems();

        } else {

            mItemsContainer.setRefreshing(false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("viewMore", viewMore);
        outState.putBoolean("restore", true);
        outState.putBoolean("spotlight", spotlight);
        outState.putInt("itemId", itemId);
        outState.putInt("distance", distance);
        outState.putParcelableArrayList(STATE_LIST, itemsList);
    }

    public void onChangeDistance(int position) {

        switch (position) {

            case 0: {

                distance = 50;

                itemId = 0;

                getItems();

                break;
            }

            case 1: {

                distance = 100;

                itemId = 0;

                getItems();

                break;
            }

            case 2: {

                distance = 250;

                itemId = 0;

                getItems();

                break;
            }

            case 3: {

                distance = 500;

                itemId = 0;

                getItems();

                break;
            }

            case 4: {

                distance = 1000;

                itemId = 0;

                getItems();

                break;
            }

            default: {

                distance = 50;

                itemId = 0;

                getItems();

                break;
            }
        }
    }

    public void getItems() {

        mItemsContainer.setRefreshing(true);

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_NEARBY_ITEMS_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!loadingMore) {

                            itemsList.clear();
                        }

                        try {

//                            Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                itemId = response.getInt("itemId");

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                            Item c = new Item(itemObj);

                                            itemsList.add(c);
                                        }
                                    }
                                }

                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loadingComplete();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loadingComplete();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("distance", Integer.toString(distance));
                params.put("itemId", Long.toString(itemId));
                params.put("lat", Double.toString(App.getInstance().getLat()));
                params.put("lng", Double.toString(App.getInstance().getLng()));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void loadingComplete() {

        if (arrayLength == LIST_ITEMS) {

            viewMore = true;

        } else {

            viewMore = false;
        }

        itemsAdapter.notifyDataSetChanged();

        if (itemsAdapter.getItemCount() == 0) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();
        }

        loadingMore = false;
        mItemsContainer.setRefreshing(false);
    }

    static class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

        public interface OnItemClickListener {

            void onItemClick(View view, int position);

            void onItemLongClick(View view, int position);
        }

        private OnItemClickListener mListener;

        private GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {

            mListener = listener;

            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                    View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());

                    if (childView != null && mListener != null) {

                        mListener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {

            View childView = view.findChildViewUnder(e.getX(), e.getY());

            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {

                mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {

            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {

                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge

                    outRect.top = spacing;
                }

                outRect.bottom = spacing; // item bottom

            } else {

                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)

                if (position >= spanCount) {

                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {

        Resources r = getResources();

        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public void showMessage(String message) {

        mMessage.setText(message);
        mMessage.setVisibility(View.VISIBLE);

        mSplash.setVisibility(View.VISIBLE);
    }

    public void hideMessage() {

        mMessage.setVisibility(View.GONE);

        mSplash.setVisibility(View.GONE);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_nearby, menu);

        MainMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.action_nearby_settings: {

                /** Getting the fragment manager */
                android.app.FragmentManager fm = getActivity().getFragmentManager();

                /** Instantiating the DialogFragment class */
                NearbySettingsDialog alert = new NearbySettingsDialog();

                /** Creating a bundle object to store the selected item's index */
                Bundle b  = new Bundle();

                /** Storing the selected item's index in the bundle object */
                b.putInt("distance", distance);

                /** Setting the bundle object to the dialog fragment object */
                alert.setArguments(b);

                /** Creating the dialog fragment object, which will in turn open the alert dialog window */

                alert.show(fm, "alert_dialog_nearby_settings");

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}