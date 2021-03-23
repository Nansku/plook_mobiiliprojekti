package co.plook;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class DatabaseDownloader
{
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference gsReference;

    //MAKE GETTER
    public boolean isRunning;

    private Map[] maps;
    private String imageUri;
    final String tag = "TULOSTUS";

    public DatabaseDownloader()
    {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public Map[] getMaps()
    {
        return maps;
    }

    public String getUri()
    {
        return imageUri;
    }

    public void loadCollection(String collectionPath)
    {
        isRunning = true;

        storage = FirebaseStorage.getInstance();
        ArrayList<Map> mapArrayList = new ArrayList<>();

        Log.d(tag, "Asetetaan db-kuuntelija");
        db.collection(collectionPath).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                Log.d(tag, "Ennen taskia");
                if (task.isSuccessful())
                {
                    Log.d(tag, "db task successfull!!");
                    for (QueryDocumentSnapshot document : task.getResult())
                    {
                        mapArrayList.add(document.getData());
                    }
                }
                else
                    Log.d(tag, "Exception: " + task.getException());

                Log.d(tag, "For looppi käyty");
                Map[] maps = new Map[mapArrayList.size()];
                mapArrayList.toArray(maps);
                Log.d(tag, "Laitetaan isRunning falseksi");
                isRunning = false;
            }
        });
    }

    public void url2Uri(String url)
    {
        isRunning = true;

        gsReference = storage.getReferenceFromUrl(url);
        gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri)
            {
                imageUri = uri.toString();
                isRunning = false;
            }
        });
    }
}
