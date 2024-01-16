package com.jimboidin.headway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class GraphActivity extends AppCompatActivity {
    private LineChart mCalorieGraph;
    private LineData data;
    public AlertDialog.Builder builder;
    private static SharedPreferences mPreferences;
    private String sharedPrefFile = "com.jimboidin.headway";
    private ArrayList<Entry> yValuesCalories;
    private ArrayList<Entry> yValuesWeight;
    private ArrayList<String> xLabels;
    private Handler timeHandler;
    private Toolbar myToolbar;
    private Toast toast;

    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    private EditText calorieEditText;
    private EditText weightEditText;
    private TextView todaysCaloriesTextView;
    private TextView currentWeightTextView;

    private boolean finalCalorieEntryMade;
    private boolean finalWeightEntryMade;
    private int lastDateStarted;
    private boolean entryIncreased;
    private boolean entryFirst;
    private String currentWeight;
    private int entryNumber;
    private int today;
    private int dailyCalories;
    public static String metric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        myToolbar = findViewById(R.id.toolbar_third);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        timeHandler = new Handler();
        toast = null;

        mCalorieGraph = findViewById(R.id.calorie_graph);
        calorieEditText = findViewById(R.id.calorie_edittext);
        weightEditText = findViewById(R.id.person_weight_edittext);
        todaysCaloriesTextView = findViewById(R.id.todays_calories_text_view);
        currentWeightTextView = findViewById(R.id.person_current_weight);
        xLabels = new ArrayList<>();
        yValuesCalories = new ArrayList<>();
        yValuesWeight = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        today = calendar.get(Calendar.DAY_OF_YEAR);
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        //deleteSharedPref();


        //setup navigation drawer
        dl = (DrawerLayout)findViewById(R.id.activity_graph);
        dl.setDrawerElevation(0);
        t = new ActionBarDrawerToggle(this, dl,R.string.Open, R.string.Close);
        dl.addDrawerListener(t);
        t.setDrawerSlideAnimationEnabled(false);
        t.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nv = (NavigationView)findViewById(R.id.nv);
        nv.setItemIconTintList(null);

        int[][] state = new int[][] {
                new int[] {-android.R.attr.state_checked},
                new int[] {android.R.attr.state_checked}
        };

        int[] color = new int[] {
                Color.parseColor("#545454"),
                Color.parseColor("#b03f5d")
        };

        ColorStateList csl = new ColorStateList(state, color);

        nv.setItemTextColor(csl);

        nv.getMenu().findItem(R.id.weight_tracker).setChecked(true);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.home:
                        dl.closeDrawer(Gravity.LEFT, false);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        break;
                    case R.id.weight_tracker:
                        break;
                    case R.id.settings:
                        dl.closeDrawer(Gravity.LEFT, false);
                        startSettingsActivity();
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });

        getDataFromSharedPref();
        currentWeightTextView.setText("0"+metric);
        if (currentWeight != "") currentWeightTextView.setText(currentWeight+metric);
        if (dailyCalories!= 0) todaysCaloriesTextView.setText(dailyCalories+"kcal");

        //reset variables if new day (after 3am)
        checkTime();
        if (finalWeightEntryMade) weightEditText.setEnabled(false);
        if (finalCalorieEntryMade) calorieEditText.setEnabled(false);
        createGraphs();
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    public void submitCalories(View view){
        hideKeyboard(view);
        if (dailyCalories == 0){
            showToast("No value to submit");
            return;
        }
        if (finalCalorieEntryMade){
            showToast("Daily calories already submitted");
        }
        if (!finalCalorieEntryMade){
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Submit?");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    submitCaloriesProceed();
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
        calorieEditText.clearFocus();
        calorieEditText.setText("");
        calorieEditText.setEnabled(false);
    }

    public void submitWeight(View view){
        hideKeyboard(view);
        if (!finalWeightEntryMade && weightEditText.getText().toString().isEmpty()){
            showToast("No value given");
            return;
        }
        if (finalWeightEntryMade){
            showToast("Daily weight already submitted");
        }
        if (!finalWeightEntryMade){
            increaseEntryNumber();
            String weight = weightEditText.getText().toString();
            yValuesWeight.add(new Entry(entryNumber, Float.parseFloat(weight)));
            finalWeightEntryMade = true;
            currentWeight = weight;
            currentWeightTextView.setText(currentWeight+metric);
            saveToSharedPref("current_weight");
            saveToSharedPref("final_weight_entry_made");
            saveToSharedPref("y_values_weight");
            updateGraph();
        }
        weightEditText.setText("");
        weightEditText.clearFocus();
        weightEditText.setEnabled(false);
    }

    public void showToast(String message){
        if (toast != null) toast.cancel();
        toast = Toast.makeText(this, message, Toast.LENGTH_LONG );
        toast.show();
    }

    public void appendCalories(View view){
        hideKeyboard(view);
        if (!finalCalorieEntryMade && calorieEditText.getText().toString().isEmpty()){
            showToast("No value given");
            return;
        }

        if (finalCalorieEntryMade){
            showToast("Daily calories already submitted");
            return;
        }
        if (!finalCalorieEntryMade){
            String caloriesToAdd = calorieEditText.getText().toString();
            dailyCalories += Float.parseFloat(caloriesToAdd);
            saveToSharedPref("daily_calories");
            updateTodaysCalories();
        }
        calorieEditText.setText("");
        calorieEditText.clearFocus();
    }

    private void checkTime(){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (lastDateStarted == 400){ //no previous dates saved (new start)
            lastDateStarted = today;
            saveToSharedPref("last_date_started");
            return;
        }
        if (today == lastDateStarted || hour < 3) {
            System.out.println("not a new graph day, final entry variables not reset");
            return;
        }

        else if (today != lastDateStarted && hour >= 3){
            System.out.println("new graph day, variables reset");
            dailyCalories = 0;
            finalWeightEntryMade = false;
            finalCalorieEntryMade = false;
            entryIncreased = false;
            lastDateStarted = today;
            todaysCaloriesTextView.setText("0kcal");
            saveToSharedPref("daily_calories");
            saveToSharedPref("entry_increased");
            saveToSharedPref("final_weight_entry_made");
            saveToSharedPref("final_calorie_entry_made");
            saveToSharedPref("last_date_started");
        }
    }

    private void updateTodaysCalories(){
        todaysCaloriesTextView.setText(dailyCalories + "kcal");
    }

    private void increaseEntryNumber(){
        if (!entryIncreased && !entryFirst){
            System.out.println("Increased!");
            String pattern = "dd MMM";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            xLabels.add(date);
            entryNumber++;
            entryIncreased = true;
            saveToSharedPref("x_labels");
            saveToSharedPref("entry_increased");
            saveToSharedPref("entry_number");
        }
        if (entryFirst) {
            entryFirst = false;
            String pattern = "dd MMM";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            xLabels.add(date);
            saveToSharedPref("x_labels");
            saveToSharedPref("entry_first");
            entryIncreased = true;
            saveToSharedPref("entry_increased");

        }

    }


    private void saveToSharedPref(String key){
        Gson gson = new Gson();
        String json;
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();

        switch (key){
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
            case "entry_first":
                preferencesEditor.putBoolean("entry_first", entryFirst);
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
            case "last_date_started":
                preferencesEditor.putInt("last_date_started", lastDateStarted);
                preferencesEditor.apply();
                break;
            case "current_weight":
                preferencesEditor.putString("current_weight", currentWeight);
                preferencesEditor.apply();
                break;

        }


    }

    private void getDataFromSharedPref(){
        xLabels.clear();
        yValuesWeight.clear();
        yValuesCalories.clear();
        //get xLabels:
        Gson gson = new Gson();
        String json = mPreferences.getString("x_labels", "");
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> tempXLabels = gson.fromJson(json, type);
        if (tempXLabels == null){
            System.out.println("tempXLabels is null");
        }
        else{
            xLabels.addAll(tempXLabels);
        }

        //get yValuesWeight:
        gson = new Gson();
        json = mPreferences.getString("y_values_weight", "");
        type = new TypeToken<ArrayList<Entry>>() {}.getType();
        ArrayList<Entry> tempYValuesWeight = gson.fromJson(json, type);
        if (tempYValuesWeight == null){
            System.out.println("tempYValuesWeight is null");
        }
        else{
            yValuesWeight.addAll(tempYValuesWeight);
        }

        //get yValuesCalories:
        gson = new Gson();
        json = mPreferences.getString("y_values_calories", "");
        type = new TypeToken<ArrayList<Entry>>() {}.getType();
        ArrayList<Entry> tempYValuesCalories = gson.fromJson(json, type);
        if (tempYValuesCalories == null){
            System.out.println("tempYValuesCalories is null");
        }
        else{
            yValuesCalories.addAll(tempYValuesCalories);
        }

        //get entryFirst:
        entryFirst = mPreferences.getBoolean("entry_first", true);
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
        //get lastDateStarted:
        lastDateStarted = mPreferences.getInt("last_date_started", 400);
        //get currentWeight:
        currentWeight = mPreferences.getString("current_weight", "0");
        //get metric:
        metric = mPreferences.getString("metric", "kg");
    }

    public void createGraphs(){
        createGraphDataSet();
        mCalorieGraph.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                try {
                    return xLabels.get((int) value);
                } catch (Exception e) {
                    return "";
                }
            }
        });


        mCalorieGraph.setData(data);
        mCalorieGraph.getData().setHighlightEnabled(false);
        mCalorieGraph.setDescription(null);
        mCalorieGraph.setScaleEnabled(false);
        if (xLabels.size() > 6){
            mCalorieGraph.setVisibleXRangeMaximum(6);
            mCalorieGraph.setVisibleXRangeMinimum(6);
            mCalorieGraph.moveViewToX(xLabels.size() - 1);
        }
        Typeface font = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
        Typeface font_medium = Typeface.create("sans-serif-condensed-medium", Typeface.NORMAL);

        YAxis yAxis = mCalorieGraph.getAxisLeft();
        yAxis.setAxisLineWidth(0);
        yAxis.setTypeface(Typeface.DEFAULT_BOLD);
        yAxis.setTextColor(Color.parseColor("#8f8f8f"));
        yAxis.setGridLineWidth(0);
        yAxis.setTypeface(font_medium);

        yAxis = mCalorieGraph.getAxisRight();
        yAxis.setAxisLineWidth(0);
        yAxis.setTypeface(Typeface.DEFAULT_BOLD);
        yAxis.setTextColor(Color.parseColor("#8f8f8f"));
        yAxis.setTypeface(font_medium);

        XAxis xAxis = mCalorieGraph.getXAxis();
        xAxis.setAxisMinimum(mCalorieGraph.getData().getXMin() - 0.3f);
        xAxis.setAxisMaximum(mCalorieGraph.getData().getXMax() + 0.3f);
        xAxis.setAxisLineWidth(0);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setTextColor(Color.parseColor("#8f8f8f"));
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTypeface(font_medium);

        Legend legend = mCalorieGraph.getLegend();
        legend.setTextColor(Color.DKGRAY);
        legend.setDrawInside(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setTypeface(font);
    }

    private void createGraphDataSet(){
        LineDataSet set1 = new LineDataSet(yValuesCalories, "CALORIES");
        set1.setColor(Color.parseColor("#6064a6"));
        set1.setCircleColor(Color.parseColor("#6064a6"));
        set1.setLineWidth(4);
        set1.setDrawValues(false);
        set1.setCircleRadius(4);
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineDataSet set2 = new LineDataSet(yValuesWeight, "WEIGHT");
        set2.setColor(Color.RED);
        set2.setCircleColor(Color.RED);
        set2.setLineWidth(2);
        set2.setCircleRadius(2);
        set2.setCircleHoleColor(Color.RED);
        set2.enableDashedLine(5,5,0);
        set2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set2.setDrawValues(false);


        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        dataSets.add(set2);
        data = new LineData(dataSets);
    }

    private void updateGraph(){
        getDataFromSharedPref();
        createGraphDataSet();
        mCalorieGraph.setData(data);
        mCalorieGraph.getData().setHighlightEnabled(false);
        XAxis xAxis = mCalorieGraph.getXAxis();
        xAxis.setAxisMinimum(mCalorieGraph.getData().getXMin() - 0.3f);
        xAxis.setAxisMaximum(mCalorieGraph.getData().getXMax() + 0.3f);
        mCalorieGraph.invalidate();
        if (xLabels.size() > 6){
            mCalorieGraph.setVisibleXRangeMaximum(6);
            mCalorieGraph.setVisibleXRangeMinimum(6);
            mCalorieGraph.moveViewToX(xLabels.size() - 1);
        }

        System.out.println("graph updated");
    }


    @Override
    protected void onResume() {
        //the following code allows graph activity to update itself if relevant changes are made in settings
        runCheckTimeBackground();
        if (SettingsActivity.fullReset || SettingsActivity.lastEntryDeleted || SettingsActivity.graphReset){
            getDataFromSharedPref();
            currentWeightTextView.setText(currentWeight+metric);
            todaysCaloriesTextView.setText("0kcal");
            calorieEditText.setEnabled(true);
            weightEditText.setEnabled(true);
            finalCalorieEntryMade = false;
            finalWeightEntryMade = false;
            updateGraph();
            SettingsActivity.fullReset = false;
            SettingsActivity.lastEntryDeleted = false;
            SettingsActivity.graphReset = false;
        }
        if (SettingsActivity.metricChanged){
            getDataFromSharedPref();
            if (!currentWeight.isEmpty())currentWeightTextView.setText(currentWeight+metric);
            else currentWeightTextView.setText("0"+metric);
            updateGraph();
            SettingsActivity.metricChanged = false;

        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        timeHandler.removeMessages(0);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    private void runCheckTimeBackground(){
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                checkTime();
                System.out.println("checkedTime!");
                timeHandler.postDelayed(this, 10000);
            }
        };
        timeHandler.post(run);
    }

    private void submitCaloriesProceed(){
        increaseEntryNumber();
        yValuesCalories.add(new Entry(entryNumber, dailyCalories));
        finalCalorieEntryMade = true;
        saveToSharedPref("final_calorie_entry_made");
        saveToSharedPref("y_values_calories");
        updateGraph();
    }

    private void hideKeyboard(View v){
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }






}