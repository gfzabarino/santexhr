package org.openapplicant.monitor.timed;

import org.openapplicant.domain.DomainObject;

public interface Timeable<T extends DomainObject> {

	T getEntity();
	
	Long calculateTime();
}
