package org.example.project.hospitalmanagementsystem.controller.admin;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.project.hospitalmanagementsystem.database.DatabaseConnection;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class ManagePatientsController {

    // ─── FXML Bindings ─────────────────────────────────────────────────────────

    @FXML private TextField searchField;
    @FXML private ComboBox<String> genderFilter;
    @FXML private ComboBox<String> statusFilter;

    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, Integer> colId;
    @FXML private TableColumn<Patient, String>  colName;
    @FXML private TableColumn<Patient, String>  colGender;
    @FXML private TableColumn<Patient, String>  colDob;
    @FXML private TableColumn<Patient, String>  colEmail;
    @FXML private TableColumn<Patient, String>  colAddress;
    @FXML private TableColumn<Patient, String>  colStatus;
    @FXML private TableColumn<Patient, Void>    colActions;

    @FXML private Label totalCountLabel;
    @FXML private Label maleCountLabel;
    @FXML private Label femaleCountLabel;
    @FXML private Label paginationInfoLabel;
    @FXML private Pagination pagination;

    // ─── Data ──────────────────────────────────────────────────────────────────

    private final ObservableList<Patient> masterList   = FXCollections.observableArrayList();
    private FilteredList<Patient>         filteredList;
    private static final int              PAGE_SIZE    = 20;

    // ═══════════════════════════════════════════════════════════════════════════
    //  Patient Model
    // ═══════════════════════════════════════════════════════════════════════════

    public static class Patient {

        private final IntegerProperty id       = new SimpleIntegerProperty();
        private final StringProperty  name     = new SimpleStringProperty();
        private final StringProperty  email    = new SimpleStringProperty();
        private final StringProperty  gender   = new SimpleStringProperty();
        private final StringProperty  dob      = new SimpleStringProperty();
        private final StringProperty  address  = new SimpleStringProperty();
        private final StringProperty  password = new SimpleStringProperty();
        private final StringProperty  role     = new SimpleStringProperty();
        private final StringProperty  status   = new SimpleStringProperty();

        public Patient(int id, String name, String password, String email,
                       String gender, String dob, String address, String role) {
            this.id.set(id);
            this.name.set(name);
            this.password.set(password);
            this.email.set(email);
            this.gender.set(gender);
            this.dob.set(dob);
            this.address.set(address);
            this.role.set(role);
            this.status.set("Active"); // default; update if your DB has a status column
        }

        // Property accessors (required by PropertyValueFactory)
        public IntegerProperty idProperty()       { return id; }
        public StringProperty  nameProperty()     { return name; }
        public StringProperty  emailProperty()    { return email; }
        public StringProperty  genderProperty()   { return gender; }
        public StringProperty  dobProperty()      { return dob; }
        public StringProperty  addressProperty()  { return address; }
        public StringProperty  passwordProperty() { return password; }
        public StringProperty  roleProperty()     { return role; }
        public StringProperty  statusProperty()   { return status; }

        // Plain getters (used by some factories & action handlers)
        public int    getId()       { return id.get(); }
        public String getName()     { return name.get(); }
        public String getEmail()    { return email.get(); }
        public String getGender()   { return gender.get(); }
        public String getDob()      { return dob.get(); }
        public String getAddress()  { return address.get(); }
        public String getPassword() { return password.get(); }
        public String getRole()     { return role.get(); }
        public String getStatus()   { return status.get(); }

        // Plain setters
        public void setStatus(String s) { status.set(s); }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Initialization
    // ═══════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        setupFilterComboBoxes();
        setupTableColumns();
        setupFiltering();
        loadPatientsFromDatabase();
        setupPagination();
    }

    // ─── ComboBox setup ────────────────────────────────────────────────────────

    private void setupFilterComboBoxes() {
        genderFilter.setItems(FXCollections.observableArrayList("All", "Male", "Female", "Other"));
        genderFilter.setValue("All");

        statusFilter.setItems(FXCollections.observableArrayList("All", "Active", "Inactive"));
        statusFilter.setValue("All");

        genderFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    // ─── Column setup ──────────────────────────────────────────────────────────

    private void setupTableColumns() {
        colId     .setCellValueFactory(new PropertyValueFactory<>("id"));
        colName   .setCellValueFactory(new PropertyValueFactory<>("name"));
        colGender .setCellValueFactory(new PropertyValueFactory<>("gender"));
        colDob    .setCellValueFactory(new PropertyValueFactory<>("dob"));
        colEmail  .setCellValueFactory(new PropertyValueFactory<>("email"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        // Status column with colored badge
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(status);
                    badge.setPadding(new Insets(3, 10, 3, 10));
                    badge.setStyle(
                            "-fx-border-radius: 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;" +
                                    (status.equalsIgnoreCase("Active")
                                            ? "-fx-background-color: #d4f5e2; -fx-text-fill: #1a7a45;"
                                            : "-fx-background-color: #fde8e8; -fx-text-fill: #b03030;")
                    );
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Actions column
        colActions.setCellFactory(buildActionCellFactory());

        // Style header
        patientsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    // ─── Actions cell factory ──────────────────────────────────────────────────

    private Callback<TableColumn<Patient, Void>, TableCell<Patient, Void>> buildActionCellFactory() {
        return col -> new TableCell<>() {

            private final Button btnView   = buildBtn("View",   "#e8f1fa", "#1e77d4");
            private final Button btnEdit   = buildBtn("Edit",   "#fef6e4", "#c47e0a");
            private final Button btnDelete = buildBtn("Delete", "#fde8e8", "#b03030");

            private Button buildBtn(String label, String bg, String fg) {
                Button b = new Button(label);
                b.setStyle(
                        "-fx-background-color: " + bg + "; " +
                                "-fx-text-fill: " + fg + "; " +
                                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;" +
                                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 10 4 10;"
                );
                return b;
            }

            {
                btnView.setOnAction(e -> handleView(getTableView().getItems().get(getIndex())));
                btnEdit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            private final HBox box = new HBox(5, btnView, btnEdit, btnDelete);
            { box.setAlignment(Pos.CENTER_LEFT); }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        };
    }

    // ─── Filtering ─────────────────────────────────────────────────────────────

    private void setupFiltering() {
        filteredList = new FilteredList<>(masterList, p -> true);

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());

        SortedList<Patient> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(patientsTable.comparatorProperty());
        patientsTable.setItems(sortedList);
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String gender = genderFilter.getValue();
        String status = statusFilter.getValue();

        filteredList.setPredicate(p -> {
            boolean matchSearch = search.isEmpty()
                    || p.getName().toLowerCase().contains(search)
                    || p.getEmail().toLowerCase().contains(search);

            boolean matchGender = gender == null || gender.equals("All")
                    || p.getGender().equalsIgnoreCase(gender);

            boolean matchStatus = status == null || status.equals("All")
                    || p.getStatus().equalsIgnoreCase(status);

            return matchSearch && matchGender && matchStatus;
        });

        updateStats();
        updatePaginationInfo();
    }

    // ─── Load from DB ──────────────────────────────────────────────────────────

    private void loadPatientsFromDatabase() {
        String query = "SELECT id, name, email, gender, birth_date, address, password, role " +
                "FROM users WHERE role = 'patient'";

        masterList.clear();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery(query)) {

            while (rs.next()) {
                masterList.add(new Patient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("gender"),
                        rs.getDate("birth_date") != null ? rs.getDate("birth_date").toString() : "N/A",
                        rs.getString("address"),
                        rs.getString("role")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load patients:\n" + e.getMessage());
        }

        updateStats();
        updatePaginationInfo();
    }

    // ─── Stats bar ─────────────────────────────────────────────────────────────

    private void updateStats() {
        long total  = masterList.size();
        long males  = masterList.stream().filter(p -> "Male".equalsIgnoreCase(p.getGender())).count();
        long females= masterList.stream().filter(p -> "Female".equalsIgnoreCase(p.getGender())).count();

        totalCountLabel.setText(String.valueOf(total));
        maleCountLabel.setText(String.valueOf(males));
        femaleCountLabel.setText(String.valueOf(females));
    }

    // ─── Pagination ────────────────────────────────────────────────────────────

    private void setupPagination() {
        pagination.setPageFactory(pageIndex -> {
            // If you want real pagination, slice the list here.
            // For now it just reflects the total.
            return new javafx.scene.layout.StackPane();
        });
        updatePaginationInfo();
    }

    private void updatePaginationInfo() {
        int count = filteredList != null ? filteredList.size() : masterList.size();
        paginationInfoLabel.setText("Showing " + count + " record" + (count == 1 ? "" : "s"));

        int pages = Math.max(1, (int) Math.ceil((double) count / PAGE_SIZE));
        pagination.setPageCount(pages);
    }

    // ─── Action Handlers ───────────────────────────────────────────────────────

    private void handleView(Patient p) {
        String info =
                "Patient ID : " + p.getId()      + "\n" +
                        "Name       : " + p.getName()    + "\n" +
                        "Email      : " + p.getEmail()   + "\n" +
                        "Gender     : " + p.getGender()  + "\n" +
                        "DOB        : " + p.getDob()     + "\n" +
                        "Address    : " + p.getAddress() + "\n" +
                        "Status     : " + p.getStatus();

        showAlert(Alert.AlertType.INFORMATION, "Patient Details — " + p.getName(), info);
    }

    private void handleEdit(Patient p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin/EditPatientModal.fxml")
            );
            Parent root = loader.load();

            // Pass the selected patient into the modal controller
            EditPatientController editCtrl = loader.getController();
            editCtrl.setPatient(p);

            // Optional: re-run stats/pagination after save (table updates live via properties)
            editCtrl.setOnSaveCallback(() -> {
                updateStats();
                updatePaginationInfo();
            });

            // Open as a modal dialog
            Stage modal = new Stage();
            modal.setTitle("Edit Patient — " + p.getName());
            modal.setScene(new Scene(root));
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(patientsTable.getScene().getWindow());
            modal.setResizable(false);
            modal.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open edit form:\n" + e.getMessage());
        }
    }


    private void handleDelete(Patient p) {
        Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete patient:\n\"" + p.getName() + "\"?",
                ButtonType.YES, ButtonType.NO).showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            String sql = "DELETE FROM users WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, p.getId());
                ps.executeUpdate();
                masterList.remove(p);
                updateStats();
                updatePaginationInfo();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Delete Failed", e.getMessage());
            }
        }
    }

    // ─── FXML Button Actions ───────────────────────────────────────────────────

    @FXML
    private void refreshPatients() {
        searchField.clear();
        genderFilter.setValue("All");
        statusFilter.setValue("All");
        loadPatientsFromDatabase();
    }

    // ─── Utility ───────────────────────────────────────────────────────────────

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}