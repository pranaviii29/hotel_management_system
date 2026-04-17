package com.hotel.model;

// Inheritance - Guest extends HotelEntity
public class Guest extends HotelEntity {
    private static final long serialVersionUID = 1L;

    private String name;
    private String phone;
    private String email;
    private String idProofType;
    private String idProofNumber;
    private Integer age;            
    private String address;

    public Guest(String name, String phone, String email,
                 String idProofType, String idProofNumber, int age, String address) {
        super("GUEST-" + System.currentTimeMillis(), java.time.LocalDate.now().toString());
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.idProofType = idProofType;
        this.idProofNumber = idProofNumber;
        this.age = age;             // Autoboxing: int -> Integer
        this.address = address;
    }

    // Polymorphism
    @Override
    public String getSummary() {
        return String.format("Guest: %s | Phone: %s | ID: %s %s", name, phone, idProofType, idProofNumber);
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getIdProofType() { return idProofType; }
    public String getIdProofNumber() { return idProofNumber; }
    public int getAge() { return age; }   // Unboxing: Integer -> int
    public String getAddress() { return address; }
}
