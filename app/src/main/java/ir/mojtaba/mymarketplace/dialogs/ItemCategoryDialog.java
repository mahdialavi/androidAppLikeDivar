package ir.mojtaba.mymarketplace.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import java.util.ArrayList;

import ir.mojtaba.mymarketplace.R;
import ir.mojtaba.mymarketplace.app.App;
import ir.mojtaba.mymarketplace.constants.Constants;

public class ItemCategoryDialog extends DialogFragment implements Constants {

    private int categoryId = 0;

    /** Declaring the interface, to invoke a callback function in the implementing activity class */
    AlertPositiveListener alertPositiveListener;

    /** An interface to be implemented in the hosting activity for "OK" button click listener */
    public interface AlertPositiveListener {

        public void onItemCategory(int categoryId);
    }

    /** This is a callback method executed when this fragment is attached to an activity.
     *  This function ensures that, the hosting activity implements the interface AlertPositiveListener
     * */
    public void onAttach(android.app.Activity activity) {

        super.onAttach(activity);

        try {

            alertPositiveListener = (AlertPositiveListener) activity;

        } catch(ClassCastException e){

            // The hosting activity does not implemented the interface AlertPositiveListener
            throw new ClassCastException(activity.toString() + " must implement AlertPositiveListener");
        }
    }

    /** This is the OK button listener for the alert dialog,
     *  which in turn invokes the method onPositiveClick(position)
     *  of the hosting activity which is supposed to implement it
     */
    OnClickListener positiveListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            AlertDialog alert = (AlertDialog)dialog;
            int itemCategory = alert.getListView().getCheckedItemPosition();

            alertPositiveListener.onItemCategory(itemCategory);
        }
    };

    /** This is a callback method which will be executed
     *  on creating this fragment
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        ArrayList<String> categories;

        categories = App.getInstance().getCategoriesList();

        int nCount = categories.size();

        String[] item_categories = new String[nCount];

        for (int i = 0; i < nCount; i++) {

            item_categories[i] = categories.get(i);

        };

        /** Getting the arguments passed to this fragment */
        Bundle bundle = getArguments();

        categoryId = bundle.getInt("categoryId", 0);

        /** Creating a builder for the alert dialog window */
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

        /** Setting a title for the window */
        b.setTitle(getText(R.string.label_post_to_dialog_title));

        /** Setting items to the alert dialog */
        b.setSingleChoiceItems(item_categories, categoryId, null);

        /** Setting a positive button and its listener */
        b.setPositiveButton(getText(R.string.action_ok), positiveListener);

        /** Setting a positive button and its listener */
        b.setNegativeButton(getText(R.string.action_cancel), null);

        /** Creating the alert dialog window using the builder class */
        AlertDialog d = b.create();

        /** Return the alert dialog window */
        return d;
    }
}