package ch.adesso.dbextractor.core;

import java.util.Collection;

public interface ScriptData {

	void script(Collection<TableDataFilter> filters, DataOutput output);

}
