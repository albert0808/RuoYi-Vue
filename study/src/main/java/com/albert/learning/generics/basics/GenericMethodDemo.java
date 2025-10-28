package com.albert.learning.generics.basics;

/**
 * 第二个：方法泛型
 */
public class GenericMethodDemo {
    public static <T> void swap(T[] array,int i,int j){
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
    public static <T> void printArray(T[] array){
        for(T element: array){
            System.out.println(element);
        }
    };

//    public static void main(String[] args) {
//        String[] strArray = {"a","b","c"};
//        GenericMethodDemo.swap();
//    }
}
