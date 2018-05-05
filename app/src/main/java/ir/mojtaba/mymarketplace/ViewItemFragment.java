package ir.mojtaba.mymarketplace;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import github.ankushsachdeva.emojicon.EditTextImeBackListener;
import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;
import ir.mojtaba.mymarketplace.adapter.CommentListAdapter;
import ir.mojtaba.mymarketplace.app.App;
import ir.mojtaba.mymarketplace.constants.Constants;
import ir.mojtaba.mymarketplace.dialogs.CommentActionDialog;
import ir.mojtaba.mymarketplace.dialogs.CommentDeleteDialog;
import ir.mojtaba.mymarketplace.dialogs.ItemActionDialog;
import ir.mojtaba.mymarketplace.dialogs.ItemDeleteDialog;
import ir.mojtaba.mymarketplace.dialogs.ItemReportDialog;
import ir.mojtaba.mymarketplace.dialogs.MyCommentActionDialog;
import ir.mojtaba.mymarketplace.dialogs.MyItemActionDialog;
import ir.mojtaba.mymarketplace.model.Comment;
import ir.mojtaba.mymarketplace.model.Item;
import ir.mojtaba.mymarketplace.util.Api;
import ir.mojtaba.mymarketplace.util.CommentInterface;
import ir.mojtaba.mymarketplace.util.CustomRequest;

public class ViewItemFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener, CommentInterface {

    private ProgressDialog pDialog;

    SwipeRefreshLayout mContentContainer;
    RelativeLayout mErrorScreen, mLoadingScreen, mEmptyScreen;
    LinearLayout mContentScreen, mCommentFormContainer;

    EmojiconEditText mCommentText;

    ListView listView;
    Button mRetryBtn, mItemViewAuthorProfile, mItemCallToAuthor;

    View mListViewHeader;

    ImageView mItemLike, mItemShare, mItemComment, mEmojiBtn, mSendComment;
    TextView mItemCategory, mItemDate, mItemTitle, mItemLikesCount, mItemCommentsCount, mItemPrice, mItemDescription, mItemLocation;
    ImageView mItemImg;

    ImageLoader imageLoader = App.getInstance().getImageLoader();

    private ArrayList<Comment> commentsList;

    private CommentListAdapter itemAdapter;

    Item item;

    long itemId = 0, replyToUserId = 0;
    int arrayLength = 0;
    String commentText;

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;
    private Boolean loadingComplete = false;

    EmojiconsPopup popup;

    public ViewItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(true);

        initpDialog();

        Intent i = getActivity().getIntent();

        itemId = i.getLongExtra("itemId", 0);

        commentsList = new ArrayList<Comment>();
        itemAdapter = new CommentListAdapter(getActivity(), commentsList, this);

