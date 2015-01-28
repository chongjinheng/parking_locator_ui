package com.project.jinheng.fyp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dd.processbutton.iml.ActionProcessButton;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.JSONDTO;
import com.project.jinheng.fyp.classes.JSONError;
import com.project.jinheng.fyp.classes.MyException;

/**
 * Created by JinHeng on 1/28/2015.
 */
public class RequestEdit extends BaseActivity {

    public static final String TAG = "RequestEdit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BaseActivity.needSearch = false;
        super.onCreate(savedInstanceState);

        Fragment fragment = RequestEditFragment.newInstance(R.layout.fragment_requestedit);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        BaseActivity.needSearch = true;
        overridePendingTransition(R.anim.fade_in, R.anim.top_to_bottom_in);
    }

    public static class RequestEditFragment extends Fragment {

        private RelativeLayout requestEditContainer;
        private ActionProcessButton sendButton;
        private ProgressDialog progressDialog;
        private EditText feedbackContent;

        public static RequestEditFragment newInstance(int layout) {
            RequestEditFragment classInstance = new RequestEditFragment();
            Bundle args = new Bundle();
            args.putInt("layout", layout);
            classInstance.setArguments(args);
            return classInstance;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(getArguments().getInt("layout"), container, false);

            requestEditContainer = (RelativeLayout) view.findViewById(R.id.wronginfo_container);
            requestEditContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            });

            feedbackContent = (EditText) view.findViewById(R.id.wronginfo_content);
            sendButton = (ActionProcessButton) view.findViewById(R.id.button_send_edit);
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AsyncTask<JSONDTO, Void, Void> sendFeedback = new AsyncTask<JSONDTO, Void, Void>() {

                        @Override
                        protected void onPreExecute() {
                            progressDialog = MyProgressDialog.initiate(getActivity());
                            progressDialog.show();
                        }

                        @Override
                        protected Void doInBackground(JSONDTO... params) {
                            try {
                                APIUtils.processAPICalls(params[0]);
                            } catch (MyException e) {
                                Log.e(TAG, e.getMessage());
                                JSONDTO returnDTO = new JSONDTO();
                                JSONError error = new JSONError(e.getError(), e.getMessage());
                                returnDTO.setError(error);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, "Exception occurred when calling API");
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            AlertDialog sentDialog = new AlertDialog.Builder(getActivity()).create();
                            sentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            sentDialog.setInverseBackgroundForced(true);
                            sentDialog.setMessage("Your feedback is recorded. \nThank you for you help.");
                            sentDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finish();
                                }
                            });
                            sentDialog.show();
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                        }

                    };

                    String feedback = feedbackContent.getText().toString();
                    JSONDTO dataToProcess = new JSONDTO();
                    dataToProcess.setServiceName(APIUtils.SEND_FEEDBACK);
                    dataToProcess.setFeedback(feedback);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        sendFeedback.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataToProcess);
                    } else {
                        sendFeedback.execute(dataToProcess);
                    }
                }
            });

            return view;
        }
    }
}
