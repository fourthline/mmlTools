/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mmlTools.core;

import java.util.ArrayList;
import java.util.List;

public class Combination<T> {
	private List<List<T>> combinations;

	/**
	 * 配列データから組み合わせを作成します.
	 * @param array  データ
	 * @param r      組み合わせる個数
	 */
	public Combination(T array[], int r) {
		combinations = comb(array, 0, r);
	}

	public List<List<T>> getArray() {
		return combinations;
	}

	private List<List<T>> comb(T array[], int n, int r) {
		if (r > 1) {
			List<List<T>> resultList = new ArrayList<>();
			for (int i = n; i < array.length-1; i++) {
				List<List<T>> rightList = comb(array, i+1, r-1);
				for (List<T> rightItem : rightList) {
					List<T> item = new ArrayList<>();
					item.add(array[i]);
					item.addAll(rightItem);
					resultList.add(item);
				}
			}
			return resultList;
		} else {
			List<List<T>> resultList = new ArrayList<>();
			for(int i = n; i < array.length; i++){
				List<T> item = new ArrayList<>();
				item.add(array[i]);
				resultList.add(item);
			}
			return resultList;
		}
	}

	public static void main(String args[]) {
		String data[] = { "1", "2", "3", "4", "5", "6" };
		new Combination<>(data, 3).getArray().forEach(t -> {
			System.out.println(t);
		});
	}
}
