package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TeacherPanel {

    private SQLmanager db;
    private User currentUser;
    private Stage primaryStage;
    private Teacher currentTeacher;
    private List<String> lessonDates = new ArrayList<>();
    private Map<String, Map<Integer, GradeCell>> gradesData = new HashMap<>();

    private YearMonth currentMonth;
    private Label monthLabel;
    private TableView<StudentRow> table;
    private ComboBox<String> classCombo;
    private List<String> teacherClasses = new ArrayList<>();

    private Stage currentPopup;

    private static final String[] MONTHS_NOMINATIVE = {
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    };

    public TeacherPanel(SQLmanager db, User currentUser, Stage primaryStage) {
        this.db = db;
        this.currentUser = currentUser;
        this.primaryStage = primaryStage;
        this.currentMonth = YearMonth.now();
        try {
            this.currentTeacher = db.getTeacherByUserId(currentUser.getUserId());
            String classNames = currentTeacher.getClassNames();
            if (classNames != null && !classNames.isEmpty()) {
                String[] classes = classNames.split(",");
                for (String c : classes) {
                    teacherClasses.add(c.trim());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void show() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label titleLabel = new Label("Журнал: " + getSubjectName());
        titleLabel.setStyle("-fx-font-size: 22px;");

        Label infoLabel = new Label("Учитель: " + currentTeacher.getTeacherFullName());
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

        classCombo = new ComboBox<>();
        classCombo.getItems().addAll(teacherClasses);
        classCombo.setMaxWidth(300);
        classCombo.setPromptText("Выберите класс");
        if (!teacherClasses.isEmpty()) {
            classCombo.setValue(teacherClasses.get(0));
        }
        classCombo.setOnAction(e -> buildMonthTable());

        table = new TableView<>();
        table.setMaxHeight(550);

        Button exportBtn = new Button("Экспорт в Excel");
        exportBtn.setMaxWidth(300);
        exportBtn.setStyle("-fx-background-color: #3ba55d;");
        exportBtn.setOnAction(e -> exportToExcel());

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

        layout.getChildren().addAll(titleLabel, infoLabel, monthNav, classCombo, table, exportBtn, logoutBtn);

        Scene scene = new Scene(layout, 1000, 750);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Журнал - Преподаватель");
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

    private AverageResult calculateAverageForPeriod(int studentId, List<Grade> allGrades, String startDate, String endDate) {
        List<String> numericGrades = new ArrayList<>();
        List<String> letterGrades = new ArrayList<>();

        for (Grade g : allGrades) {
            if (g.getStudentId() != studentId) continue;
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

    private String formatAverage(StudentRow row) {
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
        closeCurrentPopup();
        lessonDates.clear();
        gradesData.clear();
        table.getColumns().clear();
        table.getItems().clear();

        LocalDate firstDay = currentMonth.atDay(1);
        LocalDate lastDay = currentMonth.atEndOfMonth();

        LocalDate current = firstDay;
        while (!current.isAfter(lastDay)) {
            lessonDates.add(current.toString());
            current = current.plusDays(1);
        }

        try {
            loadGrades();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        TableColumn<StudentRow, String> nameCol = new TableColumn<>("Обучающийся");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().studentName));
        nameCol.setPrefWidth(200);
        nameCol.setResizable(false);
        table.getColumns().add(nameCol);

        TableColumn<StudentRow, String> avgCol = new TableColumn<>("Средний балл");
        avgCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(formatAverage(data.getValue())));
        avgCol.setPrefWidth(100);
        avgCol.setResizable(false);
        avgCol.setCellFactory(col -> {
            TableCell<StudentRow, String> cell = new TableCell<StudentRow, String>() {
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
                            StudentRow row = getTableRow().getItem();
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

            TableColumn<StudentRow, String> dateCol = new TableColumn<>(formatDate(finalDate));
            dateCol.setPrefWidth(65);
            dateCol.setResizable(false);
            dateCol.setCellValueFactory(data -> {
                GradeCell cell = getCell(data.getValue().studentId, finalDate);
                return new javafx.beans.property.SimpleStringProperty(cell != null ? cell.getValue() : "");
            });

            dateCol.setCellFactory(col -> {
                TableCell<StudentRow, String> cell = new TableCell<StudentRow, String>() {
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
                                setStyle("-fx-background-color: transparent; -fx-border-color: #5865f2; -fx-border-width: 2; -fx-cursor: hand; -fx-text-fill: white; -fx-font-weight: bold;");
                                StudentRow row = getTableRow().getItem();
                                if (row != null) {
                                    showGradePopup(row, finalDate, event.getScreenX(), event.getScreenY());
                                }
                            });
                        }
                    }
                };
                return cell;
            });

            table.getColumns().add(dateCol);
        }

        String selectedClass = classCombo.getValue();
        if (selectedClass != null) {
            try {
                List<Student> students = db.getAllStudents();
                List<Grade> allGrades = db.getGradesByTeacherId(currentTeacher.getTeacherId());

                String startDate = currentMonth.atDay(1).toString();
                String endDate = currentMonth.atEndOfMonth().toString();

                for (Student s : students) {
                    if (s.getClassName().equals(selectedClass)) {
                        AverageResult avgResult = calculateAverageForPeriod(s.getStudentId(), allGrades, startDate, endDate);
                        table.getItems().add(new StudentRow(s.getStudentId(), s.getStudentFullName(), avgResult.numericValue, avgResult.letterValue, avgResult.isLetterBased));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showAverageDialog(StudentRow student) {
        Stage dialogStage = new Stage();
        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        dialogStage.setTitle("Средний балл: " + student.studentName);

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
                List<Grade> allGrades = db.getGradesByTeacherId(currentTeacher.getTeacherId());
                AverageResult result = calculateAverageForPeriod(student.studentId, allGrades, start, end);
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

    private boolean hasNumericGrades(int studentId) {
        for (Map<Integer, GradeCell> dateMap : gradesData.values()) {
            GradeCell cell = dateMap.get(studentId);
            if (cell != null && cell.getValue() != null && cell.getValue().matches("\\d+")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasLetterGrades(int studentId) {
        for (Map<Integer, GradeCell> dateMap : gradesData.values()) {
            GradeCell cell = dateMap.get(studentId);
            if (cell != null && cell.getValue() != null && cell.getValue().matches("[A-F]")) {
                return true;
            }
        }
        return false;
    }

    private void closeCurrentPopup() {
        if (currentPopup != null) {
            currentPopup.close();
            currentPopup = null;
        }
    }

    private void exportToExcel() {
        try {
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("Журнал");

            org.apache.poi.xssf.usermodel.XSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Обучающийся");
            headerRow.createCell(1).setCellValue("Средний балл");

            int colIndex = 2;
            for (String date : lessonDates) {
                headerRow.createCell(colIndex++).setCellValue(formatDate(date));
            }

            int rowIndex = 1;
            for (StudentRow row : table.getItems()) {
                org.apache.poi.xssf.usermodel.XSSFRow excelRow = sheet.createRow(rowIndex++);
                excelRow.createCell(0).setCellValue(row.studentName);
                excelRow.createCell(1).setCellValue(formatAverage(row));

                colIndex = 2;
                for (String date : lessonDates) {
                    GradeCell cell = getCell(row.studentId, date);
                    excelRow.createCell(colIndex++).setCellValue(cell != null ? cell.getValue() : "");
                }
            }

            String fileName = "journal_" + currentMonth.getYear() + "_" + currentMonth.getMonthValue() + ".xlsx";
            java.io.FileOutputStream out = new java.io.FileOutputStream(fileName);
            workbook.write(out);
            out.close();
            workbook.close();

            showAlert("Успех", "Файл сохранён: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось экспортировать: " + e.getMessage());
        }
    }

    private void showGradePopup(StudentRow student, String date, double screenX, double screenY) {
        closeCurrentPopup();

        Stage popupStage = new Stage();
        popupStage.initStyle(StageStyle.UNDECORATED);
        popupStage.initOwner(primaryStage);
        currentPopup = popupStage;

        Label infoLabel = new Label(student.studentName + " | " + formatDate(date));
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #b9bbbe; -fx-padding: 5px;");

        String[] row1 = {"5", "4", "3", "2", "1"};
        String[] row2 = {"A", "B", "C", "D", "E", "F"};
        String[] row3 = {"Н", "Б", "УП"};

        boolean hasNumeric = hasNumericGrades(student.studentId);
        boolean hasLetter = hasLetterGrades(student.studentId);

        HBox buttonsRow1 = createButtonRow(row1, student, date, popupStage, hasLetter);
        HBox buttonsRow2 = createButtonRow(row2, student, date, popupStage, hasNumeric);
        HBox buttonsRow3 = createButtonRow(row3, student, date, popupStage, false);

        TextField commentField = new TextField();
        commentField.setId("commentField");
        commentField.setPromptText("Комментарий (например, Контрольная)");
        commentField.setMaxWidth(280);
        commentField.setStyle("-fx-background-color: #1e1f22; -fx-text-fill: #dbdee1; -fx-border-color: #3f4147; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-padding: 5px;");

        GradeCell existing = getCell(student.studentId, date);
        if (existing != null) {
            commentField.setText(existing.getComment() != null ? existing.getComment() : "");
        }

        Button deleteBtn = new Button("Удалить");
        deleteBtn.setStyle("-fx-background-color: #ed4245; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;");
        deleteBtn.setOnAction(e -> {
            GradeCell ex = getCell(student.studentId, date);
            if (ex != null && ex.getGradeId() > 0) {
                try {
                    db.deleteGrade(ex.getGradeId());
                    buildMonthTable();
                } catch (SQLException ex2) {
                    ex2.printStackTrace();
                }
            }
            closeCurrentPopup();
        });

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: #4f545c; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;");
        closeBtn.setOnAction(e -> closeCurrentPopup());

        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER);
        bottomRow.getChildren().addAll(deleteBtn, closeBtn);

        VBox layout = new VBox(5);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-background-color: #2b2d31; -fx-border-color: #5865f2; -fx-border-width: 1;");
        layout.getChildren().addAll(infoLabel, buttonsRow1, buttonsRow2, buttonsRow3, commentField, bottomRow);

        Scene scene = new Scene(layout);
        popupStage.setScene(scene);

        popupStage.setX(screenX + 5);
        popupStage.setY(screenY - 180);

        popupStage.show();
    }

    private HBox createButtonRow(String[] values, StudentRow student, String date, Stage popupStage, boolean disableButtons) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER);
        for (String value : values) {
            Button btn = new Button(value);
            if (disableButtons) {
                btn.setStyle("-fx-background-color: #4f545c; -fx-text-fill: #72767d; -fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 35px; -fx-padding: 5px; -fx-cursor: default;");
                btn.setDisable(true);
            } else {
                btn.setStyle("-fx-background-color: #5865f2; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 35px; -fx-padding: 5px;");
                btn.setOnAction(e -> {
                    TextField commentField = (TextField) popupStage.getScene().getRoot().lookup("#commentField");
                    String comment = commentField != null ? commentField.getText().trim() : "";
                    saveGrade(student, date, value, comment);
                    buildMonthTable();
                    closeCurrentPopup();
                });
            }
            row.getChildren().add(btn);
        }
        return row;
    }

    private void saveGrade(StudentRow student, String date, String value, String comment) {
        try {
            GradeCell existing = getCell(student.studentId, date);
            if (existing != null && existing.getGradeId() > 0) {
                String sql = "UPDATE Grades SET value = ?, comment = ? WHERE gradeId = ?";
                java.sql.PreparedStatement pstmt = db.getConnection().prepareStatement(sql);
                pstmt.setString(1, value);
                pstmt.setString(2, comment);
                pstmt.setInt(3, existing.getGradeId());
                pstmt.executeUpdate();
                pstmt.close();
            } else {
                Grade newGrade = new Grade(
                        student.studentId,
                        currentTeacher.getTeacherId(),
                        currentTeacher.getSubject(),
                        value,
                        date,
                        comment
                );
                db.addGrade(newGrade);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Ошибка", "Не удалось сохранить: " + ex.getMessage());
        }
    }

    private void loadGrades() throws SQLException {
        List<Grade> grades = db.getGradesByTeacherId(currentTeacher.getTeacherId());
        for (Grade g : grades) {
            String date = g.getDate();
            String value = g.getValue() != null ? g.getValue() : "";
            String comment = g.getComment() != null ? g.getComment() : "";
            GradeCell cell = new GradeCell(g.getStudentId(), date, value, g.getGradeId(), comment);

            Map<Integer, GradeCell> dateMap = gradesData.get(date);
            if (dateMap == null) {
                dateMap = new HashMap<>();
                gradesData.put(date, dateMap);
            }
            dateMap.put(g.getStudentId(), cell);
        }
    }

    private GradeCell getCell(int studentId, String date) {
        Map<Integer, GradeCell> dateMap = gradesData.get(date);
        if (dateMap != null) {
            return dateMap.get(studentId);
        }
        return null;
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

    private String getSubjectName() {
        try {
            Subject subject = db.getSubjectById(currentTeacher.getSubject());
            return subject != null ? subject.getSubjectName() : "Не назначен";
        } catch (SQLException e) {
            return "Ошибка загрузки";
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class StudentRow {
        int studentId;
        String studentName;
        double averageGrade;
        String averageGradeLetter;
        boolean isLetterBased;

        StudentRow(int studentId, String studentName, double averageGrade, String averageGradeLetter, boolean isLetterBased) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.averageGrade = averageGrade;
            this.averageGradeLetter = averageGradeLetter;
            this.isLetterBased = isLetterBased;
        }
    }
}
