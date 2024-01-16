package com.jimboidin.headway;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceFragmentCompat;

import com.github.mikephil.charting.data.Entry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat unitsSwitch;
    private Toast toast;
    private static SharedPreferences mPreferences;
    private String sharedPrefFile = "com.jimboidin.headway";
    public static boolean fullReset = false;
    public static boolean lastEntryDeleted = false;
    public static boolean graphReset = false;
    public AlertDialog.Builder builder;
    public static String metric;
    public static boolean metricChanged = false;
    private int counter;
    CompoundButton.OnCheckedChangeListener mListener;

    //Graph variables:
    private ArrayList<Entry> yValuesCalories;
    private ArrayList<Entry> yValuesWeight;
    private ArrayList<String> xLabels;
    private boolean finalCalorieEntryMade;
    private boolean finalWeightEntryMade;
    private boolean entryIncreased;
    private int entryNumber;
    private int dailyCalories = 0;
    private String currentWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);

        toast = null;
        counter = 0;
        yValuesCalories = new ArrayList<>();
        yValuesWeight = new ArrayList<>();
        xLabels = new ArrayList<>();
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        metric = mPreferences.getString("metric", "kg");
        getGraphDataFromSharedPref();


        mListener = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showToast("Pounds (lbs)");
                    changeUnits(true);
                } else {
                    showToast("Kilograms (kg)");
                    changeUnits(false);
                }
            }
        };

        unitsSwitch = findViewById(R.id.units_switch);
        unitsSwitch.setOnCheckedChangeListener(mListener);
    }

    private void changeUnits(boolean isChecked) {
        System.out.println("getting called settings!");
        if (isChecked) {
            metric = "lbs";
            saveToGraphSharedPref("metric");
            convertDataset("lbs");
        } else {
            metric = "kg";
            saveToGraphSharedPref("metric");
            convertDataset("kg");
        }
        metricChanged = true;

    }

    private void deleteLastGraphEntry() {
        if (xLabels.size() == 0) {
            showToast("No data to delete");
            return;
        }
        int index = xLabels.size() - 1;
        xLabels.remove(index);
        for (int i = 0; i < yValuesCalories.size(); i++) {
            if (yValuesCalories.get(i).getX() == index) yValuesCalories.remove(i);
        }
        for (int i = 0; i < yValuesWeight.size(); i++) {
            if (yValuesWeight.get(i).getX() == index) yValuesWeight.remove(i);
        }
        finalCalorieEntryMade = false;
        finalWeightEntryMade = false;
        entryIncreased = false;
        entryNumber--;
        dailyCalories = 0;
        if (yValuesWeight.size() > 0) {
            Entry entry = yValuesWeight.get(yValuesWeight.size() - 1);
            if (entry.getY() == Math.round(entry.getY())) {
                int currentWeightInt = (int) entry.getY();
                currentWeight = Integer.toString(currentWeightInt);
            } else {
                currentWeight = Float.toString(entry.getY());
            }
        } else {
            currentWeight = "0";
        }


        saveToGraphSharedPref("daily_calories");
        saveToGraphSharedPref("current_weight");
        saveToGraphSharedPref("y_values_weight");
        saveToGraphSharedPref("y_values_calories");
        saveToGraphSharedPref("x_labels");
        saveToGraphSharedPref("entry_increased");
        saveToGraphSharedPref("entry_number");
        saveToGraphSharedPref("final_calorie_entry_made");
        saveToGraphSharedPref("final_weight_entry_made");

        lastEntryDeleted = true;
        showToast("Graph entry reset");
    }

    private void saveToGraphSharedPref(String key) {
        Gson gson = new Gson();
        String json;
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();

        switch (key) {
            case "x_labels":
                json = gson.toJson(xLabels);
                preferencesEditor.putString("x_labels", json);
                preferencesEditor.apply();
                break;
            case "y_values_weight":
                json = gson.toJson(yValuesWeight);
                preferencesEditor.putString("y_values_weight", json);
                preferencesEditor.apply();
                break;
            case "y_values_calories":
                json = gson.toJson(yValuesCalories);
                preferencesEditor.putString("y_values_calories", json);
                preferencesEditor.apply();
                break;
            case "entry_number":
                preferencesEditor.putInt("entry_number", entryNumber);
                preferencesEditor.apply();
                break;
            case "entry_increased":
                preferencesEditor.putBoolean("entry_increased", entryIncreased);
                preferencesEditor.apply();
                break;
            case "final_calorie_entry_made":
                preferencesEditor.putBoolean("final_calorie_entry_made", finalCalorieEntryMade);
                preferencesEditor.apply();
                break;
            case "final_weight_entry_made":
                preferencesEditor.putBoolean("final_weight_entry_made", finalWeightEntryMade);
                preferencesEditor.apply();
                break;
            case "daily_calories":
                preferencesEditor.putInt("daily_calories", dailyCalories);
                preferencesEditor.apply();
                break;
            case "current_weight":
                preferencesEditor.putString("current_weight", currentWeight);
                preferencesEditor.apply();
                break;
            case "metric":
                preferencesEditor.putString("metric", metric);
                preferencesEditor.apply();
                break;

        }

    }


    private void resetGraphData() {
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.remove("x_labels");
        preferencesEditor.remove("y_values_weight");
        preferencesEditor.remove("y_values_calories");
        preferencesEditor.remove("entry_number");
        preferencesEditor.remove("entry_increased");
        preferencesEditor.remove("final_calorie_entry_made");
        preferencesEditor.remove("final_weight_entry_made");
        preferencesEditor.remove("daily_calories");
        preferencesEditor.remove("entry_first");
        preferencesEditor.remove("last_date_started");
        preferencesEditor.remove("current_weight");
        preferencesEditor.apply();
        showToast("Graph Reset");
        graphReset = true;
    }

    private void resetAllData() {
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.clear().apply();
        fullReset = true;
        showToast("Reset All Data");

    }


    public void showToast(String message) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }

    private void getGraphDataFromSharedPref() {
        //get xLabels:
        Gson gson = new Gson();
        String json = mPreferences.getString("x_labels", "");
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        ArrayList<String> tempXLabels = gson.fromJson(json, type);
        if (tempXLabels == null) {
            System.out.println("tempXLabels is null");
        } else {
            xLabels.addAll(tempXLabels);
        }

        //get yValuesWeight:
        gson = new Gson();
        json = mPreferences.getString("y_values_weight", "");
        type = new TypeToken<ArrayList<Entry>>() {
        }.getType();
        ArrayList<Entry> tempYValuesWeight = gson.fromJson(json, type);
        if (tempYValuesWeight == null) {
            System.out.println("tempYValuesWeight is null");
        } else {
            yValuesWeight.addAll(tempYValuesWeight);
        }

        //get yValuesCalories:
        gson = new Gson();
        json = mPreferences.getString("y_values_calories", "");
        type = new TypeToken<ArrayList<Entry>>() {
        }.getType();
        ArrayList<Entry> tempYValuesCalories = gson.fromJson(json, type);
        if (tempYValuesCalories == null) {
            System.out.println("tempYValuesCalories is null");
        } else {
            yValuesCalories.addAll(tempYValuesCalories);
        }

        //get entryNumber:
        entryNumber = mPreferences.getInt("entry_number", 0);
        //get entryIncreased:
        entryIncreased = mPreferences.getBoolean("entry_increased", false);
        //get finalCalorieEntryMade:
        finalCalorieEntryMade = mPreferences.getBoolean("final_calorie_entry_made", false);
        //get finalWeightEntryMade:
        finalWeightEntryMade = mPreferences.getBoolean("final_weight_entry_made", false);
        //get dailyCalories:
        dailyCalories = mPreferences.getInt("daily_calories", 0);
        //get currentWeight:
        currentWeight = mPreferences.getString("current_weight", "");
    }

    public void showDeleteDialog(View view) {
        final int id = view.getId();
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset?");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (id == R.id.reset_last_graph_setting) deleteLastGraphEntry();
                else if (id == R.id.reset_graph_setting) resetGraphData();
                else if (id == R.id.reset_all_data_setting) resetAllData();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        if (metric.equals("kg")) {
            unitsSwitch.setOnCheckedChangeListener(null);
            unitsSwitch.setChecked(false);
            unitsSwitch.setOnCheckedChangeListener(mListener);
        }
        if (metric.equals("lbs")) {
            unitsSwitch.setOnCheckedChangeListener(null);
            unitsSwitch.setChecked(true);
            unitsSwitch.setOnCheckedChangeListener(mListener);
        }
        super.onResume();
    }

    private void convertDataset(String units) {
        int size = yValuesWeight.size();
        System.out.println(size);
        if (units.equals("lbs") && size > 0) {
            for (int i = 0; i < size; i++) {
                int yValue = (int) yValuesWeight.get(i).getY();
                double newValue = yValue / 0.453592;
                double roundedValue = Math.round(newValue * 100.0) / 100.0;
                System.out.println("rounded lbs value is: " + roundedValue);
                yValuesWeight.get(i).setY((float) roundedValue);
            }
        } else if (units.equals("kg") && size > 0) {
            for (int i = 0; i < size; i++) {
                int yValue = (int) yValuesWeight.get(i).getY();
                double newValue = yValue * 0.453592;
                double roundedValue = Math.round(newValue * 100.0) / 100.0;
                System.out.println("rounded kg value is: " + roundedValue);
                yValuesWeight.get(i).setY((float) roundedValue);
            }

        }
        saveToGraphSharedPref("y_values_weight");

        if (metric.equals("lbs") && !currentWeight.isEmpty() && !currentWeight.equals("0")) {
            float weight = Float.parseFloat(currentWeight);
            float newWeight = (float) (weight / 0.453592);
            currentWeight = Double.toString(Math.round(newWeight * 100.0) / 100.0);

        } else if (metric.equals("kg") && !currentWeight.isEmpty() && !currentWeight.equals("0")) {
            float weight = Float.parseFloat(currentWeight);
            double newWeight = (float) (weight * 0.453592);
            currentWeight = Double.toString(Math.round(newWeight * 100.0) / 100.0);
        }

        saveToGraphSharedPref("current_weight");
        System.out.println("saved conversion to shared pref");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void aboutApp(View view) {
        if (counter > 3) {
            showToast("Johnny B. Goode");
            counter = 0;
        } else {
            showToast("Headway v.1.0.2");
            counter++;
        }
    }


}