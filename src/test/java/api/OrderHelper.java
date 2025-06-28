package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import pojo.Order;

import static io.restassured.RestAssured.given;

public class OrderHelper {

    @Step("Получение ингредиентов")
    public static Response getIngredients() {
        return given()
                .baseUri(Endpoints.BASE_URI)
                .get(Endpoints.INGREDIENTS_ENDPOINT);
    }

    @Step("Создание заказа")
    public static Response createOrder(Order order, String accessToken) {
        if (accessToken != null) {
            return given()
                    .baseUri(Endpoints.BASE_URI)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(order)
                    .post(Endpoints.ORDERS_ENDPOINT);
        } else {
            return given()
                    .baseUri(Endpoints.BASE_URI)
                    .header("Content-Type", "application/json")
                    .body(order)
                    .post(Endpoints.ORDERS_ENDPOINT);
        }
    }
}