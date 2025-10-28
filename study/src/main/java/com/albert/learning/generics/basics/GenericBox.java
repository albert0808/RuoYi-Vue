package com.albert.learning.generics.basics;

/**
 * 第一个：类泛型
 */
public class GenericBox<T> {
    private T value;

    public void setValue(T value){
        this.value = value;
    }

    public T getValue(){
        return value;
    }

    public GenericBox(T value){
        this.value = value;
    }

    public static void main(String[] args) {
        GenericBox<String> stringBox = new GenericBox<>("abc");
        System.out.println("stringBox="+stringBox.getValue());

        GenericBox<Integer> intBox = new GenericBox<>(123);
        // 编译器保证了类型安全
        // intBox.setValue("abc"); // ❌ 报错
        System.out.println("intBox="+intBox.getValue());

    }
}
