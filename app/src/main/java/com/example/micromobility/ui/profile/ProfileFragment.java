package com.example.micromobility.ui.profile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.micromobility.MainActivity;
import com.example.micromobility.R;
import com.example.micromobility.Storage.InternalStorage;
import com.example.micromobility.configuration.Help;
import com.example.micromobility.ui.home.HomeFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class ProfileFragment extends Fragment implements MainActivity.IOnBackPressed {


    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 2;

    private int[] tabIcons = {
            R.drawable.ic_map_black_24dp,
            R.drawable.ic_myvideos
    };


    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    TabLayout tabLayout;
    private ViewPager2 viewPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private ScreenSlidePagerAdapter pagerAdapter;
    private View mcontainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mcontainer = inflater.inflate(R.layout.fragment_profile, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_profile)+ " \uD83D\uDC64");



        // View pager
        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = mcontainer.findViewById(R.id.view_pager);

        tabLayout = mcontainer.findViewById(R.id.tabs);
        pagerAdapter = new ScreenSlidePagerAdapter(this, mcontainer.getContext());
        pagerAdapter.addFragment(new MyVideosFragment(), "My videos", tabIcons[0]);
        pagerAdapter.addFragment(new MyMapFragment(), "My map", tabIcons[1]);


        viewPager.setAdapter(pagerAdapter);


        viewPager.setPageTransformer(new ZoomOutPageTransformer());
        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0:
                                tab.setText("MY VIDEOS");
                                break;
                            case 1:
                                tab.setText("MY MAP");
                                break;
                            default:
                                tab.setText("MY VIDEOS");
                                break;
                        }
                    }
                }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                highLightCurrentTab(position);
            }
        });
        return mcontainer;
    }
    @Override
    public boolean onBackPressed() {
        if (viewPager != null) {
            if (viewPager.getCurrentItem() == 0) {
                // If the user is currently looking at the first step, allow the system to handle the
                // Back button. This calls finish() on this activity and pops the back stack.
                return false;
            } else {
                // Otherwise, select the previous step.
                    loadFragment(new HomeFragment(), "Profile-Home");
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("On Resume from profile fragment has been called in position "+ viewPager.getCurrentItem());
    }

    private void loadFragment(Fragment fragment, String tag) {
        System.out.println("Pressed "+ viewPager.getCurrentItem());
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(tag);
        transaction.commit();
    }



    private void highLightCurrentTab(int position) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            assert tab != null;
            tab.setCustomView(null);
            tab.setCustomView(pagerAdapter.getTabView(i));
        }
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        assert tab != null;
        tab.setCustomView(null);
        tab.setCustomView(pagerAdapter.getSelectedTabView(position));

    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private final List<Integer> mFragmentIconList = new ArrayList<>();
        private Context context;

        public ScreenSlidePagerAdapter(Fragment fa, Context context) {
            super(fa);
            this.context = context;
        }

        @Override
        public Fragment createFragment(int position) {
            return mFragmentList.get(position);
        }

        public void addFragment(Fragment fragment, String title, int tabIcon) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
            mFragmentIconList.add(tabIcon);
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }


        public View getTabView(int position) {
            View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
            TextView tabTextView = view.findViewById(R.id.tabTextView);
            tabTextView.setText(mFragmentTitleList.get(position));
            ImageView tabImageView = view.findViewById(R.id.tabImageView);
            tabImageView.setImageResource(mFragmentIconList.get(position));
            return view;
        }
        public View getSelectedTabView(int position) {
            View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
            TextView tabTextView = view.findViewById(R.id.tabTextView);
            tabTextView.setText(mFragmentTitleList.get(position));
            tabTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
            ImageView tabImageView = view.findViewById(R.id.tabImageView);
            tabImageView.setImageResource(mFragmentIconList.get(position));
            tabImageView.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryLight), PorterDuff.Mode.SRC_ATOP);
            return view;
        }

}

   // Animation for the transition method
    public class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }

}

