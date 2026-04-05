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
import java.util.UUID;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ItemReceivingByIdTests extends ApiConfig{

  @BeforeEach
  public void setUp(){
    RestAssured.basePath = "api/1/item";
    RestAssured.config = RestAssuredConfig.config().connectionConfig(ConnectionConfig.connectionConfig()
                    .closeIdleConnectionsAfterEachResponse());
  }

  public String createAnnoucement(){
    StatisticsPojo statistics = new StatisticsPojo(123, 12, 12);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Автобус MAN", 120000, statistics);

    String idAnnoucement = given().contentType("application/json").body(item)
            .when().post()
            .then().statusCode(200).extract().path("status");
    return idAnnoucement.split(" - ")[1];
  }

  @DisplayName("2.1)Получение объявления по существующему в системе id")
  @Test
  public void getItemValidId(){
    String id = createAnnoucement();
    given().pathParam("id", id)
            .when().get("/{id}")
            .then().log().body().statusCode(200);
  }

  @DisplayName("2.2)Попытка получить объявления по случайному id(формат UUID)")
  @Test
  public void getItemRandomId() {
    String id = UUID.randomUUID().toString();

    Response response = given().pathParam("id", id).when().get("/{id}");
    int statusCode = response.getStatusCode();
    if (statusCode == 404){
      String message = "item " + id + " not found";
      response.then().log().body().body("result.message", equalTo(message));
    } else {
      response.then().log().body().body("[0].id", equalTo(id));
    }
  }

  @DisplayName("2.3)Передача пустого id")
  @Test
  public void getItemWithEmptyId(){
    String id = "";
    given().pathParam("id", id)
            .when().get("/{id}")
            .then().log().body().contentType("application/JSON").statusCode(404)
            .body("message", equalTo("route /api/1/item/ not found"));
  }

  @DisplayName("2.4)Получение объявления, где вместо id стоит пробел")
  @Test
  public void getItemIdSpace(){
    String id = " ";
    given().pathParam("id", id)
            .when().get("/{id}")
            .then().log().body().statusCode(400);
  }

  @DisplayName("2.5)Невалидный формат id")
  @ParameterizedTest(name = "Invalid ID: [{index}] \"{0}\"")
  @ValueSource(strings = {
          " ",
          "123",
          "abc-def",
          "STRING-URL",
          "565b467a-b226",
          "zzzzzzzz-zzzz-zzzz-zzzz-hjsag76876sb"
  })
  public void getItemWithInvalidIdFormat(String idUUUID){
      String message = "ID айтема не UUID: " + idUUUID;
      given().pathParam("id", idUUUID)
              .when().get("/{id}")
              .then().log().body().statusCode(400).body("result.message", equalTo(message));
  }

  @DisplayName("2.6)Проверить, что поля созданного объявление равны полям объявления, которые вернет система по этому id")
  @Test()
  public void createAndGetItemAllFieldsMatch() {
    StatisticsPojo statistics = new StatisticsPojo(11111, 23, 16);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Телевизор Samsung 7 Series", 35777, statistics);

    String url = given().contentType("application/json").body(item)
            .when().post()
            .then().statusCode(200).extract().path("status");
    String id = url.split(" - ")[1];

    given().pathParam("id", id)
            .when().get("/{id}")
            .then().log().body()
            .body("[0].id", equalTo(id))
            .body("[0].name", equalTo(item.getName()))
            .body("[0].price", equalTo(item.getPrice()))
            .body("[0].sellerId", equalTo(item.getSellerId()))
            .body("[0].statistics.contacts",equalTo(statistics.getContacts()))
            .body("[0].statistics.likes",equalTo(statistics.getLikes()) )
            .body("[0].statistics.viewCount",equalTo(statistics.getViewCount()) )
            .statusCode(200);
  }

  @DisplayName("2.7)Повторное создание объявления с идентичными данными и проверка равенства полей созданных объявлений")
  @Test
  public void createduplicateItemGetItemEquality(){
    StatisticsPojo statistics = new StatisticsPojo(32746, 89, 34);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Оконная рама из серебра", 14000, statistics);

    String statusFirst =
            given().contentType("application/json").body(item)
            .when().post()
            .then().statusCode(200).extract().jsonPath().getString("status");
    String idFirst = statusFirst.split(" - ")[1];

    Awaitility.await()
            .atMost(Duration.ofSeconds(4))
            .pollInterval(Duration.ofMillis(50))
            .until(() -> {
              return given().pathParam("id", idFirst)
                      .when().get("/{id}")
                      .then().extract().statusCode() == 200;
            });

    String statusSecond =
            given().contentType("application/json").body(item)
            .when().post()
            .then().statusCode(200).extract().jsonPath().getString("status");
    String idSecond = statusSecond.split(" - ")[1];

    AnnouncmentPojo[] itemFirst = given().pathParam("id", idFirst)
            .when().get("/{id}")
            .then().log().body().statusCode(200).extract().as(AnnouncmentPojo[].class);;

    AnnouncmentPojo[] itemSecond = given().pathParam("id", idSecond)
            .when().get("/{id}")
            .then().log().body().statusCode(200).extract().as(AnnouncmentPojo[].class);

    assertNotEquals(idFirst, idSecond);
    assertNotEquals(itemFirst[0].getCreatedAt(), itemSecond[0].getCreatedAt());
    assertEquals(itemFirst[0].getName(), itemSecond[0].getName());
    assertEquals(itemFirst[0].getPrice(), itemSecond[0].getPrice());
    assertEquals(itemFirst[0].getSellerId(), itemSecond[0].getSellerId());
    assertEquals(itemFirst[0].getStatistics().getContacts(), itemSecond[0].getStatistics().getContacts());
    assertEquals(itemFirst[0].getStatistics().getLikes(), itemSecond[0].getStatistics().getLikes());
    assertEquals(itemFirst[0].getStatistics().getViewCount(), itemSecond[0].getStatistics().getViewCount());
  }
}
