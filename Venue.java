package sample;

public class Venue {

    private String address;
    private String city;
    private int capacity;
    private int phoneNum;

    private void ViewAllEvents(){
        System.out.println();
    }

    /**
     * no arg constructor
     */
    public Venue() {
    }

    /**
     * constructor
     */
    public Venue(String address, String city, int capacity, int phoneNum) {
        this.address = address;
        this.city = city;
        this.capacity = capacity;
        this.phoneNum = phoneNum;
    }

    /**
     * GET address
     */
    public String getAddress() {
        return address;
    }

    /**
     * SET address
     */
    public void setAddress(String address) {
        this.address = address;
    }
    /**
     * GET CITY
     */
    public String getCity() {
        return city;
    }

    /**
     * SET CITY
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * GET CAPACITY
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * SET CAPACITY
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * GET PHONE NUM
     */
    public int getPhoneNum() {
        return phoneNum;
    }

    /**
     * SET PHONE NUM
     */
    public void setPhoneNum(int phoneNum) {
        this.phoneNum = phoneNum;
    }
}
