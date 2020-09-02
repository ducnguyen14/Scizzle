package com.example.instaclone.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.instaclone.Profile.ProfileActivity;
import com.example.instaclone.R;
import com.example.instaclone.Utils.BottomNavigationViewHelper;
import com.example.instaclone.Utils.UserListAdapter;
import com.example.instaclone.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity/DEBUG";
    private static final int ACTIVITY_NUM = 1;

    private Context mContext = SearchActivity.this;

    // Notes: Widgets
    private EditText mSearchParam;
    private ListView mListView;

    // Notes: Variables
    private List<User> mUserList;
    private UserListAdapter mAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchParam = (EditText) findViewById(R.id.search);
        mListView = (ListView) findViewById(R.id.listView);


        Log.d(TAG, "onCreate: started");

        // Notes: Set ups
        hideSoftKeyboard();
        setupBottomNavigationView();
        initTextListener();
    }


    private void initTextListener()
    {
        Log.d(TAG, "\tinitTextListener: initializing");

        mUserList = new ArrayList<>();

        mSearchParam.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                // Notes: TODO - Having the search to all lower wont be able to search for Case Sensitive username. Fix if have time
//                String text = mSearchParam.getText().toString().toLowerCase(Locale.getDefault());
                String text = mSearchParam.getText().toString();
                searchForMatch(text);
            }
        });


    }


    private void searchForMatch(String keyword)
    {
        Log.d(TAG, "\tsearchForMatch: searching for a match: " + keyword);

        mUserList.clear();

        // Notes: Update the users listview
        if(keyword.length() == 0)
        {

        }
        else
        {
            // Notes: TODO - Can we optimize this search query???
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_username))
                    .equalTo(keyword);

            query.addListenerForSingleValueEvent(new ValueEventListener()
            {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    // Notes: A match is found
                    for(DataSnapshot singleSnapshot: snapshot.getChildren())
                    {
                        Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(User.class).toString());

                        mUserList.add(singleSnapshot.getValue(User.class));

                        // Notes: Update the users list view
                        updateUsersList();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {

                }
            });

        }

    }

    private void updateUsersList()
    {
        Log.d(TAG, "updateUsersList: updating users list");


        mAdapter = new UserListAdapter(SearchActivity.this, R.layout.layout_user_listitem, mUserList);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(TAG, "onItemClick: selected user: " + mUserList.get(position).toString());

                // Notes: Navigate to profile activity (2 cases)
                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);

                // Notes: This lets ProfileActivity.java know this intent is from SearchActivity.java
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.search_activity));

                /*
                    Notes: Case 1 - Navigating to your own profile.
                           Case 2 - Navigating to a another user's profile
                           Solution - Pass which user is in the intent.
                 */


                intent.putExtra(getString(R.string.intent_user), mUserList.get(position));

                startActivity(intent);

            }
        });

    }



    private void hideSoftKeyboard()
    {
        if(getCurrentFocus() != null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    /**
     * Notes: BottomNavigationView setup
     */
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "\tsetupBottomNavigationView: setting up BottomNavigationView");

        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);

        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);


        // Notes: Highlighting the correct Icon when navigating
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);


    }

}
