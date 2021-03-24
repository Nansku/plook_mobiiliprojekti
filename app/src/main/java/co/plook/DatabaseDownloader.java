package co.plook;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class DatabaseDownloader
{
    private FirebaseFirestore db;
    private OnLoadedListener listener;

    public DatabaseDownloader()
    {
        db = FirebaseFirestore.getInstance();
    }

    public void loadCollection(String collectionPath, String field, String query)
    {
        ArrayList<Map> mapArrayList = new ArrayList<>();

        db.collection(collectionPath)
                .whereEqualTo(field, query)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult())
                        mapArrayList.add(document.getData());

                    Map[] maps = new Map[mapArrayList.size()];
                    mapArrayList.toArray(maps);
                    listener.onLoaded(maps);
                }
                else
                    listener.onFailure();
            }
        });
    }

    public interface OnLoadedListener
    {
        void onLoaded(Object[] o);
        void onFailure();
    }

    public void setOnLoadedListener(OnLoadedListener eventListener)
    {
        listener = eventListener;
    }
}
