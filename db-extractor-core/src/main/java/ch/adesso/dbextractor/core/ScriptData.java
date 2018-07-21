package ch.adesso.dbextractor.core;

import java.io.PrintStream;
import java.util.List;

public interface ScriptData {

	void script(List<TableDataFilter> list, PrintStream outStream);

}
