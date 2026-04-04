package task2_1;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class ItemCreationsTests extends ApiConfig{
  @BeforeEach
  public void setUp(){
    RestAssured.basePath = "api/1/item";
  }

  @DisplayName("Создание объявления с валидными данными")
  @Test
  public void createValidInfoTest(){
    Statistics statistics = new Statistics(2414368, 1, 1);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Продажа комбайна", 8300, statistics);

    given().contentType("application/json").body(item)
            .when().post().then().log().all().statusCode(200);
  }

  @DisplayName("Создание объявления с отрицательной стоимостью (Bug-01)")
  @Test
  public void createItemBadPrice(){
    Statistics statistics = new Statistics(92346412, 2, 3);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Iphone 12 pro max 256GB", -13000, statistics);
    given().contentType("application/json").body(item)
            .when().post().then().log().all().statusCode(200);
  }

  @DisplayName("Создание объявления с нулевой стоимостью (Bug-02)")
  @Test
  public void createItemZeroPrice(){Statistics statistics = new Statistics(49876589, 9, 1);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Пылесос REDMOND", 0, statistics);
    given().contentType("application/json").body(item)
            .when().post().then().log().all().statusCode(400);
  }

}
