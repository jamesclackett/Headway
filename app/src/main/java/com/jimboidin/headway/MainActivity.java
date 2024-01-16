package com.jimboidin.headway;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;


public class MainActivity extends AppCompatActivity {
    private static RecyclerView mRecyclerView;
    private static final LinkedList<String[]> mRoutineLinkedList = new LinkedList<>();
    private RoutineListAdapter mAdapter;
    private static ItemTouchHelper helper;
    private static SharedPreferences mPreferences;
    private String sharedPrefFile = "com.jimboidin.headway";
    private IntroPref introPref;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;
    public static Toolbar myToolbar;
    private static TextView toolbarText;
    public static Drawable navigationIcon;
    public static TextView mainHeading;
    public AlertDialog.Builder builder;
    private int routineId;
    public static boolean isNew;
    public static Boolean isAttached = false;
    public static boolean longPressed = false;
    public static Button addRoutineButton;
    public static ImageView doneButton;
    private String chosenName;

    public static int beige;
    public static int darkgrey;
    public static int primaryDark;
    public static Drawable customBorder;
    public static Drawable bottomBorder;
    public static Drawable headingBottomBorder;
    public static Drawable bottomBorderEdit;
    public static Drawable bottomBorderRoutine;
    public static Drawable bottomBorderRoutineEdit;
    public static Drawable customBorderSALT;
    public static Drawable customBorderALT;
    public static RecyclerView.ItemAnimator rvAnimator;
    public static Animation animation;
    public static Animation animation_out;
    public static Drawable customBorderRoutine;
    public static Animation animationFadeOut;
    public static Animation animationFadeIn;
    public static Animation animationFadeOutSlow;
    public static Animation animationFadeInSlow;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        myToolbar.setTitle(""); //custom title view will be set
        setSupportActionBar(myToolbar);
        mRoutineLinkedList.clear();

        introPref = new IntroPref(this);
        //introPref.setIsFirstTimeLaunch(true);
        if (introPref.isFirstTimeLaunch()){
            launchIntroSliderActivity();
        }

