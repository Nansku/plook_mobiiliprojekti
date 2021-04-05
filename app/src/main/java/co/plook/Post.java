package co.plook;

public class Post
{
    private String caption;
    private String description;
    private String imageUrl;
    private String postID;
    private String name;

    public Post()
    {
        caption = "";
        description = "";
        imageUrl = "";
        postID = "";
        name = "";
    }

    public Post(String caption, String description, String imageUrl, String postID, String name)
    {
        this.caption = caption;
        this.description = description;
        this.imageUrl = imageUrl;
        this.postID = postID;
        this.name = name;
    }

    public void setPostID(String postID) { this.postID = postID; }

    public void setCaption(String caption) { this.caption = caption; }

    public void setDescription(String description) { this.description = description; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setName(String name) { this.name = name; }

    public String getPostID() { return postID; }

    public String getCaption() { return caption; }

    public String getDescription() { return description; }

    public String getImageUrl() { return imageUrl; }

    public String getName() { return name; }
}
