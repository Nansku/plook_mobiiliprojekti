package co.plook;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DatabaseReader
{
    public FirebaseFirestore db;

    //private String userID;


    public DatabaseReader() 
    {
        db = FirebaseFirestore.getInstance();
    }

    //WIP logic that determines what the type of queried field is

    public Task<QuerySnapshot> findDocuments(Query q)
    {
        return q.get().addOnCompleteListener(task -> { });
    }

    //find documents in collectionPath that have one of 'criteria' lists strings
    public Task<QuerySnapshot> findDocuments(String collectionPath, String field, String[] criteria)
    {
        CollectionReference collRef = db.collection(collectionPath);
        Query q = collRef.whereArrayContainsAny(field, Arrays.asList(criteria));

        return q.get().addOnCompleteListener(task -> { });
    }

    //find documents in collectionPath that have the string criteria in specified field
    public Task<QuerySnapshot> findDocuments(String collectionPath, String field, String criteria)
    {
        CollectionReference collRef = db.collection(collectionPath);
        Query q = collRef.whereEqualTo(field, criteria);

        return q.get().addOnCompleteListener(task -> { });
    }

    //
    public Task<QuerySnapshot> findDocumentByID(String collectionPath, String documentId)
    {
        CollectionReference collRef = db.collection(collectionPath);
        Query q = collRef.whereEqualTo("__name__", documentId);

        return q.get().addOnCompleteListener(task -> { });
    }

    //find subcollections! currently the only subcollection exists in comment_sections
    public Task<QuerySnapshot> findSubcollection(String collectionPath, String documentId, String subcollectionPath)
    {
        CollectionReference collRef = db.collection(collectionPath).document(documentId).collection(subcollectionPath);
        Query q = collRef.orderBy("time", Query.Direction.ASCENDING);

        return q.get().addOnCompleteListener(task -> { });
    }

    //siirrä database luokkaan ja muuta parametriksi ArrayList<String>
    public Task<List<Object>> requestNicknames(ArrayList<String> userIDs)
    {
        Task[] tasks = new Task[userIDs.size()];

        for (int i = 0; i < userIDs.size(); i++)
        {
            Task<QuerySnapshot> userNameTask = findDocumentByID("users", userIDs.get(i))
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task)
                        {

                        }
                    });
            tasks[i] = userNameTask;
        }

        return Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>()
        {
            @Override
            public void onSuccess(List<Object> objects)
            {

            }
        });
    }
}