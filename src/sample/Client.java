package sample;

import java.sql.*;
import java.util.ArrayList;

/**
 * CLIENT CLASS
 * This file contains the Client class, and methods to control the client's event bookings.
 *
 * METHODS: addToDatabase, getBookings, bookEvent, cancelBooking
 */

public class Client {

    private String id;
    private String password;
    private String firstName;
    private String lastName;
    private Long phoneNumber;
    private String emailAddress;

    /**
     *  NO-ARG CONSTRUCTOR
     */
    public Client() {
    }

    /**
     *  CONSTRUCTOR
     */
    public Client(String id, String password, String firstName, String lastName, Long phoneNumber,
                  String emailAddress) {
        this.id = id;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }

    /**
     * GET ID
     */
    public String getId() {
        return id;
    }

    /**
     * SET ID
     * Checks the client database to ensure that the ID is
     * not already in use -- if it is, sets ID to "INVALID"
     */
    public void setId(String id) {
        try{
            Connection conn = DriverManager.getConnection("jdbc:sqlite:database.db");
            Statement statement = conn.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS clients (id TEXT, password TEXT, firstName TEXT, " +
                    "lastName TEXT, phone INTEGER, email TEXT)");
            // search database for client ID
            ResultSet rs = statement.executeQuery("SELECT * FROM clients");
            boolean idExists = false;
            while (rs.next() && !idExists) { // searches until ID is found or there are no more records
                if (id.equals(rs.getString("id")))
                {
                    this.id = "INVALID";
                    idExists = true;
                }
            }
            // if not found, set ID
            if (!idExists)
                this.id = id;
            statement.close();
            conn.close();
        } catch (SQLException e){
            System.out.println("Something went wrong: " + e.getMessage());
        }
    }

    /**
     * GET PASSWORD
     */
    public String getPassword() {
        return password;
    }

    /**
     * SET PASSWORD
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * GET FIRST NAME
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * SET FIRST NAME
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * GET LAST NAME
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * SET LAST NAME
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * GET PHONE NUMBER
     */
    public Long getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * SET PHONE NUMBER
     * Removes all non-digits from input string before parsing to Long, and
     * ensuring that the number is exactly 10 digits -- if invalid, sets to 0
     */
    public void setPhoneNumber(String input) {
        String digits = "";
        Long phoneNum = 0L;
        for (int i = 0; i < input.length(); ++i)
        {
            if (Character.isDigit(input.charAt(i)))
                digits += input.charAt(i);
        }
        if (!digits.equals("")) {
            phoneNum = phoneNum.parseLong(digits);
            if (phoneNum < 10000000000L && phoneNum > 999999999L)
                this.phoneNumber = phoneNum;
            else
                this.phoneNumber = 0L;
        }
        else this.phoneNumber = 0L;
    }

    /**
     * GET EMAIL ADDRESS
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * SET EMAIL ADDRESS
     * Checks that input follows correct email address format: text@text.text --
     * if invalid, sets to "INVALID"
     */
    public void setEmailAddress(String emailAddress) {
        boolean foundAt = false;
        boolean foundDot = false;
        int j = 0;
        for (int i = 0; i < emailAddress.length(); ++i){
            // check if @ character is found anywhere except the first character
            if (emailAddress.charAt(i) == '@' && i > 0) {
                foundAt = true;
                j = i; // record where @ was found
            }
            // check if . is found after @ and before the end of the input string
            if (foundAt && i > j && emailAddress.charAt(i) == '.' && i < (emailAddress.length() - 1))
                foundDot = true;
        }
        if (foundAt && foundDot)
            this.emailAddress = emailAddress;
        else
            this.emailAddress = "INVALID";
    }


