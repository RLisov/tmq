package com.tamaq.courier.controllers.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.tamaq.courier.R;


public class ExitDialog extends AppCompatDialogFragment {

    private View mRootView;
    private Listener mListener;

    public ExitDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(AppCompatDialogFragment.STYLE_NO_TITLE, R.style.DialogFragmentTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.dialog_exit_user, container, false);

        Button negativeButton = (Button) mRootView.findViewById(R.id.negativeButton);
        Button positiveButton = (Button) mRootView.findViewById(R.id.positiveButton);

        negativeButton.setOnClickListener(view -> getDialog().dismiss());
        positiveButton.setOnClickListener(view -> {
            mListener.onExit();
            getDialog().dismiss();
        });

        return mRootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int width = ViewGroup.LayoutParams.WRAP_CONTENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onExit();
    }

}
