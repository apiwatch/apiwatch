/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.util.antlr;

import java.util.Iterator;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.Tree;

public class IterableTree extends CommonTree implements Iterable<IterableTree> {

    public IterableTree() {
        super();
    }

    public IterableTree(CommonTree node) {
        super(node);
    }

    public IterableTree(Token t) {
        super(t);
    }

    public IterableTree firstChildOfType(int type) {
        for (IterableTree child : this) {
            if (child.getType() == type) {
                return child;
            }
        }
        return null;
    }

    @Override
    public Tree dupNode() {
        return new IterableTree(this);
    }

    @Override
    public IterableTree getChild(int i) {
        return (IterableTree) super.getChild(i);
    }

    @Override
    public Iterator<IterableTree> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<IterableTree> {

        private int cursor = 0;

        @Override
        public boolean hasNext() {
            return children != null && cursor < children.size();
        }

        @Override
        public IterableTree next() {
            IterableTree next = (IterableTree) children.get(cursor);
            cursor += 1;
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    public static class Adaptor extends CommonTreeAdaptor {
        
        @Override
        public Object create(Token payload) {
            return new IterableTree(payload);
        }        
        
    }

}
