package com.fantasy.tbs.domain;

public enum Type {

    entrance{
        @Override
        public <T, A> T accept(A arg, Visitor<T, A> visitor) {
            return visitor.visitEntrance(arg);
        }
    },
    exit {
        @Override
        public <T, A> T accept(A arg, Visitor<T, A> visitor) {
            return visitor.visitExit(arg);
        }
    };

    public abstract <T, A> T accept(A arg, Visitor<T, A> visitor);

    public interface Visitor<T, A>{
        T visitEntrance(A arg);
        T visitExit(A arg);
    }
}
