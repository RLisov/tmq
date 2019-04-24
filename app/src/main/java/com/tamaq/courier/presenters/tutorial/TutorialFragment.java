package com.tamaq.courier.presenters.tutorial;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jakewharton.rxbinding2.view.RxView;
import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.login.LoginFragment;
import com.tamaq.courier.presenters.registration.RegistrationFragment;
import com.tamaq.courier.presenters.welcome.WelcomeFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TutorialFragment extends BaseFragment implements TutorialContract.View {

    @BindView(R.id.btnSkip)
    View btnSkip;

    @BindView(R.id.firstDot)
    ImageView firstDot;
    @BindView(R.id.secondDot)
    ImageView secondDot;
    @BindView(R.id.thirdDot)
    ImageView thirdDot;
    @BindView(R.id.fourthDot)
    ImageView fourthDot;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @Inject
    TutorialContract.Presenter presenter;
    private TutorialPageAdapter mViewPagerAdapter;
    private int[] mSlidesLayouts;
    private ImageView mSelectedSlideDot;

    public static TutorialFragment newInstance() {
        return new TutorialFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null)
            return rootView;
        presenter.attachPresenter(this);
        rootView = inflater.inflate(R.layout.fragment_tutorial, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        initSlides();

        return rootView;
    }

    private void initSlides() {
        mSelectedSlideDot = firstDot;

        RxView.clicks(btnSkip).subscribe(aVoid ->
                replaceFragment(WelcomeFragment.newInstance(), false));

        mSlidesLayouts = new int[]{
                R.layout.tutorial_slide_first,
                R.layout.tutorial_slide_second,
                R.layout.tutorial_slide_third,
                R.layout.tutorial_slide_fourth
        };

        mViewPagerAdapter = new TutorialPageAdapter();
        viewPager.setAdapter(mViewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                changeDotColor(position);

                btnSkip.setVisibility(position == mSlidesLayouts.length - 1
                        ? View.INVISIBLE
                        : View.VISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    private void changeDotColor(int position) {
        if (mSelectedSlideDot != null)
            mSelectedSlideDot.setImageDrawable(ContextCompat.getDrawable(getContext(),
                    R.drawable.oval_724));

        switch (position) {
            case 0:
                mSelectedSlideDot = firstDot;
                break;

            case 1:
                mSelectedSlideDot = secondDot;
                break;

            case 2:
                mSelectedSlideDot = thirdDot;
                break;

            case 3:
                mSelectedSlideDot = fourthDot;
                break;
        }

        if (mSelectedSlideDot != null)
            mSelectedSlideDot.setImageDrawable(ContextCompat.getDrawable(getContext(),
                    R.drawable.combined_shape));
    }

    @Override
    public void onInjectDependencies(AppComponent appComponent) {
        super.onInjectDependencies(appComponent);
        DaggerCommonComponent.builder()
                .appComponent(appComponent)
                .commonModule(new CommonModule()).build().inject(this);
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }

    private class TutorialPageAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        private TutorialPageAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater)
                    getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(mSlidesLayouts[position], container, false);

            if (position == 3) {
                RxView.clicks(view.findViewById(R.id.tutorialEnter)).subscribe(aVoid ->
                        replaceFragment(LoginFragment.newInstance(), false));
                RxView.clicks(view.findViewById(R.id.tutorialRegistration)).subscribe(aVoid ->
                        replaceFragment(RegistrationFragment.newInstance(), false));
            }

            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return mSlidesLayouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
