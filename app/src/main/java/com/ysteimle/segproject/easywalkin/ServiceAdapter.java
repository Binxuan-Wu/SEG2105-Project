package com.ysteimle.segproject.easywalkin;

/**
 * Adapter for recycler view showing a list of services.
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
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.List;


public class ServiceAdapter extends ListAdapter<Service, ServiceAdapter.ServiceViewHolder> {
    // Don't think this is necessary:
    //List<Service> services;
    
    public interface OnServiceClickListener {
        void onServiceClick(Service service);
    }
    
    OnServiceClickListener serviceListener;

    // provide reference to the views within a data item
    public class ServiceViewHolder extends RecyclerView.ViewHolder {
        // The Service object that is displayed is also saved here so that we can use the
        // id in the onClick method of the Edit button
        public Service mService;
        // The holder must contain a member variable for each view in the list item
        public TextView nameView;
        public TextView providerView;
        public TextView descriptionView;
        public Button editBtn;

        // Create a constructor that takes the item view as input and finds all the subviews
        public ServiceViewHolder(View itemView) {
            // Store itemView as a public final variable that we can use to access the context
            // from any ServiceViewHolder instance
            super(itemView);

            nameView = (TextView) itemView.findViewById(R.id.serviceNameView);
            providerView = (TextView) itemView.findViewById(R.id.serviceProviderView);
            descriptionView = (TextView) itemView.findViewById(R.id.serviceDescriptionView);
            editBtn = (Button) itemView.findViewById(R.id.serviceEditBtn);
        }
        
        public void bind(final Service service, final OnServiceClickListener listener) {
            nameView.setText(service.name);
            providerView.setText("Provider: " + service.provider);
            if (service.description.isEmpty()) {
                // If there is no description for the service, hide the corresponding text view
                descriptionView.setVisibility(View.GONE);
            } else {
                descriptionView.setText("Description: " + service.description);
            }

            // Set service in viewHolder to be the one at this position
            // Then, viewHolder knows all the info in the service and in particular it's ID,
            // which can then be used in the onClick method of the Edit button to find the service
            // in the database
            mService = service;
            
            // Set up on click method
            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onServiceClick(service);
                }
            });
            
        }
    }
    

    // Declare item callback
    public static final DiffUtil.ItemCallback<Service> service_diff_callback =
            new DiffUtil.ItemCallback<Service>() {
                @Override
                public boolean areItemsTheSame(@NonNull Service oldItem, @NonNull Service newItem) {
                    // Since identifiers are unique, if they match, then we have the same item.
                    return oldItem.id.equals(newItem.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Service oldItem, @NonNull Service newItem) {
                    return (oldItem.name.equals(newItem.name) &&
                            oldItem.provider.equals(newItem.provider) &&
                            oldItem.description.equals(newItem.description));
                }
            };

    //Constructors
    public ServiceAdapter () {
        super(service_diff_callback);
    }

    public ServiceAdapter (OnServiceClickListener listener) {
        super(service_diff_callback);
        this.serviceListener = listener;
    }

    @Override
    public ServiceAdapter.ServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        
        // Inflate the service list layout
        View serviceView = inflater.inflate(R.layout.service_list_item, parent, false);
        
        // Return a new viewholder instance
        ServiceViewHolder serviceViewHolder = new ServiceViewHolder(serviceView);
        return serviceViewHolder;
    }
    
    @Override
    public void onBindViewHolder (ServiceAdapter.ServiceViewHolder viewHolder, int position) {
        // Get data model
        Service service = getItem(position);
        
        // Set item views based on data model
        TextView nameView = viewHolder.nameView;
        nameView.setText(service.name);
        TextView providerView = viewHolder.providerView;
        providerView.setText("Provider: " + service.provider);
        TextView descriptionView = viewHolder.descriptionView;
        if (service.description.isEmpty()) {
            // If there is no description for the service, hide the corresponding text view
            descriptionView.setVisibility(View.GONE);
        } else {
            descriptionView.setText("Description: " + service.description);
        }
        
        // Set service in viewHolder to be the one at this position
        // Then, viewHolder knows all the info in the service and in particular it's ID,
        // which can then be used in the onClick method of the Edit button to find the service
        // in the database
        viewHolder.mService = service;
        
        viewHolder.bind(service,serviceListener);
    }

}
