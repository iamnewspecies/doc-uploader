package juspay.lender.kyc;


public class Address {
    private String careof;
    private String country;
    private String dist;
    private String house;
    private String loc;
    private String pc;
    private String po;
    private String state;
    private String street;
    private String subdist;
    private String vtc;

    public Address() {
        super();
    }

    public Address(String careof, String country, String dist, String house, String loc, String pc, String po, String state, String street, String subdist, String vtc) {
        this.careof = careof;
        this.country = country;
        this.dist = dist;
        this.house = house;
        this.loc = loc;
        this.pc = pc;
        this.po = po;
        this.state = state;
        this.street = street;
        this.subdist = subdist;
        this.vtc = vtc;
    }

    public String getAddress() {
        return this.careof + "," +
                this.country + "," +
                this.dist + "," +
                this.house + "," +
                this.loc + "," +
                this.pc + "," +
                this.po + "," +
                this.state + "," +
                this.street + "," +
                this.subdist + "," +
                this.vtc;
    }
}
