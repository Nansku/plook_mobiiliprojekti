package co.plook;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DatabaseReader
{
    private FirebaseFirestore db;
    private OnLoadedListener listener;

    private String userID;

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
        Query q = collRef.orderBy("time", Query.Direction.ASCENDING);

        queryData(type, q);
    }

    private void queryData(CollectionType type, Query q)
    {
        Task t1 = q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
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
        void onLoadedCommentators(Map<String, String> names);
        void onFailure();
    }

    public void setOnLoadedListener(OnLoadedListener eventListener)
    {
        listener = eventListener;
    }


    //1. request: find the post object AND comment section using postID
    //2. request: get every commentators display name for UI using userIDs
    public Task loadComments(String postID)
    {
        Task getPostTask = db.collection("posts").document(postID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {}
        });

        Task getCommentsTask = db.collection("comment_sections").document(postID).collection("comments").orderBy("time", Query.Direction.ASCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {}
        });

        Task parallelTask = Tasks.whenAllSuccess(getPostTask, getCommentsTask).addOnSuccessListener(new OnSuccessListener<List<Object>>()
        {
            @Override
            public void onSuccess(List<Object> objects)
            {
                //listener.onLoaded(CollectionType.comment_section, objects);
                loadCommentators(objects);
            }
        });
        return parallelTask;
    }

    //search for all commentator userIDs, put them in a list and make .length amount of pipelined firestorage requests
    private void loadCommentators(List<Object> objects)
    {
        QuerySnapshot querySnapshot = (QuerySnapshot) objects.get(1);
        //Log.d("OBJECTS", );
        List<DocumentSnapshot> snapshots = querySnapshot.getDocuments();
        ArrayList<String> userIDs = new ArrayList<>();

        //loop through userIDs and get a list of unique names
        for (DocumentSnapshot snapshot : snapshots)
        {
            String userID = snapshot.get("userID").toString();
            if (!userIDs.contains(userID))
                userIDs.add(userID);
        }

        Task[] tasks = new Task[userIDs.size()];

        for (int i = 0; i < userIDs.size(); i++)
        {
            Task userNameTask = db.collection("users").document(userIDs.get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
            {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task)
                {

                }
            });
            tasks[i] = userNameTask;
        }

        Task parallelTask = Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>()
        {
            @Override
            public void onSuccess(List<Object> objects)
            {
                List<DocumentSnapshot> userDocs = (List<DocumentSnapshot>)(List<?>) objects;
                Map<String, String> usernamePairs = new HashMap<>();
                for (int i = 0; i < userDocs.size(); i++)
                {
                    usernamePairs.put(userIDs.get(i), userDocs.get(i).get("name").toString());
                }

                listener.onLoadedCommentators(usernamePairs);
            }
        });
    }





    public class TaskRunner implements Runnable
    {
        Task[] tasks;

        private TaskRunner(Task[] t)
        {
            tasks = t;
        }


        @Override
        public void run()
        {
            Task combinedTask = Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                @Override
                public void onSuccess(List<Object> objects)
                {
                    Log.d("Debuggaus", objects.toString());
                }
            });
        }
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
