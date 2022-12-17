package ch.disappointment.WalkoutCompanion.ui.tracks;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.function.Consumer;

import ch.disappointment.WalkoutCompanion.R;
import ch.disappointment.WalkoutCompanion.api.ApiService;
import ch.disappointment.WalkoutCompanion.persistence.TracksDaoService;
import ch.disappointment.WalkoutCompanion.persistence.model.Track;
import ch.disappointment.WalkoutCompanion.ui.map.MapActivity;

public class TracksFragment extends Fragment {
    private View root;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private static int MAP_INTENT_REQ_CODE = 49;
    private TracksViewModel viewModel;
    private TracksDaoService tracksDaoService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_tracks, container, false);

        this.root = root;

        recyclerView = root.findViewById(R.id.tracksRecyclerView);
        recyclerView.setAdapter(new TracksAdapter(requireActivity()));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        floatingActionButton = root.findViewById(R.id.tracks_fab);
        floatingActionButton.setOnClickListener(view -> {
            showDialog((trackName) -> {
                Intent mapIntent = new Intent(requireActivity(), MapActivity.class);
                mapIntent.putExtra(MapActivity.EXTRA_KEY_TRACK_NAME, trackName);
                mapIntent.putExtra(MapActivity.EXTRA_KEY_TRACK_ID, (String) null);
                mapIntent.putExtra(MapActivity.EXTRA_KEY_MODE, MapActivity.OpenModes.NEW);
                startActivityForResult(mapIntent, MAP_INTENT_REQ_CODE);
            }, null);
        });

        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        viewModel = provider.get(TracksViewModel.class);

        tracksDaoService = new TracksDaoService(requireContext());
        ArrayList<Track> trackList = tracksDaoService.listTracks(requireContext(), ApiService.getInstance(requireContext()).getLoggedUser().getUsername());
        viewModel.setTracks(trackList);

        viewModel.getTracks().observe(requireActivity(), tracks -> {
            recyclerView.getAdapter().notifyDataSetChanged();
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        tracksDaoService = new TracksDaoService(requireContext());
        ArrayList<Track> trackList = tracksDaoService.listTracks(requireContext(), ApiService.getInstance(requireContext()).getLoggedUser().getUsername());
        viewModel.setTracks(trackList);
    }

    private void showDialog(Consumer<String> onOk, Runnable okCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Title");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newTrackName = input.getText().toString();
            onOk.accept(newTrackName);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            if (okCancel != null)
                okCancel.run();
        });

        builder.show();
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

            Intent mapIntent = new Intent(activity, MapActivity.class);
            mapIntent.putExtra(MapActivity.EXTRA_KEY_TRACK_NAME, t.getName());
            mapIntent.putExtra(MapActivity.EXTRA_KEY_TRACK_ID, t.getId());
            mapIntent.putExtra(MapActivity.EXTRA_KEY_MODE, MapActivity.OpenModes.VIEW);
            activity.startActivity(mapIntent);
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
        Instant end = t.endsAt();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy hh:mm");

        String startText = null;
        String endText = null;
        String description = null;

        if (start != null && end != null) {
            startText = dateFormat.format(Date.from(start));
            endText = dateFormat.format(Date.from(end));
            description = "From: " + startText + "\n" + "To: " + endText;
            // set description
        }

        viewHolder.getTextView().setText(t.getName());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        ViewModelProvider provider = new ViewModelProvider(activity);
        viewModel = provider.get(TracksViewModel.class);

        return viewModel.getTracks().getValue().size();
    }
}