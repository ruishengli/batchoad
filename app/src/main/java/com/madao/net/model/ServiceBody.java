package com.madao.net.model;

public class ServiceBody<T1, T2> {
	private T1 head;
	private T2 body;
	
	public ServiceBody(T1 t1, T2 t2) {
		this.head = t1;
		this.body = t2;
	}
	
	public T1 getHead() {
		return head;
	}
	public void setHead(T1 Head) {
		this.head = Head;
	}
	public T2 getBody() {
		return body;
	}
	public void setBody(T2 Body) {
		this.body = Body;
	}
}
