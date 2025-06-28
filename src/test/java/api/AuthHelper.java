package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import pojo.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class AuthHelper {

    @Step("Регистрация пользователя")
    public static Response registerUser(User user) {
        return given()
                .baseUri(Endpoints.BASE_URI)
                .header("Content-Type", "application/json")
                .body(user)
                .post(Endpoints.REGISTER_ENDPOINT);
    }

    @Step("Авторизация пользователя")
    public static Response loginUser(User user) {
        return given()
                .baseUri(Endpoints.BASE_URI)
                .header("Content-Type", "application/json")
                .body(user)
                .post(Endpoints.LOGIN_ENDPOINT);
    }

    @Step("Получение accessToken")
    public static String getAccessToken(Response response) {
        String token = response.path("accessToken");
        return token != null ? token.replace("Bearer ", "") : null;
    }

    @Step("Удаление пользователя")
    public static void deleteUser(String accessToken) {
        if (accessToken != null && !accessToken.isEmpty()) {
            given()
                    .baseUri(Endpoints.BASE_URI)
                    .header("Authorization", "Bearer " + accessToken)
                    .delete(Endpoints.USER_ENDPOINT)
                    .then()
                    .statusCode(anyOf(is(200), is(202)));
        }
    }
}