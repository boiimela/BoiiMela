package com.gssproductions.boiimela;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    TextView textViewWelcome, textViewFullName,
            textViewEmail, textViewDoB,
            textViewGender, textViewmobile,
            textView_verify_email, tv_total_ads_count,
            tv_total_ads_sold_count, tv_total_ads_available_count;

    //ProgressBar progressBar;
    String fullName, email, mobile, gender, doB;
    ImageView imageView;
    FirebaseAuth authProfile;

    Toolbar toolbar;

    int adscount = 0;
    int adsSoldcount = 0;

    private MenuItem menuItem;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // --------------

        setHasOptionsMenu(true);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(getContext(), "something went wrong user details are not right", Toast.LENGTH_LONG).show();

        } else {
           // progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }

        FirebaseDatabase.getInstance()
                .getReference()
                .child("bookData/" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        adscount = (int) snapshot.getChildrenCount();
                        System.out.println(adscount);
                        tv_total_ads_count.setText(String.valueOf(adscount));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        FirebaseDatabase.getInstance()
                .getReference()
                .child("bookData/" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild("sold")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot i : snapshot.getChildren()){
                            BookData ob = i.getValue(BookData.class);

                            if(ob.isSold){
                                adsSoldcount++;
                            }

                        }
                        System.out.println(adsSoldcount);
                        tv_total_ads_sold_count.setText(String.valueOf(adsSoldcount));

                        int adsAvailable = adscount - adsSoldcount;
                        tv_total_ads_available_count.setText(String.valueOf(adsAvailable));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.navigation_menu, menu);  // Use filter.xml from step 1
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //textViewWelcome = view.findViewById(R.id.TextView_show_welcome);
        textViewFullName = view.findViewById(R.id.textView_show_full_name);
        textViewEmail = view.findViewById(R.id.textView_show_email);
        textViewDoB = view.findViewById(R.id.textView_show_dob);
        textViewGender = view.findViewById(R.id.textView_show_gender);
        textViewmobile = view.findViewById(R.id.textView_show_mobile);
        textView_verify_email = view.findViewById(R.id.textView_verify_email);

        tv_total_ads_count = view.findViewById(R.id.tv_total_ads_count);
        tv_total_ads_sold_count = view.findViewById(R.id.tv_total_ads_sold_count);
        tv_total_ads_available_count = view.findViewById(R.id.tv_total_ads_available_count);

        toolbar = view.findViewById(R.id.toolbar);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("");


        tv_total_ads_available_count.setText("");
        tv_total_ads_count.setText("");
        tv_total_ads_sold_count.setText("");


        if(!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            textView_verify_email.setText("Verified");
            textView_verify_email.setTextColor(Color.GREEN);
            textView_verify_email.setPaintFlags(0);
        }


      return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.sign_out){

            FirebaseDatabase.getInstance()
                    .getReference()
                    .child("tokens")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .removeValue();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();

                }
            }, 2000);


            return true;
        }

        else if(id == R.id.menu_settings){
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();
        //Extracting user reference from database for " Registered users"

        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadwriteUserDetails readUserDetails = snapshot.getValue(ReadwriteUserDetails.class);
                if (readUserDetails != null) {
                    fullName = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    doB = readUserDetails.doB;
                    gender = readUserDetails.gender;
                    mobile = readUserDetails.mobile;

                    // textViewWelcome.setText("Welcome, " + fullName + "!");
                    textViewFullName.setText(fullName);
                    textViewEmail.setText(email);
                    textViewGender.setText(gender);
                    textViewDoB.setText(doB);
                    textViewmobile.setText(mobile);

                    if(!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                        textView_verify_email.setText("Verify");
                        textView_verify_email.setTextColor(Color.RED);
                        textView_verify_email.setPaintFlags(0);
                        textView_verify_email.setOnClickListener(click -> {
                            FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                        });
                    }
                    else{
                        textView_verify_email.setText("Verified");
                        textView_verify_email.setTextColor(Color.GREEN);
                    }

                }
                //progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
              //    Toast.makeText(ProfileFragment.this, "something went wrong!", Toast.LENGTH_LONG).show();
            }
        });

    }
}