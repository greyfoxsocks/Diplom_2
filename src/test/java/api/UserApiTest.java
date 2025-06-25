package api;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import pojo.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("API тесты для создания пользователя")
public class UserApiTest {
    private static final String BASE_URI = "https://stellarburgers.nomoreparties.site";
    private String accessToken;

    @After
    public void tearDown() {
        if (accessToken != null) {
            AuthHelper.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    public void createUniqueUserSuccess() {
        User user = new User("unique_user_" + System.currentTimeMillis() + "@test.ru", "password", "Username");
        Response response = AuthHelper.registerUser(user);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true));

        accessToken = AuthHelper.getAccessToken(response);
    }

    @Test
    @DisplayName("Создание уже зарегистрированного пользователя")
    public void createDuplicateUserFail() {
        String email = "duplicate_" + System.currentTimeMillis() + "@test.ru";
        User user = new User(email, "password", "Username");

        // Первая регистрация
        Response firstRegister = AuthHelper.registerUser(user);
        firstRegister.then().statusCode(200);
        accessToken = AuthHelper.getAccessToken(firstRegister);

        // Вторая регистрация с тем же email
        Response response = AuthHelper.registerUser(user);
        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля")
    public void createUserWithoutRequiredFieldFail() {
        User user = new User("invalid@test.ru", null, "Username");

        given()
                .baseUri(BASE_URI)
                .header("Content-Type", "application/json")
                .body(user)
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
}