package com.albert.learning.leecode;

import java.util.Arrays;

public class AStringBaseMethod {
    public static void main(String[] args) {
        String str = "12345ABCDE ";
        /*基本操作*/
        //1. 长度
        System.out.println(str.length());
        //2. 位置字符
        System.out.println(str.charAt(1));
        //3. 截取
        System.out.println(str.substring(0,3));
        //4. 相等判断
        System.out.println(str.equals("123"));
        //5. 转换为数组
        System.out.println(Arrays.toString(str.toCharArray()));
        /*技巧*/
        //1. 反转
        System.out.println(new StringBuilder(str).reverse().toString());
        int[] arr = {1,3,2,5,4,9};
        Arrays.sort(arr);
        System.out.println("正序:"+Arrays.toString(arr));
        StringBuilder strb = new StringBuilder();
        for(int i:arr){
            strb.append(i);
        }
        System.out.println("逆序:"+Arrays.toString(strb.reverse().toString().toCharArray()));
        //2. 拆分字符串
        System.out.println(Arrays.toString(str.split("")));
        //3. 去除空格
        System.out.println(str.trim());
        //4. 拼接字符串
        System.out.println(String.join("-","2","A"));
    }
}
