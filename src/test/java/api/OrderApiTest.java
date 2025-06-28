package api;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import pojo.Order;
import pojo.User;

import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;

@DisplayName("API тесты для создания заказов")
public class OrderApiTest {
    private String accessToken;

    private List<String> getValidIngredients() {
        Response response = OrderHelper.getIngredients();
        response.then().statusCode(SC_OK);
        return response.jsonPath().getList("data._id");
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            AuthHelper.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Успешное создание заказа авторизованным пользователем с валидными ингредиентами")
    public void createOrderWithAuthAndIngredientsSuccessTest() {
        String email = "order_user_" + System.currentTimeMillis() + "@test.ru";
        User user = new User(email, "password", "Username");

        Response registerResponse = AuthHelper.registerUser(user);
        registerResponse.then().statusCode(SC_OK);
        accessToken = AuthHelper.getAccessToken(registerResponse);
        assertNotNull("Access token должен быть получен", accessToken);

        Order order = new Order(getValidIngredients().toArray(new String[0]));
        OrderHelper.createOrder(order, accessToken)
                .then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Создание заказа без авторизации, но с валидными ингредиентами")
    public void createOrderWithoutAuthSuccessTest() {
        Order order = new Order(getValidIngredients().toArray(new String[0]));
        OrderHelper.createOrder(order, null)
                .then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Попытка создания заказа без ингредиентов возвращает ошибку")
    public void createOrderWithoutIngredientsFailTest() {
        Order order = new Order(new String[]{});
        OrderHelper.createOrder(order, null)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с невалидным хешем ингредиентов")
    @Description("Попытка создания заказа с невалидным хешем ингредиентов возвращает внутреннюю ошибку сервера")
    public void createOrderWithInvalidIngredientHashFailTest() {
        Order order = new Order(new String[]{"invalid_hash_12345"});
        OrderHelper.createOrder(order, null)
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }
}