package com.project.jinheng.fyp;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.Lot;

/**
 * Created by JinHeng on 1/5/2015.
 */
public class LotDetailActivity extends BaseActivity {

    private static Lot lot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String json = getIntent().getStringExtra("details");
        lot = (Lot) APIUtils.fromJSON(json, Lot.class);

        Fragment fragment = LotDetailFragment.newInstance(R.layout.fragment_lotdetails);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.top_to_bottom_in);
    }

    public static class LotDetailFragment extends Fragment {

        private static final String TAG = "LotDetailFragment";

        private GoogleMap map;

        public static LotDetailFragment newInstance(int layout) {
            LotDetailFragment classInstance = new LotDetailFragment();
            Bundle args = new Bundle();
            args.putInt("layout", layout);
            classInstance.setArguments(args);
            return classInstance;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(getArguments().getInt("layout"), container, false);

//            if (map == null) {
//                map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
//                if (map == null) {
//                    Log.d(TAG, "Map not loaded");
//                }
//            }

            TextView name = (TextView) view.findViewById(R.id.detail_lot_name);
            TextView type = (TextView) view.findViewById(R.id.detail_lot_type);
            TextView address = (TextView) view.findViewById(R.id.detail_lot_address);
            TextView operationHour = (TextView) view.findViewById(R.id.detail_lot_operation_hour);
            TextView capacity = (TextView) view.findViewById(R.id.detail_lot_capacity);
            TextView availability = (TextView) view.findViewById(R.id.detail_lot_availability);
            TextView nearbyAttraction = (TextView) view.findViewById(R.id.detail_lot_nearby);
            TextView price = (TextView) view.findViewById(R.id.detail_lot_price);

            name.setText(lot.getLotName());
            type.setText(lot.getLotType());
            address.setText(lot.getAddress() + " " + lot.getCity() + ", " + lot.getState());
            operationHour.setText(lot.getOperationHour());
            capacity.setText(lot.getCapacity().toString());
            availability.setText(lot.getAvailability());
            nearbyAttraction.setText(lot.getNearbyAttraction());
            price.setText("RM" + Double.valueOf(lot.getPrice().getFirstHour()) / 100);

            return view;
        }
    }
}
