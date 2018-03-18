package ch.njol.brokkr.compiler;

import ch.njol.brokkr.ast.ASTTopLevelElements.ASTBrokkrFile;

public class SemanticChecker implements BrokkrFileChecker {
	
	/*
	 * checks TODO:
	 * classes:
	 *    - all methods implemented (either by a default implementation or directly in the class)
	 *       - this includes all fields defined
	 * interfaces:
	 *    - no loops in interface inheritance (actually, this could/should(?) be done by the linker)
	 * functions:
	 *    - all results assigned in all possible return paths
	 *    - all variables assigned before use
	 */
	@Override
	public void check(final ASTBrokkrFile file) {
		
	}
	
}
