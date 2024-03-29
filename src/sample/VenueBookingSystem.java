package sample;

import javafx.application.Application;
import javafx.fxml.FXML;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.text.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;


/**
 * MAIN CODE FILE
 * This file runs the software, and also controls the UI.
 * ALL methods to do with buttons clicks and displaying
 * things for the user should be here.
 */

public class VenueBookingSystem extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Venue Booking System");
        primaryStage.setScene(new Scene(root, 500, 600));
        primaryStage.show();
    }

    private Client client; // initialize the client

    /**
     * UI elements need to be declared here in order to be referenced in code
     */
    @FXML
    TextField clientIDField, newIDField, newFNField, newLNField, newPhoneField,
            newPassField, newEmailField, updatePhoneField, updateEmailField, eventNameField;
    @FXML
    PasswordField passwordField,updatePasswordField;
    @FXML
    Label loginSuccessLabel, regSuccessLabel, updateSuccessLabel, clientIDLabel, eventBookingSuccess, cancelSuccessLabel;
    @FXML
    VBox bookingVBox, venuesVBox, cancelBookingVBox, eventCalendarVBox;
    @FXML
    Pane loginPane, registerPane, mainPane, updatePane;
    @FXML
    ScrollPane viewBookingsPane, bookEventPane, cancelBookingPane, eventCalendarPane;
    @FXML
    MenuButton startTimeField, endTimeField, bookingCancelSelection, venueChoice, eventTypeChoice;
    @FXML
    DatePicker dateChoice;
    @FXML
    RadioButton privateEventRadioButton;
    @FXML
    Button calcFeeButton, btBook, btCancel, btView, btCal;




    /**
     * METHOD: handleLogin
     * Runs when user clicks the 'Login' button.
     * Checks if Client ID is in the database -- if found, checks if password matches.
     * If not found, or password is incorrect, shows error message.
     *
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        try{
            Connection conn = DriverManager.getConnection("jdbc:sqlite:database.db");
            Statement statement = conn.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS clients (id TEXT, password TEXT, firstName TEXT, " +
                    "lastName TEXT, phone INTEGER, email TEXT)");
            // search database for client ID
            ResultSet rs = statement.executeQuery("SELECT * FROM clients");
            boolean loginSuccess = false;
            while (rs.next() && !loginSuccess) { // searches until login is successful or there are no more records
                if (clientIDField.getText().equals(rs.getString("id")))
                // if ID found, check if password matches
                    if (passwordField.getText().equals(rs.getString("password")))
                    {
                        loginPane.setVisible(false); // hide login screen
                        mainPane.setVisible(true); // show main screen
                        // load client details from database and store in client object
                        client = new Client(rs.getString("id"), rs.getString("password"),
                                rs.getString("firstName"), rs.getString("lastName"),
                                rs.getLong("phone"), rs.getString("email"));
                        // this part displays the client's name at top of screen
                        clientIDLabel.setText("Client: "+client.getFirstName()+" "+client.getLastName());
                        loginSuccess = true; // stop searching
                    }
            }
            // if not found, print error message
            if (!loginSuccess)
                loginSuccessLabel.setText("Login failed. Try again or register a new user.");
            statement.close();
            conn.close();
        } catch (SQLException e){
            System.out.println("Something went wrong: " + e.getMessage());
        }
    }

    /**
     * METHOD: handleButtonClick
     * Runs whenever the user clicks on any of the main buttons.
     * Generally, these methods simply show/hide the various panes.
     */
    @FXML
    private void handleButtonClick(ActionEvent event){
        Button button = (Button) event.getSource(); // this determines which button was clicked
        switch (button.getText()){
            case "New User": // show new user registration screen
            {
                loginPane.setVisible(false); // hide login screen
                registerPane.setVisible(true); // show register screen
                break;
            }
            case "Book Event":
            {
                screenChange();
                bookEventPane.setVisible(true); // show event booking screen
                // get all venue options and display them
                for (Venue venue : Venue.getAllVenues()) {
                    Label nameLabel = new Label(venue.getName());
                    Label addressLabel = new Label(venue.getAddress()+" "+venue.getCity()+" Phone: "+venue.getPhoneNum());
                    Label capacityLabel = new Label("Capacity: "+venue.getCapacity().toString());
                    Separator separator = new Separator();
                    venuesVBox.getChildren().addAll(nameLabel, addressLabel, capacityLabel, separator);
                }
                // display venue names in drop-down menu
                for (Venue venue : Venue.getAllVenues()) {
                    MenuItem venueOption = new MenuItem(venue.getName());
                    venueOption.setOnAction(this::handleVenueChoice); // this line makes the options selectable
                    venueChoice.getItems().add(venueOption);
                }
                break;
            }
            case "Cancel Booking":
            {
                screenChange();
                cancelBookingPane.setVisible(true); // show cancel booking screen
                // get client's bookings and display them
                displayEvents(client.getBookings(), cancelBookingVBox);
                // add each event's name and date to drop-down menu
                for (EventBooking eventBooking : client.getBookings()) {
                    MenuItem cancelSelection = new MenuItem(eventBooking.toString());
                    cancelSelection.setOnAction(this::handleCancelSelection); // this line makes the options selectable
                    bookingCancelSelection.getItems().add(cancelSelection);
                }
                break;
            }
            case "View Bookings":
            {
                screenChange();
                viewBookingsPane.setVisible(true); // show view bookings screen
                // get client's bookings and display them
                displayEvents(client.getBookings(), bookingVBox);
                break;
            }
            case "Event Calendar":
            {
                screenChange();
                eventCalendarPane.setVisible(true); // show event calendar screen
                // get all events and display them
                displayEvents(EventBooking.getAllEvents(), eventCalendarVBox);
                break;
            }
            case "Update Contact Info":
            {
                screenChange();
                updatePane.setVisible(true); // show update info screen
                break;
            }
            case "Log Out":
            {
                Client client = new Client(); // Re-initialize client
                screenChange();
                passwordField.setText(null); // clear login fields
                clientIDField.setText(null);
                mainPane.setVisible(false); // hide main screen
                loginPane.setVisible(true); // show login screen
                break;
            }
        }
    }

    // Headache-saving method. Resets almost literally everything.
    // Runs every time the user clicks a button to change screens within the main pane.
    private void screenChange(){
        // set all screens to non-visible
        bookEventPane.setVisible(false);
        viewBookingsPane.setVisible(false);
        eventCalendarPane.setVisible(false);
        cancelBookingPane.setVisible(false);
        updatePane.setVisible(false);
        // empty all VBoxes
        bookingVBox.getChildren().clear();
        cancelBookingVBox.getChildren().clear();
        venuesVBox.getChildren().clear();
        eventCalendarVBox.getChildren().clear();
        // reset all drop-down menus
        venueChoice.getItems().clear();
        bookingCancelSelection.getItems().clear();
        // clear all input fields
        bookingCancelSelection.setText("Select a Booking to Cancel");
        venueChoice.setText("Select a venue");
        startTimeField.setText("Start Time");
        endTimeField.setText("End Time");
        eventNameField.setText("");
        dateChoice.setValue(LocalDate.now());
        eventTypeChoice.setText("Select Event Type");
        privateEventRadioButton.setSelected(false);
        calcFeeButton.setText("Calculate Fee: ");
        updateEmailField.setText("");
        updatePhoneField.setText("");
        updatePasswordField.setText("");
        // clear all labels
        eventBookingSuccess.setText(null);
        cancelSuccessLabel.setText(null);
        regSuccessLabel.setText(null);
        updateSuccessLabel.setText(null);
        loginSuccessLabel.setText(null);
    }

    /**
     * METHOD: handleReg // New User Registration Screen
     *
     * Runs when user clicks the 'Register New User' button.
     * Instantiates a client with the input data. Shows error message if data is invalid.
     * Else, adds the new client to the database and logs them into the system.
     *
     */
    @FXML
    private void handleReg(ActionEvent event) {
        client = new Client();
        // ensure input is not absurdly long
        if (newIDField.getText().length() > 80 || newPassField.getText().length() > 80 ||
                newFNField.getText().length() > 80 || newLNField.getText().length() > 80 ||
                newEmailField.getText().length() > 80 || newPhoneField.getText().length() > 80)
            regSuccessLabel.setText("ERROR -- one or more of your input fields is too long.");
        // ensure no fields are left blank
        else if (newIDField.getText().equals("") || newPassField.getText().equals("") ||
                newPhoneField.getText().equals("") || newLNField.getText().equals("") ||
                newFNField.getText().equals("") || newEmailField.getText().equals(""))
            regSuccessLabel.setText("ERROR -- all fields must be complete.");
        else {
            client.setId(newIDField.getText());
            client.setPassword(newPassField.getText());
            client.setFirstName(newFNField.getText());
            client.setLastName(newLNField.getText());
            client.setPhoneNumber(newPhoneField.getText());
            client.setEmailAddress(newEmailField.getText());
            if (client.getId().equals("INVALID")) // non-unique ID
                regSuccessLabel.setText("That ID is already in use.");
            else if (client.getPhoneNumber() == 0) // phone number is not exactly 10 digits
                regSuccessLabel.setText("Invalid phone number. Please enter all 10 digits.");
            else if (client.getEmailAddress().equals("INVALID")) // email is incorrect format
                regSuccessLabel.setText("Invalid email address.");
            else {
                client.addToDatabase(); // add new client to database
                registerPane.setVisible(false); // hide register pane
                mainPane.setVisible(true); // show main screen
                // this part displays the client's name at the top of screen
                clientIDLabel.setText("Client: " + client.getFirstName() + " " + client.getLastName());
                // clear input fields
                newIDField.setText("");
                newPassField.setText("");
                newFNField.setText("");
                newLNField.setText("");
                newPhoneField.setText("");
                newEmailField.setText("");
            }
        }
    }


    /**
     * THE FOLLOWING METHODS WORK WITH THE 'BOOK EVENT' SCREEN
     *
     * handleStartTime - sets 'Start Time' field to selection
     * handleEndTime - sets 'End Time' field to selection
     * handleEventType - sets 'Event Type' field to selection
     * handleCalcFee - calculates and displays the booking fee
     * handleSubmitEventBooking - books the event, or displays an error message
     *
     */
    @FXML
    private void handleStartTime(ActionEvent event){
        MenuItem choice = (MenuItem) event.getSource();
        startTimeField.setText(choice.getText());
    }
    @FXML
    private void handleEndTime(ActionEvent event){
        MenuItem choice = (MenuItem) event.getSource();
        endTimeField.setText(choice.getText());
    }
    @FXML
    private void handleEventType(ActionEvent event){
        MenuItem choice = (MenuItem) event.getSource();
        eventTypeChoice.setText(choice.getText());
    }
    @FXML
    private void handleVenueChoice(ActionEvent event){
        MenuItem choice = (MenuItem) event.getSource();
        venueChoice.setText(choice.getText());
    }
    @FXML
    private void handleCalcFee(ActionEvent event){
        EventBooking eventBooking = new EventBooking(); // initialize an event
        Venue eventVenue = new Venue(); // find the data for selected venue and copy it here
        for (Venue venue : Venue.getAllVenues()) {
            if (venue.getName().equals(venueChoice.getText()))
                eventVenue = venue;
        }
        // set some of the event details to user input
        if(privateEventRadioButton.isSelected())
            eventBooking.setPrivateEvent(true);
        else
            eventBooking.setPrivateEvent(false);
        eventBooking.setStartTime(startTimeField.getText());
        eventBooking.setEndTime(endTimeField.getText());
        eventBooking.setEventType(eventTypeChoice.getText());
        // calculate and display the fee
        calcFeeButton.setText("Calculate Fee: "+ NumberFormat.getCurrencyInstance().format(eventBooking.calcFee(eventVenue)));
    }
    @FXML
    private void handleSubmitEventBooking(ActionEvent event){
        EventBooking eventBooking = new EventBooking(); // initialize the event
        if (eventNameField.getText().length() > 80) // ensure input is not absurdly long
            eventBookingSuccess.setText("ERROR -- event name is too long.");
        // ensure no fields are left blank
        else if (eventNameField.getText().equals("") || venueChoice.getText().equals("Select a venue") ||
                startTimeField.getText().equals("Start Time") || endTimeField.getText().equals("End Time") ||
                dateChoice.getValue().equals(LocalDate.now()) || eventTypeChoice.getText().equals("Select Event Type"))
            eventBookingSuccess.setText("ERROR -- all fields must be complete.");
        else {
            // set event details to user input
            eventBooking.setEventName(eventNameField.getText());
            eventBooking.setVenue(venueChoice.getText());
            eventBooking.setStartTime(startTimeField.getText());
            eventBooking.setEndTime(endTimeField.getText());
            eventBooking.setEventType(eventTypeChoice.getText());
            eventBooking.setEventDate(dateChoice.getValue().toString());
            eventBooking.setClientID(client.getId());
            if (privateEventRadioButton.isSelected())
                eventBooking.setPrivateEvent(true);
            else
                eventBooking.setPrivateEvent(false);
            eventBooking.setFeePaid(false); // this value is always going to be false because there is no way to
                                            // actually pay the fee within this software

            Venue eventVenue = new Venue(); // get the venue data
            for (Venue venue : Venue.getAllVenues()) {
                if (venue.getName().equals(venueChoice.getText()))
                    eventVenue = venue;
            }
            eventBooking.setBookingFee(eventBooking.calcFee(eventVenue)); // calculate the fee

            // check if start time is after end time -- if so, event cannot be booked
            String digits = "";
            Integer start = 0;
            Integer end = 0;
            boolean stpm = false;
            boolean etpm = false;
            // this part converts a String to Integer -- removes all non-digit characters before parsing
            for (int i = 0; i < eventBooking.getStartTime().length(); ++i) {
                if (Character.isDigit(eventBooking.getStartTime().charAt(i)))
                    digits += eventBooking.getStartTime().charAt(i);
                else if (eventBooking.getStartTime().charAt(i) == 'P')
                    stpm = true;
            }
            start = start.parseInt(digits);
            if (stpm && start >= 100) // don't do this for 12pm
                start += 1200; // covert to 24 hour format, e.g. 7PM will parse to 700, add 1200 = 1900
            // do the same for end time
            digits = "";
            for (int i = 0; i < eventBooking.getEndTime().length(); ++i) {
                if (Character.isDigit(eventBooking.getEndTime().charAt(i)))
                    digits += eventBooking.getEndTime().charAt(i);
                else if (eventBooking.getEndTime().charAt(i) == 'P')
                    etpm = true;
            }
            end = start.parseInt(digits);
            if (etpm && end >= 100) // don't do this for 12pm
                end += 1200;
            if (!etpm && end < 400.0) // do this for hours after midnight
                end += 2400;
            // if end time is before or same as start time, unless end time is after 12am, event cannot be booked
            if ((end <= start) && !(!etpm && (end == 1200 || end < 700)))
                eventBookingSuccess.setText("Event cannot be booked. Check that your start/end times are correct.");
            else
                eventBookingSuccess.setText(client.bookEvent(eventBooking)); // attempt to book the event
        }
    }


    /**
     * THE FOLLOWING TWO METHODS WORK WITH THE 'CANCEL EVENT' SCREEN
     *
     * handleCancelSelection - sets the menu button text to user's selection
     *
     * METHOD: handleSubmitCancellation
     * DESCRIPTION: Matches user's selection with a booking in the database,
     * then calls the cancelBooking method in Client class to remove the event
     */
    @FXML
    private void handleCancelSelection(ActionEvent event){
        MenuItem choice = (MenuItem) event.getSource();
        bookingCancelSelection.setText(choice.getText());
    }
    @FXML
    private void handleSubmitCancellation(ActionEvent event){
        EventBooking cancelBooking = new EventBooking(); // initialize booking to cancel
        ArrayList<EventBooking> clientBookings = client.getBookings(); // get client's bookings
        for (EventBooking booking : clientBookings) {
            if (bookingCancelSelection.getText().equals(booking.toString())) // match selection to booking
                cancelBooking = booking;
        }
        cancelSuccessLabel.setText(client.cancelBooking(cancelBooking)); // attempt to cancel
    }


    /**
     * METHOD: displayEvents
     * DESCRIPTION: This method takes a list of event bookings and displays the data in a VBox
     * @param events - ArrayList of EventBookings to be displayed
     * @param eventVBox - the vBox to display them in
     */
    private void displayEvents(ArrayList<EventBooking> events, VBox eventVBox){
        for (EventBooking eventBooking : events) {
            Label dateLabel = new Label(eventBooking.getEventDate());
            Label nameLabel = new Label(eventBooking.getEventName());
            if (eventBooking.isPrivateEvent())
                nameLabel.setText(nameLabel.getText() + " (private event)");
            Label timeLabel = new Label(eventBooking.getStartTime() + " - " + eventBooking.getEndTime());
            Label venueLabel = new Label(eventBooking.getVenue());
            eventVBox.getChildren().addAll(dateLabel, nameLabel, timeLabel, venueLabel);
            // only display fee for the current client's bookings
            if (eventBooking.getClientID().equals(client.getId())) {
                Label feeLabel = new Label("Booking Fee: " +
                        NumberFormat.getCurrencyInstance().format(eventBooking.getBookingFee()) + " Paid? ");
                if (eventBooking.isFeePaid())
                    feeLabel.setText(feeLabel.getText() + "Y");
                else
                    feeLabel.setText(feeLabel.getText() + "N");
                eventVBox.getChildren().add(feeLabel);
            }
            eventVBox.getChildren().add(new Separator());
        }
    }

    /**
     * METHOD: handleUpdateInfo | Update Contact Info Screen
     *
     * DESCRIPTION: Runs when user clicks 'Submit' button after updating their  password
     * and/or contact info. Validates input, and if valid, updates the database.
     *
     * Note: This changes the fields in current client object no matter what.
     * However, if the data is invalid, the database will not be affected.
     *
     */
    @FXML
    private void handleUpdateInfo(ActionEvent event) {
        // ensure input is not absurdly long
        if (updatePasswordField.getText().length() > 80 || updateEmailField.getText().length() > 80 ||
                updatePhoneField.getText().length() > 80)
            updateSuccessLabel.setText("ERROR -- one or more of your input fields is too long.");
        else {
            if (updatePasswordField.getText().equals("")) // only update non-empty fields
                client.setPassword(client.getPassword()); // this code is a little redundant, but it works
            else
                client.setPassword(updatePasswordField.getText());
            if (updatePhoneField.getText().equals(""))
                client.setPhoneNumber(client.getPhoneNumber().toString());
            else
                client.setPhoneNumber(updatePhoneField.getText());
            if (updateEmailField.getText().equals(""))
                client.setEmailAddress(client.getEmailAddress());
            else
                client.setEmailAddress(updateEmailField.getText());

            if (client.getPhoneNumber() == 0) // validate phone number
                updateSuccessLabel.setText("Invalid phone number. Please enter all 10 digits.");
            else if (client.getEmailAddress().equals("INVALID")) // validate email address
                updateSuccessLabel.setText("Invalid email address.");
            else {
                client.addToDatabase(); // update database
                updateSuccessLabel.setText("Your information was updated.");
                // clear input fields
                updatePasswordField.setText("");
                updatePhoneField.setText("");
                updateEmailField.setText("");
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
