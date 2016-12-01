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
    private com.google.android.gms.maps.model.Marker mapMarker;
    private LocationUser locationUser;
    private Boolean answered;

    private Boolean isQr;

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

    public com.google.android.gms.maps.model.Marker getMapMarker() {
        return mapMarker;
    }

    public void setMapMarker(com.google.android.gms.maps.model.Marker mapMarker) {
        this.mapMarker = mapMarker;
    }

    public LocationUser getLocationUser()
    {
        return locationUser;
    }

    public void setLocationUser(LocationUser locationUser)
    {
        this.locationUser = locationUser;
    }

    public Boolean getAnswered()
    {
        return answered;
    }

    public void setAnswered(Boolean answered)
    {
        this.answered = answered;
    }

    public Boolean isQr()
    {
        return isQr;
    }

    public void setQr(Boolean qr)
    {
        isQr = qr;
    }
}
