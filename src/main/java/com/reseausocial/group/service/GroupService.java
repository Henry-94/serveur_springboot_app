package com.reseausocial.group.service;

import com.reseausocial.group.dto.*;
import com.reseausocial.group.entity.*;
import com.reseausocial.group.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service @RequiredArgsConstructor
public class GroupService {
    private final StudyGroupRepository groups;
    private final GroupMemberRepository members;
    private final GroupMessageRepository messages;
    private final UserRepository users;
    private final GroupInvitationRepository invitations;

    @Transactional(readOnly = true)
    public List<GroupResponse> list(String email) {
        User current = user(email);
        return groups.findAllByOrderByCreatedAtDesc().stream()
                .filter(g -> members.findByGroupIdAndUserId(g.getId(), current.getId()).isPresent())
                .map(g -> GroupResponse.of(g, members.countByGroupId(g.getId()), messages.findTop1ByGroupIdOrderBySentAtDesc(g.getId()).orElse(null)))
                .toList();
    }

    @Transactional
    public GroupResponse create(CreateGroupRequest request, String email) {
        User user = user(email);
        String description = request.description() == null || request.description().isBlank() ? null : request.description().trim();
        StudyGroup group = groups.saveAndFlush(StudyGroup.builder().name(request.name().trim()).description(description).createdBy(user).build());
        members.saveAndFlush(GroupMember.builder().group(group).user(user).role("ADMIN").build());
        return GroupResponse.of(group, 1);
    }

    @Transactional
    public GroupResponse join(Long id, String email) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rejoignez ce groupe uniquement après avoir accepté une invitation.");
    }

    @Transactional(readOnly = true)
    public List<UserResponse> inviteCandidates(Long id, String email) {
        requireAdmin(id, email);
        return users.findAll().stream()
                .filter(candidate -> members.findByGroupIdAndUserId(id, candidate.getId()).isEmpty())
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Transactional
    public InvitationResponse invite(Long id, String invitedEmail, String inviterEmail) {
        StudyGroup group = group(id); User inviter = user(inviterEmail);
        GroupMember inviterMember = members.findByGroupIdAndUserId(id, inviter.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous devez être membre du groupe."));
        if (!"ADMIN".equals(inviterMember.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul le créateur peut inviter un membre.");
        User invited = user(invitedEmail.trim().toLowerCase());
        if (members.findByGroupIdAndUserId(id, invited.getId()).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet utilisateur est déjà membre du groupe.");
        if (invitations.existsByGroupIdAndInviteeIdAndStatus(id, invited.getId(), "PENDING")) throw new ResponseStatusException(HttpStatus.CONFLICT, "Une invitation est déjà en attente.");
        return InvitationResponse.of(invitations.save(GroupInvitation.builder().group(group).inviter(inviter).invitee(invited).status("PENDING").build()));
    }

    @Transactional
    public void delete(Long id, String email) {
        StudyGroup group = group(id); User user = user(email);
        if (!group.getCreatedBy().getId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul le créateur peut supprimer ce groupe.");
        messages.findByGroupIdOrderBySentAtAsc(id).forEach(messages::delete);
        members.findByGroupIdOrderByJoinedAtAsc(id).forEach(members::delete);
        groups.delete(group);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> members(Long id, String email) { requireMember(id, email); return this.members.findByGroupIdOrderByJoinedAtAsc(id).stream().map(MemberResponse::of).toList(); }

    @Transactional(readOnly = true)
    public List<MessageResponse> messages(Long id, String email) { requireMember(id, email); return this.messages.findTop100ByGroupIdOrderBySentAtDesc(id).stream().sorted(java.util.Comparator.comparing(GroupMessage::getSentAt)).map(MessageResponse::of).toList(); }

    @Transactional
    public MessageResponse send(Long id, SendMessageRequest request, String email) {
        requireMember(id, email); StudyGroup group = group(id); User user = user(email);
        return MessageResponse.of(messages.save(GroupMessage.builder().group(group).author(user).content(request.content().trim()).build()));
    }
    @Transactional
    public GroupResponse updateImage(Long id, String email, String image) {
        StudyGroup group = group(id); User user = user(email);
        if (!group.getCreatedBy().getId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul le créateur peut modifier ce groupe.");
        group.setImage(image);
        return GroupResponse.of(groups.save(group), members.countByGroupId(id));
    }
    private void requireMember(Long id, String email) { if (members.findByGroupIdAndUserId(id, user(email).getId()).isEmpty()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous devez rejoindre ce groupe."); }
    private void requireAdmin(Long id, String email) {
        User current = user(email);
        GroupMember member = members.findByGroupIdAndUserId(id, current.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous devez être membre du groupe."));
        if (!"ADMIN".equals(member.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul l'administrateur peut inviter un utilisateur.");
    }
    private StudyGroup group(Long id) { return groups.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Groupe introuvable.")); }
    private User user(String email) { return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable.")); }
}
