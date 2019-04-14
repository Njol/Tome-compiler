package ch.njol.tome.ast.expressions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ir.IRGenericArgument;
import ch.njol.tome.ir.IRTypeBoundGenericArgument;
import ch.njol.tome.parser.Parser;

public class ASTGenericArgumentTypeBoundValue extends AbstractASTElementWithIR<IRGenericArgument> implements ASTGenericArgumentValue {
	
	private @Nullable ASTTypeUse<?> extends_, super_;
	
	@Override
	public String toString() {
		return "?" + (extends_ != null ? " extends " + extends_ : "") + (super_ != null ? " super " + super_ : "");
	}
	
	public static ASTGenericArgumentTypeBoundValue parse(final Parser parent) {
		return parent.one(p -> {
			final ASTGenericArgumentTypeBoundValue ast = new ASTGenericArgumentTypeBoundValue();
			p.one('?');
			p.unordered(() -> {
				if (p.try_("extends"))
					ast.extends_ = ASTTypeUseWithOperators.parse(p);
			}, () -> {
				if (p.try_("super"))
					ast.super_ = ASTTypeUseWithOperators.parse(p);
			});
			return ast;
		});
	}
	
	@Override
	protected IRGenericArgument calculateIR() {
		return IRTypeBoundGenericArgument.fromBounds(extends_, super_, getIRContext());
	}
	
}
