package com.jimboidin.headway;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.LinkedList;

public class ExerciseListAdapter extends RecyclerView.Adapter<ExerciseListAdapter.ExerciseViewHolder> {
    private final LinkedList<String[]> mExerciseList;
    private LayoutInflater mInflater;
    public static LinkedList<EditText> editTextList = new LinkedList<>();
    public static boolean isNew = false;
    public static boolean longPressed = false;
    public int rowId = 0;
    public static LinkedList<View> exerciseItemViewList = new LinkedList<>();

    public ExerciseListAdapter(Context context,
                           LinkedList<String[]> exerciseList) {
        mInflater = LayoutInflater.from(context);
        this.mExerciseList = exerciseList;
    }


    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.exerciselist_item, parent, false);
        return new ExerciseViewHolder(mItemView, this);
    }


    @Override
    public void onBindViewHolder(@NonNull final ExerciseListAdapter.ExerciseViewHolder holder, final int position) {
        rowId++;
        mExerciseList.get(position)[4] = Integer.toString(rowId);
        RoutineActivity.saveToSharePref(mExerciseList);
        for (int i = 0; i < mExerciseList.get(position).length-1; i++){
            final String mCurrent = mExerciseList.get(position)[i];
            final EditText editText = holder.exerciseItemView[i];

            editText.setText(mCurrent);
            editText.setTag(rowId);


            //establish dynamic maxLength based on screen size
            if (editText.getId() == R.id.exercise_name_EditText){
                System.out.println("Screen width is " + RoutineActivity.physicalScreenSize);
                if (RoutineActivity.physicalScreenSize >= 6.8){
                    editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(41) });
                    System.out.println("maxLength set to 41");
                }
                else if (RoutineActivity.physicalScreenSize >= 6.0){
                    editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(31) });
                    System.out.println("maxLength set to 31");
                }
                else if (RoutineActivity.physicalScreenSize < 6.0){
                    editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(21) });
                    System.out.println("maxLength set to 21");
                }
            }


            if (! editTextList.contains(editText)) editTextList.addLast(editText);

            if (isNew){ //Makes sure new exercises are created in edit mode
                holder.itemView.setBackground(MainActivity.customBorder);
                LinearLayout ll = holder.itemView.findViewById(R.id.exerciseLinearLayout);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) ll.getLayoutParams();
                params.setMargins(40,15,40,0);
                ll.setLayoutParams(params);
                editText.setTextColor(Color.parseColor("#878787"));
                editText.setEnabled(false);
                System.out.println("isNEw!");
            }


            editText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                public void onDestroyActionMode(ActionMode mode) {
                }

                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }
            });

            editText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longPressed = true;
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    RoutineActivity.attachHelper();
                    RoutineActivity.showEditingButtons();
                    for (int i = 0; i < editTextList.size(); i++){
                        editTextList.get(i).setSelection(0,0);
                        editTextList.get(i).setEnabled(false);
                    }
                    return false;
                }
            });

            //This is used for when an edittext is clicked on, OnClickListener does nothing
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus && ! longPressed) {
                        //RoutineActivity.saveButton.setVisibility(View.VISIBLE);
                        RoutineActivity.customLayoutManager.setScrollEnabled(false);
                        editText.setCursorVisible(true);
                        int tag = (int) editText.getTag();
                        for (int i = 0; i < editTextList.size(); i++){
                            editTextList.get(i).setLongClickable(false);
                            if (!editTextList.get(i).getTag().equals(tag)){
                                editTextList.get(i).setClickable(false);
                                RoutineActivity.hintColor = editTextList.get(i).getCurrentHintTextColor();
                                editTextList.get(i).setHintTextColor(Color.LTGRAY);
                            }
                        }


                    }
                }
            });

            editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    editText.setCursorVisible(false);
                    editText.setSelection(0,0);
                    for (int i = 0; i < editTextList.size(); i++){
                        editTextList.get(i).setLongClickable(true);
                        editTextList.get(i).setClickable(true);
                        editTextList.get(i).setTextColor(Color.BLACK);
                        editTextList.get(i).setHintTextColor(RoutineActivity.hintColor);
                        editTextList.get(i).clearFocus();
                    }
                    //editText.clearFocus();
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    RoutineActivity.saveData();
                    RoutineActivity.customLayoutManager.setScrollEnabled(true);
                    return false;
                }
            });
        }
        if (! exerciseItemViewList.contains(holder.itemView)){
            exerciseItemViewList.addLast(holder.itemView);
        }


    }



    @Override
    public int getItemCount() {
        return mExerciseList.size();
    }



    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        public final EditText[] exerciseItemView = new EditText[4];
        final ExerciseListAdapter mAdapter;
        final View itemView;

        public ExerciseViewHolder(@NonNull View itemView, ExerciseListAdapter adapter) {
            super(itemView);
            exerciseItemView[0] = itemView.findViewById(R.id.exercise_name_EditText);
            exerciseItemView[1] = itemView.findViewById(R.id.reps_EditText);
            exerciseItemView[2] = itemView.findViewById(R.id.sets_EditText);
            exerciseItemView[3] = itemView.findViewById(R.id.weight_EditText);
            this.mAdapter = adapter;
            this.itemView = itemView;
        }
    }
}
