package test.reflect;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.hexing.fk.bp.model.TaskDLSJYDJ;

public class TestMethods {

	public static void main(String[] args) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, IntrospectionException {
		TaskDLSJYDJ taskDLSJYDJ = new TaskDLSJYDJ();
		Class<? extends TaskDLSJYDJ> clazz = taskDLSJYDJ.getClass();
		Field[] fields = clazz.getDeclaredFields();
		Method[] methods = clazz.getMethods();
		
		for(Field field:fields){
			String fieldName = field.getName();
			if(field.getType() == String.class && fieldName.endsWith("SJ")){
				PropertyDescriptor  pd = new PropertyDescriptor(fieldName,clazz);
				pd.getWriteMethod().invoke(taskDLSJYDJ, "123");
			}
		}
		
		System.out.println(taskDLSJYDJ.getFXWGZDXLFSSJ());
	}
}
