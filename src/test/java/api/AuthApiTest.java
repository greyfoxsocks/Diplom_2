package api;

import api.helpers.AuthHelper;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pojo.User;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.*;

public class AuthApiTest {
    private String accessToken;
    private User user;

    @Before
    public void setUp() {
        // Создание пользователя перед каждым тестом
        String email = "login_user_" + System.currentTimeMillis() + "@test.ru";
        user = new User(email, "password", "Login User");

        // Регистрация пользователя
        Response registerResponse = AuthHelper.registerUser(user);
        accessToken = AuthHelper.getAccessToken(registerResponse);
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            AuthHelper.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Успешный логин пользователя")
    @Description("Проверка успешной авторизации существующего пользователя")
    public void loginUserSuccessTest() {
        Response loginResponse = AuthHelper.loginUser(user);
        loginResponse.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Логин с неверным паролем")
    @Description("Попытка авторизации с неверным паролем должна вернуть ошибку")
    public void loginUserWithWrongPasswordFailTest() {
        User wrongUser = new User(user.getEmail(), "wrong_password", user.getName());
        AuthHelper.loginUser(wrongUser)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин с неверным email")
    @Description("Попытка авторизации с несуществующим email должна вернуть ошибку")
    public void loginUserWithWrongEmailFailTest() {
        User user = new User("nonexistent@test.ru", "password", "NonExistent User");
        AuthHelper.loginUser(user)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}