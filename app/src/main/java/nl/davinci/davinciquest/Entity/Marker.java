package nl.davinci.davinciquest.Entity;

/**
 * Created by Vincent on 2-11-2016.
 */

public class Marker
{
    private int id;
    private String name;
    private double latitude;
    private double longitude;
    private int vraag_id;
    private String info;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public int getVraag_id()
    {
        return vraag_id;
    }

    public void setVraag_id(int vraag_id)
    {
        this.vraag_id = vraag_id;
    }

    public String getInfo()
    {
        return info;
    }

    public void setInfo(String info)
    {
        this.info = info;
    }
}
