package co.plook;

import android.content.Context;

import com.google.firebase.Timestamp;

public class Post
{
    private String caption;
    private String description;
    private String imageUrl;
    private String postID;
    private String userID;
    private String channelID;
    private String[] tags;
    private Timestamp time;
    private long score;
    private long myVote;

    public Post()
    {
        caption = "";
        description = "";
        imageUrl = "";
        postID = "";
        userID = "";
        channelID = "";
        tags = new String[0];
        score = 0;
        myVote = 0;
    }

    public Post(String caption, String description, String imageUrl, String postID, String userID, String channelID, String[] tags, Timestamp time, int score)
    {
        this.caption = caption;
        this.description = description;
        this.imageUrl = imageUrl;
        this.postID = postID;
        this.userID = userID;
        this.channelID = channelID;
        this.tags = tags;
        this.time = time;
        this.score = score;
        myVote = 0;
    }

    public void setPostID(String postID) { this.postID = postID; }

    public void setCaption(String caption) { this.caption = caption; }

    public void setDescription(String description) { this.description = description; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setUserID(String userID) { this.userID = userID; }

    public void setChannelID(String channelID) { this.channelID = channelID; }

    public void setTags(String[] tags) { this.tags = tags; }

    public void setTime(Timestamp time) { this.time = time; }

    public void setScore(long score) { this.score = score; }

    public void setMyVote(long myVote) { this.myVote = myVote; }

    public String getPostID() { return postID; }

    public String getCaption() { return caption; }

    public String getDescription() { return description; }

    public String getImageUrl() { return imageUrl; }

    public String getUserID() { return userID; }

    public String getChannelID() { return channelID; }

    public String[] getTags() { return tags; }

    public Timestamp getTime() {return time; }

    public long getScore() { return score; }

    public long getMyVote() { return myVote; }

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
