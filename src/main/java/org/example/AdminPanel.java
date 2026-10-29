package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.sql.SQLException;
import java.util.List;

public class AdminPanel {

    private SQLmanager db;
    private User currentUser;
    private Stage primaryStage;

    public AdminPanel(SQLmanager db, User currentUser, Stage primaryStage) {
        this.db = db;
        this.currentUser = currentUser;
        this.primaryStage = primaryStage;
    }

    public void show() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Панель администратора");
        titleLabel.setStyle("-fx-font-size: 22px;");

        Label welcomeLabel = new Label("Добро пожаловать, " + currentUser.getUserLogin() + "!");
        welcomeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b9bbbe;");

        Button addSubjectBtn = new Button("Добавить предмет");
        Button addTeacherBtn = new Button("Добавить учителя");
        Button addStudentBtn = new Button("Добавить ученика");
        Button viewUsersBtn = new Button("Просмотреть всех пользователей");
        Button logoutBtn = new Button("Выйти");

        addSubjectBtn.setMaxWidth(300);
        addTeacherBtn.setMaxWidth(300);
        addStudentBtn.setMaxWidth(300);
        viewUsersBtn.setMaxWidth(300);
        logoutBtn.setMaxWidth(300);

        addSubjectBtn.setOnAction(e -> showAddSubjectDialog());
        addTeacherBtn.setOnAction(e -> showAddTeacherDialog());
        addStudentBtn.setOnAction(e -> showAddStudentDialog());
        viewUsersBtn.setOnAction(e -> showAllUsers());
        logoutBtn.setOnAction(e -> {
            try {
                new LoginApp().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(
                titleLabel,
                welcomeLabel,
                addSubjectBtn,
                addTeacherBtn,
                addStudentBtn,
                viewUsersBtn,
                logoutBtn
        );

        Scene scene = new Scene(layout, 500, 500);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Журнал - Админ-панель");
        primaryStage.setScene(scene);
    }

    private void showAddSubjectDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Добавить предмет");
        dialog.setHeaderText("Введите название предмета:");
        dialog.setContentText("Название:");

        dialog.showAndWait().ifPresent(subjectName -> {
            try {
                Subject subject = new Subject(subjectName);
                db.addSubject(subject);
                showAlert("Успех", "Предмет '" + subjectName + "' добавлен!");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Ошибка", "Не удалось добавить предмет: " + e.getMessage());
            }
        });
    }

    private void showAddTeacherDialog() {
        Stage dialogStage = new Stage();
        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setTitle("Добавить учителя");

        Label titleLabel = new Label("Добавить учителя");
        titleLabel.setStyle("-fx-font-size: 20px;");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("ФИО учителя");
        fullNameField.setMaxWidth(300);

        ComboBox<Subject> subjectCombo = new ComboBox<>();
        subjectCombo.setPromptText("Выберите предмет");
        subjectCombo.setMaxWidth(300);
        try {
            subjectCombo.getItems().addAll(db.getAllSubjects());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить предметы");
            return;
        }

        Button addButton = new Button("Добавить");
        addButton.setMaxWidth(300);
        addButton.setOnAction(e -> {
            String fullName = fullNameField.getText().trim();
            Subject selectedSubject = subjectCombo.getValue();

            if (fullName.isEmpty() || selectedSubject == null) {
                showAlert("Ошибка", "Введите ФИО и выберите предмет!");
                return;
            }

            try {
                User teacherUser = db.addTeacherWithAutoLogin(fullName, selectedSubject.getSubjectId());
                showAlert("Успех", "Учитель добавлен!\nЛогин: " + teacherUser.getUserLogin() +
                        "\nПароль: " + teacherUser.getUserPassword());
                dialogStage.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Ошибка", "Не удалось добавить учителя: " + ex.getMessage());
            }
        });

        Button cancelButton = new Button("Отмена");
        cancelButton.setMaxWidth(300);
        cancelButton.setStyle("-fx-background-color: #4f545c;");
        cancelButton.setOnAction(e -> dialogStage.close());

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(titleLabel, fullNameField, subjectCombo, addButton, cancelButton);

        Scene scene = new Scene(layout, 400, 350);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private void showAddStudentDialog() {
        Stage dialogStage = new Stage();
        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setTitle("Добавить ученика");

        Label titleLabel = new Label("Добавить ученика");
        titleLabel.setStyle("-fx-font-size: 20px;");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("ФИО ученика");
        fullNameField.setMaxWidth(300);

        Button addButton = new Button("Добавить");
        addButton.setMaxWidth(300);
        addButton.setOnAction(e -> {
            String fullName = fullNameField.getText().trim();

            if (fullName.isEmpty()) {
                showAlert("Ошибка", "Введите ФИО ученика!");
                return;
            }

            try {
                User studentUser = db.addStudentWithAutoLogin(fullName);
                showAlert("Успех", "Ученик добавлен!\nЛогин: " + studentUser.getUserLogin() +
                        "\nПароль: " + studentUser.getUserPassword());
                dialogStage.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Ошибка", "Не удалось добавить ученика: " + ex.getMessage());
            }
        });

        Button cancelButton = new Button("Отмена");
        cancelButton.setMaxWidth(300);
        cancelButton.setStyle("-fx-background-color: #4f545c;");
        cancelButton.setOnAction(e -> dialogStage.close());

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(titleLabel, fullNameField, addButton, cancelButton);

        Scene scene = new Scene(layout, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private void showAllUsers() {
        try {
            List<User> users = db.getAllUsers();
            StringBuilder userList = new StringBuilder();
            for (User user : users) {
                userList.append("Логин: ").append(user.getUserLogin())
                        .append(", Роль: ").append(user.getUserRole())
                        .append("\n");
            }
            showAlert("Список пользователей", userList.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить список пользователей");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
