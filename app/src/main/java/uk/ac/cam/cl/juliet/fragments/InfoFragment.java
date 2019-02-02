package uk.ac.cam.cl.juliet.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import uk.ac.cam.cl.juliet.R;

/**
 * Fragment for the 'information' screen.
 *
 * @author Ben Cole
 */
public class InfoFragment extends Fragment {

    private InfoOverviewFragment infoOverviewFragment;
    private InfoMoreDetailFragment infoMoreDetailFragment;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        // Create instances of the inner fragments
        infoOverviewFragment = new InfoOverviewFragment();
        infoMoreDetailFragment = new InfoMoreDetailFragment();

        // Connect the UI elements up
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager viewPager = view.findViewById(R.id.infoViewPager);
        InfoPagerAdapter adapter = new InfoPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_info);
    }

    /**
     * Supplies inner fragments to the TabLayout, so that the fragment can be replaced when a tab is
     * clicked.
     */
    private class InfoPagerAdapter extends FragmentStatePagerAdapter {

        InfoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return infoOverviewFragment;
                case 1:
                    return infoMoreDetailFragment;
            }
            return null;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_overview);
                case 1:
                    return getString(R.string.tab_details);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
