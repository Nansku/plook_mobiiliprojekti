package co.plook;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;

public class DatabaseDownloader {

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference gsReference;

    private String imageUri;

    public DatabaseDownloader()
    {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    //WIP get specific fields from documents
    public Post[] getPosts()
    {
        Map[] maps = getCollection("posts");

        int mapCount = maps.length;
        Post[] posts = new Post[mapCount];

        for (int i = 0; i < mapCount; i++)
        {
            Post post = new Post();
            post.setCaption(maps[i].get("caption").toString());
            post.setDescription(maps[i].get("description").toString());
            post.setImageUrl(url2Uri(maps[i].get("url").toString()));

            posts[i] = post;
        }

        return posts;
    }



    private Map[] getCollection(String collectionPath)
    {
        storage = FirebaseStorage.getInstance();
        ArrayList<Map> mapArrayList = new ArrayList<>();

        db.collection(collectionPath).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot document : task.getResult())
                    {
                        mapArrayList.add(document.getData());
                    }
                }
                else
                    System.out.println(task.getException());
            }
        });

        Map[] maps = new Map[mapArrayList.size()];
        return mapArrayList.toArray(maps);
    }


    private String url2Uri(String url)
    {
        gsReference = storage.getReferenceFromUrl(url);

        gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri)
            {
                imageUri = uri.toString();
            }
        });
        return imageUri;
    }
}
