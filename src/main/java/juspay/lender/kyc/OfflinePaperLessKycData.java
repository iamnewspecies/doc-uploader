package juspay.lender.kyc;

public class OfflinePaperLessKycData {
    private String version;
    private String name;
    private String referenceId;
    private String photo;
    private String dob;
    private String email;
    private String mobile;
    private String gender;
    private Address address; // careof + country + dist + house + loc + pc + po + state + street + subdist + vtc
    private String signature;

    public OfflinePaperLessKycData(){
        super();
    }
    public OfflinePaperLessKycData(String version,String name, String referenceId, String photo, String dob, String email, String mobile, String gender, Address address, String signature){
        this.version = version;
        this.name = name;
        this.referenceId = referenceId;
        this.photo = photo;
        this.dob = dob;
        this.email = email;
        this.mobile = mobile;
        this.gender = gender;
        this.address = address;
        this.signature = signature;
    }

}

