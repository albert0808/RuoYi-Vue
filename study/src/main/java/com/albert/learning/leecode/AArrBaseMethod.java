package com.albert.learning.leecode;

import java.util.Arrays;
public class AArrBaseMethod {
    public static void main(String[] args) {
        /*定义以及初始化*/
        int[] arr1 = new int[8];// 定义长度为5的数组，初始值全为0
        int[] arr2 = {7,2,3,4,5};// 定义并赋值

        /*二维数组*/
        int[][] arr3 = new int[3][4];// 3行4列的矩阵
        int[][] arr4 = {{1,2},{3,4}};// 初始化二维数组
        /*用法*/
        //1. 数组长度
        System.out.println(arr1.length);
        //2. 复制，参数分别为（源数组，源数组起始拷贝位置，目标数组，目标数组起始位置，源数组拷贝长度）
        System.arraycopy(arr2,0,arr1,0,5);
        System.out.println(Arrays.toString(arr1));
        //3. 排序，局部排序，升序
        System.out.println("源数组："+Arrays.toString(arr1));
        Arrays.sort(arr1,0,3);
        System.out.println("局部排序："+Arrays.toString(arr1));
        Arrays.sort(arr1);
        System.out.println("全部排序："+Arrays.toString(arr1));
        //4. 填充
        Arrays.fill(arr1, 7);//全部填充为7
        System.out.println(Arrays.toString(arr1));
        //5. 二维数组打印
        System.out.println(Arrays.deepToString(arr4));
        //6. 比较
        System.out.println(Arrays.equals(arr1, arr2));
    }


}
