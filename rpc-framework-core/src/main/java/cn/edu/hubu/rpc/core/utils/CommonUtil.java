package cn.edu.hubu.rpc.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author hxy
 * @Date 2022/4/21
 */

public class CommonUtil {
    /**
     * 获取目标对象的实现接口
     *
     * @param targetClass
     * @return
     */
    public static List<Class<?>> getAllInterfaces(Class targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("targetClass is null!");
        }
        Class[] clazz = targetClass.getInterfaces();
        if (clazz.length == 0) {
            return Collections.emptyList();
        }
        List<Class<?>> classes = new ArrayList<>(clazz.length);
        for (Class aClass : clazz) {
            classes.add(aClass);
        }
        return classes;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str){
        return !isEmpty(str);
    }
    public static boolean isBlank(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        for (int i = 0; i < s.length(); ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    public static boolean isEmptyList(List list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmptyList(List list) {
        return !isEmptyList(list);
    }

    /**
     * 去空格
     */
    public static String trim(String str)
    {
        return (str == null ? "" : str.trim());
    }
    /**
     * 是否包含字符串
     *
     * @param str 验证字符串
     * @param strs 字符串组
     * @return 包含返回true
     */
    public static boolean inStringIgnoreCase(String str, String... strs)
    {
        if (str != null && strs != null)
        {
            for (String s : strs)
            {
                if (str.equalsIgnoreCase(trim(s)))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static String buildProviderNode(String str){

        return "/"+str + "/provider";
    }

}
