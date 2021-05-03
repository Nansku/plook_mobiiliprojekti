package co.plook;

import android.content.Context;

import com.google.firebase.Timestamp;

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


    //return 'x seconds/minutes/hours/days ago using this.time and Timestamp.now()
    public String getTimeDifference(Context c)
    {
        String str = "";
        long difference = Timestamp.now().getSeconds() - time.getSeconds();

        if (difference < 60)
            str = c.getResources().getString(R.string.comment_lessThanMinuteAgo);
        else if (difference < 3600)
        {
            long minutes = difference / 60;
            str = minutes + " " + (minutes == 1 ? c.getResources().getString(R.string.time_minute) : c.getResources().getString(R.string.time_minutes));
        }
        else if (difference < 86400)
        {
            long hours = difference / 3600;
            str = hours + " " + (hours == 1 ? c.getResources().getString(R.string.time_hour) : c.getResources().getString(R.string.time_hours));
        }
        else if (difference < 31536000)
        {
            long days = difference / 86400;
            str = days + " " + (days == 1 ? c.getResources().getString(R.string.time_day) : c.getResources().getString(R.string.time_days));
        }
        else
        {
            long years = difference / 31536000;
            str = years + " " + (years == 1 ? c.getResources().getString(R.string.time_year) : c.getResources().getString(R.string.time_years));
        }

        return str;
    }
}
