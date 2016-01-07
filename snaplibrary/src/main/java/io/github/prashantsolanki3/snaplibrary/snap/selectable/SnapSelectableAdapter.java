package io.github.prashantsolanki3.snaplibrary.snap.selectable;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.prashantsolanki3.snaplibrary.snap.AbstractSnapMultiAdapter;
import io.github.prashantsolanki3.snaplibrary.snap.SnapLayoutWrapper;
import io.github.prashantsolanki3.snaplibrary.snap.SnapViewHolder;
import io.github.prashantsolanki3.snaplibrary.snap.recycler.SnapOnItemClickListener;
import io.github.prashantsolanki3.snaplibrary.snap.utils.UtilsLayoutWrapper;

/**
 * Package io.github.prashantsolanki3.snaplibrary.snap.selectable
 * <p>
 * Created by Prashant on 1/5/2016.
 * <p>
 * Email: solankisrp2@gmail.com
 * Github: @prashantsolanki3
 */
public class SnapSelectableAdapter<T, VH extends SnapSelectableViewHolder<T>> extends AbstractSnapMultiAdapter<T> {

    public List<T> selectedItems;
    public SelectionType selectionType;
    public boolean selectionEnabled = false;
    public int selectionLimit = Integer.MAX_VALUE;
    SelectionListener selectionListener = null;

    /*
    *
    * Adapter Methods
    *
    * */

    /**
     * @param context Context.
     * @param wrapper SnapLayoutWrapper
     */
    public SnapSelectableAdapter(@NonNull Context context, @NonNull SnapSelectableLayoutWrapper wrapper, SelectionType selectionType) {
        super(context, new ArrayList<SnapLayoutWrapper>(Collections.singletonList(wrapper)));
        this.selectionType = selectionType;

        selectedItems = new ArrayList<>();

        switch (selectionType) {
            case SINGLE:
                selectionEnabled = true;
                break;
            case MULTIPLE:
                selectionEnabled = true;
                break;
            case MULTIPLE_ON_LONG_PRESS:
                longPressHandler();
                selectionEnabled = false;
                break;
            default:
                throw new IllegalArgumentException("Selection type not Supported");
        }
    }

    @Override
    public SnapViewHolder onCreateViewHolder(ViewGroup parent, int viewType) throws RuntimeException {
        SnapLayoutWrapper wrapper = UtilsLayoutWrapper.getWrapperFromType(getLayoutWrappers(), viewType);

        final ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(wrapper.getLayoutId(), parent, false);

        try {
            Constructor e = wrapper.getViewHolder().getConstructor(View.class, Context.class, SnapSelectableAdapter.class);
            //noinspection unchecked
            return (SnapViewHolder) e.newInstance(view, getContext(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final SnapViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        SnapSelectableViewHolder<T> viewHolder = (SnapSelectableViewHolder<T>) holder;


        if (UtilsLayoutWrapper.isViewHolderSelectable(getLayoutWrappers(), viewHolder) && selectionEnabled) {

            viewHolder.onSelectionEnabled(viewHolder, viewHolder.getItemData(), position);

            if (isSelected(viewHolder.getItemData()))
                viewHolder.onSelected(viewHolder, viewHolder.getItemData(), position);
            else
                viewHolder.onDeselected(viewHolder, viewHolder.getItemData(), position);
        }
    }

    //TODO: Long press to enable MULTIPLE_ON_LONG_PRESS Selection Mode. Disable when No items selected.
    //TODO: MULTIPLE, SINGLE Selection: Always enabled.


    /*
    *
    * Selection Methods
    *
    */

    /**
     * Toggle GalleryItems Selection
     * returns True if Item is added.
     * and False if item is removed.
     */
    public boolean toggleSelection(T selection, int pos) {
        if (selectedItems.contains(selection)) {
            deselectItem(selection, pos);
            return false;
        } else {
            selectItem(selection, pos);
            return true;
        }
    }

    /**
     * @return true if the item is selected and false is item is not selected or the selection limit is reached.
     */
    public boolean selectItem(T selection, int pos) {

        if (getSelectionLimit() == selectedItems.size())
            return false;

        selectedItems.add(selection);
        Log.d("selected", pos + " " + selection.toString());
        notifyItemChanged(pos);
        return true;
    }

    public boolean deselectItem(T selection, int pos) {
        selectedItems.remove(selection);
        Log.d("Deselected", pos + " " + selection.toString());
        notifyItemChanged(pos);
        if (selectionType == SelectionType.MULTIPLE_ON_LONG_PRESS && selectedItems.isEmpty()) {
            setSelectionEnabled(false);
            return false;
        }
        return true;
    }

    void longPressHandler() {
        setOnItemClickListener(new SnapOnItemClickListener() {
            @Override
            public void onItemClick(SnapViewHolder viewHolder, View view, int position) {
                if (isSelectionEnabled())
                    toggleSelection((T) viewHolder.getItemData(), position);
            }

            @Override
            public void onItemLongPress(SnapViewHolder viewHolder, View view, int position) {
                setSelectionEnabled(true);
                toggleSelection((T) viewHolder.getItemData(), position);
            }
        });
    }

    /**
     * Handle Contextual ActionBar
     */
    public void setOnSelectionListener(SelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    /*
    *
    * Getters and Setters
    *
    * */

    public void setSelectionEnabled(boolean selectionEnabled) {
        this.selectionEnabled = selectionEnabled;
        if (selectionListener != null)
            if (selectionEnabled)
                selectionListener.onSelectionModeEnabled(selectionType);
            else
                selectionListener.onSelectionModeDisabled(selectionType);
        notifyDataSetChanged();
    }

    public boolean isSelectionEnabled() {
        return selectionEnabled;
    }

    public boolean isSelected(T item) {
        return selectedItems.contains(item);
    }

    public int getSelectionLimit() {
        return selectionLimit;
    }

    public void setSelectionLimit(int selectionLimit) {
        this.selectionLimit = selectionLimit;
    }


    /*
    *
    * Enum and listeners
    *
    * */

    public enum SelectionType {
        MULTIPLE, MULTIPLE_ON_LONG_PRESS, SINGLE
    }

    public interface SelectionListener {
        void onSelectionModeEnabled(SelectionType selectionType);

        void onSelectionModeDisabled(SelectionType selectionType);
    }

}