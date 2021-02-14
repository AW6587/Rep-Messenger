package com.example.repmessenger;

import android.os.Parcel;
import android.os.Parcelable;

public class ResultItems implements Parcelable {
    String city, name, office, party, phone, email, twitter;

    public ResultItems(){}

    public ResultItems(String city, String name, String office, String party, String phone, String email, String twitter){
        this.city = city;
        this.name = name;
        this.office = office;
        this.party = party;
        this.phone = phone;
        this.email = email;
        this.twitter = twitter;
    }

    public String getCity(){return city;}
    public void setCity(String city){this.city = city;}
    public String getName(){return name;}
    public void setName(String name){this.name = name;}
    public String getOffice(){return office;}
    public void setOffice(String office){this.office = office;}
    public String getParty(){return party;}
    public void setParty(String party){this.party = party;}
    public String getPhone(){return phone;}
    public void setPhone(String phone){this.phone = phone;}
    public String getEmail(){return email;}
    public void setEmail(String email){this.email = email;}
    public String getTwitter(){return twitter;}
    public void setTwitter(String twitter){this.twitter = twitter;}

    // methods for parceling to save state on screen rotation
    protected ResultItems(Parcel in) {
        city = in.readString();
        name = in.readString();
        office = in.readString();
        party = in.readString();
        phone = in.readString();
        email = in.readString();
        twitter = in.readString();
    }

    public static final Creator<ResultItems> CREATOR = new Creator<ResultItems>() {
        @Override
        public ResultItems createFromParcel(Parcel in) {
            return new ResultItems(in);
        }

        @Override
        public ResultItems[] newArray(int size) {
            return new ResultItems[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(city);
        parcel.writeString(name);
        parcel.writeString(office);
        parcel.writeString(party);
        parcel.writeString(phone);
        parcel.writeString(email);
        parcel.writeString(twitter);
    }
}
