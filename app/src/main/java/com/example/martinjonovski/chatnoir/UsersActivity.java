package com.example.martinjonovski.chatnoir;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;
    private String mCurrentUserId;
    private UsersAdapter mAdapter;
    private int pos;

    private MenuItem mSearchItem;
    private List<Users> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mCurrentUserId = getIntent().getStringExtra("user_id");
        mToolbar = (Toolbar) findViewById(R.id.users_page_appbar);
        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        userList = new ArrayList<>();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        FirebaseDatabase.getInstance().getReference().child("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> list = dataSnapshot.getChildren();
                        // Filter User

                        for (DataSnapshot dataSnapshot1 : list) {
                            if (!dataSnapshot1.getKey().equals(mCurrentUserId)) {
                                Users userToAdd = new Users();
                                userToAdd.setName(dataSnapshot1.child("name").getValue(String.class));
                                userToAdd.setImage_thumb(dataSnapshot1.child("image_thumb").getValue(String.class));
                                userToAdd.setStatus("Status");

                                if (userToAdd.getName() != null) {
                                    userToAdd.setUid(dataSnapshot1.getKey().toString());
                                    userList.add(userToAdd);
                                }
                            }
                        }

                        // Setting data
                        //    mBaseRecyclerAdapter.setItems(userList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        mAdapter = new UsersAdapter(userList, getApplicationContext(), true);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mUsersList.setAdapter(mAdapter);
//        searchButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (searchText.getText().toString() != null && !searchText.getText().toString().isEmpty()) {
//                    String userToSearch = searchText.getText().toString();
//                    updateUsersList(userToSearch);
//                } else {
//                    mAdapter = new UsersAdapter(userList, getApplicationContext(), true);
//                    mUsersList.setAdapter(mAdapter);
//                }
//
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//            }
//        });

    }

    private void updateUsersList(String userToSearch) {
        userToSearch = userToSearch.toLowerCase();
        List<Users> result = new ArrayList<>();
        for (Users user : userList) {
            if (user.getName().toLowerCase().contains(userToSearch)) {
                result.add(user);
            }
        }
        mAdapter = new UsersAdapter(result, getApplicationContext(), true);
        mUsersList.setAdapter(mAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        mUsersList.setAdapter(mAdapter);
        // mUsersList.getAdapter().notifyItemRemoved(pos);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView mDisplayName = (TextView) mView.findViewById(R.id.user_single_name);
            mDisplayName.setText(name);
        }

        public void setStatus(String status) {
            TextView mDisplayStatus = (TextView) mView.findViewById(R.id.user_single_status);
            mDisplayStatus.setText(status);
        }

        public void setOnlineStatus(boolean bool) {
            ImageView imageView = (ImageView) mView.findViewById(R.id.single_online_img);
            Bitmap icon = BitmapFactory.decodeResource(mView.getContext().getResources(),
                    R.drawable.online_bg);

            if (bool) {

                imageView.setImageBitmap(icon);

            } else {
                Bitmap iconOff = BitmapFactory.decodeResource(mView.getContext().getResources(),
                        R.drawable.offline_icon);
                imageView.setImageBitmap(iconOff);

            }
        }

        public void setThumb(String thumb, Context context) {
            CircleImageView circleImage = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(context).load(thumb).placeholder(R.drawable.photo).into(circleImage);
        }

    }

    public void animateSearchToolbar(int numberOfMenuIcon, boolean containsOverflow, boolean show) {

        mToolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));

        if (show) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int width = mToolbar.getWidth() -
                        (containsOverflow ? getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(mToolbar,
                        isRtl(getResources()) ? mToolbar.getWidth() - width : width, mToolbar.getHeight() / 2, 0.0f, (float) width);
                createCircularReveal.setDuration(250);
                createCircularReveal.start();
            } else {
                TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, (float) (-mToolbar.getHeight()), 0.0f);
                translateAnimation.setDuration(220);
                mToolbar.clearAnimation();
                mToolbar.startAnimation(translateAnimation);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int width = mToolbar.getWidth() -
                        (containsOverflow ? getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(mToolbar,
                        isRtl(getResources()) ? mToolbar.getWidth() - width : width, mToolbar.getHeight() / 2, (float) width, 0.0f);
                createCircularReveal.setDuration(250);
                createCircularReveal.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ColorDrawable buttonColor = (ColorDrawable) mToolbar.getBackground();
                        mToolbar.setBackgroundColor(buttonColor.getColor());
                        super.onAnimationEnd(animation);
                    }
                });
                createCircularReveal.start();
            } else {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                Animation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-mToolbar.getHeight()));
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(alphaAnimation);
                animationSet.addAnimation(translateAnimation);
                animationSet.setDuration(220);
                animationSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ColorDrawable buttonColor = (ColorDrawable) mToolbar.getBackground();
                        mToolbar.setBackgroundColor(getThemeColor(UsersActivity.this, buttonColor.getColor()));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mToolbar.startAnimation(animationSet);
            }
        }
    }

    private boolean isRtl(Resources resources) {
        return resources.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private static int getThemeColor(Context context, int id) {
        Resources.Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(new int[]{id});
        int result = a.getColor(0, 0);
        a.recycle();
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        mSearchItem = menu.findItem(R.id.m_search);

        MenuItemCompat.setOnActionExpandListener(mSearchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Called when SearchView is collapsing
                if (mSearchItem.isActionViewExpanded()) {
                    animateSearchToolbar(1, false, false);
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Called when SearchView is expanding
                animateSearchToolbar(1, true, true);
                return true;
            }
        });

        return true;
    }

}
