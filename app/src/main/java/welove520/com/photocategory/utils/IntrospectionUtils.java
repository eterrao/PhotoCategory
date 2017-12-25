package welove520.com.photocategory.utils;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LightHuangfu
 * UserInfoSend: Seraph
 * Date: 12-8-29
 * Time: 下午6:09
 */
public class IntrospectionUtils {
    private static final String TAG = "IntrospectionUtils";
    private static final boolean DEBUG = true;

    public static Map<String, Object> getParamMap4SQLight(Object o) throws InvocationTargetException, IllegalAccessException {
        return getParamMap4SQLight(o, true);
    }

    /**
     * @param o      要转化的对象
     * @param isSend 当为TURE的时候，是将驼峰是命名转换成下划线形式命名
     * @return 返回一个对象的参数列表和对应的值
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Map<String, String> getParamMap(Object o, boolean isSend) throws InvocationTargetException, IllegalAccessException {
        Map<String, String> params = new LinkedHashMap<String, String>();
        Method[] methods = o.getClass().getMethods();
        for (Method method : methods) {
            if (!method.getName().startsWith("get") || "getClass".equalsIgnoreCase(method.getName())) {
                continue;
            }
            String field;
            if (isSend)
                field = formatVariable2JsonStyle(method.getName().replaceAll("get", ""));
            else
                field = format2HumpStyle(method.getName().replaceAll("get", ""));
            Object value = method.invoke(o);
            if (value != null) {
                //空值不算
                params.put(field, String.valueOf(value));
            }
        }
        return params;
    }

    /**
     * 方法逻辑基本同getParamMap(Object o, boolean isSend)，但是这个转化会在发送请求的时候过滤掉带有ignore注解的字段
     *
     * @param o 要转化的对象
     * @return 返回一个对象的参数列表和对应的值
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Map<String, String> getParamMapV2(Object o) throws InvocationTargetException, IllegalAccessException {
        Map<String, String> params = getParamMap(o, true);
        List<Field> sendFields = getFields(o.getClass());
        for (Field field : sendFields) {
            if (field.isAnnotationPresent(Ignore.class)) {
                String jsonStyleFiled = formatVariable2JsonStyle(field.getName());
                if (DEBUG) {
                    Log.d(TAG, "filed is " + field.getName() + " and json style " + jsonStyleFiled);
                }
                params.remove(jsonStyleFiled);
            }
        }
        return params;
    }

    public static Map<String, Object> getParamMap4SQLight(Object o, boolean isSend) throws InvocationTargetException, IllegalAccessException {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        Method[] methods = o.getClass().getMethods();
        for (Method method : methods) {
            if (!method.getName().startsWith("get") || "getClass".equalsIgnoreCase(method.getName())) {
                continue;
            }
            String field;
            if (isSend)
                field = formatVariable2JsonStyle(method.getName().replaceAll("get", ""));
            else
                field = format2HumpStyle(method.getName().replaceAll("get", ""));
            params.put(field, String.valueOf(method.invoke(o)));
        }
        return params;
    }

    /**
     * @param filed
     * @return 将驼峰变量转换成下划线形式
     */
    public static String formatVariable2JsonStyle(String filed) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (char c : filed.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (index > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
            index++;
        }
        return sb.toString().toLowerCase();
    }

    /**
     * 将下划线式写法改为驼峰式
     *
     * @param filed
     * @return
     */
    public static String format2HumpStyle(String filed) {
        StringBuilder sb = new StringBuilder();
        char[] c = filed.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '_') {
                sb.append(Character.toUpperCase(c[i + 1]));
                i++;
            } else {
                sb.append(c[i]);
            }
        }
        return sb.toString();
    }

    public static List<Field> getFields(Class cla) {
        Field[] fields = cla.getDeclaredFields();
        List<Field> fieldList = new ArrayList<Field>();

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            fieldList.add(field);
        }
        return fieldList;
    }

    public static List<String> getSendFields(Class cla) {
        List<String> sendFields = new ArrayList<String>();
        for (Field field : getFields(cla)) {
            sendFields.add(formatVariable2JsonStyle(field.getName()));
        }
        return sendFields;
    }

    public static void main(String args[]) {
        System.out.println(format2HumpStyle("user_name"));
    }
}
