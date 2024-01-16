package com.jimboidin.headway;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.ContactsContract;
import android.view.ActionMode;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

public class NoteListAdapter  extends RecyclerView.Adapter<NoteListAdapter.NoteViewHolder> {
    private final LinkedList<String> mNoteList;
    private LayoutInflater mInflater;
    public static LinkedList<View> itemViewList = new LinkedList<>();
    public static boolean longPressed = false;
    public static boolean isNew = false;

    public NoteListAdapter(Context context, LinkedList<String> noteList) {
        mInflater = LayoutInflater.from(context);
        this.mNoteList = noteList;
    }

    @NonNull
    @Override
    public NoteListAdapter.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.notes_item, parent, false);
        return new NoteListAdapter.NoteViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteListAdapter.NoteViewHolder holder, int position) {
        holder.noteItem.setText(mNoteList.get(position));
        final EditText editText = holder.noteItem;

        if (! itemViewList.contains(holder.itemView)) itemViewList.addLast(holder.itemView);

        if (isNew){
            editText.clearFocus();
            editText.setEnabled(false);
        }



        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                for (int i = 0; i < itemViewList.size(); i++){
                    EditText editText1 = itemViewList.get(i).findViewById(R.id.note_edit_text);
                    editText1.setCursorVisible(false);
                    editText1.clearFocus();
                }
                RoutineActivity.saveNoteData();
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && ! longPressed) {
                    editText.setCursorVisible(true);
                }
            }
        });

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
                longPressed=true;
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                RoutineActivity.showEditingButtons();
                for (int i = 0; i < itemViewList.size(); i++){
                    EditText editText1 = itemViewList.get(i).findViewById(R.id.note_edit_text);
                    editText1.clearFocus();
                    editText1.setEnabled(false);
                }
                RoutineActivity.attachHelper();
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        public final EditText noteItem;
        final NoteListAdapter mAdapter;
        final View itemView;

        public NoteViewHolder(@NonNull View itemView, NoteListAdapter adapter) {
            super(itemView);
            this.itemView = itemView;
            this.noteItem = itemView.findViewById(R.id.note_edit_text);
            this.mAdapter = adapter;
        }
    }
}
