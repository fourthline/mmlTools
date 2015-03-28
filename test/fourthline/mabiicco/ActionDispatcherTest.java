/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mabiicco;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;


public final class ActionDispatcherTest {

	private ActionDispatcher obj;

	@Before
	public void initializeObj() {
		this.obj = ActionDispatcher.getInstance();
	}

	private Object getField(String fieldName) throws Exception {
		Field f = ActionDispatcher.class.getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.get(obj);
	}

	@Test
	public void test_initialize() throws Exception {
		obj.initialize();
		HashMap<?, ?> actionMap = (HashMap<?, ?>) getField("actionMap");
		Set<?> keySet = actionMap.keySet();
		System.out.println("keySet size: " + keySet.size());
		Field fields[] = ActionDispatcher.class.getDeclaredFields();
		for (Field f : fields) {
			if (f.isAnnotationPresent(ActionDispatcher.Action.class)) {
				String key = f.get(obj).toString();
				assertTrue(key, keySet.contains(key));
				System.out.println(key + " -> " + actionMap.get(key).toString());
			}
		}
	}
}
