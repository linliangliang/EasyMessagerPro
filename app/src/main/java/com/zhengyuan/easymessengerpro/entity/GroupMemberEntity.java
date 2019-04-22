package com.zhengyuan.easymessengerpro.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 群组成员entity
 * Created by gpsts on 17-6-19.
 */

public class GroupMemberEntity implements Parcelable {

    public String id;
    public String name;
    public int memberNum;
    public boolean isSelected = false;

    public GroupMemberEntity(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public GroupMemberEntity(String name, int peopleNum) {
        this.name = name;
        this.memberNum = peopleNum;
    }

    public GroupMemberEntity(String name) {
        super();
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(name);
        dest.writeInt(memberNum);
        dest.writeInt(isSelected ? 1 : 0);
    }

    public static final Parcelable.Creator<GroupMemberEntity> CREATOR = new Parcelable.Creator<GroupMemberEntity>() {

        @Override
        public GroupMemberEntity createFromParcel(Parcel source) {

            return new GroupMemberEntity(source);
        }

        @Override
        public GroupMemberEntity[] newArray(int size) {
            return new GroupMemberEntity[0];
        }
    };

    private GroupMemberEntity(Parcel parcel) {

        this.name = parcel.readString();
        this.memberNum = parcel.readInt();
        this.isSelected = (parcel.readInt() == 1);
    }
}
