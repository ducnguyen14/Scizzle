package com.example.instaclone.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.instaclone.R;

public class ConfirmPasswordDialog extends DialogFragment {

    private static final String TAG = "ConfirmPassDialog/DEBU";


    public interface OnConfirmPasswordListener
    {
        // Notes: Will be implemented in EditProfileFragment.java
        public void onConfirmPassword(String password);
    }
    OnConfirmPasswordListener mOnConfirmPasswordListener;


    // Notes: Variables
    TextView mPassword;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container, false);
        Log.d(TAG, "onCreateView: started.");

        mPassword = (TextView) view.findViewById(R.id.confirm_password);

        TextView confirmDialog = (TextView) view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: captured password and confirming.");

                String password = mPassword.getText().toString();

                // Notes: Password does not equal null
                if(!password.equals(""))
                {
                    mOnConfirmPasswordListener.onConfirmPassword(password);
                    getDialog().dismiss();
                }
                else
                {
                    Toast.makeText(getActivity(), "you must enter a password", Toast.LENGTH_SHORT).show();
                }

            }
        });


        TextView cancelDialog = (TextView) view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: closing the dialog");
                getDialog().dismiss();
            }
        });






        return view;
    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try
        {
            mOnConfirmPasswordListener = (OnConfirmPasswordListener) getTargetFragment();
        }
        catch (ClassCastException e)
        {
            Log.e(TAG, "\tonAttach: ClassCastException: " + e.getMessage() );
        }
    }





}
