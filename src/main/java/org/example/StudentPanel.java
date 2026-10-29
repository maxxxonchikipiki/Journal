package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StudentPanel {

    private SQLmanager db;
    private User currentUser;
    private Stage primaryStage;
    private Student currentStudent;

    private YearMonth currentMonth;
    private Label monthLabel;
    private TableView<SubjectRow> table;

    private static final String[] MONTHS_NOMINATIVE = {
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    };

    public StudentPanel(SQLmanager db, User currentUser, Stage primaryStage) {
        this.db = db;
        this.currentUser = currentUser;
        this.primaryStage = primaryStage;
        this.currentMonth = YearMonth.now();
        try {
            this.currentStudent = db.getStudentByUserId(currentUser.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void show() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label titleLabel = new Label("Мой журнал");
        titleLabel.setStyle("-fx-font-size: 22px;");

        Label infoLabel = new Label("Ученик: " + currentStudent.getStudentFullName() +
                " | Класс: " + currentStudent.getClassName());
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b9bbbe;");

        HBox monthNav = new HBox(15);
        monthNav.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("◀");
        prevBtn.setStyle("-fx-font-size: 18px; -fx-min-width: 50px;");
        prevBtn.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            updateMonthLabel();
            buildMonthTable();
        });

        monthLabel = new Label(formatMonth(currentMonth));
        monthLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button nextBtn = new Button("▶");
        nextBtn.setStyle("-fx-font-size: 18px; -fx-min-width: 50px;");
        nextBtn.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            updateMonthLabel();
            buildMonthTable();
        });

        monthNav.getChildren().addAll(prevBtn, monthLabel, nextBtn);

        table = new TableView<>();
        table.setMaxHeight(550);

        Button logoutBtn = new Button("Выйти");
        logoutBtn.setMaxWidth(300);
        logoutBtn.setStyle("-fx-background-color: #ed4245;");
        logoutBtn.setOnAction(e -> {
            try {
                new LoginApp().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(titleLabel, infoLabel, monthNav, table, logoutBtn);

        Scene scene = new Scene(layout, 1000, 750);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Журнал - Ученик");
        primaryStage.setScene(scene);

        buildMonthTable();
    }

    private void updateMonthLabel() {
        monthLabel.setText(formatMonth(currentMonth));
    }

    private String formatMonth(YearMonth ym) {
        int monthIndex = ym.getMonthValue() - 1;
        return MONTHS_NOMINATIVE[monthIndex] + " " + ym.getYear();
    }

    private double letterToNumber(String letter) {
        switch (letter) {
            case "A": return 5.0;
            case "B": return 4.5;
            case "C": return 3.75;
            case "D": return 3.25;
            case "E": return 2.75;
            case "F": return 2.0;
            default: return 0;
        }
    }

    private String numberToLetter(double num) {
        if (num >= 4.75) return "A";
        if (num >= 4.25) return "B";
        if (num >= 3.875) return "C";
        if (num >= 3.375) return "D";
        if (num >= 2.875) return "E";
        return "F";
    }

    private static class AverageResult {
        double numericValue;
        String letterValue;
        boolean isLetterBased;

        AverageResult(double numericValue, String letterValue, boolean isLetterBased) {
            this.numericValue = numericValue;
            this.letterValue = letterValue;
            this.isLetterBased = isLetterBased;
        }
    }

    private AverageResult calculateAverageForPeriod(List<Grade> allGrades, int subjectId, String startDate, String endDate) {
        List<String> numericGrades = new ArrayList<>();
        List<String> letterGrades = new ArrayList<>();

        for (Grade g : allGrades) {
            if (g.getSubjectId() != subjectId) continue;
            String date = g.getDate();
            if (date.compareTo(startDate) < 0 || date.compareTo(endDate) > 0) continue;

            String value = g.getValue();
            if (value == null || value.isEmpty()) continue;
            if (value.equals("Н") || value.equals("Б") || value.equals("УП")) continue;

            if (value.matches("\\d+")) {
                numericGrades.add(value);
            } else if (value.matches("[A-F]")) {
                letterGrades.add(value);
            }
        }

        if (!numericGrades.isEmpty()) {
            int sum = 0;
            for (String v : numericGrades) sum += Integer.parseInt(v);
            double avg = Math.round((sum / (double) numericGrades.size()) * 100.0) / 100.0;
            return new AverageResult(avg, null, false);
        } else if (!letterGrades.isEmpty()) {
            double sum = 0;
            for (String v : letterGrades) sum += letterToNumber(v);
            double avg = sum / letterGrades.size();
            String letter = numberToLetter(avg);
            return new AverageResult(avg, letter, true);
        } else {
            return new AverageResult(0, null, false);
        }
    }

    private String formatAverage(SubjectRow row) {
        if (row.isLetterBased) {
            return row.averageGradeLetter != null ? row.averageGradeLetter : "—";
        }
        if (row.averageGrade == 0) return "0.0";
        String formatted = String.format("%.2f", row.averageGrade);
        if (formatted.endsWith("0")) {
            return formatted.substring(0, formatted.length() - 1);
        }
        return formatted;
    }

    private void buildMonthTable() {
        table.getColumns().clear();
        table.getItems().clear();

        LocalDate firstDay = currentMonth.atDay(1);
        LocalDate lastDay = currentMonth.atEndOfMonth();

        List<String> lessonDates = new ArrayList<>();
        LocalDate current = firstDay;
        while (!current.isAfter(lastDay)) {
            lessonDates.add(current.toString());
            current = current.plusDays(1);
        }

        TableColumn<SubjectRow, String> subjectCol = new TableColumn<>("Предмет");
        subjectCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().subjectName));
        subjectCol.setPrefWidth(200);
        subjectCol.setResizable(false);
        table.getColumns().add(subjectCol);

        TableColumn<SubjectRow, String> avgCol = new TableColumn<>("Средний балл");
        avgCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(formatAverage(data.getValue())));
        avgCol.setPrefWidth(100);
        avgCol.setResizable(false);
        avgCol.setCellFactory(col -> {
            TableCell<SubjectRow, String> cell = new TableCell<SubjectRow, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setAlignment(Pos.CENTER);
                    if (empty || item == null) {
                        setStyle("-fx-background-color: transparent; -fx-border-color: #3f4147; -fx-border-width: 0.5; -fx-cursor: hand;");
                        setOnMouseClicked(null);
                    } else {
                        setStyle("-fx-background-color: transparent; -fx-border-color: #3f4147; -fx-border-width: 0.5; -fx-cursor: hand; -fx-text-fill: white; -fx-font-weight: bold;");
                        setOnMouseClicked(event -> {
                            SubjectRow row = getTableRow().getItem();
                            if (row != null) {
                                showAverageDialog(row);
                            }
                        });
                    }
                }
            };
            return cell;
        });
        table.getColumns().add(avgCol);

        for (String date : lessonDates) {
            final String finalDate = date;

            TableColumn<SubjectRow, String> dateCol = new TableColumn<>(formatDate(finalDate));
            dateCol.setPrefWidth(65);
            dateCol.setResizable(false);
            dateCol.setCellValueFactory(data -> {
                String grade = data.getValue().grades.get(finalDate);
                return new javafx.beans.property.SimpleStringProperty(grade != null ? grade : "");
            });

            dateCol.setCellFactory(col -> {
                TableCell<SubjectRow, String> cell = new TableCell<SubjectRow, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : item);
                        setAlignment(Pos.CENTER);
                        if (empty || item == null) {
                            setStyle("-fx-background-color: transparent;");
                        } else {
                            setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;");
                        }
                    }
                };
                return cell;
            });

            table.getColumns().add(dateCol);
        }

        try {
            List<Grade> allGrades = db.getGradesByStudentId(currentStudent.getStudentId());
            List<Subject> allSubjects = db.getAllSubjects();

            String startDate = currentMonth.atDay(1).toString();
            String endDate = currentMonth.atEndOfMonth().toString();

            for (Subject subject : allSubjects) {
                Map<String, String> subjectGrades = new HashMap<>();

                for (Grade grade : allGrades) {
                    if (grade.getSubjectId() == subject.getSubjectId()) {
                        String value = grade.getValue() != null ? grade.getValue() : "";
                        subjectGrades.put(grade.getDate(), value);
                    }
                }

                AverageResult avgResult = calculateAverageForPeriod(allGrades, subject.getSubjectId(), startDate, endDate);
                table.getItems().add(new SubjectRow(subject.getSubjectName(), subjectGrades, avgResult.numericValue, avgResult.letterValue, avgResult.isLetterBased));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAverageDialog(SubjectRow subject) {
        Stage dialogStage = new Stage();
        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        dialogStage.setTitle("Средний балл: " + subject.subjectName);

        Label titleLabel = new Label("Выберите период:");
        titleLabel.setStyle("-fx-font-size: 18px;");

        TextField startDateField = new TextField(currentMonth.atDay(1).toString());
        startDateField.setPromptText("ГГГГ-ММ-ДД");
        startDateField.setMaxWidth(300);

        TextField endDateField = new TextField(currentMonth.atEndOfMonth().toString());
        endDateField.setPromptText("ГГГГ-ММ-ДД");
        endDateField.setMaxWidth(300);

        Label resultLabel = new Label("");
        resultLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button calcBtn = new Button("Рассчитать");
        calcBtn.setMaxWidth(300);
        calcBtn.setOnAction(e -> {
            String start = startDateField.getText().trim();
            String end = endDateField.getText().trim();
            if (!start.matches("\\d{4}-\\d{2}-\\d{2}") || !end.matches("\\d{4}-\\d{2}-\\d{2}")) {
                showAlert("Ошибка", "Неверный формат даты! Используйте ГГГГ-ММ-ДД");
                return;
            }
            try {
                List<Grade> allGrades = db.getGradesByStudentId(currentStudent.getStudentId());
                int subjectId = -1;
                for (Subject s : db.getAllSubjects()) {
                    if (s.getSubjectName().equals(subject.subjectName)) {
                        subjectId = s.getSubjectId();
                        break;
                    }
                }
                if (subjectId == -1) {
                    showAlert("Ошибка", "Предмет не найден");
                    return;
                }
                AverageResult result = calculateAverageForPeriod(allGrades, subjectId, start, end);
                if (result.isLetterBased) {
                    resultLabel.setText("Средний балл: " + result.letterValue);
                } else if (result.numericValue > 0) {
                    String formatted = String.format("%.2f", result.numericValue);
                    if (formatted.endsWith("0")) formatted = formatted.substring(0, formatted.length() - 1);
                    resultLabel.setText("Средний балл: " + formatted);
                } else {
                    resultLabel.setText("Нет оценок за период");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Ошибка", "Не удалось рассчитать: " + ex.getMessage());
            }
        });

        Button closeBtn = new Button("Закрыть");
        closeBtn.setMaxWidth(300);
        closeBtn.setStyle("-fx-background-color: #4f545c;");
        closeBtn.setOnAction(e -> dialogStage.close());

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(titleLabel, new Label("С:"), startDateField, new Label("По:"), endDateField, calcBtn, resultLabel, closeBtn);

        Scene scene = new Scene(layout, 350, 400);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private String formatDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
            return date.format(formatter);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class SubjectRow {
        String subjectName;
        Map<String, String> grades;
        double averageGrade;
        String averageGradeLetter;
        boolean isLetterBased;

        SubjectRow(String subjectName, Map<String, String> grades, double averageGrade, String averageGradeLetter, boolean isLetterBased) {
            this.subjectName = subjectName;
            this.grades = grades;
            this.averageGrade = averageGrade;
            this.averageGradeLetter = averageGradeLetter;
            this.isLetterBased = isLetterBased;
        }
    }
}
