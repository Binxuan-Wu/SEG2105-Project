package com.ysteimle.segproject.easywalkin;

/*
 * Adapter for recycler view showing a list of users (could be either patients or employees).
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;


public class UserAdapter extends ListAdapter<User, UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user, String databasePath);
    }

    private OnUserClickListener userListener;
    private String userDatabasePath;

    // provide reference to the views within a data item
    class UserViewHolder extends RecyclerView.ViewHolder {
        // The User object that is displayed is also saved here so that we can use the
        // id in the onClick method of the Edit button
        User mUser;
        // The holder must contain a member variable for each view in the list item
        TextView nameView;
        TextView emailView;
        TextView clinicView;

        // Create a constructor that takes the item view as input and finds all the subviews
        UserViewHolder(View itemView) {
            // Store itemView as a public final variable that we can use to access the context
            // from any ServiceViewHolder instance
            super(itemView);

            nameView = (TextView) itemView.findViewById(R.id.userNameView);
            emailView = (TextView) itemView.findViewById(R.id.userEmailView);
            clinicView = (TextView) itemView.findViewById(R.id.userClinicView);
        }

        void bind(final User user, final OnUserClickListener listener) {
            nameView.setText(String.format("%s %s", user.firstName,user.lastName));
            emailView.setText(user.email);

            // For now, make sure clinic text view is gone; could add it later if we want to
            // display which clinic an employee works at
            clinicView.setVisibility(View.GONE);

            // Set user in viewHolder to be the one at this position
            // Then, viewHolder knows all the info in the user and in particular it's ID,
            // which can then be used in the onClick method
            mUser = user;

            // Set up on click method
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onUserClick(user, userDatabasePath);
                }
            });

        }
    }

    // Declare item callback
    private static final DiffUtil.ItemCallback<User> user_diff_callback =
            new DiffUtil.ItemCallback<User>() {
                @Override
                public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                    // Since identifiers are unique, if they match, then we have the same item.
                    return oldItem.id.equals(newItem.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                    return (oldItem.equals(newItem));
                }
            };

    //Constructors
    public UserAdapter () {
        super(user_diff_callback);
    }

    public UserAdapter (String databasePath, OnUserClickListener listener) {
        super(user_diff_callback);
        this.userListener = listener;
        this.userDatabasePath = databasePath;
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the service list layout
        View userView = inflater.inflate(R.layout.user_list_item, parent, false);

        // Return a new viewholder instance
        return new UserViewHolder(userView);
    }

    @Override
    public void onBindViewHolder (UserAdapter.UserViewHolder viewHolder, int position) {
        // Get data model
        User user = getItem(position);

        // set up text views and onClickListener
        viewHolder.bind(user,userListener);
    }
}
