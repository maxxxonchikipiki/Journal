package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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

        TextField classNamesField = new TextField();
        classNamesField.setPromptText("Классы (например, 9А, 10Б)");
        classNamesField.setMaxWidth(300);

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
            String classNames = classNamesField.getText().trim();
            Subject selectedSubject = subjectCombo.getValue();

            if (fullName.isEmpty() || classNames.isEmpty() || selectedSubject == null) {
                showAlert("Ошибка", "Заполните все поля и выберите предмет!");
                return;
            }

            try {
                User teacherUser = db.addTeacherWithAutoLogin(fullName, selectedSubject.getSubjectId(), classNames);
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
        layout.getChildren().addAll(titleLabel, fullNameField, classNamesField, subjectCombo, addButton, cancelButton);

        Scene scene = new Scene(layout, 400, 400);
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

        TextField classField = new TextField();
        classField.setPromptText("Класс (например, 9А)");
        classField.setMaxWidth(300);

        Button addButton = new Button("Добавить");
        addButton.setMaxWidth(300);
        addButton.setOnAction(e -> {
            String fullName = fullNameField.getText().trim();
            String className = classField.getText().trim();

            if (fullName.isEmpty() || className.isEmpty()) {
                showAlert("Ошибка", "Введите ФИО и класс ученика!");
                return;
            }

            try {
                User studentUser = db.addStudentWithAutoLogin(fullName, className);
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
        layout.getChildren().addAll(titleLabel, fullNameField, classField, addButton, cancelButton);

        Scene scene = new Scene(layout, 400, 350);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private void showAllUsers() {
        try {
            List<User> users = db.getAllUsers();
            List<Student> students = db.getAllStudents();
            List<Teacher> teachers = db.getAllTeachers();
            List<Subject> subjects = db.getAllSubjects();

            Stage dialogStage = new Stage();
            dialogStage.initOwner(primaryStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Все пользователи");

            TableView<User> table = new TableView<>();
            table.setMaxWidth(900);
            table.setMaxHeight(400);

            TableColumn<User, String> loginCol = new TableColumn<>("Логин");
            loginCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserLogin()));
            loginCol.setPrefWidth(100);

            TableColumn<User, String> passwordCol = new TableColumn<>("Пароль");
            passwordCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserPassword()));
            passwordCol.setPrefWidth(100);

            TableColumn<User, String> roleCol = new TableColumn<>("Роль");
            roleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserRole()));
            roleCol.setPrefWidth(80);

            TableColumn<User, String> nameCol = new TableColumn<>("ФИО");
            nameCol.setCellValueFactory(data -> {
                User u = data.getValue();
                String name = "—";
                if ("STUDENT".equals(u.getUserRole())) {
                    for (Student s : students) {
                        if (s.getUserId() == u.getUserId()) {
                            name = s.getStudentFullName();
                            break;
                        }
                    }
                } else if ("TEACHER".equals(u.getUserRole())) {
                    for (Teacher t : teachers) {
                        if (t.getUserId() == u.getUserId()) {
                            name = t.getTeacherFullName();
                            break;
                        }
                    }
                } else if ("ADMIN".equals(u.getUserRole())) {
                    name = "Администратор";
                }
                return new javafx.beans.property.SimpleStringProperty(name);
            });
            nameCol.setPrefWidth(180);

            TableColumn<User, String> classCol = new TableColumn<>("Класс/Предмет");
            classCol.setCellValueFactory(data -> {
                User u = data.getValue();
                String info = "—";
                if ("STUDENT".equals(u.getUserRole())) {
                    for (Student s : students) {
                        if (s.getUserId() == u.getUserId()) {
                            info = s.getClassName();
                            break;
                        }
                    }
                } else if ("TEACHER".equals(u.getUserRole())) {
                    for (Teacher t : teachers) {
                        if (t.getUserId() == u.getUserId()) {
                            try {
                                Subject subj = db.getSubjectById(t.getSubject());
                                info = t.getClassNames() + " (" + (subj != null ? subj.getSubjectName() : "—") + ")";
                            } catch (SQLException ex) {
                                info = t.getClassNames() + " (ошибка)";
                            }
                            break;
                        }
                    }
                }
                return new javafx.beans.property.SimpleStringProperty(info);
            });
            classCol.setPrefWidth(150);

            TableColumn<User, Void> actionCol = new TableColumn<>("Действия");
            actionCol.setPrefWidth(150);
            actionCol.setCellFactory(col -> {
                TableCell<User, Void> cell = new TableCell<User, Void>() {
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            User user = getTableRow().getItem();
                            HBox buttons = new HBox(5);
                            buttons.setAlignment(Pos.CENTER);

                            if (!"ADMIN".equals(user.getUserRole())) {
                                Button editBtn = new Button("✏️");
                                editBtn.setStyle("-fx-background-color: #5865f2; -fx-text-fill: white; -fx-font-size: 12px; -fx-min-width: 30px;");
                                editBtn.setOnAction(e -> showEditUserDialog(user, students, teachers, subjects));

                                Button deleteBtn = new Button("🗑");
                                deleteBtn.setStyle("-fx-background-color: #ed4245; -fx-text-fill: white; -fx-font-size: 12px; -fx-min-width: 30px;");
                                deleteBtn.setOnAction(e -> deleteUser(user));

                                buttons.getChildren().addAll(editBtn, deleteBtn);
                            }

                            setGraphic(buttons);
                        }
                    }
                };
                return cell;
            });

            table.getColumns().addAll(loginCol, passwordCol, roleCol, nameCol, classCol, actionCol);
            table.getItems().addAll(users);

            Button closeButton = new Button("Закрыть");
            closeButton.setMaxWidth(200);
            closeButton.setOnAction(e -> dialogStage.close());

            VBox layout = new VBox(15);
            layout.setPadding(new Insets(20));
            layout.setAlignment(Pos.CENTER);
            layout.getChildren().addAll(new Label("Список пользователей"), table, closeButton);

            Scene scene = new Scene(layout, 950, 550);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            dialogStage.setScene(scene);
            dialogStage.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить список пользователей");
        }
    }

    private void showEditUserDialog(User user, List<Student> students, List<Teacher> teachers, List<Subject> subjects) {
        Stage editStage = new Stage();
        editStage.initOwner(primaryStage);
        editStage.initModality(Modality.WINDOW_MODAL);
        editStage.setTitle("Редактировать: " + user.getUserLogin());

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Редактирование пользователя");
        titleLabel.setStyle("-fx-font-size: 20px;");

        if ("STUDENT".equals(user.getUserRole())) {
            Student student = students.stream().filter(s -> s.getUserId() == user.getUserId()).findFirst().orElse(null);
            if (student != null) {
                TextField classField = new TextField(student.getClassName());
                classField.setPromptText("Класс (например, 9А)");
                classField.setMaxWidth(300);

                Button saveBtn = new Button("Сохранить");
                saveBtn.setMaxWidth(300);
                saveBtn.setOnAction(e -> {
                    try {
                        db.updateStudentClass(user.getUserId(), classField.getText().trim());
                        showAlert("Успех", "Класс обновлён!");
                        editStage.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert("Ошибка", "Не удалось обновить: " + ex.getMessage());
                    }
                });

                Button cancelBtn = new Button("Отмена");
                cancelBtn.setMaxWidth(300);
                cancelBtn.setStyle("-fx-background-color: #4f545c;");
                cancelBtn.setOnAction(e -> editStage.close());

                layout.getChildren().addAll(titleLabel, new Label("Текущий класс: " + student.getClassName()), classField, saveBtn, cancelBtn);
            }
        } else if ("TEACHER".equals(user.getUserRole())) {
            Teacher teacher = teachers.stream().filter(t -> t.getUserId() == user.getUserId()).findFirst().orElse(null);
            if (teacher != null) {
                TextField classNamesField = new TextField(teacher.getClassNames());
                classNamesField.setPromptText("Классы (например, 9А, 10Б)");
                classNamesField.setMaxWidth(300);

                ComboBox<Subject> subjectCombo = new ComboBox<>();
                subjectCombo.getItems().addAll(subjects);
                subjectCombo.setPromptText("Выберите предмет");
                subjectCombo.setMaxWidth(300);
                subjectCombo.setValue(subjects.stream().filter(s -> s.getSubjectId() == teacher.getSubject()).findFirst().orElse(null));

                Button saveBtn = new Button("Сохранить");
                saveBtn.setMaxWidth(300);
                saveBtn.setOnAction(e -> {
                    Subject selected = subjectCombo.getValue();
                    if (selected == null) {
                        showAlert("Ошибка", "Выберите предмет!");
                        return;
                    }
                    try {
                        db.updateTeacher(user.getUserId(), selected.getSubjectId(), classNamesField.getText().trim());
                        showAlert("Успех", "Данные учителя обновлены!");
                        editStage.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert("Ошибка", "Не удалось обновить: " + ex.getMessage());
                    }
                });

                Button cancelBtn = new Button("Отмена");
                cancelBtn.setMaxWidth(300);
                cancelBtn.setStyle("-fx-background-color: #4f545c;");
                cancelBtn.setOnAction(e -> editStage.close());

                layout.getChildren().addAll(titleLabel, new Label("Текущие классы: " + teacher.getClassNames()), classNamesField, subjectCombo, saveBtn, cancelBtn);
            }
        }

        Scene scene = new Scene(layout, 400, 400);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        editStage.setScene(scene);
        editStage.showAndWait();
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удалить пользователя " + user.getUserLogin() + "?");
        confirm.setContentText("Это действие нельзя отменить.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    db.deleteUser(user.getUserId());
                    showAlert("Успех", "Пользователь удалён!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Ошибка", "Не удалось удалить: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
