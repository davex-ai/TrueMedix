package org.example.project.hospitalmanagementsystem.model;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

public class Complaint {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty email, name,category, urgency, phone, message, screenshotPath;
    private final SimpleStringProperty status, submittedAt, respondedAt, response;

    public Complaint(int id, String name, String email,String category, String urgency, String phone,
                     String message, String screenshotPath, String status,
                     String submittedAt, String respondedAt, String response) {
        this.id = new SimpleIntegerProperty(id);
        this.email = new SimpleStringProperty(email);
        this.name =  new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.urgency = new SimpleStringProperty(urgency);
        this.phone = new SimpleStringProperty(phone);
        this.message = new SimpleStringProperty(message);
        this.screenshotPath = new SimpleStringProperty(screenshotPath);
        this.status = new SimpleStringProperty(status);
        this.submittedAt = new SimpleStringProperty(submittedAt);
        this.respondedAt = new SimpleStringProperty(respondedAt);
        this.response = new SimpleStringProperty(response);
    }


    public int getId() { return id.get(); }
    public String getCategory() { return category.get(); }
    public String getUrgency() { return urgency.get(); }
    public String getPhone() { return phone.get(); }
    public String getMessage() { return message.get(); }
    public String getScreenshotPath() { return screenshotPath.get(); }
    public String getStatus() { return status.get(); }
    public String getSubmittedAt() { return submittedAt.get(); }
    public String getRespondedAt() { return respondedAt.get(); }
    public String getResponse() { return response.get(); }

    public String getName() {
        return name.get();
    }

    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty categoryProperty() { return category; }
    public SimpleStringProperty urgencyProperty() { return urgency; }
    public SimpleStringProperty phoneProperty() { return phone; }
    public SimpleStringProperty messageProperty() { return message; }
    public SimpleStringProperty screenshotPathProperty() { return screenshotPath; }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleStringProperty submittedAtProperty() { return submittedAt; }
    public SimpleStringProperty respondedAtProperty() { return respondedAt; }
    public SimpleStringProperty responseProperty() { return response; }

    public String getEmail() { return email.get(); }

    public SimpleStringProperty emailProperty() { return email; }
    public SimpleStringProperty nameProperty() {
        return name;
    }



}
