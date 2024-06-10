/*
 * Copyright (C) 2022-2024 たんらる
 */

package jp.fourthline.mabiicco.ui;

import jp.fourthline.mabiicco.AppResource;

public interface SettingButtonGroupItem {
	String getButtonName();

	public static final class SettingButtonItem {
		public static SettingButtonItem[] create(SettingButtonGroupItem[] items) {
			SettingButtonItem[] array = new SettingButtonItem[items.length];
			for (int i = 0; i < items.length; i++) {
				array[i] = new SettingButtonItem(items[i]);
			}
			return array;
		}
		private final SettingButtonGroupItem item;
		private final String name;
		public SettingButtonItem(SettingButtonGroupItem item) {
			this.item = item;
			this.name = AppResource.appText(item.getButtonName());
		}

		@Override
		public String toString() {
			return name;
		}

		public SettingButtonGroupItem getItem() {
			return item;
		}
	}
}
