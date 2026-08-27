package com.reseausocial.group.dto;

import java.util.List;

public record NotificationFeedResponse(List<InvitationResponse> groupInvitations, List<EventInvitationResponse> eventInvitations) {}
