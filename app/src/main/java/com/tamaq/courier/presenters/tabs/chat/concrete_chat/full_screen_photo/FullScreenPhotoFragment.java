package com.tamaq.courier.presenters.tabs.chat.concrete_chat.full_screen_photo;


import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tamaq.courier.R;
import com.tamaq.courier.presenters.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FullScreenPhotoFragment extends BaseFragment {

    private static final String IMAGE_BITMAP = "image_bitmap";
    private static final String TRANSITION_NAME = "transition_name";

    @BindView(R.id.image)
    ImageView image;

    public FullScreenPhotoFragment() {
        // Required empty public constructor
    }

    public static FullScreenPhotoFragment newInstance(Bitmap bitmap, String transitionName) {
        FullScreenPhotoFragment fragment = new FullScreenPhotoFragment();
        Bundle args = new Bundle();
        args.putParcelable(IMAGE_BITMAP, bitmap);
        args.putString(TRANSITION_NAME, transitionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(TransitionInflater
                    .from(getContext())
                    .inflateTransition(android.R.transition.move));
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String transitionName = getArguments().getString(TRANSITION_NAME);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            image.setTransitionName(transitionName);
        }

        Bitmap bitmap = getArguments().getParcelable(IMAGE_BITMAP);
        image.setImageBitmap(bitmap);
        startPostponedEnterTransition();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_full_screen_photo, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        initializeNavigationBar();
        setChangeToolbarColor(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
        getSupportActionBar().setTitle("");

//        Bitmap bitmap = getArguments().getParcelable(IMAGE_BITMAP);
//
//        image.setImageBitmap(bitmap);

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStackImmediate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
