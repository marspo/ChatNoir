package com.example.martinjonovski.chatnoir;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;

/**
 * Created by Martin Jonovski on 10/23/2017.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private Drawable myDrawable;
    private Context context;

    public SectionsPagerAdapter(FragmentManager fm, Context ctx) {
        super(fm);
        this.context = ctx;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
//
//
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "REQUESTS";
            case 1:
                return "CHATS";
            case 2:
                return "FRIENDS";
            default:
                return null;

        }
    }


}
