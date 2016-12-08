package nl.davinci.davinciquest.Entity;

/**
 * Created by nicog on 12/8/2016.
 */

public class User {
    private int id;
    private String name;
    private int pin;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }
}
