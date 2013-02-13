package org.neociclo.accord.components.oftpcmd;

public interface CommandOptionConverter<T> {

	public interface NullConverter extends CommandOptionConverter<Void> {
	}

	public T convert(String ... options) throws Exception;

}
