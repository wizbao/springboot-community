package com.wiz.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Description:
 * @Create: 2022-03-25-20:42
 * @Author: Hey
 */
@Data
public class Message {
    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    private int status;  // 0-未读;1-已读;2-删除;
    private Date createTime;
}
