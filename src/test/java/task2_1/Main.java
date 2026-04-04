package task2_1;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class Main {
  public static void main(String[] args) {

    Response response = RestAssured
            .given()
            .when()
            .options("https://qa-internship.avito.com");

    // Статус
    System.out.println(response.getStatusCode());

    // Заголовок Allow
    System.out.println(response.getHeader("Allow"));
  }
}