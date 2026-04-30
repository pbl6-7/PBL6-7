package com.campus.activity.dto;

import com.campus.activity.entity.ActivityRegistration;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RegistrationResponse {
    private Long id;
    private Long activityId;
    private String activityTitle;
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;
    private String activityLocation;
    private Long userId;
    private String userName;
    private LocalDateTime registrationTime;
    private String status;

    public static RegistrationResponse fromEntity(ActivityRegistration registration) {
        RegistrationResponse response = new RegistrationResponse();
        response.setId(registration.getId());
        response.setActivityId(registration.getActivityId());
        response.setUserId(registration.getUserId());
        response.setRegistrationTime(registration.getRegistrationTime());
        response.setStatus(registration.getStatus());
        return response;
    }
}
