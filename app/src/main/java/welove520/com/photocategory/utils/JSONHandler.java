package welove520.com.photocategory.utils;

import android.util.Log;


import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import welove520.com.photocategory.BuildConfig;

public class JSONHandler {
    public static String TAG = "json";

    public static Object parse(String jsonStr, Class beanClass)
            throws Exception {
        Object obj = null;
        if (jsonStr == null || "".equalsIgnoreCase(jsonStr)) {
            return null;
        }
        JSONObject jsonObj = new JSONObject(jsonStr);
        if (beanClass == null) {
            p("Class instance is Null");
            return null;
        }
        obj = beanClass.newInstance();

        Method[] methods = beanClass.getMethods();
        List<Method> setMethod = new ArrayList<Method>();

        // get all setMethod
        for (Method m : methods) {
            String name = m.getName();
            // 在methods里面都是public类型的，所以不需要判断
            if (name.startsWith("set") && name.length() > 3) {
                setMethod.add(m);
            }
        }

        for (Method setM : setMethod) {
            // 将变量名转换
            // eg:setUserId --> userId
            String getSetName = setM.getName();
            String deleteSet = getSetName.substring(3);    // 通关删除方法前的set得到成员变量名，第一个字符为大写
            String fName = Character.toLowerCase(deleteSet.charAt(0))
                    + deleteSet.substring(1);   // 将得到的成员变量名开头的大写变成小写
            String fieldName = IntrospectionUtils.formatVariable2JsonStyle(fName);  // 将成员变量改成驼峰写法
            // Date Type of Field
            Class[] paramTypes = setM.getParameterTypes();// 获得一个方法参数数组（getparameterTypes用于返回一个描述参数类型的Class对象数组）
            Class type = paramTypes[0];
            // 先判断是否是基本类型
            if ((int.class).equals(type) || (Integer.class).equals(type)) {
                try {
                    setM.invoke(obj, jsonObj.getInt(fieldName));
                } catch (Exception ex) {
                    p("Error: " + ex.toString());
                }
            } else if ((long.class).equals(type) || (Long.class).equals(type)) {
                try {
                    setM.invoke(obj, jsonObj.getLong(fieldName));
                } catch (Exception ex) {
                    p("Error: " + ex.toString());
                }
            } else if ((double.class).equals(type) || (Double.class).equals(type)) {
                try {
                    setM.invoke(obj, jsonObj.getDouble(fieldName));
                } catch (Exception ex) {
                    p("Error: " + ex.toString());
                }
            } else if (type.isAssignableFrom(String.class)) {
                // 判断type是否是String类的子类或父类
                try {
                    setM.invoke(obj, jsonObj.getString(fieldName));
                } catch (Exception ex) {
                    p("Error: " + ex.toString());
                }
            } else if (type.isAssignableFrom(List.class)) {
                try {
                    // 此 Method 对象所表示的方法的形参类型
                    ParameterizedType pt = (ParameterizedType) setM.getGenericParameterTypes()[0];
                    Class genericType = (Class) pt.getActualTypeArguments()[0];
                    if (genericType != null) {
                        JSONArray array = null;
                        try {
                            array = jsonObj.getJSONArray(fieldName);
                        } catch (Exception ex) {
                            p("Error: " + ex.toString());
                            array = null;
                        }
                        if (array == null)
                            continue;
                        List arrayList = new ArrayList();
                        for (int j = 0; j < array.length(); j++) {
                            if (genericType.equals(String.class)) {
                                arrayList.add(array.getString(j));
                            } else if (genericType.equals(Integer.class)) {
                                arrayList.add(array.getInt(j));
                            } else if (genericType.equals(Long.class)) {
                                arrayList.add(array.getLong(j));
                            } else if (genericType.equals(Boolean.class)) {
                                arrayList.add(array.getBoolean(j));
                            } else if (genericType.equals(Double.class) || genericType.equals(Float.class)) {
                                arrayList.add(array.getDouble(j));
                            } else {
                                arrayList.add(parse(array.getJSONObject(j).toString(), genericType));
                            }
                        }
                        setM.invoke(obj, arrayList);
                    }
                } catch (Exception ex) {
                    p("Error: " + ex.toString());
                }
            } else {
                // 对于自定义类进行递归操作
                try {
                    JSONObject customObj = jsonObj.getJSONObject(fieldName);
                    if (customObj != null)
                        setM.invoke(obj, parse(customObj.toString(), type));
                } catch (Exception ex) {
                    p("Error: " + ex.toString());
                }
            }
        }
        return obj;
    }

    public static void p(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "# " + msg);
        }
    }
}
