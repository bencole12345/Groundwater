package uk.ac.cam.cl.juliet.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import uk.ac.cam.cl.juliet.R;
import uk.ac.cam.cl.juliet.data.AttenuatorSettings;
import uk.ac.cam.cl.juliet.dialogs.AttenuatorsDialog;

/**
 * Fragment for the 'settings' screen.
 *
 * @author Ben Cole
 */
public class SettingsFragment extends Fragment
        implements AttenuatorsDialog.OnAttenuatorsSelectedListener, Button.OnClickListener {

    private TextView connectionStatusText;
    private ImageView connectionStatusIcon;
    private TextView selectedDateOutput;
    private TextView selectedTimeOutput;
    private TextView latitudeOutput;
    private TextView longitudeOutput;
    private Button setDateButton;
    private Button setTimeButton;
    private Button setGPSButton;
    private Button configureAttenuatorsButton;
    private Button sendToDeviceButton;

    private int minute;
    private int hourOfDay;
    private int day;
    private int month;
    private int year;
    private double latitude;
    private double longitude;
    private List<Integer> attenuators;
    private List<Integer> gains;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        connectionStatusText = view.findViewById(R.id.connectionStatusText);
        connectionStatusIcon = view.findViewById(R.id.connectionStatusImageView);

        // Find the text views
        selectedDateOutput = view.findViewById(R.id.selectedDateText);
        selectedTimeOutput = view.findViewById(R.id.selectedTimeText);
        latitudeOutput = view.findViewById(R.id.latitudeText);
        longitudeOutput = view.findViewById(R.id.longitudeText);

        // Find the buttons and set this class as the click listener
        setDateButton = view.findViewById(R.id.setDateButton);
        setDateButton.setOnClickListener(this);
        setTimeButton = view.findViewById(R.id.setTimeButton);
        setTimeButton.setOnClickListener(this);
        setGPSButton = view.findViewById(R.id.setGPSButton);
        setGPSButton.setOnClickListener(this);
        configureAttenuatorsButton = view.findViewById(R.id.configureAttenuatorsButton);
        configureAttenuatorsButton.setOnClickListener(this);
        sendToDeviceButton = view.findViewById(R.id.sendToDeviceButton);
        sendToDeviceButton.setOnClickListener(this);

        setDefaultValues();

        setConnectedStatus(getConnectionStatus());
        return view;
    }

    @Override
    public void onResume() {
        Activity activity = getActivity();
        if (activity == null) return;
        super.onResume();
        try {
            activity.getWindow()
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private boolean getConnectionStatus() {
        // TODO: Implement
        return true;
    }

    private void setConnectedStatus(boolean connected) {
        if (connected) {
            connectionStatusText.setText(R.string.connected);
            connectionStatusText.setTextColor(getResources().getColor(R.color.success));
            connectionStatusIcon.setImageResource(R.drawable.baseline_wifi_black_24);
            connectionStatusIcon.setColorFilter(getResources().getColor(R.color.success));
        } else {
            connectionStatusText.setText(R.string.disconnected);
            connectionStatusText.setTextColor(getResources().getColor(R.color.failure));
            connectionStatusIcon.setImageResource(R.drawable.baseline_wifi_off_black_24);
            connectionStatusIcon.setColorFilter(getResources().getColor(R.color.failure));
        }
    }

    /** Sets the default values for when the screen is first loaded. */
    private void setDefaultValues() {
        Calendar calendar = Calendar.getInstance();
        minute = calendar.get(Calendar.MINUTE);
        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        onNewTimeSet(hourOfDay, minute);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);
        onNewDateSet(year, month, day);
        attenuators = new ArrayList<>();
        attenuators.add(15);
        gains = new ArrayList<>();
        gains.add(-14);
    }

    /** Displays a dialog for setting the time of the radar device. */
    private void showSetTimeDialog() {
        Context context = getContext();
        if (context == null) return;

        // Get current time
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog =
                new TimePickerDialog(
                        context,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                onNewTimeSet(hourOfDay, minute);
                            }
                        },
                        hourOfDay,
                        minute,
                        false);

        dialog.show();
    }

    /**
     * Callback for when the dialog completes and a new time has been chosen.
     *
     * @param hour The hourOfDay of the day
     * @param minute The minute of the hourOfDay
     */
    private void onNewTimeSet(int hour, int minute) {
        this.hourOfDay = hour;
        this.minute = minute;
        // TODO: handle string formatting properly
        selectedTimeOutput.setText(hour + ":" + minute);
    }

    /** Displays a dialog for setting the date of the radar device. */
    private void showSetDateDialog() {
        Context context = getContext();
        if (context == null) return;

        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog =
                new DatePickerDialog(
                        context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(
                                    DatePicker view, int year, int month, int dayOfMonth) {
                                onNewDateSet(year, month, dayOfMonth);
                            }
                        },
                        year,
                        month,
                        day);

        dialog.show();
    }

    /**
     * Callback for when the dialog completes and a new date has been chosen.
     *
     * @param year The year that was selected
     * @param month The month of the year
     * @param day The day of the month
     */
    private void onNewDateSet(int year, int month, int day) {
        // TODO: handle string formatting properly
        this.year = year;
        this.month = month;
        this.day = day;
        selectedDateOutput.setText(day + "/" + month + "/" + year);
    }

    private void showSetLocationDialog() {
        // TODO: implement
        Context context = getContext();
        if (context == null) return;
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_gps_coordinates);
        final TextView latitudeInput = dialog.findViewById(R.id.latitudeInput);
        final TextView longitudeInput = dialog.findViewById(R.id.longitudeInput);
        dialog.findViewById(R.id.setButton)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                double latitude =
                                        Double.parseDouble(latitudeInput.getText().toString());
                                double longitude =
                                        Double.parseDouble(longitudeInput.getText().toString());
                                dialog.cancel();
                                onNewLocationSet(latitude, longitude);
                            }
                        });
        dialog.findViewById(R.id.cancelButton)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });
        dialog.show();
    }

    /**
     * Callback for when a new location is selected.
     *
     * @param latitude The latitude that was selected
     * @param longitude The longitude that was selected
     */
    private void onNewLocationSet(double latitude, double longitude) {
        if ((-90 <= latitude) && (latitude <= 90) && (-180 <= longitude) && (longitude <= 180)) {
            this.latitude = latitude;
            this.longitude = longitude;
            latitudeOutput.setText(String.format(Locale.getDefault(), "%f", latitude));
            longitudeOutput.setText(String.format(Locale.getDefault(), "%f", longitude));
        } else {
            Toast.makeText(getContext(), R.string.invalid_gps_coords, Toast.LENGTH_SHORT).show();
        }
    }

    private void showAttenuatorsDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager == null) return;
        AttenuatorsDialog dialog = new AttenuatorsDialog();
        dialog.setOnAttenuatorsSelectedListener(this);
        AttenuatorSettings attenuatorSettings = new AttenuatorSettings(attenuators, gains);
        Bundle arguments = new Bundle();
        arguments.putSerializable(AttenuatorsDialog.ATTENUATOR_SETTINGS, attenuatorSettings);
        dialog.setArguments(arguments);
        dialog.show(fragmentManager, "AttenuatorsDialog");
    }

    /** Handles packaging up the configuration settings and sending them to the device. */
    private void sendToDevice() {
        // TODO: Implement this
        // TODO: we need a way to build a config file by setting params, then let the Config class
        //       write those params to a file.
    }

    @Override
    public void onAttenuatorsSelected(List<Integer> attenuators, List<Integer> gains) {
        this.attenuators = attenuators;
        this.gains = gains;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setDateButton:
                showSetDateDialog();
                break;
            case R.id.setTimeButton:
                showSetTimeDialog();
                break;
            case R.id.setGPSButton:
                showSetLocationDialog();
                break;
            case R.id.configureAttenuatorsButton:
                showAttenuatorsDialog();
                break;
            case R.id.sendToDeviceButton:
                sendToDevice();
        }
    }
}
