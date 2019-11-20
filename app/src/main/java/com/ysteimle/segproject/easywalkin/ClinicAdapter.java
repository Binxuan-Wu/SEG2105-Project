package com.ysteimle.segproject.easywalkin;

/*
 * Adapter for recycler view showing a list of clinics.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class ClinicAdapter extends ListAdapter<Clinic, ClinicAdapter.ClinicViewHolder>{

    public interface OnClinicClickListener {
        void onClinicClick(Clinic clinic);
    }

    private OnClinicClickListener clinicListener;

    // This determines if the action button is for joining or selecting a clinic
    private static final int joinClinic = 0;
    private static final int selectClinic = 1;
    private int version;

    // provide reference to the views within a data item
    class ClinicViewHolder extends RecyclerView.ViewHolder {
        // The Clinic object that is displayed is also saved here so that we can use the
        // id in the onClick method of the Action button
        Clinic mClinic;
        // The holder must contain a member variable for each view in the list item
        TextView nameView;
        TextView phoneView;
        TextView addressView;
        Button actionBtn;

        // Create a constructor that takes the item view as input and finds all the subviews
        ClinicViewHolder(View itemView) {
            // Store itemView as a public final variable that we can use to access the context
            // from any ClinicViewHolder instance
            super(itemView);

            nameView = (TextView) itemView.findViewById(R.id.clinicSearchNameView);
            phoneView = (TextView) itemView.findViewById(R.id.clinicSearchPhoneView);
            addressView = (TextView) itemView.findViewById(R.id.clinicSearchAddressView);
            actionBtn = (Button) itemView.findViewById(R.id.clinicSearchActionBtn);
        }

        void bind(final Clinic clinic, final OnClinicClickListener listener) {
            nameView.setText(clinic.clinicName);
            addressView.setText(clinic.clinicAddress.printFormat());
            phoneView.setText(clinic.phone);
            if (version == joinClinic) {
                actionBtn.setText(R.string.Join);
            } else {
                actionBtn.setText(R.string.Select);
            }


            // Set clinic in viewHolder to be the one at this position
            // Then, viewHolder knows all the info in the clinic and in particular it's ID,
            // which can then be used in the onClick method of the Action button to find the clinic
            // in the database
            mClinic = clinic;

            // Set up on click method
            actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClinicClick(clinic);
                }
            });

        }
    }

    // Declare item callback
    private static final DiffUtil.ItemCallback<Clinic> clinic_diff_callback =
            new DiffUtil.ItemCallback<Clinic>() {
                @Override
                public boolean areItemsTheSame(@NonNull Clinic oldItem, @NonNull Clinic newItem) {
                    // Since identifiers are unique, if they match, then we have the same item.
                    return oldItem.clinicId.equals(newItem.clinicId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Clinic oldItem, @NonNull Clinic newItem) {
                    return oldItem.equals(newItem);
                }
            };

    //Constructors
    public ClinicAdapter () {
        super(clinic_diff_callback);
    }

    ClinicAdapter (int version, OnClinicClickListener listener) {
        super(clinic_diff_callback);
        this.clinicListener = listener;
        this.version = version;
    }

    @NonNull
    @Override
    public ClinicAdapter.ClinicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the clinic list layout
        View clinicView = inflater.inflate(R.layout.clinic_list_item, parent, false);

        // Return a new viewholder instance
        return new ClinicViewHolder(clinicView);
    }

    @Override
    public void onBindViewHolder (ClinicAdapter.ClinicViewHolder viewHolder, int position) {
        // Get data model
        Clinic clinic = getItem(position);

        // set up text view and onClickListener
        viewHolder.bind(clinic,clinicListener);
    }
}