    /**
     * METHOD: addToDatabase
     *
     * DESCRIPTION: Searches the database for client ID -- if found, updates the data (password and contact
     * info only). If not found, creates a new record with the current client data.
     *
     */
    public void addToDatabase(){
        try{
            Connection conn = DriverManager.getConnection("jdbc:sqlite:database.db");
            Statement statement = conn.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS clients (id TEXT, password TEXT, firstName TEXT, " +
                    "lastName TEXT, phone INTEGER, email TEXT)");
            // search database for client ID
            ResultSet rs = statement.executeQuery("SELECT * FROM clients");
            boolean idExists = false;
            while (rs.next() && !idExists) { // searches until ID is found or there are no more records
                if (id.equals(rs.getString("id")))
                {
                    statement.execute("UPDATE clients SET password = '"+this.password+"', phone = '"+
                            this.phoneNumber+"', email = '"+this.emailAddress+"' WHERE id = '"+
                            this.id+"'");
                    idExists = true;
                }
            }
            // if not found, create a new record
            if (!idExists){
                statement.execute("INSERT INTO clients (id, password, firstName, lastName, phone, email)" +
                        "VALUES ('"+this.getId()+"', '"+this.getPassword()+"', '"+this.getFirstName()+ "', '"+
                        this.getLastName()+"', '"+this.getPhoneNumber()+"', '"+this.getEmailAddress()+"')");
            }
            statement.close();
            conn.close();
        } catch (SQLException e){
            System.out.println("Something went wrong: " + e.getMessage());
        }
    }

    /**
     * METHOD: getBookings
     *
     * DESCRIPTION: Searches database for all of a client's bookings and stores them in an ArrayList.
     * Should be called when user clicks the 'View Bookings' button.
     *
     * RETURNS: ArrayList of EventBookings
     *
     */
    public ArrayList<EventBooking> getBookings(){
        ArrayList<EventBooking> clientBookings = new ArrayList<>();
        try{
            Connection conn = DriverManager.getConnection("jdbc:sqlite:database.db");
            Statement statement = conn.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS events (name TEXT, type TEXT, clientID TEXT, " +
                    "venue TEXT, privateEvent TEXT, date TEXT, startTime TEXT, endTime TEXT, fee REAL, " +
                    "feePaid TEXT)");
            // search event database for client ID
            ResultSet rs = statement.executeQuery("SELECT * FROM events ORDER BY date ASC");
            while (rs.next()) {
                if (this.getId().equals(rs.getString("clientID")))
                {
                    // convert booleans
                    boolean privateEvent = false;
                    if (rs.getString("privateEvent").equals("true"))
                        privateEvent = true;
                    boolean feePaid = false;
                    if (rs.getString("feePaid").equals("true"))
                        feePaid = true;
                    // instantiate an event from data and add to arraylist
                    clientBookings.add(new EventBooking(rs.getString("name"), rs.getString("type"),
                            rs.getString("clientID"), rs.getString("venue"), privateEvent,
                            rs.getString("date"), rs.getString("startTime"),
                            rs.getString("endTime"), rs.getDouble("fee"), feePaid));
                }
            }
            statement.close();
            conn.close();
        } catch (SQLException e){
            System.out.println("Something went wrong: " + e.getMessage());
        }
        return clientBookings;
    }

