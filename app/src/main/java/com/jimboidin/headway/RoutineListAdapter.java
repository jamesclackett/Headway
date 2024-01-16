package com.jimboidin.headway;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

public class RoutineListAdapter extends RecyclerView.Adapter<RoutineListAdapter.RoutineViewHolder>{
    private final LinkedList<String[]> mRoutineList;
    private LayoutInflater mInflater;
    public static LinkedList<View> itemViewList = new LinkedList<>();


    public RoutineListAdapter(Context context, LinkedList<String[]> routineList) {
            mInflater = LayoutInflater.from(context);
            this.mRoutineList = routineList;
    }

    @NonNull
    @Override
    public RoutineListAdapter.RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.routinelist_item, parent, false);
        return new RoutineListAdapter.RoutineViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull final RoutineListAdapter.RoutineViewHolder holder, int position) {
        holder.itemView.setBackground(MainActivity.bottomBorderRoutine);

        if (MainActivity.longPressed){
            System.out.println("Longpressed");
            holder.editRoutineName.setVisibility(View.VISIBLE);
            holder.editRoutineName.setColorFilter(Color.parseColor("#8c626c"), PorterDuff.Mode.SRC_IN);
            holder.itemView.setBackground(MainActivity.customBorder);
            holder.buttonItem.setTextColor(Color.parseColor("#9c9c9c"));

            LinearLayout ll = holder.itemView.findViewById(R.id.routineLinearLayout);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) ll.getLayoutParams();
            params.setMargins(40,15,40,0);
            if (MainActivity.isNew){
                ll.setTranslationY(10);
                MainActivity.isNew = false;
            }
        }

        for (int i = 0; i < mRoutineList.get(position).length; i++){
            final String mCurrent = mRoutineList.get(position)[0];
            final Button button = holder.buttonItem;
            final String id = mRoutineList.get(position)[1];
            final ImageView editRoutineName = holder.editRoutineName;

            button.setText(mCurrent);
            button.setTag(id);
            editRoutineName.setTag(id);

            button.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    MainActivity.longPressed = true;
                    MainActivity.attachHelper();
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    MainActivity.showAddRoutineButton();
                    for (int i = 0; i < itemViewList.size(); i++){
                        LinearLayout ll = itemViewList.get(i).findViewById(R.id.routineLinearLayout);
                        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) ll.getLayoutParams();
                        ll.animate().translationY(10).setDuration(250);
                        params.setMargins(40,15,40,0);

                        ImageView editRoutineButton = itemViewList.get(i).findViewById(R.id.edit_routine_name_button);
                        editRoutineButton.setVisibility(View.VISIBLE);
                        editRoutineButton.setColorFilter(Color.parseColor("#8c626c"), PorterDuff.Mode.SRC_IN);
                        itemViewList.get(i).setBackground(MainActivity.customBorderRoutine);
                        Button routineButton = itemViewList.get(i).findViewById(R.id.routine_item_button);
                        routineButton.setTextColor(Color.parseColor("#9c9c9c"));
                    }

                    return false;
                }
            });


        }
        if (! itemViewList.contains(holder.itemView)){
            itemViewList.addLast(holder.itemView);
        }

    }

    @Override
    public int getItemCount() {
        return mRoutineList.size();
    }


    class RoutineViewHolder extends RecyclerView.ViewHolder {
        public final Button buttonItem;
        public final ImageView editRoutineName;
        final RoutineListAdapter mAdapter;
        final View itemView;

        public RoutineViewHolder(@NonNull View itemView, RoutineListAdapter adapter) {
            super(itemView);
            this.itemView = itemView;
            this.editRoutineName = itemView.findViewById(R.id.edit_routine_name_button);
            this.buttonItem = itemView.findViewById(R.id.routine_item_button);
            this.mAdapter = adapter;
        }
    }
}
