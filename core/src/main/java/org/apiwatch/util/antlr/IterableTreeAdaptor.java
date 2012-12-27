package org.apiwatch.util.antlr;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeAdaptor;

public class IterableTreeAdaptor extends CommonTreeAdaptor {

    @Override
    public Object create(Token payload) {
        return new IterableTree(payload);
    }

    @Override
    public Object errorNode(TokenStream input, Token start, Token stop, RecognitionException e) {
        return new IterableErrorTree(input, start, stop, e);
    }

}
