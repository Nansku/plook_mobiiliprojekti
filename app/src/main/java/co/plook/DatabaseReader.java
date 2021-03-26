package co.plook;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.Collection;

public class DatabaseReader
{
    private FirebaseFirestore db;
    private OnLoadedListener listener;

    public DatabaseReader()
    {
        db = FirebaseFirestore.getInstance();
    }

    //WIP logic that determines what the type of queried field is

    public void loadCollection(CollectionType type, String collectionPath, String field, String[] query)
    {
        CollectionReference collRef = db.collection(collectionPath);
        Query q = collRef.whereArrayContainsAny(field, Arrays.asList(query));

        queryData(type, q);
    }

    public void loadCollection(CollectionType type, String collectionPath, String field, String query)
    {
        CollectionReference collRef = db.collection(collectionPath);
        Query q = collRef.whereArrayContains(field, query);

        queryData(type, q);
    }

    public void loadCollection(CollectionType type, String collectionPath)
    {
        CollectionReference collRef = db.collection(collectionPath);
        Query q = collRef;

        queryData(type, q);
    }

    public void findById(CollectionType type, String collectionPath, String documentId)
    {
        CollectionReference collRef = db.collection(collectionPath);
        Query q = collRef.whereEqualTo("__name__", documentId);

        queryData(type, q);
    }

    public void findSubcollection(CollectionType type, String collectionPath, String documentId, String subcollectionPath)
    {
        CollectionReference collRef = db.collection(collectionPath).document(documentId).collection(subcollectionPath);
        Query q = collRef;

        queryData(type, q);
    }

    private void queryData(CollectionType type, Query q)
    {
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful())
                {
                    listener.onLoaded(type, task.getResult());
                }
                else
                    listener.onFailure();
            }
        });


    }

    public interface OnLoadedListener
    {
        void onLoaded(CollectionType type, QuerySnapshot documentSnapshots);
        void onFailure();
    }

    public void setOnLoadedListener(OnLoadedListener eventListener)
    {
        listener = eventListener;
    }
}

enum CollectionType
{
    comment_section,
    post,
    tag,
    user,
    vote
}
