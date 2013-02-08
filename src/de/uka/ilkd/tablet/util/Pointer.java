package de.uka.ilkd.tablet.util;

public class Pointer<T> {

	T element;

	public T get() {
		return element;
	}

	public void set(T element) {
		this.element = element;
	}
}
