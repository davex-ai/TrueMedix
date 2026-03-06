package org.example.project.hospitalmanagementsystem.controller.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import org.example.project.hospitalmanagementsystem.model.Complaint;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminComplaintsController {

    @FXML private TableView<Complaint>            complaintTable;
    @FXML private TableColumn<Complaint, String>  nameColumn, emailColumn, messageColumn,
            statusColumn, submittedAtColumn;
    @FXML private TableColumn<Complaint, String>  categoryColumn, urgencyColumn,
            phoneColumn, screenshotPathColumn;
    @FXML private TableColumn<Complaint, Void>    actionsColumn;

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterCategory, filterUrgency, filterStatus;
    @FXML private Label            resultsLabel, selectionInfoLabel;

    @FXML private Label statTotalLabel, statPendingLabel, statResolvedLabel,
            statHighUrgencyLabel, statRateLabel;

    @FXML private BarChart<String, Number> statusChart, categoryChart, urgencyChart;
    @FXML private CategoryAxis statusChartX, categoryChartX, urgencyChartX;
    @FXML private NumberAxis   statusChartY, categoryChartY, urgencyChartY;

    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private ObservableList<Complaint> complaints = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        setupActionsColumn();
        setupFilters();
        setupDoubleClick();
        setupSelectionListener();
        loadComplaints();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        phoneColumn.setCellValueFactory(cell -> cell.getValue().phoneProperty());
        categoryColumn.setCellValueFactory(cell -> cell.getValue().categoryProperty());
        messageColumn.setCellValueFactory(cell -> cell.getValue().messageProperty());
        submittedAtColumn.setCellValueFactory(cell -> cell.getValue().submittedAtProperty());
        screenshotPathColumn.setCellValueFactory(cell -> cell.getValue().screenshotPathProperty());

        urgencyColumn.setCellValueFactory(cell -> cell.getValue().urgencyProperty());
        urgencyColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String urgency, boolean empty) {
                super.updateItem(urgency, empty);
                if (empty || urgency == null) { setGraphic(null); setText(null); return; }
                String bg, fg;
                switch (urgency.toLowerCase()) {
                    case "high":   bg = "#fde8e8"; fg = "#b03030"; break;
                    case "medium": bg = "#fef6e4"; fg = "#c47e0a"; break;
                    case "low":    bg = "#d4f5e2"; fg = "#1a7a45"; break;
                    default:       bg = "#e8f0f8"; fg = "#3d5a73"; break;
                }
                Label badge = new Label(urgency);
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";");
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); setText(null); return; }
                String bg, fg;
                switch (status.toLowerCase()) {
                    case "resolved":   bg = "#d4f5e2"; fg = "#1a7a45"; break;
                    case "pending":    bg = "#fef6e4"; fg = "#c47e0a"; break;
                    case "responded":  bg = "#e8eeff"; fg = "#3b48c4"; break;
                    default:           bg = "#e8f0f8"; fg = "#3d5a73"; break;
                }
                Label badge = new Label(status);
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";");
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER_LEFT);
            }
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final Button btnView    = makeBtn("View",    "#e8f1fa", "#1e77d4");
            private final Button btnRespond = makeBtn("Respond", "#d4f5e2", "#1a7a45");
            private final Button btnDelete  = makeBtn("Delete",  "#fde8e8", "#b03030");

            private Button makeBtn(String label, String bg, String fg) {
                Button b = new Button(label);
                b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 9 4 9;");
                return b;
            }

            {
                btnView.setOnAction(e -> showDetailDialog(getTableView().getItems().get(getIndex())));
                btnRespond.setOnAction(e -> respondToComplaint(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> deleteComplaint(getTableView().getItems().get(getIndex())));
            }

            private final HBox box = new HBox(5, btnView, btnRespond, btnDelete);
            { box.setAlignment(Pos.CENTER_LEFT); }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupFilters() {
        filterCategory.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterUrgency.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterStatus.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void populateFilterCombos() {
        List<String> categories = complaints.stream()
                .map(Complaint::getCategory).distinct().sorted().collect(Collectors.toList());
        categories.add(0, "All Categories");
        filterCategory.setItems(FXCollections.observableArrayList(categories));
        filterCategory.setValue("All Categories");

        List<String> urgencies = complaints.stream()
                .map(Complaint::getUrgency).distinct().sorted().collect(Collectors.toList());
        urgencies.add(0, "All Urgencies");
        filterUrgency.setItems(FXCollections.observableArrayList(urgencies));
        filterUrgency.setValue("All Urgencies");

        List<String> statuses = complaints.stream()
                .map(Complaint::getStatus).distinct().sorted().collect(Collectors.toList());
        statuses.add(0, "All Statuses");
        filterStatus.setItems(FXCollections.observableArrayList(statuses));
        filterStatus.setValue("All Statuses");
    }

    private void applyFilters() {
        String search   = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String category = filterCategory.getValue();
        String urgency  = filterUrgency.getValue();
        String status   = filterStatus.getValue();

        ObservableList<Complaint> result = complaints.filtered(c -> {
            boolean matchSearch = search.isEmpty()
                    || c.getName().toLowerCase().contains(search)
                    || c.getMessage().toLowerCase().contains(search)
                    || c.getCategory().toLowerCase().contains(search);
            boolean matchCategory = category == null || category.equals("All Categories")
                    || c.getCategory().equalsIgnoreCase(category);
            boolean matchUrgency = urgency == null || urgency.equals("All Urgencies")
                    || c.getUrgency().equalsIgnoreCase(urgency);
            boolean matchStatus = status == null || status.equals("All Statuses")
                    || c.getStatus().equalsIgnoreCase(status);
            return matchSearch && matchCategory && matchUrgency && matchStatus;
        });

        complaintTable.setItems(result);
        resultsLabel.setText(result.size() + " record" + (result.size() == 1 ? "" : "s"));
        updateAnalytics(result);
    }

    private void setupDoubleClick() {
        complaintTable.setRowFactory(tv -> {
            TableRow<Complaint> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !row.isEmpty())
                    showDetailDialog(row.getItem());
            });
            return row;
        });
    }

    private void setupSelectionListener() {
        complaintTable.getSelectionModel().selectedItemProperty().addListener((obs, o, selected) -> {
            if (selected != null)
                selectionInfoLabel.setText("Selected: " + selected.getName() + " — " + selected.getCategory());
            else
                selectionInfoLabel.setText("No row selected");
        });
    }

    @FXML
    private void loadComplaints() {
        complaints.setAll(complaintDAO.getAllComplaints());
        populateFilterCombos();
        applyFilters();
        updateStats(complaints);
        updateAnalytics(complaints);
    }

    @FXML
    private void handleSearch(KeyEvent event) {
        applyFilters();
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        filterCategory.setValue("All Categories");
        filterUrgency.setValue("All Urgencies");
        filterStatus.setValue("All Statuses");
        applyFilters();
    }

    @FXML
    private void handleRespond() {
        Complaint selected = complaintTable.getSelectionModel().getSelectedItem();
        if (selected != null) respondToComplaint(selected);
    }

    private void respondToComplaint(Complaint c) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Respond to Complaint");
        dialog.setHeaderText("Complaint from: " + c.getName() + "  |  Category: " + c.getCategory());
        dialog.setContentText("Enter your response:");
        dialog.showAndWait().ifPresent(response -> {
            if (!response.trim().isEmpty()) {
                if (complaintDAO.respondToComplaint(c.getId(), response.trim())) {
                    loadComplaints();
                    showAlert(Alert.AlertType.INFORMATION, "Response Sent", "The complaint was successfully responded to.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to send response.");
                }
            }
        });
    }

    @FXML
    private void handleDelete() {
        Complaint selected = complaintTable.getSelectionModel().getSelectedItem();
        if (selected != null) deleteComplaint(selected);
    }

    private void deleteComplaint(Complaint c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete complaint from \"" + c.getName() + "\"?\nThis cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES && complaintDAO.deleteComplaint(c.getId()))
                loadComplaints();
        });
    }

    private void showDetailDialog(Complaint c) {
        String info =
                "ID         : " + c.getId()           + "\n" +
                        "Name       : " + c.getName()          + "\n" +
                        "Email      : " + c.getEmail()         + "\n" +
                        "Phone      : " + c.getPhone()         + "\n" +
                        "Category   : " + c.getCategory()      + "\n" +
                        "Urgency    : " + c.getUrgency()       + "\n" +
                        "Status     : " + c.getStatus()        + "\n" +
                        "Submitted  : " + c.getSubmittedAt()   + "\n" +
                        "Responded  : " + nvl(c.getRespondedAt()) + "\n\n" +
                        "Message    :\n" + c.getMessage()      + "\n\n" +
                        "Response   :\n" + nvl(c.getResponse());

        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle("Complaint Details");
        dlg.setHeaderText(c.getName() + " — " + c.getCategory());
        dlg.setContentText(info);
        dlg.getDialogPane().setPrefWidth(520);
        dlg.showAndWait();
    }

    private void updateStats(ObservableList<Complaint> list) {
        long total    = list.size();
        long pending  = list.stream().filter(c -> "pending".equalsIgnoreCase(c.getStatus())).count();
        long resolved = list.stream().filter(c -> "resolved".equalsIgnoreCase(c.getStatus())).count();
        long high     = list.stream().filter(c -> "high".equalsIgnoreCase(c.getUrgency())).count();
        String rate   = total == 0 ? "0%" : Math.round((resolved * 100.0) / total) + "%";

        statTotalLabel.setText(String.valueOf(total));
        statPendingLabel.setText(String.valueOf(pending));
        statResolvedLabel.setText(String.valueOf(resolved));
        statHighUrgencyLabel.setText(String.valueOf(high));
        statRateLabel.setText(rate);
    }

    private void updateAnalytics(ObservableList<Complaint> list) {
        buildChart(statusChart,   list.stream().collect(Collectors.groupingBy(c -> capitalize(c.getStatus()),   Collectors.counting())), "#1e77d4");
        buildChart(categoryChart, list.stream().collect(Collectors.groupingBy(c -> capitalize(c.getCategory()), Collectors.counting())), "#1a7a45");
        buildChart(urgencyChart,  list.stream().collect(Collectors.groupingBy(c -> capitalize(c.getUrgency()),  Collectors.counting())), "#b03030");
    }

    private void buildChart(BarChart<String, Number> chart, Map<String, Long> data, String hex) {
        chart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        data.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .forEach(e -> s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        chart.getData().add(s);
        s.getData().forEach(d -> {
            if (d.getNode() != null)
                d.getNode().setStyle("-fx-bar-fill: " + hex + "; -fx-background-radius: 4 4 0 0;");
        });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    private String nvl(String s) {
        return s == null || s.isEmpty() ? "—" : s;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}