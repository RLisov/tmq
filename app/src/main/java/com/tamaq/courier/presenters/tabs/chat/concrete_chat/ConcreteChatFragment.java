package com.tamaq.courier.presenters.tabs.chat.concrete_chat;


import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cocosw.bottomsheet.BottomSheet;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.tamaq.courier.R;
import com.tamaq.courier.controllers.adapters.ConcreteChatRecyclerAdapter;
import com.tamaq.courier.controllers.service.MyFirebaseMessagingService;
import com.tamaq.courier.dao.DialogDAO;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.MessageRealm;
import com.tamaq.courier.presenters.activities.ConcreteChatActivity;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.tabs.chat.concrete_chat.full_screen_photo.FullScreenPhotoFragment;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.HelperCommon;
import com.tamaq.courier.utils.PhotoHelper;
import com.tamaq.courier.utils.PrefsHelper;
import com.tamaq.courier.widgets.SlideUpAnimator;
import com.tamaq.courier.widgets.recycler_decorations.SpaceItemDecorationFirstLast;
import com.tamaq.courier.widgets.swipeToRefreshBottom.SwipeRefreshLayoutBottom;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class ConcreteChatFragment extends BaseFragment implements ConcreteChatContract.View {

    private static final String CHAT_ID = "chat_id";
    private static final String USER_NAME = "user_name";
    private static final String ORDER_STATUS = "order_status";
    private static final String NEW_MESSAGE = "new_message";

    @Inject
    ConcreteChatContract.Presenter presenter;

    @BindView(R.id.userAvatarImage)
    CircleImageView userAvatarImage;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.emptyStateLayout)
    View emptyStateLayout;
    @BindView(R.id.sendMessageContainer)
    View sendMessageContainer;
    @BindView(R.id.messageText)
    EditText messageText;
    @BindView(R.id.sendMessageButton)
    View sendMessageButton;
    @BindView(R.id.cameraImageView)
    View cameraImageView;

    @BindView(R.id.swipeRefreshLayoutBottom)
    SwipeRefreshLayoutBottom swipeRefreshLayoutBottom;

    private ConcreteChatRecyclerAdapter mAdapter;
    private boolean mMessagePending;

    private LinearLayoutManager mLinearLayoutManager;

    private boolean isFragmentActive = true;

    public ConcreteChatFragment() {
        // Required empty public constructor
    }

    public static ConcreteChatFragment newInstance(String chatId, String userName, boolean orderCompleted) {
        ConcreteChatFragment fragment = new ConcreteChatFragment();
        Bundle args = new Bundle();
        args.putString(CHAT_ID, chatId);
        args.putString(USER_NAME, userName);
        args.putBoolean(ORDER_STATUS, orderCompleted);
        fragment.setArguments(args);
        return fragment;
    }

    public static ConcreteChatFragment newInstance(String chatId, boolean orderCompleted) {
        ConcreteChatFragment fragment = new ConcreteChatFragment();
        Bundle args = new Bundle();
        args.putString(CHAT_ID, chatId);
        args.putBoolean(ORDER_STATUS, orderCompleted);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_concrete_chat, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);

        sendMessageContainer.setVisibility(getArguments().getBoolean(ORDER_STATUS)
                ? View.GONE
                : View.VISIBLE);

        initToolbar();
        initListeners();
        initRecycler();

        String chatId = getArguments().getString(CHAT_ID);
        presenter.loadMessages(chatId);

        MyFirebaseMessagingService.resetChatFields();

        return rootView;
    }

    private void initToolbar() {
        initializeNavigationBar();
        setChangeToolbarColor(false);
        getSupportActionBar().setTitle(getArguments().getString(USER_NAME));
        getSupportActionBar().setSubtitle(getArguments().getBoolean(ORDER_STATUS)
                ? getString(R.string.customer_order_completed)
                : getString(R.string.customer));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
    }

    private void initListeners() {
        RxTextView.textChanges(messageText).subscribe(charSequence -> {
            if (!mMessagePending) {
                if (charSequence.length() <= 0) {
                    ((ConcreteChatActivity) getActivity()).setNeedHandleDispatchTouchEvent(true);
                    sendMessageButton.setAlpha(0.50f);
                    sendMessageButton.setClickable(false);
                } else {
                    ((ConcreteChatActivity) getActivity()).setNeedHandleDispatchTouchEvent(false);
                    sendMessageButton.setAlpha(1f);
                    sendMessageButton.setClickable(true);
                }
            }
        });

        RxView.clicks(sendMessageButton).subscribe(aVoid -> {
            if (HelperCommon.isNetworkConnected(getContext())) {
                mMessagePending = true;
                presenter.sendMessage(messageText.getText().toString(), mAdapter.getItemCount());
                messageText.setText("");
                sendMessageButton.setClickable(false);
                sendMessageButton.setAlpha(0.50f);
                ((ConcreteChatActivity) getActivity()).setNeedHandleDispatchTouchEvent(true);
            } else handleInternetDisabled();
        });
        sendMessageButton.setClickable(false);

        RxView.clicks(cameraImageView).subscribe(aVoid ->
                new BottomSheet.Builder(getActivity())
                        .sheet(R.menu.camer_menu_new)
                        .listener((dialog, which) -> {
                            switch (which) {
                                case R.id.takePhoto:
                                    takePhoto();
                                    break;

                                case R.id.choosePhoto:
                                    chooseGalleryPhoto();
                                    break;
                            }
                        }).show());

        DialogDAO.getInstance().setMessageUpdatedActions(() -> {
            if (isFragmentActive) presenter.updateMessages();
        });
    }

    private void initRecycler() {
        mAdapter = new ConcreteChatRecyclerAdapter(getContext());

        swipeRefreshLayoutBottom.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayoutBottom.setOnRefreshListener(() -> {
            presenter.updateMessages();
            new Handler().postDelayed(() -> {
                if (getContext() != null) swipeRefreshLayoutBottom.setRefreshing(false);
            }, 10000);
        });

        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLinearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setItemAnimator(new SlideUpAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(20);

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);

            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        });
        mAdapter.setOnPhotoClickAction(this::openPhotoWithAnimation);
        mAdapter.setOnMessageClick(this::showBottomSheetForUnsuccessful);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new SpaceItemDecorationFirstLast(HelperCommon.dpToPx(16)));
    }

    private void takePhoto() {
        RxPermissions rxPermissions = new RxPermissions(getActivity());
        if (rxPermissions.isGranted(Manifest.permission.CAMERA)
                && rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && rxPermissions.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            startActivityForResult(PhotoHelper.prepareTakePhotoIntent(getContext()), PhotoHelper.CAMERA_KEY);
        } else {
            rxPermissions.request(android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe(isGranted -> {
                        if (isGranted) {
                            startActivityForResult(PhotoHelper.prepareTakePhotoIntent(getContext()), PhotoHelper.CAMERA_KEY);
                        }
                    });
        }
    }

    private void chooseGalleryPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        RxPermissions rxPermissions = new RxPermissions(getActivity());
        if (rxPermissions.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            startActivityForResult(intent, PhotoHelper.GALLERY_KEY);
        } else {
            rxPermissions.request(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe(isGranted -> {
                        if (isGranted) startActivityForResult(intent, PhotoHelper.GALLERY_KEY);
                    });
        }
    }


    @Override
    public void onInjectDependencies(AppComponent appComponent) {
        super.onInjectDependencies(appComponent);
        DaggerCommonComponent.builder()
                .appComponent(TamaqApp.get(getContext()).getAppComponent())
                .commonModule(new CommonModule())
                .build().inject(this);
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }

    @Override
    public void onStart() {
        isFragmentActive = true;
        PrefsHelper.saveChatScreenIsActive(getContext(), true);
        super.onStart();
    }

    @Override
    public void onPause() {
        isFragmentActive = false;
        PrefsHelper.saveChatScreenIsActive(getContext(), false);
        super.onPause();
    }

    private void showBottomSheetForUnsuccessful(String messageId, int position) {
        new BottomSheet.Builder(getActivity())
                .sheet(R.menu.resend_or_delete_menu)
                .listener((dialog, which) -> {
                    if (which == R.id.resend) presenter.resendMessage(messageId, position);
                    else presenter.removeMessage(messageId, position);
                }).show();
    }

    private void openPhotoWithAnimation(Bitmap bitmapImage, ImageView sharedImageView) {
        String transitionName = ViewCompat.getTransitionName(sharedImageView);

        FullScreenPhotoFragment fullScreenPhotoFragment = FullScreenPhotoFragment.newInstance(bitmapImage, transitionName);

        ConcreteChatFragment.this.getFragmentManager()
                .beginTransaction()
                .addSharedElement(sharedImageView, transitionName)
                .addToBackStack(fullScreenPhotoFragment.getClass().getSimpleName())
                .replace(R.id.containerFrame, fullScreenPhotoFragment, fullScreenPhotoFragment.getClass().getSimpleName())
                .commit();
    }

    @Override
    public void messageUnsent(int position) {
        mAdapter.updateMessageUnsent(position);
    }

    @Override
    public void displayMessages(String avatarUrl, String name, List<MessageRealm> messages) {
        Glide.with(getContext())
                .load(avatarUrl)
                .placeholder(R.drawable.user_pik_80)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .animate(android.R.anim.fade_in)
                .into(userAvatarImage);

        getSupportActionBar().setTitle(name);

        recyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        mAdapter.setObjects(messages);

        swipeRefreshLayoutBottom.setRefreshing(false);

        if (checkNotNull(PhotoHelper.getCurrentPhotoPath())) {
            presenter.sendPhoto(PhotoHelper.getCurrentPhotoPath(), mAdapter.getItemCount());
        }
    }

    @Override
    public void messageSent(MessageRealm messageRealm) {
        mAdapter.addMessage(messageRealm);
        recyclerView.invalidateItemDecorations();
        PhotoHelper.clearCurrentPhotoPath();

        mMessagePending = false;

        if (!messageText.getText().toString().isEmpty() && !sendMessageButton.isClickable()) {
            sendMessageButton.setClickable(true);
            sendMessageButton.setAlpha(1f);
        }
    }

    @Override
    public void displayEmptyChat() {
        swipeRefreshLayoutBottom.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void removeMessage(int position) {
        mAdapter.removeMessage(position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && (requestCode == PhotoHelper.CAMERA_KEY || requestCode == PhotoHelper.GALLERY_KEY)) {
            String picturePath = requestCode == PhotoHelper.CAMERA_KEY
                    ? PhotoHelper.getCurrentPhotoPath()
                    : PhotoHelper.getGalleryPicturePath(getContext(), data);

            presenter.sendPhoto(picturePath, mAdapter.getItemCount());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