        //variable setup
        beige = getResources().getColor(R.color.colorLightBeige);
        primaryDark = getResources().getColor(R.color.colorPrimaryDark);
        darkgrey = getResources().getColor(R.color.colorDarkGrey);
        customBorder = getResources().getDrawable(R.drawable.custom_border);
        bottomBorder = getResources().getDrawable(R.drawable.bottom_border);
        headingBottomBorder = getResources().getDrawable(R.drawable.heading_bottom_border);
        bottomBorderEdit = getResources().getDrawable(R.drawable.bottom_border_editmode);
        bottomBorderRoutine = getResources().getDrawable(R.drawable.bottom_border_routine);
        bottomBorderRoutineEdit = getResources().getDrawable(R.drawable.bottom_border_routine_edit);
        customBorderSALT = getResources().getDrawable(R.drawable.custom_border_alt_start);
        customBorderALT = getResources().getDrawable(R.drawable.custom_border_alternative);
        customBorderRoutine = getResources().getDrawable(R.drawable.custom_border_routine);
        animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
        animation_out = AnimationUtils.loadAnimation(this, R.anim.slide_out_top);
        animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        animationFadeOutSlow = AnimationUtils.loadAnimation(this, R.anim.fade_out_slow);
        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        animationFadeInSlow = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);
        doneButton = findViewById(R.id.done_button_main);
        mainHeading = findViewById(R.id.main_heading);
        toolbarText = findViewById(R.id.toolbarText);
        addRoutineButton = findViewById(R.id.add_routine_button);

        //setup navigation drawer
        dl = (DrawerLayout)findViewById(R.id.activity_main);
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
        nv.getMenu().findItem(R.id.home).setChecked(true);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.home:
                        //
                        break;
                    case R.id.weight_tracker:
                        dl.closeDrawer(Gravity.LEFT, false);
                        startGraphActivity(null);
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

        //import dataset from SharedPref:
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        routineId = mPreferences.getInt("routineId", 0);

        Gson gson = new Gson();
        String json = mPreferences.getString("RoutineList", "");
        Type type = new TypeToken<LinkedList<String[]>>() {}.getType();
        LinkedList<String[]> tempLinkedList = gson.fromJson(json, type);

        if (tempLinkedList == null || tempLinkedList.size() == 0){
            routineId = 1000;
            String[] routineItem = new String[3];
            routineItem[0] = "New Routine";
            routineItem[1] = Integer.toString(routineId);
            mRoutineLinkedList.addLast(routineItem);
        }
        else{
            for (String[] strArr: tempLinkedList){
                mRoutineLinkedList.addLast(strArr);
            }
        }

        //setup RecyclerView and adapter
        mRecyclerView = findViewById(R.id.routine_recyclerview);
        mAdapter = new RoutineListAdapter(this, mRoutineLinkedList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rvAnimator = mRecyclerView.getItemAnimator();


        //setup itemtouchhelper to allow user to manage recyclerview items
        helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT|ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int position_dragged = viewHolder.getAdapterPosition();
                int position_target = target.getAdapterPosition();
                Collections.swap(mRoutineLinkedList, position_dragged, position_target);
                saveToSharePref(mRoutineLinkedList);
                mAdapter.notifyItemMoved(position_dragged, position_target);
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                askIfDelete(viewHolder.itemView.findViewById(R.id.routine_item_button));
            }

        });

    }

    private void launchIntroSliderActivity() {
        introPref.setIsFirstTimeLaunch(false);
        startActivity(new Intent(this, IntroSliderActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // nav menu icon disappears sometimes, hope this fixes
        System.out.println("resumed!");

        if (SettingsActivity.fullReset){ // if all data reset
            mRoutineLinkedList.clear();
            mAdapter.notifyDataSetChanged();
            showAddRoutineButton();
            SettingsActivity.fullReset = false;
        }
    }

    public static void showAddRoutineButton(){
        addRoutineButton.setVisibility(View.VISIBLE);
        addRoutineButton.startAnimation(animationFadeInSlow);
    }

    public static void hideAddRoutineButton(){
        addRoutineButton.startAnimation(animationFadeOutSlow);
        addRoutineButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                addRoutineButton.setVisibility(View.GONE);
            }
        }, 100);
    }

    public static void attachHelper(){
        navigationIcon =  myToolbar.getNavigationIcon();
        myToolbar.setNavigationIcon(null);
        mRecyclerView.setItemAnimator(rvAnimator);
        helper.attachToRecyclerView(mRecyclerView);
        doneButton.setVisibility(View.VISIBLE);
        showAddRoutineButton();
        toolbarText.setVisibility(View.INVISIBLE);
        doneButton.startAnimation(animationFadeIn);
        isAttached = true;
    }


    public void detachHelper(View view){
        isAttached = false;
        doneButton.startAnimation(animationFadeOut);
        doneButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                doneButton.setVisibility(View.GONE);
                myToolbar.setNavigationIcon(navigationIcon);


            }
        }, 250);

        longPressed = false;
        mRecyclerView.setItemAnimator(null);
        helper.attachToRecyclerView(null);
        for (int i = 0; i < RoutineListAdapter.itemViewList.size(); i++){
            LinearLayout ll = RoutineListAdapter.itemViewList.get(i).findViewById(R.id.routineLinearLayout);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) ll.getLayoutParams();
            ll.animate().translationY(0).setDuration(350);
            params.setMargins(0,0,0,0);

            RoutineListAdapter.itemViewList.get(i).setBackground(bottomBorderRoutine);
            ImageView editRoutineButton = RoutineListAdapter.itemViewList.get(i).findViewById(R.id.edit_routine_name_button);
            editRoutineButton.setVisibility(View.GONE);

            Button routineButton = RoutineListAdapter.itemViewList.get(i).findViewById(R.id.routine_item_button);
            routineButton.setTextColor(darkgrey);
        }

        hideAddRoutineButton();
        toolbarText.setVisibility(View.VISIBLE);
        toolbarText.startAnimation(animationFadeIn);
    }

    public void newRoutine(View view) {
        isNew = true;
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        view.setEnabled(false);
        routineId++;
        String[] routineItem = new String[2];
        routineItem[0] = "New Routine";
        routineItem[1] = Integer.toString(routineId);
        mRoutineLinkedList.addLast(routineItem);
        saveRoutineID(routineId);
        saveToSharePref(mRoutineLinkedList);
        mRecyclerView.getAdapter().notifyItemInserted(mRoutineLinkedList.size()-1);
        view.setEnabled(true);
        showNameDialog(Integer.toString(routineId), true);

    }


    public static void saveToSharePref(LinkedList<String[]> ll){
        Gson gson = new Gson();
        String json = gson.toJson(ll);
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString("RoutineList", json);
        preferencesEditor.apply();
    }

    private void saveRoutineID(int routineId){
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt("routineId", routineId);
        preferencesEditor.apply();
    }

    public void deleteRoutines(View view) {
        mRoutineLinkedList.clear();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.remove("RoutineList");
        preferencesEditor.apply();
        showAddRoutineButton();
    }

    public void askIfDelete(View view){
        String id = view.getTag().toString();
        showDeleteDialog(id);
    }


    public void confirmedDeleteSelectedRoutine(String id){
        for (int i = 0; i < mRoutineLinkedList.size(); i++){
            if (id.equals(mRoutineLinkedList.get(i)[1])){
                mRoutineLinkedList.remove(i);
            }
        }
        //delete the routine too
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.remove(id);
        preferencesEditor.apply();

        saveToSharePref(mRoutineLinkedList);
        mAdapter.notifyDataSetChanged();
        if (mRoutineLinkedList.size() == 0) {
            showAddRoutineButton();
        }
    }

    public void editRoutineName (View view){
        String id = view.getTag().toString();
        showNameDialog(id, false);
    }

    public void showDeleteDialog(final String id){
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete item?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmedDeleteSelectedRoutine(id);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                mAdapter.notifyDataSetChanged();
            }
        });
        builder.show();
    }


    public void showNameDialog(final String id, final boolean isNew){
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter title");
        builder.setMessage("Choose a name for your routine");
        final EditText input = new EditText(this);
        input.setPadding(10,5,10,35);
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 40;
        params.rightMargin = 40;
        input.setLayoutParams(params);
        container.addView(input);

        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        builder.setView(container);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().isEmpty() && isNew){
                    chosenName = "New Routine";
                }
                else if(input.getText().toString().isEmpty() && ! isNew){
                    return;
                }
                else {
                    chosenName= input.getText().toString();
                }

                for (int i = 0; i < mRoutineLinkedList.size(); i++){
                    if (id.equals(mRoutineLinkedList.get(i)[1])){
                        mRoutineLinkedList.get(i)[0] = chosenName;
                        break;
                    }
                }
                saveToSharePref(mRoutineLinkedList);
                mAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isNew){
                    confirmedDeleteSelectedRoutine(id);
                }
                dialog.cancel();
            }
        });
        builder.show();
    }



    public void startRoutineActivity(View view) {
        String id = view.getTag().toString();
        String name = "";
        for (int i = 0; i < mRoutineLinkedList.size(); i++){
            if (mRoutineLinkedList.get(i)[1].equals(id)){
                name = mRoutineLinkedList.get(i)[0];
            }
        }
        Intent intent = new Intent(this, RoutineActivity.class);
        intent.putExtra("routineIdExtra", id);
        intent.putExtra("routineName", name);
        if (!longPressed) {
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    public void startGraphActivity(View view){
        Intent intent = new Intent(this, GraphActivity.class);
        if (!longPressed){
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        if (!longPressed){
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (!isAttached) super.onBackPressed();
        else detachHelper(null);
    }
}