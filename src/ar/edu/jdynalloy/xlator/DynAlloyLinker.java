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

/**
 * Prints the DynAlloy modules of a translation as text, and adds the prelude: the last step of
 * TACO's {@code JDynAlloyToDynAlloyManager.process}, right after
 * {@link JDynAlloyTranslator#translateAll(ar.edu.jdynalloy.ast.JDynAlloyModule[], boolean)}, whose
 * result it takes (docs/ARCHITECTURE.md, "How TACO calls it", step 5). It resolves nothing across
 * modules: each module is printed on its own by dynalloy4's {@code DynalloyPrinter} (C-like
 * grammar, pretty-printed), and TACO writes the texts to the {@code .dals} files.
 *
 * <p>No state of its own. {@link #link} builds the prelude with the static {@code JPreludeFactory},
 * whose registrations, made by earlier steps of this run and left by earlier runs in the same JVM,
 * decide parts of it (docs/ARCHITECTURE.md, "State that outlives a run").
 */
public class DynAlloyLinker {

	/**
	 * Builds the {@code prelude} module ({@code JPreludeFactory.buildPreludeModule}, which also
	 * looks at the ids of the modules to decide some of its parts) and prints it and every module.
	 * Side effect: the new prelude replaces the one in {@code JPreludeFactory}'s static field (which
	 * nothing reads).
	 *
	 * @param ms the modules returned by {@code JDynAlloyTranslator.translateAll}
	 * @return a {@code HashMap} from module id to its text, the prelude under {@code "prelude"}, in no
	 *         particular order (TACO writes the prelude first itself); of two modules with the same
	 *         id, only the text of the later one is kept
	 */
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
	
	/**
	 * Returns the texts of {@link #link} joined, each followed by a line break, in the map's order,
	 * so the prelude is not necessarily first. Nothing in jDynAlloy or TACO calls it.
	 *
	 * @param ms the modules
	 * @return the text
	 */
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
