package task2_1.config;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public class ApiConfig {
  @BeforeAll
  public static void globalSetUp(){
    RestAssured.baseURI = "https://qa-internship.avito.com/";
  }
}
