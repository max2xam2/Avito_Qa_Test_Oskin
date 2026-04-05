package task2_1.tests.creationItem;

import io.restassured.RestAssured;
import io.restassured.config.ConnectionConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.*;
import org.awaitility.Awaitility;
import task2_1.models.AnnouncmentPojo;
import task2_1.config.ApiConfig;
import task2_1.utils.GenerateSellerID;
import task2_1.models.StatisticsPojo;

import java.time.Duration;
import java.util.List;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class ItemCreationsTests extends ApiConfig {
  @BeforeEach
  public void setUp(){
    RestAssured.basePath = "api/1/item";
    RestAssured.config = RestAssuredConfig.config().connectionConfig(ConnectionConfig.connectionConfig()
                    .closeIdleConnectionsAfterEachResponse());
  }

  @DisplayName("Создание объявления с валидными данными")
  @Test
  public void createItemValidData(){
    StatisticsPojo statistics = new StatisticsPojo(2414368, 1, 1);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Продажа комбайна", 8300, statistics);

    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(200);
  }

  @DisplayName("Граничные значения (INT_MAX) во всех числовых полях")
  @Test
  public void createWithMaxIntValues(){
    StatisticsPojo statistics = new StatisticsPojo(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Чайник REDMOND", Integer.MAX_VALUE, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(200);
  }

  @DisplayName("Граничные значения (INT_MIN) во всех числовых полях")
  @Test
  public void createWithMinIntValues(){
    StatisticsPojo statistics = new StatisticsPojo(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Чайник REDMOND", Integer.MIN_VALUE, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(200);
  }

  @DisplayName("Повторный POST с одинаковым телом: генерация уникальных идентификаторов(неидемпотентность)")
  @Test
  public void createDuplicateBody() {
    StatisticsPojo statistics = new StatisticsPojo(912375, 10, 9);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Хлебопечка BORK", 1000, statistics);

    String statusFirst =
            given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(200).extract().path("status");
    String idFirst = statusFirst.split(" - ")[1];

    Awaitility.await()
            .pollInterval(Duration.ofMillis(100))
            .ignoreExceptions()
            .until(() -> {
              JsonPath json = given()
                      .pathParam("id", idFirst)
                      .when().get("/{id}")
                      .then()
                      .extract().jsonPath();
              List<String> ids = json.getList("id");
              return ids != null && ids.contains(idFirst);
            });

    String statusSecond =
            given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(200).extract().path("status");
    String idSecond = statusSecond.split(" - ")[1];

    Assertions.assertNotEquals(idFirst,idSecond);
  }

  @DisplayName("Создание объявления с пустым телом")
  @Test
  public void createWithEmptyBody(){
    String body = """
          """;
    given().contentType("application/json").body(body)
            .when().post()
            .then().log().all().statusCode(400);
  }

  @DisplayName("Создание объявления со строковым форматом sellerId")
  @Test
  public void createWithStringSellerId() {
    String body = """
            {
              "sellerID": "sellerId",
              "name": "BMW X5",
              "price": 34217,
              "statistics": {
                    "contacts": 234423,
                    "likes": 2134,
                    "viewCount": 123
                }
            }
            """;

    given().contentType("application/json").body(body)
            .when().post()
            .then().log().all().statusCode(400).body("status", equalTo("не передано тело объявления"));

  }

  @DisplayName("Строка в поле стоимость")
  @Test
  public void createWithNonNumericPrice() {
    int sellerId = GenerateSellerID.generateSellerId();
    String body = """
            {
              "sellerID": %d,
              "name": "BMW X5",
              "price": "?",
              "statistics": {
                    "contacts": 234423,
                    "likes": 2134,
                    "viewCount": 123
                }
            }
            """.formatted(sellerId);

    given().contentType("application/json").body(body)
            .when().post()
            .then().log().all().statusCode(400);
  }

  @DisplayName("Создание объявление с пустым именем")
  @Test
  public void createWithEmptyName(){
    StatisticsPojo statistics = new StatisticsPojo(49876589, 9, 1);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "", 0, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(400);
  }

  @DisplayName("Создание объявления с нулевой стоимостью (Bug-02)")
  @Test
  public void createItemZeroPrice(){
    StatisticsPojo statistics = new StatisticsPojo(49876589, 9, 1);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Пылесос REDMOND", 0, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(400);
  }

  @DisplayName("Создание объявления SellerId равен нулю")
  @Test
  public void createWithZeroSellerId(){
    StatisticsPojo statistics = new StatisticsPojo(92346412, 2, 3);
    int sellerId = 0;
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Iphone 14 pro max 256GB", 12000, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(400);
  }

  @DisplayName("Поля statistics равны нулю")
  @Test
  public void createWithZeroStatistics(){
    StatisticsPojo statistics = new StatisticsPojo(0, 0, 0);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Чайник REDMOND", 1233, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(400);
  }

  @DisplayName("Заполнен только contacts")
  @Test
  public void createWithOnlyContacts(){
    StatisticsPojo statistics = new StatisticsPojo(9, 0, 0);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Чайник REDMOND", 1233, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(400);
  }

  @DisplayName("Заполнен только likes")
  @Test
  public void createWithOnlyLikes(){
    StatisticsPojo statistics = new StatisticsPojo(0, 9, 0);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Чайник REDMOND", 1233, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(400);
  }

  @DisplayName("Заполнен только viewCount")
  @Test
  public void createWithOnlyViewCount(){
    StatisticsPojo statistics = new StatisticsPojo(0, 0, 9);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Чайник REDMOND", 1233, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(400);
  }

  @DisplayName("Заполнены contacts и likes")
  @Test
  public void createWithContactsAndLikes(){
    StatisticsPojo statistics = new StatisticsPojo(9, 9, 0);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Чайник REDMOND", 1233, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(400);
  }

  @DisplayName("Заполнены likes и viewCount")
  @Test
  public void createWithLikesAndViewCount(){
    StatisticsPojo statistics = new StatisticsPojo(0, 9, 9);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Чайник REDMOND", 1233, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(400);
  }

  @DisplayName("Заполнены contacts и viewCount")
  @Test
  public void createWithContactsAndViewCount(){
    StatisticsPojo statistics = new StatisticsPojo(9, 0, 9);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Чайник REDMOND", 1233, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(400);
  }

  @DisplayName("Комплексная невалидность (пустое имя + нулевая цена + нулевые stats)")
  @Test
  public void createWithMultipleInvalidFields(){
    StatisticsPojo statistics = new StatisticsPojo(0, 0, 0);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "", 0, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(400);
  }

  @DisplayName("Создание объявления с отрицательной стоимостью (Bug-01)")
  @Test
  public void createItemNegativePrice(){
    StatisticsPojo statistics = new StatisticsPojo(92346412, 2, 3);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Iphone 12 pro max 256GB", -13000, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(200);
  }

  @DisplayName("Создание объявления с отрицательными значениями поля statistics (Bug - 03))")
  @Test
  public void negativeStatisticalValues(){
    StatisticsPojo statistics = new StatisticsPojo(-123123, -12, -7);
    int sellerId = GenerateSellerID.generateSellerId();
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Iphone 17 Pro 256GB", 130000, statistics);
    given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(200);
  }

  @DisplayName("Создание объявления SellerId = -1(отрицательное цисло) (BUGS-04)")
  @Test
  public void createWithNegativeSellerId(){
    StatisticsPojo statistics = new StatisticsPojo(2344, 1, 18);
    int sellerId = -1;
    AnnouncmentPojo item = new AnnouncmentPojo(sellerId, "Iphone 11 pro 512GB", 9990, statistics);

    String statusSecond =
            given().contentType("application/json").body(item)
            .when().post()
            .then().log().all().statusCode(200).extract().path("status");
    String statusitem = statusSecond.split(" - ")[0];

    Assertions.assertEquals(statusitem,"Сохранили объявление");
  }
}
