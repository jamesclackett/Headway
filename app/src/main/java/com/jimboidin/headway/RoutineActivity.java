package com.jimboidin.headway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;

public class RoutineActivity extends AppCompatActivity {
    private static SharedPreferences mPreferences;
    private String sharedPrefFile = "com.jimboidin.headway";
    private static RecyclerView mRecyclerView;
    private static final LinkedList<String[]> mExerciseLinkedList = new LinkedList<>();
    private ExerciseListAdapter mAdapter;
    private static final LinkedList<String> mNotesLinkedList = new LinkedList<>();
    private static RecyclerView mRecyclerViewNotes;
    private NoteListAdapter mAdapterNotes;
    private static ItemTouchHelper helper;
    private static ItemTouchHelper helperNotes;
    public AlertDialog.Builder builder;
    public static Toolbar secondToolbar;
    public static CustomLayoutManager customLayoutManager;

    private static ImageView doneButton;
    private static ImageView addNoteButton;
    private static Button addButton;
    private static TextView notesHeader;
    private static RecyclerView.ItemAnimator rvAnimator;

    private boolean freshDataset = false;
    private static boolean helperAttached;
    public static String key;
    public static int beige;
    public static int dark;
    public static int hintColor;
    public static String routineName;
    public static TextView customTitleSecond;
    public static double physicalScreenSize;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mExerciseLinkedList.clear();
        mNotesLinkedList.clear();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        double x = Math.pow(dm.widthPixels/dm.xdpi,2);
        double y = Math.pow(dm.heightPixels/dm.ydpi,2);
        double screenInches = Math.sqrt(x+y);

        screenInches=  (double)Math.round(screenInches * 10) / 10;
        physicalScreenSize = screenInches;


        beige = getResources().getColor(R.color.colorLightBeige);
        dark = getResources().getColor(R.color.colorPrimaryDark);
        doneButton = findViewById(R.id.done_button_main);
        addButton = findViewById(R.id.add_exercise_button);
        addNoteButton = findViewById(R.id.add_note_button);
        customTitleSecond = findViewById(R.id.custom_title_second);
        notesHeader = findViewById(R.id.note_header);

