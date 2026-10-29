package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginApp extends Application {

    private SQLmanager db;

    @Override
    public void start(Stage primaryStage) {
        try {
            db = new SQLmanager();

            db.createUsers();
            db.createStudents();
            db.createSubjects();
            db.createTeachers();
            db.createGrades();

            if (db.hasUsers()) {
                showLoginWindow(primaryStage);
            } else {
                showFirstAdminSetup(primaryStage);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось подключиться к базе данных: " + e.getMessage());
        }
    }

    private void showLoginWindow(Stage primaryStage) {
        TextField loginField = new TextField();
        loginField.setPromptText("Логин");
        loginField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        passwordField.setMaxWidth(300);

        Button loginButton = new Button("Войти");
        loginButton.setMaxWidth(300);

        loginButton.setOnAction(e -> {
            String login = loginField.getText().trim();
            String password = passwordField.getText().trim();

            if (login.isEmpty() || password.isEmpty()) {
                showAlert("Ошибка", "Заполните все поля!");
                return;
            }

            try {
                User user = db.login(login, password);
                if (user != null) {
                    System.out.println("Успешный вход: " + user.getUserLogin() + " (" + user.getUserRole() + ")");
                    openMainWindow(primaryStage, user);
                } else {
                    showAlert("Ошибка", "Неверный логин или пароль!");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Ошибка", "Ошибка при входе: " + ex.getMessage());
            }
        });

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                new Label("Вход в систему"),
                loginField,
                passwordField,
                loginButton
        );

        Scene scene = new Scene(layout, 450, 350);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Журнал - Вход");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showFirstAdminSetup(Stage primaryStage) {
        Label titleLabel = new Label("Первый запуск");
        titleLabel.setStyle("-fx-font-size: 22px;");

        Label descLabel = new Label("Создайте аккаунт администратора");
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b9bbbe;");

        TextField loginField = new TextField();
        loginField.setPromptText("Придумайте логин");
        loginField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Придумайте пароль");
        passwordField.setMaxWidth(300);

        Button createButton = new Button("Создать администратора");
        createButton.setMaxWidth(300);

        createButton.setOnAction(e -> {
            String login = loginField.getText().trim();
            String password = passwordField.getText().trim();

            if (login.isEmpty() || password.isEmpty()) {
                showAlert("Ошибка", "Заполните все поля!");
                return;
            }

            try {
                db.registerUser(login, password, "ADMIN");
                showAlert("Успех", "Администратор создан! Теперь войдите в систему.");
                showLoginWindow(primaryStage);
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Ошибка", "Ошибка при создании: " + ex.getMessage());
            }
        });

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                titleLabel,
                descLabel,
                loginField,
                passwordField,
                createButton
        );

        Scene scene = new Scene(layout, 450, 380);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Журнал - Первый запуск");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showRegisterWindow(Stage primaryStage) {
        TextField loginField = new TextField();
        loginField.setPromptText("Придумайте логин");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Придумайте пароль");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("STUDENT", "TEACHER", "ADMIN");
        roleBox.setValue("STUDENT");

        Button backButton = new Button("Назад");
        Button registerButton = new Button("Зарегистрироваться");

        registerButton.setOnAction(e -> {
            String login = loginField.getText().trim();
            String password = passwordField.getText().trim();
            String role = roleBox.getValue();

            if (login.isEmpty() || password.isEmpty()) {
                showAlert("Ошибка", "Заполните все поля!");
                return;
            }

            try {
                User existing = db.getUserByLogin(login);
                if (existing != null) {
                    showAlert("Ошибка", "Пользователь с таким логином уже существует!");
                    return;
                }

                db.registerUser(login, password, role);
                showAlert("Успех", "Регистрация прошла успешно! Теперь войдите.");
                showLoginWindow(primaryStage);
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Ошибка", "Ошибка регистрации: " + ex.getMessage());
            }
        });

        backButton.setOnAction(e -> showLoginWindow(primaryStage));

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                new Label("Регистрация"),
                loginField,
                passwordField,
                roleBox,
                registerButton,
                backButton
        );

        Scene scene = new Scene(layout, 300, 300);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setTitle("Журнал - Регистрация");
        primaryStage.setScene(scene);
    }

    private void openMainWindow(Stage primaryStage, User user) {
        if ("ADMIN".equals(user.getUserRole())) {
            AdminPanel adminPanel = new AdminPanel(db, user, primaryStage);
            adminPanel.show();
        } else {
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(20));
            layout.setAlignment(Pos.CENTER);

            Label welcomeLabel = new Label("Добро пожаловать, " + user.getUserLogin() + "!");
            Label roleLabel = new Label("Ваша роль: " + user.getUserRole());

            Button logoutButton = new Button("Выйти");
            logoutButton.setOnAction(e -> showLoginWindow(primaryStage));

            layout.getChildren().addAll(welcomeLabel, roleLabel, logoutButton);

            Scene scene = new Scene(layout, 400, 300);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            primaryStage.setTitle("Журнал - Главная");
            primaryStage.setScene(scene);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        try {
            if (db != null) db.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
