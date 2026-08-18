/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.dbc.parser;

import com.vrd.dbc.parser.DbcMessage;
import com.vrd.dbc.parser.DbcNode;
import com.vrd.dbc.parser.DbcSignal;
import com.vrd.dbc.parser.DbcValueTable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DbcDatabase {
    private String version;
    private List<DbcNode> nodes;
    private List<DbcMessage> messages;
    private Map<Long, DbcMessage> messageById;
    private Map<String, DbcMessage> messageByName;
    private Map<String, DbcValueTable> valueTables;
    private Map<String, String> attributes;

    public DbcMessage getMessageById(long messageId) {
        return this.messageById.get(messageId);
    }

    public DbcMessage getMessageByName(String name) {
        return this.messageByName.get(name);
    }

    public void addMessage(DbcMessage message) {
        this.messages.add(message);
        this.messageById.put(message.getMessageId(), message);
        this.messageByName.put(message.getName(), message);
    }

    public List<DbcSignal> getAllSignals() {
        ArrayList<DbcSignal> allSignals = new ArrayList<DbcSignal>();
        for (DbcMessage msg : this.messages) {
            allSignals.addAll(msg.getSignals());
        }
        return allSignals;
    }

    public int getMessageCount() {
        return this.messages.size();
    }

    public int getSignalCount() {
        return this.messages.stream().mapToInt(m -> m.getSignals().size()).sum();
    }

    public Map<String, Object> toSummaryMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("version", this.version);
        map.put("nodeCount", this.nodes.size());
        map.put("messageCount", this.messages.size());
        map.put("signalCount", this.getSignalCount());
        ArrayList<String> nodeNames = new ArrayList<String>();
        for (DbcNode n : this.nodes) {
            nodeNames.add(n.getName());
        }
        map.put("nodes", nodeNames);
        ArrayList<Map<String, Object>> msgList = new ArrayList<Map<String, Object>>();
        for (DbcMessage msg : this.messages) {
            msgList.add(msg.toMap());
        }
        map.put("messages", msgList);
        return map;
    }

    private static List<DbcNode> $default$nodes() {
        return new ArrayList<DbcNode>();
    }

    private static List<DbcMessage> $default$messages() {
        return new ArrayList<DbcMessage>();
    }

    private static Map<Long, DbcMessage> $default$messageById() {
        return new LinkedHashMap<Long, DbcMessage>();
    }

    private static Map<String, DbcMessage> $default$messageByName() {
        return new LinkedHashMap<String, DbcMessage>();
    }

    private static Map<String, DbcValueTable> $default$valueTables() {
        return new LinkedHashMap<String, DbcValueTable>();
    }

    private static Map<String, String> $default$attributes() {
        return new LinkedHashMap<String, String>();
    }

    public static DbcDatabaseBuilder builder() {
        return new DbcDatabaseBuilder();
    }

    public String getVersion() {
        return this.version;
    }

    public List<DbcNode> getNodes() {
        return this.nodes;
    }

    public List<DbcMessage> getMessages() {
        return this.messages;
    }

    public Map<Long, DbcMessage> getMessageById() {
        return this.messageById;
    }

    public Map<String, DbcMessage> getMessageByName() {
        return this.messageByName;
    }

    public Map<String, DbcValueTable> getValueTables() {
        return this.valueTables;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setNodes(List<DbcNode> nodes) {
        this.nodes = nodes;
    }

    public void setMessages(List<DbcMessage> messages) {
        this.messages = messages;
    }

    public void setMessageById(Map<Long, DbcMessage> messageById) {
        this.messageById = messageById;
    }

    public void setMessageByName(Map<String, DbcMessage> messageByName) {
        this.messageByName = messageByName;
    }

    public void setValueTables(Map<String, DbcValueTable> valueTables) {
        this.valueTables = valueTables;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DbcDatabase)) {
            return false;
        }
        DbcDatabase other = (DbcDatabase)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$version = this.getVersion();
        String other$version = other.getVersion();
        if (this$version == null ? other$version != null : !this$version.equals(other$version)) {
            return false;
        }
        List<DbcNode> this$nodes = this.getNodes();
        List<DbcNode> other$nodes = other.getNodes();
        if (this$nodes == null ? other$nodes != null : !((Object)this$nodes).equals(other$nodes)) {
            return false;
        }
        List<DbcMessage> this$messages = this.getMessages();
        List<DbcMessage> other$messages = other.getMessages();
        if (this$messages == null ? other$messages != null : !((Object)this$messages).equals(other$messages)) {
            return false;
        }
        Map<Long, DbcMessage> this$messageById = this.getMessageById();
        Map<Long, DbcMessage> other$messageById = other.getMessageById();
        if (this$messageById == null ? other$messageById != null : !((Object)this$messageById).equals(other$messageById)) {
            return false;
        }
        Map<String, DbcMessage> this$messageByName = this.getMessageByName();
        Map<String, DbcMessage> other$messageByName = other.getMessageByName();
        if (this$messageByName == null ? other$messageByName != null : !((Object)this$messageByName).equals(other$messageByName)) {
            return false;
        }
        Map<String, DbcValueTable> this$valueTables = this.getValueTables();
        Map<String, DbcValueTable> other$valueTables = other.getValueTables();
        if (this$valueTables == null ? other$valueTables != null : !((Object)this$valueTables).equals(other$valueTables)) {
            return false;
        }
        Map<String, String> this$attributes = this.getAttributes();
        Map<String, String> other$attributes = other.getAttributes();
        return !(this$attributes == null ? other$attributes != null : !((Object)this$attributes).equals(other$attributes));
    }

    protected boolean canEqual(Object other) {
        return other instanceof DbcDatabase;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $version = this.getVersion();
        result = result * 59 + ($version == null ? 43 : $version.hashCode());
        List<DbcNode> $nodes = this.getNodes();
        result = result * 59 + ($nodes == null ? 43 : ((Object)$nodes).hashCode());
        List<DbcMessage> $messages = this.getMessages();
        result = result * 59 + ($messages == null ? 43 : ((Object)$messages).hashCode());
        Map<Long, DbcMessage> $messageById = this.getMessageById();
        result = result * 59 + ($messageById == null ? 43 : ((Object)$messageById).hashCode());
        Map<String, DbcMessage> $messageByName = this.getMessageByName();
        result = result * 59 + ($messageByName == null ? 43 : ((Object)$messageByName).hashCode());
        Map<String, DbcValueTable> $valueTables = this.getValueTables();
        result = result * 59 + ($valueTables == null ? 43 : ((Object)$valueTables).hashCode());
        Map<String, String> $attributes = this.getAttributes();
        result = result * 59 + ($attributes == null ? 43 : ((Object)$attributes).hashCode());
        return result;
    }

    public String toString() {
        return "DbcDatabase(version=" + this.getVersion() + ", nodes=" + String.valueOf(this.getNodes()) + ", messages=" + String.valueOf(this.getMessages()) + ", messageById=" + String.valueOf(this.getMessageById()) + ", messageByName=" + String.valueOf(this.getMessageByName()) + ", valueTables=" + String.valueOf(this.getValueTables()) + ", attributes=" + String.valueOf(this.getAttributes()) + ")";
    }

    public DbcDatabase() {
        this.nodes = DbcDatabase.$default$nodes();
        this.messages = DbcDatabase.$default$messages();
        this.messageById = DbcDatabase.$default$messageById();
        this.messageByName = DbcDatabase.$default$messageByName();
        this.valueTables = DbcDatabase.$default$valueTables();
        this.attributes = DbcDatabase.$default$attributes();
    }

    public DbcDatabase(String version, List<DbcNode> nodes, List<DbcMessage> messages, Map<Long, DbcMessage> messageById, Map<String, DbcMessage> messageByName, Map<String, DbcValueTable> valueTables, Map<String, String> attributes) {
        this.version = version;
        this.nodes = nodes;
        this.messages = messages;
        this.messageById = messageById;
        this.messageByName = messageByName;
        this.valueTables = valueTables;
        this.attributes = attributes;
    }

    public static class DbcDatabaseBuilder {
        private String version;
        private boolean nodes$set;
        private List<DbcNode> nodes$value;
        private boolean messages$set;
        private List<DbcMessage> messages$value;
        private boolean messageById$set;
        private Map<Long, DbcMessage> messageById$value;
        private boolean messageByName$set;
        private Map<String, DbcMessage> messageByName$value;
        private boolean valueTables$set;
        private Map<String, DbcValueTable> valueTables$value;
        private boolean attributes$set;
        private Map<String, String> attributes$value;

        DbcDatabaseBuilder() {
        }

        public DbcDatabaseBuilder version(String version) {
            this.version = version;
            return this;
        }

        public DbcDatabaseBuilder nodes(List<DbcNode> nodes) {
            this.nodes$value = nodes;
            this.nodes$set = true;
            return this;
        }

        public DbcDatabaseBuilder messages(List<DbcMessage> messages) {
            this.messages$value = messages;
            this.messages$set = true;
            return this;
        }

        public DbcDatabaseBuilder messageById(Map<Long, DbcMessage> messageById) {
            this.messageById$value = messageById;
            this.messageById$set = true;
            return this;
        }

        public DbcDatabaseBuilder messageByName(Map<String, DbcMessage> messageByName) {
            this.messageByName$value = messageByName;
            this.messageByName$set = true;
            return this;
        }

        public DbcDatabaseBuilder valueTables(Map<String, DbcValueTable> valueTables) {
            this.valueTables$value = valueTables;
            this.valueTables$set = true;
            return this;
        }

        public DbcDatabaseBuilder attributes(Map<String, String> attributes) {
            this.attributes$value = attributes;
            this.attributes$set = true;
            return this;
        }

        public DbcDatabase build() {
            List<DbcNode> nodes$value = this.nodes$value;
            if (!this.nodes$set) {
                nodes$value = DbcDatabase.$default$nodes();
            }
            List<DbcMessage> messages$value = this.messages$value;
            if (!this.messages$set) {
                messages$value = DbcDatabase.$default$messages();
            }
            Map<Long, DbcMessage> messageById$value = this.messageById$value;
            if (!this.messageById$set) {
                messageById$value = DbcDatabase.$default$messageById();
            }
            Map<String, DbcMessage> messageByName$value = this.messageByName$value;
            if (!this.messageByName$set) {
                messageByName$value = DbcDatabase.$default$messageByName();
            }
            Map<String, DbcValueTable> valueTables$value = this.valueTables$value;
            if (!this.valueTables$set) {
                valueTables$value = DbcDatabase.$default$valueTables();
            }
            Map<String, String> attributes$value = this.attributes$value;
            if (!this.attributes$set) {
                attributes$value = DbcDatabase.$default$attributes();
            }
            return new DbcDatabase(this.version, nodes$value, messages$value, messageById$value, messageByName$value, valueTables$value, attributes$value);
        }

        public String toString() {
            return "DbcDatabase.DbcDatabaseBuilder(version=" + this.version + ", nodes$value=" + String.valueOf(this.nodes$value) + ", messages$value=" + String.valueOf(this.messages$value) + ", messageById$value=" + String.valueOf(this.messageById$value) + ", messageByName$value=" + String.valueOf(this.messageByName$value) + ", valueTables$value=" + String.valueOf(this.valueTables$value) + ", attributes$value=" + String.valueOf(this.attributes$value) + ")";
        }
    }
}