        getActivity().setTitle(getString(R.string.title_activity_view_item));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_view_item, container, false);

        popup = new EmojiconsPopup(rootView, getActivity());

        popup.setSizeForSoftKeyboard();

        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                mCommentText.append(emojicon.getEmoji());
            }
        });

        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mCommentText.dispatchKeyEvent(event);
            }
        });

        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {

                setIconEmojiKeyboard();
            }
        });

        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {

                if (popup.isShowing())

                    popup.dismiss();
            }
        });

        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                mCommentText.append(emojicon.getEmoji());
            }
        });

        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mCommentText.dispatchKeyEvent(event);
            }
        });

        if (savedInstanceState != null) {

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");

            replyToUserId = savedInstanceState.getLong("replyToUserId");

        } else {

            restore = false;
            loading = false;
            preload = false;

            replyToUserId = 0;
        }

        if (loading) {

            showpDialog();
        }

        mEmptyScreen = (RelativeLayout) rootView.findViewById(R.id.emptyScreen);
        mErrorScreen = (RelativeLayout) rootView.findViewById(R.id.errorScreen);
        mLoadingScreen = (RelativeLayout) rootView.findViewById(R.id.loadingScreen);
        mContentContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.contentContainer);
        mContentContainer.setOnRefreshListener(this);

        mContentScreen = (LinearLayout) rootView.findViewById(R.id.contentScreen);
        mCommentFormContainer = (LinearLayout) rootView.findViewById(R.id.commentFormContainer);

        mCommentText = (EmojiconEditText) rootView.findViewById(R.id.commentText);
        mSendComment = (ImageView) rootView.findViewById(R.id.sendCommentImg);
        mEmojiBtn = (ImageView) rootView.findViewById(R.id.emojiBtn);

        mSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                send();
            }
        });

        mRetryBtn = (Button) rootView.findViewById(R.id.retryBtn);

        mRetryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().isConnected()) {

                    showLoadingScreen();

                    getItem();
                }
            }
        });

        listView = (ListView) rootView.findViewById(R.id.listView);

        mListViewHeader = getActivity().getLayoutInflater().inflate(R.layout.view_item_list_row, null);

        listView.addHeaderView(mListViewHeader);

        listView.setAdapter(itemAdapter);

        mItemViewAuthorProfile = (Button) mListViewHeader.findViewById(R.id.itemViewAuthorProfile);
        mItemCallToAuthor = (Button) mListViewHeader.findViewById(R.id.itemCallToAuthor);

        mItemPrice = (TextView) mListViewHeader.findViewById(R.id.itemPrice);
        mItemDescription = (TextView) mListViewHeader.findViewById(R.id.itemDescription);

        mItemLike = (ImageView) mListViewHeader.findViewById(R.id.itemLike);
        mItemShare = (ImageView) mListViewHeader.findViewById(R.id.itemShare);
        mItemComment = (ImageView) mListViewHeader.findViewById(R.id.itemComment);

        mItemLocation = (TextView) mListViewHeader.findViewById(R.id.itemLocation);
        mItemDate = (TextView) mListViewHeader.findViewById(R.id.itemDate);
        mItemTitle = (TextView) mListViewHeader.findViewById(R.id.itemTitle);
        mItemCategory = (TextView) mListViewHeader.findViewById(R.id.itemCategory);
        mItemLikesCount = (TextView) mListViewHeader.findViewById(R.id.itemLikesCount);
        mItemCommentsCount = (TextView) mListViewHeader.findViewById(R.id.itemCommentsCount);
        mItemImg = (ImageView) mListViewHeader.findViewById(R.id.itemImg);

        Typeface fontb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/irsans.ttf");
        mItemPrice.setTypeface(fontb);
        mItemDescription.setTypeface(fontb);
        mItemLocation.setTypeface(fontb);
        mItemDate.setTypeface(fontb);
        mItemTitle.setTypeface(fontb);
        mItemCategory.setTypeface(fontb);
        mItemLikesCount.setTypeface(fontb);
        mItemCommentsCount.setTypeface(fontb);
		mItemViewAuthorProfile.setTypeface(fontb);
		mItemCallToAuthor.setTypeface(fontb);


        if (!EMOJI_KEYBOARD) {

            mEmojiBtn.setVisibility(View.GONE);
        }

        mEmojiBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!popup.isShowing()) {

                    if (popup.isKeyBoardOpen()){

                        popup.showAtBottom();
                        setIconSoftKeyboard();

                    } else {

                        mCommentText.setFocusableInTouchMode(true);
                        mCommentText.requestFocus();
                        popup.showAtBottomPending();

                        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mCommentText, InputMethodManager.SHOW_IMPLICIT);
                        setIconSoftKeyboard();
                    }

                } else {

                    popup.dismiss();
                }
            }
        });

        EditTextImeBackListener er = new EditTextImeBackListener() {

            @Override
            public void onImeBack(EmojiconEditText ctrl, String text) {

                hideEmojiKeyboard();
            }
        };

        mCommentText.setOnEditTextImeBackListener(er);

        if (!restore) {

            if (App.getInstance().isConnected()) {

                showLoadingScreen();
                getItem();

            } else {

                showErrorScreen();
            }

        } else {

            if (App.getInstance().isConnected()) {

                if (!preload) {

                    loadingComplete();
                    updateItem();

                } else {

                    showLoadingScreen();
                }

            } else {

                showErrorScreen();
            }
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    public void hideEmojiKeyboard() {

        popup.dismiss();
    }

    public void setIconEmojiKeyboard() {

        mEmojiBtn.setBackgroundResource(R.drawable.ic_emoji);
    }

    public void setIconSoftKeyboard() {

        mEmojiBtn.setBackgroundResource(R.drawable.ic_keyboard);
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

        outState.putBoolean("restore", true);
        outState.putBoolean("loading", loading);
        outState.putBoolean("preload", preload);

        outState.putLong("replyToUserId", replyToUserId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ITEM_EDIT && resultCode == getActivity().RESULT_OK) {

            item.setAllowComments(data.getIntExtra("itemAllowComments", 1));
            item.setCategoryId(data.getIntExtra("categoryId", 1));
            item.setPrice(data.getIntExtra("itemPrice", 0));
            item.setCategoryTitle(data.getStringExtra("categoryTitle"));
            item.setTitle(data.getStringExtra("itemTitle"));
            item.setContent(data.getStringExtra("itemDescription"));
            item.setImgUrl(data.getStringExtra("itemImgUrl"));

            updateItem();

            loadingComplete();
        }
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            mContentContainer.setRefreshing(true);
            getItem();

        } else {

            mContentContainer.setRefreshing(false);
        }
    }

    public void updateItem() {

        if (imageLoader == null) {

            imageLoader = App.getInstance().getImageLoader();
        }

        getActivity().setTitle(item.getTitle());

        if (item.getCity().length() > 0 || item.getCountry().length() > 0) {

            mItemLocation.setVisibility(View.VISIBLE);
            mItemLocation.setText(item.getCountry() + " " + item.getCity());

        } else {

            mItemLocation.setVisibility(View.GONE);
        }

        mItemCategory.setText(item.getCategoryTitle());
        mItemDate.setText(item.getDate());

        mItemComment.setVisibility(View.GONE);
        mItemCommentsCount.setVisibility(View.GONE);

        mItemCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), CategoryActivity.class);
                intent.putExtra("categoryId", item.getCategoryId());
                intent.putExtra("title", item.getCategoryTitle());
                startActivity(intent);
            }
        });

        if (item.getFromUserId() == App.getInstance().getId()) {

            itemAdapter.setMyPost(true);

        } else {

            itemAdapter.setMyPost(false);
        }

        mItemLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (App.getInstance().isConnected()) {

                    CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEMS_LIKE, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    try {

                                        if (!response.getBoolean("error")) {

                                            item.setLikesCount(response.getInt("likesCount"));
                                            item.setMyLike(response.getBoolean("myLike"));
                                        }

                                    } catch (JSONException e) {

                                        e.printStackTrace();

                                    } finally {

                                        updateItem();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            Toast.makeText(getActivity(), getString(R.string.error_data_loading), Toast.LENGTH_LONG).show();
                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("accountId", Long.toString(App.getInstance().getId()));
                            params.put("accessToken", App.getInstance().getAccessToken());
                            params.put("itemId", Long.toString(item.getId()));

                            return params;
                        }
                    };

                    App.getInstance().addToRequestQueue(jsonReq);

                } else {

                    Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (item.getFromUserPhone().length() > 0) {

            mItemCallToAuthor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + item.getFromUserPhone()));
                    startActivity(intent);
                }
            });

            mItemCallToAuthor.setVisibility(View.VISIBLE);

        } else {

            mItemCallToAuthor.setVisibility(View.GONE);
        }

        mItemViewAuthorProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profileId", item.getFromUserId());
                getActivity().startActivity(intent);
            }
        });

        mItemShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Api api = new Api(getActivity());
                api.itemShare(item);
            }
        });

        if (item.isMyLike()) {

            mItemLike.setImageResource(R.drawable.perk_active);

        } else {

            mItemLike.setImageResource(R.drawable.perk);
        }

        if (item.getLikesCount() > 0) {

            mItemLikesCount.setText(Integer.toString(item.getLikesCount()));
            mItemLikesCount.setVisibility(View.VISIBLE);

        } else {

            mItemLikesCount.setText(Integer.toString(item.getLikesCount()));
            mItemLikesCount.setVisibility(View.GONE);
        }

        if (item.getTitle().length() > 0) {

            mItemTitle.setText(item.getTitle());

            mItemTitle.setVisibility(View.VISIBLE);

        } else {

            mItemTitle.setVisibility(View.GONE);
        }

        if (item.getContent().length() != 0) {

            mItemDescription.setText(item.getContent().replaceAll("<br>", "\n"));
            mItemDescription.setVisibility(View.VISIBLE);

        } else {

            mItemDescription.setVisibility(View.GONE);
        }

        mItemPrice.setText(Integer.toString(item.getPrice()) + " " + getString(R.string.label_currency));

        if (item.getImgUrl().length() > 0) {

            imageLoader.get(item.getImgUrl(), ImageLoader.getImageListener(mItemImg, R.drawable.img_loading, R.drawable.img_loading));
            mItemImg.setVisibility(View.VISIBLE);

            mItemImg.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent i = new Intent(getActivity(), PhotoViewActivity.class);
                    i.putExtra("imgUrl", item.getImgUrl());
                    getActivity().startActivity(i);
                }
            });

        } else {

            mItemImg.setVisibility(View.GONE);
        }
    }


    public void getItem() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEM_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

