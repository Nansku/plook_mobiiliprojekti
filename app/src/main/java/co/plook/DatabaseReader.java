package co.plook;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;

public class DatabaseReader
{
    public FirebaseFirestore db;

    private String userID;

    public DatabaseReader() {
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
        Query q = collRef.whereArrayContains(field, criteria);

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
}