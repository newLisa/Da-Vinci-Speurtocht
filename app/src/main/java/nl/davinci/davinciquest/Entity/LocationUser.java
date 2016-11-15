package nl.davinci.davinciquest.Entity;

/**
 * Created by nicog on 11/15/2016.
 */

public class LocationUser {
    int id;

    int location_id;

    int user_id;

    int answered_correct;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLocation_id() {
        return location_id;
    }

    public void setLocation_id(int location_id) {
        this.location_id = location_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getAnswered_correct() {
        return answered_correct;
    }

    public void setAnswered_correct(int answered_correct) {
        this.answered_correct = answered_correct;
    }
}
