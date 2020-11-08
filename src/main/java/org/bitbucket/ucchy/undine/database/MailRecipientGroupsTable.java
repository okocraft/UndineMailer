package org.bitbucket.ucchy.undine.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bitbucket.ucchy.undine.database.Database.DatabaseType;

public class MailRecipientGroupsTable {
    
    public static final String NAME = "undine_mailrecipientgroups";

    private final Database database;
    @SuppressWarnings("unused")
    private final MailDataTable mailDataTable;
    @SuppressWarnings("unused")
    private final GroupDataTable groupDataTable;
    
    MailRecipientGroupsTable(Database database, MailDataTable mailDataTable, GroupDataTable groupDataTable) {
        this.database = database;
        this.mailDataTable = mailDataTable;
        this.groupDataTable = groupDataTable;
        createTable();
    }

    void createTable() {
        database.execute(
            "CREATE TABLE IF NOT EXISTS " + NAME + " (" +
                "mailId INTEGER NOT NULL, " +
                "recipientGroup VARCHAR(64) NOT NULL" + (database.getDatabaseType() == DatabaseType.SQLITE ? " COLLATE NOCASE" : "") + ", " +
                "PRIMARY KEY (mailId, recipientGroup), " +
                "FOREIGN KEY (mailId) REFERENCES " + MailDataTable.NAME + "(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "FOREIGN KEY (recipientGroup) REFERENCES " + GroupDataTable.NAME + "(name) ON DELETE CASCADE ON UPDATE CASCADE" +
            ")"
        );
    }

    /**
     * toGroupに指定したgroupを含むメールのidをすべて取得する。
     * @param group グループ
     * @return メールIDのリスト
     */
    public ArrayList<Integer> getMailIdsByGroup(String groupName) {
        return database.query("SELECT mailId FROM " + NAME + " WHERE recipientGroup = '" + groupName + "'", rs -> {
            try {
                ArrayList<Integer> ids = new ArrayList<>();
                while (rs.next()) {
                    ids.add(rs.getInt("mailId"));
                }
                return ids;
            } catch (SQLException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }

    /**
     * 指定したgroupのどれか一つでも含むメールのidをすべて取得する。
     * @param groups グループIDのリスト
     * @return メールIDのリスト
     */
    public ArrayList<Integer> getMailIdsByGroups(List<String> groupNames) {
        if (groupNames.isEmpty()) {
            return new ArrayList<>();
        }
        return database.query(
            "SELECT mailId FROM " + NAME + " WHERE recipientGroup " + Database.createIn(groupNames), rs -> {
            try {
                ArrayList<Integer> ids = new ArrayList<>();
                while (rs.next()) {
                    ids.add(rs.getInt("mailId"));
                }
                return ids;
            } catch (SQLException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }
    
    public ArrayList<String> getGroups(int mailId) {
        return database.query("SELECT recipientGroup FROM " + NAME + " WHERE mailId = " + mailId, rs -> {
            try {
                ArrayList<String> groupNames = new ArrayList<>();
                while (rs.next()) {
                    groupNames.add(rs.getString("recipientGroup"));
                }
                return groupNames;
            } catch (SQLException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }

    public void addGroup(int id, String groupName) {
        if (database.getDatabaseType() == DatabaseType.MYSQL) {
            database.execute("INSERT INTO " + NAME + " (mailId, recipientGroup) VALUES (" + id + ", '" + groupName + "') ON DUPLICATE KEY UPDATE recipientGroup = recipientGroup");
        } else if (database.getDatabaseType() == DatabaseType.SQLITE) {
            database.execute("INSERT INTO " + NAME + " (mailId, recipientGroup) VALUES (" + id + ", '" + groupName + "') ON CONFLICT(mailId, recipientGroup) DO NOTHING");
        }
    }

    public void removeGroup(int id, String groupName) {
        database.execute("DELETE FROM " + NAME + " WHERE mailId = " + id + " AND recipientGroup = '" + groupName + "'");
    }

    public void clearGroup(int id) {
        database.execute("DELETE FROM " + NAME + " WHERE mailId = " + id);
    }
}