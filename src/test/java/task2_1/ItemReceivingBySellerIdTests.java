package task2_1;

import io.restassured.RestAssured;
import io.restassured.config.ConnectionConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.Duration;
import java.util.List;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ItemReceivingBySellerIdTests extends ApiConfig{
  @BeforeEach
  public void setUp(){
    RestAssured.config = RestAssuredConfig.config().connectionConfig(ConnectionConfig.connectionConfig()
                    .closeIdleConnectionsAfterEachResponse());
  }

  private String createItemAndGetId(int sellerId, String name, int price) {
    StatisticsPojo stats = new StatisticsPojo(100, 10, 10);
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, name, price, stats);

    String status =
            given().log().all().contentType("application/json").body(item)
            .when().post("/api/1/item")
            .then().statusCode(200).extract().jsonPath().getString("status");
    return status.split(" - ")[1];
  }

  @DisplayName("3.1)Проверим, что по запросу GET нам возвращаются только объявления этого пользователя, если он есть," +
          "иначе возвращается пустой массив")
  @Test
  public void getItemBySellerIdOnlyMatchingItem(){
    int sellerId = GenerateSellerID.generateSellerId();
    Response response =
            given().pathParam("sellerId", sellerId)
                    .when().get("/api/1/{sellerId}/item")
                    .then().log().body().statusCode(200).contentType("application/json").extract().response();
    List<Integer> returnedSellerIds = response.jsonPath().getList("sellerId");
    if (returnedSellerIds.isEmpty()){
      return;
    } else {
      for (Integer id : returnedSellerIds){
        assertEquals(sellerId, id);
      }
    }
  }

  @DisplayName("3.2)Массовое создание объявлений: все элементы корректно отображаются в списке продавца")
  @Test
  public void createMultipleItems(){
    int sellerId = GenerateSellerID.generateSellerId();
    String itemFirstId = createItemAndGetId(sellerId, "Автоматическая поилка для питомцев", 4300);
    String itemSecond = createItemAndGetId(sellerId, "Удочка электронная", 8150);
    given().pathParam("sellerId", sellerId)
            .when().get("/api/1/{sellerId}/item")
            .then().statusCode(200).contentType("application/json")
            .body("$", hasSize(greaterThanOrEqualTo(2)))
            .body("id", hasItems(itemFirstId, itemSecond))
            .body("sellerId", everyItem(equalTo(sellerId)));
  }


  @DisplayName("3.3)Первое объявление продавца: переход от пустого списка к валидным данным и созданию объявления")
  @Test
  public void createFirstItemsSellerEmptyListItemCorrectly(){
    int sellerId = GenerateSellerID.generateSellerId();
    List<Integer> Items =
            given().pathParam("sellerId", sellerId)
            .when().get("/api/1/{sellerId}/item")
            .then().statusCode(200).extract().jsonPath().getList("sellerId");

    if (Items.isEmpty()) {
      createItemAndGetId(sellerId, "Тестовый товар", 1000);

      Awaitility.await().atMost(Duration.ofSeconds(3)).pollInterval(Duration.ofMillis(100))
              .ignoreExceptions()
              .until(() -> given().pathParam("sellerId", sellerId)
                      .when().get("/api/1/{sellerId}/item").statusCode() == 200);
      Items =
              given().pathParam("sellerId", sellerId)
              .when().get("/api/1/{sellerId}/item")
              .then().statusCode(200).extract().jsonPath().getList("sellerId");

      assertFalse(Items.isEmpty());

      for(Integer id : Items){
        assertEquals(sellerId, id);
      }
    }
  }

  @DisplayName("3.4)Некорректный формат sellerId (строка вместо числа)")
  @Test
  public void getWithInvalidSellerIdFormat(){
    given()
    .when().get("/api/1/abs/item")
    .then().statusCode(400).body("result.message", equalTo("передан некорректный идентификатор продавца"));;
  }

  @DisplayName("3.5)SellerId <= 0 (BUGS-05)")
  @ParameterizedTest(name = "sellerId={0} должен возвращать ошибку, но возвращает 200")
  @ValueSource(ints = {0, -1})
  public void getWithZeroOrNegativeSellerId(int sellerId){
    given().
    when().get("/api/1/{sellerId}/item", sellerId).then().log().all().statusCode(200);
  }
}