    /**
     * METHOD: bookEvent
     *
     * DESCRIPTION: This method takes an EventBooking as a parameter, then check the database to ensure that the
     * venue is available on the selected date and there is no duplicate event. If all is good, add new event data
     * to event database. Else, return an error message.
     *
     * @param event - EventBooking containing the event to be booked
     *
     * RETURNS: String to be output to the UI giving the result
     *
     */
    public String bookEvent(EventBooking event){
        boolean bookingSuccess = true; // assume true by default -- after testing, this works best
        boolean venueNotAvailable = false;
        String ID = event.getClientID();
        String venue = event.getVenue();
        String name = event.getEventName();
        String type =  event.getEventType();
        String date = event.getEventDate();
        String start = event.getStartTime();
        String end = event.getEndTime();
        double fee = event.getBookingFee();
        boolean feePaid = event.isFeePaid();
        boolean privateEvent = event.isPrivateEvent();
        try{
            Connection conn = DriverManager.getConnection("jdbc:sqlite:database.db");
            Statement statement = conn.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS events (name TEXT, type TEXT, clientID TEXT, " +
                    "venue TEXT, privateEvent TEXT, date TEXT, startTime TEXT, endTime TEXT, fee REAL, " +
                    "feePaid TEXT)");
            // search event database for selected venue
            ResultSet rs = statement.executeQuery("SELECT * FROM events ORDER BY date ASC");
            if (!rs.next()) { // if no events in database, booking is successful
                bookingSuccess = true;
            }
            // for each event in database, check if venue matches selected venue
            // if it does, check if date matches selected date (i.e. there is a booking conflict)
            // also make sure client does not have a booking with the same name on the same date
            // (this could cause issues with the cancellation method)
            while (rs.next()) {
                if (rs.getString("venue").equals(venue) && rs.getString("date").equals(date)) {
                    bookingSuccess = false;
                    venueNotAvailable = true;
                }
                else if (rs.getString("clientID").equals(ID) && rs.getString("name").equals(name)
                        && rs.getString("date").equals(date))
                    bookingSuccess = false;
                else
                    bookingSuccess = true;
            }
            if (bookingSuccess) // if booking is successful, add to database
                statement.execute("INSERT INTO events (name, type, clientID, venue, privateEvent, date, startTime, endTime, fee, feePaid)" +
                        "VALUES ('" + name + "', '" + type + "', '" + ID + "', '" + venue + "', '" + privateEvent
                        + "', '" + date + "', '" + start + "', '" + end + "', '" + fee + "', '" + feePaid + "')");
            statement.close();
            conn.close();
        } catch (SQLException e){
            System.out.println("Something went wrong: " + e.getMessage());
            return "ERROR -- Try removing special characters from your event name.";
        }
        if (bookingSuccess)
            return "Your event has been booked!";
        else if (venueNotAvailable)
            return "This venue is not available. Please select a different venue or date.";
        else
            return "You cannot book multiple events with the same name and date.";
    }

    /**
     * METHOD: cancelBooking
     *
     * DESCRIPTION: This method performs the mechanics of cancelling a client's booking. Accepts an
     * EventBooking as parameter, then searches the database for a matching event, and removes it.
     *
     * @param event - EventBooking containing the event to be canceled
     *
     * RETURNS: String to be output to the UI giving the result
     *
     */
    public String cancelBooking(EventBooking event){
        boolean cancelSuccess = false;
        try{
            Connection conn = DriverManager.getConnection("jdbc:sqlite:database.db");
            Statement statement = conn.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS events (name TEXT, type TEXT, clientID TEXT, " +
                    "venue TEXT, privateEvent TEXT, date TEXT, startTime TEXT, endTime TEXT, fee REAL, " +
                    "feePaid TEXT)");
            // search event database for client ID
            // then check if event name and date match the event they want to cancel
            ResultSet rs = statement.executeQuery("SELECT * FROM events ORDER BY date ASC");
            while (rs.next()) {
                if (rs.getString("clientID").equals(event.getClientID())) {
                    if (rs.getString("name").equals(event.getEventName())) {
                        //statement.execute("DELETE FROM events WHERE(name, clientID, date) VALUES('"
                         //       +event.getEventName()+ "', '" +event.getClientID()+ "', '" +event.getEventDate()+ "')");
                        statement.execute("DELETE FROM events WHERE name = '"+event.getEventName()+"' AND clientID = '"+
                                event.getClientID()+"' AND date = '"+event.getEventDate()+"'");
                        cancelSuccess = true;
                    }
                    else
                    cancelSuccess = false; // event not found (i.e. they left the selection field blank)
                }
            }
            statement.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }
        if (cancelSuccess)
            return "Your booking was successfully cancelled.";
        else
            return "ERROR -- this booking could not be found.";
    }
}
