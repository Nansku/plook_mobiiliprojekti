package co.plook;

public class Post
{
    private String caption;
    private String description;
    private String imageUrl;
    private String postID;

    public Post()
    {
        caption = "";
        description = "";
        imageUrl = "";
        postID = "";
    }

    public Post(String caption, String description, String imageUrl, String postID)
    {
        this.caption = caption;
        this.description = description;
        this.imageUrl = imageUrl;
        this.postID = postID;
    }

    public void setPostID(String postID) { this.postID = postID; }

    public void setCaption(String caption) { this.caption = caption; }

    public void setDescription(String description) { this.description = description; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getPostID() { return postID; }

    public String getCaption() { return caption; }

    public String getDescription() { return description; }

    public String getImageUrl() { return imageUrl; }
}
