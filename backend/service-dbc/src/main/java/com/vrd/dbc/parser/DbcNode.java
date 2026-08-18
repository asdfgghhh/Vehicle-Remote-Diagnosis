/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.dbc.parser;

public class DbcNode {
    private String name;
    private String comment;

    public static DbcNodeBuilder builder() {
        return new DbcNodeBuilder();
    }

    public String getName() {
        return this.name;
    }

    public String getComment() {
        return this.comment;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DbcNode)) {
            return false;
        }
        DbcNode other = (DbcNode)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$name = this.getName();
        String other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        String this$comment = this.getComment();
        String other$comment = other.getComment();
        return !(this$comment == null ? other$comment != null : !this$comment.equals(other$comment));
    }

    protected boolean canEqual(Object other) {
        return other instanceof DbcNode;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        String $comment = this.getComment();
        result = result * 59 + ($comment == null ? 43 : $comment.hashCode());
        return result;
    }

    public String toString() {
        return "DbcNode(name=" + this.getName() + ", comment=" + this.getComment() + ")";
    }

    public DbcNode() {
    }

    public DbcNode(String name, String comment) {
        this.name = name;
        this.comment = comment;
    }

    public static class DbcNodeBuilder {
        private String name;
        private String comment;

        DbcNodeBuilder() {
        }

        public DbcNodeBuilder name(String name) {
            this.name = name;
            return this;
        }

        public DbcNodeBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public DbcNode build() {
            return new DbcNode(this.name, this.comment);
        }

        public String toString() {
            return "DbcNode.DbcNodeBuilder(name=" + this.name + ", comment=" + this.comment + ")";
        }
    }
}

