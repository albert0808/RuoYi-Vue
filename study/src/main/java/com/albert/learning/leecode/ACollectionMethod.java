package com.albert.learning.leecode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ACollectionMethod {
    public static void main(String[] args) {
        /*List*/
        List<Integer> list = new ArrayList<Integer>();
        list.add(5);
        list.add(6);
        list.add(3);
        list.add(8);
        list.add(7);
        System.out.println("源list:"+list);
        //排序
        Collections.sort(list);
        System.out.println("排序后list:"+list);
        /*Set*/
        /*Map*/
    }

}
