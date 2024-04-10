package com.github.linktest;

public class CapWordsConverter {

    public static String toCapWords(String input, String separator) {
        StringBuilder capWords = new StringBuilder();
        String[] words = input.split(separator);

        for (String word : words) {
            // 如果word为空（可能由于连续的分隔符导致），则跳过
            if (word.isEmpty()) continue;

            // 将首字母转换为大写，如果单词长度大于1，则添加其余的部分
            capWords.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                capWords.append(word.substring(1));
            }
        }

        return capWords.toString();
    }

    public static void main(String[] args) {
        String input = "hello_world_this_is_a_test";
        String input2 = "test1";
        String capWords = toCapWords(input, "_");
        String capWords2 = toCapWords(input2, "_");
        System.out.println(capWords);  // 输出: HelloWorldThisIsATest
        System.out.println(capWords2);  // 输出: Test1
    }
}