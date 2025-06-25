package api;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import pojo.Order;
import pojo.User;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;

@DisplayName("API тесты для создания заказов")
public class OrderApiTest {
    private static final String BASE_URI = "https://stellarburgers.nomoreparties.site";
    private static final String INGREDIENTS_ENDPOINT = "/api/ingredients";
    private static final String ORDERS_ENDPOINT = "/api/orders";
    private String accessToken;

    private List<String> getValidIngredients() {
        return given()
                .baseUri(BASE_URI)
                .get(INGREDIENTS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .path("data._id");
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            AuthHelper.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    public void createOrderWithAuthAndIngredientsSuccess() {
        String email = "order_user_" + System.currentTimeMillis() + "@test.ru";
        User user = new User(email, "password", "Username");

        Response registerResponse = AuthHelper.registerUser(user);
        registerResponse.then().statusCode(200);

        accessToken = AuthHelper.getAccessToken(registerResponse);
        assertNotNull("Access token должен быть получен", accessToken);

        Order order = new Order(getValidIngredients().toArray(new String[0]));

        given()
                .baseUri(BASE_URI)
                .header("Content-Type", "application/json")
                .header("Authorization", accessToken) // Токен уже содержит "Bearer "
                .body(order)
                .post(ORDERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void createOrderWithoutAuthSuccess() {
        Order order = new Order(getValidIngredients().toArray(new String[0]));

        given()
                .baseUri(BASE_URI)
                .header("Content-Type", "application/json")
                .body(order)
                .post(ORDERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    public void createOrderWithoutIngredientsFail() {
        Order order = new Order(new String[]{});

        given()
                .baseUri(BASE_URI)
                .header("Content-Type", "application/json")
                .body(order)
                .post(ORDERS_ENDPOINT)
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с невалидным хешем ингредиентов")
    public void createOrderWithInvalidIngredientHashFail() {
        Order order = new Order(new String[]{"invalid_hash_12345"});

        given()
                .baseUri(BASE_URI)
                .header("Content-Type", "application/json")
                .body(order)
                .post(ORDERS_ENDPOINT)
                .then()
                .statusCode(500);
    }
}