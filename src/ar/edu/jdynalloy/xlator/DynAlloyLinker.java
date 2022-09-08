package ar.edu.jdynalloy.xlator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;

import ar.edu.jdynalloy.factory.JPreludeFactory;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.util.DynalloyPrinter;
import ar.uba.dc.rfm.dynalloy.util.DynalloyPrinter.DynalloyGrammar;

public class DynAlloyLinker {

	public Map<String, String> link(Vector<DynalloyModule> ms) {
		Map<String, String> result = new HashMap<String, String>();

		HashSet<String> moduleIds = getModuleIds(ms);
		
		DynalloyModule preludeModule = JPreludeFactory.buildPreludeModule(moduleIds);
		
		DynalloyModule[] precompiledModules = new DynalloyModule[] { preludeModule };
		for (DynalloyModule m : precompiledModules) {
			DynalloyPrinter printer = new DynalloyPrinter();
			printer.setGrammar(DynalloyGrammar.C_LIKE);
			printer.setPrettyPrinting(true);
			String strModule = (String) m.accept(printer);
//			System.out.println(strModule);
			result.put(m.getModuleId(), strModule);
		}

		for (DynalloyModule m : ms) {
			DynalloyPrinter printer = new DynalloyPrinter();
			printer.setGrammar(DynalloyGrammar.C_LIKE);
			printer.setPrettyPrinting(true);
			String strModule = (String) m.accept(printer);
			result.put(m.getModuleId(), strModule);
		}

		return result;
	}

	private HashSet<String> getModuleIds(Vector<DynalloyModule> ms){
		HashSet<String> result = new HashSet<String>();
		
		for (DynalloyModule m : ms) {
			result.add(m.getModuleId());
		}
		
		return result;
	}
	
	public String linkAll(Vector<DynalloyModule> ms) {
		StringBuffer result = new StringBuffer();
		Map<String, String> modules = link(ms);
		for (String m : modules.values()) {
			result.append(m);
			result.append("\n");
		}
		return result.toString();
	}
}
