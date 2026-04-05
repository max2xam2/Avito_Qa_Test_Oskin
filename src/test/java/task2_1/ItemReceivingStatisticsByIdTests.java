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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemReceivingStatisticsByIdTests extends ApiConfig{

  @BeforeEach
  public void setUp(){
    RestAssured.basePath = "api";
    RestAssured.config = RestAssuredConfig.config().connectionConfig(ConnectionConfig.connectionConfig()
                    .closeIdleConnectionsAfterEachResponse());
  }

  @DisplayName("4.1)Создание объявления: возвращённые данные совпадают с переданными")
  @Test
  public void getStatisticsById(){
    StatisticsPojo statistics = new StatisticsPojo(2414368, 1, 1);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Продажа трактора", 8300, statistics);

    String statusResponse = given().contentType("application/json").body(item)
            .when().post("/1/item")
            .then().log().all().statusCode(200).extract().path("status");

    String ItemId = statusResponse.toString().split(" - ")[1];

    Awaitility.await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(1, TimeUnit.SECONDS)
            .until(() -> given().pathParam("itemId", ItemId)
                    .get("/2/statistic/{itemId}")
                    .statusCode() == 200);

    given().pathParam("itemId", ItemId).accept("application/json")
            .when().get("/2/statistic/{itemId}")
            .then().log().ifValidationFails().statusCode(200)
              .body("$", hasSize(greaterThanOrEqualTo(1)))
              .body("[0].contacts", equalTo(2414368))
              .body("[0].likes", equalTo(1))
              .body("[0].viewCount", equalTo(1));
  }

  @DisplayName("4.2)Идемпотентность GET: 3 последовательных запроса возвращают идентичный ответ")
  @Test
  public void checkingRequestIdempotency(){
    String randomUuid = UUID.randomUUID().toString();

    Response responseFirst = given().pathParam("itemId", randomUuid).accept("application/json")
            .when().get("/2/statistic/{itemId}")
            .then().extract().response();

    Response responseSecond = given().pathParam("itemId", randomUuid).accept("application/json")
            .when().get("/2/statistic/{itemId}")
            .then().extract().response();

    Response responseThird = given().pathParam("itemId", randomUuid).accept("application/json")
            .when().get("/2/statistic/{itemId}")
            .then().extract().response();

    Object body1 = responseFirst.jsonPath().get("$");
    Object body2 = responseSecond.jsonPath().get("$");
    Object body3 = responseThird.jsonPath().get("$");

    assertEquals(responseFirst.statusCode(), responseSecond.statusCode());
    assertEquals(responseSecond.statusCode(), responseThird.statusCode());
    assertEquals(body1, body2);
    assertEquals(body2, body3);
  }

  @DisplayName("4.3)Проверка ответа эндпоинта statistics при запросе по случайному UUID")
  @Test
  public void requestByRandomId(){
    String randomUuid = UUID.randomUUID().toString();
    String message = "statistic " + randomUuid + " not found";
    Response response = given() .pathParam("itemId", randomUuid).accept("application/json")
            .when().get("/2/statistic/{itemId}")
            .then().log().all().extract().response();

    int statusCode = response.statusCode();

    if (statusCode == 404){
      assertEquals(message, response.jsonPath().getString("result.message"));
    } else {
      assertNotNull(response.jsonPath().getList("$"));
      assertFalse(response.jsonPath().getList("$").isEmpty());
      assertNotNull(response.jsonPath().getInt("[0].contacts"));
      assertNotNull(response.jsonPath().getInt("[0].likes"));
      assertNotNull(response.jsonPath().getInt("[0].viewCount"));
      assertTrue(response.jsonPath().getInt("[0].contacts") instanceof Integer);
      assertTrue(response.jsonPath().getInt("[0].likes") instanceof Integer);
      assertTrue(response.jsonPath().getInt("[0].viewCount") instanceof Integer);
      assertTrue(response.jsonPath().getInt("[0].contacts") > 0);
      assertTrue(response.jsonPath().getInt("[0].likes") > 0);
      assertTrue(response.jsonPath().getInt("[0].viewCount") > 0);
    }
  }

  @DisplayName("4.4)Невалидный формат ID(не является UUID)")
  @ParameterizedTest(name = "Invalid ID: [{index}] \"{0}\"")
  @ValueSource(strings = {
          " ",
          "123",
          "abc-def",
          "STRING-URL",
          "565b467a-b226",
          "zzzzzzzz-zzzz-zzzz-zzzz-hjsag76876sb"
  })
  public void invalidIdFormatId(String id){
    given().pathParam("itemId", id)
            .when().get("/2/statistic/{itemId}")
            .then().log().all().statusCode(404)
            .body("result.message", equalTo("передан некорректный идентификатор объявления"));
  }
}
