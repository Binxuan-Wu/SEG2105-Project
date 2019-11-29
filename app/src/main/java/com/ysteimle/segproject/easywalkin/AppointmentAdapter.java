package com.ysteimle.segproject.easywalkin;

/*
 * Adapter for recycler view showing a list of scheduled appointments.
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

public class AppointmentAdapter extends ListAdapter<Appointment,AppointmentAdapter.AppointmentViewHolder> {

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    private OnAppointmentClickListener appointmentListener;

    // provide reference to the views within a data item
    class AppointmentViewHolder extends RecyclerView.ViewHolder {
        // The Appointment object that is displayed is also saved here so that we can use the
        // id in the onClick method of the action button
        Appointment mAppointment;
        // The holder must contain a member variable for each view in the list item
        TextView dateTimeView;
        TextView clinicInfoView;
        Button actionBtn;

        // Create a constructor that takes the item view as input and finds all the subviews
        AppointmentViewHolder(View itemView) {
            // Store itemView as a public final variable that we can use to access the context
            // from any AppointmentViewHolder instance
            super(itemView);

            dateTimeView = (TextView) itemView.findViewById(R.id.appointmentDateTimeView);
            clinicInfoView = (TextView) itemView.findViewById(R.id.appointmentClinicInfoView);
            actionBtn = (Button) itemView.findViewById(R.id.appointmentActionBtn);
        }

        void bind(final Appointment appointment, final OnAppointmentClickListener listener) {
            dateTimeView.setText(appointment.dateTimePrintFormat());
            clinicInfoView.setText(appointment.clinicInfo);

            // Set appointment in viewHolder to be the one at this position
            // Then, viewHolder knows all the info in the appointment and in particular it's ID (and the clinic ID),
            // which can then be used in the onClick method of the action button to find the appointment
            // in the database
            mAppointment = appointment;

            // Set up on click method
            actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onAppointmentClick(appointment);
                }
            });

        }
    }

    // Declare item callback
    private static final DiffUtil.ItemCallback<Appointment> appointment_diff_callback =
            new DiffUtil.ItemCallback<Appointment>() {
                @Override
                public boolean areItemsTheSame(@NonNull Appointment oldItem, @NonNull Appointment newItem) {
                    // Since identifiers are unique, if they match, then we have the same item.
                    return oldItem.instanceId.equals(newItem.instanceId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Appointment oldItem, @NonNull Appointment newItem) {
                    return oldItem.instanceId.equals(newItem.instanceId)
                            && oldItem.clinicInfo.equals(newItem.clinicInfo)
                            && oldItem.dateTimePrintFormat().equals(newItem.dateTimePrintFormat());
                }
            };

    //Constructors
    public AppointmentAdapter () {
        super(appointment_diff_callback);
    }

    AppointmentAdapter (OnAppointmentClickListener listener) {
        super(appointment_diff_callback);
        this.appointmentListener = listener;
    }

    @NonNull
    @Override
    public AppointmentAdapter.AppointmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the service list layout
        View appointmentView = inflater.inflate(R.layout.appointment_list_item, parent, false);

        // Return a new viewholder instance
        return new AppointmentViewHolder(appointmentView);
    }

    @Override
    public void onBindViewHolder (AppointmentAdapter.AppointmentViewHolder viewHolder, int position) {
        // Get data model
        Appointment appointment = getItem(position);

        // set up text view and onClickListener
        viewHolder.bind(appointment,appointmentListener);
    }
}
