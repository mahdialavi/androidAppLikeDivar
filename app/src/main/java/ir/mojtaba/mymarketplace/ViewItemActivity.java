package ir.mojtaba.mymarketplace;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import ir.mojtaba.mymarketplace.common.ActivityBase;
import ir.mojtaba.mymarketplace.dialogs.CommentActionDialog;
import ir.mojtaba.mymarketplace.dialogs.CommentDeleteDialog;
import ir.mojtaba.mymarketplace.dialogs.ItemActionDialog;
import ir.mojtaba.mymarketplace.dialogs.ItemDeleteDialog;
import ir.mojtaba.mymarketplace.dialogs.ItemReportDialog;
import ir.mojtaba.mymarketplace.dialogs.MyCommentActionDialog;
import ir.mojtaba.mymarketplace.dialogs.MyItemActionDialog;


public class ViewItemActivity extends ActivityBase implements CommentDeleteDialog.AlertPositiveListener, ItemDeleteDialog.AlertPositiveListener, ItemReportDialog.AlertPositiveListener, MyItemActionDialog.AlertPositiveListener, ItemActionDialog.AlertPositiveListener, CommentActionDialog.AlertPositiveListener, MyCommentActionDialog.AlertPositiveListener {

    Toolbar mToolbar;

    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_item);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState != null) {

            fragment = getSupportFragmentManager().getFragment(savedInstanceState, "currentFragment");

        } else {

            fragment = new ViewItemFragment();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {

        super.onSaveInstanceState(outState);

        getSupportFragmentManager().putFragment(outState, "currentFragment", fragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {

        ViewItemFragment p = (ViewItemFragment) fragment;
        p.hideEmojiKeyboard();

        super.onPause();
    }

    @Override
    public void onItemDelete(int position) {

        ViewItemFragment p = (ViewItemFragment) fragment;
        p.onItemDelete(position);
    }

    @Override
    public void onItemReport(int position, int reasonId) {

        ViewItemFragment p = (ViewItemFragment) fragment;
        p.onItemReport(position, reasonId);
    }

    @Override
    public void onItemRemove(int position) {

        ViewItemFragment p = (ViewItemFragment) fragment;
        p.onItemRemove(position);
    }

    @Override
    public void onItemEdit(int position) {

        ViewItemFragment p = (ViewItemFragment) fragment;
        p.onItemEdit(position);
    }

    @Override
    public void onItemShare(int position) {

        ViewItemFragment p = (ViewItemFragment) fragment;
        p.onItemShare(position);
    }

    @Override
    public void onItemReportDialog(int position) {

        ViewItemFragment p = (ViewItemFragment) fragment;
        p.report(position);
    }

    @Override
    public void onCommentRemove(int position) {

        ViewItemFragment p = (ViewItemFragment) fragment;
        p.onCommentRemove(position);
    }

    @Override
    public void onCommentDelete(int position) {

        ViewItemFragment p = (ViewItemFragment) fragment;
        p.onCommentDelete(position);
    }

    @Override
    public void onCommentReply(int position) {

        ViewItemFragment p = (ViewItemFragment) fragment;
        p.onCommentReply(position);
    }

    @Override
    public void onBackPressed(){

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {

            case android.R.id.home: {

                finish();
                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }
}
