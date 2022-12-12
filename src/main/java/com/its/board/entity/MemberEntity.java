package com.its.board.entity;

import com.its.board.dto.MemberDTO;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "member_table")
public class MemberEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String memberEmail;

    @Column(length = 20, nullable = false)
    private String memberPassword;

    @Column(length = 20, nullable = false)
    private String memberName;

    @Column
    private int memberAge;

    @Column(length = 30)
    private String memberPhone;

//    // 회원(1)-게시글(N) 연관관계(on delete cascade)
//    @OneToMany(mappedBy = "memberEntity", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
//    private List<BoardEntity> boardEntityList = new ArrayList<>();
//
//    // 회원(1)-댓글(N) 연관관계(on delete cascade)
//    @OneToMany(mappedBy = "memberEntity", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
//    private List<CommentEntity> commentEntityList = new ArrayList<>();

    // 회원(1)-게시글(N) 연관관계(on delete set null)
    @OneToMany(mappedBy = "memberEntity", cascade = CascadeType.PERSIST, orphanRemoval = false, fetch = FetchType.LAZY)
    private List<BoardEntity> boardEntityList = new ArrayList<>();

    // 회원(1)-댓글(N) 연관관계(on delete set null)
    @OneToMany(mappedBy = "memberEntity", cascade = CascadeType.PERSIST, orphanRemoval = false, fetch = FetchType.LAZY)
    private List<CommentEntity> commentEntityList = new ArrayList<>();

    // 회원 삭제시 게시글의 member_id, board_writer 컬럼
    // 댓글의 member_id, comment_writer 컬럼을 null 로 세팅
    // 회원 삭제 요청이 있는 경우 먼저 실행되는 메서드
    @PreRemove
    private void preRemove() {
        boardEntityList.forEach(board -> {
            board.setMemberEntity(null); // board_table.member_id
            board.setBoardWriter("탈퇴회원"); // board_table.board_writer
        });
        commentEntityList.forEach(comment -> {
            comment.setMemberEntity(null);
            comment.setCommentWriter("탈퇴회원");
        });
    }

    public static MemberEntity toSaveEntity(MemberDTO memberDTO) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setMemberEmail(memberDTO.getMemberEmail());
        memberEntity.setMemberPassword(memberDTO.getMemberPassword());
        memberEntity.setMemberName(memberDTO.getMemberName());
        memberEntity.setMemberAge(memberDTO.getMemberAge());
        memberEntity.setMemberPhone(memberDTO.getMemberPhone());
        return memberEntity;
    }


}




