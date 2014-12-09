package com.project.jinheng.fyp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;

/**
 * Created by JinHeng on 12/2/2014.
 */
public class ForgotPasswordActivity extends Activity {

    private ImageButton imageButton;
    private RelativeLayout fpasswordContainer;
    private EditText fpasswordEmail;
    private ActionProcessButton resetPasswordButton;
    private Button resetPasswordButtonFake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        imageButton = (ImageButton) findViewById(R.id.dropdown_arrow_fpassword);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.top_to_bottom_in);
            }
        });

        /**
         * Define when will the register button be enabled
         */
        TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    checkInputAndEnableReset();
                    return true;
                }
                return false;
            }
        };
        fpasswordContainer = (RelativeLayout) findViewById(R.id.fpassword_container);

        fpasswordEmail = (EditText) findViewById(R.id.forgot_pass_edit_text);
        fpasswordEmail.setOnEditorActionListener(editorActionListener);

        resetPasswordButton = (ActionProcessButton) findViewById(R.id.button_reset_pass);
        resetPasswordButtonFake = (Button) findViewById(R.id.button_fpassword_disabled);

        fpasswordContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                checkInputAndEnableReset();
            }
        });

    }

    public void checkInputAndEnableReset() {
        if (!fpasswordEmail.getText().toString().equals("")) {
            resetPasswordButton.setEnabled(true);
            resetPasswordButton.setAlpha(1.0f);
            resetPasswordButtonFake.setAlpha(0.0f);
            resetPasswordButtonFake.setVisibility(View.INVISIBLE);

        } else {
            resetPasswordButton.setEnabled(false);
            resetPasswordButton.setAlpha(0.0f);
            resetPasswordButtonFake.setAlpha(0.5f);
            resetPasswordButtonFake.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.top_to_bottom_in);
    }

    public void forgotPasswordButtonClicked(View view) {

    }

}
