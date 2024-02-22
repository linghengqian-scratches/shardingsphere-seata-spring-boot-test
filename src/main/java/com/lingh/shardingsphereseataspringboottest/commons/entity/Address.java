package com.lingh.shardingsphereseataspringboottest.commons.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Address implements Serializable {

    private static final long serialVersionUID = 4743102234543827855L;

    private Long addressId;

    private String addressName;
}
