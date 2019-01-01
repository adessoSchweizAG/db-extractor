package ch.adesso.dbextractor.core;

import java.util.List;

public interface ScriptData {

	void script(List<TableDataFilter> filters, DataOutput output);

}
