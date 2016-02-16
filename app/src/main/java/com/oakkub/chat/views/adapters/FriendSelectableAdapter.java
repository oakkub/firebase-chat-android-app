package com.oakkub.chat.views.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.oakkub.chat.R;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;
import com.oakkub.chat.views.adapters.viewholders.SelectableFriendHolder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by OaKKuB on 12/25/2015.
 */
public class FriendSelectableAdapter extends RecyclerViewMultipleSelectionAdapter<UserInfo> implements Filterable {

    private static final String STATE_SELECTED_FILTERED_ITEMS = "state:selectedFilteredItems";

    private OnAdapterItemClick onAdapterItemClick;
    private ArrayList<UserInfo> filteredItems;

    private boolean hasFiltered;

    public FriendSelectableAdapter(OnAdapterItemClick onAdapterItemClick, boolean hasFiltered) {
        this.onAdapterItemClick = onAdapterItemClick;
        this.filteredItems = new ArrayList<>();
        this.hasFiltered = hasFiltered;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectable_friend_list, parent, false);
        return new SelectableFriendHolder(view, onAdapterItemClick);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        UserInfo userInfo = getItem(position);
        SelectableFriendHolder selectableFriendHolder = (SelectableFriendHolder) holder;

        selectableFriendHolder.profileImage.setImageURI(Uri.parse(userInfo.getProfileImageURL()));
        selectableFriendHolder.friendNameTextView.setText(userInfo.getDisplayName());

        selectableFriendHolder.friendCheckBox.setVisibility(onAdapterItemClick == null || !hasFiltered ? View.GONE : View.VISIBLE);
        selectableFriendHolder.friendCheckBox.setChecked(isSelected(userInfo.hashCode()));
    }

    @Override
    public int getItemCount() {
        return hasFiltered ? filteredItems.size() : super.getItemCount();
    }

    public int getPhysicalItemCount() {
        return super.getItemCount();
    }

    @Override
    public UserInfo getItem(int position) {
        if (hasFiltered) {
            if (position >= getItemCount()) return null;
            else if (position < 0) return null;
            else return filteredItems.get(position);
        } else {
            return super.getItem(position);
        }
    }

    @Override
    public boolean contains(UserInfo item) {
        if (hasFiltered) {
            return filteredItems.contains(item);
        } else {
            return super.contains(item);
        }
    }

    @Override
    public Filter getFilter() {
        return new UserInfoFilter(this, items);
    }

    public interface OnFilteredListener {
        void onFilterSuccess();

        void onFilterFailed();
    }

    private static class UserInfoFilter extends Filter {

        private final WeakReference<FriendSelectableAdapter> friendAdapter;
        private final ArrayList<UserInfo> originalList;
        private final ArrayList<UserInfo> filteredList;

        public UserInfoFilter(FriendSelectableAdapter friendSelectableAdapter,
                              ArrayList<UserInfo> originalList) {
            this.friendAdapter = new WeakReference<>(friendSelectableAdapter);
            this.originalList = new ArrayList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();

            String filterPattern = constraint.toString().toLowerCase().trim();

            if (filterPattern.length() == 0) {
                filteredList.addAll(originalList);
            } else {

                for (int i = 0, size = originalList.size(); i < size; i++) {
                    UserInfo userInfo = originalList.get(i);

                    if (userInfo.getDisplayName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(userInfo);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            FriendSelectableAdapter adapter = friendAdapter.get();

            if (adapter != null) {
                adapter.filteredItems.clear();
                adapter.filteredItems.addAll((ArrayList<UserInfo>) results.values);
                adapter.notifyDataSetChanged();
            }
        }

    }
}
