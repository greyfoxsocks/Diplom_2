package api;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import pojo.User;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.*;

public class AuthApiTest {
    private String accessToken;

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
        // Создание уникального пользователя
        String email = "login_user_" + System.currentTimeMillis() + "@test.ru";
        User user = new User(email, "password", "Login User");

        // Регистрация и сохранение токена ДО проверок
        Response registerResponse = AuthHelper.registerUser(user);
        accessToken = AuthHelper.getAccessToken(registerResponse);

        // Авторизация под созданным пользователем
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
        // Создание уникального пользователя
        String email = "wrong_pass_user_" + System.currentTimeMillis() + "@test.ru";
        User user = new User(email, "password", "Wrong Pass User");

        // Регистрация и сохранение токена ДО проверок
        Response registerResponse = AuthHelper.registerUser(user);
        accessToken = AuthHelper.getAccessToken(registerResponse);

        // Попытка авторизации с неверным паролем
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
        // Попытка авторизации с несуществующими данными
        User user = new User("nonexistent@test.ru", "password", "NonExistent User");
        AuthHelper.loginUser(user)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}