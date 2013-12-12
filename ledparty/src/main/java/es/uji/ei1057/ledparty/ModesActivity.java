package es.uji.ei1057.ledparty;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Locale;

public class ModesActivity extends Activity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    ModePagerAdapter modePagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager viewPager;
    private BluetoothMaster bluetoothMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothMaster = ((LEDPartyApp) getApplicationContext()).getBluetoothMaster(this);

        setContentView(R.layout.activity_modes);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        modePagerAdapter = new ModePagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(modePagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
                Log.d("ledparty", "onPageSelected: " + position);
                ModeFragment f = ((ModeFragment) modePagerAdapter.getItem(position));
                int i = f.getArguments().getInt(ModeFragment.ARG_SECTION_NUMBER);
                Toast.makeText(getApplicationContext(), "Fragment: " + i, Toast.LENGTH_SHORT).show();
                bluetoothMaster.setMode(i);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < modePagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(modePagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        // Show the Up button in the action bar.
        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        BluetoothMaster bluetoothMaster = ((LEDPartyApp) getApplication()).getBluetoothMaster(this);
        Log.d("ledparty", "ModesActivity onCreate");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.modes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure.
                NavUtils.navigateUpTo(this, new Intent(this, BluetoothConnectActivity.class));
                return true;
            case R.id.action_settings:
                Toast.makeText(this, "No implementado", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class ModePagerAdapter extends FragmentPagerAdapter {

        public ModePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a ModeFragment (defined as a static inner class below).
            switch (position) {
                case ModeFragment.MODE_TEXT:
                    return TextModeFragment.newInstance();
                case ModeFragment.MODE_SPECTRAL:
                    return ModeFragment.newInstance(position);
                case ModeFragment.MODE_BEATBOX:
                    return ModeFragment.newInstance(position);
                default:
                    return ModeFragment.newInstance(position);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case ModeFragment.MODE_TEXT:
                    return getString(R.string.title_section_text).toUpperCase(l);
                case ModeFragment.MODE_SPECTRAL:
                    return getString(R.string.title_section_spectral).toUpperCase(l);
                case ModeFragment.MODE_BEATBOX:
                    return getString(R.string.title_section_beatbox).toUpperCase(l);
            }
            return null;
        }
    }
}
