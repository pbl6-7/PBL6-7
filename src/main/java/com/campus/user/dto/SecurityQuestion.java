package com.campus.user.dto;

import lombok.Data;

/**
 * 密保问题DTO
 */
@Data
public class SecurityQuestion {
    private Integer questionId;
    private String question;

    public SecurityQuestion(Integer questionId, String question) {
        this.questionId = questionId;
        this.question = question;
    }
}
