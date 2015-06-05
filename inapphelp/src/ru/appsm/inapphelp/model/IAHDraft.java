package ru.appsm.inapphelp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Anirudh on 19/11/14.
 */
public class IAHDraft implements Serializable {

    @SerializedName("draft_message")
    private String draftMessage;

    @SerializedName("draft_attachments")
    private IAHAttachment[] draftAttachments;

    @SerializedName("draft_user")
    private IAHUser draftUser;

    @SerializedName("draft_reply_message")
    private String draftReplyMessage;

    @SerializedName("draft_reply_attachments")
    private IAHAttachment[] draftReplyAttachments;

    public IAHDraft() {

    }


    public String getMessage() {
        return draftMessage;
    }

    public IAHAttachment[] getAttachments() {
        return draftAttachments;
    }

    public void setDraftMessage(String message) {
        this.draftMessage = message;
    }

    public IAHUser getDraftUser() {
        return draftUser;
    }

    public String getDraftReplyMessage() {
        return draftReplyMessage;
    }

    public IAHAttachment[] getDraftReplyAttachments() {
        return draftReplyAttachments;
    }

    public void setDraftAttachments(IAHAttachment[] attachmentsArray) {
        this.draftAttachments = attachmentsArray;
    }

    public void setDraftUSer(IAHUser user) {
        this.draftUser = user;
    }

    public void setDraftReplyMessage(String message) {
        this.draftReplyMessage = message;
    }

    public void setDraftReplyAttachments(IAHAttachment[] attachments) {
        this.draftReplyAttachments = attachments;
    }
}
