package com.madao.net.model;

public class ServiceContent<T1, T2> {
	private ServiceBody<T1, T2> service;
	
	public ServiceContent(T1 t1, T2 t2) {
		this.service = new ServiceBody<T1, T2>(t1, t2);
	}

	public ServiceBody<T1, T2> getService() {
		return service;
	}

	public void setService(ServiceBody<T1, T2> Service) {
		this.service = Service;
	}
	
}
