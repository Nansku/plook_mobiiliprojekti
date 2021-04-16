package co.plook;

import java.util.List;

public class Post
{
    private String caption;
    private String description;
    private String imageUrl;
    private String postID;
    private String userID;
    private String channelID;
    private String[] tags;

    public Post()
    {
        caption = "";
        description = "";
        imageUrl = "";
        postID = "";
        userID = "";
        channelID = "";
        tags = new String[0];
    }

    public Post(String caption, String description, String imageUrl, String postID, String userID, String channelID, String[] tags)
    {
        this.caption = caption;
        this.description = description;
        this.imageUrl = imageUrl;
        this.postID = postID;
        this.userID = userID;
        this.channelID = channelID;
        this.tags = tags;
    }

    public void setPostID(String postID) { this.postID = postID; }

    public void setCaption(String caption) { this.caption = caption; }

    public void setDescription(String description) { this.description = description; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setUserID(String userID) { this.userID = userID; }

    public void setChannelID(String channelID) { this.channelID = channelID; }

    public void setTags(String[] tags) { this.tags = tags; }

    public String getPostID() { return postID; }

    public String getCaption() { return caption; }

    public String getDescription() { return description; }

    public String getImageUrl() { return imageUrl; }

    public String getUserID() { return userID; }

    public String getChannelID() { return channelID; }

    public String[] getTags() { return tags; }
}
