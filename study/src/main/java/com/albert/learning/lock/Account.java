package com.albert.learning.lock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Account implements Serializable {
    private String name;
    private int balance; // 单位：元

}
