package com.ysteimle.segproject.easywalkin;

public class Address {
    public String unit; // optional (e.g. if clinic is located in a certain room in an office building)
    public String streetAdd;
    public String city;
    public String province; // either specify that it should be a two-letter code or to be chosen from a list
    public String country = defaultCountry; // default will be Canada
    public String postalCode;

    private static final String defaultCountry = "Canada";

    public Address () {}

    public Address (String streetAdd, String city, String province, String postalCode) {
        this.streetAdd = streetAdd;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
    }

    public Address (String unit, String streetAdd, String city, String province, String postalCode) {
        this.unit = unit;
        this.streetAdd = streetAdd;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
    }

    public String printFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append(streetAdd);
        if (!unit.isEmpty()) {
            sb.append(" (");
            sb.append(unit);
            sb.append(")");
        }
        sb.append(", ");
        sb.append(city);
        sb.append(", ");
        sb.append(province);
        sb.append(", ");
        sb.append(postalCode);
        if (!country.equals(defaultCountry)) {
            sb.append(", ");
            sb.append(country);
        }
        return sb.toString();
    }

    // method to determine if the address is in a specific city and province
    public boolean isAtLocation(String cityName, String provinceCode) {
        return cityName.equals(this.city) && provinceCode.equals(this.province);
    }
}