//                                Toast.makeText(ViewItemActivity.this, response.toString(), Toast.LENGTH_SHORT).show();

                                commentsList.clear();

                                itemId = response.getInt("itemId");

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                            item = new Item(itemObj);

                                            updateItem();
                                        }
                                    }
                                }

                                if (item.getAllowComments() == COMMENTS_ENABLED) {

                                    if (response.has("comments")) {

                                        JSONObject commentsObj = response.getJSONObject("comments");

                                        if (commentsObj.has("comments")) {

                                            JSONArray commentsArray = commentsObj.getJSONArray("comments");

                                            arrayLength = commentsArray.length();

                                            if (arrayLength > 0) {

                                                for (int i = commentsArray.length() - 1; i > -1 ; i--) {

                                                    JSONObject itemObj = (JSONObject) commentsArray.get(i);

                                                    Comment comment = new Comment(itemObj);

                                                    commentsList.add(comment);
                                                }
                                            }
                                        }
                                    }
                                }

                                loadingComplete();

                            } else {

                                showErrorScreen();
                            }

                        } catch (JSONException e) {

                            showErrorScreen();

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                showErrorScreen();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(itemId));
                params.put("language", "en");

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void send() {

        commentText = mCommentText.getText().toString();
        commentText = commentText.trim();

        if (App.getInstance().isConnected() && App.getInstance().getId() != 0 && commentText.length() > 0) {

            loading = true;

            showpDialog();

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_COMMENTS_NEW, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (!response.getBoolean("error")) {

                                    if (response.has("comment")) {

                                        JSONObject commentObj = (JSONObject) response.getJSONObject("comment");

                                        Comment comment = new Comment(commentObj);

                                        commentsList.add(comment);

                                        itemAdapter.notifyDataSetChanged();

                                        listView.setSelection(itemAdapter.getCount() - 1);

                                        mCommentText.setText("");
                                        replyToUserId = 0;
                                    }

                                    Toast.makeText(getActivity(), getString(R.string.msg_comment_has_been_added), Toast.LENGTH_SHORT).show();

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
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());

                    params.put("itemId", Long.toString(item.getId()));
                    params.put("commentText", commentText);

                    params.put("replyToUserId", Long.toString(replyToUserId));

                    return params;
                }
            };

            int socketTimeout = 0;//0 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            jsonReq.setRetryPolicy(policy);

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public void onItemDelete(final int position) {

        Api api = new Api(getActivity());

        api.itemDelete(item.getId());

        getActivity().finish();
    }

    public void onItemReport(int position, int reasonId) {

        if (App.getInstance().isConnected()) {

            Api api = new Api(getActivity());

            api.itemReport(item.getId(), reasonId);

        } else {

            Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
        }
    }

    public void onItemEdit(final int position) {

        Intent i = new Intent(getActivity(), EditItemActivity.class);
        i.putExtra("itemId", item.getId());
        i.putExtra("itemTitle", item.getTitle());
        i.putExtra("itemPrice", item.getPrice());
        i.putExtra("itemDescription", item.getContent());
        i.putExtra("itemImgUrl", item.getImgUrl());
        i.putExtra("categoryId", item.getCategoryId());
        i.putExtra("itemAllowComments", item.getAllowComments());
        startActivityForResult(i, ITEM_EDIT);
    }

    public void onItemShare(final int position) {

        Api api = new Api(getActivity());
        api.itemShare(item);
    }

    public void onItemRemove(int position) {

        android.app.FragmentManager fm = getActivity().getFragmentManager();

        ItemDeleteDialog alert = new ItemDeleteDialog();

        Bundle b = new Bundle();
        b.putInt("position", 0);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_post_delete");
    }

    public void report(int position) {

        android.app.FragmentManager fm = getActivity().getFragmentManager();

        ItemReportDialog alert = new ItemReportDialog();

        Bundle b  = new Bundle();
        b.putInt("position", position);
        b.putInt("reason", 0);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_post_report");
    }

    public void action(int position) {

        if (item.getFromUserId() == App.getInstance().getId()) {

            /** Getting the fragment manager */
            android.app.FragmentManager fm = getActivity().getFragmentManager();

            /** Instantiating the DialogFragment class */
            MyItemActionDialog alert = new MyItemActionDialog();

            /** Creating a bundle object to store the selected item's index */
            Bundle b  = new Bundle();

            /** Storing the selected item's index in the bundle object */
            b.putInt("position", position);

            /** Setting the bundle object to the dialog fragment object */
            alert.setArguments(b);

            /** Creating the dialog fragment object, which will in turn open the alert dialog window */

            alert.show(fm, "alert_my_post_action");

        } else {

            /** Getting the fragment manager */
            android.app.FragmentManager fm = getActivity().getFragmentManager();

            /** Instantiating the DialogFragment class */
            ItemActionDialog alert = new ItemActionDialog();

            /** Creating a bundle object to store the selected item's index */
            Bundle b  = new Bundle();

            /** Storing the selected item's index in the bundle object */
            b.putInt("position", position);

            /** Setting the bundle object to the dialog fragment object */
            alert.setArguments(b);

            /** Creating the dialog fragment object, which will in turn open the alert dialog window */

            alert.show(fm, "alert_post_action");
        }
    }

    public void loadingComplete() {

        itemAdapter.notifyDataSetChanged();

        if (listView.getAdapter().getCount() == 0) {

            showEmptyScreen();

        } else {

            showContentScreen();
        }

        if (mContentContainer.isRefreshing()) {

            mContentContainer.setRefreshing(false);
        }
    }

    public void showLoadingScreen() {

        preload = true;

        mContentScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);
        mEmptyScreen.setVisibility(View.GONE);

        mLoadingScreen.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showEmptyScreen() {

        mContentScreen.setVisibility(View.GONE);
        mLoadingScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mEmptyScreen.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showErrorScreen() {

        mContentScreen.setVisibility(View.GONE);
        mLoadingScreen.setVisibility(View.GONE);
        mEmptyScreen.setVisibility(View.GONE);

        mErrorScreen.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showContentScreen() {

        preload = false;

        mLoadingScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);
        mEmptyScreen.setVisibility(View.GONE);

        mContentScreen.setVisibility(View.VISIBLE);

        if (item.getAllowComments() == COMMENTS_DISABLED) {

            mCommentFormContainer.setVisibility(View.GONE);

        } else {

            mCommentFormContainer.setVisibility(View.VISIBLE);
        }

        if (App.getInstance().getId() == 0) {

            mCommentFormContainer.setVisibility(View.GONE);
        }

        loadingComplete = true;

        getActivity().invalidateOptionsMenu();
    }

    public void commentAction(int position) {

        final Comment comment = commentsList.get(position);

        if (comment.getFromUserId() != App.getInstance().getId()) {

            /** Getting the fragment manager */
            android.app.FragmentManager fm = getActivity().getFragmentManager();

            /** Instantiating the DialogFragment class */
            CommentActionDialog alert = new CommentActionDialog();

            /** Creating a bundle object to store the selected item's index */
            Bundle b  = new Bundle();

            /** Storing the selected item's index in the bundle object */
            b.putInt("position", position);

            /** Setting the bundle object to the dialog fragment object */
            alert.setArguments(b);

            /** Creating the dialog fragment object, which will in turn open the alert dialog window */

            alert.show(fm, "alert_dialog_comment_action");

        } else {

            /** Getting the fragment manager */
            android.app.FragmentManager fm = getActivity().getFragmentManager();

            /** Instantiating the DialogFragment class */
            MyCommentActionDialog alert = new MyCommentActionDialog();

            /** Creating a bundle object to store the selected item's index */
            Bundle b  = new Bundle();

            /** Storing the selected item's index in the bundle object */
            b.putInt("position", position);

            /** Setting the bundle object to the dialog fragment object */
            alert.setArguments(b);

            /** Creating the dialog fragment object, which will in turn open the alert dialog window */

            alert.show(fm, "alert_dialog_my_comment_action");
        }
    }

    public void onCommentReply(final int position) {

        if (item.getAllowComments() == COMMENTS_ENABLED) {

            final Comment comment = commentsList.get(position);

            replyToUserId = comment.getFromUserId();

            mCommentText.setText("@" + comment.getFromUserUsername() + ", ");
            mCommentText.setSelection(mCommentText.getText().length());

            mCommentText.requestFocus();

        } else {

            Toast.makeText(getActivity(), getString(R.string.msg_comments_disabled), Toast.LENGTH_SHORT).show();
        }
    }

    public void onCommentRemove(int position) {

        /** Getting the fragment manager */
        android.app.FragmentManager fm = getActivity().getFragmentManager();

        /** Instantiating the DialogFragment class */
        CommentDeleteDialog alert = new CommentDeleteDialog();

        /** Creating a bundle object to store the selected item's index */
        Bundle b  = new Bundle();

        /** Storing the selected item's index in the bundle object */
        b.putInt("position", position);

        /** Setting the bundle object to the dialog fragment object */
        alert.setArguments(b);

        /** Creating the dialog fragment object, which will in turn open the alert dialog window */

        alert.show(fm, "alert_dialog_comment_delete");
    }

    public void onCommentDelete(final int position) {

        final Comment comment = commentsList.get(position);

        commentsList.remove(position);
        itemAdapter.notifyDataSetChanged();

        Api api = new Api(getActivity());

        api.commentDelete(comment.getId());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_view_item, menu);

//        MainMenu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (loadingComplete) {

            if (App.getInstance().getId() != item.getFromUserId()) {

                menu.removeItem(R.id.action_edit);
                menu.removeItem(R.id.action_remove);
            }

            //show all menu items
            hideMenuItems(menu, true);

        } else {

            //hide all menu items
            hideMenuItems(menu, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_edit: {

                // edit item

                onItemEdit(0);

                return true;
            }

            case R.id.action_remove: {

                // remove item

                onItemRemove(0);

                return true;
            }

            case R.id.action_report: {

                // report item

                report(0);

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void hideMenuItems(Menu menu, boolean visible) {

        for (int i = 0; i < menu.size(); i++){

            menu.getItem(i).setVisible(visible);
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