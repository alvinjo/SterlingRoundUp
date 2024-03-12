package com.alvin.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "USER_TABLE")
public class User {

    @Id
    private String id;

    @Column(name = "ENCRYPTED_ACCESS_TOKEN")
    private String encryptedAccessToken;

}