        secondToolbar = (Toolbar) findViewById(R.id.toolbar_second);
        secondToolbar.setTitle("");
        setSupportActionBar(secondToolbar);

        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);

        }

        Intent intent = getIntent();
        routineName = intent.getStringExtra("routineName");
        customTitleSecond.setText(routineName);
        key = intent.getStringExtra("routineIdExtra");
        System.out.println("==================================================================");

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        Gson gson = new Gson();
        String json = mPreferences.getString(key, "");
        Type type = new TypeToken<LinkedList<String[]>>() {
        }.getType();
        LinkedList<String[]> tempLinkedList = gson.fromJson(json, type);

        if (tempLinkedList == null || tempLinkedList.size() == 0) {
            String[] exerciseItem = new String[5];
            exerciseItem[0] = "";
            exerciseItem[1] = "";
            exerciseItem[2] = "";
            exerciseItem[3] = "";
            exerciseItem[4] = "";
            mExerciseLinkedList.addLast(exerciseItem);
        } else {
            for (String[] strArr : tempLinkedList) {
                mExerciseLinkedList.addLast(strArr);
            }
        }

        // Get Notes from sharedPref:
        gson = new Gson();
        json = mPreferences.getString(key + "_notes", "");
        type = new TypeToken<LinkedList<String>>() {
        }.getType();
        LinkedList<String> tempNoteLinkedList = gson.fromJson(json, type);

        if (tempNoteLinkedList != null) {
            if (tempNoteLinkedList.size() != 0) {
                findViewById(R.id.note_header).setVisibility(View.VISIBLE);
                for (String str : tempNoteLinkedList) {
                    if (!str.isEmpty()) mNotesLinkedList.addLast(str);
                }
            }

        }
        if (tempNoteLinkedList == null || tempNoteLinkedList.isEmpty()){
            notesHeader.setVisibility(View.GONE);
        }

        //Set up RecyclerView & Adapter
        mRecyclerView = findViewById(R.id.recyclerview);
        mAdapter = new ExerciseListAdapter(this, mExerciseLinkedList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new CustomLayoutManager(this));
        rvAnimator = mRecyclerView.getItemAnimator();
        customLayoutManager = (CustomLayoutManager) mRecyclerView.getLayoutManager();
        mRecyclerView.setNestedScrollingEnabled(false);


        mRecyclerViewNotes = findViewById(R.id.notes_recyclerview);
        mAdapterNotes = new NoteListAdapter(this, mNotesLinkedList);
        mRecyclerViewNotes.setAdapter(mAdapterNotes);
        mRecyclerViewNotes.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewNotes.setNestedScrollingEnabled(false);


        helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int position_dragged = viewHolder.getAdapterPosition();
                int position_target = target.getAdapterPosition();
                Collections.swap(mExerciseLinkedList, position_dragged, position_target);
                saveToSharePref(mExerciseLinkedList);
                mAdapter.notifyItemMoved(position_dragged, position_target);
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                EditText exercise = viewHolder.itemView.findViewById(R.id.exercise_name_EditText);
                String id = exercise.getTag().toString();
                showDeleteDialog(id);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                float topY = viewHolder.itemView.getTop() + dY;
                float bottomY = topY + viewHolder.itemView.getHeight();

                // Only redraw child if it is inbounds of view
                if (topY > 0 && bottomY < recyclerView.getHeight()) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        });

        helperNotes = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                showDeleteNoteDialog(pos);

            }
        });

    }

        @Override
    protected void onPause() {
        super.onPause();
        System.out.println("paused!!!!");
        saveData();
    }

    @Override
    public void onBackPressed() {
        if (helperAttached) detachHelper();
        else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    public void newExercise(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        view.setEnabled(false);
        String[] exerciseItem = new String[5];
        exerciseItem[0] = "";
        exerciseItem[1] = "";
        exerciseItem[2] = "";
        exerciseItem[3] = "";
        exerciseItem[4] = "";
        mExerciseLinkedList.addLast(exerciseItem);
        if (freshDataset) {
            addButton.setVisibility(View.GONE);
        }else {
            ExerciseListAdapter.isNew = true;
        }
        freshDataset = false;
        saveToSharePref(mExerciseLinkedList);
        if (getCurrentFocus() != null ) getCurrentFocus().clearFocus();
        mRecyclerView.getAdapter().notifyItemInserted(mExerciseLinkedList.size()-1);
        view.setEnabled(true);
    }

    public static void saveToSharePref(LinkedList<String[]> ll){
        Gson gson = new Gson();
        String json = gson.toJson(ll);
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString(key, json);
        preferencesEditor.apply();
    }

    public static void saveNotesToSharedPref(LinkedList<String> ll){
        Gson gson = new Gson();
        String json = gson.toJson(ll);
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString(key+"_notes", json);
        preferencesEditor.apply();
    }

    public void deleteExercises(View view){
        mExerciseLinkedList.clear();
        ExerciseListAdapter.editTextList.clear();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.remove(key);
        preferencesEditor.apply();
        freshDataset = true;
        addButton.setVisibility(View.VISIBLE);
    }


    public static void showEditingButtons(){
        addButton.setVisibility(View.VISIBLE);
        addButton.startAnimation(MainActivity.animationFadeInSlow);

    }

    public static void hideEditingButtons(){  //make this a function that fades in or out of any view given
        addButton.startAnimation(MainActivity.animationFadeOutSlow);
        addButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                addButton.setVisibility(View.GONE);
            }
        }, 100);
    }

    public void fixParams(){
        for (int i = 0; i < ExerciseListAdapter.exerciseItemViewList.size(); i++) {
            LinearLayout ll = ExerciseListAdapter.exerciseItemViewList.get(i).findViewById(R.id.exerciseLinearLayout);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) ll.getLayoutParams();
            params.setMargins(40, 15, 40, 5);
            ll.animate().translationY(5).setDuration(150);
            ll.setLayoutParams(params);
        }

    }

    public static void attachHelper(){
        mRecyclerView.setItemAnimator(rvAnimator);
        ExerciseListAdapter.isNew = true;

        for (int i = 0; i < ExerciseListAdapter.exerciseItemViewList.size(); i++){
            LinearLayout ll = ExerciseListAdapter.exerciseItemViewList.get(i).findViewById(R.id.exerciseLinearLayout);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) ll.getLayoutParams();
            params.setMargins(40,15,40,5);
            ll.animate().translationY(5).setDuration(150);
            ll.setLayoutParams(params);

            View itemViewElement  = ExerciseListAdapter.exerciseItemViewList.get(i);
            itemViewElement.setBackground(MainActivity.customBorder);
            EditText editTextName = itemViewElement.findViewById(R.id.exercise_name_EditText);
            editTextName.setTextColor(Color.parseColor("#878787"));
            EditText editTextReps = itemViewElement.findViewById(R.id.reps_EditText);
            editTextReps.setTextColor(Color.parseColor("#878787"));
            EditText editTextSets = itemViewElement.findViewById(R.id.sets_EditText);
            editTextSets.setTextColor(Color.parseColor("#878787"));
            EditText editTextWeight = itemViewElement.findViewById(R.id.weight_EditText);
            editTextWeight.setTextColor(Color.parseColor("#878787"));
        }

        for (int i=0; i < ExerciseListAdapter.editTextList.size(); i++) {
            ExerciseListAdapter.editTextList.get(i).clearFocus();
            ExerciseListAdapter.editTextList.get(i).requestFocus();
            ExerciseListAdapter.editTextList.get(i).setEnabled(false);

        }

        if (! helperAttached) {
            customTitleSecond.startAnimation(MainActivity.animationFadeOut);
            customTitleSecond.postDelayed(new Runnable() {
                @Override
                public void run() {
                    customTitleSecond.setVisibility(View.GONE);
                    doneButton.setVisibility(View.VISIBLE);
                }
            }, 0);
            doneButton.startAnimation(MainActivity.animationFadeIn);
            addNoteButton.setVisibility(View.VISIBLE);
            addNoteButton.startAnimation(MainActivity.animationFadeIn);

            helperAttached = true;
            helper.attachToRecyclerView(mRecyclerView);
            helperNotes.attachToRecyclerView(mRecyclerViewNotes);
            System.out.println("helper attached");
        } else {
            System.out.println("helper already attached!");
        }

        notesHeader.animate().translationY(5).setDuration(150);

        for (int i = 0; i < NoteListAdapter.itemViewList.size(); i++){
            NoteListAdapter.itemViewList.get(i).animate().translationY(5).setDuration(150);
        }
    }

    public void detachHelperButton(View view){
        detachHelper();
    }

    public static void detachHelper() {
        doneButton.startAnimation(MainActivity.animationFadeOut);
        doneButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                doneButton.setVisibility(View.GONE);
                customTitleSecond.setVisibility(View.VISIBLE);
            }
        }, 0);
        customTitleSecond.startAnimation(MainActivity.animationFadeIn);

        addNoteButton.startAnimation(MainActivity.animationFadeOut);
        addNoteButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                addNoteButton.setVisibility(View.GONE);
            }
        },10);

        ExerciseListAdapter.longPressed = false;
        NoteListAdapter.longPressed = false;
        ExerciseListAdapter.isNew = false;
        NoteListAdapter.isNew = false;
        hideEditingButtons();
        mRecyclerView.setItemAnimator(null);
        helper.attachToRecyclerView(null);
        helperNotes.attachToRecyclerView(null);
        helperAttached = false;
        System.out.println("helper detached");

        for (int i = 0; i < ExerciseListAdapter.exerciseItemViewList.size(); i++){
            LinearLayout ll = ExerciseListAdapter.exerciseItemViewList.get(i).findViewById(R.id.exerciseLinearLayout);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) ll.getLayoutParams();
            params.setMargins(0,0,0,0);
            ll.animate().translationY(0).setDuration(150);
            ll.setLayoutParams(params);

            View itemViewElement  = ExerciseListAdapter.exerciseItemViewList.get(i);
            itemViewElement.setBackground(MainActivity.bottomBorder);
            EditText editTextName = itemViewElement.findViewById(R.id.exercise_name_EditText);
            editTextName.setTextColor(Color.BLACK);
            EditText editTextReps = itemViewElement.findViewById(R.id.reps_EditText);
            editTextReps.setTextColor(Color.BLACK);
            EditText editTextSets = itemViewElement.findViewById(R.id.sets_EditText);
            editTextSets.setTextColor(Color.BLACK);
            EditText editTextWeight = itemViewElement.findViewById(R.id.weight_EditText);
            editTextWeight.setTextColor(Color.BLACK);

        }

        for (int i=0; i < ExerciseListAdapter.editTextList.size(); i++){
            ExerciseListAdapter.editTextList.get(i).setLongClickable(true);
            ExerciseListAdapter.editTextList.get(i).clearFocus();
            ExerciseListAdapter.editTextList.get(i).setEnabled(true);

        }

        notesHeader.animate().translationY(0).setDuration(250);

        for (int i = 0; i < NoteListAdapter.itemViewList.size(); i++){
            EditText editText = NoteListAdapter.itemViewList.get(i).findViewById(R.id.note_edit_text);
            editText.clearFocus();
            editText.setCursorVisible(false);
            editText.setEnabled(true);
            NoteListAdapter.itemViewList.get(i).animate().translationY(0).setDuration(250);
        }


    }

    public void saveDataButton(View view){
        saveData();
        getCurrentFocus().clearFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        for (int i = 0; i < ExerciseListAdapter.editTextList.size(); i++){
            ExerciseListAdapter.editTextList.get(i).setLongClickable(true);
            ExerciseListAdapter.editTextList.get(i).setClickable(true);
            ExerciseListAdapter.editTextList.get(i).setTextColor(Color.BLACK);
        }
    }

    public static void saveNoteData(){
        int size = mRecyclerViewNotes.getAdapter().getItemCount();
        for (int i = 0; i < size; i++){
            EditText editText = mRecyclerViewNotes.getChildAt(i).findViewById(R.id.note_edit_text);
            mNotesLinkedList.set(i, editText.getText().toString());
        }
        saveNotesToSharedPref(mNotesLinkedList);
        mRecyclerViewNotes.getAdapter().notifyDataSetChanged();
    }


    public static void saveData(){
        customLayoutManager.setScrollEnabled(true);
        int size = mRecyclerView.getAdapter().getItemCount();

        for (int i = 0; i < size; i++){
            if (mRecyclerView.getChildAt(i) != null) {
                String[] exerciseItem = new String[5];
                EditText editTextName = mRecyclerView.getChildAt(i).findViewById(R.id.exercise_name_EditText);
                EditText editTextReps = mRecyclerView.getChildAt(i).findViewById(R.id.reps_EditText);
                EditText editTextSets = mRecyclerView.getChildAt(i).findViewById(R.id.sets_EditText);
                EditText editTextWeight = mRecyclerView.getChildAt(i).findViewById(R.id.weight_EditText);

                exerciseItem[0] = editTextName.getText().toString();
                exerciseItem[1] = editTextReps.getText().toString();
                exerciseItem[2] = editTextSets.getText().toString();
                exerciseItem[3] = editTextWeight.getText().toString();
                exerciseItem[4] = editTextName.getTag().toString();

                for (int j = 0; j <mExerciseLinkedList.size(); j++){
                    if (mExerciseLinkedList.get(j)[4].equals((String) exerciseItem[4])){
                        mExerciseLinkedList.set(j, exerciseItem);
                    }
                }
            }
        }

        saveToSharePref(mExerciseLinkedList);
    }


    public void addNoteButton(View view) {
        NoteListAdapter.isNew = true;
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        if (mNotesLinkedList.size() == 0) findViewById(R.id.note_header).setVisibility(View.VISIBLE);
        mNotesLinkedList.addLast("");
        mRecyclerViewNotes.getAdapter().notifyItemInserted(mNotesLinkedList.size()-1);
        saveNotesToSharedPref(mNotesLinkedList);
    }

    public void confirmDeleteNote(int pos){
        mNotesLinkedList.remove(pos);
        mAdapterNotes.notifyDataSetChanged();
        saveNotesToSharedPref(mNotesLinkedList);
        if (mNotesLinkedList.size() == 0) notesHeader.setVisibility(View.GONE);
    }

    public void confirmDelete(String id){
        for (int i = 0; i < mExerciseLinkedList.size(); i++) {
            if (mExerciseLinkedList.get(i)[4].equals(id)) {
                mExerciseLinkedList.remove(i);
            }
        }
        saveToSharePref(mExerciseLinkedList);
        ExerciseListAdapter.editTextList.clear();
        fixParams();
        mAdapter.notifyDataSetChanged();
        ///ExerciseListAdapter.isNew = false;

    }


    public void showDeleteDialog(final String id){
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete item?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmDelete(id);
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

    public void showDeleteNoteDialog(final int pos){
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete item?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmDeleteNote(pos);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                mAdapterNotes.notifyDataSetChanged();
            }
        });
        builder.show();
    }
}