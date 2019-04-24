package com.tamaq.courier.utils;

import android.app.Activity;
import android.app.SearchManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.MenuItem;

import io.reactivex.functions.Consumer;

import static android.content.Context.SEARCH_SERVICE;

public class SearchMenuHelper {

    private final MenuItem mMenuItem;
    private Consumer<String> mTextChangeListener;
    private Consumer<Boolean> mActionExpandListener;

    public SearchMenuHelper(Activity activity, MenuItem menuItem, String searchHint) {
        mMenuItem = menuItem;

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        SearchManager searchManager = (SearchManager) activity.getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setQueryHint(searchHint);

        MenuItemCompat.setOnActionExpandListener(mMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                try {
                    if (mActionExpandListener != null) mActionExpandListener.accept(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                try {
                    if (mActionExpandListener != null) mActionExpandListener.accept(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    if (mTextChangeListener != null) mTextChangeListener.accept(newText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    public void setTextChangeListener(Consumer<String> textChangeListener) {
        mTextChangeListener = textChangeListener;
    }

    public void setOnActionExpandListener(Consumer<Boolean> actionExpandListener) {
        mActionExpandListener = actionExpandListener;
    }

    public void showIcon(boolean needShow) {
        if (mMenuItem != null) mMenuItem.setVisible(needShow);
    }

    public void enableIcon(boolean needEnable) {
        if (mMenuItem != null) {
            mMenuItem.getIcon().setAlpha(needEnable ? 255 : 127);
            mMenuItem.setEnabled(needEnable);
        }
    }
}
