package com.bykuharev.testtaskcleverdev.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "company_user")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User extends BaseEntity{

    @Column(name = "login", nullable = false, unique = true)
    private String login;


}
