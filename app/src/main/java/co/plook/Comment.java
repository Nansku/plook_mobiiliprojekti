package co.plook;

import android.annotation.SuppressLint;

import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Comment
{
    private String userID;
    private String userName;
    private String text;
    private String repliedToID;
    private Timestamp time;


    public Comment(String userID, String userName, String text, String repliedToID, Timestamp time)
    {
        this.userID = userID;
        this.userName = userName;
        this.text = text;
        this.repliedToID = repliedToID;
        this.time = time;
    }

    public void setUserName(String userName) { this.userName = userName; }

    public String getUserID()
    {
        return userID;
    }

    public String getUserName() { return userName; }

    public String getText()
    {
        return text;
    }

    public String getRepliedToID()
    {
        return repliedToID;
    }

    public Timestamp getTime()
    {
        return time;
    }


    //return 'x seconds/minutes/hours/days ago' using this.time and Timestamp.now()
    public String getTimeDifference()
    {
        String str = "";
        long difference = Timestamp.now().getSeconds() - time.getSeconds();

        if (difference < 60)
            str = "Less than a minute ago";
        else if (difference < 3600)
            str = difference / 60 + " minutes ago";
        else if (difference < 86400)
            str = (difference / 3600) + " hours ago";
        else
        {
            @SuppressLint("SimpleDateFormat") DateFormat date = new SimpleDateFormat("dd/MM/yyyy");
            str = date.format(time.toDate());
        }

        return str;
    }
}
