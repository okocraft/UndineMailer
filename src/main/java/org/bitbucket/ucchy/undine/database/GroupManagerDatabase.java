package org.bitbucket.ucchy.undine.database;

import java.util.ArrayList;
import java.util.List;

import org.bitbucket.ucchy.undine.UndineMailer;
import org.bitbucket.ucchy.undine.group.GroupData;
import org.bitbucket.ucchy.undine.group.GroupManager;
import org.bitbucket.ucchy.undine.sender.MailSender;

/**
 * メールグループをデータベースで管理するクラス
 * @author LazyGon
 */
public class GroupManagerDatabase extends GroupManager {

    private GroupDataTable groupDataTable;

    /**
     * コンストラクタ
     * 
     * @param parent
     */
    public GroupManagerDatabase(UndineMailer parent) {
        super(parent);

        this.groupDataTable = parent.getDatabase().groupDataTable;

        // 特殊グループの登録
        addGroup(groupAll);
        addGroup(groupAllConnected);
        addGroup(groupAllLogin);
    }

    @Override
    public void reload() {
        // Do nothing. 常にデータベースと同期されているため、再読込する必要はない。
        return;
    }

    @Override
    public void addGroup(GroupData group) {
        saveGroupData(group);
    }

    @Override
    public GroupData getGroup(String name) {
        name = name.toLowerCase();
        GroupData pexGroup = getPexGroup(name);
        if (pexGroup != null) {
            return pexGroup;
        }

        return groupDataTable.exists(name) 
            ? new GroupDataDatabase(parent, name)
            : null;
    }

    public ArrayList<GroupData> getGroups(List<String> names) {
        return convert(groupDataTable.getNames(), true);
    }

    @Override
    public void removeGroup(String name) {
        groupDataTable.deleteByName(name);
    }

    @Override
    public ArrayList<String> getAllGroupNames() {
        return groupDataTable.getNames();
    }

    @Override
    public ArrayList<GroupData> getAllGroups() {
        return convert(groupDataTable.getNames(), false);
    }

    @Override
    public boolean existGroupName(String name) {
        return groupDataTable.exists(name);
    }

    @Override
    public void saveGroupData(GroupData group) {
        groupDataTable.add(group.getName(), group.getOwner(), group.getSendMode(), group.getModifyMode(), group.getDissolutionMode());
    }

    @Override
    public int getOwnerGroupCount(MailSender sender) {
        return groupDataTable.getNamesByOwner(parent.getDatabase().mailSenderTable.getId(sender)).size();
    }

    private ArrayList<GroupData> convert(ArrayList<String> groupNames, boolean filter) {
        if (filter) {
            groupDataTable.retainExistsByName(groupNames);
        }
        ArrayList<GroupData> groups = new ArrayList<>();
        for (String name : groupNames) {
            groups.add(new GroupDataDatabase(parent, name));
        }
        return groups;
    }
}
