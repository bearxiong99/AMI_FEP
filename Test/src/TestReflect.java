import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.hexing.fk.bp.model.TaskYFFSJYDJ;


public class TestReflect {
	public static void main(String[] args) {
		Object o = new TaskYFFSJYDJ();
		Class<? extends Object> clazz=o.getClass();
		try {
			System.out.println(clazz.getName());
			Field[] fields = clazz.getDeclaredFields();
			for(Field field : fields){
				PropertyDescriptor  pd = new PropertyDescriptor(field.getName(),
					      clazz);
				Method method=pd.getReadMethod();
				method =pd.getWriteMethod();
				System.out.println(method);
			}
			Method method=clazz.getMethod("setYE", String.class);
			method.invoke(o,"3");
			System.out.println(o);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
	}
}
