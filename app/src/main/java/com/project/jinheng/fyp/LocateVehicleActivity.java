package com.project.jinheng.fyp;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.jinheng.fyp.BaseActivity;

/**
 * Created by JinHeng on 1/5/2015.
 */
public class LocateVehicleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment fragment = LocateVehicleFragment.newInstance(R.layout.fragment_locate_vehicle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    public static class LocateVehicleFragment extends Fragment {

        public static LocateVehicleFragment newInstance(int layout) {
            LocateVehicleFragment classInstance = new LocateVehicleFragment();
            Bundle args = new Bundle();
            args.putInt("layout", layout);
            classInstance.setArguments(args);
            return classInstance;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(getArguments().getInt("layout"), container, false);
            return view;
        }
    }

}
