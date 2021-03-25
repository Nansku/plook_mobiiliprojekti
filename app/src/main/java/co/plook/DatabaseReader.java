package co.plook;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;

public class DatabaseReader
{
    private FirebaseFirestore db;
    private OnLoadedListener listener;

    public DatabaseReader()
    {
        db = FirebaseFirestore.getInstance();
    }

    //WIP logic that determines what the type of queried field is

    public void loadCollection(String collectionPath, String field, String[] query)
    {
        CollectionReference collRef = db.collection(collectionPath);
        Query q = collRef.whereArrayContainsAny(field, Arrays.asList(query));

        queryData(q);
    }

    public void loadCollection(String collectionPath, String field, String query)
    {
        CollectionReference collRef = db.collection(collectionPath);
        Query q = collRef.whereArrayContains(field, query);

        queryData(q);
    }

    public void loadCollection(String collectionPath)
    {
        CollectionReference collRef = db.collection(collectionPath);
        Query q = collRef;

        queryData(q);
    }

    public void findById(String collectionPath, String id)
    {
        CollectionReference collRef = db.collection(collectionPath);
        Query q = collRef.whereEqualTo("__name__", id);

        queryData(q);
    }

    private void queryData(Query q)
    {
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful())
                {
                    listener.onLoaded(task.getResult());
                }
                else
                    listener.onFailure();
            }
        });
    }

    public interface OnLoadedListener
    {
        void onLoaded(QuerySnapshot documentSnapshots);
        void onFailure();
    }

    public void setOnLoadedListener(OnLoadedListener eventListener)
    {
        listener = eventListener;
    }


}
