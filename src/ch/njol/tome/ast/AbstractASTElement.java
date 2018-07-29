package ch.njol.tome.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.common.AbstractModifiable;
import ch.njol.tome.common.Modifiable;
import ch.njol.tome.common.ModificationListener;
import ch.njol.tome.util.PrettyPrinter;

public abstract class AbstractASTElement extends AbstractModifiable implements ASTElement {
	
	public @Nullable ASTElement parent = null;
	
	@Override
	public void setParent(final @Nullable ASTElement parent) {
		ASTElementPart.assertValidParent(this, parent);
		this.parent = parent;
	}
	
	@Override
	public @Nullable ASTElement parent() {
		return parent;
	}
	
	private final List<ASTElementPart> parts = new ArrayList<>();
	
	private final List<? extends ASTElementPart> unmodifiableParts = Collections.unmodifiableList(parts);
	
	@Override
	public List<? extends ASTElementPart> parts() {
		return unmodifiableParts;
	}
	
	@Override
	public void insertChild(final ASTElementPart child, final int index) {
		assert 0 <= index && index <= parts.size();
		child.removeFromParent();
		parts.add(index, child);
		child.setParent(this);
	}
	
	private final static class Region implements ModificationListener {
		private final AbstractASTElement ast;
		private ASTElement root; // depends on root to get notified on all element mutations in the tree
		
		public Region(final AbstractASTElement ast) {
			this.ast = ast;
			root = ast.root();
			root.addModificationListener(this);
		}
		
		private int absoluteStart = -1;
		private int relativeStart = -1;
		private int length = -1;
		
		public int absoluteStart() {
			if (absoluteStart < 0) {
				absoluteStart = ast.superAbsoluteRegionStart();
			}
			return absoluteStart;
		}
		
		public int absoluteEnd() {
			return absoluteStart() + length();
		}
		
		public int relativeStart() {
			if (relativeStart < 0) {
				relativeStart = ast.superRelativeRegionStart();
			}
			return relativeStart;
		}
		
		public int relativeEnd() {
			return relativeStart() + length();
		}
		
		public int length() {
			if (length < 0) {
				length = ast.superRegionLength();
			}
			return length;
		}
		
		@Override
		public void onModification(final Modifiable source) {
			assert source == root;
			ASTElement newRoot = ast.root();
			if (newRoot != root) {
				root.removeModificationListener(this);
				newRoot.addModificationListener(this);
				root = newRoot;
			}
			absoluteStart = -1;
			relativeStart = -1;
			length = -1;
		}
	}
	
	private final Region region = new Region(this);
	
	private int superRegionLength() {
		return ASTElement.super.regionLength();
	}
	
	private int superAbsoluteRegionStart() {
		return ASTElement.super.absoluteRegionStart();
	}
	
	private int superRelativeRegionStart() {
		return ASTElement.super.relativeRegionStart();
	}
	
	@Override
	public int absoluteRegionStart() {
		return region.absoluteStart();
	}
	
	@Override
	public int absoluteRegionEnd() {
		return region.absoluteEnd();
	}
	
	@Override
	public int relativeRegionStart() {
		return region.relativeStart();
	}
	
	@Override
	public int relativeRegionEnd() {
		return region.relativeEnd();
	}
	
	@Override
	public int regionLength() {
		return region.length();
	}
	
//	protected void addElementAndFollowingWhitespaceAsChildren(final ASTElementPart element) {
//		final ASTElement parent = element.parent();
//		if (parent == null) {
//			addChild(element);
//			return;
//		}
//		final List<? extends ASTElementPart> siblings = parent.parts();
//		final int elementPos = siblings.indexOf(element);
//		assert elementPos >= 0;
//		ASTElementPart sibling = siblings.get(elementPos);
//		addChild(element);
//		while (elementPos < siblings.size() && (sibling = siblings.get(elementPos)) instanceof WhitespaceOrCommentToken) {
//			addChild(sibling);
//		}
//	}
	
	@Override
	public void removeChild(final ASTElementPart child) {
		parts.removeIf(p -> p == child);
		child.setParent(null);
	}
	
	@Override
	public void addLink(final ASTLink<?> link) {
		links.add(link);
	}
	
	private final List<ASTLink<?>> links = new ArrayList<>();
	
	@Override
	public List<ASTLink<?>> links() {
		return links;
	}
	
	@Override
	public final void invalidateSelf() {
		modified();
	}
	
	@Override
	public final boolean equals(@Nullable final Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public final int hashCode() {
		return super.hashCode();
	}
	
//		public @Nullable Symbol lastSymbol() {
//			for (int i = parts.size() - 1; i >= 0; i--) {
//				if (parts.get(i) instanceof Symbol)
//					return (Symbol) parts.get(i);
//			}
//			return null;
//		}
	
	@Override
	public void print(final PrettyPrinter out) {
		out.print("/*" + getClass().getSimpleName() + "*/ ");
		for (final ASTElementPart part : parts()) {
			if (part instanceof ASTElement) {
				out.changeIndentation(2);
				part.print(out);
				out.changeIndentation(-2);
			} else {
				final String s = part.toString();
				if (s.equals("{") || s.equals("}") || s.equals(";")) {
					if (s.equals("{"))
						out.increaseIndentation();
					else if (s.equals("}"))
						out.decreaseIndentation();
					out.printLine(s);
					out.printIndentation();
				} else {
					out.print(s);
					out.print(" ");
				}
			}
		}
	}
	
	@Override
	public abstract String toString();
	
}
