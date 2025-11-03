package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.StatusHomestay;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "tbl_homestays")
public class Homestay extends BaseEntity{
    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "rating")
    private Float rating;

    @Column(name = "max_guest")
    private Integer maxGuest;

    @Column(name = "min_guest")
    private Integer minGuest;

    @Column(name = "num_bedrooms")
    private Integer numBedrooms;

    @Column(name = "num_beds")
    private Integer numBeds;

    @Column(name = "num_bathrooms")
    private Integer numBathrooms;

    @Column(name = "num_kitchen")
    private Integer numKitchen;

    @Column(name = "status")
    private StatusHomestay statusHomestay;

    @Column(name = "advanced_payment")
    private Integer advancedPayment;

    @Column(name = "warning_count")
    private Integer warningCount;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "addressId", referencedColumnName = "id")
    @MapsId
    Address address;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    Host host;

    @OneToMany(mappedBy = "homestay", cascade = CascadeType.ALL)
    Set<Review> listReview;

    @OneToMany(mappedBy = "homestay" , cascade = CascadeType.ALL)
    Set<HomestayPenalties> listHomestayPenalties;

    @OneToMany(mappedBy = "homestay" , cascade = CascadeType.ALL)
    Set<HomestayDailyPrice> listHomestayDailyPrice;

    @OneToMany(mappedBy = "homestay" , cascade = CascadeType.ALL)
    Set<Image> listImage;

    @OneToMany(mappedBy = "homestay" , cascade = CascadeType.ALL)
    Set<Bill> listBill;

    @OneToMany(mappedBy = "homestay" , cascade = CascadeType.ALL)
    Set<HomestayRule> listHomestayRule;

    @OneToMany(mappedBy = "homestay", cascade = CascadeType.ALL)
    Set<PersonHomestay> listPersonHomestay;

    @ManyToMany
    Set<Facilities> listFacilities;

    @ManyToMany
    Set<Amenities> listAmenities;
}
