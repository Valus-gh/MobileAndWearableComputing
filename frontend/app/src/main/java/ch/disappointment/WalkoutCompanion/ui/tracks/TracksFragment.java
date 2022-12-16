package ch.disappointment.WalkoutCompanion.ui.tracks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;

import ch.disappointment.WalkoutCompanion.R;
import ch.disappointment.WalkoutCompanion.persistence.model.Track;
import ch.disappointment.WalkoutCompanion.ui.map.MapActivity;
import ch.disappointment.WalkoutCompanion.ui.map.MapViewModel;

public class TracksFragment extends Fragment {
    private View root;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_tracks, container, false);

        this.root = root;

        recyclerView = root.findViewById(R.id.tracksRecyclerView);
        recyclerView.setAdapter(new TracksAdapter(requireActivity()));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        TracksViewModel viewModel = provider.get(TracksViewModel.class);

        viewModel.getTracks().observe(requireActivity(), tracks -> {
            recyclerView.getAdapter().notifyDataSetChanged();
        });

        return root;
    }

}


class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.ViewHolder> {

    private final FragmentActivity activity;
    private TracksViewModel viewModel;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (TextView) view.findViewById(R.id.textView);
        }

        public TextView getTextView() {
            return textView;
        }
    }


    public TracksAdapter(FragmentActivity activity) {
        this.activity = activity;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_tracklist_track, viewGroup, false);

        ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(clickedView -> {
            long index = holder.getLayoutPosition();

            ViewModelProvider provider = new ViewModelProvider(activity);
            viewModel = provider.get(TracksViewModel.class);

            Track t = viewModel.getTracks().getValue().get(Long.valueOf(index).intValue());

            Intent myIntent = new Intent(activity, MapActivity.class);
            myIntent.putExtra("trackId", t.getId());
            activity.startActivity(myIntent);
        });

        return holder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        ViewModelProvider provider = new ViewModelProvider(activity);
        viewModel = provider.get(TracksViewModel.class);

        Track t = viewModel.getTracks().getValue().get(position);

        Instant start = t.startsAt();
        Instant end = t.startsAt();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy hh:mm");

        String startText = dateFormat.format(Date.from(start));
        String endText = dateFormat.format(Date.from(end));

        String name = "From: " + startText + "\n" + "To: " + endText;

        viewHolder.getTextView().setText(name);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        ViewModelProvider provider = new ViewModelProvider(activity);
        viewModel = provider.get(TracksViewModel.class);

        return viewModel.getTracks().getValue().size();
    }
}