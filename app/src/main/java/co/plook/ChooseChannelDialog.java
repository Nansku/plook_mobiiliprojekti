package co.plook;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.List;
import java.util.concurrent.TimeoutException;

public class ChooseChannelDialog extends DialogFragment {

    private static final String TAG = "ChooseChannelDialog";
    TextView followedChannels, allChannels;
    String userID;
    DatabaseReader dbReader;
    private View view;
    private List<String> followed_channels;
    ClickListener clickListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_choose_channel, container, false);
        followedChannels = view.findViewById(R.id.followedChannels);
        allChannels = view.findViewById(R.id.allChannels);
        dbReader = new DatabaseReader();

        Bundle getUserInfo = getArguments();
        assert getUserInfo != null;
        userID = getUserInfo.getString("userID");

        loadChannels();

        return view;
    }

    private void loadChannels()
    {
        // Get followed channels
        dbReader.findDocumentByID("user_contacts", userID).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                ViewGroup content = view.findViewById(R.id.personal_channels_followed);

                followed_channels = (List<String>) document.get("followed_channels");

                String[] channelIDs = followed_channels.toArray(new String[0]);

                if (channelIDs.length > 0)
                {
                    // Get followed channels
                    dbReader.findDocumentsWhereIn("channels", "__name__", channelIDs).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for (DocumentSnapshot document : task.getResult()) {
                                populateChannelsList(document, content);
                            }
                        }
                    });
                }

                // Get all channels
                Query query = dbReader.db.collection("channels");
                dbReader.findDocuments(query).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        ViewGroup content = view.findViewById(R.id.personal_channels_all);

                        for (DocumentSnapshot document : task.getResult())
                        {
                            if (followed_channels == null || !followed_channels.contains(document.getId()))
                                populateChannelsList(document, content);
                        }
                    }
                });
            }
        });
    }

    private void populateChannelsList(DocumentSnapshot channelData, ViewGroup content)
    {
        View child = getLayoutInflater().inflate(R.layout.layout_channel_browser_button, content, false);
        content.addView(child);

        // Channel name
        TextView textView_name = child.findViewById(R.id.channel_name);
        String channelName = channelData.getString("name");
        textView_name.setText(channelName);

        // Channel follower count
        List<String> followerIDs = (List<String>) channelData.get("followers");
        int followerCount = followerIDs == null ? 0 : followerIDs.size();
        TextView textView_channelFollowers = child.findViewById(R.id.channel_follower_count);
        textView_channelFollowers.setText(followerCount + " FOLLOWERS");

        child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onChannelClicked(channelData.getId(), channelName);
            }
        });
    }

    public interface ClickListener
    {
        void onChannelClicked(String clickedChannelID, String clickedChannelName);
    }

    public void setOnChannelClickedListener(ClickListener clickListener)
    {
        this.clickListener = clickListener;
    }
}
