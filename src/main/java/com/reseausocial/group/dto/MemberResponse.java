package com.reseausocial.group.dto;
import com.reseausocial.group.entity.GroupMember;
public record MemberResponse(Long id, String fullName, String email, String role) { public static MemberResponse of(GroupMember m) { return new MemberResponse(m.getUser().getId(), m.getUser().getFullName(), m.getUser().getEmail(), m.getRole()); } }
