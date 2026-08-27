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
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Service @RequiredArgsConstructor
public class GroupService {
    private final StudyGroupRepository groups;
    private final GroupMemberRepository members;
    private final GroupMessageRepository messages;
    private final UserRepository users;
    private final GroupInvitationRepository invitations;
    private final GroupMessageDeletionRepository deletions;

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

    @Transactional
    public void leave(Long id, String email) { User current = user(email); if (group(id).getCreatedBy().getId().equals(current.getId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le créateur ne peut pas quitter son groupe."); members.deleteByGroupIdAndUserId(id, current.getId()); }

    @Transactional
    public void removeMember(Long id, Long memberId, String email) {
        StudyGroup group = group(id); User current = user(email);
        requireAdmin(id, email);
        if (group.getCreatedBy().getId().equals(memberId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L’administrateur ne peut pas être supprimé du groupe.");
        if (current.getId().equals(memberId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilisez l’option quitter pour sortir du groupe.");
        if (members.findByGroupIdAndUserId(id, memberId).isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Membre introuvable dans ce groupe.");
        members.deleteByGroupIdAndUserId(id, memberId);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> messages(Long id, String email) { requireMember(id, email); Long userId = user(email).getId(); return this.messages.findTop100ByGroupIdOrderBySentAtDesc(id).stream().filter(message -> !deletions.existsByMessageIdAndUserId(message.getId(), userId)).sorted(java.util.Comparator.comparing(GroupMessage::getSentAt)).map(MessageResponse::of).toList(); }

    @Transactional
    public void markMessagesRead(Long id, String email) {
        requireMember(id, email); messages.markUnreadAsRead(id, user(email).getId(), LocalDateTime.now());
    }

    @Transactional
    public MessageResponse send(Long id, SendMessageRequest request, String email) {
        requireMember(id, email); StudyGroup group = group(id); User user = user(email);
        return MessageResponse.of(messages.save(GroupMessage.builder().group(group).author(user).content(request.content().trim()).messageType("TEXT").build()));
    }
    @Transactional
    public MessageResponse sendAttachment(Long id, MultipartFile file, String email) throws java.io.IOException {
        requireMember(id, email);
        return MessageResponse.of(messages.save(GroupMessage.builder().group(group(id)).author(user(email)).content(file.getOriginalFilename() == null ? "Fichier" : file.getOriginalFilename()).messageType(file.getContentType() != null && file.getContentType().startsWith("audio/") ? "AUDIO" : "FILE").fileName(file.getOriginalFilename()).contentType(file.getContentType()).attachmentData(Base64.getEncoder().encodeToString(file.getBytes())).build()));
    }
    @Transactional
    public MessageResponse markDelivered(Long id, String email) {
        GroupMessage message = messages.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message introuvable."));
        if (message.getDeliveredAt() == null) message.setDeliveredAt(LocalDateTime.now());
        return MessageResponse.of(messages.save(message));
    }
    @Transactional
    public MessageResponse react(Long groupId, Long messageId, String emoji, String email) {
        requireMember(groupId, email);
        GroupMessage message = messages.findById(messageId).filter(item -> item.getGroup().getId().equals(groupId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message introuvable."));
        if (!Map.of("👍", true, "❤️", true, "😂", true, "😮", true, "😢", true, "🙏", true, "👏", true).containsKey(emoji)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Réaction non supportée.");
        Long userId = user(email).getId();
        Map<String,String> byUser = reactionUsers(message.getReactionUsers());
        String previous = byUser.put(String.valueOf(userId), emoji);
        Map<String,Integer> reactions = MessageResponse.parse(message.getReactions());
        if (previous != null && !previous.equals(emoji)) decrement(reactions, previous);
        if (previous == null || !previous.equals(emoji)) reactions.put(emoji, reactions.getOrDefault(emoji, 0) + 1);
        message.setReactionUsers(serializeReactionUsers(byUser));
        message.setReactions(MessageResponse.serialize(reactions));
        return MessageResponse.of(messages.save(message));
    }
    private void decrement(Map<String,Integer> reactions, String emoji) { int count = reactions.getOrDefault(emoji, 0) - 1; if (count <= 0) reactions.remove(emoji); else reactions.put(emoji, count); }
    private Map<String,String> reactionUsers(String value) { Map<String,String> result = new java.util.LinkedHashMap<>(); if (value == null || value.isBlank()) return result; for (String item : value.split(";")) { String[] pair = item.split(":", 2); if (pair.length == 2) result.put(pair[0], pair[1]); } return result; }
    private String serializeReactionUsers(Map<String,String> values) { StringBuilder result = new StringBuilder(); values.forEach((id, emoji) -> { if (result.length() > 0) result.append(';'); result.append(id).append(':').append(emoji); }); return result.toString(); }
    @Transactional
    public void deleteMessage(Long groupId, Long messageId, String scope, String email) {
        requireMember(groupId, email); User current = user(email);
        GroupMessage message = messages.findById(messageId).filter(item -> item.getGroup().getId().equals(groupId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message introuvable."));
        if (!"me".equals(scope) && !"everyone".equals(scope)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Portée de suppression invalide.");
        if ("me".equals(scope)) { if (!deletions.existsByMessageIdAndUserId(messageId, current.getId())) deletions.save(GroupMessageDeletion.builder().message(message).user(current).build()); return; }
        if (!message.getAuthor().getId().equals(current.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul l’auteur peut supprimer ce message pour tout le monde.");
        message.setContent("Ce message a été supprimé"); message.setMessageType("DELETED"); message.setFileName(null); message.setContentType(null); message.setAttachmentData(null); message.setReactions(null); messages.save(message);
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
