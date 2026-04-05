package task2_1;

public class AnnouncmentPojo {
  public String createdAt;
  public String id;
  public String name;
  public Integer price;
  public Integer sellerId;
  public StatisticsPojo statistics;

  public AnnouncmentPojo() {
  }

  public AnnouncmentPojo(Integer sellerId, String name, Integer price, StatisticsPojo statistics) {
    this.sellerId = sellerId;
    this.price = price;
    this.name = name;
    this.statistics = statistics;

  }

  public AnnouncmentPojo(String createdAt, String id, String name, Integer price, Integer sellerId, StatisticsPojo statistics) {
    this.createdAt = createdAt;
    this.id = id;
    this.name = name;
    this.price = price;
    this.sellerId = sellerId;
    this.statistics = statistics;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Integer getPrice() {
    return price;
  }

  public Integer getSellerId() {
    return sellerId;
  }

  public StatisticsPojo getStatistics() {
    return statistics;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPrice(Integer price) {
    this.price = price;
  }

  public void setSellerId(Integer sellerId) {
    this.sellerId = sellerId;
  }

  public void setStatistics(StatisticsPojo statistics) {
    this.statistics = statistics;
  }
}
