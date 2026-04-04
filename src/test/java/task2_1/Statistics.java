package task2_1;

public class Statistics {
  public Integer contacts;
  public Integer likes;
  public Integer viewCount;

  public Statistics() {
  }

  public Statistics(Integer contacts, Integer likes, Integer viewCount) {
    this.contacts = contacts;
    this.likes = likes;
    this.viewCount = viewCount;
  }

  public Integer getContacts() {
    return contacts;
  }

  public Integer getLikes() {
    return likes;
  }

  public Integer getViewCount() {
    return viewCount;
  }

  public void setContacts(Integer contacts) {
    this.contacts = contacts;
  }

  public void setLikes(Integer likes) {
    this.likes = likes;
  }

  public void setViewCount(Integer viewCount) {
    this.viewCount = viewCount;
  }
}
